package net.sf.fmj.media.rtp;

import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.media.Format;
import javax.media.format.AudioFormat;
import net.sf.fmj.media.rtp.util.SSRCTable;
import org.jitsi.impl.neomedia.portaudio.Pa;

public class SSRCCache {
    static final int BYE_THRESHOLD = 50;
    static final int CONTROL = 2;
    static final int DATA = 1;
    private static final int NOTIFYPERIOD = 500;
    static final int RTCP_MIN_TIME = 5000;
    static final int SRCDATA = 3;
    int avgrtcpsize;
    boolean byestate;
    SSRCTable cache;
    int[] clockrate;
    Hashtable conflicttable;
    RTPEventHandler eventhandler;
    boolean initial;
    SSRCInfo ourssrc;
    double rtcp_bw_fraction;
    int rtcp_min_time;
    double rtcp_sender_bw_fraction;
    boolean rtcpsent;
    int sendercount;
    int sessionbandwidth;
    public final RTPSessionMgr sm;
    RTPSourceInfoCache sourceInfoCache;
    OverallStats stats;
    OverallTransStats transstats;

    SSRCCache(RTPSessionMgr sm) {
        this.cache = new SSRCTable();
        this.stats = null;
        this.transstats = null;
        this.clockrate = new int[128];
        this.sendercount = 0;
        this.rtcp_bw_fraction = Pa.LATENCY_UNSPECIFIED;
        this.rtcp_sender_bw_fraction = Pa.LATENCY_UNSPECIFIED;
        this.rtcp_min_time = RTCP_MIN_TIME;
        this.sessionbandwidth = 0;
        this.initial = true;
        this.byestate = false;
        this.rtcpsent = false;
        this.avgrtcpsize = 128;
        this.conflicttable = new Hashtable(5);
        this.stats = sm.defaultstats;
        this.transstats = sm.transstats;
        this.sourceInfoCache = new RTPSourceInfoCache();
        this.sourceInfoCache.setMainCache(this.sourceInfoCache);
        this.sourceInfoCache.setSSRCCache(this);
        this.sm = sm;
        this.eventhandler = new RTPEventHandler(sm);
        setclockrates();
    }

    SSRCCache(RTPSessionMgr sm, RTPSourceInfoCache sic) {
        this.cache = new SSRCTable();
        this.stats = null;
        this.transstats = null;
        this.clockrate = new int[128];
        this.sendercount = 0;
        this.rtcp_bw_fraction = Pa.LATENCY_UNSPECIFIED;
        this.rtcp_sender_bw_fraction = Pa.LATENCY_UNSPECIFIED;
        this.rtcp_min_time = RTCP_MIN_TIME;
        this.sessionbandwidth = 0;
        this.initial = true;
        this.byestate = false;
        this.rtcpsent = false;
        this.avgrtcpsize = 128;
        this.conflicttable = new Hashtable(5);
        this.stats = sm.defaultstats;
        this.transstats = sm.transstats;
        this.sourceInfoCache = sic;
        sic.setSSRCCache(this);
        this.sm = sm;
        this.eventhandler = new RTPEventHandler(sm);
    }

    /* access modifiers changed from: 0000 */
    public int aliveCount() {
        int tot = 0;
        Enumeration e = this.cache.elements();
        while (e.hasMoreElements()) {
            if (((SSRCInfo) e.nextElement()).alive) {
                tot++;
            }
        }
        return tot;
    }

