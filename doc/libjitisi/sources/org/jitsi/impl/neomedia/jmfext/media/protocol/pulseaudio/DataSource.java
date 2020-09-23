package org.jitsi.impl.neomedia.jmfext.media.protocol.pulseaudio;

import java.io.IOException;
import java.util.StringTokenizer;
import javax.media.Buffer;
import javax.media.GainControl;
import javax.media.MediaLocator;
import javax.media.control.FormatControl;
import org.jitsi.impl.neomedia.MediaServiceImpl;
import org.jitsi.impl.neomedia.NeomediaServiceUtils;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;
import org.jitsi.impl.neomedia.device.PulseAudioSystem;
import org.jitsi.impl.neomedia.jmfext.media.protocol.AbstractPullBufferCaptureDevice;
import org.jitsi.impl.neomedia.jmfext.media.protocol.AbstractPullBufferStream;
import org.jitsi.impl.neomedia.pulseaudio.PA;
import org.jitsi.impl.neomedia.pulseaudio.PA.stream_request_cb_t;
import org.jitsi.service.neomedia.BasicVolumeControl;
import org.jitsi.util.Logger;

public class DataSource extends AbstractPullBufferCaptureDevice {
    private static final int BUFFER_IN_TENS_OF_MILLIS = 10;
    /* access modifiers changed from: private|static|final */
    public static final boolean DEBUG = logger.isDebugEnabled();
    private static final int FRAGSIZE_IN_TENS_OF_MILLIS = 2;
    /* access modifiers changed from: private|static|final */
    public static final boolean SOFTWARE_GAIN;
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(DataSource.class);

    private class PulseAudioStream extends AbstractPullBufferStream<DataSource> {
        private byte[] buffer;
        private int channels;
        private boolean corked = true;
        private long cvolume;
        private int fragsize;
        private final GainControl gainControl;
        private float gainControlLevel;
        private int length;
        private int offset;
        /* access modifiers changed from: private|final */
        public final PulseAudioSystem pulseAudioSystem = PulseAudioSystem.getPulseAudioSystem();
        private final stream_request_cb_t readCallback = new stream_request_cb_t() {
            public void callback(long s, int nbytes) {
                PulseAudioStream.this.readCallback(s, nbytes);
            }
        };
        private long stream;

        public PulseAudioStream(FormatControl formatControl) {
            super(DataSource.this, formatControl);
            if (this.pulseAudioSystem == null) {
                throw new IllegalStateException("pulseAudioSystem");
            }
            MediaServiceImpl mediaServiceImpl = NeomediaServiceUtils.getMediaServiceImpl();
            this.gainControl = mediaServiceImpl == null ? null : (GainControl) mediaServiceImpl.getInputVolumeControl();
        }

        public void read(Buffer buffer) throws IOException {
            this.pulseAudioSystem.lockMainloop();
            try {
                if (this.stream == 0) {
                    throw new IOException("stream");
                }
                byte[] data = AbstractCodec2.validateByteArraySize(buffer, this.fragsize, DataSource.DEBUG);
                int toRead = this.fragsize;
                int offset = 0;
                int length = 0;
                while (toRead > 0 && !this.corked) {
                    if (this.length <= 0) {
                        this.pulseAudioSystem.waitMainloop();
                    } else {
                        int toCopy = toRead < this.length ? toRead : this.length;
                        System.arraycopy(this.buffer, this.offset, data, offset, toCopy);
                        this.offset += toCopy;
                        this.length -= toCopy;
                        if (this.length <= 0) {
                            this.offset = 0;
                            this.length = 0;
                        }
                        toRead -= toCopy;
                        offset += toCopy;
                        length += toCopy;
                    }
                }
                buffer.setFlags(128);
                buffer.setLength(length);
                buffer.setOffset(0);
                buffer.setTimeStamp(System.nanoTime());
                if (this.gainControl != null) {
                    if (!DataSource.SOFTWARE_GAIN && this.cvolume != 0) {
                        float gainControlLevel = this.gainControl.getLevel();
                        if (this.gainControlLevel != gainControlLevel) {
                            this.gainControlLevel = gainControlLevel;
                            setStreamVolume(this.stream, gainControlLevel);
                        }
                    } else if (length > 0) {
                        BasicVolumeControl.applyGain(this.gainControl, data, 0, length);
                    }
                }
                this.pulseAudioSystem.unlockMainloop();
            } catch (Throwable th) {
                this.pulseAudioSystem.unlockMainloop();
            }
        }

