package org.jivesoftware.smackx.pubsub;

import java.util.List;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public class ItemsExtension extends NodeExtension implements EmbeddedPacketExtension {
    protected List<? extends PacketExtension> items;
    protected Boolean notify;
    protected ItemsElementType type;

    public enum ItemsElementType {
        items(PubSubElementType.ITEMS, "max_items"),
        retract(PubSubElementType.RETRACT, "notify");
        
        private String att;
        private PubSubElementType elem;

        private ItemsElementType(PubSubElementType nodeElement, String attribute) {
            this.elem = nodeElement;
            this.att = attribute;
        }

        public PubSubElementType getNodeElement() {
            return this.elem;
        }

        public String getElementAttribute() {
            return this.att;
        }
    }

    public ItemsExtension(ItemsElementType itemsType, String nodeId, List<? extends PacketExtension> items) {
        super(itemsType.getNodeElement(), nodeId);
        this.type = itemsType;
        this.items = items;
    }

    public ItemsExtension(String nodeId, List<? extends PacketExtension> items, boolean notify) {
        super(ItemsElementType.retract.getNodeElement(), nodeId);
        this.type = ItemsElementType.retract;
        this.items = items;
        this.notify = Boolean.valueOf(notify);
    }

    public ItemsElementType getItemsElementType() {
        return this.type;
    }

    public List<PacketExtension> getExtensions() {
        return getItems();
    }

    public List<? extends PacketExtension> getItems() {
        return this.items;
    }

    public boolean getNotify() {
        return this.notify.booleanValue();
    }

    public String toXML() {
        if (this.items == null || this.items.size() == 0) {
            return super.toXML();
        }
        StringBuilder builder = new StringBuilder(Separators.LESS_THAN);
        builder.append(getElementName());
        builder.append(" node='");
        builder.append(getNode());
        if (this.notify != null) {
            builder.append("' ");
            builder.append(this.type.getElementAttribute());
            builder.append("='");
            builder.append(this.notify.equals(Boolean.TRUE) ? 1 : 0);
            builder.append("'>");
        } else {
            builder.append("'>");
            for (PacketExtension item : this.items) {
                builder.append(item.toXML());
            }
        }
        builder.append("</");
        builder.append(getElementName());
        builder.append(Separators.GREATER_THAN);
        return builder.toString();
    }

    public String toString() {
        return getClass().getName() + "Content [" + toXML() + "]";
    }
}
