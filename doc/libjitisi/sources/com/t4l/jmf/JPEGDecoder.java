package com.t4l.jmf;

import com.lti.utils.UnsignedUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.media.Buffer;
import javax.media.Codec;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.JPEGFormat;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import net.sf.fmj.utility.LoggerSingleton;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.image.BufferedImage;

public class JPEGDecoder implements Codec {
    static Hashtable imageTable = new Hashtable();
    private static final JPEGFormat jpegFormat = new JPEGFormat();
    private static final Logger logger = LoggerSingleton.logger;
    private static final RGBFormat rgbFormat = new RGBFormat(null, -1, Format.intArray, -1.0f, -1, -1, -1, -1);

    protected static void readJPEG(byte[] data, BufferedImage dest) throws IOException {
        ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(data));
        ImageReader reader = (ImageReader) ImageIO.getImageReaders(stream).next();
        if (reader == null) {
            throw new UnsupportedOperationException("This image is unsupported.");
        }
        reader.setInput(stream, false);
        ImageReadParam param = reader.getDefaultReadParam();
        param.setDestination(dest);
        reader.read(0, param);
    }

    public void close() {
        synchronized (imageTable) {
            imageTable.clear();
        }
    }

    public Object getControl(String controlType) {
        return null;
    }

    public Object[] getControls() {
        return new String[0];
    }

    public String getName() {
        return "JPEG Decoder";
    }

    public Format[] getSupportedInputFormats() {
        return new VideoFormat[]{jpegFormat};
    }

    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return new VideoFormat[]{rgbFormat};
        } else if (!input.relax().matches(jpegFormat)) {
            return new Format[0];
        } else {
            VideoFormat inputVideoFormat = (VideoFormat) input;
            return new VideoFormat[]{new RGBFormat(inputVideoFormat.getSize(), -1, Format.intArray, inputVideoFormat.getFrameRate(), 32, 16711680, 65280, UnsignedUtils.MAX_UBYTE)};
        }
    }

    public void open() throws ResourceUnavailableException {
    }

    public int process(Buffer input, Buffer output) {
        Format inputFormat = input.getFormat();
        Format outputFormat = output.getFormat();
        if (inputFormat.relax().matches(jpegFormat) && (outputFormat == null || outputFormat.relax().matches(rgbFormat))) {
            return processJPEGtoRGB(input, output);
        }
        return 1;
    }

    /* access modifiers changed from: protected */
    public int processJPEGtoRGB(Buffer input, Buffer output) {
        int i;
        synchronized (imageTable) {
            try {
                int[] intArray;
                VideoFormat inputFormat = (VideoFormat) input.getFormat();
                if (((RGBFormat) output.getFormat()) == null) {
                    int width = inputFormat.getSize().width;
                    int height = inputFormat.getSize().height;
                    output.setFormat(new RGBFormat(new Dimension(width, height), width * height, Format.intArray, inputFormat.getFrameRate(), 32, 16711680, 65280, UnsignedUtils.MAX_UBYTE, 1, width, 0, 1));
                }
                byte[] b = (byte[]) input.getData();
                Dimension d = inputFormat.getSize();
                BufferedImage dest = (BufferedImage) imageTable.get(d);
                if (dest == null) {
                    BufferedImage bufferedImage = new BufferedImage(d.width, d.height, 1);
                }
                readJPEG(b, dest);
                imageTable.put(d, dest);
                Object obj = output.getData();
                if (obj instanceof int[]) {
                    intArray = (int[]) obj;
                } else {
                    Object intArray2 = new int[(dest.getWidth() * dest.getHeight())];
                    output.setData(intArray2);
                }
                RGBConverter.populateArray(dest, intArray2, (RGBFormat) output.getFormat());
                output.setDiscard(input.isDiscard());
                output.setDuration(input.getDuration());
                output.setEOM(input.isEOM());
                output.setFlags(input.getFlags());
                output.setHeader(null);
                output.setTimeStamp(input.getTimeStamp());
                output.setSequenceNumber(input.getSequenceNumber());
                output.setOffset(0);
                output.setLength(dest.getWidth() * dest.getHeight());
                i = 0;
            } catch (Throwable t) {
                logger.log(Level.WARNING, "" + t, t);
                i = 1;
            }
        }
        return i;
    }

    public void reset() {
    }

    public Format setInputFormat(Format f) {
        return f.relax().matches(jpegFormat) ? f : null;
    }

    public Format setOutputFormat(Format f) {
        return f.relax().matches(rgbFormat) ? f : null;
    }
}
