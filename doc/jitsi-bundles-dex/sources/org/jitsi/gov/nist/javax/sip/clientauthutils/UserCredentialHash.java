package org.jitsi.gov.nist.javax.sip.clientauthutils;

public interface UserCredentialHash {
    String getHashUserDomainPassword();

    String getSipDomain();

    String getUserName();
}
