package net.sf.fmj.media.rtp;

import java.util.Vector;
import javax.media.protocol.DataSource;
import javax.media.rtp.LocalParticipant;
import javax.media.rtp.Participant;
import javax.media.rtp.RTPStream;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceptionStats;
import javax.media.rtp.rtcp.Feedback;
import javax.media.rtp.rtcp.Report;
import javax.media.rtp.rtcp.SenderReport;

public class RecvSSRCInfo extends SSRCInfo implements ReceiveStream, SenderReport {
    RecvSSRCInfo(SSRCCache cache, int ssrc) {
        super(cache, ssrc);
    }

    RecvSSRCInfo(SSRCInfo info) {
        super(info);
    }

    public DataSource getDataSource() {
        return this.dsource;
    }

    public long getNTPTimeStampLSW() {
        return this.lastSRntptimestamp & 4294967295L;
    }

    public long getNTPTimeStampMSW() {
        return (this.lastSRntptimestamp >> 32) & 4294967295L;
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
        return this;
    }

    public ReceptionStats getSourceReceptionStats() {
        return this.stats;
    }

    public long getSSRC() {
        return (long) this.ssrc;
    }

    public RTPStream getStream() {
        return this;
    }
}
