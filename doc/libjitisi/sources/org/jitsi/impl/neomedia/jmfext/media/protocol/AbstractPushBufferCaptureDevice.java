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
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;

public abstract class AbstractPushBufferCaptureDevice extends PushBufferDataSource implements CaptureDevice {
    private final AbstractBufferCaptureDevice<AbstractPushBufferStream<?>> impl;

    public abstract AbstractPushBufferStream<?> createStream(int i, FormatControl formatControl);

    protected AbstractPushBufferCaptureDevice() {
        this(null);
    }

    protected AbstractPushBufferCaptureDevice(MediaLocator locator) {
        this.impl = new AbstractBufferCaptureDevice<AbstractPushBufferStream<?>>() {
            /* access modifiers changed from: protected */
            public FrameRateControl createFrameRateControl() {
                return AbstractPushBufferCaptureDevice.this.createFrameRateControl();
            }

            /* access modifiers changed from: protected */
            public AbstractPushBufferStream<?> createStream(int streamIndex, FormatControl formatControl) {
                return AbstractPushBufferCaptureDevice.this.createStream(streamIndex, formatControl);
            }

            /* access modifiers changed from: protected */
            public void doConnect() throws IOException {
                AbstractPushBufferCaptureDevice.this.doConnect();
            }

            /* access modifiers changed from: protected */
            public void doDisconnect() {
                AbstractPushBufferCaptureDevice.this.doDisconnect();
            }

            /* access modifiers changed from: protected */
            public void doStart() throws IOException {
                AbstractPushBufferCaptureDevice.this.doStart();
            }

            /* access modifiers changed from: protected */
            public void doStop() throws IOException {
                AbstractPushBufferCaptureDevice.this.doStop();
            }

            public CaptureDeviceInfo getCaptureDeviceInfo() {
                return AbstractPushBufferCaptureDevice.this.getCaptureDeviceInfo();
            }

            public Object[] getControls() {
                return AbstractPushBufferCaptureDevice.this.getControls();
            }

            /* access modifiers changed from: protected */
            public Format getFormat(int streamIndex, Format oldValue) {
                return AbstractPushBufferCaptureDevice.this.getFormat(streamIndex, oldValue);
            }

            /* access modifiers changed from: protected */
            public Format[] getSupportedFormats(int streamIndex) {
                return AbstractPushBufferCaptureDevice.this.getSupportedFormats(streamIndex);
            }

            /* access modifiers changed from: protected */
            public Format setFormat(int streamIndex, Format oldValue, Format newValue) {
                return AbstractPushBufferCaptureDevice.this.setFormat(streamIndex, oldValue, newValue);
            }
        };
        if (locator != null) {
            setLocator(locator);
        }
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
        return AbstractBufferCaptureDevice.getCaptureDeviceInfo(this);
    }

    public String getContentType() {
        return ContentDescriptor.RAW;
    }

    public Object getControl(String controlType) {
        return this.impl.getControl(controlType);
    }

    public Object[] getControls() {
        return this.impl.defaultGetControls();
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

    public PushBufferStream[] getStreams() {
        return (PushBufferStream[]) this.impl.getStreams(PushBufferStream.class);
    }

    /* access modifiers changed from: protected */
    public Format[] getSupportedFormats(int streamIndex) {
        return this.impl.defaultGetSupportedFormats(streamIndex);
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
