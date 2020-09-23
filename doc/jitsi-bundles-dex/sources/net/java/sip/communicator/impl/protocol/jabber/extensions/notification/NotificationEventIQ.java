package net.java.sip.communicator.impl.protocol.jabber.extensions.notification;

import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.util.StringUtils;

public class NotificationEventIQ extends IQ {
    public static final String ELEMENT_NAME = "notification";
    static final String EVENT_NAME = "name";
    static final String EVENT_SOURCE = "source";
    static final String EVENT_VALUE = "value";
    public static final String NAMESPACE = "sip-communicator:iq:notification";
    private String eventName;
    private String eventSource;
    private String eventValue;

    public String getChildElementXML() {
        StringBuilder buf = new StringBuilder();
        buf.append(Separators.LESS_THAN).append(ELEMENT_NAME).append(" xmlns=\"").append(NAMESPACE).append("\">");
        buf.append(Separators.LESS_THAN).append("name").append(Separators.GREATER_THAN).append(StringUtils.escapeForXML(getEventName())).append("</").append("name").append(Separators.GREATER_THAN);
        buf.append(Separators.LESS_THAN).append("value").append(Separators.GREATER_THAN).append(StringUtils.escapeForXML(getEventValue())).append("</").append("value").append(Separators.GREATER_THAN);
        buf.append(Separators.LESS_THAN).append("source").append(Separators.GREATER_THAN).append(StringUtils.escapeForXML(getEventSource())).append("</").append("source").append(Separators.GREATER_THAN);
        buf.append("</").append(ELEMENT_NAME).append(Separators.GREATER_THAN);
        return buf.toString();
    }

    public String getEventName() {
        return this.eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventValue() {
        return this.eventValue;
    }

    public void setEventValue(String eventValue) {
        this.eventValue = eventValue;
    }

    public String getEventSource() {
        return this.eventSource;
    }

    public void setEventSource(String eventSource) {
        this.eventSource = eventSource;
    }
}
