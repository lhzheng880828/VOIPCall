package net.java.sip.communicator.impl.protocol.jabber;

import java.security.GeneralSecurityException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import net.java.sip.communicator.service.certificate.CertificateService;
import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.SecurityAuthority;
import net.java.sip.communicator.service.protocol.UserCredentials;
import org.jitsi.util.Logger;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

class LoginByClientCertificateStrategy implements JabberLoginStrategy {
    private static final Logger logger = Logger.getLogger(LoginByClientCertificateStrategy.class);
    private AccountID accountID;

    public LoginByClientCertificateStrategy(AccountID accountID) {
        this.accountID = accountID;
    }

    public UserCredentials prepareLogin(SecurityAuthority authority, int reasonCode) {
        return null;
    }

    public boolean loginPreparationSuccessful() {
        return true;
    }

    public boolean isTlsRequired() {
        return true;
    }

    public SSLContext createSslContext(CertificateService cs, X509TrustManager trustManager) throws GeneralSecurityException {
        return cs.getSSLContext(this.accountID.getAccountPropertyString("CLIENT_TLS_CERTIFICATE"), trustManager);
    }

    public boolean login(XMPPConnection connection, String userName, String resource) throws XMPPException {
        SASLAuthentication.supportSASLMechanism("EXTERNAL", 0);
        try {
            connection.login("", "", resource);
            return true;
        } catch (XMPPException ex) {
            if (ex.getMessage().contains("EXTERNAL failed: not-authorized")) {
                logger.error("Certificate login failed", ex);
                return false;
            }
            throw ex;
        }
    }
}
