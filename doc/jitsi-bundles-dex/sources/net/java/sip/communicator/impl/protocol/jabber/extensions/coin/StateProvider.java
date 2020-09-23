package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.provider.PacketExtensionProvider;

public class StateProvider implements PacketExtensionProvider {
    public StatePacketExtension parseExtension(XmlPullParser parser) throws Exception {
        boolean done = false;
        StatePacketExtension ext = new StatePacketExtension();
        while (!done) {
            int eventType = parser.next();
            String elementName = parser.getName();
            if (eventType == 2) {
                if (elementName.equals("active")) {
                    ext.setActive(Boolean.parseBoolean(CoinIQProvider.parseText(parser)) ? 1 : 0);
                } else if (elementName.equals(StatePacketExtension.ELEMENT_LOCKED)) {
                    ext.setLocked(Boolean.parseBoolean(CoinIQProvider.parseText(parser)) ? 1 : 0);
                }
                if (elementName.equals(StatePacketExtension.ELEMENT_USER_COUNT)) {
                    ext.setUserCount(Integer.parseInt(CoinIQProvider.parseText(parser)));
                }
            } else if (eventType == 3 && parser.getName().equals(StatePacketExtension.ELEMENT_NAME)) {
                done = true;
            }
        }
        return ext;
    }
}
