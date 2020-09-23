package org.jivesoftware.smackx.workgroup.packet;

import org.jitsi.gov.nist.core.Separators;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;

public class UserID implements PacketExtension {
    public static final String ELEMENT_NAME = "user";
    public static final String NAMESPACE = "http://jivesoftware.com/protocol/workgroup";
    private String userID;

    public static class Provider implements PacketExtensionProvider {
        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            String userID = parser.getAttributeValue("", "id");
            parser.next();
            return new UserID(userID);
        }
    }

    public UserID(String userID) {
        this.userID = userID;
    }

    public String getUserID() {
        return this.userID;
    }

    public String getElementName() {
        return "user";
    }

    public String getNamespace() {
        return "http://jivesoftware.com/protocol/workgroup";
    }

    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append(Separators.LESS_THAN).append("user").append(" xmlns=\"").append("http://jivesoftware.com/protocol/workgroup").append("\" ");
        buf.append("id=\"").append(getUserID());
        buf.append("\"/>");
        return buf.toString();
    }
}
