package net.sf.fmj.media.rtp;

import javax.media.Buffer;
import javax.media.control.BufferControl;
import net.sf.fmj.media.Log;

class BasicJitterBufferBehaviour implements JitterBufferBehaviour {
    protected final JitterBuffer q = this.stream.q;
    private int recvBufSize;
    protected final JitterBufferStats stats = this.stream.stats;
    protected final RTPSourceStream stream;

    protected BasicJitterBufferBehaviour(RTPSourceStream stream) {
        this.stream = stream;
    }

    /* access modifiers changed from: protected */
    public void dropFirstPkt() {
        this.q.dropFirstFill();
    }

    public void dropPkt() {
        dropFirstPkt();
    }

    /* access modifiers changed from: protected */
    public BufferControl getBufferControl() {
        return this.stream.getBufferControl();
    }

    /* access modifiers changed from: protected */
    public void grow(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity");
        }
        int qCapacity = this.q.getCapacity();
        if (capacity != qCapacity) {
            if (capacity < qCapacity) {
                throw new IllegalArgumentException("capacity");
            }
            Log.info("Growing packet queue to " + capacity);
            this.stats.incrementNbGrow();
            this.q.setCapacity(capacity);
        }
    }

    public boolean isAdaptive() {
        return false;
    }

    /* access modifiers changed from: protected */
    public int monitorQSize(Buffer buffer) {
        return 0;
    }

    public boolean preAdd(Buffer buffer, RTPRawReceiver rtprawreceiver) {
        this.stats.updateSizePerPacket(buffer);
        int aprxBufferLengthInPkts = monitorQSize(buffer);
        if (aprxBufferLengthInPkts > 0) {
            setRecvBufSize(rtprawreceiver, aprxBufferLengthInPkts);
        }
        return true;
    }

    public void read(Buffer buffer) {
        if (this.q.getFillCount() == 0) {
            buffer.setDiscard(true);
            return;
        }
        Buffer bufferFromQueue = this.q.getFill();
        try {
            Object bufferData = buffer.getData();
            Object bufferHeader = buffer.getHeader();
            buffer.copy(bufferFromQueue);
            bufferFromQueue.setData(bufferData);
            bufferFromQueue.setHeader(bufferHeader);
        } finally {
            this.q.returnFree(bufferFromQueue);
        }
    }

    public void reset() {
    }

    /* access modifiers changed from: protected */
    public void setRecvBufSize(RTPRawReceiver rtprawreceiver, int aprxBufferLengthInPkts) {
        int aprxThresholdInBytes = (aprxBufferLengthInPkts * this.stats.getSizePerPacket()) / 2;
        if (rtprawreceiver != null && aprxThresholdInBytes > this.recvBufSize) {
            rtprawreceiver.setRecvBufSize(aprxThresholdInBytes);
            int recvBufSize = rtprawreceiver.getRecvBufSize();
            if (recvBufSize < aprxThresholdInBytes) {
                aprxThresholdInBytes = Integer.MAX_VALUE;
            }
            this.recvBufSize = aprxThresholdInBytes;
            Log.comment("RTP socket receive buffer size: " + recvBufSize + " bytes.\n");
        }
    }

    public boolean willReadBlock() {
        return this.q.noMoreFill();
    }
}
