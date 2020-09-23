package net.sf.fmj.media.protocol;

import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.SourceStream;

public class BasicSourceStream implements SourceStream {
    public static final int LENGTH_DISCARD = -2;
    protected ContentDescriptor contentDescriptor;
    protected long contentLength;
    protected Object[] controls;

    public BasicSourceStream() {
        this.contentDescriptor = null;
        this.contentLength = -1;
        this.controls = new Object[0];
    }

    public BasicSourceStream(ContentDescriptor cd, long contentLength) {
        this.contentDescriptor = null;
        this.contentLength = -1;
        this.controls = new Object[0];
        this.contentDescriptor = cd;
        this.contentLength = contentLength;
    }

    public boolean endOfStream() {
        return false;
    }

    public ContentDescriptor getContentDescriptor() {
        return this.contentDescriptor;
    }

    public long getContentLength() {
        return this.contentLength;
    }

    public Object getControl(String controlType) {
        try {
            Class<?> cls = Class.forName(controlType);
            Object[] cs = getControls();
            for (int i = 0; i < cs.length; i++) {
                if (cls.isInstance(cs[i])) {
                    return cs[i];
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public Object[] getControls() {
        return this.controls;
    }
}
