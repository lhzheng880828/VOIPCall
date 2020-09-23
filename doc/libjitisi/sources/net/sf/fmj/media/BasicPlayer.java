package net.sf.fmj.media;

import java.io.IOException;
import java.util.Vector;
import javax.media.CachingControl;
import javax.media.CachingControlEvent;
import javax.media.ClockStartedError;
import javax.media.Control;
import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DownloadProgressListener;
import javax.media.DurationUpdateEvent;
import javax.media.EndOfMediaEvent;
import javax.media.ExtendedCachingControl;
import javax.media.GainControl;
import javax.media.IncompatibleSourceException;
import javax.media.IncompatibleTimeBaseException;
import javax.media.MediaLocator;
import javax.media.NotRealizedError;
import javax.media.Player;
import javax.media.Processor;
import javax.media.StopTimeChangeEvent;
import javax.media.Time;
import javax.media.TimeBase;
import javax.media.control.BufferControl;
import javax.media.protocol.DataSource;
import javax.media.protocol.RateConfiguration;
import javax.media.protocol.RateConfigureable;
import javax.media.protocol.RateRange;
import net.sf.fmj.media.control.SliderRegionControl;
import net.sf.fmj.media.control.SliderRegionControlAdapter;
import org.jitsi.android.util.java.awt.Component;

public abstract class BasicPlayer extends BasicController implements Player, ControllerListener, DownloadProgressListener {
    static final int LOCAL_STOP = 0;
    static final int RESTARTING = 2;
    static final int STOP_BY_REQUEST = 1;
    private ControllerEvent CachingControlEvent = null;
    private boolean aboutToRestart = false;
    protected BufferControl bufferControl = null;
    protected CachingControl cachingControl = null;
    private boolean closing = false;
    private Vector configureEventList = new Vector();
    protected Component controlComp = null;
    protected Vector controllerList = new Vector();
    protected Control[] controls = null;
    private Vector currentControllerList = new Vector();
    private Time duration = DURATION_UNKNOWN;
    private Vector eomEventsReceivedFrom = new Vector();
    protected ExtendedCachingControl extendedCachingControl = null;
    protected boolean framePositioning = true;
    long lastTime = 0;
    private Time mediaTimeAtStart;
    private Object mediaTimeSync = new Object();
    private Vector optionalControllerList = new Vector();
    private PlayThread playThread = null;
    private Vector potentialEventsList = null;
    private Vector prefetchEventList = new Vector();
    private boolean prefetchFailed = false;
    private Vector realizeEventList = new Vector();
    private boolean receivedAllEvents = false;
    private Vector receivedEventList = new Vector();
    public SliderRegionControl regionControl = null;
    private Vector removedControllerList = new Vector();
    private Controller restartFrom = null;
    protected DataSource source = null;
    private Object startSync = new Object();
    private Time startTime;
    private StatsThread statsThread = null;
    private Vector stopAtTimeReceivedFrom = new Vector();
    private Vector stopEventList = new Vector();

    public abstract boolean audioEnabled();

    public abstract TimeBase getMasterTimeBase();

    public abstract void updateStats();

    public abstract boolean videoEnabled();

    public BasicPlayer() {
        this.configureEventList.addElement("javax.media.ConfigureCompleteEvent");
        this.configureEventList.addElement("javax.media.ResourceUnavailableEvent");
        this.realizeEventList.addElement("javax.media.RealizeCompleteEvent");
        this.realizeEventList.addElement("javax.media.ResourceUnavailableEvent");
        this.prefetchEventList.addElement("javax.media.PrefetchCompleteEvent");
        this.prefetchEventList.addElement("javax.media.ResourceUnavailableEvent");
        this.stopEventList.addElement("javax.media.StopEvent");
        this.stopEventList.addElement("javax.media.StopByRequestEvent");
        this.stopEventList.addElement("javax.media.StopAtTimeEvent");
        this.stopThreadEnabled = false;
    }

    /* access modifiers changed from: protected|final */
    public final void abortPrefetch() {
        if (this.controllerList != null) {
            int i = this.controllerList.size();
            while (true) {
                i--;
                if (i < 0) {
                    break;
                }
                ((Controller) this.controllerList.elementAt(i)).deallocate();
            }
        }
        synchronized (this) {
            notify();
        }
    }

    /* access modifiers changed from: protected|final */
    public final void abortRealize() {
        if (this.controllerList != null) {
            int i = this.controllerList.size();
            while (true) {
                i--;
                if (i < 0) {
                    break;
                }
                ((Controller) this.controllerList.elementAt(i)).deallocate();
            }
        }
        synchronized (this) {
            notify();
        }
    }

