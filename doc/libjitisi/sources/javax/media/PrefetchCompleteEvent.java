package javax.media;

public class PrefetchCompleteEvent extends TransitionEvent {
    public PrefetchCompleteEvent(Controller from, int previous, int current, int target) {
        super(from, previous, current, target);
    }
}
