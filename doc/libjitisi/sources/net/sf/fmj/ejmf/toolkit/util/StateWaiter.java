package net.sf.fmj.ejmf.toolkit.util;

import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Processor;
import javax.media.StopEvent;
import javax.media.Time;
import javax.media.TransitionEvent;

public class StateWaiter implements ControllerListener {
    private Controller controller;
    private boolean listening = false;
    private int state;
    private boolean stateReached = false;

    public StateWaiter(Controller controller) {
        this.controller = controller;
    }

    private void addAsListener() {
        if (!this.listening) {
            this.controller.addControllerListener(this);
            this.listening = true;
        }
    }

    public boolean blockingConfigure() {
        setState(Processor.Configured);
        ((Processor) this.controller).configure();
        return waitForState();
    }

    public boolean blockingPrefetch() {
        setState(500);
        this.controller.prefetch();
        return waitForState();
    }

    public boolean blockingRealize() {
        setState(Controller.Realized);
        this.controller.realize();
        return waitForState();
    }

    public boolean blockingStart() {
        setState(Controller.Started);
        this.controller.start();
        return waitForState();
    }

    public boolean blockingSyncStart(Time t) {
        setState(Controller.Started);
        this.controller.syncStart(t);
        return waitForState();
    }

    public boolean blockingWait(int state) {
        setState(state);
        return waitForState();
    }

    public synchronized void controllerUpdate(ControllerEvent event) {
        if (event.getSourceController() == this.controller) {
            if (event instanceof TransitionEvent) {
                this.stateReached = ((TransitionEvent) event).getCurrentState() >= this.state;
            }
            if ((event instanceof StopEvent) || (event instanceof ControllerClosedEvent) || this.stateReached) {
                removeAsListener();
                notifyAll();
            }
        }
    }

    private void removeAsListener() {
        this.controller.removeControllerListener(this);
        this.listening = false;
    }

    private void setState(int state) {
        this.state = state;
        this.stateReached = false;
        addAsListener();
    }

    private synchronized boolean waitForState() {
        while (this.listening) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        return this.stateReached;
    }
}
