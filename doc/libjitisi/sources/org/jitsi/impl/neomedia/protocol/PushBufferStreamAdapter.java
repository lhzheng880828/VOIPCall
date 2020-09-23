package org.jitsi.impl.neomedia.protocol;

import java.io.IOException;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.PushBufferStream;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.SourceTransferHandler;

public class PushBufferStreamAdapter extends BufferStreamAdapter<PushSourceStream> implements PushBufferStream {
    public PushBufferStreamAdapter(PushSourceStream stream, Format format) {
        super(stream, format);
    }

    public void read(Buffer buffer) throws IOException {
        read(buffer, new byte[((PushSourceStream) this.stream).getMinimumTransferSize()]);
    }

    /* access modifiers changed from: protected */
    public int read(byte[] buffer, int offset, int length) throws IOException {
        return ((PushSourceStream) this.stream).read(buffer, offset, length);
    }

    public void setTransferHandler(final BufferTransferHandler transferHandler) {
        ((PushSourceStream) this.stream).setTransferHandler(new SourceTransferHandler() {
            public void transferData(PushSourceStream stream) {
                transferHandler.transferData(PushBufferStreamAdapter.this);
            }
        });
    }
}
