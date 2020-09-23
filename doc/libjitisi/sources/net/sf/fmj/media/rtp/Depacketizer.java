package net.sf.fmj.media.rtp;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.PlugIn;

public interface Depacketizer extends PlugIn {
    public static final int DEPACKETIZER = 6;

    Format[] getSupportedInputFormats();

    Format parse(Buffer buffer);

    Format setInputFormat(Format format);
}
