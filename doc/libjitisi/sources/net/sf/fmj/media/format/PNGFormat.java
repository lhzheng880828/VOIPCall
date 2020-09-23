package net.sf.fmj.media.format;

import javax.media.Format;
import javax.media.format.VideoFormat;
import net.sf.fmj.codegen.FormatTraceUtils;
import net.sf.fmj.media.BonusVideoFormatEncodings;
import net.sf.fmj.utility.FormatUtils;
import org.jitsi.android.util.java.awt.Dimension;

public class PNGFormat extends VideoFormat {
    public PNGFormat() {
        super(BonusVideoFormatEncodings.PNG);
        this.dataType = Format.byteArray;
    }

    public PNGFormat(Dimension size, int maxDataLength, Class<?> dataType, float frameRate) {
        super(BonusVideoFormatEncodings.PNG, size, maxDataLength, dataType, frameRate);
    }

    public Object clone() {
        return new PNGFormat(FormatUtils.clone(this.size), this.maxDataLength, this.dataType, this.frameRate);
    }

    /* access modifiers changed from: protected */
    public void copy(Format f) {
        super.copy(f);
        PNGFormat oCast = (PNGFormat) f;
    }

    public boolean equals(Object format) {
        if (!super.equals(format) || !(format instanceof PNGFormat)) {
            return false;
        }
        PNGFormat oCast = (PNGFormat) format;
        return true;
    }

    public Format intersects(Format other) {
        Format result = super.intersects(other);
        if (other instanceof PNGFormat) {
            PNGFormat resultCast = (PNGFormat) result;
            PNGFormat oCast = (PNGFormat) other;
            if (!getClass().isAssignableFrom(other.getClass()) && other.getClass().isAssignableFrom(getClass())) {
            }
        }
        FormatTraceUtils.traceIntersects(this, other, result);
        return result;
    }

    public boolean matches(Format format) {
        if (!super.matches(format)) {
            FormatTraceUtils.traceMatches(this, format, false);
            return false;
        } else if (format instanceof PNGFormat) {
            PNGFormat oCast = (PNGFormat) format;
            FormatTraceUtils.traceMatches(this, format, true);
            return true;
        } else {
            FormatTraceUtils.traceMatches(this, format, true);
            return true;
        }
    }

    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("PNG video format:");
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
        return b.toString();
    }
}
