package org.jitsi.impl.neomedia.codec.video;

import com.lti.utils.UnsignedUtils;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.format.YUVFormat;
import net.sf.fmj.media.AbstractCodec;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.codec.FFmpeg;
import org.jitsi.impl.neomedia.control.FrameProcessingControlImpl;
import org.jitsi.util.Logger;

public class SwScale extends AbstractCodec {
    public static final int MIN_SWS_SCALE_HEIGHT_OR_WIDTH = 4;
    private static final Logger logger = Logger.getLogger(SwScale.class);
    private final boolean fixOddYuv420Size;
    private final FrameProcessingControlImpl frameProcessingControl;
    private final boolean preserveAspectRatio;
    private Dimension preserveAspectRatioCachedIn;
    private Dimension preserveAspectRatioCachedOut;
    private Dimension preserveAspectRatioCachedRet;
    private VideoFormat[] supportedOutputFormats;
    private long swsContext;

    private static int getFFmpegPixelFormat(RGBFormat rgb) {
        switch (rgb.getBitsPerPixel()) {
            case 24:
                return FFmpeg.PIX_FMT_RGB24;
            case 32:
                switch (rgb.getRedMask()) {
                    case -16777216:
                    case 4:
                        return FFmpeg.PIX_FMT_RGB32_1;
                    case 1:
                    case UnsignedUtils.MAX_UBYTE /*255*/:
                        return FFmpeg.PIX_FMT_BGR32;
                    case 2:
                    case 65280:
                        return FFmpeg.PIX_FMT_BGR32_1;
                    case 3:
                    case 16711680:
                        return FFmpeg.PIX_FMT_RGB32;
                    default:
                        return -1;
                }
            default:
                return -1;
        }
    }

    private static VideoFormat setSize(VideoFormat format, Dimension size) {
        if (format instanceof RGBFormat) {
            int i;
            RGBFormat rgbFormat = (RGBFormat) format;
            Class<?> dataType = format.getDataType();
            int bitsPerPixel = rgbFormat.getBitsPerPixel();
            int pixelStride = rgbFormat.getPixelStride();
            if (!(pixelStride != -1 || dataType == null || bitsPerPixel == -1)) {
                pixelStride = dataType.equals(Format.byteArray) ? bitsPerPixel / 8 : 1;
            }
            float frameRate = format.getFrameRate();
            int redMask = rgbFormat.getRedMask();
            int greenMask = rgbFormat.getGreenMask();
            int blueMask = rgbFormat.getBlueMask();
            if (pixelStride == -1 || size == null) {
                i = -1;
            } else {
                i = pixelStride * size.width;
            }
            return new RGBFormat(size, -1, dataType, frameRate, bitsPerPixel, redMask, greenMask, blueMask, pixelStride, i, rgbFormat.getFlipped(), rgbFormat.getEndian());
        } else if (format instanceof YUVFormat) {
            return new YUVFormat(size, -1, format.getDataType(), format.getFrameRate(), ((YUVFormat) format).getYuvType(), -1, -1, 0, -1, -1);
        } else if (format == null) {
            return format;
        } else {
            logger.warn("SwScale outputFormat of type " + format.getClass().getName() + " is not supported for optimized scaling.");
            return format;
        }
    }

    public SwScale() {
        this(false);
    }

    public SwScale(boolean fixOddYuv420Size) {
        this(fixOddYuv420Size, false);
    }

    public SwScale(boolean fixOddYuv420Size, boolean preserveAspectRatio) {
        this.frameProcessingControl = new FrameProcessingControlImpl();
        this.supportedOutputFormats = new VideoFormat[]{new RGBFormat(), new YUVFormat(2)};
        this.swsContext = 0;
        this.fixOddYuv420Size = fixOddYuv420Size;
        this.preserveAspectRatio = preserveAspectRatio;
        this.inputFormats = new Format[]{new AVFrameFormat(), new RGBFormat(), new YUVFormat(2)};
        addControl(this.frameProcessingControl);
    }

    public void close() {
        try {
            if (this.swsContext != 0) {
                FFmpeg.sws_freeContext(this.swsContext);
                this.swsContext = 0;
            }
            super.close();
        } catch (Throwable th) {
            super.close();
        }
    }

    public Format getInputFormat() {
        return super.getInputFormat();
    }

