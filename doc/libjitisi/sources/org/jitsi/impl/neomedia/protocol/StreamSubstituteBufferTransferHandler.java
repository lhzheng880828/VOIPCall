package org.jitsi.impl.neomedia.protocol;

import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.PushBufferStream;

public class StreamSubstituteBufferTransferHandler implements BufferTransferHandler {
    private final PushBufferStream stream;
    private final PushBufferStream substitute;
    private final BufferTransferHandler transferHandler;

    public StreamSubstituteBufferTransferHandler(BufferTransferHandler transferHandler, PushBufferStream stream, PushBufferStream substitute) {
        this.transferHandler = transferHandler;
        this.stream = stream;
        this.substitute = substitute;
    }

    public void transferData(PushBufferStream stream) {
        BufferTransferHandler bufferTransferHandler = this.transferHandler;
        if (stream == this.stream) {
            stream = this.substitute;
        }
        bufferTransferHandler.transferData(stream);
    }
}
