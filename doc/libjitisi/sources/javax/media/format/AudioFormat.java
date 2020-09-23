package javax.media.format;

import javax.media.Format;
import org.jitsi.impl.neomedia.portaudio.Pa;

public class AudioFormat extends Format {
    public static final String ALAW = "alaw";
    public static final int BIG_ENDIAN = 1;
    public static final String DOLBYAC3 = "dolbyac3";
    public static final String DVI = "dvi";
    public static final String DVI_RTP = "dvi/rtp";
    public static final String G723 = "g723";
    public static final String G723_RTP = "g723/rtp";
    public static final String G728 = "g728";
    public static final String G728_RTP = "g728/rtp";
    public static final String G729 = "g729";
    public static final String G729A = "g729a";
    public static final String G729A_RTP = "g729a/rtp";
    public static final String G729_RTP = "g729/rtp";
    public static final String GSM = "gsm";
    public static final String GSM_MS = "gsm/ms";
    public static final String GSM_RTP = "gsm/rtp";
    public static final String IMA4 = "ima4";
    public static final String IMA4_MS = "ima4/ms";
    public static final String LINEAR = "LINEAR";
    public static final int LITTLE_ENDIAN = 0;
    public static final String MAC3 = "MAC3";
    public static final String MAC6 = "MAC6";
    public static final String MPEG = "mpegaudio";
    public static final String MPEGLAYER3 = "mpeglayer3";
    public static final String MPEG_RTP = "mpegaudio/rtp";
    public static final String MSADPCM = "msadpcm";
    public static final String MSNAUDIO = "msnaudio";
    public static final String MSRT24 = "msrt24";
    public static final int SIGNED = 1;
    public static final String TRUESPEECH = "truespeech";
    public static final String ULAW = "ULAW";
    public static final String ULAW_RTP = "ULAW/rtp";
    public static final int UNSIGNED = 0;
    public static final String VOXWAREAC10 = "voxwareac10";
    public static final String VOXWAREAC16 = "voxwareac16";
    public static final String VOXWAREAC20 = "voxwareac20";
    public static final String VOXWAREAC8 = "voxwareac8";
    public static final String VOXWAREMETASOUND = "voxwaremetasound";
    public static final String VOXWAREMETAVOICE = "voxwaremetavoice";
    public static final String VOXWARERT29H = "voxwarert29h";
    public static final String VOXWARETQ40 = "voxwaretq40";
    public static final String VOXWARETQ60 = "voxwaretq60";
    public static final String VOXWAREVR12 = "voxwarevr12";
    public static final String VOXWAREVR18 = "voxwarevr18";
    protected int channels;
    protected int endian;
    protected double frameRate;
    protected int frameSizeInBits;
    boolean init;
    int margin;
    double multiplier;
    protected double sampleRate;
    protected int sampleSizeInBits;
    protected int signed;

    public AudioFormat(String encoding) {
        super(encoding);
        this.sampleRate = -1.0d;
        this.sampleSizeInBits = -1;
        this.channels = -1;
        this.endian = -1;
        this.signed = -1;
        this.frameRate = -1.0d;
        this.frameSizeInBits = -1;
        this.multiplier = -1.0d;
        this.margin = 0;
        this.init = false;
    }

    public AudioFormat(String encoding, double sampleRate, int sampleSizeInBits, int channels) {
        this(encoding);
        this.sampleRate = sampleRate;
        this.sampleSizeInBits = sampleSizeInBits;
        this.channels = channels;
    }

    public AudioFormat(String encoding, double sampleRate, int sampleSizeInBits, int channels, int endian, int signed) {
        this(encoding, sampleRate, sampleSizeInBits, channels);
        this.endian = endian;
        this.signed = signed;
    }

    public AudioFormat(String encoding, double sampleRate, int sampleSizeInBits, int channels, int endian, int signed, int frameSizeInBits, double frameRate, Class<?> dataType) {
        this(encoding, sampleRate, sampleSizeInBits, channels, endian, signed);
        this.frameSizeInBits = frameSizeInBits;
        this.frameRate = frameRate;
        this.dataType = dataType;
    }

