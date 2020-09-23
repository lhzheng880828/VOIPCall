package org.jitsi.impl.neomedia.device;

public class PlaybackDevices extends Devices {
    public static final String PROP_DEVICE = "playbackDevice";

    public PlaybackDevices(AudioSystem audioSystem) {
        super(audioSystem);
    }

    /* access modifiers changed from: protected */
    public String getPropDevice() {
        return "playbackDevice";
    }
}
