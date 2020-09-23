package org.jitsi.impl.neomedia.transform;

import java.net.DatagramSocket;
import org.jitsi.impl.neomedia.RTPConnectorUDPOutputStream;
import org.jitsi.impl.neomedia.RawPacket;

public class TransformUDPOutputStream extends RTPConnectorUDPOutputStream {
    private PacketTransformer transformer;

    public TransformUDPOutputStream(DatagramSocket socket) {
        super(socket);
    }

    /* access modifiers changed from: protected */
    public RawPacket[] createRawPacket(byte[] buffer, int offset, int length) {
        RawPacket[] pkts = super.createRawPacket(buffer, offset, length);
        PacketTransformer transformer = getTransformer();
        if (transformer != null) {
            return transformer.transform(pkts);
        }
        return pkts;
    }

    public PacketTransformer getTransformer() {
        return this.transformer;
    }

    public void setTransformer(PacketTransformer transformer) {
        this.transformer = transformer;
    }
}
