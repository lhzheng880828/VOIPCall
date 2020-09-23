package org.jitsi.impl.neomedia.jmfext.media.protocol.quicktime;

import java.io.IOException;
import javax.media.Format;
import javax.media.control.FormatControl;
import javax.media.control.FrameRateControl;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.format.YUVFormat;
import javax.media.protocol.BufferTransferHandler;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.NeomediaServiceUtils;
import org.jitsi.impl.neomedia.codec.FFmpeg;
import org.jitsi.impl.neomedia.codec.video.AVFrameFormat;
import org.jitsi.impl.neomedia.codec.video.ByteBuffer;
import org.jitsi.impl.neomedia.jmfext.media.protocol.AbstractPushBufferStream;
import org.jitsi.impl.neomedia.jmfext.media.protocol.ByteBufferPool;
import org.jitsi.impl.neomedia.quicktime.CVImageBuffer;
import org.jitsi.impl.neomedia.quicktime.CVPixelBuffer;
import org.jitsi.impl.neomedia.quicktime.CVPixelBufferAttributeKey;
import org.jitsi.impl.neomedia.quicktime.CVPixelFormatType;
import org.jitsi.impl.neomedia.quicktime.NSDictionary;
import org.jitsi.impl.neomedia.quicktime.NSMutableDictionary;
import org.jitsi.impl.neomedia.quicktime.QTCaptureDecompressedVideoOutput;
import org.jitsi.impl.neomedia.quicktime.QTCaptureDecompressedVideoOutput.Delegate;
import org.jitsi.impl.neomedia.quicktime.QTSampleBuffer;

public class QuickTimeStream extends AbstractPushBufferStream<DataSource> {
    private final boolean automaticallyDropsLateVideoFrames;
    private final ByteBufferPool byteBufferPool = new ByteBufferPool();
    final QTCaptureDecompressedVideoOutput captureOutput = new QTCaptureDecompressedVideoOutput();
    private VideoFormat captureOutputFormat;
    private ByteBuffer data;
    private Format dataFormat;
    private final Object dataSyncRoot = new Object();
    private long dataTimeStamp;
    private Format format;
    private ByteBuffer nextData;
    private Format nextDataFormat;
    private long nextDataTimeStamp;
    private Thread transferDataThread;

