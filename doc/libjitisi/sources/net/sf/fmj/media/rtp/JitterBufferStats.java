package net.sf.fmj.media.rtp;

import javax.media.Buffer;
import javax.media.control.PacketQueueControl;
import net.sf.fmj.media.Log;
import org.jitsi.android.util.java.awt.Component;

class JitterBufferStats implements PacketQueueControl {
    private int discardedFull;
    private int discardedLate;
    private int discardedReset;
    private int discardedShrink;
    private int discardedVeryLate;
    private int maxSizeReached;
    private int nbAdd;
    private int nbGrow;
    private int nbReset;
    private int sizePerPacket;
    private final RTPSourceStream stream;

    JitterBufferStats(RTPSourceStream stream) {
        this.stream = stream;
    }

    public Component getControlComponent() {
        return null;
    }

    public int getCurrentDelayMs() {
        return getCurrentDelayPackets() * 20;
    }

    public int getCurrentDelayPackets() {
        return getCurrentSizePackets() / 2;
    }

    public int getCurrentPacketCount() {
        return this.stream.q.getFillCount();
    }

    public int getCurrentSizePackets() {
        return this.stream.q.getCapacity();
    }

    public int getDiscarded() {
        return (((getDiscardedFull() + getDiscardedLate()) + getDiscardedReset()) + getDiscardedShrink()) + getDiscardedVeryLate();
    }

    public int getDiscardedFull() {
        return this.discardedFull;
    }

    public int getDiscardedLate() {
        return this.discardedLate;
    }

    public int getDiscardedReset() {
        return this.discardedReset;
    }

    public int getDiscardedShrink() {
        return this.discardedShrink;
    }

    public int getDiscardedVeryLate() {
        return this.discardedVeryLate;
    }

    public int getMaxSizeReached() {
        return this.maxSizeReached;
    }

    /* access modifiers changed from: 0000 */
    public int getNbAdd() {
        return this.nbAdd;
    }

    /* access modifiers changed from: 0000 */
    public int getSizePerPacket() {
        return this.sizePerPacket;
    }

    /* access modifiers changed from: 0000 */
    public void incrementDiscardedFull() {
        this.discardedFull++;
    }

    /* access modifiers changed from: 0000 */
    public void incrementDiscardedLate() {
        this.discardedLate++;
    }

    /* access modifiers changed from: 0000 */
    public void incrementDiscardedReset() {
        this.discardedReset++;
    }

    /* access modifiers changed from: 0000 */
    public void incrementDiscardedShrink() {
        this.discardedShrink++;
    }

    /* access modifiers changed from: 0000 */
    public void incrementDiscardedVeryLate() {
        this.discardedVeryLate++;
    }

    /* access modifiers changed from: 0000 */
    public void incrementNbAdd() {
        this.nbAdd++;
    }

    /* access modifiers changed from: 0000 */
    public void incrementNbGrow() {
        this.nbGrow++;
    }

    /* access modifiers changed from: 0000 */
    public void incrementNbReset() {
        this.nbReset++;
    }

    public boolean isAdaptiveBufferEnabled() {
        return this.stream.getBehaviour().isAdaptive();
    }

    /* access modifiers changed from: 0000 */
    public void printStats() {
        String cn = RTPSourceStream.class.getName() + " ";
        Log.info(cn + "Total packets added: " + getNbAdd());
        Log.info(cn + "Times reset() called: " + this.nbReset);
        Log.info(cn + "Times grow() called: " + this.nbGrow);
        Log.info(cn + "Packets dropped because full: " + getDiscardedFull());
        Log.info(cn + "Packets dropped while shrinking: " + getDiscardedShrink());
        Log.info(cn + "Packets dropped because they were late: " + getDiscardedLate());
        Log.info(cn + "Packets dropped because they were late by more than MAX_SIZE: " + getDiscardedVeryLate());
        Log.info(cn + "Packets dropped in reset(): " + getDiscardedReset());
        Log.info(cn + "Max size reached: " + getMaxSizeReached());
        Log.info(cn + "Adaptive jitter buffer mode was " + (isAdaptiveBufferEnabled() ? "enabled" : "disabled"));
    }

    /* access modifiers changed from: 0000 */
    public void updateMaxSizeReached() {
        int size = getCurrentSizePackets();
        if (this.maxSizeReached < size) {
            this.maxSizeReached = size;
        }
    }

    /* access modifiers changed from: 0000 */
    public void updateSizePerPacket(Buffer buffer) {
        int bufferLength = buffer.getLength();
        if (this.sizePerPacket != 0) {
            bufferLength = (this.sizePerPacket + bufferLength) / 2;
        }
        this.sizePerPacket = bufferLength;
    }
}
