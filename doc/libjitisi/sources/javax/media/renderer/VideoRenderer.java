package javax.media.renderer;

import javax.media.Renderer;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.Rectangle;

public interface VideoRenderer extends Renderer {
    Rectangle getBounds();

    Component getComponent();

    void setBounds(Rectangle rectangle);

    boolean setComponent(Component component);
}
