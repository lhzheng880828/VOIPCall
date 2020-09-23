package net.sf.fmj.media.rtp;

import net.sf.fmj.media.rtp.util.SSRCTable;

public class StreamSynch {
    private static SSRCTable sources;

    public StreamSynch() {
        if (sources == null) {
            sources = new SSRCTable();
        }
    }

    public long calcTimestamp(int ssrc, int pt, long rtpTimestamp) {
        SynchSource source = (SynchSource) sources.get(ssrc);
        if (source == null) {
            return -1;
        }
        long rate = 1;
        if (pt >= 0 && pt <= 5) {
            rate = 8000;
        } else if (pt == 5) {
            rate = 8000;
        } else if (pt == 6) {
            rate = 16000;
        } else if (pt >= 7 && pt <= 9) {
            rate = 8000;
        } else if (pt >= 10 && pt <= 11) {
            rate = 44100;
        } else if (pt == 14) {
            rate = 90000;
        } else if (pt == 15) {
            rate = 8000;
        } else if (pt == 16) {
            rate = 11025;
        } else if (pt == 17) {
            rate = 22050;
        } else if (pt >= 25 && pt <= 26) {
            rate = 90000;
        } else if (pt == 28) {
            rate = 90000;
        } else if (pt >= 31 && pt <= 34) {
            rate = 90000;
        } else if (pt == 42) {
            rate = 90000;
        }
        return source.ntpTimestamp + (((((rtpTimestamp - source.rtpTimestamp) * 1000) * 1000) * 1000) / rate);
    }

    public void remove(int ssrc) {
        if (sources != null) {
            sources.remove(ssrc);
        }
    }

    public void update(int ssrc, long rtpTimestamp, long ntpTimestampMSW, long ntpTimestampLSW) {
        long ntpTimestamp = (1000000000 * ntpTimestampMSW) + ((long) (1.0E9d * (((double) ntpTimestampLSW) / 4.294967296E9d)));
        SynchSource source = (SynchSource) sources.get(ssrc);
        if (source == null) {
            sources.put(ssrc, new SynchSource(ssrc, rtpTimestamp, ntpTimestamp));
            return;
        }
        source.factor = (double) ((rtpTimestamp - source.rtpTimestamp) * (ntpTimestamp - source.ntpTimestamp));
        source.rtpTimestamp = rtpTimestamp;
        source.ntpTimestamp = ntpTimestamp;
    }
}
