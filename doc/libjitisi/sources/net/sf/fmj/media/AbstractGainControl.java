package net.sf.fmj.media;

import java.util.ArrayList;
import java.util.List;
import javax.media.GainChangeEvent;
import javax.media.GainChangeListener;
import javax.media.GainControl;
import org.jitsi.android.util.java.awt.Component;

public abstract class AbstractGainControl implements GainControl {
    private final List<GainChangeListener> listeners = new ArrayList();
    private boolean mute;
    private float savedLevelDuringMute;

    protected static float dBToLevel(float db) {
        return (float) Math.pow(10.0d, ((double) db) / 20.0d);
    }

    protected static float levelToDb(float level) {
        return (float) (Math.log10((double) level) * 20.0d);
    }

    public void addGainChangeListener(GainChangeListener listener) {
        synchronized (this.listeners) {
            this.listeners.add(listener);
        }
    }

    public Component getControlComponent() {
        return null;
    }

    public float getDB() {
        return levelToDb(getLevel());
    }

    public boolean getMute() {
        return this.mute;
    }

    /* access modifiers changed from: protected */
    public float getSavedLevelDuringMute() {
        return this.savedLevelDuringMute;
    }

    /* access modifiers changed from: protected */
    public void notifyListenersGainChangeEvent() {
        notifyListenersGainChangeEvent(new GainChangeEvent(this, getMute(), getDB(), getLevel()));
    }

    /* access modifiers changed from: protected */
    public void notifyListenersGainChangeEvent(GainChangeEvent event) {
        List<GainChangeListener> listenersCopy = new ArrayList();
        synchronized (this.listeners) {
            listenersCopy.addAll(this.listeners);
        }
        for (int i = 0; i < listenersCopy.size(); i++) {
            ((GainChangeListener) listenersCopy.get(i)).gainChange(event);
        }
    }

    public void removeGainChangeListener(GainChangeListener listener) {
        synchronized (this.listeners) {
            this.listeners.remove(listener);
        }
    }

    public float setDB(float gain) {
        setLevel(dBToLevel(gain));
        float result = getDB();
        notifyListenersGainChangeEvent();
        return result;
    }

    public void setMute(boolean mute) {
        if (mute != this.mute) {
            if (mute) {
                this.savedLevelDuringMute = getLevel();
                setLevel(0.0f);
                this.mute = true;
            } else {
                setLevel(this.savedLevelDuringMute);
                this.mute = false;
            }
            notifyListenersGainChangeEvent();
        }
    }
}
