package net.java.sip.communicator.impl.protocol.jabber;

import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetChangePassword;
import net.java.sip.communicator.util.Logger;
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.XMPPException;

public class OperationSetChangePasswordJabberImpl implements OperationSetChangePassword {
    private static final Logger logger = Logger.getLogger(OperationSetChangePasswordJabberImpl.class);
    private ProtocolProviderServiceJabberImpl protocolProvider;

    OperationSetChangePasswordJabberImpl(ProtocolProviderServiceJabberImpl protocolProvider) {
        this.protocolProvider = protocolProvider;
    }

    public void changePassword(String newPass) throws IllegalStateException, OperationFailedException {
        try {
            new AccountManager(this.protocolProvider.getConnection()).changePassword(newPass);
        } catch (XMPPException e) {
            if (logger.isInfoEnabled()) {
                logger.info("Tried to change jabber password, but the server does not support inband password changes", e);
            }
            throw new OperationFailedException("In-band password changes not supported", 18, e);
        }
    }

    public boolean supportsPasswordChange() {
        try {
            return this.protocolProvider.getDiscoveryManager().discoverInfo(this.protocolProvider.getAccountID().getService()).containsFeature(ProtocolProviderServiceJabberImpl.URN_REGISTER);
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("Exception occurred while trying to find out if inband registrations are supported. Returning trueanyway.");
            }
            return true;
        }
    }
}
