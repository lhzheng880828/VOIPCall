package org.jitsi.impl.neomedia.jmfext.media.protocol;

import java.io.IOException;
import javax.media.Format;
import javax.media.control.FormatControl;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;

public abstract class AbstractPushBufferStream<T extends PushBufferDataSource> extends AbstractBufferStream<T> implements PushBufferStream {
    protected BufferTransferHandler transferHandler;

    public /* bridge */ /* synthetic */ void close() {
        super.close();
    }

    public /* bridge */ /* synthetic */ boolean endOfStream() {
        return super.endOfStream();
    }

    public /* bridge */ /* synthetic */ ContentDescriptor getContentDescriptor() {
        return super.getContentDescriptor();
    }

    public /* bridge */ /* synthetic */ long getContentLength() {
        return super.getContentLength();
    }

    public /* bridge */ /* synthetic */ Object[] getControls() {
        return super.getControls();
    }

    public /* bridge */ /* synthetic */ Format getFormat() {
        return super.getFormat();
    }

    public /* bridge */ /* synthetic */ void start() throws IOException {
        super.start();
    }

    public /* bridge */ /* synthetic */ void stop() throws IOException {
        super.stop();
    }

    protected AbstractPushBufferStream(T dataSource, FormatControl formatControl) {
        super(dataSource, formatControl);
    }

    public void setTransferHandler(BufferTransferHandler transferHandler) {
        this.transferHandler = transferHandler;
    }
}
