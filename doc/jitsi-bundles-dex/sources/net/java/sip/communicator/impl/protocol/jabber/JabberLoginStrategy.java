package net.java.sip.communicator.impl.protocol.jabber;

import java.security.GeneralSecurityException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import net.java.sip.communicator.service.certificate.CertificateService;
import net.java.sip.communicator.service.protocol.SecurityAuthority;
import net.java.sip.communicator.service.protocol.UserCredentials;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

public interface JabberLoginStrategy {
    SSLContext createSslContext(CertificateService certificateService, X509TrustManager x509TrustManager) throws GeneralSecurityException;

    boolean isTlsRequired();

    boolean login(XMPPConnection xMPPConnection, String str, String str2) throws XMPPException;

    boolean loginPreparationSuccessful();

    UserCredentials prepareLogin(SecurityAuthority securityAuthority, int i);
}
