package net.java.sip.communicator.impl.protocol.jabber.extensions.jingleinfo;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

public class StunPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "stun";
    public static final String NAMESPACE = null;

    public StunPacketExtension() {
        super(NAMESPACE, ELEMENT_NAME);
    }
}
