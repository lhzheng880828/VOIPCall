package javax.media.rtp.event;

import javax.media.rtp.Participant;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.SessionManager;

public class ActiveReceiveStreamEvent extends ReceiveStreamEvent {
    public ActiveReceiveStreamEvent(SessionManager from, Participant participant, ReceiveStream stream) {
        super(from, stream, participant);
    }
}
