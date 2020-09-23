package com.lti.utils.synchronization;

public class SynchronizedObjectHolder<T> {
    private T object;

    public SynchronizedObjectHolder(T value) {
        setObject(value);
    }

    public synchronized T getObject() {
        return this.object;
    }

    public synchronized void setObject(T value) {
        this.object = value;
        notifyAll();
    }

    public synchronized void waitUntilNotNull() throws InterruptedException {
        while (this.object == null) {
            wait();
        }
    }

    public synchronized boolean waitUntilNotNull(int timeout) throws InterruptedException {
        boolean z;
        long start = System.currentTimeMillis();
        while (this.object == null) {
            long wait = ((long) timeout) - (System.currentTimeMillis() - start);
            if (wait <= 0) {
                z = false;
                break;
            }
            wait(wait);
        }
        z = true;
        return z;
    }
}
