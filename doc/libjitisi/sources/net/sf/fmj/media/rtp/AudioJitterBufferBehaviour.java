package net.sf.fmj.media.rtp;

import com.sun.media.format.WavAudioFormat;
import com.sun.media.util.Registry;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.control.BufferControl;
import javax.media.format.AudioFormat;
import net.sf.fmj.ejmf.toolkit.util.TimeSource;
import net.sf.fmj.media.Log;

class AudioJitterBufferBehaviour extends BasicJitterBufferBehaviour {
    private static final int DEFAULT_AUD_PKT_SIZE = 256;
    private static final int DEFAULT_MS_PER_PKT = 20;
    private static final int INITIAL_PACKETS = 300;
    private static final AudioFormat MPEG = new AudioFormat(AudioFormat.MPEG_RTP);
    private final boolean AJB_ENABLED = Registry.getBoolean("adaptive_jitter_buffer_ENABLE", true);
    private final int AJB_GROW_INCREMENT = Registry.getInt("adaptive_jitter_buffer_GROW_INCREMENT", 2);
    private final int AJB_GROW_INTERVAL = Registry.getInt("adaptive_jitter_buffer_GROW_INTERVAL", 30);
    private final int AJB_GROW_THRESHOLD = Registry.getInt("adaptive_jitter_buffer_GROW_THRESHOLD", 3);
    private final int AJB_MAX_SIZE = Registry.getInt("adaptive_jitter_buffer_MAX_SIZE", 16);
    private final int AJB_MIN_SIZE = Registry.getInt("adaptive_jitter_buffer_MIN_SIZE", 4);
    private final int AJB_SHRINK_DECREMENT = Registry.getInt("adaptive_jitter_buffer_SHRINK_DECREMENT", 1);
    private final int AJB_SHRINK_INTERVAL = Registry.getInt("adaptive_jitter_buffer_SHRINK_INTERVAL", WavAudioFormat.WAVE_FORMAT_VOXWARE_VR18);
    private final int AJB_SHRINK_THRESHOLD = Registry.getInt("adaptive_jitter_buffer_SHRINK_THRESHOLD", 1);
    private int growCount;
    private byte[] history;
    private int historyLength;
    private int historyTail;
    private long msPerPkt = 20;
    private boolean replenish = true;
    private int shrinkCount = 0;
    private boolean skipFec = false;

    public AudioJitterBufferBehaviour(RTPSourceStream stream) {
        super(stream);
        initHistory();
    }

    public void dropPkt() {
        super.dropPkt();
        this.skipFec = true;
        if (this.q.getFillCount() < this.AJB_SHRINK_THRESHOLD) {
            this.shrinkCount = 0;
        }
    }

    /* access modifiers changed from: protected */
    public void grow(int capacity) {
        super.grow(capacity);
        resetHistory();
    }

    private void initHistory() {
        this.history = new byte[this.AJB_GROW_INTERVAL];
        this.historyLength = 0;
        this.historyTail = 0;
        this.growCount = 0;
    }

    public boolean isAdaptive() {
        return this.AJB_ENABLED;
    }

