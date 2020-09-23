package org.jitsi.impl.neomedia.jmfext.media.protocol.directshow;

import java.io.IOException;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.control.FormatControl;
import javax.media.control.FrameRateControl;
import javax.media.protocol.BufferTransferHandler;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.codec.FFmpeg;
import org.jitsi.impl.neomedia.codec.video.AVFrame;
import org.jitsi.impl.neomedia.codec.video.AVFrameFormat;
import org.jitsi.impl.neomedia.codec.video.ByteBuffer;
import org.jitsi.impl.neomedia.jmfext.media.protocol.AbstractPushBufferStream;
import org.jitsi.impl.neomedia.jmfext.media.protocol.ByteBufferPool;
import org.jitsi.impl.neomedia.jmfext.media.protocol.directshow.DSCaptureDevice.ISampleGrabberCB;
import org.jitsi.util.Logger;

public class DirectShowStream extends AbstractPushBufferStream<DataSource> {
    private static final Logger logger = Logger.getLogger(DirectShowStream.class);
    private final boolean automaticallyDropsLateVideoFrames = false;
    private long avctx = 0;
    private long avframe = 0;
    private final ByteBufferPool byteBufferPool = new ByteBufferPool();
    private ByteBuffer data;
    private final Object dataSyncRoot = new Object();
    private long dataTimeStamp;
    private final ISampleGrabberCB delegate = new ISampleGrabberCB() {
        public void SampleCB(long source, long ptr, int length) {
            DirectShowStream.this.SampleCB(source, ptr, length);
        }
    };
    private DSCaptureDevice device;
    private Format format;
    private int nativePixelFormat = 0;
    private ByteBuffer nextData;
    private long nextDataTimeStamp;
    private Thread transferDataThread;

    static boolean isSupportedFormat(Format format) {
        if (format instanceof AVFrameFormat) {
            AVFrameFormat avFrameFormat = (AVFrameFormat) format;
            if (!(((long) avFrameFormat.getDeviceSystemPixFmt()) == -1 || avFrameFormat.getSize() == null)) {
                return true;
            }
        }
        return false;
    }

    DirectShowStream(DataSource dataSource, FormatControl formatControl) {
        super(dataSource, formatControl);
    }

    private void connect() throws IOException {
        if (this.device == null) {
            throw new IOException("device == null");
        }
        this.device.setDelegate(this.delegate);
    }

    private void disconnect() throws IOException {
        try {
            stop();
        } finally {
            if (this.device != null) {
                this.device.setDelegate(null);
            }
        }
    }

    /* access modifiers changed from: protected */
    public Format doGetFormat() {
        return this.format == null ? super.doGetFormat() : this.format;
    }

    /* access modifiers changed from: protected */
    public Format doSetFormat(Format format) {
        if (!isSupportedFormat(format)) {
            return super.doSetFormat(format);
        }
        if (this.device == null) {
            return format;
        }
        try {
            setDeviceFormat(format);
        } catch (IOException ioe) {
            logger.error("Failed to set format on DirectShowStream: " + format, ioe);
        }
        if (format.matches(this.format)) {
            return format;
        }
        return null;
    }

