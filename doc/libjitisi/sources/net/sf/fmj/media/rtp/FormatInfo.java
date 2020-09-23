package net.sf.fmj.media.rtp;

import com.sun.media.format.WavAudioFormat;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;

public class FormatInfo {
    public static final int PAYLOAD_NOTFOUND = -1;
    static AudioFormat mpegAudio = new AudioFormat(AudioFormat.MPEG_RTP);
    private SSRCCache cache = null;
    Format[] formatList = new Format[111];

    public static boolean isSupported(int i) {
        switch (i) {
            case 0:
            case 3:
            case 4:
            case 5:
            case 6:
            case 8:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 26:
            case 31:
            case 32:
            case WavAudioFormat.WAVE_FORMAT_DSPGROUP_TRUESPEECH /*34*/:
                return true;
            default:
                return false;
        }
    }

    public FormatInfo() {
        initFormats();
    }

    public void add(int i, Format format) {
        if (i >= this.formatList.length) {
            expandTable(i);
        }
        if (this.formatList[i] == null) {
            this.formatList[i] = format;
            if (this.cache != null && (format instanceof VideoFormat)) {
                this.cache.clockrate[i] = 90000;
            }
            if (this.cache != null && (format instanceof AudioFormat)) {
                if (mpegAudio.matches(format)) {
                    this.cache.clockrate[i] = 90000;
                } else {
                    this.cache.clockrate[i] = (int) ((AudioFormat) format).getSampleRate();
                }
            }
        }
    }

    private void expandTable(int i) {
        Format[] aformat = new Format[(i + 1)];
        for (int j = 0; j < this.formatList.length; j++) {
            aformat[j] = this.formatList[j];
        }
        this.formatList = aformat;
    }

    public Format get(int i) {
        return i >= this.formatList.length ? null : this.formatList[i];
    }

    public int getPayload(Format format) {
        if (format.getEncoding() != null && format.getEncoding().equals(AudioFormat.G729A_RTP)) {
            format = new AudioFormat(AudioFormat.G729_RTP);
        }
        for (int i = 0; i < this.formatList.length; i++) {
            if (format.matches(this.formatList[i])) {
                return i;
            }
        }
        return -1;
    }

    public void initFormats() {
        this.formatList[0] = new AudioFormat(AudioFormat.ULAW_RTP, 8000.0d, 8, 1);
        this.formatList[3] = new AudioFormat(AudioFormat.GSM_RTP, 8000.0d, -1, 1);
        this.formatList[4] = new AudioFormat(AudioFormat.G723_RTP, 8000.0d, -1, 1);
        this.formatList[5] = new AudioFormat(AudioFormat.DVI_RTP, 8000.0d, 4, 1);
        this.formatList[8] = new AudioFormat("ALAW/rtp", 8000.0d, 8, 1);
        this.formatList[14] = new AudioFormat(AudioFormat.MPEG_RTP, -1.0d, -1, -1);
        this.formatList[15] = new AudioFormat(AudioFormat.G728_RTP, 8000.0d, -1, 1);
        this.formatList[16] = new AudioFormat(AudioFormat.DVI_RTP, 11025.0d, 4, 1);
        this.formatList[17] = new AudioFormat(AudioFormat.DVI_RTP, 22050.0d, 4, 1);
        this.formatList[18] = new AudioFormat(AudioFormat.G729_RTP, 8000.0d, -1, 1);
        this.formatList[26] = new VideoFormat(VideoFormat.JPEG_RTP);
        this.formatList[31] = new VideoFormat(VideoFormat.H261_RTP);
        this.formatList[32] = new VideoFormat(VideoFormat.MPEG_RTP);
        this.formatList[34] = new VideoFormat(VideoFormat.H263_RTP);
    }

    public void setCache(SSRCCache ssrccache) {
        this.cache = ssrccache;
    }
}
