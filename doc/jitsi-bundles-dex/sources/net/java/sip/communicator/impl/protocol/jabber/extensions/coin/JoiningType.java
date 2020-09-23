package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

public enum JoiningType {
    dialed_in("dialed-in"),
    dialed_out("dialed-out"),
    focus_owner("focus-owner");
    
    private final String type;

    private JoiningType(String type) {
        this.type = type;
    }

    public String toString() {
        return this.type;
    }

    public static JoiningType parseString(String typeStr) throws IllegalArgumentException {
        for (JoiningType value : values()) {
            if (value.toString().equals(typeStr)) {
                return value;
            }
        }
        throw new IllegalArgumentException(typeStr + " is not a valid reason");
    }
}
