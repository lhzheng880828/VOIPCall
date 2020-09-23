package com.lti.utils.synchronization;

public class MessageDrivenThread extends CloseableThread {
    private MessageDrivenThreadListener listener;
    private ProducerConsumerQueue q = new ProducerConsumerQueue();

    public MessageDrivenThread(ThreadGroup group, String threadName) {
        super(group, threadName);
    }

    public MessageDrivenThread(ThreadGroup group, String threadName, MessageDrivenThreadListener listener) {
        super(group, threadName);
        this.listener = listener;
    }

    /* access modifiers changed from: protected */
    public void doMessageReceived(Object o) {
        if (this.listener != null) {
            this.listener.onMessage(this, o);
        }
    }

    public void post(Object msg) throws InterruptedException {
        this.q.put(msg);
    }

    public void run() {
        while (!isClosing()) {
            try {
                doMessageReceived(this.q.get());
            } catch (InterruptedException e) {
            } finally {
                setClosed();
            }
        }
    }

    public void setListener(MessageDrivenThreadListener listener) {
        this.listener = listener;
    }
}
