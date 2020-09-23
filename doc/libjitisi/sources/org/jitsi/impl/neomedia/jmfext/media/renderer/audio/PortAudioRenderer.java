package org.jitsi.impl.neomedia.jmfext.media.renderer.audio;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import javax.media.Format;
import javax.media.GainControl;
import javax.media.MediaLocator;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.impl.neomedia.control.DiagnosticsControl;
import org.jitsi.impl.neomedia.device.AudioSystem;
import org.jitsi.impl.neomedia.device.AudioSystem.DataFlow;
import org.jitsi.impl.neomedia.device.PortAudioSystem;
import org.jitsi.impl.neomedia.device.PortAudioSystem.PaUpdateAvailableDeviceListListener;
import org.jitsi.impl.neomedia.jmfext.media.protocol.portaudio.DataSource;
import org.jitsi.impl.neomedia.portaudio.Pa;
import org.jitsi.impl.neomedia.portaudio.PortAudioException;
import org.jitsi.service.neomedia.BasicVolumeControl;
import org.jitsi.util.Logger;

public class PortAudioRenderer extends AbstractAudioRenderer<PortAudioSystem> {
    private static final Format[] EMPTY_SUPPORTED_INPUT_FORMATS = new Format[0];
    private static final byte FLAG_OPEN = (byte) 1;
    private static final byte FLAG_STARTED = (byte) 2;
    private static final String PLUGIN_NAME = "PortAudio Renderer";
    private static final Format[] SUPPORTED_INPUT_FORMATS;
    private static final double[] SUPPORTED_INPUT_SAMPLE_RATES = new double[]{8000.0d, 11025.0d, 16000.0d, 22050.0d, 32000.0d, 44100.0d, 48000.0d};
    private static final Logger logger = Logger.getLogger(PortAudioRenderer.class);
    private byte[] bufferLeft;
    private int bufferLeftLength;
    private int bytesPerBuffer;
    private final DiagnosticsControl diagnosticsControl;
    /* access modifiers changed from: private */
    public byte flags;
    private int framesPerBuffer;
    private long outputParameters;
    private final PaUpdateAvailableDeviceListListener paUpdateAvailableDeviceListListener;
    private boolean started;
    /* access modifiers changed from: private */
    public long stream;
    private boolean streamIsBusy;
    private Format[] supportedInputFormats;
    /* access modifiers changed from: private */
    public long writeIsMalfunctioningSince;

    static {
        int count = SUPPORTED_INPUT_SAMPLE_RATES.length;
        SUPPORTED_INPUT_FORMATS = new Format[count];
        for (int i = 0; i < count; i++) {
            SUPPORTED_INPUT_FORMATS[i] = new AudioFormat(AudioFormat.LINEAR, SUPPORTED_INPUT_SAMPLE_RATES[i], 16, -1, 0, 1, -1, -1.0d, Format.byteArray);
        }
    }

    public PortAudioRenderer() {
        this(true);
    }

