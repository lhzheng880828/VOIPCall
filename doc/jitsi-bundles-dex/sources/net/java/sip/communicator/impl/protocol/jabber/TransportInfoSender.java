package net.java.sip.communicator.impl.protocol.jabber;

import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension;

public interface TransportInfoSender {
    void sendTransportInfo(Iterable<ContentPacketExtension> iterable);
}
