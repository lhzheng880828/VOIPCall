package net.sf.fmj.media.protocol;

import java.io.IOException;
import javax.media.Buffer;
import javax.media.Duration;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import net.sf.fmj.media.Log;

public class DelegateDataSource extends PushBufferDataSource implements Streamable {
    protected boolean connected = false;
    protected String contentType = ContentDescriptor.RAW;
    protected PushBufferDataSource master;
    protected boolean started = false;
    protected DelegateStream[] streams;

    class DelegateStream implements PushBufferStream, BufferTransferHandler {
        Format format;
        PushBufferStream master;
        BufferTransferHandler th;

        public DelegateStream(Format format) {
            this.format = format;
        }

        public boolean endOfStream() {
            if (this.master != null) {
                return this.master.endOfStream();
            }
            return false;
        }

        public ContentDescriptor getContentDescriptor() {
            if (this.master != null) {
                return this.master.getContentDescriptor();
            }
            return new ContentDescriptor(ContentDescriptor.RAW);
        }

        public long getContentLength() {
            if (this.master != null) {
                return this.master.getContentLength();
            }
            return -1;
        }

        public Object getControl(String controlType) {
            if (this.master != null) {
                return this.master.getControl(controlType);
            }
            return null;
        }

        public Object[] getControls() {
            if (this.master != null) {
                return this.master.getControls();
            }
            return new Object[0];
        }

        public Format getFormat() {
            if (this.master != null) {
                return this.master.getFormat();
            }
            return this.format;
        }

        public PushBufferStream getMaster() {
            return this.master;
        }

        public void read(Buffer buffer) throws IOException {
            if (this.master != null) {
                this.master.read(buffer);
            }
            throw new IOException("No data available");
        }

        public void setMaster(PushBufferStream master) {
            this.master = master;
            master.setTransferHandler(this);
        }

        public void setTransferHandler(BufferTransferHandler transferHandler) {
            this.th = transferHandler;
        }

        public void transferData(PushBufferStream stream) {
            if (this.th != null) {
                this.th.transferData(stream);
            }
        }
    }

    public DelegateDataSource(Format[] format) {
        this.streams = new DelegateStream[format.length];
        for (int i = 0; i < format.length; i++) {
            this.streams[i] = new DelegateStream(format[i]);
        }
        try {
            connect();
        } catch (IOException e) {
        }
    }

    public void connect() throws IOException {
        if (!this.connected) {
            if (this.master != null) {
                this.master.connect();
            }
            this.connected = true;
        }
    }

    public void disconnect() {
        try {
            if (this.started) {
                stop();
            }
        } catch (IOException e) {
        }
        if (this.master != null) {
            this.master.disconnect();
        }
        this.connected = false;
    }

    public String getContentType() {
        if (this.connected) {
            return this.contentType;
        }
        System.err.println("Error: DataSource not connected");
        return null;
    }

    public Object getControl(String controlType) {
        if (this.master != null) {
            return this.master.getControl(controlType);
        }
        return null;
    }

    public Object[] getControls() {
        if (this.master != null) {
            return this.master.getControls();
        }
        return new Object[0];
    }

    public Time getDuration() {
        if (this.master != null) {
            return this.master.getDuration();
        }
        return Duration.DURATION_UNKNOWN;
    }

    public MediaLocator getLocator() {
        if (this.master != null) {
            return this.master.getLocator();
        }
        return null;
    }

    public DataSource getMaster() {
        return this.master;
    }

    public PushBufferStream[] getStreams() {
        return this.streams;
    }

    public boolean isPrefetchable() {
        return false;
    }

    public void setMaster(PushBufferDataSource ds) throws IOException {
        int i;
        this.master = ds;
        PushBufferStream[] mstrms = ds.getStreams();
        for (i = 0; i < mstrms.length; i++) {
            for (int j = 0; j < this.streams.length; j++) {
                if (this.streams[j].getFormat().matches(mstrms[i].getFormat())) {
                    this.streams[j].setMaster(mstrms[i]);
                }
            }
        }
        for (i = 0; i < mstrms.length; i++) {
            if (this.streams[i].getMaster() == null) {
                Log.error("DelegateDataSource: cannot not find a matching track from the master with this format: " + this.streams[i].getFormat());
            }
        }
        if (this.connected) {
            this.master.connect();
        }
        if (this.started) {
            this.master.start();
        }
    }

    public void start() throws IOException {
        if (!this.connected) {
            throw new Error("DataSource must be connected before it can be started");
        } else if (!this.started) {
            if (this.master != null) {
                this.master.start();
            }
            this.started = true;
        }
    }

    public void stop() throws IOException {
        if (this.connected && this.started) {
            if (this.master != null) {
                this.master.stop();
            }
            this.started = false;
        }
    }
}
