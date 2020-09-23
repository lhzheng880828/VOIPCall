package org.jitsi.impl.neomedia.jmfext.media.renderer.audio;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.media.Buffer;
import javax.media.Codec;
import javax.media.Format;
import javax.media.GainControl;
import javax.media.MediaLocator;
import javax.media.PlugInManager;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import org.jitsi.impl.neomedia.MediaUtils;
import org.jitsi.impl.neomedia.device.AudioSystem;
import org.jitsi.impl.neomedia.device.AudioSystem.DataFlow;
import org.jitsi.impl.neomedia.device.CaptureDeviceInfo2;
import org.jitsi.impl.neomedia.device.WASAPISystem;
import org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.HResultException;
import org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI;
import org.jitsi.service.neomedia.BasicVolumeControl;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.util.Logger;

public class WASAPIRenderer extends AbstractAudioRenderer<WASAPISystem> {
    private static final String PLUGIN_NAME = "Windows Audio Session API (WASAPI) Renderer";
    private static final Logger logger = Logger.getLogger(WASAPIRenderer.class);
    private long bufferDuration;
    private boolean busy;
    private long devicePeriod;
    private int devicePeriodInFrames;
    private int dstChannels;
    private AudioFormat dstFormat;
    private int dstSampleSize;
    private long eventHandle;
    private Runnable eventHandleCmd;
    private Executor eventHandleExecutor;
    private long iAudioClient;
    private long iAudioRenderClient;
    private boolean locatorIsNull;
    private int numBufferFrames;
    private byte[] remainder;
    private int remainderLength;
    private Codec resampler;
    private int resamplerChannels;
    private int resamplerFrameSize;
    private Buffer resamplerInBuffer;
    private Buffer resamplerOutBuffer;
    private int resamplerSampleSize;
    private int srcChannels;
    private AudioFormat srcFormat;
    private int srcFrameSize;
    private int srcSampleSize;
    private boolean started;
    private long writeIsMalfunctioningSince;
    private long writeIsMalfunctioningTimeout;

    public WASAPIRenderer() {
        this(DataFlow.PLAYBACK);
    }

    public WASAPIRenderer(DataFlow dataFlow) {
        super(AudioSystem.LOCATOR_PROTOCOL_WASAPI, dataFlow);
        this.devicePeriod = 10;
        this.writeIsMalfunctioningSince = 0;
    }

    public WASAPIRenderer(boolean playback) {
        this(playback ? DataFlow.PLAYBACK : DataFlow.NOTIFY);
    }

    public synchronized void close() {
        try {
            stop();
            if (this.iAudioRenderClient != 0) {
                WASAPI.IAudioRenderClient_Release(this.iAudioRenderClient);
                this.iAudioRenderClient = 0;
            }
            if (this.iAudioClient != 0) {
                WASAPI.IAudioClient_Release(this.iAudioClient);
                this.iAudioClient = 0;
            }
            if (this.eventHandle != 0) {
                WASAPI.CloseHandle(this.eventHandle);
                this.eventHandle = 0;
            }
        } catch (HResultException hre) {
            logger.warn("Failed to close event HANDLE.", hre);
        } catch (Throwable th) {
            if (this.iAudioRenderClient != 0) {
                WASAPI.IAudioRenderClient_Release(this.iAudioRenderClient);
                this.iAudioRenderClient = 0;
            }
            if (this.iAudioClient != 0) {
                WASAPI.IAudioClient_Release(this.iAudioClient);
                this.iAudioClient = 0;
            }
            if (this.eventHandle != 0) {
                try {
                    WASAPI.CloseHandle(this.eventHandle);
                } catch (HResultException hre2) {
                    logger.warn("Failed to close event HANDLE.", hre2);
                }
                this.eventHandle = 0;
            }
            maybeCloseResampler();
            this.dstFormat = null;
            this.locatorIsNull = false;
            this.remainder = null;
            this.remainderLength = 0;
            this.srcFormat = null;
            this.started = false;
            super.close();
        }
        maybeCloseResampler();
        this.dstFormat = null;
        this.locatorIsNull = false;
        this.remainder = null;
        this.remainderLength = 0;
        this.srcFormat = null;
        this.started = false;
        super.close();
        return;
    }

    private static AudioFormat findFirst(AudioFormat[] formats) {
        for (AudioFormat aFormat : formats) {
            if (aFormat != null) {
                return aFormat;
            }
        }
        return null;
    }

    private AudioFormat[] getFormatsToInitializeIAudioClient() {
        AudioFormat inputFormat = this.inputFormat;
        if (inputFormat == null) {
            throw new NullPointerException("No inputFormat set.");
        }
        AudioFormat[] preferredFormats = WASAPISystem.getFormatsToInitializeIAudioClient(inputFormat);
        Format[] supportedFormats = getSupportedInputFormats();
        List<AudioFormat> formats = new ArrayList(preferredFormats.length + supportedFormats.length);
        for (AudioFormat format : preferredFormats) {
            if (!formats.contains(format)) {
                formats.add(format);
            }
        }
        for (Format format2 : supportedFormats) {
            if (!formats.contains(format2) && (format2 instanceof AudioFormat)) {
                formats.add((AudioFormat) format2);
            }
        }
        return (AudioFormat[]) formats.toArray(new AudioFormat[formats.size()]);
    }

