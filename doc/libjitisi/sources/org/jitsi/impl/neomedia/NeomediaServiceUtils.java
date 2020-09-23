package org.jitsi.impl.neomedia;

import org.jitsi.service.libjitsi.LibJitsi;

public class NeomediaServiceUtils {
    public static MediaServiceImpl getMediaServiceImpl() {
        return (MediaServiceImpl) LibJitsi.getMediaService();
    }

    private NeomediaServiceUtils() {
    }
}
