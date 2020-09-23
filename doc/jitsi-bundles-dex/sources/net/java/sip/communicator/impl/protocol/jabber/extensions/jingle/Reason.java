package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import org.jitsi.javax.sip.header.SubscriptionStateHeader;
import org.jivesoftware.smackx.Form;

public enum Reason {
    ALTERNATIVE_SESSION("alternative-session"),
    BUSY("busy"),
    CANCEL(Form.TYPE_CANCEL),
    CONNECTIVITY_ERROR("connectivity-error"),
    DECLINE("decline"),
    EXPIRED("expired"),
    FAILED_APPLICATION("failed-application"),
    FAILED_TRANSPORT("failed-transport"),
    GENERAL_ERROR("general-error"),
    GONE("gone"),
    INCOMPATIBLE_PARAMETERS("incompatible-parameters"),
    MEDIA_ERROR("media-error"),
    SECURITY_ERROR("security-error"),
    SUCCESS("success"),
    TIMEOUT(SubscriptionStateHeader.TIMEOUT),
    UNSUPPORTED_APPLICATIONS("unsupported-applications"),
    UNSUPPORTED_TRANSPORTS("unsupported-transports");
    
    private final String reasonValue;

    private Reason(String reasonValue) {
        this.reasonValue = reasonValue;
    }

    public String toString() {
        return this.reasonValue;
    }

    public static Reason parseString(String reasonValueStr) throws IllegalArgumentException {
        for (Reason value : values()) {
            if (value.toString().equals(reasonValueStr)) {
                return value;
            }
        }
        throw new IllegalArgumentException(reasonValueStr + " is not a valid reason");
    }
}
