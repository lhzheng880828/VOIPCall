package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

public class CoinPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "conference-info";
    public static final String ISFOCUS_ATTR_NAME = "isfocus";
    public static final String NAMESPACE = "";

    public CoinPacketExtension() {
        super("", "conference-info");
    }

    public CoinPacketExtension(boolean isFocus) {
        super("", "conference-info");
        setAttribute(ISFOCUS_ATTR_NAME, Boolean.valueOf(isFocus));
    }
}
