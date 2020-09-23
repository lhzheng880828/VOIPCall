package net.java.sip.communicator.impl.protocol.jabber.extensions.caps;

import org.jivesoftware.smack.packet.PacketExtension;

public class CapsPacketExtension implements PacketExtension {
    public static final String ELEMENT_NAME = "c";
    public static final String HASH_METHOD = "sha-1";
    public static final String NAMESPACE = "http://jabber.org/protocol/caps";
    private String ext;
    private String hash;
    private String node;
    private String ver;

    public CapsPacketExtension(String ext, String node, String hash, String ver) {
        this.ext = ext;
        this.node = node;
        this.ver = ver;
        this.hash = hash;
    }

    public String getElementName() {
        return ELEMENT_NAME;
    }

    public String getNamespace() {
        return NAMESPACE;
    }

    public String getNode() {
        return this.node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getVersion() {
        return this.ver;
    }

    public void setVersion(String version) {
        this.ver = version;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getExtensions() {
        return this.ext;
    }

    public void setExtensions(String extensions) {
        this.ext = extensions;
    }

    public String toXML() {
        StringBuilder bldr = new StringBuilder("<c xmlns='" + getNamespace() + "' ");
        if (getExtensions() != null) {
            bldr.append("ext='" + getExtensions() + "' ");
        }
        bldr.append("hash='" + getHash() + "' ");
        bldr.append("node='" + getNode() + "' ");
        bldr.append("ver='" + getVersion() + "'/>");
        return bldr.toString();
    }
}
