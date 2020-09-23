package org.jitsi.impl.neomedia.protocol;

import java.io.IOException;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.protocol.PullBufferStream;
import javax.media.protocol.PullSourceStream;

public class PullBufferStreamAdapter extends BufferStreamAdapter<PullSourceStream> implements PullBufferStream {
    public PullBufferStreamAdapter(PullSourceStream stream, Format format) {
        super(stream, format);
    }

    private static int getFrameSizeInBytes(Format format) {
        AudioFormat audioFormat = (AudioFormat) format;
        int frameSizeInBits = audioFormat.getFrameSizeInBits();
        if (frameSizeInBits <= 0) {
            return (audioFormat.getSampleSizeInBits() / 8) * audioFormat.getChannels();
        }
        return frameSizeInBits <= 8 ? 1 : frameSizeInBits / 8;
    }

    public void read(Buffer buffer) throws IOException {
        Object data = buffer.getData();
        byte[] bytes = null;
        if (data != null) {
            if (data instanceof byte[]) {
                bytes = (byte[]) data;
            } else if (data instanceof short[]) {
                bytes = new byte[(((short[]) data).length * 2)];
            } else if (data instanceof int[]) {
                bytes = new byte[(((int[]) data).length * 4)];
            }
        }
        if (bytes == null) {
            int frameSizeInBytes = getFrameSizeInBytes(getFormat());
            if (frameSizeInBytes <= 0) {
                frameSizeInBytes = 4;
            }
            bytes = new byte[(frameSizeInBytes * 1024)];
        }
        read(buffer, bytes);
    }

    /* access modifiers changed from: protected */
    public int read(byte[] buffer, int offset, int length) throws IOException {
        return ((PullSourceStream) this.stream).read(buffer, offset, length);
    }

    public boolean willReadBlock() {
        return ((PullSourceStream) this.stream).willReadBlock();
    }
}
