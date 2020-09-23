package net.sf.fmj.media;

import javax.media.Clock;
import javax.media.IncompatibleTimeBaseException;
import javax.media.Time;
import javax.media.TimeBase;

public abstract class BasicSinkModule extends BasicModule {
    private Clock clock;
    protected boolean prerolling = false;
    protected float rate = 1.0f;
    protected long stopTime = -1;

    public void doneReset() {
    }

    public void doSetMediaTime(Time t) {
        if (this.clock != null) {
            this.clock.setMediaTime(t);
        }
    }

    public float doSetRate(float r) {
        if (this.clock != null) {
            this.rate = this.clock.setRate(r);
        } else {
            this.rate = r;
        }
        return this.rate;
    }

    public void doStart() {
        super.doStart();
        if (this.clock != null) {
            this.clock.syncStart(this.clock.getTimeBase().getTime());
        }
    }

    public void doStop() {
        if (this.clock != null) {
            this.clock.stop();
        }
    }

    public Clock getClock() {
        return this.clock;
    }

    public long getMediaNanoseconds() {
        if (this.clock != null) {
            return this.clock.getMediaNanoseconds();
        }
        return this.controller.getMediaNanoseconds();
    }

    public Time getMediaTime() {
        if (this.clock != null) {
            return this.clock.getMediaTime();
        }
        return this.controller.getMediaTime();
    }

    public TimeBase getTimeBase() {
        if (this.clock != null) {
            return this.clock.getTimeBase();
        }
        return this.controller.getTimeBase();
    }

    /* access modifiers changed from: protected */
    public void setClock(Clock c) {
        this.clock = c;
    }

    public void setPreroll(long wanted, long actual) {
        if (actual < wanted) {
            this.prerolling = true;
        }
    }

    public void setStopTime(Time t) {
        if (t == Clock.RESET) {
            this.stopTime = -1;
        } else {
            this.stopTime = t.getNanoseconds();
        }
    }

    public void setTimeBase(TimeBase tb) throws IncompatibleTimeBaseException {
        if (this.clock != null) {
            this.clock.setTimeBase(tb);
        }
    }

    public void triggerReset() {
    }
}
