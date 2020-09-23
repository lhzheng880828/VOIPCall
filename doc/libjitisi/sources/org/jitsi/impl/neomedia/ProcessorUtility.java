package org.jitsi.impl.neomedia;

import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Processor;
import org.jitsi.util.Logger;

public class ProcessorUtility implements ControllerListener {
    private static final Logger logger = Logger.getLogger(ProcessorUtility.class);
    private boolean failed = false;
    private final Object stateLock = new Object();

    private Object getStateLock() {
        return this.stateLock;
    }

    private void setFailed(boolean failed) {
        this.failed = failed;
    }

    public void controllerUpdate(ControllerEvent ce) {
        if (ce instanceof ControllerClosedEvent) {
            if (ce instanceof ControllerErrorEvent) {
                logger.warn("ControllerErrorEvent: " + ce);
            } else if (logger.isDebugEnabled()) {
                logger.debug("ControllerClosedEvent: " + ce);
            }
            setFailed(true);
        }
        Object stateLock = getStateLock();
        synchronized (stateLock) {
            stateLock.notifyAll();
        }
    }

    public synchronized boolean waitForState(Processor processor, int state) {
        boolean z = false;
        synchronized (this) {
            processor.addControllerListener(this);
            setFailed(false);
            if (state == Processor.Configured) {
                processor.configure();
            } else if (state == Controller.Realized) {
                processor.realize();
            }
            boolean interrupted = false;
            while (processor.getState() < state && !this.failed) {
                Object stateLock = getStateLock();
                synchronized (stateLock) {
                    try {
                        stateLock.wait();
                    } catch (InterruptedException ie) {
                        logger.warn("Interrupted while waiting on Processor " + processor + " for state " + state, ie);
                        interrupted = true;
                    }
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
            processor.removeControllerListener(this);
            if (!this.failed) {
                z = true;
            }
        }
        return z;
    }
}