    QuickTimeStream(DataSource dataSource, FormatControl formatControl) {
        super(dataSource, formatControl);
        if (formatControl != null) {
            Format format = formatControl.getFormat();
            if (format != null) {
                setCaptureOutputFormat(format);
            }
        }
        this.automaticallyDropsLateVideoFrames = this.captureOutput.setAutomaticallyDropsLateVideoFrames(true);
        this.captureOutput.setDelegate(new Delegate() {
            public void outputVideoFrameWithSampleBuffer(CVImageBuffer videoFrame, QTSampleBuffer sampleBuffer) {
                QuickTimeStream.this.captureOutputDidOutputVideoFrameWithSampleBuffer(QuickTimeStream.this.captureOutput, videoFrame, sampleBuffer);
            }
        });
        FrameRateControl frameRateControl = (FrameRateControl) dataSource.getControl(FrameRateControl.class.getName());
        if (frameRateControl != null) {
            float frameRate = frameRateControl.getFrameRate();
            if (frameRate > 0.0f) {
                setFrameRate(frameRate);
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Missing block: B:33:0x00a4, code skipped:
            if (r1 == false) goto L_?;
     */
    /* JADX WARNING: Missing block: B:34:0x00a6, code skipped:
            r2 = r9.transferHandler;
     */
    /* JADX WARNING: Missing block: B:35:0x00a8, code skipped:
            if (r2 == null) goto L_?;
     */
    /* JADX WARNING: Missing block: B:36:0x00aa, code skipped:
            r2.transferData(r9);
     */
    /* JADX WARNING: Missing block: B:44:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:45:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:46:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:47:?, code skipped:
            return;
     */
    public void captureOutputDidOutputVideoFrameWithSampleBuffer(org.jitsi.impl.neomedia.quicktime.QTCaptureOutput r10, org.jitsi.impl.neomedia.quicktime.CVImageBuffer r11, org.jitsi.impl.neomedia.quicktime.QTSampleBuffer r12) {
        /*
        r9 = this;
        r0 = r11;
        r0 = (org.jitsi.impl.neomedia.quicktime.CVPixelBuffer) r0;
        r3 = r9.getVideoFrameFormat(r0);
        r5 = r9.dataSyncRoot;
        monitor-enter(r5);
        r4 = r9.automaticallyDropsLateVideoFrames;	 Catch:{ all -> 0x00b7 }
        if (r4 != 0) goto L_0x0051;
    L_0x000e:
        r4 = r9.data;	 Catch:{ all -> 0x00b7 }
        if (r4 == 0) goto L_0x0051;
    L_0x0012:
        r4 = r9.nextData;	 Catch:{ all -> 0x00b7 }
        if (r4 == 0) goto L_0x001e;
    L_0x0016:
        r4 = r9.nextData;	 Catch:{ all -> 0x00b7 }
        r4.free();	 Catch:{ all -> 0x00b7 }
        r4 = 0;
        r9.nextData = r4;	 Catch:{ all -> 0x00b7 }
    L_0x001e:
        r4 = r9.byteBufferPool;	 Catch:{ all -> 0x00b7 }
        r6 = r0.getByteCount();	 Catch:{ all -> 0x00b7 }
        r4 = r4.getBuffer(r6);	 Catch:{ all -> 0x00b7 }
        r9.nextData = r4;	 Catch:{ all -> 0x00b7 }
        r4 = r9.nextData;	 Catch:{ all -> 0x00b7 }
        if (r4 == 0) goto L_0x004f;
    L_0x002e:
        r4 = r9.nextData;	 Catch:{ all -> 0x00b7 }
        r6 = r9.nextData;	 Catch:{ all -> 0x00b7 }
        r6 = r6.getPtr();	 Catch:{ all -> 0x00b7 }
        r8 = r9.nextData;	 Catch:{ all -> 0x00b7 }
        r8 = r8.getCapacity();	 Catch:{ all -> 0x00b7 }
        r6 = r0.getBytes(r6, r8);	 Catch:{ all -> 0x00b7 }
        r4.setLength(r6);	 Catch:{ all -> 0x00b7 }
        r6 = java.lang.System.nanoTime();	 Catch:{ all -> 0x00b7 }
        r9.nextDataTimeStamp = r6;	 Catch:{ all -> 0x00b7 }
        r4 = r9.nextDataFormat;	 Catch:{ all -> 0x00b7 }
        if (r4 != 0) goto L_0x004f;
    L_0x004d:
        r9.nextDataFormat = r3;	 Catch:{ all -> 0x00b7 }
    L_0x004f:
        monitor-exit(r5);	 Catch:{ all -> 0x00b7 }
    L_0x0050:
        return;
    L_0x0051:
        r4 = r9.data;	 Catch:{ all -> 0x00b7 }
        if (r4 == 0) goto L_0x005d;
    L_0x0055:
        r4 = r9.data;	 Catch:{ all -> 0x00b7 }
        r4.free();	 Catch:{ all -> 0x00b7 }
        r4 = 0;
        r9.data = r4;	 Catch:{ all -> 0x00b7 }
    L_0x005d:
        r4 = r9.byteBufferPool;	 Catch:{ all -> 0x00b7 }
        r6 = r0.getByteCount();	 Catch:{ all -> 0x00b7 }
        r4 = r4.getBuffer(r6);	 Catch:{ all -> 0x00b7 }
        r9.data = r4;	 Catch:{ all -> 0x00b7 }
        r4 = r9.data;	 Catch:{ all -> 0x00b7 }
        if (r4 == 0) goto L_0x008e;
    L_0x006d:
        r4 = r9.data;	 Catch:{ all -> 0x00b7 }
        r6 = r9.data;	 Catch:{ all -> 0x00b7 }
        r6 = r6.getPtr();	 Catch:{ all -> 0x00b7 }
        r8 = r9.data;	 Catch:{ all -> 0x00b7 }
        r8 = r8.getCapacity();	 Catch:{ all -> 0x00b7 }
        r6 = r0.getBytes(r6, r8);	 Catch:{ all -> 0x00b7 }
        r4.setLength(r6);	 Catch:{ all -> 0x00b7 }
        r6 = java.lang.System.nanoTime();	 Catch:{ all -> 0x00b7 }
        r9.dataTimeStamp = r6;	 Catch:{ all -> 0x00b7 }
        r4 = r9.dataFormat;	 Catch:{ all -> 0x00b7 }
        if (r4 != 0) goto L_0x008e;
    L_0x008c:
        r9.dataFormat = r3;	 Catch:{ all -> 0x00b7 }
    L_0x008e:
        r4 = r9.nextData;	 Catch:{ all -> 0x00b7 }
        if (r4 == 0) goto L_0x009a;
    L_0x0092:
        r4 = r9.nextData;	 Catch:{ all -> 0x00b7 }
        r4.free();	 Catch:{ all -> 0x00b7 }
        r4 = 0;
        r9.nextData = r4;	 Catch:{ all -> 0x00b7 }
    L_0x009a:
        r4 = r9.automaticallyDropsLateVideoFrames;	 Catch:{ all -> 0x00b7 }
        if (r4 == 0) goto L_0x00b0;
    L_0x009e:
        r4 = r9.data;	 Catch:{ all -> 0x00b7 }
        if (r4 == 0) goto L_0x00ae;
    L_0x00a2:
        r1 = 1;
    L_0x00a3:
        monitor-exit(r5);	 Catch:{ all -> 0x00b7 }
        if (r1 == 0) goto L_0x0050;
    L_0x00a6:
        r2 = r9.transferHandler;
        if (r2 == 0) goto L_0x0050;
    L_0x00aa:
        r2.transferData(r9);
        goto L_0x0050;
    L_0x00ae:
        r1 = 0;
        goto L_0x00a3;
    L_0x00b0:
        r1 = 0;
        r4 = r9.dataSyncRoot;	 Catch:{ all -> 0x00b7 }
        r4.notifyAll();	 Catch:{ all -> 0x00b7 }
        goto L_0x00a3;
    L_0x00b7:
        r4 = move-exception;
        monitor-exit(r5);	 Catch:{ all -> 0x00b7 }
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.jmfext.media.protocol.quicktime.QuickTimeStream.captureOutputDidOutputVideoFrameWithSampleBuffer(org.jitsi.impl.neomedia.quicktime.QTCaptureOutput, org.jitsi.impl.neomedia.quicktime.CVImageBuffer, org.jitsi.impl.neomedia.quicktime.QTSampleBuffer):void");
    }

    public void close() {
        super.close();
        this.captureOutput.setDelegate(null);
        this.byteBufferPool.drain();
    }

    /* access modifiers changed from: protected */
    public Format doGetFormat() {
        if (this.format != null) {
            return this.format;
        }
        Format format = getCaptureOutputFormat();
        if (format == null) {
            return super.doGetFormat();
        }
        VideoFormat videoFormat = (VideoFormat) format;
        if (videoFormat.getSize() != null) {
            this.format = format;
            return format;
        }
        Dimension defaultSize = NeomediaServiceUtils.getMediaServiceImpl().getDeviceConfiguration().getVideoSize();
        return videoFormat.intersects(new VideoFormat(null, new Dimension(defaultSize.width, defaultSize.height), -1, null, -1.0f));
    }

    private Format getCaptureOutputFormat() {
        NSDictionary pixelBufferAttributes = this.captureOutput.pixelBufferAttributes();
        if (pixelBufferAttributes != null) {
            int pixelFormatType = pixelBufferAttributes.intForKey(CVPixelBufferAttributeKey.kCVPixelBufferPixelFormatTypeKey);
            int width = pixelBufferAttributes.intForKey(CVPixelBufferAttributeKey.kCVPixelBufferWidthKey);
            int height = pixelBufferAttributes.intForKey(CVPixelBufferAttributeKey.kCVPixelBufferHeightKey);
            switch (pixelFormatType) {
                case 32:
                    if (this.captureOutputFormat instanceof AVFrameFormat) {
                        Dimension dimension = (width == 0 && height == 0) ? null : new Dimension(width, height);
                        return new AVFrameFormat(dimension, -1.0f, 27, 32);
                    }
                    Dimension dimension2 = (width == 0 && height == 0) ? null : new Dimension(width, height);
                    return new RGBFormat(dimension2, -1, Format.byteArray, -1.0f, 32, 2, 3, 4);
                case CVPixelFormatType.kCVPixelFormatType_420YpCbCr8Planar /*2033463856*/:
                    if (width == 0 && height == 0) {
                        if (this.captureOutputFormat instanceof AVFrameFormat) {
                            return new AVFrameFormat(0, CVPixelFormatType.kCVPixelFormatType_420YpCbCr8Planar);
                        }
                        return new YUVFormat(2);
                    } else if (this.captureOutputFormat instanceof AVFrameFormat) {
                        return new AVFrameFormat(new Dimension(width, height), -1.0f, 0, CVPixelFormatType.kCVPixelFormatType_420YpCbCr8Planar);
                    } else {
                        int strideY = width;
                        int strideUV = strideY / 2;
                        int offsetU = strideY * height;
                        return new YUVFormat(new Dimension(width, height), -1, Format.byteArray, -1.0f, 2, strideY, strideUV, 0, offsetU, offsetU + ((strideUV * height) / 2));
                    }
            }
        }
        return null;
    }

    public float getFrameRate() {
        return (float) (1.0d / this.captureOutput.minimumVideoFrameInterval());
    }

    private Format getVideoFrameFormat(CVPixelBuffer videoFrame) {
        Format format = getFormat();
        Dimension size = ((VideoFormat) format).getSize();
        return (size == null || (size.width == 0 && size.height == 0)) ? format.intersects(new VideoFormat(null, new Dimension(videoFrame.getWidth(), videoFrame.getHeight()), -1, null, -1.0f)) : format;
    }

    /* JADX WARNING: Missing block: B:40:?, code skipped:
            return;
     */
    public void read(javax.media.Buffer r11) throws java.io.IOException {
        /*
        r10 = this;
        r7 = r10.dataSyncRoot;
        monitor-enter(r7);
        r6 = r10.data;	 Catch:{ all -> 0x004f }
        if (r6 != 0) goto L_0x000d;
    L_0x0007:
        r6 = 0;
        r11.setLength(r6);	 Catch:{ all -> 0x004f }
        monitor-exit(r7);	 Catch:{ all -> 0x004f }
    L_0x000c:
        return;
    L_0x000d:
        r6 = r10.dataFormat;	 Catch:{ all -> 0x004f }
        if (r6 == 0) goto L_0x0016;
    L_0x0011:
        r6 = r10.dataFormat;	 Catch:{ all -> 0x004f }
        r11.setFormat(r6);	 Catch:{ all -> 0x004f }
    L_0x0016:
        r3 = r11.getFormat();	 Catch:{ all -> 0x004f }
        if (r3 != 0) goto L_0x0025;
    L_0x001c:
        r3 = r10.getFormat();	 Catch:{ all -> 0x004f }
        if (r3 == 0) goto L_0x0025;
    L_0x0022:
        r11.setFormat(r3);	 Catch:{ all -> 0x004f }
    L_0x0025:
        r6 = r3 instanceof org.jitsi.impl.neomedia.codec.video.AVFrameFormat;	 Catch:{ all -> 0x004f }
        if (r6 == 0) goto L_0x0052;
    L_0x0029:
        r6 = r10.data;	 Catch:{ all -> 0x004f }
        r6 = org.jitsi.impl.neomedia.codec.video.AVFrame.read(r11, r3, r6);	 Catch:{ all -> 0x004f }
        if (r6 >= 0) goto L_0x0036;
    L_0x0031:
        r6 = r10.data;	 Catch:{ all -> 0x004f }
        r6.free();	 Catch:{ all -> 0x004f }
    L_0x0036:
        r6 = 0;
        r10.data = r6;	 Catch:{ all -> 0x004f }
    L_0x0039:
        r6 = 32896; // 0x8080 float:4.6097E-41 double:1.6253E-319;
        r11.setFlags(r6);	 Catch:{ all -> 0x004f }
        r8 = r10.dataTimeStamp;	 Catch:{ all -> 0x004f }
        r11.setTimeStamp(r8);	 Catch:{ all -> 0x004f }
        r6 = r10.automaticallyDropsLateVideoFrames;	 Catch:{ all -> 0x004f }
        if (r6 != 0) goto L_0x004d;
    L_0x0048:
        r6 = r10.dataSyncRoot;	 Catch:{ all -> 0x004f }
        r6.notifyAll();	 Catch:{ all -> 0x004f }
    L_0x004d:
        monitor-exit(r7);	 Catch:{ all -> 0x004f }
        goto L_0x000c;
    L_0x004f:
        r6 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x004f }
        throw r6;
    L_0x0052:
        r5 = r11.getData();	 Catch:{ all -> 0x004f }
        r6 = r10.data;	 Catch:{ all -> 0x004f }
        r4 = r6.getLength();	 Catch:{ all -> 0x004f }
        r6 = r5 instanceof byte[];	 Catch:{ all -> 0x004f }
        if (r6 == 0) goto L_0x008b;
    L_0x0060:
        r5 = (byte[]) r5;	 Catch:{ all -> 0x004f }
        r0 = r5;
        r0 = (byte[]) r0;	 Catch:{ all -> 0x004f }
        r2 = r0;
        r6 = r2.length;	 Catch:{ all -> 0x004f }
        if (r6 >= r4) goto L_0x006a;
    L_0x0069:
        r2 = 0;
    L_0x006a:
        if (r2 != 0) goto L_0x0071;
    L_0x006c:
        r2 = new byte[r4];	 Catch:{ all -> 0x004f }
        r11.setData(r2);	 Catch:{ all -> 0x004f }
    L_0x0071:
        r6 = 0;
        r8 = r10.data;	 Catch:{ all -> 0x004f }
        r8 = r8.getPtr();	 Catch:{ all -> 0x004f }
        org.jitsi.impl.neomedia.quicktime.CVPixelBuffer.memcpy(r2, r6, r4, r8);	 Catch:{ all -> 0x004f }
        r6 = r10.data;	 Catch:{ all -> 0x004f }
        r6.free();	 Catch:{ all -> 0x004f }
        r6 = 0;
        r10.data = r6;	 Catch:{ all -> 0x004f }
        r11.setLength(r4);	 Catch:{ all -> 0x004f }
        r6 = 0;
        r11.setOffset(r6);	 Catch:{ all -> 0x004f }
        goto L_0x0039;
    L_0x008b:
        r2 = 0;
        goto L_0x006a;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.jmfext.media.protocol.quicktime.QuickTimeStream.read(javax.media.Buffer):void");
    }

    /* access modifiers changed from: private */
    public void runInTransferDataThread() {
        boolean transferData = false;
        while (Thread.currentThread().equals(this.transferDataThread)) {
            if (transferData) {
                BufferTransferHandler transferHandler = this.transferHandler;
                if (transferHandler != null) {
                    transferHandler.transferData(this);
                }
                synchronized (this.dataSyncRoot) {
                    if (this.data != null) {
                        this.data.free();
                    }
                    this.data = this.nextData;
                    this.dataTimeStamp = this.nextDataTimeStamp;
                    if (this.dataFormat == null) {
                        this.dataFormat = this.nextDataFormat;
                    }
                    this.nextData = null;
                }
            }
            synchronized (this.dataSyncRoot) {
                if (this.data == null) {
                    this.data = this.nextData;
                    this.dataTimeStamp = this.nextDataTimeStamp;
                    if (this.dataFormat == null) {
                        this.dataFormat = this.nextDataFormat;
                    }
                    this.nextData = null;
                }
                if (this.data == null) {
                    boolean interrupted = false;
                    try {
                        this.dataSyncRoot.wait();
                    } catch (InterruptedException e) {
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

    private void setCaptureOutputFormat(Format format) {
        int width;
        int height;
        String encoding;
        VideoFormat videoFormat = (VideoFormat) format;
        Dimension size = videoFormat.getSize();
        if (size == null) {
            Dimension defaultSize = NeomediaServiceUtils.getMediaServiceImpl().getDeviceConfiguration().getVideoSize();
            width = defaultSize.width;
            height = defaultSize.height;
        } else {
            width = size.width;
            height = size.height;
        }
        NSMutableDictionary pixelBufferAttributes = null;
        if (width > 0 && height > 0) {
            if (null == null) {
                pixelBufferAttributes = new NSMutableDictionary();
            }
            pixelBufferAttributes.setIntForKey(width, CVPixelBufferAttributeKey.kCVPixelBufferWidthKey);
            pixelBufferAttributes.setIntForKey(height, CVPixelBufferAttributeKey.kCVPixelBufferHeightKey);
        }
        if (format instanceof AVFrameFormat) {
            switch (((AVFrameFormat) format).getPixFmt()) {
                case 0:
                    encoding = VideoFormat.YUV;
                    break;
                case FFmpeg.PIX_FMT_ARGB /*27*/:
                    encoding = VideoFormat.RGB;
                    break;
                default:
                    encoding = null;
                    break;
            }
        } else if (format.isSameEncoding(VideoFormat.RGB)) {
            encoding = VideoFormat.RGB;
        } else if (format.isSameEncoding(VideoFormat.YUV)) {
            encoding = VideoFormat.YUV;
        } else {
            encoding = null;
        }
        if (VideoFormat.RGB.equalsIgnoreCase(encoding)) {
            if (pixelBufferAttributes == null) {
                pixelBufferAttributes = new NSMutableDictionary();
            }
            pixelBufferAttributes.setIntForKey(32, CVPixelBufferAttributeKey.kCVPixelBufferPixelFormatTypeKey);
        } else if (VideoFormat.YUV.equalsIgnoreCase(encoding)) {
            if (pixelBufferAttributes == null) {
                pixelBufferAttributes = new NSMutableDictionary();
            }
            pixelBufferAttributes.setIntForKey(CVPixelFormatType.kCVPixelFormatType_420YpCbCr8Planar, CVPixelBufferAttributeKey.kCVPixelBufferPixelFormatTypeKey);
        } else {
            throw new IllegalArgumentException("format");
        }
        if (pixelBufferAttributes != null) {
            this.captureOutput.setPixelBufferAttributes(pixelBufferAttributes);
            this.captureOutputFormat = videoFormat;
        }
    }

    public float setFrameRate(float frameRate) {
        this.captureOutput.setMinimumVideoFrameInterval(1.0d / ((double) frameRate));
        return getFrameRate();
    }

    public void start() throws IOException {
        super.start();
        if (!this.automaticallyDropsLateVideoFrames) {
            this.transferDataThread = new Thread(getClass().getSimpleName()) {
                public void run() {
                    QuickTimeStream.this.runInTransferDataThread();
                }
            };
            this.transferDataThread.start();
        }
    }

    public void stop() throws IOException {
        try {
            this.transferDataThread = null;
            synchronized (this.dataSyncRoot) {
                if (this.data != null) {
                    this.data.free();
                    this.data = null;
                }
                this.dataFormat = null;
                if (this.nextData != null) {
                    this.nextData.free();
                    this.nextData = null;
                }
                this.nextDataFormat = null;
                if (!this.automaticallyDropsLateVideoFrames) {
                    this.dataSyncRoot.notifyAll();
                }
            }
        } finally {
            super.stop();
            this.byteBufferPool.drain();
        }
    }
}
