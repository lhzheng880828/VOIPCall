package net.sf.fmj.media.rtp;

import java.io.IOException;
import java.util.Vector;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.DataSource;
import javax.media.rtp.LocalParticipant;
import javax.media.rtp.Participant;
import javax.media.rtp.RTPStream;
import javax.media.rtp.SendStream;
import javax.media.rtp.TransmissionStats;
import javax.media.rtp.rtcp.Feedback;
import javax.media.rtp.rtcp.Report;
import javax.media.rtp.rtcp.SenderReport;
import javax.media.rtp.rtcp.SourceDescription;
import net.sf.fmj.ejmf.toolkit.util.TimeSource;

public class SendSSRCInfo extends SSRCInfo implements SenderReport, SendStream {
    private static final int PACKET_SIZE = 4000;
    static AudioFormat dviAudio = new AudioFormat(AudioFormat.DVI_RTP);
    static AudioFormat g723Audio = new AudioFormat(AudioFormat.G723_RTP);
    static AudioFormat gsmAudio = new AudioFormat(AudioFormat.GSM_RTP);
    static AudioFormat mpegAudio = new AudioFormat(AudioFormat.MPEG_RTP);
    static VideoFormat mpegVideo = new VideoFormat(VideoFormat.MPEG_RTP);
    static AudioFormat ulawAudio = new AudioFormat(AudioFormat.ULAW_RTP);
    boolean inited;
    protected long lastBufSeq;
    protected long lastSeq;
    protected Format myformat;
    protected int packetsize;
    protected RTCPReporter rtcprep;
    protected RTPTransStats stats;
    protected long totalSamples;

    public SendSSRCInfo(SSRCCache cache, int ssrc) {
        super(cache, ssrc);
        this.inited = false;
        this.packetsize = 0;
        this.myformat = null;
        this.totalSamples = 0;
        this.lastSeq = -1;
        this.lastBufSeq = -1;
        this.stats = null;
        this.rtcprep = null;
        this.baseseq = TrueRandom.nextInt();
        this.maxseq = this.baseseq;
        this.lasttimestamp = TrueRandom.nextLong();
        this.sender = true;
        this.wassender = true;
        this.sinkstream = new RTPSinkStream();
        this.stats = new RTPTransStats();
    }

    public SendSSRCInfo(SSRCInfo info) {
        super(info);
        this.inited = false;
        this.packetsize = 0;
        this.myformat = null;
        this.totalSamples = 0;
        this.lastSeq = -1;
        this.lastBufSeq = -1;
        this.stats = null;
        this.rtcprep = null;
        this.baseseq = TrueRandom.nextInt();
        this.maxseq = this.baseseq;
        this.lasttimestamp = TrueRandom.nextLong();
        this.sender = true;
        this.wassender = true;
        this.sinkstream = new RTPSinkStream();
        this.stats = new RTPTransStats();
    }

    private int calculateSampleCount(Buffer b) {
        AudioFormat f = (AudioFormat) b.getFormat();
        if (f == null) {
            return -1;
        }
        long t = f.computeDuration((long) b.getLength());
        if (t == -1) {
            return -1;
        }
        if (f.getSampleRate() != -1.0d) {
            return (int) ((((double) t) * f.getSampleRate()) / 1.0E9d);
        }
        if (f.getFrameRate() != -1.0d) {
            return (int) ((((double) t) * f.getFrameRate()) / 1.0E9d);
        }
        return -1;
    }

    public void close() {
        try {
            stop();
        } catch (IOException e) {
        }
        getSSRCCache().sm.removeSendStream(this);
    }

    /* access modifiers changed from: protected */
    public void createDS() {
    }

    public DataSource getDataSource() {
        return this.pds;
    }

    public long getNTPTimeStampLSW() {
        return this.lastSRntptimestamp;
    }

    public long getNTPTimeStampMSW() {
        return this.lastSRntptimestamp >> 32;
    }

    public Participant getParticipant() {
        SSRCCache cache = getSSRCCache();
        if ((this.sourceInfo instanceof LocalParticipant) && cache.sm.IsNonParticipating()) {
            return null;
        }
        return this.sourceInfo;
    }

    public long getRTPTimeStamp() {
        return this.lastSRrtptimestamp;
    }

    public long getSenderByteCount() {
        return this.lastSRoctetcount;
    }

