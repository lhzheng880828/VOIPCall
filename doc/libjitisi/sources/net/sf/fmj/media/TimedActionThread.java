package net.sf.fmj.media;

import net.sf.fmj.ejmf.toolkit.util.TimeSource;
import net.sf.fmj.media.util.MediaThread;

/* compiled from: BasicController */
abstract class TimedActionThread extends MediaThread {
    protected boolean aborted = false;
    protected BasicController controller;
    protected long wakeupTime;

    public abstract void action();

    public abstract long getTime();

    TimedActionThread(BasicController mc, long nanoseconds) {
        this.controller = mc;
        useControlPriority();
        this.wakeupTime = nanoseconds;
    }

    public synchronized void abort() {
        this.aborted = true;
        notify();
    }

    public void run() {
        while (true) {
            long now = getTime();
            if (now >= this.wakeupTime || this.aborted) {
                break;
            }
            long sleepTime = this.wakeupTime - now;
            if (sleepTime > 1000000000) {
                sleepTime = 1000000000;
            }
            synchronized (this) {
                try {
                    wait(sleepTime / TimeSource.MICROS_PER_SEC);
                } catch (InterruptedException e) {
                }
            }
        }
        if (!this.aborted) {
            action();
            return;
        }
        return;
    }
}
