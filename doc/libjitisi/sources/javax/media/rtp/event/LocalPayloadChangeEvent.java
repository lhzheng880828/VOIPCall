package javax.media.rtp.event;

import javax.media.rtp.SendStream;
import javax.media.rtp.SessionManager;

public class LocalPayloadChangeEvent extends SendStreamEvent {
    private int newpayload;
    private int oldpayload;

    public LocalPayloadChangeEvent(SessionManager from, SendStream sendStream, int oldpayload, int newpayload) {
        super(from, sendStream, null);
        this.newpayload = newpayload;
        this.oldpayload = oldpayload;
    }

    public int getNewPayload() {
        return this.newpayload;
    }
}
