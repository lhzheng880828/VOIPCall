package net.sf.fmj.media.rtp;

import java.util.Vector;
import javax.media.rtp.Participant;
import javax.media.rtp.RTPStream;
import javax.media.rtp.rtcp.SourceDescription;

public abstract class RTPSourceInfo implements Participant {
    private SourceDescription cname;
    RTPSourceInfoCache sic;
    private SSRCInfo[] ssrc = new SSRCInfo[0];

    RTPSourceInfo(String cname, RTPSourceInfoCache sic) {
        this.cname = new SourceDescription(1, cname, 0, false);
        this.sic = sic;
    }

    /* access modifiers changed from: declared_synchronized */
    public synchronized void addSSRC(SSRCInfo ssrcinfo) {
        for (SSRCInfo sSRCInfo : this.ssrc) {
            if (sSRCInfo == ssrcinfo) {
                break;
            }
        }
        SSRCInfo[] sSRCInfoArr = this.ssrc;
        SSRCInfo[] sSRCInfoArr2 = new SSRCInfo[(this.ssrc.length + 1)];
        this.ssrc = sSRCInfoArr2;
        System.arraycopy(sSRCInfoArr, 0, sSRCInfoArr2, 0, this.ssrc.length - 1);
        this.ssrc[this.ssrc.length - 1] = ssrcinfo;
    }

    public String getCNAME() {
        return this.cname.getDescription();
    }

    /* access modifiers changed from: 0000 */
    public SourceDescription getCNAMESDES() {
        return this.cname;
    }

    public Vector getReports() {
        Vector reportlist = new Vector();
        for (Object addElement : this.ssrc) {
            reportlist.addElement(addElement);
        }
        reportlist.trimToSize();
        return reportlist;
    }

    public Vector getSourceDescription() {
        Vector sdeslist;
        if (this.ssrc.length == 0) {
            sdeslist = new Vector(0);
            return sdeslist;
        }
        sdeslist = this.ssrc[0].getSourceDescription();
        Vector vector = sdeslist;
        return sdeslist;
    }

    /* access modifiers changed from: 0000 */
    public RTPStream getSSRCStream(long filterssrc) {
        int i = 0;
        while (i < this.ssrc.length) {
            if ((this.ssrc[i] instanceof RTPStream) && this.ssrc[i].ssrc == ((int) filterssrc)) {
                return (RTPStream) this.ssrc[i];
            }
            i++;
        }
        return null;
    }

    /* access modifiers changed from: 0000 */
    public int getStreamCount() {
        return this.ssrc.length;
    }

    public Vector getStreams() {
        Vector recvstreams = new Vector();
        for (int i = 0; i < this.ssrc.length; i++) {
            if (this.ssrc[i].isActive()) {
                recvstreams.addElement(this.ssrc[i]);
            }
        }
        recvstreams.trimToSize();
        return recvstreams;
    }

    /* access modifiers changed from: declared_synchronized */
    public synchronized void removeSSRC(SSRCInfo ssrcinfo) {
        if (ssrcinfo.dsource != null) {
            this.sic.ssrccache.sm.removeDataSource(ssrcinfo.dsource);
        }
        for (int i = 0; i < this.ssrc.length; i++) {
            if (this.ssrc[i] == ssrcinfo) {
                this.ssrc[i] = this.ssrc[this.ssrc.length - 1];
                SSRCInfo[] sSRCInfoArr = this.ssrc;
                SSRCInfo[] sSRCInfoArr2 = new SSRCInfo[(this.ssrc.length - 1)];
                this.ssrc = sSRCInfoArr2;
                System.arraycopy(sSRCInfoArr, 0, sSRCInfoArr2, 0, this.ssrc.length);
                break;
            }
        }
        if (this.ssrc.length == 0) {
            this.sic.remove(this.cname.getDescription());
        }
    }
}
