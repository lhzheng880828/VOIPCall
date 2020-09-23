package org.jitsi.gov.nist.javax.sip.stack.timers;

import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.javax.sip.SipStackImpl;
import org.jitsi.gov.nist.javax.sip.stack.SIPStackTimerTask;

public class ScheduledExecutorSipTimer implements SipTimer {
    private static StackLogger logger = CommonLogger.getLogger(ScheduledExecutorSipTimer.class);
    private volatile int numCancelled = 0;
    protected SipStackImpl sipStackImpl;
    ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(1);

    private class ScheduledSipTimerTask implements Runnable {
        private SIPStackTimerTask task;

        public ScheduledSipTimerTask(SIPStackTimerTask task) {
            this.task = task;
        }

        public void run() {
            try {
                if (this.task != null) {
                    this.task.runTask();
                }
            } catch (Throwable e) {
                System.out.println("SIP stack timer task failed due to exception:");
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        this.threadPoolExecutor.shutdown();
        logger.logStackTrace(32);
        if (logger.isLoggingEnabled(16)) {
            logger.logInfo("the sip stack timer " + getClass().getName() + " has been stopped");
        }
    }

    public boolean schedule(SIPStackTimerTask task, long delay) {
        if (this.threadPoolExecutor.isShutdown()) {
            throw new IllegalStateException("The SIP Stack Timer has been stopped, no new tasks can be scheduled !");
        }
        task.setSipTimerTask(this.threadPoolExecutor.schedule(new ScheduledSipTimerTask(task), delay, TimeUnit.MILLISECONDS));
        return true;
    }

    public boolean scheduleWithFixedDelay(SIPStackTimerTask task, long delay, long period) {
        if (this.threadPoolExecutor.isShutdown()) {
            throw new IllegalStateException("The SIP Stack Timer has been stopped, no new tasks can be scheduled !");
        }
        task.setSipTimerTask(this.threadPoolExecutor.scheduleWithFixedDelay(new ScheduledSipTimerTask(task), delay, period, TimeUnit.MILLISECONDS));
        return true;
    }

    public void start(SipStackImpl sipStack, Properties configurationProperties) {
        this.sipStackImpl = sipStack;
        this.threadPoolExecutor.prestartAllCoreThreads();
        if (logger.isLoggingEnabled(16)) {
            logger.logInfo("the sip stack timer " + getClass().getName() + " has been started");
        }
    }

    public boolean cancel(SIPStackTimerTask task) {
        boolean cancelled = false;
        ScheduledFuture<?> sipTimerTask = (ScheduledFuture) task.getSipTimerTask();
        if (sipTimerTask != null) {
            task.cleanUpBeforeCancel();
            task.setSipTimerTask(null);
            this.threadPoolExecutor.remove((Runnable) sipTimerTask);
            cancelled = sipTimerTask.cancel(false);
        }
        this.numCancelled++;
        if (this.numCancelled % 50 == 0) {
            this.threadPoolExecutor.purge();
        }
        return cancelled;
    }

    public boolean isStarted() {
        return this.threadPoolExecutor.isTerminated();
    }
}
