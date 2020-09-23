package org.jitsi.impl.neomedia.transform;

import java.net.Socket;
import org.jitsi.impl.neomedia.RTPConnectorTCPOutputStream;
import org.jitsi.impl.neomedia.RawPacket;

public class TransformTCPOutputStream extends RTPConnectorTCPOutputStream {
    private PacketTransformer transformer;

    public TransformTCPOutputStream(Socket socket) {
        super(socket);
    }

    /* access modifiers changed from: protected */
    public RawPacket[] createRawPacket(byte[] buffer, int offset, int length) {
        RawPacket[] pkts = super.createRawPacket(buffer, offset, length);
        PacketTransformer transformer = getTransformer();
        if (transformer != null) {
            pkts = transformer.transform(pkts);
            if (pkts == null && this.targets.size() > 0) {
                throw new NullPointerException("pkts");
            }
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
