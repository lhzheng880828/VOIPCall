package org.jivesoftware.smackx.pubsub;

import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public class PayloadItem<E extends PacketExtension> extends Item {
    private E payload;

    public PayloadItem(E payloadExt) {
        if (payloadExt == null) {
            throw new IllegalArgumentException("payload cannot be 'null'");
        }
        this.payload = payloadExt;
    }

    public PayloadItem(String itemId, E payloadExt) {
        super(itemId);
        if (payloadExt == null) {
            throw new IllegalArgumentException("payload cannot be 'null'");
        }
        this.payload = payloadExt;
    }

    public PayloadItem(String itemId, String nodeId, E payloadExt) {
        super(itemId, nodeId);
        if (payloadExt == null) {
            throw new IllegalArgumentException("payload cannot be 'null'");
        }
        this.payload = payloadExt;
    }

    public E getPayload() {
        return this.payload;
    }

    public String toXML() {
        StringBuilder builder = new StringBuilder("<item");
        if (getId() != null) {
            builder.append(" id='");
            builder.append(getId());
            builder.append(Separators.QUOTE);
        }
        if (getNode() != null) {
            builder.append(" node='");
            builder.append(getNode());
            builder.append(Separators.QUOTE);
        }
        builder.append(Separators.GREATER_THAN);
        builder.append(this.payload.toXML());
        builder.append("</item>");
        return builder.toString();
    }

    public String toString() {
        return getClass().getName() + " | Content [" + toXML() + "]";
    }
}
