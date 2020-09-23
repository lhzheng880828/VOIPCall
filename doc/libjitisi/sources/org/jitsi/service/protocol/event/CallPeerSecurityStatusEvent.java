package org.jitsi.service.protocol.event;

import java.util.EventObject;

public abstract class CallPeerSecurityStatusEvent extends EventObject {
    public static final int AUDIO_SESSION = 1;
    public static final int VIDEO_SESSION = 2;
    private static final long serialVersionUID = 0;
    private final int sessionType;

    public CallPeerSecurityStatusEvent(Object source, int sessionType) {
        super(source);
        this.sessionType = sessionType;
    }

    public int getSessionType() {
        return this.sessionType;
    }
}
