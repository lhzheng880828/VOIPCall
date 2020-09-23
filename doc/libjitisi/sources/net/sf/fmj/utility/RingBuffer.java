package net.sf.fmj.utility;

public class RingBuffer {
    private Object[] buckets;
    private int overrunCounter;
    private int readIndex;
    private int writeIndex;

    private RingBuffer() {
    }

    public RingBuffer(int maxItems) {
        resize(maxItems);
    }

    public synchronized Object get() throws InterruptedException {
        Object item;
        if (isEmpty()) {
            wait();
        }
        Object[] objArr = this.buckets;
        int i = this.readIndex;
        this.readIndex = i + 1;
        item = objArr[i];
        if (this.readIndex >= this.buckets.length) {
            this.readIndex = 0;
        }
        return item;
    }

    public synchronized int getOverrunCounter() {
        return this.overrunCounter;
    }

    public synchronized boolean isEmpty() {
        boolean z;
        if (this.readIndex == this.writeIndex) {
            z = true;
        } else {
            z = false;
        }
        return z;
    }

    public synchronized boolean isFull() {
        boolean z;
        int index = this.writeIndex + 1;
        if (index >= this.buckets.length) {
            index = 0;
        }
        if (index == this.readIndex) {
            z = true;
        } else {
            z = false;
        }
        return z;
    }

    public synchronized Object peek() {
        Object obj;
        if (isEmpty()) {
            obj = null;
        } else {
            obj = this.buckets[this.readIndex];
        }
        return obj;
    }

    public synchronized boolean put(Object item) {
        boolean fBufferOverrun;
        fBufferOverrun = false;
        if (isFull()) {
            try {
                get();
                fBufferOverrun = true;
            } catch (Exception e) {
            }
            this.overrunCounter++;
        }
        Object[] objArr = this.buckets;
        int i = this.writeIndex;
        this.writeIndex = i + 1;
        objArr[i] = item;
        if (this.writeIndex >= this.buckets.length) {
            this.writeIndex = 0;
        }
        notifyAll();
        return fBufferOverrun;
    }

    public synchronized void resize(int maxItems) {
        if (maxItems < 1) {
            maxItems = 1;
        }
        this.buckets = new Object[(maxItems + 1)];
        this.overrunCounter = 0;
        this.writeIndex = 0;
        this.readIndex = 0;
    }

    public synchronized int size() {
        return this.buckets.length - 1;
    }
}
