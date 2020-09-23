package org.jivesoftware.smackx.pubsub;

import java.util.Arrays;
import java.util.List;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.pubsub.packet.PubSubNamespace;

public class EventElement implements EmbeddedPacketExtension {
    private NodeExtension ext;
    private EventElementType type;

    public EventElement(EventElementType eventType, NodeExtension eventExt) {
        this.type = eventType;
        this.ext = eventExt;
    }

    public EventElementType getEventType() {
        return this.type;
    }

    public List<PacketExtension> getExtensions() {
        return Arrays.asList(new PacketExtension[]{getEvent()});
    }

    public NodeExtension getEvent() {
        return this.ext;
    }

    public String getElementName() {
        return "event";
    }

    public String getNamespace() {
        return PubSubNamespace.EVENT.getXmlns();
    }

    public String toXML() {
        StringBuilder builder = new StringBuilder("<event xmlns='" + PubSubNamespace.EVENT.getXmlns() + "'>");
        builder.append(this.ext.toXML());
        builder.append("</event>");
        return builder.toString();
    }
}
