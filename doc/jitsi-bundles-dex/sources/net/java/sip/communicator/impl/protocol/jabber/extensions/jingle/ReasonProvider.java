package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;

public class ReasonProvider implements PacketExtensionProvider {
    public ReasonPacketExtension parseExtension(XmlPullParser parser) throws Exception {
        String text = null;
        Reason reason = null;
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            String elementName = parser.getName();
            if (eventType == 2) {
                if (reason == null) {
                    reason = Reason.parseString(elementName);
                } else if (elementName.equals("text")) {
                    text = parseText(parser);
                }
            } else if (eventType == 3 && parser.getName().equals("reason")) {
                done = true;
            }
        }
        return new ReasonPacketExtension(reason, text, (PacketExtension) null);
    }

    public String parseText(XmlPullParser parser) throws Exception {
        boolean done = false;
        String text = null;
        while (!done) {
            int eventType = parser.next();
            if (eventType == 4) {
                text = parser.getText();
            } else if (eventType == 3) {
                done = true;
            }
        }
        return text;
    }
}
