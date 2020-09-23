package org.jivesoftware.smack.sasl;

import org.jivesoftware.smack.SASLAuthentication;

public class SASLCramMD5Mechanism extends SASLMechanism {
    public SASLCramMD5Mechanism(SASLAuthentication saslAuthentication) {
        super(saslAuthentication);
    }

    /* access modifiers changed from: protected */
    public String getName() {
        return "CRAM-MD5";
    }
}
