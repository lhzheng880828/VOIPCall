package org.jivesoftware.smackx.packet;

import org.jitsi.gov.nist.core.Separators;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;

public class AttentionExtension implements PacketExtension {
    public static final String ELEMENT_NAME = "attention";
    public static final String NAMESPACE = "urn:xmpp:attention:0";

    public static class Provider implements PacketExtensionProvider {
        public PacketExtension parseExtension(XmlPullParser arg0) throws Exception {
            return new AttentionExtension();
        }
    }

    public String getElementName() {
        return ELEMENT_NAME;
    }

    public String getNamespace() {
        return NAMESPACE;
    }

    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append(Separators.LESS_THAN).append(getElementName()).append(" xmlns=\"").append(getNamespace()).append("\"/>");
        return sb.toString();
    }
}
