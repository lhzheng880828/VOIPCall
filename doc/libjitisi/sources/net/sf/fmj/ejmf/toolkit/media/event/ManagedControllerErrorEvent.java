package net.sf.fmj.ejmf.toolkit.media.event;

import javax.media.ControllerErrorEvent;
import javax.media.Player;

public class ManagedControllerErrorEvent extends ControllerErrorEvent {
    private ControllerErrorEvent event;

    public ManagedControllerErrorEvent(Player manager, ControllerErrorEvent event) {
        super(manager);
        this.event = event;
    }

    public ManagedControllerErrorEvent(Player manager, ControllerErrorEvent event, String message) {
        super(manager, message);
        this.event = event;
    }

    public ControllerErrorEvent getControllerErrorEvent() {
        return this.event;
    }
}
