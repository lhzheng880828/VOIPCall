package org.jitsi.service.neomedia.event;

import java.util.EventObject;
import org.jitsi.service.neomedia.AudioMediaStream;
import org.jitsi.service.neomedia.DTMFRtpTone;

public class DTMFToneEvent extends EventObject {
    private static final long serialVersionUID = 0;
    private final DTMFRtpTone dtmfTone;

    public DTMFToneEvent(AudioMediaStream source, DTMFRtpTone dtmfTone) {
        super(source);
        this.dtmfTone = dtmfTone;
    }

    public DTMFRtpTone getDtmfTone() {
        return this.dtmfTone;
    }
}
