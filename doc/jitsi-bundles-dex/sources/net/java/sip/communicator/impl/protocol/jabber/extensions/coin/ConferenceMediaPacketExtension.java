package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

public class ConferenceMediaPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "available-media";
    public static final String NAMESPACE = "";

    public ConferenceMediaPacketExtension() {
        super("", ELEMENT_NAME);
    }
}
