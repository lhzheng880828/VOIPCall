package net.sf.fmj.ejmf.toolkit.media;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.ClockStartedError;
import javax.media.ClockStoppedException;
import javax.media.Control;
import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DataStarvedEvent;
import javax.media.DeallocateEvent;
import javax.media.EndOfMediaEvent;
import javax.media.IncompatibleTimeBaseException;
import javax.media.MediaTimeSetEvent;
import javax.media.NotPrefetchedError;
import javax.media.NotRealizedError;
import javax.media.PrefetchCompleteEvent;
import javax.media.RateChangeEvent;
import javax.media.RealizeCompleteEvent;
import javax.media.RestartingEvent;
import javax.media.StartEvent;
import javax.media.StopAtTimeEvent;
import javax.media.StopByRequestEvent;
import javax.media.StopEvent;
import javax.media.StopTimeChangeEvent;
import javax.media.Time;
import javax.media.TimeBase;
import javax.media.TransitionEvent;
import net.sf.fmj.ejmf.toolkit.controls.RateControl;
import net.sf.fmj.ejmf.toolkit.util.TimeSource;
import net.sf.fmj.utility.LoggerSingleton;

public abstract class AbstractController extends AbstractClock implements Controller {
    private static final Logger logger = LoggerSingleton.logger;
    private Vector controls = new Vector();
    private int currentState = 100;
    private ControllerEventQueue eventqueue = new ControllerEventQueue(this.listeners, "ControllerEventQueue for " + this);
    private Vector listeners = new Vector();
    private int previousState;
    private StopTimeMonitor stopTimeMonitor = new StopTimeMonitor(this, "StopTimeMonitor for " + this);
    private int targetState;
    private ThreadQueue threadqueue;
    private Object threadqueueMutex = new Object();

    public abstract void doClose();

    public abstract boolean doDeallocate();

    public abstract boolean doPrefetch();

    public abstract boolean doRealize();

    public abstract void doSetMediaTime(Time time);

    public abstract float doSetRate(float f);

    public abstract boolean doStop();

    public abstract boolean doSyncStart(Time time);

    public AbstractController() {
        this.eventqueue.start();
        this.stopTimeMonitor.start();
        addControl(new RateControl(this));
    }

    public void addControl(Control newControl) {
        synchronized (this.controls) {
            if (!this.controls.contains(newControl)) {
                this.controls.addElement(newControl);
            }
        }
    }

    public void addControllerListener(ControllerListener listener) {
        synchronized (this.listeners) {
            if (!this.listeners.contains(listener)) {
                this.listeners.addElement(listener);
            }
        }
    }

