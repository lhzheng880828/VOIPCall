package javax.media.protocol;

import java.io.IOException;

public interface PushSourceStream extends SourceStream {
    int getMinimumTransferSize();

    int read(byte[] bArr, int i, int i2) throws IOException;

    void setTransferHandler(SourceTransferHandler sourceTransferHandler);
}
