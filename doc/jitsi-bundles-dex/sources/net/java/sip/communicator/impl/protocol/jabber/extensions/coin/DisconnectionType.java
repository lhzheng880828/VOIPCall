package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

public enum DisconnectionType {
    departed("departed"),
    booted("booted"),
    failed("failed"),
    busy("busy");
    
    private final String type;

    private DisconnectionType(String type) {
        this.type = type;
    }

    public String toString() {
        return this.type;
    }

    public static DisconnectionType parseString(String typeStr) throws IllegalArgumentException {
        for (DisconnectionType value : values()) {
            if (value.toString().equals(typeStr)) {
                return value;
            }
        }
        throw new IllegalArgumentException(typeStr + " is not a valid reason");
    }
}
