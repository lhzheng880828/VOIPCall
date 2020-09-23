package org.jitsi.impl.neomedia.imgstreaming;

import org.jitsi.android.util.java.awt.geom.AffineTransform;
import org.jitsi.android.util.java.awt.image.AffineTransformOp;
import org.jitsi.android.util.java.awt.image.BufferedImage;
import org.jitsi.android.util.java.awt.image.WritableRaster;

public class ImgStreamingUtils {
    public static BufferedImage getScaledImage(BufferedImage src, int width, int height, int type) {
        double scaleWidth = ((double) width) / ((double) src.getWidth());
        double scaleHeight = ((double) height) / ((double) src.getHeight());
        AffineTransform tx = new AffineTransform();
        if (!(Double.compare(scaleWidth, 1.0d) == 0 && Double.compare(scaleHeight, 1.0d) == 0)) {
            tx.scale(scaleWidth, scaleHeight);
        }
        return new AffineTransformOp(tx, 2).filter(src, new BufferedImage(width, height, type));
    }

    public static byte[] getImageBytes(BufferedImage src, byte[] output) {
        if (src.getType() != 2) {
            throw new IllegalArgumentException("src.type");
        }
        byte[] data;
        WritableRaster raster = src.getRaster();
        int width = src.getWidth();
        int height = src.getHeight();
        int size = (width * height) * 4;
        int off = 0;
        int[] pixel = new int[4];
        if (output == null || output.length < size) {
            data = new byte[size];
        } else {
            data = output;
        }
        int y = 0;
        while (y < height) {
            int off2 = off;
            for (int x = 0; x < width; x++) {
                raster.getPixel(x, y, pixel);
                off = off2 + 1;
                data[off2] = (byte) pixel[0];
                off2 = off + 1;
                data[off] = (byte) pixel[1];
                off = off2 + 1;
                data[off2] = (byte) pixel[2];
                off2 = off + 1;
                data[off] = (byte) pixel[3];
            }
            y++;
            off = off2;
        }
        return data;
    }
}
