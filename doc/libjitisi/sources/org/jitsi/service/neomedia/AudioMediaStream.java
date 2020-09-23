package org.jitsi.service.neomedia;

import org.jitsi.service.neomedia.event.CsrcAudioLevelListener;
import org.jitsi.service.neomedia.event.DTMFListener;
import org.jitsi.service.neomedia.event.SimpleAudioLevelListener;
import org.jitsi.service.protocol.DTMFTone;

public interface AudioMediaStream extends MediaStream {
    void addDTMFListener(DTMFListener dTMFListener);

    void removeDTMFListener(DTMFListener dTMFListener);

    void setCsrcAudioLevelListener(CsrcAudioLevelListener csrcAudioLevelListener);

    void setLocalUserAudioLevelListener(SimpleAudioLevelListener simpleAudioLevelListener);

    void setOutputVolumeControl(VolumeControl volumeControl);

    void setStreamAudioLevelListener(SimpleAudioLevelListener simpleAudioLevelListener);

    void startSendingDTMF(DTMFTone dTMFTone, DTMFMethod dTMFMethod, int i, int i2, int i3);

    void stopSendingDTMF(DTMFMethod dTMFMethod);
}
