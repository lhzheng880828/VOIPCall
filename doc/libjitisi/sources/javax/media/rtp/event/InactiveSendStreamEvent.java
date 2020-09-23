package javax.media.rtp.event;

import javax.media.rtp.Participant;
import javax.media.rtp.SendStream;
import javax.media.rtp.SessionManager;

public class InactiveSendStreamEvent extends SendStreamEvent {
    public InactiveSendStreamEvent(SessionManager from, Participant participant, SendStream sendStream) {
        super(from, sendStream, participant);
    }
}
