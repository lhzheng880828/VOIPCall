package net.sf.fmj.ejmf.toolkit.media;

import java.io.IOException;
import java.util.Vector;
import javax.media.Clock;
import javax.media.ClockStartedError;
import javax.media.ClockStoppedException;
import javax.media.Controller;
import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DurationUpdateEvent;
import javax.media.GainControl;
import javax.media.IncompatibleSourceException;
import javax.media.IncompatibleTimeBaseException;
import javax.media.NotRealizedError;
import javax.media.Player;
import javax.media.ResourceUnavailableEvent;
import javax.media.Time;
import javax.media.TransitionEvent;
import javax.media.protocol.DataSource;
import net.sf.fmj.ejmf.toolkit.media.event.ManagedControllerErrorEvent;
import org.jitsi.android.util.java.awt.Component;

public abstract class AbstractPlayer extends AbstractController implements Player, ControllerListener {
    private Component controlPanelComponent;
    private ControllerErrorEvent controllerError;
    private Vector controllers = new Vector();
    private Time duration = new Time(0);
    private GainControl gainControl;
    private DataSource source;
    private Component visualComponent;

    public abstract void doPlayerClose();

    public abstract boolean doPlayerDeallocate();

    public abstract boolean doPlayerPrefetch();

    public abstract boolean doPlayerRealize();

    public abstract void doPlayerSetMediaTime(Time time);

    public abstract float doPlayerSetRate(float f);

    public abstract boolean doPlayerStop();

    public abstract boolean doPlayerSyncStart(Time time);

    public abstract Time getPlayerDuration();

    public abstract Time getPlayerStartLatency();

    public synchronized void addController(Controller newController) throws IncompatibleTimeBaseException {
        if (!(this.controllers.contains(newController) || this == newController)) {
            int currentState = getState();
            if (currentState == 100 || currentState == 200) {
                throw new NotRealizedError("Cannot add Controller to an Unrealized Player");
            } else if (currentState == Controller.Started) {
                throw new ClockStartedError("Cannot add Controller to a Started Player");
            } else {
                int controllerState = newController.getState();
                if (controllerState == 100 || controllerState == 200) {
                    throw new NotRealizedError("Cannot add Unrealized Controller to a Player");
                } else if (controllerState == Controller.Started) {
                    throw new ClockStartedError("Cannot add Started Controller to a Player");
                } else {
                    newController.setTimeBase(getTimeBase());
                    stop();
                    newController.stop();
                    currentState = getState();
                    if (newController.getState() < 500 && currentState > Controller.Realized) {
                        deallocate();
                    }
                    newController.setMediaTime(getMediaTime());
                    newController.setStopTime(Clock.RESET);
                    float rate = getRate();
                    if (rate != newController.setRate(rate)) {
                        newController.setRate(1.0f);
                        setRate(1.0f);
                    }
                    this.controllers.addElement(newController);
                    newController.addControllerListener(this);
                    updateDuration();
                }
            }
        }
    }

    private boolean areControllersStopped() {
        boolean z;
        synchronized (this.controllers) {
            int n = this.controllers.size();
            for (int i = 0; i < n; i++) {
                if (((Controller) this.controllers.elementAt(i)).getState() == Controller.Started) {
                    z = false;
                    break;
                }
            }
            z = true;
        }
        return z;
    }

    public final void controllerUpdate(ControllerEvent e) {
        synchronized (this.controllers) {
            if (e instanceof TransitionEvent) {
                this.controllers.notifyAll();
            } else if (e instanceof ControllerErrorEvent) {
                setControllerError((ControllerErrorEvent) e);
                this.controllers.notifyAll();
            }
        }
    }

    public final synchronized void doClose() {
        Vector controllers = getControllers();
        int n = controllers.size();
        for (int i = 0; i < n; i++) {
            ((Controller) controllers.elementAt(i)).close();
        }
        try {
            this.source.stop();
            this.source.disconnect();
        } catch (IOException e) {
        }
        doPlayerClose();
        this.source = null;
        this.gainControl = null;
        this.controllerError = null;
    }

