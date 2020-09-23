package javax.media.rtp.event;

import javax.media.MediaEvent;
import javax.media.rtp.SessionManager;

public class RTPEvent extends MediaEvent {
    private SessionManager eventSrc;

    public RTPEvent(SessionManager from) {
        super(from);
        this.eventSrc = from;
    }

    public SessionManager getSessionManager() {
        return this.eventSrc;
    }

    public Object getSource() {
        return this.eventSrc;
    }

    public String toString() {
        return getClass().getName() + "[source = " + this.eventSrc + "]";
    }
}
