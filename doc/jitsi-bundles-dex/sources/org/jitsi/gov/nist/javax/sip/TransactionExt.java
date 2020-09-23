package org.jitsi.gov.nist.javax.sip;

import java.security.cert.Certificate;
import java.util.List;
import javax.net.ssl.SSLPeerUnverifiedException;
import org.jitsi.javax.sip.SipProvider;
import org.jitsi.javax.sip.Transaction;

public interface TransactionExt extends Transaction {
    List<String> extractCertIdentities() throws SSLPeerUnverifiedException;

    String getCipherSuite() throws UnsupportedOperationException;

    String getHost();

    Certificate[] getLocalCertificates() throws UnsupportedOperationException;

    String getPeerAddress();

    Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException;

    int getPeerPort();

    int getPort();

    SipProvider getSipProvider();

    int getTimerD();

    int getTimerT2();

    int getTimerT4();

    String getTransport();

    boolean isReleaseReferences();

    void setReleaseReferences(boolean z);

    void setTimerD(int i);

    void setTimerT2(int i);

    void setTimerT4(int i);
}
