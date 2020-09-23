package javax.media;

public class DataStarvedEvent extends StopEvent {
    public DataStarvedEvent(Controller from, int previous, int current, int target, Time mediaTime) {
        super(from, previous, current, target, mediaTime);
    }
}
