package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.provider.PacketExtensionProvider;

public class DescriptionProvider implements PacketExtensionProvider {
    public DescriptionPacketExtension parseExtension(XmlPullParser parser) throws Exception {
        boolean done = false;
        DescriptionPacketExtension ext = new DescriptionPacketExtension();
        while (!done) {
            int eventType = parser.next();
            String elementName = parser.getName();
            if (eventType == 2) {
                if (elementName.equals("subject")) {
                    ext.setSubject(CoinIQProvider.parseText(parser));
                } else if (elementName.equals(DescriptionPacketExtension.ELEMENT_FREE_TEXT)) {
                    ext.setFreeText(CoinIQProvider.parseText(parser));
                } else if (elementName.equals("display-text")) {
                    ext.setDisplayText(CoinIQProvider.parseText(parser));
                }
            } else if (eventType == 3 && parser.getName().equals(DescriptionPacketExtension.ELEMENT_NAME)) {
                done = true;
            }
        }
        return ext;
    }
}
