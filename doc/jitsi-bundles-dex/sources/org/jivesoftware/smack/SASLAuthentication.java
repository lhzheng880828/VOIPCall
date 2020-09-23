package org.jivesoftware.smack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.harmony.javax.security.auth.callback.CallbackHandler;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.Bind;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Session;
import org.jivesoftware.smack.sasl.SASLAnonymous;
import org.jivesoftware.smack.sasl.SASLCramMD5Mechanism;
import org.jivesoftware.smack.sasl.SASLDigestMD5Mechanism;
import org.jivesoftware.smack.sasl.SASLExternalMechanism;
import org.jivesoftware.smack.sasl.SASLGSSAPIMechanism;
import org.jivesoftware.smack.sasl.SASLMechanism;
import org.jivesoftware.smack.sasl.SASLPlainMechanism;

public class SASLAuthentication implements UserAuthentication {
    private static Map<String, Class> implementedMechanisms = new HashMap();
    private static List<String> mechanismsPreferences = new ArrayList();
    private Connection connection;
    private SASLMechanism currentMechanism = null;
    private String errorCondition;
    private boolean resourceBinded;
    private boolean saslFailed;
    private boolean saslNegotiated;
    private Collection<String> serverMechanisms = new ArrayList();
    private boolean sessionSupported;

    static {
        registerSASLMechanism("EXTERNAL", SASLExternalMechanism.class);
        registerSASLMechanism("GSSAPI", SASLGSSAPIMechanism.class);
        registerSASLMechanism("DIGEST-MD5", SASLDigestMD5Mechanism.class);
        registerSASLMechanism("CRAM-MD5", SASLCramMD5Mechanism.class);
        registerSASLMechanism("PLAIN", SASLPlainMechanism.class);
        registerSASLMechanism("ANONYMOUS", SASLAnonymous.class);
        supportSASLMechanism("GSSAPI", 0);
        supportSASLMechanism("DIGEST-MD5", 1);
        supportSASLMechanism("CRAM-MD5", 2);
        supportSASLMechanism("PLAIN", 3);
        supportSASLMechanism("ANONYMOUS", 4);
    }

    public static void registerSASLMechanism(String name, Class mClass) {
        implementedMechanisms.put(name, mClass);
    }

    public static void unregisterSASLMechanism(String name) {
        implementedMechanisms.remove(name);
        mechanismsPreferences.remove(name);
    }

    public static void supportSASLMechanism(String name) {
        mechanismsPreferences.add(0, name);
    }

    public static void supportSASLMechanism(String name, int index) {
        mechanismsPreferences.add(index, name);
    }

    public static void unsupportSASLMechanism(String name) {
        mechanismsPreferences.remove(name);
    }

    public static List<Class> getRegisterSASLMechanisms() {
        List<Class> answer = new ArrayList();
        for (String mechanismsPreference : mechanismsPreferences) {
            answer.add(implementedMechanisms.get(mechanismsPreference));
        }
        return answer;
    }

    SASLAuthentication(Connection connection) {
        this.connection = connection;
        init();
    }

    public boolean hasAnonymousAuthentication() {
        return this.serverMechanisms.contains("ANONYMOUS");
    }

    public boolean hasNonAnonymousAuthentication() {
        return (this.serverMechanisms.isEmpty() || (this.serverMechanisms.size() == 1 && hasAnonymousAuthentication())) ? false : true;
    }

    public String authenticate(String username, String resource, CallbackHandler cbh) throws XMPPException {
        String selectedMechanism = null;
        for (String mechanism : mechanismsPreferences) {
            if (implementedMechanisms.containsKey(mechanism) && this.serverMechanisms.contains(mechanism)) {
                selectedMechanism = mechanism;
                break;
            }
        }
        if (selectedMechanism != null) {
            try {
                this.currentMechanism = (SASLMechanism) ((Class) implementedMechanisms.get(selectedMechanism)).getConstructor(new Class[]{SASLAuthentication.class}).newInstance(new Object[]{this});
                this.currentMechanism.authenticate(username, this.connection.getHost(), cbh);
                synchronized (this) {
                    if (!(this.saslNegotiated || this.saslFailed)) {
                        try {
                            wait(30000);
                        } catch (InterruptedException e) {
                        }
                    }
                }
                if (!this.saslFailed) {
                    if (this.saslNegotiated) {
                        return bindResourceAndEstablishSession(resource);
                    }
                    throw new XMPPException("SASL authentication failed");
                } else if (this.errorCondition != null) {
                    throw new XMPPException("SASL authentication " + selectedMechanism + " failed: " + this.errorCondition);
                } else {
                    throw new XMPPException("SASL authentication failed using mechanism " + selectedMechanism);
                }
            } catch (XMPPException e2) {
                throw e2;
            } catch (Exception e3) {
                e3.printStackTrace();
            }
        } else {
            throw new XMPPException("SASL Authentication failed. No known authentication mechanisims.");
        }
    }

