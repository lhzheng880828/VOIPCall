package org.jivesoftware.smackx.packet;

import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public abstract class PEPItem implements PacketExtension {
    String id;

    public abstract String getItemDetailsXML();

    public abstract String getNode();

    public PEPItem(String id) {
        this.id = id;
    }

    public String getElementName() {
        return "item";
    }

    public String getNamespace() {
        return "http://jabber.org/protocol/pubsub";
    }

    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append(Separators.LESS_THAN).append(getElementName()).append(" id=\"").append(this.id).append("\">");
        buf.append(getItemDetailsXML());
        buf.append("</").append(getElementName()).append(Separators.GREATER_THAN);
        return buf.toString();
    }
}
