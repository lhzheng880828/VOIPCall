package javax.media.format;

import javax.media.Format;
import net.sf.fmj.codegen.FormatTraceUtils;
import net.sf.fmj.utility.FormatUtils;
import org.jitsi.android.util.java.awt.Dimension;

public class H261Format extends VideoFormat {
    private static String ENCODING = VideoFormat.H261;
    protected int stillImageTransmission;

    public H261Format() {
        super(ENCODING);
        this.stillImageTransmission = -1;
        this.dataType = Format.byteArray;
    }

    public H261Format(Dimension size, int maxDataLength, Class<?> dataType, float frameRate, int stillImageTransmission) {
        super(ENCODING, size, maxDataLength, dataType, frameRate);
        this.stillImageTransmission = -1;
        this.stillImageTransmission = stillImageTransmission;
    }

    public Object clone() {
        return new H261Format(FormatUtils.clone(this.size), this.maxDataLength, this.dataType, this.frameRate, this.stillImageTransmission);
    }

    /* access modifiers changed from: protected */
    public void copy(Format f) {
        super.copy(f);
        this.stillImageTransmission = ((H261Format) f).stillImageTransmission;
    }

    public boolean equals(Object format) {
        if (!super.equals(format) || !(format instanceof H261Format)) {
            return false;
        }
        if (this.stillImageTransmission == ((H261Format) format).stillImageTransmission) {
            return true;
        }
        return false;
    }

    public int getStillImageTransmission() {
        return this.stillImageTransmission;
    }

    public Format intersects(Format other) {
        Format result = super.intersects(other);
        if (other instanceof H261Format) {
            H261Format resultCast = (H261Format) result;
            H261Format oCast = (H261Format) other;
            if (getClass().isAssignableFrom(other.getClass())) {
                if (FormatUtils.specified(this.stillImageTransmission)) {
                    resultCast.stillImageTransmission = this.stillImageTransmission;
                }
            } else if (other.getClass().isAssignableFrom(getClass()) && !FormatUtils.specified(resultCast.stillImageTransmission)) {
                resultCast.stillImageTransmission = oCast.stillImageTransmission;
            }
        }
        FormatTraceUtils.traceIntersects(this, other, result);
        return result;
    }

    public boolean matches(Format format) {
        if (!super.matches(format)) {
            FormatTraceUtils.traceMatches(this, format, false);
            return false;
        } else if (format instanceof H261Format) {
            boolean result = FormatUtils.matches(this.stillImageTransmission, ((H261Format) format).stillImageTransmission);
            FormatTraceUtils.traceMatches(this, format, result);
            return result;
        } else {
            FormatTraceUtils.traceMatches(this, format, true);
            return true;
        }
    }

    public String toString() {
        return "H.261 video format";
    }
}