    public synchronized void addController(Controller newController) throws IncompatibleTimeBaseException {
        int playerState = getState();
        if (playerState == Controller.Started) {
            throwError(new ClockStartedError("Cannot add controller to a started player"));
        }
        if (playerState == 100 || playerState == 200) {
            throwError(new NotRealizedError("A Controller cannot be added to an Unrealized Player"));
        }
        if (!(newController == null || newController == this)) {
            int controllerState = newController.getState();
            if (controllerState == 100 || controllerState == 200) {
                throwError(new NotRealizedError("An Unrealized Controller cannot be added to a Player"));
            }
            if (!this.controllerList.contains(newController)) {
                if (playerState == 500 && (controllerState == Controller.Realized || controllerState == Controller.Prefetching)) {
                    deallocate();
                }
                manageController(newController);
                newController.setTimeBase(getTimeBase());
                newController.setMediaTime(getMediaTime());
                newController.setStopTime(getStopTime());
                if (newController.setRate(getRate()) != getRate()) {
                    setRate(1.0f);
                }
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public float checkRateConfig(RateConfigureable rc, float rate) {
        RateConfiguration[] config = rc.getRateConfigurations();
        if (config == null) {
            return 1.0f;
        }
        int i = 0;
        while (i < config.length) {
            RateRange rr = config[i].getRate();
            if (rr == null || !rr.inRange(rate)) {
                i++;
            } else {
                rr.setCurrentRate(rate);
                float corrected = rate;
                RateConfiguration c = rc.setRateConfiguration(config[i]);
                if (c == null) {
                    return corrected;
                }
                rr = c.getRate();
                if (rr != null) {
                    return rr.getCurrentRate();
                }
                return corrected;
            }
        }
        return 1.0f;
    }

    /* access modifiers changed from: protected */
    public void completeConfigure() {
        super.completeConfigure();
        synchronized (this) {
            notify();
        }
    }

    /* access modifiers changed from: protected */
    public void completePrefetch() {
        super.completePrefetch();
        synchronized (this) {
            notify();
        }
    }

    /* access modifiers changed from: protected */
    public void completeRealize() {
        this.state = Controller.Realized;
        try {
            slaveToMasterTimeBase(getMasterTimeBase());
        } catch (IncompatibleTimeBaseException e) {
            Log.error(e);
        }
        super.completeRealize();
        synchronized (this) {
            notify();
        }
    }

    /* access modifiers changed from: protected */
    public void controllerSetStopTime(Time t) {
        super.setStopTime(t);
    }

    /* access modifiers changed from: protected */
    public void controllerStopAtTime() {
        super.stopAtTime();
    }

    public final void controllerUpdate(ControllerEvent evt) {
        processEvent(evt);
    }

    /* access modifiers changed from: protected */
    public boolean deviceBusy(BasicController mc) {
        return true;
    }

    /* access modifiers changed from: protected */
    public void doClose() {
        synchronized (this) {
            this.closing = true;
            notifyAll();
        }
        if (getState() == Controller.Started) {
            stop(0);
        }
        if (this.controllerList != null) {
            while (!this.controllerList.isEmpty()) {
                Controller c = (Controller) this.controllerList.firstElement();
                c.close();
                this.controllerList.removeElement(c);
            }
        }
        this.controlComp = null;
        if (this.statsThread != null) {
            this.statsThread.kill();
        }
        sendEvent(new ControllerClosedEvent(this));
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized boolean doConfigure() {
        boolean z = false;
        synchronized (this) {
            Controller c;
            this.potentialEventsList = this.configureEventList;
            resetReceivedEventList();
            this.receivedAllEvents = false;
            this.currentControllerList.removeAllElements();
            int i = this.controllerList.size();
            while (true) {
                i--;
                if (i < 0) {
                    break;
                }
                c = (Controller) this.controllerList.elementAt(i);
                if (c.getState() == 100 && ((c instanceof Processor) || (c instanceof BasicController))) {
                    this.currentControllerList.addElement(c);
                }
            }
            i = this.currentControllerList.size();
            while (true) {
                i--;
                if (i < 0) {
                    break;
                }
                c = (Controller) this.currentControllerList.elementAt(i);
                if (c instanceof Processor) {
                    ((Processor) c).configure();
                } else if (c instanceof BasicController) {
                    ((BasicController) c).configure();
                }
            }
            if (!this.currentControllerList.isEmpty()) {
                while (!this.closing && !this.receivedAllEvents) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
                this.currentControllerList.removeAllElements();
            }
            i = this.controllerList.size();
            while (true) {
                i--;
                if (i < 0) {
                    z = true;
                    break;
                }
                c = (Controller) this.controllerList.elementAt(i);
                if (((c instanceof Processor) || (c instanceof BasicController)) && c.getState() < Processor.Configured) {
                    Log.error("Error: Unable to configure " + c);
                    this.source.disconnect();
                    break;
                }
            }
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public void doFailedConfigure() {
        super.doFailedConfigure();
        synchronized (this) {
            notify();
        }
        close();
    }

    /* access modifiers changed from: protected */
    public void doFailedPrefetch() {
        super.doFailedPrefetch();
        synchronized (this) {
            notify();
        }
    }

    /* access modifiers changed from: protected */
    public void doFailedRealize() {
        super.doFailedRealize();
        synchronized (this) {
            notify();
        }
        close();
    }

    /* access modifiers changed from: protected */
    public boolean doPrefetch() {
        this.potentialEventsList = this.prefetchEventList;
        resetReceivedEventList();
        this.receivedAllEvents = false;
        this.currentControllerList.removeAllElements();
        Vector list = this.controllerList;
        if (list == null) {
            return false;
        }
        Controller c;
        int i = list.size();
        while (true) {
            i--;
            if (i < 0) {
                break;
            }
            c = (Controller) list.elementAt(i);
            if (c.getState() == Controller.Realized) {
                this.currentControllerList.addElement(c);
                c.prefetch();
            }
        }
        if (!this.currentControllerList.isEmpty()) {
            synchronized (this) {
                while (!this.closing && !this.receivedAllEvents) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
                this.currentControllerList.removeAllElements();
            }
        }
        i = list.size();
        while (true) {
            i--;
            if (i >= 0) {
                c = (Controller) list.elementAt(i);
                if (c.getState() < 500) {
                    Log.error("Error: Unable to prefetch " + c + "\n");
                    if (this.optionalControllerList.contains(c)) {
                        this.removedControllerList.addElement(c);
                    } else {
                        synchronized (this) {
                            this.prefetchFailed = true;
                            notifyAll();
                        }
                        return false;
                    }
                }
            } else {
                if (this.removedControllerList != null) {
                    i = this.removedControllerList.size();
                    Object o;
                    do {
                        i--;
                        if (i >= 0) {
                            o = this.removedControllerList.elementAt(i);
                            this.controllerList.removeElement(o);
                            ((BasicController) o).close();
                        } else {
                            this.removedControllerList.removeAllElements();
                        }
                    } while (deviceBusy((BasicController) o));
                    synchronized (this) {
                        this.prefetchFailed = true;
                        notifyAll();
                    }
                    return false;
                }
                return true;
            }
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized boolean doRealize() {
        boolean z = false;
        synchronized (this) {
            Controller c;
            this.potentialEventsList = this.realizeEventList;
            resetReceivedEventList();
            this.receivedAllEvents = false;
            this.currentControllerList.removeAllElements();
            int i = this.controllerList.size();
            while (true) {
                i--;
                if (i < 0) {
                    break;
                }
                c = (Controller) this.controllerList.elementAt(i);
                if (c.getState() == 100 || c.getState() == Processor.Configured) {
                    this.currentControllerList.addElement(c);
                }
            }
            i = this.currentControllerList.size();
            while (true) {
                i--;
                if (i < 0) {
                    break;
                }
                ((Controller) this.currentControllerList.elementAt(i)).realize();
            }
            if (!this.currentControllerList.isEmpty()) {
                while (!this.closing && !this.receivedAllEvents) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
                this.currentControllerList.removeAllElements();
            }
            i = this.controllerList.size();
            do {
                i--;
                if (i < 0) {
                    updateDuration();
                    this.statsThread = new StatsThread(this);
                    this.statsThread.start();
                    z = true;
                    break;
                }
                c = (Controller) this.controllerList.elementAt(i);
            } while (c.getState() >= Controller.Realized);
            Log.error("Error: Unable to realize " + c);
            this.source.disconnect();
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public void doSetMediaTime(Time now) {
    }

    /* access modifiers changed from: protected */
    public float doSetRate(float factor) {
        return factor;
    }

    private void doSetStopTime(Time t) {
        getClock().setStopTime(t);
        int i = this.controllerList.size();
        while (true) {
            i--;
            if (i >= 0) {
                ((Controller) this.controllerList.elementAt(i)).setStopTime(t);
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void doStart() {
    }

    /* access modifiers changed from: protected */
    public void doStop() {
    }

    public void downloadUpdate() {
        if (this.extendedCachingControl != null) {
            sendEvent(new CachingControlEvent(this, this.cachingControl, this.cachingControl.getContentProgress()));
            if (this.regionControl != null) {
                int maxValuePercent;
                long contentLength = this.cachingControl.getContentLength();
                if (contentLength == -1 || contentLength <= 0) {
                    maxValuePercent = 0;
                } else {
                    maxValuePercent = (int) ((100.0d * ((double) this.extendedCachingControl.getEndOffset())) / ((double) contentLength));
                    if (maxValuePercent < 0) {
                        maxValuePercent = 0;
                    } else if (maxValuePercent > 100) {
                        maxValuePercent = 100;
                    }
                }
                this.regionControl.setMinValue(0);
                this.regionControl.setMaxValue((long) maxValuePercent);
            }
        }
    }

    public String getContentType() {
        if (this.source != null) {
            return this.source.getContentType();
        }
        return null;
    }

    public final Vector getControllerList() {
        return this.controllerList;
    }

    public Component getControlPanelComponent() {
        if (getState() < Controller.Realized) {
            throwError(new NotRealizedError("Cannot get control panel component on an unrealized player"));
        }
        return this.controlComp;
    }

    public Control[] getControls() {
        if (this.controls != null) {
            return this.controls;
        }
        int i;
        Vector cv = new Vector();
        if (this.cachingControl != null) {
            cv.addElement(this.cachingControl);
        }
        if (this.bufferControl != null) {
            cv.addElement(this.bufferControl);
        }
        int size = this.controllerList.size();
        for (i = 0; i < size; i++) {
            Object[] cs = ((Controller) this.controllerList.elementAt(i)).getControls();
            if (cs != null) {
                for (Object addElement : cs) {
                    cv.addElement(addElement);
                }
            }
        }
        size = cv.size();
        Control[] ctrls = new Control[size];
        for (i = 0; i < size; i++) {
            ctrls[i] = (Control) cv.elementAt(i);
        }
        if (getState() < Controller.Realized) {
            return ctrls;
        }
        this.controls = ctrls;
        return ctrls;
    }

    public Time getDuration() {
        long t = getMediaNanoseconds();
        if (t > this.lastTime) {
            this.lastTime = t;
            updateDuration();
        }
        return this.duration;
    }

    public GainControl getGainControl() {
        if (getState() >= Controller.Realized) {
            return (GainControl) getControl("javax.media.GainControl");
        }
        throwError(new NotRealizedError("Cannot get gain control on an unrealized player"));
        return null;
    }

    public MediaLocator getMediaLocator() {
        if (this.source != null) {
            return this.source.getLocator();
        }
        return null;
    }

    private Vector getPotentialEventsList() {
        return this.potentialEventsList;
    }

    private Vector getReceivedEventsList() {
        return this.receivedEventList;
    }

    /* access modifiers changed from: protected */
    public DataSource getSource() {
        return this.source;
    }

    public Time getStartLatency() {
        super.getStartLatency();
        long t = 0;
        for (int i = 0; i < this.controllerList.size(); i++) {
            Time latency = ((Controller) this.controllerList.elementAt(i)).getStartLatency();
            if (latency != LATENCY_UNKNOWN && latency.getNanoseconds() > t) {
                t = latency.getNanoseconds();
            }
        }
        if (t == 0) {
            return LATENCY_UNKNOWN;
        }
        return new Time(t);
    }

    public Component getVisualComponent() {
        if (getState() < Controller.Realized) {
            throwError(new NotRealizedError("Cannot get visual component on an unrealized player"));
        }
        return null;
    }

    public boolean isAboutToRestart() {
        return this.aboutToRestart;
    }

    /* access modifiers changed from: protected */
    public boolean isConfigurable() {
        return false;
    }

    public boolean isFramePositionable() {
        return this.framePositioning;
    }

    /* access modifiers changed from: protected|final */
    public final void manageController(Controller controller) {
        manageController(controller, false);
    }

    /* access modifiers changed from: protected|final */
    public final void manageController(Controller controller, boolean optional) {
        if (!(controller == null || this.controllerList.contains(controller))) {
            this.controllerList.addElement(controller);
            if (optional) {
                this.optionalControllerList.addElement(controller);
            }
            controller.addControllerListener(this);
        }
        updateDuration();
    }

    private void notifyIfAllEventsArrived(Vector controllerList, Vector receivedEventList) {
        if (receivedEventList != null && receivedEventList.size() == this.currentControllerList.size()) {
            this.receivedAllEvents = true;
            resetReceivedEventList();
            synchronized (this) {
                notifyAll();
            }
        }
    }

    /* access modifiers changed from: final|declared_synchronized */
    public final synchronized void play() {
        if (getTargetState() == Controller.Started) {
            this.prefetchFailed = false;
            int state = getState();
            if (state == 100 || state == Processor.Configured || state == Controller.Realized) {
                prefetch();
            }
            while (!this.closing && !this.prefetchFailed && (getState() == Processor.Configuring || getState() == 200 || getState() == Controller.Realized || getState() == Controller.Prefetching)) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            if (getState() != Controller.Started && getTargetState() == Controller.Started && getState() == 500) {
                syncStart(getTimeBase().getTime());
            }
        }
    }

    /* access modifiers changed from: protected|final */
    public final void processEndOfMedia() {
        super.stop();
        sendEvent(new EndOfMediaEvent(this, Controller.Started, 500, getTargetState(), getMediaTime()));
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Missing block: B:131:?, code skipped:
            return;
     */
    public void processEvent(javax.media.ControllerEvent r15) {
        /*
        r14 = this;
        r2 = 600; // 0x258 float:8.41E-43 double:2.964E-321;
        r13 = 500; // 0x1f4 float:7.0E-43 double:2.47E-321;
        r12 = r15.getSourceController();
        r0 = r15 instanceof javax.media.AudioDeviceUnavailableEvent;
        if (r0 == 0) goto L_0x0015;
    L_0x000c:
        r0 = new javax.media.AudioDeviceUnavailableEvent;
        r0.m176init(r14);
        r14.sendEvent(r0);
    L_0x0014:
        return;
    L_0x0015:
        r0 = r15 instanceof javax.media.ControllerClosedEvent;
        if (r0 == 0) goto L_0x0044;
    L_0x0019:
        r0 = r14.closing;
        if (r0 != 0) goto L_0x0044;
    L_0x001d:
        r0 = r14.controllerList;
        r0 = r0.contains(r12);
        if (r0 == 0) goto L_0x0044;
    L_0x0025:
        r0 = r15 instanceof javax.media.ResourceUnavailableEvent;
        if (r0 != 0) goto L_0x0044;
    L_0x0029:
        r0 = r14.controllerList;
        r0.removeElement(r12);
        r0 = r15 instanceof javax.media.ControllerErrorEvent;
        if (r0 == 0) goto L_0x0041;
    L_0x0032:
        r1 = new javax.media.ControllerErrorEvent;
        r0 = r15;
        r0 = (javax.media.ControllerErrorEvent) r0;
        r0 = r0.getMessage();
        r1.m200init(r14, r0);
        r14.sendEvent(r1);
    L_0x0041:
        r14.close();
    L_0x0044:
        r0 = r15 instanceof javax.media.SizeChangeEvent;
        if (r0 == 0) goto L_0x006d;
    L_0x0048:
        r0 = r14.controllerList;
        r0 = r0.contains(r12);
        if (r0 == 0) goto L_0x006d;
    L_0x0050:
        r1 = new javax.media.SizeChangeEvent;
        r0 = r15;
        r0 = (javax.media.SizeChangeEvent) r0;
        r2 = r0.getWidth();
        r0 = r15;
        r0 = (javax.media.SizeChangeEvent) r0;
        r0 = r0.getHeight();
        r15 = (javax.media.SizeChangeEvent) r15;
        r3 = r15.getScale();
        r1.m256init(r14, r2, r0, r3);
        r14.sendEvent(r1);
        goto L_0x0014;
    L_0x006d:
        r0 = r15 instanceof javax.media.DurationUpdateEvent;
        if (r0 == 0) goto L_0x007d;
    L_0x0071:
        r0 = r14.controllerList;
        r0 = r0.contains(r12);
        if (r0 == 0) goto L_0x007d;
    L_0x0079:
        r14.updateDuration();
        goto L_0x0014;
    L_0x007d:
        r0 = r15 instanceof javax.media.RestartingEvent;
        if (r0 == 0) goto L_0x00bd;
    L_0x0081:
        r0 = r14.controllerList;
        r0 = r0.contains(r12);
        if (r0 == 0) goto L_0x00bd;
    L_0x0089:
        r14.restartFrom = r12;
        r0 = r14.controllerList;
        r9 = r0.size();
        super.stop();
        r14.setTargetState(r13);
        r10 = 0;
    L_0x0098:
        if (r10 >= r9) goto L_0x00aa;
    L_0x009a:
        r0 = r14.controllerList;
        r7 = r0.elementAt(r10);
        r7 = (javax.media.Controller) r7;
        if (r7 == r12) goto L_0x00a7;
    L_0x00a4:
        r7.stop();
    L_0x00a7:
        r10 = r10 + 1;
        goto L_0x0098;
    L_0x00aa:
        super.stop();
        r0 = new javax.media.RestartingEvent;
        r3 = 400; // 0x190 float:5.6E-43 double:1.976E-321;
        r5 = r14.getMediaTime();
        r1 = r14;
        r4 = r2;
        r0.m253init(r1, r2, r3, r4, r5);
        r14.sendEvent(r0);
    L_0x00bd:
        r0 = r15 instanceof javax.media.StartEvent;
        if (r0 == 0) goto L_0x00cb;
    L_0x00c1:
        r0 = r14.restartFrom;
        if (r12 != r0) goto L_0x00cb;
    L_0x00c5:
        r0 = 0;
        r14.restartFrom = r0;
        r14.start();
    L_0x00cb:
        r0 = r15 instanceof net.sf.fmj.media.SeekFailedEvent;
        if (r0 == 0) goto L_0x0105;
    L_0x00cf:
        r0 = r14.controllerList;
        r0 = r0.contains(r12);
        if (r0 == 0) goto L_0x0105;
    L_0x00d7:
        r0 = r14.controllerList;
        r9 = r0.size();
        super.stop();
        r14.setTargetState(r13);
        r10 = 0;
    L_0x00e4:
        if (r10 >= r9) goto L_0x00f6;
    L_0x00e6:
        r0 = r14.controllerList;
        r7 = r0.elementAt(r10);
        r7 = (javax.media.Controller) r7;
        if (r7 == r12) goto L_0x00f3;
    L_0x00f0:
        r7.stop();
    L_0x00f3:
        r10 = r10 + 1;
        goto L_0x00e4;
    L_0x00f6:
        r0 = new net.sf.fmj.media.SeekFailedEvent;
        r5 = r14.getMediaTime();
        r1 = r14;
        r3 = r13;
        r4 = r13;
        r0.m515init(r1, r2, r3, r4, r5);
        r14.sendEvent(r0);
    L_0x0105:
        r0 = r15 instanceof javax.media.EndOfMediaEvent;
        if (r0 == 0) goto L_0x0143;
    L_0x0109:
        r0 = r14.controllerList;
        r0 = r0.contains(r12);
        if (r0 == 0) goto L_0x0143;
    L_0x0111:
        r0 = r14.eomEventsReceivedFrom;
        r0 = r0.contains(r12);
        if (r0 != 0) goto L_0x0014;
    L_0x0119:
        r0 = r14.eomEventsReceivedFrom;
        r0.addElement(r12);
        r0 = r14.eomEventsReceivedFrom;
        r0 = r0.size();
        r1 = r14.controllerList;
        r1 = r1.size();
        if (r0 != r1) goto L_0x0014;
    L_0x012c:
        super.stop();
        r0 = new javax.media.EndOfMediaEvent;
        r4 = r14.getTargetState();
        r5 = r14.getMediaTime();
        r1 = r14;
        r3 = r13;
        r0.m210init(r1, r2, r3, r4, r5);
        r14.sendEvent(r0);
        goto L_0x0014;
    L_0x0143:
        r0 = r15 instanceof javax.media.StopAtTimeEvent;
        if (r0 == 0) goto L_0x01c6;
    L_0x0147:
        r0 = r14.controllerList;
        r0 = r0.contains(r12);
        if (r0 == 0) goto L_0x01c6;
    L_0x014f:
        r0 = r14.getState();
        if (r0 != r2) goto L_0x01c6;
    L_0x0155:
        r13 = r14.stopAtTimeReceivedFrom;
        monitor-enter(r13);
        r0 = r14.stopAtTimeReceivedFrom;	 Catch:{ all -> 0x0163 }
        r0 = r0.contains(r12);	 Catch:{ all -> 0x0163 }
        if (r0 == 0) goto L_0x0166;
    L_0x0160:
        monitor-exit(r13);	 Catch:{ all -> 0x0163 }
        goto L_0x0014;
    L_0x0163:
        r0 = move-exception;
        monitor-exit(r13);	 Catch:{ all -> 0x0163 }
        throw r0;
    L_0x0166:
        r0 = r14.stopAtTimeReceivedFrom;	 Catch:{ all -> 0x0163 }
        r0.addElement(r12);	 Catch:{ all -> 0x0163 }
        r0 = r14.stopAtTimeReceivedFrom;	 Catch:{ all -> 0x0163 }
        r0 = r0.size();	 Catch:{ all -> 0x0163 }
        r1 = r14.controllerList;	 Catch:{ all -> 0x0163 }
        r1 = r1.size();	 Catch:{ all -> 0x0163 }
        if (r0 != r1) goto L_0x01c1;
    L_0x0179:
        r6 = 1;
    L_0x017a:
        if (r6 != 0) goto L_0x019f;
    L_0x017c:
        r6 = 1;
        r9 = 0;
    L_0x017e:
        r0 = r14.controllerList;	 Catch:{ all -> 0x0163 }
        r0 = r0.size();	 Catch:{ all -> 0x0163 }
        if (r9 >= r0) goto L_0x019f;
    L_0x0186:
        r0 = r14.controllerList;	 Catch:{ all -> 0x0163 }
        r7 = r0.elementAt(r9);	 Catch:{ all -> 0x0163 }
        r7 = (javax.media.Controller) r7;	 Catch:{ all -> 0x0163 }
        r0 = r14.stopAtTimeReceivedFrom;	 Catch:{ all -> 0x0163 }
        r0 = r0.contains(r7);	 Catch:{ all -> 0x0163 }
        if (r0 != 0) goto L_0x01c3;
    L_0x0196:
        r0 = r14.eomEventsReceivedFrom;	 Catch:{ all -> 0x0163 }
        r0 = r0.contains(r7);	 Catch:{ all -> 0x0163 }
        if (r0 != 0) goto L_0x01c3;
    L_0x019e:
        r6 = 0;
    L_0x019f:
        if (r6 == 0) goto L_0x01be;
    L_0x01a1:
        super.stop();	 Catch:{ all -> 0x0163 }
        r0 = javax.media.Clock.RESET;	 Catch:{ all -> 0x0163 }
        r14.doSetStopTime(r0);	 Catch:{ all -> 0x0163 }
        r0 = new javax.media.StopAtTimeEvent;	 Catch:{ all -> 0x0163 }
        r2 = 600; // 0x258 float:8.41E-43 double:2.964E-321;
        r3 = 500; // 0x1f4 float:7.0E-43 double:2.47E-321;
        r4 = r14.getTargetState();	 Catch:{ all -> 0x0163 }
        r5 = r14.getMediaTime();	 Catch:{ all -> 0x0163 }
        r1 = r14;
        r0.m258init(r1, r2, r3, r4, r5);	 Catch:{ all -> 0x0163 }
        r14.sendEvent(r0);	 Catch:{ all -> 0x0163 }
    L_0x01be:
        monitor-exit(r13);	 Catch:{ all -> 0x0163 }
        goto L_0x0014;
    L_0x01c1:
        r6 = 0;
        goto L_0x017a;
    L_0x01c3:
        r9 = r9 + 1;
        goto L_0x017e;
    L_0x01c6:
        r0 = r15 instanceof javax.media.CachingControlEvent;
        if (r0 == 0) goto L_0x01e6;
    L_0x01ca:
        r0 = r14.controllerList;
        r0 = r0.contains(r12);
        if (r0 == 0) goto L_0x01e6;
    L_0x01d2:
        r15 = (javax.media.CachingControlEvent) r15;
        r11 = r15.getCachingControl();
        r0 = new javax.media.CachingControlEvent;
        r2 = r11.getContentProgress();
        r0.m182init(r14, r11, r2);
        r14.sendEvent(r0);
        goto L_0x0014;
    L_0x01e6:
        r8 = r14.potentialEventsList;
        r0 = r14.controllerList;
        if (r0 == 0) goto L_0x0014;
    L_0x01ec:
        r0 = r14.controllerList;
        r0 = r0.contains(r12);
        if (r0 == 0) goto L_0x0014;
    L_0x01f4:
        if (r8 == 0) goto L_0x0014;
    L_0x01f6:
        r0 = r15.getClass();
        r0 = r0.getName();
        r0 = r8.contains(r0);
        if (r0 == 0) goto L_0x0014;
    L_0x0204:
        r14.updateReceivedEventsList(r15);
        r0 = r14.controllerList;
        r1 = r14.getReceivedEventsList();
        r14.notifyIfAllEventsArrived(r0, r1);
        goto L_0x0014;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.BasicPlayer.processEvent(javax.media.ControllerEvent):void");
    }

    public final synchronized void removeController(Controller oldController) {
        int state = getState();
        if (state < Controller.Realized) {
            throwError(new NotRealizedError("Cannot remove controller from a unrealized player"));
        }
        if (state == Controller.Started) {
            throwError(new ClockStartedError("Cannot remove controller from a started player"));
        }
        if (oldController != null) {
            if (this.controllerList.contains(oldController)) {
                this.controllerList.removeElement(oldController);
                oldController.removeControllerListener(this);
                updateDuration();
                try {
                    oldController.setTimeBase(null);
                } catch (IncompatibleTimeBaseException e) {
                }
            }
        }
    }

    private void resetReceivedEventList() {
        if (this.receivedEventList != null) {
            this.receivedEventList.removeAllElements();
        }
    }

    /* access modifiers changed from: protected */
    public void setMediaLength(long t) {
        this.duration = new Time(t);
        super.setMediaLength(t);
    }

    /* JADX WARNING: Missing block: B:28:?, code skipped:
            return;
     */
    public final void setMediaTime(javax.media.Time r5) {
        /*
        r4 = this;
        r1 = r4.state;
        r2 = 300; // 0x12c float:4.2E-43 double:1.48E-321;
        if (r1 >= r2) goto L_0x0010;
    L_0x0006:
        r1 = new javax.media.NotRealizedError;
        r2 = MediaTimeError;
        r1.m237init(r2);
        r4.throwError(r1);
    L_0x0010:
        r2 = r4.mediaTimeSync;
        monitor-enter(r2);
        r1 = r4.syncStartInProgress();	 Catch:{ all -> 0x0052 }
        if (r1 == 0) goto L_0x001b;
    L_0x0019:
        monitor-exit(r2);	 Catch:{ all -> 0x0052 }
    L_0x001a:
        return;
    L_0x001b:
        r1 = r4.getState();	 Catch:{ all -> 0x0052 }
        r3 = 600; // 0x258 float:8.41E-43 double:2.964E-321;
        if (r1 != r3) goto L_0x002a;
    L_0x0023:
        r1 = 1;
        r4.aboutToRestart = r1;	 Catch:{ all -> 0x0052 }
        r1 = 2;
        r4.stop(r1);	 Catch:{ all -> 0x0052 }
    L_0x002a:
        r1 = r4.source;	 Catch:{ all -> 0x0052 }
        r1 = r1 instanceof javax.media.protocol.Positionable;	 Catch:{ all -> 0x0052 }
        if (r1 == 0) goto L_0x0039;
    L_0x0030:
        r1 = r4.source;	 Catch:{ all -> 0x0052 }
        r1 = (javax.media.protocol.Positionable) r1;	 Catch:{ all -> 0x0052 }
        r3 = 2;
        r5 = r1.setPosition(r5, r3);	 Catch:{ all -> 0x0052 }
    L_0x0039:
        super.setMediaTime(r5);	 Catch:{ all -> 0x0052 }
        r1 = r4.controllerList;	 Catch:{ all -> 0x0052 }
        r0 = r1.size();	 Catch:{ all -> 0x0052 }
    L_0x0042:
        r0 = r0 + -1;
        if (r0 < 0) goto L_0x0055;
    L_0x0046:
        r1 = r4.controllerList;	 Catch:{ all -> 0x0052 }
        r1 = r1.elementAt(r0);	 Catch:{ all -> 0x0052 }
        r1 = (javax.media.Controller) r1;	 Catch:{ all -> 0x0052 }
        r1.setMediaTime(r5);	 Catch:{ all -> 0x0052 }
        goto L_0x0042;
    L_0x0052:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x0052 }
        throw r1;
    L_0x0055:
        r4.doSetMediaTime(r5);	 Catch:{ all -> 0x0052 }
        r1 = r4.aboutToRestart;	 Catch:{ all -> 0x0052 }
        if (r1 == 0) goto L_0x006a;
    L_0x005c:
        r1 = r4.getTimeBase();	 Catch:{ all -> 0x0052 }
        r1 = r1.getTime();	 Catch:{ all -> 0x0052 }
        r4.syncStart(r1);	 Catch:{ all -> 0x0052 }
        r1 = 0;
        r4.aboutToRestart = r1;	 Catch:{ all -> 0x0052 }
    L_0x006a:
        monitor-exit(r2);	 Catch:{ all -> 0x0052 }
        goto L_0x001a;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.BasicPlayer.setMediaTime(javax.media.Time):void");
    }

    public float setRate(float rate) {
        if (this.state < Controller.Realized) {
            throwError(new NotRealizedError("Cannot set rate on an unrealized Player."));
        }
        if (this.source instanceof RateConfigureable) {
            rate = checkRateConfig((RateConfigureable) this.source, rate);
        }
        float oldRate = getRate();
        if (oldRate == rate) {
            return rate;
        }
        float rateSet;
        if (getState() == Controller.Started) {
            this.aboutToRestart = true;
            stop(2);
        }
        if (trySetRate(rate)) {
            rateSet = rate;
        } else if (trySetRate(oldRate)) {
            rateSet = oldRate;
        } else {
            trySetRate(1.0f);
            rateSet = 1.0f;
        }
        super.setRate(rateSet);
        if (this.aboutToRestart) {
            syncStart(getTimeBase().getTime());
            this.aboutToRestart = false;
        }
        return rateSet;
    }

    public void setSource(DataSource source) throws IOException, IncompatibleSourceException {
        this.source = source;
        try {
            this.cachingControl = (CachingControl) source.getControl(CachingControl.class.getName());
            if (this.cachingControl != null && (this.cachingControl instanceof ExtendedCachingControl)) {
                this.extendedCachingControl = (ExtendedCachingControl) this.cachingControl;
                if (this.extendedCachingControl != null) {
                    this.regionControl = new SliderRegionControlAdapter();
                    this.extendedCachingControl.addDownloadProgressListener(this, 100);
                }
            }
        } catch (ClassCastException e) {
        }
    }

    public void setStopTime(Time t) {
        if (this.state < Controller.Realized) {
            throwError(new NotRealizedError("Cannot set stop time on an unrealized controller."));
        }
        if (getClock().getStopTime() == null || getClock().getStopTime().getNanoseconds() != t.getNanoseconds()) {
            sendEvent(new StopTimeChangeEvent(this, t));
        }
        doSetStopTime(t);
    }

    public void setTimeBase(TimeBase tb) throws IncompatibleTimeBaseException {
        TimeBase oldTimeBase = getMasterTimeBase();
        if (tb == null) {
            tb = oldTimeBase;
        }
        if (this.controllerList != null) {
            int i;
            try {
                i = this.controllerList.size();
                while (true) {
                    i--;
                    if (i < 0) {
                        break;
                    }
                    ((Controller) this.controllerList.elementAt(i)).setTimeBase(tb);
                }
            } catch (IncompatibleTimeBaseException e) {
                i = this.controllerList.size();
                while (true) {
                    i--;
                    if (i < 0) {
                        break;
                    }
                    Controller cx = (Controller) this.controllerList.elementAt(i);
                    if (cx == null) {
                        break;
                    }
                    cx.setTimeBase(oldTimeBase);
                }
                Log.dumpStack(e);
                throw e;
            }
        }
        super.setTimeBase(tb);
    }

    /* access modifiers changed from: protected */
    public void slaveToMasterTimeBase(TimeBase tb) throws IncompatibleTimeBaseException {
        setTimeBase(tb);
    }

    /* JADX WARNING: Missing block: B:23:?, code skipped:
            return;
     */
    public final void start() {
        /*
        r8 = this;
        r1 = 600; // 0x258 float:8.41E-43 double:2.964E-321;
        r7 = r8.startSync;
        monitor-enter(r7);
        r0 = r8.restartFrom;	 Catch:{ all -> 0x0026 }
        if (r0 == 0) goto L_0x000b;
    L_0x0009:
        monitor-exit(r7);	 Catch:{ all -> 0x0026 }
    L_0x000a:
        return;
    L_0x000b:
        r0 = r8.getState();	 Catch:{ all -> 0x0026 }
        if (r0 != r1) goto L_0x0029;
    L_0x0011:
        r0 = new javax.media.StartEvent;	 Catch:{ all -> 0x0026 }
        r2 = 600; // 0x258 float:8.41E-43 double:2.964E-321;
        r3 = 600; // 0x258 float:8.41E-43 double:2.964E-321;
        r4 = 600; // 0x258 float:8.41E-43 double:2.964E-321;
        r5 = r8.mediaTimeAtStart;	 Catch:{ all -> 0x0026 }
        r6 = r8.startTime;	 Catch:{ all -> 0x0026 }
        r1 = r8;
        r0.m257init(r1, r2, r3, r4, r5, r6);	 Catch:{ all -> 0x0026 }
        r8.sendEvent(r0);	 Catch:{ all -> 0x0026 }
        monitor-exit(r7);	 Catch:{ all -> 0x0026 }
        goto L_0x000a;
    L_0x0026:
        r0 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x0026 }
        throw r0;
    L_0x0029:
        r0 = r8.playThread;	 Catch:{ all -> 0x0026 }
        if (r0 == 0) goto L_0x0035;
    L_0x002d:
        r0 = r8.playThread;	 Catch:{ all -> 0x0026 }
        r0 = r0.isAlive();	 Catch:{ all -> 0x0026 }
        if (r0 != 0) goto L_0x0046;
    L_0x0035:
        r0 = 600; // 0x258 float:8.41E-43 double:2.964E-321;
        r8.setTargetState(r0);	 Catch:{ all -> 0x0026 }
        r0 = new net.sf.fmj.media.PlayThread;	 Catch:{ all -> 0x0026 }
        r0.m495init(r8);	 Catch:{ all -> 0x0026 }
        r8.playThread = r0;	 Catch:{ all -> 0x0026 }
        r0 = r8.playThread;	 Catch:{ all -> 0x0026 }
        r0.start();	 Catch:{ all -> 0x0026 }
    L_0x0046:
        monitor-exit(r7);	 Catch:{ all -> 0x0026 }
        goto L_0x000a;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.BasicPlayer.start():void");
    }

    public final void stop() {
        stop(1);
    }

    /* JADX WARNING: Missing block: B:51:?, code skipped:
            return;
     */
    private void stop(int r10) {
        /*
        r9 = this;
        r4 = 600; // 0x258 float:8.41E-43 double:2.964E-321;
        r8 = r9.getState();
        switch(r8) {
            case 100: goto L_0x002c;
            case 200: goto L_0x0030;
            case 300: goto L_0x002c;
            case 400: goto L_0x0036;
            case 500: goto L_0x002c;
            case 600: goto L_0x0036;
            default: goto L_0x0009;
        };
    L_0x0009:
        r0 = r9.getState();
        if (r0 == r4) goto L_0x006c;
    L_0x000f:
        switch(r10) {
            case 1: goto L_0x003c;
            case 2: goto L_0x0056;
            default: goto L_0x0012;
        };
    L_0x0012:
        r0 = new javax.media.StopEvent;
        r2 = r9.getState();
        r3 = r9.getState();
        r4 = r9.getTargetState();
        r5 = r9.getMediaTime();
        r1 = r9;
        r0.m206init(r1, r2, r3, r4, r5);
        r9.sendEvent(r0);
    L_0x002b:
        return;
    L_0x002c:
        r9.setTargetState(r8);
        goto L_0x0009;
    L_0x0030:
        r0 = 300; // 0x12c float:4.2E-43 double:1.48E-321;
        r9.setTargetState(r0);
        goto L_0x0009;
    L_0x0036:
        r0 = 500; // 0x1f4 float:7.0E-43 double:2.47E-321;
        r9.setTargetState(r0);
        goto L_0x0009;
    L_0x003c:
        r0 = new javax.media.StopByRequestEvent;
        r2 = r9.getState();
        r3 = r9.getState();
        r4 = r9.getTargetState();
        r5 = r9.getMediaTime();
        r1 = r9;
        r0.m259init(r1, r2, r3, r4, r5);
        r9.sendEvent(r0);
        goto L_0x002b;
    L_0x0056:
        r0 = new javax.media.RestartingEvent;
        r2 = r9.getState();
        r3 = r9.getState();
        r5 = r9.getMediaTime();
        r1 = r9;
        r0.m253init(r1, r2, r3, r4, r5);
        r9.sendEvent(r0);
        goto L_0x002b;
    L_0x006c:
        r0 = r9.getState();
        if (r0 != r4) goto L_0x002b;
    L_0x0072:
        monitor-enter(r9);
        r0 = r9.stopEventList;	 Catch:{ all -> 0x009d }
        r9.potentialEventsList = r0;	 Catch:{ all -> 0x009d }
        r9.resetReceivedEventList();	 Catch:{ all -> 0x009d }
        r0 = 0;
        r9.receivedAllEvents = r0;	 Catch:{ all -> 0x009d }
        r0 = r9.currentControllerList;	 Catch:{ all -> 0x009d }
        r0.removeAllElements();	 Catch:{ all -> 0x009d }
        r0 = r9.controllerList;	 Catch:{ all -> 0x009d }
        r7 = r0.size();	 Catch:{ all -> 0x009d }
    L_0x0088:
        r7 = r7 + -1;
        if (r7 < 0) goto L_0x00a0;
    L_0x008c:
        r0 = r9.controllerList;	 Catch:{ all -> 0x009d }
        r6 = r0.elementAt(r7);	 Catch:{ all -> 0x009d }
        r6 = (javax.media.Controller) r6;	 Catch:{ all -> 0x009d }
        r0 = r9.currentControllerList;	 Catch:{ all -> 0x009d }
        r0.addElement(r6);	 Catch:{ all -> 0x009d }
        r6.stop();	 Catch:{ all -> 0x009d }
        goto L_0x0088;
    L_0x009d:
        r0 = move-exception;
        monitor-exit(r9);	 Catch:{ all -> 0x009d }
        throw r0;
    L_0x00a0:
        r0 = r9.currentControllerList;	 Catch:{ all -> 0x009d }
        if (r0 != 0) goto L_0x00a6;
    L_0x00a4:
        monitor-exit(r9);	 Catch:{ all -> 0x009d }
        goto L_0x002b;
    L_0x00a6:
        r0 = r9.currentControllerList;	 Catch:{ all -> 0x009d }
        r0 = r0.isEmpty();	 Catch:{ all -> 0x009d }
        if (r0 != 0) goto L_0x00c0;
    L_0x00ae:
        r0 = r9.closing;	 Catch:{ InterruptedException -> 0x00ba }
        if (r0 != 0) goto L_0x00bb;
    L_0x00b2:
        r0 = r9.receivedAllEvents;	 Catch:{ InterruptedException -> 0x00ba }
        if (r0 != 0) goto L_0x00bb;
    L_0x00b6:
        r9.wait();	 Catch:{ InterruptedException -> 0x00ba }
        goto L_0x00ae;
    L_0x00ba:
        r0 = move-exception;
    L_0x00bb:
        r0 = r9.currentControllerList;	 Catch:{ all -> 0x009d }
        r0.removeAllElements();	 Catch:{ all -> 0x009d }
    L_0x00c0:
        super.stop();	 Catch:{ all -> 0x009d }
        switch(r10) {
            case 1: goto L_0x00e0;
            case 2: goto L_0x00f8;
            default: goto L_0x00c6;
        };	 Catch:{ all -> 0x009d }
    L_0x00c6:
        r0 = new javax.media.StopEvent;	 Catch:{ all -> 0x009d }
        r2 = 600; // 0x258 float:8.41E-43 double:2.964E-321;
        r3 = r9.getState();	 Catch:{ all -> 0x009d }
        r4 = r9.getTargetState();	 Catch:{ all -> 0x009d }
        r5 = r9.getMediaTime();	 Catch:{ all -> 0x009d }
        r1 = r9;
        r0.m206init(r1, r2, r3, r4, r5);	 Catch:{ all -> 0x009d }
        r9.sendEvent(r0);	 Catch:{ all -> 0x009d }
    L_0x00dd:
        monitor-exit(r9);	 Catch:{ all -> 0x009d }
        goto L_0x002b;
    L_0x00e0:
        r0 = new javax.media.StopByRequestEvent;	 Catch:{ all -> 0x009d }
        r2 = 600; // 0x258 float:8.41E-43 double:2.964E-321;
        r3 = r9.getState();	 Catch:{ all -> 0x009d }
        r4 = r9.getTargetState();	 Catch:{ all -> 0x009d }
        r5 = r9.getMediaTime();	 Catch:{ all -> 0x009d }
        r1 = r9;
        r0.m259init(r1, r2, r3, r4, r5);	 Catch:{ all -> 0x009d }
        r9.sendEvent(r0);	 Catch:{ all -> 0x009d }
        goto L_0x00dd;
    L_0x00f8:
        r0 = new javax.media.RestartingEvent;	 Catch:{ all -> 0x009d }
        r2 = 600; // 0x258 float:8.41E-43 double:2.964E-321;
        r3 = r9.getState();	 Catch:{ all -> 0x009d }
        r4 = 600; // 0x258 float:8.41E-43 double:2.964E-321;
        r5 = r9.getMediaTime();	 Catch:{ all -> 0x009d }
        r1 = r9;
        r0.m253init(r1, r2, r3, r4, r5);	 Catch:{ all -> 0x009d }
        r9.sendEvent(r0);	 Catch:{ all -> 0x009d }
        goto L_0x00dd;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.BasicPlayer.stop(int):void");
    }

    /* access modifiers changed from: protected */
    public void stopAtTime() {
    }

    /* JADX WARNING: Missing block: B:30:?, code skipped:
            return;
     */
    public final void syncStart(javax.media.Time r7) {
        /*
        r6 = this;
        r5 = 600; // 0x258 float:8.41E-43 double:2.964E-321;
        r3 = r6.mediaTimeSync;
        monitor-enter(r3);
        r2 = r6.syncStartInProgress();	 Catch:{ all -> 0x0056 }
        if (r2 == 0) goto L_0x000d;
    L_0x000b:
        monitor-exit(r3);	 Catch:{ all -> 0x0056 }
    L_0x000c:
        return;
    L_0x000d:
        r1 = r6.getState();	 Catch:{ all -> 0x0056 }
        if (r1 != r5) goto L_0x001d;
    L_0x0013:
        r2 = new javax.media.ClockStartedError;	 Catch:{ all -> 0x0056 }
        r4 = "syncStart() cannot be used on an already started player";
        r2.m192init(r4);	 Catch:{ all -> 0x0056 }
        r6.throwError(r2);	 Catch:{ all -> 0x0056 }
    L_0x001d:
        r2 = 500; // 0x1f4 float:7.0E-43 double:2.47E-321;
        if (r1 == r2) goto L_0x002b;
    L_0x0021:
        r2 = new javax.media.NotPrefetchedError;	 Catch:{ all -> 0x0056 }
        r4 = "Cannot start player before it has been prefetched";
        r2.m235init(r4);	 Catch:{ all -> 0x0056 }
        r6.throwError(r2);	 Catch:{ all -> 0x0056 }
    L_0x002b:
        r2 = r6.eomEventsReceivedFrom;	 Catch:{ all -> 0x0056 }
        r2.removeAllElements();	 Catch:{ all -> 0x0056 }
        r2 = r6.stopAtTimeReceivedFrom;	 Catch:{ all -> 0x0056 }
        r2.removeAllElements();	 Catch:{ all -> 0x0056 }
        r2 = 600; // 0x258 float:8.41E-43 double:2.964E-321;
        r6.setTargetState(r2);	 Catch:{ all -> 0x0056 }
        r2 = r6.controllerList;	 Catch:{ all -> 0x0056 }
        r0 = r2.size();	 Catch:{ all -> 0x0056 }
    L_0x0040:
        r0 = r0 + -1;
        if (r0 < 0) goto L_0x0059;
    L_0x0044:
        r2 = r6.getTargetState();	 Catch:{ all -> 0x0056 }
        if (r2 != r5) goto L_0x0040;
    L_0x004a:
        r2 = r6.controllerList;	 Catch:{ all -> 0x0056 }
        r2 = r2.elementAt(r0);	 Catch:{ all -> 0x0056 }
        r2 = (javax.media.Controller) r2;	 Catch:{ all -> 0x0056 }
        r2.syncStart(r7);	 Catch:{ all -> 0x0056 }
        goto L_0x0040;
    L_0x0056:
        r2 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x0056 }
        throw r2;
    L_0x0059:
        r2 = r6.getTargetState();	 Catch:{ all -> 0x0056 }
        if (r2 != r5) goto L_0x006a;
    L_0x005f:
        r6.startTime = r7;	 Catch:{ all -> 0x0056 }
        r2 = r6.getMediaTime();	 Catch:{ all -> 0x0056 }
        r6.mediaTimeAtStart = r2;	 Catch:{ all -> 0x0056 }
        super.syncStart(r7);	 Catch:{ all -> 0x0056 }
    L_0x006a:
        monitor-exit(r3);	 Catch:{ all -> 0x0056 }
        goto L_0x000c;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.BasicPlayer.syncStart(javax.media.Time):void");
    }

    private boolean trySetRate(float rate) {
        int i = this.controllerList.size();
        do {
            i--;
            if (i < 0) {
                return true;
            }
        } while (((Controller) this.controllerList.elementAt(i)).setRate(rate) == rate);
        return false;
    }

    public final void unmanageController(Controller controller) {
        if (controller != null && this.controllerList.contains(controller)) {
            this.controllerList.removeElement(controller);
            controller.removeControllerListener(this);
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void updateDuration() {
        Time oldDuration = this.duration;
        this.duration = DURATION_UNKNOWN;
        for (int i = 0; i < this.controllerList.size(); i++) {
            Controller c = (Controller) this.controllerList.elementAt(i);
            Time dur = c.getDuration();
            if (dur.equals(DURATION_UNKNOWN)) {
                if (!(c instanceof BasicController)) {
                    this.duration = DURATION_UNKNOWN;
                    break;
                }
            } else if (dur.equals(DURATION_UNBOUNDED)) {
                this.duration = DURATION_UNBOUNDED;
                break;
            } else if (this.duration.equals(DURATION_UNKNOWN)) {
                this.duration = dur;
            } else if (this.duration.getNanoseconds() < dur.getNanoseconds()) {
                this.duration = dur;
            }
        }
        if (this.duration.getNanoseconds() != oldDuration.getNanoseconds()) {
            setMediaLength(this.duration.getNanoseconds());
            sendEvent(new DurationUpdateEvent(this, this.duration));
        }
    }

    private void updateReceivedEventsList(ControllerEvent event) {
        if (this.receivedEventList != null) {
            Controller source = event.getSourceController();
            if (!this.receivedEventList.contains(source)) {
                this.receivedEventList.addElement(source);
            }
        }
    }
}
