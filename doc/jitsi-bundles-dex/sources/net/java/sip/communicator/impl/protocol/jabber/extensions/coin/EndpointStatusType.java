package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import org.jitsi.javax.sip.header.SubscriptionStateHeader;

public enum EndpointStatusType {
    pending(SubscriptionStateHeader.PENDING),
    dialing_out("dialing-out"),
    dialing_in("dialing-in"),
    alerting("alerting"),
    on_hold("on-hold"),
    connected("connected"),
    muted_via_focus("mute-via-focus"),
    disconnecting("disconnecting"),
    disconnected("disconnected");
    
    private final String type;

    private EndpointStatusType(String type) {
        this.type = type;
    }

    public String toString() {
        return this.type;
    }

    public static EndpointStatusType parseString(String typeStr) throws IllegalArgumentException {
        for (EndpointStatusType value : values()) {
            if (value.toString().equals(typeStr)) {
                return value;
            }
        }
        throw new IllegalArgumentException(typeStr + " is not a valid reason");
    }
}
