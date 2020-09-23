package net.java.sip.communicator.impl.protocol.sip;

import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.RegistrationState;
import org.jitsi.javax.sip.SipProvider;

public class SipRegistrarlessConnection extends SipRegistrarConnection {
    private RegistrationState currentRegistrationState = RegistrationState.UNREGISTERED;
    private String defaultTransport = null;
    private ProtocolProviderServiceSipImpl sipProvider = null;

    public SipRegistrarlessConnection(ProtocolProviderServiceSipImpl sipProviderCallback, String defaultTransport) {
        this.sipProvider = sipProviderCallback;
        this.defaultTransport = defaultTransport;
    }

    /* access modifiers changed from: 0000 */
    public void register() throws OperationFailedException {
        setRegistrationState(RegistrationState.REGISTERED, 0, null);
    }

    public void unregister() throws OperationFailedException {
        setRegistrationState(RegistrationState.UNREGISTERING, 0, "");
        setRegistrationState(RegistrationState.UNREGISTERED, 0, null);
    }

    public RegistrationState getRegistrationState() {
        return this.currentRegistrationState;
    }

    public void setRegistrationState(RegistrationState newState, int reasonCode, String reason) {
        if (!this.currentRegistrationState.equals(newState)) {
            RegistrationState oldState = this.currentRegistrationState;
            this.currentRegistrationState = newState;
            this.sipProvider.fireRegistrationStateChanged(oldState, newState, reasonCode, reason);
        }
    }

    public SipProvider getJainSipProvider() {
        return this.sipProvider.getJainSipProvider(getTransport());
    }

    public String getTransport() {
        return this.defaultTransport;
    }

    public String toString() {
        String className = getClass().getName();
        try {
            className = className.substring(className.lastIndexOf(46) + 1);
        } catch (Exception e) {
        }
        return className + "-[dn=" + this.sipProvider.getOurDisplayName() + " addr=" + this.sipProvider.getAccountID().getUserID() + "]";
    }

    public boolean isRegistrarless() {
        return true;
    }
}