    public void read(Buffer buffer) throws IOException {
        synchronized (this.dataSyncRoot) {
            if (this.data == null) {
                buffer.setLength(0);
                return;
            }
            Format bufferFormat = buffer.getFormat();
            if (bufferFormat == null) {
                bufferFormat = getFormat();
                if (bufferFormat != null) {
                    buffer.setFormat(bufferFormat);
                }
            }
            if (!(bufferFormat instanceof AVFrameFormat)) {
                byte[] bytes;
                Object o = buffer.getData();
                int length = this.data.getLength();
                if (o instanceof byte[]) {
                    bytes = (byte[]) o;
                    if (bytes.length < length) {
                        bytes = null;
                    }
                } else {
                    bytes = null;
                }
                if (bytes == null) {
                    buffer.setData(new byte[length]);
                }
                this.data.free();
                this.data = null;
                buffer.setLength(length);
                buffer.setOffset(0);
            } else if (this.nativePixelFormat == DSFormat.MJPG) {
                if (this.avctx == 0) {
                    long avcodec = FFmpeg.avcodec_find_decoder(8);
                    this.avctx = FFmpeg.avcodec_alloc_context3(avcodec);
                    FFmpeg.avcodeccontext_set_workaround_bugs(this.avctx, 1);
                    if (FFmpeg.avcodec_open2(this.avctx, avcodec, new String[0]) < 0) {
                        throw new RuntimeException("Could not open codec CODEC_ID_MJPEG");
                    }
                    this.avframe = FFmpeg.avcodec_alloc_frame();
                }
                if (FFmpeg.avcodec_decode_video(this.avctx, this.avframe, this.data.getPtr(), this.data.getLength()) != -1) {
                    Object out = buffer.getData();
                    if (!((out instanceof AVFrame) && ((AVFrame) out).getPtr() == this.avframe)) {
                        buffer.setData(new AVFrame(this.avframe));
                    }
                }
                this.data.free();
                this.data = null;
            } else {
                if (AVFrame.read(buffer, bufferFormat, this.data) < 0) {
                    this.data.free();
                }
                this.data = null;
            }
            buffer.setFlags(32896);
            buffer.setTimeStamp(this.dataTimeStamp);
            this.dataSyncRoot.notifyAll();
        }
    }

