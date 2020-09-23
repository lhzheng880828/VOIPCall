package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

public class UsersPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "users";
    public static final String NAMESPACE = null;
    public static final String STATE_ATTR_NAME = "state";

    public UsersPacketExtension() {
        super(NAMESPACE, ELEMENT_NAME);
    }
}
