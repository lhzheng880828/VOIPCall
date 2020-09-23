package com.t4l.jmf;

import com.lti.utils.UnsignedUtils;
import javax.media.format.RGBFormat;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.image.BufferedImage;

public class RGBConverter {
    public static void flipVertical(int[] data, int width, int height) {
        int[] row1 = new int[width];
        int[] row2 = new int[width];
        for (int y = 0; y < height / 2; y++) {
            int offset1 = y * width;
            int offset2 = ((height - 1) - y) * width;
            System.arraycopy(data, offset1, row1, 0, width);
            System.arraycopy(data, offset2, row2, 0, width);
            System.arraycopy(row1, 0, data, offset2, width);
            System.arraycopy(row2, 0, data, offset1, width);
        }
    }

    private static int getShift(int mask) {
        int k = 0;
        for (int i = mask; i != UnsignedUtils.MAX_UBYTE; i /= 2) {
            if (i < UnsignedUtils.MAX_UBYTE) {
                throw new IllegalArgumentException("Unsupported mask: " + Integer.toString(mask, 16));
            }
            k++;
        }
        return k;
    }

    public static void populateArray(BufferedImage image, int[] dest, RGBFormat format) {
        int imageType = image.getType();
        int width = image.getWidth();
        int height = image.getHeight();
        if (format == null) {
            throw new NullPointerException();
        }
        int pixelsPerRow = format.getLineStride();
        if (dest.length < pixelsPerRow * height) {
            throw new IllegalArgumentException("Illegal array size: " + dest.length + "<" + (pixelsPerRow * height));
        }
        if (imageType == 2 || imageType == 3 || imageType == 1) {
            image.getRaster().getDataElements(0, 0, width, height, dest);
        } else {
            image.getRGB(0, 0, width, height, dest, 0, width);
        }
        int rMask = format.getRedMask();
        int gMask = format.getGreenMask();
        int bMask = format.getBlueMask();
        if (!(rMask == 16711680 && gMask == 65280 && bMask == UnsignedUtils.MAX_UBYTE && format.getLineStride() == width && format.getPixelStride() == 1)) {
            int rShift = getShift(rMask);
            int gShift = getShift(gMask);
            int bShift = getShift(bMask);
            int pixelSize = format.getPixelStride();
            for (int y = height - 1; y >= 0; y--) {
                for (int x = width - 1; x >= 0; x--) {
                    int i = (y * width) + x;
                    dest[(y * pixelsPerRow) + (x * pixelSize)] = ((((dest[i] >> 16) & UnsignedUtils.MAX_UBYTE) << rShift) + (((dest[i] >> 8) & UnsignedUtils.MAX_UBYTE) << gShift)) + (((dest[i] >> 0) & UnsignedUtils.MAX_UBYTE) << bShift);
                }
            }
        }
        if (format.getFlipped() == 1) {
            flipVertical(dest, width, height);
        }
    }

    public static void populateImage(int[] array, int offset, BufferedImage image, RGBFormat vf) {
        int targetType;
        int imageType = image.getType();
        if (imageType == 2 || imageType == 3) {
            targetType = 2;
        } else {
            targetType = 1;
        }
        processData(array, offset, vf, targetType);
        int width = image.getWidth();
        int height = image.getHeight();
        if (imageType == 2 || imageType == 1) {
            image.getRaster().setDataElements(0, 0, width, height, array);
        } else {
            image.setRGB(0, 0, width, height, array, 0, width);
        }
    }

    private static void processData(int[] array, int arrayOffset, RGBFormat vf, int targetType) {
        Dimension size = vf.getSize();
        int width = size.width;
        int height = size.height;
        int rMask = vf.getRedMask();
        int gMask = vf.getGreenMask();
        int bMask = vf.getBlueMask();
        int rShift = getShift(rMask);
        int gShift = getShift(gMask);
        int bShift = getShift(bMask);
        int padding = vf.getLineStride() - width;
        if (arrayOffset != 0 || vf.getPixelStride() != 1 || padding != 0 || rMask != 16711680 || gMask != 65280 || bMask != 255) {
            int i = 0;
            int y;
            int base;
            int x;
            int color;
            if (targetType == 2) {
                for (y = 0; y < height; y++) {
                    base = y * width;
                    for (x = 0; x < width; x++) {
                        color = array[i + arrayOffset];
                        array[base + x] = ((-16777216 + (((color >> rShift) & UnsignedUtils.MAX_UBYTE) << 16)) + (((color >> gShift) & UnsignedUtils.MAX_UBYTE) << 8)) + ((color >> bShift) & UnsignedUtils.MAX_UBYTE);
                        i++;
                    }
                    i += padding;
                }
            } else {
                for (y = 0; y < height; y++) {
                    base = y * width;
                    for (x = 0; x < width; x++) {
                        color = array[i + arrayOffset];
                        array[base + x] = ((((color >> rShift) & UnsignedUtils.MAX_UBYTE) << 16) + (((color >> gShift) & UnsignedUtils.MAX_UBYTE) << 8)) + ((color >> bShift) & UnsignedUtils.MAX_UBYTE);
                        i++;
                    }
                    i += padding;
                }
            }
            if (vf.getFlipped() == 1) {
                flipVertical(array, width, height);
            }
        } else if (targetType != 1) {
            int area = width * height;
            for (int a = 0; a < area; a++) {
                array[a] = (array[a] & 16777215) - 16777216;
            }
        }
    }
}
