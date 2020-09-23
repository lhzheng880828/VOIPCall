package org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.media.MediaLocator;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import javax.media.protocol.BufferTransferHandler;
import org.jitsi.impl.neomedia.device.AudioSystem.DataFlow;
import org.jitsi.impl.neomedia.device.WASAPISystem;
import org.jitsi.impl.neomedia.jmfext.media.renderer.audio.WASAPIRenderer;
import org.jitsi.util.Logger;

public class AudioCaptureClient {
    private static final Logger logger = Logger.getLogger(AudioCaptureClient.class);
    private byte[] available;
    private int availableLength;
    private int bufferFrames;
    final int bufferSize;
    private boolean busy;
    final long devicePeriod;
    private int dstChannels;
    private int dstFrameSize;
    private int dstSampleSize;
    private long eventHandle;
    private Runnable eventHandleCmd;
    private Executor eventHandleExecutor;
    private long iAudioCaptureClient;
    private long iAudioClient;
    final AudioFormat outFormat;
    private boolean read;
    private int srcChannels;
    private int srcSampleSize;
    private boolean started;
    private final BufferTransferHandler transferHandler;

    private static int maybeIAudioCaptureClientGetNextPacketSize(long iAudioCaptureClient) {
        try {
            return WASAPI.IAudioCaptureClient_GetNextPacketSize(iAudioCaptureClient);
        } catch (HResultException hre) {
            logger.error("IAudioCaptureClient_GetNextPacketSize", hre);
            return 0;
        }
    }

    public AudioCaptureClient(WASAPISystem audioSystem, MediaLocator locator, DataFlow dataFlow, int streamFlags, long hnsBufferDuration, AudioFormat outFormat, BufferTransferHandler transferHandler) throws Exception {
        AudioFormat[] formats = WASAPISystem.getFormatsToInitializeIAudioClient(outFormat);
        long eventHandle = WASAPI.CreateEvent(0, false, false, null);
        if (eventHandle == 0) {
            throw new IOException("CreateEvent");
        }
        long iAudioClient;
        long iAudioCaptureClient;
        try {
            iAudioClient = audioSystem.initializeIAudioClient(locator, dataFlow, streamFlags, eventHandle, hnsBufferDuration, formats);
            if (iAudioClient == 0) {
                throw new ResourceUnavailableException("Failed to initialize IAudioClient for MediaLocator " + locator + " and AudioSystem.DataFlow " + dataFlow);
            }
            AudioFormat inFormat = null;
            for (AudioFormat aFormat : formats) {
                if (aFormat != null) {
                    inFormat = aFormat;
                    break;
                }
            }
            iAudioCaptureClient = WASAPI.IAudioClient_GetService(iAudioClient, WASAPI.IID_IAudioCaptureClient);
            if (iAudioCaptureClient == 0) {
                throw new ResourceUnavailableException("IAudioClient_GetService(IID_IAudioCaptureClient)");
            }
            long devicePeriod = WASAPI.IAudioClient_GetDefaultDevicePeriod(iAudioClient) / 10000;
            int numBufferFrames = WASAPI.IAudioClient_GetBufferSize(iAudioClient);
            int sampleRate = (int) inFormat.getSampleRate();
            long bufferDuration = (long) ((numBufferFrames * 1000) / sampleRate);
            if (devicePeriod <= 1) {
                devicePeriod = bufferDuration / 2;
                if (devicePeriod > 10 || devicePeriod <= 1) {
                    devicePeriod = 10;
                }
            }
            this.devicePeriod = devicePeriod;
            if (hnsBufferDuration == -1) {
                hnsBufferDuration = devicePeriod;
            }
            this.srcChannels = inFormat.getChannels();
            this.srcSampleSize = WASAPISystem.getSampleSizeInBytes(inFormat);
            this.dstChannels = outFormat.getChannels();
            this.dstSampleSize = WASAPISystem.getSampleSizeInBytes(outFormat);
            this.dstFrameSize = this.dstSampleSize * this.dstChannels;
            this.bufferFrames = (int) ((((long) sampleRate) * hnsBufferDuration) / 1000);
            this.bufferSize = this.dstFrameSize * this.bufferFrames;
            this.available = new byte[(this.dstFrameSize * numBufferFrames)];
            this.availableLength = 0;
            this.eventHandle = eventHandle;
            eventHandle = 0;
            this.iAudioClient = iAudioClient;
            iAudioClient = 0;
            this.iAudioCaptureClient = iAudioCaptureClient;
            this.outFormat = outFormat;
            this.transferHandler = transferHandler;
            if (0 != 0) {
                WASAPI.IAudioCaptureClient_Release(0);
            }
            if (0 != 0) {
                WASAPI.IAudioClient_Release(0);
            }
            if (0 != 0) {
                WASAPI.CloseHandle(0);
            }
        } catch (Throwable th) {
            if (eventHandle != 0) {
                WASAPI.CloseHandle(eventHandle);
            }
        }
    }

