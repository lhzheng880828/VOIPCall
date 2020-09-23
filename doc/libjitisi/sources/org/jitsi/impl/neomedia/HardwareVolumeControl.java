package org.jitsi.impl.neomedia;

import org.jitsi.impl.neomedia.device.AudioSystem;
import org.jitsi.impl.neomedia.device.AudioSystem.DataFlow;
import org.jitsi.impl.neomedia.device.CaptureDeviceInfo2;
import org.jitsi.impl.neomedia.device.CoreAudioDevice;
import org.jitsi.service.neomedia.BasicVolumeControl;
import org.jitsi.util.Logger;

public class HardwareVolumeControl extends BasicVolumeControl {
    private static final float MAX_HARDWARE_POWER = 1.0f;
    private static final Logger logger = Logger.getLogger(HardwareVolumeControl.class);
    MediaServiceImpl mediaServiceImpl = null;

    public HardwareVolumeControl(MediaServiceImpl mediaServiceImpl, String volumeLevelConfigurationPropertyName) {
        super(volumeLevelConfigurationPropertyName);
        this.mediaServiceImpl = mediaServiceImpl;
        this.volumeLevel = getDefaultVolumeLevel();
        float volume = getVolume();
        if (volume != -1.0f) {
            this.volumeLevel = volume;
        }
    }

    protected static float getDefaultVolumeLevel() {
        return 0.5f;
    }

    protected static float getGainReferenceLevel() {
        return MAX_HARDWARE_POWER;
    }

    /* access modifiers changed from: protected */
    public void updateHardwareVolume() {
        String deviceUID = getCaptureDeviceUID();
        float hardwareVolumeLevel = this.volumeLevel * MAX_HARDWARE_POWER;
        if (hardwareVolumeLevel > MAX_HARDWARE_POWER) {
            hardwareVolumeLevel = MAX_HARDWARE_POWER;
        }
        if (setInputDeviceVolume(deviceUID, hardwareVolumeLevel) != 0) {
            logger.debug("Could not change hardware input device level");
        }
    }

    /* access modifiers changed from: protected */
    public String getCaptureDeviceUID() {
        AudioSystem audioSystem = this.mediaServiceImpl.getDeviceConfiguration().getAudioSystem();
        CaptureDeviceInfo2 captureDevice = audioSystem == null ? null : audioSystem.getSelectedDevice(DataFlow.CAPTURE);
        if (captureDevice == null) {
            return null;
        }
        return captureDevice.getUID();
    }

    /* access modifiers changed from: protected */
    public int setInputDeviceVolume(String deviceUID, float volume) {
        if (deviceUID == null) {
            return -1;
        }
        if (CoreAudioDevice.initDevices() == -1) {
            CoreAudioDevice.freeDevices();
            logger.debug("Could not initialize CoreAudio input devices");
            return -1;
        } else if (CoreAudioDevice.setInputDeviceVolume(deviceUID, volume) != 0) {
            CoreAudioDevice.freeDevices();
            logger.debug("Could not change CoreAudio input device level");
            return -1;
        } else {
            CoreAudioDevice.freeDevices();
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public float getInputDeviceVolume(String deviceUID) {
        if (deviceUID == null) {
            return -1.0f;
        }
        if (CoreAudioDevice.initDevices() == -1) {
            CoreAudioDevice.freeDevices();
            logger.debug("Could not initialize CoreAudio input devices");
            return -1.0f;
        }
        float volume = CoreAudioDevice.getInputDeviceVolume(deviceUID);
        if (volume == -1.0f) {
            CoreAudioDevice.freeDevices();
            logger.debug("Could not get CoreAudio input device level");
            return -1.0f;
        }
        CoreAudioDevice.freeDevices();
        return volume;
    }

    public float getVolume() {
        float volume = getInputDeviceVolume(getCaptureDeviceUID());
        if (volume == -1.0f) {
            return super.getVolume();
        }
        return volume;
    }
}
