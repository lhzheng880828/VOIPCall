package org.jitsi.impl.neomedia.transform;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import org.jitsi.impl.neomedia.RTPConnectorUDPInputStream;
import org.jitsi.impl.neomedia.RawPacket;

public class TransformUDPInputStream extends RTPConnectorUDPInputStream {
    private PacketTransformer transformer;

    public TransformUDPInputStream(DatagramSocket socket) {
        super(socket);
    }

    /* access modifiers changed from: protected */
    public RawPacket[] createRawPacket(DatagramPacket datagramPacket) {
        PacketTransformer transformer = getTransformer();
        RawPacket[] pkts = super.createRawPacket(datagramPacket);
        for (int i = 0; i < pkts.length; i++) {
            if (pkts[i].isInvalid()) {
                pkts[i] = null;
            }
        }
        return transformer == null ? pkts : transformer.reverseTransform(pkts);
    }

    public PacketTransformer getTransformer() {
        return this.transformer;
    }

    public void setTransformer(PacketTransformer transformer) {
        this.transformer = transformer;
    }
}
