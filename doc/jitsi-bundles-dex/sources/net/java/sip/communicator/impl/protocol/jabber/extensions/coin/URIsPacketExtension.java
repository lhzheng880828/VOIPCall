package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

public class URIsPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "uris";
    public static final String NAMESPACE = "";

    public URIsPacketExtension() {
        super("", ELEMENT_NAME);
    }
}
