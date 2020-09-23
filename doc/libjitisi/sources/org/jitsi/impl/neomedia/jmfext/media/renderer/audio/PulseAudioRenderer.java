package org.jitsi.impl.neomedia.jmfext.media.renderer.audio;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.GainControl;
import javax.media.MediaLocator;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import org.jitsi.impl.neomedia.device.AudioSystem;
import org.jitsi.impl.neomedia.device.AudioSystem.DataFlow;
import org.jitsi.impl.neomedia.device.PulseAudioSystem;
import org.jitsi.impl.neomedia.pulseaudio.PA;
import org.jitsi.impl.neomedia.pulseaudio.PA.stream_request_cb_t;
import org.jitsi.service.neomedia.BasicVolumeControl;
import org.jitsi.util.StringUtils;

public class PulseAudioRenderer extends AbstractAudioRenderer<PulseAudioSystem> {
    private static final String PLUGIN_NAME = "PulseAudio Renderer";
    private static final boolean SOFTWARE_GAIN = false;
    private static final Format[] SUPPORTED_INPUT_FORMATS;
    private int channels;
    private boolean corked;
    private long cvolume;
    private String dev;
    private float gainControlLevel;
    private final String mediaRole;
    private long stream;
    private final stream_request_cb_t writeCallback;

    static {
        Format[] formatArr = new Format[1];
        formatArr[0] = new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, -1, 0, 1, -1, -1.0d, Format.byteArray);
        SUPPORTED_INPUT_FORMATS = formatArr;
    }

    public PulseAudioRenderer() {
        this(null);
    }

    public PulseAudioRenderer(String mediaRole) {
        DataFlow dataFlow;
        AudioSystem pulseAudioSystem = PulseAudioSystem.getPulseAudioSystem();
        if (mediaRole == null || PulseAudioSystem.MEDIA_ROLE_PHONE.equals(mediaRole)) {
            dataFlow = DataFlow.PLAYBACK;
        } else {
            dataFlow = DataFlow.NOTIFY;
        }
        super(pulseAudioSystem, dataFlow);
        this.corked = true;
        this.writeCallback = new stream_request_cb_t() {
            public void callback(long s, int nbytes) {
                ((PulseAudioSystem) PulseAudioRenderer.this.audioSystem).signalMainloop(false);
            }
        };
        if (this.audioSystem == null) {
            throw new IllegalStateException("audioSystem");
        }
        if (mediaRole == null) {
            mediaRole = PulseAudioSystem.MEDIA_ROLE_PHONE;
        }
        this.mediaRole = mediaRole;
    }

    public void close() {
        ((PulseAudioSystem) this.audioSystem).lockMainloop();
        long stream;
        long cvolume;
        try {
            stream = this.stream;
            if (stream != 0) {
                stopWithMainloopLock();
                cvolume = this.cvolume;
                this.cvolume = 0;
                this.stream = 0;
                this.corked = true;
                this.dev = null;
                ((PulseAudioSystem) this.audioSystem).signalMainloop(false);
                if (cvolume != 0) {
                    PA.cvolume_free(cvolume);
                }
                PA.stream_disconnect(stream);
                PA.stream_unref(stream);
            }
            super.close();
            ((PulseAudioSystem) this.audioSystem).unlockMainloop();
        } catch (Throwable th) {
            Throwable th2 = th;
            ((PulseAudioSystem) this.audioSystem).unlockMainloop();
        }
    }

    private void cork(boolean b) {
        try {
            PulseAudioSystem.corkStream(this.stream, b);
            this.corked = b;
            ((PulseAudioSystem) this.audioSystem).signalMainloop(false);
        } catch (IOException ioe) {
            throw new UndeclaredThrowableException(ioe);
        } catch (Throwable th) {
            Throwable th2 = th;
            ((PulseAudioSystem) this.audioSystem).signalMainloop(false);
        }
    }

    private String getLocatorDev() {
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

    public String getName() {
        return PLUGIN_NAME;
    }

    public Format[] getSupportedInputFormats() {
        return (Format[]) SUPPORTED_INPUT_FORMATS.clone();
    }

    public void open() throws ResourceUnavailableException {
        ((PulseAudioSystem) this.audioSystem).lockMainloop();
        try {
            openWithMainloopLock();
            super.open();
            ((PulseAudioSystem) this.audioSystem).unlockMainloop();
        } catch (Throwable th) {
            Throwable th2 = th;
            ((PulseAudioSystem) this.audioSystem).unlockMainloop();
        }
    }

    /* JADX WARNING: Missing block: B:95:?, code skipped:
            return;
     */
    private void openWithMainloopLock() throws javax.media.ResourceUnavailableException {
        /*
        r25 = this;
        r0 = r25;
        r8 = r0.stream;
        r10 = 0;
        r7 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));
        if (r7 == 0) goto L_0x000b;
    L_0x000a:
        return;
    L_0x000b:
        r0 = r25;
        r14 = r0.inputFormat;
        r14 = (javax.media.format.AudioFormat) r14;
        r8 = r14.getSampleRate();
        r0 = (int) r8;
        r21 = r0;
        r12 = r14.getChannels();
        r22 = r14.getSampleSizeInBits();
        r7 = -1;
        r0 = r21;
        if (r0 != r7) goto L_0x0032;
    L_0x0025:
        r8 = org.jitsi.impl.neomedia.MediaUtils.MAX_AUDIO_SAMPLE_RATE;
        r10 = -4616189618054758400; // 0xbff0000000000000 float:0.0 double:-1.0;
        r7 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));
        if (r7 == 0) goto L_0x0032;
    L_0x002d:
        r8 = org.jitsi.impl.neomedia.MediaUtils.MAX_AUDIO_SAMPLE_RATE;
        r0 = (int) r8;
        r21 = r0;
    L_0x0032:
        r7 = -1;
        if (r12 != r7) goto L_0x0036;
    L_0x0035:
        r12 = 1;
    L_0x0036:
        r7 = -1;
        r0 = r22;
        if (r0 != r7) goto L_0x003d;
    L_0x003b:
        r22 = 16;
    L_0x003d:
        r2 = 0;
        r13 = 0;
        r0 = r25;
        r7 = r0.audioSystem;	 Catch:{ IllegalStateException -> 0x0069, RuntimeException -> 0x006d }
        r7 = (org.jitsi.impl.neomedia.device.PulseAudioSystem) r7;	 Catch:{ IllegalStateException -> 0x0069, RuntimeException -> 0x006d }
        r8 = r25.getClass();	 Catch:{ IllegalStateException -> 0x0069, RuntimeException -> 0x006d }
        r8 = r8.getName();	 Catch:{ IllegalStateException -> 0x0069, RuntimeException -> 0x006d }
        r0 = r25;
        r9 = r0.mediaRole;	 Catch:{ IllegalStateException -> 0x0069, RuntimeException -> 0x006d }
        r0 = r21;
        r2 = r7.createStream(r0, r12, r8, r9);	 Catch:{ IllegalStateException -> 0x0069, RuntimeException -> 0x006d }
        r0 = r25;
        r0.channels = r12;	 Catch:{ IllegalStateException -> 0x0069, RuntimeException -> 0x006d }
    L_0x005c:
        if (r13 == 0) goto L_0x0071;
    L_0x005e:
        r20 = new javax.media.ResourceUnavailableException;
        r20.m251init();
        r0 = r20;
        r0.initCause(r13);
        throw r20;
    L_0x0069:
        r18 = move-exception;
        r13 = r18;
        goto L_0x005c;
    L_0x006d:
        r19 = move-exception;
        r13 = r19;
        goto L_0x005c;
    L_0x0071:
        r8 = 0;
        r7 = (r2 > r8 ? 1 : (r2 == r8 ? 0 : -1));
        if (r7 != 0) goto L_0x007f;
    L_0x0077:
        r7 = new javax.media.ResourceUnavailableException;
        r8 = "stream";
        r7.m252init(r8);
        throw r7;
    L_0x007f:
        r7 = -1;
        r8 = r21 / 100;
        r8 = r8 * 2;
        r8 = r8 * r12;
        r9 = r22 / 8;
        r8 = r8 * r9;
        r9 = -1;
        r10 = -1;
        r11 = -1;
        r5 = org.jitsi.impl.neomedia.pulseaudio.PA.buffer_attr_new(r7, r8, r9, r10, r11);	 Catch:{ all -> 0x009d }
        r8 = 0;
        r7 = (r5 > r8 ? 1 : (r5 == r8 ? 0 : -1));
        if (r7 != 0) goto L_0x00ac;
    L_0x0095:
        r7 = new javax.media.ResourceUnavailableException;	 Catch:{ all -> 0x009d }
        r8 = "pa_buffer_attr_new";
        r7.m252init(r8);	 Catch:{ all -> 0x009d }
        throw r7;	 Catch:{ all -> 0x009d }
    L_0x009d:
        r7 = move-exception;
        r0 = r25;
        r8 = r0.stream;
        r10 = 0;
        r8 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));
        if (r8 != 0) goto L_0x00ab;
    L_0x00a8:
        org.jitsi.impl.neomedia.pulseaudio.PA.stream_unref(r2);
    L_0x00ab:
        throw r7;
    L_0x00ac:
        r24 = new org.jitsi.impl.neomedia.jmfext.media.renderer.audio.PulseAudioRenderer$2;	 Catch:{ all -> 0x00f5 }
        r24.m2528init();	 Catch:{ all -> 0x00f5 }
        r0 = r24;
        org.jitsi.impl.neomedia.pulseaudio.PA.stream_set_state_callback(r2, r0);	 Catch:{ all -> 0x00f5 }
        r4 = r25.getLocatorDev();	 Catch:{ all -> 0x00f5 }
        r7 = 8193; // 0x2001 float:1.1481E-41 double:4.048E-320;
        r8 = 0;
        r10 = 0;
        org.jitsi.impl.neomedia.pulseaudio.PA.stream_connect_playback(r2, r4, r5, r7, r8, r10);	 Catch:{ all -> 0x00f5 }
        r8 = 0;
        r7 = (r5 > r8 ? 1 : (r5 == r8 ? 0 : -1));
        if (r7 == 0) goto L_0x00ce;
    L_0x00c9:
        org.jitsi.impl.neomedia.pulseaudio.PA.buffer_attr_free(r5);	 Catch:{ all -> 0x00e6 }
        r5 = 0;
    L_0x00ce:
        r0 = r25;
        r7 = r0.audioSystem;	 Catch:{ all -> 0x00e6 }
        r7 = (org.jitsi.impl.neomedia.device.PulseAudioSystem) r7;	 Catch:{ all -> 0x00e6 }
        r8 = 2;
        r23 = r7.waitForStreamState(r2, r8);	 Catch:{ all -> 0x00e6 }
        r7 = 2;
        r0 = r23;
        if (r0 == r7) goto L_0x0100;
    L_0x00de:
        r7 = new javax.media.ResourceUnavailableException;	 Catch:{ all -> 0x00e6 }
        r8 = "stream.state";
        r7.m252init(r8);	 Catch:{ all -> 0x00e6 }
        throw r7;	 Catch:{ all -> 0x00e6 }
    L_0x00e6:
        r7 = move-exception;
        r0 = r25;
        r8 = r0.stream;	 Catch:{ all -> 0x00f5 }
        r10 = 0;
        r8 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));
        if (r8 != 0) goto L_0x00f4;
    L_0x00f1:
        org.jitsi.impl.neomedia.pulseaudio.PA.stream_disconnect(r2);	 Catch:{ all -> 0x00f5 }
    L_0x00f4:
        throw r7;	 Catch:{ all -> 0x00f5 }
    L_0x00f5:
        r7 = move-exception;
        r8 = 0;
        r8 = (r5 > r8 ? 1 : (r5 == r8 ? 0 : -1));
        if (r8 == 0) goto L_0x00ff;
    L_0x00fc:
        org.jitsi.impl.neomedia.pulseaudio.PA.buffer_attr_free(r5);	 Catch:{ all -> 0x009d }
    L_0x00ff:
        throw r7;	 Catch:{ all -> 0x009d }
    L_0x0100:
        r0 = r25;
        r7 = r0.writeCallback;	 Catch:{ all -> 0x00e6 }
        org.jitsi.impl.neomedia.pulseaudio.PA.stream_set_write_callback(r2, r7);	 Catch:{ all -> 0x00e6 }
        r16 = r25.getGainControl();	 Catch:{ all -> 0x00e6 }
        if (r16 == 0) goto L_0x0137;
    L_0x010d:
        r8 = org.jitsi.impl.neomedia.pulseaudio.PA.cvolume_new();	 Catch:{ all -> 0x00e6 }
        r0 = r25;
        r0.cvolume = r8;	 Catch:{ all -> 0x00e6 }
        r15 = 1;
        r17 = r16.getLevel();	 Catch:{ all -> 0x0164 }
        r0 = r25;
        r1 = r17;
        r0.setStreamVolume(r2, r1);	 Catch:{ all -> 0x0164 }
        r0 = r17;
        r1 = r25;
        r1.gainControlLevel = r0;	 Catch:{ all -> 0x0164 }
        r15 = 0;
        if (r15 == 0) goto L_0x0137;
    L_0x012a:
        r0 = r25;
        r8 = r0.cvolume;	 Catch:{ all -> 0x00e6 }
        org.jitsi.impl.neomedia.pulseaudio.PA.cvolume_free(r8);	 Catch:{ all -> 0x00e6 }
        r8 = 0;
        r0 = r25;
        r0.cvolume = r8;	 Catch:{ all -> 0x00e6 }
    L_0x0137:
        r0 = r25;
        r0.stream = r2;	 Catch:{ all -> 0x00e6 }
        r0 = r25;
        r0.dev = r4;	 Catch:{ all -> 0x00e6 }
        r0 = r25;
        r8 = r0.stream;	 Catch:{ all -> 0x00f5 }
        r10 = 0;
        r7 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));
        if (r7 != 0) goto L_0x014c;
    L_0x0149:
        org.jitsi.impl.neomedia.pulseaudio.PA.stream_disconnect(r2);	 Catch:{ all -> 0x00f5 }
    L_0x014c:
        r8 = 0;
        r7 = (r5 > r8 ? 1 : (r5 == r8 ? 0 : -1));
        if (r7 == 0) goto L_0x0155;
    L_0x0152:
        org.jitsi.impl.neomedia.pulseaudio.PA.buffer_attr_free(r5);	 Catch:{ all -> 0x009d }
    L_0x0155:
        r0 = r25;
        r8 = r0.stream;
        r10 = 0;
        r7 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));
        if (r7 != 0) goto L_0x000a;
    L_0x015f:
        org.jitsi.impl.neomedia.pulseaudio.PA.stream_unref(r2);
        goto L_0x000a;
    L_0x0164:
        r7 = move-exception;
        if (r15 == 0) goto L_0x0174;
    L_0x0167:
        r0 = r25;
        r8 = r0.cvolume;	 Catch:{ all -> 0x00e6 }
        org.jitsi.impl.neomedia.pulseaudio.PA.cvolume_free(r8);	 Catch:{ all -> 0x00e6 }
        r8 = 0;
        r0 = r25;
        r0.cvolume = r8;	 Catch:{ all -> 0x00e6 }
    L_0x0174:
        throw r7;	 Catch:{ all -> 0x00e6 }
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.jmfext.media.renderer.audio.PulseAudioRenderer.openWithMainloopLock():void");
    }

    /* access modifiers changed from: protected */
    public void playbackDevicePropertyChange(PropertyChangeEvent ev) {
        boolean start = true;
        ((PulseAudioSystem) this.audioSystem).lockMainloop();
        try {
            boolean open;
            if (this.stream != 0) {
                open = true;
            } else {
                open = false;
            }
            if (open) {
                String locatorDev = getLocatorDev();
                if (!(StringUtils.isEquals(this.dev, locatorDev) || StringUtils.isEquals(PA.stream_get_device_name(this.stream), locatorDev))) {
                    if (this.corked) {
                        start = false;
                    }
                    close();
                    open();
                    if (start) {
                        start();
                    }
                }
            }
            ((PulseAudioSystem) this.audioSystem).unlockMainloop();
        } catch (ResourceUnavailableException rue) {
            throw new UndeclaredThrowableException(rue);
        } catch (Throwable th) {
            Throwable th2 = th;
            ((PulseAudioSystem) this.audioSystem).unlockMainloop();
        }
    }

    public int process(Buffer buffer) {
        int ret = 0;
        if (buffer.isDiscard() || buffer.getLength() <= 0) {
            return ret;
        }
        ((PulseAudioSystem) this.audioSystem).lockMainloop();
        try {
            ret = processWithMainloopLock(buffer);
            if (ret == 1 || buffer.getLength() <= 0) {
                return ret;
            }
            return ret | 2;
        } finally {
            ((PulseAudioSystem) this.audioSystem).unlockMainloop();
        }
    }

    private int processWithMainloopLock(Buffer buffer) {
        if (this.stream == 0 || this.corked) {
            return 1;
        }
        int writableSize = PA.stream_writable_size(this.stream);
        if (writableSize <= 0) {
            ((PulseAudioSystem) this.audioSystem).waitMainloop();
            return 0;
        }
        byte[] data = (byte[]) buffer.getData();
        int offset = buffer.getOffset();
        int length = buffer.getLength();
        if (length < writableSize) {
            writableSize = length;
        }
        GainControl gainControl = getGainControl();
        if (gainControl != null) {
            if (this.cvolume != 0) {
                float gainControlLevel = gainControl.getLevel();
                if (this.gainControlLevel != gainControlLevel) {
                    this.gainControlLevel = gainControlLevel;
                    setStreamVolume(this.stream, gainControlLevel);
                }
            } else if (length > 0) {
                BasicVolumeControl.applyGain(gainControl, data, offset, writableSize);
            }
        }
        int writtenSize = PA.stream_write(this.stream, data, offset, writableSize, null, 0, 0);
        if (writtenSize < 0) {
            return 1;
        }
        buffer.setLength(length - writtenSize);
        buffer.setOffset(offset + writtenSize);
        return 0;
    }

    private void setStreamVolume(long stream, float level) {
        PA.cvolume_set(this.cvolume, this.channels, PA.sw_volume_from_linear((double) (2.0f * level)));
        long o = PA.context_set_sink_input_volume(((PulseAudioSystem) this.audioSystem).getContext(), PA.stream_get_index(stream), this.cvolume, null);
        if (o != 0) {
            PA.operation_unref(o);
        }
    }

    public void start() {
        ((PulseAudioSystem) this.audioSystem).lockMainloop();
        try {
            if (this.stream == 0) {
                openWithMainloopLock();
            }
            cork(false);
            ((PulseAudioSystem) this.audioSystem).unlockMainloop();
        } catch (ResourceUnavailableException rue) {
            throw new UndeclaredThrowableException(rue);
        } catch (Throwable th) {
            Throwable th2 = th;
            ((PulseAudioSystem) this.audioSystem).unlockMainloop();
        }
    }

    public void stop() {
        ((PulseAudioSystem) this.audioSystem).lockMainloop();
        try {
            stopWithMainloopLock();
        } finally {
            ((PulseAudioSystem) this.audioSystem).unlockMainloop();
        }
    }

    private void stopWithMainloopLock() {
        if (this.stream != 0) {
            cork(true);
        }
    }
}
