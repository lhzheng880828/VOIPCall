package org.jitsi.impl.neomedia.jmfext.media.protocol;

import java.io.IOException;
import javax.media.Format;
import javax.media.control.FormatControl;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.SourceStream;
import org.jitsi.impl.neomedia.control.AbstractControls;
import org.jitsi.impl.neomedia.control.ControlsAdapter;
import org.jitsi.util.Logger;

abstract class AbstractBufferStream<T extends DataSource> extends AbstractControls implements SourceStream {
    private static final ContentDescriptor CONTENT_DESCRIPTOR = new ContentDescriptor(ContentDescriptor.RAW);
    private static final Logger logger = Logger.getLogger(AbstractBufferStream.class);
    protected final T dataSource;
    protected final FormatControl formatControl;

    protected AbstractBufferStream(T dataSource, FormatControl formatControl) {
        this.dataSource = dataSource;
        this.formatControl = formatControl;
    }

    public void close() {
        try {
            stop();
        } catch (IOException ioex) {
            logger.error("Failed to stop " + getClass().getSimpleName(), ioex);
        }
    }

    /* access modifiers changed from: protected */
    public Format doGetFormat() {
        return null;
    }

    /* access modifiers changed from: protected */
    public Format doSetFormat(Format format) {
        return null;
    }

    public boolean endOfStream() {
        return false;
    }

    public ContentDescriptor getContentDescriptor() {
        return CONTENT_DESCRIPTOR;
    }

    public long getContentLength() {
        return -1;
    }

    public Object[] getControls() {
        if (this.formatControl == null) {
            return ControlsAdapter.EMPTY_CONTROLS;
        }
        return new Object[]{this.formatControl};
    }

    public Format getFormat() {
        return this.formatControl == null ? null : this.formatControl.getFormat();
    }

    /* access modifiers changed from: 0000 */
    public Format internalGetFormat() {
        return doGetFormat();
    }

    /* access modifiers changed from: 0000 */
    public Format internalSetFormat(Format format) {
        return doSetFormat(format);
    }

    public void start() throws IOException {
    }

    public void stop() throws IOException {
    }
}
