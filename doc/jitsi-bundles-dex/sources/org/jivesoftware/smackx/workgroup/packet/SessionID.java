package org.jivesoftware.smackx.workgroup.packet;

import org.jitsi.gov.nist.core.Separators;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;

public class SessionID implements PacketExtension {
    public static final String ELEMENT_NAME = "session";
    public static final String NAMESPACE = "http://jivesoftware.com/protocol/workgroup";
    private String sessionID;

    public static class Provider implements PacketExtensionProvider {
        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            String sessionID = parser.getAttributeValue("", "id");
            parser.next();
            return new SessionID(sessionID);
        }
    }

    public SessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public String getSessionID() {
        return this.sessionID;
    }

    public String getElementName() {
        return "session";
    }

    public String getNamespace() {
        return "http://jivesoftware.com/protocol/workgroup";
    }

    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append(Separators.LESS_THAN).append("session").append(" xmlns=\"").append("http://jivesoftware.com/protocol/workgroup").append("\" ");
        buf.append("id=\"").append(getSessionID());
        buf.append("\"/>");
        return buf.toString();
    }
}
