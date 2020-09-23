package net.sf.fmj.media;

import javax.media.Buffer;
import javax.media.Clock;
import javax.media.Drainable;
import javax.media.Format;
import javax.media.Multiplexer;
import javax.media.Prefetchable;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.DataSource;
import net.sf.fmj.ejmf.toolkit.util.TimeSource;
import net.sf.fmj.media.util.ElapseTime;

public class BasicMuxModule extends BasicSinkModule {
    public static String ConnectorNamePrefix = "input";
    static AudioFormat mpegAudio = new AudioFormat(AudioFormat.MPEG_RTP);
    private long bitsWritten = 0;
    private boolean closed = false;
    protected ElapseTime[] elapseTime;
    protected boolean[] endMarkers;
    private boolean failed = false;
    private VideoFormat firstVideoFormat = null;
    private float frameRate = 30.0f;
    private int framesPlayed = 0;
    protected InputConnector[] ics;
    protected Format[] inputs;
    private float lastFramesBehind = -1.0f;
    protected Multiplexer multiplexer;
    private Object[] pauseSync;
    protected boolean[] paused;
    protected boolean[] prefetchMarkers;
    private Object prefetchSync = new Object();
    protected boolean prefetching = false;
    protected boolean[] prerollTrack;
    protected boolean[] resettedMarkers;
    private VideoFormat rtpVideoFormat = null;
    protected boolean started = false;
    protected boolean[] stopAtTimeMarkers;

    class MyInputConnector extends BasicInputConnector {
        public String toString() {
            return super.toString() + ": " + getFormat();
        }
    }

    protected BasicMuxModule(Multiplexer m, Format[] inputs) {
        this.multiplexer = m;
        if (inputs != null) {
            this.ics = new InputConnector[inputs.length];
            for (int i = 0; i < inputs.length; i++) {
                InputConnector ic = new MyInputConnector();
                ic.setSize(1);
                ic.setModule(this);
                registerInputConnector(ConnectorNamePrefix + i, ic);
                this.ics[i] = ic;
                if ((inputs[i] instanceof VideoFormat) && this.firstVideoFormat == null) {
                    this.firstVideoFormat = (VideoFormat) inputs[i];
                    if (inputs[i].getEncoding().toUpperCase().endsWith("RTP")) {
                        this.rtpVideoFormat = this.firstVideoFormat;
                    }
                }
            }
            this.inputs = inputs;
        }
        if (this.multiplexer != null && (this.multiplexer instanceof Clock)) {
            setClock((Clock) this.multiplexer);
        }
        setProtocol(0);
    }

    public void abortPrefetch() {
        this.prefetching = false;
    }

    /* access modifiers changed from: 0000 */
    public boolean checkEnd(int idx) {
        boolean z = true;
        synchronized (this.endMarkers) {
            this.endMarkers[idx] = true;
            for (boolean z2 : this.endMarkers) {
                if (!z2) {
                    z = false;
                    break;
                }
            }
        }
        return z;
    }

    /* access modifiers changed from: 0000 */
    public boolean checkPrefetch(int idx) {
        boolean z = true;
        synchronized (this.prefetchMarkers) {
            this.prefetchMarkers[idx] = true;
            for (boolean z2 : this.prefetchMarkers) {
                if (!z2) {
                    z = false;
                    break;
                }
            }
        }
        return z;
    }

    /* access modifiers changed from: 0000 */
    public boolean checkResetted(int idx) {
        boolean z = true;
        synchronized (this.resettedMarkers) {
            this.resettedMarkers[idx] = true;
            for (boolean z2 : this.resettedMarkers) {
                if (!z2) {
                    z = false;
                    break;
                }
            }
        }
        return z;
    }

    /* access modifiers changed from: 0000 */
    public boolean checkStopAtTime(int idx) {
        boolean z = true;
        synchronized (this.stopAtTimeMarkers) {
            this.stopAtTimeMarkers[idx] = true;
            for (boolean z2 : this.stopAtTimeMarkers) {
                if (!z2) {
                    z = false;
                    break;
                }
            }
        }
        return z;
    }

