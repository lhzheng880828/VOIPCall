package org.jivesoftware.smackx.pubsub.packet;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.Packet;

public final class SyncPacketSend {
    private SyncPacketSend() {
    }

    public static Packet getReply(Connection connection, Packet packet, long timeout) throws XMPPException {
        PacketCollector response = connection.createPacketCollector(new PacketIDFilter(packet.getPacketID()));
        connection.sendPacket(packet);
        Packet result = response.nextResult(timeout);
        response.cancel();
        if (result == null) {
            throw new XMPPException("No response from server.");
        } else if (result.getError() == null) {
            return result;
        } else {
            throw new XMPPException(result.getError());
        }
    }

    public static Packet getReply(Connection connection, Packet packet) throws XMPPException {
        return getReply(connection, packet, (long) SmackConfiguration.getPacketReplyTimeout());
    }
}
