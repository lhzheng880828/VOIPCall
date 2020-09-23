package com.ibm.media.codec.audio;

import com.sun.media.BasicCodec;
import javax.media.Format;
import javax.media.format.AudioFormat;
import net.sf.fmj.media.BasicPlugIn;

public abstract class AudioCodec extends BasicCodec {
    protected final boolean DEBUG = true;
    protected String PLUGIN_NAME;
    protected AudioFormat[] defaultOutputFormats;
    protected AudioFormat inputFormat;
    protected AudioFormat outputFormat;
    protected AudioFormat[] supportedInputFormats;
    protected AudioFormat[] supportedOutputFormats;

    public boolean checkFormat(Format format) {
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
        if (!(in instanceof AudioFormat) || BasicPlugIn.matches(in, this.supportedInputFormats) == null) {
            return new Format[0];
        }
        return getMatchingOutputFormats(in);
    }

    public Format setInputFormat(Format format) {
        if (!(format instanceof AudioFormat) || BasicPlugIn.matches(format, this.supportedInputFormats) == null) {
            return null;
        }
        this.inputFormat = (AudioFormat) format;
        return format;
    }

    public Format setOutputFormat(Format format) {
        if (!(format instanceof AudioFormat) || BasicPlugIn.matches(format, getMatchingOutputFormats(this.inputFormat)) == null) {
            return null;
        }
        this.outputFormat = (AudioFormat) format;
        return format;
    }
}
