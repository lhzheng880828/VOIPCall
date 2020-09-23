package org.jitsi.impl.neomedia.transform.csrc;

import java.util.Map;
import java.util.Map.Entry;
import org.jitsi.impl.neomedia.MediaStreamImpl;
import org.jitsi.impl.neomedia.RawPacket;
import org.jitsi.impl.neomedia.transform.PacketTransformer;
import org.jitsi.impl.neomedia.transform.SinglePacketTransformer;
import org.jitsi.impl.neomedia.transform.TransformEngine;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.RTPExtension;

public class SsrcTransformEngine extends SinglePacketTransformer implements TransformEngine {
    private MediaDirection ssrcAudioLevelDirection = MediaDirection.INACTIVE;
    private byte ssrcAudioLevelExtID = (byte) -1;

    public SsrcTransformEngine(MediaStreamImpl mediaStream) {
        Map<Byte, RTPExtension> activeRTPExtensions = mediaStream.getActiveRTPExtensions();
        if (activeRTPExtensions != null && !activeRTPExtensions.isEmpty()) {
            for (Entry<Byte, RTPExtension> e : activeRTPExtensions.entrySet()) {
                RTPExtension rtpExtension = (RTPExtension) e.getValue();
                if (RTPExtension.SSRC_AUDIO_LEVEL_URN.equals(rtpExtension.getURI().toString())) {
                    Byte extID = (Byte) e.getKey();
                    setSsrcAudioLevelExtensionID(extID == null ? (byte) -1 : extID.byteValue(), rtpExtension.getDirection());
                }
            }
        }
    }

    public void close() {
    }

    public PacketTransformer getRTCPTransformer() {
        return null;
    }

    public PacketTransformer getRTPTransformer() {
        return this;
    }

    public RawPacket reverseTransform(RawPacket pkt) {
        if (this.ssrcAudioLevelExtID > (byte) 0 && this.ssrcAudioLevelDirection.allowsReceiving() && !pkt.isInvalid() && 2 == ((pkt.readByte(0) & 192) >>> 6) && pkt.extractSsrcAudioLevel(this.ssrcAudioLevelExtID) == Byte.MAX_VALUE) {
            return null;
        }
        return pkt;
    }

    public void setSsrcAudioLevelExtensionID(byte extID, MediaDirection dir) {
        this.ssrcAudioLevelExtID = extID;
        this.ssrcAudioLevelDirection = dir;
    }

    public RawPacket transform(RawPacket pkt) {
        return pkt;
    }
}
