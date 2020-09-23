package org.jivesoftware.smackx.pubsub.packet;

import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.pubsub.PubSubElementType;

public class PubSub extends IQ {
    private PubSubNamespace ns = PubSubNamespace.BASIC;

    public String getElementName() {
        return "pubsub";
    }

    public String getNamespace() {
        return this.ns.getXmlns();
    }

    public void setPubSubNamespace(PubSubNamespace ns) {
        this.ns = ns;
    }

    public PacketExtension getExtension(PubSubElementType elem) {
        return getExtension(elem.getElementName(), elem.getNamespace().getXmlns());
    }

    public PubSubNamespace getPubSubNamespace() {
        return this.ns;
    }

    public String getChildElementXML() {
        StringBuilder buf = new StringBuilder();
        buf.append(Separators.LESS_THAN).append(getElementName()).append(" xmlns=\"").append(getNamespace()).append("\">");
        buf.append(getExtensionsXML());
        buf.append("</").append(getElementName()).append(Separators.GREATER_THAN);
        return buf.toString();
    }
}
