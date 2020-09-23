package net.sf.fmj.media.multiplexer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import net.sf.fmj.media.AbstractMultiplexer;
import net.sf.fmj.utility.LoggerSingleton;

public abstract class AbstractInputStreamMux extends AbstractMultiplexer {
    private static final int PIPE_SIZE = 200000;
    private static final Logger logger = LoggerSingleton.logger;
    private final ContentDescriptor contentDescriptor;
    private InputStreamPushDataSource dataOutput;
    private PipedInputStream pipedInputStream;
    private PipedOutputStream pipedOutputStream;

    public abstract Format[] getSupportedInputFormats();

    public AbstractInputStreamMux(ContentDescriptor contentDescriptor) {
        this.contentDescriptor = contentDescriptor;
    }

    public void close() {
        logger.finer(getClass().getSimpleName() + " close");
        super.close();
        if (this.dataOutput != null) {
            try {
                this.dataOutput.stop();
            } catch (IOException e) {
                logger.log(Level.WARNING, "" + e, e);
            }
            this.dataOutput.disconnect();
        }
    }

    /* access modifiers changed from: protected */
    public InputStreamPushDataSource createInputStreamPushDataSource(ContentDescriptor outputContentDescriptor, int numTracks, InputStream[] inputStreams) {
        return new InputStreamPushDataSource(outputContentDescriptor, numTracks, inputStreams);
    }

    /* access modifiers changed from: protected */
    public void doProcess(Buffer buffer, int trackID, OutputStream os) throws IOException {
        if (buffer.isEOM()) {
            os.close();
        } else {
            os.write((byte[]) buffer.getData(), buffer.getOffset(), buffer.getLength());
        }
    }

    public DataSource getDataOutput() {
        if (this.dataOutput == null) {
            this.dataOutput = createInputStreamPushDataSource(this.outputContentDescriptor, 1, new InputStream[]{this.pipedInputStream});
        }
        logger.finer(getClass().getSimpleName() + " getDataOutput");
        return this.dataOutput;
    }

    /* access modifiers changed from: protected */
    public InputStreamPushDataSource getDataOutputNoInit() {
        return this.dataOutput;
    }

    /* access modifiers changed from: protected */
    public OutputStream getOutputStream() {
        return this.pipedOutputStream;
    }

    public ContentDescriptor[] getSupportedOutputContentDescriptors(Format[] inputs) {
        return new ContentDescriptor[]{this.contentDescriptor};
    }

    public void open() throws ResourceUnavailableException {
        logger.finer(getClass().getSimpleName() + " open");
        super.open();
    }

    public int process(Buffer buffer, int trackID) {
        logger.finer(getClass().getSimpleName() + " process " + buffer + " " + trackID + " length " + buffer.getLength());
        try {
            doProcess(buffer, trackID, this.pipedOutputStream);
            if (this.dataOutput == null) {
                return 0;
            }
            this.dataOutput.notifyDataAvailable(0);
            return 0;
        } catch (IOException e1) {
            logger.log(Level.SEVERE, "" + e1, e1);
            return 1;
        }
    }

    public int setNumTracks(int numTracks) {
        numTracks = super.setNumTracks(numTracks);
        try {
            this.pipedInputStream = new BigPipedInputStream(PIPE_SIZE);
            this.pipedOutputStream = new PipedOutputStream(this.pipedInputStream);
            return numTracks;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* access modifiers changed from: protected */
    public void writeInt(OutputStream os, long value) throws IOException {
        byte[] aBuffer = new byte[]{(byte) ((int) ((value >> 24) & 255)), (byte) ((int) ((value >> 16) & 255)), (byte) ((int) ((value >> 8) & 255)), (byte) ((int) (value & 255))};
        os.write(aBuffer, 0, aBuffer.length);
    }
}
