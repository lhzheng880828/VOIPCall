package net.sf.fmj.media;

import javax.media.Time;

public interface StateTransistor {
    void abortPrefetch();

    void abortRealize();

    void doClose();

    void doDealloc();

    void doFailedPrefetch();

    void doFailedRealize();

    boolean doPrefetch();

    boolean doRealize();

    void doSetMediaTime(Time time);

    float doSetRate(float f);

    void doStart();

    void doStop();
}
