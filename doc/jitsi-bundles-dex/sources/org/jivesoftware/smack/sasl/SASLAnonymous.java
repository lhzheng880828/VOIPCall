package org.jivesoftware.smack.sasl;

import java.io.IOException;
import org.apache.harmony.javax.security.auth.callback.CallbackHandler;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.sasl.SASLMechanism.AuthMechanism;
import org.jivesoftware.smack.sasl.SASLMechanism.Response;

public class SASLAnonymous extends SASLMechanism {
    public SASLAnonymous(SASLAuthentication saslAuthentication) {
        super(saslAuthentication);
    }

    /* access modifiers changed from: protected */
    public String getName() {
        return "ANONYMOUS";
    }

    public void authenticate(String username, String host, CallbackHandler cbh) throws IOException {
        authenticate();
    }

    public void authenticate(String username, String host, String password) throws IOException {
        authenticate();
    }

    /* access modifiers changed from: protected */
    public void authenticate() throws IOException {
        getSASLAuthentication().send(new AuthMechanism(getName(), null));
    }

    public void challengeReceived(String challenge) throws IOException {
        getSASLAuthentication().send(new Response());
    }
}
