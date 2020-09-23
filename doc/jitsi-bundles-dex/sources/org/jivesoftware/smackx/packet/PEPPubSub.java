package org.jivesoftware.smackx.packet;

import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.IQ;

public class PEPPubSub extends IQ {
    PEPItem item;

    public PEPPubSub(PEPItem item) {
        this.item = item;
    }

    public String getElementName() {
        return "pubsub";
    }

    public String getNamespace() {
        return "http://jabber.org/protocol/pubsub";
    }

    public String getChildElementXML() {
        StringBuilder buf = new StringBuilder();
        buf.append(Separators.LESS_THAN).append(getElementName()).append(" xmlns=\"").append(getNamespace()).append("\">");
        buf.append("<publish node=\"").append(this.item.getNode()).append("\">");
        buf.append(this.item.toXML());
        buf.append("</publish>");
        buf.append("</").append(getElementName()).append(Separators.GREATER_THAN);
        return buf.toString();
    }
}
