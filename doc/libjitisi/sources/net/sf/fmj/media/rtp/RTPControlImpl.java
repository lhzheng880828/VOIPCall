package net.sf.fmj.media.rtp;

import java.util.Enumeration;
import java.util.Hashtable;
import javax.media.Format;
import javax.media.rtp.GlobalReceptionStats;
import javax.media.rtp.RTPControl;
import javax.media.rtp.ReceptionStats;
import net.sf.fmj.media.util.RTPInfo;
import org.jitsi.android.util.java.awt.Component;

public abstract class RTPControlImpl implements RTPControl, RTPInfo {
    String cname;
    String codec;
    Hashtable codeclist;
    Format currentformat;
    int payload;
    int rtptime;
    int seqno;
    SSRCInfo stream;

    public abstract String getCNAME();

    public abstract int getSSRC();

    public RTPControlImpl() {
        this.cname = null;
        this.codeclist = null;
        this.rtptime = 0;
        this.seqno = 0;
        this.payload = -1;
        this.codec = "";
        this.currentformat = null;
        this.stream = null;
        this.codeclist = new Hashtable(5);
    }

    public void addFormat(Format info, int payload) {
        this.codeclist.put(new Integer(payload), info);
    }

    public Component getControlComponent() {
        return null;
    }

    public Format getFormat() {
        return this.currentformat;
    }

    public Format getFormat(int payload) {
        return (Format) this.codeclist.get(new Integer(payload));
    }

    public Format[] getFormatList() {
        Format[] infolist = new Format[this.codeclist.size()];
        int i = 0;
        Enumeration e = this.codeclist.elements();
        while (e.hasMoreElements()) {
            int i2 = i + 1;
            infolist[i] = (Format) ((Format) e.nextElement()).clone();
            i = i2;
        }
        return infolist;
    }

    public GlobalReceptionStats getGlobalStats() {
        return null;
    }

    public ReceptionStats getReceptionStats() {
        if (this.stream == null) {
            return null;
        }
        return this.stream.getSourceReceptionStats();
    }

    public void setRTPInfo(int rtptime, int seqno) {
        this.rtptime = rtptime;
        this.seqno = seqno;
    }

    public String toString() {
        String s = "\n\tRTPTime is " + this.rtptime + "\n\tSeqno is " + this.seqno;
        if (this.codeclist != null) {
            return s + "\n\tCodecInfo is " + this.codeclist.toString();
        }
        return s + "\n\tcodeclist is null";
    }
}
