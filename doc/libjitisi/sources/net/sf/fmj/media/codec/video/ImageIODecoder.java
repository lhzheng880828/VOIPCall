package net.sf.fmj.media.codec.video;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.Codec;
import javax.media.Format;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import net.sf.fmj.media.AbstractCodec;
import net.sf.fmj.media.util.ImageToBuffer;
import org.jitsi.android.util.java.awt.image.BufferedImage;

public abstract class ImageIODecoder extends AbstractCodec implements Codec {
    private final String formatName;
    private final Format[] supportedOutputFormats = new Format[]{new RGBFormat(null, -1, Format.byteArray, -1.0f, -1, -1, -1, -1)};

    public abstract Format[] getSupportedInputFormats();

    public ImageIODecoder(String formatName) {
        this.formatName = formatName;
        if (!ImageIO.getImageReadersByFormatName(formatName).hasNext()) {
            throw new RuntimeException("No ImageIO reader found for " + formatName);
        }
    }

    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return this.supportedOutputFormats;
        }
        VideoFormat inputCast = (VideoFormat) input;
        return new Format[]{new RGBFormat(inputCast.getSize(), -1, Format.byteArray, inputCast.getFrameRate(), -1, -1, -1, -1)};
    }

    public int process(Buffer input, Buffer output) {
        if (!checkInputBuffer(input)) {
            return 1;
        }
        if (isEOM(input)) {
            propagateEOM(output);
            return 0;
        }
        try {
            ByteArrayInputStream is = new ByteArrayInputStream((byte[]) input.getData(), input.getOffset(), input.getLength());
            BufferedImage image = ImageIO.read(is);
            is.close();
            Buffer b = ImageToBuffer.createBuffer(image, ((VideoFormat) this.outputFormat).getFrameRate());
            output.setData(b.getData());
            output.setOffset(b.getOffset());
            output.setLength(b.getLength());
            output.setFormat(b.getFormat());
            return 0;
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
        return super.setInputFormat(format);
    }
}
