package org.jivesoftware.smackx.pubsub;

import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public class Affiliation implements PacketExtension {
    protected String node;
    protected Type type;

    public enum Type {
        member,
        none,
        outcast,
        owner,
        publisher
    }

    public Affiliation(String nodeId, Type affiliation) {
        this.node = nodeId;
        this.type = affiliation;
    }

    public String getNodeId() {
        return this.node;
    }

    public Type getType() {
        return this.type;
    }

    public String getElementName() {
        return "subscription";
    }

    public String getNamespace() {
        return null;
    }

    public String toXML() {
        StringBuilder builder = new StringBuilder(Separators.LESS_THAN);
        builder.append(getElementName());
        appendAttribute(builder, "node", this.node);
        appendAttribute(builder, "affiliation", this.type.toString());
        builder.append("/>");
        return builder.toString();
    }

    private void appendAttribute(StringBuilder builder, String att, String value) {
        builder.append(Separators.SP);
        builder.append(att);
        builder.append("='");
        builder.append(value);
        builder.append(Separators.QUOTE);
    }
}
