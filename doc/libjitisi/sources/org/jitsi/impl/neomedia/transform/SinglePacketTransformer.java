package org.jitsi.impl.neomedia.transform;

import org.jitsi.impl.neomedia.RawPacket;

public abstract class SinglePacketTransformer implements PacketTransformer {
    public abstract RawPacket reverseTransform(RawPacket rawPacket);

    public abstract RawPacket transform(RawPacket rawPacket);

    public RawPacket[] transform(RawPacket[] pkts) {
        if (pkts != null) {
            for (int i = 0; i < pkts.length; i++) {
                RawPacket pkt = pkts[i];
                if (pkt != null) {
                    pkts[i] = transform(pkt);
                }
            }
        }
        return pkts;
    }

    public RawPacket[] reverseTransform(RawPacket[] pkts) {
        if (pkts != null) {
            for (int i = 0; i < pkts.length; i++) {
                RawPacket pkt = pkts[i];
                if (pkt != null) {
                    pkts[i] = reverseTransform(pkt);
                }
            }
        }
        return pkts;
    }
}
