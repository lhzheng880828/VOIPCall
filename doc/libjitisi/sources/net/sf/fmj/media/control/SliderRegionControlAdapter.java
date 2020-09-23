package net.sf.fmj.media.control;

import javax.media.Control;
import org.jitsi.android.util.java.awt.Component;

public class SliderRegionControlAdapter extends AtomicControlAdapter implements SliderRegionControl {
    boolean enable;
    long max;
    long min;

    public SliderRegionControlAdapter() {
        super(null, true, null);
        this.enable = true;
    }

    public SliderRegionControlAdapter(Component c, boolean def, Control parent) {
        super(c, def, parent);
    }

    public long getMaxValue() {
        return this.max;
    }

    public long getMinValue() {
        return this.min;
    }

    public boolean isEnable() {
        return this.enable;
    }

    public void setEnable(boolean f) {
        this.enable = f;
    }

    public long setMaxValue(long value) {
        this.max = value;
        informListeners();
        return this.max;
    }

    public long setMinValue(long value) {
        this.min = value;
        informListeners();
        return this.min;
    }
}
