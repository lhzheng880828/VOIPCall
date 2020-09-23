package net.sf.fmj.media.util;

import com.lti.utils.UnsignedUtils;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import org.jitsi.android.util.java.awt.Image;
import org.jitsi.android.util.java.awt.Point;
import org.jitsi.android.util.java.awt.color.ColorSpace;
import org.jitsi.android.util.java.awt.image.BufferedImage;
import org.jitsi.android.util.java.awt.image.ComponentColorModel;
import org.jitsi.android.util.java.awt.image.ComponentSampleModel;
import org.jitsi.android.util.java.awt.image.DataBufferByte;
import org.jitsi.android.util.java.awt.image.DataBufferInt;
import org.jitsi.android.util.java.awt.image.DataBufferUShort;
import org.jitsi.android.util.java.awt.image.DirectColorModel;
import org.jitsi.android.util.java.awt.image.Raster;
import org.jitsi.android.util.java.awt.image.SinglePixelPackedSampleModel;

public class BufferToImage {
    public BufferToImage(VideoFormat format) {
    }

    public BufferedImage createBufferedImage(Buffer buffer) {
        VideoFormat format = (VideoFormat) buffer.getFormat();
        int w = format.getSize().width;
        int h = format.getSize().height;
        Class<?> dataType = format.getDataType();
        if (format instanceof RGBFormat) {
            RGBFormat rgbFormat = (RGBFormat) format;
            int bitsPerPixel = rgbFormat.getBitsPerPixel();
            int redMask = rgbFormat.getRedMask();
            int greenMask = rgbFormat.getGreenMask();
            int blueMask = rgbFormat.getBlueMask();
            int lineStride = rgbFormat.getLineStride();
            int pixelStride = rgbFormat.getPixelStride();
            boolean flipped = rgbFormat.getFlipped() == 1;
            if (dataType == Format.byteArray) {
                byte[] bytes = (byte[]) buffer.getData();
                if (bitsPerPixel == 24) {
                    return new BufferedImage(new ComponentColorModel(ColorSpace.getInstance(1000), new int[]{8, 8, 8}, false, false, 1, 0), Raster.createWritableRaster(new ComponentSampleModel(0, w, h, pixelStride, lineStride, new int[]{redMask - 1, greenMask - 1, blueMask - 1}), new DataBufferByte(new byte[][]{bytes}, bytes.length), new Point(0, 0)), false, null);
                } else if (bitsPerPixel == 32) {
                    return new BufferedImage(new ComponentColorModel(ColorSpace.getInstance(1000), new int[]{8, 8, 8, 8}, true, false, 3, 0), Raster.createWritableRaster(new ComponentSampleModel(0, w, h, pixelStride, lineStride, new int[]{redMask - 1, greenMask - 1, blueMask - 1, 3}), new DataBufferByte(new byte[][]{bytes}, bytes.length), new Point(0, 0)), false, null);
                } else if (bitsPerPixel == 8) {
                    return new BufferedImage(new DirectColorModel(bitsPerPixel, redMask, greenMask, blueMask), Raster.createWritableRaster(new SinglePixelPackedSampleModel(0, w, h, lineStride, new int[]{redMask, greenMask, blueMask}), new DataBufferByte(new byte[][]{bytes}, bytes.length), new Point(0, 0)), false, null);
                } else {
                    BufferedImage bi = new BufferedImage(w, h, 1);
                    int[] pixels = new int[(w * h)];
                    int pixelIndex = 0;
                    int lineOffset = 0;
                    if (flipped) {
                        lineOffset = (h - 1) * lineStride;
                    }
                    int y = 0;
                    while (y < h) {
                        int pixelIndex2;
                        int off = lineOffset;
                        int x = 0;
                        while (true) {
                            pixelIndex2 = pixelIndex;
                            if (x >= w) {
                                break;
                            }
                            pixelIndex = pixelIndex2 + 1;
                            pixels[pixelIndex2] = ((((0 + (bytes[(off + redMask) - 1] & UnsignedUtils.MAX_UBYTE)) * 256) + (bytes[(off + greenMask) - 1] & UnsignedUtils.MAX_UBYTE)) * 256) + (bytes[(off + blueMask) - 1] & UnsignedUtils.MAX_UBYTE);
                            off += pixelStride;
                            x++;
                        }
                        if (flipped) {
                            lineOffset -= lineStride;
                        } else {
                            lineOffset += lineStride;
                        }
                        y++;
                        pixelIndex = pixelIndex2;
                    }
                    bi.setRGB(0, 0, w, h, pixels, 0, w);
                    return bi;
                }
            } else if (dataType == Format.shortArray) {
                short[] shorts = (short[]) buffer.getData();
                if (bitsPerPixel == 16) {
                    return new BufferedImage(new DirectColorModel(bitsPerPixel, redMask, greenMask, blueMask), Raster.createWritableRaster(new SinglePixelPackedSampleModel(1, w, h, lineStride, new int[]{redMask, greenMask, blueMask}), new DataBufferUShort(new short[][]{shorts}, shorts.length), new Point(0, 0)), false, null);
                }
                throw new UnsupportedOperationException();
            } else if (dataType == Format.intArray) {
                return new BufferedImage(new DirectColorModel(24, redMask, greenMask, blueMask, 0), Raster.createWritableRaster(new SinglePixelPackedSampleModel(3, w, h, new int[]{redMask, greenMask, blueMask}), new DataBufferInt(new int[][]{(int[]) buffer.getData()}, ((int[]) buffer.getData()).length), new Point(0, 0)), false, null);
            } else {
                throw new UnsupportedOperationException();
            }
        }
        throw new UnsupportedOperationException();
    }

    public Image createImage(Buffer buffer) {
        return createBufferedImage(buffer);
    }
}
