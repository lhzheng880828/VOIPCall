package net.sf.fmj.media.util;

import com.lti.utils.UnsignedUtils;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.RGBFormat;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.Graphics;
import org.jitsi.android.util.java.awt.Image;
import org.jitsi.android.util.java.awt.image.BufferedImage;
import org.jitsi.android.util.java.awt.image.ComponentColorModel;
import org.jitsi.android.util.java.awt.image.ComponentSampleModel;
import org.jitsi.android.util.java.awt.image.DataBuffer;
import org.jitsi.android.util.java.awt.image.DataBufferByte;
import org.jitsi.android.util.java.awt.image.DataBufferInt;
import org.jitsi.android.util.java.awt.image.DirectColorModel;

public class ImageToBuffer {
    private static BufferedImage convert(Image im) {
        BufferedImage bi = new BufferedImage(im.getWidth(null), im.getHeight(null), 1);
        Graphics bg = bi.getGraphics();
        bg.drawImage(im, 0, 0, null);
        bg.dispose();
        return bi;
    }

    public static Buffer createBuffer(Image image, float frameRate) {
        BufferedImage bi;
        Object pixels;
        int pixelsLength;
        Class<?> dataType;
        int bitsPerPixel;
        int red;
        int green;
        int blue;
        if (image instanceof BufferedImage) {
            bi = (BufferedImage) image;
        } else {
            bi = convert(image);
        }
        DataBuffer dataBuffer = bi.getRaster().getDataBuffer();
        if (dataBuffer instanceof DataBufferInt) {
            Object intPixels = ((DataBufferInt) dataBuffer).getData();
            pixels = intPixels;
            pixelsLength = intPixels.length;
            dataType = Format.intArray;
        } else if (dataBuffer instanceof DataBufferByte) {
            Object bytePixels = ((DataBufferByte) dataBuffer).getData();
            pixels = bytePixels;
            pixelsLength = bytePixels.length;
            dataType = Format.byteArray;
        } else {
            throw new IllegalArgumentException("Unknown or unsupported data buffer type: " + dataBuffer);
        }
        int bufferedImageType = bi.getType();
        Buffer result = new Buffer();
        Dimension size = new Dimension(bi.getWidth(), bi.getHeight());
        if (bufferedImageType == 5) {
            bitsPerPixel = 24;
            red = 1;
            green = 2;
            blue = 3;
        } else if (bufferedImageType == 4) {
            bitsPerPixel = 32;
            red = UnsignedUtils.MAX_UBYTE;
            green = 65280;
            blue = 16711680;
        } else if (bufferedImageType == 1) {
            bitsPerPixel = 32;
            red = 16711680;
            green = 65280;
            blue = UnsignedUtils.MAX_UBYTE;
        } else if (bufferedImageType == 2) {
            bitsPerPixel = 32;
            red = 16711680;
            green = 65280;
            blue = UnsignedUtils.MAX_UBYTE;
        } else if ((bi.getColorModel() instanceof ComponentColorModel) && (bi.getSampleModel() instanceof ComponentSampleModel)) {
            ComponentColorModel componentColorModel = (ComponentColorModel) bi.getColorModel();
            ComponentSampleModel componentSampleModel = (ComponentSampleModel) bi.getSampleModel();
            int[] offsets = componentSampleModel.getBandOffsets();
            if (dataBuffer instanceof DataBufferInt) {
                bitsPerPixel = 32;
                red = UnsignedUtils.MAX_UBYTE << offsets[0];
                green = UnsignedUtils.MAX_UBYTE << offsets[1];
                blue = UnsignedUtils.MAX_UBYTE << offsets[2];
            } else if (dataBuffer instanceof DataBufferByte) {
                bitsPerPixel = componentSampleModel.getPixelStride() * 8;
                red = offsets[0] + 1;
                green = offsets[1] + 1;
                blue = offsets[2] + 1;
            } else {
                throw new IllegalArgumentException("Unsupported buffered image type: " + bufferedImageType);
            }
        } else if (bi.getColorModel() instanceof DirectColorModel) {
            DirectColorModel directColorModel = (DirectColorModel) bi.getColorModel();
            if (dataBuffer instanceof DataBufferInt) {
                bitsPerPixel = 32;
                red = directColorModel.getRedMask();
                green = directColorModel.getGreenMask();
                blue = directColorModel.getBlueMask();
            } else {
                throw new IllegalArgumentException("Unsupported buffered image type: " + bufferedImageType);
            }
        } else {
            throw new IllegalArgumentException("Unsupported buffered image type: " + bufferedImageType);
        }
        result.setFormat(new RGBFormat(size, -1, dataType, frameRate, bitsPerPixel, red, green, blue));
        result.setData(pixels);
        result.setLength(pixelsLength);
        result.setOffset(0);
        return result;
    }
}
