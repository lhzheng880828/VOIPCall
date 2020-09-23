package net.sf.fmj.media.control;

public interface SliderRegionControl extends AtomicControl {
    long getMaxValue();

    long getMinValue();

    boolean isEnable();

    void setEnable(boolean z);

    long setMaxValue(long j);

    long setMinValue(long j);
}
