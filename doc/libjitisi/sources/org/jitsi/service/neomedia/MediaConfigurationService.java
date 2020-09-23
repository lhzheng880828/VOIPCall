package org.jitsi.service.neomedia;

import org.jitsi.android.util.java.awt.Component;
import org.jitsi.service.neomedia.codec.EncodingConfiguration;

public interface MediaConfigurationService {
    Component createAudioConfigPanel();

    Component createEncodingControls(MediaType mediaType, EncodingConfiguration encodingConfiguration);

    Component createVideoConfigPanel();

    MediaService getMediaService();
}
