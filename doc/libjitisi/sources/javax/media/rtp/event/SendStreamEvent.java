package javax.media.rtp.event;

import javax.media.rtp.Participant;
import javax.media.rtp.SendStream;
import javax.media.rtp.SessionManager;

public class SendStreamEvent extends RTPEvent {
    private Participant participant;
    private SendStream sendStream;

    public SendStreamEvent(SessionManager from, SendStream stream, Participant participant) {
        super(from);
        this.sendStream = stream;
        this.participant = participant;
    }

    public Participant getParticipant() {
        return this.participant;
    }

    public SendStream getSendStream() {
        return this.sendStream;
    }
}
