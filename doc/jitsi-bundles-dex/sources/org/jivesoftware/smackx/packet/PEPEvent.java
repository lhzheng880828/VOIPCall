package org.jivesoftware.smackx.packet;

import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public class PEPEvent implements PacketExtension {
    PEPItem item;

    public PEPEvent(PEPItem item) {
        this.item = item;
    }

    public void addPEPItem(PEPItem item) {
        this.item = item;
    }

    public String getElementName() {
        return "event";
    }

    public String getNamespace() {
        return "http://jabber.org/protocol/pubsub";
    }

    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append(Separators.LESS_THAN).append(getElementName()).append(" xmlns=\"").append(getNamespace()).append("\">");
        buf.append(this.item.toXML());
        buf.append("</").append(getElementName()).append(Separators.GREATER_THAN);
        return buf.toString();
    }
}
