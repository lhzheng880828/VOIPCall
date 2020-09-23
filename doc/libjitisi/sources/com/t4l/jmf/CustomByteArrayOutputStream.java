package com.t4l.jmf;

import java.io.IOException;
import java.io.OutputStream;

/* compiled from: JPEGEncoder */
class CustomByteArrayOutputStream extends OutputStream {
    int ctr = 0;
    byte[] data;

    public CustomByteArrayOutputStream(byte[] b) {
        this.data = b;
    }

    public int getBytesWritten() {
        return this.ctr;
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int offset, int len) throws IOException {
        System.arraycopy(this.data, this.ctr, b, offset, len);
        this.ctr += len;
    }

    public void write(int b) throws IOException {
        this.data[this.ctr] = (byte) b;
        this.ctr++;
    }
}
