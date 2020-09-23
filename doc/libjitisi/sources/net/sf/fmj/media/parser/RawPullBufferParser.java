package net.sf.fmj.media.parser;

import java.io.IOException;
import javax.media.Buffer;
import javax.media.Demultiplexer;
import javax.media.Format;
import javax.media.IncompatibleSourceException;
import javax.media.Time;
import javax.media.Track;
import javax.media.TrackListener;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;
import javax.media.protocol.SourceStream;

public class RawPullBufferParser extends RawPullStreamParser {
    static final String NAME = "Raw pull stream parser";

    class FrameTrack implements Track {
        boolean enabled = true;
        Format format = null;
        TrackListener listener;
        Demultiplexer parser;
        PullBufferStream pbs;
        Integer stateReq = new Integer(0);

        public FrameTrack(Demultiplexer parser, PullBufferStream pbs) {
            this.pbs = pbs;
            this.format = pbs.getFormat();
        }

        public Time getDuration() {
            return this.parser.getDuration();
        }

        public Format getFormat() {
            return this.format;
        }

        public Time getStartTime() {
            return new Time(0);
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public Time mapFrameToTime(int frameNumber) {
            return new Time(0);
        }

        public int mapTimeToFrame(Time t) {
            return -1;
        }

        public void readFrame(Buffer buffer) {
            if (buffer.getData() == null) {
                buffer.setData(new byte[500]);
            }
            try {
                this.pbs.read(buffer);
            } catch (IOException e) {
                buffer.setDiscard(true);
            }
        }

        public void setEnabled(boolean t) {
            this.enabled = t;
        }

        public void setTrackListener(TrackListener l) {
            this.listener = l;
        }
    }

    public String getName() {
        return NAME;
    }

    public void open() {
        if (this.tracks == null) {
            this.tracks = new Track[this.streams.length];
            for (int i = 0; i < this.streams.length; i++) {
                this.tracks[i] = new FrameTrack(this, (PullBufferStream) this.streams[i]);
            }
        }
    }

    public void setSource(DataSource source) throws IOException, IncompatibleSourceException {
        if (source instanceof PullBufferDataSource) {
            this.streams = ((PullBufferDataSource) source).getStreams();
            if (this.streams == null) {
                throw new IOException("Got a null stream from the DataSource");
            } else if (this.streams.length == 0) {
                throw new IOException("Got a empty stream array from the DataSource");
            } else if (supports(this.streams)) {
                this.source = source;
                this.streams = this.streams;
                return;
            } else {
                throw new IncompatibleSourceException("DataSource not supported: " + source);
            }
        }
        throw new IncompatibleSourceException("DataSource not supported: " + source);
    }

    /* access modifiers changed from: protected */
    public boolean supports(SourceStream[] streams) {
        return streams[0] != null && (streams[0] instanceof PullBufferStream);
    }
}