    /* access modifiers changed from: 0000 */
    public double calcReportInterval(boolean sender, boolean recvfromothers) {
        this.rtcp_min_time = RTCP_MIN_TIME;
        double rtcp_bw = this.rtcp_bw_fraction;
        if (this.initial) {
            this.rtcp_min_time /= 2;
        }
        int n = aliveCount();
        if (this.sendercount > 0 && ((double) this.sendercount) < ((double) n) * this.rtcp_sender_bw_fraction) {
            if (sender) {
                rtcp_bw *= this.rtcp_sender_bw_fraction;
                n = this.sendercount;
            } else {
                rtcp_bw *= 1.0d - this.rtcp_sender_bw_fraction;
                n -= this.sendercount;
            }
        }
        if (recvfromothers && rtcp_bw == Pa.LATENCY_UNSPECIFIED) {
            rtcp_bw = 0.05d;
            if (this.sendercount > 0 && ((double) this.sendercount) < ((double) n) * 0.25d) {
                if (sender) {
                    rtcp_bw = 0.05d * 0.25d;
                    n = this.sendercount;
                } else {
                    rtcp_bw = 0.05d * 0.75d;
                    n -= this.sendercount;
                }
            }
        }
        double time = Pa.LATENCY_UNSPECIFIED;
        if (rtcp_bw != Pa.LATENCY_UNSPECIFIED) {
            time = ((double) (this.avgrtcpsize * n)) / rtcp_bw;
            if (time < ((double) this.rtcp_min_time)) {
                time = (double) this.rtcp_min_time;
            }
        }
        return recvfromothers ? time : time * (Math.random() + 0.5d);
    }

    private void changessrc(SSRCInfo info) {
        info.setOurs(true);
        if (this.ourssrc != null) {
            info.sourceInfo = this.sourceInfoCache.get(this.ourssrc.sourceInfo.getCNAME(), info.ours);
            info.sourceInfo.addSSRC(info);
        }
        info.reporter.releasessrc("Local Collision Detected");
        this.ourssrc = info;
        info.reporter.restart = true;
    }

    /* access modifiers changed from: declared_synchronized */
    public synchronized void destroy() {
        this.cache.removeAll();
        if (this.eventhandler != null) {
            this.eventhandler.close();
        }
    }

    /* access modifiers changed from: 0000 */
    public SSRCInfo get(int ssrc, InetAddress address, int port) {
        SSRCInfo ssrcinfo;
        synchronized (this) {
            ssrcinfo = lookup(ssrc);
        }
        return ssrcinfo;
    }

