package net.sf.fmj.media;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.Renderer;

public abstract class AbstractRenderer extends AbstractPlugIn implements Renderer {
    protected Format inputFormat;

    public abstract Format[] getSupportedInputFormats();

    public abstract int process(Buffer buffer);

    public Format setInputFormat(Format format) {
        this.inputFormat = format;
        return this.inputFormat;
    }

    public void start() {
    }

    public void stop() {
    }
}
