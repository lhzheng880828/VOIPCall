package org.jitsi.service.neomedia;

import org.jitsi.service.neomedia.event.VolumeChangeListener;

public interface VolumeControl {
    public static final String CAPTURE_VOLUME_LEVEL_PROPERTY_NAME = "net.java.sip.communicator.service.media.CAPTURE_VOLUME_LEVEL";
    public static final String PLAYBACK_VOLUME_LEVEL_PROPERTY_NAME = "net.java.sip.communicator.service.media.PLAYBACK_VOLUME_LEVEL";

    void addVolumeChangeListener(VolumeChangeListener volumeChangeListener);

    float getMaxValue();

    float getMinValue();

    boolean getMute();

    float getVolume();

    void removeVolumeChangeListener(VolumeChangeListener volumeChangeListener);

    void setMute(boolean z);

    float setVolume(float f);
}
