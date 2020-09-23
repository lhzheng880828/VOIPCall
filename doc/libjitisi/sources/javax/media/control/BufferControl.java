package javax.media.control;

import javax.media.Control;

public interface BufferControl extends Control {
    public static final long DEFAULT_VALUE = -1;
    public static final long MAX_VALUE = -2;

    long getBufferLength();

    boolean getEnabledThreshold();

    long getMinimumThreshold();

    long setBufferLength(long j);

    void setEnabledThreshold(boolean z);

    long setMinimumThreshold(long j);
}
