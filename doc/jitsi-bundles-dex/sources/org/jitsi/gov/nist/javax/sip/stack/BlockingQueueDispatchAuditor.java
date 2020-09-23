package org.jitsi.gov.nist.javax.sip.stack;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.StackLogger;

public class BlockingQueueDispatchAuditor extends TimerTask {
    private static StackLogger logger = CommonLogger.getLogger(BlockingQueueDispatchAuditor.class);
    private Queue<? extends Runnable> queue;
    private boolean started = false;
    private int timeout = 8000;
    private Timer timer = new Timer();
    private long totalReject = 0;

    public BlockingQueueDispatchAuditor(Queue<? extends Runnable> queue) {
        this.queue = queue;
    }

    public void start(int interval) {
        if (this.started) {
            stop();
        }
        this.started = true;
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(this, (long) interval, (long) interval);
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void stop() {
        try {
            this.timer.cancel();
            this.timer = null;
        } catch (Exception e) {
        } catch (Throwable th) {
            this.started = false;
        }
        this.started = false;
    }

    public void run() {
        try {
            QueuedMessageDispatchBase runnable = (QueuedMessageDispatchBase) this.queue.peek();
            int removed = 0;
            while (runnable != null) {
                if (System.currentTimeMillis() - runnable.getReceptionTime() > ((long) this.timeout)) {
                    this.queue.poll();
                    runnable = (QueuedMessageDispatchBase) this.queue.peek();
                    removed++;
                } else {
                    runnable = null;
                }
            }
            if (removed > 0) {
                this.totalReject += (long) removed;
                if (logger != null && logger.isLoggingEnabled(8)) {
                    logger.logWarning("Removed stuck messages=" + removed + " total rejected=" + this.totalReject + " stil in queue=" + this.queue.size());
                }
            }
        } catch (Exception e) {
            if (logger != null && logger.isLoggingEnabled(8)) {
                logger.logWarning("Problem reaping old requests. This is not a fatal error." + e);
            }
        }
    }
}
