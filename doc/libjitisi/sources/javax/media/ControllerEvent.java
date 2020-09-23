package javax.media;

public class ControllerEvent extends MediaEvent {
    Controller eventSrc;

    public ControllerEvent(Controller from) {
        super(from);
        this.eventSrc = from;
    }

    public Object getSource() {
        return this.eventSrc;
    }

    public Controller getSourceController() {
        return this.eventSrc;
    }

    public String toString() {
        return getClass().getName() + "[source=" + this.eventSrc + "]";
    }
}
