package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

public class CallInfoPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "call-info";
    public static final String NAMESPACE = "";

    public CallInfoPacketExtension() {
        super("", ELEMENT_NAME);
    }
}
