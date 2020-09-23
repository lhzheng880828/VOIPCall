package org.jivesoftware.smack.sasl;

import org.jivesoftware.smack.SASLAuthentication;

public class SASLPlainMechanism extends SASLMechanism {
    public SASLPlainMechanism(SASLAuthentication saslAuthentication) {
        super(saslAuthentication);
    }

    /* access modifiers changed from: protected */
    public String getName() {
        return "PLAIN";
    }
}
