package org.jitsi.service.neomedia.event;

public interface DTMFListener {
    void dtmfToneReceptionEnded(DTMFToneEvent dTMFToneEvent);

    void dtmfToneReceptionStarted(DTMFToneEvent dTMFToneEvent);
}
