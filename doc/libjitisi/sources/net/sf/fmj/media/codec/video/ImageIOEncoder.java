package net.sf.fmj.media.codec.video;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.Codec;
import javax.media.Format;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import net.sf.fmj.media.AbstractCodec;
import net.sf.fmj.media.util.BufferToImage;
import org.jitsi.android.util.java.awt.image.BufferedImage;

public abstract class ImageIOEncoder extends AbstractCodec implements Codec {
    private BufferToImage bufferToImage;
    private final String formatName;
    private final Format[] supportedInputFormats = new Format[]{new RGBFormat(null, -1, Format.byteArray, -1.0f, -1, -1, -1, -1), new RGBFormat(null, -1, Format.intArray, -1.0f, -1, -1, -1, -1)};

    public ImageIOEncoder(String formatName) {
        this.formatName = formatName;
        if (!ImageIO.getImageWritersByFormatName(formatName).hasNext()) {
            throw new RuntimeException("No ImageIO writer found for " + formatName);
        }
    }

    public Format[] getSupportedInputFormats() {
        return this.supportedInputFormats;
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
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            if (ImageIO.write(image, this.formatName, os)) {
                os.close();
                byte[] ba = os.toByteArray();
                output.setData(ba);
                output.setOffset(0);
                output.setLength(ba.length);
                return 0;
            }
            throw new RuntimeException("No ImageIO writer found for " + this.formatName);
        } catch (IOException e) {
            output.setDiscard(true);
            output.setLength(0);
            return 1;
        }
    }

    public Format setInputFormat(Format format) {
        if (((VideoFormat) format).getSize() == null) {
            return null;
        }
        this.bufferToImage = new BufferToImage((VideoFormat) format);
        return super.setInputFormat(format);
    }
}
