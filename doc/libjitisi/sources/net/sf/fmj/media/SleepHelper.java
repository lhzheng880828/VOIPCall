package net.sf.fmj.media;

import java.util.logging.Logger;
import net.sf.fmj.ejmf.toolkit.util.TimeSource;
import net.sf.fmj.utility.LoggerSingleton;

public class SleepHelper {
    public static long MILLI_TO_NANO = TimeSource.MICROS_PER_SEC;
    private static final Logger logger = LoggerSingleton.logger;
    private boolean firstNonDiscard = true;
    private long mStart;
    private long tbStart;

    public long calculateSleep(long currentTimestamp) {
        long tbNow = System.currentTimeMillis();
        if (!this.firstNonDiscard) {
            return (((long) (((double) (currentTimestamp - this.mStart)) / (1.0d * ((double) MILLI_TO_NANO)))) + this.tbStart) - tbNow;
        }
        this.mStart = currentTimestamp;
        this.tbStart = tbNow;
        this.firstNonDiscard = false;
        return 0;
    }

    public void reset() {
        this.mStart = 0;
        this.tbStart = 0;
        this.firstNonDiscard = true;
    }

    public void sleep(long currentTimestamp) throws InterruptedException {
        long sleep = calculateSleep(currentTimestamp);
        if (sleep > 0) {
            logger.finer("Sleeping " + sleep);
            Thread.sleep(sleep);
        }
    }
}
