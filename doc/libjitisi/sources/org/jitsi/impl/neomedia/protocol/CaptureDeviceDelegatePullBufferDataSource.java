package org.jitsi.impl.neomedia.protocol;

import java.io.IOException;
import javax.media.CaptureDeviceInfo;
import javax.media.Time;
import javax.media.control.FormatControl;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;
import org.jitsi.impl.neomedia.control.ControlsAdapter;

public class CaptureDeviceDelegatePullBufferDataSource extends PullBufferDataSource implements CaptureDevice {
    protected static final PullBufferStream[] EMPTY_STREAMS = new PullBufferStream[0];
    protected final CaptureDevice captureDevice;

    public CaptureDeviceDelegatePullBufferDataSource(CaptureDevice captureDevice) {
        this.captureDevice = captureDevice;
    }

    public PullBufferStream[] getStreams() {
        if (this.captureDevice instanceof PullBufferDataSource) {
            return ((PullBufferDataSource) this.captureDevice).getStreams();
        }
        return EMPTY_STREAMS;
    }

    public String getContentType() {
        if (this.captureDevice instanceof DataSource) {
            return ((DataSource) this.captureDevice).getContentType();
        }
        return ContentDescriptor.CONTENT_UNKNOWN;
    }

    public void connect() throws IOException {
        if (this.captureDevice != null) {
            this.captureDevice.connect();
        }
    }

    public void disconnect() {
        if (this.captureDevice != null) {
            this.captureDevice.disconnect();
        }
    }

    public void start() throws IOException {
        if (this.captureDevice != null) {
            this.captureDevice.start();
        }
    }

    public void stop() throws IOException {
        if (this.captureDevice != null) {
            this.captureDevice.stop();
        }
    }

    public Object getControl(String controlType) {
        if (this.captureDevice instanceof DataSource) {
            return ((DataSource) this.captureDevice).getControl(controlType);
        }
        return null;
    }

    public Object[] getControls() {
        if (this.captureDevice instanceof DataSource) {
            return ((DataSource) this.captureDevice).getControls();
        }
        return ControlsAdapter.EMPTY_CONTROLS;
    }

    public Time getDuration() {
        if (this.captureDevice instanceof DataSource) {
            return ((DataSource) this.captureDevice).getDuration();
        }
        return DataSource.DURATION_UNKNOWN;
    }

    public FormatControl[] getFormatControls() {
        return this.captureDevice != null ? this.captureDevice.getFormatControls() : new FormatControl[0];
    }

    public CaptureDeviceInfo getCaptureDeviceInfo() {
        return this.captureDevice != null ? this.captureDevice.getCaptureDeviceInfo() : null;
    }
}
