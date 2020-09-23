package org.jivesoftware.smack.sasl;

import org.jivesoftware.smack.SASLAuthentication;

public class SASLDigestMD5Mechanism extends SASLMechanism {
    public SASLDigestMD5Mechanism(SASLAuthentication saslAuthentication) {
        super(saslAuthentication);
    }

    /* access modifiers changed from: protected */
    public String getName() {
        return "DIGEST-MD5";
    }
}
