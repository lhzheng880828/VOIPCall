package net.java.sip.communicator.impl.protocol.sip;

import java.util.Hashtable;
import java.util.Map;
import net.java.sip.communicator.service.credentialsstorage.CredentialsStorageService;
import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.ProtocolProviderFactory;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.ServiceUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class ProtocolProviderFactorySipImpl extends ProtocolProviderFactory {
    private static final Logger logger = Logger.getLogger(ProtocolProviderFactorySipImpl.class);

    public ProtocolProviderFactorySipImpl() {
        super(SipActivator.getBundleContext(), "SIP");
    }

    /* access modifiers changed from: protected */
    public void storeAccount(AccountID accountID) {
        storeXCapPassword(accountID);
        ProtocolProviderFactorySipImpl.super.storeAccount(accountID);
    }

    private void storeXCapPassword(AccountID accountID) {
        String password = accountID.getAccountPropertyString("OPT_CLIST_PASSWORD");
        if (password != null) {
            ((CredentialsStorageService) ServiceUtils.getService(getBundleContext(), CredentialsStorageService.class)).storePassword(accountID.getAccountUniqueID() + ".xcap", password);
            accountID.removeAccountProperty("OPT_CLIST_PASSWORD");
        }
    }

    public AccountID installAccount(String userIDStr, Map<String, String> accountProperties) {
        if (SipActivator.getBundleContext() == null) {
            throw new NullPointerException("The specified BundleContext was null");
        } else if (userIDStr == null) {
            throw new NullPointerException("The specified AccountID was null");
        } else if (accountProperties == null) {
            throw new NullPointerException("The specified property map was null");
        } else {
            accountProperties.put("USER_ID", userIDStr);
            if (!accountProperties.containsKey("PROTOCOL_NAME")) {
                accountProperties.put("PROTOCOL_NAME", "SIP");
            }
            AccountID accountID = createAccountID(userIDStr, accountProperties);
            if (this.registeredAccounts.containsKey(accountID)) {
                throw new IllegalStateException("An account for id " + userIDStr + " was already installed!");
            }
            storeAccount(accountID, false);
            try {
                return loadAccount(accountProperties);
            } catch (RuntimeException exc) {
                removeStoredAccount(accountID);
                throw exc;
            }
        }
    }

    public void modifyAccount(ProtocolProviderService protocolProvider, Map<String, String> accountProperties) {
        BundleContext context = SipActivator.getBundleContext();
        if (context == null) {
            throw new NullPointerException("The specified BundleContext was null");
        } else if (protocolProvider == null) {
            throw new NullPointerException("The specified Protocol Provider was null");
        } else {
            SipAccountIDImpl accountID = (SipAccountIDImpl) protocolProvider.getAccountID();
            if (this.registeredAccounts.containsKey(accountID)) {
                ServiceRegistration registration = (ServiceRegistration) this.registeredAccounts.get(accountID);
                if (registration != null) {
                    try {
                        protocolProvider.shutdown();
                    } catch (Throwable th) {
                    }
                    registration.unregister();
                }
                if (accountProperties == null) {
                    throw new NullPointerException("The specified property map was null");
                }
                if (!accountProperties.containsKey("PROTOCOL_NAME")) {
                    accountProperties.put("PROTOCOL_NAME", "SIP");
                }
                Map<String, String> oldAcccountProps = accountID.getAccountProperties();
                accountID.setAccountProperties(accountProperties);
                storeAccount(accountID);
                String userIDStr = (String) accountProperties.get("USER_ID");
                Hashtable<String, String> properties = new Hashtable();
                properties.put("PROTOCOL_NAME", "SIP");
                properties.put("USER_ID", userIDStr);
                Exception initializationException = null;
                try {
                    ((ProtocolProviderServiceSipImpl) protocolProvider).initialize(userIDStr, accountID);
                } catch (Exception ex) {
                    initializationException = ex;
                    accountID.setAccountProperties(oldAcccountProps);
                }
                try {
                    storeAccount(accountID);
                    this.registeredAccounts.put(accountID, context.registerService(ProtocolProviderService.class.getName(), protocolProvider, properties));
                    if (initializationException != null) {
                        throw initializationException;
                    }
                } catch (Exception ex2) {
                    logger.error("Failed to initialize account", ex2);
                    throw new IllegalArgumentException("Failed to initialize account. " + ex2.getMessage());
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public AccountID createAccountID(String userID, Map<String, String> accountProperties) {
        return new SipAccountIDImpl(userID, accountProperties, (String) accountProperties.get("SERVER_ADDRESS"));
    }

    /* access modifiers changed from: protected */
    public ProtocolProviderService createService(String userID, AccountID accountID) {
        ProtocolProviderServiceSipImpl service = new ProtocolProviderServiceSipImpl();
        try {
            service.initialize(userID, (SipAccountIDImpl) accountID);
            storeAccount(accountID);
            return service;
        } catch (OperationFailedException ex) {
            logger.error("Failed to initialize account", ex);
            throw new IllegalArgumentException("Failed to initialize account" + ex.getMessage());
        }
    }
}
