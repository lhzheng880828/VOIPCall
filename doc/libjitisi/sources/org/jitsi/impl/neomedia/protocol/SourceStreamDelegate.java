package org.jitsi.impl.neomedia.protocol;

import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.SourceStream;

public class SourceStreamDelegate<T extends SourceStream> implements SourceStream {
    protected final T stream;

    public SourceStreamDelegate(T stream) {
        this.stream = stream;
    }

    public boolean endOfStream() {
        return this.stream.endOfStream();
    }

    public ContentDescriptor getContentDescriptor() {
        return this.stream.getContentDescriptor();
    }

    public long getContentLength() {
        return this.stream.getContentLength();
    }

    public Object getControl(String controlType) {
        return this.stream.getControl(controlType);
    }

    public Object[] getControls() {
        return this.stream.getControls();
    }
}
