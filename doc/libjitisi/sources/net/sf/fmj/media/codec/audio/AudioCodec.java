package net.sf.fmj.media.codec.audio;

import javax.media.Format;
import javax.media.format.AudioFormat;
import net.sf.fmj.media.BasicCodec;
import net.sf.fmj.media.BasicPlugIn;

public abstract class AudioCodec extends BasicCodec {
    public boolean checkFormat(Format format) {
        if (this.inputFormat == null || this.outputFormat == null || format != this.inputFormat || !format.equals(this.inputFormat)) {
            this.inputFormat = format;
            this.outputFormat = getSupportedOutputFormats(format)[0];
        }
        if (this.outputFormat != null) {
            return true;
        }
        return false;
    }

    public Format setInputFormat(Format format) {
        if (BasicPlugIn.matches(format, this.inputFormats) == null) {
            return null;
        }
        this.inputFormat = format;
        return format;
    }

    public Format setOutputFormat(Format format) {
        if (BasicPlugIn.matches(format, getSupportedOutputFormats(this.inputFormat)) == null) {
            return null;
        }
        if (!(format instanceof AudioFormat)) {
            return null;
        }
        this.outputFormat = format;
        return format;
    }
}
