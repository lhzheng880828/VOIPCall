package net.sf.fmj.media.util;

public abstract class LoopThread extends MediaThread {
    protected boolean killed = false;
    protected boolean paused = false;
    protected boolean started = false;
    private boolean waitingAtPaused = false;

    public abstract boolean process();

    public LoopThread() {
        setName("Loop thread");
    }

    public synchronized void blockingPause() {
        if (!(this.waitingAtPaused || this.killed)) {
            this.paused = true;
            waitForCompleteStop();
        }
    }

    public boolean isPaused() {
        return this.paused;
    }

    public synchronized void kill() {
        this.killed = true;
        notifyAll();
    }

    public synchronized void pause() {
        this.paused = true;
    }

    public void run() {
        super.run();
        while (waitHereIfPaused()) {
            if (!process()) {
                return;
            }
        }
    }

    public synchronized void start() {
        if (!this.started) {
            super.start();
            this.started = true;
        }
        this.paused = false;
        notifyAll();
    }

    public synchronized void waitForCompleteStop() {
        while (!this.killed && !this.waitingAtPaused && this.paused) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public synchronized void waitForCompleteStop(int millis) {
        try {
            if (!(this.killed || this.waitingAtPaused || !this.paused)) {
                wait((long) millis);
            }
        } catch (InterruptedException e) {
        }
    }

    public synchronized boolean waitHereIfPaused() {
        boolean z = false;
        synchronized (this) {
            if (!this.killed) {
                this.waitingAtPaused = true;
                if (this.paused) {
                    notifyAll();
                }
                while (!this.killed && this.paused) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        System.err.println("Timer: timeLoop() wait interrupted " + e);
                    }
                }
                this.waitingAtPaused = false;
                if (!this.killed) {
                    z = true;
                }
            }
        }
        return z;
    }
}
