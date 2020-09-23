package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

public enum StateType {
    full,
    partial,
    deleted;

    public static StateType parseString(String typeStr) throws IllegalArgumentException {
        for (StateType value : values()) {
            if (value.toString().equals(typeStr)) {
                return value;
            }
        }
        throw new IllegalArgumentException(typeStr + " is not a valid reason");
    }
}
