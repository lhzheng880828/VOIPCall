package org.jivesoftware.smack.sasl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.harmony.javax.security.auth.callback.CallbackHandler;
import org.apache.harmony.javax.security.sasl.Sasl;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPException;

public class SASLGSSAPIMechanism extends SASLMechanism {
    public SASLGSSAPIMechanism(SASLAuthentication saslAuthentication) {
        super(saslAuthentication);
        System.setProperty("org.apache.harmony.javax.security.auth.useSubjectCredsOnly", "false");
        System.setProperty("java.security.auth.login.config", "gss.conf");
    }

    /* access modifiers changed from: protected */
    public String getName() {
        return "GSSAPI";
    }

    public void authenticate(String username, String host, CallbackHandler cbh) throws IOException, XMPPException {
        String[] mechanisms = new String[]{getName()};
        Map props = new HashMap();
        props.put("org.apache.harmony.javax.security.sasl.server.authentication", "TRUE");
        this.sc = Sasl.createSaslClient(mechanisms, username, "xmpp", host, props, cbh);
        authenticate();
    }

    public void authenticate(String username, String host, String password) throws IOException, XMPPException {
        String[] mechanisms = new String[]{getName()};
        Map props = new HashMap();
        props.put("org.apache.harmony.javax.security.sasl.server.authentication", "TRUE");
        this.sc = Sasl.createSaslClient(mechanisms, username, "xmpp", host, props, this);
        authenticate();
    }
}
