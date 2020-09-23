package org.jitsi.impl.neomedia.protocol;

import java.io.IOException;
import javax.media.CaptureDeviceInfo;
import javax.media.Time;
import javax.media.control.FormatControl;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import org.jitsi.impl.neomedia.control.ControlsAdapter;

public class CaptureDeviceDelegatePushBufferDataSource extends PushBufferDataSource implements CaptureDevice {
    protected static final PushBufferStream[] EMPTY_STREAMS = new PushBufferStream[0];
    protected final CaptureDevice captureDevice;

    public CaptureDeviceDelegatePushBufferDataSource(CaptureDevice captureDevice) {
        this.captureDevice = captureDevice;
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

    public CaptureDeviceInfo getCaptureDeviceInfo() {
        return this.captureDevice != null ? this.captureDevice.getCaptureDeviceInfo() : null;
    }

    public String getContentType() {
        if (this.captureDevice instanceof DataSource) {
            return ((DataSource) this.captureDevice).getContentType();
        }
        return ContentDescriptor.CONTENT_UNKNOWN;
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

    public PushBufferStream[] getStreams() {
        if (this.captureDevice instanceof PushBufferDataSource) {
            return ((PushBufferDataSource) this.captureDevice).getStreams();
        }
        return EMPTY_STREAMS;
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
}
