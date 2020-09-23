package javax.media;

import net.sf.fmj.ejmf.toolkit.util.TimeSource;

public final class SystemTimeBase implements TimeBase {
    static long offset = (System.currentTimeMillis() * TimeSource.MICROS_PER_SEC);

    public long getNanoseconds() {
        return (System.currentTimeMillis() * TimeSource.MICROS_PER_SEC) - offset;
    }

    public Time getTime() {
        return new Time(getNanoseconds());
    }
}
