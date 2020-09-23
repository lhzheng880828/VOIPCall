package org.jitsi.impl.neomedia.protocol;

import org.jitsi.service.neomedia.DTMFInbandTone;

public interface InbandDTMFDataSource {
    void addDTMF(DTMFInbandTone dTMFInbandTone);
}
