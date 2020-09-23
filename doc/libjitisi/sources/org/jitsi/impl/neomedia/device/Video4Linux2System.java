package org.jitsi.impl.neomedia.device;

import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;
import javax.media.MediaLocator;
import org.jitsi.impl.neomedia.MediaServiceImpl;
import org.jitsi.impl.neomedia.codec.video.AVFrameFormat;
import org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2.DataSource;
import org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2.Video4Linux2;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.util.Logger;

public class Video4Linux2System extends DeviceSystem {
    private static final String LOCATOR_PROTOCOL = "video4linux2";
    private static final Logger logger = Logger.getLogger(Video4Linux2System.class);

    public Video4Linux2System() throws Exception {
        super(MediaType.VIDEO, "video4linux2");
    }

    private boolean discoverAndRegister(String deviceName) throws Exception {
        int fd = Video4Linux2.open(deviceName, 2);
        boolean captureDeviceInfoIsAdded = false;
        if (-1 != fd) {
            long v4l2_capability;
            try {
                v4l2_capability = Video4Linux2.v4l2_capability_alloc();
                if (0 != v4l2_capability) {
                    if (Video4Linux2.ioctl(fd, Video4Linux2.VIDIOC_QUERYCAP, v4l2_capability) != -1 && (Video4Linux2.v4l2_capability_getCapabilities(v4l2_capability) & 1) == 1) {
                        captureDeviceInfoIsAdded = register(deviceName, fd, v4l2_capability);
                    }
                    Video4Linux2.free(v4l2_capability);
                }
                Video4Linux2.close(fd);
            } catch (Throwable th) {
                Video4Linux2.close(fd);
            }
        }
        return captureDeviceInfoIsAdded;
    }

    /* access modifiers changed from: protected */
    public void doInitialize() throws Exception {
        String baseDeviceName = "/dev/video";
        boolean captureDeviceInfoIsAdded = discoverAndRegister(baseDeviceName);
        for (int deviceMinorNumber = 0; deviceMinorNumber <= 63; deviceMinorNumber++) {
            captureDeviceInfoIsAdded = discoverAndRegister(new StringBuilder().append(baseDeviceName).append(deviceMinorNumber).toString()) || captureDeviceInfoIsAdded;
        }
        if (captureDeviceInfoIsAdded && !MediaServiceImpl.isJmfRegistryDisableLoad()) {
            CaptureDeviceManager.commit();
        }
    }

    private boolean register(String deviceName, int fd, long v4l2_capability) throws Exception {
        long v4l2_format = Video4Linux2.v4l2_format_alloc(1);
        int pixelformat = 0;
        String supportedRes = null;
        if (0 != v4l2_format) {
            try {
                if (Video4Linux2.ioctl(fd, Video4Linux2.VIDIOC_G_FMT, v4l2_format) != -1) {
                    long fmtPix = Video4Linux2.v4l2_format_getFmtPix(v4l2_format);
                    pixelformat = Video4Linux2.v4l2_pix_format_getPixelformat(fmtPix);
                    if (-1 == DataSource.getFFmpegPixFmt(pixelformat)) {
                        Video4Linux2.v4l2_pix_format_setPixelformat(fmtPix, Video4Linux2.V4L2_PIX_FMT_RGB24);
                        if (Video4Linux2.ioctl(fd, Video4Linux2.VIDIOC_S_FMT, v4l2_format) != -1) {
                            pixelformat = Video4Linux2.v4l2_pix_format_getPixelformat(fmtPix);
                        }
                    }
                    if (logger.isInfoEnabled()) {
                        supportedRes = Video4Linux2.v4l2_pix_format_getWidth(fmtPix) + "x" + Video4Linux2.v4l2_pix_format_getHeight(fmtPix);
                    }
                }
                Video4Linux2.free(v4l2_format);
            } catch (Throwable th) {
                Video4Linux2.free(v4l2_format);
            }
        }
        int ffmpegPixFmt = DataSource.getFFmpegPixFmt(pixelformat);
        if (-1 == ffmpegPixFmt) {
            return false;
        }
        Format format = new AVFrameFormat(ffmpegPixFmt, pixelformat);
        String name = Video4Linux2.v4l2_capability_getCard(v4l2_capability);
        if (name == null || name.length() == 0) {
            name = deviceName;
        } else {
            name = name + " (" + deviceName + ")";
        }
        if (logger.isInfoEnabled() && supportedRes != null) {
            logger.info("Webcam available resolution for " + name + ":" + supportedRes);
        }
        CaptureDeviceManager.addDevice(new CaptureDeviceInfo(name, new MediaLocator("video4linux2:" + deviceName), new Format[]{format}));
        return true;
    }
}
