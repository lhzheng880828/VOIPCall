package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.provider.PacketExtensionProvider;

public class UserProvider implements PacketExtensionProvider {
    public UserPacketExtension parseExtension(XmlPullParser parser) throws Exception {
        boolean done = false;
        String entity = parser.getAttributeValue("", "entity");
        StateType state = StateType.full;
        String stateStr = parser.getAttributeValue("", "state");
        if (stateStr != null) {
            state = StateType.parseString(stateStr);
        }
        if (entity == null) {
            throw new Exception("Coin user element must contain entity attribute");
        }
        UserPacketExtension ext = new UserPacketExtension(entity);
        ext.setAttribute("state", state);
        while (!done) {
            int eventType = parser.next();
            String elementName = parser.getName();
            if (eventType == 2) {
                if (elementName.equals("display-text")) {
                    ext.setDisplayText(CoinIQProvider.parseText(parser));
                } else if (elementName.equals("endpoint")) {
                    ext.addChildExtension(new EndpointProvider().parseExtension(parser));
                }
            } else if (eventType == 3 && parser.getName().equals("user")) {
                done = true;
            }
        }
        return ext;
    }
}
