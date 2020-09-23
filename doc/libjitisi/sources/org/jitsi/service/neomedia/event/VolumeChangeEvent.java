package org.jitsi.service.neomedia.event;

import java.util.EventObject;
import org.jitsi.service.neomedia.VolumeControl;

public class VolumeChangeEvent extends EventObject {
    private static final long serialVersionUID = 0;
    private final float level;
    private final boolean mute;

    public VolumeChangeEvent(VolumeControl source, float level, boolean mute) {
        super(source);
        this.level = level;
        this.mute = mute;
    }

    public VolumeControl getSourceVolumeControl() {
        return (VolumeControl) getSource();
    }

    public float getLevel() {
        return this.level;
    }

    public boolean getMute() {
        return this.mute;
    }
}
