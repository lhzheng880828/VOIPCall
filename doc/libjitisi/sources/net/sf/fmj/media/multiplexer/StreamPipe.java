package net.sf.fmj.media.multiplexer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;
import javax.media.Buffer;
import net.sf.fmj.media.BufferQueueInputStream;
import net.sf.fmj.utility.LoggerSingleton;

public class StreamPipe {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = LoggerSingleton.logger;
    /* access modifiers changed from: private|final */
    public final BufferQueueInputStream is = new BufferQueueInputStream();
    private final MyOutputStream os = new MyOutputStream();

    private class MyOutputStream extends OutputStream {
        private MyOutputStream() {
        }

        public void close() throws IOException {
            StreamPipe.logger.finer("MyOutputStream Closing, putting EOM buffer");
            StreamPipe.this.is.blockingPut(StreamPipe.this.createEOMBuffer());
            super.close();
        }

        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            StreamPipe.this.is.blockingPut(StreamPipe.this.createBuffer(b, off, len));
        }

        public void write(int b) throws IOException {
            write(new byte[]{(byte) b});
        }
    }

    /* access modifiers changed from: private */
    public Buffer createBuffer(byte[] data, int offset, int length) {
        Buffer b = new Buffer();
        b.setData(data);
        b.setOffset(offset);
        b.setLength(length);
        return b;
    }

    /* access modifiers changed from: private */
    public Buffer createEOMBuffer() {
        Buffer b = new Buffer();
        b.setData(new byte[0]);
        b.setOffset(0);
        b.setLength(0);
        b.setEOM(true);
        return b;
    }

    public InputStream getInputStream() {
        return this.is;
    }

    public OutputStream getOutputStream() {
        return this.os;
    }
}
