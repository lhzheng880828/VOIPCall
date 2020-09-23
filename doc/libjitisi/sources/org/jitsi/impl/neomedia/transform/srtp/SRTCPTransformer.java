package org.jitsi.impl.neomedia.transform.srtp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.jitsi.impl.neomedia.RawPacket;
import org.jitsi.impl.neomedia.transform.SinglePacketTransformer;

public class SRTCPTransformer extends SinglePacketTransformer {
    private final Map<Integer, SRTCPCryptoContext> contexts;
    private final SRTPContextFactory forwardFactory;
    private final SRTPContextFactory reverseFactory;

    public SRTCPTransformer(SRTPContextFactory factory) {
        this(factory, factory);
    }

    public SRTCPTransformer(SRTPContextFactory forwardFactory, SRTPContextFactory reverseFactory) {
        this.forwardFactory = forwardFactory;
        this.reverseFactory = reverseFactory;
        this.contexts = new HashMap();
    }

    public void close() {
        synchronized (this.contexts) {
            this.forwardFactory.close();
            if (this.reverseFactory != this.forwardFactory) {
                this.reverseFactory.close();
            }
            Iterator<SRTCPCryptoContext> i = this.contexts.values().iterator();
            while (i.hasNext()) {
                SRTCPCryptoContext context = (SRTCPCryptoContext) i.next();
                i.remove();
                if (context != null) {
                    context.close();
                }
            }
        }
    }

    private SRTCPCryptoContext getContext(RawPacket pkt, SRTPContextFactory engine) {
        SRTCPCryptoContext context;
        int ssrc = pkt.getRTCPSSRC();
        synchronized (this.contexts) {
            context = (SRTCPCryptoContext) this.contexts.get(Integer.valueOf(ssrc));
            if (context == null && engine != null) {
                context = engine.getDefaultContextControl();
                if (context != null) {
                    context = context.deriveContext(ssrc);
                    context.deriveSrtcpKeys();
                    this.contexts.put(Integer.valueOf(ssrc), context);
                }
            }
        }
        return context;
    }

    public RawPacket reverseTransform(RawPacket pkt) {
        SRTCPCryptoContext context = getContext(pkt, this.reverseFactory);
        return (context == null || !context.reverseTransformPacket(pkt)) ? null : pkt;
    }

    public RawPacket transform(RawPacket pkt) {
        SRTCPCryptoContext context = getContext(pkt, this.forwardFactory);
        if (context == null) {
            return null;
        }
        context.transformPacket(pkt);
        return pkt;
    }
}
