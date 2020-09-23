package org.jitsi.impl.neomedia.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.media.Format;
import javax.media.Manager;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import org.jitsi.impl.neomedia.MediaUtils;
import org.jitsi.impl.neomedia.NeomediaServiceUtils;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.codec.EncodingConfiguration;
import org.jitsi.service.neomedia.format.MediaFormat;
import org.jitsi.service.neomedia.format.MediaFormatFactory;
import org.jitsi.util.Logger;

public class MediaFormatFactoryImpl implements MediaFormatFactory {
    private static final Logger logger = Logger.getLogger(MediaFormatFactoryImpl.class);

    public MediaFormat createUnknownMediaFormat(MediaType type) {
        Format unknown = null;
        if (type.equals(MediaType.AUDIO)) {
            unknown = new VideoFormat(Manager.UNKNOWN_CONTENT_NAME);
        } else if (type.equals(MediaType.VIDEO)) {
            unknown = new AudioFormat(Manager.UNKNOWN_CONTENT_NAME);
        }
        return MediaFormatImpl.createInstance(unknown);
    }

    public MediaFormat createMediaFormat(String encoding) {
        return createMediaFormat(encoding, -1.0d);
    }

    public MediaFormat createMediaFormat(byte rtpPayloadType) {
        for (MediaFormat rtpPayloadTypeMediaFormat : MediaUtils.getMediaFormats(rtpPayloadType)) {
            MediaFormat mediaFormat = createMediaFormat(rtpPayloadTypeMediaFormat.getEncoding(), rtpPayloadTypeMediaFormat.getClockRate());
            if (mediaFormat != null) {
                return mediaFormat;
            }
        }
        return null;
    }

    public MediaFormat createMediaFormat(String encoding, double clockRate) {
        return createMediaFormat(encoding, clockRate, 1);
    }

    public MediaFormat createMediaFormat(String encoding, double clockRate, int channels) {
        return createMediaFormat(encoding, clockRate, channels, null);
    }

    private MediaFormat createMediaFormat(String encoding, double clockRate, int channels, Map<String, String> fmtps) {
        for (MediaFormat format : getSupportedMediaFormats(encoding, clockRate)) {
            if (format.matches(format.getMediaType(), format.getEncoding(), format.getClockRate(), channels, fmtps)) {
                return format;
            }
        }
        return null;
    }

    public MediaFormat createMediaFormat(String encoding, double clockRate, Map<String, String> formatParams, Map<String, String> advancedParams) {
        return createMediaFormat(encoding, clockRate, 1, -1.0f, formatParams, advancedParams);
    }

    public MediaFormat createMediaFormat(String encoding, double clockRate, int channels, float frameRate, Map<String, String> formatParams, Map<String, String> advancedParams) {
        MediaFormat mediaFormat = createMediaFormat(encoding, clockRate, channels, (Map) formatParams);
        if (mediaFormat == null) {
            return null;
        }
        Map formatParameters = null;
        Map advancedParameters = null;
        if (!(formatParams == null || formatParams.isEmpty())) {
            formatParameters = formatParams;
        }
        if (!(advancedParams == null || advancedParams.isEmpty())) {
            advancedParameters = advancedParams;
        }
        if (!(formatParameters == null && advancedParameters == null)) {
            switch (mediaFormat.getMediaType()) {
                case AUDIO:
                    mediaFormat = new AudioMediaFormatImpl((AudioFormat) ((AudioMediaFormatImpl) mediaFormat).getFormat(), formatParameters, advancedParameters);
                    break;
                case VIDEO:
                    VideoMediaFormatImpl videoMediaFormatImpl = (VideoMediaFormatImpl) mediaFormat;
                    mediaFormat = new VideoMediaFormatImpl((VideoFormat) videoMediaFormatImpl.getFormat(), videoMediaFormatImpl.getClockRate(), frameRate, formatParameters, advancedParameters);
                    break;
                default:
                    mediaFormat = null;
                    break;
            }
        }
        MediaFormat mediaFormat2 = mediaFormat;
        return mediaFormat;
    }

    public MediaFormat createMediaFormat(byte rtpPayloadType, String encoding, double clockRate, int channels, float frameRate, Map<String, String> formatParams, Map<String, String> advancedParams) {
        if ((byte) -1 != rtpPayloadType && (encoding == null || -1.0d == clockRate)) {
            MediaFormat[] rtpPayloadTypeMediaFormats = MediaUtils.getMediaFormats(rtpPayloadType);
            if (rtpPayloadTypeMediaFormats.length > 0) {
                if (encoding == null) {
                    encoding = rtpPayloadTypeMediaFormats[0].getEncoding();
                }
                if (-1.0d == clockRate) {
                    clockRate = rtpPayloadTypeMediaFormats[0].getClockRate();
                } else {
                    boolean clockRateIsValid = false;
                    for (MediaFormat rtpPayloadTypeMediaFormat : rtpPayloadTypeMediaFormats) {
                        if (rtpPayloadTypeMediaFormat.getEncoding().equals(encoding) && rtpPayloadTypeMediaFormat.getClockRate() == clockRate) {
                            clockRateIsValid = true;
                            break;
                        }
                    }
                    if (!clockRateIsValid) {
                        return null;
                    }
                }
            }
        }
        return createMediaFormat(encoding, clockRate, channels, frameRate, formatParams, advancedParams);
    }

    private List<MediaFormat> getMatchingMediaFormats(MediaFormat[] mediaFormats, String encoding, double clockRate) {
        if ("G722".equalsIgnoreCase(encoding) && 16000.0d == clockRate) {
            clockRate = 8000.0d;
            if (logger.isInfoEnabled()) {
                logger.info("Suppressing erroneous 16000 announcement for G.722");
            }
        }
        List<MediaFormat> supportedMediaFormats = new ArrayList();
        for (MediaFormat mediaFormat : mediaFormats) {
            if (mediaFormat.getEncoding().equalsIgnoreCase(encoding) && (-1.0d == clockRate || mediaFormat.getClockRate() == clockRate)) {
                supportedMediaFormats.add(mediaFormat);
            }
        }
        return supportedMediaFormats;
    }

    private List<MediaFormat> getSupportedMediaFormats(String encoding, double clockRate) {
        EncodingConfiguration encodingConfiguration = NeomediaServiceUtils.getMediaServiceImpl().getCurrentEncodingConfiguration();
        List<MediaFormat> supportedMediaFormats = getMatchingMediaFormats(encodingConfiguration.getAllEncodings(MediaType.AUDIO), encoding, clockRate);
        if (supportedMediaFormats.isEmpty()) {
            return getMatchingMediaFormats(encodingConfiguration.getAllEncodings(MediaType.VIDEO), encoding, clockRate);
        }
        return supportedMediaFormats;
    }
}
