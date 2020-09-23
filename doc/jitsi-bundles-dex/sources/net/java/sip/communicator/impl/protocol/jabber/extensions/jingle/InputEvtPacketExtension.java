package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

public class InputEvtPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "inputevt";
    public static final String NAMESPACE = "http://jitsi.org/protocol/inputevt";

    public InputEvtPacketExtension() {
        super("http://jitsi.org/protocol/inputevt", "inputevt");
    }
}
