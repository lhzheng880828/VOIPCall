package net.sf.fmj.media;

import java.util.Enumeration;
import java.util.Vector;
import javax.media.CachingControl;
import javax.media.Clock;
import javax.media.ClockStartedError;
import javax.media.ClockStoppedException;
import javax.media.ConfigureCompleteEvent;
import javax.media.Control;
import javax.media.Controller;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DeallocateEvent;
import javax.media.Duration;
import javax.media.IncompatibleTimeBaseException;
import javax.media.MediaTimeSetEvent;
import javax.media.NotPrefetchedError;
import javax.media.NotRealizedError;
import javax.media.PrefetchCompleteEvent;
import javax.media.RateChangeEvent;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.StartEvent;
import javax.media.StopAtTimeEvent;
import javax.media.StopTimeChangeEvent;
import javax.media.Time;
import javax.media.TimeBase;
import javax.media.TransitionEvent;

public abstract class BasicController implements Controller, Duration {
    static final int Configured = 180;
    static final int Configuring = 140;
    static String DeallocateError = "deallocate cannot be used on a started controller.";
    static String GetTimeBaseError = "Cannot get Time Base from an unrealized controller";
    static String LatencyError = "Cannot get start latency from an unrealized controller";
    static String MediaTimeError = "Cannot set media time on a unrealized controller";
    static String SetRateError = "Cannot set rate on an unrealized controller.";
    static String StopTimeError = "Cannot set stop time on an unrealized controller.";
    static String SyncStartError = "Cannot start the controller before it has been prefetched.";
    static String TimeBaseError = "Cannot set time base on an unrealized controller.";
    private Clock clock;
    private ConfigureWorkThread configureThread = null;
    private Object interruptSync = new Object();
    private boolean interrupted = false;
    private Vector listenerList = null;
    private PrefetchWorkThread prefetchThread = null;
    protected String processError = null;
    private RealizeWorkThread realizeThread = null;
    private SendEventQueue sendEvtQueue = new SendEventQueue(this);
    private TimedStartThread startThread = null;
    protected int state = 100;
    protected boolean stopThreadEnabled = true;
    private StopTimeThread stopTimeThread = null;
    private int targetState = 100;

    public abstract void abortPrefetch();

    public abstract void abortRealize();

    public abstract boolean doPrefetch();

    public abstract boolean doRealize();

    public abstract void doStart();

    public abstract boolean isConfigurable();

    public BasicController() {
        this.sendEvtQueue.setName(this.sendEvtQueue.getName() + ": SendEventQueue: " + getClass().getName());
        this.sendEvtQueue.start();
        this.clock = new BasicClock();
    }

    /* access modifiers changed from: protected */
    public void abortConfigure() {
    }

    private boolean activateStopThread(long timeToStop) {
        if (getStopTime().getNanoseconds() == CachingControl.LENGTH_UNKNOWN) {
            return false;
        }
        if (this.stopTimeThread != null && this.stopTimeThread.isAlive()) {
            this.stopTimeThread.abort();
            this.stopTimeThread = null;
        }
        if (timeToStop <= 100000000) {
            return true;
        }
        StopTimeThread stopTimeThread = new StopTimeThread(this, timeToStop);
        this.stopTimeThread = stopTimeThread;
        stopTimeThread.start();
        return false;
    }

    public final void addControllerListener(ControllerListener listener) {
        if (this.listenerList == null) {
            this.listenerList = new Vector();
        }
        synchronized (this.listenerList) {
            if (!this.listenerList.contains(listener)) {
                this.listenerList.addElement(listener);
            }
        }
    }

    private long checkStopTime() {
        long stopTime = getStopTime().getNanoseconds();
        if (stopTime == CachingControl.LENGTH_UNKNOWN) {
            return 1;
        }
        return (long) (((float) (stopTime - getMediaTime().getNanoseconds())) / getRate());
    }

