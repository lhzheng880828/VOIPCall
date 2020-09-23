package javax.media.control;

import javax.media.Control;
import javax.media.Time;

public interface FramePositioningControl extends Control {
    public static final int FRAME_UNKNOWN = Integer.MAX_VALUE;
    public static final Time TIME_UNKNOWN = Time.TIME_UNKNOWN;

    Time mapFrameToTime(int i);

    int mapTimeToFrame(Time time);

    int seek(int i);

    int skip(int i);
}
