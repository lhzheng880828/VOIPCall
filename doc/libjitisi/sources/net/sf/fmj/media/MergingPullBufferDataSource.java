package net.sf.fmj.media;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.media.Duration;
import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;

public class MergingPullBufferDataSource extends PullBufferDataSource {
    protected final List<PullBufferDataSource> sources;

    public MergingPullBufferDataSource(List<PullBufferDataSource> sources) {
        this.sources = sources;
    }

    public void connect() throws IOException {
        for (PullBufferDataSource source : this.sources) {
            source.connect();
        }
    }

    public void disconnect() {
        for (PullBufferDataSource source : this.sources) {
            source.disconnect();
        }
    }

    public String getContentType() {
        for (int i = 0; i < this.sources.size(); i++) {
            if (!((PullBufferDataSource) this.sources.get(i)).getContentType().equals(((PullBufferDataSource) this.sources.get(0)).getContentType())) {
                return ContentDescriptor.MIXED;
            }
        }
        return ((PullBufferDataSource) this.sources.get(0)).getContentType();
    }

    public Object getControl(String controlType) {
        for (PullBufferDataSource source : this.sources) {
            Object control = source.getControl(controlType);
            if (control != null) {
                return control;
            }
        }
        return null;
    }

    public Object[] getControls() {
        List<Object> controls = new ArrayList();
        for (PullBufferDataSource source : this.sources) {
            for (Object control : source.getControls()) {
                controls.add(control);
            }
        }
        return controls.toArray(new Object[0]);
    }

    public Time getDuration() {
        List<Time> durations = new ArrayList();
        for (PullBufferDataSource source : this.sources) {
            durations.add(source.getDuration());
        }
        for (Time duration : durations) {
            if (duration.getNanoseconds() == Duration.DURATION_UNKNOWN.getNanoseconds()) {
                return Duration.DURATION_UNKNOWN;
            }
        }
        for (Time duration2 : durations) {
            if (duration2.getNanoseconds() == Duration.DURATION_UNBOUNDED.getNanoseconds()) {
                return Duration.DURATION_UNBOUNDED;
            }
        }
        long max = -1;
        for (Time duration22 : durations) {
            if (duration22.getNanoseconds() > max) {
                max = duration22.getNanoseconds();
            }
        }
        if (max < 0) {
            return Duration.DURATION_UNKNOWN;
        }
        return new Time(max);
    }

    public PullBufferStream[] getStreams() {
        List<PullBufferStream> streams = new ArrayList();
        for (PullBufferDataSource source : this.sources) {
            for (PullBufferStream stream : source.getStreams()) {
                streams.add(stream);
            }
        }
        return (PullBufferStream[]) streams.toArray(new PullBufferStream[0]);
    }

    public void start() throws IOException {
        for (PullBufferDataSource source : this.sources) {
            source.start();
        }
    }

    public void stop() throws IOException {
        for (PullBufferDataSource source : this.sources) {
            source.stop();
        }
    }
}