    public String authenticate(String username, String password, String resource) throws XMPPException {
        String selectedMechanism = null;
        for (String mechanism : mechanismsPreferences) {
            if (implementedMechanisms.containsKey(mechanism) && this.serverMechanisms.contains(mechanism)) {
                selectedMechanism = mechanism;
                break;
            }
        }
        if (selectedMechanism == null) {
            return new NonSASLAuthentication(this.connection).authenticate(username, password, resource);
        }
        try {
            this.currentMechanism = (SASLMechanism) ((Class) implementedMechanisms.get(selectedMechanism)).getConstructor(new Class[]{SASLAuthentication.class}).newInstance(new Object[]{this});
            this.currentMechanism.authenticate(username, this.connection.getServiceName(), password);
            synchronized (this) {
                if (!(this.saslNegotiated || this.saslFailed)) {
                    try {
                        wait(30000);
                    } catch (InterruptedException e) {
                    }
                }
            }
            if (this.saslFailed) {
                if (this.errorCondition != null) {
                    throw new XMPPException("SASL authentication " + selectedMechanism + " failed: " + this.errorCondition);
                }
                throw new XMPPException("SASL authentication failed using mechanism " + selectedMechanism);
            } else if (this.saslNegotiated) {
                return bindResourceAndEstablishSession(resource);
            } else {
                return new NonSASLAuthentication(this.connection).authenticate(username, password, resource);
            }
        } catch (XMPPException e2) {
            throw e2;
        } catch (Exception e3) {
            e3.printStackTrace();
            return new NonSASLAuthentication(this.connection).authenticate(username, password, resource);
        }
    }

    public String authenticateAnonymously() throws XMPPException {
        try {
            this.currentMechanism = new SASLAnonymous(this);
            this.currentMechanism.authenticate(null, null, "");
            synchronized (this) {
                if (!(this.saslNegotiated || this.saslFailed)) {
                    try {
                        wait(5000);
                    } catch (InterruptedException e) {
                    }
                }
            }
            if (this.saslFailed) {
                if (this.errorCondition != null) {
                    throw new XMPPException("SASL authentication failed: " + this.errorCondition);
                }
                throw new XMPPException("SASL authentication failed");
            } else if (this.saslNegotiated) {
                return bindResourceAndEstablishSession(null);
            } else {
                return new NonSASLAuthentication(this.connection).authenticateAnonymously();
            }
        } catch (IOException e2) {
            return new NonSASLAuthentication(this.connection).authenticateAnonymously();
        }
    }

    private String bindResourceAndEstablishSession(String resource) throws XMPPException {
        synchronized (this) {
            if (!this.resourceBinded) {
                try {
                    wait(30000);
                } catch (InterruptedException e) {
                }
            }
        }
        if (this.resourceBinded) {
            Bind bindResource = new Bind();
            bindResource.setResource(resource);
            PacketCollector collector = this.connection.createPacketCollector(new PacketIDFilter(bindResource.getPacketID()));
            this.connection.sendPacket(bindResource);
            Bind response = (Bind) collector.nextResult((long) SmackConfiguration.getPacketReplyTimeout());
            collector.cancel();
            if (response == null) {
                throw new XMPPException("No response from the server.");
            } else if (response.getType() == Type.ERROR) {
                throw new XMPPException(response.getError());
            } else {
                String userJID = response.getJid();
                if (this.sessionSupported) {
                    Session session = new Session();
                    collector = this.connection.createPacketCollector(new PacketIDFilter(session.getPacketID()));
                    this.connection.sendPacket(session);
                    IQ ack = (IQ) collector.nextResult((long) SmackConfiguration.getPacketReplyTimeout());
                    collector.cancel();
                    if (ack == null) {
                        throw new XMPPException("No response from the server.");
                    } else if (ack.getType() != Type.ERROR) {
                        return userJID;
                    } else {
                        throw new XMPPException(ack.getError());
                    }
                }
                throw new XMPPException("Session establishment not offered by server");
            }
        }
        throw new XMPPException("Resource binding not offered by server");
    }

    /* access modifiers changed from: 0000 */
    public void setAvailableSASLMethods(Collection<String> mechanisms) {
        this.serverMechanisms = mechanisms;
    }

    public boolean isAuthenticated() {
        return this.saslNegotiated;
    }

    /* access modifiers changed from: 0000 */
    public void challengeReceived(String challenge) throws IOException {
        this.currentMechanism.challengeReceived(challenge);
    }

    /* access modifiers changed from: 0000 */
    public void authenticated() {
        synchronized (this) {
            this.saslNegotiated = true;
            notify();
        }
    }

    /* access modifiers changed from: 0000 */
    public void authenticationFailed() {
        authenticationFailed(null);
    }

    /* access modifiers changed from: 0000 */
    public void authenticationFailed(String condition) {
        synchronized (this) {
            this.saslFailed = true;
            this.errorCondition = condition;
            notify();
        }
    }

    /* access modifiers changed from: 0000 */
    public void bindingRequired() {
        synchronized (this) {
            this.resourceBinded = true;
            notify();
        }
    }

    public void send(Packet stanza) {
        this.connection.sendPacket(stanza);
    }

    /* access modifiers changed from: 0000 */
    public void sessionsSupported() {
        this.sessionSupported = true;
    }

    /* access modifiers changed from: protected */
    public void init() {
        this.saslNegotiated = false;
        this.saslFailed = false;
        this.resourceBinded = false;
        this.sessionSupported = false;
    }
}
