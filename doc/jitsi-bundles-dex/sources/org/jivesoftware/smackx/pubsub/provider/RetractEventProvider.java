package org.jivesoftware.smackx.pubsub.provider;

import java.util.List;
import java.util.Map;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.provider.EmbeddedExtensionProvider;
import org.jivesoftware.smackx.pubsub.RetractItem;

public class RetractEventProvider extends EmbeddedExtensionProvider {
    /* access modifiers changed from: protected */
    public PacketExtension createReturnExtension(String currentElement, String currentNamespace, Map<String, String> attributeMap, List<? extends PacketExtension> list) {
        return new RetractItem((String) attributeMap.get("id"));
    }
}
