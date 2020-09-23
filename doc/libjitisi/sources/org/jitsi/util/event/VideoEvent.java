package org.jitsi.util.event;

import java.util.EventObject;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.util.swing.VideoLayout;

public class VideoEvent extends EventObject {
    public static final int LOCAL = 1;
    public static final int REMOTE = 2;
    public static final int VIDEO_ADDED = 1;
    public static final int VIDEO_REMOVED = 2;
    private static final long serialVersionUID = 0;
    private boolean consumed;
    private final int origin;
    private final int type;
    private final Component visualComponent;

    public VideoEvent(Object source, int type, Component visualComponent, int origin) {
        super(source);
        this.type = type;
        this.visualComponent = visualComponent;
        this.origin = origin;
    }

    public VideoEvent clone(Object source) {
        return new VideoEvent(source, getType(), getVisualComponent(), getOrigin());
    }

    public void consume() {
        this.consumed = true;
    }

    public int getOrigin() {
        return this.origin;
    }

    public int getType() {
        return this.type;
    }

    public Component getVisualComponent() {
        return this.visualComponent;
    }

    public boolean isConsumed() {
        return this.consumed;
    }

    public static String originToString(int origin) {
        switch (origin) {
            case 1:
                return VideoLayout.LOCAL;
            case 2:
                return "REMOTE";
            default:
                throw new IllegalArgumentException("origin");
        }
    }

    public static String typeToString(int type) {
        switch (type) {
            case 1:
                return "VIDEO_ADDED";
            case 2:
                return "VIDEO_REMOVED";
            default:
                throw new IllegalArgumentException("type");
        }
    }
}
