package org.jivesoftware.smack.sasl;

import java.io.IOException;
import java.util.HashMap;
import org.apache.harmony.javax.security.auth.callback.Callback;
import org.apache.harmony.javax.security.auth.callback.CallbackHandler;
import org.apache.harmony.javax.security.auth.callback.NameCallback;
import org.apache.harmony.javax.security.auth.callback.PasswordCallback;
import org.apache.harmony.javax.security.auth.callback.UnsupportedCallbackException;
import org.apache.harmony.javax.security.sasl.RealmCallback;
import org.apache.harmony.javax.security.sasl.RealmChoiceCallback;
import org.apache.harmony.javax.security.sasl.Sasl;
import org.apache.harmony.javax.security.sasl.SaslClient;
import org.apache.harmony.javax.security.sasl.SaslException;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.Base64;

public abstract class SASLMechanism implements CallbackHandler {
    protected String authenticationId;
    protected String hostname;
    protected String password;
    private SASLAuthentication saslAuthentication;
    protected SaslClient sc;

    public class AuthMechanism extends Packet {
        private final String authenticationText;
        private final String name;

        public AuthMechanism(String name, String authenticationText) {
            if (name == null) {
                throw new NullPointerException("SASL mechanism name shouldn't be null.");
            }
            this.name = name;
            this.authenticationText = authenticationText;
        }

        public String toXML() {
            StringBuilder stanza = new StringBuilder();
            stanza.append("<auth mechanism=\"").append(this.name);
            stanza.append("\" xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\">");
            if (this.authenticationText != null && this.authenticationText.trim().length() > 0) {
                stanza.append(this.authenticationText);
            }
            stanza.append("</auth>");
            return stanza.toString();
        }
    }

    public static class Challenge extends Packet {
        private final String data;

        public Challenge(String data) {
            this.data = data;
        }

        public String toXML() {
            StringBuilder stanza = new StringBuilder();
            stanza.append("<challenge xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\">");
            if (this.data != null && this.data.trim().length() > 0) {
                stanza.append(this.data);
            }
            stanza.append("</challenge>");
            return stanza.toString();
        }
    }

    public static class Failure extends Packet {
        private final String condition;

        public Failure(String condition) {
            this.condition = condition;
        }

        public String getCondition() {
            return this.condition;
        }

        public String toXML() {
            StringBuilder stanza = new StringBuilder();
            stanza.append("<failure xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\">");
            if (this.condition != null && this.condition.trim().length() > 0) {
                stanza.append(Separators.LESS_THAN).append(this.condition).append("/>");
            }
            stanza.append("</failure>");
            return stanza.toString();
        }
    }

    public class Response extends Packet {
        private final String authenticationText;

        public Response() {
            this.authenticationText = null;
        }

        public Response(String authenticationText) {
            if (authenticationText == null || authenticationText.trim().length() == 0) {
                this.authenticationText = null;
            } else {
                this.authenticationText = authenticationText;
            }
        }

        public String toXML() {
            StringBuilder stanza = new StringBuilder();
            stanza.append("<response xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\">");
            if (this.authenticationText != null) {
                stanza.append(this.authenticationText);
            }
            stanza.append("</response>");
            return stanza.toString();
        }
    }

    public static class Success extends Packet {
        private final String data;

        public Success(String data) {
            this.data = data;
        }

        public String toXML() {
            StringBuilder stanza = new StringBuilder();
            stanza.append("<success xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\">");
            if (this.data != null && this.data.trim().length() > 0) {
                stanza.append(this.data);
            }
            stanza.append("</success>");
            return stanza.toString();
        }
    }

    public abstract String getName();

    public SASLMechanism(SASLAuthentication saslAuthentication) {
        this.saslAuthentication = saslAuthentication;
    }

    public void authenticate(String username, String host, String password) throws IOException, XMPPException {
        this.authenticationId = username;
        this.password = password;
        this.hostname = host;
        String str = username;
        this.sc = Sasl.createSaslClient(new String[]{getName()}, str, "xmpp", host, new HashMap(), this);
        authenticate();
    }

    public void authenticate(String username, String host, CallbackHandler cbh) throws IOException, XMPPException {
        String str = username;
        this.sc = Sasl.createSaslClient(new String[]{getName()}, str, "xmpp", host, new HashMap(), cbh);
        authenticate();
    }

    /* access modifiers changed from: protected */
    public void authenticate() throws IOException, XMPPException {
        String authenticationText = null;
        try {
            if (this.sc.hasInitialResponse()) {
                authenticationText = Base64.encodeBytes(this.sc.evaluateChallenge(new byte[0]), 8);
            }
            getSASLAuthentication().send(new AuthMechanism(getName(), authenticationText));
        } catch (SaslException e) {
            throw new XMPPException("SASL authentication failed", e);
        }
    }

    public void challengeReceived(String challenge) throws IOException {
        byte[] response;
        Packet responseStanza;
        if (challenge != null) {
            response = this.sc.evaluateChallenge(Base64.decode(challenge));
        } else {
            response = this.sc.evaluateChallenge(new byte[0]);
        }
        if (response == null) {
            responseStanza = new Response();
        } else {
            responseStanza = new Response(Base64.encodeBytes(response, 8));
        }
        getSASLAuthentication().send(responseStanza);
    }

    /* access modifiers changed from: protected */
    public SASLAuthentication getSASLAuthentication() {
        return this.saslAuthentication;
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                callbacks[i].setName(this.authenticationId);
            } else if (callbacks[i] instanceof PasswordCallback) {
                callbacks[i].setPassword(this.password.toCharArray());
            } else if (callbacks[i] instanceof RealmCallback) {
                callbacks[i].setText(this.hostname);
            } else if (!(callbacks[i] instanceof RealmChoiceCallback)) {
                throw new UnsupportedCallbackException(callbacks[i]);
            }
        }
    }
}