    /* access modifiers changed from: protected */
    public int monitorQSize(Buffer buffer) {
        int size;
        super.monitorQSize(buffer);
        if (this.AJB_ENABLED) {
            int n;
            size = this.q.getCapacity();
            if (this.historyLength >= this.AJB_GROW_INTERVAL && this.growCount >= this.AJB_GROW_THRESHOLD && size < this.AJB_MAX_SIZE) {
                n = Math.min(this.AJB_GROW_INCREMENT + size, this.AJB_MAX_SIZE);
                if (n > size) {
                    grow(n);
                }
            }
            this.shrinkCount++;
            if (this.shrinkCount >= this.AJB_SHRINK_INTERVAL && size > this.AJB_MIN_SIZE && this.q.freeNotEmpty()) {
                n = Math.max(size - this.AJB_SHRINK_DECREMENT, this.AJB_MIN_SIZE);
                if (n < size) {
                    shrink(n);
                }
            }
        }
        BufferControl bc = getBufferControl();
        if (bc == null) {
            return 0;
        }
        long ms;
        Format format = this.stream.getFormat();
        int sizePerPkt = this.stats.getSizePerPacket();
        if (sizePerPkt <= 0) {
            sizePerPkt = 256;
        }
        if (MPEG.matches(format)) {
            ms = (long) (sizePerPkt / 4);
        } else {
            ms = 20;
            try {
                long ns = buffer.getDuration();
                if (ns <= 0) {
                    ns = ((AudioFormat) format).computeDuration((long) buffer.getLength());
                    if (ns > 0) {
                        ms = ns / TimeSource.MICROS_PER_SEC;
                    }
                } else {
                    ms = ns / TimeSource.MICROS_PER_SEC;
                }
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    ThreadDeath t2 = (ThreadDeath) t;
                }
            }
        }
        this.msPerPkt = (this.msPerPkt + ms) / 2;
        int aprxBufferLengthInPkts = (int) (bc.getBufferLength() / (this.msPerPkt == 0 ? 20 : this.msPerPkt));
        if (this.AJB_ENABLED || aprxBufferLengthInPkts <= this.q.getCapacity()) {
            return aprxBufferLengthInPkts;
        }
        grow(aprxBufferLengthInPkts);
        size = this.q.getCapacity();
        Log.comment("Grew audio RTP packet queue to: " + size + " pkts, " + (size * sizePerPkt) + " bytes.\n");
        return aprxBufferLengthInPkts;
    }

    public boolean preAdd(Buffer buffer, RTPRawReceiver rtprawreceiver) {
        long lastSeqSent = this.stream.getLastReadSequenceNumber();
        if (lastSeqSent != Buffer.SEQUENCE_UNKNOWN) {
            long bufferSN = buffer.getSequenceNumber();
            if (bufferSN < lastSeqSent) {
                if (lastSeqSent - bufferSN < ((long) this.AJB_MAX_SIZE)) {
                    recordInHistory(true);
                    this.stats.incrementDiscardedLate();
                    return false;
                }
                this.stats.incrementDiscardedVeryLate();
                return false;
            }
        }
        recordInHistory(false);
        if (!super.preAdd(buffer, rtprawreceiver)) {
            return false;
        }
        if (this.AJB_ENABLED && this.q.noMoreFree() && this.stats.getNbAdd() > 300) {
            int size = this.q.getCapacity();
            if (size < this.AJB_MAX_SIZE) {
                grow(Math.min(size * 2, this.AJB_MAX_SIZE));
            } else {
                while (this.q.getFillCount() >= size / 2) {
                    this.stats.incrementDiscardedFull();
                    dropPkt();
                }
            }
        }
        return true;
    }

    public void read(Buffer buffer) {
        super.read(buffer);
        if (!buffer.isDiscard() && this.skipFec) {
            buffer.setFlags(buffer.getFlags() | Buffer.FLAG_SKIP_FEC);
            this.skipFec = false;
        }
        int totalPkts = this.q.getFillCount();
        if (totalPkts == 0) {
            this.replenish = true;
        }
        if (totalPkts < this.AJB_SHRINK_THRESHOLD) {
            this.shrinkCount = 0;
        }
    }

    private void recordInHistory(boolean late) {
        int n = late ? 1 : 0;
        this.growCount += n - this.history[this.historyTail];
        this.history[this.historyTail] = (byte) n;
        this.historyTail = (this.historyTail + 1) % this.AJB_GROW_INTERVAL;
        if (this.historyLength < this.AJB_GROW_INTERVAL) {
            this.historyLength++;
        }
    }

    public void reset() {
        super.reset();
        resetHistory();
    }

    private void resetHistory() {
        this.historyLength = 0;
        this.shrinkCount = 0;
    }

    private void shrink(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity");
        }
        int qCapacity = this.q.getCapacity();
        if (capacity != qCapacity) {
            if (capacity > qCapacity) {
                throw new IllegalArgumentException("capacity");
            }
            Log.info("Shrinking packet queue to " + capacity);
            int dropped = 0;
            while (this.q.getFillCount() > capacity) {
                dropPkt();
                this.stats.incrementDiscardedShrink();
                dropped++;
            }
            this.q.setCapacity(capacity);
            while (dropped < this.AJB_SHRINK_DECREMENT && this.q.fillNotEmpty()) {
                dropPkt();
                this.stats.incrementDiscardedShrink();
                dropped++;
            }
            resetHistory();
        }
    }

    public boolean willReadBlock() {
        boolean b = super.willReadBlock();
        if (b) {
            return b;
        }
        if (this.replenish && this.q.getFillCount() >= this.q.getCapacity() / 2) {
            this.replenish = false;
        }
        return this.replenish;
    }
}
