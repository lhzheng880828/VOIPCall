package javax.media.format;

import javax.media.Format;
import org.jitsi.android.util.java.awt.Dimension;

public class VideoFormat extends Format {
    public static final String CINEPAK = "cvid";
    public static final String H261 = "h261";
    public static final String H261_RTP = "h261/rtp";
    public static final String H263 = "h263";
    public static final String H263_1998_RTP = "h263-1998/rtp";
    public static final String H263_RTP = "h263/rtp";
    public static final String INDEO32 = "iv32";
    public static final String INDEO41 = "iv41";
    public static final String INDEO50 = "iv50";
    public static final String IRGB = "irgb";
    public static final String JPEG = "jpeg";
    public static final String JPEG_RTP = "jpeg/rtp";
    public static final String MJPEGA = "mjpa";
    public static final String MJPEGB = "mjpb";
    public static final String MJPG = "mjpg";
    public static final String MPEG = "mpeg";
    public static final String MPEG_RTP = "mpeg/rtp";
    public static final String RGB = "rgb";
    public static final String RLE = "rle";
    public static final String RPZA = "rpza";
    public static final String SMC = "smc";
    public static final String YUV = "yuv";
    protected float frameRate;
    protected int maxDataLength;
    protected Dimension size;

    public VideoFormat(String encoding) {
        super(encoding);
        this.size = null;
        this.maxDataLength = -1;
        this.frameRate = -1.0f;
    }

    public VideoFormat(String encoding, Dimension size, int maxDataLength, Class<?> dataType, float frameRate) {
        this(encoding);
        if (size != null) {
            this.size = new Dimension(size);
        }
        this.maxDataLength = maxDataLength;
        this.dataType = dataType;
        this.frameRate = frameRate;
    }

    public Object clone() {
        VideoFormat f = new VideoFormat(this.encoding, this.size, this.maxDataLength, this.dataType, this.frameRate);
        f.copy(this);
        return f;
    }

    /* access modifiers changed from: protected */
    public void copy(Format f) {
        super.copy(f);
        VideoFormat vf = (VideoFormat) f;
        if (vf.size != null) {
            this.size = new Dimension(vf.size);
        }
        this.maxDataLength = vf.maxDataLength;
        this.frameRate = vf.frameRate;
    }

    public boolean equals(Object format) {
        if (!(format instanceof VideoFormat)) {
            return false;
        }
        VideoFormat vf = (VideoFormat) format;
        if (this.size == null || vf.size == null) {
            if (this.size != vf.size) {
                return false;
            }
        } else if (!this.size.equals(vf.size)) {
            return false;
        }
        if (super.equals(format) && this.maxDataLength == vf.maxDataLength && this.frameRate == vf.frameRate) {
            return true;
        }
        return false;
    }

    public float getFrameRate() {
        return this.frameRate;
    }

    public int getMaxDataLength() {
        return this.maxDataLength;
    }

    public Dimension getSize() {
        return this.size;
    }

    public Format intersects(Format format) {
        Format fmt = super.intersects(format);
        if (fmt == null) {
            return null;
        }
        if (!(format instanceof VideoFormat)) {
            return fmt;
        }
        VideoFormat other = (VideoFormat) format;
        Format res = (VideoFormat) fmt;
        res.size = this.size != null ? this.size : other.size;
        res.maxDataLength = this.maxDataLength != -1 ? this.maxDataLength : other.maxDataLength;
        res.frameRate = this.frameRate != -1.0f ? this.frameRate : other.frameRate;
        return res;
    }

    public boolean matches(Format format) {
        if (!super.matches(format)) {
            return false;
        }
        if (!(format instanceof VideoFormat)) {
            return true;
        }
        VideoFormat vf = (VideoFormat) format;
        if ((this.size == null || vf.size == null || this.size.equals(vf.size)) && (this.frameRate == -1.0f || vf.frameRate == -1.0f || this.frameRate == vf.frameRate)) {
            return true;
        }
        return false;
    }

    public Format relax() {
        VideoFormat fmt = (VideoFormat) super.relax();
        if (fmt == null) {
            return null;
        }
        fmt.size = null;
        fmt.maxDataLength = -1;
        fmt.frameRate = -1.0f;
        return fmt;
    }

    public String toString() {
        String s = "";
        if (getEncoding() != null) {
            s = s + getEncoding().toUpperCase();
        } else {
            s = s + "N/A";
        }
        if (this.size != null) {
            s = s + ", " + this.size.width + "x" + this.size.height;
        }
        if (this.frameRate != -1.0f) {
            s = s + ", FrameRate=" + (((float) ((int) (this.frameRate * 10.0f))) / 10.0f);
        }
        if (this.maxDataLength != -1) {
            s = s + ", Length=" + this.maxDataLength;
        }
        if (this.dataType == null || this.dataType == Format.byteArray) {
            return s;
        }
        return s + ", " + this.dataType;
    }
}
