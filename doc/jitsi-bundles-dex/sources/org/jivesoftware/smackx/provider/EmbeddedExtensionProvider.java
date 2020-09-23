package org.jivesoftware.smackx.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.util.PacketParserUtils;

public abstract class EmbeddedExtensionProvider implements PacketExtensionProvider {
    public abstract PacketExtension createReturnExtension(String str, String str2, Map<String, String> map, List<? extends PacketExtension> list);

    public final PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        String namespace = parser.getNamespace();
        String name = parser.getName();
        Map<String, String> attMap = new HashMap();
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            attMap.put(parser.getAttributeName(i), parser.getAttributeValue(i));
        }
        List<PacketExtension> extensions = new ArrayList();
        do {
            if (parser.next() == 2) {
                extensions.add(PacketParserUtils.parsePacketExtension(parser.getName(), parser.getNamespace(), parser));
            }
        } while (!name.equals(parser.getName()));
        return createReturnExtension(name, namespace, attMap, extensions);
    }
}
