package org.jitsi.impl.neomedia.protocol;

import java.io.IOException;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.control.FormatControl;
import org.jitsi.impl.neomedia.jmfext.media.protocol.AbstractPushBufferCaptureDevice;
import org.jitsi.impl.neomedia.jmfext.media.protocol.AbstractPushBufferStream;

public class FakePushBufferDataSource extends AbstractPushBufferCaptureDevice {
    private final Format[] supportedFormats;

    private static class FakePushBufferStream extends AbstractPushBufferStream<FakePushBufferDataSource> {
        FakePushBufferStream(FakePushBufferDataSource dataSource, FormatControl formatControl) {
            super(dataSource, formatControl);
        }

        /* access modifiers changed from: protected */
        public Format doSetFormat(Format format) {
            return format;
        }

        public void read(Buffer buffer) throws IOException {
        }
    }

    public FakePushBufferDataSource(Format... supportedFormats) {
        this.supportedFormats = supportedFormats == null ? null : (Format[]) supportedFormats.clone();
    }

    public void connect() throws IOException {
    }

    /* access modifiers changed from: protected */
    public FakePushBufferStream createStream(int streamIndex, FormatControl formatControl) {
        return new FakePushBufferStream(this, formatControl);
    }

    public void disconnect() {
    }

    /* access modifiers changed from: protected */
    public Format[] getSupportedFormats(int streamIndex) {
        return this.supportedFormats == null ? null : (Format[]) this.supportedFormats.clone();
    }

    /* access modifiers changed from: protected */
    public Format setFormat(int streamIndex, Format oldValue, Format newValue) {
        return newValue;
    }

    public void start() throws IOException {
    }

    public void stop() throws IOException {
    }
}
