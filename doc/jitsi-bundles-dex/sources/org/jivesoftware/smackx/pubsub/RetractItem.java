package org.jivesoftware.smackx.pubsub;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.pubsub.packet.PubSubNamespace;

public class RetractItem implements PacketExtension {
    private String id;

    public RetractItem(String itemId) {
        if (itemId == null) {
            throw new IllegalArgumentException("itemId must not be 'null'");
        }
        this.id = itemId;
    }

    public String getId() {
        return this.id;
    }

    public String getElementName() {
        return "retract";
    }

    public String getNamespace() {
        return PubSubNamespace.EVENT.getXmlns();
    }

    public String toXML() {
        return "<retract id='" + this.id + "'/>";
    }
}
