package javax.media.rtp.event;

import javax.media.rtp.SessionManager;

public class SessionEvent extends RTPEvent {
    public SessionEvent(SessionManager from) {
        super(from);
    }
}