    public Object clone() {
        AudioFormat f = new AudioFormat(this.encoding);
        f.copy(this);
        return f;
    }

    public long computeDuration(long length) {
        if (this.init) {
            if (this.multiplier < Pa.LATENCY_UNSPECIFIED) {
                return -1;
            }
            return ((long) (((double) (length - ((long) this.margin))) * this.multiplier)) * 1000;
        } else if (this.encoding == null) {
            this.init = true;
            return -1;
        } else {
            if (this.encoding.equalsIgnoreCase(LINEAR) || this.encoding.equalsIgnoreCase(ULAW)) {
                if (this.sampleSizeInBits > 0 && this.channels > 0 && this.sampleRate > Pa.LATENCY_UNSPECIFIED) {
                    this.multiplier = ((double) ((8000000 / this.sampleSizeInBits) / this.channels)) / this.sampleRate;
                }
            } else if (this.encoding.equalsIgnoreCase(ULAW_RTP)) {
                if (this.sampleSizeInBits > 0 && this.channels > 0 && this.sampleRate > Pa.LATENCY_UNSPECIFIED) {
                    this.multiplier = ((double) ((8000000 / this.sampleSizeInBits) / this.channels)) / this.sampleRate;
                }
            } else if (this.encoding.equalsIgnoreCase(DVI_RTP)) {
                if (this.sampleSizeInBits > 0 && this.sampleRate > Pa.LATENCY_UNSPECIFIED) {
                    this.multiplier = ((double) (8000000 / this.sampleSizeInBits)) / this.sampleRate;
                }
                this.margin = 4;
            } else if (this.encoding.equalsIgnoreCase(GSM_RTP)) {
                if (this.sampleRate > Pa.LATENCY_UNSPECIFIED) {
                    this.multiplier = 4848484.0d / this.sampleRate;
                }
            } else if (this.encoding.equalsIgnoreCase(G723_RTP)) {
                if (this.sampleRate > Pa.LATENCY_UNSPECIFIED) {
                    this.multiplier = 1.0E7d / this.sampleRate;
                }
            } else if (this.frameSizeInBits != -1 && this.frameRate != -1.0d && this.frameSizeInBits > 0 && this.frameRate > Pa.LATENCY_UNSPECIFIED) {
                this.multiplier = ((double) (8000000 / this.frameSizeInBits)) / this.frameRate;
            }
            this.init = true;
            if (this.multiplier > Pa.LATENCY_UNSPECIFIED) {
                return ((long) (((double) (length - ((long) this.margin))) * this.multiplier)) * 1000;
            }
            return -1;
        }
    }

    /* access modifiers changed from: protected */
    public void copy(Format f) {
        super.copy(f);
        AudioFormat other = (AudioFormat) f;
        this.sampleRate = other.sampleRate;
        this.sampleSizeInBits = other.sampleSizeInBits;
        this.channels = other.channels;
        this.endian = other.endian;
        this.signed = other.signed;
        this.frameSizeInBits = other.frameSizeInBits;
        this.frameRate = other.frameRate;
    }

    public boolean equals(Object format) {
        if (!(format instanceof AudioFormat)) {
            return false;
        }
        AudioFormat other = (AudioFormat) format;
        if (super.equals(format) && this.sampleRate == other.sampleRate && this.sampleSizeInBits == other.sampleSizeInBits && this.channels == other.channels && this.endian == other.endian && this.signed == other.signed && this.frameSizeInBits == other.frameSizeInBits && this.frameRate == other.frameRate) {
            return true;
        }
        return false;
    }

    public int getChannels() {
        return this.channels;
    }

    public int getEndian() {
        return this.endian;
    }

    public double getFrameRate() {
        return this.frameRate;
    }

