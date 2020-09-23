package javax.media.protocol;

import java.io.IOException;
import java.io.InputStream;

public class InputSourceStream implements PullSourceStream {
    private ContentDescriptor contentDescriptor;
    protected boolean eosReached;
    protected InputStream stream;

    public InputSourceStream(InputStream s, ContentDescriptor type) {
        this.stream = s;
        this.contentDescriptor = type;
    }

    public void close() throws IOException {
        this.stream.close();
    }

    public boolean endOfStream() {
        return this.eosReached;
    }

    public ContentDescriptor getContentDescriptor() {
        return this.contentDescriptor;
    }

    public long getContentLength() {
        return -1;
    }

    public Object getControl(String controlName) {
        return null;
    }

    public Object[] getControls() {
        return new Object[0];
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
        int result = this.stream.read(buffer, offset, length);
        if (result == -1) {
            this.eosReached = true;
        }
        return result;
    }

    public boolean willReadBlock() {
        try {
            return this.stream.available() <= 0;
        } catch (IOException e) {
            return true;
        }
    }
}
