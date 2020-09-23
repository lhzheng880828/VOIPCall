package javax.media;

public class ControllerErrorEvent extends ControllerClosedEvent {
    public ControllerErrorEvent(Controller from) {
        super(from);
    }

    public ControllerErrorEvent(Controller from, String why) {
        super(from, why);
    }

    public String toString() {
        return getClass().getName() + "[source=" + getSource() + ",message=" + this.message + "]";
    }
}
