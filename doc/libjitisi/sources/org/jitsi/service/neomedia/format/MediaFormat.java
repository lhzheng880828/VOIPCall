package org.jitsi.service.neomedia.format;

import java.util.Map;
import org.jitsi.service.neomedia.MediaType;

public interface MediaFormat {
    public static final int MAX_DYNAMIC_PAYLOAD_TYPE = 127;
    public static final int MIN_DYNAMIC_PAYLOAD_TYPE = 96;
    public static final byte RTP_PAYLOAD_TYPE_UNKNOWN = (byte) -1;

    boolean equals(Object obj);

    boolean formatParametersMatch(Map<String, String> map);

    Map<String, String> getAdditionalCodecSettings();

    Map<String, String> getAdvancedAttributes();

    double getClockRate();

    String getClockRateString();

    String getEncoding();

    Map<String, String> getFormatParameters();

    MediaType getMediaType();

    byte getRTPPayloadType();

    String getRealUsedClockRateString();

    boolean matches(MediaType mediaType, String str, double d, int i, Map<String, String> map);

    boolean matches(MediaFormat mediaFormat);

    void setAdditionalCodecSettings(Map<String, String> map);

    String toString();
}
