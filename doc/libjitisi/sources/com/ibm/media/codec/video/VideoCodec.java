package com.ibm.media.codec.video;

import com.sun.media.BasicCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.VideoFormat;
import net.sf.fmj.utility.LoggerSingleton;

@Deprecated
public abstract class VideoCodec extends BasicCodec {
    private static final boolean TRACE = false;
    private static final Logger logger = LoggerSingleton.logger;
    protected final boolean DEBUG = true;
    protected String PLUGIN_NAME;
    protected VideoFormat[] defaultOutputFormats;
    protected VideoFormat inputFormat;
    protected VideoFormat outputFormat;
    protected VideoFormat[] supportedInputFormats;
    protected VideoFormat[] supportedOutputFormats;

    public boolean checkFormat(Format format) {
        if (!((VideoFormat) format).getSize().equals(this.outputFormat.getSize())) {
            videoResized();
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public Format getInputFormat() {
        return this.inputFormat;
    }

    /* access modifiers changed from: protected */
    public Format[] getMatchingOutputFormats(Format in) {
        return new Format[0];
    }

    public String getName() {
        return this.PLUGIN_NAME;
    }

    /* access modifiers changed from: protected */
    public Format getOutputFormat() {
        return this.outputFormat;
    }

    public Format[] getSupportedInputFormats() {
        return this.supportedInputFormats;
    }

    public Format[] getSupportedOutputFormats(Format in) {
        if (in == null) {
            return this.defaultOutputFormats;
        }
        List<Format> result = new ArrayList();
        for (Format matches : this.supportedInputFormats) {
            if (in.matches(matches)) {
                this.inputFormat = (VideoFormat) in;
                Format[] matching = getMatchingOutputFormats(in);
                for (Object add : matching) {
                    result.add(add);
                }
            }
        }
        return (Format[]) result.toArray(new Format[result.size()]);
    }

    public Format setInputFormat(Format format) {
        if (!(format instanceof VideoFormat)) {
            return null;
        }
        for (Format matches : this.supportedInputFormats) {
            if (format.matches(matches)) {
                this.inputFormat = (VideoFormat) format;
                return this.inputFormat;
            }
        }
        return null;
    }

    public Format setOutputFormat(Format format) {
        if (!(format instanceof VideoFormat)) {
            return null;
        }
        Format[] formats = getMatchingOutputFormats(this.inputFormat);
        for (Format matches : formats) {
            if (format.matches(matches)) {
                this.outputFormat = (VideoFormat) format;
                return this.outputFormat;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void updateOutput(Buffer outputBuffer, Format format, int length, int offset) {
        outputBuffer.setFormat(format);
        outputBuffer.setLength(length);
        outputBuffer.setOffset(offset);
    }

    /* access modifiers changed from: protected */
    public void videoResized() {
    }
}
