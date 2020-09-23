package javax.media;

public class MediaTimeSetEvent extends ControllerEvent {
    Time mediaTime;

    public MediaTimeSetEvent(Controller from, Time newMediaTime) {
        super(from);
        this.mediaTime = newMediaTime;
    }

    public Time getMediaTime() {
        return this.mediaTime;
    }

    public String toString() {
        return getClass().getName() + "[source=" + getSource() + ",mediaTime=" + this.mediaTime + "]";
    }
}
