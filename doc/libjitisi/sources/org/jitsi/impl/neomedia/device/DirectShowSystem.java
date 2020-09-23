package org.jitsi.impl.neomedia.device;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;
import javax.media.MediaLocator;
import org.jitsi.impl.neomedia.MediaServiceImpl;
import org.jitsi.impl.neomedia.codec.video.AVFrameFormat;
import org.jitsi.impl.neomedia.jmfext.media.protocol.directshow.DSCaptureDevice;
import org.jitsi.impl.neomedia.jmfext.media.protocol.directshow.DSFormat;
import org.jitsi.impl.neomedia.jmfext.media.protocol.directshow.DSManager;
import org.jitsi.impl.neomedia.jmfext.media.protocol.directshow.DataSource;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.util.Logger;

public class DirectShowSystem extends DeviceSystem {
    private static final String LOCATOR_PROTOCOL = "directshow";
    private static final Logger logger = Logger.getLogger(DirectShowSystem.class);

    public DirectShowSystem() throws Exception {
        super(MediaType.VIDEO, "directshow");
    }

    /* access modifiers changed from: protected */
    public void doInitialize() throws Exception {
        DSManager manager = new DSManager();
        try {
            DSCaptureDevice[] devices = manager.getCaptureDevices();
            boolean captureDeviceInfoIsAdded = false;
            int i = 0;
            int count = devices == null ? 0 : devices.length;
            while (i < count) {
                DSCaptureDevice device = devices[i];
                DSFormat[] dsFormats = device.getSupportedFormats();
                String name = device.getName();
                if (dsFormats.length == 0) {
                    logger.warn("Camera '" + name + "' reported no supported formats.");
                } else {
                    List<Format> formats = new ArrayList(dsFormats.length);
                    for (DSFormat dsFormat : dsFormats) {
                        int pixelFormat = dsFormat.getPixelFormat();
                        int ffmpegPixFmt = DataSource.getFFmpegPixFmt(pixelFormat);
                        if (ffmpegPixFmt != -1) {
                            Format format = new AVFrameFormat(ffmpegPixFmt, pixelFormat);
                            if (!formats.contains(format)) {
                                formats.add(format);
                            }
                        }
                    }
                    if (formats.isEmpty()) {
                        logger.warn("No support for the formats of camera '" + name + "': " + Arrays.toString(dsFormats));
                    } else {
                        Format[] formatsArray = (Format[]) formats.toArray(new Format[formats.size()]);
                        if (logger.isInfoEnabled()) {
                            logger.info("Support for the formats of camera '" + name + "': " + Arrays.toString(formatsArray));
                        }
                        CaptureDeviceManager.addDevice(new CaptureDeviceInfo(name, new MediaLocator("directshow:" + name), formatsArray));
                        captureDeviceInfoIsAdded = true;
                    }
                }
                i++;
            }
            if (captureDeviceInfoIsAdded && !MediaServiceImpl.isJmfRegistryDisableLoad()) {
                CaptureDeviceManager.commit();
            }
            manager.dispose();
        } catch (Throwable th) {
            manager.dispose();
        }
    }
}
