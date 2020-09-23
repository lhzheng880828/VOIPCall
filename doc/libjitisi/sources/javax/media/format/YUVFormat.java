package javax.media.format;

import javax.media.Format;
import org.jitsi.android.util.java.awt.Dimension;

public class YUVFormat extends VideoFormat {
    private static String ENCODING = VideoFormat.YUV;
    public static final int YUV_111 = 8;
    public static final int YUV_411 = 1;
    public static final int YUV_420 = 2;
    public static final int YUV_422 = 4;
    public static final int YUV_SIGNED = 64;
    public static final int YUV_YUYV = 32;
    public static final int YUV_YVU9 = 16;
    protected int offsetU = -1;
    protected int offsetV = -1;
    protected int offsetY = -1;
    protected int strideUV = -1;
    protected int strideY = -1;
    protected int yuvType = -1;

    public YUVFormat() {
        super(ENCODING);
    }

    public YUVFormat(Dimension size, int maxDataLength, Class<?> dataType, float frameRate, int yuvType, int strideY, int strideUV, int offsetY, int offsetU, int offsetV) {
        super(ENCODING, size, maxDataLength, dataType, frameRate);
        this.yuvType = yuvType;
        this.strideY = strideY;
        this.strideUV = strideUV;
        this.offsetY = offsetY;
        this.offsetU = offsetU;
        this.offsetV = offsetV;
    }

    public YUVFormat(int yuvType) {
        super(ENCODING);
        this.yuvType = yuvType;
    }

    public Object clone() {
        YUVFormat f = new YUVFormat(this.size, this.maxDataLength, this.dataType, this.frameRate, this.yuvType, this.strideY, this.strideUV, this.offsetY, this.offsetU, this.offsetV);
        f.copy(this);
        return f;
    }

    /* access modifiers changed from: protected */
    public void copy(Format f) {
        super.copy(f);
        if (f instanceof YUVFormat) {
            YUVFormat other = (YUVFormat) f;
            this.yuvType = other.yuvType;
            this.strideY = other.strideY;
            this.strideUV = other.strideUV;
            this.offsetY = other.offsetY;
            this.offsetU = other.offsetU;
            this.offsetV = other.offsetV;
        }
    }

    public boolean equals(Object format) {
        if (!(format instanceof YUVFormat)) {
            return false;
        }
        YUVFormat other = (YUVFormat) format;
        if (super.equals(format) && this.yuvType == other.yuvType && this.strideY == other.strideY && this.strideUV == other.strideUV && this.offsetY == other.offsetY && this.offsetU == other.offsetU && this.offsetV == other.offsetV) {
            return true;
        }
        return false;
    }

    public int getOffsetU() {
        return this.offsetU;
    }

    public int getOffsetV() {
        return this.offsetV;
    }

    public int getOffsetY() {
        return this.offsetY;
    }

    public int getStrideUV() {
        return this.strideUV;
    }

    public int getStrideY() {
        return this.strideY;
    }

    public int getYuvType() {
        return this.yuvType;
    }

    public Format intersects(Format format) {
        Format fmt = super.intersects(format);
        if (fmt == null) {
            return null;
        }
        if (!(format instanceof YUVFormat)) {
            return fmt;
        }
        YUVFormat other = (YUVFormat) format;
        Format res = (YUVFormat) fmt;
        res.yuvType = this.yuvType != -1 ? this.yuvType : other.yuvType;
        res.strideY = this.strideY != -1 ? this.strideY : other.strideY;
        res.strideUV = this.strideUV != -1 ? this.strideUV : other.strideUV;
        res.offsetY = this.offsetY != -1 ? this.offsetY : other.offsetY;
        res.offsetU = this.offsetU != -1 ? this.offsetU : other.offsetU;
        res.offsetV = this.offsetV != -1 ? this.offsetV : other.offsetV;
        return res;
    }

    public boolean matches(Format format) {
        if (!super.matches(format)) {
            return false;
        }
        if (!(format instanceof YUVFormat)) {
            return true;
        }
        YUVFormat other = (YUVFormat) format;
        if ((this.yuvType == -1 || other.yuvType == -1 || this.yuvType == other.yuvType) && ((this.strideY == -1 || other.strideY == -1 || this.strideY == other.strideY) && ((this.strideUV == -1 || other.strideUV == -1 || this.strideUV == other.strideUV) && ((this.offsetY == -1 || other.offsetY == -1 || this.offsetY == other.offsetY) && ((this.offsetU == -1 || other.offsetU == -1 || this.offsetU == other.offsetU) && (this.offsetV == -1 || other.offsetV == -1 || this.offsetV == other.offsetV)))))) {
            return true;
        }
        return false;
    }

    public Format relax() {
        YUVFormat fmt = (YUVFormat) super.relax();
        if (fmt == null) {
            return null;
        }
        fmt.strideY = -1;
        fmt.strideUV = -1;
        fmt.offsetY = -1;
        fmt.offsetU = -1;
        fmt.offsetV = -1;
        return fmt;
    }

    public String toString() {
        return "YUV Video Format: Size = " + this.size + " MaxDataLength = " + this.maxDataLength + " DataType = " + this.dataType + " yuvType = " + this.yuvType + " StrideY = " + this.strideY + " StrideUV = " + this.strideUV + " OffsetY = " + this.offsetY + " OffsetU = " + this.offsetU + " OffsetV = " + this.offsetV + "\n";
    }
}
