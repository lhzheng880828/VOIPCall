package net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;

public class WhiteboardObjectJabberProvider implements PacketExtensionProvider {
    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        PacketExtension extension = null;
        StringBuilder sb = new StringBuilder();
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            if (eventType == 2 && !parser.getName().equals(WhiteboardObjectPacketExtension.ELEMENT_NAME) && !parser.getName().equals(WhiteboardSessionPacketExtension.ELEMENT_NAME)) {
                sb.append(parser.getText());
            } else if (eventType == 4) {
                sb.append(parser.getText());
            } else if (eventType == 3 && parser.getName().equals("image")) {
                sb.append(parser.getText());
            } else if (eventType == 3 && parser.getName().equals("text")) {
                sb.append(parser.getText());
            } else if (eventType == 3 && parser.getName().equals(WhiteboardObjectPacketExtension.ELEMENT_NAME)) {
                extension = new WhiteboardObjectPacketExtension(sb.toString());
                done = true;
            } else if (eventType == 3 && parser.getName().equals(WhiteboardSessionPacketExtension.ELEMENT_NAME)) {
                extension = new WhiteboardSessionPacketExtension(sb.toString());
                done = true;
            }
        }
        return extension;
    }
}
