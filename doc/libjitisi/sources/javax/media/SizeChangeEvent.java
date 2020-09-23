package javax.media;

import javax.media.format.FormatChangeEvent;

public class SizeChangeEvent extends FormatChangeEvent {
    protected int height;
    protected float scale;
    protected int width;

    public SizeChangeEvent(Controller from, int width, int height, float scale) {
        super(from);
        this.width = width;
        this.height = height;
        this.scale = scale;
    }

    public int getHeight() {
        return this.height;
    }

    public float getScale() {
        return this.scale;
    }

    public int getWidth() {
        return this.width;
    }
}
