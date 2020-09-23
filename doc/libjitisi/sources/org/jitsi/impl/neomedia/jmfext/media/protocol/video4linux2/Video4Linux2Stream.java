package org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2;

import java.io.IOException;
import javax.media.Format;
import javax.media.control.FormatControl;
import javax.media.format.VideoFormat;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.NeomediaServiceUtils;
import org.jitsi.impl.neomedia.codec.FFmpeg;
import org.jitsi.impl.neomedia.codec.video.AVFrameFormat;
import org.jitsi.impl.neomedia.jmfext.media.protocol.AbstractVideoPullBufferStream;
import org.jitsi.impl.neomedia.jmfext.media.protocol.ByteBufferPool;

public class Video4Linux2Stream extends AbstractVideoPullBufferStream<DataSource> {
    private long avctx = 0;
    private long avframe = 0;
    private final ByteBufferPool byteBufferPool = new ByteBufferPool();
    private int capabilities = 0;
    private int fd = -1;
    private Format format;
    private int[] mmapLengths;
    private long[] mmaps;
    private int nativePixelFormat = 0;
    private int requestbuffersCount = 0;
    private int requestbuffersMemory = 0;
    private boolean startInRead = false;
    private long v4l2_buffer = Video4Linux2.v4l2_buffer_alloc(1);

    public Video4Linux2Stream(DataSource dataSource, FormatControl formatControl) {
        super(dataSource, formatControl);
        if (0 == this.v4l2_buffer) {
            throw new OutOfMemoryError("v4l2_buffer_alloc");
        }
        Video4Linux2.v4l2_buffer_setMemory(this.v4l2_buffer, 1);
    }

    public void close() {
        super.close();
        if (this.v4l2_buffer != 0) {
            Video4Linux2.free(this.v4l2_buffer);
            this.v4l2_buffer = 0;
        }
        this.byteBufferPool.drain();
    }