    public PortAudioRenderer(boolean enableVolumeControl) {
        DataFlow dataFlow;
        String str = AudioSystem.LOCATOR_PROTOCOL_PORTAUDIO;
        if (enableVolumeControl) {
            dataFlow = DataFlow.PLAYBACK;
        } else {
            dataFlow = DataFlow.NOTIFY;
        }
        super(str, dataFlow);
        this.bufferLeftLength = 0;
        this.diagnosticsControl = new DiagnosticsControl() {
            public Component getControlComponent() {
                return null;
            }

            public long getMalfunctioningSince() {
                return PortAudioRenderer.this.writeIsMalfunctioningSince;
            }

            public String toString() {
                MediaLocator locator = PortAudioRenderer.this.getLocator();
                if (locator == null) {
                    return null;
                }
                String id = DataSource.getDeviceID(locator);
                if (id == null) {
                    return null;
                }
                int index = Pa.getDeviceIndex(id, 0, 1);
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
        this.flags = (byte) 0;
        this.outputParameters = 0;
        this.paUpdateAvailableDeviceListListener = new PaUpdateAvailableDeviceListListener() {
            public void didPaUpdateAvailableDeviceList() throws Exception {
                synchronized (PortAudioRenderer.this) {
                    PortAudioRenderer.this.waitWhileStreamIsBusy();
                    byte flags = PortAudioRenderer.this.flags;
                    if ((flags & 1) == 1) {
                        try {
                            PortAudioRenderer.this.open();
                            if ((flags & 2) == 2) {
                                PortAudioRenderer.this.start();
                            }
                        } catch (Throwable th) {
                            PortAudioRenderer.this.flags = flags;
                        }
                    }
                    PortAudioRenderer.this.flags = flags;
                }
            }

            public void willPaUpdateAvailableDeviceList() throws Exception {
                synchronized (PortAudioRenderer.this) {
                    PortAudioRenderer.this.waitWhileStreamIsBusy();
                    byte flags = PortAudioRenderer.this.flags;
                    try {
                        if (PortAudioRenderer.this.stream != 0) {
                            PortAudioRenderer.this.close();
                        }
                        PortAudioRenderer.this.flags = flags;
                    } catch (Throwable th) {
                        PortAudioRenderer.this.flags = flags;
                    }
                }
            }
        };
        this.started = false;
        this.stream = 0;
        this.streamIsBusy = false;
        this.writeIsMalfunctioningSince = 0;
        PortAudioSystem.addPaUpdateAvailableDeviceListListener(this.paUpdateAvailableDeviceListListener);
    }

    public synchronized void close() {
        try {
            stop();
            if (this.stream != 0) {
                Pa.CloseStream(this.stream);
                this.stream = 0;
                this.started = false;
                this.flags = (byte) (this.flags & -4);
                if (this.writeIsMalfunctioningSince != 0) {
                    setWriteIsMalfunctioning(false);
                }
            }
        } catch (PortAudioException paex) {
            logger.error("Failed to close PortAudio stream.", paex);
        } catch (Throwable th) {
            if (this.stream != 0) {
                try {
                    Pa.CloseStream(this.stream);
                    this.stream = 0;
                    this.started = false;
                    this.flags = (byte) (this.flags & -4);
                    if (this.writeIsMalfunctioningSince != 0) {
                        setWriteIsMalfunctioning(false);
                    }
                } catch (PortAudioException paex2) {
                    logger.error("Failed to close PortAudio stream.", paex2);
                }
            }
            if (this.stream == 0 && this.outputParameters != 0) {
                Pa.StreamParameters_free(this.outputParameters);
                this.outputParameters = 0;
            }
            super.close();
        }
        if (this.stream == 0 && this.outputParameters != 0) {
            Pa.StreamParameters_free(this.outputParameters);
            this.outputParameters = 0;
        }
        super.close();
        return;
    }

    public String getName() {
        return PLUGIN_NAME;
    }

    public Format[] getSupportedInputFormats() {
        if (this.supportedInputFormats == null) {
            MediaLocator locator = getLocator();
            if (locator != null) {
                String deviceID = DataSource.getDeviceID(locator);
                if (!(deviceID == null || deviceID.length() == 0)) {
                    int deviceIndex = Pa.getDeviceIndex(deviceID, 0, 1);
                    if (deviceIndex != -1) {
                        long deviceInfo = Pa.GetDeviceInfo(deviceIndex);
                        if (deviceInfo != 0) {
                            int maxOutputChannels = Math.min(Pa.DeviceInfo_getMaxOutputChannels(deviceInfo), 2);
                            List<Format> supportedInputFormats = new ArrayList(SUPPORTED_INPUT_FORMATS.length);
                            for (Format supportedInputFormat : SUPPORTED_INPUT_FORMATS) {
                                getSupportedInputFormats(supportedInputFormat, deviceIndex, 1, maxOutputChannels, supportedInputFormats);
                            }
                            this.supportedInputFormats = supportedInputFormats.isEmpty() ? EMPTY_SUPPORTED_INPUT_FORMATS : (Format[]) supportedInputFormats.toArray(EMPTY_SUPPORTED_INPUT_FORMATS);
                        }
                    }
                }
            }
            this.supportedInputFormats = SUPPORTED_INPUT_FORMATS;
        }
        if (this.supportedInputFormats.length == 0) {
            return EMPTY_SUPPORTED_INPUT_FORMATS;
        }
        return (Format[]) this.supportedInputFormats.clone();
    }

    private void getSupportedInputFormats(Format format, int deviceIndex, int minOutputChannels, int maxOutputChannels, List<Format> supportedInputFormats) {
        AudioFormat audioFormat = (AudioFormat) format;
        int sampleSizeInBits = audioFormat.getSampleSizeInBits();
        long sampleFormat = Pa.getPaSampleFormat(sampleSizeInBits);
        double sampleRate = audioFormat.getSampleRate();
        for (int channels = minOutputChannels; channels <= maxOutputChannels; channels++) {
            long outputParameters = Pa.StreamParameters_new(deviceIndex, channels, sampleFormat, Pa.LATENCY_UNSPECIFIED);
            if (outputParameters != 0) {
                try {
                    if (Pa.IsFormatSupported(0, outputParameters, sampleRate)) {
                        List<Format> list = supportedInputFormats;
                        list.add(new AudioFormat(audioFormat.getEncoding(), sampleRate, sampleSizeInBits, channels, audioFormat.getEndian(), audioFormat.getSigned(), -1, -1.0d, audioFormat.getDataType()));
                    }
                    Pa.StreamParameters_free(outputParameters);
                } catch (Throwable th) {
                    Pa.StreamParameters_free(outputParameters);
                }
            }
        }
    }

    public synchronized void open() throws ResourceUnavailableException {
        try {
            PortAudioSystem.willPaOpenStream();
            doOpen();
            PortAudioSystem.didPaOpenStream();
            super.open();
        } catch (Throwable t) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to open PortAudioRenderer", t);
            }
            if (t instanceof ThreadDeath) {
                ThreadDeath t2 = (ThreadDeath) t;
            } else if (t instanceof ResourceUnavailableException) {
                ResourceUnavailableException t3 = (ResourceUnavailableException) t;
            } else {
                new ResourceUnavailableException().initCause(t);
            }
        }
    }

