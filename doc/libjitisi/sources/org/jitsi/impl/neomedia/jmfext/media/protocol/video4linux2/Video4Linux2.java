package org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2;

import com.lti.utils.UnsignedUtils;

public class Video4Linux2 {
    public static final int MAP_SHARED = 1;
    public static final int O_NONBLOCK = 2048;
    public static final int O_RDWR = 2;
    public static final int PROT_READ = 1;
    public static final int PROT_WRITE = 2;
    public static final int V4L2_BUF_TYPE_VIDEO_CAPTURE = 1;
    public static final int V4L2_CAP_STREAMING = 67108864;
    public static final int V4L2_CAP_VIDEO_CAPTURE = 1;
    public static final int V4L2_FIELD_NONE = 1;
    public static final int V4L2_MEMORY_MMAP = 1;
    public static final int V4L2_MEMORY_USERPTR = 2;
    public static final int V4L2_PIX_FMT_BGR24 = v4l2_fourcc('B', 'G', 'R', '3');
    public static final int V4L2_PIX_FMT_JPEG = v4l2_fourcc('J', 'P', 'E', 'G');
    public static final int V4L2_PIX_FMT_MJPEG = v4l2_fourcc('M', 'J', 'P', 'G');
    public static final int V4L2_PIX_FMT_NONE = 0;
    public static final int V4L2_PIX_FMT_RGB24 = v4l2_fourcc('R', 'G', 'B', '3');
    public static final int V4L2_PIX_FMT_UYVY = v4l2_fourcc('U', 'Y', 'V', 'Y');
    public static final int V4L2_PIX_FMT_VYUY = v4l2_fourcc('V', 'Y', 'U', 'Y');
    public static final int V4L2_PIX_FMT_YUV420 = v4l2_fourcc('Y', 'U', '1', '2');
    public static final int V4L2_PIX_FMT_YUYV = v4l2_fourcc('Y', 'U', 'Y', 'V');
    public static final int VIDIOC_DQBUF = VIDIOC_DQBUF();
    public static final int VIDIOC_G_FMT = VIDIOC_G_FMT();
    public static final int VIDIOC_QBUF = VIDIOC_QBUF();
    public static final int VIDIOC_QUERYBUF = VIDIOC_QUERYBUF();
    public static final int VIDIOC_QUERYCAP = VIDIOC_QUERYCAP();
    public static final int VIDIOC_REQBUFS = VIDIOC_REQBUFS();
    public static final int VIDIOC_STREAMOFF = VIDIOC_STREAMOFF();
    public static final int VIDIOC_STREAMON = VIDIOC_STREAMON();
    public static final int VIDIOC_S_FMT = VIDIOC_S_FMT();
    public static final int VIDIOC_S_PARM = VIDIOC_S_PARM();

    private static native int VIDIOC_DQBUF();

    private static native int VIDIOC_G_FMT();

    private static native int VIDIOC_QBUF();

    private static native int VIDIOC_QUERYBUF();

    private static native int VIDIOC_QUERYCAP();

    private static native int VIDIOC_REQBUFS();

    private static native int VIDIOC_STREAMOFF();

    private static native int VIDIOC_STREAMON();

    private static native int VIDIOC_S_FMT();

    private static native int VIDIOC_S_PARM();

    public static native int close(int i);

    public static native void free(long j);

    public static native int ioctl(int i, int i2, long j);

    public static native long memcpy(long j, long j2, int i);

    public static native long mmap(long j, int i, int i2, int i3, int i4, long j2);

    public static native int munmap(long j, int i);

    public static native int open(String str, int i);

    public static native long v4l2_buf_type_alloc(int i);

    public static native long v4l2_buffer_alloc(int i);

    public static native int v4l2_buffer_getBytesused(long j);

    public static native int v4l2_buffer_getIndex(long j);

    public static native int v4l2_buffer_getLength(long j);

    public static native long v4l2_buffer_getMOffset(long j);

    public static native void v4l2_buffer_setIndex(long j, int i);

    public static native void v4l2_buffer_setMemory(long j, int i);

    public static native long v4l2_capability_alloc();

    public static native int v4l2_capability_getCapabilities(long j);

    public static native String v4l2_capability_getCard(long j);

    public static native long v4l2_format_alloc(int i);

    public static native long v4l2_format_getFmtPix(long j);

    public static native int v4l2_pix_format_getHeight(long j);

    public static native int v4l2_pix_format_getPixelformat(long j);

    public static native int v4l2_pix_format_getWidth(long j);

    public static native void v4l2_pix_format_setBytesperline(long j, int i);

    public static native void v4l2_pix_format_setField(long j, int i);

    public static native void v4l2_pix_format_setPixelformat(long j, int i);

    public static native void v4l2_pix_format_setWidthAndHeight(long j, int i, int i2);

    public static native long v4l2_requestbuffers_alloc(int i);

    public static native int v4l2_requestbuffers_getCount(long j);

    public static native void v4l2_requestbuffers_setCount(long j, int i);

    public static native void v4l2_requestbuffers_setMemory(long j, int i);

    public static native long v4l2_streamparm_alloc(int i);

    public static native void v4l2_streamparm_setFps(long j, int i);

    static {
        System.loadLibrary("jnvideo4linux2");
    }

    private static int v4l2_fourcc(char a, char b, char c, char d) {
        return (((a & UnsignedUtils.MAX_UBYTE) | ((b & UnsignedUtils.MAX_UBYTE) << 8)) | ((c & UnsignedUtils.MAX_UBYTE) << 16)) | ((d & UnsignedUtils.MAX_UBYTE) << 24);
    }
}