    public void close() {
        if (this.iAudioCaptureClient != 0) {
            WASAPI.IAudioCaptureClient_Release(this.iAudioCaptureClient);
            this.iAudioCaptureClient = 0;
        }
        if (this.iAudioClient != 0) {
            WASAPI.IAudioClient_Release(this.iAudioClient);
            this.iAudioClient = 0;
        }
        if (this.eventHandle != 0) {
            try {
                WASAPI.CloseHandle(this.eventHandle);
            } catch (HResultException hre) {
                logger.warn("Failed to close event HANDLE.", hre);
            }
            this.eventHandle = 0;
        }
        this.available = null;
        this.availableLength = 0;
        this.started = false;
    }

    private int doRead(IMediaBuffer iMediaBuffer, byte[] buffer, int offset, int length) throws IOException {
        int toRead = Math.min(length, this.availableLength);
        if (toRead == 0) {
            return 0;
        }
        int read;
        if (iMediaBuffer == null) {
            read = toRead;
            System.arraycopy(this.available, 0, buffer, offset, toRead);
        } else {
            read = iMediaBuffer.push(this.available, 0, toRead);
        }
        popFromAvailable(read);
        return read;
    }

    /* access modifiers changed from: 0000 */
    public int getAvailableLength() {
        return this.availableLength;
    }

    private void popFromAvailable(int length) {
        this.availableLength = WASAPIRenderer.pop(this.available, this.availableLength, length);
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
        return read(null, buffer, offset, length);
    }

    private int read(IMediaBuffer iMediaBuffer, byte[] buffer, int offset, int length) throws IOException {
        String message;
        synchronized (this) {
            if (this.iAudioClient == 0 || this.iAudioCaptureClient == 0) {
                message = getClass().getName() + " is disconnected.";
            } else if (this.started) {
                message = null;
                this.busy = true;
            } else {
                message = getClass().getName() + " is stopped.";
            }
        }
        if (message != null) {
            throw new IOException(message);
        }
        int read;
        Throwable cause;
        try {
            read = doRead(iMediaBuffer, buffer, offset, length);
            cause = null;
            if (read > 0) {
                this.read = true;
            }
            synchronized (this) {
                this.busy = false;
                notifyAll();
            }
        } catch (Throwable th) {
            synchronized (this) {
                this.busy = false;
                notifyAll();
            }
        }
        if (cause == null) {
            return read;
        }
        if (cause instanceof ThreadDeath) {
            throw ((ThreadDeath) cause);
        } else if (cause instanceof IOException) {
            throw ((IOException) cause);
        } else {
            IOException ioe = new IOException();
            ioe.initCause(cause);
            throw ioe;
        }
    }

    public int read(IMediaBuffer iMediaBuffer, int length) throws IOException {
        return read(iMediaBuffer, null, 0, length);
    }

