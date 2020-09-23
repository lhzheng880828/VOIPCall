package org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi;

public interface IMMNotificationClient {
    void OnDefaultDeviceChanged(int i, int i2, String str);

    void OnDeviceAdded(String str);

    void OnDeviceRemoved(String str);

    void OnDeviceStateChanged(String str, int i);

    void OnPropertyValueChanged(String str, long j);
}
