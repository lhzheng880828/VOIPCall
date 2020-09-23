package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

public class TransferredPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "transferred";
    public static final String NAMESPACE = "urn:xmpp:jingle:transfer:0";

    public TransferredPacketExtension() {
        super("urn:xmpp:jingle:transfer:0", ELEMENT_NAME);
    }
}
