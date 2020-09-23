package org.jivesoftware.smackx.pubsub;

import org.jitsi.gov.nist.core.Separators;

public class Item extends NodeExtension {
    private String id;

    public Item() {
        super(PubSubElementType.ITEM);
    }

    public Item(String itemId) {
        super(PubSubElementType.ITEM);
        this.id = itemId;
    }

    public Item(String itemId, String nodeId) {
        super(PubSubElementType.ITEM_EVENT, nodeId);
        this.id = itemId;
    }

    public String getId() {
        return this.id;
    }

    public String getNamespace() {
        return null;
    }

    public String toXML() {
        StringBuilder builder = new StringBuilder("<item");
        if (this.id != null) {
            builder.append(" id='");
            builder.append(this.id);
            builder.append(Separators.QUOTE);
        }
        if (getNode() != null) {
            builder.append(" node='");
            builder.append(getNode());
            builder.append(Separators.QUOTE);
        }
        builder.append("/>");
        return builder.toString();
    }

    public String toString() {
        return getClass().getName() + " | Content [" + toXML() + "]";
    }
}
