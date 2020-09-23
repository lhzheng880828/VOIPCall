package org.jitsi.impl.neomedia.control;

import java.util.ArrayList;
import java.util.List;
import javax.media.Controls;
import javax.media.Format;
import javax.media.control.FormatControl;
import org.jitsi.android.util.java.awt.Component;

public abstract class AbstractFormatControl implements FormatControl {
    private boolean enabled;

    public Component getControlComponent() {
        return null;
    }

    public static FormatControl[] getFormatControls(Controls controlsImpl) {
        List<FormatControl> formatControls = new ArrayList();
        for (Object control : controlsImpl.getControls()) {
            if (control instanceof FormatControl) {
                formatControls.add((FormatControl) control);
            }
        }
        return (FormatControl[]) formatControls.toArray(new FormatControl[formatControls.size()]);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Format setFormat(Format format) {
        return setFormat(this, format);
    }

    public static Format setFormat(FormatControl formatControl, Format format) {
        boolean formatIsSupported = false;
        if (format != null) {
            for (Format supportedFormat : formatControl.getSupportedFormats()) {
                if (supportedFormat.matches(format)) {
                    formatIsSupported = true;
                    break;
                }
            }
        }
        if (formatIsSupported) {
            return formatControl.getFormat();
        }
        return null;
    }
}