    public void blockUntilStart(Time t) {
        long latency;
        Time latencyTime = getStartLatency();
        if (latencyTime == LATENCY_UNKNOWN) {
            latency = 0;
        } else {
            latency = latencyTime.getNanoseconds();
        }
        long delay = ((t.getNanoseconds() - latency) - getTimeBase().getNanoseconds()) / TimeSource.MICROS_PER_SEC;
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }
        }
    }

    public final synchronized void close() {
        stop();
        doClose();
        this.controls = null;
        postControllerClosedEvent();
    }

    public final synchronized void deallocate() {
        if (this.currentState == Controller.Started) {
            throw new ClockStartedError("deallocate() cannot be called on a started Controller");
        }
        synchronized (this.threadqueueMutex) {
            if (this.threadqueue != null) {
                this.threadqueue.stopThreads();
            }
        }
        if (doDeallocate()) {
            int state;
            if (this.currentState == 100 || this.currentState == 200) {
                state = 100;
            } else {
                state = Controller.Realized;
            }
            setState(state);
            setTargetState(state);
            postDeallocateEvent();
        }
        synchronized (this.threadqueueMutex) {
            if (this.threadqueue != null) {
                this.threadqueue.close();
            }
            this.threadqueue = null;
        }
        if (this.stopTimeMonitor != null) {
            this.stopTimeMonitor.close();
        }
        this.stopTimeMonitor = null;
        if (this.eventqueue != null) {
            this.eventqueue.close();
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void endOfMedia() throws ClockStoppedException {
        if (this.currentState != Controller.Started) {
            throw new ClockStoppedException();
        }
        super.stop();
        setState(500);
        setTargetState(500);
        postEndOfMediaEvent();
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    public javax.media.Control getControl(java.lang.String r9) {
        /*
        r8 = this;
        r5 = 0;
        r0 = java.lang.Class.forName(r9);	 Catch:{ Exception -> 0x0021 }
        r6 = r8.controls;
        monitor-enter(r6);
        r3 = 0;
        r7 = r8.controls;	 Catch:{ all -> 0x002a }
        r4 = r7.size();	 Catch:{ all -> 0x002a }
    L_0x000f:
        if (r3 >= r4) goto L_0x0027;
    L_0x0011:
        r7 = r8.controls;	 Catch:{ all -> 0x002a }
        r1 = r7.elementAt(r3);	 Catch:{ all -> 0x002a }
        r1 = (javax.media.Control) r1;	 Catch:{ all -> 0x002a }
        r7 = r0.isInstance(r1);	 Catch:{ all -> 0x002a }
        if (r7 == 0) goto L_0x0024;
    L_0x001f:
        monitor-exit(r6);	 Catch:{ all -> 0x002a }
    L_0x0020:
        return r1;
    L_0x0021:
        r2 = move-exception;
        r1 = r5;
        goto L_0x0020;
    L_0x0024:
        r3 = r3 + 1;
        goto L_0x000f;
    L_0x0027:
        monitor-exit(r6);	 Catch:{ all -> 0x002a }
        r1 = r5;
        goto L_0x0020;
    L_0x002a:
        r5 = move-exception;
        monitor-exit(r6);	 Catch:{ all -> 0x002a }
        throw r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.ejmf.toolkit.media.AbstractController.getControl(java.lang.String):javax.media.Control");
    }

    public Control[] getControls() {
        Control[] array;
        synchronized (this.controls) {
            array = new Control[this.controls.size()];
            this.controls.copyInto(array);
        }
        return array;
    }

    public Time getDuration() {
        return DURATION_UNKNOWN;
    }

    public synchronized Time getMediaTime() {
        Time duration;
        Time mediaTime = super.getMediaTime();
        duration = getDuration();
        if (duration == DURATION_UNKNOWN || duration == DURATION_UNBOUNDED || mediaTime.getNanoseconds() <= duration.getNanoseconds()) {
            duration = mediaTime;
        }
        return duration;
    }

    public int getPreviousState() {
        return this.previousState;
    }

    public Time getStartLatency() {
        if (this.currentState != 100 && this.currentState != 200) {
            return LATENCY_UNKNOWN;
        }
        throw new NotRealizedError("Cannot get start latency from an unrealized Controller.");
    }

    public int getState() {
        return this.currentState;
    }

    public int getTargetState() {
        return this.targetState;
    }

    /* access modifiers changed from: protected */
    public ThreadQueue getThreadQueue() {
        ThreadQueue threadQueue;
        synchronized (this.threadqueueMutex) {
            if (this.threadqueue == null) {
                this.threadqueue = new ThreadQueue("ThreadQueue for " + this);
                this.threadqueue.start();
            }
            threadQueue = this.threadqueue;
        }
        return threadQueue;
    }

    public synchronized TimeBase getTimeBase() {
        if (this.currentState == 100 || this.currentState == 200) {
            throw new NotRealizedError("Cannot get time base from an Unrealized Controller");
        }
        return super.getTimeBase();
    }

    /* access modifiers changed from: protected */
    public void postControllerClosedEvent() {
        postEvent(new ControllerClosedEvent(this));
    }

    /* access modifiers changed from: protected */
    public void postControllerErrorEvent(String msg) {
        postEvent(new ControllerErrorEvent(this, msg));
    }

    /* access modifiers changed from: protected */
    public void postDataStarvedEvent() {
        postEvent(new DataStarvedEvent(this, this.previousState, this.currentState, this.targetState, getMediaTime()));
    }

    /* access modifiers changed from: protected */
    public void postDeallocateEvent() {
        postEvent(new DeallocateEvent(this, this.previousState, this.currentState, this.targetState, getMediaTime()));
    }

    /* access modifiers changed from: protected */
    public void postEndOfMediaEvent() {
        postEvent(new EndOfMediaEvent(this, this.previousState, this.currentState, this.targetState, getMediaTime()));
    }

    /* access modifiers changed from: protected */
    public void postEvent(ControllerEvent event) {
        this.eventqueue.postEvent(event);
    }

    /* access modifiers changed from: protected */
    public void postPrefetchCompleteEvent() {
        postEvent(new PrefetchCompleteEvent(this, this.previousState, this.currentState, this.targetState));
    }

    /* access modifiers changed from: protected */
    public void postRealizeCompleteEvent() {
        postEvent(new RealizeCompleteEvent(this, this.previousState, this.currentState, this.targetState));
    }

    /* access modifiers changed from: protected */
    public void postRestartingEvent() {
        postEvent(new RestartingEvent(this, this.previousState, this.currentState, this.targetState, getMediaTime()));
    }

    /* access modifiers changed from: protected */
    public void postStartEvent() {
        postEvent(new StartEvent(this, this.previousState, this.currentState, this.targetState, getMediaStartTime(), getTimeBaseStartTime()));
    }

    /* access modifiers changed from: protected */
    public void postStopAtTimeEvent() {
        postEvent(new StopAtTimeEvent(this, this.previousState, this.currentState, this.targetState, getMediaTime()));
    }

    /* access modifiers changed from: protected */
    public void postStopByRequestEvent() {
        postEvent(new StopByRequestEvent(this, this.previousState, this.currentState, this.targetState, getMediaTime()));
    }

    /* access modifiers changed from: protected */
    public void postStopEvent() {
        postEvent(new StopEvent(this, this.previousState, this.currentState, this.targetState, getMediaTime()));
    }

    /* access modifiers changed from: protected */
    public void postTransitionEvent() {
        postEvent(new TransitionEvent(this, this.previousState, this.currentState, this.targetState));
    }

    public final synchronized void prefetch() {
        if (this.currentState >= 500) {
            postPrefetchCompleteEvent();
        } else {
            if (this.targetState < 500) {
                setTargetState(500);
            }
            getThreadQueue().addThread(new Thread("Controller Prefetch Thread") {
                public void run() {
                    if (AbstractController.this.getState() < 500) {
                        AbstractController.this.synchronousPrefetch();
                    }
                }
            });
        }
    }

    public final synchronized void realize() {
        if (this.currentState >= Controller.Realized) {
            postRealizeCompleteEvent();
        } else {
            if (this.targetState < Controller.Realized) {
                setTargetState(Controller.Realized);
            }
            getThreadQueue().addThread(new Thread("Controller Realize Thread") {
                public void run() {
                    if (AbstractController.this.getState() < Controller.Realized) {
                        AbstractController.this.synchronousRealize();
                    }
                }
            });
        }
    }

    public void removeControl(Control oldControl) {
        this.controls.removeElement(oldControl);
    }

    public void removeControllerListener(ControllerListener listener) {
        synchronized (this.listeners) {
            this.listeners.removeElement(listener);
        }
    }

    public synchronized void setMediaTime(Time t) {
        if (this.currentState == 100 || this.currentState == 200) {
            throw new NotRealizedError("Cannot set media time on an Unrealized Controller");
        }
        long nano = t.getNanoseconds();
        Time duration = getDuration();
        if (!(duration == DURATION_UNKNOWN || duration == DURATION_UNBOUNDED)) {
            long limit = duration.getNanoseconds();
            if (nano > limit) {
                t = new Time(limit);
            }
        }
        super.setMediaTime(t);
        doSetMediaTime(t);
        postEvent(new MediaTimeSetEvent(this, t));
    }

    public synchronized float setRate(float rate) {
        float rate2;
        if (this.currentState == 100 || this.currentState == 200) {
            throw new NotRealizedError("Cannot set rate on an Unrealized Controller.");
        }
        float oldRate = getRate();
        float superRate = super.setRate(rate);
        float subRate = doSetRate(superRate);
        if (!(rate == 1.0f || superRate == subRate)) {
            superRate = super.setRate(subRate);
            if (superRate != subRate) {
                rate2 = setRate(1.0f);
            }
        }
        float superRate2 = superRate;
        if (superRate2 != oldRate) {
            postEvent(new RateChangeEvent(this, superRate2));
        }
        superRate = superRate2;
        rate2 = superRate2;
        return rate2;
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void setState(int state) {
        if (state != this.currentState) {
            this.previousState = this.currentState;
            this.currentState = state;
        }
    }

    public synchronized void setStopTime(Time mediaStopTime) {
        if (this.currentState == 100 || this.currentState == 200) {
            throw new NotRealizedError("Cannot set stop time on an unrealized Controller");
        }
        if (mediaStopTime.getNanoseconds() != getStopTime().getNanoseconds()) {
            super.setStopTime(mediaStopTime);
            postEvent(new StopTimeChangeEvent(this, mediaStopTime));
        }
    }

    /* access modifiers changed from: protected */
    public void setTargetState(int state) {
        this.targetState = state;
    }

    public synchronized void setTimeBase(TimeBase timebase) throws IncompatibleTimeBaseException {
        if (this.currentState == 100 || this.currentState == 200) {
            throw new NotRealizedError("Cannot set TimeBase on an Unrealized Controller.");
        }
        super.setTimeBase(timebase);
    }

    public final void stop() {
        if (stopController()) {
            postStopByRequestEvent();
        }
    }

    /* access modifiers changed from: protected */
    public void stopAtTime() {
        if (stopController()) {
            postStopAtTimeEvent();
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized boolean stopController() {
        boolean z = true;
        synchronized (this) {
            synchronized (this.threadqueueMutex) {
                if (this.threadqueue != null) {
                    this.threadqueue.stopThreads();
                }
            }
            switch (this.currentState) {
                case 100:
                case Controller.Realized /*300*/:
                case 500:
                    setTargetState(this.currentState);
                    break;
                case 200:
                    setState(100);
                    setTargetState(100);
                    break;
                case Controller.Prefetching /*400*/:
                    setState(Controller.Realized);
                    setTargetState(Controller.Realized);
                    break;
                default:
                    if (!doStop()) {
                        z = false;
                        break;
                    }
                    super.stop();
                    setState(500);
                    setTargetState(500);
                    break;
            }
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public void stopInRestart() {
        if (stopController()) {
            postRestartingEvent();
        }
    }

    /* access modifiers changed from: protected */
    public void synchronousPrefetch() {
        boolean result;
        if (this.currentState < Controller.Realized) {
            synchronousRealize();
            if (this.currentState < Controller.Realized) {
                return;
            }
        }
        setState(Controller.Prefetching);
        postTransitionEvent();
        try {
            result = doPrefetch();
        } catch (Throwable e) {
            logger.log(Level.WARNING, "" + e, e);
            postControllerErrorEvent("" + e);
            result = false;
        }
        if (result) {
            setState(500);
            postPrefetchCompleteEvent();
            return;
        }
        setState(Controller.Realized);
        setTargetState(Controller.Realized);
    }

    /* access modifiers changed from: protected */
    public void synchronousRealize() {
        boolean result;
        setState(200);
        postTransitionEvent();
        try {
            result = doRealize();
        } catch (Throwable e) {
            logger.log(Level.WARNING, "" + e, e);
            postControllerErrorEvent("" + e);
            result = false;
        }
        if (result) {
            setState(Controller.Realized);
            postRealizeCompleteEvent();
            setRate(1.0f);
            return;
        }
        setState(100);
        setTargetState(100);
    }

    /* access modifiers changed from: protected */
    public void synchronousSyncStart(Time t) {
        long latency;
        boolean result;
        setState(Controller.Started);
        postStartEvent();
        Time latencyTime = getStartLatency();
        if (latencyTime == LATENCY_UNKNOWN) {
            latency = 0;
        } else {
            latency = latencyTime.getNanoseconds();
        }
        long start = t.getNanoseconds();
        long now = getTimeBase().getNanoseconds();
        if (now + latency > start) {
            t = new Time(now + latency);
        }
        super.syncStart(t);
        try {
            result = doSyncStart(t);
        } catch (Throwable e) {
            logger.log(Level.WARNING, "" + e, e);
            postControllerErrorEvent("" + e);
            result = false;
        }
        if (!result) {
            setState(500);
            setTargetState(500);
        }
    }

    public final synchronized void syncStart(final Time t) {
        if (this.currentState == Controller.Started) {
            throw new ClockStartedError("syncStart() cannot be called on a started Clock");
        } else if (this.currentState != 500) {
            throw new NotPrefetchedError("Cannot start the Controller before it has been prefetched");
        } else {
            setTargetState(Controller.Started);
            getThreadQueue().addThread(new Thread("Controller Start Thread") {
                public void run() {
                    if (AbstractController.this.getState() < Controller.Started) {
                        AbstractController.this.synchronousSyncStart(t);
                    }
                }
            });
        }
    }
}
