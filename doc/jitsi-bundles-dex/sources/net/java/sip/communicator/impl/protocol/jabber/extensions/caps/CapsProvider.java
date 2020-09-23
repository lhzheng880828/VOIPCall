package net.java.sip.communicator.impl.protocol.jabber.extensions.caps;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;

public class CapsProvider implements PacketExtensionProvider {
    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        boolean done = false;
        String ext = null;
        String hash = null;
        String version = null;
        String node = null;
        while (!done) {
            if (parser.getEventType() == 2 && parser.getName().equalsIgnoreCase(CapsPacketExtension.ELEMENT_NAME)) {
                ext = parser.getAttributeValue(null, "ext");
                hash = parser.getAttributeValue(null, "hash");
                version = parser.getAttributeValue(null, "ver");
                node = parser.getAttributeValue(null, "node");
            }
            if (parser.getEventType() == 3 && parser.getName().equalsIgnoreCase(CapsPacketExtension.ELEMENT_NAME)) {
                done = true;
            } else {
                parser.next();
            }
        }
        return new CapsPacketExtension(ext, node, hash, version);
    }
}
