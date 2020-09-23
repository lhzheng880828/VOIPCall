package org.jitsi.service.protocol.event;

import java.util.EventObject;

public class CallPeerSecurityMessageEvent extends EventObject {
    public static final int ERROR = 3;
    public static final int INFORMATION = 0;
    public static final int SEVERE = 2;
    public static final int WARNING = 1;
    private static final long serialVersionUID = 0;
    private final String eventI18nMessage;
    private final String eventMessage;
    private final int eventSeverity;

    public CallPeerSecurityMessageEvent(Object source, String eventMessage, String i18nMessage, int eventSeverity) {
        super(source);
        this.eventMessage = eventMessage;
        this.eventI18nMessage = i18nMessage;
        this.eventSeverity = eventSeverity;
    }

    public String getMessage() {
        return this.eventMessage;
    }

    public String getI18nMessage() {
        return this.eventI18nMessage;
    }

    public int getEventSeverity() {
        return this.eventSeverity;
    }
}
