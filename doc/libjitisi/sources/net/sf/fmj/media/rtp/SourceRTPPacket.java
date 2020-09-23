package net.sf.fmj.media.rtp;

import net.sf.fmj.media.rtp.util.RTPPacket;

public class SourceRTPPacket {
    RTPPacket p;
    SSRCInfo ssrcinfo;

    public SourceRTPPacket(RTPPacket p, SSRCInfo ssrcinfo) {
        this.p = p;
        this.ssrcinfo = ssrcinfo;
    }
}
