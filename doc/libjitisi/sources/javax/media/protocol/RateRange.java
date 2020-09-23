package javax.media.protocol;

import java.io.Serializable;

public class RateRange implements Serializable {
    private boolean exact;
    private float max;
    private float min;
    private float value;

    public RateRange(float init, float min, float max, boolean isExact) {
        this.value = init;
        this.min = min;
        this.max = max;
        this.exact = isExact;
    }

    public RateRange(RateRange r) {
        this(r.value, r.min, r.max, r.exact);
    }

    public float getCurrentRate() {
        return this.value;
    }

    public float getMaximumRate() {
        return this.max;
    }

    public float getMinimumRate() {
        return this.min;
    }

    public boolean inRange(float rate) {
        throw new UnsupportedOperationException();
    }

    public boolean isExact() {
        return this.exact;
    }

    public float setCurrentRate(float rate) {
        this.value = rate;
        return this.value;
    }
}