    public void connectorPushed(InputConnector ic) {
        Buffer buffer;
        int flags;
        int rc;
        int idx = -1;
        if (this.ics[0] == ic) {
            idx = 0;
        } else if (this.ics[1] == ic) {
            idx = 1;
        } else {
            for (int i = 2; i < this.ics.length; i++) {
                if (this.ics[i] == ic) {
                    idx = i;
                    break;
                }
            }
            if (idx == -1) {
                throw new RuntimeException("BasicMuxModule: unmatched input connector!");
            }
        }
        while (true) {
            if (this.paused[idx]) {
                synchronized (this.pauseSync[idx]) {
                    while (this.paused[idx] && !this.closed) {
                        try {
                            this.pauseSync[idx].wait();
                        } catch (Exception e) {
                        }
                    }
                }
            }
            if (this.stopTime <= -1 || this.elapseTime[idx].value < this.stopTime) {
                buffer = ic.getValidBuffer();
                flags = buffer.getFlags();
                rc = 0;
            } else {
                this.paused[idx] = true;
                if (checkStopAtTime(idx)) {
                    if (this.multiplexer instanceof Drainable) {
                        ((Drainable) this.multiplexer).drain();
                    }
                    doStop();
                    if (this.moduleListener != null) {
                        this.moduleListener.stopAtTime(this);
                    }
                }
            }
        }
        buffer = ic.getValidBuffer();
        flags = buffer.getFlags();
        rc = 0;
        if (this.resetted) {
            if ((flags & 512) != 0 && checkResetted(idx)) {
                this.resetted = false;
                doStop();
                if (this.moduleListener != null) {
                    this.moduleListener.resetted(this);
                }
            }
            ic.readReport();
        } else if (this.failed || this.closed || buffer.isDiscard()) {
            ic.readReport();
        } else {
            if (!((flags & 1024) == 0 || this.moduleListener == null)) {
                this.moduleListener.markedDataArrived(this, buffer);
                flags &= -1025;
                buffer.setFlags(flags);
            }
            boolean dataPrerolled = false;
            Format format = buffer.getFormat();
            if (format == null) {
                format = ic.getFormat();
                buffer.setFormat(format);
            }
            if (this.elapseTime[idx].update(buffer.getLength(), buffer.getTimeStamp(), format)) {
                if (this.prerollTrack[idx]) {
                    long target = getMediaNanoseconds();
                    if (this.elapseTime[idx].value > target) {
                        if ((format instanceof AudioFormat) && AudioFormat.LINEAR.equals(format.getEncoding())) {
                            int remain = (int) ElapseTime.audioTimeToLen(this.elapseTime[idx].value - target, (AudioFormat) format);
                            int offset = (buffer.getOffset() + buffer.getLength()) - remain;
                            if (offset >= 0) {
                                buffer.setOffset(offset);
                                buffer.setLength(remain);
                            }
                        }
                        this.prerollTrack[idx] = false;
                        this.elapseTime[idx].setValue(target);
                    } else {
                        dataPrerolled = true;
                    }
                }
                if (this.stopTime > -1 && this.elapseTime[idx].value > this.stopTime && (format instanceof AudioFormat)) {
                    int exceededLen = (int) ElapseTime.audioTimeToLen(this.elapseTime[idx].value - this.stopTime, (AudioFormat) format);
                    if (buffer.getLength() > exceededLen) {
                        buffer.setLength(buffer.getLength() - exceededLen);
                    }
                }
            }
            if (this.moduleListener != null && (format instanceof VideoFormat)) {
                float fb = (((float) (((getMediaNanoseconds() / TimeSource.MICROS_PER_SEC) - (buffer.getTimeStamp() / TimeSource.MICROS_PER_SEC)) - (getLatency() / TimeSource.MICROS_PER_SEC))) * this.frameRate) / 1000.0f;
                if (fb < 0.0f) {
                    fb = 0.0f;
                }
                if (this.lastFramesBehind != fb && (flags & 32) == 0) {
                    this.moduleListener.framesBehind(this, fb, ic);
                    this.lastFramesBehind = fb;
                }
            }
            do {
                if (dataPrerolled) {
                    rc = 0;
                } else {
                    try {
                        rc = this.multiplexer.process(buffer, idx);
                    } catch (Throwable e2) {
                        Log.dumpStack(e2);
                        if (this.moduleListener != null) {
                            this.moduleListener.internalErrorOccurred(this);
                        }
                    }
                    if (rc == 0 && format == this.firstVideoFormat) {
                        if (format != this.rtpVideoFormat) {
                            this.framesPlayed++;
                        } else if ((flags & 2048) > 0) {
                            this.framesPlayed++;
                        }
                    }
                }
                if ((rc & 8) == 0) {
                    if (this.prefetching && (!(this.multiplexer instanceof Prefetchable) || ((Prefetchable) this.multiplexer).isPrefetched())) {
                        synchronized (this.prefetchSync) {
                            if (!(this.started || !this.prefetching || this.resetted)) {
                                this.paused[idx] = true;
                            }
                            if (checkPrefetch(idx)) {
                                this.prefetching = false;
                            }
                        }
                        if (!(this.prefetching || this.moduleListener == null)) {
                            this.moduleListener.bufferPrefetched(this);
                        }
                    }
                    if (this.resetted) {
                        break;
                    }
                } else {
                    this.failed = true;
                    if (this.moduleListener != null) {
                        this.moduleListener.pluginTerminated(this);
                    }
                    ic.readReport();
                    return;
                }
            } while (rc == 2);
            this.bitsWritten += (long) buffer.getLength();
            if (buffer.isEOM()) {
                if (!this.resetted) {
                    this.paused[idx] = true;
                }
                if (checkEnd(idx)) {
                    doStop();
                    if (this.moduleListener != null) {
                        this.moduleListener.mediaEnded(this);
                    }
                }
            }
            ic.readReport();
        }
    }

