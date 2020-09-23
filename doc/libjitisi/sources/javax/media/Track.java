package javax.media;

public interface Track extends Duration {
    public static final int FRAME_UNKNOWN = Integer.MAX_VALUE;
    public static final Time TIME_UNKNOWN = Time.TIME_UNKNOWN;

    Format getFormat();

    Time getStartTime();

    boolean isEnabled();

    Time mapFrameToTime(int i);

    int mapTimeToFrame(Time time);

    void readFrame(Buffer buffer);

    void setEnabled(boolean z);

    void setTrackListener(TrackListener trackListener);
}
