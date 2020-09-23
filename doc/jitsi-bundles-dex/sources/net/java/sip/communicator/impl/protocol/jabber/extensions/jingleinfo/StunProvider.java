package net.java.sip.communicator.impl.protocol.jabber.extensions.jingleinfo;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;

public class StunProvider implements PacketExtensionProvider {
    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        boolean done = false;
        StunPacketExtension ext = new StunPacketExtension();
        while (!done) {
            int eventType = parser.next();
            String elementName = parser.getName();
            if (eventType == 2) {
                if (elementName.equals(ServerPacketExtension.ELEMENT_NAME)) {
                    ext.addChildExtension(((PacketExtensionProvider) ProviderManager.getInstance().getExtensionProvider(ServerPacketExtension.ELEMENT_NAME, ServerPacketExtension.NAMESPACE)).parseExtension(parser));
                }
            } else if (eventType == 3 && parser.getName().equals(StunPacketExtension.ELEMENT_NAME)) {
                done = true;
            }
        }
        return ext;
    }
}