    public int getFrameSizeInBits() {
        return this.frameSizeInBits;
    }

    public double getSampleRate() {
        return this.sampleRate;
    }

    public int getSampleSizeInBits() {
        return this.sampleSizeInBits;
    }

    public int getSigned() {
        return this.signed;
    }

    public Format intersects(Format format) {
        Format fmt = super.intersects(format);
        if (fmt == null) {
            return null;
        }
        if (!(fmt instanceof AudioFormat)) {
            return fmt;
        }
        AudioFormat other = (AudioFormat) format;
        Format res = (AudioFormat) fmt;
        res.sampleRate = this.sampleRate != -1.0d ? this.sampleRate : other.sampleRate;
        res.sampleSizeInBits = this.sampleSizeInBits != -1 ? this.sampleSizeInBits : other.sampleSizeInBits;
        res.channels = this.channels != -1 ? this.channels : other.channels;
        res.endian = this.endian != -1 ? this.endian : other.endian;
        res.signed = this.signed != -1 ? this.signed : other.signed;
        res.frameSizeInBits = this.frameSizeInBits != -1 ? this.frameSizeInBits : other.frameSizeInBits;
        res.frameRate = this.frameRate != -1.0d ? this.frameRate : other.frameRate;
        return res;
    }

    public boolean matches(Format format) {
        if (!super.matches(format)) {
            return false;
        }
        if (!(format instanceof AudioFormat)) {
            return true;
        }
        AudioFormat other = (AudioFormat) format;
        if ((this.sampleRate == -1.0d || other.sampleRate == -1.0d || this.sampleRate == other.sampleRate) && ((this.sampleSizeInBits == -1 || other.sampleSizeInBits == -1 || this.sampleSizeInBits == other.sampleSizeInBits) && ((this.channels == -1 || other.channels == -1 || this.channels == other.channels) && ((this.endian == -1 || other.endian == -1 || this.endian == other.endian) && ((this.signed == -1 || other.signed == -1 || this.signed == other.signed) && ((this.frameSizeInBits == -1 || other.frameSizeInBits == -1 || this.frameSizeInBits == other.frameSizeInBits) && (this.frameRate == -1.0d || other.frameRate == -1.0d || this.frameRate == other.frameRate))))))) {
            return true;
        }
        return false;
    }

    public String toString() {
        String str;
        String strChannels = "";
        String strEndian = "";
        if (this.channels == 1) {
            strChannels = ", Mono";
        } else if (this.channels == 2) {
            strChannels = ", Stereo";
        } else if (this.channels != -1) {
            strChannels = ", " + this.channels + "-channel";
        }
        if (this.sampleSizeInBits > 8) {
            if (this.endian == 1) {
                strEndian = ", BigEndian";
            } else if (this.endian == 0) {
                strEndian = ", LittleEndian";
            }
        }
        StringBuilder append = new StringBuilder().append(getEncoding());
        if (this.sampleRate != -1.0d) {
            str = ", " + this.sampleRate + " Hz";
        } else {
            str = ", Unknown Sample Rate";
        }
        append = append.append(str);
        if (this.sampleSizeInBits != -1) {
            str = ", " + this.sampleSizeInBits + "-bit";
        } else {
            str = "";
        }
        append = append.append(str).append(strChannels).append(strEndian);
        str = this.signed != -1 ? this.signed == 1 ? ", Signed" : ", Unsigned" : "";
        append = append.append(str);
        if (this.frameRate != -1.0d) {
            str = ", " + this.frameRate + " frame rate";
        } else {
            str = "";
        }
        append = append.append(str);
        if (this.frameSizeInBits != -1) {
            str = ", FrameSize=" + this.frameSizeInBits + " bits";
        } else {
            str = "";
        }
        append = append.append(str);
        if (this.dataType == Format.byteArray || this.dataType == null) {
            str = "";
        } else {
            str = ", " + this.dataType;
        }
        return append.append(str).toString();
    }
}
