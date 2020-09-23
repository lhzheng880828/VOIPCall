package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;

public class UserLanguagesProvider implements PacketExtensionProvider {
    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        boolean done = false;
        UserLanguagesPacketExtension ext = new UserLanguagesPacketExtension();
        while (!done) {
            int eventType = parser.next();
            String elementName = parser.getName();
            if (eventType == 2) {
                if (elementName.equals(UserLanguagesPacketExtension.ELEMENT_LANGUAGES)) {
                    ext.setLanguages(CoinIQProvider.parseText(parser));
                }
            } else if (eventType == 3 && parser.getName().equals(UserLanguagesPacketExtension.ELEMENT_NAME)) {
                done = true;
            }
        }
        return ext;
    }
}