        /* access modifiers changed from: private */
        public void readCallback(long stream, int length) {
            try {
                int peeked;
                if (this.corked) {
                    peeked = 0;
                } else {
                    int offset;
                    if (this.buffer == null || this.buffer.length < length) {
                        this.buffer = new byte[length];
                        this.offset = 0;
                        this.length = 0;
                        offset = 0;
                    } else {
                        offset = this.offset + this.length;
                        if (offset + length > this.buffer.length) {
                            int overflow = (this.length + length) - this.buffer.length;
                            if (overflow > 0) {
                                if (overflow >= this.length) {
                                    if (DataSource.DEBUG && DataSource.logger.isDebugEnabled()) {
                                        DataSource.logger.debug("Dropping " + this.length + " bytes!");
                                    }
                                    this.offset = 0;
                                    this.length = 0;
                                    offset = 0;
                                } else {
                                    if (DataSource.DEBUG && DataSource.logger.isDebugEnabled()) {
                                        DataSource.logger.debug("Dropping " + overflow + " bytes!");
                                    }
                                    this.offset += overflow;
                                    this.length -= overflow;
                                }
                            }
                            if (this.length > 0) {
                                int i = 0;
                                while (i < this.length) {
                                    this.buffer[i] = this.buffer[this.offset];
                                    i++;
                                    this.offset++;
                                }
                                this.offset = 0;
                                offset = this.length;
                            }
                        }
                    }
                    peeked = PA.stream_peek(stream, this.buffer, offset);
                }
                PA.stream_drop(stream);
                this.length += peeked;
            } finally {
                this.pulseAudioSystem.signalMainloop(DataSource.DEBUG);
            }
        }

        public void connect() throws IOException {
            this.pulseAudioSystem.lockMainloop();
            try {
                connectWithMainloopLock();
            } finally {
                this.pulseAudioSystem.unlockMainloop();
            }
        }

