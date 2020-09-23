package org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi;

import java.io.IOException;

public class PtrMediaBuffer implements IMediaBuffer {
    final long ptr;

    public PtrMediaBuffer(long ptr) {
        if (ptr == 0) {
            throw new IllegalArgumentException("ptr");
        }
        this.ptr = ptr;
    }

    public int GetLength() throws IOException {
        try {
            return VoiceCaptureDSP.IMediaBuffer_GetLength(this.ptr);
        } catch (HResultException hre) {
            throw new IOException(hre);
        }
    }

    public int GetMaxLength() throws IOException {
        try {
            return VoiceCaptureDSP.IMediaBuffer_GetMaxLength(this.ptr);
        } catch (HResultException hre) {
            throw new IOException(hre);
        }
    }

    public int pop(byte[] buffer, int offset, int length) throws IOException {
        try {
            return VoiceCaptureDSP.MediaBuffer_pop(this.ptr, buffer, offset, length);
        } catch (HResultException hre) {
            throw new IOException(hre);
        }
    }

    public int push(byte[] buffer, int offset, int length) throws IOException {
        try {
            return VoiceCaptureDSP.MediaBuffer_push(this.ptr, buffer, offset, length);
        } catch (HResultException hre) {
            throw new IOException(hre);
        }
    }

    public int Release() {
        return VoiceCaptureDSP.IMediaBuffer_Release(this.ptr);
    }

    public void SetLength(int length) throws IOException {
        try {
            VoiceCaptureDSP.IMediaBuffer_SetLength(this.ptr, length);
        } catch (HResultException hre) {
            throw new IOException(hre);
        }
    }
}
