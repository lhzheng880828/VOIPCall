package com.lti.utils.synchronization;

public class SynchronizedBoolean {
    private boolean b = false;

    public SynchronizedBoolean(boolean initValue) {
        this.b = initValue;
    }

    public synchronized boolean getAndSet(boolean oldValue, boolean newValue) {
        boolean z;
        if (this.b != oldValue) {
            z = false;
        } else {
            setValue(newValue);
            z = true;
        }
        return z;
    }

    public synchronized boolean getValue() {
        return this.b;
    }

    public synchronized void setValue(boolean newValue) {
        if (this.b != newValue) {
            this.b = newValue;
            notifyAll();
        }
    }

    public synchronized void waitUntil(boolean value) throws InterruptedException {
        while (this.b != value) {
            wait();
        }
    }

    public synchronized boolean waitUntil(boolean value, int timeout) throws InterruptedException {
        boolean z;
        long start = System.currentTimeMillis();
        while (this.b != value) {
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
