package net.sf.fmj.media;

import com.lti.utils.UnsignedUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import javax.media.Buffer;
import javax.media.Track;
import net.sf.fmj.utility.LoggerSingleton;

public class TrackInputStream extends InputStream {
    private static final Logger logger = LoggerSingleton.logger;
    private Buffer buffer;
    private final Track track;

    public TrackInputStream(Track track) {
        this.track = track;
    }

    private void fillBuffer() {
        if (this.buffer == null) {
            this.buffer = new Buffer();
            this.buffer.setFormat(this.track.getFormat());
        }
        while (!this.buffer.isEOM() && this.buffer.getLength() <= 0) {
            this.track.readFrame(this.buffer);
            logger.fine("Read buffer from track: " + this.buffer.getLength());
            if (!this.buffer.isDiscard()) {
                return;
            }
        }
    }

    public Buffer getBuffer() {
        return this.buffer;
    }

    public int read() throws IOException {
        fillBuffer();
        if (this.buffer.getLength() == 0 && this.buffer.isEOM()) {
            return -1;
        }
        int result = ((byte[]) this.buffer.getData())[this.buffer.getOffset()] & UnsignedUtils.MAX_UBYTE;
        this.buffer.setOffset(this.buffer.getOffset() + 1);
        this.buffer.setLength(this.buffer.getLength() - 1);
        return result;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        fillBuffer();
        if (this.buffer.getLength() == 0 && this.buffer.isEOM()) {
            return -1;
        }
        int lengthToCopy;
        byte[] data = (byte[]) this.buffer.getData();
        if (this.buffer.getLength() < len) {
            lengthToCopy = this.buffer.getLength();
        } else {
            lengthToCopy = len;
        }
        System.arraycopy(data, this.buffer.getOffset(), b, off, lengthToCopy);
        this.buffer.setOffset(this.buffer.getOffset() + lengthToCopy);
        this.buffer.setLength(this.buffer.getLength() - lengthToCopy);
        return lengthToCopy;
    }
}
