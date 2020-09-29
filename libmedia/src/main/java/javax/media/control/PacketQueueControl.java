package javax.media.control;

import javax.media.Control;

/**
 * Author:cl
 * Email:lhzheng@grandstream.cn
 * Date:20-9-29
 */
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
