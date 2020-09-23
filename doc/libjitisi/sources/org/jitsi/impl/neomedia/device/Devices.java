package org.jitsi.impl.neomedia.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.media.MediaLocator;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.libjitsi.LibJitsi;

public abstract class Devices {
    private static final String PROP_DISABLE_USB_DEVICE_AUTO_SELECTION = "org.jitsi.impl.neomedia.device.disableUsbDeviceAutoSelection";
    private final AudioSystem audioSystem;
    private CaptureDeviceInfo2 device;
    private final List<String> devicePreferences = new ArrayList();
    private List<CaptureDeviceInfo2> devices;

    public abstract String getPropDevice();

    public Devices(AudioSystem audioSystem) {
        this.audioSystem = audioSystem;
    }

    private void addToDevicePreferences(String newDeviceIdentifier, boolean isSelected) {
        synchronized (this.devicePreferences) {
            this.devicePreferences.remove(newDeviceIdentifier);
            if (isSelected) {
                this.devicePreferences.add(0, newDeviceIdentifier);
            } else {
                this.devicePreferences.add(newDeviceIdentifier);
            }
        }
    }

    public CaptureDeviceInfo2 getDevice(MediaLocator locator) {
        if (this.devices == null) {
            return null;
        }
        for (CaptureDeviceInfo2 aDevice : this.devices) {
            if (locator.equals(aDevice.getLocator())) {
                return aDevice;
            }
        }
        return null;
    }

    public List<CaptureDeviceInfo2> getDevices() {
        if (this.devices == null) {
            return Collections.emptyList();
        }
        return new ArrayList(this.devices);
    }

    public CaptureDeviceInfo2 getSelectedDevice(List<CaptureDeviceInfo2> activeDevices) {
        if (activeDevices != null) {
            CaptureDeviceInfo2 activeDevice;
            String property = getPropDevice();
            loadDevicePreferences(property);
            renameOldFashionedIdentifier(activeDevices);
            boolean isEmptyList = this.devicePreferences.isEmpty();
            for (int i = activeDevices.size() - 1; i >= 0; i--) {
                activeDevice = (CaptureDeviceInfo2) activeDevices.get(i);
                if (!this.devicePreferences.contains(activeDevice.getModelIdentifier())) {
                    boolean isSelected = activeDevice.isSameTransportType("USB");
                    ConfigurationService cfg = LibJitsi.getConfigurationService();
                    if (cfg != null && cfg.getBoolean(PROP_DISABLE_USB_DEVICE_AUTO_SELECTION, false)) {
                        isSelected = false;
                    }
                    if (!(!isEmptyList || activeDevice.isSameTransportType("Bluetooth") || activeDevice.isSameTransportType("AirPlay"))) {
                        isSelected = true;
                    }
                    saveDevice(property, activeDevice, isSelected);
                }
            }
            synchronized (this.devicePreferences) {
                for (String devicePreference : this.devicePreferences) {
                    for (CaptureDeviceInfo2 activeDevice2 : activeDevices) {
                        if (devicePreference.equals(activeDevice2.getModelIdentifier())) {
                            return activeDevice2;
                        } else if (devicePreference.equals(NoneAudioSystem.LOCATOR_PROTOCOL)) {
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }

    private void loadDevicePreferences(String property) {
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        if (cfg != null) {
            String deviceIdentifiersString = cfg.getString(this.audioSystem.getPropertyName(property + "_list"));
            synchronized (this.devicePreferences) {
                if (deviceIdentifiersString != null) {
                    this.devicePreferences.clear();
                    for (String deviceIdentifier : deviceIdentifiersString.substring(2, deviceIdentifiersString.length() - 2).split("\", \"")) {
                        this.devicePreferences.add(deviceIdentifier);
                    }
                } else {
                    deviceIdentifiersString = cfg.getString(this.audioSystem.getPropertyName(property));
                    if (!(deviceIdentifiersString == null || NoneAudioSystem.LOCATOR_PROTOCOL.equalsIgnoreCase(deviceIdentifiersString))) {
                        this.devicePreferences.clear();
                        this.devicePreferences.add(deviceIdentifiersString);
                    }
                }
            }
        }
    }

    private void renameOldFashionedIdentifier(List<CaptureDeviceInfo2> activeDevices) {
        for (CaptureDeviceInfo2 activeDevice : activeDevices) {
            String name = activeDevice.getName();
            String id = activeDevice.getModelIdentifier();
            if (!name.equals(id)) {
                synchronized (this.devicePreferences) {
                    while (true) {
                        int nameIndex = this.devicePreferences.indexOf(name);
                        if (nameIndex == -1) {
                        } else if (this.devicePreferences.indexOf(id) == -1) {
                            this.devicePreferences.set(nameIndex, id);
                        } else {
                            this.devicePreferences.remove(nameIndex);
                        }
                    }
                }
                break;
            }
        }
    }

    private void saveDevice(String property, CaptureDeviceInfo2 device, boolean isSelected) {
        addToDevicePreferences(device == null ? NoneAudioSystem.LOCATOR_PROTOCOL : device.getModelIdentifier(), isSelected);
        writeDevicePreferences(property);
    }

    public void setDevice(CaptureDeviceInfo2 device, boolean save) {
        if (device == null || !device.equals(this.device)) {
            String property = getPropDevice();
            CaptureDeviceInfo2 oldValue = this.device;
            if (save) {
                saveDevice(property, device, true);
            }
            this.device = device;
            this.audioSystem.propertyChange(property, oldValue, this.device);
        }
    }

    public void setDevices(List<CaptureDeviceInfo2> devices) {
        this.devices = devices == null ? null : new ArrayList(devices);
    }

    private void writeDevicePreferences(String property) {
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        if (cfg != null) {
            property = this.audioSystem.getPropertyName(property + "_list");
            StringBuilder value = new StringBuilder("[\"");
            synchronized (this.devicePreferences) {
                int devicePreferenceCount = this.devicePreferences.size();
                if (devicePreferenceCount != 0) {
                    value.append((String) this.devicePreferences.get(0));
                    for (int i = 1; i < devicePreferenceCount; i++) {
                        value.append("\", \"").append((String) this.devicePreferences.get(i));
                    }
                }
            }
            value.append("\"]");
            cfg.setProperty(property, value.toString());
        }
    }
}