        /* JADX WARNING: Missing block: B:96:?, code skipped:
            return;
     */
        private void connectWithMainloopLock() throws java.io.IOException {
            /*
            r28 = this;
            r0 = r28;
            r0 = r0.stream;
            r24 = r0;
            r26 = 0;
            r6 = (r24 > r26 ? 1 : (r24 == r26 ? 0 : -1));
            if (r6 == 0) goto L_0x000d;
        L_0x000c:
            return;
        L_0x000d:
            r13 = r28.getFormat();
            r13 = (javax.media.format.AudioFormat) r13;
            r24 = r13.getSampleRate();
            r0 = r24;
            r0 = (int) r0;
            r19 = r0;
            r11 = r13.getChannels();
            r20 = r13.getSampleSizeInBits();
            r6 = -1;
            r0 = r19;
            if (r0 != r6) goto L_0x0038;
        L_0x0029:
            r24 = org.jitsi.impl.neomedia.MediaUtils.MAX_AUDIO_SAMPLE_RATE;
            r26 = -4616189618054758400; // 0xbff0000000000000 float:0.0 double:-1.0;
            r6 = (r24 > r26 ? 1 : (r24 == r26 ? 0 : -1));
            if (r6 == 0) goto L_0x0038;
        L_0x0031:
            r24 = org.jitsi.impl.neomedia.MediaUtils.MAX_AUDIO_SAMPLE_RATE;
            r0 = r24;
            r0 = (int) r0;
            r19 = r0;
        L_0x0038:
            r6 = -1;
            if (r11 != r6) goto L_0x003c;
        L_0x003b:
            r11 = 1;
        L_0x003c:
            r6 = -1;
            r0 = r20;
            if (r0 != r6) goto L_0x0043;
        L_0x0041:
            r20 = 16;
        L_0x0043:
            r4 = 0;
            r12 = 0;
            r0 = r28;
            r6 = r0.pulseAudioSystem;	 Catch:{ IllegalStateException -> 0x006d, RuntimeException -> 0x0071 }
            r9 = r28.getClass();	 Catch:{ IllegalStateException -> 0x006d, RuntimeException -> 0x0071 }
            r9 = r9.getName();	 Catch:{ IllegalStateException -> 0x006d, RuntimeException -> 0x0071 }
            r23 = "phone";
            r0 = r19;
            r1 = r23;
            r4 = r6.createStream(r0, r11, r9, r1);	 Catch:{ IllegalStateException -> 0x006d, RuntimeException -> 0x0071 }
            r0 = r28;
            r0.channels = r11;	 Catch:{ IllegalStateException -> 0x006d, RuntimeException -> 0x0071 }
        L_0x0060:
            if (r12 == 0) goto L_0x0075;
        L_0x0062:
            r16 = new java.io.IOException;
            r16.<init>();
            r0 = r16;
            r0.initCause(r12);
            throw r16;
        L_0x006d:
            r17 = move-exception;
            r12 = r17;
            goto L_0x0060;
        L_0x0071:
            r18 = move-exception;
            r12 = r18;
            goto L_0x0060;
        L_0x0075:
            r24 = 0;
            r6 = (r4 > r24 ? 1 : (r4 == r24 ? 0 : -1));
            if (r6 != 0) goto L_0x0083;
        L_0x007b:
            r6 = new java.io.IOException;
            r9 = "stream";
            r6.<init>(r9);
            throw r6;
        L_0x0083:
            r6 = r19 / 100;
            r6 = r6 * r11;
            r9 = r20 / 8;
            r10 = r6 * r9;
            r6 = r10 * 2;
            r0 = r28;
            r0.fragsize = r6;	 Catch:{ all -> 0x00bc }
            r6 = r10 * 10;
            r6 = new byte[r6];	 Catch:{ all -> 0x00bc }
            r0 = r28;
            r0.buffer = r6;	 Catch:{ all -> 0x00bc }
            r6 = -1;
            r9 = -1;
            r23 = -1;
            r24 = -1;
            r0 = r28;
            r0 = r0.fragsize;	 Catch:{ all -> 0x00bc }
            r25 = r0;
            r0 = r23;
            r1 = r24;
            r2 = r25;
            r7 = org.jitsi.impl.neomedia.pulseaudio.PA.buffer_attr_new(r6, r9, r0, r1, r2);	 Catch:{ all -> 0x00bc }
            r24 = 0;
            r6 = (r7 > r24 ? 1 : (r7 == r24 ? 0 : -1));
            if (r6 != 0) goto L_0x00cd;
        L_0x00b4:
            r6 = new java.io.IOException;	 Catch:{ all -> 0x00bc }
            r9 = "pa_buffer_attr_new";
            r6.<init>(r9);	 Catch:{ all -> 0x00bc }
            throw r6;	 Catch:{ all -> 0x00bc }
        L_0x00bc:
            r6 = move-exception;
            r0 = r28;
            r0 = r0.stream;
            r24 = r0;
            r26 = 0;
            r9 = (r24 > r26 ? 1 : (r24 == r26 ? 0 : -1));
            if (r9 != 0) goto L_0x00cc;
        L_0x00c9:
            org.jitsi.impl.neomedia.pulseaudio.PA.stream_unref(r4);
        L_0x00cc:
            throw r6;
        L_0x00cd:
            r22 = new org.jitsi.impl.neomedia.jmfext.media.protocol.pulseaudio.DataSource$PulseAudioStream$2;	 Catch:{ all -> 0x011a }
            r0 = r22;
            r1 = r28;
            r0.m2469init();	 Catch:{ all -> 0x011a }
            r0 = r22;
            org.jitsi.impl.neomedia.pulseaudio.PA.stream_set_state_callback(r4, r0);	 Catch:{ all -> 0x011a }
            r0 = r28;
            r6 = org.jitsi.impl.neomedia.jmfext.media.protocol.pulseaudio.DataSource.this;	 Catch:{ all -> 0x011a }
            r6 = r6.getLocatorDev();	 Catch:{ all -> 0x011a }
            r9 = 8193; // 0x2001 float:1.1481E-41 double:4.048E-320;
            org.jitsi.impl.neomedia.pulseaudio.PA.stream_connect_record(r4, r6, r7, r9);	 Catch:{ all -> 0x011a }
            r24 = 0;
            r6 = (r7 > r24 ? 1 : (r7 == r24 ? 0 : -1));
            if (r6 == 0) goto L_0x00f3;
        L_0x00ee:
            org.jitsi.impl.neomedia.pulseaudio.PA.buffer_attr_free(r7);	 Catch:{ all -> 0x0109 }
            r7 = 0;
        L_0x00f3:
            r0 = r28;
            r6 = r0.pulseAudioSystem;	 Catch:{ all -> 0x0109 }
            r9 = 2;
            r21 = r6.waitForStreamState(r4, r9);	 Catch:{ all -> 0x0109 }
            r6 = 2;
            r0 = r21;
            if (r0 == r6) goto L_0x0125;
        L_0x0101:
            r6 = new java.io.IOException;	 Catch:{ all -> 0x0109 }
            r9 = "stream.state";
            r6.<init>(r9);	 Catch:{ all -> 0x0109 }
            throw r6;	 Catch:{ all -> 0x0109 }
        L_0x0109:
            r6 = move-exception;
            r0 = r28;
            r0 = r0.stream;	 Catch:{ all -> 0x011a }
            r24 = r0;
            r26 = 0;
            r9 = (r24 > r26 ? 1 : (r24 == r26 ? 0 : -1));
            if (r9 != 0) goto L_0x0119;
        L_0x0116:
            org.jitsi.impl.neomedia.pulseaudio.PA.stream_disconnect(r4);	 Catch:{ all -> 0x011a }
        L_0x0119:
            throw r6;	 Catch:{ all -> 0x011a }
        L_0x011a:
            r6 = move-exception;
            r24 = 0;
            r9 = (r7 > r24 ? 1 : (r7 == r24 ? 0 : -1));
            if (r9 == 0) goto L_0x0124;
        L_0x0121:
            org.jitsi.impl.neomedia.pulseaudio.PA.buffer_attr_free(r7);	 Catch:{ all -> 0x00bc }
        L_0x0124:
            throw r6;	 Catch:{ all -> 0x00bc }
        L_0x0125:
            r0 = r28;
            r6 = r0.readCallback;	 Catch:{ all -> 0x0109 }
            org.jitsi.impl.neomedia.pulseaudio.PA.stream_set_read_callback(r4, r6);	 Catch:{ all -> 0x0109 }
            r6 = org.jitsi.impl.neomedia.jmfext.media.protocol.pulseaudio.DataSource.SOFTWARE_GAIN;	 Catch:{ all -> 0x0109 }
            if (r6 != 0) goto L_0x0168;
        L_0x0132:
            r0 = r28;
            r6 = r0.gainControl;	 Catch:{ all -> 0x0109 }
            if (r6 == 0) goto L_0x0168;
        L_0x0138:
            r24 = org.jitsi.impl.neomedia.pulseaudio.PA.cvolume_new();	 Catch:{ all -> 0x0109 }
            r0 = r24;
            r2 = r28;
            r2.cvolume = r0;	 Catch:{ all -> 0x0109 }
            r14 = 1;
            r0 = r28;
            r6 = r0.gainControl;	 Catch:{ all -> 0x0195 }
            r15 = r6.getLevel();	 Catch:{ all -> 0x0195 }
            r0 = r28;
            r0.setStreamVolume(r4, r15);	 Catch:{ all -> 0x0195 }
            r0 = r28;
            r0.gainControlLevel = r15;	 Catch:{ all -> 0x0195 }
            r14 = 0;
            if (r14 == 0) goto L_0x0168;
        L_0x0157:
            r0 = r28;
            r0 = r0.cvolume;	 Catch:{ all -> 0x0109 }
            r24 = r0;
            org.jitsi.impl.neomedia.pulseaudio.PA.cvolume_free(r24);	 Catch:{ all -> 0x0109 }
            r24 = 0;
            r0 = r24;
            r2 = r28;
            r2.cvolume = r0;	 Catch:{ all -> 0x0109 }
        L_0x0168:
            r0 = r28;
            r0.stream = r4;	 Catch:{ all -> 0x0109 }
            r0 = r28;
            r0 = r0.stream;	 Catch:{ all -> 0x011a }
            r24 = r0;
            r26 = 0;
            r6 = (r24 > r26 ? 1 : (r24 == r26 ? 0 : -1));
            if (r6 != 0) goto L_0x017b;
        L_0x0178:
            org.jitsi.impl.neomedia.pulseaudio.PA.stream_disconnect(r4);	 Catch:{ all -> 0x011a }
        L_0x017b:
            r24 = 0;
            r6 = (r7 > r24 ? 1 : (r7 == r24 ? 0 : -1));
            if (r6 == 0) goto L_0x0184;
        L_0x0181:
            org.jitsi.impl.neomedia.pulseaudio.PA.buffer_attr_free(r7);	 Catch:{ all -> 0x00bc }
        L_0x0184:
            r0 = r28;
            r0 = r0.stream;
            r24 = r0;
            r26 = 0;
            r6 = (r24 > r26 ? 1 : (r24 == r26 ? 0 : -1));
            if (r6 != 0) goto L_0x000c;
        L_0x0190:
            org.jitsi.impl.neomedia.pulseaudio.PA.stream_unref(r4);
            goto L_0x000c;
        L_0x0195:
            r6 = move-exception;
            if (r14 == 0) goto L_0x01a9;
        L_0x0198:
            r0 = r28;
            r0 = r0.cvolume;	 Catch:{ all -> 0x0109 }
            r24 = r0;
            org.jitsi.impl.neomedia.pulseaudio.PA.cvolume_free(r24);	 Catch:{ all -> 0x0109 }
            r24 = 0;
            r0 = r24;
            r2 = r28;
            r2.cvolume = r0;	 Catch:{ all -> 0x0109 }
        L_0x01a9:
            throw r6;	 Catch:{ all -> 0x0109 }
            */
            throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.jmfext.media.protocol.pulseaudio.DataSource$PulseAudioStream.connectWithMainloopLock():void");
        }

