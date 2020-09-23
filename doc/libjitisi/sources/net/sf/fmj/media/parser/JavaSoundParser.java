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
import javax.media.ResourceUnavailableException;
import javax.media.Time;
import javax.media.Track;
import javax.media.format.AudioFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.SourceCloneable;
import net.sf.fmj.media.AbstractDemultiplexer;
import net.sf.fmj.media.AbstractTrack;
import net.sf.fmj.media.PullSourceStreamInputStream;
import net.sf.fmj.media.renderer.audio.JavaSoundUtils;
import net.sf.fmj.utility.LoggerSingleton;
import org.jitsi.android.util.javax.sound.sampled.AudioInputStream;
import org.jitsi.android.util.javax.sound.sampled.AudioSystem;
import org.jitsi.android.util.javax.sound.sampled.UnsupportedAudioFileException;
import org.jitsi.impl.neomedia.portaudio.Pa;

public class JavaSoundParser extends AbstractDemultiplexer {
    private static final boolean OPEN_IN_SET_SOURCE = true;
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = LoggerSingleton.logger;
    private PullDataSource sourceForFormat;
    private PullDataSource sourceForReadFrame;
    private ContentDescriptor[] supportedInputContentDescriptors = new ContentDescriptor[]{new ContentDescriptor(FileTypeDescriptor.WAVE), new ContentDescriptor(FileTypeDescriptor.BASIC_AUDIO), new ContentDescriptor(FileTypeDescriptor.AIFF), new ContentDescriptor(FileTypeDescriptor.MPEG_AUDIO), new ContentDescriptor("audio.ogg"), new ContentDescriptor("application.ogg")};
    private PullSourceStreamTrack[] tracks;

    private class PullSourceStreamTrack extends AbstractTrack {
        private AudioInputStream aisForReadFrame;
        private final AudioFormat format;
        private final long frameLength;
        private final org.jitsi.android.util.javax.sound.sampled.AudioFormat javaSoundInputFormat;
        private PullSourceStream pssForReadFrame;
        private PullSourceStreamInputStream pssisForReadFrame;
        private long totalBytesRead = 0;

        public PullSourceStreamTrack(PullSourceStream pssForFormat, PullSourceStream pssForReadFrame) throws UnsupportedAudioFileException, IOException {
            PullSourceStreamInputStream pssisForFormat = new PullSourceStreamInputStream(pssForFormat);
            AudioInputStream aisForFormat = AudioSystem.getAudioInputStream(JavaSoundParser.markSupportedInputStream(pssisForFormat));
            this.javaSoundInputFormat = aisForFormat.getFormat();
            this.frameLength = aisForFormat.getFrameLength();
            this.format = JavaSoundUtils.convertFormat(this.javaSoundInputFormat);
            JavaSoundParser.logger.fine("JavaSoundParser: java sound format: " + this.javaSoundInputFormat);
            JavaSoundParser.logger.fine("JavaSoundParser: jmf format: " + this.format);
            JavaSoundParser.logger.fine("JavaSoundParser: Frame length=" + this.frameLength);
            aisForFormat.close();
            pssisForFormat.close();
            setPssForReadFrame(pssForReadFrame);
        }

        private long bytesToNanos(long bytes) {
            if (this.javaSoundInputFormat.getFrameSize() <= 0 || this.javaSoundInputFormat.getFrameRate() <= 0.0f) {
                return -1;
            }
            return (long) JavaSoundParser.secondsToNanos((double) (((float) (bytes / ((long) this.javaSoundInputFormat.getFrameSize()))) / this.javaSoundInputFormat.getFrameRate()));
        }

        public boolean canSkipNanos() {
            if (this.javaSoundInputFormat.getFrameSize() <= 0 || this.javaSoundInputFormat.getFrameRate() <= 0.0f) {
                return false;
            }
            return true;
        }

        public Time getDuration() {
            long lengthInFrames = this.frameLength;
            if (lengthInFrames < 0) {
                JavaSoundParser.logger.fine("PullSourceStreamTrack: returning Duration.DURATION_UNKNOWN (1)");
                return Duration.DURATION_UNKNOWN;
            }
            double lengthInSeconds = (double) (((float) lengthInFrames) / this.javaSoundInputFormat.getFrameRate());
            if (lengthInSeconds < Pa.LATENCY_UNSPECIFIED) {
                JavaSoundParser.logger.fine("PullSourceStreamTrack: returning Duration.DURATION_UNKNOWN (2)");
                return Duration.DURATION_UNKNOWN;
            }
            double lengthInNanos = JavaSoundParser.secondsToNanos(lengthInSeconds);
            JavaSoundParser.logger.fine("PullSourceStreamTrack: returning " + ((long) lengthInNanos));
            return new Time((long) lengthInNanos);
        }

        public Format getFormat() {
            return this.format;
        }

        public long getTotalBytesRead() {
            return this.totalBytesRead;
        }

        public Time mapFrameToTime(int frameNumber) {
            return TIME_UNKNOWN;
        }

        public int mapTimeToFrame(Time t) {
            return Integer.MAX_VALUE;
        }

        private long nanosToBytes(long nanos) {
            if (this.javaSoundInputFormat.getFrameSize() <= 0 || this.javaSoundInputFormat.getFrameRate() <= 0.0f) {
                return -1;
            }
            return (long) ((JavaSoundParser.nanosToSeconds((double) nanos) * ((double) this.javaSoundInputFormat.getFrameRate())) * ((double) this.javaSoundInputFormat.getFrameSize()));
        }

