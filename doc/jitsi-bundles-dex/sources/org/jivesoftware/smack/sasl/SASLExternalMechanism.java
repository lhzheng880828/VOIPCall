package org.jivesoftware.smack.sasl;

import org.jivesoftware.smack.SASLAuthentication;

public class SASLExternalMechanism extends SASLMechanism {
    public SASLExternalMechanism(SASLAuthentication saslAuthentication) {
        super(saslAuthentication);
    }

    /* access modifiers changed from: protected */
    public String getName() {
        return "EXTERNAL";
    }
}