    /* access modifiers changed from: 0000 */
    /* JADX WARNING: Missing block: B:109:0x01a9, code skipped:
            r3 = r4;
     */
    /* JADX WARNING: Missing block: B:119:?, code skipped:
            return r4;
     */
    public net.sf.fmj.media.rtp.SSRCInfo get(int r19, java.net.InetAddress r20, int r21, int r22) {
        /*
        r18 = this;
        r3 = 0;
        r6 = 0;
        monitor-enter(r18);
        r0 = r18;
        r13 = r0.ourssrc;	 Catch:{ all -> 0x0075 }
        if (r13 == 0) goto L_0x002d;
    L_0x0009:
        r0 = r18;
        r13 = r0.ourssrc;	 Catch:{ all -> 0x0075 }
        r13 = r13.ssrc;	 Catch:{ all -> 0x0075 }
        r0 = r19;
        if (r13 != r0) goto L_0x002d;
    L_0x0013:
        r0 = r18;
        r13 = r0.ourssrc;	 Catch:{ all -> 0x0075 }
        r13 = r13.address;	 Catch:{ all -> 0x0075 }
        if (r13 == 0) goto L_0x002d;
    L_0x001b:
        r0 = r18;
        r13 = r0.ourssrc;	 Catch:{ all -> 0x0075 }
        r13 = r13.address;	 Catch:{ all -> 0x0075 }
        r0 = r20;
        r13 = r13.equals(r0);	 Catch:{ all -> 0x0075 }
        if (r13 != 0) goto L_0x002d;
    L_0x0029:
        r6 = 1;
        r18.LocalCollision(r19);	 Catch:{ all -> 0x0075 }
    L_0x002d:
        r3 = r18.lookup(r19);	 Catch:{ all -> 0x0075 }
        if (r3 == 0) goto L_0x0045;
    L_0x0033:
        monitor-enter(r3);	 Catch:{ all -> 0x0075 }
        r13 = r3.address;	 Catch:{ all -> 0x0072 }
        if (r13 == 0) goto L_0x003c;
    L_0x0038:
        r13 = r3.alive;	 Catch:{ all -> 0x0072 }
        if (r13 != 0) goto L_0x0058;
    L_0x003c:
        r0 = r20;
        r3.address = r0;	 Catch:{ all -> 0x0072 }
        r0 = r21;
        r3.port = r0;	 Catch:{ all -> 0x0072 }
    L_0x0044:
        monitor-exit(r3);	 Catch:{ all -> 0x0072 }
    L_0x0045:
        if (r3 == 0) goto L_0x00b1;
    L_0x0047:
        r13 = 1;
        r0 = r22;
        if (r0 != r13) goto L_0x00b1;
    L_0x004c:
        r13 = r3 instanceof net.sf.fmj.media.rtp.RecvSSRCInfo;	 Catch:{ all -> 0x0075 }
        if (r13 != 0) goto L_0x00b1;
    L_0x0050:
        r13 = r3.ours;	 Catch:{ all -> 0x0075 }
        if (r13 == 0) goto L_0x00a2;
    L_0x0054:
        r8 = 0;
        monitor-exit(r18);	 Catch:{ all -> 0x0075 }
        r12 = r8;
    L_0x0057:
        return r12;
    L_0x0058:
        r13 = r3.address;	 Catch:{ all -> 0x0072 }
        r0 = r20;
        r13 = r13.equals(r0);	 Catch:{ all -> 0x0072 }
        if (r13 != 0) goto L_0x0044;
    L_0x0062:
        r13 = r3.probation;	 Catch:{ all -> 0x0072 }
        if (r13 <= 0) goto L_0x0078;
    L_0x0066:
        r13 = 2;
        r3.probation = r13;	 Catch:{ all -> 0x0072 }
        r0 = r20;
        r3.address = r0;	 Catch:{ all -> 0x0072 }
        r0 = r21;
        r3.port = r0;	 Catch:{ all -> 0x0072 }
        goto L_0x0044;
    L_0x0072:
        r13 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x0072 }
        throw r13;	 Catch:{ all -> 0x0075 }
    L_0x0075:
        r13 = move-exception;
    L_0x0076:
        monitor-exit(r18);	 Catch:{ all -> 0x0075 }
        throw r13;
    L_0x0078:
        r0 = r18;
        r13 = r0.stats;	 Catch:{ all -> 0x0072 }
        r14 = 4;
        r15 = 1;
        r13.update(r14, r15);	 Catch:{ all -> 0x0072 }
        r0 = r18;
        r13 = r0.transstats;	 Catch:{ all -> 0x0072 }
        r14 = r13.remote_coll;	 Catch:{ all -> 0x0072 }
        r14 = r14 + 1;
        r13.remote_coll = r14;	 Catch:{ all -> 0x0072 }
        r2 = new javax.media.rtp.event.RemoteCollisionEvent;	 Catch:{ all -> 0x0072 }
        r0 = r18;
        r13 = r0.sm;	 Catch:{ all -> 0x0072 }
        r14 = r3.ssrc;	 Catch:{ all -> 0x0072 }
        r14 = (long) r14;	 Catch:{ all -> 0x0072 }
        r2.m362init(r13, r14);	 Catch:{ all -> 0x0072 }
        r0 = r18;
        r13 = r0.eventhandler;	 Catch:{ all -> 0x0072 }
        r13.postEvent(r2);	 Catch:{ all -> 0x0072 }
        r12 = 0;
        monitor-exit(r3);	 Catch:{ all -> 0x0072 }
        monitor-exit(r18);	 Catch:{ all -> 0x0075 }
        goto L_0x0057;
    L_0x00a2:
        r7 = new net.sf.fmj.media.rtp.RecvSSRCInfo;	 Catch:{ all -> 0x0075 }
        r7.m873init(r3);	 Catch:{ all -> 0x0075 }
        r3 = r7;
        r0 = r18;
        r13 = r0.cache;	 Catch:{ all -> 0x0075 }
        r0 = r19;
        r13.put(r0, r3);	 Catch:{ all -> 0x0075 }
    L_0x00b1:
        if (r3 == 0) goto L_0x010a;
    L_0x00b3:
        r13 = 2;
        r0 = r22;
        if (r0 != r13) goto L_0x010a;
    L_0x00b8:
        r13 = r3 instanceof net.sf.fmj.media.rtp.PassiveSSRCInfo;	 Catch:{ all -> 0x0075 }
        if (r13 != 0) goto L_0x010a;
    L_0x00bc:
        r13 = r3.ours;	 Catch:{ all -> 0x0075 }
        if (r13 == 0) goto L_0x00c4;
    L_0x00c0:
        r9 = 0;
        monitor-exit(r18);	 Catch:{ all -> 0x0075 }
        r12 = r9;
        goto L_0x0057;
    L_0x00c4:
        r13 = java.lang.System.out;	 Catch:{ all -> 0x0075 }
        r14 = "changing to Passive";
        r13.println(r14);	 Catch:{ all -> 0x0075 }
        r13 = java.lang.System.out;	 Catch:{ all -> 0x0075 }
        r14 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0075 }
        r14.<init>();	 Catch:{ all -> 0x0075 }
        r15 = "existing one ";
        r14 = r14.append(r15);	 Catch:{ all -> 0x0075 }
        r14 = r14.append(r3);	 Catch:{ all -> 0x0075 }
        r14 = r14.toString();	 Catch:{ all -> 0x0075 }
        r13.println(r14);	 Catch:{ all -> 0x0075 }
        r7 = new net.sf.fmj.media.rtp.PassiveSSRCInfo;	 Catch:{ all -> 0x0075 }
        r7.m788init(r3);	 Catch:{ all -> 0x0075 }
        r13 = java.lang.System.out;	 Catch:{ all -> 0x0075 }
        r14 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0075 }
        r14.<init>();	 Catch:{ all -> 0x0075 }
        r15 = "new one is ";
        r14 = r14.append(r15);	 Catch:{ all -> 0x0075 }
        r14 = r14.append(r7);	 Catch:{ all -> 0x0075 }
        r14 = r14.toString();	 Catch:{ all -> 0x0075 }
        r13.println(r14);	 Catch:{ all -> 0x0075 }
        r3 = r7;
        r0 = r18;
        r13 = r0.cache;	 Catch:{ all -> 0x0075 }
        r0 = r19;
        r13.put(r0, r3);	 Catch:{ all -> 0x0075 }
    L_0x010a:
        r4 = r3;
        if (r4 != 0) goto L_0x0172;
    L_0x010d:
        r13 = 3;
        r0 = r22;
        if (r0 != r13) goto L_0x013c;
    L_0x0112:
        r0 = r18;
        r13 = r0.ourssrc;	 Catch:{ all -> 0x01c3 }
        if (r13 == 0) goto L_0x012b;
    L_0x0118:
        r0 = r18;
        r13 = r0.ourssrc;	 Catch:{ all -> 0x01c3 }
        r13 = r13.ssrc;	 Catch:{ all -> 0x01c3 }
        r0 = r19;
        if (r13 != r0) goto L_0x012b;
    L_0x0122:
        r0 = r18;
        r10 = r0.ourssrc;	 Catch:{ all -> 0x01c3 }
        monitor-exit(r18);	 Catch:{ all -> 0x01c3 }
        r3 = r4;
        r12 = r10;
        goto L_0x0057;
    L_0x012b:
        r3 = new net.sf.fmj.media.rtp.SendSSRCInfo;	 Catch:{ all -> 0x01c3 }
        r0 = r18;
        r1 = r19;
        r3.m878init(r0, r1);	 Catch:{ all -> 0x01c3 }
        r13 = net.sf.fmj.media.rtp.TrueRandom.nextInt();	 Catch:{ all -> 0x0075 }
        r3.initsource(r13);	 Catch:{ all -> 0x0075 }
        r4 = r3;
    L_0x013c:
        r13 = 1;
        r0 = r22;
        if (r0 != r13) goto L_0x014b;
    L_0x0141:
        r3 = new net.sf.fmj.media.rtp.RecvSSRCInfo;	 Catch:{ all -> 0x01c3 }
        r0 = r18;
        r1 = r19;
        r3.m872init(r0, r1);	 Catch:{ all -> 0x01c3 }
        r4 = r3;
    L_0x014b:
        r13 = 2;
        r0 = r22;
        if (r0 != r13) goto L_0x01c7;
    L_0x0150:
        r3 = new net.sf.fmj.media.rtp.PassiveSSRCInfo;	 Catch:{ all -> 0x01c3 }
        r0 = r18;
        r1 = r19;
        r3.m787init(r0, r1);	 Catch:{ all -> 0x01c3 }
    L_0x0159:
        if (r3 != 0) goto L_0x0160;
    L_0x015b:
        r11 = 0;
        monitor-exit(r18);	 Catch:{ all -> 0x0075 }
        r12 = r11;
        goto L_0x0057;
    L_0x0160:
        r0 = r20;
        r3.address = r0;	 Catch:{ all -> 0x0075 }
        r0 = r21;
        r3.port = r0;	 Catch:{ all -> 0x0075 }
        r0 = r18;
        r13 = r0.cache;	 Catch:{ all -> 0x0075 }
        r0 = r19;
        r13.put(r0, r3);	 Catch:{ all -> 0x0075 }
        r4 = r3;
    L_0x0172:
        r13 = r4.address;	 Catch:{ all -> 0x01c3 }
        if (r13 != 0) goto L_0x0182;
    L_0x0176:
        r13 = r4.port;	 Catch:{ all -> 0x01c3 }
        if (r13 != 0) goto L_0x0182;
    L_0x017a:
        r0 = r20;
        r4.address = r0;	 Catch:{ all -> 0x01c3 }
        r0 = r21;
        r4.port = r0;	 Catch:{ all -> 0x01c3 }
    L_0x0182:
        if (r6 == 0) goto L_0x01a8;
    L_0x0184:
        r5 = 0;
        r13 = r4 instanceof net.sf.fmj.media.rtp.RecvSSRCInfo;	 Catch:{ all -> 0x01c3 }
        if (r13 == 0) goto L_0x01ad;
    L_0x0189:
        r5 = new javax.media.rtp.event.LocalCollisionEvent;	 Catch:{ all -> 0x01c3 }
        r0 = r18;
        r14 = r0.sm;	 Catch:{ all -> 0x01c3 }
        r0 = r4;
        r0 = (javax.media.rtp.ReceiveStream) r0;	 Catch:{ all -> 0x01c3 }
        r13 = r0;
        r0 = r18;
        r15 = r0.ourssrc;	 Catch:{ all -> 0x01c3 }
        r15 = r15.ssrc;	 Catch:{ all -> 0x01c3 }
        r0 = (long) r15;	 Catch:{ all -> 0x01c3 }
        r16 = r0;
        r0 = r16;
        r5.m355init(r14, r13, r0);	 Catch:{ all -> 0x01c3 }
    L_0x01a1:
        r0 = r18;
        r13 = r0.eventhandler;	 Catch:{ all -> 0x01c3 }
        r13.postEvent(r5);	 Catch:{ all -> 0x01c3 }
    L_0x01a8:
        monitor-exit(r18);	 Catch:{ all -> 0x01c3 }
        r3 = r4;
        r12 = r4;
        goto L_0x0057;
    L_0x01ad:
        r5 = new javax.media.rtp.event.LocalCollisionEvent;	 Catch:{ all -> 0x01c3 }
        r0 = r18;
        r13 = r0.sm;	 Catch:{ all -> 0x01c3 }
        r14 = 0;
        r0 = r18;
        r15 = r0.ourssrc;	 Catch:{ all -> 0x01c3 }
        r15 = r15.ssrc;	 Catch:{ all -> 0x01c3 }
        r0 = (long) r15;	 Catch:{ all -> 0x01c3 }
        r16 = r0;
        r0 = r16;
        r5.m355init(r13, r14, r0);	 Catch:{ all -> 0x01c3 }
        goto L_0x01a1;
    L_0x01c3:
        r13 = move-exception;
        r3 = r4;
        goto L_0x0076;
    L_0x01c7:
        r3 = r4;
        goto L_0x0159;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.rtp.SSRCCache.get(int, java.net.InetAddress, int, int):net.sf.fmj.media.rtp.SSRCInfo");
    }

    /* access modifiers changed from: 0000 */
    public SSRCTable getMainCache() {
        return this.cache;
    }

    /* access modifiers changed from: 0000 */
    public RTPSourceInfoCache getRTPSICache() {
        return this.sourceInfoCache;
    }

    /* access modifiers changed from: 0000 */
    public int getSessionBandwidth() {
        if (this.sessionbandwidth != 0) {
            return this.sessionbandwidth;
        }
        throw new IllegalArgumentException("Session Bandwidth not set");
    }

    private void LocalCollision(int ssrc) {
        int newSSRC;
        do {
            newSSRC = (int) this.sm.generateSSRC(GenerateSSRCCause.LOCAL_COLLISION);
        } while (lookup(newSSRC) != null);
        SSRCInfo newinfo = new PassiveSSRCInfo(this.ourssrc);
        newinfo.ssrc = newSSRC;
        this.cache.put(newSSRC, newinfo);
        changessrc(newinfo);
        this.ourssrc = newinfo;
        this.stats.update(3, 1);
        OverallTransStats overallTransStats = this.transstats;
        overallTransStats.local_coll++;
    }

    /* access modifiers changed from: 0000 */
    public SSRCInfo lookup(int ssrc) {
        return (SSRCInfo) this.cache.get(ssrc);
    }

    /* access modifiers changed from: 0000 */
    public void remove(int ssrc) {
        SSRCInfo info = (SSRCInfo) this.cache.remove(ssrc);
        if (info != null) {
            info.delete();
        }
    }

    public void reset(int size) {
        this.initial = true;
        this.sendercount = 0;
        this.avgrtcpsize = size;
    }

    /* access modifiers changed from: 0000 */
    public void setclockrates() {
        int i;
        for (i = 0; i < 16; i++) {
            this.clockrate[i] = 8000;
        }
        this.clockrate[6] = 16000;
        this.clockrate[10] = 44100;
        this.clockrate[11] = 44100;
        this.clockrate[14] = 90000;
        this.clockrate[16] = 11025;
        this.clockrate[17] = 22050;
        this.clockrate[18] = 44100;
        for (i = 24; i < 34; i++) {
            this.clockrate[i] = 90000;
        }
        for (i = 96; i < 128; i++) {
            Format fmt = this.sm.formatinfo.get(i);
            if (fmt == null || !(fmt instanceof AudioFormat)) {
                this.clockrate[i] = 90000;
            } else {
                this.clockrate[i] = (int) ((AudioFormat) fmt).getSampleRate();
            }
        }
    }

    /* access modifiers changed from: declared_synchronized */
    public synchronized void updateavgrtcpsize(int size) {
        this.avgrtcpsize = (int) ((0.0625d * ((double) size)) + (0.9375d * ((double) this.avgrtcpsize)));
    }
}
