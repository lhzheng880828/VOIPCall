package javax.media.protocol;

import java.io.IOException;
import javax.media.Buffer;
import javax.media.Format;

public interface PullBufferStream extends SourceStream {
    Format getFormat();

    void read(Buffer buffer) throws IOException;

    boolean willReadBlock();
}