        private void cork(boolean b) throws IOException {
            try {
                PulseAudioSystem.corkStream(this.stream, b);
                this.corked = b;
            } finally {
                this.pulseAudioSystem.signalMainloop(DataSource.DEBUG);
            }
        }

        public void disconnect() throws IOException {
            this.pulseAudioSystem.lockMainloop();
            long stream;
            long cvolume;
            try {
                stream = this.stream;
                if (stream != 0) {
                    stopWithMainloopLock();
                    cvolume = this.cvolume;
                    this.cvolume = 0;
                    this.stream = 0;
                    this.buffer = null;
                    this.corked = true;
                    this.fragsize = 0;
                    this.length = 0;
                    this.offset = 0;
                    this.pulseAudioSystem.signalMainloop(DataSource.DEBUG);
                    if (cvolume != 0) {
                        PA.cvolume_free(cvolume);
                    }
                    PA.stream_disconnect(stream);
                    PA.stream_unref(stream);
                }
                this.pulseAudioSystem.unlockMainloop();
            } catch (Throwable th) {
                this.pulseAudioSystem.unlockMainloop();
            }
        }

        private void setStreamVolume(long stream, float level) {
            PA.cvolume_set(this.cvolume, this.channels, PA.sw_volume_from_linear((double) (2.0f * level)));
            long o = PA.context_set_source_output_volume(this.pulseAudioSystem.getContext(), PA.stream_get_index(stream), this.cvolume, null);
            if (o != 0) {
                PA.operation_unref(o);
            }
        }

