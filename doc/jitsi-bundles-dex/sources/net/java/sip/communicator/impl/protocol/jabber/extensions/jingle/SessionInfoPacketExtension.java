package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

public class SessionInfoPacketExtension extends AbstractPacketExtension {
    public static final String NAMESPACE = "urn:xmpp:jingle:apps:rtp:info:1";
    private final SessionInfoType type;

    public SessionInfoPacketExtension(SessionInfoType type) {
        super(NAMESPACE, type.toString());
        this.type = type;
    }

    public SessionInfoType getType() {
        return this.type;
    }
}
