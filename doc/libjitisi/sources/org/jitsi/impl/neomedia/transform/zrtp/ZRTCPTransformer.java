package org.jitsi.impl.neomedia.transform.zrtp;

import org.jitsi.impl.neomedia.RawPacket;
import org.jitsi.impl.neomedia.transform.SinglePacketTransformer;

public class ZRTCPTransformer extends SinglePacketTransformer {
    private SinglePacketTransformer srtcpIn = null;
    private SinglePacketTransformer srtcpOut = null;

    public void close() {
        if (this.srtcpOut != null) {
            this.srtcpOut.close();
            this.srtcpOut = null;
        }
        if (this.srtcpIn != null) {
            this.srtcpIn.close();
            this.srtcpIn = null;
        }
    }

    public RawPacket transform(RawPacket pkt) {
        return this.srtcpOut == null ? pkt : this.srtcpOut.transform(pkt);
    }

    public RawPacket reverseTransform(RawPacket pkt) {
        return this.srtcpIn == null ? pkt : this.srtcpIn.reverseTransform(pkt);
    }

    public void setSrtcpIn(SinglePacketTransformer srtcpIn) {
        this.srtcpIn = srtcpIn;
    }

    public void setSrtcpOut(SinglePacketTransformer srtcpOut) {
        this.srtcpOut = srtcpOut;
    }
}
