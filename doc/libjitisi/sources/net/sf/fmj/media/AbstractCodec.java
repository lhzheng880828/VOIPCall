package net.sf.fmj.media;

import javax.media.Buffer;
import javax.media.Codec;
import javax.media.Format;
import net.sf.fmj.utility.LoggingStringUtils;

public abstract class AbstractCodec extends AbstractPlugIn implements Codec {
    protected Format inputFormat = null;
    protected Format[] inputFormats = new Format[0];
    protected boolean opened = false;
    protected Format outputFormat = null;

    public abstract Format[] getSupportedOutputFormats(Format format);

    public abstract int process(Buffer buffer, Buffer buffer2);

    /* access modifiers changed from: protected */
    public boolean checkInputBuffer(Buffer b) {
        return true;
    }

    /* access modifiers changed from: protected|final */
    public final void dump(String label, Buffer buffer) {
        System.out.println(label + ": " + LoggingStringUtils.bufferToStr(buffer));
    }

    /* access modifiers changed from: protected */
    public Format getInputFormat() {
        return this.inputFormat;
    }

    /* access modifiers changed from: protected */
    public Format getOutputFormat() {
        return this.outputFormat;
    }

    public Format[] getSupportedInputFormats() {
        return this.inputFormats;
    }

    /* access modifiers changed from: protected */
    public boolean isEOM(Buffer b) {
        return b.isEOM();
    }

    /* access modifiers changed from: protected */
    public void propagateEOM(Buffer b) {
        b.setEOM(true);
    }

    public Format setInputFormat(Format format) {
        this.inputFormat = format;
        return this.inputFormat;
    }

    public Format setOutputFormat(Format format) {
        this.outputFormat = format;
        return this.outputFormat;
    }
}
