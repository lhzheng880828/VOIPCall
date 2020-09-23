package net.sf.fmj.media;

import javax.media.Buffer;
import javax.media.control.FrameGrabbingControl;
import javax.media.renderer.VideoRenderer;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.Rectangle;

public abstract class AbstractVideoRenderer extends AbstractRenderer implements VideoRenderer, FrameGrabbingControl {
    private Rectangle bounds = null;
    private Buffer lastBuffer;

    public abstract int doProcess(Buffer buffer);

    public abstract Component getComponent();

    public Rectangle getBounds() {
        return this.bounds;
    }

    public Component getControlComponent() {
        return null;
    }

    public Buffer grabFrame() {
        return this.lastBuffer;
    }

    public final int process(Buffer buffer) {
        this.lastBuffer = buffer;
        return doProcess(buffer);
    }

    public void setBounds(Rectangle rect) {
        this.bounds = rect;
    }

    public boolean setComponent(Component comp) {
        return false;
    }
}
