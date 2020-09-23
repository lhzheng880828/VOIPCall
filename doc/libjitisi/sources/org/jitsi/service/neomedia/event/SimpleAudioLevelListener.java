package org.jitsi.service.neomedia.event;

public interface SimpleAudioLevelListener {
    public static final int MAX_LEVEL = 127;
    public static final int MIN_LEVEL = 0;

    void audioLevelChanged(int i);
}