    private BufferTransferHandler readInEventHandleCmd() {
        int numFramesInNextPacket = maybeIAudioCaptureClientGetNextPacketSize(this.iAudioCaptureClient);
        if (numFramesInNextPacket != 0) {
            int toRead = numFramesInNextPacket * this.dstFrameSize;
            int toPop = toRead - (this.available.length - this.availableLength);
            if (toPop > 0) {
                popFromAvailable(toPop);
            }
            try {
                this.availableLength += WASAPI.IAudioCaptureClient_Read(this.iAudioCaptureClient, this.available, this.availableLength, toRead, this.srcSampleSize, this.srcChannels, this.dstSampleSize, this.dstChannels);
            } catch (HResultException hre) {
                logger.error("IAudioCaptureClient_Read", hre);
            }
        }
        if (this.availableLength >= this.bufferSize) {
            return this.transferHandler;
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Missing block: B:8:0x000f, code skipped:
            monitor-enter(r12);
     */
    /* JADX WARNING: Missing block: B:11:0x0016, code skipped:
            if (r13.equals(r12.eventHandleCmd) == false) goto L_0x001e;
     */
    /* JADX WARNING: Missing block: B:12:0x0018, code skipped:
            r12.eventHandleCmd = null;
            notifyAll();
     */
    /* JADX WARNING: Missing block: B:13:0x001e, code skipped:
            monitor-exit(r12);
     */
    /* JADX WARNING: Missing block: B:14:0x001f, code skipped:
            return;
     */
    public void runInEventHandleCmd(java.lang.Runnable r13) {
        /*
        r12 = this;
        r10 = 0;
        org.jitsi.impl.neomedia.jmfext.media.renderer.audio.AbstractAudioRenderer.useAudioThreadPriority();	 Catch:{ all -> 0x0035 }
    L_0x0005:
        monitor-enter(r12);	 Catch:{ all -> 0x0035 }
        r7 = r12.eventHandleCmd;	 Catch:{ all -> 0x0032 }
        r7 = r13.equals(r7);	 Catch:{ all -> 0x0032 }
        if (r7 != 0) goto L_0x0020;
    L_0x000e:
        monitor-exit(r12);	 Catch:{ all -> 0x0032 }
    L_0x000f:
        monitor-enter(r12);
        r7 = r12.eventHandleCmd;	 Catch:{ all -> 0x00b7 }
        r7 = r13.equals(r7);	 Catch:{ all -> 0x00b7 }
        if (r7 == 0) goto L_0x001e;
    L_0x0018:
        r7 = 0;
        r12.eventHandleCmd = r7;	 Catch:{ all -> 0x00b7 }
        r12.notifyAll();	 Catch:{ all -> 0x00b7 }
    L_0x001e:
        monitor-exit(r12);	 Catch:{ all -> 0x00b7 }
        return;
    L_0x0020:
        r8 = r12.iAudioClient;	 Catch:{ all -> 0x0032 }
        r7 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));
        if (r7 == 0) goto L_0x0030;
    L_0x0026:
        r8 = r12.iAudioCaptureClient;	 Catch:{ all -> 0x0032 }
        r7 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));
        if (r7 == 0) goto L_0x0030;
    L_0x002c:
        r7 = r12.started;	 Catch:{ all -> 0x0032 }
        if (r7 != 0) goto L_0x0047;
    L_0x0030:
        monitor-exit(r12);	 Catch:{ all -> 0x0032 }
        goto L_0x000f;
    L_0x0032:
        r7 = move-exception;
        monitor-exit(r12);	 Catch:{ all -> 0x0032 }
        throw r7;	 Catch:{ all -> 0x0035 }
    L_0x0035:
        r7 = move-exception;
        monitor-enter(r12);
        r8 = r12.eventHandleCmd;	 Catch:{ all -> 0x00ba }
        r8 = r13.equals(r8);	 Catch:{ all -> 0x00ba }
        if (r8 == 0) goto L_0x0045;
    L_0x003f:
        r8 = 0;
        r12.eventHandleCmd = r8;	 Catch:{ all -> 0x00ba }
        r12.notifyAll();	 Catch:{ all -> 0x00ba }
    L_0x0045:
        monitor-exit(r12);	 Catch:{ all -> 0x00ba }
        throw r7;
    L_0x0047:
        r0 = r12.eventHandle;	 Catch:{ all -> 0x0032 }
        r7 = (r0 > r10 ? 1 : (r0 == r10 ? 0 : -1));
        if (r7 != 0) goto L_0x0055;
    L_0x004d:
        r7 = new java.lang.IllegalStateException;	 Catch:{ all -> 0x0032 }
        r8 = "eventHandle";
        r7.<init>(r8);	 Catch:{ all -> 0x0032 }
        throw r7;	 Catch:{ all -> 0x0032 }
    L_0x0055:
        r12.waitWhileBusy();	 Catch:{ all -> 0x0032 }
        r7 = 1;
        r12.busy = r7;	 Catch:{ all -> 0x0032 }
        monitor-exit(r12);	 Catch:{ all -> 0x0032 }
        r5 = r12.readInEventHandleCmd();	 Catch:{ all -> 0x0090 }
        if (r5 == 0) goto L_0x0065;
    L_0x0062:
        r7 = 0;
        r12.read = r7;	 Catch:{ all -> 0x0090 }
    L_0x0065:
        r8 = r12.iAudioCaptureClient;	 Catch:{ all -> 0x0090 }
        r3 = maybeIAudioCaptureClientGetNextPacketSize(r8);	 Catch:{ all -> 0x0090 }
        monitor-enter(r12);	 Catch:{ all -> 0x0035 }
        r7 = 0;
        r12.busy = r7;	 Catch:{ all -> 0x008d }
        r12.notifyAll();	 Catch:{ all -> 0x008d }
        monitor-exit(r12);	 Catch:{ all -> 0x008d }
        if (r5 == 0) goto L_0x007d;
    L_0x0075:
        r7 = 0;
        r5.transferData(r7);	 Catch:{ Throwable -> 0x009d }
        r7 = r12.read;	 Catch:{ Throwable -> 0x009d }
        if (r7 != 0) goto L_0x0005;
    L_0x007d:
        if (r3 != 0) goto L_0x0005;
    L_0x007f:
        r8 = r12.devicePeriod;	 Catch:{ HResultException -> 0x00ad }
        r6 = org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI.WaitForSingleObject(r0, r8);	 Catch:{ HResultException -> 0x00ad }
    L_0x0085:
        r7 = -1;
        if (r6 == r7) goto L_0x000f;
    L_0x0088:
        r7 = 128; // 0x80 float:1.794E-43 double:6.32E-322;
        if (r6 != r7) goto L_0x0005;
    L_0x008c:
        goto L_0x000f;
    L_0x008d:
        r7 = move-exception;
        monitor-exit(r12);	 Catch:{ all -> 0x008d }
        throw r7;	 Catch:{ all -> 0x0035 }
    L_0x0090:
        r7 = move-exception;
        monitor-enter(r12);	 Catch:{ all -> 0x0035 }
        r8 = 0;
        r12.busy = r8;	 Catch:{ all -> 0x009a }
        r12.notifyAll();	 Catch:{ all -> 0x009a }
        monitor-exit(r12);	 Catch:{ all -> 0x009a }
        throw r7;	 Catch:{ all -> 0x0035 }
    L_0x009a:
        r7 = move-exception;
        monitor-exit(r12);	 Catch:{ all -> 0x009a }
        throw r7;	 Catch:{ all -> 0x0035 }
    L_0x009d:
        r4 = move-exception;
        r7 = r4 instanceof java.lang.ThreadDeath;	 Catch:{ all -> 0x0035 }
        if (r7 == 0) goto L_0x00a5;
    L_0x00a2:
        r4 = (java.lang.ThreadDeath) r4;	 Catch:{ all -> 0x0035 }
        throw r4;	 Catch:{ all -> 0x0035 }
    L_0x00a5:
        r7 = logger;	 Catch:{ all -> 0x0035 }
        r8 = "BufferTransferHandler.transferData";
        r7.error(r8, r4);	 Catch:{ all -> 0x0035 }
        goto L_0x007d;
    L_0x00ad:
        r2 = move-exception;
        r6 = -1;
        r7 = logger;	 Catch:{ all -> 0x0035 }
        r8 = "WaitForSingleObject";
        r7.error(r8, r2);	 Catch:{ all -> 0x0035 }
        goto L_0x0085;
    L_0x00b7:
        r7 = move-exception;
        monitor-exit(r12);	 Catch:{ all -> 0x00b7 }
        throw r7;
    L_0x00ba:
        r7 = move-exception;
        monitor-exit(r12);	 Catch:{ all -> 0x00ba }
        throw r7;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.AudioCaptureClient.runInEventHandleCmd(java.lang.Runnable):void");
    }

    public synchronized void start() throws IOException {
        if (this.iAudioClient != 0) {
            waitWhileBusy();
            waitWhileEventHandleCmd();
            Runnable eventHandleCmd;
            try {
                WASAPI.IAudioClient_Start(this.iAudioClient);
                this.started = true;
                this.availableLength = 0;
                if (this.eventHandle != 0 && this.eventHandleCmd == null) {
                    eventHandleCmd = new Runnable() {
                        public void run() {
                            AudioCaptureClient.this.runInEventHandleCmd(this);
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
                    WASAPIStream.throwNewIOException("IAudioClient_Start", hre);
                }
            } catch (Throwable th) {
                if (!false) {
                    if (eventHandleCmd.equals(this.eventHandleCmd)) {
                        this.eventHandleCmd = null;
                    }
                }
            }
        }
    }

    public synchronized void stop() throws IOException {
        if (this.iAudioClient != 0) {
            waitWhileBusy();
            try {
                WASAPI.IAudioClient_Stop(this.iAudioClient);
                this.started = false;
                waitWhileEventHandleCmd();
                this.availableLength = 0;
            } catch (HResultException hre) {
                WASAPIStream.throwNewIOException("IAudioClient_Stop", hre);
            }
        }
        return;
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
