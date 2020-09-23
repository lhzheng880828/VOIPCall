package org.jitsi.impl.neomedia.format;

import java.util.Map;
import javax.media.format.AudioFormat;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.format.AudioMediaFormat;

public class AudioMediaFormatImpl extends MediaFormatImpl<AudioFormat> implements AudioMediaFormat {
    AudioMediaFormatImpl(AudioFormat format) {
        this(format, null, null);
    }

    AudioMediaFormatImpl(AudioFormat format, Map<String, String> formatParameters, Map<String, String> advancedParameters) {
        super(fixChannels(format), formatParameters, advancedParameters);
    }

    public AudioMediaFormatImpl(String encoding) {
        this(new AudioFormat(encoding));
    }

    AudioMediaFormatImpl(String encoding, double clockRate) {
        this(encoding, clockRate, 1);
    }

    AudioMediaFormatImpl(String encoding, double clockRate, int channels) {
        this(encoding, clockRate, channels, null, null);
    }

    AudioMediaFormatImpl(String encoding, double clockRate, Map<String, String> formatParameters, Map<String, String> advancedParameters) {
        this(encoding, clockRate, 1, formatParameters, advancedParameters);
    }

    AudioMediaFormatImpl(String encoding, double clockRate, int channels, Map<String, String> formatParameters, Map<String, String> advancedParameters) {
        this(new AudioFormat(encoding, clockRate, -1, channels), (Map) formatParameters, (Map) advancedParameters);
    }

    private static AudioFormat fixChannels(AudioFormat format) {
        if (-1 == format.getChannels()) {
            return (AudioFormat) format.intersects(new AudioFormat(format.getEncoding(), format.getSampleRate(), format.getSampleSizeInBits(), 1));
        }
        return format;
    }

    public int getChannels() {
        int channels = ((AudioFormat) this.format).getChannels();
        return -1 == channels ? 1 : channels;
    }

    public double getClockRate() {
        return ((AudioFormat) this.format).getSampleRate();
    }

    public final MediaType getMediaType() {
        return MediaType.AUDIO;
    }
}
