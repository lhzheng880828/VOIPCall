package org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.media.Buffer;
import javax.media.Codec;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.Renderer;
import javax.media.ResourceUnavailableException;
import javax.media.control.FormatControl;
import javax.media.format.AudioFormat;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.PushBufferStream;
import org.jitsi.impl.neomedia.device.AudioSystem;
import org.jitsi.impl.neomedia.device.AudioSystem.DataFlow;
import org.jitsi.impl.neomedia.device.CaptureDeviceInfo2;
import org.jitsi.impl.neomedia.device.DeviceSystem;
import org.jitsi.impl.neomedia.device.WASAPISystem;
import org.jitsi.impl.neomedia.jmfext.media.protocol.AbstractPushBufferStream;
import org.jitsi.impl.neomedia.jmfext.media.renderer.audio.AbstractAudioRenderer;
import org.jitsi.impl.neomedia.jmfext.media.renderer.audio.WASAPIRenderer;
import org.jitsi.util.Logger;

public class WASAPIStream extends AbstractPushBufferStream<DataSource> {
    private static final int CAPTURE_INPUT_STREAM_INDEX = 0;
    private static final boolean DEFAULT_SOURCE_MODE = true;
    private static final int RENDER_INPUT_STREAM_INDEX = 1;
    /* access modifiers changed from: private|static */
    public static Logger logger = Logger.getLogger(WASAPIStream.class);
    private boolean aec;
    private int bufferMaxLength;
    private int bufferSize;
    private AudioCaptureClient capture;
    private int captureBufferMaxLength;
    private PtrMediaBuffer captureIMediaBuffer;
    private boolean captureIsBusy;
    private double captureNanosPerByte;
    private long devicePeriod;
    private long dmoOutputDataBuffer;
    private AudioFormat effectiveFormat;
    private AudioFormat format;
    private long iMediaBuffer;
    private long iMediaObject;
    private MediaLocator locator;
    private byte[] processInputBuffer;
    private Thread processThread;
    private byte[] processed;
    private int processedLength;
    private final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
            try {
                WASAPIStream.this.propertyChange(ev);
            } catch (Exception e) {
                StringBuilder msg = new StringBuilder();
                msg.append("Failed to handle a change to the value of the property ");
                msg.append(ev.getPropertyName());
                Object source = ev.getSource();
                if (source != null) {
                    msg.append(" of a ");
                    msg.append(source.getClass());
                }
                msg.append('.');
                WASAPIStream.logger.error(msg, e);
            }
        }
    };
    private AudioCaptureClient render;
    private int renderBufferMaxLength;
    private double renderBytesPerNano;
    private MediaLocator renderDevice;
    private int renderDeviceIndex;
    private PtrMediaBuffer renderIMediaBuffer;
    private boolean renderIsBusy;
    private Renderer renderer;
    private boolean replenishRender;
    private Codec resampler;
    private Buffer resamplerBuffer;
    private boolean sourceMode;
    private boolean started;

    private static AudioFormat findClosestMatch(Format[] formats, AudioFormat format, Class<? extends AudioFormat> clazz) {
        AudioFormat match = findFirstMatch(formats, format, clazz);
        if (match != null) {
            return match;
        }
        match = findFirstMatch(formats, new AudioFormat(format.getEncoding(), format.getSampleRate(), format.getSampleSizeInBits(), -1, format.getEndian(), format.getSigned(), -1, -1.0d, format.getDataType()), clazz);
        if (match != null) {
            return match;
        }
        return findFirstMatch(formats, new AudioFormat(format.getEncoding(), -1.0d, format.getSampleSizeInBits(), -1, format.getEndian(), format.getSigned(), -1, -1.0d, format.getDataType()), clazz);
    }

    private static AudioFormat findClosestMatch(List<AudioFormat> formats, AudioFormat format, Class<? extends AudioFormat> clazz) {
        return findClosestMatch((Format[]) formats.toArray(new Format[formats.size()]), format, (Class) clazz);
    }

    private static AudioFormat findFirstMatch(Format[] formats, AudioFormat format, Class<? extends AudioFormat> clazz) {
        for (Format aFormat : formats) {
            if (aFormat.matches(format) && (clazz == null || clazz.isInstance(aFormat))) {
                return (AudioFormat) aFormat.intersects(format);
            }
        }
        return null;
    }

    private static int IMediaObject_SetXXXputType(long iMediaObject, boolean inOrOut, int dwXXXputStreamIndex, AudioFormat audioFormat, int dwFlags) throws HResultException {
        int channels = audioFormat.getChannels();
        double sampleRate = audioFormat.getSampleRate();
        int sampleSizeInBits = audioFormat.getSampleSizeInBits();
        if (-1 == channels) {
            throw new IllegalArgumentException("audioFormat.channels");
        } else if (-1.0d == sampleRate) {
            throw new IllegalArgumentException("audioFormat.sampleRate");
        } else if (-1 == sampleSizeInBits) {
            throw new IllegalArgumentException("audioFormat.sampleSizeInBits");
        } else {
            char nChannels = (char) channels;
            int nSamplesPerSec = (int) sampleRate;
            char wBitsPerSample = (char) sampleSizeInBits;
            char nBlockAlign = (char) ((nChannels * wBitsPerSample) / 8);
            long waveformatex = WASAPI.WAVEFORMATEX_alloc();
            if (waveformatex == 0) {
                throw new OutOfMemoryError("WAVEFORMATEX_alloc");
            }
            long pmt;
            try {
                WASAPI.WAVEFORMATEX_fill(waveformatex, 1, nChannels, nSamplesPerSec, nSamplesPerSec * nBlockAlign, nBlockAlign, wBitsPerSample, 0);
                pmt = VoiceCaptureDSP.MoCreateMediaType(0);
                if (pmt == 0) {
                    throw new OutOfMemoryError("MoCreateMediaType");
                }
                int hresult = VoiceCaptureDSP.DMO_MEDIA_TYPE_fill(pmt, VoiceCaptureDSP.MEDIATYPE_Audio, VoiceCaptureDSP.MEDIASUBTYPE_PCM, true, false, wBitsPerSample / 8, VoiceCaptureDSP.FORMAT_WaveFormatEx, 0, WASAPI.WAVEFORMATEX_sizeof() + 0, waveformatex);
                if (WASAPI.FAILED(hresult)) {
                    throw new HResultException(hresult, "DMO_MEDIA_TYPE_fill");
                }
                if (inOrOut) {
                    hresult = VoiceCaptureDSP.IMediaObject_SetInputType(iMediaObject, dwXXXputStreamIndex, pmt, dwFlags);
                } else {
                    hresult = VoiceCaptureDSP.IMediaObject_SetOutputType(iMediaObject, dwXXXputStreamIndex, pmt, dwFlags);
                }
                if (WASAPI.FAILED(hresult)) {
                    String str;
                    if (inOrOut) {
                        str = "IMediaObject_SetInputType";
                    } else {
                        str = "IMediaObject_SetOutputType";
                    }
                    throw new HResultException(hresult, str);
                }
                VoiceCaptureDSP.DMO_MEDIA_TYPE_setCbFormat(pmt, 0);
                VoiceCaptureDSP.DMO_MEDIA_TYPE_setFormattype(pmt, VoiceCaptureDSP.FORMAT_None);
                VoiceCaptureDSP.DMO_MEDIA_TYPE_setPbFormat(pmt, 0);
                VoiceCaptureDSP.MoDeleteMediaType(pmt);
                WASAPI.CoTaskMemFree(waveformatex);
                return hresult;
            } catch (Throwable th) {
                WASAPI.CoTaskMemFree(waveformatex);
            }
        }
    }

    private static int maybeIMediaBufferGetLength(IMediaBuffer iMediaBuffer) {
        try {
            return iMediaBuffer.GetLength();
        } catch (IOException ioe) {
            logger.error("IMediaBuffer.GetLength", ioe);
            return 0;
        }
    }

    private static int maybeIMediaBufferGetLength(long iMediaBuffer) {
        try {
            return VoiceCaptureDSP.IMediaBuffer_GetLength(iMediaBuffer);
        } catch (HResultException hre) {
            logger.error("IMediaBuffer_GetLength", hre);
            return 0;
        }
    }

    private static int maybeMediaBufferPush(long pBuffer, byte[] buffer, int offset, int length) {
        int written;
        Throwable exception;
        try {
            written = VoiceCaptureDSP.MediaBuffer_push(pBuffer, buffer, offset, length);
            exception = null;
        } catch (HResultException hre) {
            written = 0;
            exception = hre;
        }
        if (!(exception == null && written == length)) {
            Logger logger = logger;
            StringBuilder append = new StringBuilder().append("Failed to push/write ");
            if (written > 0) {
                length -= written;
            }
            logger.error(append.append(length).append(" bytes into an IMediaBuffer.").toString(), exception);
        }
        return written;
    }

    static void throwNewIOException(String message, HResultException hre) throws IOException {
        logger.error(message, hre);
        IOException ioe = new IOException(message);
        ioe.initCause(hre);
        throw ioe;
    }

    public WASAPIStream(DataSource dataSource, FormatControl formatControl) {
        super(dataSource, formatControl);
    }

    private long computeCaptureDuration(int length) {
        return (long) (((double) length) * this.captureNanosPerByte);
    }

    private int computeRenderLength(long duration) {
        return (int) (((double) duration) * this.renderBytesPerNano);
    }

    private void configureAEC(long iPropertyStore) throws HResultException {
        try {
            if (VoiceCaptureDSP.MFPKEY_WMAAECMA_FEATURE_MODE != 0) {
                long j;
                int i;
                VoiceCaptureDSP.IPropertyStore_SetValue(iPropertyStore, VoiceCaptureDSP.MFPKEY_WMAAECMA_FEATURE_MODE, true);
                AudioSystem audioSystem = ((DataSource) this.dataSource).audioSystem;
                if (VoiceCaptureDSP.MFPKEY_WMAAECMA_FEATR_AES != 0) {
                    j = VoiceCaptureDSP.MFPKEY_WMAAECMA_FEATR_AES;
                    if (audioSystem.isEchoCancel()) {
                        i = 2;
                    } else {
                        i = 0;
                    }
                    VoiceCaptureDSP.IPropertyStore_SetValue(iPropertyStore, j, i);
                }
                boolean isAGC = audioSystem.isAutomaticGainControl();
                if (VoiceCaptureDSP.MFPKEY_WMAAECMA_FEATR_AGC != 0) {
                    VoiceCaptureDSP.IPropertyStore_SetValue(iPropertyStore, VoiceCaptureDSP.MFPKEY_WMAAECMA_FEATR_AGC, isAGC);
                }
                if (VoiceCaptureDSP.MFPKEY_WMAAECMA_MIC_GAIN_BOUNDER != 0) {
                    VoiceCaptureDSP.IPropertyStore_SetValue(iPropertyStore, VoiceCaptureDSP.MFPKEY_WMAAECMA_MIC_GAIN_BOUNDER, isAGC);
                }
                if (VoiceCaptureDSP.MFPKEY_WMAAECMA_FEATR_NS != 0) {
                    j = VoiceCaptureDSP.MFPKEY_WMAAECMA_FEATR_NS;
                    if (audioSystem.isDenoise()) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    VoiceCaptureDSP.IPropertyStore_SetValue(iPropertyStore, j, i);
                }
                if (VoiceCaptureDSP.MFPKEY_WMAAECMA_FEATR_ECHO_LENGTH != 0) {
                    VoiceCaptureDSP.IPropertyStore_SetValue(iPropertyStore, VoiceCaptureDSP.MFPKEY_WMAAECMA_FEATR_ECHO_LENGTH, 256);
                }
            }
        } catch (HResultException hre) {
            logger.error("Failed to perform optional configuration on the Voice Capture DSP that implements acoustic echo cancellation (AEC).", hre);
        }
    }

    private synchronized void connect() throws IOException {
        if (this.capture == null) {
            try {
                doConnect();
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    ThreadDeath t2 = (ThreadDeath) t;
                } else {
                    logger.error("Failed to connect a WASAPIStream to an audio endpoint device.", t);
                    if (t instanceof IOException) {
                        IOException t3 = (IOException) t;
                    } else {
                        new IOException().initCause(t);
                    }
                }
            }
        }
    }

    private synchronized void disconnect() throws IOException {
        try {
            stop();
            uninitializeAEC();
            uninitializeRender();
            uninitializeCapture();
            maybeCloseResampler();
            this.effectiveFormat = null;
            this.format = null;
            this.sourceMode = false;
            ((DataSource) this.dataSource).audioSystem.removePropertyChangeListener(this.propertyChangeListener);
        } catch (Throwable th) {
            uninitializeAEC();
            uninitializeRender();
            uninitializeCapture();
            maybeCloseResampler();
            this.effectiveFormat = null;
            this.format = null;
            this.sourceMode = false;
            ((DataSource) this.dataSource).audioSystem.removePropertyChangeListener(this.propertyChangeListener);
        }
    }

    private void doConnect() throws Exception {
        MediaLocator locator = getLocator();
        if (locator == null) {
            throw new NullPointerException("No locator set.");
        }
        AudioFormat format = (AudioFormat) getFormat();
        if (format == null) {
            throw new NullPointerException("No format set.");
        }
        WASAPISystem audioSystem = ((DataSource) this.dataSource).audioSystem;
        AudioFormat effectiveFormat = null;
        if (((DataSource) this.dataSource).aec) {
            this.aec = true;
            try {
                CaptureDeviceInfo2 captureDevice = audioSystem.getDevice(DataFlow.CAPTURE, locator);
                if (captureDevice != null) {
                    CaptureDeviceInfo2 renderDevice = audioSystem.getSelectedDevice(DataFlow.PLAYBACK);
                    if (renderDevice != null) {
                        if (1 != null) {
                            effectiveFormat = doConnectInSourceMode(captureDevice, renderDevice, format);
                        } else {
                            effectiveFormat = doConnectInFilterMode(captureDevice, renderDevice, format);
                        }
                        this.sourceMode = true;
                    }
                }
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    ThreadDeath t2 = (ThreadDeath) t;
                }
                logger.error("Failed to enable acoustic echo cancellation (AEC). Will try without it.", t);
            }
        }
        if (this.iMediaObject == 0) {
            this.aec = false;
            this.renderDevice = null;
            this.renderDeviceIndex = -1;
            initializeCapture(locator, format);
            effectiveFormat = this.capture.outFormat;
        }
        this.effectiveFormat = effectiveFormat;
        this.format = format;
        boolean disconnect = true;
        try {
            maybeOpenResampler();
            if (this.resampler != null) {
                this.resamplerBuffer = new Buffer();
            }
            if (((DataSource) this.dataSource).aec) {
                audioSystem.addPropertyChangeListener(this.propertyChangeListener);
            } else {
                audioSystem.removePropertyChangeListener(this.propertyChangeListener);
            }
            disconnect = false;
        } finally {
            if (disconnect) {
                disconnect();
            }
        }
    }

    private AudioFormat doConnectInFilterMode(CaptureDeviceInfo2 captureDevice, CaptureDeviceInfo2 renderDevice, AudioFormat outFormat) throws Exception {
        AudioFormat captureFormat = findClosestMatchCaptureSupportedFormat(outFormat);
        if (captureFormat == null) {
            throw new IllegalStateException("Failed to determine an AudioFormat with which to initialize IAudioClient for MediaLocator " + this.locator + " based on AudioFormat " + outFormat);
        }
        MediaLocator renderLocator;
        AudioFormat renderFormat;
        if (renderDevice == null) {
            renderLocator = null;
            renderFormat = captureFormat;
        } else {
            renderLocator = renderDevice.getLocator();
            if (renderLocator == null) {
                throw new IllegalStateException("A CaptureDeviceInfo2 instance which describes a Windows Audio Session API (WASAPI) render endpoint device and which does not have an actual locator/MediaLocator is illegal.");
            }
            renderFormat = findClosestMatch(renderDevice.getFormats(), outFormat, NativelySupportedAudioFormat.class);
            if (renderFormat == null) {
                throw new IllegalStateException("Failed to determine an AudioFormat with which to initialize IAudioClient for MediaLocator " + renderLocator + " based on AudioFormat " + outFormat);
            }
        }
        boolean uninitialize = true;
        initializeCapture(this.locator, captureFormat);
        if (renderLocator != null) {
            try {
                initializeRender(renderLocator, renderFormat);
            } catch (Throwable th) {
                if (uninitialize) {
                    uninitializeCapture();
                }
            }
        }
        AudioFormat aecOutFormat = initializeAEC(false, captureDevice, captureFormat, renderDevice, renderFormat, outFormat);
        uninitialize = false;
        if (null != null) {
            uninitializeRender();
        }
        if (null != null) {
            uninitializeCapture();
        }
        return aecOutFormat;
    }

    private AudioFormat doConnectInSourceMode(CaptureDeviceInfo2 captureDevice, CaptureDeviceInfo2 renderDevice, AudioFormat outFormat) throws Exception {
        return initializeAEC(true, captureDevice, null, renderDevice, null, outFormat);
    }

    /* access modifiers changed from: protected */
    public Format doGetFormat() {
        synchronized (this) {
            if (this.format != null) {
                AudioFormat audioFormat = this.format;
                return audioFormat;
            }
            return super.doGetFormat();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x006b  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x003c A:{SYNTHETIC} */
    private void doRead(javax.media.Buffer r21) throws java.io.IOException {
        /*
        r20 = this;
        r0 = r20;
        r15 = r0.aec;
        if (r15 == 0) goto L_0x0045;
    L_0x0006:
        r0 = r20;
        r3 = r0.bufferMaxLength;
    L_0x000a:
        r15 = 0;
        r0 = r21;
        r6 = org.jitsi.impl.neomedia.codec.AbstractCodec2.validateByteArraySize(r0, r3, r15);
        r8 = 0;
        r15 = 0;
        r0 = r21;
        r0.setLength(r15);
        r15 = 0;
        r0 = r21;
        r0.setOffset(r15);
    L_0x001e:
        monitor-enter(r20);
        r0 = r20;
        r15 = r0.capture;	 Catch:{ all -> 0x0068 }
        if (r15 != 0) goto L_0x002b;
    L_0x0025:
        r0 = r20;
        r15 = r0.sourceMode;	 Catch:{ all -> 0x0068 }
        if (r15 == 0) goto L_0x004a;
    L_0x002b:
        r5 = 1;
    L_0x002c:
        if (r5 == 0) goto L_0x004c;
    L_0x002e:
        r9 = 0;
        r15 = 1;
        r0 = r20;
        r0.captureIsBusy = r15;	 Catch:{ all -> 0x0068 }
        r15 = 1;
        r0 = r20;
        r0.renderIsBusy = r15;	 Catch:{ all -> 0x0068 }
    L_0x0039:
        monitor-exit(r20);	 Catch:{ all -> 0x0068 }
        if (r9 == 0) goto L_0x006b;
    L_0x003c:
        r20.yield();
        r15 = new java.io.IOException;
        r15.<init>(r9);
        throw r15;
    L_0x0045:
        r0 = r20;
        r3 = r0.bufferSize;
        goto L_0x000a;
    L_0x004a:
        r5 = 0;
        goto L_0x002c;
    L_0x004c:
        r15 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0068 }
        r15.<init>();	 Catch:{ all -> 0x0068 }
        r16 = r20.getClass();	 Catch:{ all -> 0x0068 }
        r16 = r16.getName();	 Catch:{ all -> 0x0068 }
        r15 = r15.append(r16);	 Catch:{ all -> 0x0068 }
        r16 = " is disconnected.";
        r15 = r15.append(r16);	 Catch:{ all -> 0x0068 }
        r9 = r15.toString();	 Catch:{ all -> 0x0068 }
        goto L_0x0039;
    L_0x0068:
        r15 = move-exception;
        monitor-exit(r20);	 Catch:{ all -> 0x0068 }
        throw r15;
    L_0x006b:
        r14 = r3 - r8;
        r0 = r20;
        r0 = r0.iMediaObject;	 Catch:{ Throwable -> 0x00e5, all -> 0x00fb }
        r16 = r0;
        r18 = 0;
        r15 = (r16 > r18 ? 1 : (r16 == r18 ? 0 : -1));
        if (r15 == 0) goto L_0x00c5;
    L_0x0079:
        r2 = 1;
    L_0x007a:
        if (r2 == 0) goto L_0x00d9;
    L_0x007c:
        r0 = r20;
        r15 = r0.processedLength;	 Catch:{ Throwable -> 0x00e5, all -> 0x00fb }
        r14 = java.lang.Math.min(r14, r15);	 Catch:{ Throwable -> 0x00e5, all -> 0x00fb }
        if (r14 != 0) goto L_0x00c7;
    L_0x0086:
        r10 = 0;
    L_0x0087:
        r4 = 0;
        monitor-enter(r20);
        r15 = 0;
        r0 = r20;
        r0.captureIsBusy = r15;	 Catch:{ all -> 0x00e2 }
        r15 = 0;
        r0 = r20;
        r0.renderIsBusy = r15;	 Catch:{ all -> 0x00e2 }
        r20.notifyAll();	 Catch:{ all -> 0x00e2 }
        monitor-exit(r20);	 Catch:{ all -> 0x00e2 }
    L_0x0097:
        if (r4 != 0) goto L_0x011a;
    L_0x0099:
        if (r8 != 0) goto L_0x00ab;
    L_0x009b:
        r12 = java.lang.System.nanoTime();
        r15 = 128; // 0x80 float:1.794E-43 double:6.32E-322;
        r0 = r21;
        r0.setFlags(r15);
        r0 = r21;
        r0.setTimeStamp(r12);
    L_0x00ab:
        r8 = r8 + r10;
        if (r8 >= r3) goto L_0x00b0;
    L_0x00ae:
        if (r10 != 0) goto L_0x0115;
    L_0x00b0:
        r0 = r20;
        r15 = r0.effectiveFormat;
        if (r15 == 0) goto L_0x00bf;
    L_0x00b6:
        r0 = r20;
        r15 = r0.effectiveFormat;
        r0 = r21;
        r0.setFormat(r15);
    L_0x00bf:
        r0 = r21;
        r0.setLength(r8);
        return;
    L_0x00c5:
        r2 = 0;
        goto L_0x007a;
    L_0x00c7:
        r0 = r20;
        r15 = r0.processed;	 Catch:{ Throwable -> 0x00e5, all -> 0x00fb }
        r16 = 0;
        r0 = r16;
        java.lang.System.arraycopy(r15, r0, r6, r8, r14);	 Catch:{ Throwable -> 0x00e5, all -> 0x00fb }
        r0 = r20;
        r0.popFromProcessed(r14);	 Catch:{ Throwable -> 0x00e5, all -> 0x00fb }
        r10 = r14;
        goto L_0x0087;
    L_0x00d9:
        r0 = r20;
        r15 = r0.capture;	 Catch:{ Throwable -> 0x00e5, all -> 0x00fb }
        r10 = r15.read(r6, r8, r14);	 Catch:{ Throwable -> 0x00e5, all -> 0x00fb }
        goto L_0x0087;
    L_0x00e2:
        r15 = move-exception;
        monitor-exit(r20);	 Catch:{ all -> 0x00e2 }
        throw r15;
    L_0x00e5:
        r11 = move-exception;
        r10 = 0;
        r4 = r11;
        monitor-enter(r20);
        r15 = 0;
        r0 = r20;
        r0.captureIsBusy = r15;	 Catch:{ all -> 0x00f8 }
        r15 = 0;
        r0 = r20;
        r0.renderIsBusy = r15;	 Catch:{ all -> 0x00f8 }
        r20.notifyAll();	 Catch:{ all -> 0x00f8 }
        monitor-exit(r20);	 Catch:{ all -> 0x00f8 }
        goto L_0x0097;
    L_0x00f8:
        r15 = move-exception;
        monitor-exit(r20);	 Catch:{ all -> 0x00f8 }
        throw r15;
    L_0x00fb:
        r15 = move-exception;
        monitor-enter(r20);
        r16 = 0;
        r0 = r16;
        r1 = r20;
        r1.captureIsBusy = r0;	 Catch:{ all -> 0x0112 }
        r16 = 0;
        r0 = r16;
        r1 = r20;
        r1.renderIsBusy = r0;	 Catch:{ all -> 0x0112 }
        r20.notifyAll();	 Catch:{ all -> 0x0112 }
        monitor-exit(r20);	 Catch:{ all -> 0x0112 }
        throw r15;
    L_0x0112:
        r15 = move-exception;
        monitor-exit(r20);	 Catch:{ all -> 0x0112 }
        throw r15;
    L_0x0115:
        r20.yield();
        goto L_0x001e;
    L_0x011a:
        r15 = r4 instanceof java.lang.ThreadDeath;
        if (r15 == 0) goto L_0x0121;
    L_0x011e:
        r4 = (java.lang.ThreadDeath) r4;
        throw r4;
    L_0x0121:
        r15 = r4 instanceof java.io.IOException;
        if (r15 == 0) goto L_0x0128;
    L_0x0125:
        r4 = (java.io.IOException) r4;
        throw r4;
    L_0x0128:
        r7 = new java.io.IOException;
        r7.<init>();
        r7.initCause(r4);
        throw r7;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPIStream.doRead(javax.media.Buffer):void");
    }

    private AudioFormat findClosestMatchCaptureSupportedFormat(AudioFormat format) {
        return findClosestMatch(((DataSource) this.dataSource).getIAudioClientSupportedFormats(), format, NativelySupportedAudioFormat.class);
    }

    public Format getFormat() {
        synchronized (this) {
            if (this.format != null) {
                AudioFormat audioFormat = this.format;
                return audioFormat;
            }
            return super.getFormat();
        }
    }

    private MediaLocator getLocator() {
        return this.locator;
    }

    private AudioFormat initializeAEC(boolean sourceMode, CaptureDeviceInfo2 captureDevice, AudioFormat captureFormat, CaptureDeviceInfo2 renderDevice, AudioFormat renderFormat, AudioFormat outFormat) throws Exception {
        WASAPISystem audioSystem = ((DataSource) this.dataSource).audioSystem;
        AudioFormat aecOutFormat = findClosestMatch(audioSystem.getAECSupportedFormats(), outFormat, null);
        if (aecOutFormat == null) {
            throw new IllegalStateException("Failed to determine an AudioFormat with which to initialize Voice Capture DSP/acoustic echo cancellation (AEC) based on AudioFormat " + outFormat);
        }
        long iMediaObject = audioSystem.initializeAEC();
        if (iMediaObject == 0) {
            throw new ResourceUnavailableException("Failed to initialize a Voice Capture DSP for the purposes of acoustic echo cancellation (AEC).");
        }
        long iPropertyStore;
        long iMediaBuffer;
        long dmoOutputDataBuffer;
        try {
            iPropertyStore = VoiceCaptureDSP.IMediaObject_QueryInterface(iMediaObject, VoiceCaptureDSP.IID_IPropertyStore);
            if (iPropertyStore == 0) {
                throw new RuntimeException("IMediaObject_QueryInterface IID_IPropertyStore");
            }
            int hresult = VoiceCaptureDSP.IPropertyStore_SetValue(iPropertyStore, VoiceCaptureDSP.MFPKEY_WMAAECMA_DMO_SOURCE_MODE, sourceMode);
            if (WASAPI.FAILED(hresult)) {
                throw new HResultException(hresult, "IPropertyStore_SetValue MFPKEY_WMAAECMA_DMO_SOURCE_MODE");
            }
            configureAEC(iPropertyStore);
            hresult = IMediaObject_SetXXXputType(iMediaObject, false, 0, aecOutFormat, 0);
            if (WASAPI.FAILED(hresult)) {
                throw new HResultException(hresult, "IMediaObject_SetOutputType, " + aecOutFormat);
            }
            iMediaBuffer = VoiceCaptureDSP.MediaBuffer_alloc((WASAPISystem.getSampleSizeInBytes(aecOutFormat) * aecOutFormat.getChannels()) * ((int) ((20 * ((long) ((int) aecOutFormat.getSampleRate()))) / 1000)));
            if (iMediaBuffer == 0) {
                throw new OutOfMemoryError("MediaBuffer_alloc");
            }
            dmoOutputDataBuffer = VoiceCaptureDSP.DMO_OUTPUT_DATA_BUFFER_alloc(iMediaBuffer, 0, 0, 0);
            if (dmoOutputDataBuffer == 0) {
                throw new OutOfMemoryError("DMO_OUTPUT_DATA_BUFFER_alloc");
            }
            this.bufferMaxLength = VoiceCaptureDSP.IMediaBuffer_GetMaxLength(iMediaBuffer);
            this.processed = new byte[(this.bufferMaxLength * 3)];
            this.processedLength = 0;
            this.renderDevice = renderDevice == null ? null : renderDevice.getLocator();
            this.renderDeviceIndex = -1;
            if (sourceMode) {
                initializeAECInSourceMode(iPropertyStore, captureDevice, renderDevice);
            } else {
                initializeAECInFilterMode(iMediaObject, captureFormat, renderFormat);
            }
            this.dmoOutputDataBuffer = dmoOutputDataBuffer;
            this.iMediaBuffer = iMediaBuffer;
            iMediaBuffer = 0;
            this.iMediaObject = iMediaObject;
            iMediaObject = 0;
            if (0 != 0) {
                WASAPI.CoTaskMemFree(0);
            }
            if (0 != 0) {
                VoiceCaptureDSP.IMediaBuffer_Release(0);
            }
            if (iPropertyStore != 0) {
                WASAPI.IPropertyStore_Release(iPropertyStore);
            }
            if (0 != 0) {
                VoiceCaptureDSP.IMediaObject_Release(0);
            }
            return aecOutFormat;
        } catch (Throwable th) {
            if (iMediaObject != 0) {
                VoiceCaptureDSP.IMediaObject_Release(iMediaObject);
            }
        }
    }

    private void initializeAECInFilterMode(long iMediaObject, AudioFormat inFormat0, AudioFormat inFormat1) throws Exception {
        int hresult = IMediaObject_SetXXXputType(iMediaObject, true, 0, inFormat0, 0);
        if (WASAPI.FAILED(hresult)) {
            throw new HResultException(hresult, "IMediaObject_SetInputType, dwInputStreamIndex " + 0 + ", " + inFormat0);
        }
        hresult = IMediaObject_SetXXXputType(iMediaObject, true, 1, inFormat1, 0);
        if (WASAPI.FAILED(hresult)) {
            throw new HResultException(hresult, "IMediaObject_SetInputType, dwInputStreamIndex " + 1 + ", " + inFormat1);
        }
        long captureIMediaBuffer = VoiceCaptureDSP.MediaBuffer_alloc(this.capture.bufferSize);
        if (captureIMediaBuffer == 0) {
            throw new OutOfMemoryError("MediaBuffer_alloc");
        }
        long renderIMediaBuffer;
        try {
            renderIMediaBuffer = VoiceCaptureDSP.MediaBuffer_alloc((this.render == null ? this.capture : this.render).bufferSize);
            if (renderIMediaBuffer == 0) {
                throw new OutOfMemoryError("MediaBuffer_alloc");
            }
            AudioCaptureClient audioCaptureClient;
            this.captureBufferMaxLength = VoiceCaptureDSP.IMediaBuffer_GetMaxLength(captureIMediaBuffer);
            this.renderBufferMaxLength = VoiceCaptureDSP.IMediaBuffer_GetMaxLength(renderIMediaBuffer);
            this.captureIMediaBuffer = new PtrMediaBuffer(captureIMediaBuffer);
            captureIMediaBuffer = 0;
            this.renderIMediaBuffer = new PtrMediaBuffer(renderIMediaBuffer);
            renderIMediaBuffer = 0;
            AudioFormat af = this.capture.outFormat;
            this.captureNanosPerByte = 8.0E9d / ((((double) af.getSampleSizeInBits()) * af.getSampleRate()) * ((double) af.getChannels()));
            if (this.render == null) {
                audioCaptureClient = this.capture;
            } else {
                audioCaptureClient = this.render;
            }
            af = audioCaptureClient.outFormat;
            this.renderBytesPerNano = ((((double) af.getSampleSizeInBits()) * af.getSampleRate()) * ((double) af.getChannels())) / 8.0E9d;
            if (0 != 0) {
                VoiceCaptureDSP.IMediaBuffer_Release(0);
            }
            if (0 != 0) {
                VoiceCaptureDSP.IMediaBuffer_Release(0);
            }
        } catch (Throwable th) {
            if (captureIMediaBuffer != 0) {
                VoiceCaptureDSP.IMediaBuffer_Release(captureIMediaBuffer);
            }
        }
    }

    private void initializeAECInSourceMode(long iPropertyStore, CaptureDeviceInfo2 captureDevice, CaptureDeviceInfo2 renderDevice) throws Exception {
        WASAPISystem audioSystem = ((DataSource) this.dataSource).audioSystem;
        int captureDeviceIndex = audioSystem.getIMMDeviceIndex(captureDevice.getLocator().getRemainder(), 1);
        if (captureDeviceIndex == -1) {
            throw new IllegalStateException("Acoustic echo cancellation (AEC) cannot be initialized without a microphone.");
        }
        MediaLocator renderLocator = renderDevice.getLocator();
        int renderDeviceIndex = audioSystem.getIMMDeviceIndex(renderLocator.getRemainder(), 0);
        if (renderDeviceIndex == -1) {
            throw new IllegalStateException("Acoustic echo cancellation (AEC) cannot be initialized without a speaker (line).");
        }
        int hresult = VoiceCaptureDSP.IPropertyStore_SetValue(iPropertyStore, VoiceCaptureDSP.MFPKEY_WMAAECMA_DEVICE_INDEXES, (65535 & captureDeviceIndex) | ((65535 & renderDeviceIndex) << 16));
        if (WASAPI.FAILED(hresult)) {
            throw new HResultException(hresult, "IPropertyStore_SetValue MFPKEY_WMAAECMA_DEVICE_INDEXES");
        }
        AbstractAudioRenderer<?> renderer = new WASAPIRenderer();
        renderer.setLocator(renderLocator);
        Format[] rendererSupportedInputFormats = renderer.getSupportedInputFormats();
        if (!(rendererSupportedInputFormats == null || rendererSupportedInputFormats.length == 0)) {
            renderer.setInputFormat(rendererSupportedInputFormats[0]);
        }
        renderer.open();
        this.devicePeriod = 5;
        this.renderDeviceIndex = renderDeviceIndex;
        this.renderer = renderer;
    }

    private void initializeCapture(MediaLocator locator, AudioFormat format) throws Exception {
        long hnsBufferDuration = this.aec ? -1 : 20;
        AudioFormat captureFormat = findClosestMatchCaptureSupportedFormat(format);
        if (captureFormat == null) {
            throw new IllegalStateException("Failed to determine an AudioFormat with which to initialize IAudioClient for MediaLocator " + locator + " based on AudioFormat " + format);
        }
        this.capture = new AudioCaptureClient(((DataSource) this.dataSource).audioSystem, locator, DataFlow.CAPTURE, 0, hnsBufferDuration, captureFormat, new BufferTransferHandler() {
            public void transferData(PushBufferStream stream) {
                WASAPIStream.this.transferCaptureData();
            }
        });
        this.bufferSize = this.capture.bufferSize;
        this.devicePeriod = this.capture.devicePeriod;
    }

    private void initializeRender(MediaLocator locator, AudioFormat format) throws Exception {
        MediaLocator mediaLocator = locator;
        this.render = new AudioCaptureClient(((DataSource) this.dataSource).audioSystem, mediaLocator, DataFlow.PLAYBACK, 131072, 20, format, new BufferTransferHandler() {
            public void transferData(PushBufferStream stream) {
                WASAPIStream.this.transferRenderData();
            }
        });
        this.replenishRender = true;
    }

    private void maybeCloseResampler() {
        Codec resampler = this.resampler;
        if (resampler != null) {
            this.resampler = null;
            this.resamplerBuffer = null;
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

    private void maybeOpenResampler() {
        AudioFormat inFormat = this.effectiveFormat;
        AudioFormat outFormat = this.format;
        if (inFormat.getSampleRate() != outFormat.getSampleRate() || inFormat.getSampleSizeInBits() != outFormat.getSampleSizeInBits()) {
            Codec resampler = WASAPIRenderer.maybeOpenResampler(inFormat, outFormat);
            if (resampler == null) {
                throw new IllegalStateException("Failed to open a codec to resample [" + inFormat + "] into [" + outFormat + "].");
            }
            this.resampler = resampler;
        }
    }

    private void popFromProcessed(int length) {
        this.processedLength = WASAPIRenderer.pop(this.processed, this.processedLength, length);
    }

    private void processInput(int dwInputStreamIndex, int maxLength) {
        PtrMediaBuffer oBuffer;
        int bufferMaxLength;
        AudioCaptureClient audioCaptureClient;
        int dwFlags;
        int hresult;
        switch (dwInputStreamIndex) {
            case 0:
                oBuffer = this.captureIMediaBuffer;
                bufferMaxLength = this.captureBufferMaxLength;
                audioCaptureClient = this.capture;
                break;
            case 1:
                oBuffer = this.renderIMediaBuffer;
                bufferMaxLength = this.renderBufferMaxLength;
                audioCaptureClient = this.render;
                break;
            default:
                throw new IllegalArgumentException("dwInputStreamIndex");
        }
        if (maxLength < 0 || maxLength > bufferMaxLength) {
            maxLength = bufferMaxLength;
        }
        long pBuffer = oBuffer.ptr;
        try {
            dwFlags = VoiceCaptureDSP.IMediaObject_GetInputStatus(this.iMediaObject, dwInputStreamIndex);
        } catch (HResultException hre) {
            dwFlags = 0;
            hresult = hre.getHResult();
            logger.error("IMediaObject_GetInputStatus", hre);
        }
        if ((dwFlags & 1) == 1) {
            int toRead = -1;
            if (dwInputStreamIndex == 1) {
                if (audioCaptureClient == null) {
                    toRead = 0;
                } else if (this.replenishRender) {
                    if (audioCaptureClient.getAvailableLength() < (this.renderBufferMaxLength * 3) / 2) {
                        toRead = 0;
                    } else {
                        this.replenishRender = false;
                    }
                }
            }
            if (toRead == -1) {
                try {
                    toRead = maxLength - VoiceCaptureDSP.IMediaBuffer_GetLength(pBuffer);
                } catch (HResultException hre2) {
                    hresult = hre2.getHResult();
                    toRead = 0;
                    logger.error("IMediaBuffer_GetLength", hre2);
                }
            }
            if (toRead > 0) {
                try {
                    int read = audioCaptureClient.read(oBuffer, toRead);
                    if (dwInputStreamIndex == 1 && read == 0) {
                        this.replenishRender = true;
                    }
                } catch (IOException ioe) {
                    logger.error("Failed to read from IAudioCaptureClient.", ioe);
                }
            }
            if (dwInputStreamIndex == 1) {
                int length;
                try {
                    length = VoiceCaptureDSP.IMediaBuffer_GetLength(pBuffer);
                } catch (HResultException hre22) {
                    hresult = hre22.getHResult();
                    length = 0;
                    logger.error("IMediaBuffer_GetLength", hre22);
                }
                int silence = maxLength - length;
                if (silence > 0) {
                    if (this.processInputBuffer == null || this.processInputBuffer.length < silence) {
                        this.processInputBuffer = new byte[silence];
                    }
                    Arrays.fill(this.processInputBuffer, 0, silence, (byte) 0);
                    maybeMediaBufferPush(pBuffer, this.processInputBuffer, 0, silence);
                }
            }
            try {
                hresult = VoiceCaptureDSP.IMediaObject_ProcessInput(this.iMediaObject, dwInputStreamIndex, pBuffer, 0, 0, 0);
            } catch (HResultException hre222) {
                if (hre222.getHResult() != -2147220988) {
                    logger.error("IMediaObject_ProcessInput", hre222);
                }
            }
        }
    }

    private void processOutput() {
        int dwStatus;
        do {
            try {
                VoiceCaptureDSP.IMediaObject_ProcessOutput(this.iMediaObject, 0, 1, this.dmoOutputDataBuffer);
                dwStatus = VoiceCaptureDSP.DMO_OUTPUT_DATA_BUFFER_getDwStatus(this.dmoOutputDataBuffer);
            } catch (HResultException hre) {
                dwStatus = 0;
                logger.error("IMediaObject_ProcessOutput", hre);
            }
            try {
                int toRead = VoiceCaptureDSP.IMediaBuffer_GetLength(this.iMediaBuffer);
                if (toRead > 0) {
                    int toPop = toRead - (this.processed.length - this.processedLength);
                    if (toPop > 0) {
                        popFromProcessed(toPop);
                    }
                    int read = VoiceCaptureDSP.MediaBuffer_pop(this.iMediaBuffer, this.processed, this.processedLength, toRead);
                    if (read > 0) {
                        this.processedLength += read;
                    }
                }
            } catch (HResultException hre2) {
                logger.error("Failed to read from acoustic echo cancellation (AEC) output IMediaBuffer.", hre2);
                return;
            }
        } while ((dwStatus & 16777216) == 16777216);
    }

    /* access modifiers changed from: private|declared_synchronized */
    public synchronized void propertyChange(PropertyChangeEvent ev) throws Exception {
        boolean renderDeviceDidChange;
        String propertyName = ev.getPropertyName();
        if (DeviceSystem.PROP_DEVICES.equals(propertyName)) {
            renderDeviceDidChange = true;
        } else if ("playbackDevice".equals(propertyName)) {
            MediaLocator oldRenderDevice = this.renderDevice;
            WASAPISystem audioSystem = ((DataSource) this.dataSource).audioSystem;
            CaptureDeviceInfo2 newRenderDeviceInfo = audioSystem.getSelectedDevice(DataFlow.PLAYBACK);
            MediaLocator newRenderDevice = newRenderDeviceInfo == null ? null : newRenderDeviceInfo.getLocator();
            if (oldRenderDevice != null ? oldRenderDevice.equals(newRenderDevice) : newRenderDevice == null) {
                renderDeviceDidChange = this.renderDeviceIndex != (newRenderDevice == null ? -1 : audioSystem.getIMMDeviceIndex(newRenderDevice.getRemainder(), 0));
            } else {
                renderDeviceDidChange = true;
            }
        } else {
            renderDeviceDidChange = false;
        }
        if (renderDeviceDidChange) {
            waitWhileCaptureIsBusy();
            waitWhileRenderIsBusy();
            boolean connected = (this.capture == null && this.iMediaObject == 0) ? false : true;
            if (connected) {
                boolean started = this.started;
                disconnect();
                connect();
                if (started) {
                    start();
                }
            }
        }
    }

    public void read(Buffer buffer) throws IOException {
        Codec resampler = this.resampler;
        if (resampler == null) {
            doRead(buffer);
            return;
        }
        Buffer resamplerBuffer = this.resamplerBuffer;
        doRead(resamplerBuffer);
        if (resampler.process(resamplerBuffer, buffer) == 1) {
            throw new IOException("Failed to resample from [" + this.effectiveFormat + "] into [" + this.format + "].");
        }
    }

    private BufferTransferHandler runInProcessThread() {
        int captureMaxLength = this.captureBufferMaxLength;
        boolean flush;
        do {
            if (this.sourceMode) {
                flush = false;
            } else {
                processInput(0, captureMaxLength);
                int captureLength = maybeIMediaBufferGetLength(this.captureIMediaBuffer);
                if (captureLength < captureMaxLength) {
                    flush = false;
                } else {
                    processInput(1, computeRenderLength(computeCaptureDuration(captureLength)));
                    flush = true;
                }
            }
            processOutput();
            try {
                if (WASAPI.SUCCEEDED(VoiceCaptureDSP.IMediaObject_Flush(this.iMediaObject)) && flush) {
                    this.captureIMediaBuffer.SetLength(0);
                    this.renderIMediaBuffer.SetLength(0);
                    continue;
                }
            } catch (HResultException hre) {
                logger.error("IMediaBuffer_Flush", hre);
                continue;
            } catch (IOException ioe) {
                logger.error("IMediaBuffer.SetLength", ioe);
                continue;
            }
        } while (flush);
        BufferTransferHandler transferHandler = this.transferHandler;
        return (transferHandler == null || this.processedLength < this.bufferMaxLength) ? null : transferHandler;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Missing block: B:43:?, code skipped:
            r2 = runInProcessThread();
     */
    /* JADX WARNING: Missing block: B:45:?, code skipped:
            monitor-enter(r7);
     */
    /* JADX WARNING: Missing block: B:48:?, code skipped:
            r7.captureIsBusy = false;
            r7.renderIsBusy = false;
            notifyAll();
     */
    /* JADX WARNING: Missing block: B:49:0x0063, code skipped:
            monitor-exit(r7);
     */
    /* JADX WARNING: Missing block: B:50:0x0064, code skipped:
            if (r2 == null) goto L_0x008c;
     */
    /* JADX WARNING: Missing block: B:52:?, code skipped:
            r2.transferData(r7);
     */
    /* JADX WARNING: Missing block: B:79:0x008c, code skipped:
            yield();
     */
    public void runInProcessThread(java.lang.Thread r8) {
        /*
        r7 = this;
        r4 = 1;
        r3 = 0;
        org.jitsi.impl.neomedia.jmfext.media.renderer.audio.AbstractAudioRenderer.useAudioThreadPriority();	 Catch:{ all -> 0x0034 }
    L_0x0005:
        monitor-enter(r7);	 Catch:{ all -> 0x0034 }
        r5 = r7.processThread;	 Catch:{ all -> 0x0031 }
        r5 = r8.equals(r5);	 Catch:{ all -> 0x0031 }
        if (r5 != 0) goto L_0x0020;
    L_0x000e:
        monitor-exit(r7);	 Catch:{ all -> 0x0031 }
    L_0x000f:
        monitor-enter(r7);
        r3 = r7.processThread;	 Catch:{ all -> 0x0091 }
        r3 = r8.equals(r3);	 Catch:{ all -> 0x0091 }
        if (r3 == 0) goto L_0x001e;
    L_0x0018:
        r3 = 0;
        r7.processThread = r3;	 Catch:{ all -> 0x0091 }
        r7.notifyAll();	 Catch:{ all -> 0x0091 }
    L_0x001e:
        monitor-exit(r7);	 Catch:{ all -> 0x0091 }
        return;
    L_0x0020:
        r5 = r7.capture;	 Catch:{ all -> 0x0031 }
        if (r5 != 0) goto L_0x0028;
    L_0x0024:
        r5 = r7.sourceMode;	 Catch:{ all -> 0x0031 }
        if (r5 == 0) goto L_0x0046;
    L_0x0028:
        r0 = r4;
    L_0x0029:
        if (r0 == 0) goto L_0x002f;
    L_0x002b:
        r5 = r7.started;	 Catch:{ all -> 0x0031 }
        if (r5 != 0) goto L_0x0048;
    L_0x002f:
        monitor-exit(r7);	 Catch:{ all -> 0x0031 }
        goto L_0x000f;
    L_0x0031:
        r3 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x0031 }
        throw r3;	 Catch:{ all -> 0x0034 }
    L_0x0034:
        r3 = move-exception;
        monitor-enter(r7);
        r4 = r7.processThread;	 Catch:{ all -> 0x0094 }
        r4 = r8.equals(r4);	 Catch:{ all -> 0x0094 }
        if (r4 == 0) goto L_0x0044;
    L_0x003e:
        r4 = 0;
        r7.processThread = r4;	 Catch:{ all -> 0x0094 }
        r7.notifyAll();	 Catch:{ all -> 0x0094 }
    L_0x0044:
        monitor-exit(r7);	 Catch:{ all -> 0x0094 }
        throw r3;
    L_0x0046:
        r0 = r3;
        goto L_0x0029;
    L_0x0048:
        r7.waitWhileCaptureIsBusy();	 Catch:{ all -> 0x0031 }
        r7.waitWhileRenderIsBusy();	 Catch:{ all -> 0x0031 }
        r5 = 1;
        r7.captureIsBusy = r5;	 Catch:{ all -> 0x0031 }
        r5 = 1;
        r7.renderIsBusy = r5;	 Catch:{ all -> 0x0031 }
        monitor-exit(r7);	 Catch:{ all -> 0x0031 }
        r2 = r7.runInProcessThread();	 Catch:{ all -> 0x0075 }
        monitor-enter(r7);	 Catch:{ all -> 0x0034 }
        r5 = 0;
        r7.captureIsBusy = r5;	 Catch:{ all -> 0x0072 }
        r5 = 0;
        r7.renderIsBusy = r5;	 Catch:{ all -> 0x0072 }
        r7.notifyAll();	 Catch:{ all -> 0x0072 }
        monitor-exit(r7);	 Catch:{ all -> 0x0072 }
        if (r2 == 0) goto L_0x008c;
    L_0x0066:
        r2.transferData(r7);	 Catch:{ Throwable -> 0x006a }
        goto L_0x0005;
    L_0x006a:
        r1 = move-exception;
        r5 = r1 instanceof java.lang.ThreadDeath;	 Catch:{ all -> 0x0034 }
        if (r5 == 0) goto L_0x0085;
    L_0x006f:
        r1 = (java.lang.ThreadDeath) r1;	 Catch:{ all -> 0x0034 }
        throw r1;	 Catch:{ all -> 0x0034 }
    L_0x0072:
        r3 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x0072 }
        throw r3;	 Catch:{ all -> 0x0034 }
    L_0x0075:
        r3 = move-exception;
        monitor-enter(r7);	 Catch:{ all -> 0x0034 }
        r4 = 0;
        r7.captureIsBusy = r4;	 Catch:{ all -> 0x0082 }
        r4 = 0;
        r7.renderIsBusy = r4;	 Catch:{ all -> 0x0082 }
        r7.notifyAll();	 Catch:{ all -> 0x0082 }
        monitor-exit(r7);	 Catch:{ all -> 0x0082 }
        throw r3;	 Catch:{ all -> 0x0034 }
    L_0x0082:
        r3 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x0082 }
        throw r3;	 Catch:{ all -> 0x0034 }
    L_0x0085:
        r5 = logger;	 Catch:{ all -> 0x0034 }
        r6 = "BufferTransferHandler.transferData";
        r5.error(r6, r1);	 Catch:{ all -> 0x0034 }
    L_0x008c:
        r7.yield();	 Catch:{ all -> 0x0034 }
        goto L_0x0005;
    L_0x0091:
        r3 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x0091 }
        throw r3;
    L_0x0094:
        r3 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x0094 }
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPIStream.runInProcessThread(java.lang.Thread):void");
    }

    /* access modifiers changed from: 0000 */
    public void setLocator(MediaLocator locator) throws IOException {
        if (this.locator != locator) {
            if (this.locator != null) {
                disconnect();
            }
            this.locator = locator;
            if (this.locator != null) {
                connect();
            }
        }
    }

    public synchronized void start() throws IOException {
        if (this.capture != null) {
            waitWhileCaptureIsBusy();
            this.capture.start();
        }
        if (this.render != null) {
            waitWhileRenderIsBusy();
            this.render.start();
        }
        this.started = true;
        if (this.aec && ((this.capture != null || this.sourceMode) && this.processThread == null)) {
            if (this.renderer != null) {
                this.renderer.start();
            }
            this.processThread = new Thread(WASAPIStream.class + ".processThread") {
                public void run() {
                    WASAPIStream.this.runInProcessThread(this);
                }
            };
            this.processThread.setDaemon(true);
            this.processThread.start();
        }
    }

    public synchronized void stop() throws IOException {
        if (this.capture != null) {
            waitWhileCaptureIsBusy();
            this.capture.stop();
        }
        if (this.render != null) {
            waitWhileRenderIsBusy();
            this.render.stop();
            this.replenishRender = true;
        }
        this.started = false;
        waitWhileProcessThread();
        this.processedLength = 0;
        if (this.renderer != null) {
            this.renderer.stop();
        }
    }

    /* access modifiers changed from: private */
    public void transferCaptureData() {
        if (this.aec) {
            synchronized (this) {
                notifyAll();
            }
            return;
        }
        BufferTransferHandler transferHandler = this.transferHandler;
        if (transferHandler != null) {
            transferHandler.transferData(this);
        }
    }

    /* access modifiers changed from: private */
    public void transferRenderData() {
    }

    private void uninitializeAEC() {
        if (this.iMediaObject != 0) {
            VoiceCaptureDSP.IMediaObject_Release(this.iMediaObject);
            this.iMediaObject = 0;
        }
        if (this.dmoOutputDataBuffer != 0) {
            WASAPI.CoTaskMemFree(this.dmoOutputDataBuffer);
            this.dmoOutputDataBuffer = 0;
        }
        if (this.iMediaBuffer != 0) {
            VoiceCaptureDSP.IMediaBuffer_Release(this.iMediaBuffer);
            this.iMediaBuffer = 0;
        }
        if (this.renderIMediaBuffer != null) {
            this.renderIMediaBuffer.Release();
            this.renderIMediaBuffer = null;
        }
        if (this.captureIMediaBuffer != null) {
            this.captureIMediaBuffer.Release();
            this.captureIMediaBuffer = null;
        }
        Renderer renderer = this.renderer;
        this.renderer = null;
        if (renderer != null) {
            renderer.close();
        }
    }

    private void uninitializeCapture() {
        if (this.capture != null) {
            this.capture.close();
            this.capture = null;
        }
    }

    private void uninitializeRender() {
        if (this.render != null) {
            this.render.close();
            this.render = null;
        }
    }

    private synchronized void waitWhileCaptureIsBusy() {
        boolean interrupted = false;
        while (this.captureIsBusy) {
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

    private synchronized void waitWhileProcessThread() {
        while (this.processThread != null) {
            yield();
        }
    }

    private synchronized void waitWhileRenderIsBusy() {
        boolean interrupted = false;
        while (this.renderIsBusy) {
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

    private synchronized void yield() {
        boolean interrupted = false;
        try {
            wait(this.devicePeriod);
        } catch (InterruptedException e) {
            interrupted = true;
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
