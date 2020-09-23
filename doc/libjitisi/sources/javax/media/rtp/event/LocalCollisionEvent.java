package javax.media.rtp.event;

import javax.media.rtp.ReceiveStream;
import javax.media.rtp.SessionManager;

public class LocalCollisionEvent extends SessionEvent {
    private long newSSRC;
    private ReceiveStream recvStream;

    public LocalCollisionEvent(SessionManager from, ReceiveStream recvStream, long newSSRC) {
        super(from);
        this.recvStream = recvStream;
        this.newSSRC = newSSRC;
    }

    public long getNewSSRC() {
        return this.newSSRC;
    }

    public ReceiveStream getReceiveStream() {
        return this.recvStream;
    }
}