        public void readFrame(Buffer buffer) {
            if (buffer.getData() == null) {
                buffer.setData(new byte[10000]);
            }
            byte[] bytes = (byte[]) buffer.getData();
            try {
                int result = this.aisForReadFrame.read(bytes, 0, bytes.length);
                if (result < 0) {
                    buffer.setEOM(true);
                    buffer.setLength(0);
                    return;
                }
                if (this.javaSoundInputFormat.getFrameSize() > 0 && this.javaSoundInputFormat.getFrameRate() > 0.0f) {
                    buffer.setTimeStamp(bytesToNanos(this.totalBytesRead));
                    buffer.setDuration(bytesToNanos((long) result));
                }
                this.totalBytesRead += (long) result;
                buffer.setLength(result);
                buffer.setOffset(0);
            } catch (IOException e) {
                buffer.setEOM(true);
                buffer.setDiscard(true);
                buffer.setLength(0);
                JavaSoundParser.logger.log(Level.WARNING, "" + e, e);
            }
        }

        public void setPssForReadFrame(PullSourceStream pssForReadFrame) throws UnsupportedAudioFileException, IOException {
            this.pssForReadFrame = pssForReadFrame;
            this.pssisForReadFrame = new PullSourceStreamInputStream(pssForReadFrame);
            this.aisForReadFrame = AudioSystem.getAudioInputStream(JavaSoundParser.markSupportedInputStream(this.pssisForReadFrame));
            this.totalBytesRead = 0;
        }

        public long skipNanos(long nanos) throws IOException {
            long bytes = nanosToBytes(nanos);
            if (bytes <= 0) {
                JavaSoundParser.logger.fine("JavaSoundParser: skipping nanos: 0");
                return 0;
            }
            long bytesSkipped = this.aisForReadFrame.skip(bytes);
            this.totalBytesRead += bytesSkipped;
            if (bytesSkipped == bytes) {
                JavaSoundParser.logger.fine("JavaSoundParser: skipping nanos: " + nanos);
                return nanos;
            }
            long result = bytesToNanos(bytesSkipped);
            JavaSoundParser.logger.fine("JavaSoundParser: skipping nanos: " + result);
            return result;
        }
    }

    /* access modifiers changed from: private|static */
    public static InputStream markSupportedInputStream(InputStream is) {
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

    private void doOpen() throws IOException, UnsupportedAudioFileException {
        this.sourceForReadFrame = (PullDataSource) ((SourceCloneable) this.sourceForFormat).createClone();
        if (this.sourceForReadFrame == null) {
            throw new IOException("Could not create clone");
        }
        this.sourceForReadFrame.start();
        this.sourceForFormat.start();
        PullSourceStream[] streamsForFormat = this.sourceForFormat.getStreams();
        PullSourceStream[] streamsForReadFrame = this.sourceForReadFrame.getStreams();
        this.tracks = new PullSourceStreamTrack[streamsForFormat.length];
        for (int i = 0; i < streamsForFormat.length; i++) {
            this.tracks[i] = new PullSourceStreamTrack(streamsForFormat[i], streamsForReadFrame[i]);
        }
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

    public void open() throws ResourceUnavailableException {
    }

    public Time setPosition(Time where, int rounding) {
        int i;
        for (PullSourceStreamTrack canSkipNanos : this.tracks) {
            if (!canSkipNanos.canSkipNanos()) {
                return super.setPosition(where, rounding);
            }
        }
        if (where.getNanoseconds() == 0) {
            boolean noBytesRead = true;
            for (PullSourceStreamTrack canSkipNanos2 : this.tracks) {
                if (canSkipNanos2.getTotalBytesRead() != 0) {
                    noBytesRead = false;
                    break;
                }
            }
            if (noBytesRead) {
                return where;
            }
        }
        try {
            logger.fine("JavaSoundParser: cloning, reconnecting, and restarting source");
            this.sourceForReadFrame = (PullDataSource) ((SourceCloneable) this.sourceForFormat).createClone();
            if (this.sourceForReadFrame == null) {
                throw new RuntimeException("Could not create clone");
            }
            this.sourceForReadFrame.start();
            for (i = 0; i < this.tracks.length; i++) {
                this.tracks[i].setPssForReadFrame(this.sourceForReadFrame.getStreams()[i]);
                if (where.getNanoseconds() > 0) {
                    this.tracks[i].skipNanos(where.getNanoseconds());
                }
            }
            return where;
        } catch (IOException e) {
            logger.log(Level.WARNING, "" + e, e);
            throw new RuntimeException(e);
        } catch (UnsupportedAudioFileException e2) {
            logger.log(Level.WARNING, "" + e2, e2);
            throw new RuntimeException(e2);
        }
    }

    public void setSource(DataSource source) throws IOException, IncompatibleSourceException {
        if (!(source instanceof PullDataSource)) {
            throw new IncompatibleSourceException();
        } else if (source instanceof SourceCloneable) {
            this.sourceForFormat = (PullDataSource) source;
            try {
                doOpen();
            } catch (UnsupportedAudioFileException e) {
                logger.log(Level.INFO, "" + e);
                throw new IncompatibleSourceException("" + e);
            } catch (IOException e2) {
                logger.log(Level.WARNING, "" + e2, e2);
                throw e2;
            }
        } else {
            throw new IncompatibleSourceException();
        }
    }

    public void start() throws IOException {
    }
}
