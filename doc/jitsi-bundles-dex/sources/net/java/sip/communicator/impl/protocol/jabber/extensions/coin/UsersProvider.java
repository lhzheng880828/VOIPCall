package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.provider.PacketExtensionProvider;

public class UsersProvider implements PacketExtensionProvider {
    public UsersPacketExtension parseExtension(XmlPullParser parser) throws Exception {
        boolean done = false;
        StateType state = StateType.full;
        String stateStr = parser.getAttributeValue("", "state");
        if (stateStr != null) {
            state = StateType.parseString(stateStr);
        }
        UsersPacketExtension ext = new UsersPacketExtension();
        ext.setAttribute("state", state);
        while (!done) {
            int eventType = parser.next();
            String elementName = parser.getName();
            if (eventType == 2) {
                if (elementName.equals("user")) {
                    ext.addChildExtension(new UserProvider().parseExtension(parser));
                }
            } else if (eventType == 3 && parser.getName().equals(UsersPacketExtension.ELEMENT_NAME)) {
                done = true;
            }
        }
        return ext;
    }
}
