package net.sf.fmj.media.parser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.BadHeaderException;
import javax.media.Buffer;
import javax.media.Duration;
import javax.media.Format;
import javax.media.IncompatibleSourceException;
import javax.media.Time;
import javax.media.Track;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;
import net.sf.fmj.media.AbstractDemultiplexer;
import net.sf.fmj.media.AbstractTrack;
import net.sf.fmj.utility.LoggerSingleton;
import org.jitsi.android.util.javax.sound.sampled.AudioFormat;
import org.jitsi.impl.neomedia.portaudio.Pa;

public class GsmParser extends AbstractDemultiplexer {
    /* access modifiers changed from: private|static */
    public static double GSM_FRAME_RATE = 50.0d;
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = LoggerSingleton.logger;
    private PullDataSource source;
    private PullDataSource sourceForReadFrame;
    private ContentDescriptor[] supportedInputContentDescriptors = new ContentDescriptor[]{new ContentDescriptor(FileTypeDescriptor.GSM)};
    private PullSourceStreamTrack[] tracks;

    private class PullSourceStreamTrack extends AbstractTrack {
        private static final int GSM_FRAME_SIZE = 33;
        private long frameLength;
        private AudioFormat javaSoundInputFormat;
        private PullSourceStream stream;
        private long totalBytesRead;

        public PullSourceStreamTrack(PullSourceStream stream) {
            this.stream = stream;
            this.frameLength = stream.getContentLength() / 33;
        }

        private long bytesToNanos(long bytes) {
            return (long) GsmParser.secondsToNanos(((double) (bytes / 33)) / GsmParser.GSM_FRAME_RATE);
        }

        public Time getDuration() {
            long lengthInFrames = this.frameLength;
            if (lengthInFrames < 0) {
                GsmParser.logger.fine("PullSourceStreamTrack: returning Duration.DURATION_UNKNOWN (1)");
                return Duration.DURATION_UNKNOWN;
            }
            double lengthInSeconds = ((double) lengthInFrames) / GsmParser.GSM_FRAME_RATE;
            if (lengthInSeconds < Pa.LATENCY_UNSPECIFIED) {
                GsmParser.logger.fine("PullSourceStreamTrack: returning Duration.DURATION_UNKNOWN (2)");
                return Duration.DURATION_UNKNOWN;
            }
            double lengthInNanos = GsmParser.secondsToNanos(lengthInSeconds);
            GsmParser.logger.fine("PullSourceStreamTrack: returning " + ((long) lengthInNanos));
            return new Time((long) lengthInNanos);
        }

        public Format getFormat() {
            return new javax.media.format.AudioFormat("gsm", 8000.0d, 8, 1, -1, 1, 264, -1.0d, Format.byteArray);
        }

        public long getTotalBytesRead() {
            return this.totalBytesRead;
        }

        private long nanosToBytes(long nanos) {
            return (long) ((GsmParser.nanosToSeconds((double) nanos) * GsmParser.GSM_FRAME_RATE) * 33.0d);
        }

        public void readFrame(Buffer buffer) {
            if (buffer.getData() == null) {
                buffer.setData(new byte[16500]);
            }
            byte[] bytes = (byte[]) buffer.getData();
            try {
                int result = this.stream.read(bytes, 0, bytes.length);
                if (result < 0) {
                    buffer.setEOM(true);
                    buffer.setLength(0);
                    return;
                }
                buffer.setLength(result);
                buffer.setOffset(0);
            } catch (IOException e) {
                buffer.setEOM(true);
                buffer.setDiscard(true);
                buffer.setLength(0);
                GsmParser.logger.log(Level.WARNING, "" + e, e);
            }
        }

        public void setPssForReadFrame(PullSourceStream pullSourceStream) {
            this.stream = pullSourceStream;
        }

        public long skipNanos(long nanos) throws IOException {
            long bytes = nanosToBytes(nanos);
            if (bytes <= 0) {
                GsmParser.logger.fine("GsmParser: skipping nanos: 0");
                return 0;
            }
            this.totalBytesRead += 0;
            if (0 == bytes) {
                GsmParser.logger.fine("GsmParser: skipping nanos: " + nanos);
                return nanos;
            }
            long result = bytesToNanos(0);
            GsmParser.logger.fine("GsmParser: skipping nanos: " + result);
            return result;
        }
    }

    private static InputStream markSupportedInputStream(InputStream is) {
        return is.markSupported() ? is : new BufferedInputStream(is);
    }

    /* access modifiers changed from: private|static|final */
    public static final double nanosToSeconds(double nanos) {
        return nanos / 1.0E9d;
    }

    /* access modifiers changed from: private|static|final */
    public static final double secondsToNanos(double secs) {
        return 1.0E9d * secs;
    }

    public ContentDescriptor[] getSupportedInputContentDescriptors() {
        return this.supportedInputContentDescriptors;
    }

    public Track[] getTracks() throws IOException, BadHeaderException {
        return this.tracks;
    }

    public boolean isPositionable() {
        return true;
    }

    public boolean isRandomAccess() {
        return super.isRandomAccess();
    }

    public Time setPosition(Time where, int rounding) {
        return null;
    }

    public void setSource(DataSource source) throws IOException, IncompatibleSourceException {
        if (source instanceof PullDataSource) {
            this.source = (PullDataSource) source;
            return;
        }
        throw new IncompatibleSourceException();
    }

    public void start() throws IOException {
        this.source.start();
        PullSourceStream[] streamsForFormat = this.source.getStreams();
        this.tracks = new PullSourceStreamTrack[streamsForFormat.length];
        for (int i = 0; i < streamsForFormat.length; i++) {
            this.tracks[i] = new PullSourceStreamTrack(streamsForFormat[i]);
        }
    }

    public void stop() {
        try {
            this.source.stop();
        } catch (IOException e) {
            logger.log(Level.WARNING, "" + e, e);
        }
    }
}
