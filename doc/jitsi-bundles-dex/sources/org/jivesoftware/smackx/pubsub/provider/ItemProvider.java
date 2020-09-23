package org.jivesoftware.smackx.pubsub.provider;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.SimplePayload;

public class ItemProvider implements PacketExtensionProvider {
    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        String id = parser.getAttributeValue(null, "id");
        String node = parser.getAttributeValue(null, "node");
        String elem = parser.getName();
        int tag = parser.next();
        if (tag == 3) {
            return new Item(id, node);
        }
        String payloadElemName = parser.getName();
        String payloadNS = parser.getNamespace();
        if (ProviderManager.getInstance().getExtensionProvider(payloadElemName, payloadNS) != null) {
            return new PayloadItem(id, node, PacketParserUtils.parsePacketExtension(payloadElemName, payloadNS, parser));
        }
        boolean done = false;
        StringBuilder payloadText = new StringBuilder();
        while (!done) {
            if (tag == 3 && parser.getName().equals(elem)) {
                done = true;
            } else if (!(tag == 2 && parser.isEmptyElementTag())) {
                payloadText.append(parser.getText());
            }
            if (!done) {
                tag = parser.next();
            }
        }
        return new PayloadItem(id, node, new SimplePayload(payloadElemName, payloadNS, payloadText.toString()));
    }
}
