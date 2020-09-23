package net.java.sip.communicator.impl.protocol.jabber;

import java.security.GeneralSecurityException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import net.java.sip.communicator.impl.protocol.jabber.sasl.SASLDigestMD5Mechanism;
import net.java.sip.communicator.service.certificate.CertificateService;
import net.java.sip.communicator.service.protocol.AbstractProtocolProviderService;
import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.SecurityAuthority;
import net.java.sip.communicator.service.protocol.UserCredentials;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

public class LoginByPasswordStrategy implements JabberLoginStrategy {
    private static Object modifySASLMechanisms = new Object();
    private static boolean saslMechanismsInitialized = false;
    private String DISABLE_CUSTOM_DIGEST_MD5_ACCOUNT_PROP = "DISABLE_CUSTOM_DIGEST_MD5";
    private String DISABLE_CUSTOM_DIGEST_MD5_CONFIG_PROP = "net.java.sip.communicator.impl.protocol.jabber.DISABLE_CUSTOM_DIGEST_MD5";
    private final AccountID accountID;
    private String password;
    private final AbstractProtocolProviderService protocolProvider;

    public LoginByPasswordStrategy(AbstractProtocolProviderService protocolProvider, AccountID accountID) {
        this.protocolProvider = protocolProvider;
        this.accountID = accountID;
    }

    public UserCredentials prepareLogin(SecurityAuthority authority, int reasonCode) {
        return loadPassword(authority, reasonCode);
    }

    public boolean loginPreparationSuccessful() {
        return this.password != null;
    }

    public boolean login(XMPPConnection connection, String userName, String resource) throws XMPPException {
        boolean disableCustomDigestMD5 = false;
        synchronized (modifySASLMechanisms) {
            boolean disableCustomDigestMD5PerAccount = this.accountID.getAccountPropertyBoolean(this.DISABLE_CUSTOM_DIGEST_MD5_ACCOUNT_PROP, false);
            if (!saslMechanismsInitialized || disableCustomDigestMD5PerAccount) {
                SASLAuthentication.supportSASLMechanism("PLAIN", 0);
                if (disableCustomDigestMD5PerAccount || JabberActivator.getConfigurationService().getBoolean(this.DISABLE_CUSTOM_DIGEST_MD5_CONFIG_PROP, false)) {
                    disableCustomDigestMD5 = true;
                }
                if (!disableCustomDigestMD5) {
                    SASLAuthentication.unregisterSASLMechanism("DIGEST-MD5");
                    SASLAuthentication.registerSASLMechanism("DIGEST-MD5", SASLDigestMD5Mechanism.class);
                    SASLAuthentication.supportSASLMechanism("DIGEST-MD5");
                }
                saslMechanismsInitialized = true;
            }
        }
        synchronized (modifySASLMechanisms) {
            connection.login(userName, this.password, resource);
        }
        return true;
    }

    public boolean isTlsRequired() {
        return !this.accountID.getAccountPropertyBoolean("ALLOW_NON_SECURE", false);
    }

    public SSLContext createSslContext(CertificateService cs, X509TrustManager trustManager) throws GeneralSecurityException {
        return cs.getSSLContext(trustManager);
    }

    private UserCredentials loadPassword(SecurityAuthority authority, int reasonCode) {
        UserCredentials cachedCredentials = null;
        this.password = JabberActivator.getProtocolProviderFactory().loadPassword(this.accountID);
        if (this.password == null) {
            UserCredentials credentials = new UserCredentials();
            credentials.setUserName(this.accountID.getUserID());
            credentials = authority.obtainCredentials(this.accountID.getDisplayName(), credentials, reasonCode);
            if (credentials == null) {
                this.protocolProvider.fireRegistrationStateChanged(this.protocolProvider.getRegistrationState(), RegistrationState.UNREGISTERED, 0, "No credentials provided");
                return null;
            }
            char[] pass = credentials.getPassword();
            if (pass == null) {
                this.protocolProvider.fireRegistrationStateChanged(this.protocolProvider.getRegistrationState(), RegistrationState.UNREGISTERED, 0, "No password entered");
                return null;
            }
            this.password = new String(pass);
            if (credentials.isPasswordPersistent()) {
                JabberActivator.getProtocolProviderFactory().storePassword(this.accountID, this.password);
            } else {
                cachedCredentials = credentials;
            }
        }
        return cachedCredentials;
    }
}
