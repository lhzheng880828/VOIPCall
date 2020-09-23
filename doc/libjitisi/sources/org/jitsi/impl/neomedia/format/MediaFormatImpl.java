package org.jitsi.impl.neomedia.format;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import org.jitsi.impl.neomedia.MediaUtils;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.format.AudioMediaFormat;
import org.jitsi.service.neomedia.format.MediaFormat;

public abstract class MediaFormatImpl<T extends Format> implements MediaFormat {
    public static final String CLOCK_RATE_PNAME = "clockRate";
    static final Map<String, String> EMPTY_FORMAT_PARAMETERS = Collections.emptyMap();
    public static final String ENCODING_PNAME = "encoding";
    public static final String FORMAT_PARAMETERS_PNAME = "fmtps";
    private final Map<String, String> advancedAttributes;
    private Map<String, String> codecSettings;
    protected final T format;
    private final Map<String, String> formatParameters;

    public static MediaFormat createInstance(Format format) {
        MediaFormat mediaFormat = MediaUtils.getMediaFormat(format);
        if (mediaFormat != null) {
            return mediaFormat;
        }
        if (format instanceof AudioFormat) {
            return new AudioMediaFormatImpl((AudioFormat) format);
        }
        if (format instanceof VideoFormat) {
            return new VideoMediaFormatImpl((VideoFormat) format);
        }
        return mediaFormat;
    }

    public static MediaFormatImpl<? extends Format> createInstance(Format format, double clockRate, Map<String, String> formatParameters, Map<String, String> advancedAttributess) {
        if (format instanceof AudioFormat) {
            AudioFormat audioFormat = (AudioFormat) format;
            return new AudioMediaFormatImpl((AudioFormat) new AudioFormat(audioFormat.getEncoding(), clockRate, audioFormat.getSampleSizeInBits(), audioFormat.getChannels()).intersects(audioFormat), (Map) formatParameters, (Map) advancedAttributess);
        } else if (format instanceof VideoFormat) {
            return new VideoMediaFormatImpl((VideoFormat) format, clockRate, -1.0f, formatParameters, advancedAttributess);
        } else {
            return null;
        }
    }

    public static boolean formatParametersAreEqual(String encoding, Map<String, String> fmtps1, Map<String, String> fmtps2) {
        if (fmtps1 == null) {
            if (fmtps2 == null || fmtps2.isEmpty()) {
                return true;
            }
            return false;
        } else if (fmtps2 == null) {
            if (fmtps1 == null || fmtps1.isEmpty()) {
                return true;
            }
            return false;
        } else if (fmtps1.size() != fmtps2.size()) {
            return false;
        } else {
            for (Entry<String, String> fmtp1 : fmtps1.entrySet()) {
                String key1 = (String) fmtp1.getKey();
                if (!fmtps2.containsKey(key1)) {
                    return false;
                }
                String value1 = (String) fmtp1.getValue();
                String value2 = (String) fmtps2.get(key1);
                if (value1 == null || value1.length() == 0) {
                    if (!(value2 == null || value2.length() == 0)) {
                        return false;
                    }
                } else if (!value1.equals(value2)) {
                    return false;
                }
            }
            return true;
        }
    }

    protected MediaFormatImpl(T format) {
        this(format, null, null);
    }

    protected MediaFormatImpl(T format, Map<String, String> formatParameters, Map<String, String> advancedAttributes) {
        this.codecSettings = EMPTY_FORMAT_PARAMETERS;
        if (format == null) {
            throw new NullPointerException("format");
        }
        this.format = format;
        Map hashMap = (formatParameters == null || formatParameters.isEmpty()) ? EMPTY_FORMAT_PARAMETERS : new HashMap(formatParameters);
        this.formatParameters = hashMap;
        hashMap = (advancedAttributes == null || advancedAttributes.isEmpty()) ? EMPTY_FORMAT_PARAMETERS : new HashMap(advancedAttributes);
        this.advancedAttributes = hashMap;
    }

