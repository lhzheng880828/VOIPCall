package org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2;

import java.io.IOException;
import javax.media.MediaLocator;
import javax.media.control.FormatControl;
import javax.media.protocol.PullBufferStream;
import org.jitsi.impl.neomedia.device.DeviceSystem;
import org.jitsi.impl.neomedia.jmfext.media.protocol.AbstractVideoPullBufferCaptureDevice;

public class DataSource extends AbstractVideoPullBufferCaptureDevice {
    private static final int[] V4L2_TO_FFMPEG_PIX_FMT = new int[]{Video4Linux2.V4L2_PIX_FMT_UYVY, 17, Video4Linux2.V4L2_PIX_FMT_YUV420, 0, Video4Linux2.V4L2_PIX_FMT_YUYV, 1, Video4Linux2.V4L2_PIX_FMT_MJPEG, 13, Video4Linux2.V4L2_PIX_FMT_JPEG, 13, Video4Linux2.V4L2_PIX_FMT_RGB24, 2, Video4Linux2.V4L2_PIX_FMT_BGR24, 3};
    private int fd = -1;

    public DataSource(MediaLocator locator) {
        super(locator);
    }

    /* access modifiers changed from: protected */
    public Video4Linux2Stream createStream(int streamIndex, FormatControl formatControl) {
        return new Video4Linux2Stream(this, formatControl);
    }

    /* access modifiers changed from: protected */
    public void doConnect() throws IOException {
        super.doConnect();
        String deviceName = getDeviceName();
        int fd = Video4Linux2.open(deviceName, 2);
        if (-1 == fd) {
            throw new IOException("Failed to open " + deviceName);
        }
        boolean close = true;
        try {
            synchronized (getStreamSyncRoot()) {
                for (PullBufferStream stream : getStreams()) {
                    ((Video4Linux2Stream) stream).setFd(fd);
                }
            }
            close = false;
            this.fd = fd;
        } finally {
            if (close) {
                Video4Linux2.close(fd);
                fd = -1;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void doDisconnect() {
        try {
            synchronized (getStreamSyncRoot()) {
                Object[] streams = streams();
                if (streams != null) {
                    for (Object stream : streams) {
                        try {
                            ((Video4Linux2Stream) stream).setFd(-1);
                        } catch (IOException e) {
                        }
                    }
                }
            }
        } finally {
            try {
                super.doDisconnect();
            } finally {
                Video4Linux2.close(this.fd);
            }
        }
    }

    private String getDeviceName() {
        MediaLocator locator = getLocator();
        return (locator == null || !DeviceSystem.LOCATOR_PROTOCOL_VIDEO4LINUX2.equalsIgnoreCase(locator.getProtocol())) ? null : locator.getRemainder();
    }

    public static int getFFmpegPixFmt(int v4l2PixFmt) {
        for (int i = 0; i < V4L2_TO_FFMPEG_PIX_FMT.length; i += 2) {
            if (V4L2_TO_FFMPEG_PIX_FMT[i] == v4l2PixFmt) {
                return V4L2_TO_FFMPEG_PIX_FMT[i + 1];
            }
        }
        return -1;
    }

    public static int getV4L2PixFmt(int ffmpegPixFmt) {
        for (int i = 0; i < V4L2_TO_FFMPEG_PIX_FMT.length; i += 2) {
            if (V4L2_TO_FFMPEG_PIX_FMT[i + 1] == ffmpegPixFmt) {
                return V4L2_TO_FFMPEG_PIX_FMT[i];
            }
        }
        return 0;
    }
}
