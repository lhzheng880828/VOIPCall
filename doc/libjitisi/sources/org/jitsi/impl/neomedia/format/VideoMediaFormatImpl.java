package org.jitsi.impl.neomedia.format;

import java.util.Map;
import javax.media.format.VideoFormat;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.codec.video.h264.JNIEncoder;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.service.neomedia.format.VideoMediaFormat;

public class VideoMediaFormatImpl extends MediaFormatImpl<VideoFormat> implements VideoMediaFormat {
    public static final double DEFAULT_CLOCK_RATE = 90000.0d;
    private final double clockRate;

    VideoMediaFormatImpl(String encoding) {
        this(encoding, 90000.0d);
    }

    VideoMediaFormatImpl(String encoding, double clockRate) {
        this(new VideoFormat(encoding), clockRate);
    }

    VideoMediaFormatImpl(VideoFormat format) {
        this(format, 90000.0d);
    }

    VideoMediaFormatImpl(VideoFormat format, double clockRate) {
        this(format, clockRate, -1.0f, null, null);
    }

    VideoMediaFormatImpl(VideoFormat format, double clockRate, float frameRate, Map<String, String> formatParameters, Map<String, String> advancedParameters) {
        super(new ParameterizedVideoFormat(format.getEncoding(), format.getSize(), format.getMaxDataLength(), format.getDataType(), frameRate, formatParameters), formatParameters, advancedParameters);
        this.clockRate = clockRate;
    }

    public boolean equals(Object mediaFormat) {
        if (this == mediaFormat) {
            return true;
        }
        if (!super.equals(mediaFormat)) {
            return false;
        }
        VideoMediaFormatImpl videoMediaFormatImpl = (VideoMediaFormatImpl) mediaFormat;
        double clockRate = getClockRate();
        double videoMediaFormatImplClockRate = videoMediaFormatImpl.getClockRate();
        if (-1.0d == clockRate) {
            clockRate = 90000.0d;
        }
        if (-1.0d == videoMediaFormatImplClockRate) {
            videoMediaFormatImplClockRate = 90000.0d;
        }
        if (clockRate != videoMediaFormatImplClockRate) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean formatParametersAreEqual(Map<String, String> fmtps1, Map<String, String> fmtps2) {
        return formatParametersAreEqual(getEncoding(), fmtps1, fmtps2);
    }

    public static boolean formatParametersAreEqual(String encoding, Map<String, String> fmtps1, Map<String, String> fmtps2) {
        if ("H264".equalsIgnoreCase(encoding) || Constants.H264_RTP.equalsIgnoreCase(encoding)) {
            String packetizationMode = JNIEncoder.PACKETIZATION_MODE_FMTP;
            String pm1 = null;
            String pm2 = null;
            if (fmtps1 != null) {
                pm1 = (String) fmtps1.remove(packetizationMode);
            }
            if (fmtps2 != null) {
                pm2 = (String) fmtps2.remove(packetizationMode);
            }
            if (pm1 == null) {
                pm1 = "0";
            }
            if (pm2 == null) {
                pm2 = "0";
            }
            if (!pm1.equals(pm2)) {
                return false;
            }
        }
        return MediaFormatImpl.formatParametersAreEqual(encoding, fmtps1, fmtps2);
    }

    public boolean formatParametersMatch(Map<String, String> fmtps) {
        return formatParametersMatch(getEncoding(), getFormatParameters(), fmtps) && super.formatParametersMatch(fmtps);
    }

    public static boolean formatParametersMatch(String encoding, Map<String, String> fmtps1, Map<String, String> fmtps2) {
        String pm2 = null;
        if ("H264".equalsIgnoreCase(encoding) || Constants.H264_RTP.equalsIgnoreCase(encoding)) {
            String packetizationMode = JNIEncoder.PACKETIZATION_MODE_FMTP;
            String pm1 = fmtps1 == null ? null : (String) fmtps1.get(packetizationMode);
            if (fmtps2 != null) {
                pm2 = (String) fmtps2.get(packetizationMode);
            }
            if (pm1 == null) {
                pm1 = "0";
            }
            if (pm2 == null) {
                pm2 = "0";
            }
            if (!pm1.equals(pm2)) {
                return false;
            }
        }
        return true;
    }

    public double getClockRate() {
        return this.clockRate;
    }

    public float getFrameRate() {
        return ((VideoFormat) this.format).getFrameRate();
    }

    public final MediaType getMediaType() {
        return MediaType.VIDEO;
    }

    public Dimension getSize() {
        return ((VideoFormat) this.format).getSize();
    }

    public int hashCode() {
        double clockRate = getClockRate();
        if (-1.0d == clockRate) {
            clockRate = 90000.0d;
        }
        return super.hashCode() | Double.valueOf(clockRate).hashCode();
    }
}
