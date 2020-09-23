package javax.media.rtp.event;

import javax.media.rtp.Participant;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.SessionManager;

public class ReceiveStreamEvent extends RTPEvent {
    private Participant participant;
    private ReceiveStream recvStream;

    public ReceiveStreamEvent(SessionManager from, ReceiveStream stream, Participant participant) {
        super(from);
        this.recvStream = stream;
        this.participant = participant;
    }

    public Participant getParticipant() {
        return this.participant;
    }

    public ReceiveStream getReceiveStream() {
        return this.recvStream;
    }
}
