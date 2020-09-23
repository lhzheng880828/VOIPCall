package org.jitsi.service.neomedia.control;

import javax.media.Control;

public interface PacketLossAwareEncoder extends Control {
    void setExpectedPacketLoss(int i);
}
