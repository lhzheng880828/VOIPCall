package org.jivesoftware.smackx.pubsub.provider;

import java.util.List;
import java.util.Map;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.provider.EmbeddedExtensionProvider;
import org.jivesoftware.smackx.pubsub.SubscriptionsExtension;

public class SubscriptionsProvider extends EmbeddedExtensionProvider {
    /* access modifiers changed from: protected */
    public PacketExtension createReturnExtension(String currentElement, String currentNamespace, Map<String, String> attributeMap, List<? extends PacketExtension> content) {
        return new SubscriptionsExtension((String) attributeMap.get("node"), content);
    }
}
