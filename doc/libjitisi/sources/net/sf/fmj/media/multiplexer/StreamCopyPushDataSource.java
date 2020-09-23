package net.sf.fmj.media.multiplexer;

import com.lti.utils.synchronization.CloseableThread;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.Format;
import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushDataSource;
import javax.media.protocol.PushSourceStream;
import net.sf.fmj.utility.IOUtils;
import net.sf.fmj.utility.LoggerSingleton;

public class StreamCopyPushDataSource extends PushDataSource {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = LoggerSingleton.logger;
    private final Format[] inputFormats;
    private final InputStream[] inputStreams;
    private final int numTracks;
    private final ContentDescriptor outputContentDescriptor;
    private InputStreamPushSourceStream[] pushSourceStreams;
    private WriterThread[] writerThreads;

    private class WriterThread extends CloseableThread {
        private Format format;
        private final InputStream in;
        private final OutputStream out;
        private final int trackID;

        public WriterThread(int trackID, InputStream in, OutputStream out, Format format) {
            this.trackID = trackID;
            this.in = in;
            this.out = out;
            this.format = format;
        }

        public void run() {
            try {
                StreamCopyPushDataSource.this.write(this.in, this.out, this.trackID);
                StreamCopyPushDataSource.logger.finer("WriterThread closing output stream");
                this.out.close();
            } catch (InterruptedIOException e) {
                StreamCopyPushDataSource.logger.log(Level.FINE, "" + e, e);
            } catch (IOException e2) {
                StreamCopyPushDataSource.logger.log(Level.WARNING, "" + e2, e2);
            } finally {
                setClosed();
            }
        }
    }

    public StreamCopyPushDataSource(ContentDescriptor outputContentDescriptor, int numTracks, InputStream[] inputStreams, Format[] inputFormats) {
        this.outputContentDescriptor = outputContentDescriptor;
        this.numTracks = numTracks;
        this.inputStreams = inputStreams;
        this.inputFormats = inputFormats;
    }

    public void connect() throws IOException {
        logger.finer(getClass().getSimpleName() + " connect");
        this.pushSourceStreams = new InputStreamPushSourceStream[this.numTracks];
        this.writerThreads = new WriterThread[this.numTracks];
        for (int track = 0; track < this.numTracks; track++) {
            StreamPipe p = new StreamPipe();
            this.pushSourceStreams[track] = new InputStreamPushSourceStream(this.outputContentDescriptor, p.getInputStream());
            this.writerThreads[track] = new WriterThread(track, this.inputStreams[track], p.getOutputStream(), this.inputFormats[track]);
            this.writerThreads[track].setName("WriterThread for track " + track);
            this.writerThreads[track].setDaemon(true);
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
        for (int track = 0; track < this.numTracks; track++) {
            this.writerThreads[track].start();
        }
    }

    public void stop() throws IOException {
        int track;
        logger.finer(getClass().getSimpleName() + " stop");
        for (track = 0; track < this.numTracks; track++) {
            this.writerThreads[track].close();
        }
        track = 0;
        while (track < this.numTracks) {
            try {
                this.writerThreads[track].waitUntilClosed();
                track++;
            } catch (InterruptedException e) {
                throw new InterruptedIOException();
            }
        }
    }

    public void waitUntilFinished() throws InterruptedException {
        int track = 0;
        while (track < this.numTracks) {
            try {
                this.writerThreads[track].waitUntilClosed();
                track++;
            } catch (InterruptedException e) {
                throw e;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void write(InputStream in, OutputStream out, int track) throws IOException {
        IOUtils.copyStream(in, out);
    }
}
