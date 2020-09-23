package net.sf.fmj.media.rtp.util;

import java.io.IOException;
import java.io.InterruptedIOException;

public class PacketForwarder implements Runnable {
    boolean closed = false;
    PacketConsumer consumer = null;
    public IOException exception = null;
    private boolean paused;
    PacketSource source = null;
    RTPMediaThread thread;

    public PacketForwarder(PacketSource s, PacketConsumer c) {
        this.source = s;
        this.consumer = c;
        this.closed = false;
        this.exception = null;
    }

    private boolean checkForClose() {
        if (!this.closed || this.thread == null) {
            return false;
        }
        if (this.source != null) {
            this.source.closeSource();
        }
        return true;
    }

    public void close() {
        this.closed = true;
        if (this.consumer != null) {
            this.consumer.closeConsumer();
        }
    }

    public PacketConsumer getConsumer() {
        return this.consumer;
    }

    public String getId() {
        if (this.thread != null) {
            return this.thread.getName();
        }
        System.err.println("the packetforwarders thread is null");
        return null;
    }

    public PacketSource getSource() {
        return this.source;
    }

    public void run() {
        if (!this.closed && this.exception == null) {
            do {
                try {
                    Packet p = this.source.receiveFrom();
                    if (checkForClose()) {
                        this.consumer.closeConsumer();
                        return;
                    }
                    if (p != null) {
                        this.consumer.sendTo(p);
                    }
                    try {
                    } catch (IOException ioe) {
                        if (!checkForClose()) {
                            this.exception = ioe;
                        }
                        this.consumer.closeConsumer();
                        return;
                    } catch (Throwable th) {
                        this.consumer.closeConsumer();
                        throw th;
                    }
                } catch (InterruptedIOException e) {
                }
            } while (!checkForClose());
            this.consumer.closeConsumer();
        } else if (this.source != null) {
            this.source.closeSource();
        }
    }

    public void setVideoPriority() {
        this.thread.useVideoNetworkPriority();
    }

    public void startPF() {
        startPF(null);
    }

    public void startPF(String threadname) {
        if (this.thread != null) {
            throw new IllegalArgumentException("Called start more than once");
        }
        if (threadname == null) {
            threadname = "RTPMediaThread";
        }
        this.thread = new RTPMediaThread(this, threadname);
        this.thread.useNetworkPriority();
        this.thread.setDaemon(true);
        this.thread.start();
    }
}
