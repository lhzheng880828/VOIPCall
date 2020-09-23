package com.lti.utils.synchronization;

import com.lti.utils.collections.Queue;

public class ProducerConsumerQueue<T> {
    private final Queue<T> q;
    private final int sizeLimit;

    public ProducerConsumerQueue() {
        this.q = new Queue();
        this.sizeLimit = -1;
    }

    public ProducerConsumerQueue(int sizeLimit) {
        this.q = new Queue();
        this.sizeLimit = sizeLimit;
    }

    public synchronized T get() throws InterruptedException {
        T o;
        while (this.q.isEmpty()) {
            wait();
        }
        o = this.q.dequeue();
        notifyAll();
        return o;
    }

    public synchronized T get(long timeout, T returnOnTimeout) throws InterruptedException {
        long t1;
        do {
            if (!this.q.isEmpty()) {
                T o = this.q.dequeue();
                notifyAll();
                returnOnTimeout = o;
                break;
            }
            t1 = System.currentTimeMillis();
            wait(timeout);
        } while (System.currentTimeMillis() - t1 <= timeout);
        return returnOnTimeout;
    }

    public synchronized boolean isEmpty() {
        return this.q.size() == 0;
    }

    public synchronized boolean isFull() {
        boolean z;
        z = this.sizeLimit > 0 && this.q.size() >= this.sizeLimit;
        return z;
    }

    public synchronized void put(T value) throws InterruptedException {
        while (this.sizeLimit > 0 && this.q.size() >= this.sizeLimit) {
            wait();
        }
        this.q.enqueue(value);
        notifyAll();
    }

    public synchronized boolean put(T value, long timeout) throws InterruptedException {
        boolean z;
        long t1;
        do {
            if (this.sizeLimit <= 0 || this.q.size() < this.sizeLimit) {
                this.q.enqueue(value);
                notifyAll();
                z = true;
                break;
            }
            t1 = System.currentTimeMillis();
            wait(timeout);
        } while (System.currentTimeMillis() - t1 <= timeout);
        z = false;
        return z;
    }

    public synchronized int size() {
        return this.q.size();
    }

    public int sizeLimit() {
        return this.sizeLimit;
    }

    public synchronized void waitUntilEmpty() throws InterruptedException {
        while (!this.q.isEmpty()) {
            wait();
        }
    }

    public synchronized void waitUntilNotEmpty() throws InterruptedException {
        while (this.q.isEmpty()) {
            wait();
        }
    }

    public synchronized boolean waitUntilNotEmpty(long timeout) throws InterruptedException {
        boolean z = false;
        synchronized (this) {
            long start = System.currentTimeMillis();
            while (this.q.isEmpty()) {
                long waitRemaining = timeout - (System.currentTimeMillis() - start);
                if (waitRemaining < 1) {
                    break;
                }
                wait(waitRemaining);
            }
            if (!this.q.isEmpty()) {
                z = true;
            }
        }
        return z;
    }
}
