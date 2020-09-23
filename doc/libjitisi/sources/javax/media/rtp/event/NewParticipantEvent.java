package javax.media.rtp.event;

import javax.media.rtp.Participant;
import javax.media.rtp.SessionManager;

public class NewParticipantEvent extends SessionEvent {
    private Participant participant;

    public NewParticipantEvent(SessionManager from, Participant participant) {
        super(from);
        this.participant = participant;
    }

    public Participant getParticipant() {
        return this.participant;
    }
}
