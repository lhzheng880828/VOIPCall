package net.sf.fmj.media;

import javax.media.Buffer;
import javax.media.Clock;
import javax.media.Drainable;
import javax.media.Format;
import javax.media.Prefetchable;
import javax.media.Renderer;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.renderer.VideoRenderer;
import net.sf.fmj.ejmf.toolkit.util.TimeSource;
import net.sf.fmj.filtergraph.SimpleGraphBuilder;
import net.sf.fmj.media.renderer.audio.AudioRenderer;
import net.sf.fmj.media.rtp.util.RTPTimeBase;
import net.sf.fmj.media.rtp.util.RTPTimeReporter;
import net.sf.fmj.media.util.ElapseTime;

public class BasicRendererModule extends BasicSinkModule implements RTPTimeReporter {
    static final int MAX_CHUNK_SIZE = 16;
    static final long RTP_TIME_MARGIN = 2000000000;
    final int FLOW_LIMIT = 20;
    private long LEEWAY = 10;
    final float MAX_RATE = 1.05f;
    final float RATE_INCR = 0.01f;
    private boolean checkRTP = false;
    private int chunkSize = Integer.MAX_VALUE;
    private ElapseTime elapseTime = new ElapseTime();
    protected PlaybackEngine engine;
    private boolean failed = false;
    protected float frameRate = 30.0f;
    protected int framesPlayed = 0;
    protected boolean framesWereBehind = false;
    protected InputConnector ic;
    private long lastDuration = 0;
    private long lastRendered = 0;
    long lastTimeStamp;
    AudioFormat linearFormat = new AudioFormat(AudioFormat.LINEAR);
    private boolean noSync = false;
    private boolean notToDropNext = false;
    private boolean opened = false;
    boolean overMsg = false;
    int overflown = 10;
    private Object prefetchSync = new Object();
    protected boolean prefetching = false;
    float rate = 1.0f;
    RenderThread renderThread;
    protected Renderer renderer;
    private String rtpCNAME = null;
    boolean rtpErrMsg = false;
    private RTPTimeBase rtpTimeBase = null;
    protected boolean started = false;
    private Buffer storedBuffer = null;
    long systemErr = 0;
    AudioFormat ulawFormat = new AudioFormat(AudioFormat.ULAW);

    protected BasicRendererModule(Renderer r) {
        setRenderer(r);
        this.ic = new BasicInputConnector();
        if (r instanceof VideoRenderer) {
            this.ic.setSize(4);
        } else {
            this.ic.setSize(1);
        }
        this.ic.setModule(this);
        registerInputConnector("input", this.ic);
        setProtocol(1);
    }

    public void abortPrefetch() {
        this.renderThread.pause();
        this.renderer.close();
        this.prefetching = false;
        this.opened = false;
    }

    private int computeChunkSize(Format format) {
        if (!(format instanceof AudioFormat) || (!this.ulawFormat.matches(format) && !this.linearFormat.matches(format))) {
            return Integer.MAX_VALUE;
        }
        AudioFormat af = (AudioFormat) format;
        int units = (af.getSampleSizeInBits() * af.getChannels()) / 8;
        if (units == 0) {
            units = 1;
        }
        return (((((int) af.getSampleRate()) * units) / 16) / units) * units;
    }

    public void doClose() {
        this.renderThread.kill();
        if (this.renderer != null) {
            this.renderer.close();
        }
        if (this.rtpTimeBase != null) {
            RTPTimeBase.remove(this, this.rtpCNAME);
            this.rtpTimeBase = null;
        }
    }

    public void doDealloc() {
        this.renderer.close();
    }

    public void doFailedPrefetch() {
        this.renderThread.pause();
        this.renderer.close();
        this.opened = false;
        this.prefetching = false;
    }

    private void donePrefetch() {
        synchronized (this.prefetchSync) {
            if (!this.started && this.prefetching) {
                this.renderThread.pause();
            }
            this.prefetching = false;
        }
        if (this.moduleListener != null) {
            this.moduleListener.bufferPrefetched(this);
        }
    }

