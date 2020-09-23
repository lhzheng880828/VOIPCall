package net.sf.fmj.media.util;

import javax.media.Format;
import javax.media.format.AudioFormat;

public class ElapseTime {
    public long value = 0;

    public static long audioLenToTime(long len, AudioFormat af) {
        return af.computeDuration(len);
    }

    public static long audioTimeToLen(long duration, AudioFormat af) {
        long units;
        long bytesPerSec;
        if (af.getSampleSizeInBits() > 0) {
            units = (long) (af.getSampleSizeInBits() * af.getChannels());
            bytesPerSec = (long) ((((double) units) * af.getSampleRate()) / 8.0d);
        } else if (af.getFrameSizeInBits() == -1 || af.getFrameRate() == -1.0d) {
            bytesPerSec = 0;
            units = 0;
        } else {
            units = (long) af.getFrameSizeInBits();
            bytesPerSec = (long) ((((double) units) * af.getFrameRate()) / 8.0d);
        }
        if (bytesPerSec == 0) {
            return 0;
        }
        return (((duration * bytesPerSec) / 1000000000) / units) * units;
    }

    public long getValue() {
        return this.value;
    }

    public void setValue(long t) {
        this.value = t;
    }

    public boolean update(int len, long ts, Format f) {
        if (f instanceof AudioFormat) {
            long t = ((AudioFormat) f).computeDuration((long) len);
            if (t > 0) {
                this.value += t;
            } else if (ts <= 0) {
                return false;
            } else {
                this.value = ts;
            }
        } else if (ts <= 0) {
            return false;
        } else {
            this.value = ts;
        }
        return true;
    }
}
