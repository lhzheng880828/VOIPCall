package javax.media.rtp.event;

import javax.media.rtp.ReceiveStream;
import javax.media.rtp.SessionManager;

public class RemotePayloadChangeEvent extends ReceiveStreamEvent {
    private int newpayload;
    private int oldpayload;

    public RemotePayloadChangeEvent(SessionManager from, ReceiveStream recvStream, int oldpayload, int newpayload) {
        super(from, recvStream, null);
        this.newpayload = newpayload;
        this.oldpayload = oldpayload;
    }

    public int getNewPayload() {
        return this.newpayload;
    }
}
