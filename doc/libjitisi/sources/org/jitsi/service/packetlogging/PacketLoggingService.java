package org.jitsi.service.packetlogging;

public interface PacketLoggingService {

    public enum ProtocolName {
        SIP,
        JABBER,
        RTP,
        ICE4J
    }

    public enum TransportName {
        UDP,
        TCP
    }

    PacketLoggingConfiguration getConfiguration();

    boolean isLoggingEnabled();

    boolean isLoggingEnabled(ProtocolName protocolName);

    void logPacket(ProtocolName protocolName, byte[] bArr, int i, byte[] bArr2, int i2, TransportName transportName, boolean z, byte[] bArr3);

    void logPacket(ProtocolName protocolName, byte[] bArr, int i, byte[] bArr2, int i2, TransportName transportName, boolean z, byte[] bArr3, int i3, int i4);
}
