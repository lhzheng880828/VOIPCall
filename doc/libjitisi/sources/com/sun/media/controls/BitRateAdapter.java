package com.sun.media.controls;

import com.sun.media.ui.TextComp;
import javax.media.control.BitRateControl;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.event.ActionEvent;
import org.jitsi.android.util.java.awt.event.ActionListener;

public class BitRateAdapter implements BitRateControl, ActionListener {
    protected int max;
    protected int min;
    protected boolean settable;
    protected final TextComp textComp = new TextComp();
    protected int value;

    public BitRateAdapter(int value, int min, int max, boolean settable) {
        this.value = value;
        this.min = min;
        this.max = max;
        this.settable = settable;
    }

    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException();
    }

    public int getBitRate() {
        return this.value;
    }

    public Component getControlComponent() {
        throw new UnsupportedOperationException();
    }

    public int getMaxSupportedBitRate() {
        return this.max;
    }

    public int getMinSupportedBitRate() {
        return this.min;
    }

    public int setBitRate(int bitrate) {
        throw new UnsupportedOperationException();
    }
}
