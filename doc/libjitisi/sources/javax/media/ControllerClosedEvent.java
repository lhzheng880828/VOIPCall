package javax.media;

public class ControllerClosedEvent extends ControllerEvent {
    protected String message;

    public ControllerClosedEvent(Controller from) {
        super(from);
    }

    public ControllerClosedEvent(Controller from, String why) {
        super(from);
        this.message = why;
    }

    public String getMessage() {
        return this.message;
    }
}
