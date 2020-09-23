package com.lti.utils.synchronization;

public abstract class CloseableThread extends Thread {
    private final SynchronizedBoolean closed = new SynchronizedBoolean(false);
    protected final SynchronizedBoolean closing = new SynchronizedBoolean(false);

    public CloseableThread(String threadName) {
        super(threadName);
    }

    @Deprecated
    public CloseableThread(ThreadGroup group, String threadName) {
        super(group, threadName);
    }

    public void close() {
        this.closing.setValue(true);
        interrupt();
    }

    public boolean isClosed() {
        return this.closed.getValue();
    }

    /* access modifiers changed from: protected */
    public boolean isClosing() {
        return this.closing.getValue();
    }

    /* access modifiers changed from: protected */
    public void setClosed() {
        this.closed.setValue(true);
    }

    /* access modifiers changed from: protected */
    public void setClosing() {
        this.closing.setValue(true);
    }

    public void waitUntilClosed() throws InterruptedException {
        this.closed.waitUntil(true);
    }
}
