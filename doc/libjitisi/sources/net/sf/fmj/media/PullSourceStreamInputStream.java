package net.sf.fmj.media;

import com.lti.utils.UnsignedUtils;
import java.io.IOException;
import java.io.InputStream;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.Seekable;

public class PullSourceStreamInputStream extends InputStream {
    private long markPosition = -1;
    private final PullSourceStream pss;
    private final Seekable seekable;

    public PullSourceStreamInputStream(PullSourceStream pss) {
        this.pss = pss;
        if (pss instanceof Seekable) {
            this.seekable = (Seekable) pss;
        } else {
            this.seekable = null;
        }
    }

    public synchronized void mark(int readlimit) {
        if (!markSupported()) {
            super.mark(readlimit);
        }
        this.markPosition = this.seekable.tell();
    }

    public boolean markSupported() {
        return this.seekable != null;
    }

    public int read() throws IOException {
        byte[] buffer = new byte[1];
        if (this.pss.read(buffer, 0, 1) <= 0) {
            return -1;
        }
        return buffer[0] & UnsignedUtils.MAX_UBYTE;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return this.pss.read(b, off, len);
    }

    public synchronized void reset() throws IOException {
        if (!markSupported()) {
            super.reset();
        }
        if (this.markPosition < 0) {
            throw new IOException("mark must be called before reset");
        }
        this.seekable.seek(this.markPosition);
    }

    public long skip(long n) throws IOException {
        if (this.seekable == null) {
            return super.skip(n);
        }
        if (n <= 0) {
            return 0;
        }
        long beforeSeek = this.seekable.tell();
        return this.seekable.seek(beforeSeek + n) - beforeSeek;
    }
}
