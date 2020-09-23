package net.sf.fmj.media.cdp.javasound;

import java.util.logging.Logger;
import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;
import javax.media.MediaLocator;
import net.sf.fmj.media.protocol.javasound.DataSource;
import net.sf.fmj.utility.LoggerSingleton;
import org.jitsi.android.util.javax.sound.sampled.AudioSystem;
import org.jitsi.android.util.javax.sound.sampled.Mixer;
import org.jitsi.android.util.javax.sound.sampled.Mixer.Info;

public class CaptureDevicePlugger {
    private static final Logger logger = LoggerSingleton.logger;

    public void addCaptureDevices() {
        int index = 0;
        Info[] mixerInfo = AudioSystem.getMixerInfo();
        for (int i = 0; i < mixerInfo.length; i++) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo[i]);
            Format[] formats = DataSource.querySupportedFormats(i);
            if (formats != null && formats.length > 0) {
                CaptureDeviceInfo jmfInfo = new CaptureDeviceInfo("javasound:" + mixerInfo[i].getName() + ":" + index, new MediaLocator("javasound:#" + i), formats);
                index++;
                if (CaptureDeviceManager.getDevice(jmfInfo.getName()) == null) {
                    CaptureDeviceManager.addDevice(jmfInfo);
                    logger.fine("CaptureDevicePlugger: Added " + jmfInfo.getLocator());
                } else {
                    logger.fine("CaptureDevicePlugger: Already present, skipping " + jmfInfo.getLocator());
                }
            }
        }
    }
}
