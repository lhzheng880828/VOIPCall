package org.jitsi.impl.neomedia.codec;

import java.util.Map;
import javax.media.Controller;
import org.jitsi.impl.neomedia.MediaUtils;
import org.jitsi.impl.neomedia.codec.video.h264.JNIEncoder;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.service.neomedia.codec.EncodingConfiguration;
import org.jitsi.service.neomedia.format.MediaFormat;

public class EncodingConfigurationImpl extends EncodingConfiguration {
    public static final boolean G729 = false;

    public EncodingConfigurationImpl() {
        initializeFormatPreferences();
    }

    private void initializeFormatPreferences() {
        setEncodingPreference("H264", 90000.0d, 1100);
        setEncodingPreference(Constants.H263P, 90000.0d, 0);
        setEncodingPreference(Constants.VP8, 90000.0d, 0);
        setEncodingPreference("JPEG", 90000.0d, 950);
        setEncodingPreference("H261", 90000.0d, 800);
        setEncodingPreference("opus", 48000.0d, 750);
        setEncodingPreference(Constants.SILK, 24000.0d, 714);
        setEncodingPreference(Constants.SILK, 16000.0d, 713);
        setEncodingPreference("G722", 8000.0d, 705);
        setEncodingPreference(Constants.SPEEX, 32000.0d, 701);
        setEncodingPreference(Constants.SPEEX, 16000.0d, 700);
        setEncodingPreference("PCMU", 8000.0d, 650);
        setEncodingPreference("PCMA", 8000.0d, Controller.Started);
        setEncodingPreference("iLBC", 8000.0d, 500);
        setEncodingPreference("GSM", 8000.0d, 450);
        setEncodingPreference(Constants.SPEEX, 8000.0d, 352);
        setEncodingPreference("G723", 8000.0d, JNIEncoder.DEFAULT_KEYINT);
        setEncodingPreference(Constants.SILK, 12000.0d, 0);
        setEncodingPreference(Constants.SILK, 8000.0d, 0);
        setEncodingPreference("G729", 8000.0d, 0);
        setEncodingPreference(Constants.TELEPHONE_EVENT, 8000.0d, 1);
    }

    /* access modifiers changed from: protected */
    public void setEncodingPreference(String encoding, double clockRate, int pref) {
        MediaFormat mediaFormat = null;
        for (MediaFormat mf : MediaUtils.getMediaFormats(encoding)) {
            if (mf.getClockRate() == clockRate) {
                mediaFormat = mf;
                break;
            }
        }
        if (mediaFormat != null) {
            this.encodingPreferences.put(getEncodingPreferenceKey(mediaFormat), Integer.valueOf(pref));
        }
    }

    public MediaFormat[] getAllEncodings(MediaType type) {
        return MediaUtils.getMediaFormats(type);
    }

    /* access modifiers changed from: protected */
    public int compareEncodingPreferences(MediaFormat enc1, MediaFormat enc2) {
        int fmtpCount2 = 0;
        int res = getPriority(enc2) - getPriority(enc1);
        if (res != 0) {
            return res;
        }
        res = enc1.getEncoding().compareToIgnoreCase(enc2.getEncoding());
        if (res != 0) {
            return res;
        }
        res = Double.compare(enc2.getClockRate(), enc1.getClockRate());
        if (res != 0) {
            return res;
        }
        int index1 = MediaUtils.getMediaFormatIndex(enc1);
        if (index1 != -1) {
            int index2 = MediaUtils.getMediaFormatIndex(enc2);
            if (index2 != -1) {
                res = index1 - index2;
            }
        }
        if (res != 0) {
            return res;
        }
        Map<String, String> fmtps1 = enc1.getFormatParameters();
        Map<String, String> fmtps2 = enc2.getFormatParameters();
        int fmtpCount1 = fmtps1 == null ? 0 : fmtps1.size();
        if (fmtps2 != null) {
            fmtpCount2 = fmtps2.size();
        }
        return fmtpCount2 - fmtpCount1;
    }
}
