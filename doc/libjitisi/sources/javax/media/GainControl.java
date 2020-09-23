package javax.media;

public interface GainControl extends Control {
    void addGainChangeListener(GainChangeListener gainChangeListener);

    float getDB();

    float getLevel();

    boolean getMute();

    void removeGainChangeListener(GainChangeListener gainChangeListener);

    float setDB(float f);

    float setLevel(float f);

    void setMute(boolean z);
}