    public Dimension getOutputSize() {
        Format outputFormat = getOutputFormat();
        if (outputFormat == null) {
            outputFormat = this.supportedOutputFormats[0];
        }
        return ((VideoFormat) outputFormat).getSize();
    }

    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return this.supportedOutputFormats;
        }
        Dimension size = this.supportedOutputFormats[0].getSize();
        VideoFormat videoInput = (VideoFormat) input;
        if (size == null) {
            size = videoInput.getSize();
        }
        float frameRate = videoInput.getFrameRate();
        return new Format[]{new RGBFormat(size, -1, null, frameRate, 32, -1, -1, -1), new YUVFormat(size, -1, null, frameRate, 2, -1, -1, -1, -1, -1)};
    }

    private Dimension preserveAspectRatio(Dimension in, Dimension out) {
        int inHeight = in.height;
        int inWidth = in.width;
        int outHeight = out.height;
        int outWidth = out.width;
        if (this.preserveAspectRatioCachedIn != null && this.preserveAspectRatioCachedOut != null && this.preserveAspectRatioCachedIn.height == inHeight && this.preserveAspectRatioCachedIn.width == inWidth && this.preserveAspectRatioCachedOut.height == outHeight && this.preserveAspectRatioCachedOut.width == outWidth && this.preserveAspectRatioCachedRet != null) {
            return this.preserveAspectRatioCachedRet;
        }
        double heightRatio;
        double widthRatio;
        boolean scale = false;
        if (outHeight == inHeight || outHeight <= 0) {
            heightRatio = 1.0d;
        } else {
            scale = true;
            heightRatio = ((double) inHeight) / ((double) outHeight);
        }
        if (outWidth == inWidth || outWidth <= 0) {
            widthRatio = 1.0d;
        } else {
            scale = true;
            widthRatio = ((double) inWidth) / ((double) outWidth);
        }
        Dimension ret = out;
        if (scale) {
            double ratio = Math.min(heightRatio, widthRatio);
            int retHeight = (int) (((double) outHeight) * ratio);
            int retWidth = (int) (((double) outWidth) * ratio);
            if ((Math.abs(retHeight - outHeight) > 1 || Math.abs(retWidth - outWidth) > 1) && (retHeight < 4 || retWidth < 4)) {
                ret = new Dimension(retWidth, retHeight);
                this.preserveAspectRatioCachedRet = ret;
            }
        }
        this.preserveAspectRatioCachedIn = new Dimension(inWidth, inHeight);
        this.preserveAspectRatioCachedOut = new Dimension(outWidth, outHeight);
        if (ret != out) {
            return ret;
        }
        this.preserveAspectRatioCachedRet = this.preserveAspectRatioCachedOut;
        return ret;
    }

    public int process(Buffer in, Buffer out) {
        if (!checkInputBuffer(in)) {
            return 1;
        }
        if (isEOM(in)) {
            propagateEOM(out);
            return 0;
        } else if (in.isDiscard() || this.frameProcessingControl.isMinimalProcessing()) {
            out.setDiscard(true);
            return 0;
        } else {
            Format inFormat = (VideoFormat) in.getFormat();
            Format thisInFormat = getInputFormat();
            if (!(inFormat == thisInFormat || inFormat.equals(thisInFormat))) {
                setInputFormat(inFormat);
            }
            Dimension inSize = inFormat.getSize();
            if (inSize == null) {
                return 1;
            }
            int inWidth = inSize.width;
            int inHeight = inSize.height;
            if (inWidth < 4 || inHeight < 4) {
                return 4;
            }
            VideoFormat outFormat = (VideoFormat) getOutputFormat();
            if (outFormat == null) {
                outFormat = (VideoFormat) out.getFormat();
                if (outFormat == null) {
                    return 1;
                }
            }
            Dimension outSize = outFormat.getSize();
            if (outSize == null) {
                outSize = inSize;
            } else if (this.preserveAspectRatio) {
                outSize = preserveAspectRatio(inSize, outSize);
            }
            int outWidth = outSize.width;
            int outHeight = outSize.height;
            if (outWidth < 4 || outHeight < 4) {
                return 4;
            }
            Format outFormat2 = setSize(outFormat, outSize);
            if (outFormat2 == null) {
                return 1;
            }
            int dstFmt;
            int dstLength;
            int srcFmt;
            long srcPicture;
            if (outFormat2 instanceof RGBFormat) {
                dstFmt = getFFmpegPixelFormat((RGBFormat) outFormat2);
                dstLength = (outWidth * outHeight) * 4;
            } else if (!(outFormat2 instanceof YUVFormat)) {
                return 1;
            } else {
                dstFmt = 0;
                dstLength = (outWidth * outHeight) + ((((outWidth + 1) / 2) * 2) * ((outHeight + 1) / 2));
            }
            Class<?> outDataType = outFormat2.getDataType();
            Object dst = out.getData();
            if (Format.byteArray.equals(outDataType)) {
                if (dst == null || ((byte[]) dst).length < dstLength) {
                    dst = new byte[dstLength];
                }
            } else if (Format.intArray.equals(outDataType)) {
                dstLength = (dstLength / 4) + (dstLength % 4 == 0 ? 0 : 1);
                if (dst == null || ((int[]) dst).length < dstLength) {
                    dst = new int[dstLength];
                }
            } else if (Format.shortArray.equals(outDataType)) {
                dstLength = (dstLength / 2) + (dstLength % 2 == 0 ? 0 : 1);
                if (dst == null || ((short[]) dst).length < dstLength) {
                    dst = new short[dstLength];
                }
            } else {
                logger.error("Unsupported output data type " + outDataType);
                return 1;
            }
            Object src = in.getData();
            if (src instanceof AVFrame) {
                srcFmt = ((AVFrameFormat) inFormat).getPixFmt();
                srcPicture = ((AVFrame) src).getPtr();
            } else {
                srcFmt = inFormat instanceof YUVFormat ? 0 : getFFmpegPixelFormat((RGBFormat) inFormat);
                srcPicture = 0;
            }
            this.swsContext = FFmpeg.sws_getCachedContext(this.swsContext, inWidth, inHeight, srcFmt, outWidth, outHeight, dstFmt, 4);
            if (srcPicture == 0) {
                FFmpeg.sws_scale(this.swsContext, src, srcFmt, inWidth, inHeight, 0, inHeight, dst, dstFmt, outWidth, outHeight);
            } else {
                FFmpeg.sws_scale(this.swsContext, srcPicture, 0, inHeight, dst, dstFmt, outWidth, outHeight);
            }
            out.setData(dst);
            out.setDuration(in.getDuration());
            out.setFlags(in.getFlags());
            out.setFormat(outFormat2);
            out.setLength(dstLength);
            out.setOffset(0);
            out.setSequenceNumber(in.getSequenceNumber());
            out.setTimeStamp(in.getTimeStamp());
            int inFlags = in.getFlags();
            int outFlags = out.getFlags();
            if ((32768 & inFlags) != 0) {
                outFlags |= 32768;
            }
            if ((inFlags & 64) != 0) {
                outFlags |= 64;
            }
            if ((inFlags & 256) != 0) {
                outFlags |= 256;
            }
            if ((inFlags & 4096) != 0) {
                outFlags |= 4096;
            }
            if ((inFlags & 128) != 0) {
                outFlags |= 128;
            }
            out.setFlags(outFlags);
            return 0;
        }
    }

    public Format setInputFormat(Format format) {
        Format inputFormat = format instanceof VideoFormat ? super.setInputFormat(format) : null;
        if (inputFormat != null && logger.isDebugEnabled()) {
            logger.debug(getClass().getName() + " 0x" + Integer.toHexString(hashCode()) + " set to input in " + inputFormat);
        }
        return inputFormat;
    }

    public Format setOutputFormat(Format format) {
        if (this.fixOddYuv420Size && (format instanceof YUVFormat)) {
            YUVFormat yuvFormat = (YUVFormat) format;
            if (2 == yuvFormat.getYuvType()) {
                Dimension size = yuvFormat.getSize();
                if (size != null && size.width > 2 && size.height > 2) {
                    int width = (size.width >> 1) << 1;
                    int height = (size.height >> 1) << 1;
                    if (!(width == size.width && height == size.height)) {
                        Format yUVFormat = new YUVFormat(new Dimension(width, height), -1, yuvFormat.getDataType(), yuvFormat.getFrameRate(), yuvFormat.getYuvType(), -1, -1, 0, -1, -1);
                    }
                }
            }
        }
        Format outputFormat = super.setOutputFormat(format);
        if (outputFormat != null && logger.isDebugEnabled()) {
            logger.debug(getClass().getName() + " 0x" + Integer.toHexString(hashCode()) + " set to output in " + outputFormat);
        }
        return outputFormat;
    }

    private void setOutputFormatSize(Dimension size) {
        VideoFormat outputFormat = (VideoFormat) getOutputFormat();
        if (outputFormat != null) {
            outputFormat = setSize(outputFormat, size);
            if (outputFormat != null) {
                setOutputFormat(outputFormat);
            }
        }
    }

    public void setOutputSize(Dimension size) {
        if (size == null || (size.height >= 4 && size.width >= 4)) {
            for (int i = 0; i < this.supportedOutputFormats.length; i++) {
                this.supportedOutputFormats[i] = setSize(this.supportedOutputFormats[i], size);
            }
            setOutputFormatSize(size);
        }
    }
}
