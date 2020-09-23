package org.jivesoftware.smackx.pubsub;

import org.jivesoftware.smack.packet.PacketExtension;

public class NodeExtension implements PacketExtension {
    private PubSubElementType element;
    private String node;

    public NodeExtension(PubSubElementType elem, String nodeId) {
        this.element = elem;
        this.node = nodeId;
    }

    public NodeExtension(PubSubElementType elem) {
        this(elem, null);
    }

    public String getNode() {
        return this.node;
    }

    public String getElementName() {
        return this.element.getElementName();
    }

    public String getNamespace() {
        return this.element.getNamespace().getXmlns();
    }

    public String toXML() {
        return '<' + getElementName() + (this.node == null ? "" : " node='" + this.node + '\'') + "/>";
    }

    public String toString() {
        return getClass().getName() + " - content [" + toXML() + "]";
    }
}
