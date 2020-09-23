package net.sf.fmj.media;

import java.util.Vector;
import net.sf.fmj.media.util.MediaThread;

/* compiled from: BasicController */
abstract class StateTransitionWorkThread extends MediaThread {
    boolean allEventsArrived = false;
    BasicController controller;
    Vector eventQueue = new Vector();

    public abstract void aborted();

    public abstract void completed();

    public abstract void failed();

    public abstract boolean process();

    StateTransitionWorkThread() {
        useControlPriority();
    }

    public void run() {
        this.controller.resetInterrupt();
        try {
            boolean success = process();
            if (this.controller.isInterrupted()) {
                aborted();
            } else if (success) {
                completed();
            } else {
                failed();
            }
        } catch (OutOfMemoryError e) {
            System.err.println("Out of memory!");
        }
        this.controller.resetInterrupt();
    }
}
