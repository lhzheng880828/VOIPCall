package org.jivesoftware.smackx.provider;

import java.util.List;
import java.util.Map;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.packet.HeadersExtension;

public class HeadersProvider extends EmbeddedExtensionProvider {
    /* access modifiers changed from: protected */
    public PacketExtension createReturnExtension(String currentElement, String currentNamespace, Map<String, String> map, List<? extends PacketExtension> content) {
        return new HeadersExtension(content);
    }
}
