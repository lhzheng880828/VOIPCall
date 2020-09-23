package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

public enum JingleAction {
    CONTENT_ACCEPT("content-accept"),
    CONTENT_ADD("content-add"),
    CONTENT_MODIFY("content-modify"),
    CONTENT_REJECT("content-reject"),
    CONTENT_REMOVE("content-remove"),
    DESCRIPTION_INFO("description-info"),
    SECURITY_INFO("security-info"),
    SESSION_ACCEPT("session-accept"),
    SESSION_INFO("session-info"),
    SESSION_INITIATE("session-initiate"),
    SESSION_TERMINATE("session-terminate"),
    TRANSPORT_ACCEPT("transport-accept"),
    TRANSPORT_INFO("transport-info"),
    TRANSPORT_REJECT("transport-reject"),
    TRANSPORT_REPLACE("transport-replace");
    
    private final String actionName;

    private JingleAction(String actionName) {
        this.actionName = actionName;
    }

    public String toString() {
        return this.actionName;
    }

    public static JingleAction parseString(String jingleActionStr) throws IllegalArgumentException {
        for (JingleAction value : values()) {
            if (value.toString().equals(jingleActionStr)) {
                return value;
            }
        }
        throw new IllegalArgumentException(jingleActionStr + " is not a valid jingle action");
    }
}
