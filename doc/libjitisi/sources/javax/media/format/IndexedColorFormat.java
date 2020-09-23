package javax.media.format;

import javax.media.Format;
import net.sf.fmj.codegen.FormatTraceUtils;
import net.sf.fmj.utility.FormatUtils;
import org.jitsi.android.util.java.awt.Dimension;

public class IndexedColorFormat extends VideoFormat {
    private static String ENCODING = VideoFormat.IRGB;
    protected byte[] blueValues;
    protected byte[] greenValues;
    protected int lineStride;
    protected int mapSize;
    protected byte[] redValues;

    public IndexedColorFormat(Dimension size, int maxDataLength, Class<?> dataType, float frameRate, int lineStride, int mapSize, byte[] red, byte[] green, byte[] blue) {
        super(ENCODING, size, maxDataLength, dataType, frameRate);
        this.lineStride = lineStride;
        this.mapSize = mapSize;
        this.redValues = red;
        this.greenValues = green;
        this.blueValues = blue;
    }

    public Object clone() {
        return new IndexedColorFormat(FormatUtils.clone(this.size), this.maxDataLength, this.dataType, this.frameRate, this.lineStride, this.mapSize, this.redValues, this.greenValues, this.blueValues);
    }

    /* access modifiers changed from: protected */
    public void copy(Format f) {
        super.copy(f);
        IndexedColorFormat oCast = (IndexedColorFormat) f;
        this.lineStride = oCast.lineStride;
        this.mapSize = oCast.mapSize;
        this.redValues = oCast.redValues;
        this.greenValues = oCast.greenValues;
        this.blueValues = oCast.blueValues;
    }

    public boolean equals(Object format) {
        if (!super.equals(format) || !(format instanceof IndexedColorFormat)) {
            return false;
        }
        IndexedColorFormat oCast = (IndexedColorFormat) format;
        if (this.lineStride == oCast.lineStride && this.mapSize == oCast.mapSize && this.redValues == oCast.redValues && this.greenValues == oCast.greenValues && this.blueValues == oCast.blueValues) {
            return true;
        }
        return false;
    }

    public byte[] getBlueValues() {
        return this.blueValues;
    }

    public byte[] getGreenValues() {
        return this.greenValues;
    }

    public int getLineStride() {
        return this.lineStride;
    }

    public int getMapSize() {
        return this.mapSize;
    }

    public byte[] getRedValues() {
        return this.redValues;
    }

    public Format intersects(Format other) {
        Format result = super.intersects(other);
        if (other instanceof IndexedColorFormat) {
            IndexedColorFormat resultCast = (IndexedColorFormat) result;
            IndexedColorFormat oCast = (IndexedColorFormat) other;
            if (getClass().isAssignableFrom(other.getClass())) {
                if (FormatUtils.specified(this.lineStride)) {
                    resultCast.lineStride = this.lineStride;
                }
                if (FormatUtils.specified(this.mapSize)) {
                    resultCast.mapSize = this.mapSize;
                }
                if (FormatUtils.specified(this.redValues)) {
                    resultCast.redValues = this.redValues;
                }
                if (FormatUtils.specified(this.greenValues)) {
                    resultCast.greenValues = this.greenValues;
                }
                if (FormatUtils.specified(this.blueValues)) {
                    resultCast.blueValues = this.blueValues;
                }
            } else if (other.getClass().isAssignableFrom(getClass())) {
                if (!FormatUtils.specified(resultCast.lineStride)) {
                    resultCast.lineStride = oCast.lineStride;
                }
                if (!FormatUtils.specified(resultCast.mapSize)) {
                    resultCast.mapSize = oCast.mapSize;
                }
                if (!FormatUtils.specified(resultCast.redValues)) {
                    resultCast.redValues = oCast.redValues;
                }
                if (!FormatUtils.specified(resultCast.greenValues)) {
                    resultCast.greenValues = oCast.greenValues;
                }
                if (!FormatUtils.specified(resultCast.blueValues)) {
                    resultCast.blueValues = oCast.blueValues;
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
        } else if (format instanceof IndexedColorFormat) {
            IndexedColorFormat oCast = (IndexedColorFormat) format;
            if (FormatUtils.matches(oCast.lineStride, this.lineStride) && FormatUtils.matches(oCast.mapSize, this.mapSize) && FormatUtils.matches(oCast.redValues, this.redValues) && FormatUtils.matches(oCast.greenValues, this.greenValues) && FormatUtils.matches(oCast.blueValues, this.blueValues)) {
                result = true;
            }
            FormatTraceUtils.traceMatches(this, format, result);
            return result;
        } else {
            FormatTraceUtils.traceMatches(this, format, true);
            return true;
        }
    }

    public Format relax() {
        IndexedColorFormat result = (IndexedColorFormat) super.relax();
        result.lineStride = -1;
        return result;
    }
}
