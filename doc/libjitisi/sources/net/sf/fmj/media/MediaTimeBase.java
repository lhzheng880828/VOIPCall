package net.sf.fmj.media;

import javax.media.SystemTimeBase;
import javax.media.Time;
import javax.media.TimeBase;

public abstract class MediaTimeBase implements TimeBase {
    long offset = 0;
    long origin = 0;
    TimeBase systemTimeBase = null;
    long time = 0;

    public abstract long getMediaTime();

    public MediaTimeBase() {
        mediaStopped();
    }

    public synchronized long getNanoseconds() {
        if (this.systemTimeBase != null) {
            this.time = (this.origin + this.systemTimeBase.getNanoseconds()) - this.offset;
        } else {
            this.time = (this.origin + getMediaTime()) - this.offset;
        }
        return this.time;
    }

    public Time getTime() {
        return new Time(getNanoseconds());
    }

    public synchronized void mediaStarted() {
        this.systemTimeBase = null;
        this.offset = getMediaTime();
        this.origin = this.time;
    }

    public synchronized void mediaStopped() {
        this.systemTimeBase = new SystemTimeBase();
        this.offset = this.systemTimeBase.getNanoseconds();
        this.origin = this.time;
    }
}
