package net.java.sip.communicator.impl.protocol.sip;

import java.util.Timer;
import java.util.TimerTask;

public class TimerScheduler {
    private Timer timer;

    public synchronized void cancel() {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
    }

    private synchronized Timer getTimer() {
        if (this.timer == null) {
            this.timer = new Timer(true);
        }
        return this.timer;
    }

    public synchronized void schedule(TimerTask task, long delay) {
        getTimer().schedule(task, delay);
    }

    public synchronized void schedule(TimerTask task, long delay, long period) {
        getTimer().schedule(task, delay, period);
    }
}
