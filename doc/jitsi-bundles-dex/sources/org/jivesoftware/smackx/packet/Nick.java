package org.jivesoftware.smackx.packet;

import org.jitsi.gov.nist.core.Separators;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;

public class Nick implements PacketExtension {
    public static final String ELEMENT_NAME = "nick";
    public static final String NAMESPACE = "http://jabber.org/protocol/nick";
    private String name = null;

    public static class Provider implements PacketExtensionProvider {
        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            parser.next();
            String name = parser.getText();
            while (parser.getEventType() != 3) {
                parser.next();
            }
            return new Nick(name);
        }
    }

    public Nick(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getElementName() {
        return ELEMENT_NAME;
    }

    public String getNamespace() {
        return NAMESPACE;
    }

    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append(Separators.LESS_THAN).append(ELEMENT_NAME).append(" xmlns=\"").append(NAMESPACE).append("\">");
        buf.append(getName());
        buf.append("</").append(ELEMENT_NAME).append('>');
        return buf.toString();
    }
}
