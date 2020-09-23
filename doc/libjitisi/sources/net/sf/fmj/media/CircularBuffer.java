package net.sf.fmj.media;

import javax.media.Buffer;

public class CircularBuffer {
    private int availableFramesForReading;
    private int availableFramesForWriting;
    private Buffer[] buf;
    private int head;
    private int lockedFramesForReading;
    private int lockedFramesForWriting;
    private int size;
    private int tail;

    public CircularBuffer(int n) {
        this.size = n;
        this.buf = new Buffer[n];
        for (int i = 0; i < n; i++) {
            this.buf[i] = new Buffer();
        }
        reset();
    }

    public synchronized boolean canRead() {
        return this.availableFramesForReading > 0;
    }

    public synchronized boolean canWrite() {
        return this.availableFramesForWriting > 0;
    }

    public void error() {
        throw new RuntimeException("CircularQueue failure:\n head=" + this.head + "\n tail=" + this.tail + "\n canRead=" + this.availableFramesForReading + "\n canWrite=" + this.availableFramesForWriting + "\n lockedRead=" + this.lockedFramesForReading + "\n lockedWrite=" + this.lockedFramesForWriting);
    }

    public synchronized Buffer getEmptyBuffer() {
        Buffer buffer;
        if (this.availableFramesForWriting == 0) {
            error();
        }
        this.lockedFramesForWriting++;
        buffer = this.buf[this.tail];
        this.availableFramesForWriting--;
        this.tail++;
        if (this.tail >= this.size) {
            this.tail -= this.size;
        }
        return buffer;
    }

    public synchronized boolean lockedRead() {
        return this.lockedFramesForReading > 0;
    }

    public synchronized boolean lockedWrite() {
        return this.lockedFramesForWriting > 0;
    }

    public synchronized Buffer peek() {
        if (this.availableFramesForReading == 0) {
            error();
        }
        return this.buf[this.head];
    }

    public void print() {
        System.err.println("CircularQueue : head=" + this.head + " tail=" + this.tail + " canRead=" + this.availableFramesForReading + " canWrite=" + this.availableFramesForWriting + " lockedRead=" + this.lockedFramesForReading + " lockedWrite=" + this.lockedFramesForWriting);
    }

    public synchronized Buffer read() {
        Buffer buffer;
        if (this.availableFramesForReading == 0) {
            error();
        }
        buffer = this.buf[this.head];
        this.lockedFramesForReading++;
        this.availableFramesForReading--;
        this.head++;
        if (this.head >= this.size) {
            this.head -= this.size;
        }
        return buffer;
    }

    public synchronized void readReport() {
        if (this.lockedFramesForReading == 0) {
            error();
        }
        this.lockedFramesForReading--;
        this.availableFramesForWriting++;
    }

    public synchronized void reset() {
        this.availableFramesForReading = 0;
        this.availableFramesForWriting = this.size;
        this.lockedFramesForReading = 0;
        this.lockedFramesForWriting = 0;
        this.head = 0;
        this.tail = 0;
    }

    public synchronized void writeReport() {
        if (this.lockedFramesForWriting == 0) {
            error();
        }
        this.lockedFramesForWriting--;
        this.availableFramesForReading++;
    }
}
