package org.jitsi.gov.nist.javax.sip.header;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.header.EventHeader;

public class Event extends ParametersHeader implements EventHeader {
    private static final long serialVersionUID = -6458387810431874841L;
    protected String eventType;

    public Event() {
        super("Event");
    }

    public void setEventType(String eventType) throws ParseException {
        if (eventType == null) {
            throw new NullPointerException(" the eventType is null");
        }
        this.eventType = eventType;
    }

    public String getEventType() {
        return this.eventType;
    }

    public void setEventId(String eventId) throws ParseException {
        if (eventId == null) {
            throw new NullPointerException(" the eventId parameter is null");
        }
        setParameter("id", eventId);
    }

    public String getEventId() {
        return getParameter("id");
    }

    public String encodeBody() {
        return encodeBody(new StringBuilder()).toString();
    }

    /* access modifiers changed from: protected */
    public StringBuilder encodeBody(StringBuilder buffer) {
        if (this.eventType != null) {
            buffer.append(this.eventType);
        }
        if (!this.parameters.isEmpty()) {
            buffer.append(Separators.SEMICOLON);
            this.parameters.encode(buffer);
        }
        return buffer;
    }

    public boolean match(Event matchTarget) {
        if (matchTarget.eventType == null && this.eventType != null) {
            return false;
        }
        if (matchTarget.eventType != null && this.eventType == null) {
            return false;
        }
        if (this.eventType == null && matchTarget.eventType == null) {
            return false;
        }
        if (getEventId() == null && matchTarget.getEventId() != null) {
            return false;
        }
        if ((getEventId() != null && matchTarget.getEventId() == null) || !matchTarget.eventType.equalsIgnoreCase(this.eventType)) {
            return false;
        }
        if (getEventId() == matchTarget.getEventId() || getEventId().equalsIgnoreCase(matchTarget.getEventId())) {
            return true;
        }
        return false;
    }
}
