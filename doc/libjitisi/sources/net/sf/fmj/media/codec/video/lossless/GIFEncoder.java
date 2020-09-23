package net.sf.fmj.media.codec.video.lossless;

import javax.media.Format;
import javax.media.format.VideoFormat;
import net.sf.fmj.media.codec.video.ImageIOEncoder;
import net.sf.fmj.media.format.GIFFormat;

public class GIFEncoder extends ImageIOEncoder {
    private final Format[] supportedOutputFormats = new Format[]{new GIFFormat()};

    public GIFEncoder() {
        super("GIF");
    }

    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return this.supportedOutputFormats;
        }
        VideoFormat inputCast = (VideoFormat) input;
        return new Format[]{new GIFFormat(inputCast.getSize(), -1, Format.byteArray, inputCast.getFrameRate())};
    }
}