    public String getName() {
        return PLUGIN_NAME;
    }

    public Format[] getSupportedInputFormats() {
        if (getLocator() != null) {
            return super.getSupportedInputFormats();
        }
        double sampleRate = MediaUtils.MAX_AUDIO_SAMPLE_RATE;
        if (sampleRate == -1.0d && Constants.AUDIO_SAMPLE_RATES.length != 0) {
            sampleRate = Constants.AUDIO_SAMPLE_RATES[0];
        }
        return WASAPISystem.getFormatsToInitializeIAudioClient(new AudioFormat(AudioFormat.LINEAR, sampleRate, 16, 2, 0, 1, -1, -1.0d, Format.byteArray));
    }

    private void maybeCloseResampler() {
        Codec resampler = this.resampler;
        if (resampler != null) {
            this.resampler = null;
            this.resamplerInBuffer = null;
            this.resamplerOutBuffer = null;
            try {
                resampler.close();
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    ThreadDeath t2 = (ThreadDeath) t;
                } else {
                    logger.error("Failed to close resampler.", t);
                }
            }
        }
    }

    private int maybeIAudioRenderClientWrite(byte[] data, int offset, int length, int srcSampleSize, int srcChannels) {
        try {
            return WASAPI.IAudioRenderClient_Write(this.iAudioRenderClient, data, offset, length, srcSampleSize, srcChannels, this.dstSampleSize, this.dstChannels);
        } catch (HResultException hre) {
            logger.error("IAudioRenderClient_Write", hre);
            return 0;
        }
    }

    private void maybeOpenResampler() {
        AudioFormat inFormat = this.inputFormat;
        AudioFormat outFormat = this.dstFormat;
        if (inFormat.getSampleRate() == outFormat.getSampleRate() && inFormat.getSampleSizeInBits() == outFormat.getSampleSizeInBits()) {
            return;
        }
        AudioFormat outFormat2;
        int channels = inFormat.getChannels();
        if (outFormat.getChannels() != channels) {
            outFormat2 = new AudioFormat(outFormat.getEncoding(), outFormat.getSampleRate(), outFormat.getSampleSizeInBits(), channels, outFormat.getEndian(), outFormat.getSigned(), -1, -1.0d, outFormat.getDataType());
        } else {
            outFormat2 = outFormat;
        }
        Codec resampler = maybeOpenResampler(inFormat, outFormat2);
        if (resampler == null) {
            throw new IllegalStateException("Failed to open a codec to resample [" + inFormat + "] into [" + outFormat2 + "].");
        }
        this.resampler = resampler;
        this.resamplerChannels = outFormat2.getChannels();
        this.resamplerSampleSize = WASAPISystem.getSampleSizeInBytes(outFormat2);
        this.resamplerFrameSize = this.resamplerChannels * this.resamplerSampleSize;
    }

    public static Codec maybeOpenResampler(AudioFormat inFormat, AudioFormat outFormat) {
        List<String> classNames = PlugInManager.getPlugInList(inFormat, outFormat, 2);
        if (classNames == null) {
            return null;
        }
        for (String className : classNames) {
            try {
                Codec codec = (Codec) Class.forName(className).newInstance();
                Format setInput = codec.setInputFormat(inFormat);
                if (setInput != null && inFormat.matches(setInput)) {
                    Format setOutput = codec.setOutputFormat(outFormat);
                    if (setOutput != null && outFormat.matches(setOutput)) {
                        codec.open();
                        return codec;
                    }
                }
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    ThreadDeath t2 = (ThreadDeath) t;
                } else {
                    logger.warn("Failed to open resampler " + className, t);
                }
            }
        }
        return null;
    }

    public synchronized void open() throws ResourceUnavailableException {
        if (this.iAudioClient == 0) {
            MediaLocator locator = null;
            long eventHandle;
            long iAudioClient;
            long iAudioRenderClient;
            try {
                boolean z;
                locator = getLocator();
                if (locator == null) {
                    z = true;
                } else {
                    z = false;
                }
                this.locatorIsNull = z;
                if (!z) {
                    AudioFormat[] formats = getFormatsToInitializeIAudioClient();
                    eventHandle = WASAPI.CreateEvent(0, false, false, null);
                    iAudioClient = ((WASAPISystem) this.audioSystem).initializeIAudioClient(locator, this.dataFlow, 0, eventHandle, 20, formats);
                    if (iAudioClient == 0) {
                        throw new ResourceUnavailableException("Failed to initialize IAudioClient for MediaLocator " + locator + " and AudioSystem.DataFlow " + this.dataFlow);
                    }
                    iAudioRenderClient = WASAPI.IAudioClient_GetService(iAudioClient, WASAPI.IID_IAudioRenderClient);
                    if (iAudioRenderClient == 0) {
                        throw new ResourceUnavailableException("IAudioClient_GetService(IID_IAudioRenderClient)");
                    }
                    this.srcFormat = (AudioFormat) this.inputFormat;
                    this.dstFormat = findFirst(formats);
                    this.devicePeriod = WASAPI.IAudioClient_GetDefaultDevicePeriod(iAudioClient) / 10000;
                    this.numBufferFrames = WASAPI.IAudioClient_GetBufferSize(iAudioClient);
                    int dstSampleRate = (int) this.dstFormat.getSampleRate();
                    this.bufferDuration = (((long) this.numBufferFrames) * 1000) / ((long) dstSampleRate);
                    if (this.devicePeriod <= 1) {
                        this.devicePeriod = this.bufferDuration / 2;
                        if (this.devicePeriod > 10 || this.devicePeriod <= 1) {
                            this.devicePeriod = 10;
                        }
                    }
                    this.devicePeriodInFrames = (int) ((this.devicePeriod * ((long) dstSampleRate)) / 1000);
                    this.dstChannels = this.dstFormat.getChannels();
                    this.dstSampleSize = WASAPISystem.getSampleSizeInBytes(this.dstFormat);
                    maybeOpenResampler();
                    this.srcChannels = this.srcFormat.getChannels();
                    this.srcSampleSize = WASAPISystem.getSampleSizeInBytes(this.srcFormat);
                    this.srcFrameSize = this.srcSampleSize * this.srcChannels;
                    this.remainder = new byte[(this.numBufferFrames * this.srcFrameSize)];
                    this.remainderLength = this.remainder.length;
                    if (this.resampler != null) {
                        this.resamplerInBuffer = new Buffer();
                        this.resamplerInBuffer.setData(this.remainder);
                        this.resamplerInBuffer.setFormat(this.srcFormat);
                        this.resamplerOutBuffer = new Buffer();
                    }
                    this.writeIsMalfunctioningSince = 0;
                    this.writeIsMalfunctioningTimeout = 2 * Math.max(this.bufferDuration, this.devicePeriod);
                    this.eventHandle = eventHandle;
                    eventHandle = 0;
                    this.iAudioClient = iAudioClient;
                    iAudioClient = 0;
                    this.iAudioRenderClient = iAudioRenderClient;
                    if (0 != 0) {
                        WASAPI.IAudioRenderClient_Release(0);
                    }
                    if (0 != 0) {
                        WASAPI.IAudioClient_Release(0);
                        maybeCloseResampler();
                    }
                    if (eventHandle != 0) {
                        WASAPI.CloseHandle(eventHandle);
                    }
                }
                super.open();
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    ThreadDeath t2 = (ThreadDeath) t;
                } else {
                    logger.error("Failed to open a WASAPIRenderer on audio endpoint device " + toString(locator), t);
                    if (t instanceof ResourceUnavailableException) {
                        ResourceUnavailableException t3 = (ResourceUnavailableException) t;
                    } else {
                        new ResourceUnavailableException().initCause(t);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void playbackDevicePropertyChange(PropertyChangeEvent ev) {
        waitWhileBusy();
        boolean open = !(this.iAudioClient == 0 || this.iAudioRenderClient == 0) || this.locatorIsNull;
        if (open) {
            boolean start = this.started;
            close();
            try {
                open();
                if (start) {
                    start();
                }
            } catch (ResourceUnavailableException rue) {
                throw new UndeclaredThrowableException(rue);
            }
        }
    }

    private void popFromRemainder(int length) {
        this.remainderLength = pop(this.remainder, this.remainderLength, length);
    }

    public static int pop(byte[] array, int arrayLength, int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length");
        } else if (length == 0) {
            return arrayLength;
        } else {
            int newArrayLength = arrayLength - length;
            if (newArrayLength > 0) {
                int i = 0;
                int j = length;
                while (i < newArrayLength) {
                    array[i] = array[j];
                    i++;
                    j++;
                }
            } else {
                newArrayLength = 0;
            }
            return newArrayLength;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:111:0x01cd A:{Catch:{ HResultException -> 0x0105, all -> 0x0115 }} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00db  */
    /* JADX WARNING: Removed duplicated region for block: B:170:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x00fc  */
    /* JADX WARNING: Missing block: B:29:0x0051, code skipped:
            r23 = 0;
            r24 = 0;
     */
    /* JADX WARNING: Missing block: B:33:0x005d, code skipped:
            if (r30.eventHandle != 0) goto L_0x0121;
     */
    /* JADX WARNING: Missing block: B:36:?, code skipped:
            r21 = org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI.IAudioClient_GetCurrentPadding(r30.iAudioClient);
     */
    /* JADX WARNING: Missing block: B:73:0x0105, code skipped:
            r16 = move-exception;
     */
    /* JADX WARNING: Missing block: B:74:0x0106, code skipped:
            r21 = 0;
            r23 = 1;
     */
    /* JADX WARNING: Missing block: B:76:?, code skipped:
            logger.error("IAudioClient_GetCurrentPadding", r16);
     */
    /* JADX WARNING: Missing block: B:78:0x0116, code skipped:
            monitor-enter(r30);
     */
    /* JADX WARNING: Missing block: B:81:?, code skipped:
            r30.busy = false;
            notifyAll();
     */
    /* JADX WARNING: Missing block: B:86:?, code skipped:
            r21 = r30.numBufferFrames;
     */
    /* JADX WARNING: Missing block: B:166:?, code skipped:
            return r23;
     */
    public int process(javax.media.Buffer r31) {
        /*
        r30 = this;
        r19 = r31.getLength();
        r4 = 1;
        r0 = r19;
        if (r0 >= r4) goto L_0x000c;
    L_0x0009:
        r23 = 0;
    L_0x000b:
        return r23;
    L_0x000c:
        r4 = r31.getData();
        r4 = (byte[]) r4;
        r13 = r4;
        r13 = (byte[]) r13;
        r22 = r31.getOffset();
        monitor-enter(r30);
        r0 = r30;
        r4 = r0.iAudioClient;	 Catch:{ all -> 0x0038 }
        r10 = 0;
        r4 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r4 == 0) goto L_0x002e;
    L_0x0024:
        r0 = r30;
        r4 = r0.iAudioRenderClient;	 Catch:{ all -> 0x0038 }
        r10 = 0;
        r4 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r4 != 0) goto L_0x003e;
    L_0x002e:
        r0 = r30;
        r4 = r0.locatorIsNull;	 Catch:{ all -> 0x0038 }
        if (r4 == 0) goto L_0x003b;
    L_0x0034:
        r23 = 0;
    L_0x0036:
        monitor-exit(r30);	 Catch:{ all -> 0x0038 }
        goto L_0x000b;
    L_0x0038:
        r4 = move-exception;
        monitor-exit(r30);	 Catch:{ all -> 0x0038 }
        throw r4;
    L_0x003b:
        r23 = 1;
        goto L_0x0036;
    L_0x003e:
        r0 = r30;
        r4 = r0.started;	 Catch:{ all -> 0x0038 }
        if (r4 != 0) goto L_0x0048;
    L_0x0044:
        r23 = 1;
        monitor-exit(r30);	 Catch:{ all -> 0x0038 }
        goto L_0x000b;
    L_0x0048:
        r30.waitWhileBusy();	 Catch:{ all -> 0x0038 }
        r4 = 1;
        r0 = r30;
        r0.busy = r4;	 Catch:{ all -> 0x0038 }
        monitor-exit(r30);	 Catch:{ all -> 0x0038 }
        r23 = 0;
        r24 = 0;
        r0 = r30;
        r4 = r0.eventHandle;	 Catch:{ all -> 0x0115 }
        r10 = 0;
        r4 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r4 != 0) goto L_0x0121;
    L_0x005f:
        r0 = r30;
        r4 = r0.iAudioClient;	 Catch:{ HResultException -> 0x0105 }
        r21 = org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI.IAudioClient_GetCurrentPadding(r4);	 Catch:{ HResultException -> 0x0105 }
    L_0x0067:
        r4 = 1;
        r0 = r23;
        if (r0 == r4) goto L_0x00da;
    L_0x006c:
        r0 = r30;
        r4 = r0.numBufferFrames;	 Catch:{ all -> 0x0115 }
        r20 = r4 - r21;
        if (r20 != 0) goto L_0x0199;
    L_0x0074:
        r0 = r30;
        r4 = r0.eventHandle;	 Catch:{ all -> 0x0115 }
        r10 = 0;
        r4 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r4 != 0) goto L_0x0129;
    L_0x007e:
        r23 = r23 | 2;
        r0 = r30;
        r0 = r0.devicePeriod;	 Catch:{ all -> 0x0115 }
        r24 = r0;
        r0 = r30;
        r4 = r0.writeIsMalfunctioningSince;	 Catch:{ all -> 0x0115 }
        r10 = 0;
        r4 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r4 != 0) goto L_0x0096;
    L_0x0090:
        r4 = 1;
        r0 = r30;
        r0.setWriteIsMalfunctioning(r4);	 Catch:{ all -> 0x0115 }
    L_0x0096:
        r4 = r23 & 2;
        r5 = 2;
        if (r4 != r5) goto L_0x00da;
    L_0x009b:
        r0 = r30;
        r4 = r0.writeIsMalfunctioningSince;	 Catch:{ all -> 0x0115 }
        r10 = 0;
        r4 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r4 == 0) goto L_0x00da;
    L_0x00a5:
        r4 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x0115 }
        r0 = r30;
        r10 = r0.writeIsMalfunctioningSince;	 Catch:{ all -> 0x0115 }
        r28 = r4 - r10;
        r0 = r30;
        r4 = r0.writeIsMalfunctioningTimeout;	 Catch:{ all -> 0x0115 }
        r4 = (r28 > r4 ? 1 : (r28 == r4 ? 0 : -1));
        if (r4 <= 0) goto L_0x00da;
    L_0x00b7:
        r4 = 0;
        r0 = r30;
        r0.remainderLength = r4;	 Catch:{ all -> 0x0115 }
        r23 = 1;
        r4 = logger;	 Catch:{ all -> 0x0115 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0115 }
        r5.<init>();	 Catch:{ all -> 0x0115 }
        r9 = "Audio endpoint device appears to be malfunctioning: ";
        r5 = r5.append(r9);	 Catch:{ all -> 0x0115 }
        r9 = r30.getLocator();	 Catch:{ all -> 0x0115 }
        r5 = r5.append(r9);	 Catch:{ all -> 0x0115 }
        r5 = r5.toString();	 Catch:{ all -> 0x0115 }
        r4.warn(r5);	 Catch:{ all -> 0x0115 }
    L_0x00da:
        monitor-enter(r30);
        r4 = 0;
        r0 = r30;
        r0.busy = r4;	 Catch:{ all -> 0x0297 }
        r30.notifyAll();	 Catch:{ all -> 0x0297 }
        monitor-exit(r30);	 Catch:{ all -> 0x0297 }
        r4 = r23 & 2;
        r5 = 2;
        if (r4 != r5) goto L_0x000b;
    L_0x00e9:
        r4 = 0;
        r4 = (r24 > r4 ? 1 : (r24 == r4 ? 0 : -1));
        if (r4 <= 0) goto L_0x000b;
    L_0x00ef:
        r18 = 0;
        monitor-enter(r30);
        r0 = r30;
        r1 = r24;
        r0.wait(r1);	 Catch:{ InterruptedException -> 0x029d }
    L_0x00f9:
        monitor-exit(r30);	 Catch:{ all -> 0x02a2 }
        if (r18 == 0) goto L_0x000b;
    L_0x00fc:
        r4 = java.lang.Thread.currentThread();
        r4.interrupt();
        goto L_0x000b;
    L_0x0105:
        r16 = move-exception;
        r21 = 0;
        r23 = 1;
        r4 = logger;	 Catch:{ all -> 0x0115 }
        r5 = "IAudioClient_GetCurrentPadding";
        r0 = r16;
        r4.error(r5, r0);	 Catch:{ all -> 0x0115 }
        goto L_0x0067;
    L_0x0115:
        r4 = move-exception;
        monitor-enter(r30);
        r5 = 0;
        r0 = r30;
        r0.busy = r5;	 Catch:{ all -> 0x029a }
        r30.notifyAll();	 Catch:{ all -> 0x029a }
        monitor-exit(r30);	 Catch:{ all -> 0x029a }
        throw r4;
    L_0x0121:
        r0 = r30;
        r0 = r0.numBufferFrames;	 Catch:{ all -> 0x0115 }
        r21 = r0;
        goto L_0x0067;
    L_0x0129:
        r0 = r30;
        r4 = r0.remainder;	 Catch:{ all -> 0x0115 }
        r4 = r4.length;	 Catch:{ all -> 0x0115 }
        r0 = r30;
        r5 = r0.remainderLength;	 Catch:{ all -> 0x0115 }
        r26 = r4 - r5;
        if (r26 <= 0) goto L_0x017f;
    L_0x0136:
        r0 = r26;
        r1 = r19;
        if (r0 <= r1) goto L_0x013e;
    L_0x013c:
        r26 = r19;
    L_0x013e:
        r0 = r30;
        r4 = r0.remainder;	 Catch:{ all -> 0x0115 }
        r0 = r30;
        r5 = r0.remainderLength;	 Catch:{ all -> 0x0115 }
        r0 = r22;
        r1 = r26;
        java.lang.System.arraycopy(r13, r0, r4, r5, r1);	 Catch:{ all -> 0x0115 }
        r0 = r30;
        r4 = r0.remainderLength;	 Catch:{ all -> 0x0115 }
        r4 = r4 + r26;
        r0 = r30;
        r0.remainderLength = r4;	 Catch:{ all -> 0x0115 }
        r0 = r19;
        r1 = r26;
        if (r0 <= r1) goto L_0x016d;
    L_0x015d:
        r4 = r19 - r26;
        r0 = r31;
        r0.setLength(r4);	 Catch:{ all -> 0x0115 }
        r4 = r22 + r26;
        r0 = r31;
        r0.setOffset(r4);	 Catch:{ all -> 0x0115 }
        r23 = r23 | 2;
    L_0x016d:
        r0 = r30;
        r4 = r0.writeIsMalfunctioningSince;	 Catch:{ all -> 0x0115 }
        r10 = 0;
        r4 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r4 == 0) goto L_0x0096;
    L_0x0177:
        r4 = 0;
        r0 = r30;
        r0.setWriteIsMalfunctioning(r4);	 Catch:{ all -> 0x0115 }
        goto L_0x0096;
    L_0x017f:
        r23 = r23 | 2;
        r0 = r30;
        r0 = r0.devicePeriod;	 Catch:{ all -> 0x0115 }
        r24 = r0;
        r0 = r30;
        r4 = r0.writeIsMalfunctioningSince;	 Catch:{ all -> 0x0115 }
        r10 = 0;
        r4 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r4 != 0) goto L_0x0096;
    L_0x0191:
        r4 = 1;
        r0 = r30;
        r0.setWriteIsMalfunctioning(r4);	 Catch:{ all -> 0x0115 }
        goto L_0x0096;
    L_0x0199:
        r0 = r30;
        r4 = r0.remainderLength;	 Catch:{ all -> 0x0115 }
        r14 = r4 + r19;
        r0 = r30;
        r4 = r0.srcFrameSize;	 Catch:{ all -> 0x0115 }
        r4 = r4 * r20;
        r8 = java.lang.Math.min(r14, r4);	 Catch:{ all -> 0x0115 }
        r0 = r30;
        r4 = r0.remainderLength;	 Catch:{ all -> 0x0115 }
        if (r4 <= 0) goto L_0x0254;
    L_0x01af:
        r0 = r30;
        r6 = r0.remainder;	 Catch:{ all -> 0x0115 }
        r7 = 0;
        r0 = r30;
        r4 = r0.remainderLength;	 Catch:{ all -> 0x0115 }
        r26 = r8 - r4;
        if (r26 > 0) goto L_0x0211;
    L_0x01bc:
        r23 = r23 | 2;
    L_0x01be:
        r0 = r30;
        r4 = r0.srcFrameSize;	 Catch:{ all -> 0x0115 }
        r4 = r8 / r4;
        if (r4 != 0) goto L_0x0259;
    L_0x01c6:
        r27 = 0;
    L_0x01c8:
        r4 = 1;
        r0 = r23;
        if (r0 == r4) goto L_0x0096;
    L_0x01cd:
        if (r6 != r13) goto L_0x028c;
    L_0x01cf:
        if (r27 != 0) goto L_0x01e9;
    L_0x01d1:
        r0 = r30;
        r4 = r0.remainder;	 Catch:{ all -> 0x0115 }
        r0 = r30;
        r5 = r0.remainderLength;	 Catch:{ all -> 0x0115 }
        r0 = r22;
        java.lang.System.arraycopy(r13, r0, r4, r5, r8);	 Catch:{ all -> 0x0115 }
        r0 = r30;
        r4 = r0.remainderLength;	 Catch:{ all -> 0x0115 }
        r4 = r4 + r8;
        r0 = r30;
        r0.remainderLength = r4;	 Catch:{ all -> 0x0115 }
        r27 = r8;
    L_0x01e9:
        r0 = r19;
        r1 = r27;
        if (r0 <= r1) goto L_0x01ff;
    L_0x01ef:
        r4 = r19 - r27;
        r0 = r31;
        r0.setLength(r4);	 Catch:{ all -> 0x0115 }
        r4 = r22 + r27;
        r0 = r31;
        r0.setOffset(r4);	 Catch:{ all -> 0x0115 }
        r23 = r23 | 2;
    L_0x01ff:
        r0 = r30;
        r4 = r0.writeIsMalfunctioningSince;	 Catch:{ all -> 0x0115 }
        r10 = 0;
        r4 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r4 == 0) goto L_0x0096;
    L_0x0209:
        r4 = 0;
        r0 = r30;
        r0.setWriteIsMalfunctioning(r4);	 Catch:{ all -> 0x0115 }
        goto L_0x0096;
    L_0x0211:
        r0 = r26;
        r1 = r19;
        if (r0 <= r1) goto L_0x0219;
    L_0x0217:
        r26 = r19;
    L_0x0219:
        r0 = r30;
        r4 = r0.remainder;	 Catch:{ all -> 0x0115 }
        r0 = r30;
        r5 = r0.remainderLength;	 Catch:{ all -> 0x0115 }
        r0 = r22;
        r1 = r26;
        java.lang.System.arraycopy(r13, r0, r4, r5, r1);	 Catch:{ all -> 0x0115 }
        r0 = r30;
        r4 = r0.remainderLength;	 Catch:{ all -> 0x0115 }
        r4 = r4 + r26;
        r0 = r30;
        r0.remainderLength = r4;	 Catch:{ all -> 0x0115 }
        r0 = r30;
        r4 = r0.remainderLength;	 Catch:{ all -> 0x0115 }
        if (r8 <= r4) goto L_0x023c;
    L_0x0238:
        r0 = r30;
        r8 = r0.remainderLength;	 Catch:{ all -> 0x0115 }
    L_0x023c:
        r0 = r19;
        r1 = r26;
        if (r0 <= r1) goto L_0x01be;
    L_0x0242:
        r4 = r19 - r26;
        r0 = r31;
        r0.setLength(r4);	 Catch:{ all -> 0x0115 }
        r4 = r22 + r26;
        r0 = r31;
        r0.setOffset(r4);	 Catch:{ all -> 0x0115 }
        r23 = r23 | 2;
        goto L_0x01be;
    L_0x0254:
        r6 = r13;
        r7 = r22;
        goto L_0x01be;
    L_0x0259:
        r15 = r30.getGainControl();	 Catch:{ all -> 0x0115 }
        if (r15 == 0) goto L_0x0262;
    L_0x025f:
        org.jitsi.service.neomedia.BasicVolumeControl.applyGain(r15, r6, r7, r8);	 Catch:{ all -> 0x0115 }
    L_0x0262:
        r0 = r30;
        r4 = r0.iAudioRenderClient;	 Catch:{ HResultException -> 0x027c }
        r0 = r30;
        r9 = r0.srcSampleSize;	 Catch:{ HResultException -> 0x027c }
        r0 = r30;
        r10 = r0.srcChannels;	 Catch:{ HResultException -> 0x027c }
        r0 = r30;
        r11 = r0.dstSampleSize;	 Catch:{ HResultException -> 0x027c }
        r0 = r30;
        r12 = r0.dstChannels;	 Catch:{ HResultException -> 0x027c }
        r27 = org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI.IAudioRenderClient_Write(r4, r6, r7, r8, r9, r10, r11, r12);	 Catch:{ HResultException -> 0x027c }
        goto L_0x01c8;
    L_0x027c:
        r16 = move-exception;
        r27 = 0;
        r23 = 1;
        r4 = logger;	 Catch:{ all -> 0x0115 }
        r5 = "IAudioRenderClient_Write";
        r0 = r16;
        r4.error(r5, r0);	 Catch:{ all -> 0x0115 }
        goto L_0x01c8;
    L_0x028c:
        if (r27 <= 0) goto L_0x01ff;
    L_0x028e:
        r0 = r30;
        r1 = r27;
        r0.popFromRemainder(r1);	 Catch:{ all -> 0x0115 }
        goto L_0x01ff;
    L_0x0297:
        r4 = move-exception;
        monitor-exit(r30);	 Catch:{ all -> 0x0297 }
        throw r4;
    L_0x029a:
        r4 = move-exception;
        monitor-exit(r30);	 Catch:{ all -> 0x029a }
        throw r4;
    L_0x029d:
        r17 = move-exception;
        r18 = 1;
        goto L_0x00f9;
    L_0x02a2:
        r4 = move-exception;
        monitor-exit(r30);	 Catch:{ all -> 0x02a2 }
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.jmfext.media.renderer.audio.WASAPIRenderer.process(javax.media.Buffer):int");
    }

    private int resample(int inOffset, int inLength) {
        this.resamplerInBuffer.setLength(inLength);
        this.resamplerInBuffer.setOffset(inOffset);
        this.resamplerOutBuffer.setDiscard(false);
        this.resamplerOutBuffer.setLength(0);
        this.resamplerOutBuffer.setOffset(0);
        if (this.resampler.process(this.resamplerInBuffer, this.resamplerOutBuffer) == 1 || this.resamplerOutBuffer.isDiscard()) {
            return 0;
        }
        return inLength;
    }

    /* access modifiers changed from: private */
    public void runInEventHandleCmd(Runnable eventHandleCmd) {
        try {
            AbstractAudioRenderer.useAudioThreadPriority();
            int wfso;
            do {
                synchronized (this) {
                    if (eventHandleCmd.equals(this.eventHandleCmd)) {
                        if (this.iAudioClient != 0 && this.iAudioRenderClient != 0 && this.started) {
                            long eventHandle = this.eventHandle;
                            if (eventHandle != 0) {
                                int numPaddingFrames;
                                waitWhileBusy();
                                this.busy = true;
                                try {
                                    numPaddingFrames = WASAPI.IAudioClient_GetCurrentPadding(this.iAudioClient);
                                } catch (HResultException hre) {
                                    numPaddingFrames = this.numBufferFrames;
                                    logger.error("IAudioClient_GetCurrentPadding", hre);
                                } catch (Throwable th) {
                                    synchronized (this) {
                                        this.busy = false;
                                        notifyAll();
                                    }
                                }
                                int numFramesRequested = this.numBufferFrames - numPaddingFrames;
                                if (this.resampler != null) {
                                    int srcSampleRate = (int) this.srcFormat.getSampleRate();
                                    int dstSampleRate = (int) this.dstFormat.getSampleRate();
                                    if (srcSampleRate != dstSampleRate) {
                                        numFramesRequested = (numFramesRequested * srcSampleRate) / dstSampleRate;
                                    }
                                }
                                if (numFramesRequested > 0) {
                                    int written;
                                    int remainderFrames = this.remainderLength / this.srcFrameSize;
                                    if (numFramesRequested > remainderFrames && remainderFrames >= this.devicePeriodInFrames) {
                                        numFramesRequested = remainderFrames;
                                    }
                                    int toWrite = numFramesRequested * this.srcFrameSize;
                                    if (toWrite - this.remainderLength > 0) {
                                        Arrays.fill(this.remainder, this.remainderLength, toWrite, (byte) 0);
                                        this.remainderLength = toWrite;
                                    }
                                    GainControl gainControl = getGainControl();
                                    if (!(gainControl == null || toWrite == 0)) {
                                        BasicVolumeControl.applyGain(gainControl, this.remainder, 0, toWrite);
                                    }
                                    if (this.resampler == null) {
                                        written = maybeIAudioRenderClientWrite(this.remainder, 0, toWrite, this.srcSampleSize, this.srcChannels);
                                    } else if (resample(0, toWrite) == toWrite) {
                                        maybeIAudioRenderClientWrite((byte[]) this.resamplerOutBuffer.getData(), this.resamplerOutBuffer.getOffset(), (this.resamplerOutBuffer.getLength() / this.resamplerFrameSize) * this.resamplerFrameSize, this.resamplerSampleSize, this.resamplerChannels);
                                        written = toWrite;
                                    } else {
                                        written = 0;
                                    }
                                    if (written != 0) {
                                        popFromRemainder(written);
                                        if (this.writeIsMalfunctioningSince != 0) {
                                            setWriteIsMalfunctioning(false);
                                        }
                                    }
                                }
                                synchronized (this) {
                                    this.busy = false;
                                    notifyAll();
                                }
                                try {
                                    wfso = WASAPI.WaitForSingleObject(eventHandle, this.devicePeriod);
                                } catch (HResultException hre2) {
                                    wfso = -1;
                                    logger.error("WaitForSingleObject", hre2);
                                }
                                if (wfso == -1) {
                                    break;
                                }
                            } else {
                                throw new IllegalStateException("eventHandle");
                            }
                        }
                        break;
                    }
                    break;
                }
            } while (wfso != 128);
            synchronized (this) {
                if (eventHandleCmd.equals(this.eventHandleCmd)) {
                    this.eventHandleCmd = null;
                    notifyAll();
                }
            }
        } catch (Throwable th2) {
            synchronized (this) {
                if (eventHandleCmd.equals(this.eventHandleCmd)) {
                    this.eventHandleCmd = null;
                    notifyAll();
                }
            }
        }
    }

    public synchronized Format setInputFormat(Format format) {
        Format inputFormat;
        if (this.iAudioClient == 0 && this.iAudioRenderClient == 0) {
            inputFormat = super.setInputFormat(format);
        } else {
            inputFormat = null;
        }
        return inputFormat;
    }

    private void setWriteIsMalfunctioning(boolean writeIsMalfunctioning) {
        if (!writeIsMalfunctioning) {
            this.writeIsMalfunctioningSince = 0;
        } else if (this.writeIsMalfunctioningSince == 0) {
            this.writeIsMalfunctioningSince = System.currentTimeMillis();
        }
    }

    public synchronized void start() {
        if (this.iAudioClient != 0) {
            waitWhileBusy();
            waitWhileEventHandleCmd();
            if (this.remainder != null) {
                if (this.remainderLength > 0) {
                    int i = this.remainder.length - 1;
                    for (int j = this.remainderLength - 1; j >= 0; j--) {
                        this.remainder[i] = this.remainder[j];
                        i--;
                    }
                } else if (this.remainderLength < 0) {
                    this.remainderLength = 0;
                }
                int silence = this.remainder.length - this.remainderLength;
                if (silence > 0) {
                    Arrays.fill(this.remainder, 0, silence, (byte) 0);
                }
                this.remainderLength = this.remainder.length;
            }
            Runnable eventHandleCmd;
            try {
                WASAPI.IAudioClient_Start(this.iAudioClient);
                this.started = true;
                if (this.eventHandle != 0 && this.eventHandleCmd == null) {
                    eventHandleCmd = new Runnable() {
                        public void run() {
                            WASAPIRenderer.this.runInEventHandleCmd(this);
                        }
                    };
                    if (this.eventHandleExecutor == null) {
                        this.eventHandleExecutor = Executors.newSingleThreadExecutor();
                    }
                    this.eventHandleCmd = eventHandleCmd;
                    this.eventHandleExecutor.execute(eventHandleCmd);
                    if (!true) {
                        if (eventHandleCmd.equals(this.eventHandleCmd)) {
                            this.eventHandleCmd = null;
                        }
                    }
                }
            } catch (HResultException hre) {
                if (hre.getHResult() != WASAPI.AUDCLNT_E_NOT_STOPPED) {
                    logger.error("IAudioClient_Start", hre);
                }
            } catch (Throwable th) {
                if (!false) {
                    if (eventHandleCmd.equals(this.eventHandleCmd)) {
                        this.eventHandleCmd = null;
                    }
                }
            }
        } else if (this.locatorIsNull) {
            this.started = true;
        }
    }

    public synchronized void stop() {
        if (this.iAudioClient != 0) {
            waitWhileBusy();
            try {
                WASAPI.IAudioClient_Stop(this.iAudioClient);
                this.started = false;
                waitWhileEventHandleCmd();
                this.writeIsMalfunctioningSince = 0;
            } catch (HResultException hre) {
                logger.error("IAudioClient_Stop", hre);
            }
        } else if (this.locatorIsNull) {
            this.started = false;
        }
        return;
    }

    private String toString(MediaLocator locator) {
        if (locator == null) {
            return "null";
        }
        String s = null;
        try {
            String id = locator.getRemainder();
            if (id != null) {
                CaptureDeviceInfo2 cdi2 = ((WASAPISystem) this.audioSystem).getDevice(this.dataFlow, locator);
                if (cdi2 != null) {
                    String name = cdi2.getName();
                    if (!(name == null || id.equals(name))) {
                        s = id + " with friendly name " + name;
                    }
                }
                if (s == null) {
                    s = id;
                }
            }
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                ThreadDeath t2 = (ThreadDeath) t;
            }
        }
        if (s == null) {
            return locator.toString();
        }
        return s;
    }

    private synchronized void waitWhileBusy() {
        boolean interrupted = false;
        while (this.busy) {
            try {
                wait(this.devicePeriod);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private synchronized void waitWhileEventHandleCmd() {
        if (this.eventHandle == 0) {
            throw new IllegalStateException("eventHandle");
        }
        boolean interrupted = false;
        while (this.eventHandleCmd != null) {
            try {
                wait(this.devicePeriod);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
