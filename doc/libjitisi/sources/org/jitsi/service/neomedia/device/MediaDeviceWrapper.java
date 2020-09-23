package org.jitsi.service.neomedia.device;

public interface MediaDeviceWrapper extends MediaDevice {
    MediaDevice getWrappedDevice();
}
