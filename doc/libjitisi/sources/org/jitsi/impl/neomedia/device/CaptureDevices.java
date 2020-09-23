package org.jitsi.impl.neomedia.device;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;
import javax.media.format.AudioFormat;
import org.jitsi.impl.neomedia.MediaServiceImpl;

public class CaptureDevices extends Devices {
    public static final String PROP_DEVICE = "captureDevice";

    public CaptureDevices(AudioSystem audioSystem) {
        super(audioSystem);
    }

    public List<CaptureDeviceInfo2> getDevices() {
        List<CaptureDeviceInfo2> devices = super.getDevices();
        if (devices.isEmpty()) {
            return devices;
        }
        List<CaptureDeviceInfo2> thisDevices = new ArrayList(devices.size());
        Format format = new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, -1);
        for (CaptureDeviceInfo2 device : devices) {
            for (Format deviceFormat : device.getFormats()) {
                if (deviceFormat.matches(format)) {
                    thisDevices.add(device);
                    break;
                }
            }
        }
        return thisDevices;
    }

    /* access modifiers changed from: protected */
    public String getPropDevice() {
        return "captureDevice";
    }

    public void setDevices(List<CaptureDeviceInfo2> devices) {
        super.setDevices(devices);
        if (devices != null) {
            boolean commit = false;
            for (CaptureDeviceInfo activeDevice : devices) {
                CaptureDeviceManager.addDevice(activeDevice);
                commit = true;
            }
            if (commit && !MediaServiceImpl.isJmfRegistryDisableLoad()) {
                try {
                    CaptureDeviceManager.commit();
                } catch (IOException e) {
                }
            }
        }
    }
}
