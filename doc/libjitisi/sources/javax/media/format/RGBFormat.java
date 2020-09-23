package javax.media.format;

import javax.media.Format;
import org.jitsi.android.util.java.awt.Dimension;

public class RGBFormat extends VideoFormat {
    public static final int BIG_ENDIAN = 0;
    private static String ENCODING = VideoFormat.RGB;
    public static final int LITTLE_ENDIAN = 1;
    protected int bitsPerPixel;
    protected int blueMask;
    protected int endian;
    protected int flipped;
    protected int greenMask;
    protected int lineStride;
    protected int pixelStride;
    protected int redMask;

    public RGBFormat() {
        super(ENCODING);
        this.redMask = -1;
        this.greenMask = -1;
        this.blueMask = -1;
        this.bitsPerPixel = -1;
        this.pixelStride = -1;
        this.lineStride = -1;
        this.flipped = -1;
        this.endian = -1;
        this.dataType = null;
    }

    public RGBFormat(Dimension size, int maxDataLength, Class<?> dataType, float frameRate, int bitsPerPixel, int red, int green, int blue) {
        super(ENCODING, size, maxDataLength, dataType, frameRate);
        this.redMask = -1;
        this.greenMask = -1;
        this.blueMask = -1;
        this.bitsPerPixel = -1;
        this.pixelStride = -1;
        this.lineStride = -1;
        this.flipped = -1;
        this.endian = -1;
        this.bitsPerPixel = bitsPerPixel;
        this.redMask = red;
        this.greenMask = green;
        this.blueMask = blue;
        if (bitsPerPixel == -1 || dataType == null) {
            this.pixelStride = -1;
        } else {
            this.pixelStride = bitsPerPixel / 8;
            if (dataType != byteArray) {
                this.pixelStride = 1;
            }
        }
        if (size == null || this.pixelStride == -1) {
            this.lineStride = -1;
        } else {
            this.lineStride = this.pixelStride * size.width;
        }
        this.flipped = 0;
        if (bitsPerPixel == 16 && dataType == byteArray) {
            this.endian = 1;
        } else {
            this.endian = -1;
        }
    }

    public RGBFormat(Dimension size, int maxDataLength, Class<?> dataType, float frameRate, int bitsPerPixel, int red, int green, int blue, int pixelStride, int lineStride, int flipped, int endian) {
        super(ENCODING, size, maxDataLength, dataType, frameRate);
        this.redMask = -1;
        this.greenMask = -1;
        this.blueMask = -1;
        this.bitsPerPixel = -1;
        this.pixelStride = -1;
        this.lineStride = -1;
        this.flipped = -1;
        this.endian = -1;
        this.bitsPerPixel = bitsPerPixel;
        this.redMask = red;
        this.greenMask = green;
        this.blueMask = blue;
        this.pixelStride = pixelStride;
        this.lineStride = lineStride;
        this.flipped = flipped;
        this.endian = endian;
    }

    public Object clone() {
        RGBFormat f = new RGBFormat(this.size, this.maxDataLength, this.dataType, this.frameRate, this.bitsPerPixel, this.redMask, this.greenMask, this.blueMask, this.pixelStride, this.lineStride, this.flipped, this.endian);
        f.copy(this);
        return f;
    }

    /* access modifiers changed from: protected */
    public void copy(Format f) {
        super.copy(f);
        if (f instanceof RGBFormat) {
            RGBFormat other = (RGBFormat) f;
            this.bitsPerPixel = other.bitsPerPixel;
            this.redMask = other.redMask;
            this.greenMask = other.greenMask;
            this.blueMask = other.blueMask;
            this.pixelStride = other.pixelStride;
            this.lineStride = other.lineStride;
            this.flipped = other.flipped;
            this.endian = other.endian;
        }
    }

    public boolean equals(Object format) {
        if (!(format instanceof RGBFormat)) {
            return false;
        }
        RGBFormat other = (RGBFormat) format;
        if (super.equals(format) && this.bitsPerPixel == other.bitsPerPixel && this.redMask == other.redMask && this.greenMask == other.greenMask && this.blueMask == other.blueMask && this.pixelStride == other.pixelStride && this.lineStride == other.lineStride && this.endian == other.endian && this.flipped == other.flipped) {
            return true;
        }
        return false;
    }

