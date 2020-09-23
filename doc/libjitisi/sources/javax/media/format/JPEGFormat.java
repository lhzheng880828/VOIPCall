package javax.media.format;

import javax.media.Format;
import net.sf.fmj.codegen.FormatTraceUtils;
import net.sf.fmj.utility.FormatUtils;
import org.jitsi.android.util.java.awt.Dimension;

public class JPEGFormat extends VideoFormat {
    public static final int DEC_402 = 3;
    public static final int DEC_411 = 4;
    public static final int DEC_420 = 1;
    public static final int DEC_422 = 0;
    public static final int DEC_444 = 2;
    int decimation;
    int qFactor;

    public JPEGFormat() {
        super(VideoFormat.JPEG);
        this.qFactor = -1;
        this.decimation = -1;
        this.dataType = Format.byteArray;
    }

    public JPEGFormat(Dimension size, int maxDataLength, Class<?> dataType, float frameRate, int q, int dec) {
        super(VideoFormat.JPEG, size, maxDataLength, dataType, frameRate);
        this.qFactor = -1;
        this.decimation = -1;
        this.qFactor = q;
        this.decimation = dec;
    }

    public Object clone() {
        return new JPEGFormat(FormatUtils.clone(this.size), this.maxDataLength, this.dataType, this.frameRate, this.qFactor, this.decimation);
    }

    /* access modifiers changed from: protected */
    public void copy(Format f) {
        super.copy(f);
        JPEGFormat oCast = (JPEGFormat) f;
        this.qFactor = oCast.qFactor;
        this.decimation = oCast.decimation;
    }

    public boolean equals(Object format) {
        if (!super.equals(format) || !(format instanceof JPEGFormat)) {
            return false;
        }
        JPEGFormat oCast = (JPEGFormat) format;
        if (this.qFactor == oCast.qFactor && this.decimation == oCast.decimation) {
            return true;
        }
        return false;
    }

    public int getDecimation() {
        return this.decimation;
    }

    public int getQFactor() {
        return this.qFactor;
    }

    public Format intersects(Format other) {
        Format result = super.intersects(other);
        if (other instanceof JPEGFormat) {
            JPEGFormat resultCast = (JPEGFormat) result;
            JPEGFormat oCast = (JPEGFormat) other;
            if (getClass().isAssignableFrom(other.getClass())) {
                if (FormatUtils.specified(this.qFactor)) {
                    resultCast.qFactor = this.qFactor;
                }
                if (FormatUtils.specified(this.decimation)) {
                    resultCast.decimation = this.decimation;
                }
            } else if (other.getClass().isAssignableFrom(getClass())) {
                if (!FormatUtils.specified(resultCast.qFactor)) {
                    resultCast.qFactor = oCast.qFactor;
                }
                if (!FormatUtils.specified(resultCast.decimation)) {
                    resultCast.decimation = oCast.decimation;
                }
            }
        }
        FormatTraceUtils.traceIntersects(this, other, result);
        return result;
    }

    public boolean matches(Format format) {
        boolean result = false;
        if (!super.matches(format)) {
            FormatTraceUtils.traceMatches(this, format, false);
            return false;
        } else if (format instanceof JPEGFormat) {
            JPEGFormat oCast = (JPEGFormat) format;
            if (FormatUtils.matches(oCast.qFactor, this.qFactor) && FormatUtils.matches(oCast.decimation, this.decimation)) {
                result = true;
            }
            FormatTraceUtils.traceMatches(this, format, result);
            return result;
        } else {
            FormatTraceUtils.traceMatches(this, format, true);
            return true;
        }
    }

    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("jpeg video format:");
        if (FormatUtils.specified(this.size)) {
            b.append(" size = " + this.size.width + "x" + this.size.height);
        }
        if (FormatUtils.specified(this.frameRate)) {
            b.append(" FrameRate = " + this.frameRate);
        }
        if (FormatUtils.specified(this.maxDataLength)) {
            b.append(" maxDataLength = " + this.maxDataLength);
        }
        if (FormatUtils.specified(this.dataType)) {
            b.append(" dataType = " + this.dataType);
        }
        if (FormatUtils.specified(this.qFactor)) {
            b.append(" q factor = " + this.qFactor);
        }
        if (FormatUtils.specified(this.decimation)) {
            b.append(" decimation = " + this.decimation);
        }
        return b.toString();
    }
}