    public final boolean doDeallocate() {
        int i;
        resetControllerError();
        int size = this.controllers.size();
        Thread[] threads = new Thread[size];
        for (i = 0; i < size; i++) {
            final Controller c = (Controller) this.controllers.elementAt(i);
            threads[i] = new Thread("Player Deallocate Thread") {
                public void run() {
                    c.deallocate();
                }
            };
            threads[i].start();
        }
        if (!doPlayerDeallocate()) {
            return false;
        }
        for (i = 0; i < size; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
            }
        }
        if (this.controllerError == null) {
            return true;
        }
        postManagedControllerErrorEvent();
        return false;
    }

    public final boolean doPrefetch() {
        resetControllerError();
        for (int i = 0; i < this.controllers.size(); i++) {
            ((Controller) this.controllers.elementAt(i)).prefetch();
        }
        if (!doPlayerPrefetch()) {
            return false;
        }
        synchronized (this.controllers) {
            while (this.controllerError == null && !isStateReached(500)) {
                try {
                    this.controllers.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        if (this.controllerError != null) {
            postManagedControllerErrorEvent();
            return false;
        }
        updateDuration();
        return true;
    }

    public final boolean doRealize() {
        try {
            this.source.start();
            if (!doPlayerRealize()) {
                return false;
            }
            updateDuration();
            return true;
        } catch (IOException e) {
            postEvent(new ResourceUnavailableEvent(this, "Could not start DataSource"));
            return false;
        }
    }

    public final synchronized void doSetMediaTime(Time t) {
        int n = this.controllers.size();
        for (int i = 0; i < n; i++) {
            ((Controller) this.controllers.elementAt(i)).setMediaTime(t);
        }
        doPlayerSetMediaTime(t);
    }

    public final synchronized float doSetRate(float rate) {
        float f = 1.0f;
        synchronized (this) {
            float actual;
            int n = this.controllers.size();
            for (int i = 0; i < n; i++) {
                actual = ((Controller) this.controllers.elementAt(i)).setRate(rate);
                if (rate != 1.0f && actual != rate) {
                    doSetRate(1.0f);
                    break;
                }
            }
            actual = doPlayerSetRate(rate);
            if (this.controllers.isEmpty() || rate == 1.0f || actual == rate) {
                f = actual;
            } else {
                doSetRate(1.0f);
                float f2 = actual;
            }
        }
        return f;
    }

    public final boolean doStop() {
        int i;
        resetControllerError();
        int size = this.controllers.size();
        Thread[] threads = new Thread[size];
        for (i = 0; i < size; i++) {
            final Controller c = (Controller) this.controllers.elementAt(i);
            threads[i] = new Thread("Player Stop Thread") {
                public void run() {
                    c.stop();
                }
            };
            threads[i].start();
        }
        if (!doPlayerStop()) {
            return false;
        }
        for (i = 0; i < size; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
            }
        }
        if (this.controllerError == null) {
            return true;
        }
        postManagedControllerErrorEvent();
        return false;
    }

    public final boolean doSyncStart(Time t) {
        resetControllerError();
        for (int i = 0; i < this.controllers.size(); i++) {
            ((Controller) this.controllers.elementAt(i)).syncStart(t);
        }
        if (!doPlayerSyncStart(t)) {
            return false;
        }
        synchronized (this.controllers) {
            while (this.controllerError == null && !isStateReached(Controller.Started)) {
                try {
                    this.controllers.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        if (this.controllerError == null) {
            return true;
        }
        postManagedControllerErrorEvent();
        return false;
    }

    /* access modifiers changed from: protected */
    public void endOfMedia() throws ClockStoppedException {
        synchronized (this.controllers) {
            while (!areControllersStopped()) {
                try {
                    this.controllers.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        super.endOfMedia();
    }

    private ControllerErrorEvent getControllerError() {
        return this.controllerError;
    }

    /* access modifiers changed from: protected */
    public Vector getControllers() {
        return (Vector) this.controllers.clone();
    }

    public Component getControlPanelComponent() {
        int currentState = getState();
        if (currentState != 100 && currentState != 200) {
            return this.controlPanelComponent;
        }
        throw new NotRealizedError("Cannot get control panel Component on an Unrealized Player");
    }

    public final synchronized Time getDuration() {
        if (this.duration == null) {
            updateDuration();
        }
        return this.duration;
    }

    public GainControl getGainControl() {
        int currentState = getState();
        if (currentState != 100 && currentState != 200) {
            return this.gainControl;
        }
        throw new NotRealizedError("Cannot get gain control on an Unrealized Player");
    }

    public DataSource getSource() {
        return this.source;
    }

    public synchronized Time getStartLatency() {
        Time latency;
        int currentState = getState();
        if (currentState == 100 || currentState == 200) {
            throw new NotRealizedError("Cannot get start latency from an Unrealized Controller");
        }
        latency = getPlayerStartLatency();
        int n = this.controllers.size();
        for (int i = 0; i < n; i++) {
            Time l = ((Controller) this.controllers.elementAt(i)).getStartLatency();
            if (l != LATENCY_UNKNOWN && (latency == LATENCY_UNKNOWN || l.getNanoseconds() > latency.getNanoseconds())) {
                latency = l;
            }
        }
        return latency;
    }

    public Component getVisualComponent() {
        int currentState = getState();
        if (currentState != 100 && currentState != 200) {
            return this.visualComponent;
        }
        throw new NotRealizedError("Cannot get visual Component on an Unrealized Player");
    }

    private boolean isStateReached(int state) {
        boolean z;
        synchronized (this.controllers) {
            int n = this.controllers.size();
            for (int i = 0; i < n; i++) {
                if (((Controller) this.controllers.elementAt(i)).getState() < state) {
                    z = false;
                    break;
                }
            }
            z = true;
        }
        return z;
    }

    private void postManagedControllerErrorEvent() {
        postEvent(new ManagedControllerErrorEvent(this, this.controllerError, "Managing Player " + getClass().getName() + " received ControllerErrorEvent from " + this.controllerError.getSourceController().getClass().getName()));
        resetControllerError();
    }

    public synchronized void removeController(Controller oldController) {
        int currentState = getState();
        if (currentState == 100 || currentState == 200) {
            throw new NotRealizedError("Cannot remove Controller from an Unrealized Player");
        } else if (currentState == Controller.Started) {
            throw new ClockStartedError("Cannot remove Controller from a Started Player");
        } else if (this.controllers.indexOf(oldController) != -1) {
            stop();
            this.controllers.removeElement(oldController);
            oldController.removeControllerListener(this);
            try {
                oldController.setTimeBase(null);
            } catch (IncompatibleTimeBaseException e) {
            }
            updateDuration();
        }
    }

    private void resetControllerError() {
        setControllerError(null);
    }

    private void setControllerError(ControllerErrorEvent e) {
        this.controllerError = e;
    }

    /* access modifiers changed from: protected */
    public void setControlPanelComponent(Component c) {
        this.controlPanelComponent = c;
    }

    /* access modifiers changed from: protected */
    public void setGainControl(GainControl c) {
        if (this.gainControl != null) {
            removeControl(this.gainControl);
        }
        addControl(c);
        this.gainControl = c;
    }

    public synchronized void setMediaTime(Time t) {
        boolean isStarted = getState() == Controller.Started;
        if (isStarted) {
            stopInRestart();
        }
        super.setMediaTime(t);
        if (isStarted) {
            start();
        }
    }

    public synchronized float setRate(float rate) {
        float newRate;
        boolean isStarted = getState() == Controller.Started;
        if (isStarted) {
            stopInRestart();
        }
        newRate = super.setRate(rate);
        if (isStarted) {
            start();
        }
        return newRate;
    }

    public void setSource(DataSource source) throws IncompatibleSourceException {
        if (this.source != null) {
            throw new IncompatibleSourceException("Datasource already set in MediaHandler " + getClass().getName());
        }
        this.source = source;
    }

    /* access modifiers changed from: protected */
    public void setVisualComponent(Component c) {
        this.visualComponent = c;
    }

    public final void start() {
        int state = getState();
        int target = getTargetState();
        if (state == Controller.Started) {
            postStartEvent();
            return;
        }
        if (target < Controller.Started) {
            setTargetState(Controller.Started);
        }
        Thread thread = new Thread("Player Start Thread") {
            public void run() {
                if (AbstractPlayer.this.getState() < Controller.Started) {
                    AbstractPlayer.this.synchronousStart();
                }
            }
        };
        thread.setName("SynchronousStart Thread for " + this);
        getThreadQueue().addThread(thread);
    }

    /* access modifiers changed from: protected */
    public void synchronousStart() {
        if (getState() < 500) {
            synchronousPrefetch();
            if (getState() < 500) {
                return;
            }
        }
        synchronousSyncStart(getTimeBase().getTime());
    }

    private final synchronized void updateDuration() {
        Time duration = getPlayerDuration();
        if (duration != DURATION_UNKNOWN) {
            int n = this.controllers.size();
            for (int i = 0; i < n; i++) {
                Time d = ((Controller) this.controllers.elementAt(i)).getDuration();
                if (d == DURATION_UNKNOWN) {
                    duration = d;
                    break;
                }
                if (duration != DURATION_UNBOUNDED && (d == DURATION_UNBOUNDED || d.getNanoseconds() > duration.getNanoseconds())) {
                    duration = d;
                }
            }
        }
        boolean newDuration = false;
        if (duration == DURATION_UNKNOWN || duration == DURATION_UNBOUNDED || this.duration == DURATION_UNKNOWN || this.duration == DURATION_UNBOUNDED) {
            if (this.duration != duration) {
                newDuration = true;
            }
        } else if (this.duration == null || duration.getNanoseconds() != this.duration.getNanoseconds()) {
            newDuration = true;
        }
        if (newDuration) {
            this.duration = duration;
            postEvent(new DurationUpdateEvent(this, duration));
        }
    }
}
