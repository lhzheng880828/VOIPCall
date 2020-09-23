package com.t4l.jmf;

import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
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

public class JPEGEncoder implements Codec {
    static Hashtable imageTable = new Hashtable();
    private static final VideoFormat jpegFormat = new JPEGFormat();
    private static final Logger logger = LoggerSingleton.logger;
    private static final RGBFormat rgbFormat = new RGBFormat(null, -1, Format.intArray, -1.0f, -1, -1, -1, -1);

    protected static int writeJPEG(BufferedImage image, byte[] data) throws IOException {
        ImageWriter iw = (ImageWriter) ImageIO.getImageWritersByMIMEType("image/jpeg").next();
        ImageWriteParam iwParam = iw.getDefaultWriteParam();
        iwParam.setCompressionMode(2);
        iwParam.setCompressionQuality(0.8f);
        CustomByteArrayOutputStream out = new CustomByteArrayOutputStream(data);
        iw.setOutput(out);
        iw.write(null, new IIOImage(image, null, null), iwParam);
        return out.getBytesWritten();
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
        return "JPEG Encoder";
    }

    public Format[] getSupportedInputFormats() {
        return new VideoFormat[]{rgbFormat};
    }

    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return new VideoFormat[]{jpegFormat};
        } else if (!input.relax().matches(rgbFormat)) {
            return new Format[0];
        } else {
            VideoFormat inputVideoFormat = (VideoFormat) input;
            return new VideoFormat[]{new JPEGFormat(inputVideoFormat.getSize(), -1, Format.byteArray, inputVideoFormat.getFrameRate(), -1, -1)};
        }
    }

    public void open() throws ResourceUnavailableException {
    }

    public int process(Buffer input, Buffer output) {
        Format inputFormat = input.getFormat();
        Format outputFormat = output.getFormat();
        if (inputFormat.relax().matches(rgbFormat) && outputFormat.relax().matches(jpegFormat)) {
            return processRGBtoJPEG(input, output);
        }
        return 1;
    }

    /* access modifiers changed from: protected */
    public int processRGBtoJPEG(Buffer input, Buffer output) {
        int i;
        synchronized (imageTable) {
            try {
                byte[] bytes;
                RGBFormat inputFormat = (RGBFormat) input.getFormat();
                if (((VideoFormat) output.getFormat()) == null) {
                    int width = inputFormat.getSize().width;
                    int height = inputFormat.getSize().height;
                    output.setFormat(new JPEGFormat(new Dimension(width, height), (width * height) + 200, Format.byteArray, inputFormat.getFrameRate(), -1, -1));
                }
                int[] data = (int[]) input.getData();
                Dimension d = inputFormat.getSize();
                BufferedImage dest = (BufferedImage) imageTable.get(d);
                if (dest == null) {
                    dest = new BufferedImage(d.width, d.height, 1);
                }
                RGBConverter.populateImage(data, input.getOffset(), dest, inputFormat);
                Object obj = output.getData();
                if (obj instanceof byte[]) {
                    bytes = (byte[]) obj;
                } else {
                    bytes = new byte[((d.width * d.height) + 200)];
                    output.setData(bytes);
                }
                int length = writeJPEG(dest, bytes);
                imageTable.put(d, dest);
                output.setLength(length);
                output.setDiscard(input.isDiscard());
                output.setDuration(input.getDuration());
                output.setEOM(input.isEOM());
                output.setFlags(input.getFlags());
                output.setHeader(null);
                output.setTimeStamp(input.getTimeStamp());
                output.setSequenceNumber(input.getSequenceNumber());
                output.setOffset(0);
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
        return f.relax().matches(rgbFormat) ? f : null;
    }

    public Format setOutputFormat(Format f) {
        return f.relax().matches(jpegFormat) ? f : null;
    }
}
