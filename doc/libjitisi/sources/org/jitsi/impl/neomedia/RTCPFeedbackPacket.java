package org.jitsi.impl.neomedia;

import javax.media.rtp.OutputDataStream;

public class RTCPFeedbackPacket {
    private int fmt = 0;
    private int payloadType = 0;
    private final long senderSSRC;
    private final long sourceSSRC;

    public RTCPFeedbackPacket(int type, int payloadType, long sender, long src) {
        this.fmt = type;
        this.payloadType = payloadType;
        this.senderSSRC = sender;
        this.sourceSSRC = src;
    }

    public void writeTo(OutputDataStream out) {
        out.write(new byte[]{(byte) ((this.fmt & 31) | 128), (byte) this.payloadType, (byte) 0, (byte) 2, (byte) ((int) (this.senderSSRC >> 24)), (byte) ((int) ((this.senderSSRC >> 16) & 255)), (byte) ((int) ((this.senderSSRC >> 8) & 255)), (byte) ((int) (this.senderSSRC & 255)), (byte) ((int) (this.sourceSSRC >> 24)), (byte) ((int) ((this.sourceSSRC >> 16) & 255)), (byte) ((int) ((this.sourceSSRC >> 8) & 255)), (byte) ((int) (this.sourceSSRC & 255))}, 0, 12);
    }
}
