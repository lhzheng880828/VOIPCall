package org.jitsi.impl.neomedia.device;

public class NotifyDevices extends PlaybackDevices {
    public static final String PROP_DEVICE = "notifyDevice";

    public NotifyDevices(AudioSystem audioSystem) {
        super(audioSystem);
    }

    /* access modifiers changed from: protected */
    public String getPropDevice() {
        return "notifyDevice";
    }
}
