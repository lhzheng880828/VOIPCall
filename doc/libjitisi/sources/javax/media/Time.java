package javax.media;

import java.io.Serializable;

public class Time implements Serializable {
    private static final double NANO_TO_SEC = 1.0E-9d;
    public static final long ONE_SECOND = 1000000000;
    public static final Time TIME_UNKNOWN = new Time((long) Buffer.SEQUENCE_UNKNOWN);
    protected long nanoseconds;

    public Time(double seconds) {
        this.nanoseconds = secondsToNanoseconds(seconds);
    }

    public Time(long nanoseconds) {
        this.nanoseconds = nanoseconds;
    }

    public long getNanoseconds() {
        return this.nanoseconds;
    }

    public double getSeconds() {
        return ((double) this.nanoseconds) * NANO_TO_SEC;
    }

    /* access modifiers changed from: protected */
    public long secondsToNanoseconds(double seconds) {
        return (long) (1.0E9d * seconds);
    }
}
