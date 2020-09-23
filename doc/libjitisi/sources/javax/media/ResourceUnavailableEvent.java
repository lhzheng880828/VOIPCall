package javax.media;

public class ResourceUnavailableEvent extends ControllerErrorEvent {
    public ResourceUnavailableEvent(Controller from) {
        super(from);
    }

    public ResourceUnavailableEvent(Controller from, String why) {
        super(from, why);
    }
}
