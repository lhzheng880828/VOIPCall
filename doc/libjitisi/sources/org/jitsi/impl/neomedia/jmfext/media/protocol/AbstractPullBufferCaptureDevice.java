package org.jitsi.impl.neomedia.jmfext.media.protocol;

import java.io.IOException;
import javax.media.CaptureDeviceInfo;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.control.FormatControl;
import javax.media.control.FrameRateControl;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;

public abstract class AbstractPullBufferCaptureDevice extends PullBufferDataSource implements CaptureDevice {
    private CaptureDeviceInfo deviceInfo;
    private final AbstractBufferCaptureDevice<AbstractPullBufferStream<?>> impl = new AbstractBufferCaptureDevice<AbstractPullBufferStream<?>>() {
        /* access modifiers changed from: protected */
        public FrameRateControl createFrameRateControl() {
            return AbstractPullBufferCaptureDevice.this.createFrameRateControl();
        }

        /* access modifiers changed from: protected */
        public AbstractPullBufferStream<?> createStream(int streamIndex, FormatControl formatControl) {
            return AbstractPullBufferCaptureDevice.this.createStream(streamIndex, formatControl);
        }

        /* access modifiers changed from: protected */
        public void doConnect() throws IOException {
            AbstractPullBufferCaptureDevice.this.doConnect();
        }

        /* access modifiers changed from: protected */
        public void doDisconnect() {
            AbstractPullBufferCaptureDevice.this.doDisconnect();
        }

        /* access modifiers changed from: protected */
        public void doStart() throws IOException {
            AbstractPullBufferCaptureDevice.this.doStart();
        }

        /* access modifiers changed from: protected */
        public void doStop() throws IOException {
            AbstractPullBufferCaptureDevice.this.doStop();
        }

        public CaptureDeviceInfo getCaptureDeviceInfo() {
            return AbstractPullBufferCaptureDevice.this.getCaptureDeviceInfo();
        }

        /* access modifiers changed from: protected */
        public Format getFormat(int streamIndex, Format oldValue) {
            return AbstractPullBufferCaptureDevice.this.getFormat(streamIndex, oldValue);
        }

        /* access modifiers changed from: protected */
        public Format[] getSupportedFormats(int streamIndex) {
            return AbstractPullBufferCaptureDevice.this.getSupportedFormats(streamIndex);
        }

        /* access modifiers changed from: protected */
        public Format setFormat(int streamIndex, Format oldValue, Format newValue) {
            return AbstractPullBufferCaptureDevice.this.setFormat(streamIndex, oldValue, newValue);
        }
    };

    public abstract AbstractPullBufferStream<?> createStream(int i, FormatControl formatControl);

    protected AbstractPullBufferCaptureDevice() {
    }

    protected AbstractPullBufferCaptureDevice(MediaLocator locator) {
        setLocator(locator);
    }

    public void connect() throws IOException {
        this.impl.connect();
    }

    /* access modifiers changed from: protected */
    public FrameRateControl createFrameRateControl() {
        return null;
    }

    public void disconnect() {
        this.impl.disconnect();
    }

    /* access modifiers changed from: protected */
    public void doConnect() throws IOException {
    }

    /* access modifiers changed from: protected */
    public void doDisconnect() {
    }

    /* access modifiers changed from: protected */
    public void doStart() throws IOException {
        this.impl.defaultDoStart();
    }

    /* access modifiers changed from: protected */
    public void doStop() throws IOException {
        this.impl.defaultDoStop();
    }

    public CaptureDeviceInfo getCaptureDeviceInfo() {
        return this.deviceInfo == null ? AbstractBufferCaptureDevice.getCaptureDeviceInfo(this) : this.deviceInfo;
    }

    public String getContentType() {
        return ContentDescriptor.RAW;
    }

    public Object getControl(String controlType) {
        return this.impl.getControl(controlType);
    }

    public Object[] getControls() {
        return this.impl.getControls();
    }

    public Time getDuration() {
        return DURATION_UNBOUNDED;
    }

    /* access modifiers changed from: protected */
    public Format getFormat(int streamIndex, Format oldValue) {
        return this.impl.defaultGetFormat(streamIndex, oldValue);
    }

    public FormatControl[] getFormatControls() {
        return this.impl.getFormatControls();
    }

    /* access modifiers changed from: protected */
    public Object getStreamSyncRoot() {
        return this.impl.getStreamSyncRoot();
    }

    public PullBufferStream[] getStreams() {
        return (PullBufferStream[]) this.impl.getStreams(PullBufferStream.class);
    }

    /* access modifiers changed from: protected */
    public Format[] getSupportedFormats(int streamIndex) {
        return this.impl.defaultGetSupportedFormats(streamIndex);
    }

    public void setCaptureDeviceInfo(CaptureDeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    /* access modifiers changed from: protected */
    public Format setFormat(int streamIndex, Format oldValue, Format newValue) {
        return oldValue;
    }

    public void start() throws IOException {
        this.impl.start();
    }

    public void stop() throws IOException {
        this.impl.stop();
    }

    /* access modifiers changed from: protected */
    public AbstractBufferStream<?>[] streams() {
        return this.impl.streams();
    }
}
