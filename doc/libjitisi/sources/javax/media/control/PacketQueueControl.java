package javax.media.control;

import javax.media.Control;

public interface PacketQueueControl extends Control {
    int getCurrentDelayMs();

    int getCurrentDelayPackets();

    int getCurrentPacketCount();

    int getCurrentSizePackets();

    int getDiscarded();

    int getDiscardedFull();

    int getDiscardedLate();

    int getDiscardedReset();

    int getDiscardedShrink();

    int getMaxSizeReached();

    boolean isAdaptiveBufferEnabled();
}
