package org.jitsi.impl.neomedia.codec.video;

import org.jitsi.impl.neomedia.codec.FFmpeg;

public class ByteBuffer {
    private int capacity;
    private int length;
    private long ptr;

    public ByteBuffer(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity");
        }
        this.ptr = FFmpeg.av_malloc(capacity);
        if (this.ptr == 0) {
            throw new OutOfMemoryError("av_malloc(" + capacity + ")");
        }
        this.capacity = capacity;
        this.length = 0;
    }

    public ByteBuffer(long ptr) {
        this.ptr = ptr;
        this.capacity = 0;
        this.length = 0;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            free();
        } finally {
            super.finalize();
        }
    }

    public synchronized void free() {
        if (!(this.capacity == 0 || this.ptr == 0)) {
            FFmpeg.av_free(this.ptr);
            this.capacity = 0;
            this.ptr = 0;
        }
    }

    public synchronized int getCapacity() {
        return this.capacity;
    }

    public int getLength() {
        return this.length;
    }

    public synchronized long getPtr() {
        return this.ptr;
    }

    public void setLength(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length");
        }
        this.length = length;
    }
}
