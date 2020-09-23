package org.jivesoftware.smackx.provider;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smackx.packet.MessageEvent;

public class MessageEventProvider implements PacketExtensionProvider {
    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        MessageEvent messageEvent = new MessageEvent();
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            if (eventType == 2) {
                if (parser.getName().equals("id")) {
                    messageEvent.setPacketID(parser.nextText());
                }
                if (parser.getName().equals(MessageEvent.COMPOSING)) {
                    messageEvent.setComposing(true);
                }
                if (parser.getName().equals(MessageEvent.DELIVERED)) {
                    messageEvent.setDelivered(true);
                }
                if (parser.getName().equals(MessageEvent.DISPLAYED)) {
                    messageEvent.setDisplayed(true);
                }
                if (parser.getName().equals(MessageEvent.OFFLINE)) {
                    messageEvent.setOffline(true);
                }
            } else if (eventType == 3 && parser.getName().equals("x")) {
                done = true;
            }
        }
        return messageEvent;
    }
}
