package net.sf.fmj.media;

import javax.media.Controller;
import javax.media.StopEvent;
import javax.media.Time;

public class SeekFailedEvent extends StopEvent {
    public SeekFailedEvent(Controller from, int previous, int current, int target, Time mediaTime) {
        super(from, previous, current, target, mediaTime);
    }
}
