package com.sun.media.format;

import javax.media.Format;
import javax.media.format.VideoFormat;
import net.sf.fmj.codegen.FormatTraceUtils;
import net.sf.fmj.utility.FormatUtils;
import org.jitsi.android.util.java.awt.Dimension;

public class AviVideoFormat extends VideoFormat {
    protected int bitsPerPixel = -1;
    protected int clrImportant = -1;
    protected int clrUsed = -1;
    protected byte[] codecSpecificHeader;
    protected int imageSize = -1;
    protected int planes = -1;
    protected int xPelsPerMeter = -1;
    protected int yPelsPerMeter = -1;

    public AviVideoFormat(String encoding) {
        super(encoding);
    }

    public AviVideoFormat(String encoding, Dimension size, int maxDataLength, Class<?> dataType, float frameRate, int planes, int bitsPerPixel, int imageSize, int xPelsPerMeter, int yPelsPerMeter, int clrUsed, int clrImportant, byte[] codecHeader) {
        super(encoding, size, maxDataLength, dataType, frameRate);
        this.planes = planes;
        this.bitsPerPixel = bitsPerPixel;
        this.imageSize = imageSize;
        this.xPelsPerMeter = xPelsPerMeter;
        this.yPelsPerMeter = yPelsPerMeter;
        this.clrUsed = clrUsed;
        this.clrImportant = clrImportant;
        this.codecSpecificHeader = codecHeader;
    }

    public Object clone() {
        return new AviVideoFormat(this.encoding, this.size, this.maxDataLength, this.dataType, this.frameRate, this.planes, this.bitsPerPixel, this.imageSize, this.xPelsPerMeter, this.yPelsPerMeter, this.clrUsed, this.clrImportant, this.codecSpecificHeader);
    }

    /* access modifiers changed from: protected */
    public void copy(Format f) {
        super.copy(f);
        AviVideoFormat oCast = (AviVideoFormat) f;
        this.planes = oCast.planes;
        this.bitsPerPixel = oCast.bitsPerPixel;
        this.imageSize = oCast.imageSize;
        this.xPelsPerMeter = oCast.xPelsPerMeter;
        this.yPelsPerMeter = oCast.yPelsPerMeter;
        this.clrUsed = oCast.clrUsed;
        this.clrImportant = oCast.clrImportant;
        this.codecSpecificHeader = oCast.codecSpecificHeader;
    }

    public boolean equals(Object format) {
        if (!super.equals(format) || !(format instanceof AviVideoFormat)) {
            return false;
        }
        AviVideoFormat oCast = (AviVideoFormat) format;
        if (this.planes == oCast.planes && this.bitsPerPixel == oCast.bitsPerPixel && this.imageSize == oCast.imageSize && this.xPelsPerMeter == oCast.xPelsPerMeter && this.yPelsPerMeter == oCast.yPelsPerMeter && this.clrUsed == oCast.clrUsed && this.clrImportant == oCast.clrImportant && FormatUtils.byteArraysEqual(this.codecSpecificHeader, oCast.codecSpecificHeader)) {
            return true;
        }
        return false;
    }

    public int getBitsPerPixel() {
        return this.bitsPerPixel;
    }

    public int getClrImportant() {
        return this.clrImportant;
    }

    public int getClrUsed() {
        return this.clrUsed;
    }

    public byte[] getCodecSpecificHeader() {
        return this.codecSpecificHeader;
    }

    public int getImageSize() {
        return this.imageSize;
    }

    public int getPlanes() {
        return this.planes;
    }

    public int getXPelsPerMeter() {
        return this.xPelsPerMeter;
    }

    public int getYPelsPerMeter() {
        return this.yPelsPerMeter;
    }

    public Format intersects(Format other) {
        Format result = super.intersects(other);
        if (other instanceof AviVideoFormat) {
            AviVideoFormat resultCast = (AviVideoFormat) result;
            AviVideoFormat oCast = (AviVideoFormat) other;
            if (getClass().isAssignableFrom(other.getClass())) {
                if (FormatUtils.specified(this.planes)) {
                    resultCast.planes = this.planes;
                }
                if (FormatUtils.specified(this.bitsPerPixel)) {
                    resultCast.bitsPerPixel = this.bitsPerPixel;
                }
                if (FormatUtils.specified(this.imageSize)) {
                    resultCast.imageSize = this.imageSize;
                }
                if (FormatUtils.specified(this.xPelsPerMeter)) {
                    resultCast.xPelsPerMeter = this.xPelsPerMeter;
                }
                if (FormatUtils.specified(this.yPelsPerMeter)) {
                    resultCast.yPelsPerMeter = this.yPelsPerMeter;
                }
                if (FormatUtils.specified(this.clrUsed)) {
                    resultCast.clrUsed = this.clrUsed;
                }
                if (FormatUtils.specified(this.clrImportant)) {
                    resultCast.clrImportant = this.clrImportant;
                }
                if (FormatUtils.specified(this.codecSpecificHeader)) {
                    resultCast.codecSpecificHeader = this.codecSpecificHeader;
                }
            } else if (other.getClass().isAssignableFrom(getClass())) {
                if (FormatUtils.specified(this.planes)) {
                    resultCast.planes = oCast.planes;
                }
                if (FormatUtils.specified(this.bitsPerPixel)) {
                    resultCast.bitsPerPixel = oCast.bitsPerPixel;
                }
                if (FormatUtils.specified(this.imageSize)) {
                    resultCast.imageSize = oCast.imageSize;
                }
                if (FormatUtils.specified(this.xPelsPerMeter)) {
                    resultCast.xPelsPerMeter = oCast.xPelsPerMeter;
                }
                if (FormatUtils.specified(this.yPelsPerMeter)) {
                    resultCast.yPelsPerMeter = oCast.yPelsPerMeter;
                }
                if (FormatUtils.specified(this.clrUsed)) {
                    resultCast.clrUsed = oCast.clrUsed;
                }
                if (FormatUtils.specified(this.clrImportant)) {
                    resultCast.clrImportant = oCast.clrImportant;
                }
                if (!FormatUtils.specified(resultCast.codecSpecificHeader)) {
                    resultCast.codecSpecificHeader = oCast.codecSpecificHeader;
                }
            }
        }
        return result;
    }

    public boolean matches(Format format) {
        if (!super.matches(format)) {
            return false;
        }
        if (!(format instanceof AviVideoFormat)) {
            return true;
        }
        AviVideoFormat oCast = (AviVideoFormat) format;
        if (FormatUtils.matches(this.planes, oCast.planes) && FormatUtils.matches(this.bitsPerPixel, oCast.bitsPerPixel) && FormatUtils.matches(this.imageSize, oCast.imageSize) && FormatUtils.matches(this.xPelsPerMeter, oCast.xPelsPerMeter) && FormatUtils.matches(this.yPelsPerMeter, oCast.yPelsPerMeter) && FormatUtils.matches(this.clrUsed, oCast.clrUsed) && FormatUtils.matches(this.clrImportant, oCast.clrImportant)) {
            return true;
        }
        return false;
    }

    public Format relax() {
        AviVideoFormat result = (AviVideoFormat) super.relax();
        result.imageSize = -1;
        FormatTraceUtils.traceRelax(this, result);
        return result;
    }

    public String toString() {
        return super.toString() + " " + (this.codecSpecificHeader == null ? 0 : this.codecSpecificHeader.length) + " extra bytes";
    }
}
