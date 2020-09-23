package org.jitsi.impl.neomedia.transform;

public interface TransformEngine {
    PacketTransformer getRTCPTransformer();

    PacketTransformer getRTPTransformer();
}
