package org.jitsi.service.neomedia;

import com.lti.utils.UnsignedUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.media.GainChangeEvent;
import javax.media.GainChangeListener;
import javax.media.GainControl;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.event.VolumeChangeEvent;
import org.jitsi.service.neomedia.event.VolumeChangeListener;
import org.jitsi.util.Logger;

public class BasicVolumeControl implements VolumeControl, GainControl {
    protected static final float MAX_VOLUME_LEVEL = 1.0f;
    public static final int MAX_VOLUME_PERCENT = 200;
    protected static final float MIN_VOLUME_LEVEL = 0.0f;
    public static final int MIN_VOLUME_PERCENT = 0;
    private static final Logger logger = Logger.getLogger(BasicVolumeControl.class);
    private float db;
    private List<GainChangeListener> gainChangeListeners;
    private float gainReferenceLevel = getGainReferenceLevel();
    private boolean mute = false;
    private final List<WeakReference<VolumeChangeListener>> volumeChangeListeners = new ArrayList();
    protected float volumeLevel = getDefaultVolumeLevel();
    private final String volumeLevelConfigurationPropertyName;

    public static void applyGain(GainControl gainControl, byte[] buffer, int offset, int length) {
        if (gainControl.getMute()) {
            Arrays.fill(buffer, offset, offset + length, (byte) 0);
            return;
        }
        float level = gainControl.getLevel() * 2.0f;
        if (level != MAX_VOLUME_LEVEL) {
            int toIndex = offset + length;
            for (int i = offset; i < toIndex; i += 2) {
                short s;
                int i1 = i + 1;
                int si = (int) (((float) ((short) ((buffer[i] & UnsignedUtils.MAX_UBYTE) | (buffer[i1] << 8)))) * level);
                if (si > 32767) {
                    s = Short.MAX_VALUE;
                } else if (si < -32768) {
                    s = Short.MIN_VALUE;
                } else {
                    s = (short) si;
                }
                buffer[i] = (byte) s;
                buffer[i1] = (byte) (s >> 8);
            }
        }
    }

    private static float getDbFromPowerRatio(float powerLevelRequired, float referencePowerLevel) {
        return (float) (20.0d * Math.log10((double) Math.max(powerLevelRequired / referencePowerLevel, 1.0E-4f)));
    }

    protected static float getDefaultVolumeLevel() {
        return 0.5f;
    }

    protected static float getGainReferenceLevel() {
        return getDefaultVolumeLevel();
    }

    private static float getPowerRatioFromDb(float gainInDb, float referencePowerLevel) {
        return ((float) Math.pow(10.0d, (double) (gainInDb / 20.0f))) * referencePowerLevel;
    }

    public BasicVolumeControl(String volumeLevelConfigurationPropertyName) {
        this.volumeLevelConfigurationPropertyName = volumeLevelConfigurationPropertyName;
        loadVolume();
    }

    public void addGainChangeListener(GainChangeListener listener) {
        if (listener != null) {
            if (this.gainChangeListeners == null) {
                this.gainChangeListeners = new ArrayList();
            }
            this.gainChangeListeners.add(listener);
        }
    }

    public void addVolumeChangeListener(VolumeChangeListener listener) {
        synchronized (this.volumeChangeListeners) {
            Iterator<WeakReference<VolumeChangeListener>> i = this.volumeChangeListeners.iterator();
            boolean contains = false;
            while (i.hasNext()) {
                VolumeChangeListener l = (VolumeChangeListener) ((WeakReference) i.next()).get();
                if (l == null) {
                    i.remove();
                } else if (l.equals(listener)) {
                    contains = true;
                }
            }
            if (!contains) {
                this.volumeChangeListeners.add(new WeakReference(listener));
            }
        }
    }

    private void fireGainChange() {
        if (this.gainChangeListeners != null) {
            GainChangeEvent ev = new GainChangeEvent(this, this.mute, this.db, this.volumeLevel);
            for (GainChangeListener l : this.gainChangeListeners) {
                l.gainChange(ev);
            }
        }
    }

    private void fireVolumeChange() {
        VolumeChangeListener l;
        synchronized (this.volumeChangeListeners) {
            Iterator<WeakReference<VolumeChangeListener>> i = this.volumeChangeListeners.iterator();
            List<VolumeChangeListener> ls = new ArrayList(this.volumeChangeListeners.size());
            while (i.hasNext()) {
                l = (VolumeChangeListener) ((WeakReference) i.next()).get();
                if (l == null) {
                    i.remove();
                } else {
                    ls.add(l);
                }
            }
        }
        VolumeChangeEvent ev = new VolumeChangeEvent(this, this.volumeLevel, this.mute);
        for (VolumeChangeListener l2 : ls) {
            l2.volumeChange(ev);
        }
    }

    public Component getControlComponent() {
        return null;
    }

    public float getDB() {
        return this.db;
    }

    public float getLevel() {
        return this.volumeLevel;
    }

    public float getMaxValue() {
        return MAX_VOLUME_LEVEL;
    }

    public float getMinValue() {
        return 0.0f;
    }

    public boolean getMute() {
        return this.mute;
    }

    public float getVolume() {
        return this.volumeLevel;
    }

    /* access modifiers changed from: protected */
    public void loadVolume() {
        try {
            ConfigurationService cfg = LibJitsi.getConfigurationService();
            if (cfg != null) {
                String volumeLevelString = cfg.getString(this.volumeLevelConfigurationPropertyName);
                if (volumeLevelString != null) {
                    this.volumeLevel = Float.parseFloat(volumeLevelString);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Restored volume: " + this.volumeLevel);
                    }
                }
            }
        } catch (Throwable t) {
            logger.warn("Failed to restore volume", t);
        }
    }

    public void removeGainChangeListener(GainChangeListener listener) {
        if (listener != null && this.gainChangeListeners != null) {
            this.gainChangeListeners.remove(listener);
        }
    }

    public void removeVolumeChangeListener(VolumeChangeListener listener) {
        synchronized (this.volumeChangeListeners) {
            Iterator<WeakReference<VolumeChangeListener>> i = this.volumeChangeListeners.iterator();
            while (i.hasNext()) {
                VolumeChangeListener l = (VolumeChangeListener) ((WeakReference) i.next()).get();
                if (l == null || l.equals(listener)) {
                    i.remove();
                }
            }
        }
    }

    public float setDB(float gain) {
        if (this.db != gain) {
            this.db = gain;
            setVolumeLevel(getPowerRatioFromDb(gain, this.gainReferenceLevel));
        }
        return this.db;
    }

    public float setLevel(float level) {
        return setVolumeLevel(level);
    }

    public void setMute(boolean mute) {
        if (this.mute != mute) {
            this.mute = mute;
            fireVolumeChange();
            fireGainChange();
        }
    }

    public float setVolume(float value) {
        return setVolumeLevel(value);
    }

    private float setVolumeLevel(float value) {
        if (value < 0.0f) {
            value = 0.0f;
        } else if (value > MAX_VOLUME_LEVEL) {
            value = MAX_VOLUME_LEVEL;
        }
        if (this.volumeLevel == value) {
            return value;
        }
        this.volumeLevel = value;
        updateHardwareVolume();
        fireVolumeChange();
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        if (cfg != null) {
            cfg.setProperty(this.volumeLevelConfigurationPropertyName, String.valueOf(this.volumeLevel));
        }
        this.db = getDbFromPowerRatio(value, this.gainReferenceLevel);
        fireGainChange();
        return this.volumeLevel;
    }

    /* access modifiers changed from: protected */
    public void updateHardwareVolume() {
    }
}
