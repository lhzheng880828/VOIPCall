package net.sf.fmj.media;

import javax.media.Buffer;
import javax.media.Duration;
import javax.media.Format;
import javax.media.Time;
import javax.media.Track;
import javax.media.TrackListener;

public abstract class AbstractTrack implements Track {
    private boolean enabled = true;
    private TrackListener trackListener;

    public abstract Format getFormat();

    public abstract void readFrame(Buffer buffer);

    public Time getDuration() {
        return Duration.DURATION_UNKNOWN;
    }

    public Time getStartTime() {
        return TIME_UNKNOWN;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public Time mapFrameToTime(int frameNumber) {
        return TIME_UNKNOWN;
    }

    public int mapTimeToFrame(Time t) {
        return Integer.MAX_VALUE;
    }

    public void setEnabled(boolean t) {
        this.enabled = t;
    }

    public void setTrackListener(TrackListener listener) {
        this.trackListener = listener;
    }
}
