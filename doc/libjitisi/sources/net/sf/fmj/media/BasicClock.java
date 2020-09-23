package net.sf.fmj.media;

import javax.media.CachingControl;
import javax.media.Clock;
import javax.media.ClockStartedError;
import javax.media.ClockStoppedException;
import javax.media.IncompatibleTimeBaseException;
import javax.media.StopTimeSetError;
import javax.media.SystemTimeBase;
import javax.media.Time;
import javax.media.TimeBase;

public class BasicClock implements Clock {
    public static final int STARTED = 1;
    public static final int STOPPED = 0;
    private TimeBase master = new SystemTimeBase();
    private long mediaLength = -1;
    private long mediaStart = 0;
    private long mediaTime = 0;
    private float rate = 1.0f;
    private long startTime = CachingControl.LENGTH_UNKNOWN;
    private long stopTime = CachingControl.LENGTH_UNKNOWN;

    public long getMediaNanoseconds() {
        if (getState() == 0) {
            return this.mediaTime;
        }
        long now = this.master.getNanoseconds();
        if (now <= this.startTime) {
            return this.mediaTime;
        }
        long t = ((long) (((double) (now - this.startTime)) * ((double) this.rate))) + this.mediaTime;
        if (this.mediaLength == -1 || t <= this.mediaStart + this.mediaLength) {
            return t;
        }
        return this.mediaStart + this.mediaLength;
    }

    public Time getMediaTime() {
        return new Time(getMediaNanoseconds());
    }

    public float getRate() {
        return this.rate;
    }

    public int getState() {
        if (this.startTime == CachingControl.LENGTH_UNKNOWN) {
            return 0;
        }
        if (this.stopTime == CachingControl.LENGTH_UNKNOWN) {
        }
        return 1;
    }

    public Time getStopTime() {
        return new Time(this.stopTime);
    }

    public Time getSyncTime() {
        return new Time(0);
    }

    public TimeBase getTimeBase() {
        return this.master;
    }

    public Time mapToTimeBase(Time t) throws ClockStoppedException {
        if (getState() != 0) {
            return new Time(((long) (((float) (t.getNanoseconds() - this.mediaTime)) / this.rate)) + this.startTime);
        }
        ClockStoppedException e = new ClockStoppedException();
        Log.dumpStack(e);
        throw e;
    }

    /* access modifiers changed from: protected */
    public void setMediaLength(long t) {
        this.mediaLength = t;
    }

    /* access modifiers changed from: protected */
    public void setMediaStart(long t) {
        this.mediaStart = t;
    }

    public void setMediaTime(Time now) {
        if (getState() == 1) {
            throwError(new ClockStartedError("setMediaTime() cannot be used on a started clock."));
        }
        long t = now.getNanoseconds();
        if (t < this.mediaStart) {
            this.mediaTime = this.mediaStart;
        } else if (this.mediaLength == -1 || t <= this.mediaStart + this.mediaLength) {
            this.mediaTime = t;
        } else {
            this.mediaTime = this.mediaStart + this.mediaLength;
        }
    }

    public float setRate(float factor) {
        if (getState() == 1) {
            throwError(new ClockStartedError("setRate() cannot be used on a started clock."));
        }
        this.rate = factor;
        return this.rate;
    }

    public void setStopTime(Time t) {
        if (getState() == 1 && this.stopTime != CachingControl.LENGTH_UNKNOWN) {
            throwError(new StopTimeSetError("setStopTime() may be set only once on a Started Clock"));
        }
        this.stopTime = t.getNanoseconds();
    }

    public void setTimeBase(TimeBase master) throws IncompatibleTimeBaseException {
        if (getState() == 1) {
            throwError(new ClockStartedError("setTimeBase cannot be used on a started clock."));
        }
        if (master != null) {
            this.master = master;
        } else if (!(this.master instanceof SystemTimeBase)) {
            this.master = new SystemTimeBase();
        }
    }

    public void stop() {
        if (getState() != 0) {
            this.mediaTime = getMediaNanoseconds();
            this.startTime = CachingControl.LENGTH_UNKNOWN;
        }
    }

    public void syncStart(Time tbt) {
        if (getState() == 1) {
            throwError(new ClockStartedError("syncStart() cannot be used on an already started clock."));
        }
        if (this.master.getNanoseconds() > tbt.getNanoseconds()) {
            this.startTime = this.master.getNanoseconds();
        } else {
            this.startTime = tbt.getNanoseconds();
        }
    }

    /* access modifiers changed from: protected */
    public void throwError(Error e) {
        Log.dumpStack(e);
        throw e;
    }
}
