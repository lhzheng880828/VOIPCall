package org.jitsi.gov.nist.javax.sip;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Collection;
import org.jitsi.gov.nist.core.net.AddressResolver;
import org.jitsi.gov.nist.javax.sip.clientauthutils.AccountManager;
import org.jitsi.gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import org.jitsi.gov.nist.javax.sip.clientauthutils.SecureAccountManager;
import org.jitsi.gov.nist.javax.sip.header.extensions.JoinHeader;
import org.jitsi.gov.nist.javax.sip.header.extensions.ReplacesHeader;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.SipStack;
import org.jitsi.javax.sip.header.HeaderFactory;

public interface SipStackExt extends SipStack {
    AuthenticationHelper getAuthenticationHelper(AccountManager accountManager, HeaderFactory headerFactory);

    Collection<Dialog> getDialogs();

    Dialog getJoinDialog(JoinHeader joinHeader);

    SocketAddress getLocalAddressForTcpDst(InetAddress inetAddress, int i, InetAddress inetAddress2, int i2) throws IOException;

    SocketAddress getLocalAddressForTlsDst(InetAddress inetAddress, int i, InetAddress inetAddress2) throws IOException;

    Dialog getReplacesDialog(ReplacesHeader replacesHeader);

    AuthenticationHelper getSecureAuthenticationHelper(SecureAccountManager secureAccountManager, HeaderFactory headerFactory);

    void setAddressResolver(AddressResolver addressResolver);

    void setEnabledCipherSuites(String[] strArr);
}
