package org.jivesoftware.smackx.packet;

import org.jivesoftware.smack.packet.PacketExtension;

public class Header implements PacketExtension {
    private String name;
    private String value;

    public Header(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public String getElementName() {
        return "header";
    }

    public String getNamespace() {
        return HeadersExtension.NAMESPACE;
    }

    public String toXML() {
        return "<header name='" + this.name + "'>" + this.value + "</header>";
    }
}
