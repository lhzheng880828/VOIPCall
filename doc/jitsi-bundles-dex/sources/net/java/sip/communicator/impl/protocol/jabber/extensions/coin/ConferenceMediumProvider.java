package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;

public class ConferenceMediumProvider implements PacketExtensionProvider {
    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        boolean done = false;
        String label = parser.getAttributeValue("", "label");
        if (label == null) {
            throw new Exception("Coin medium element must contain entity attribute");
        }
        ConferenceMediumPacketExtension ext = new ConferenceMediumPacketExtension(UserRolesPacketExtension.ELEMENT_ROLE, label);
        while (!done) {
            int eventType = parser.next();
            String elementName = parser.getName();
            if (eventType == 2) {
                if (elementName.equals("display-text")) {
                    ext.setDisplayText(CoinIQProvider.parseText(parser));
                } else if (elementName.equals("status")) {
                    ext.setStatus(CoinIQProvider.parseText(parser));
                } else if (elementName.equals("type")) {
                    ext.setType(CoinIQProvider.parseText(parser));
                }
            } else if (eventType == 3 && parser.getName().equals(ConferenceMediumPacketExtension.ELEMENT_NAME)) {
                done = true;
            }
        }
        return ext;
    }
}
