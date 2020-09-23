package javax.media;

public class DurationUpdateEvent extends ControllerEvent {
    Time duration;

    public DurationUpdateEvent(Controller from, Time newDuration) {
        super(from);
        this.duration = newDuration;
    }

    public Time getDuration() {
        return this.duration;
    }

    public String toString() {
        return getClass().getName() + "[source=" + getSource() + ",duration=" + this.duration + "]";
    }
}
