package org.jitsi.impl.neomedia.transform;

import java.net.DatagramPacket;
import java.net.Socket;
import org.jitsi.impl.neomedia.RTPConnectorTCPInputStream;
import org.jitsi.impl.neomedia.RawPacket;

public class TransformTCPInputStream extends RTPConnectorTCPInputStream {
    private PacketTransformer transformer;

    public TransformTCPInputStream(Socket socket) {
        super(socket);
    }

    /* access modifiers changed from: protected */
    public RawPacket[] createRawPacket(DatagramPacket datagramPacket) {
        PacketTransformer transformer = getTransformer();
        RawPacket[] pkts = super.createRawPacket(datagramPacket);
        return transformer == null ? pkts : transformer.reverseTransform(pkts);
    }

    public PacketTransformer getTransformer() {
        return this.transformer;
    }

    public void setTransformer(PacketTransformer transformer) {
        this.transformer = transformer;
    }
}
