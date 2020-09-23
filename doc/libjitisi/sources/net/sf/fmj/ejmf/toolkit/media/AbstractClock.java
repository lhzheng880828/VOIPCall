package net.sf.fmj.ejmf.toolkit.media;

import javax.media.Clock;
import javax.media.ClockStartedError;
import javax.media.ClockStoppedException;
import javax.media.IncompatibleTimeBaseException;
import javax.media.Manager;
import javax.media.StopTimeSetError;
import javax.media.Time;
import javax.media.TimeBase;

public class AbstractClock implements Clock {
    private boolean isStarted = false;
    private Time mediaStartTime = new Time(0);
    private Time mediaStopTime = Clock.RESET;
    private float rate = 1.0f;
    private TimeBase systemtimebase = Manager.getSystemTimeBase();
    private Time timeBaseStartTime;
    private TimeBase timebase = this.systemtimebase;

    private synchronized Time calculateMediaTime() {
        Time time;
        long tbCurrent = this.timebase.getNanoseconds();
        long tbStart = this.timeBaseStartTime.getNanoseconds();
        if (tbCurrent < tbStart) {
            time = this.mediaStartTime;
        } else {
            time = new Time((long) ((((float) (tbCurrent - tbStart)) * this.rate) + ((float) this.mediaStartTime.getNanoseconds())));
        }
        return time;
    }

    public synchronized long getMediaNanoseconds() {
        return getMediaTime().getNanoseconds();
    }

    /* access modifiers changed from: protected */
    public Time getMediaStartTime() {
        return this.mediaStartTime;
    }

    public synchronized Time getMediaTime() {
        Time calculateMediaTime;
        if (this.isStarted) {
            calculateMediaTime = calculateMediaTime();
        } else {
            calculateMediaTime = this.mediaStartTime;
        }
        return calculateMediaTime;
    }

    public synchronized float getRate() {
        return this.rate;
    }

    public synchronized Time getStopTime() {
        return this.mediaStopTime;
    }

    public synchronized Time getSyncTime() {
        Time time;
        if (this.isStarted) {
            long startNano = this.timeBaseStartTime.getNanoseconds();
            long nowNano = getTimeBase().getNanoseconds();
            if (startNano >= nowNano) {
                time = new Time(nowNano - startNano);
            }
        }
        time = getMediaTime();
        return time;
    }

    public synchronized TimeBase getTimeBase() {
        return this.timebase;
    }

    /* access modifiers changed from: protected */
    public Time getTimeBaseStartTime() {
        return this.timeBaseStartTime;
    }

    public synchronized Time mapToTimeBase(Time t) throws ClockStoppedException {
        if (this.isStarted) {
        } else {
            throw new ClockStoppedException("Cannot map media time to time-base time on a Stopped Clock");
        }
        return new Time((long) ((((float) (t.getNanoseconds() - this.mediaStartTime.getNanoseconds())) / this.rate) + ((float) this.timeBaseStartTime.getNanoseconds())));
    }

    public synchronized void setMediaTime(Time t) {
        if (this.isStarted) {
            throw new ClockStartedError("Cannot set media time on a Started Clock");
        }
        this.mediaStartTime = t;
    }

    public synchronized float setRate(float rate) {
        if (this.isStarted) {
            throw new ClockStartedError("Cannot set rate on a Started Clock");
        }
        if (rate != 0.0f) {
            this.rate = rate;
        }
        return this.rate;
    }

    public synchronized void setStopTime(Time mediaStopTime) {
        if (!this.isStarted || this.mediaStopTime == RESET) {
            this.mediaStopTime = mediaStopTime;
        } else {
            throw new StopTimeSetError("Stop time may be set only once on a Started Clock");
        }
    }

    public synchronized void setTimeBase(TimeBase timebase) throws IncompatibleTimeBaseException {
        if (this.isStarted) {
            throw new ClockStartedError("Cannot set time base on a Started Clock");
        } else if (timebase == null) {
            this.timebase = this.systemtimebase;
        } else {
            this.timebase = timebase;
        }
    }

    public synchronized void stop() {
        if (this.isStarted) {
            this.mediaStartTime = calculateMediaTime();
            this.isStarted = false;
        }
    }

    public synchronized void syncStart(Time t) {
        if (this.isStarted) {
            throw new ClockStartedError("syncStart() cannot be called on a started Clock");
        }
        long now = getTimeBase().getNanoseconds();
        long start = t.getNanoseconds();
        if (start - now > 0) {
            this.timeBaseStartTime = new Time(start);
        } else {
            this.timeBaseStartTime = new Time(now);
        }
        this.isStarted = true;
    }
}
