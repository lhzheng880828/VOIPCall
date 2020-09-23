package org.jitsi.impl.neomedia.transform.pt;

import java.util.HashMap;
import java.util.Map;
import org.jitsi.impl.neomedia.RawPacket;
import org.jitsi.impl.neomedia.transform.PacketTransformer;
import org.jitsi.impl.neomedia.transform.SinglePacketTransformer;
import org.jitsi.impl.neomedia.transform.TransformEngine;

public class PayloadTypeTransformEngine extends SinglePacketTransformer implements TransformEngine {
    private Map<Byte, Byte> mappingOverrides = new HashMap();
    private Map<Byte, Byte> mappingOverridesCopy = null;

    public RawPacket transform(RawPacket pkt) {
        if (!(this.mappingOverridesCopy == null || this.mappingOverridesCopy.isEmpty())) {
            Byte newPT = (Byte) this.mappingOverridesCopy.get(Byte.valueOf(pkt.getPayloadType()));
            if (newPT != null) {
                pkt.setPayload(newPT.byteValue());
            }
        }
        return pkt;
    }

    public RawPacket reverseTransform(RawPacket pkt) {
        return pkt;
    }

    public void close() {
    }

    public PacketTransformer getRTPTransformer() {
        return this;
    }

    public PacketTransformer getRTCPTransformer() {
        return null;
    }

    public void addPTMappingOverride(byte originalPt, byte overridePt) {
        Byte existingOverride = (Byte) this.mappingOverrides.get(Byte.valueOf(originalPt));
        if (existingOverride == null || existingOverride.byteValue() != overridePt) {
            this.mappingOverrides.put(Byte.valueOf(originalPt), Byte.valueOf(overridePt));
            this.mappingOverridesCopy = new HashMap(this.mappingOverrides);
        }
    }
}
