package net.sf.fmj.media.parser;

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
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;
import net.sf.fmj.media.AbstractDemultiplexer;
import net.sf.fmj.media.AbstractTrack;
import net.sf.fmj.media.PullSourceStreamInputStream;
import net.sf.fmj.utility.LoggerSingleton;
import org.xml.sax.SAXException;

public class XmlMovieParser extends AbstractDemultiplexer {
    private static final Logger logger = LoggerSingleton.logger;
    private PullDataSource source;
    private ContentDescriptor[] supportedInputContentDescriptors = new ContentDescriptor[]{new ContentDescriptor("video.xml")};
    private PullSourceStreamTrack[] tracks;
    /* access modifiers changed from: private */
    public XmlMovieSAXHandler xmlMovieSAXHandler;
    private XmlMovieSAXParserThread xmlMovieSAXParserThread;

    private abstract class PullSourceStreamTrack extends AbstractTrack {
        public abstract void deallocate();

        private PullSourceStreamTrack() {
        }
    }

    private class VideoTrack extends PullSourceStreamTrack {
        private final Format format;
        private final int track;

        public VideoTrack(int track, Format format) throws ResourceUnavailableException {
            super();
            this.track = track;
            this.format = format;
        }

        public boolean canSkipNanos() {
            return false;
        }

        public void deallocate() {
        }

        public Time getDuration() {
            return Duration.DURATION_UNKNOWN;
        }

        public Format getFormat() {
            return this.format;
        }

        public Time mapFrameToTime(int frameNumber) {
            return TIME_UNKNOWN;
        }

        public int mapTimeToFrame(Time t) {
            return Integer.MAX_VALUE;
        }

        public void readFrame(Buffer buffer) {
            try {
                buffer.copy(XmlMovieParser.this.xmlMovieSAXHandler.readBuffer(this.track));
            } catch (SAXException e) {
                throw new RuntimeException(e);
            } catch (IOException e2) {
                throw new RuntimeException(e2);
            } catch (InterruptedException e3) {
                throw new RuntimeException(e3);
            }
        }

        public long skipNanos(long nanos) throws IOException {
            return 0;
        }
    }

    public void close() {
        if (this.tracks != null) {
            for (int i = 0; i < this.tracks.length; i++) {
                if (this.tracks[i] != null) {
                    this.tracks[i].deallocate();
                    this.tracks[i] = null;
                }
            }
            this.tracks = null;
        }
        super.close();
    }

    public ContentDescriptor[] getSupportedInputContentDescriptors() {
        return this.supportedInputContentDescriptors;
    }

    public Track[] getTracks() throws IOException, BadHeaderException {
        return this.tracks;
    }

    public boolean isPositionable() {
        return false;
    }

    public boolean isRandomAccess() {
        return super.isRandomAccess();
    }

    public void open() throws ResourceUnavailableException {
        try {
            this.source.start();
            PullSourceStream[] streams = this.source.getStreams();
            if (streams.length > 1) {
                logger.warning("only 1 stream supported, " + streams.length + " found");
            }
            InputStream is = new PullSourceStreamInputStream(streams[0]);
            this.xmlMovieSAXHandler = new XmlMovieSAXHandler();
            this.xmlMovieSAXParserThread = new XmlMovieSAXParserThread(this.xmlMovieSAXHandler, is);
            this.xmlMovieSAXParserThread.start();
            Format[] formats = this.xmlMovieSAXHandler.readTracksInfo();
            this.tracks = new PullSourceStreamTrack[formats.length];
            for (int i = 0; i < formats.length; i++) {
                this.tracks[i] = new VideoTrack(i, formats[i]);
            }
            super.open();
        } catch (IOException e) {
            logger.log(Level.WARNING, "" + e, e);
            throw new ResourceUnavailableException("" + e);
        } catch (SAXException e2) {
            logger.log(Level.WARNING, "" + e2, e2);
            throw new ResourceUnavailableException("" + e2);
        } catch (InterruptedException e3) {
            logger.log(Level.WARNING, "" + e3, e3);
            throw new ResourceUnavailableException("" + e3);
        }
    }

    public void setSource(DataSource source) throws IOException, IncompatibleSourceException {
        String protocol = source.getLocator().getProtocol();
        if (source instanceof PullDataSource) {
            this.source = (PullDataSource) source;
            return;
        }
        throw new IncompatibleSourceException();
    }

    public void start() throws IOException {
    }
}
