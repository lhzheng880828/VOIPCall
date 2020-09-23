package net.java.sip.communicator.impl.protocol.sip;

import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationSetCusaxUtils;
import net.java.sip.communicator.service.protocol.ProtocolProviderActivator;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.util.Logger;

public class OperationSetCusaxUtilsSipImpl implements OperationSetCusaxUtils {
    private static final Logger logger = Logger.getLogger(OperationSetCusaxUtilsSipImpl.class);
    private final ProtocolProviderServiceSipImpl provider;

    public OperationSetCusaxUtilsSipImpl(ProtocolProviderServiceSipImpl provider) {
        this.provider = provider;
    }

    public boolean doesDetailBelong(Contact contact, String detailAddress) {
        return false;
    }

    public ProtocolProviderService getLinkedCusaxProvider() {
        String cusaxProviderID = this.provider.getAccountID().getAccountPropertyString("cusax.XMPP_ACCOUNT_ID");
        if (cusaxProviderID == null) {
            return null;
        }
        AccountID acc = ProtocolProviderActivator.getAccountManager().findAccountID(cusaxProviderID);
        if (acc == null) {
            logger.warn("No connected cusax account found for " + cusaxProviderID);
            return null;
        }
        for (ProtocolProviderService pProvider : ProtocolProviderActivator.getProtocolProviders()) {
            if (pProvider.getAccountID().equals(acc)) {
                return pProvider;
            }
        }
        return null;
    }
}
