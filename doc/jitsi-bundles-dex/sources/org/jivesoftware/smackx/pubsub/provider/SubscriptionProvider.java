package org.jivesoftware.smackx.pubsub.provider;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.Subscription.State;

public class SubscriptionProvider implements PacketExtensionProvider {
    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        State state = null;
        String jid = parser.getAttributeValue(null, "jid");
        String nodeId = parser.getAttributeValue(null, "node");
        String subId = parser.getAttributeValue(null, "subid");
        String state2 = parser.getAttributeValue(null, "subscription");
        boolean isRequired = false;
        if (parser.next() == 2 && parser.getName().equals("subscribe-options")) {
            if (parser.next() == 2 && parser.getName().equals("required")) {
                isRequired = true;
            }
            while (parser.next() != 3) {
                if (parser.getName() == "subscribe-options") {
                    break;
                }
            }
        }
        while (parser.getEventType() != 3) {
            parser.next();
        }
        if (state2 != null) {
            state = State.valueOf(state2);
        }
        return new Subscription(jid, nodeId, subId, state, isRequired);
    }
}
