package net.sf.fmj.media;

import com.sun.media.controls.BitRateAdapter;
import com.sun.media.controls.FrameRateAdapter;
import java.io.IOException;
import java.util.Vector;
import javax.media.Buffer;
import javax.media.Clock;
import javax.media.ClockStoppedException;
import javax.media.Control;
import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.Demultiplexer;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.GainControl;
import javax.media.IncompatibleSourceException;
import javax.media.IncompatibleTimeBaseException;
import javax.media.InternalErrorEvent;
import javax.media.Manager;
import javax.media.MediaTimeSetEvent;
import javax.media.NotRealizedError;
import javax.media.Owned;
import javax.media.PlugIn;
import javax.media.Renderer;
import javax.media.RestartingEvent;
import javax.media.SizeChangeEvent;
import javax.media.StartEvent;
import javax.media.StopAtTimeEvent;
import javax.media.StopByRequestEvent;
import javax.media.StopTimeChangeEvent;
import javax.media.Time;
import javax.media.TimeBase;
import javax.media.Track;
import javax.media.control.BitRateControl;
import javax.media.control.BufferControl;
import javax.media.control.FramePositioningControl;
import javax.media.control.FrameRateControl;
import javax.media.format.AudioFormat;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.format.YUVFormat;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;
import javax.media.renderer.VideoRenderer;
import javax.media.renderer.VisualContainer;
import net.sf.fmj.ejmf.toolkit.util.TimeSource;
import net.sf.fmj.filtergraph.GraphNode;
import net.sf.fmj.filtergraph.SimpleGraphBuilder;
import net.sf.fmj.media.control.FramePositioningAdapter;
import net.sf.fmj.media.control.ProgressControl;
import net.sf.fmj.media.control.ProgressControlAdapter;
import net.sf.fmj.media.control.StringControl;
import net.sf.fmj.media.control.StringControlAdapter;
import net.sf.fmj.media.protocol.Streamable;
import net.sf.fmj.media.renderer.audio.AudioRenderer;
import net.sf.fmj.media.util.RTPInfo;
import net.sf.fmj.media.util.Resource;
import org.jitsi.android.util.java.awt.Color;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.Container;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.FlowLayout;
import org.jitsi.android.util.java.awt.Panel;

public class PlaybackEngine extends BasicController implements ModuleListener {
    static String NOT_CONFIGURED_ERROR = "cannot be called before configured";
    static String NOT_REALIZED_ERROR = "cannot be called before realized";
    static String STARTED_ERROR = "cannot be called after started";
    public static boolean TRACE_ON = false;
    static boolean USE_BACKUP = false;
    static boolean USE_MASTER = true;
    protected static boolean needSavingDB = false;
    protected BitRateControl bitRateControl;
    String configError = ("Failed to configure: " + this);
    String configInt2Error = "interrupted while the Processor is being configured.";
    String configIntError = "  The configure process is being interrupted.\n";
    protected Container container = null;
    private boolean dataPathBlocked = false;
    private boolean deallocated = false;
    protected DataSource dsource;
    protected Vector filters;
    protected FramePositioningControl framePositioningControl = null;
    protected FrameRateControl frameRateControl;
    protected String genericProcessorError = "cannot handle the customized options set on the Processor.\nCheck the logs for full details.";
    private boolean internalErrorOccurred = false;
    long lastBitRate = 0;
    long lastStatsTime = 0;
    private long latency = 0;
    long markedDataStartTime = 0;
    protected BasicSinkModule masterSink = null;
    protected Vector modules;
    String parseError = "failed to parse the input media.";
    protected Demultiplexer parser;
    protected BasicPlayer player;
    public boolean prefetchEnabled = true;
    String prefetchError = ("Failed to prefetch: " + this);
    boolean prefetchLogged = false;
    private long prefetchTime;
    protected boolean prefetched = false;
    protected ProgressControl progressControl;
    private float rate = 1.0f;
    protected String realizeError = ("Failed to realize: " + this);
    private long realizeTime;
    boolean reportOnce = false;
    RTPInfo rtpInfo = null;
    protected Vector sinks;
    protected SlaveClock slaveClock;
    protected BasicSourceModule source;
    protected boolean started = false;
    boolean testedRTP = false;
    protected String timeBaseError = "  Cannot manage the different time bases.\n";
    private Time timeBeforeAbortPrefetch = null;
    protected BasicTrackControl[] trackControls = new BasicTrackControl[0];
    protected Track[] tracks;
    private boolean useMoreRenderBuffer = false;
    protected Vector waitEnded;
    protected Vector waitPrefetched;
    protected Vector waitResetted;
    protected Vector waitStopped;

    class BitRateA extends BitRateAdapter implements Owned {
        public BitRateA(int initialBitRate, int minBitRate, int maxBitRate, boolean settable) {
            super(initialBitRate, minBitRate, maxBitRate, settable);
        }

        public Component getControlComponent() {
            return null;
        }

        public Object getOwner() {
            return PlaybackEngine.this.player;
        }

        public int setBitRate(int rate) {
            this.value = rate;
            return this.value;
        }
    }

    class HeavyPanel extends Panel implements VisualContainer {
        public HeavyPanel(Vector visuals) {
        }
    }

    class LightPanel extends Container implements VisualContainer {
        public LightPanel(Vector visuals) {
        }
    }

    class PlayerGraphBuilder extends SimpleGraphBuilder {
        protected PlaybackEngine engine;

        PlayerGraphBuilder(PlaybackEngine engine) {
            this.engine = engine;
        }

        /* access modifiers changed from: protected */
        public GraphNode buildTrackFromGraph(BasicTrackControl tc, GraphNode node) {
            return this.engine.buildTrackFromGraph(tc, node);
        }
    }

    class PlayerTControl extends BasicTrackControl implements Owned {
        protected PlayerGraphBuilder gb;

        public PlayerTControl(PlaybackEngine engine, Track track, OutputConnector oc) {
            super(engine, track, oc);
        }

