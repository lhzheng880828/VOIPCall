package net.sf.fmj.media.multiplexer;

import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;

public class RawMux extends AbstractStreamCopyMux {
    public RawMux() {
        super(new ContentDescriptor(ContentDescriptor.RAW));
    }

    public Format[] getSupportedInputFormats() {
        Format[] formatArr = new Format[2];
        formatArr[0] = new AudioFormat(null, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray);
        formatArr[1] = new VideoFormat(null, null, -1, Format.byteArray, -1.0f);
        return formatArr;
    }
}
