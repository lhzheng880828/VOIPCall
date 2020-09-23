package org.jitsi.impl.neomedia.transform.srtp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.jitsi.impl.neomedia.RawPacket;
import org.jitsi.impl.neomedia.transform.SinglePacketTransformer;

public class SRTPTransformer extends SinglePacketTransformer {
    private final Map<Integer, SRTPCryptoContext> contexts;
    private final SRTPContextFactory forwardFactory;
    private final SRTPContextFactory reverseFactory;

    public SRTPTransformer(SRTPContextFactory factory) {
        this(factory, factory);
    }

    public SRTPTransformer(SRTPContextFactory forwardFactory, SRTPContextFactory reverseFactory) {
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
            Iterator<SRTPCryptoContext> i = this.contexts.values().iterator();
            while (i.hasNext()) {
                SRTPCryptoContext context = (SRTPCryptoContext) i.next();
                i.remove();
                if (context != null) {
                    context.close();
                }
            }
        }
    }

    private SRTPCryptoContext getContext(int ssrc, SRTPContextFactory engine, int deriveSrtpKeysIndex) {
        SRTPCryptoContext context;
        synchronized (this.contexts) {
            context = (SRTPCryptoContext) this.contexts.get(Integer.valueOf(ssrc));
            if (context == null) {
                context = engine.getDefaultContext();
                if (context != null) {
                    context = context.deriveContext(ssrc, 0, 0);
                    context.deriveSrtpKeys((long) deriveSrtpKeysIndex);
                    this.contexts.put(Integer.valueOf(ssrc), context);
                }
            }
        }
        return context;
    }

    public RawPacket reverseTransform(RawPacket pkt) {
        if ((pkt.readByte(0) & 192) != 128) {
            return null;
        }
        SRTPCryptoContext context = getContext(pkt.getSSRC(), this.reverseFactory, pkt.getSequenceNumber());
        if (context == null || !context.reverseTransformPacket(pkt)) {
            pkt = null;
        }
        return pkt;
    }

    public RawPacket transform(RawPacket pkt) {
        return getContext(pkt.getSSRC(), this.forwardFactory, 0).transformPacket(pkt) ? pkt : null;
    }
}
