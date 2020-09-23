package net.java.sip.communicator.impl.netaddr;

import org.ice4j.stack.PacketLogger;
import org.jitsi.service.packetlogging.PacketLoggingService;
import org.jitsi.service.packetlogging.PacketLoggingService.ProtocolName;
import org.jitsi.service.packetlogging.PacketLoggingService.TransportName;

public class Ice4jPacketLogger implements PacketLogger {
    public void logPacket(byte[] sourceAddress, int sourcePort, byte[] destinationAddress, int destinationPort, byte[] packetContent, boolean sender) {
        if (isEnabled()) {
            NetaddrActivator.getPacketLogging().logPacket(ProtocolName.ICE4J, sourceAddress, sourcePort, destinationAddress, destinationPort, TransportName.UDP, sender, packetContent);
        }
    }

    public boolean isEnabled() {
        PacketLoggingService packetLoggingService = NetaddrActivator.getPacketLogging();
        return packetLoggingService != null && packetLoggingService.isLoggingEnabled(ProtocolName.ICE4J);
    }
}
