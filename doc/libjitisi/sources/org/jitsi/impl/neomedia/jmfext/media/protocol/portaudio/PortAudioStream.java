package org.jitsi.impl.neomedia.jmfext.media.protocol.portaudio;

import java.io.IOException;
import javax.media.Format;
import javax.media.GainControl;
import javax.media.control.FormatControl;
import javax.media.format.AudioFormat;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.impl.neomedia.MediaServiceImpl;
import org.jitsi.impl.neomedia.NeomediaServiceUtils;
import org.jitsi.impl.neomedia.control.DiagnosticsControl;
import org.jitsi.impl.neomedia.device.AudioSystem;
import org.jitsi.impl.neomedia.device.DeviceConfiguration;
import org.jitsi.impl.neomedia.device.PortAudioSystem;
import org.jitsi.impl.neomedia.device.PortAudioSystem.PaUpdateAvailableDeviceListListener;
import org.jitsi.impl.neomedia.jmfext.media.protocol.AbstractPullBufferStream;
import org.jitsi.impl.neomedia.portaudio.Pa;
import org.jitsi.impl.neomedia.portaudio.Pa.HostApiTypeId;
import org.jitsi.impl.neomedia.portaudio.PortAudioException;
import org.jitsi.util.Logger;

public class PortAudioStream extends AbstractPullBufferStream<DataSource> {
    private static final long NEVER = 0;
    private static final Logger logger = Logger.getLogger(PortAudioStream.class);
    private final boolean audioQualityImprovement;
    private int bytesPerBuffer;
    /* access modifiers changed from: private */
    public String deviceID;
    private final DiagnosticsControl diagnosticsControl = new DiagnosticsControl() {
        public Component getControlComponent() {
            return null;
        }

        public long getMalfunctioningSince() {
            return PortAudioStream.this.readIsMalfunctioningSince;
        }

        public String toString() {
            String id = PortAudioStream.this.deviceID;
            if (PortAudioStream.this.deviceID == null) {
                return null;
            }
            int index = Pa.getDeviceIndex(id, 1, 0);
            if (index == -1) {
                return null;
            }
            long info = Pa.GetDeviceInfo(index);
            if (info != 0) {
                return Pa.DeviceInfo_getName(info);
            }
            return null;
        }
    };
    private AudioFormat format;
    private int framesPerBuffer;
    private final GainControl gainControl;
    private long inputParameters = 0;
    private final PaUpdateAvailableDeviceListListener paUpdateAvailableDeviceListListener = new PaUpdateAvailableDeviceListListener() {
        private String deviceID = null;
        private boolean start = false;

        public void didPaUpdateAvailableDeviceList() throws Exception {
            synchronized (PortAudioStream.this) {
                try {
                    PortAudioStream.this.waitWhileStreamIsBusy();
                    if (PortAudioStream.this.stream == 0) {
                        PortAudioStream.this.setDeviceID(this.deviceID);
                        if (this.start) {
                            PortAudioStream.this.start();
                        }
                    }
                    this.deviceID = null;
                    this.start = false;
                } catch (Throwable th) {
                    this.deviceID = null;
                    this.start = false;
                }
            }
        }

        public void willPaUpdateAvailableDeviceList() throws Exception {
            synchronized (PortAudioStream.this) {
                PortAudioStream.this.waitWhileStreamIsBusy();
                if (PortAudioStream.this.stream == 0) {
                    this.deviceID = null;
                    this.start = false;
                } else {
                    this.deviceID = PortAudioStream.this.deviceID;
                    this.start = PortAudioStream.this.started;
                    try {
                        PortAudioStream.this.setDeviceID(null);
                        if (!true) {
                            this.deviceID = null;
                            this.start = false;
                        }
                    } catch (Throwable th) {
                        if (!false) {
                            this.deviceID = null;
                            this.start = false;
                        }
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public long readIsMalfunctioningSince = 0;
    private int sequenceNumber = 0;
    /* access modifiers changed from: private */
    public boolean started = false;
    /* access modifiers changed from: private */
    public long stream = 0;
    private boolean streamIsBusy = false;

    public PortAudioStream(DataSource dataSource, FormatControl formatControl, boolean audioQualityImprovement) {
        super(dataSource, formatControl);
        this.audioQualityImprovement = audioQualityImprovement;
        MediaServiceImpl mediaServiceImpl = NeomediaServiceUtils.getMediaServiceImpl();
        this.gainControl = mediaServiceImpl == null ? null : (GainControl) mediaServiceImpl.getInputVolumeControl();
        PortAudioSystem.addPaUpdateAvailableDeviceListListener(this.paUpdateAvailableDeviceListListener);
    }

    private void connect() throws IOException {
        int deviceIndex = Pa.getDeviceIndex(this.deviceID, 1, 0);
        if (deviceIndex == -1) {
            throw new IOException("The audio device " + this.deviceID + " appears to be disconnected.");
        }
        AudioFormat format = (AudioFormat) getFormat();
        int channels = format.getChannels();
        if (channels == -1) {
            channels = 1;
        }
        int sampleSizeInBits = format.getSampleSizeInBits();
        long sampleFormat = Pa.getPaSampleFormat(sampleSizeInBits);
        double sampleRate = format.getSampleRate();
        int framesPerBuffer = (int) ((20.0d * sampleRate) / ((double) (channels * 1000)));
        try {
            this.inputParameters = Pa.StreamParameters_new(deviceIndex, channels, sampleFormat, Pa.getSuggestedLatency());
            this.stream = Pa.OpenStream(this.inputParameters, 0, sampleRate, (long) framesPerBuffer, 3, null);
            if (this.stream == 0 && this.inputParameters != 0) {
                Pa.StreamParameters_free(this.inputParameters);
                this.inputParameters = 0;
            }
            if (this.stream == 0) {
                throw new IOException("Pa_OpenStream");
            }
            this.framesPerBuffer = framesPerBuffer;
            this.bytesPerBuffer = (Pa.GetSampleSize(sampleFormat) * channels) * framesPerBuffer;
            this.format = new AudioFormat(AudioFormat.LINEAR, sampleRate, sampleSizeInBits, channels, 0, 1, -1, -1.0d, Format.byteArray);
            boolean denoise = false;
            boolean echoCancel = false;
            long echoCancelFilterLengthInMillis = 100;
            if (this.audioQualityImprovement) {
                AudioSystem audioSystem = AudioSystem.getAudioSystem(AudioSystem.LOCATOR_PROTOCOL_PORTAUDIO);
                if (audioSystem != null) {
                    denoise = audioSystem.isDenoise();
                    echoCancel = audioSystem.isEchoCancel();
                    if (echoCancel) {
                        MediaServiceImpl mediaServiceImpl = NeomediaServiceUtils.getMediaServiceImpl();
                        if (mediaServiceImpl != null) {
                            DeviceConfiguration devCfg = mediaServiceImpl.getDeviceConfiguration();
                            if (devCfg != null) {
                                echoCancelFilterLengthInMillis = devCfg.getEchoCancelFilterLengthInMillis();
                            }
                        }
                    }
                }
            }
            Pa.setDenoise(this.stream, denoise);
            long j = this.stream;
            if (!echoCancel) {
                echoCancelFilterLengthInMillis = 0;
            }
            Pa.setEchoFilterLengthInMillis(j, echoCancelFilterLengthInMillis);
            if (this.readIsMalfunctioningSince != 0) {
                setReadIsMalfunctioning(false);
            }
        } catch (PortAudioException paex) {
            logger.error("Failed to open " + getClass().getSimpleName(), paex);
            IOException iOException = new IOException(paex.getLocalizedMessage());
            iOException.initCause(paex);
            throw iOException;
        } catch (Throwable th) {
            if (this.stream == 0 && this.inputParameters != 0) {
                Pa.StreamParameters_free(this.inputParameters);
                this.inputParameters = 0;
            }
        }
    }

    /* access modifiers changed from: protected */
    public Format doGetFormat() {
        return this.format == null ? super.doGetFormat() : this.format;
    }

    /* JADX WARNING: Missing block: B:74:0x015e, code skipped:
            if (6 == 0) goto L_0x0160;
     */
    /* JADX WARNING: Missing block: B:88:0x0187, code skipped:
            if (6 == 0) goto L_0x0189;
     */
    public void read(javax.media.Buffer r19) throws java.io.IOException {
        /*
        r18 = this;
        monitor-enter(r18);
        r0 = r18;
        r12 = r0.stream;	 Catch:{ all -> 0x006d }
        r14 = 0;
        r12 = (r12 > r14 ? 1 : (r12 == r14 ? 0 : -1));
        if (r12 != 0) goto L_0x0044;
    L_0x000b:
        r12 = new java.lang.StringBuilder;	 Catch:{ all -> 0x006d }
        r12.<init>();	 Catch:{ all -> 0x006d }
        r13 = r18.getClass();	 Catch:{ all -> 0x006d }
        r13 = r13.getName();	 Catch:{ all -> 0x006d }
        r12 = r12.append(r13);	 Catch:{ all -> 0x006d }
        r13 = " is disconnected.";
        r12 = r12.append(r13);	 Catch:{ all -> 0x006d }
        r9 = r12.toString();	 Catch:{ all -> 0x006d }
    L_0x0026:
        if (r9 == 0) goto L_0x0038;
    L_0x0028:
        r0 = r18;
        r12 = r0.readIsMalfunctioningSince;	 Catch:{ all -> 0x006d }
        r14 = 0;
        r12 = (r12 > r14 ? 1 : (r12 == r14 ? 0 : -1));
        if (r12 == 0) goto L_0x0038;
    L_0x0032:
        r12 = 0;
        r0 = r18;
        r0.setReadIsMalfunctioning(r12);	 Catch:{ all -> 0x006d }
    L_0x0038:
        monitor-exit(r18);	 Catch:{ all -> 0x006d }
        if (r9 == 0) goto L_0x0070;
    L_0x003b:
        yield();
        r12 = new java.io.IOException;
        r12.<init>(r9);
        throw r12;
    L_0x0044:
        r0 = r18;
        r12 = r0.started;	 Catch:{ all -> 0x006d }
        if (r12 != 0) goto L_0x0066;
    L_0x004a:
        r12 = new java.lang.StringBuilder;	 Catch:{ all -> 0x006d }
        r12.<init>();	 Catch:{ all -> 0x006d }
        r13 = r18.getClass();	 Catch:{ all -> 0x006d }
        r13 = r13.getName();	 Catch:{ all -> 0x006d }
        r12 = r12.append(r13);	 Catch:{ all -> 0x006d }
        r13 = " is stopped.";
        r12 = r12.append(r13);	 Catch:{ all -> 0x006d }
        r9 = r12.toString();	 Catch:{ all -> 0x006d }
        goto L_0x0026;
    L_0x0066:
        r9 = 0;
        r12 = 1;
        r0 = r18;
        r0.streamIsBusy = r12;	 Catch:{ all -> 0x006d }
        goto L_0x0026;
    L_0x006d:
        r12 = move-exception;
        monitor-exit(r18);	 Catch:{ all -> 0x006d }
        throw r12;
    L_0x0070:
        r6 = 0;
        r5 = 0;
        r0 = r18;
        r12 = r0.bytesPerBuffer;	 Catch:{ all -> 0x0124 }
        r13 = 0;
        r0 = r19;
        r4 = org.jitsi.impl.neomedia.codec.AbstractCodec2.validateByteArraySize(r0, r12, r13);	 Catch:{ all -> 0x0124 }
        r0 = r18;
        r12 = r0.stream;	 Catch:{ PortAudioException -> 0x0107 }
        r0 = r18;
        r14 = r0.framesPerBuffer;	 Catch:{ PortAudioException -> 0x0107 }
        r14 = (long) r14;	 Catch:{ PortAudioException -> 0x0107 }
        org.jitsi.impl.neomedia.portaudio.Pa.ReadStream(r12, r4, r14);	 Catch:{ PortAudioException -> 0x0107 }
        r0 = r18;
        r12 = r0.gainControl;	 Catch:{ all -> 0x0124 }
        if (r12 == 0) goto L_0x009c;
    L_0x0090:
        r0 = r18;
        r12 = r0.gainControl;	 Catch:{ all -> 0x0124 }
        r13 = 0;
        r0 = r18;
        r14 = r0.bytesPerBuffer;	 Catch:{ all -> 0x0124 }
        org.jitsi.service.neomedia.BasicVolumeControl.applyGain(r12, r4, r13, r14);	 Catch:{ all -> 0x0124 }
    L_0x009c:
        r2 = java.lang.System.nanoTime();	 Catch:{ all -> 0x0124 }
        r12 = 128; // 0x80 float:1.794E-43 double:6.32E-322;
        r0 = r19;
        r0.setFlags(r12);	 Catch:{ all -> 0x0124 }
        r0 = r18;
        r12 = r0.format;	 Catch:{ all -> 0x0124 }
        if (r12 == 0) goto L_0x00b6;
    L_0x00ad:
        r0 = r18;
        r12 = r0.format;	 Catch:{ all -> 0x0124 }
        r0 = r19;
        r0.setFormat(r12);	 Catch:{ all -> 0x0124 }
    L_0x00b6:
        r12 = 0;
        r0 = r19;
        r0.setHeader(r12);	 Catch:{ all -> 0x0124 }
        r0 = r18;
        r12 = r0.bytesPerBuffer;	 Catch:{ all -> 0x0124 }
        r0 = r19;
        r0.setLength(r12);	 Catch:{ all -> 0x0124 }
        r12 = 0;
        r0 = r19;
        r0.setOffset(r12);	 Catch:{ all -> 0x0124 }
        r0 = r18;
        r12 = r0.sequenceNumber;	 Catch:{ all -> 0x0124 }
        r13 = r12 + 1;
        r0 = r18;
        r0.sequenceNumber = r13;	 Catch:{ all -> 0x0124 }
        r12 = (long) r12;	 Catch:{ all -> 0x0124 }
        r0 = r19;
        r0.setSequenceNumber(r12);	 Catch:{ all -> 0x0124 }
        r0 = r19;
        r0.setTimeStamp(r2);	 Catch:{ all -> 0x0124 }
        r11 = 0;
        monitor-enter(r18);
        r12 = 0;
        r0 = r18;
        r0.streamIsBusy = r12;	 Catch:{ all -> 0x0172 }
        r18.notifyAll();	 Catch:{ all -> 0x0172 }
        r12 = 0;
        r12 = (r6 > r12 ? 1 : (r6 == r12 ? 0 : -1));
        if (r12 != 0) goto L_0x014c;
    L_0x00f0:
        r0 = r18;
        r12 = r0.readIsMalfunctioningSince;	 Catch:{ all -> 0x0172 }
        r14 = 0;
        r12 = (r12 > r14 ? 1 : (r12 == r14 ? 0 : -1));
        if (r12 == 0) goto L_0x0100;
    L_0x00fa:
        r12 = 0;
        r0 = r18;
        r0.setReadIsMalfunctioning(r12);	 Catch:{ all -> 0x0172 }
    L_0x0100:
        monitor-exit(r18);	 Catch:{ all -> 0x0172 }
        if (r11 == 0) goto L_0x0106;
    L_0x0103:
        yield();
    L_0x0106:
        return;
    L_0x0107:
        r10 = move-exception;
        r6 = r10.getErrorCode();	 Catch:{ all -> 0x0124 }
        r5 = r10.getHostApiType();	 Catch:{ all -> 0x0124 }
        r12 = logger;	 Catch:{ all -> 0x0124 }
        r13 = "Failed to read from PortAudio stream.";
        r12.error(r13, r10);	 Catch:{ all -> 0x0124 }
        r8 = new java.io.IOException;	 Catch:{ all -> 0x0124 }
        r12 = r10.getLocalizedMessage();	 Catch:{ all -> 0x0124 }
        r8.<init>(r12);	 Catch:{ all -> 0x0124 }
        r8.initCause(r10);	 Catch:{ all -> 0x0124 }
        throw r8;	 Catch:{ all -> 0x0124 }
    L_0x0124:
        r12 = move-exception;
        r11 = 0;
        monitor-enter(r18);
        r13 = 0;
        r0 = r18;
        r0.streamIsBusy = r13;	 Catch:{ all -> 0x019b }
        r18.notifyAll();	 Catch:{ all -> 0x019b }
        r14 = 0;
        r13 = (r6 > r14 ? 1 : (r6 == r14 ? 0 : -1));
        if (r13 != 0) goto L_0x0175;
    L_0x0135:
        r0 = r18;
        r14 = r0.readIsMalfunctioningSince;	 Catch:{ all -> 0x019b }
        r16 = 0;
        r13 = (r14 > r16 ? 1 : (r14 == r16 ? 0 : -1));
        if (r13 == 0) goto L_0x0145;
    L_0x013f:
        r13 = 0;
        r0 = r18;
        r0.setReadIsMalfunctioning(r13);	 Catch:{ all -> 0x019b }
    L_0x0145:
        monitor-exit(r18);	 Catch:{ all -> 0x019b }
        if (r11 == 0) goto L_0x014b;
    L_0x0148:
        yield();
    L_0x014b:
        throw r12;
    L_0x014c:
        r12 = -9987; // 0xffffffffffffd8fd float:NaN double:NaN;
        r12 = (r12 > r6 ? 1 : (r12 == r6 ? 0 : -1));
        if (r12 == 0) goto L_0x0160;
    L_0x0152:
        r12 = org.jitsi.impl.neomedia.portaudio.Pa.HostApiTypeId.paMME;	 Catch:{ all -> 0x0172 }
        r12 = r12.equals(r5);	 Catch:{ all -> 0x0172 }
        if (r12 == 0) goto L_0x0100;
    L_0x015a:
        r12 = 6;
        r12 = (r12 > r6 ? 1 : (r12 == r6 ? 0 : -1));
        if (r12 != 0) goto L_0x0100;
    L_0x0160:
        r0 = r18;
        r12 = r0.readIsMalfunctioningSince;	 Catch:{ all -> 0x0172 }
        r14 = 0;
        r12 = (r12 > r14 ? 1 : (r12 == r14 ? 0 : -1));
        if (r12 != 0) goto L_0x0170;
    L_0x016a:
        r12 = 1;
        r0 = r18;
        r0.setReadIsMalfunctioning(r12);	 Catch:{ all -> 0x0172 }
    L_0x0170:
        r11 = 1;
        goto L_0x0100;
    L_0x0172:
        r12 = move-exception;
        monitor-exit(r18);	 Catch:{ all -> 0x0172 }
        throw r12;
    L_0x0175:
        r14 = -9987; // 0xffffffffffffd8fd float:NaN double:NaN;
        r13 = (r14 > r6 ? 1 : (r14 == r6 ? 0 : -1));
        if (r13 == 0) goto L_0x0189;
    L_0x017b:
        r13 = org.jitsi.impl.neomedia.portaudio.Pa.HostApiTypeId.paMME;	 Catch:{ all -> 0x019b }
        r13 = r13.equals(r5);	 Catch:{ all -> 0x019b }
        if (r13 == 0) goto L_0x0145;
    L_0x0183:
        r14 = 6;
        r13 = (r14 > r6 ? 1 : (r14 == r6 ? 0 : -1));
        if (r13 != 0) goto L_0x0145;
    L_0x0189:
        r0 = r18;
        r14 = r0.readIsMalfunctioningSince;	 Catch:{ all -> 0x019b }
        r16 = 0;
        r13 = (r14 > r16 ? 1 : (r14 == r16 ? 0 : -1));
        if (r13 != 0) goto L_0x0199;
    L_0x0193:
        r13 = 1;
        r0 = r18;
        r0.setReadIsMalfunctioning(r13);	 Catch:{ all -> 0x019b }
    L_0x0199:
        r11 = 1;
        goto L_0x0145;
    L_0x019b:
        r12 = move-exception;
        monitor-exit(r18);	 Catch:{ all -> 0x019b }
        throw r12;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.jmfext.media.protocol.portaudio.PortAudioStream.read(javax.media.Buffer):void");
    }

    /* access modifiers changed from: declared_synchronized */
    public synchronized void setDeviceID(String deviceID) throws IOException {
        if (this.deviceID != null) {
            waitWhileStreamIsBusy();
            if (this.stream != 0) {
                if (this.started) {
                    try {
                        stop();
                    } catch (IOException e) {
                    }
                }
                boolean closed = false;
                try {
                    Pa.CloseStream(this.stream);
                    if (true) {
                        this.stream = 0;
                        if (this.inputParameters != 0) {
                            Pa.StreamParameters_free(this.inputParameters);
                            this.inputParameters = 0;
                        }
                        this.format = null;
                        if (this.readIsMalfunctioningSince != 0) {
                            setReadIsMalfunctioning(false);
                        }
                    }
                } catch (PortAudioException pae) {
                    long errorCode = pae.getErrorCode();
                    if (errorCode == -9987 || (HostApiTypeId.paMME.equals(pae.getHostApiType()) && errorCode == 6)) {
                        closed = true;
                    }
                    if (!closed) {
                        logger.error("Failed to close " + getClass().getSimpleName(), pae);
                        IOException ioe = new IOException(pae.getLocalizedMessage());
                        ioe.initCause(pae);
                        throw ioe;
                    } else if (closed) {
                        this.stream = 0;
                        if (this.inputParameters != 0) {
                            Pa.StreamParameters_free(this.inputParameters);
                            this.inputParameters = 0;
                        }
                        this.format = null;
                        if (this.readIsMalfunctioningSince != 0) {
                            setReadIsMalfunctioning(false);
                        }
                    }
                } catch (Throwable th) {
                    if (null != null) {
                        this.stream = 0;
                        if (this.inputParameters != 0) {
                            Pa.StreamParameters_free(this.inputParameters);
                            this.inputParameters = 0;
                        }
                        this.format = null;
                        if (this.readIsMalfunctioningSince != 0) {
                            setReadIsMalfunctioning(false);
                        }
                    }
                }
            }
        }
        this.deviceID = deviceID;
        this.started = false;
        if (this.deviceID != null) {
            PortAudioSystem.willPaOpenStream();
            connect();
            PortAudioSystem.didPaOpenStream();
        }
    }

    private void setReadIsMalfunctioning(boolean malfunctioning) {
        if (!malfunctioning) {
            this.readIsMalfunctioningSince = 0;
        } else if (this.readIsMalfunctioningSince == 0) {
            this.readIsMalfunctioningSince = System.currentTimeMillis();
            PortAudioSystem.monitorFunctionalHealth(this.diagnosticsControl);
        }
    }

    public synchronized void start() throws IOException {
        if (this.stream != 0) {
            waitWhileStreamIsBusy();
            try {
                Pa.StartStream(this.stream);
                this.started = true;
            } catch (PortAudioException paex) {
                logger.error("Failed to start " + getClass().getSimpleName(), paex);
                IOException ioex = new IOException(paex.getLocalizedMessage());
                ioex.initCause(paex);
                throw ioex;
            }
        }
    }

    public synchronized void stop() throws IOException {
        if (this.stream != 0) {
            waitWhileStreamIsBusy();
            try {
                Pa.StopStream(this.stream);
                this.started = false;
                if (this.readIsMalfunctioningSince != 0) {
                    setReadIsMalfunctioning(false);
                }
            } catch (PortAudioException paex) {
                logger.error("Failed to stop " + getClass().getSimpleName(), paex);
                IOException ioex = new IOException(paex.getLocalizedMessage());
                ioex.initCause(paex);
                throw ioex;
            }
        }
    }

    /* access modifiers changed from: private */
    public void waitWhileStreamIsBusy() {
        boolean interrupted = false;
        while (this.stream != 0 && this.streamIsBusy) {
            try {
                wait();
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    public static void yield() {
        boolean interrupted = false;
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            interrupted = true;
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