    public boolean advancedAttributesAreEqual(Map<String, String> adv, Map<String, String> adv2) {
        if (adv == null && adv2 != null) {
            return false;
        }
        if (adv != null && adv2 == null) {
            return false;
        }
        if (adv == null && adv2 == null) {
            return true;
        }
        if (adv.size() != adv2.size()) {
            return false;
        }
        for (Entry<String, String> a : adv.entrySet()) {
            String value = (String) adv2.get(a.getKey());
            if (value == null) {
                return false;
            }
            if (!value.equals(a.getValue())) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object mediaFormat) {
        if (this == mediaFormat) {
            return true;
        }
        if (!getClass().isInstance(mediaFormat)) {
            return false;
        }
        MediaFormatImpl<T> mediaFormatImpl = (MediaFormatImpl) mediaFormat;
        if (getFormat().equals(mediaFormatImpl.getFormat()) && formatParametersAreEqual(getFormatParameters(), mediaFormatImpl.getFormatParameters())) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean formatParametersAreEqual(Map<String, String> fmtps1, Map<String, String> fmtps2) {
        return formatParametersAreEqual(getEncoding(), fmtps1, fmtps2);
    }

    public boolean formatParametersMatch(Map<String, String> map) {
        return true;
    }

    public Map<String, String> getAdditionalCodecSettings() {
        return this.codecSettings;
    }

    public Map<String, String> getAdvancedAttributes() {
        return this.advancedAttributes == EMPTY_FORMAT_PARAMETERS ? EMPTY_FORMAT_PARAMETERS : new HashMap(this.advancedAttributes);
    }

    public String getClockRateString() {
        double clockRate = getClockRate();
        long clockRateL = (long) clockRate;
        if (((double) clockRateL) == clockRate) {
            return Long.toString(clockRateL);
        }
        return Double.toString(clockRate);
    }

    public String getEncoding() {
        String jmfEncoding = getJMFEncoding();
        String encoding = MediaUtils.jmfEncodingToEncoding(jmfEncoding);
        if (encoding != null) {
            return encoding;
        }
        encoding = jmfEncoding;
        int encodingLength = encoding.length();
        if (encodingLength <= 3) {
            return encoding;
        }
        int rtpPos = encodingLength - 4;
        if ("/rtp".equalsIgnoreCase(encoding.substring(rtpPos))) {
            return encoding.substring(0, rtpPos);
        }
        return encoding;
    }

    public T getFormat() {
        return this.format;
    }

    public Map<String, String> getFormatParameters() {
        return this.formatParameters == EMPTY_FORMAT_PARAMETERS ? EMPTY_FORMAT_PARAMETERS : new HashMap(this.formatParameters);
    }

    public String getJMFEncoding() {
        return this.format.getEncoding();
    }

    public String getRealUsedClockRateString() {
        if (getEncoding().equalsIgnoreCase("G722")) {
            return "16000";
        }
        return getClockRateString();
    }

    public byte getRTPPayloadType() {
        return MediaUtils.getRTPPayloadType(getJMFEncoding(), getClockRate());
    }

    public int hashCode() {
        return getJMFEncoding().hashCode() | getFormatParameters().hashCode();
    }

    public boolean matches(MediaFormat format) {
        if (format == null) {
            return false;
        }
        MediaType mediaType = format.getMediaType();
        return matches(mediaType, format.getEncoding(), format.getClockRate(), MediaType.AUDIO.equals(mediaType) ? ((AudioMediaFormat) format).getChannels() : -1, format.getFormatParameters());
    }

    public boolean matches(MediaType mediaType, String encoding, double clockRate, int channels, Map<String, String> formatParameters) {
        if (!getMediaType().equals(mediaType) || !getEncoding().equals(encoding)) {
            return false;
        }
        if (clockRate != -1.0d) {
            double formatClockRate = getClockRate();
            if (!(formatClockRate == -1.0d || formatClockRate == clockRate)) {
                return false;
            }
        }
        if (MediaType.AUDIO.equals(mediaType)) {
            if (channels == -1) {
                channels = 1;
            }
            int formatChannels = ((AudioMediaFormat) this).getChannels();
            if (formatChannels == -1) {
                formatChannels = 1;
            }
            if (formatChannels != channels) {
                return false;
            }
        }
        return formatParametersMatch(formatParameters);
    }

    public void setAdditionalCodecSettings(Map<String, String> settings) {
        if (settings == null || settings.isEmpty()) {
            settings = EMPTY_FORMAT_PARAMETERS;
        }
        this.codecSettings = settings;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("rtpmap:");
        str.append(getRTPPayloadType());
        str.append(' ');
        str.append(getEncoding());
        str.append('/');
        str.append(getClockRateString());
        if (MediaType.AUDIO.equals(getMediaType())) {
            int channels = ((AudioFormat) getFormat()).getChannels();
            if (channels != 1) {
                str.append('/');
                str.append(channels);
            }
        }
        Map<String, String> formatParameters = getFormatParameters();
        if (!formatParameters.isEmpty()) {
            str.append(" fmtp:");
            boolean prependSeparator = false;
            for (Entry<String, String> formatParameter : formatParameters.entrySet()) {
                if (prependSeparator) {
                    str.append(';');
                } else {
                    prependSeparator = true;
                }
                str.append((String) formatParameter.getKey());
                str.append('=');
                str.append((String) formatParameter.getValue());
            }
        }
        return str.toString();
    }
}