    private void doOpen() throws ResourceUnavailableException {
        if (this.stream == 0) {
            MediaLocator locator = getLocator();
            if (locator == null) {
                throw new ResourceUnavailableException("No locator/MediaLocator is set.");
            }
            String deviceID = DataSource.getDeviceID(locator);
            int deviceIndex = Pa.getDeviceIndex(deviceID, 0, 1);
            if (deviceIndex == -1) {
                throw new ResourceUnavailableException("The audio device " + deviceID + " appears to be disconnected.");
            }
            AudioFormat inputFormat = (AudioFormat) this.inputFormat;
            if (inputFormat == null) {
                throw new ResourceUnavailableException("inputFormat not set");
            }
            int channels = inputFormat.getChannels();
            if (channels == -1) {
                channels = 1;
            }
            long sampleFormat = Pa.getPaSampleFormat(inputFormat.getSampleSizeInBits());
            double sampleRate = inputFormat.getSampleRate();
            this.framesPerBuffer = (int) ((20.0d * sampleRate) / ((double) (channels * 1000)));
            try {
                this.outputParameters = Pa.StreamParameters_new(deviceIndex, channels, sampleFormat, Pa.getSuggestedLatency());
                this.stream = Pa.OpenStream(0, this.outputParameters, sampleRate, (long) this.framesPerBuffer, 3, null);
                this.started = false;
                if (this.stream == 0) {
                    this.flags = (byte) (this.flags & -4);
                    if (this.outputParameters != 0) {
                        Pa.StreamParameters_free(this.outputParameters);
                        this.outputParameters = 0;
                    }
                } else {
                    this.flags = (byte) (this.flags | 3);
                }
                if (this.stream == 0) {
                    throw new ResourceUnavailableException("Pa_OpenStream");
                }
                this.bytesPerBuffer = (Pa.GetSampleSize(sampleFormat) * channels) * this.framesPerBuffer;
                if (this.writeIsMalfunctioningSince != 0) {
                    setWriteIsMalfunctioning(false);
                }
            } catch (PortAudioException paex) {
                logger.error("Failed to open PortAudio stream.", paex);
                throw new ResourceUnavailableException(paex.getMessage());
            } catch (Throwable th) {
                this.started = false;
                if (this.stream == 0) {
                    this.flags = (byte) (this.flags & -4);
                    if (this.outputParameters != 0) {
                        Pa.StreamParameters_free(this.outputParameters);
                        this.outputParameters = 0;
                    }
                } else {
                    this.flags = (byte) (this.flags | 3);
                }
            }
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void playbackDevicePropertyChange(PropertyChangeEvent ev) {
        waitWhileStreamIsBusy();
        byte flags = this.flags;
        if ((flags & 1) == 1) {
            try {
                close();
                open();
                if ((flags & 2) == 2) {
                    start();
                }
            } catch (ResourceUnavailableException rue) {
                throw new UndeclaredThrowableException(rue);
            } catch (Throwable th) {
                this.flags = flags;
            }
        }
        this.flags = flags;
    }

    /* JADX WARNING: Missing block: B:13:0x0020, code skipped:
            r0 = 0;
            r2 = null;
     */
    /* JADX WARNING: Missing block: B:15:?, code skipped:
            process((byte[]) r11.getData(), r11.getOffset(), r11.getLength());
     */
    /* JADX WARNING: Missing block: B:16:0x0036, code skipped:
            r4 = false;
     */
    /* JADX WARNING: Missing block: B:17:0x0037, code skipped:
            monitor-enter(r10);
     */
    /* JADX WARNING: Missing block: B:20:?, code skipped:
            r10.streamIsBusy = false;
            notifyAll();
     */
    /* JADX WARNING: Missing block: B:21:0x0042, code skipped:
            if (0 != 0) goto L_0x005b;
     */
    /* JADX WARNING: Missing block: B:23:0x004a, code skipped:
            if (r10.writeIsMalfunctioningSince == 0) goto L_0x0050;
     */
    /* JADX WARNING: Missing block: B:24:0x004c, code skipped:
            setWriteIsMalfunctioning(false);
     */
    /* JADX WARNING: Missing block: B:25:0x0050, code skipped:
            monitor-exit(r10);
     */
    /* JADX WARNING: Missing block: B:26:0x0051, code skipped:
            if (r4 == false) goto L_0x0056;
     */
    /* JADX WARNING: Missing block: B:27:0x0053, code skipped:
            org.jitsi.impl.neomedia.jmfext.media.protocol.portaudio.PortAudioStream.yield();
     */
    /* JADX WARNING: Missing block: B:34:0x005f, code skipped:
            if (-9987 == 0) goto L_0x006f;
     */
    /* JADX WARNING: Missing block: B:37:0x0067, code skipped:
            if (org.jitsi.impl.neomedia.portaudio.Pa.HostApiTypeId.paMME.equals(null) == false) goto L_0x0050;
     */
    /* JADX WARNING: Missing block: B:39:0x006d, code skipped:
            if (6 != 0) goto L_0x0050;
     */
    /* JADX WARNING: Missing block: B:41:0x0075, code skipped:
            if (r10.writeIsMalfunctioningSince != 0) goto L_0x007b;
     */
    /* JADX WARNING: Missing block: B:42:0x0077, code skipped:
            setWriteIsMalfunctioning(true);
     */
    /* JADX WARNING: Missing block: B:43:0x007b, code skipped:
            r4 = true;
     */
    /* JADX WARNING: Missing block: B:47:0x0080, code skipped:
            r3 = move-exception;
     */
    /* JADX WARNING: Missing block: B:49:?, code skipped:
            r0 = r3.getErrorCode();
            r2 = r3.getHostApiType();
            logger.error("Failed to process Buffer.", r3);
     */
    /* JADX WARNING: Missing block: B:50:0x0090, code skipped:
            r4 = false;
     */
    /* JADX WARNING: Missing block: B:51:0x0091, code skipped:
            monitor-enter(r10);
     */
    /* JADX WARNING: Missing block: B:54:?, code skipped:
            r10.streamIsBusy = false;
            notifyAll();
     */
    /* JADX WARNING: Missing block: B:55:0x009c, code skipped:
            if (r0 == 0) goto L_0x009e;
     */
    /* JADX WARNING: Missing block: B:57:0x00a4, code skipped:
            if (r10.writeIsMalfunctioningSince != 0) goto L_0x00a6;
     */
    /* JADX WARNING: Missing block: B:58:0x00a6, code skipped:
            setWriteIsMalfunctioning(false);
     */
    /* JADX WARNING: Missing block: B:60:0x00ab, code skipped:
            if (r4 != false) goto L_0x00ad;
     */
    /* JADX WARNING: Missing block: B:61:0x00ad, code skipped:
            org.jitsi.impl.neomedia.jmfext.media.protocol.portaudio.PortAudioStream.yield();
     */
    /* JADX WARNING: Missing block: B:63:0x00b5, code skipped:
            if (-9987 != r0) goto L_0x00b7;
     */
    /* JADX WARNING: Missing block: B:66:0x00bd, code skipped:
            if (org.jitsi.impl.neomedia.portaudio.Pa.HostApiTypeId.paMME.equals(r2) != false) goto L_0x00bf;
     */
    /* JADX WARNING: Missing block: B:68:0x00c3, code skipped:
            if (6 == r0) goto L_0x00c5;
     */
    /* JADX WARNING: Missing block: B:70:0x00cb, code skipped:
            if (r10.writeIsMalfunctioningSince == 0) goto L_0x00cd;
     */
    /* JADX WARNING: Missing block: B:71:0x00cd, code skipped:
            setWriteIsMalfunctioning(true);
     */
    /* JADX WARNING: Missing block: B:72:0x00d1, code skipped:
            r4 = true;
     */
    /* JADX WARNING: Missing block: B:77:0x00d7, code skipped:
            r4 = false;
     */
    /* JADX WARNING: Missing block: B:78:0x00d8, code skipped:
            monitor-enter(r10);
     */
    /* JADX WARNING: Missing block: B:81:?, code skipped:
            r10.streamIsBusy = false;
            notifyAll();
     */
    /* JADX WARNING: Missing block: B:82:0x00e3, code skipped:
            if (r0 == 0) goto L_0x00e5;
     */
    /* JADX WARNING: Missing block: B:84:0x00eb, code skipped:
            if (r10.writeIsMalfunctioningSince != 0) goto L_0x00ed;
     */
    /* JADX WARNING: Missing block: B:85:0x00ed, code skipped:
            setWriteIsMalfunctioning(false);
     */
    /* JADX WARNING: Missing block: B:87:0x00f2, code skipped:
            if (r4 != false) goto L_0x00f4;
     */
    /* JADX WARNING: Missing block: B:88:0x00f4, code skipped:
            org.jitsi.impl.neomedia.jmfext.media.protocol.portaudio.PortAudioStream.yield();
     */
    /* JADX WARNING: Missing block: B:91:0x00fc, code skipped:
            if (-9987 != r0) goto L_0x00fe;
     */
    /* JADX WARNING: Missing block: B:94:0x0104, code skipped:
            if (org.jitsi.impl.neomedia.portaudio.Pa.HostApiTypeId.paMME.equals(r2) != false) goto L_0x0106;
     */
    /* JADX WARNING: Missing block: B:96:0x010a, code skipped:
            if (6 == r0) goto L_0x010c;
     */
    /* JADX WARNING: Missing block: B:98:0x0112, code skipped:
            if (r10.writeIsMalfunctioningSince == 0) goto L_0x0114;
     */
    /* JADX WARNING: Missing block: B:99:0x0114, code skipped:
            setWriteIsMalfunctioning(true);
     */
    /* JADX WARNING: Missing block: B:100:0x0118, code skipped:
            r4 = true;
     */
    /* JADX WARNING: Missing block: B:108:?, code skipped:
            return 0;
     */
    public int process(javax.media.Buffer r11) {
        /*
        r10 = this;
        monitor-enter(r10);
        r5 = r10.started;	 Catch:{ all -> 0x0058 }
        if (r5 == 0) goto L_0x000d;
    L_0x0005:
        r6 = r10.stream;	 Catch:{ all -> 0x0058 }
        r8 = 0;
        r5 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
        if (r5 != 0) goto L_0x001c;
    L_0x000d:
        r6 = r10.writeIsMalfunctioningSince;	 Catch:{ all -> 0x0058 }
        r8 = 0;
        r5 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
        if (r5 == 0) goto L_0x0019;
    L_0x0015:
        r5 = 0;
        r10.setWriteIsMalfunctioning(r5);	 Catch:{ all -> 0x0058 }
    L_0x0019:
        r5 = 0;
        monitor-exit(r10);	 Catch:{ all -> 0x0058 }
    L_0x001b:
        return r5;
    L_0x001c:
        r5 = 1;
        r10.streamIsBusy = r5;	 Catch:{ all -> 0x0058 }
        monitor-exit(r10);	 Catch:{ all -> 0x0058 }
        r0 = 0;
        r2 = 0;
        r5 = r11.getData();	 Catch:{ PortAudioException -> 0x0080 }
        r5 = (byte[]) r5;	 Catch:{ PortAudioException -> 0x0080 }
        r5 = (byte[]) r5;	 Catch:{ PortAudioException -> 0x0080 }
        r6 = r11.getOffset();	 Catch:{ PortAudioException -> 0x0080 }
        r7 = r11.getLength();	 Catch:{ PortAudioException -> 0x0080 }
        r10.process(r5, r6, r7);	 Catch:{ PortAudioException -> 0x0080 }
        r4 = 0;
        monitor-enter(r10);
        r5 = 0;
        r10.streamIsBusy = r5;	 Catch:{ all -> 0x007d }
        r10.notifyAll();	 Catch:{ all -> 0x007d }
        r6 = 0;
        r5 = (r0 > r6 ? 1 : (r0 == r6 ? 0 : -1));
        if (r5 != 0) goto L_0x005b;
    L_0x0044:
        r6 = r10.writeIsMalfunctioningSince;	 Catch:{ all -> 0x007d }
        r8 = 0;
        r5 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
        if (r5 == 0) goto L_0x0050;
    L_0x004c:
        r5 = 0;
        r10.setWriteIsMalfunctioning(r5);	 Catch:{ all -> 0x007d }
    L_0x0050:
        monitor-exit(r10);	 Catch:{ all -> 0x007d }
        if (r4 == 0) goto L_0x0056;
    L_0x0053:
        org.jitsi.impl.neomedia.jmfext.media.protocol.portaudio.PortAudioStream.yield();
    L_0x0056:
        r5 = 0;
        goto L_0x001b;
    L_0x0058:
        r5 = move-exception;
        monitor-exit(r10);	 Catch:{ all -> 0x0058 }
        throw r5;
    L_0x005b:
        r6 = -9987; // 0xffffffffffffd8fd float:NaN double:NaN;
        r5 = (r6 > r0 ? 1 : (r6 == r0 ? 0 : -1));
        if (r5 == 0) goto L_0x006f;
    L_0x0061:
        r5 = org.jitsi.impl.neomedia.portaudio.Pa.HostApiTypeId.paMME;	 Catch:{ all -> 0x007d }
        r5 = r5.equals(r2);	 Catch:{ all -> 0x007d }
        if (r5 == 0) goto L_0x0050;
    L_0x0069:
        r6 = 6;
        r5 = (r6 > r0 ? 1 : (r6 == r0 ? 0 : -1));
        if (r5 != 0) goto L_0x0050;
    L_0x006f:
        r6 = r10.writeIsMalfunctioningSince;	 Catch:{ all -> 0x007d }
        r8 = 0;
        r5 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
        if (r5 != 0) goto L_0x007b;
    L_0x0077:
        r5 = 1;
        r10.setWriteIsMalfunctioning(r5);	 Catch:{ all -> 0x007d }
    L_0x007b:
        r4 = 1;
        goto L_0x0050;
    L_0x007d:
        r5 = move-exception;
        monitor-exit(r10);	 Catch:{ all -> 0x007d }
        throw r5;
    L_0x0080:
        r3 = move-exception;
        r0 = r3.getErrorCode();	 Catch:{ all -> 0x00d6 }
        r2 = r3.getHostApiType();	 Catch:{ all -> 0x00d6 }
        r5 = logger;	 Catch:{ all -> 0x00d6 }
        r6 = "Failed to process Buffer.";
        r5.error(r6, r3);	 Catch:{ all -> 0x00d6 }
        r4 = 0;
        monitor-enter(r10);
        r5 = 0;
        r10.streamIsBusy = r5;	 Catch:{ all -> 0x00d3 }
        r10.notifyAll();	 Catch:{ all -> 0x00d3 }
        r6 = 0;
        r5 = (r0 > r6 ? 1 : (r0 == r6 ? 0 : -1));
        if (r5 != 0) goto L_0x00b1;
    L_0x009e:
        r6 = r10.writeIsMalfunctioningSince;	 Catch:{ all -> 0x00d3 }
        r8 = 0;
        r5 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
        if (r5 == 0) goto L_0x00aa;
    L_0x00a6:
        r5 = 0;
        r10.setWriteIsMalfunctioning(r5);	 Catch:{ all -> 0x00d3 }
    L_0x00aa:
        monitor-exit(r10);	 Catch:{ all -> 0x00d3 }
        if (r4 == 0) goto L_0x0056;
    L_0x00ad:
        org.jitsi.impl.neomedia.jmfext.media.protocol.portaudio.PortAudioStream.yield();
        goto L_0x0056;
    L_0x00b1:
        r6 = -9987; // 0xffffffffffffd8fd float:NaN double:NaN;
        r5 = (r6 > r0 ? 1 : (r6 == r0 ? 0 : -1));
        if (r5 == 0) goto L_0x00c5;
    L_0x00b7:
        r5 = org.jitsi.impl.neomedia.portaudio.Pa.HostApiTypeId.paMME;	 Catch:{ all -> 0x00d3 }
        r5 = r5.equals(r2);	 Catch:{ all -> 0x00d3 }
        if (r5 == 0) goto L_0x00aa;
    L_0x00bf:
        r6 = 6;
        r5 = (r6 > r0 ? 1 : (r6 == r0 ? 0 : -1));
        if (r5 != 0) goto L_0x00aa;
    L_0x00c5:
        r6 = r10.writeIsMalfunctioningSince;	 Catch:{ all -> 0x00d3 }
        r8 = 0;
        r5 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
        if (r5 != 0) goto L_0x00d1;
    L_0x00cd:
        r5 = 1;
        r10.setWriteIsMalfunctioning(r5);	 Catch:{ all -> 0x00d3 }
    L_0x00d1:
        r4 = 1;
        goto L_0x00aa;
    L_0x00d3:
        r5 = move-exception;
        monitor-exit(r10);	 Catch:{ all -> 0x00d3 }
        throw r5;
    L_0x00d6:
        r5 = move-exception;
        r4 = 0;
        monitor-enter(r10);
        r6 = 0;
        r10.streamIsBusy = r6;	 Catch:{ all -> 0x011a }
        r10.notifyAll();	 Catch:{ all -> 0x011a }
        r6 = 0;
        r6 = (r0 > r6 ? 1 : (r0 == r6 ? 0 : -1));
        if (r6 != 0) goto L_0x00f8;
    L_0x00e5:
        r6 = r10.writeIsMalfunctioningSince;	 Catch:{ all -> 0x011a }
        r8 = 0;
        r6 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
        if (r6 == 0) goto L_0x00f1;
    L_0x00ed:
        r6 = 0;
        r10.setWriteIsMalfunctioning(r6);	 Catch:{ all -> 0x011a }
    L_0x00f1:
        monitor-exit(r10);	 Catch:{ all -> 0x011a }
        if (r4 == 0) goto L_0x00f7;
    L_0x00f4:
        org.jitsi.impl.neomedia.jmfext.media.protocol.portaudio.PortAudioStream.yield();
    L_0x00f7:
        throw r5;
    L_0x00f8:
        r6 = -9987; // 0xffffffffffffd8fd float:NaN double:NaN;
        r6 = (r6 > r0 ? 1 : (r6 == r0 ? 0 : -1));
        if (r6 == 0) goto L_0x010c;
    L_0x00fe:
        r6 = org.jitsi.impl.neomedia.portaudio.Pa.HostApiTypeId.paMME;	 Catch:{ all -> 0x011a }
        r6 = r6.equals(r2);	 Catch:{ all -> 0x011a }
        if (r6 == 0) goto L_0x00f1;
    L_0x0106:
        r6 = 6;
        r6 = (r6 > r0 ? 1 : (r6 == r0 ? 0 : -1));
        if (r6 != 0) goto L_0x00f1;
    L_0x010c:
        r6 = r10.writeIsMalfunctioningSince;	 Catch:{ all -> 0x011a }
        r8 = 0;
        r6 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
        if (r6 != 0) goto L_0x0118;
    L_0x0114:
        r6 = 1;
        r10.setWriteIsMalfunctioning(r6);	 Catch:{ all -> 0x011a }
    L_0x0118:
        r4 = 1;
        goto L_0x00f1;
    L_0x011a:
        r5 = move-exception;
        monitor-exit(r10);	 Catch:{ all -> 0x011a }
        throw r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.jmfext.media.renderer.audio.PortAudioRenderer.process(javax.media.Buffer):int");
    }

    private void process(byte[] buffer, int offset, int length) throws PortAudioException {
        if (this.bufferLeft != null && this.bufferLeftLength > 0) {
            int numberOfBytesToCopyToBufferLeft;
            int numberOfBytesInBufferLeftToBytesPerBuffer = this.bytesPerBuffer - this.bufferLeftLength;
            if (numberOfBytesInBufferLeftToBytesPerBuffer < length) {
                numberOfBytesToCopyToBufferLeft = numberOfBytesInBufferLeftToBytesPerBuffer;
            } else {
                numberOfBytesToCopyToBufferLeft = length;
            }
            System.arraycopy(buffer, offset, this.bufferLeft, this.bufferLeftLength, numberOfBytesToCopyToBufferLeft);
            offset += numberOfBytesToCopyToBufferLeft;
            length -= numberOfBytesToCopyToBufferLeft;
            this.bufferLeftLength += numberOfBytesToCopyToBufferLeft;
            if (this.bufferLeftLength == this.bytesPerBuffer) {
                Pa.WriteStream(this.stream, this.bufferLeft, (long) this.framesPerBuffer);
                this.bufferLeftLength = 0;
            }
        }
        int numberOfWrites = length / this.bytesPerBuffer;
        if (numberOfWrites > 0) {
            GainControl gainControl = getGainControl();
            if (gainControl != null) {
                BasicVolumeControl.applyGain(gainControl, buffer, offset, length);
            }
            Pa.WriteStream(this.stream, buffer, offset, (long) this.framesPerBuffer, numberOfWrites);
            int bytesWritten = numberOfWrites * this.bytesPerBuffer;
            offset += bytesWritten;
            length -= bytesWritten;
        }
        if (length > 0) {
            if (this.bufferLeft == null) {
                this.bufferLeft = new byte[this.bytesPerBuffer];
            }
            System.arraycopy(buffer, offset, this.bufferLeft, 0, length);
            this.bufferLeftLength = length;
        }
    }

    public void setLocator(MediaLocator locator) {
        super.setLocator(locator);
        this.supportedInputFormats = null;
    }

    private void setWriteIsMalfunctioning(boolean writeIsMalfunctioning) {
        if (!writeIsMalfunctioning) {
            this.writeIsMalfunctioningSince = 0;
        } else if (this.writeIsMalfunctioningSince == 0) {
            this.writeIsMalfunctioningSince = System.currentTimeMillis();
            PortAudioSystem.monitorFunctionalHealth(this.diagnosticsControl);
        }
    }

    public synchronized void start() {
        if (!(this.started || this.stream == 0)) {
            try {
                Pa.StartStream(this.stream);
                this.started = true;
                this.flags = (byte) (this.flags | 2);
            } catch (PortAudioException paex) {
                logger.error("Failed to start PortAudio stream.", paex);
            }
        }
        return;
    }

    public synchronized void stop() {
        waitWhileStreamIsBusy();
        if (this.started && this.stream != 0) {
            try {
                Pa.StopStream(this.stream);
                this.started = false;
                this.flags = (byte) (this.flags & -3);
                this.bufferLeft = null;
                if (this.writeIsMalfunctioningSince != 0) {
                    setWriteIsMalfunctioning(false);
                }
            } catch (PortAudioException paex) {
                logger.error("Failed to close PortAudio stream.", paex);
            }
        }
        return;
    }

    /* access modifiers changed from: private */
    public void waitWhileStreamIsBusy() {
        boolean interrupted = false;
        while (this.streamIsBusy) {
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
}
