package javax.media;

public class GainChangeEvent extends MediaEvent {
    GainControl eventSrc;
    float newDB;
    float newLevel;
    boolean newMute;

    public GainChangeEvent(GainControl from, boolean mute, float dB, float level) {
        super(from);
        this.eventSrc = from;
        this.newMute = mute;
        this.newDB = dB;
        this.newLevel = level;
    }

    public float getDB() {
        return this.newDB;
    }

    public float getLevel() {
        return this.newLevel;
    }

    public boolean getMute() {
        return this.newMute;
    }

    public Object getSource() {
        return this.eventSrc;
    }

    public GainControl getSourceGainControl() {
        return this.eventSrc;
    }
}
