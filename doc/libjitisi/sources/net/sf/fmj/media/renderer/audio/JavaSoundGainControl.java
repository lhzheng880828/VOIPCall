package net.sf.fmj.media.renderer.audio;

import net.sf.fmj.media.AbstractGainControl;
import org.jitsi.android.util.javax.sound.sampled.BooleanControl;
import org.jitsi.android.util.javax.sound.sampled.FloatControl;

class JavaSoundGainControl extends AbstractGainControl {
    private final boolean gainUnitsDb;
    private final FloatControl masterGainControl;
    private final float max;
    private final float min;
    private final BooleanControl muteControl;
    private final float range;

    public JavaSoundGainControl(FloatControl masterGainControl, BooleanControl muteControl) {
        this.masterGainControl = masterGainControl;
        this.muteControl = muteControl;
        if (masterGainControl != null) {
            this.min = masterGainControl.getMinimum();
            this.max = masterGainControl.getMaximum();
            this.gainUnitsDb = masterGainControl.getUnits().equals("dB");
        } else {
            this.max = 0.0f;
            this.min = 0.0f;
            this.gainUnitsDb = false;
        }
        this.range = this.max - this.min;
    }

    public float getDB() {
        if (this.masterGainControl == null) {
            return 0.0f;
        }
        if (this.gainUnitsDb) {
            return this.masterGainControl.getValue();
        }
        return AbstractGainControl.levelToDb(getLevel());
    }

    public float getLevel() {
        if (this.masterGainControl == null) {
            return 0.0f;
        }
        if (this.gainUnitsDb) {
            return AbstractGainControl.dBToLevel(this.masterGainControl.getValue());
        }
        return (this.masterGainControl.getValue() - this.min) / this.range;
    }

    public boolean getMute() {
        if (this.muteControl == null) {
            return false;
        }
        return this.muteControl.getValue();
    }

    public float setDB(float gain) {
        if (this.masterGainControl == null) {
            return 0.0f;
        }
        if (this.gainUnitsDb) {
            this.masterGainControl.setValue(gain);
        } else {
            setLevel(AbstractGainControl.dBToLevel(gain));
        }
        float result = getDB();
        notifyListenersGainChangeEvent();
        return result;
    }

    public float setLevel(float level) {
        if (this.masterGainControl == null) {
            return 0.0f;
        }
        if (this.gainUnitsDb) {
            this.masterGainControl.setValue(AbstractGainControl.levelToDb(level));
        } else {
            this.masterGainControl.setValue(this.min + (this.range * level));
        }
        float result = getLevel();
        notifyListenersGainChangeEvent();
        return result;
    }

    public void setMute(boolean mute) {
        if (this.muteControl != null) {
            this.muteControl.setValue(mute);
            notifyListenersGainChangeEvent();
        }
    }
}
