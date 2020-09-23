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
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.SourceStream;

public class RawPullStreamParser extends RawParser {
    static final String NAME = "Raw pull stream parser";
    protected SourceStream[] streams;
    protected Track[] tracks = null;

    class FrameTrack implements Track {
        boolean enabled = true;
        Format format = null;
        TrackListener listener;
        Demultiplexer parser;
        PullSourceStream pss;
        Integer stateReq = new Integer(0);

        public FrameTrack(Demultiplexer parser, PullSourceStream pss) {
            this.pss = pss;
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
            byte[] data = (byte[]) buffer.getData();
            if (data == null) {
                data = new byte[500];
                buffer.setData(data);
            }
            try {
                buffer.setLength(this.pss.read(data, 0, data.length));
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

    public void close() {
        if (this.source != null) {
            try {
                this.source.stop();
                this.source.disconnect();
            } catch (IOException e) {
            }
            this.source = null;
        }
    }

    public String getName() {
        return NAME;
    }

    public Track[] getTracks() {
        return this.tracks;
    }

    public void open() {
        if (this.tracks == null) {
            this.tracks = new Track[this.streams.length];
            for (int i = 0; i < this.streams.length; i++) {
                this.tracks[i] = new FrameTrack(this, (PullSourceStream) this.streams[i]);
            }
        }
    }

    public void setSource(DataSource source) throws IOException, IncompatibleSourceException {
        if (source instanceof PullDataSource) {
            this.streams = ((PullDataSource) source).getStreams();
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

    public void start() throws IOException {
        this.source.start();
    }

    public void stop() {
        try {
            this.source.stop();
        } catch (IOException e) {
        }
    }

    /* access modifiers changed from: protected */
    public boolean supports(SourceStream[] streams) {
        return streams[0] != null && (streams[0] instanceof PullSourceStream);
    }
}
