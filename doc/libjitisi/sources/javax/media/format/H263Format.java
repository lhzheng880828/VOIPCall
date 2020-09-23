package javax.media.format;

import javax.media.Format;
import net.sf.fmj.codegen.FormatTraceUtils;
import net.sf.fmj.utility.FormatUtils;
import org.jitsi.android.util.java.awt.Dimension;

public class H263Format extends VideoFormat {
    private static String ENCODING = VideoFormat.H263;
    protected int advancedPrediction;
    protected int arithmeticCoding;
    protected int errorCompensation;
    protected int hrDB;
    protected int pbFrames;
    protected int unrestrictedVector;

    public H263Format() {
        super(ENCODING);
        this.advancedPrediction = -1;
        this.arithmeticCoding = -1;
        this.errorCompensation = -1;
        this.hrDB = -1;
        this.pbFrames = -1;
        this.unrestrictedVector = -1;
        this.dataType = Format.byteArray;
    }

    public H263Format(Dimension size, int maxDataLength, Class<?> dataType, float frameRate, int advancedPrediction, int arithmeticCoding, int errorCompensation, int hrDB, int pbFrames, int unrestrictedVector) {
        super(ENCODING, size, maxDataLength, dataType, frameRate);
        this.advancedPrediction = -1;
        this.arithmeticCoding = -1;
        this.errorCompensation = -1;
        this.hrDB = -1;
        this.pbFrames = -1;
        this.unrestrictedVector = -1;
        this.advancedPrediction = advancedPrediction;
        this.arithmeticCoding = arithmeticCoding;
        this.errorCompensation = errorCompensation;
        this.hrDB = hrDB;
        this.pbFrames = pbFrames;
        this.unrestrictedVector = unrestrictedVector;
    }

    public Object clone() {
        return new H263Format(FormatUtils.clone(this.size), this.maxDataLength, this.dataType, this.frameRate, this.advancedPrediction, this.arithmeticCoding, this.errorCompensation, this.hrDB, this.pbFrames, this.unrestrictedVector);
    }

    /* access modifiers changed from: protected */
    public void copy(Format f) {
        super.copy(f);
        H263Format oCast = (H263Format) f;
        this.advancedPrediction = oCast.advancedPrediction;
        this.arithmeticCoding = oCast.arithmeticCoding;
        this.errorCompensation = oCast.errorCompensation;
        this.hrDB = oCast.hrDB;
        this.pbFrames = oCast.pbFrames;
        this.unrestrictedVector = oCast.unrestrictedVector;
    }

    public boolean equals(Object format) {
        if (!super.equals(format) || !(format instanceof H263Format)) {
            return false;
        }
        H263Format oCast = (H263Format) format;
        if (this.advancedPrediction == oCast.advancedPrediction && this.arithmeticCoding == oCast.arithmeticCoding && this.errorCompensation == oCast.errorCompensation && this.hrDB == oCast.hrDB && this.pbFrames == oCast.pbFrames && this.unrestrictedVector == oCast.unrestrictedVector) {
            return true;
        }
        return false;
    }

    public int getAdvancedPrediction() {
        return this.advancedPrediction;
    }

    public int getArithmeticCoding() {
        return this.arithmeticCoding;
    }

    public int getErrorCompensation() {
        return this.errorCompensation;
    }

    public int getHrDB() {
        return this.hrDB;
    }

    public int getPBFrames() {
        return this.pbFrames;
    }

    public int getUnrestrictedVector() {
        return this.unrestrictedVector;
    }

    public Format intersects(Format other) {
        Format result = super.intersects(other);
        if (other instanceof H263Format) {
            H263Format resultCast = (H263Format) result;
            H263Format oCast = (H263Format) other;
            if (getClass().isAssignableFrom(other.getClass())) {
                if (FormatUtils.specified(this.advancedPrediction)) {
                    resultCast.advancedPrediction = this.advancedPrediction;
                }
                if (FormatUtils.specified(this.arithmeticCoding)) {
                    resultCast.arithmeticCoding = this.arithmeticCoding;
                }
                if (FormatUtils.specified(this.errorCompensation)) {
                    resultCast.errorCompensation = this.errorCompensation;
                }
                if (FormatUtils.specified(this.hrDB)) {
                    resultCast.hrDB = this.hrDB;
                }
                if (FormatUtils.specified(this.pbFrames)) {
                    resultCast.pbFrames = this.pbFrames;
                }
                if (FormatUtils.specified(this.unrestrictedVector)) {
                    resultCast.unrestrictedVector = this.unrestrictedVector;
                }
            } else if (other.getClass().isAssignableFrom(getClass())) {
                if (!FormatUtils.specified(resultCast.advancedPrediction)) {
                    resultCast.advancedPrediction = oCast.advancedPrediction;
                }
                if (!FormatUtils.specified(resultCast.arithmeticCoding)) {
                    resultCast.arithmeticCoding = oCast.arithmeticCoding;
                }
                if (!FormatUtils.specified(resultCast.errorCompensation)) {
                    resultCast.errorCompensation = oCast.errorCompensation;
                }
                if (!FormatUtils.specified(resultCast.hrDB)) {
                    resultCast.hrDB = oCast.hrDB;
                }
                if (!FormatUtils.specified(resultCast.pbFrames)) {
                    resultCast.pbFrames = oCast.pbFrames;
                }
                if (!FormatUtils.specified(resultCast.unrestrictedVector)) {
                    resultCast.unrestrictedVector = oCast.unrestrictedVector;
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
        } else if (format instanceof H263Format) {
            H263Format oCast = (H263Format) format;
            if (FormatUtils.matches(this.advancedPrediction, oCast.advancedPrediction) && FormatUtils.matches(this.arithmeticCoding, oCast.arithmeticCoding) && FormatUtils.matches(this.errorCompensation, oCast.errorCompensation) && FormatUtils.matches(this.hrDB, oCast.hrDB) && FormatUtils.matches(this.pbFrames, oCast.pbFrames) && FormatUtils.matches(this.unrestrictedVector, oCast.unrestrictedVector)) {
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
        return "H.263 video format";
    }
}
