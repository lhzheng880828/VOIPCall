package net.sf.fmj.media.rtp;

import java.net.InetAddress;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;
import javax.media.Format;
import javax.media.rtp.LocalParticipant;
import javax.media.rtp.Participant;
import javax.media.rtp.rtcp.Report;
import javax.media.rtp.rtcp.SourceDescription;
import net.sf.fmj.media.protocol.rtp.DataSource;
import net.sf.fmj.media.rtp.util.SSRCTable;
import org.jitsi.impl.neomedia.portaudio.Pa;

public abstract class SSRCInfo implements Report {
    static final int INITIALPROBATION = 2;
    static final int PAYLOAD_UNASSIGNED = -1;
    boolean active = false;
    InetAddress address;
    boolean aging;
    boolean alive = false;
    int baseseq;
    boolean byeReceived = false;
    long byeTime = 0;
    String byereason = null;
    int bytesreceived;
    private SSRCCache cache;
    int clockrate = 0;
    Format currentformat = null;
    int cycles = 0;
    DataSource dsource = null;
    RTPSourceStream dstream = null;
    SourceDescription email = null;
    boolean inactivesent = false;
    double jitter = Pa.LATENCY_UNSPECIFIED;
    long lastHeardFrom = 0;
    int lastPayloadType = -1;
    long lastRTCPreceiptTime = 0;
    long lastRTPReceiptTime;
    long lastSRntptimestamp = 0;
    long lastSRoctetcount = 0;
    long lastSRpacketcount = 0;
    long lastSRreceiptTime = 0;
    long lastSRrtptimestamp = 0;
    int lastbadseq;
    boolean lastsr = false;
    long lasttimestamp = 0;
    SourceDescription loc = null;
    int maxseq = 0;
    SourceDescription name = null;
    boolean newpartsent = false;
    boolean newrecvstream = false;
    SourceDescription note = null;
    boolean ours = false;
    int payloadType = -1;
    boolean payloadchange = false;
    javax.media.protocol.DataSource pds = null;
    SourceDescription phone = null;
    int port;
    int prevlost;
    int prevmaxseq;
    SourceDescription priv = null;
    int probation = 2;
    boolean quiet = false;
    int received;
    boolean recvstrmap = false;
    RTCPReporter reporter;
    SSRCTable reports = new SSRCTable();
    long rtptime;
    boolean sender = false;
    RTPSinkStream sinkstream = null;
    RTPSourceInfo sourceInfo = null;
    int ssrc;
    long starttime;
    RTPStats stats = null;
    boolean streamconnect = false;
    long systime;
    SourceDescription tool = null;
    boolean wassender = false;
    boolean wrapped = false;

    SSRCInfo(SSRCCache cache, int ssrc) {
        this.cache = cache;
        this.ssrc = ssrc;
        this.stats = new RTPStats();
    }

    SSRCInfo(SSRCInfo info) {
        this.cache = info.cache;
        this.alive = info.alive;
        this.sourceInfo = info.sourceInfo;
        if (this.sourceInfo != null) {
            this.sourceInfo.addSSRC(this);
        }
        this.cache.remove(info.ssrc);
        this.name = info.name;
        this.email = info.email;
        this.phone = info.phone;
        this.loc = info.loc;
        this.tool = info.tool;
        this.note = info.note;
        this.priv = info.priv;
        this.lastSRntptimestamp = info.lastSRntptimestamp;
        this.lastSRrtptimestamp = info.lastSRrtptimestamp;
        this.lastSRoctetcount = info.lastSRoctetcount;
        this.lastSRpacketcount = info.lastSRpacketcount;
        this.lastRTCPreceiptTime = info.lastRTCPreceiptTime;
        this.lastSRreceiptTime = info.lastSRreceiptTime;
        this.lastHeardFrom = info.lastHeardFrom;
        this.quiet = info.quiet;
        this.inactivesent = info.inactivesent;
        this.aging = info.aging;
        this.reports = info.reports;
        this.ours = info.ours;
        this.ssrc = info.ssrc;
        this.streamconnect = info.streamconnect;
        this.newrecvstream = info.newrecvstream;
        this.recvstrmap = info.recvstrmap;
        this.newpartsent = info.newpartsent;
        this.lastsr = info.lastsr;
        this.probation = info.probation;
        this.wassender = info.wassender;
        this.prevmaxseq = info.prevmaxseq;
        this.prevlost = info.prevlost;
        this.starttime = info.starttime;
        this.reporter = info.reporter;
        if (info.reporter != null) {
            this.reporter.transmit.setSSRCInfo(this);
        }
        this.payloadType = info.payloadType;
        this.dsource = info.dsource;
        this.pds = info.pds;
        this.dstream = info.dstream;
        this.lastRTPReceiptTime = info.lastRTPReceiptTime;
        this.maxseq = info.maxseq;
        this.cycles = info.cycles;
        this.baseseq = info.baseseq;
        this.lastbadseq = info.lastbadseq;
        this.received = info.received;
        this.lasttimestamp = info.lasttimestamp;
        this.lastPayloadType = info.lastPayloadType;
        this.jitter = info.jitter;
        this.bytesreceived = info.bytesreceived;
        this.address = info.address;
        this.port = info.port;
        this.stats = info.stats;
        this.clockrate = info.clockrate;
        this.byeTime = info.byeTime;
        this.byeReceived = info.byeReceived;
    }

