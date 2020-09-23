package org.jivesoftware.smackx.pubsub.provider;

import java.util.List;
import java.util.Map;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.provider.EmbeddedExtensionProvider;
import org.jivesoftware.smackx.pubsub.AffiliationsExtension;

public class AffiliationsProvider extends EmbeddedExtensionProvider {
    /* access modifiers changed from: protected */
    public PacketExtension createReturnExtension(String currentElement, String currentNamespace, Map<String, String> map, List<? extends PacketExtension> content) {
        return new AffiliationsExtension(content);
    }
}
