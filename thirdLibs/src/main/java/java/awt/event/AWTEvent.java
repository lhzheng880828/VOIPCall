package java.awt.event;

import java.util.EventObject;

public class AWTEvent extends EventObject {
    public AWTEvent(Object source, int id) {
        super(source);
    }
}
