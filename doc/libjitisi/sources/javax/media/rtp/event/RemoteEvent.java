package javax.media.rtp.event;

import javax.media.rtp.SessionManager;

public class RemoteEvent extends RTPEvent {
    public RemoteEvent(SessionManager from) {
        super(from);
    }
}