    public int getBitsPerPixel() {
        return this.bitsPerPixel;
    }

    public int getBlueMask() {
        return this.blueMask;
    }

    public int getEndian() {
        return this.endian;
    }

    public int getFlipped() {
        return this.flipped;
    }

    public int getGreenMask() {
        return this.greenMask;
    }

    public int getLineStride() {
        return this.lineStride;
    }

    public int getPixelStride() {
        return this.pixelStride;
    }

    public int getRedMask() {
        return this.redMask;
    }

    public Format intersects(Format format) {
        Format fmt = super.intersects(format);
        if (fmt == null) {
            return null;
        }
        if (!(format instanceof RGBFormat)) {
            return fmt;
        }
        RGBFormat other = (RGBFormat) format;
        Format res = (RGBFormat) fmt;
        res.bitsPerPixel = this.bitsPerPixel != -1 ? this.bitsPerPixel : other.bitsPerPixel;
        res.pixelStride = this.pixelStride != -1 ? this.pixelStride : other.pixelStride;
        res.lineStride = this.lineStride != -1 ? this.lineStride : other.lineStride;
        res.redMask = this.redMask != -1 ? this.redMask : other.redMask;
        res.greenMask = this.greenMask != -1 ? this.greenMask : other.greenMask;
        res.blueMask = this.blueMask != -1 ? this.blueMask : other.blueMask;
        res.flipped = this.flipped != -1 ? this.flipped : other.flipped;
        res.endian = this.endian != -1 ? this.endian : other.endian;
        return res;
    }

    public boolean matches(Format format) {
        if (!super.matches(format)) {
            return false;
        }
        if (!(format instanceof RGBFormat)) {
            return true;
        }
        RGBFormat other = (RGBFormat) format;
        if ((this.bitsPerPixel == -1 || other.bitsPerPixel == -1 || this.bitsPerPixel == other.bitsPerPixel) && ((this.redMask == -1 || other.redMask == -1 || this.redMask == other.redMask) && ((this.greenMask == -1 || other.greenMask == -1 || this.greenMask == other.greenMask) && ((this.blueMask == -1 || other.blueMask == -1 || this.blueMask == other.blueMask) && ((this.pixelStride == -1 || other.pixelStride == -1 || this.pixelStride == other.pixelStride) && ((this.endian == -1 || other.endian == -1 || this.endian == other.endian) && (this.flipped == -1 || other.flipped == -1 || this.flipped == other.flipped))))))) {
            return true;
        }
        return false;
    }

    public Format relax() {
        RGBFormat fmt = (RGBFormat) super.relax();
        if (fmt == null) {
            return null;
        }
        fmt.lineStride = -1;
        fmt.pixelStride = -1;
        return fmt;
    }

    public String toString() {
        String s = getEncoding().toUpperCase();
        if (this.size != null) {
            s = s + ", " + this.size.width + "x" + this.size.height;
        }
        if (this.frameRate != -1.0f) {
            s = s + ", FrameRate=" + (((float) ((int) (this.frameRate * 10.0f))) / 10.0f);
        }
        if (this.maxDataLength != -1) {
            s = s + ", Length=" + this.maxDataLength;
        }
        s = (s + ", " + this.bitsPerPixel + "-bit") + ", Masks=" + this.redMask + ":" + this.greenMask + ":" + this.blueMask;
        if (this.pixelStride != 1) {
            s = s + ", PixelStride=" + this.pixelStride;
        }
        s = s + ", LineStride=" + this.lineStride;
        if (this.flipped != -1) {
            s = s + (this.flipped == 1 ? ", Flipped" : "");
        }
        if (this.dataType == byteArray && this.bitsPerPixel == 16 && this.endian != -1) {
            s = s + (this.endian == 0 ? ", BigEndian" : ", LittleEndian");
        }
        if (this.dataType == null || this.dataType == Format.byteArray) {
            return s;
        }
        return s + ", " + this.dataType;
    }
}
