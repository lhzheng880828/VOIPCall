package org.jitsi.util.event;

import org.jitsi.android.util.java.awt.Component;

public class SizeChangeVideoEvent extends VideoEvent {
    public static final int VIDEO_SIZE_CHANGE = 3;
    private static final long serialVersionUID = 0;
    private final int height;
    private final int width;

    public SizeChangeVideoEvent(Object source, Component visualComponent, int origin, int width, int height) {
        super(source, 3, visualComponent, origin);
        this.width = width;
        this.height = height;
    }

    public VideoEvent clone(Object source) {
        return new SizeChangeVideoEvent(source, getVisualComponent(), getOrigin(), getWidth(), getHeight());
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }
}
