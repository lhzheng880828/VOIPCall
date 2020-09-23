package net.java.sip.communicator.impl.protocol.jabber;

import java.util.Hashtable;
import java.util.Map;
import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.ProtocolProviderFactory;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.util.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class ProtocolProviderFactoryJabberImpl extends ProtocolProviderFactory {
    public static final String IS_USE_JINGLE_NODES = "JINGLE_NODES_ENABLED";

    protected ProtocolProviderFactoryJabberImpl() {
        super(JabberActivator.getBundleContext(), "Jabber");
    }

    /* access modifiers changed from: protected */
    public void storeAccount(AccountID accountID) {
        ProtocolProviderFactoryJabberImpl.super.storeAccount(accountID);
    }

    public AccountID installAccount(String userIDStr, Map<String, String> accountProperties) {
        if (JabberActivator.getBundleContext() == null) {
            throw new NullPointerException("The specified BundleContext was null");
        } else if (userIDStr == null) {
            throw new NullPointerException("The specified AccountID was null");
        } else if (accountProperties == null) {
            throw new NullPointerException("The specified property map was null");
        } else {
            accountProperties.put("USER_ID", userIDStr);
            if (accountProperties.get("SERVER_ADDRESS") == null) {
                if (StringUtils.parseServer(userIDStr) != null) {
                    accountProperties.put("SERVER_ADDRESS", StringUtils.parseServer(userIDStr));
                } else {
                    throw new IllegalArgumentException("Should specify a server for user name " + userIDStr + Separators.DOT);
                }
            }
            if (accountProperties.get("SERVER_PORT") == null) {
                accountProperties.put("SERVER_PORT", "5222");
            }
            AccountID accountID = new JabberAccountIDImpl(userIDStr, accountProperties);
            if (this.registeredAccounts.containsKey(accountID)) {
                throw new IllegalStateException("An account for id " + userIDStr + " was already installed!");
            }
            storeAccount(accountID, false);
            return loadAccount(accountProperties);
        }
    }

    /* access modifiers changed from: protected */
    public AccountID createAccountID(String userID, Map<String, String> accountProperties) {
        return new JabberAccountIDImpl(userID, accountProperties);
    }

    /* access modifiers changed from: protected */
    public ProtocolProviderService createService(String userID, AccountID accountID) {
        ProtocolProviderServiceJabberImpl service = new ProtocolProviderServiceJabberImpl();
        service.initialize(userID, accountID);
        return service;
    }

    public void modifyAccount(ProtocolProviderService protocolProvider, Map<String, String> accountProperties) throws NullPointerException {
        BundleContext context = JabberActivator.getBundleContext();
        if (context == null) {
            throw new NullPointerException("The specified BundleContext was null");
        } else if (protocolProvider == null) {
            throw new NullPointerException("The specified Protocol Provider was null");
        } else {
            JabberAccountIDImpl accountID = (JabberAccountIDImpl) protocolProvider.getAccountID();
            if (this.registeredAccounts.containsKey(accountID)) {
                ServiceRegistration registration = (ServiceRegistration) this.registeredAccounts.get(accountID);
                if (registration != null) {
                    try {
                        if (protocolProvider.isRegistered()) {
                            protocolProvider.unregister();
                            protocolProvider.shutdown();
                        }
                    } catch (Throwable th) {
                    }
                    registration.unregister();
                }
                if (accountProperties == null) {
                    throw new NullPointerException("The specified property map was null");
                }
                accountProperties.put("USER_ID", accountID.getUserID());
                if (((String) accountProperties.get("SERVER_ADDRESS")) == null) {
                    throw new NullPointerException("null is not a valid ServerAddress");
                }
                if (accountProperties.get("SERVER_PORT") == null) {
                    accountProperties.put("SERVER_PORT", "5222");
                }
                if (!accountProperties.containsKey("PROTOCOL_NAME")) {
                    accountProperties.put("PROTOCOL_NAME", "Jabber");
                }
                accountID.setAccountProperties(accountProperties);
                storeAccount(accountID);
                Hashtable<String, String> properties = new Hashtable();
                properties.put("PROTOCOL_NAME", "Jabber");
                properties.put("USER_ID", accountID.getUserID());
                ((ProtocolProviderServiceJabberImpl) protocolProvider).initialize(accountID.getUserID(), accountID);
                storeAccount(accountID);
                this.registeredAccounts.put(accountID, context.registerService(ProtocolProviderService.class.getName(), protocolProvider, properties));
            }
        }
    }
}