    public Feedback getSenderFeedback() {
        Feedback reportblk = null;
        Feedback feedback;
        try {
            Vector reports = getSSRCCache().sm.getLocalParticipant().getReports();
            for (int i = 0; i < reports.size(); i++) {
                Vector feedback2 = ((Report) reports.elementAt(i)).getFeedbackReports();
                for (int j = 0; j < feedback2.size(); j++) {
                    reportblk = (Feedback) feedback2.elementAt(j);
                    if (reportblk.getSSRC() == getSSRC()) {
                        return reportblk;
                    }
                }
            }
            feedback = reportblk;
            return null;
        } catch (NullPointerException e) {
            feedback = null;
            return null;
        }
    }

    public long getSenderPacketCount() {
        return this.lastSRpacketcount;
    }

    public SenderReport getSenderReport() {
        try {
            Vector reports = getSSRCCache().sm.getLocalParticipant().getReports();
            for (int i = 0; i < reports.size(); i++) {
                Report report = (Report) reports.elementAt(i);
                Vector feedback = report.getFeedbackReports();
                for (int j = 0; j < feedback.size(); j++) {
                    if (((Feedback) feedback.elementAt(j)).getSSRC() == getSSRC()) {
                        return (SenderReport) report;
                    }
                }
            }
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public long getSequenceNumber(Buffer b) {
        long seq = b.getSequenceNumber();
        if (this.lastSeq == -1) {
            this.lastSeq = (long) (((double) System.currentTimeMillis()) * Math.random());
            this.lastBufSeq = seq;
            return this.lastSeq;
        }
        if (seq - this.lastBufSeq > 1) {
            this.lastSeq += seq - this.lastBufSeq;
        } else {
            this.lastSeq++;
        }
        this.lastBufSeq = seq;
        return this.lastSeq;
    }

    public TransmissionStats getSourceTransmissionStats() {
        return this.stats;
    }

    public RTPStream getStream() {
        return this;
    }

    public long getTimeStamp(Buffer b) {
        if (b.getFormat() instanceof AudioFormat) {
            if (!mpegAudio.matches(b.getFormat())) {
                this.totalSamples += (long) calculateSampleCount(b);
                return this.totalSamples;
            } else if (b.getTimeStamp() >= 0) {
                return (b.getTimeStamp() * 90) / TimeSource.MICROS_PER_SEC;
            } else {
                return System.currentTimeMillis() * 90;
            }
        } else if (!(b.getFormat() instanceof VideoFormat)) {
            return b.getTimeStamp();
        } else {
            if (b.getTimeStamp() >= 0) {
                return (b.getTimeStamp() * 90) / TimeSource.MICROS_PER_SEC;
            }
            return System.currentTimeMillis() * 90;
        }
    }

    public int setBitRate(int rate) {
        if (this.sinkstream != null) {
            this.sinkstream.rate = rate;
        }
        return rate;
    }

    /* access modifiers changed from: protected */
    public void setFormat(Format fmt) {
        this.myformat = fmt;
        if (this.sinkstream != null) {
            int rate = 0;
            if (fmt instanceof AudioFormat) {
                if (ulawAudio.matches(fmt) || dviAudio.matches(fmt) || mpegAudio.matches(fmt)) {
                    rate = ((int) ((AudioFormat) fmt).getSampleRate()) * ((AudioFormat) fmt).getSampleSizeInBits();
                } else if (gsmAudio.matches(fmt)) {
                    rate = 13200;
                } else if (g723Audio.matches(fmt)) {
                    rate = 6300;
                }
                this.sinkstream.rate = rate;
                return;
            }
            return;
        }
        System.err.println("RTPSinkStream is NULL");
    }

    public void setSourceDescription(SourceDescription[] userdesclist) {
        super.setSourceDescription(userdesclist);
    }

    public void start() throws IOException {
        if (!this.inited) {
            this.inited = true;
            this.probation = 0;
            initsource(TrueRandom.nextInt());
            this.lasttimestamp = TrueRandom.nextLong();
        }
        if (this.pds != null) {
            this.pds.start();
        }
        if (this.sinkstream != null) {
            this.sinkstream.start();
        }
    }

    public void stop() throws IOException {
        if (this.pds != null) {
            this.pds.stop();
        }
        if (this.sinkstream != null) {
            this.sinkstream.stop();
        }
    }
}
