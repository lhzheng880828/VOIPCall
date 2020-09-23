package net.sf.fmj.media.multiplexer;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.Time;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import net.sf.fmj.ejmf.toolkit.util.TimeSource;
import net.sf.fmj.media.BasicClock;

public class RawSyncBufferMux extends RawBufferMux {
    static int LEEWAY = 5;
    static int THRESHOLD = 80;
    static AudioFormat mpegAudio = new AudioFormat(AudioFormat.MPEG_RTP);
    static VideoFormat mpegVideo = new VideoFormat(VideoFormat.MPEG_RTP);
    private boolean masterTrackEnded;
    protected boolean monoIncrTime;
    private long monoStartTime;
    private long monoTime;
    boolean mpegBFrame;
    boolean mpegPFrame;
    private boolean resetted;
    private Object waitLock;

    public RawSyncBufferMux() {
        this.mpegBFrame = false;
        this.mpegPFrame = false;
        this.monoIncrTime = false;
        this.monoStartTime = 0;
        this.monoTime = 0;
        this.waitLock = new Object();
        this.resetted = false;
        this.masterTrackEnded = false;
        this.timeBase = new RawMuxTimeBase();
        this.allowDrop = true;
        this.clock = new BasicClock();
        try {
            this.clock.setTimeBase(this.timeBase);
        } catch (Exception e) {
        }
    }

    public String getName() {
        return "Raw Sync Buffer Multiplexer";
    }

    public boolean initializeTracks(Format[] trackFormats) {
        if (!super.initializeTracks(trackFormats)) {
            return false;
        }
        this.masterTrackID = 0;
        for (int i = 0; i < trackFormats.length; i++) {
            if (trackFormats[i] instanceof AudioFormat) {
                this.masterTrackID = i;
            }
        }
        return true;
    }

    public int process(Buffer buffer, int trackID) {
        if ((buffer.getFlags() & 4096) != 0) {
            buffer.setFlags((buffer.getFlags() & -4097) | 256);
        }
        if (this.mc[trackID] != null && this.mc[trackID].isEnabled()) {
            this.mc[trackID].process(buffer);
        }
        if (this.streams == null || buffer == null || trackID >= this.streams.length) {
            return 1;
        }
        if (buffer.isDiscard()) {
            return 0;
        }
        if ((buffer.getFlags() & 64) == 0) {
            if (buffer.getFormat() instanceof AudioFormat) {
                if (mpegAudio.matches(buffer.getFormat())) {
                    waitForPT(buffer.getTimeStamp(), trackID);
                } else {
                    waitForPT(this.mediaTime[trackID], trackID);
                }
            } else if (buffer.getTimeStamp() >= 0) {
                if (!mpegVideo.matches(buffer.getFormat()) || (buffer.getFlags() & 2048) == 0) {
                    waitForPT(buffer.getTimeStamp(), trackID);
                } else {
                    int ptype = ((byte[]) buffer.getData())[buffer.getOffset() + 2] & 7;
                    if (ptype > 2) {
                        this.mpegBFrame = true;
                    } else if (ptype == 2) {
                        this.mpegPFrame = true;
                    }
                    if (ptype > 2 || ((ptype == 2 && !this.mpegBFrame) || (ptype == 1 && (this.mpegBFrame | this.mpegPFrame) == 0))) {
                        waitForPT(buffer.getTimeStamp(), trackID);
                    }
                }
            }
        }
        updateTime(buffer, trackID);
        buffer.setFlags(buffer.getFlags() | 96);
        if ((!(buffer.getFormat() instanceof AudioFormat) || mpegAudio.matches(buffer.getFormat())) && this.monoIncrTime) {
            this.monoTime = (this.monoStartTime + buffer.getTimeStamp()) - (this.mediaStartTime * TimeSource.MICROS_PER_SEC);
            buffer.setTimeStamp(this.monoTime);
        }
        if (buffer.isEOM() && trackID == this.masterTrackID) {
            this.masterTrackEnded = true;
        }
        buffer.setHeader(new Long(System.currentTimeMillis()));
        return this.streams[trackID].process(buffer);
    }

    public void reset() {
        super.reset();
        this.mpegBFrame = false;
        this.mpegPFrame = false;
        synchronized (this.waitLock) {
            this.resetted = true;
            this.waitLock.notify();
        }
    }

    public void setMediaTime(Time now) {
        super.setMediaTime(now);
        this.monoStartTime = this.monoTime + 10;
    }

    public void syncStart(Time at) {
        this.masterTrackEnded = false;
        super.syncStart(at);
    }

    /* access modifiers changed from: protected */
    public void updateTime(Buffer buf, int trackID) {
        if (buf.getFormat() instanceof AudioFormat) {
            if (!mpegAudio.matches(buf.getFormat())) {
                long t = ((AudioFormat) buf.getFormat()).computeDuration((long) buf.getLength());
                if (t >= 0) {
                    long[] jArr = this.mediaTime;
                    jArr[trackID] = jArr[trackID] + t;
                } else {
                    this.mediaTime[trackID] = buf.getTimeStamp();
                }
            } else if (buf.getTimeStamp() >= 0) {
                this.mediaTime[trackID] = buf.getTimeStamp();
            } else if (this.systemStartTime >= 0) {
                this.mediaTime[trackID] = ((this.mediaStartTime + System.currentTimeMillis()) - this.systemStartTime) * TimeSource.MICROS_PER_SEC;
            }
        } else if (buf.getTimeStamp() >= 0 || this.systemStartTime < 0) {
            this.mediaTime[trackID] = buf.getTimeStamp();
        } else {
            this.mediaTime[trackID] = ((this.mediaStartTime + System.currentTimeMillis()) - this.systemStartTime) * TimeSource.MICROS_PER_SEC;
        }
        this.timeBase.update();
    }

