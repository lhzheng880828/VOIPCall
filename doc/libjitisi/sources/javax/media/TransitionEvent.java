package javax.media;

public class TransitionEvent extends ControllerEvent {
    int currentState;
    int previousState;
    int targetState;

    public TransitionEvent(Controller from, int previousState, int currentState, int targetState) {
        super(from);
        this.previousState = previousState;
        this.currentState = currentState;
        this.targetState = targetState;
    }

    public int getCurrentState() {
        return this.currentState;
    }

    public int getPreviousState() {
        return this.previousState;
    }

    public int getTargetState() {
        return this.targetState;
    }

    public String toString() {
        return getClass().getName() + "[source=" + getSource() + ",previousState=" + this.previousState + ",currentState=" + this.currentState + ",targetState=" + this.targetState + "]";
    }
}
