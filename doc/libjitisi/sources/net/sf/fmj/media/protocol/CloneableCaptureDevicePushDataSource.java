package net.sf.fmj.media.protocol;

import java.io.IOException;
import javax.media.CaptureDeviceInfo;
import javax.media.Time;
import javax.media.control.FormatControl;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushDataSource;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.SourceCloneable;

public class CloneableCaptureDevicePushDataSource extends PushDataSource implements SourceCloneable, CaptureDevice {
    private SuperCloneableDataSource superClass;

    public CloneableCaptureDevicePushDataSource(PushDataSource source) {
        this.superClass = new SuperCloneableDataSource(source);
    }

    public void connect() throws IOException {
        this.superClass.connect();
    }

    public DataSource createClone() {
        return this.superClass.createClone();
    }

    public void disconnect() {
        this.superClass.disconnect();
    }

    public CaptureDeviceInfo getCaptureDeviceInfo() {
        return ((CaptureDevice) this.superClass.input).getCaptureDeviceInfo();
    }

    public String getContentType() {
        return this.superClass.getContentType();
    }

    public Object getControl(String controlType) {
        return this.superClass.getControl(controlType);
    }

    public Object[] getControls() {
        return this.superClass.getControls();
    }

    public Time getDuration() {
        return this.superClass.getDuration();
    }

    public FormatControl[] getFormatControls() {
        return ((CaptureDevice) this.superClass.input).getFormatControls();
    }

    public PushSourceStream[] getStreams() {
        if (this.superClass.streams == null) {
            this.superClass.streams = new PushSourceStream[this.superClass.streamsAdapters.length];
            for (int i = 0; i < this.superClass.streamsAdapters.length; i++) {
                this.superClass.streams[i] = this.superClass.streamsAdapters[i].getAdapter();
            }
        }
        return (PushSourceStream[]) this.superClass.streams;
    }

    public void start() throws IOException {
        this.superClass.start();
    }

    public void stop() throws IOException {
        this.superClass.stop();
    }
}
