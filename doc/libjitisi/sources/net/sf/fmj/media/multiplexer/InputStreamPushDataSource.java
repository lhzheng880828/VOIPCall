package net.sf.fmj.media.multiplexer;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushDataSource;
import javax.media.protocol.PushSourceStream;
import net.sf.fmj.utility.LoggerSingleton;

public class InputStreamPushDataSource extends PushDataSource {
    private static final Logger logger = LoggerSingleton.logger;
    private final InputStream[] inputStreams;
    private final int numTracks;
    private final ContentDescriptor outputContentDescriptor;
    private InputStreamPushSourceStream[] pushSourceStreams;

    public InputStreamPushDataSource(ContentDescriptor outputContentDescriptor, int numTracks, InputStream[] inputStreams) {
        this.outputContentDescriptor = outputContentDescriptor;
        this.numTracks = numTracks;
        this.inputStreams = inputStreams;
    }

    public void connect() throws IOException {
        logger.finer(getClass().getSimpleName() + " connect");
        this.pushSourceStreams = new InputStreamPushSourceStream[this.numTracks];
        for (int track = 0; track < this.numTracks; track++) {
            this.pushSourceStreams[track] = new InputStreamPushSourceStream(this.outputContentDescriptor, this.inputStreams[track]);
        }
    }

    public void disconnect() {
        logger.finer(getClass().getSimpleName() + " disconnect");
    }

    public String getContentType() {
        logger.finer(getClass().getSimpleName() + " getContentType");
        return this.outputContentDescriptor.getContentType();
    }

    public Object getControl(String controlType) {
        logger.finer(getClass().getSimpleName() + " getControl");
        return null;
    }

    public Object[] getControls() {
        logger.finer(getClass().getSimpleName() + " getControls");
        return new Object[0];
    }

    public Time getDuration() {
        logger.finer(getClass().getSimpleName() + " getDuration");
        return Time.TIME_UNKNOWN;
    }

    public PushSourceStream[] getStreams() {
        logger.finer(getClass().getSimpleName() + " getStreams");
        return this.pushSourceStreams;
    }

    public void notifyDataAvailable(int track) {
        this.pushSourceStreams[track].notifyDataAvailable();
    }

    public void start() throws IOException {
        logger.finer(getClass().getSimpleName() + " start");
    }

    public void stop() throws IOException {
        logger.finer(getClass().getSimpleName() + " stop");
    }
}
