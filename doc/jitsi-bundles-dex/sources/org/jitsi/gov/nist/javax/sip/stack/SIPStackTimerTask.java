package org.jitsi.gov.nist.javax.sip.stack;

public abstract class SIPStackTimerTask {
    Object timerTask = null;

    public abstract void runTask();

    public void cleanUpBeforeCancel() {
    }

    public void setSipTimerTask(Object timer) {
        this.timerTask = timer;
    }

    public Object getSipTimerTask() {
        return this.timerTask;
    }
}
