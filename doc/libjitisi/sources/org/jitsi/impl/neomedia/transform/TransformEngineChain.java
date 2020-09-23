package org.jitsi.impl.neomedia.transform;

import org.jitsi.impl.neomedia.RawPacket;

public class TransformEngineChain implements TransformEngine {
    /* access modifiers changed from: private|final */
    public final TransformEngine[] engineChain;
    private PacketTransformerChain rtcpTransformChain;
    private PacketTransformerChain rtpTransformChain;

    private class PacketTransformerChain implements PacketTransformer {
        private final boolean isRtp;

        public PacketTransformerChain(boolean isRtp) {
            this.isRtp = isRtp;
        }

        public void close() {
            for (TransformEngine engine : TransformEngineChain.this.engineChain) {
                PacketTransformer pTransformer = this.isRtp ? engine.getRTPTransformer() : engine.getRTCPTransformer();
                if (pTransformer != null) {
                    pTransformer.close();
                }
            }
        }

        public RawPacket[] transform(RawPacket[] pkts) {
            for (TransformEngine engine : TransformEngineChain.this.engineChain) {
                PacketTransformer pTransformer = this.isRtp ? engine.getRTPTransformer() : engine.getRTCPTransformer();
                if (pTransformer != null) {
                    pkts = pTransformer.transform(pkts);
                }
            }
            return pkts;
        }

        public RawPacket[] reverseTransform(RawPacket[] pkts) {
            for (int i = TransformEngineChain.this.engineChain.length - 1; i >= 0; i--) {
                TransformEngine engine = TransformEngineChain.this.engineChain[i];
                PacketTransformer pTransformer = this.isRtp ? engine.getRTPTransformer() : engine.getRTCPTransformer();
                if (pTransformer != null) {
                    pkts = pTransformer.reverseTransform(pkts);
                }
            }
            return pkts;
        }
    }

    public TransformEngineChain(TransformEngine[] engineChain) {
        this.engineChain = (TransformEngine[]) engineChain.clone();
    }

    public PacketTransformer getRTPTransformer() {
        boolean invokeOnEngineChain;
        PacketTransformer rtpTransformer;
        synchronized (this) {
            if (this.rtpTransformChain == null) {
                this.rtpTransformChain = new PacketTransformerChain(true);
                invokeOnEngineChain = true;
            } else {
                invokeOnEngineChain = false;
            }
            rtpTransformer = this.rtpTransformChain;
        }
        if (invokeOnEngineChain) {
            for (TransformEngine engine : this.engineChain) {
                engine.getRTPTransformer();
            }
        }
        return rtpTransformer;
    }

    public PacketTransformer getRTCPTransformer() {
        boolean invokeOnEngineChain;
        PacketTransformer rtpTransformer;
        synchronized (this) {
            if (this.rtcpTransformChain == null) {
                this.rtcpTransformChain = new PacketTransformerChain(false);
                invokeOnEngineChain = true;
            } else {
                invokeOnEngineChain = false;
            }
            rtpTransformer = this.rtcpTransformChain;
        }
        if (invokeOnEngineChain) {
            for (TransformEngine engine : this.engineChain) {
                engine.getRTCPTransformer();
            }
        }
        return rtpTransformer;
    }
}
