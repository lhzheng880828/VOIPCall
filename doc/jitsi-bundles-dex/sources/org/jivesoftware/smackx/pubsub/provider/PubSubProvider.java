package org.jivesoftware.smackx.pubsub.provider;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.jivesoftware.smackx.pubsub.packet.PubSub;
import org.jivesoftware.smackx.pubsub.packet.PubSubNamespace;

public class PubSubProvider implements IQProvider {
    public IQ parseIQ(XmlPullParser parser) throws Exception {
        PubSub pubsub = new PubSub();
        String namespace = parser.getNamespace();
        pubsub.setPubSubNamespace(PubSubNamespace.valueOfFromXmlns(namespace));
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            if (eventType == 2) {
                PacketExtension ext = PacketParserUtils.parsePacketExtension(parser.getName(), namespace, parser);
                if (ext != null) {
                    pubsub.addExtension(ext);
                }
            } else if (eventType == 3 && parser.getName().equals("pubsub")) {
                done = true;
            }
        }
        return pubsub;
    }
}
