package javax.media;

import org.jitsi.android.util.java.awt.Component;

public interface Player extends MediaHandler, Controller {
    void addController(Controller controller) throws IncompatibleTimeBaseException;

    Component getControlPanelComponent();

    GainControl getGainControl();

    Component getVisualComponent();

    void removeController(Controller controller);

    void start();
}
