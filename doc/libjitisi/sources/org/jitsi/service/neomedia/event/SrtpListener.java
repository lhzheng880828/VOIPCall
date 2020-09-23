package org.jitsi.service.neomedia.event;

import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.SrtpControl;

public interface SrtpListener {
    void securityMessageReceived(String str, String str2, int i);

    void securityNegotiationStarted(MediaType mediaType, SrtpControl srtpControl);

    void securityTimeout(MediaType mediaType);

    void securityTurnedOff(MediaType mediaType);

    void securityTurnedOn(MediaType mediaType, String str, SrtpControl srtpControl);
}
