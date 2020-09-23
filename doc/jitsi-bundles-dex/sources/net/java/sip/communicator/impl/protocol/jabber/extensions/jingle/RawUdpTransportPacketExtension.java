package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import java.util.List;
import org.jivesoftware.smack.packet.PacketExtension;

public class RawUdpTransportPacketExtension extends IceUdpTransportPacketExtension {
    public static final String ELEMENT_NAME = "transport";
    public static final String NAMESPACE = "urn:xmpp:jingle:transports:raw-udp:1";

    public RawUdpTransportPacketExtension() {
        super("urn:xmpp:jingle:transports:raw-udp:1", "transport");
    }

    public List<? extends PacketExtension> getChildExtensions() {
        return super.getChildExtensions();
    }
}
