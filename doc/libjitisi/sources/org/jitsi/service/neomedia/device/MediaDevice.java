package org.jitsi.service.neomedia.device;

import java.util.List;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.QualityPreset;
import org.jitsi.service.neomedia.RTPExtension;
import org.jitsi.service.neomedia.codec.EncodingConfiguration;
import org.jitsi.service.neomedia.format.MediaFormat;

public interface MediaDevice {
    MediaDirection getDirection();

    MediaFormat getFormat();

    MediaType getMediaType();

    List<RTPExtension> getSupportedExtensions();

    List<MediaFormat> getSupportedFormats();

    List<MediaFormat> getSupportedFormats(QualityPreset qualityPreset, QualityPreset qualityPreset2);

    List<MediaFormat> getSupportedFormats(QualityPreset qualityPreset, QualityPreset qualityPreset2, EncodingConfiguration encodingConfiguration);
}
