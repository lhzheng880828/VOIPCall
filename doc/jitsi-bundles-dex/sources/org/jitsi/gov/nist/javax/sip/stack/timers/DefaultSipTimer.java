package org.jitsi.gov.nist.javax.sip.stack.timers;

import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.javax.sip.SipStackImpl;
import org.jitsi.gov.nist.javax.sip.stack.SIPStackTimerTask;

public class DefaultSipTimer extends Timer implements SipTimer {
    private static StackLogger logger = CommonLogger.getLogger(DefaultSipTimer.class);
    protected SipStackImpl sipStackImpl;
    protected AtomicBoolean started = new AtomicBoolean(false);

    private class DefaultTimerTask extends TimerTask {
        private SIPStackTimerTask task;

        public DefaultTimerTask(SIPStackTimerTask task) {
            this.task = task;
            task.setSipTimerTask(this);
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

        public boolean cancel() {
            if (this.task != null) {
                this.task.cleanUpBeforeCancel();
                this.task = null;
            }
            return super.cancel();
        }
    }

    public boolean schedule(SIPStackTimerTask task, long delay) {
        if (this.started.get()) {
            super.schedule(new DefaultTimerTask(task), delay);
            return true;
        }
        throw new IllegalStateException("The SIP Stack Timer has been stopped, no new tasks can be scheduled !");
    }

    public boolean scheduleWithFixedDelay(SIPStackTimerTask task, long delay, long period) {
        if (this.started.get()) {
            super.schedule(new DefaultTimerTask(task), delay, period);
            return true;
        }
        throw new IllegalStateException("The SIP Stack Timer has been stopped, no new tasks can be scheduled !");
    }

    public boolean cancel(SIPStackTimerTask task) {
        return ((TimerTask) task.getSipTimerTask()).cancel();
    }

    public void start(SipStackImpl sipStack, Properties configurationProperties) {
        this.sipStackImpl = sipStack;
        this.started.set(true);
        if (logger.isLoggingEnabled(16)) {
            logger.logInfo("the sip stack timer " + getClass().getName() + " has been started");
        }
    }

    public void stop() {
        this.started.set(false);
        cancel();
        logger.logStackTrace(32);
        if (logger.isLoggingEnabled(16)) {
            logger.logInfo("the sip stack timer " + getClass().getName() + " has been stopped");
        }
    }

    public boolean isStarted() {
        return this.started.get();
    }
}
