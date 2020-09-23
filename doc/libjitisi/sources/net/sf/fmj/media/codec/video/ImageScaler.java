package net.sf.fmj.media.codec.video;

import javax.media.Buffer;
import javax.media.Codec;
import javax.media.Format;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import net.sf.fmj.media.AbstractCodec;
import net.sf.fmj.media.util.BufferToImage;
import net.sf.fmj.media.util.ImageToBuffer;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.geom.AffineTransform;
import org.jitsi.android.util.java.awt.image.AffineTransformOp;
import org.jitsi.android.util.java.awt.image.BufferedImage;

public class ImageScaler extends AbstractCodec implements Codec {
    private final Dimension DIMENSION = null;
    private BufferToImage bufferToImage;
    private final Format[] supportedInputFormats = new Format[]{new RGBFormat(null, -1, Format.byteArray, -1.0f, -1, -1, -1, -1), new RGBFormat(null, -1, Format.intArray, -1.0f, -1, -1, -1, -1)};
    private final Format[] supportedOutputFormats = new Format[]{new RGBFormat(this.DIMENSION, -1, Format.intArray, -1.0f, -1, -1, -1, -1)};

    public Format[] getSupportedInputFormats() {
        return this.supportedInputFormats;
    }

    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return this.supportedOutputFormats;
        }
        VideoFormat inputCast = (VideoFormat) input;
        return new Format[]{new RGBFormat(this.DIMENSION, -1, Format.intArray, -1.0f, -1, -1, -1, -1)};
    }

    public int process(Buffer input, Buffer output) {
        if (!checkInputBuffer(input)) {
            return 1;
        }
        if (isEOM(input)) {
            propagateEOM(output);
            return 0;
        }
        BufferedImage image = (BufferedImage) this.bufferToImage.createImage(input);
        Dimension inputSize = ((VideoFormat) this.inputFormat).getSize();
        Dimension outputSize = ((VideoFormat) this.outputFormat).getSize();
        BufferedImage scaled = scale(image, ((double) outputSize.width) / ((double) inputSize.width), ((double) outputSize.height) / ((double) inputSize.height));
        System.out.println("scaled: " + scaled.getWidth() + "x" + scaled.getHeight());
        Buffer b = ImageToBuffer.createBuffer(scaled, ((VideoFormat) this.outputFormat).getFrameRate());
        output.setData(b.getData());
        output.setLength(b.getLength());
        output.setOffset(b.getOffset());
        output.setFormat(b.getFormat());
        return 0;
    }

    private BufferedImage scale(BufferedImage bi, double scaleX, double scaleY) {
        AffineTransform tx = new AffineTransform();
        tx.scale(scaleX, scaleY);
        return new AffineTransformOp(tx, 3).filter(bi, null);
    }

    public Format setInputFormat(Format format) {
        if (((VideoFormat) format).getSize() == null) {
            return null;
        }
        this.bufferToImage = new BufferToImage((VideoFormat) format);
        return super.setInputFormat(format);
    }
}
