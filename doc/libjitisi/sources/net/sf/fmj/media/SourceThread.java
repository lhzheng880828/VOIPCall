package net.sf.fmj.media;

import javax.media.Buffer;
import javax.media.Track;
import javax.media.TrackListener;
import net.sf.fmj.media.rtp.util.RTPTimeBase;
import net.sf.fmj.media.util.LoopThread;

/* compiled from: BasicSourceModule */
class SourceThread extends LoopThread implements TrackListener {
    static int remapTimeFlag = 4480;
    BasicSourceModule bsm;
    protected boolean checkLatency = false;
    long counter = 0;
    long currentTime = 0;
    int index = 0;
    protected long lastRelativeTime = -1;
    protected MyOutputConnector oc;
    protected boolean readBlocked = false;
    protected boolean resetted = false;
    long sequenceNum = 0;

    public SourceThread(BasicSourceModule bsm, MyOutputConnector oc, int i) {
        this.bsm = bsm;
        this.oc = oc;
        this.index = i;
        setName(getName() + ": " + oc.track);
        oc.track.setTrackListener(this);
    }

    /* access modifiers changed from: protected */
    public boolean process() {
        this.readBlocked = false;
        Buffer buffer = this.oc.getEmptyBuffer();
        buffer.setOffset(0);
        buffer.setLength(0);
        buffer.setFlags(0);
        long j = this.sequenceNum;
        this.sequenceNum = 1 + j;
        buffer.setSequenceNumber(j);
        if (this.resetted) {
            synchronized (this.bsm.resetSync) {
                if (this.resetted) {
                    buffer.setFlags(512);
                    this.resetted = false;
                    pause();
                    if (this.bsm.checkAllPaused()) {
                        this.bsm.parser.stop();
                        this.bsm.parser.reset();
                    }
                    this.oc.writeReport();
                }
            }
            return true;
        }
        try {
            this.oc.track.readFrame(buffer);
        } catch (Throwable e) {
            Log.dumpStack(e);
            if (this.bsm.moduleListener != null) {
                this.bsm.moduleListener.internalErrorOccurred(this.bsm);
            }
        }
        if (PlaybackEngine.TRACE_ON && !this.bsm.verifyBuffer(buffer)) {
            System.err.println("verify buffer failed: " + this.oc.track);
            Thread.dumpStack();
            if (this.bsm.moduleListener != null) {
                this.bsm.moduleListener.internalErrorOccurred(this.bsm);
            }
        }
        if (!(buffer.getTimeStamp() == -1 || (buffer.getFlags() & remapTimeFlag) == 0)) {
            boolean success = true;
            if ((buffer.getFlags() & 128) != 0) {
                success = remapSystemTime(buffer);
            } else if ((buffer.getFlags() & 256) != 0) {
                success = remapRelativeTime(buffer);
            } else if ((buffer.getFlags() & 4096) != 0) {
                success = remapRTPTime(buffer);
            }
            if (!success) {
                buffer.setDiscard(true);
                this.oc.writeReport();
                return true;
            }
        }
        if (this.checkLatency) {
            buffer.setFlags(buffer.getFlags() | 1024);
            if (this.bsm.moduleListener != null) {
                this.bsm.moduleListener.markedDataArrived(this.bsm, buffer);
            }
            this.checkLatency = false;
        } else {
            buffer.setFlags(buffer.getFlags() & -1025);
        }
        if (this.readBlocked && this.bsm.moduleListener != null) {
            this.bsm.moduleListener.dataBlocked(this.bsm, false);
        }
        if (buffer.isEOM()) {
            synchronized (this.bsm.resetSync) {
                if (!this.resetted) {
                    pause();
                    if (this.bsm.checkAllPaused()) {
                        this.bsm.parser.stop();
                    }
                }
            }
        } else {
            BasicSourceModule basicSourceModule = this.bsm;
            basicSourceModule.bitsRead += (long) buffer.getLength();
        }
        this.oc.writeReport();
        return true;
    }

    public void readHasBlocked(Track t) {
        this.readBlocked = true;
        if (this.bsm.moduleListener != null) {
            this.bsm.moduleListener.dataBlocked(this.bsm, true);
        }
    }

    private boolean remapRelativeTime(Buffer buffer) {
        buffer.setFlags((buffer.getFlags() & -257) | 96);
        return true;
    }

    private boolean remapRTPTime(Buffer buffer) {
        if (buffer.getTimeStamp() <= 0) {
            buffer.setTimeStamp(-1);
        } else {
            if (this.bsm.cname == null) {
                this.bsm.cname = this.bsm.engine.getCNAME();
                if (this.bsm.cname == null) {
                    buffer.setTimeStamp(-1);
                }
            }
            if (this.bsm.rtpOffsetInvalid) {
                if (this.bsm.rtpMapperUpdatable == null) {
                    this.bsm.rtpMapperUpdatable = RTPTimeBase.getMapperUpdatable(this.bsm.cname);
                    if (this.bsm.rtpMapperUpdatable == null) {
                        this.bsm.rtpOffsetInvalid = false;
                    }
                }
                if (this.bsm.rtpMapperUpdatable != null) {
                    this.bsm.rtpMapperUpdatable.setOrigin(this.bsm.currentRTPTime);
                    this.bsm.rtpMapperUpdatable.setOffset(buffer.getTimeStamp());
                    this.bsm.rtpOffsetInvalid = false;
                }
            }
            if (this.bsm.rtpMapper == null) {
                this.bsm.rtpMapper = RTPTimeBase.getMapper(this.bsm.cname);
            }
            if (this.bsm.rtpMapper.getOffset() != this.bsm.oldOffset) {
                this.bsm.oldOffset = this.bsm.rtpMapper.getOffset();
            }
            long dur = buffer.getTimeStamp() - this.bsm.rtpMapper.getOffset();
            if (dur < 0) {
                if (this.bsm.rtpMapperUpdatable != null) {
                    this.bsm.rtpOffsetInvalid = true;
                } else {
                    dur = 0;
                }
            }
            this.bsm.currentRTPTime = this.bsm.rtpMapper.getOrigin() + dur;
            buffer.setTimeStamp(this.bsm.currentRTPTime);
        }
        return true;
    }

    private boolean remapSystemTime(Buffer buffer) {
        if (!this.bsm.started) {
            return false;
        }
        long ts = buffer.getTimeStamp() - this.bsm.lastSystemTime;
        if (ts < 0) {
            return false;
        }
        this.bsm.currentSystemTime = this.bsm.originSystemTime + ts;
        buffer.setTimeStamp(this.bsm.currentSystemTime);
        buffer.setFlags((buffer.getFlags() & -129) | 96);
        return true;
    }

    public synchronized void start() {
        super.start();
        this.lastRelativeTime = -1;
    }
}