    public void doneReset() {
        this.renderThread.pause();
    }

    public boolean doPrefetch() {
        super.doPrefetch();
        if (!this.opened) {
            try {
                this.renderer.open();
                this.prefetchFailed = false;
                this.opened = true;
            } catch (ResourceUnavailableException e) {
                this.prefetchFailed = true;
                return false;
            }
        }
        if (!((PlaybackEngine) this.controller).prefetchEnabled) {
            return true;
        }
        this.prefetching = true;
        this.renderThread.start();
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean doProcess() {
        Buffer buffer;
        if ((this.started || this.prefetching) && this.stopTime > -1 && this.elapseTime.value >= this.stopTime) {
            if (this.renderer instanceof Drainable) {
                ((Drainable) this.renderer).drain();
            }
            doStop();
            if (this.moduleListener != null) {
                this.moduleListener.stopAtTime(this);
            }
        }
        if (this.storedBuffer != null) {
            buffer = this.storedBuffer;
        } else {
            buffer = this.ic.getValidBuffer();
        }
        if (!this.checkRTP) {
            if ((buffer.getFlags() & 4096) != 0) {
                String key = this.engine.getCNAME();
                if (key != null) {
                    this.rtpTimeBase = RTPTimeBase.find(this, key);
                    this.rtpCNAME = key;
                    if (this.ic.getFormat() instanceof AudioFormat) {
                        Log.comment("RTP master time set: " + this.renderer + "\n");
                        this.rtpTimeBase.setMaster(this);
                    }
                    this.checkRTP = true;
                    this.noSync = false;
                } else {
                    this.noSync = true;
                }
            } else {
                this.checkRTP = true;
            }
        }
        this.lastTimeStamp = buffer.getTimeStamp();
        if (this.failed || this.resetted) {
            if ((buffer.getFlags() & 512) != 0) {
                this.resetted = false;
                this.renderThread.pause();
                if (this.moduleListener != null) {
                    this.moduleListener.resetted(this);
                }
            }
            this.storedBuffer = null;
            this.ic.readReport();
            return true;
        }
        boolean rtn = scheduleBuffer(buffer);
        if (this.storedBuffer == null && buffer.isEOM()) {
            if (this.prefetching) {
                donePrefetch();
            }
            if ((buffer.getFlags() & 64) == 0 && buffer.getTimeStamp() > 0 && buffer.getDuration() > 0 && buffer.getFormat() != null && !(buffer.getFormat() instanceof AudioFormat) && !this.noSync) {
                waitForPT(buffer.getTimeStamp() + this.lastDuration);
            }
            this.storedBuffer = null;
            this.ic.readReport();
            doStop();
            if (this.moduleListener != null) {
                this.moduleListener.mediaEnded(this);
            }
            return true;
        } else if (this.storedBuffer != null) {
            return rtn;
        } else {
            this.ic.readReport();
            return rtn;
        }
    }

    public boolean doRealize() {
        this.chunkSize = computeChunkSize(this.ic.getFormat());
        this.renderThread = new RenderThread(this);
        this.engine = (PlaybackEngine) getController();
        return true;
    }

    public void doStart() {
        super.doStart();
        if (!(this.renderer instanceof Clock)) {
            this.renderer.start();
        }
        this.prerolling = false;
        this.started = true;
        synchronized (this.prefetchSync) {
            this.prefetching = false;
            this.renderThread.start();
        }
    }

    public void doStop() {
        this.started = false;
        this.prefetching = true;
        super.doStop();
        if (this.renderer != null && !(this.renderer instanceof Clock)) {
            this.renderer.stop();
        }
    }

    public Object getControl(String s) {
        return this.renderer.getControl(s);
    }

    public Object[] getControls() {
        return this.renderer.getControls();
    }

    public int getFramesPlayed() {
        return this.framesPlayed;
    }

    public Renderer getRenderer() {
        return this.renderer;
    }

    public long getRTPTime() {
        if (!(this.ic.getFormat() instanceof AudioFormat)) {
            return this.lastTimeStamp;
        }
        if (this.renderer instanceof AudioRenderer) {
            return this.lastTimeStamp - ((AudioRenderer) this.renderer).getLatency();
        }
        return this.lastTimeStamp;
    }

    private long getSyncTime(long pts) {
        if (this.rtpTimeBase == null) {
            return getMediaNanoseconds();
        }
        if (this.rtpTimeBase.getMaster() == getController()) {
            return pts;
        }
        long ts = this.rtpTimeBase.getNanoseconds();
        if (ts <= pts + RTP_TIME_MARGIN && ts >= pts - RTP_TIME_MARGIN) {
            return ts;
        }
        if (this.rtpErrMsg) {
            return pts;
        }
        Log.comment("Cannot perform RTP sync beyond a difference of: " + ((ts - pts) / TimeSource.MICROS_PER_SEC) + " msecs.\n");
        this.rtpErrMsg = true;
        return pts;
    }

    private boolean handleFormatChange(Format format) {
        if (reinitRenderer(format)) {
            Format oldFormat = this.ic.getFormat();
            this.ic.setFormat(format);
            if (this.moduleListener != null) {
                this.moduleListener.formatChanged(this, oldFormat, format);
            }
            if (!(format instanceof VideoFormat)) {
                return true;
            }
            float fr = ((VideoFormat) format).getFrameRate();
            if (fr == -1.0f) {
                return true;
            }
            this.frameRate = fr;
            return true;
        }
        this.storedBuffer = null;
        this.failed = true;
        if (this.moduleListener != null) {
            this.moduleListener.formatChangedFailure(this, this.ic.getFormat(), format);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handlePreroll(Buffer buf) {
        if (buf.getFormat() instanceof AudioFormat) {
            if (!hasReachAudioPrerollTarget(buf)) {
                return false;
            }
        } else if ((buf.getFlags() & 96) == 0 && buf.getTimeStamp() >= 0 && buf.getTimeStamp() < getSyncTime(buf.getTimeStamp())) {
            return false;
        }
        this.prerolling = false;
        return true;
    }

    private boolean hasReachAudioPrerollTarget(Buffer buf) {
        long target = getSyncTime(buf.getTimeStamp());
        this.elapseTime.update(buf.getLength(), buf.getTimeStamp(), buf.getFormat());
        if (this.elapseTime.value < target) {
            return false;
        }
        long remain = ElapseTime.audioTimeToLen(this.elapseTime.value - target, (AudioFormat) buf.getFormat());
        int offset = (buf.getOffset() + buf.getLength()) - ((int) remain);
        if (offset >= 0) {
            buf.setOffset(offset);
            buf.setLength((int) remain);
        }
        this.elapseTime.setValue(target);
        return true;
    }

    public boolean isThreaded() {
        return true;
    }

    /* access modifiers changed from: protected */
    public void process() {
    }

    public int processBuffer(Buffer buffer) {
        int rc;
        int remain = buffer.getLength();
        int offset = buffer.getOffset();
        int rc2 = 0;
        boolean isEOM = false;
        if (this.renderer instanceof Clock) {
            if ((buffer.getFlags() & 8192) != 0) {
                this.overflown++;
            } else {
                this.overflown--;
            }
            if (this.overflown > 20) {
                if (this.rate < 1.05f) {
                    this.rate += 0.01f;
                    this.renderer.stop();
                    ((Clock) this.renderer).setRate(this.rate);
                    this.renderer.start();
                    if (!this.overMsg) {
                        Log.comment("Data buffers overflown.  Adjust rendering speed up to 5 % to compensate");
                        this.overMsg = true;
                    }
                }
                this.overflown = 10;
            } else if (this.overflown <= 0) {
                if (this.rate > 1.0f) {
                    this.rate -= 0.01f;
                    this.renderer.stop();
                    ((Clock) this.renderer).setRate(this.rate);
                    this.renderer.start();
                }
                this.overflown = 10;
            }
        }
        do {
            if (this.stopTime <= -1 || this.elapseTime.value < this.stopTime) {
                int len;
                if (remain <= this.chunkSize || this.prerolling) {
                    if (isEOM) {
                        isEOM = false;
                        buffer.setEOM(true);
                    }
                    len = remain;
                } else {
                    if (buffer.isEOM()) {
                        isEOM = true;
                        buffer.setEOM(false);
                    }
                    len = this.chunkSize;
                }
                buffer.setLength(len);
                buffer.setOffset(offset);
                if (!this.prerolling || handlePreroll(buffer)) {
                    try {
                        rc = this.renderer.process(buffer);
                    } catch (Throwable e) {
                        Log.dumpStack(e);
                        if (this.moduleListener != null) {
                            this.moduleListener.internalErrorOccurred(this);
                        }
                        rc = rc2;
                    }
                    if ((rc & 8) == 0) {
                        if ((rc & 1) == 0) {
                            if ((rc & 2) != 0) {
                                len -= buffer.getLength();
                            }
                            offset += len;
                            remain -= len;
                            if (this.prefetching && (!(this.renderer instanceof Prefetchable) || ((Prefetchable) this.renderer).isPrefetched())) {
                                isEOM = false;
                                buffer.setEOM(false);
                                donePrefetch();
                                break;
                            }
                            this.elapseTime.update(len, buffer.getTimeStamp(), buffer.getFormat());
                            rc2 = rc;
                        } else {
                            buffer.setDiscard(true);
                            if (this.prefetching) {
                                donePrefetch();
                            }
                            rc2 = rc;
                            return rc;
                        }
                    }
                    this.failed = true;
                    if (this.moduleListener != null) {
                        this.moduleListener.pluginTerminated(this);
                    }
                    rc2 = rc;
                    return rc;
                }
                offset += len;
                remain -= len;
                if (remain <= 0) {
                    break;
                }
            } else {
                if (this.prefetching) {
                    donePrefetch();
                }
                return 2;
            }
        } while (!this.resetted);
        rc = rc2;
        if (isEOM) {
            buffer.setEOM(true);
        }
        buffer.setLength(remain);
        buffer.setOffset(offset);
        if (rc == 0) {
            this.framesPlayed++;
        }
        rc2 = rc;
        return rc;
    }

    /* access modifiers changed from: protected */
    public boolean reinitRenderer(Format input) {
        if (this.renderer != null && this.renderer.setInputFormat(input) != null) {
            return true;
        }
        if (this.started) {
            this.renderer.stop();
            this.renderer.reset();
        }
        this.renderer.close();
        this.renderer = null;
        Renderer r = SimpleGraphBuilder.findRenderer(input);
        if (r == null) {
            return false;
        }
        setRenderer(r);
        if (this.started) {
            this.renderer.start();
        }
        this.chunkSize = computeChunkSize(input);
        return true;
    }

    public void reset() {
        super.reset();
        this.prefetching = false;
    }

    public void resetFramesPlayed() {
        this.framesPlayed = 0;
    }

    /* access modifiers changed from: protected */
    public boolean scheduleBuffer(Buffer buf) {
        int rc = 0;
        Format format = buf.getFormat();
        if (format == null) {
            format = this.ic.getFormat();
            buf.setFormat(format);
        }
        if (format != this.ic.getFormat() && !format.equals(this.ic.getFormat()) && !buf.isDiscard() && !handleFormatChange(format)) {
            return false;
        }
        if (!((buf.getFlags() & 1024) == 0 || this.moduleListener == null)) {
            this.moduleListener.markedDataArrived(this, buf);
            buf.setFlags(buf.getFlags() & -1025);
        }
        if (!this.prefetching && !(format instanceof AudioFormat) && buf.getTimeStamp() > 0 && (buf.getFlags() & 96) != 96 && !this.noSync) {
            long lateBy = ((getSyncTime(buf.getTimeStamp()) / TimeSource.MICROS_PER_SEC) - (buf.getTimeStamp() / TimeSource.MICROS_PER_SEC)) - (getLatency() / TimeSource.MICROS_PER_SEC);
            if (this.storedBuffer != null || lateBy <= 0) {
                if (this.moduleListener != null && this.framesWereBehind && (format instanceof VideoFormat)) {
                    this.moduleListener.framesBehind(this, 0.0f, this.ic);
                    this.framesWereBehind = false;
                }
                if (!buf.isDiscard()) {
                    if ((buf.getFlags() & 64) == 0) {
                        waitForPT(buf.getTimeStamp());
                    }
                    if (!this.resetted) {
                        rc = processBuffer(buf);
                        this.lastRendered = buf.getTimeStamp();
                    }
                }
            } else if (buf.isDiscard()) {
                this.notToDropNext = true;
            } else {
                if (buf.isEOM()) {
                    this.notToDropNext = true;
                } else if (this.moduleListener != null && (format instanceof VideoFormat)) {
                    float fb = (((float) lateBy) * this.frameRate) / 1000.0f;
                    if (fb < 1.0f) {
                        fb = 1.0f;
                    }
                    this.moduleListener.framesBehind(this, fb, this.ic);
                    this.framesWereBehind = true;
                }
                if ((buf.getFlags() & 32) != 0) {
                    rc = processBuffer(buf);
                } else if (lateBy < this.LEEWAY || this.notToDropNext || buf.getTimeStamp() - this.lastRendered > 1000000000) {
                    rc = processBuffer(buf);
                    this.lastRendered = buf.getTimeStamp();
                    this.notToDropNext = false;
                }
            }
        } else if (!buf.isDiscard()) {
            rc = processBuffer(buf);
        }
        if ((rc & 1) != 0) {
            this.storedBuffer = null;
        } else if ((rc & 2) != 0) {
            this.storedBuffer = buf;
        } else {
            this.storedBuffer = null;
            if (buf.getDuration() >= 0) {
                this.lastDuration = buf.getDuration();
            }
        }
        return true;
    }

    public void setFormat(Connector connector, Format format) {
        this.renderer.setInputFormat(format);
        if (format instanceof VideoFormat) {
            float fr = ((VideoFormat) format).getFrameRate();
            if (fr != -1.0f) {
                this.frameRate = fr;
            }
        }
    }

    public void setPreroll(long wanted, long actual) {
        super.setPreroll(wanted, actual);
        this.elapseTime.setValue(actual);
    }

    /* access modifiers changed from: protected */
    public void setRenderer(Renderer r) {
        this.renderer = r;
        if (this.renderer instanceof Clock) {
            setClock((Clock) this.renderer);
        }
    }

    public void triggerReset() {
        if (this.renderer != null) {
            this.renderer.reset();
        }
        synchronized (this.prefetchSync) {
            this.prefetching = false;
            if (this.resetted) {
                this.renderThread.start();
            }
        }
    }

    private boolean waitForPT(long pt) {
        long lastAheadBy = -1;
        int beenHere = 0;
        long aheadBy = (pt - getSyncTime(pt)) / TimeSource.MICROS_PER_SEC;
        if (this.rate != 1.0f) {
            aheadBy = (long) (((float) aheadBy) / this.rate);
        }
        while (aheadBy > this.systemErr && !this.resetted) {
            long interval;
            if (aheadBy == lastAheadBy) {
                interval = aheadBy + ((long) (beenHere * 5));
                if (interval > 33) {
                    interval = 33;
                } else {
                    beenHere++;
                }
            } else {
                interval = aheadBy;
                beenHere = 0;
            }
            if (interval > 125) {
                interval = 125;
            }
            long before = System.currentTimeMillis();
            interval -= this.systemErr;
            if (interval > 0) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                }
            }
            this.systemErr = (((System.currentTimeMillis() - before) - interval) + this.systemErr) / 2;
            if (this.systemErr < 0) {
                this.systemErr = 0;
            } else if (this.systemErr > interval) {
                this.systemErr = interval;
            }
            lastAheadBy = aheadBy;
            aheadBy = (pt - getSyncTime(pt)) / TimeSource.MICROS_PER_SEC;
            if (this.rate != 1.0f) {
                aheadBy = (long) (((float) aheadBy) / this.rate);
            }
            if (getState() != 600) {
                break;
            }
        }
        return true;
    }
}
