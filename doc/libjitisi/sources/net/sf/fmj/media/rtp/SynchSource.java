package net.sf.fmj.media.rtp;

import org.jitsi.impl.neomedia.portaudio.Pa;

class SynchSource {
    double factor = Pa.LATENCY_UNSPECIFIED;
    long ntpTimestamp;
    long rtpTimestamp;
    int ssrc;

    public SynchSource(int ssrc, long rtpTimestamp, long ntpTimestamp) {
        this.ssrc = ssrc;
        this.rtpTimestamp = rtpTimestamp;
        this.ntpTimestamp = ntpTimestamp;
    }
}