    /* access modifiers changed from: private */
    public void runInTransferDataThread() {
        boolean transferData = false;
        FrameRateControl frameRateControl = (FrameRateControl) ((DataSource) this.dataSource).getControl(FrameRateControl.class.getName());
        long transferDataTimeStamp = -1;
        while (Thread.currentThread().equals(this.transferDataThread)) {
            boolean interrupted;
            if (transferData) {
                BufferTransferHandler transferHandler = this.transferHandler;
                if (transferHandler != null) {
                    if (frameRateControl != null) {
                        long newTransferDataTimeStamp = System.currentTimeMillis();
                        if (transferDataTimeStamp != -1) {
                            float frameRate = frameRateControl.getFrameRate();
                            if (frameRate > 0.0f) {
                                long minimumVideoFrameInterval = (long) (1000.0f / frameRate);
                                if (minimumVideoFrameInterval > 0) {
                                    long t = newTransferDataTimeStamp - transferDataTimeStamp;
                                    if (t > 0 && t < minimumVideoFrameInterval) {
                                        interrupted = false;
                                        try {
                                            Thread.sleep(minimumVideoFrameInterval - t);
                                        } catch (InterruptedException e) {
                                            interrupted = true;
                                        }
                                        if (interrupted) {
                                            Thread.currentThread().interrupt();
                                        }
                                    }
                                }
                            }
                        }
                        transferDataTimeStamp = newTransferDataTimeStamp;
                    }
                    transferHandler.transferData(this);
                }
                synchronized (this.dataSyncRoot) {
                    if (this.data != null) {
                        this.data.free();
                    }
                    this.data = this.nextData;
                    this.dataTimeStamp = this.nextDataTimeStamp;
                    this.nextData = null;
                }
            }
            synchronized (this.dataSyncRoot) {
                if (this.data == null) {
                    this.data = this.nextData;
                    this.dataTimeStamp = this.nextDataTimeStamp;
                    this.nextData = null;
                }
                if (this.data == null) {
                    interrupted = false;
                    try {
                        this.dataSyncRoot.wait();
                    } catch (InterruptedException e2) {
                        interrupted = true;
                    }
                    if (interrupted) {
                        Thread.currentThread().interrupt();
                    }
                    transferData = this.data != null;
                } else {
                    transferData = true;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Missing block: B:23:0x0085, code skipped:
            if (false == false) goto L_?;
     */
    /* JADX WARNING: Missing block: B:24:0x0087, code skipped:
            r10 = r13.transferHandler;
     */
    /* JADX WARNING: Missing block: B:25:0x0089, code skipped:
            if (r10 == null) goto L_?;
     */
    /* JADX WARNING: Missing block: B:26:0x008b, code skipped:
            r10.transferData(r13);
     */
    /* JADX WARNING: Missing block: B:34:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:35:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:36:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:37:?, code skipped:
            return;
     */
    public void SampleCB(long r14, long r16, int r18) {
        /*
        r13 = this;
        r9 = 0;
        r11 = r13.dataSyncRoot;
        monitor-enter(r11);
        r2 = r13.data;	 Catch:{ all -> 0x008f }
        if (r2 == 0) goto L_0x003e;
    L_0x0008:
        r2 = r13.nextData;	 Catch:{ all -> 0x008f }
        if (r2 == 0) goto L_0x0014;
    L_0x000c:
        r2 = r13.nextData;	 Catch:{ all -> 0x008f }
        r2.free();	 Catch:{ all -> 0x008f }
        r2 = 0;
        r13.nextData = r2;	 Catch:{ all -> 0x008f }
    L_0x0014:
        r2 = r13.byteBufferPool;	 Catch:{ all -> 0x008f }
        r0 = r18;
        r2 = r2.getBuffer(r0);	 Catch:{ all -> 0x008f }
        r13.nextData = r2;	 Catch:{ all -> 0x008f }
        r2 = r13.nextData;	 Catch:{ all -> 0x008f }
        if (r2 == 0) goto L_0x003c;
    L_0x0022:
        r12 = r13.nextData;	 Catch:{ all -> 0x008f }
        r2 = r13.nextData;	 Catch:{ all -> 0x008f }
        r6 = r2.getPtr();	 Catch:{ all -> 0x008f }
        r2 = r14;
        r4 = r16;
        r8 = r18;
        r2 = org.jitsi.impl.neomedia.jmfext.media.protocol.directshow.DSCaptureDevice.samplecopy(r2, r4, r6, r8);	 Catch:{ all -> 0x008f }
        r12.setLength(r2);	 Catch:{ all -> 0x008f }
        r2 = java.lang.System.nanoTime();	 Catch:{ all -> 0x008f }
        r13.nextDataTimeStamp = r2;	 Catch:{ all -> 0x008f }
    L_0x003c:
        monitor-exit(r11);	 Catch:{ all -> 0x008f }
    L_0x003d:
        return;
    L_0x003e:
        r2 = r13.data;	 Catch:{ all -> 0x008f }
        if (r2 == 0) goto L_0x004a;
    L_0x0042:
        r2 = r13.data;	 Catch:{ all -> 0x008f }
        r2.free();	 Catch:{ all -> 0x008f }
        r2 = 0;
        r13.data = r2;	 Catch:{ all -> 0x008f }
    L_0x004a:
        r2 = r13.byteBufferPool;	 Catch:{ all -> 0x008f }
        r0 = r18;
        r2 = r2.getBuffer(r0);	 Catch:{ all -> 0x008f }
        r13.data = r2;	 Catch:{ all -> 0x008f }
        r2 = r13.data;	 Catch:{ all -> 0x008f }
        if (r2 == 0) goto L_0x0072;
    L_0x0058:
        r12 = r13.data;	 Catch:{ all -> 0x008f }
        r2 = r13.data;	 Catch:{ all -> 0x008f }
        r6 = r2.getPtr();	 Catch:{ all -> 0x008f }
        r2 = r14;
        r4 = r16;
        r8 = r18;
        r2 = org.jitsi.impl.neomedia.jmfext.media.protocol.directshow.DSCaptureDevice.samplecopy(r2, r4, r6, r8);	 Catch:{ all -> 0x008f }
        r12.setLength(r2);	 Catch:{ all -> 0x008f }
        r2 = java.lang.System.nanoTime();	 Catch:{ all -> 0x008f }
        r13.dataTimeStamp = r2;	 Catch:{ all -> 0x008f }
    L_0x0072:
        r2 = r13.nextData;	 Catch:{ all -> 0x008f }
        if (r2 == 0) goto L_0x007e;
    L_0x0076:
        r2 = r13.nextData;	 Catch:{ all -> 0x008f }
        r2.free();	 Catch:{ all -> 0x008f }
        r2 = 0;
        r13.nextData = r2;	 Catch:{ all -> 0x008f }
    L_0x007e:
        r9 = 0;
        r2 = r13.dataSyncRoot;	 Catch:{ all -> 0x008f }
        r2.notifyAll();	 Catch:{ all -> 0x008f }
        monitor-exit(r11);	 Catch:{ all -> 0x008f }
        if (r9 == 0) goto L_0x003d;
    L_0x0087:
        r10 = r13.transferHandler;
        if (r10 == 0) goto L_0x003d;
    L_0x008b:
        r10.transferData(r13);
        goto L_0x003d;
    L_0x008f:
        r2 = move-exception;
        monitor-exit(r11);	 Catch:{ all -> 0x008f }
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.jmfext.media.protocol.directshow.DirectShowStream.SampleCB(long, long, int):void");
    }

    /* access modifiers changed from: 0000 */
    public void setDevice(DSCaptureDevice device) throws IOException {
        if (this.device != device) {
            if (this.device != null) {
                disconnect();
            }
            this.device = device;
            if (this.device != null) {
                connect();
            }
        }
    }

    private void setDeviceFormat(Format format) throws IOException {
        if (format == null) {
            throw new IOException("format == null");
        } else if (format instanceof AVFrameFormat) {
            AVFrameFormat avFrameFormat = (AVFrameFormat) format;
            this.nativePixelFormat = avFrameFormat.getDeviceSystemPixFmt();
            Dimension size = avFrameFormat.getSize();
            if (size == null) {
                throw new IOException("format.size == null");
            }
            int hresult = this.device.setFormat(new DSFormat(size.width, size.height, avFrameFormat.getDeviceSystemPixFmt()));
            switch (hresult) {
                case 0:
                case 1:
                    this.format = format;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Set format on DirectShowStream: " + format);
                        return;
                    }
                    return;
                default:
                    throwNewHResultException(hresult);
                    return;
            }
        } else {
            throw new IOException("!(format instanceof AVFrameFormat)");
        }
    }

    public void start() throws IOException {
        super.start();
        boolean started = false;
        try {
            setDeviceFormat(getFormat());
            if (this.transferDataThread == null) {
                this.transferDataThread = new Thread(getClass().getSimpleName()) {
                    public void run() {
                        DirectShowStream.this.runInTransferDataThread();
                    }
                };
                this.transferDataThread.start();
            }
            this.device.start();
            started = true;
        } finally {
            if (!started) {
                stop();
            }
        }
    }

    public void stop() throws IOException {
        try {
            this.device.stop();
            this.transferDataThread = null;
            synchronized (this.dataSyncRoot) {
                if (this.data != null) {
                    this.data.free();
                    this.data = null;
                }
                if (this.nextData != null) {
                    this.nextData.free();
                    this.nextData = null;
                }
                this.dataSyncRoot.notifyAll();
            }
        } finally {
            super.stop();
            if (this.avctx != 0) {
                FFmpeg.avcodec_close(this.avctx);
                FFmpeg.av_free(this.avctx);
                this.avctx = 0;
            }
            if (this.avframe != 0) {
                FFmpeg.avcodec_free_frame(this.avframe);
                this.avframe = 0;
            }
            this.byteBufferPool.drain();
        }
    }

    private void throwNewHResultException(int hresult) throws IOException {
        throw new IOException("HRESULT 0x" + Long.toHexString(((long) hresult) & 4294967295L));
    }
}
