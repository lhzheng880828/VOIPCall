package net.sf.fmj.media.rtp;

import javax.media.Buffer;
import javax.media.Controller;
import javax.media.Format;
import javax.media.control.BufferControl;
import javax.media.format.VideoFormat;
import net.sf.fmj.media.Log;
import org.jitsi.service.neomedia.codec.Constants;

class VideoJitterBufferBehaviour extends BasicJitterBufferBehaviour {
    private static final int BUF_CHECK_INTERVAL = 7000;
    private static final int DEFAULT_PKTS_TO_BUFFER = 90;
    private static final int DEFAULT_VIDEO_RATE = 15;
    private static final int FUDGE = 5;
    private static final VideoFormat H264 = new VideoFormat(Constants.H264_RTP);
    private static final int MIN_BUF_CHECK = 10000;
    private static final VideoFormat MPEG = new VideoFormat(VideoFormat.MPEG_RTP);
    private int fps = 15;
    private int framesEst = 0;
    private long lastCheckTime = 0;
    private long lastPktSeq = 0;
    private int maxPktsToBuffer = 0;
    private int pktsEst;
    private int pktsPerFrame = 15;
    private int tooMuchBufferingCount = 0;

    public VideoJitterBufferBehaviour(RTPSourceStream stream) {
        super(stream);
    }

    private void cutByHalf() {
        int capacity = this.q.getCapacity() / 2;
        if (capacity > 0) {
            this.q.setCapacity(capacity);
        }
    }

    /* access modifiers changed from: protected */
    public void dropFirstPkt() {
        if (MPEG.matches(this.stream.getFormat())) {
            dropMpegPkt();
        } else {
            super.dropFirstPkt();
        }
    }

    private void dropMpegPkt() {
        int i = 0;
        int j = -1;
        int k = -1;
        int count = this.q.getFillCount();
        while (i < count) {
            Buffer buffer = this.q.getFill(i);
            int i1 = ((byte[]) buffer.getData())[buffer.getOffset() + 2] & 7;
            if (i1 > 2) {
                k = i;
                break;
            }
            if (i1 == 2 && j == -1) {
                j = i;
            }
            i++;
        }
        if (k == -1) {
            i = j != -1 ? j : 0;
        }
        this.q.dropFill(i);
    }

    public boolean isAdaptive() {
        return true;
    }

    /* access modifiers changed from: protected */
    public int monitorQSize(Buffer buffer) {
        int aprxBufferLengthInPkts;
        super.monitorQSize(buffer);
        if (this.lastPktSeq + 1 == buffer.getSequenceNumber()) {
            this.pktsEst++;
        } else {
            this.pktsEst = 1;
        }
        this.lastPktSeq = buffer.getSequenceNumber();
        Format format = this.stream.getFormat();
        if (MPEG.matches(format)) {
            if ((((byte[]) buffer.getData())[buffer.getOffset() + 2] & 7) < 3 && (buffer.getFlags() & 2048) != 0) {
                this.pktsPerFrame = (this.pktsPerFrame + this.pktsEst) / 2;
                this.pktsEst = 0;
            }
            this.fps = 30;
        } else if (H264.matches(format)) {
            this.pktsPerFrame = Controller.Realized;
            this.fps = 15;
        }
        if ((buffer.getFlags() & 2048) != 0) {
            this.pktsPerFrame = (this.pktsPerFrame + this.pktsEst) / 2;
            this.pktsEst = 0;
            this.framesEst++;
            long l = System.currentTimeMillis();
            if (l - this.lastCheckTime >= 1000) {
                this.lastCheckTime = l;
                this.fps = (this.fps + this.framesEst) / 2;
                this.framesEst = 0;
                if (this.fps > 30) {
                    this.fps = 30;
                }
            }
        }
        BufferControl bc = getBufferControl();
        if (bc != null) {
            aprxBufferLengthInPkts = (int) ((bc.getBufferLength() * ((long) this.fps)) / 1000);
            if (aprxBufferLengthInPkts <= 0) {
                aprxBufferLengthInPkts = 1;
            }
            aprxBufferLengthInPkts *= this.pktsPerFrame;
        } else {
            aprxBufferLengthInPkts = DEFAULT_PKTS_TO_BUFFER;
        }
        if (H264.matches(format)) {
            this.maxPktsToBuffer = 200;
        } else if (this.maxPktsToBuffer > 0) {
            this.maxPktsToBuffer = (this.maxPktsToBuffer + aprxBufferLengthInPkts) / 2;
        } else {
            this.maxPktsToBuffer = aprxBufferLengthInPkts;
        }
        int size = this.q.getCapacity();
        int i1 = this.q.getFillCount();
        if (size > MIN_BUF_CHECK && i1 < size / 4) {
            int i = this.tooMuchBufferingCount;
            this.tooMuchBufferingCount = i + 1;
            if (i > (this.pktsPerFrame * this.fps) * BUF_CHECK_INTERVAL) {
                cutByHalf();
                this.tooMuchBufferingCount = 0;
            }
        } else if (i1 < size / 2 || size >= this.maxPktsToBuffer) {
            this.tooMuchBufferingCount = 0;
        } else {
            aprxBufferLengthInPkts = size + (size / 2);
            if (aprxBufferLengthInPkts > this.maxPktsToBuffer) {
                aprxBufferLengthInPkts = this.maxPktsToBuffer;
            }
            this.q.setCapacity(aprxBufferLengthInPkts + 5);
            Log.comment("RTP video buffer size: " + this.q.getCapacity() + " pkts, " + (this.stats.getSizePerPacket() * aprxBufferLengthInPkts) + " bytes.\n");
            this.tooMuchBufferingCount = 0;
        }
        return aprxBufferLengthInPkts;
    }

    public void reset() {
        super.reset();
        this.tooMuchBufferingCount = 0;
    }
}
