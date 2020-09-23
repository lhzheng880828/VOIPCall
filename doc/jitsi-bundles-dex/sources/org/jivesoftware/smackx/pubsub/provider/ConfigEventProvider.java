package org.jivesoftware.smackx.pubsub.provider;

import java.util.List;
import java.util.Map;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.packet.DataForm;
import org.jivesoftware.smackx.provider.EmbeddedExtensionProvider;
import org.jivesoftware.smackx.pubsub.ConfigurationEvent;
import org.jivesoftware.smackx.pubsub.ConfigureForm;

public class ConfigEventProvider extends EmbeddedExtensionProvider {
    /* access modifiers changed from: protected */
    public PacketExtension createReturnExtension(String currentElement, String currentNamespace, Map<String, String> attMap, List<? extends PacketExtension> content) {
        if (content.size() == 0) {
            return new ConfigurationEvent((String) attMap.get("node"));
        }
        return new ConfigurationEvent((String) attMap.get("node"), new ConfigureForm((DataForm) content.iterator().next()));
    }
}
