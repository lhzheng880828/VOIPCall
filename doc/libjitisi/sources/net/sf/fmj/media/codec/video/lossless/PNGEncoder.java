package net.sf.fmj.media.codec.video.lossless;

import javax.media.Format;
import javax.media.format.VideoFormat;
import net.sf.fmj.media.codec.video.ImageIOEncoder;
import net.sf.fmj.media.format.PNGFormat;

public class PNGEncoder extends ImageIOEncoder {
    private final Format[] supportedOutputFormats = new Format[]{new PNGFormat()};

    public PNGEncoder() {
        super("PNG");
    }

    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return this.supportedOutputFormats;
        }
        VideoFormat inputCast = (VideoFormat) input;
        return new Format[]{new PNGFormat(inputCast.getSize(), -1, Format.byteArray, inputCast.getFrameRate())};
    }
}
