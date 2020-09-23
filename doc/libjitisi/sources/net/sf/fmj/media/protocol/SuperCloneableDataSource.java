package net.sf.fmj.media.protocol;

import java.io.IOException;
import java.util.Vector;
import javax.media.Time;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.protocol.PushDataSource;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.SourceStream;

class SuperCloneableDataSource extends DataSource {
    private Vector clones = new Vector();
    protected DataSource input;
    public SourceStream[] streams = null;
    public CloneableSourceStreamAdapter[] streamsAdapters;

    class PushBufferDataSourceSlave extends PushBufferDataSource {
        PushBufferStream[] streams = null;

        public PushBufferDataSourceSlave() {
            this.streams = new PushBufferStream[SuperCloneableDataSource.this.streamsAdapters.length];
            for (int i = 0; i < this.streams.length; i++) {
                this.streams[i] = (PushBufferStream) SuperCloneableDataSource.this.streamsAdapters[i].createSlave();
            }
        }

        public void connect() throws IOException {
            for (PushBufferStream pushBufferStream : this.streams) {
                ((SourceStreamSlave) pushBufferStream).connect();
            }
        }

        public void disconnect() {
            for (PushBufferStream pushBufferStream : this.streams) {
                ((SourceStreamSlave) pushBufferStream).disconnect();
            }
        }

        public String getContentType() {
            return SuperCloneableDataSource.this.input.getContentType();
        }

        public Object getControl(String controlType) {
            return SuperCloneableDataSource.this.input.getControl(controlType);
        }

        public Object[] getControls() {
            return SuperCloneableDataSource.this.input.getControls();
        }

        public Time getDuration() {
            return SuperCloneableDataSource.this.input.getDuration();
        }

        public PushBufferStream[] getStreams() {
            return this.streams;
        }

        public void start() throws IOException {
        }

        public void stop() throws IOException {
        }
    }

    class PushDataSourceSlave extends PushDataSource {
        PushSourceStream[] streams = null;

        public PushDataSourceSlave() {
            this.streams = new PushSourceStream[SuperCloneableDataSource.this.streamsAdapters.length];
            for (int i = 0; i < this.streams.length; i++) {
                this.streams[i] = (PushSourceStream) SuperCloneableDataSource.this.streamsAdapters[i].createSlave();
            }
        }

        public void connect() throws IOException {
            for (PushSourceStream pushSourceStream : this.streams) {
                ((SourceStreamSlave) pushSourceStream).connect();
            }
        }

        public void disconnect() {
            for (PushSourceStream pushSourceStream : this.streams) {
                ((SourceStreamSlave) pushSourceStream).disconnect();
            }
        }

        public String getContentType() {
            return SuperCloneableDataSource.this.input.getContentType();
        }

        public Object getControl(String controlType) {
            return SuperCloneableDataSource.this.input.getControl(controlType);
        }

        public Object[] getControls() {
            return SuperCloneableDataSource.this.input.getControls();
        }

        public Time getDuration() {
            return SuperCloneableDataSource.this.input.getDuration();
        }

        public PushSourceStream[] getStreams() {
            return this.streams;
        }

        public void start() throws IOException {
        }

        public void stop() throws IOException {
        }
    }

    SuperCloneableDataSource(DataSource input) {
        this.input = input;
        SourceStream[] originalStreams = null;
        if (input instanceof PullDataSource) {
            originalStreams = ((PullDataSource) input).getStreams();
        }
        if (input instanceof PushDataSource) {
            originalStreams = ((PushDataSource) input).getStreams();
        }
        if (input instanceof PullBufferDataSource) {
            originalStreams = ((PullBufferDataSource) input).getStreams();
        }
        if (input instanceof PushBufferDataSource) {
            originalStreams = ((PushBufferDataSource) input).getStreams();
        }
        this.streamsAdapters = new CloneableSourceStreamAdapter[originalStreams.length];
        for (int i = 0; i < originalStreams.length; i++) {
            this.streamsAdapters[i] = new CloneableSourceStreamAdapter(originalStreams[i]);
        }
    }

    public void connect() throws IOException {
        this.input.connect();
    }

    /* access modifiers changed from: 0000 */
    public DataSource createClone() {
        DataSource newSlave;
        if ((this.input instanceof PullDataSource) || (this.input instanceof PushDataSource)) {
            newSlave = new PushDataSourceSlave();
        } else {
            newSlave = new PushBufferDataSourceSlave();
        }
        this.clones.addElement(newSlave);
        try {
            newSlave.connect();
            return newSlave;
        } catch (IOException e) {
            return null;
        }
    }

    public void disconnect() {
        this.input.disconnect();
    }

    public String getContentType() {
        return this.input.getContentType();
    }

    public Object getControl(String controlType) {
        return this.input.getControl(controlType);
    }

    public Object[] getControls() {
        return this.input.getControls();
    }

    public Time getDuration() {
        return this.input.getDuration();
    }

    public void start() throws IOException {
        this.input.start();
    }

    public void stop() throws IOException {
        this.input.stop();
    }
}
