package net.java.sip.communicator.impl.protocol.jabber.extensions.inputevt;

public enum InputEvtAction {
    NOTIFY("notify"),
    START("start"),
    STOP("stop");
    
    private final String actionName;

    private InputEvtAction(String actionName) {
        this.actionName = actionName;
    }

    public String toString() {
        return this.actionName;
    }

    public static InputEvtAction parseString(String inputActionStr) throws IllegalArgumentException {
        for (InputEvtAction value : values()) {
            if (value.toString().equals(inputActionStr)) {
                return value;
            }
        }
        throw new IllegalArgumentException(inputActionStr + " is not a valid Input action");
    }
}
