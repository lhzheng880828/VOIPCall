package org.jitsi.impl.neomedia.device;

import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.format.RGBFormat;
import org.jitsi.impl.neomedia.MediaServiceImpl;
import org.jitsi.impl.neomedia.codec.video.AVFrameFormat;
import org.jitsi.impl.neomedia.quicktime.QTCaptureDevice;
import org.jitsi.impl.neomedia.quicktime.QTFormatDescription;
import org.jitsi.impl.neomedia.quicktime.QTMediaType;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.util.Logger;

public class QuickTimeSystem extends DeviceSystem {
    private static final String LOCATOR_PROTOCOL = "quicktime";
    private static final Logger logger = Logger.getLogger(QuickTimeSystem.class);

    public QuickTimeSystem() throws Exception {
        super(MediaType.VIDEO, "quicktime");
    }

    /* access modifiers changed from: protected */
    public void doInitialize() throws Exception {
        boolean captureDeviceInfoIsAdded = false;
        for (QTCaptureDevice inputDevice : QTCaptureDevice.inputDevicesWithMediaType(QTMediaType.Video)) {
            CaptureDeviceInfo device = new CaptureDeviceInfo(inputDevice.localizedDisplayName(), new MediaLocator("quicktime:" + inputDevice.uniqueID()), new Format[]{new AVFrameFormat(27), new RGBFormat()});
            if (logger.isInfoEnabled()) {
                for (QTFormatDescription f : inputDevice.formatDescriptions()) {
                    logger.info("Webcam available resolution for " + inputDevice.localizedDisplayName() + ":" + f.sizeForKey(QTFormatDescription.VideoEncodedPixelsSizeAttribute));
                }
            }
            CaptureDeviceManager.addDevice(device);
            captureDeviceInfoIsAdded = true;
            if (logger.isDebugEnabled()) {
                logger.debug("Added CaptureDeviceInfo " + device);
            }
        }
        if (captureDeviceInfoIsAdded && !MediaServiceImpl.isJmfRegistryDisableLoad()) {
            CaptureDeviceManager.commit();
        }
    }
}
