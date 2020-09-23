package org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi;

import java.io.IOException;

public interface IMediaBuffer {
    int GetLength() throws IOException;

    int GetMaxLength() throws IOException;

    int Release();

    void SetLength(int i) throws IOException;

    int pop(byte[] bArr, int i, int i2) throws IOException;

    int push(byte[] bArr, int i, int i2) throws IOException;
}
