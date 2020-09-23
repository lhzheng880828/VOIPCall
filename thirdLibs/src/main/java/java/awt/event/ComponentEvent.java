package java.awt.event;

import java.awt.Component;

public class ComponentEvent extends AWTEvent {
    public ComponentEvent(Object source, int id) {
        super(source, id);
    }

    public Component getComponent() {
        return null;
    }
}