        public void start() throws IOException {
            this.pulseAudioSystem.lockMainloop();
            try {
                if (this.stream == 0) {
                    connectWithMainloopLock();
                }
                cork(DataSource.DEBUG);
                super.start();
            } finally {
                this.pulseAudioSystem.unlockMainloop();
            }
        }

        public void stop() throws IOException {
            this.pulseAudioSystem.lockMainloop();
            try {
                stopWithMainloopLock();
            } finally {
                this.pulseAudioSystem.unlockMainloop();
            }
        }

        private void stopWithMainloopLock() throws IOException {
            if (this.stream != 0) {
                cork(true);
            }
            super.stop();
        }
    }

    static {
        boolean softwareGain = true;
        try {
            String libraryVersion = PA.get_library_version();
            if (libraryVersion != null) {
                StringTokenizer st = new StringTokenizer(libraryVersion, ".");
                int major = Integer.parseInt(st.nextToken());
                int minor = Integer.parseInt(st.nextToken());
                if (major >= 1 && minor >= 0) {
                    softwareGain = DEBUG;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Will control the volume through the native PulseAudio API.");
                    }
                }
            }
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                ThreadDeath t2 = (ThreadDeath) t;
            }
        }
        SOFTWARE_GAIN = softwareGain;
    }

    /* access modifiers changed from: protected */
    public PulseAudioStream createStream(int streamIndex, FormatControl formatControl) {
        return new PulseAudioStream(formatControl);
    }

    /* access modifiers changed from: protected */
    public void doDisconnect() {
        synchronized (getStreamSyncRoot()) {
            Object[] streams = streams();
            if (!(streams == null || streams.length == 0)) {
                for (Object stream : streams) {
                    if (stream instanceof PulseAudioStream) {
                        try {
                            ((PulseAudioStream) stream).disconnect();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        }
        super.doDisconnect();
    }

    /* access modifiers changed from: private */
    public String getLocatorDev() {
        MediaLocator locator = getLocator();
        if (locator == null) {
            return null;
        }
        String locatorDev = locator.getRemainder();
        if (locatorDev == null || locatorDev.length() > 0) {
            return locatorDev;
        }
        return null;
    }
}
