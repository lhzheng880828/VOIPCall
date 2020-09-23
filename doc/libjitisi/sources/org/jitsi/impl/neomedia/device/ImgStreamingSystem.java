package org.jitsi.impl.neomedia.device;

import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.format.RGBFormat;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.Toolkit;
import org.jitsi.impl.neomedia.MediaServiceImpl;
import org.jitsi.impl.neomedia.codec.video.AVFrameFormat;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.device.ScreenDevice;
import org.jitsi.util.OSUtils;

public class ImgStreamingSystem extends DeviceSystem {
    private static final String LOCATOR_PROTOCOL = "imgstreaming";

    public ImgStreamingSystem() throws Exception {
        super(MediaType.VIDEO, "imgstreaming", 1);
    }

    /* access modifiers changed from: protected */
    public void doInitialize() throws Exception {
        Dimension size;
        ScreenDevice[] screens = ScreenDeviceImpl.getAvailableScreenDevices();
        String name = "Desktop Streaming";
        int i = 0;
        boolean multipleMonitorsOneScreen = false;
        Dimension screenSize = null;
        if (OSUtils.IS_LINUX) {
            size = new Dimension(0, 0);
            for (ScreenDevice screen : screens) {
                Dimension s = screen.getSize();
                size.width += s.width;
                size.height += s.height;
            }
            try {
                screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                if (screenSize.width == size.width || screenSize.height == size.height) {
                    multipleMonitorsOneScreen = true;
                }
            } catch (Exception e) {
            }
        }
        for (ScreenDevice screen2 : screens) {
            size = screenSize != null ? screenSize : screen2.getSize();
            CaptureDeviceManager.addDevice(new CaptureDeviceInfo(name + " " + i, new MediaLocator("imgstreaming:" + i), new Format[]{new AVFrameFormat(size, -1.0f, 27, -1), new RGBFormat(size, -1, Format.byteArray, -1.0f, 32, 2, 3, 4)}));
            i++;
            if (multipleMonitorsOneScreen) {
                break;
            }
        }
        if (!MediaServiceImpl.isJmfRegistryDisableLoad()) {
            CaptureDeviceManager.commit();
        }
    }
}
