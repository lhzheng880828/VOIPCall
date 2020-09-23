package org.jitsi.impl.neomedia.protocol;

import java.io.IOException;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.SourceStream;

public abstract class BufferStreamAdapter<T extends SourceStream> implements SourceStream {
    private final Format format;
    protected final T stream;

    public abstract int read(byte[] bArr, int i, int i2) throws IOException;

    public BufferStreamAdapter(T stream, Format format) {
        this.stream = stream;
        this.format = format;
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

    public Format getFormat() {
        return this.format;
    }

    public T getStream() {
        return this.stream;
    }

    /* access modifiers changed from: protected */
    public void read(Buffer buffer, byte[] bytes) throws IOException {
        int numberOfBytesRead = read(bytes, 0, bytes.length);
        if (numberOfBytesRead > -1) {
            buffer.setData(bytes);
            buffer.setOffset(0);
            buffer.setLength(numberOfBytesRead);
            Format format = getFormat();
            if (format != null) {
                buffer.setFormat(format);
            }
        }
    }
}
