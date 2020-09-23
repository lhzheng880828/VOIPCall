package org.jitsi.service.neomedia.format;

import java.util.Map;
import org.jitsi.service.neomedia.MediaType;

public interface MediaFormatFactory {
    public static final int CHANNELS_NOT_SPECIFIED = -1;
    public static final double CLOCK_RATE_NOT_SPECIFIED = -1.0d;

    MediaFormat createMediaFormat(byte b);

    MediaFormat createMediaFormat(byte b, String str, double d, int i, float f, Map<String, String> map, Map<String, String> map2);

    MediaFormat createMediaFormat(String str);

    MediaFormat createMediaFormat(String str, double d);

    MediaFormat createMediaFormat(String str, double d, int i);

    MediaFormat createMediaFormat(String str, double d, int i, float f, Map<String, String> map, Map<String, String> map2);

    MediaFormat createMediaFormat(String str, double d, Map<String, String> map, Map<String, String> map2);

    MediaFormat createUnknownMediaFormat(MediaType mediaType);
}
