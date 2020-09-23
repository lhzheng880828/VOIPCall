package net.sf.fmj.media;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.media.Duration;
import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;

public class MergingPushBufferDataSource extends PushBufferDataSource {
    protected final List<PushBufferDataSource> sources;

    public MergingPushBufferDataSource(List<PushBufferDataSource> sources) {
        this.sources = sources;
    }

    public void connect() throws IOException {
        for (PushBufferDataSource source : this.sources) {
            source.connect();
        }
    }

    public void disconnect() {
        for (PushBufferDataSource source : this.sources) {
            source.disconnect();
        }
    }

    public String getContentType() {
        for (int i = 0; i < this.sources.size(); i++) {
            if (!((PushBufferDataSource) this.sources.get(i)).getContentType().equals(((PushBufferDataSource) this.sources.get(0)).getContentType())) {
                return ContentDescriptor.MIXED;
            }
        }
        return ((PushBufferDataSource) this.sources.get(0)).getContentType();
    }

    public Object getControl(String controlType) {
        for (PushBufferDataSource source : this.sources) {
            Object control = source.getControl(controlType);
            if (control != null) {
                return control;
            }
        }
        return null;
    }

    public Object[] getControls() {
        List<Object> controls = new ArrayList();
        for (PushBufferDataSource source : this.sources) {
            for (Object control : source.getControls()) {
                controls.add(control);
            }
        }
        return controls.toArray(new Object[0]);
    }

    public Time getDuration() {
        List<Time> durations = new ArrayList();
        for (PushBufferDataSource source : this.sources) {
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

    public PushBufferStream[] getStreams() {
        List<PushBufferStream> streams = new ArrayList();
        for (PushBufferDataSource source : this.sources) {
            for (PushBufferStream stream : source.getStreams()) {
                streams.add(stream);
            }
        }
        return (PushBufferStream[]) streams.toArray(new PushBufferStream[0]);
    }

    public void start() throws IOException {
        for (PushBufferDataSource source : this.sources) {
            source.start();
        }
    }

    public void stop() throws IOException {
        for (PushBufferDataSource source : this.sources) {
            source.stop();
        }
    }
}
