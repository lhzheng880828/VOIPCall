package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

public class SidebarsByValPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "sidebars-by-val";
    public static final String NAMESPACE = "";

    public SidebarsByValPacketExtension() {
        super("", ELEMENT_NAME);
    }
}
