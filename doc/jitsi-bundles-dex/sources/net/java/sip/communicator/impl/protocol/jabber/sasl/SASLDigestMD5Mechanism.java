package net.java.sip.communicator.impl.protocol.jabber.sasl;

import java.io.IOException;
import java.util.HashMap;
import org.apache.harmony.javax.security.auth.callback.CallbackHandler;
import org.apache.harmony.javax.security.sasl.Sasl;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.sasl.SASLMechanism.Response;
import org.jivesoftware.smack.util.Base64;

public class SASLDigestMD5Mechanism extends org.jivesoftware.smack.sasl.SASLDigestMD5Mechanism {
    public SASLDigestMD5Mechanism(SASLAuthentication saslAuthentication) {
        super(saslAuthentication);
    }

    public void authenticate(String username, String host, String password) throws IOException, XMPPException {
        this.authenticationId = username;
        this.password = password;
        this.hostname = host;
        this.sc = Sasl.createSaslClient(new String[]{getName()}, null, "xmpp", host, new HashMap(), this);
        authenticate();
    }

    public void authenticate(String username, String host, CallbackHandler cbh) throws IOException, XMPPException {
        this.sc = Sasl.createSaslClient(new String[]{getName()}, null, "xmpp", host, new HashMap(), cbh);
        authenticate();
    }

    public void challengeReceived(String challenge) throws IOException {
        byte[] response;
        if (challenge != null) {
            response = this.sc.evaluateChallenge(Base64.decode(challenge));
        } else {
            response = this.sc.evaluateChallenge(null);
        }
        String authenticationText = null;
        if (response != null) {
            authenticationText = Base64.encodeBytes(response, 8);
        }
        if (authenticationText == null || authenticationText.equals("")) {
            authenticationText = Separators.EQUALS;
        }
        getSASLAuthentication().send(new Response(authenticationText));
    }
}
