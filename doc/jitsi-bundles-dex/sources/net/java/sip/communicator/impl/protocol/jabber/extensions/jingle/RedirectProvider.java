package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.provider.PacketExtensionProvider;

public class RedirectProvider implements PacketExtensionProvider {
    public RedirectPacketExtension parseExtension(XmlPullParser parser) throws Exception {
        boolean done = false;
        String text = parseText(parser);
        while (!done) {
            int eventType = parser.next();
            if (eventType != 2 && eventType == 3 && parser.getName().equals("redirect")) {
                done = true;
            }
        }
        RedirectPacketExtension redirectExt = new RedirectPacketExtension();
        redirectExt.setText(text);
        redirectExt.setRedir(text);
        return redirectExt;
    }

    public String parseText(XmlPullParser parser) throws Exception {
        boolean done = false;
        String text = null;
        while (!done) {
            int eventType = parser.next();
            if (eventType == 4) {
                text = parser.getText();
                done = true;
            } else if (eventType == 3) {
                done = true;
            }
        }
        return text;
    }
}