    /* access modifiers changed from: 0000 */
    public void addSDESInfo(RTCPSDES chunk) {
        int ci = 0;
        while (ci < chunk.items.length && chunk.items[ci].type != 1) {
            ci++;
        }
        String s = new String(chunk.items[ci].data);
        String sourceinfocname = null;
        if (this.sourceInfo != null) {
            sourceinfocname = this.sourceInfo.getCNAME();
        }
        if (!(this.sourceInfo == null || s.equals(sourceinfocname))) {
            this.sourceInfo.removeSSRC(this);
            this.sourceInfo = null;
        }
        if (this.sourceInfo == null) {
            this.sourceInfo = this.cache.sourceInfoCache.get(s, this.ours);
            this.sourceInfo.addSSRC(this);
        }
        if (chunk.items.length > 1) {
            for (int i = 0; i < chunk.items.length; i++) {
                s = new String(chunk.items[i].data);
                switch (chunk.items[i].type) {
                    case 2:
                        if (this.name != null) {
                            this.name.setDescription(s);
                            break;
                        } else {
                            this.name = new SourceDescription(2, s, 0, false);
                            break;
                        }
                    case 3:
                        if (this.email != null) {
                            this.email.setDescription(s);
                            break;
                        } else {
                            this.email = new SourceDescription(3, s, 0, false);
                            break;
                        }
                    case 4:
                        if (this.phone != null) {
                            this.phone.setDescription(s);
                            break;
                        } else {
                            this.phone = new SourceDescription(4, s, 0, false);
                            break;
                        }
                    case 5:
                        if (this.loc != null) {
                            this.loc.setDescription(s);
                            break;
                        } else {
                            this.loc = new SourceDescription(5, s, 0, false);
                            break;
                        }
                    case 6:
                        if (this.tool != null) {
                            this.tool.setDescription(s);
                            break;
                        } else {
                            this.tool = new SourceDescription(6, s, 0, false);
                            break;
                        }
                    case 7:
                        if (this.note != null) {
                            this.note.setDescription(s);
                            break;
                        } else {
                            this.note = new SourceDescription(7, s, 0, false);
                            break;
                        }
                    case 8:
                        if (this.priv != null) {
                            this.priv.setDescription(s);
                            break;
                        } else {
                            this.priv = new SourceDescription(8, s, 0, false);
                            break;
                        }
                    default:
                        break;
                }
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void delete() {
        if (this.sourceInfo != null) {
            this.sourceInfo.removeSSRC(this);
        }
    }

    public String getCNAME() {
        return this.sourceInfo != null ? this.sourceInfo.getCNAME() : null;
    }

    public Vector getFeedbackReports() {
        Vector reportlist;
        if (this.reports.size() == 0) {
            reportlist = new Vector(0);
            return reportlist;
        }
        reportlist = new Vector(this.reports.size());
        Enumeration reportblks = this.reports.elements();
        while (reportblks.hasMoreElements()) {
            try {
                RTCPReportBlock[] reportblklist = (RTCPReportBlock[]) reportblks.nextElement();
                RTCPReportBlock report = new RTCPReportBlock();
                reportlist.addElement(reportblklist[0]);
            } catch (NoSuchElementException e) {
                System.err.println("No more elements");
            }
        }
        reportlist.trimToSize();
        Vector vector = reportlist;
        return reportlist;
    }

    public Participant getParticipant() {
        if ((this.sourceInfo instanceof LocalParticipant) && this.cache.sm.IsNonParticipating()) {
            return null;
        }
        return this.sourceInfo;
    }

    /* access modifiers changed from: 0000 */
    public int getPayloadType() {
        return this.payloadType;
    }

    /* access modifiers changed from: 0000 */
    public RTPSourceInfo getRTPSourceInfo() {
        return this.sourceInfo;
    }

    public Vector getSourceDescription() {
        Vector sdeslist = new Vector();
        sdeslist.addElement(this.sourceInfo.getCNAMESDES());
        if (this.name != null) {
            sdeslist.addElement(this.name);
        }
        if (this.email != null) {
            sdeslist.addElement(this.email);
        }
        if (this.phone != null) {
            sdeslist.addElement(this.phone);
        }
        if (this.loc != null) {
            sdeslist.addElement(this.loc);
        }
        if (this.tool != null) {
            sdeslist.addElement(this.tool);
        }
        if (this.note != null) {
            sdeslist.addElement(this.note);
        }
        if (this.priv != null) {
            sdeslist.addElement(this.priv);
        }
        sdeslist.trimToSize();
        return sdeslist;
    }

    public long getSSRC() {
        return (long) this.ssrc;
    }

    public SSRCCache getSSRCCache() {
        return this.cache;
    }

    private void InitSDES() {
        this.name = new SourceDescription(2, null, 0, false);
        this.email = new SourceDescription(3, null, 0, false);
        this.phone = new SourceDescription(4, null, 0, false);
        this.loc = new SourceDescription(5, null, 0, false);
        this.tool = new SourceDescription(6, null, 0, false);
        this.note = new SourceDescription(7, null, 0, false);
        this.priv = new SourceDescription(8, null, 0, false);
    }

    /* access modifiers changed from: 0000 */
    public void initsource(int seqnum) {
        if (this.probation <= 0) {
            this.active = true;
            setSender(true);
        }
        this.baseseq = seqnum;
        this.maxseq = seqnum - 1;
        this.lastbadseq = -2;
        this.cycles = 0;
        this.received = 0;
        this.bytesreceived = 0;
        this.lastRTPReceiptTime = 0;
        this.lasttimestamp = 0;
        this.jitter = Pa.LATENCY_UNSPECIFIED;
        this.prevmaxseq = this.maxseq;
        this.prevlost = 0;
    }

    /* access modifiers changed from: 0000 */
    public boolean isActive() {
        return this.active;
    }

    /* access modifiers changed from: 0000 */
    public void setAging(boolean beaging) {
        if (this.aging != beaging) {
            this.aging = beaging;
        }
    }

    /* access modifiers changed from: 0000 */
    public void setAlive(boolean bealive) {
        setAging(false);
        if (this.alive != bealive) {
            if (bealive) {
                this.reports.removeAll();
            } else {
                setSender(false);
            }
            this.alive = bealive;
        }
    }

    /* access modifiers changed from: 0000 */
    public void setOurs(boolean beours) {
        if (this.ours != beours) {
            if (beours) {
                setAlive(true);
            } else {
                setAlive(false);
            }
            this.ours = beours;
        }
    }

    /* access modifiers changed from: 0000 */
    public void setSender(boolean besender) {
        if (this.sender != besender) {
            SSRCCache sSRCCache;
            if (besender) {
                sSRCCache = this.cache;
                sSRCCache.sendercount++;
                setAlive(true);
            } else {
                sSRCCache = this.cache;
                sSRCCache.sendercount--;
            }
            this.sender = besender;
        }
    }

    /* access modifiers changed from: 0000 */
    public void setSourceDescription(SourceDescription[] userdesclist) {
        if (userdesclist != null) {
            SourceDescription currdesc;
            String cname = null;
            for (int i = 0; i < userdesclist.length; i++) {
                currdesc = userdesclist[i];
                if (currdesc != null && currdesc.getType() == 1) {
                    cname = userdesclist[i].getDescription();
                    break;
                }
            }
            String sourceinfocname = null;
            if (this.sourceInfo != null) {
                sourceinfocname = this.sourceInfo.getCNAME();
            }
            if (!(this.sourceInfo == null || cname == null || cname.equals(sourceinfocname))) {
                this.sourceInfo.removeSSRC(this);
                this.sourceInfo = null;
            }
            if (this.sourceInfo == null) {
                this.sourceInfo = this.cache.sourceInfoCache.get(cname, true);
                this.sourceInfo.addSSRC(this);
            }
            for (SourceDescription currdesc2 : userdesclist) {
                if (currdesc2 != null) {
                    switch (currdesc2.getType()) {
                        case 2:
                            if (this.name != null) {
                                this.name.setDescription(currdesc2.getDescription());
                                break;
                            } else {
                                this.name = new SourceDescription(2, currdesc2.getDescription(), 0, false);
                                break;
                            }
                        case 3:
                            if (this.email != null) {
                                this.email.setDescription(currdesc2.getDescription());
                                break;
                            } else {
                                this.email = new SourceDescription(3, currdesc2.getDescription(), 0, false);
                                break;
                            }
                        case 4:
                            if (this.phone != null) {
                                this.phone.setDescription(currdesc2.getDescription());
                                break;
                            } else {
                                this.phone = new SourceDescription(4, currdesc2.getDescription(), 0, false);
                                break;
                            }
                        case 5:
                            if (this.loc != null) {
                                this.loc.setDescription(currdesc2.getDescription());
                                break;
                            } else {
                                this.loc = new SourceDescription(5, currdesc2.getDescription(), 0, false);
                                break;
                            }
                        case 6:
                            if (this.tool != null) {
                                this.tool.setDescription(currdesc2.getDescription());
                                break;
                            } else {
                                this.tool = new SourceDescription(6, currdesc2.getDescription(), 0, false);
                                break;
                            }
                        case 7:
                            if (this.note != null) {
                                this.note.setDescription(currdesc2.getDescription());
                                break;
                            } else {
                                this.note = new SourceDescription(7, currdesc2.getDescription(), 0, false);
                                break;
                            }
                        case 8:
                            if (this.priv != null) {
                                this.priv.setDescription(currdesc2.getDescription());
                                break;
                            } else {
                                this.priv = new SourceDescription(8, currdesc2.getDescription(), 0, false);
                                break;
                            }
                        default:
                            break;
                    }
                }
            }
        }
    }
}
