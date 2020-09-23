package net.sf.fmj.media.multiplexer;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import net.sf.fmj.media.AbstractMultiplexer;
import net.sf.fmj.media.BufferQueueInputStream;
import net.sf.fmj.utility.LoggerSingleton;

public abstract class AbstractStreamCopyMux extends AbstractMultiplexer {
    private static final Logger logger = LoggerSingleton.logger;
    private BufferQueueInputStream[] bufferQueueInputStreams;
    private final ContentDescriptor contentDescriptor;
    private StreamCopyPushDataSource dataOutput;

    public abstract Format[] getSupportedInputFormats();

    public AbstractStreamCopyMux(ContentDescriptor contentDescriptor) {
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
    public StreamCopyPushDataSource createInputStreamPushDataSource(ContentDescriptor outputContentDescriptor, int numTracks, InputStream[] inputStreams, Format[] inputFormats) {
        return new StreamCopyPushDataSource(outputContentDescriptor, numTracks, inputStreams, inputFormats);
    }

    public DataSource getDataOutput() {
        if (this.dataOutput == null) {
            this.dataOutput = createInputStreamPushDataSource(this.outputContentDescriptor, this.numTracks, this.bufferQueueInputStreams, this.inputFormats);
        }
        logger.finer(getClass().getSimpleName() + " getDataOutput");
        return this.dataOutput;
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
        if (buffer.isEOM()) {
            logger.finer("processing EOM buffer for track: " + trackID);
        }
        if (!this.bufferQueueInputStreams[trackID].put(buffer)) {
            return 2;
        }
        try {
            if (buffer.isEOM()) {
                logger.fine("EOM, waitUntilFinished...");
                if (this.dataOutput != null) {
                    this.dataOutput.waitUntilFinished();
                }
                logger.fine("EOM, finished.");
            }
            if (this.dataOutput != null) {
                this.dataOutput.notifyDataAvailable(trackID);
            }
            return 0;
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "" + e, e);
            return 1;
        }
    }

    public int setNumTracks(int numTracks) {
        numTracks = super.setNumTracks(numTracks);
        this.bufferQueueInputStreams = new BufferQueueInputStream[numTracks];
        for (int track = 0; track < numTracks; track++) {
            this.bufferQueueInputStreams[track] = new BufferQueueInputStream();
        }
        return numTracks;
    }
}