    /* access modifiers changed from: protected */
    public Format doGetFormat() {
        if (this.format != null) {
            return this.format;
        }
        Format format = getFdFormat();
        if (format == null) {
            return super.doGetFormat();
        }
        if (((VideoFormat) format).getSize() == null) {
            return format;
        }
        this.format = format;
        return format;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x015f A:{REMOVE} */
    public void doRead(javax.media.Buffer r25) throws java.io.IOException {
        /*
        r24 = this;
        r14 = r25.getFormat();
        r4 = r14 instanceof org.jitsi.impl.neomedia.codec.video.AVFrameFormat;
        if (r4 != 0) goto L_0x0009;
    L_0x0008:
        r14 = 0;
    L_0x0009:
        if (r14 != 0) goto L_0x0016;
    L_0x000b:
        r14 = r24.getFormat();
        if (r14 == 0) goto L_0x0016;
    L_0x0011:
        r0 = r25;
        r0.setFormat(r14);
    L_0x0016:
        r0 = r24;
        r4 = r0.startInRead;
        if (r4 == 0) goto L_0x0053;
    L_0x001c:
        r4 = 0;
        r0 = r24;
        r0.startInRead = r4;
        r4 = 1;
        r20 = org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2.Video4Linux2.v4l2_buf_type_alloc(r4);
        r4 = 0;
        r4 = (r4 > r20 ? 1 : (r4 == r20 ? 0 : -1));
        if (r4 != 0) goto L_0x0034;
    L_0x002c:
        r4 = new java.lang.OutOfMemoryError;
        r5 = "v4l2_buf_type_alloc";
        r4.<init>(r5);
        throw r4;
    L_0x0034:
        r0 = r24;
        r4 = r0.fd;	 Catch:{ all -> 0x004b }
        r5 = org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2.Video4Linux2.VIDIOC_STREAMON;	 Catch:{ all -> 0x004b }
        r0 = r20;
        r4 = org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2.Video4Linux2.ioctl(r4, r5, r0);	 Catch:{ all -> 0x004b }
        r5 = -1;
        if (r4 != r5) goto L_0x0050;
    L_0x0043:
        r4 = new java.io.IOException;	 Catch:{ all -> 0x004b }
        r5 = "ioctl: request= VIDIOC_STREAMON";
        r4.<init>(r5);	 Catch:{ all -> 0x004b }
        throw r4;	 Catch:{ all -> 0x004b }
    L_0x004b:
        r4 = move-exception;
        org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2.Video4Linux2.free(r20);
        throw r4;
    L_0x0050:
        org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2.Video4Linux2.free(r20);
    L_0x0053:
        r0 = r24;
        r4 = r0.fd;
        r5 = org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2.Video4Linux2.VIDIOC_DQBUF;
        r0 = r24;
        r6 = r0.v4l2_buffer;
        r4 = org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2.Video4Linux2.ioctl(r4, r5, r6);
        r5 = -1;
        if (r4 != r5) goto L_0x006c;
    L_0x0064:
        r4 = new java.io.IOException;
        r5 = "ioctl: request= VIDIOC_DQBUF";
        r4.<init>(r5);
        throw r4;
    L_0x006c:
        r18 = java.lang.System.nanoTime();
        r0 = r24;
        r4 = r0.v4l2_buffer;	 Catch:{ all -> 0x00cb }
        r15 = org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2.Video4Linux2.v4l2_buffer_getIndex(r4);	 Catch:{ all -> 0x00cb }
        r0 = r24;
        r4 = r0.mmaps;	 Catch:{ all -> 0x00cb }
        r8 = r4[r15];	 Catch:{ all -> 0x00cb }
        r0 = r24;
        r4 = r0.v4l2_buffer;	 Catch:{ all -> 0x00cb }
        r10 = org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2.Video4Linux2.v4l2_buffer_getBytesused(r4);	 Catch:{ all -> 0x00cb }
        r0 = r24;
        r4 = r0.nativePixelFormat;	 Catch:{ all -> 0x00cb }
        r5 = org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2.Video4Linux2.V4L2_PIX_FMT_JPEG;	 Catch:{ all -> 0x00cb }
        if (r4 == r5) goto L_0x0096;
    L_0x008e:
        r0 = r24;
        r4 = r0.nativePixelFormat;	 Catch:{ all -> 0x00cb }
        r5 = org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2.Video4Linux2.V4L2_PIX_FMT_MJPEG;	 Catch:{ all -> 0x00cb }
        if (r4 != r5) goto L_0x013f;
    L_0x0096:
        r0 = r24;
        r4 = r0.avctx;	 Catch:{ all -> 0x00cb }
        r6 = 0;
        r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r4 != 0) goto L_0x00f1;
    L_0x00a0:
        r4 = 8;
        r12 = org.jitsi.impl.neomedia.codec.FFmpeg.avcodec_find_decoder(r4);	 Catch:{ all -> 0x00cb }
        r4 = org.jitsi.impl.neomedia.codec.FFmpeg.avcodec_alloc_context3(r12);	 Catch:{ all -> 0x00cb }
        r0 = r24;
        r0.avctx = r4;	 Catch:{ all -> 0x00cb }
        r0 = r24;
        r4 = r0.avctx;	 Catch:{ all -> 0x00cb }
        r6 = 1;
        org.jitsi.impl.neomedia.codec.FFmpeg.avcodeccontext_set_workaround_bugs(r4, r6);	 Catch:{ all -> 0x00cb }
        r0 = r24;
        r4 = r0.avctx;	 Catch:{ all -> 0x00cb }
        r6 = 0;
        r6 = new java.lang.String[r6];	 Catch:{ all -> 0x00cb }
        r4 = org.jitsi.impl.neomedia.codec.FFmpeg.avcodec_open2(r4, r12, r6);	 Catch:{ all -> 0x00cb }
        if (r4 >= 0) goto L_0x00e9;
    L_0x00c3:
        r4 = new java.lang.RuntimeException;	 Catch:{ all -> 0x00cb }
        r5 = "Could not open codec CODEC_ID_MJPEG";
        r4.<init>(r5);	 Catch:{ all -> 0x00cb }
        throw r4;	 Catch:{ all -> 0x00cb }
    L_0x00cb:
        r4 = move-exception;
        r0 = r24;
        r5 = r0.fd;
        r6 = org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2.Video4Linux2.VIDIOC_QBUF;
        r0 = r24;
        r0 = r0.v4l2_buffer;
        r22 = r0;
        r0 = r22;
        r5 = org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2.Video4Linux2.ioctl(r5, r6, r0);
        r6 = -1;
        if (r5 != r6) goto L_0x015f;
    L_0x00e1:
        r4 = new java.io.IOException;
        r5 = "ioctl: request= VIDIOC_QBUF";
        r4.<init>(r5);
        throw r4;
    L_0x00e9:
        r4 = org.jitsi.impl.neomedia.codec.FFmpeg.avcodec_alloc_frame();	 Catch:{ all -> 0x00cb }
        r0 = r24;
        r0.avframe = r4;	 Catch:{ all -> 0x00cb }
    L_0x00f1:
        r0 = r24;
        r4 = r0.avctx;	 Catch:{ all -> 0x00cb }
        r0 = r24;
        r6 = r0.avframe;	 Catch:{ all -> 0x00cb }
        r4 = org.jitsi.impl.neomedia.codec.FFmpeg.avcodec_decode_video(r4, r6, r8, r10);	 Catch:{ all -> 0x00cb }
        r5 = -1;
        if (r4 == r5) goto L_0x0126;
    L_0x0100:
        r16 = r25.getData();	 Catch:{ all -> 0x00cb }
        r0 = r16;
        r4 = r0 instanceof org.jitsi.impl.neomedia.codec.video.AVFrame;	 Catch:{ all -> 0x00cb }
        if (r4 == 0) goto L_0x0118;
    L_0x010a:
        r16 = (org.jitsi.impl.neomedia.codec.video.AVFrame) r16;	 Catch:{ all -> 0x00cb }
        r4 = r16.getPtr();	 Catch:{ all -> 0x00cb }
        r0 = r24;
        r6 = r0.avframe;	 Catch:{ all -> 0x00cb }
        r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r4 == 0) goto L_0x0126;
    L_0x0118:
        r4 = new org.jitsi.impl.neomedia.codec.video.AVFrame;	 Catch:{ all -> 0x00cb }
        r0 = r24;
        r6 = r0.avframe;	 Catch:{ all -> 0x00cb }
        r4.m2229init(r6);	 Catch:{ all -> 0x00cb }
        r0 = r25;
        r0.setData(r4);	 Catch:{ all -> 0x00cb }
    L_0x0126:
        r0 = r24;
        r4 = r0.fd;
        r5 = org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2.Video4Linux2.VIDIOC_QBUF;
        r0 = r24;
        r6 = r0.v4l2_buffer;
        r4 = org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2.Video4Linux2.ioctl(r4, r5, r6);
        r5 = -1;
        if (r4 != r5) goto L_0x0160;
    L_0x0137:
        r4 = new java.io.IOException;
        r5 = "ioctl: request= VIDIOC_QBUF";
        r4.<init>(r5);
        throw r4;
    L_0x013f:
        r0 = r24;
        r4 = r0.byteBufferPool;	 Catch:{ all -> 0x00cb }
        r11 = r4.getBuffer(r10);	 Catch:{ all -> 0x00cb }
        if (r11 == 0) goto L_0x0126;
    L_0x0149:
        r4 = r11.getPtr();	 Catch:{ all -> 0x00cb }
        org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2.Video4Linux2.memcpy(r4, r8, r10);	 Catch:{ all -> 0x00cb }
        r11.setLength(r10);	 Catch:{ all -> 0x00cb }
        r0 = r25;
        r4 = org.jitsi.impl.neomedia.codec.video.AVFrame.read(r0, r14, r11);	 Catch:{ all -> 0x00cb }
        if (r4 >= 0) goto L_0x0126;
    L_0x015b:
        r11.free();	 Catch:{ all -> 0x00cb }
        goto L_0x0126;
    L_0x015f:
        throw r4;
    L_0x0160:
        r4 = 32896; // 0x8080 float:4.6097E-41 double:1.6253E-319;
        r0 = r25;
        r0.setFlags(r4);
        r0 = r25;
        r1 = r18;
        r0.setTimeStamp(r1);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2.Video4Linux2Stream.doRead(javax.media.Buffer):void");
    }

    private Format getFdFormat() {
        Format format = null;
        if (-1 != this.fd) {
            long v4l2_format = Video4Linux2.v4l2_format_alloc(1);
            if (v4l2_format == 0) {
                throw new OutOfMemoryError("v4l2_format_alloc");
            }
            try {
                if (Video4Linux2.ioctl(this.fd, Video4Linux2.VIDIOC_G_FMT, v4l2_format) != -1) {
                    long fmtPix = Video4Linux2.v4l2_format_getFmtPix(v4l2_format);
                    int pixelformat = Video4Linux2.v4l2_pix_format_getPixelformat(fmtPix);
                    int ffmpegPixFmt = DataSource.getFFmpegPixFmt(pixelformat);
                    if (-1 != ffmpegPixFmt) {
                        format = new AVFrameFormat(new Dimension(Video4Linux2.v4l2_pix_format_getWidth(fmtPix), Video4Linux2.v4l2_pix_format_getHeight(fmtPix)), -1.0f, ffmpegPixFmt, pixelformat);
                    }
                }
                Video4Linux2.free(v4l2_format);
            } catch (Throwable th) {
                Video4Linux2.free(v4l2_format);
            }
        }
        return format;
    }

    private void munmap() {
        try {
            if (this.mmaps != null) {
                for (int i = 0; i < this.mmaps.length; i++) {
                    long mmap = this.mmaps[i];
                    if (mmap != 0) {
                        Video4Linux2.munmap(mmap, this.mmapLengths[i]);
                        this.mmaps[i] = 0;
                        this.mmapLengths[i] = 0;
                    }
                }
            }
            this.mmaps = null;
            this.mmapLengths = null;
        } catch (Throwable th) {
            this.mmaps = null;
            this.mmapLengths = null;
        }
    }

    private void negotiateFdInputMethod() throws IOException {
        long v4l2_capability = Video4Linux2.v4l2_capability_alloc();
        if (0 == v4l2_capability) {
            throw new OutOfMemoryError("v4l2_capability_alloc");
        }
        try {
            if (Video4Linux2.ioctl(this.fd, Video4Linux2.VIDIOC_QUERYCAP, v4l2_capability) == -1) {
                throw new IOException("ioctl: request= VIDIOC_QUERYCAP");
            }
            this.capabilities = Video4Linux2.v4l2_capability_getCapabilities(v4l2_capability);
            if ((this.capabilities & Video4Linux2.V4L2_CAP_STREAMING) != Video4Linux2.V4L2_CAP_STREAMING) {
                throw new IOException("Non-streaming V4L2 device not supported.");
            }
            long v4l2_requestbuffers = Video4Linux2.v4l2_requestbuffers_alloc(1);
            if (0 == v4l2_requestbuffers) {
                throw new OutOfMemoryError("v4l2_requestbuffers_alloc");
            }
            try {
                this.requestbuffersMemory = 1;
                Video4Linux2.v4l2_requestbuffers_setMemory(v4l2_requestbuffers, this.requestbuffersMemory);
                Video4Linux2.v4l2_requestbuffers_setCount(v4l2_requestbuffers, 2);
                if (Video4Linux2.ioctl(this.fd, Video4Linux2.VIDIOC_REQBUFS, v4l2_requestbuffers) == -1) {
                    throw new IOException("ioctl: request= VIDIOC_REQBUFS, memory= " + this.requestbuffersMemory);
                }
                this.requestbuffersCount = Video4Linux2.v4l2_requestbuffers_getCount(v4l2_requestbuffers);
                if (this.requestbuffersCount < 1) {
                    throw new IOException("Insufficient V4L2 device memory.");
                }
                long v4l2_buffer = Video4Linux2.v4l2_buffer_alloc(1);
                if (0 == v4l2_buffer) {
                    throw new OutOfMemoryError("v4l2_buffer_alloc");
                }
                try {
                    Video4Linux2.v4l2_buffer_setMemory(v4l2_buffer, 1);
                    this.mmaps = new long[this.requestbuffersCount];
                    this.mmapLengths = new int[this.requestbuffersCount];
                    for (int i = 0; i < this.requestbuffersCount; i++) {
                        Video4Linux2.v4l2_buffer_setIndex(v4l2_buffer, i);
                        if (Video4Linux2.ioctl(this.fd, Video4Linux2.VIDIOC_QUERYBUF, v4l2_buffer) == -1) {
                            throw new IOException("ioctl: request= VIDIOC_QUERYBUF");
                        }
                        int length = Video4Linux2.v4l2_buffer_getLength(v4l2_buffer);
                        long mmap = Video4Linux2.mmap(0, length, 3, 1, this.fd, Video4Linux2.v4l2_buffer_getMOffset(v4l2_buffer));
                        if (-1 == mmap) {
                            throw new IOException("mmap");
                        }
                        this.mmaps[i] = mmap;
                        this.mmapLengths[i] = length;
                    }
                    if (false) {
                        munmap();
                    }
                    Video4Linux2.free(v4l2_buffer);
                } catch (Throwable th) {
                    Video4Linux2.free(v4l2_buffer);
                }
            } finally {
                Video4Linux2.free(v4l2_requestbuffers);
            }
        } finally {
            Video4Linux2.free(v4l2_capability);
        }
    }

    /* access modifiers changed from: 0000 */
    public void setFd(int fd) throws IOException {
        if (this.fd != fd) {
            if (this.fd != -1) {
                try {
                    stop();
                } catch (IOException e) {
                }
                munmap();
            }
            this.fd = -1;
            this.capabilities = 0;
            this.requestbuffersMemory = 0;
            this.requestbuffersCount = 0;
            if (fd != -1) {
                Format format = getFormat();
                this.fd = fd;
                if (format != null) {
                    setFdFormat(format);
                }
                setFdCropToDefault();
                negotiateFdInputMethod();
            }
        }
    }

    private void setFdCropToDefault() {
    }

    private void setFdFormat(Format format) throws IOException {
        int pixelformat = 0;
        if (format instanceof AVFrameFormat) {
            pixelformat = ((AVFrameFormat) format).getDeviceSystemPixFmt();
            this.nativePixelFormat = pixelformat;
        }
        if (pixelformat == 0) {
            throw new IOException("Unsupported format " + format);
        }
        long v4l2_format = Video4Linux2.v4l2_format_alloc(1);
        if (v4l2_format == 0) {
            throw new OutOfMemoryError("v4l2_format_alloc");
        }
        try {
            if (Video4Linux2.ioctl(this.fd, Video4Linux2.VIDIOC_G_FMT, v4l2_format) == -1) {
                throw new IOException("ioctl: request= VIDIO_G_FMT");
            }
            Dimension size = ((VideoFormat) format).getSize();
            long fmtPix = Video4Linux2.v4l2_format_getFmtPix(v4l2_format);
            int width = Video4Linux2.v4l2_pix_format_getWidth(fmtPix);
            int height = Video4Linux2.v4l2_pix_format_getHeight(fmtPix);
            boolean setFdFormat = false;
            if (size == null) {
                size = NeomediaServiceUtils.getMediaServiceImpl().getDeviceConfiguration().getVideoSize();
            }
            if (!(size == null || (size.width == width && size.height == height))) {
                Video4Linux2.v4l2_pix_format_setWidthAndHeight(fmtPix, size.width, size.height);
                setFdFormat = true;
            }
            if (Video4Linux2.v4l2_pix_format_getPixelformat(v4l2_format) != pixelformat) {
                Video4Linux2.v4l2_pix_format_setPixelformat(fmtPix, pixelformat);
                setFdFormat = true;
            }
            if (setFdFormat) {
                setFdFormat(v4l2_format, fmtPix, size, pixelformat);
            }
            Video4Linux2.free(v4l2_format);
        } catch (Throwable th) {
            Video4Linux2.free(v4l2_format);
        }
    }

    private void setFdFormat(long v4l2_format, long fmtPix, Dimension size, int pixelformat) throws IOException {
        Video4Linux2.v4l2_pix_format_setField(fmtPix, 1);
        Video4Linux2.v4l2_pix_format_setBytesperline(fmtPix, 0);
        if (Video4Linux2.ioctl(this.fd, Video4Linux2.VIDIOC_S_FMT, v4l2_format) == -1) {
            throw new IOException("ioctl: request= VIDIOC_S_FMT" + (size == null ? "" : ", size= " + size.width + "x" + size.height) + ", pixelformat= " + pixelformat);
        } else if (Video4Linux2.v4l2_pix_format_getPixelformat(fmtPix) != pixelformat) {
            throw new IOException("Failed to change the format of the V4L2 device to " + pixelformat);
        }
    }

    public void start() throws IOException {
        super.start();
        long v4l2_buffer = Video4Linux2.v4l2_buffer_alloc(1);
        if (0 == v4l2_buffer) {
            throw new OutOfMemoryError("v4l2_buffer_alloc");
        }
        try {
            Video4Linux2.v4l2_buffer_setMemory(v4l2_buffer, 1);
            for (int i = 0; i < this.requestbuffersCount; i++) {
                Video4Linux2.v4l2_buffer_setIndex(v4l2_buffer, i);
                if (Video4Linux2.ioctl(this.fd, Video4Linux2.VIDIOC_QBUF, v4l2_buffer) == -1) {
                    throw new IOException("ioctl: request= VIDIOC_QBUF, index= " + i);
                }
            }
            this.startInRead = true;
        } finally {
            Video4Linux2.free(v4l2_buffer);
        }
    }

    public void stop() throws IOException {
        long v4l2_buf_type;
        try {
            v4l2_buf_type = Video4Linux2.v4l2_buf_type_alloc(1);
            if (0 == v4l2_buf_type) {
                throw new OutOfMemoryError("v4l2_buf_type_alloc");
            } else if (Video4Linux2.ioctl(this.fd, Video4Linux2.VIDIOC_STREAMOFF, v4l2_buf_type) == -1) {
                throw new IOException("ioctl: request= VIDIOC_STREAMOFF");
            } else {
                Video4Linux2.free(v4l2_buf_type);
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
        } catch (Throwable th) {
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
}
