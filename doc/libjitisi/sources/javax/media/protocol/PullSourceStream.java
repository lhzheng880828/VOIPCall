package javax.media.protocol;

import java.io.IOException;

public interface PullSourceStream extends SourceStream {
    int read(byte[] bArr, int i, int i2) throws IOException;

    boolean willReadBlock();
}
