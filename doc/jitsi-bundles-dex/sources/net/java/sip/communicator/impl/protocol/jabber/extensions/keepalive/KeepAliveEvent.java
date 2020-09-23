package net.java.sip.communicator.impl.protocol.jabber.extensions.keepalive;

import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;

public class KeepAliveEvent extends IQ {
    public static final String ELEMENT_NAME = "ping";
    public static final String NAMESPACE = "urn:xmpp:ping";

    public KeepAliveEvent(String from, String to) {
        if (to == null) {
            throw new IllegalArgumentException("Parameter cannot be null");
        }
        setType(Type.GET);
        setTo(to);
        setFrom(from);
    }

    public String getChildElementXML() {
        StringBuffer buf = new StringBuffer();
        buf.append(Separators.LESS_THAN).append(ELEMENT_NAME).append(" xmlns=\"").append(NAMESPACE).append("\"/>");
        return buf.toString();
    }
}
