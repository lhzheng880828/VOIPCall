package org.jivesoftware.smackx.pubsub;

import org.jivesoftware.smack.packet.PacketExtension;

public class SimplePayload implements PacketExtension {
    private String elemName;
    private String ns;
    private String payload;

    public SimplePayload(String elementName, String namespace, String xmlPayload) {
        this.elemName = elementName;
        this.payload = xmlPayload;
        this.ns = namespace;
    }

    public String getElementName() {
        return this.elemName;
    }

    public String getNamespace() {
        return this.ns;
    }

    public String toXML() {
        return this.payload;
    }

    public String toString() {
        return getClass().getName() + "payload [" + toXML() + "]";
    }
}