        public boolean buildTrack(int trackID, int numTracks) {
            if (this.gb == null) {
                this.gb = new PlayerGraphBuilder(this.engine);
            } else {
                this.gb.reset();
            }
            boolean rtn = this.gb.buildGraph((BasicTrackControl) this);
            this.gb = null;
            return rtn;
        }

        /* access modifiers changed from: protected */
        public FrameRateControl frameRateControl() {
            return PlaybackEngine.this.frameRateControl;
        }

        public Object getOwner() {
            return PlaybackEngine.this.player;
        }

        public boolean isTimeBase() {
            for (int j = 0; j < this.modules.size(); j++) {
                if (this.modules.elementAt(j) == PlaybackEngine.this.masterSink) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: protected */
        public ProgressControl progressControl() {
            return PlaybackEngine.this.progressControl;
        }
    }

    class SlaveClock implements Clock {
        BasicClock backup = new BasicClock();
        Clock current = this.backup;
        Clock master;

        SlaveClock() {
        }

        public long getMediaNanoseconds() {
            return this.current.getMediaNanoseconds();
        }

        public Time getMediaTime() {
            return this.current.getMediaTime();
        }

        public float getRate() {
            return this.current.getRate();
        }

        public Time getStopTime() {
            return this.backup.getStopTime();
        }

        public Time getSyncTime() {
            return this.current.getSyncTime();
        }

        public TimeBase getTimeBase() {
            return this.current.getTimeBase();
        }

        public Time mapToTimeBase(Time t) throws ClockStoppedException {
            return this.current.mapToTimeBase(t);
        }

        /* access modifiers changed from: protected */
        public void reset(boolean useMaster) {
            if (this.master == null || !useMaster) {
                if (this.master != null) {
                    synchronized (this.backup) {
                        boolean started = false;
                        if (this.backup.getState() == 1) {
                            this.backup.stop();
                            started = true;
                        }
                        this.backup.setMediaTime(this.master.getMediaTime());
                        if (started) {
                            this.backup.syncStart(this.backup.getTimeBase().getTime());
                        }
                    }
                }
                this.current = this.backup;
                return;
            }
            this.current = this.master;
        }

        public void setMaster(Clock master) {
            this.master = master;
            this.current = master == null ? this.backup : master;
            if (master != null) {
                try {
                    this.backup.setTimeBase(master.getTimeBase());
                } catch (IncompatibleTimeBaseException e) {
                }
            }
        }

        public void setMediaTime(Time now) {
            synchronized (this.backup) {
                if (this.backup.getState() == 1) {
                    this.backup.stop();
                    this.backup.setMediaTime(now);
                    this.backup.syncStart(this.backup.getTimeBase().getTime());
                } else {
                    this.backup.setMediaTime(now);
                }
            }
        }

        public float setRate(float factor) {
            return this.backup.setRate(factor);
        }

        public void setStopTime(Time t) {
            synchronized (this.backup) {
                this.backup.setStopTime(t);
            }
        }

        public void setTimeBase(TimeBase tb) throws IncompatibleTimeBaseException {
            synchronized (this.backup) {
                this.backup.setTimeBase(tb);
            }
        }

        public void stop() {
            synchronized (this.backup) {
                this.backup.stop();
            }
        }

        public void syncStart(Time tbt) {
            synchronized (this.backup) {
                if (this.backup.getState() != 1) {
                    this.backup.syncStart(tbt);
                }
            }
        }
    }

    public static void setMemoryTrace(boolean on) {
        TRACE_ON = on;
    }

    static boolean isRawVideo(Format fmt) {
        return (fmt instanceof RGBFormat) || (fmt instanceof YUVFormat);
    }

    static void profile(String msg, long time) {
        Log.profile("Profile: " + msg + ": " + (System.currentTimeMillis() - time) + " ms\n");
    }

    public PlaybackEngine(BasicPlayer p) {
        long initTime = System.currentTimeMillis();
        this.player = p;
        createProgressControl();
        SlaveClock slaveClock = new SlaveClock();
        this.slaveClock = slaveClock;
        setClock(slaveClock);
        this.stopThreadEnabled = false;
        profile("instantiation", initTime);
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void abortConfigure() {
        if (this.source != null) {
            this.source.abortRealize();
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void abortPrefetch() {
        this.timeBeforeAbortPrefetch = getMediaTime();
        doReset();
        int size = this.modules.size();
        for (int i = 0; i < size; i++) {
            ((StateTransistor) this.modules.elementAt(i)).abortPrefetch();
        }
        this.deallocated = true;
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void abortRealize() {
        int size = this.modules.size();
        for (int i = 0; i < size; i++) {
            ((StateTransistor) this.modules.elementAt(i)).abortRealize();
        }
    }

    public boolean audioEnabled() {
        int i = 0;
        while (i < this.trackControls.length) {
            if (this.trackControls[i].isEnabled() && (this.trackControls[i].getOriginalFormat() instanceof AudioFormat)) {
                return true;
            }
            i++;
        }
        return false;
    }

    public void bufferPrefetched(Module src) {
        if (this.prefetchEnabled && (src instanceof BasicSinkModule)) {
            synchronized (this.waitPrefetched) {
                if (this.waitPrefetched.contains(src)) {
                    this.waitPrefetched.removeElement(src);
                }
                if (this.waitPrefetched.isEmpty()) {
                    this.waitPrefetched.notifyAll();
                    if (!this.prefetchLogged) {
                        profile("prefetch", this.prefetchTime);
                        this.prefetchLogged = true;
                    }
                    if (!(getState() == Controller.Started || getTargetState() == Controller.Started)) {
                        this.source.pause();
                    }
                    this.prefetched = true;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public GraphNode buildTrackFromGraph(BasicTrackControl tc, GraphNode node) {
        BasicModule src = null;
        BasicModule dst = null;
        OutputConnector oc = null;
        boolean lastNode = true;
        Vector used = new Vector(5);
        if (node.plugin == null) {
            return null;
        }
        int i;
        InputConnector ic;
        int indent = 0 + 1;
        Log.setIndent(0);
        GraphNode node2 = node;
        while (node2 != null && node2.plugin != null) {
            src = createModule(node2, used);
            if (src == null) {
                Log.error("Internal error: buildTrackFromGraph");
                node2.failed = true;
                i = indent;
                node = node2;
                return node2;
            }
            if (lastNode) {
                if (src instanceof BasicRendererModule) {
                    tc.rendererModule = (BasicRendererModule) src;
                    if (this.useMoreRenderBuffer && (tc.rendererModule.getRenderer() instanceof AudioRenderer)) {
                        setRenderBufferSize(tc.rendererModule.getRenderer());
                    }
                } else if (src instanceof BasicFilterModule) {
                    tc.lastOC = src.getOutputConnector(null);
                    tc.lastOC.setFormat(node2.output);
                }
                lastNode = false;
            }
            ic = src.getInputConnector(null);
            ic.setFormat(node2.input);
            if (dst != null) {
                oc = src.getOutputConnector(null);
                ic = dst.getInputConnector(null);
                oc.setFormat(ic.getFormat());
            }
            src.setController(this);
            if (src.doRealize()) {
                if (!(oc == null || ic == null)) {
                    connectModules(oc, ic, dst);
                }
                dst = src;
                node2 = node2.prev;
            } else {
                i = indent - 1;
                Log.setIndent(indent);
                node2.failed = true;
                node = node2;
                return node2;
            }
        }
        dst = src;
        do {
            dst.setModuleListener(this);
            this.modules.addElement(dst);
            tc.modules.addElement(dst);
            if (dst instanceof BasicFilterModule) {
                this.filters.addElement(dst);
            } else if (dst instanceof BasicSinkModule) {
                this.sinks.addElement(dst);
            }
            oc = dst.getOutputConnector(null);
            if (oc == null) {
                break;
            }
            ic = oc.getInputConnector();
            if (ic == null) {
                break;
            }
            dst = (BasicModule) ic.getModule();
        } while (dst != null);
        tc.firstOC.setFormat(tc.getOriginalFormat());
        ic = src.getInputConnector(null);
        Format fmt = ic.getFormat();
        if (fmt == null || !fmt.equals(tc.getOriginalFormat())) {
            ic.setFormat(tc.getOriginalFormat());
        }
        connectModules(tc.firstOC, ic, src);
        i = indent - 1;
        Log.setIndent(indent);
        node = node2;
        return null;
    }

    /* access modifiers changed from: protected */
    public void connectModules(OutputConnector oc, InputConnector ic, BasicModule dst) {
        if (dst instanceof BasicRendererModule) {
            oc.setProtocol(ic.getProtocol());
        } else {
            ic.setProtocol(oc.getProtocol());
        }
        oc.connectTo(ic, ic.getFormat());
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Missing block: B:8:0x001b, code skipped:
            if (r2 == null) goto L_0x001d;
     */
    public net.sf.fmj.media.BasicModule createModule(net.sf.fmj.filtergraph.GraphNode r7, java.util.Vector r8) {
        /*
        r6 = this;
        r3 = 0;
        r5 = -1;
        r0 = 0;
        r4 = r7.plugin;
        if (r4 != 0) goto L_0x0009;
    L_0x0007:
        r1 = r0;
    L_0x0008:
        return r3;
    L_0x0009:
        r4 = r7.plugin;
        r4 = r8.contains(r4);
        if (r4 == 0) goto L_0x0037;
    L_0x0011:
        r4 = r7.cname;
        if (r4 == 0) goto L_0x001d;
    L_0x0015:
        r4 = r7.cname;
        r2 = net.sf.fmj.filtergraph.SimpleGraphBuilder.createPlugIn(r4, r5);
        if (r2 != 0) goto L_0x003c;
    L_0x001d:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = "Failed to instantiate ";
        r4 = r4.append(r5);
        r5 = r7.cname;
        r4 = r4.append(r5);
        r4 = r4.toString();
        net.sf.fmj.media.Log.write(r4);
        r1 = r0;
        goto L_0x0008;
    L_0x0037:
        r2 = r7.plugin;
        r8.addElement(r2);
    L_0x003c:
        r3 = r7.type;
        if (r3 == r5) goto L_0x0045;
    L_0x0040:
        r3 = r7.type;
        r4 = 4;
        if (r3 != r4) goto L_0x0053;
    L_0x0045:
        r3 = r2 instanceof javax.media.Renderer;
        if (r3 == 0) goto L_0x0053;
    L_0x0049:
        r0 = new net.sf.fmj.media.BasicRendererModule;
        r2 = (javax.media.Renderer) r2;
        r0.m462init(r2);
    L_0x0050:
        r1 = r0;
        r3 = r0;
        goto L_0x0008;
    L_0x0053:
        r3 = r7.type;
        if (r3 == r5) goto L_0x005c;
    L_0x0057:
        r3 = r7.type;
        r4 = 2;
        if (r3 != r4) goto L_0x0050;
    L_0x005c:
        r3 = r2 instanceof javax.media.Codec;
        if (r3 == 0) goto L_0x0050;
    L_0x0060:
        r0 = new net.sf.fmj.media.BasicFilterModule;
        r2 = (javax.media.Codec) r2;
        r0.m454init(r2);
        goto L_0x0050;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.PlaybackEngine.createModule(net.sf.fmj.filtergraph.GraphNode, java.util.Vector):net.sf.fmj.media.BasicModule");
    }

    public void createProgressControl() {
        StringControl frameRate = new StringControlAdapter();
        frameRate.setValue(" N/A");
        StringControl bitRate = new StringControlAdapter();
        bitRate.setValue(" N/A");
        StringControl videoProps = new StringControlAdapter();
        videoProps.setValue(" N/A");
        StringControl audioProps = new StringControlAdapter();
        audioProps.setValue(" N/A");
        StringControl audioCodec = new StringControlAdapter();
        audioCodec.setValue(" N/A");
        StringControl videoCodec = new StringControlAdapter();
        videoCodec.setValue(" N/A");
        this.progressControl = new ProgressControlAdapter(frameRate, bitRate, videoProps, audioProps, videoCodec, audioCodec);
    }

    /* access modifiers changed from: protected */
    public Component createVisualContainer(Vector visuals) {
        Boolean hint = (Boolean) Manager.getHint(3);
        if (this.container == null) {
            if (hint == null || !hint.booleanValue()) {
                this.container = new HeavyPanel(visuals);
            } else {
                this.container = new LightPanel(visuals);
            }
            this.container.setLayout(new FlowLayout());
            this.container.setBackground(Color.black);
            for (int i = 0; i < visuals.size(); i++) {
                Component c = (Component) visuals.elementAt(i);
                this.container.add(c);
                c.setSize(c.getPreferredSize());
            }
        }
        return this.container;
    }

    public void dataBlocked(Module src, boolean blocked) {
        this.dataPathBlocked = blocked;
        if (blocked) {
            resetPrefetchedList();
            resetResettedList();
        }
        if (getTargetState() == Controller.Started) {
            if (blocked) {
                localStop();
                setTargetState(Controller.Started);
                sendEvent(new RestartingEvent(this, Controller.Started, Controller.Prefetching, Controller.Started, getMediaTime()));
                return;
            }
            sendEvent(new StartEvent(this, 500, Controller.Started, Controller.Started, getMediaTime(), getTimeBase().getTime()));
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void doClose() {
        if (this.modules != null) {
            if (getState() == Controller.Started) {
                localStop();
            }
            if (getState() == 500) {
                doReset();
            }
            int size = this.modules.size();
            for (int i = 0; i < size; i++) {
                ((StateTransistor) this.modules.elementAt(i)).doClose();
            }
            if (needSavingDB) {
                Resource.saveDB();
                needSavingDB = false;
            }
        } else if (this.source != null) {
            this.source.doClose();
        }
    }

    /* access modifiers changed from: protected */
    public boolean doConfigure() {
        if (!doConfigure1()) {
            return false;
        }
        String[] names = this.source.getOutputConnectorNames();
        this.trackControls = new BasicTrackControl[this.tracks.length];
        for (int i = 0; i < this.tracks.length; i++) {
            this.trackControls[i] = new PlayerTControl(this, this.tracks[i], this.source.getOutputConnector(names[i]));
        }
        return doConfigure2();
    }

    /* access modifiers changed from: protected */
    public boolean doConfigure1() {
        long parsingTime = System.currentTimeMillis();
        this.modules = new Vector();
        this.filters = new Vector();
        this.sinks = new Vector();
        this.waitPrefetched = new Vector();
        this.waitStopped = new Vector();
        this.waitEnded = new Vector();
        this.waitResetted = new Vector();
        this.source.setModuleListener(this);
        this.source.setController(this);
        this.modules.addElement(this.source);
        if (!this.source.doRealize()) {
            Log.error(this.configError);
            if (this.source.errMsg != null) {
                Log.error("  " + this.source.errMsg + "\n");
            }
            this.player.processError = this.parseError;
            return false;
        } else if (isInterrupted()) {
            Log.error(this.configError);
            Log.error(this.configIntError);
            this.player.processError = this.configInt2Error;
            return false;
        } else {
            Demultiplexer demultiplexer = this.source.getDemultiplexer();
            this.parser = demultiplexer;
            if (demultiplexer == null) {
                Log.error(this.configError);
                Log.error("  Cannot obtain demultiplexer for the source.\n");
                this.player.processError = this.parseError;
                return false;
            }
            try {
                this.tracks = this.parser.getTracks();
                if (isInterrupted()) {
                    Log.error(this.configError);
                    Log.error(this.configIntError);
                    this.player.processError = this.configInt2Error;
                    return false;
                }
                profile("parsing", parsingTime);
                return true;
            } catch (Exception e) {
                Log.error(this.configError);
                Log.error("  Cannot obtain tracks from the demultiplexer: " + e + "\n");
                this.player.processError = this.parseError;
                return false;
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean doConfigure2() {
        if (this.parser.isPositionable() && this.parser.isRandomAccess()) {
            Track master = FramePositioningAdapter.getMasterTrack(this.tracks);
            if (master != null) {
                this.framePositioningControl = new FramePositioningAdapter(this.player, master);
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void doDeallocate() {
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void doFailedPrefetch() {
        int size = this.modules.size();
        for (int i = 0; i < size; i++) {
            ((StateTransistor) this.modules.elementAt(i)).doFailedPrefetch();
        }
        super.doFailedPrefetch();
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void doFailedRealize() {
        int size = this.modules.size();
        for (int i = 0; i < size; i++) {
            ((StateTransistor) this.modules.elementAt(i)).doFailedRealize();
        }
        super.doFailedRealize();
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized boolean doPrefetch() {
        boolean z = true;
        synchronized (this) {
            if (!this.prefetched) {
                if (!(doPrefetch1() && doPrefetch2())) {
                    z = false;
                }
            }
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public boolean doPrefetch1() {
        if (this.timeBeforeAbortPrefetch != null) {
            doSetMediaTime(this.timeBeforeAbortPrefetch);
            this.timeBeforeAbortPrefetch = null;
        }
        this.prefetchTime = System.currentTimeMillis();
        resetPrefetchedList();
        if (this.source.doPrefetch()) {
            boolean atLeastOneTrack = false;
            int i = 0;
            while (i < this.trackControls.length) {
                boolean usedToFailed = this.trackControls[i].prefetchFailed;
                if (!usedToFailed || getState() <= Controller.Prefetching) {
                    if (this.trackControls[i].prefetchTrack()) {
                        atLeastOneTrack = true;
                        if (!usedToFailed) {
                            continue;
                        } else if (manageTimeBases()) {
                            doSetMediaTime(getMediaTime());
                        } else {
                            Log.error(this.prefetchError);
                            Log.error(this.timeBaseError);
                            return false;
                        }
                    }
                    this.trackControls[i].prError();
                    if (this.trackControls[i].isTimeBase() && !manageTimeBases()) {
                        Log.error(this.prefetchError);
                        Log.error(this.timeBaseError);
                        this.player.processError = this.timeBaseError;
                        return false;
                    } else if ((this.trackControls[i].getFormat() instanceof AudioFormat) && this.trackControls[i].rendererFailed) {
                        this.player.processError = "cannot open the audio device.";
                    }
                }
                i++;
            }
            if (atLeastOneTrack) {
                this.player.processError = null;
                return true;
            }
            Log.error(this.prefetchError);
            return false;
        }
        Log.error(this.prefetchError);
        if (this.dsource == null) {
            return false;
        }
        Log.error("  Cannot prefetch the source: " + this.dsource.getLocator() + "\n");
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean doPrefetch2() {
        if (this.prefetchEnabled) {
            synchronized (this.waitPrefetched) {
                this.source.doStart();
                try {
                    if (!this.waitPrefetched.isEmpty()) {
                        this.waitPrefetched.wait(3000);
                    }
                } catch (InterruptedException e) {
                }
            }
        } else {
            this.prefetched = true;
        }
        this.deallocated = false;
        return true;
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized boolean doRealize() {
        boolean z;
        z = doRealize1() && doRealize2();
        return z;
    }

    /* access modifiers changed from: protected */
    public boolean doRealize1() {
        Log.comment("Building flow graph for: " + this.dsource.getLocator() + "\n");
        this.realizeTime = System.currentTimeMillis();
        boolean atLeastOneTrack = false;
        int trackID = 0;
        int numTracks = getNumTracks();
        for (int i = 0; i < this.trackControls.length; i++) {
            if (this.trackControls[i].isEnabled()) {
                Log.setIndent(0);
                Log.comment("Building Track: " + i);
                if (this.trackControls[i].buildTrack(trackID, numTracks)) {
                    atLeastOneTrack = true;
                    this.trackControls[i].setEnabled(true);
                } else if (this.trackControls[i].isCustomized()) {
                    Log.error(this.realizeError);
                    this.trackControls[i].prError();
                    this.player.processError = this.genericProcessorError;
                    return false;
                } else {
                    this.trackControls[i].setEnabled(false);
                    Log.warning("Failed to handle track " + i);
                    this.trackControls[i].prError();
                }
                if (isInterrupted()) {
                    Log.error(this.realizeError);
                    Log.error("  The graph building process is being interrupted.\n");
                    this.player.processError = "interrupted while the player is being constructed.";
                    return false;
                }
                trackID++;
                Log.write("\n");
            }
        }
        if (atLeastOneTrack) {
            return true;
        }
        Log.error(this.realizeError);
        this.player.processError = "input media not supported: " + getCodecList();
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean doRealize2() {
        if (manageTimeBases()) {
            Log.comment("Here's the completed flow graph:");
            traceGraph(this.source);
            Log.write("\n");
            profile("graph building", this.realizeTime);
            this.realizeTime = System.currentTimeMillis();
            updateFormats();
            profile("realize, post graph building", this.realizeTime);
            return true;
        }
        Log.error(this.realizeError);
        Log.error(this.timeBaseError);
        this.player.processError = this.timeBaseError;
        return false;
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void doReset() {
        synchronized (this.waitResetted) {
            int i;
            BasicSinkModule bsm;
            resetResettedList();
            for (i = this.modules.size() - 1; i >= 0; i--) {
                BasicModule m = (BasicModule) this.modules.elementAt(i);
                if (!m.prefetchFailed()) {
                    m.reset();
                }
            }
            int size = this.sinks.size();
            for (i = 0; i < size; i++) {
                bsm = (BasicSinkModule) this.sinks.elementAt(i);
                if (!bsm.prefetchFailed()) {
                    bsm.triggerReset();
                }
            }
            if (!this.waitResetted.isEmpty()) {
                try {
                    this.waitResetted.wait(3000);
                } catch (Exception e) {
                }
            }
            size = this.sinks.size();
            for (i = 0; i < size; i++) {
                bsm = (BasicSinkModule) this.sinks.elementAt(i);
                if (!bsm.prefetchFailed()) {
                    bsm.doneReset();
                }
            }
        }
        this.prefetched = false;
    }

    /* access modifiers changed from: protected */
    public void doSetMediaTime(Time when) {
        this.slaveClock.setMediaTime(when);
        Time t = this.source.setPosition(when, 0);
        if (t == null) {
            t = when;
        }
        int size = this.sinks.size();
        for (int i = 0; i < size; i++) {
            BasicSinkModule bsm = (BasicSinkModule) this.sinks.elementAt(i);
            bsm.doSetMediaTime(when);
            bsm.setPreroll(when.getNanoseconds(), t.getNanoseconds());
        }
    }

    public synchronized float doSetRate(float r) {
        float f;
        float r2;
        if (r <= 0.0f) {
            r2 = 1.0f;
        } else {
            r2 = r;
        }
        try {
            if (r2 == this.rate) {
                r = r2;
            } else {
                if (this.masterSink == null) {
                    r2 = getClock().setRate(r2);
                } else {
                    r2 = this.masterSink.doSetRate(r2);
                }
                int size = this.modules.size();
                for (int i = 0; i < size; i++) {
                    BasicModule m = (BasicModule) this.modules.elementAt(i);
                    if (m != this.masterSink) {
                        m.doSetRate(r2);
                    }
                }
                this.rate = r2;
                r = r2;
                f = r2;
            }
        } finally {
            r = r2;
        }
        return f;
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void doStart() {
        if (!this.started) {
            doStart1();
            doStart2();
        }
    }

    /* access modifiers changed from: protected */
    public void doStart1() {
        if ((this.dsource instanceof CaptureDevice) && !isRTP()) {
            reset();
        }
        resetPrefetchedList();
        resetStoppedList();
        resetEndedList();
        for (int i = 0; i < this.trackControls.length; i++) {
            if (this.trackControls[i].isEnabled()) {
                this.trackControls[i].startTrack();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void doStart2() {
        this.source.doStart();
        this.started = true;
        this.prefetched = true;
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void doStop() {
        if (this.started) {
            doStop1();
            doStop2();
        }
    }

    /* access modifiers changed from: protected */
    public void doStop1() {
        resetPrefetchedList();
        this.source.doStop();
        for (int i = 0; i < this.trackControls.length; i++) {
            if (this.trackControls[i].isEnabled()) {
                this.trackControls[i].stopTrack();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void doStop2() {
        if (!this.prefetchEnabled) {
            this.source.pause();
        }
        this.started = false;
    }

    /* access modifiers changed from: protected */
    public BasicSinkModule findMasterSink() {
        int i = 0;
        while (i < this.trackControls.length) {
            if (this.trackControls[i].isEnabled() && this.trackControls[i].rendererModule != null && this.trackControls[i].rendererModule.getClock() != null) {
                return this.trackControls[i].rendererModule;
            }
            i++;
        }
        return null;
    }

    public void formatChanged(Module src, Format oldFormat, Format newFormat) {
        Log.comment(src + ": input format changed: " + newFormat);
        if ((src instanceof BasicRendererModule) && (oldFormat instanceof VideoFormat) && (newFormat instanceof VideoFormat)) {
            Dimension s1 = ((VideoFormat) oldFormat).getSize();
            Dimension s2 = ((VideoFormat) newFormat).getSize();
            if (s2 == null) {
                return;
            }
            if (s1 == null || !s1.equals(s2)) {
                sendEvent(new SizeChangeEvent(this, s2.width, s2.height, 1.0f));
            }
        }
    }

    public void formatChangedFailure(Module src, Format oldFormat, Format newFormat) {
        if (!this.internalErrorOccurred) {
            sendEvent(new InternalErrorEvent(this, "Internal module " + src + ": failed to handle a data format change!"));
            this.internalErrorOccurred = true;
            close();
        }
    }

    public void framesBehind(Module src, float frames, InputConnector ic) {
        while (ic != null) {
            OutputConnector oc = ic.getOutputConnector();
            if (oc != null) {
                src = oc.getModule();
                if (src != null && (src instanceof BasicFilterModule)) {
                    ((BasicFilterModule) src).setFramesBehind(frames);
                    ic = src.getInputConnector(null);
                } else {
                    return;
                }
            }
            return;
        }
    }

    /* access modifiers changed from: protected */
    public long getBitRate() {
        return this.source.getBitsRead();
    }

    public String getCNAME() {
        if (this.rtpInfo == null) {
            RTPInfo rTPInfo = (RTPInfo) this.dsource.getControl(RTPInfo.class.getName());
            this.rtpInfo = rTPInfo;
            if (rTPInfo == null) {
                return null;
            }
        }
        return this.rtpInfo.getCNAME();
    }

    /* access modifiers changed from: 0000 */
    public String getCodecList() {
        String list = "";
        for (int i = 0; i < this.trackControls.length; i++) {
            Format fmt = this.trackControls[i].getOriginalFormat();
            if (!(fmt == null || fmt.getEncoding() == null)) {
                list = list + fmt.getEncoding();
                if (fmt instanceof VideoFormat) {
                    list = list + " video";
                } else if (fmt instanceof AudioFormat) {
                    list = list + " audio";
                }
                if (i + 1 < this.trackControls.length) {
                    list = list + ", ";
                }
            }
        }
        return list;
    }

    public Control[] getControls() {
        int i;
        int size;
        Vector cv = new Vector();
        int size2 = this.modules == null ? 0 : this.modules.size();
        int otherSize = 0;
        for (i = 0; i < size2; i++) {
            Object[] cs = ((Module) this.modules.elementAt(i)).getControls();
            if (cs != null) {
                for (Object addElement : cs) {
                    cv.addElement(addElement);
                }
            }
        }
        size2 = cv.size();
        if (videoEnabled() && this.frameRateControl == null) {
            this.frameRateControl = new FrameRateAdapter(this.player, 0.0f, 0.0f, 30.0f, false) {
                public Component getControlComponent() {
                    return null;
                }

                public Object getOwner() {
                    return PlaybackEngine.this.player;
                }

                public float setFrameRate(float rate) {
                    this.value = rate;
                    return -1.0f;
                }
            };
        }
        if (this.bitRateControl == null) {
            this.bitRateControl = new BitRateA(0, -1, -1, false);
        }
        if (this.frameRateControl != null) {
            otherSize = 0 + 1;
        }
        if (this.bitRateControl != null) {
            otherSize++;
        }
        if (this.framePositioningControl != null) {
            otherSize++;
        }
        Control[] controls = new Control[((size2 + otherSize) + this.trackControls.length)];
        for (i = 0; i < size2; i++) {
            controls[i] = (Control) cv.elementAt(i);
        }
        if (this.bitRateControl != null) {
            size = size2 + 1;
            controls[size2] = this.bitRateControl;
            size2 = size;
        }
        if (this.frameRateControl != null) {
            size = size2 + 1;
            controls[size2] = this.frameRateControl;
            size2 = size;
        }
        if (this.framePositioningControl != null) {
            size = size2 + 1;
            controls[size2] = this.framePositioningControl;
            size2 = size;
        }
        for (i = 0; i < this.trackControls.length; i++) {
            controls[size2 + i] = this.trackControls[i];
        }
        return controls;
    }

    public Time getDuration() {
        return this.source.getDuration();
    }

    public GainControl getGainControl() {
        return (GainControl) getControl("javax.media.GainControl");
    }

    public long getLatency() {
        return this.latency;
    }

    /* access modifiers changed from: 0000 */
    public int getNumTracks() {
        int num = 0;
        for (BasicTrackControl isEnabled : this.trackControls) {
            if (isEnabled.isEnabled()) {
                num++;
            }
        }
        return num;
    }

    /* access modifiers changed from: protected */
    public PlugIn getPlugIn(BasicModule m) {
        if (m instanceof BasicSourceModule) {
            return ((BasicSourceModule) m).getDemultiplexer();
        }
        if (m instanceof BasicFilterModule) {
            return ((BasicFilterModule) m).getCodec();
        }
        if (m instanceof BasicRendererModule) {
            return ((BasicRendererModule) m).getRenderer();
        }
        return null;
    }

    public Time getStartLatency() {
        if (this.state == 100 || this.state == 200) {
            throwError(new NotRealizedError("Cannot get start latency from an unrealized controller"));
        }
        return LATENCY_UNKNOWN;
    }

    public TimeBase getTimeBase() {
        return getClock().getTimeBase();
    }

    public Component getVisualComponent() {
        Vector visuals = new Vector(1);
        if (this.modules == null) {
            return null;
        }
        for (int i = 0; i < this.modules.size(); i++) {
            PlugIn pi = getPlugIn((BasicModule) this.modules.elementAt(i));
            if (pi instanceof VideoRenderer) {
                Component comp = ((VideoRenderer) pi).getComponent();
                if (comp != null) {
                    visuals.addElement(comp);
                }
            }
        }
        if (visuals.size() == 0) {
            return null;
        }
        if (visuals.size() == 1) {
            return (Component) visuals.elementAt(0);
        }
        return createVisualContainer(visuals);
    }

    public void internalErrorOccurred(Module src) {
        if (!this.internalErrorOccurred) {
            sendEvent(new InternalErrorEvent(this, "Internal module " + src + " failed!"));
            this.internalErrorOccurred = true;
            close();
        }
    }

    /* access modifiers changed from: protected */
    public boolean isConfigurable() {
        return true;
    }

    public boolean isRTP() {
        boolean z = true;
        if (this.testedRTP) {
            return this.rtpInfo != null;
        } else {
            this.rtpInfo = (RTPInfo) this.dsource.getControl(RTPInfo.class.getName());
            this.testedRTP = true;
            if (this.rtpInfo == null) {
                z = false;
            }
            return z;
        }
    }

    /* access modifiers changed from: protected */
    public BasicModule lastModule(BasicModule bm) {
        OutputConnector oc = bm.getOutputConnector(null);
        while (oc != null) {
            InputConnector ic = oc.getInputConnector();
            if (ic == null) {
                break;
            }
            bm = (BasicModule) ic.getModule();
            oc = bm.getOutputConnector(null);
        }
        return bm;
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void localStop() {
        super.stop();
    }

    /* access modifiers changed from: 0000 */
    public boolean manageTimeBases() {
        this.masterSink = findMasterSink();
        return updateMasterTimeBase();
    }

    public void markedDataArrived(Module src, Buffer buffer) {
        if (src instanceof BasicSourceModule) {
            this.markedDataStartTime = getMediaNanoseconds();
            return;
        }
        long t = getMediaNanoseconds() - this.markedDataStartTime;
        if (t > 0 && t < 1000000000) {
            if (!this.reportOnce) {
                Log.comment("Computed latency for video: " + (t / TimeSource.MICROS_PER_SEC) + " ms\n");
                this.reportOnce = true;
            }
            this.latency = (this.latency + t) / 2;
        }
    }

    public void mediaEnded(Module src) {
        if (src instanceof BasicSinkModule) {
            synchronized (this.waitEnded) {
                if (this.waitEnded.contains(src)) {
                    this.waitEnded.removeElement(src);
                }
                if (this.waitEnded.isEmpty()) {
                    this.started = false;
                    stopControllerOnly();
                    sendEvent(new EndOfMediaEvent(this, Controller.Started, 500, getTargetState(), getMediaTime()));
                    this.slaveClock.reset(USE_MASTER);
                } else if (src == this.masterSink) {
                    this.slaveClock.reset(USE_BACKUP);
                }
            }
        }
    }

    public void pluginTerminated(Module src) {
        if (!this.internalErrorOccurred) {
            sendEvent(new ControllerClosedEvent(this));
            this.internalErrorOccurred = true;
            close();
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void reset() {
        if (!(this.started || !this.prefetched || this.dataPathBlocked)) {
            doReset();
        }
    }

    /* access modifiers changed from: protected */
    public void resetBitRate() {
        this.source.resetBitsRead();
    }

    private void resetEndedList() {
        synchronized (this.waitEnded) {
            this.waitEnded.removeAllElements();
            int size = this.sinks.size();
            for (int i = 0; i < size; i++) {
                BasicSinkModule bsm = (BasicSinkModule) this.sinks.elementAt(i);
                if (!bsm.prefetchFailed()) {
                    this.waitEnded.addElement(bsm);
                }
            }
            this.waitEnded.notifyAll();
        }
    }

    private void resetPrefetchedList() {
        synchronized (this.waitPrefetched) {
            this.waitPrefetched.removeAllElements();
            int size = this.sinks.size();
            for (int i = 0; i < size; i++) {
                BasicSinkModule bsm = (BasicSinkModule) this.sinks.elementAt(i);
                if (!bsm.prefetchFailed()) {
                    this.waitPrefetched.addElement(bsm);
                }
            }
            this.waitPrefetched.notifyAll();
        }
    }

    private void resetResettedList() {
        synchronized (this.waitResetted) {
            this.waitResetted.removeAllElements();
            int size = this.sinks.size();
            for (int i = 0; i < size; i++) {
                BasicSinkModule bsm = (BasicSinkModule) this.sinks.elementAt(i);
                if (!bsm.prefetchFailed()) {
                    this.waitResetted.addElement(bsm);
                }
            }
            this.waitResetted.notifyAll();
        }
    }

    private void resetStoppedList() {
        synchronized (this.waitStopped) {
            this.waitStopped.removeAllElements();
            int size = this.sinks.size();
            for (int i = 0; i < size; i++) {
                BasicSinkModule bsm = (BasicSinkModule) this.sinks.elementAt(i);
                if (!bsm.prefetchFailed()) {
                    this.waitStopped.addElement(bsm);
                }
            }
            this.waitStopped.notifyAll();
        }
    }

    public void resetted(Module src) {
        synchronized (this.waitResetted) {
            if (this.waitResetted.contains(src)) {
                this.waitResetted.removeElement(src);
            }
            if (this.waitResetted.isEmpty()) {
                this.waitResetted.notifyAll();
            }
        }
    }

    public synchronized void setMediaTime(Time when) {
        if (this.state < Controller.Realized) {
            throwError(new NotRealizedError("Cannot set media time on a unrealized controller"));
        }
        if (when.getNanoseconds() != getMediaNanoseconds()) {
            reset();
            this.timeBeforeAbortPrefetch = null;
            doSetMediaTime(when);
            doPrefetch();
            sendEvent(new MediaTimeSetEvent(this, when));
        }
    }

    public void setProgressControl(ProgressControl p) {
        this.progressControl = p;
    }

    /* access modifiers changed from: protected */
    public void setRenderBufferSize(Renderer r) {
        BufferControl bc = (BufferControl) r.getControl(BufferControl.class.getName());
        if (bc != null) {
            bc.setBufferLength(2000);
        }
    }

    public void setSource(DataSource ds) throws IOException, IncompatibleSourceException {
        try {
            this.source = BasicSourceModule.createModule(ds);
            if (this.source == null) {
                throw new IncompatibleSourceException();
            }
            this.source.setController(this);
            this.dsource = ds;
            if ((this.dsource instanceof Streamable) && !((Streamable) this.dsource).isPrefetchable()) {
                this.prefetchEnabled = false;
                this.dataPathBlocked = true;
            }
            if (this.dsource instanceof CaptureDevice) {
                this.prefetchEnabled = false;
            }
        } catch (IOException ioe) {
            Log.warning("Input DataSource: " + ds);
            Log.warning("  Failed with IO exception: " + ioe.getMessage());
            throw ioe;
        } catch (IncompatibleSourceException ise) {
            Log.warning("Input DataSource: " + ds);
            Log.warning("  is not compatible with the MediaEngine.");
            Log.warning("  It's likely that the DataSource is required to extend PullDataSource;");
            Log.warning("  and that its source streams implement the Seekable interface ");
            Log.warning("  and with random access capability.");
            throw ise;
        }
    }

    public void setStopTime(Time t) {
        if (getState() < Controller.Realized) {
            throwError(new NotRealizedError("Cannot set stop time on an unrealized controller."));
        }
        if (!(getStopTime() == null || getStopTime().getNanoseconds() == t.getNanoseconds())) {
            sendEvent(new StopTimeChangeEvent(this, t));
        }
        if (getState() != Controller.Started || t == Clock.RESET || t.getNanoseconds() >= getMediaNanoseconds()) {
            getClock().setStopTime(t);
            int size = this.sinks.size();
            for (int i = 0; i < size; i++) {
                ((BasicSinkModule) this.sinks.elementAt(i)).setStopTime(t);
            }
            return;
        }
        localStop();
        setStopTime(Clock.RESET);
        sendEvent(new StopAtTimeEvent(this, getState(), 500, getTargetState(), getMediaTime()));
    }

    public void setTimeBase(TimeBase tb) throws IncompatibleTimeBaseException {
        getClock().setTimeBase(tb);
        if (this.sinks != null) {
            int size = this.sinks.size();
            for (int i = 0; i < size; i++) {
                ((BasicSinkModule) this.sinks.elementAt(i)).setTimeBase(tb);
            }
        }
    }

    public synchronized void stop() {
        super.stop();
        sendEvent(new StopByRequestEvent(this, Controller.Started, 500, getTargetState(), getMediaTime()));
    }

    public void stopAtTime(Module src) {
        if (src instanceof BasicSinkModule) {
            synchronized (this.waitStopped) {
                if (this.waitStopped.contains(src)) {
                    this.waitStopped.removeElement(src);
                }
                if (this.waitStopped.isEmpty() || (this.waitEnded.size() == 1 && this.waitEnded.contains(src))) {
                    this.started = false;
                    stopControllerOnly();
                    setStopTime(Clock.RESET);
                    sendEvent(new StopAtTimeEvent(this, Controller.Started, 500, getTargetState(), getMediaTime()));
                    this.slaveClock.reset(USE_MASTER);
                } else if (src == this.masterSink) {
                    this.slaveClock.reset(USE_BACKUP);
                }
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void traceGraph(BasicModule source) {
        String[] names = source.getOutputConnectorNames();
        for (String outputConnector : names) {
            OutputConnector oc = source.getOutputConnector(outputConnector);
            InputConnector ic = oc.getInputConnector();
            if (ic != null) {
                Module m = ic.getModule();
                if (m != null) {
                    Log.write("  " + getPlugIn(source));
                    Log.write("     connects to: " + getPlugIn((BasicModule) m));
                    Log.write("     format: " + oc.getFormat());
                    traceGraph((BasicModule) m);
                }
            }
        }
    }

    public void updateFormats() {
        for (BasicTrackControl updateFormat : this.trackControls) {
            updateFormat.updateFormat();
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean updateMasterTimeBase() {
        int size = this.sinks.size();
        if (this.masterSink != null) {
            this.slaveClock.setMaster(this.masterSink.getClock());
        } else {
            this.slaveClock.setMaster(null);
        }
        for (int i = 0; i < size; i++) {
            BasicSinkModule bsm = (BasicSinkModule) this.sinks.elementAt(i);
            if (!(bsm == this.masterSink || bsm.prefetchFailed())) {
                try {
                    bsm.setTimeBase(this.slaveClock.getTimeBase());
                } catch (IncompatibleTimeBaseException e) {
                    return false;
                }
            }
        }
        return true;
    }

    public void updateRates() {
        if (getState() >= Controller.Realized) {
            long rate;
            long now = System.currentTimeMillis();
            if (now == this.lastStatsTime) {
                rate = this.lastBitRate;
            } else {
                rate = (long) (((((double) getBitRate()) * 8.0d) / ((double) (now - this.lastStatsTime))) * 1000.0d);
            }
            long avg = (this.lastBitRate + rate) / 2;
            if (this.bitRateControl != null) {
                this.bitRateControl.setBitRate((int) avg);
            }
            this.lastBitRate = rate;
            this.lastStatsTime = now;
            resetBitRate();
            for (BasicTrackControl updateRates : this.trackControls) {
                updateRates.updateRates(now);
            }
            this.source.checkLatency();
        }
    }

    public boolean videoEnabled() {
        int i = 0;
        while (i < this.trackControls.length) {
            if (this.trackControls[i].isEnabled() && (this.trackControls[i].getOriginalFormat() instanceof VideoFormat)) {
                return true;
            }
            i++;
        }
        return false;
    }
}
