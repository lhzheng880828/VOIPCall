package net.java.sip.communicator.impl.protocol.jabber.extensions.colibri;

public enum RTPLevelRelayType {
    MIXER,
    TRANSLATOR;

    public static RTPLevelRelayType parseRTPLevelRelayType(String s) {
        if (s == null) {
            throw new NullPointerException("s");
        }
        for (RTPLevelRelayType v : values()) {
            if (v.toString().equalsIgnoreCase(s)) {
                return v;
            }
        }
        throw new IllegalArgumentException(s);
    }

    public String toString() {
        return name().toLowerCase();
    }
}