    /* JADX WARNING: Missing block: B:14:0x003d, code skipped:
            if (r11.masterTrackID == -1) goto L_0x0043;
     */
    /* JADX WARNING: Missing block: B:16:0x0041, code skipped:
            if (r14 != r11.masterTrackID) goto L_0x007c;
     */
    /* JADX WARNING: Missing block: B:17:0x0043, code skipped:
            r0 = (r12 - r11.mediaStartTime) - (java.lang.System.currentTimeMillis() - r11.systemStartTime);
     */
    /* JADX WARNING: Missing block: B:41:0x007c, code skipped:
            r0 = r12 - (r11.mediaTime[r11.masterTrackID] / net.sf.fmj.ejmf.toolkit.util.TimeSource.MICROS_PER_SEC);
     */
    private void waitForPT(long r12, int r14) {
        /*
        r11 = this;
        r4 = 1000000; // 0xf4240 float:1.401298E-39 double:4.940656E-318;
        r12 = r12 / r4;
        r3 = r11.masterTrackID;
        r4 = -1;
        if (r3 == r4) goto L_0x000d;
    L_0x0009:
        r3 = r11.masterTrackID;
        if (r14 != r3) goto L_0x002c;
    L_0x000d:
        r4 = r11.systemStartTime;
        r6 = 0;
        r3 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r3 >= 0) goto L_0x001e;
    L_0x0015:
        r0 = 0;
    L_0x0017:
        r4 = 2000; // 0x7d0 float:2.803E-42 double:9.88E-321;
        r3 = (r0 > r4 ? 1 : (r0 == r4 ? 0 : -1));
        if (r3 <= 0) goto L_0x0050;
    L_0x001d:
        return;
    L_0x001e:
        r4 = r11.mediaStartTime;
        r4 = r12 - r4;
        r6 = java.lang.System.currentTimeMillis();
        r8 = r11.systemStartTime;
        r6 = r6 - r8;
        r0 = r4 - r6;
        goto L_0x0017;
    L_0x002c:
        r3 = r11.mediaTime;
        r4 = r11.masterTrackID;
        r4 = r3[r4];
        r6 = 1000000; // 0xf4240 float:1.401298E-39 double:4.940656E-318;
        r4 = r4 / r6;
        r0 = r12 - r4;
        goto L_0x0017;
    L_0x0039:
        monitor-exit(r4);	 Catch:{ all -> 0x0076 }
        r3 = r11.masterTrackID;
        r4 = -1;
        if (r3 == r4) goto L_0x0043;
    L_0x003f:
        r3 = r11.masterTrackID;
        if (r14 != r3) goto L_0x007c;
    L_0x0043:
        r4 = r11.mediaStartTime;
        r4 = r12 - r4;
        r6 = java.lang.System.currentTimeMillis();
        r8 = r11.systemStartTime;
        r6 = r6 - r8;
        r0 = r4 - r6;
    L_0x0050:
        r3 = LEEWAY;
        r4 = (long) r3;
        r3 = (r0 > r4 ? 1 : (r0 == r4 ? 0 : -1));
        if (r3 <= 0) goto L_0x001d;
    L_0x0057:
        r3 = r11.masterTrackEnded;
        if (r3 != 0) goto L_0x001d;
    L_0x005b:
        r3 = THRESHOLD;
        r4 = (long) r3;
        r3 = (r0 > r4 ? 1 : (r0 == r4 ? 0 : -1));
        if (r3 <= 0) goto L_0x0065;
    L_0x0062:
        r3 = THRESHOLD;
        r0 = (long) r3;
    L_0x0065:
        r4 = r11.waitLock;
        monitor-enter(r4);
        r3 = r11.waitLock;	 Catch:{ Exception -> 0x0079 }
        r3.wait(r0);	 Catch:{ Exception -> 0x0079 }
        r3 = r11.resetted;	 Catch:{ all -> 0x0076 }
        if (r3 == 0) goto L_0x0039;
    L_0x0071:
        r3 = 0;
        r11.resetted = r3;	 Catch:{ all -> 0x0076 }
        monitor-exit(r4);	 Catch:{ all -> 0x0076 }
        goto L_0x001d;
    L_0x0076:
        r3 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x0076 }
        throw r3;
    L_0x0079:
        r2 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x0076 }
        goto L_0x001d;
    L_0x007c:
        r3 = r11.mediaTime;
        r4 = r11.masterTrackID;
        r4 = r3[r4];
        r6 = 1000000; // 0xf4240 float:1.401298E-39 double:4.940656E-318;
        r4 = r4 / r6;
        r0 = r12 - r4;
        goto L_0x0050;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.multiplexer.RawSyncBufferMux.waitForPT(long, int):void");
    }
}