    public void doClose() {
        this.multiplexer.close();
        this.closed = true;
        int i = 0;
        while (i < this.pauseSync.length) {
            synchronized (this.pauseSync[i]) {
                this.pauseSync[i].notifyAll();
            }
            i++;
        }
    }

    public void doDealloc() {
    }

    public void doFailedPrefetch() {
        this.prefetching = false;
    }

    public boolean doPrefetch() {
        if (((PlaybackEngine) this.controller).prefetchEnabled) {
            resetPrefetchMarkers();
            this.prefetching = true;
            resume();
        }
        return true;
    }

    public boolean doRealize() {
        if (this.multiplexer == null || this.inputs == null) {
            return false;
        }
        try {
            this.multiplexer.open();
            this.prefetchMarkers = new boolean[this.ics.length];
            this.endMarkers = new boolean[this.ics.length];
            this.resettedMarkers = new boolean[this.ics.length];
            this.stopAtTimeMarkers = new boolean[this.ics.length];
            this.paused = new boolean[this.ics.length];
            this.prerollTrack = new boolean[this.ics.length];
            this.pauseSync = new Object[this.ics.length];
            this.elapseTime = new ElapseTime[this.ics.length];
            for (int i = 0; i < this.ics.length; i++) {
                this.prerollTrack[i] = false;
                this.pauseSync[i] = new Object();
                this.elapseTime[i] = new ElapseTime();
            }
            pause();
            return true;
        } catch (ResourceUnavailableException e) {
            return false;
        }
    }

    public void doStart() {
        super.doStart();
        resetEndMarkers();
        resetStopAtTimeMarkers();
        this.started = true;
        synchronized (this.prefetchSync) {
            this.prefetching = false;
            resume();
        }
    }

    public void doStop() {
        super.doStop();
        this.started = false;
        resetPrefetchMarkers();
        this.prefetching = true;
    }

    public long getBitsWritten() {
        return this.bitsWritten;
    }

    public Object getControl(String s) {
        return this.multiplexer.getControl(s);
    }

    public Object[] getControls() {
        return this.multiplexer.getControls();
    }

    public DataSource getDataOutput() {
        return this.multiplexer.getDataOutput();
    }

    public int getFramesPlayed() {
        return this.framesPlayed;
    }

    public Multiplexer getMultiplexer() {
        return this.multiplexer;
    }

    public boolean isThreaded() {
        return false;
    }

    /* access modifiers changed from: 0000 */
    public void pause() {
        for (int i = 0; i < this.paused.length; i++) {
            this.paused[i] = true;
        }
    }

    /* access modifiers changed from: protected */
    public void process() {
    }

    public void reset() {
        super.reset();
        resetResettedMarkers();
        this.prefetching = false;
    }

    public void resetBitsWritten() {
        this.bitsWritten = 0;
    }

    /* access modifiers changed from: 0000 */
    public void resetEndMarkers() {
        synchronized (this.endMarkers) {
            for (int i = 0; i < this.endMarkers.length; i++) {
                this.endMarkers[i] = false;
            }
        }
    }

    public void resetFramesPlayed() {
        this.framesPlayed = 0;
    }

    /* access modifiers changed from: 0000 */
    public void resetPrefetchMarkers() {
        synchronized (this.prefetchMarkers) {
            for (int i = 0; i < this.prefetchMarkers.length; i++) {
                this.prefetchMarkers[i] = false;
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void resetResettedMarkers() {
        synchronized (this.resettedMarkers) {
            for (int i = 0; i < this.resettedMarkers.length; i++) {
                this.resettedMarkers[i] = false;
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void resetStopAtTimeMarkers() {
        synchronized (this.stopAtTimeMarkers) {
            for (int i = 0; i < this.stopAtTimeMarkers.length; i++) {
                this.stopAtTimeMarkers[i] = false;
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void resume() {
        int i = 0;
        while (i < this.pauseSync.length) {
            synchronized (this.pauseSync[i]) {
                this.paused[i] = false;
                this.pauseSync[i].notifyAll();
            }
            i++;
        }
    }

    public void setFormat(Connector connector, Format format) {
        if (format instanceof VideoFormat) {
            float fr = ((VideoFormat) format).getFrameRate();
            if (fr != -1.0f) {
                this.frameRate = fr;
            }
        }
    }

    public void setPreroll(long wanted, long actual) {
        super.setPreroll(wanted, actual);
        int i = 0;
        while (i < this.elapseTime.length) {
            this.elapseTime[i].setValue(actual);
            if ((this.inputs[i] instanceof AudioFormat) && mpegAudio.matches(this.inputs[i])) {
                this.prerollTrack[i] = false;
            } else {
                this.prerollTrack[i] = true;
            }
            i++;
        }
    }

    public void triggerReset() {
        this.multiplexer.reset();
        synchronized (this.prefetchSync) {
            this.prefetching = false;
            if (this.resetted) {
                resume();
            }
        }
    }
}
