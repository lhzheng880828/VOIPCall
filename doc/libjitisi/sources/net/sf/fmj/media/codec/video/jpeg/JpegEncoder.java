package net.sf.fmj.media.codec.video.jpeg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.media.Buffer;
import javax.media.Codec;
import javax.media.Format;
import javax.media.format.JPEGFormat;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import net.sf.fmj.media.AbstractCodec;
import net.sf.fmj.media.util.BufferToImage;
import org.jitsi.android.util.java.awt.image.BufferedImage;

public class JpegEncoder extends AbstractCodec implements Codec {
    private BufferToImage bufferToImage;
    private final Format[] supportedInputFormats = new Format[]{new RGBFormat(null, -1, Format.byteArray, -1.0f, -1, -1, -1, -1), new RGBFormat(null, -1, Format.intArray, -1.0f, -1, -1, -1, -1)};
    private final Format[] supportedOutputFormats = new Format[]{new JPEGFormat()};

    public Format[] getSupportedInputFormats() {
        return this.supportedInputFormats;
    }

    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return this.supportedOutputFormats;
        }
        VideoFormat inputCast = (VideoFormat) input;
        return new Format[]{new JPEGFormat(inputCast.getSize(), -1, Format.byteArray, inputCast.getFrameRate(), -1, -1)};
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
            JPEGImageWriteParam param = new JPEGImageWriteParam(null);
            param.setCompressionMode(2);
            param.setCompressionQuality(0.74f);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            MemoryCacheImageOutputStream out = new MemoryCacheImageOutputStream(os);
            ImageWriter encoder = (ImageWriter) ImageIO.getImageWritersByFormatName("JPEG").next();
            encoder.setOutput(out);
            encoder.write(null, new IIOImage(image, null, null), param);
            out.close();
            os.close();
            byte[] ba = os.toByteArray();
            output.setData(ba);
            output.setOffset(0);
            output.setLength(ba.length);
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
        this.bufferToImage = new BufferToImage((VideoFormat) format);
        return super.setInputFormat(format);
    }
}
