package org.jitsi.impl.neomedia.protocol;

import java.io.IOException;
import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferStream;

public abstract class PushBufferDataSourceDelegate<T extends DataSource> extends CaptureDeviceDelegatePushBufferDataSource {
    protected final T dataSource;

    public abstract PushBufferStream[] getStreams();

    public PushBufferDataSourceDelegate(T dataSource) {
        super(dataSource instanceof CaptureDevice ? (CaptureDevice) dataSource : null);
        if (dataSource == null) {
            throw new NullPointerException("dataSource");
        }
        this.dataSource = dataSource;
    }

    public void connect() throws IOException {
        this.dataSource.connect();
    }

    public void disconnect() {
        this.dataSource.disconnect();
    }

    public String getContentType() {
        return this.dataSource.getContentType();
    }

    public MediaLocator getLocator() {
        return this.dataSource.getLocator();
    }

    public Object getControl(String controlType) {
        return this.dataSource.getControl(controlType);
    }

    public Object[] getControls() {
        return this.dataSource.getControls();
    }

    public T getDataSource() {
        return this.dataSource;
    }

    public Time getDuration() {
        return this.dataSource.getDuration();
    }

    public void start() throws IOException {
        this.dataSource.start();
    }

    public void stop() throws IOException {
        this.dataSource.stop();
    }
}
