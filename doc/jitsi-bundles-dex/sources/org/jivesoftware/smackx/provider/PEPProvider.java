package org.jivesoftware.smackx.provider;

import java.util.HashMap;
import java.util.Map;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;

public class PEPProvider implements PacketExtensionProvider {
    Map<String, PacketExtensionProvider> nodeParsers = new HashMap();
    PacketExtension pepItem;

    public void registerPEPParserExtension(String node, PacketExtensionProvider pepItemParser) {
        this.nodeParsers.put(node, pepItemParser);
    }

    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            if (eventType == 2) {
                if (!parser.getName().equals("event") && parser.getName().equals("items")) {
                    PacketExtensionProvider nodeParser = (PacketExtensionProvider) this.nodeParsers.get(parser.getAttributeValue("", "node"));
                    if (nodeParser != null) {
                        this.pepItem = nodeParser.parseExtension(parser);
                    }
                }
            } else if (eventType == 3 && parser.getName().equals("event")) {
                done = true;
            }
        }
        return this.pepItem;
    }
}
