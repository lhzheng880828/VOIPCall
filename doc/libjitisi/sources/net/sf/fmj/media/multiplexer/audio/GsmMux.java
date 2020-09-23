package net.sf.fmj.media.multiplexer.audio;

import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.FileTypeDescriptor;
import net.sf.fmj.media.multiplexer.AbstractStreamCopyMux;

public class GsmMux extends AbstractStreamCopyMux {
    public GsmMux() {
        super(new ContentDescriptor(FileTypeDescriptor.GSM));
    }

    public Format[] getSupportedInputFormats() {
        return new Format[]{new AudioFormat("gsm", 8000.0d, 8, 1, -1, -1)};
    }
}
