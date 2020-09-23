package org.jitsi.impl.neomedia.control;

import javax.media.Format;
import javax.media.control.FormatControl;
import org.jitsi.android.util.java.awt.Component;

public class ReadOnlyFormatControlDelegate implements FormatControl {
    private final FormatControl formatControl;

    public ReadOnlyFormatControlDelegate(FormatControl formatControl) {
        this.formatControl = formatControl;
    }

    public Component getControlComponent() {
        return this.formatControl.getControlComponent();
    }

    public Format getFormat() {
        return this.formatControl.getFormat();
    }

    public Format[] getSupportedFormats() {
        return this.formatControl.getSupportedFormats();
    }

    public boolean isEnabled() {
        return this.formatControl.isEnabled();
    }

    public void setEnabled(boolean enabled) {
    }

    public Format setFormat(Format format) {
        return AbstractFormatControl.setFormat(this, format);
    }
}