    public final void close() {
        doClose();
        interrupt();
        if (this.startThread != null) {
            this.startThread.abort();
        }
        if (this.stopTimeThread != null) {
            this.stopTimeThread.abort();
        }
        if (this.sendEvtQueue != null) {
            this.sendEvtQueue.kill();
            this.sendEvtQueue = null;
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void completeConfigure() {
        this.state = 180;
        sendEvent(new ConfigureCompleteEvent(this, 140, 180, getTargetState()));
        if (getTargetState() >= Controller.Realized) {
            realize();
        }
    }

    /* access modifiers changed from: protected */
    public void completePrefetch() {
        this.clock.stop();
        this.state = 500;
        sendEvent(new PrefetchCompleteEvent(this, Controller.Prefetching, 500, getTargetState()));
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void completeRealize() {
        this.state = Controller.Realized;
        sendEvent(new RealizeCompleteEvent(this, 200, Controller.Realized, getTargetState()));
        if (getTargetState() >= 500) {
            prefetch();
        }
    }

    public synchronized void configure() {
        if (getTargetState() < 180) {
            setTargetState(180);
        }
        switch (this.state) {
            case 100:
                this.state = 140;
                sendEvent(new TransitionEvent(this, 100, 140, getTargetState()));
                this.configureThread = new ConfigureWorkThread(this);
                this.configureThread.setName(this.configureThread.getName() + "[ " + this + " ]" + " ( configureThread)");
                this.configureThread.start();
                break;
            case 180:
            case 200:
            case Controller.Realized /*300*/:
            case Controller.Prefetching /*400*/:
            case 500:
            case Controller.Started /*600*/:
                sendEvent(new ConfigureCompleteEvent(this, this.state, this.state, getTargetState()));
                break;
        }
    }

    public final void deallocate() {
        int previousState = getState();
        if (this.state == Controller.Started) {
            throwError(new ClockStartedError(DeallocateError));
        }
        switch (this.state) {
            case 140:
            case 200:
                interrupt();
                this.state = 100;
                break;
            case Controller.Prefetching /*400*/:
                interrupt();
                this.state = Controller.Realized;
                break;
            case 500:
                abortPrefetch();
                this.state = Controller.Realized;
                resetInterrupt();
                break;
        }
        setTargetState(this.state);
        doDeallocate();
        synchronized (this.interruptSync) {
            while (isInterrupted()) {
                try {
                    this.interruptSync.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        sendEvent(new DeallocateEvent(this, previousState, this.state, this.state, getMediaTime()));
    }

    /* access modifiers changed from: protected|final */
    public final void dispatchEvent(ControllerEvent evt) {
        if (this.listenerList != null) {
            synchronized (this.listenerList) {
                Enumeration list = this.listenerList.elements();
                while (list.hasMoreElements()) {
                    ((ControllerListener) list.nextElement()).controllerUpdate(evt);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void doClose() {
    }

    /* access modifiers changed from: protected */
    public boolean doConfigure() {
        return true;
    }

    /* access modifiers changed from: protected */
    public void doDeallocate() {
    }

    /* access modifiers changed from: protected */
    public void doFailedConfigure() {
        this.state = 100;
        setTargetState(100);
        String msg = "Failed to configure";
        if (this.processError != null) {
            msg = msg + ": " + this.processError;
        }
        sendEvent(new ResourceUnavailableEvent(this, msg));
        this.processError = null;
    }

    /* access modifiers changed from: protected */
    public void doFailedPrefetch() {
        this.state = Controller.Realized;
        setTargetState(Controller.Realized);
        String msg = "Failed to prefetch";
        if (this.processError != null) {
            msg = msg + ": " + this.processError;
        }
        sendEvent(new ResourceUnavailableEvent(this, msg));
        this.processError = null;
    }

    /* access modifiers changed from: protected */
    public void doFailedRealize() {
        this.state = 100;
        setTargetState(100);
        String msg = "Failed to realize";
        if (this.processError != null) {
            msg = msg + ": " + this.processError;
        }
        sendEvent(new ResourceUnavailableEvent(this, msg));
        this.processError = null;
    }

    /* access modifiers changed from: protected */
    public void doSetMediaTime(Time when) {
    }

    /* access modifiers changed from: protected */
    public float doSetRate(float factor) {
        return factor;
    }

    /* access modifiers changed from: protected */
    public void doStop() {
    }

    /* access modifiers changed from: protected */
    public Clock getClock() {
        return this.clock;
    }

    public Control getControl(String type) {
        try {
            Class<?> cls = Class.forName(type);
            Control[] cs = getControls();
            for (int i = 0; i < cs.length; i++) {
                if (cls.isInstance(cs[i])) {
                    return cs[i];
                }
            }
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public Control[] getControls() {
        return new Control[0];
    }

    public Time getDuration() {
        return Duration.DURATION_UNKNOWN;
    }

    public long getMediaNanoseconds() {
        return this.clock.getMediaNanoseconds();
    }

    public Time getMediaTime() {
        return this.clock.getMediaTime();
    }

    public float getRate() {
        return this.clock.getRate();
    }

    public Time getStartLatency() {
        if (this.state < Controller.Realized) {
            throwError(new NotRealizedError(LatencyError));
        }
        return LATENCY_UNKNOWN;
    }

    public final int getState() {
        return this.state;
    }

    public Time getStopTime() {
        return this.clock.getStopTime();
    }

    public Time getSyncTime() {
        return new Time(0);
    }

    public final int getTargetState() {
        return this.targetState;
    }

    public TimeBase getTimeBase() {
        if (this.state < Controller.Realized) {
            throwError(new NotRealizedError(GetTimeBaseError));
        }
        return this.clock.getTimeBase();
    }

    /* access modifiers changed from: protected */
    public void interrupt() {
        synchronized (this.interruptSync) {
            this.interrupted = true;
            this.interruptSync.notify();
        }
    }

    /* access modifiers changed from: protected */
    public boolean isInterrupted() {
        return this.interrupted;
    }

    public Time mapToTimeBase(Time t) throws ClockStoppedException {
        return this.clock.mapToTimeBase(t);
    }

    public final void prefetch() {
        if (getTargetState() <= Controller.Realized) {
            setTargetState(500);
        }
        switch (this.state) {
            case 100:
            case 180:
                realize();
                return;
            case Controller.Realized /*300*/:
                this.state = Controller.Prefetching;
                sendEvent(new TransitionEvent(this, Controller.Realized, Controller.Prefetching, getTargetState()));
                this.prefetchThread = new PrefetchWorkThread(this);
                this.prefetchThread.setName(this.prefetchThread.getName() + " ( prefetchThread)");
                this.prefetchThread.start();
                return;
            case 500:
            case Controller.Started /*600*/:
                sendEvent(new PrefetchCompleteEvent(this, this.state, this.state, getTargetState()));
                return;
            default:
                return;
        }
    }

    public final synchronized void realize() {
        if (getTargetState() < Controller.Realized) {
            setTargetState(Controller.Realized);
        }
        switch (this.state) {
            case 100:
                if (isConfigurable()) {
                    configure();
                    break;
                }
                break;
            case 180:
                break;
            case Controller.Realized /*300*/:
            case Controller.Prefetching /*400*/:
            case 500:
            case Controller.Started /*600*/:
                sendEvent(new RealizeCompleteEvent(this, this.state, this.state, getTargetState()));
                break;
        }
        int oldState = this.state;
        this.state = 200;
        sendEvent(new TransitionEvent(this, oldState, 200, getTargetState()));
        this.realizeThread = new RealizeWorkThread(this);
        this.realizeThread.setName(this.realizeThread.getName() + "[ " + this + " ]" + " ( realizeThread)");
        this.realizeThread.start();
    }

    public final void removeControllerListener(ControllerListener listener) {
        if (this.listenerList != null) {
            synchronized (this.listenerList) {
                if (this.listenerList != null) {
                    this.listenerList.removeElement(listener);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void resetInterrupt() {
        synchronized (this.interruptSync) {
            this.interrupted = false;
            this.interruptSync.notify();
        }
    }

    /* access modifiers changed from: protected|final */
    public final void sendEvent(ControllerEvent evt) {
        if (this.sendEvtQueue != null) {
            this.sendEvtQueue.postEvent(evt);
        }
    }

    /* access modifiers changed from: protected */
    public void setClock(Clock c) {
        this.clock = c;
    }

    /* access modifiers changed from: protected */
    public void setMediaLength(long t) {
        if (this.clock instanceof BasicClock) {
            ((BasicClock) this.clock).setMediaLength(t);
        }
    }

    public void setMediaTime(Time when) {
        if (this.state < Controller.Realized) {
            throwError(new NotRealizedError(MediaTimeError));
        }
        this.clock.setMediaTime(when);
        doSetMediaTime(when);
        sendEvent(new MediaTimeSetEvent(this, when));
    }

    public float setRate(float factor) {
        if (this.state < Controller.Realized) {
            throwError(new NotRealizedError(SetRateError));
        }
        float oldRate = getRate();
        float newRate = this.clock.setRate(doSetRate(factor));
        if (newRate != oldRate) {
            sendEvent(new RateChangeEvent(this, newRate));
        }
        return newRate;
    }

    public void setStopTime(Time t) {
        if (this.state < Controller.Realized) {
            throwError(new NotRealizedError(StopTimeError));
        }
        Time oldStopTime = getStopTime();
        this.clock.setStopTime(t);
        boolean stopTimeHasPassed = false;
        if (this.state == Controller.Started) {
            long timeToStop = checkStopTime();
            if (timeToStop < 0 || (this.stopThreadEnabled && activateStopThread(timeToStop))) {
                stopTimeHasPassed = true;
            }
        }
        if (oldStopTime.getNanoseconds() != t.getNanoseconds()) {
            sendEvent(new StopTimeChangeEvent(this, t));
        }
        if (stopTimeHasPassed) {
            stopAtTime();
        }
    }

    /* access modifiers changed from: protected|final */
    public final void setTargetState(int state) {
        this.targetState = state;
    }

    public void setTimeBase(TimeBase tb) throws IncompatibleTimeBaseException {
        if (this.state < Controller.Realized) {
            throwError(new NotRealizedError(TimeBaseError));
        }
        this.clock.setTimeBase(tb);
    }

    public void stop() {
        if (this.state == Controller.Started || this.state == Controller.Prefetching) {
            stopControllerOnly();
            doStop();
        }
    }

    /* access modifiers changed from: protected */
    public void stopAtTime() {
        stop();
        setStopTime(Clock.RESET);
        sendEvent(new StopAtTimeEvent(this, Controller.Started, 500, getTargetState(), getMediaTime()));
    }

    /* access modifiers changed from: protected */
    public void stopControllerOnly() {
        if (this.state == Controller.Started || this.state == Controller.Prefetching) {
            this.clock.stop();
            this.state = 500;
            setTargetState(500);
            if (!(this.stopTimeThread == null || !this.stopTimeThread.isAlive() || Thread.currentThread() == this.stopTimeThread)) {
                this.stopTimeThread.abort();
            }
            if (this.startThread != null && this.startThread.isAlive()) {
                this.startThread.abort();
            }
        }
    }

    public void syncStart(Time tbt) {
        if (this.state < 500) {
            throwError(new NotPrefetchedError(SyncStartError));
        }
        this.clock.syncStart(tbt);
        this.state = Controller.Started;
        setTargetState(Controller.Started);
        sendEvent(new StartEvent(this, 500, Controller.Started, Controller.Started, getMediaTime(), tbt));
        long timeToStop = checkStopTime();
        if (timeToStop < 0 || (this.stopThreadEnabled && activateStopThread(timeToStop))) {
            stopAtTime();
            return;
        }
        this.startThread = new TimedStartThread(this, tbt.getNanoseconds());
        this.startThread.setName(this.startThread.getName() + " ( startThread: " + this + " )");
        this.startThread.start();
    }

    /* access modifiers changed from: protected */
    public boolean syncStartInProgress() {
        return this.startThread != null && this.startThread.isAlive();
    }

    /* access modifiers changed from: protected */
    public void throwError(Error e) {
        Log.dumpStack(e);
        throw e;
    }
}
