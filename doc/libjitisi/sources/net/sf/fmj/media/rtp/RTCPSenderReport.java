package net.sf.fmj.media.rtp;

import java.io.IOException;
import javax.media.rtp.RTPStream;
import javax.media.rtp.rtcp.Feedback;
import javax.media.rtp.rtcp.SenderReport;

public class RTCPSenderReport extends RTCPReport implements SenderReport {
    RTCPSenderInfo senderInformation = null;
    private RTPStream stream = null;

    public RTCPSenderReport(byte[] data, int offset, int length) throws IOException {
        super(data, offset, length);
        this.senderInformation = new RTCPSenderInfo(data, offset + 8, length - 8);
        readFeedbackReports(data, (offset + 8) + 20, (length - 8) - 20);
        offset += (this.header.getLength() + 1) * 4;
        length -= (this.header.getLength() + 1) * 4;
        readSourceDescription(data, offset, length);
        readBye(data, offset + this.sdesBytes, length - this.sdesBytes);
    }

    public long getNTPTimeStampLSW() {
        return this.senderInformation.getNtpTimestampLSW();
    }

    public long getNTPTimeStampMSW() {
        return this.senderInformation.getNtpTimestampMSW();
    }

    public long getRTPTimeStamp() {
        return this.senderInformation.getTimestamp();
    }

    public long getSenderByteCount() {
        return this.senderInformation.getOctetCount();
    }

    public Feedback getSenderFeedback() {
        return null;
    }

    public long getSenderPacketCount() {
        return this.senderInformation.getPacketCount();
    }

    public RTPStream getStream() {
        return this.stream;
    }

    /* access modifiers changed from: protected */
    public void setStream(RTPStream stream) {
        this.stream = stream;
    }
}
