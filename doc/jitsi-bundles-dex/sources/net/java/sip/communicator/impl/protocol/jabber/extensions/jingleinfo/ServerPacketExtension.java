package net.java.sip.communicator.impl.protocol.jabber.extensions.jingleinfo;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

public class ServerPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "server";
    public static final String HOST_ATTR_NAME = "host";
    public static final String NAMESPACE = null;
    public static final String SSL_ATTR_NAME = "tcpssl";
    public static final String TCP_ATTR_NAME = "tcp";
    public static final String UDP_ATTR_NAME = "udp";

    public ServerPacketExtension() {
        super(NAMESPACE, ELEMENT_NAME);
    }

    public String getHost() {
        return super.getAttributeAsString("host");
    }

    public int getUdp() {
        return Integer.parseInt(super.getAttributeAsString("udp"));
    }

    public int getTcp() {
        return Integer.parseInt(super.getAttributeAsString("tcp"));
    }

    public int getSsl() {
        return Integer.parseInt(super.getAttributeAsString(SSL_ATTR_NAME));
    }
}
