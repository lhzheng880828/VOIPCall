package net.sf.fmj.media.codec.video.colorspace;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import net.sf.fmj.media.BasicCodec;
import net.sf.fmj.media.BasicPlugIn;
import org.jitsi.android.util.java.awt.Dimension;

public class RGBScaler extends BasicCodec {
    private static boolean nativeAvailable = false;
    private int nativeData;
    protected float quality;

    private native void nativeClose();

    private native void nativeScale(Object obj, long j, Object obj2, long j2, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8);

    public RGBScaler() {
        this(null);
    }

    public RGBScaler(Dimension sizeOut) {
        this.quality = 0.5f;
        this.nativeData = 0;
        this.inputFormats = new Format[]{new RGBFormat(null, -1, Format.byteArray, -1.0f, 24, 3, 2, 1, 3, -1, 0, -1)};
        if (sizeOut != null) {
            setOutputSize(sizeOut);
        }
    }

    public void close() {
        super.close();
        if (nativeAvailable && this.nativeData != 0) {
            try {
                nativeClose();
            } catch (Throwable th) {
            }
        }
    }

    public String getName() {
        return "RGB Scaler";
    }

    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return this.outputFormats;
        }
        if (BasicPlugIn.matches(input, this.inputFormats) == null) {
            return new Format[0];
        }
        VideoFormat frameRateFormat = new VideoFormat(null, null, -1, null, ((VideoFormat) input).getFrameRate());
        return new Format[]{this.outputFormats[0].intersects(frameRateFormat)};
    }

    /* access modifiers changed from: protected */
    public void nearestNeighbour(Buffer inBuffer, Buffer outBuffer) {
        Object inObj;
        Object outObj;
        RGBFormat vfIn = (RGBFormat) inBuffer.getFormat();
        Dimension sizeIn = vfIn.getSize();
        RGBFormat vfOut = (RGBFormat) outBuffer.getFormat();
        Dimension sizeOut = vfOut.getSize();
        int pixStrideIn = vfIn.getPixelStride();
        int pixStrideOut = vfOut.getPixelStride();
        int lineStrideIn = vfIn.getLineStride();
        int lineStrideOut = vfOut.getLineStride();
        float horRatio = ((float) sizeIn.width) / ((float) sizeOut.width);
        float verRatio = ((float) sizeIn.height) / ((float) sizeOut.height);
        long inBytes = 0;
        long outBytes = 0;
        if (nativeAvailable) {
            inObj = getInputData(inBuffer);
            outObj = validateData(outBuffer, 0, true);
            inBytes = getNativeData(inObj);
            outBytes = getNativeData(outObj);
        } else {
            inObj = inBuffer.getData();
            outObj = outBuffer.getData();
        }
        if (nativeAvailable) {
            try {
                nativeScale(inObj, inBytes, outObj, outBytes, pixStrideIn, lineStrideIn, sizeIn.width, sizeIn.height, pixStrideOut, lineStrideOut, sizeOut.width, sizeOut.height);
            } catch (Throwable th) {
                nativeAvailable = false;
            }
        }
        if (!nativeAvailable) {
            byte[] inData = (byte[]) inObj;
            byte[] outData = (byte[]) outObj;
            for (int y = 0; y < sizeOut.height; y++) {
                int ptrOut = y * lineStrideOut;
                int ptrIn = ((int) (((float) y) * verRatio)) * lineStrideIn;
                for (int x = 0; x < sizeOut.width; x++) {
                    int ptrIn2 = ptrIn + (((int) (((float) x) * horRatio)) * pixStrideIn);
                    outData[ptrOut] = inData[ptrIn2];
                    outData[ptrOut + 1] = inData[ptrIn2 + 1];
                    outData[ptrOut + 2] = inData[ptrIn2 + 2];
                    ptrOut += pixStrideOut;
                }
            }
        }
    }

    public int process(Buffer inBuffer, Buffer outBuffer) {
        outBuffer.setLength(((VideoFormat) this.outputFormat).getMaxDataLength());
        outBuffer.setFormat(this.outputFormat);
        if (this.quality <= 0.5f) {
            nearestNeighbour(inBuffer, outBuffer);
        }
        return 0;
    }

    public Format setInputFormat(Format input) {
        if (BasicPlugIn.matches(input, this.inputFormats) == null) {
            return null;
        }
        return input;
    }

    public Format setOutputFormat(Format output) {
        if (output != null) {
            if (BasicPlugIn.matches(output, this.outputFormats) != null) {
                RGBFormat incoming = (RGBFormat) output;
                Dimension size = incoming.getSize();
                int maxDataLength = incoming.getMaxDataLength();
                int lineStride = incoming.getLineStride();
                float frameRate = incoming.getFrameRate();
                int flipped = incoming.getFlipped();
                int endian = incoming.getEndian();
                if (size == null) {
                    return null;
                }
                if (maxDataLength < (size.width * size.height) * 3) {
                    maxDataLength = (size.width * size.height) * 3;
                }
                if (lineStride < size.width * 3) {
                    lineStride = size.width * 3;
                }
                if (flipped != 0) {
                }
                this.outputFormat = this.outputFormats[0].intersects(new RGBFormat(size, maxDataLength, null, frameRate, -1, -1, -1, -1, -1, lineStride, -1, -1));
                return this.outputFormat;
            }
        }
        return null;
    }

    public void setOutputSize(Dimension sizeOut) {
        Format[] formatArr = new Format[1];
        formatArr[0] = new RGBFormat(sizeOut, (sizeOut.width * sizeOut.height) * 3, Format.byteArray, -1.0f, 24, 3, 2, 1, 3, sizeOut.width * 3, 0, -1);
        this.outputFormats = formatArr;
    }
}
