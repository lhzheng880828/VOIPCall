package org.jitsi.impl.neomedia.transform;

import org.jitsi.impl.neomedia.RawPacket;

public interface PacketTransformer {
    void close();

    RawPacket[] reverseTransform(RawPacket[] rawPacketArr);

    RawPacket[] transform(RawPacket[] rawPacketArr);
}
