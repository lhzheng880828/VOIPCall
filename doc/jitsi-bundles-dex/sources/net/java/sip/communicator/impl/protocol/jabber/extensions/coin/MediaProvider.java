package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.provider.PacketExtensionProvider;

public class MediaProvider implements PacketExtensionProvider {
    public MediaPacketExtension parseExtension(XmlPullParser parser) throws Exception {
        boolean done = false;
        String id = parser.getAttributeValue("", "id");
        if (id == null) {
            throw new Exception("Coin media must contains src-id element");
        }
        MediaPacketExtension ext = new MediaPacketExtension(id);
        while (!done) {
            int eventType = parser.next();
            String elementName = parser.getName();
            if (eventType == 2) {
                if (elementName.equals("display-text")) {
                    ext.setDisplayText(CoinIQProvider.parseText(parser));
                } else if (elementName.equals("label")) {
                    ext.setLabel(CoinIQProvider.parseText(parser));
                } else if (elementName.equals(MediaPacketExtension.ELEMENT_SRC_ID)) {
                    ext.setSrcID(CoinIQProvider.parseText(parser));
                } else if (elementName.equals("status")) {
                    ext.setStatus(CoinIQProvider.parseText(parser));
                } else if (elementName.equals("type")) {
                    ext.setType(CoinIQProvider.parseText(parser));
                }
            } else if (eventType == 3 && parser.getName().equals("media")) {
                done = true;
            }
        }
        return ext;
    }
}
