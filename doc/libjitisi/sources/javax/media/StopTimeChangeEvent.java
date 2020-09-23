package javax.media;

public class StopTimeChangeEvent extends ControllerEvent {
    Time stopTime;

    public StopTimeChangeEvent(Controller from, Time newStopTime) {
        super(from);
        this.stopTime = newStopTime;
    }

    public Time getStopTime() {
        return this.stopTime;
    }

    public String toString() {
        return getClass().getName() + "[source=" + getSource() + ",stopTime=" + this.stopTime + "]";
    }
}
