package net.sf.fmj.ejmf.toolkit.util;

public interface TimeSource {
    public static final long MICROS_PER_SEC = 1000000;
    public static final long MILLIS_PER_SEC = 1000;
    public static final long NANOS_PER_SEC = 1000000000;

    long getConversionDivisor();

    long getTime();
}
