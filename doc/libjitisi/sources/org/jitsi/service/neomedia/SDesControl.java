package org.jitsi.service.neomedia;

import ch.imvs.sdes4j.srtp.SrtpCryptoAttribute;

public interface SDesControl extends SrtpControl {
    public static final String PROTO_NAME = SrtpControlType.SDES.toString();
    public static final String SDES_CIPHER_SUITES = "net.java.sip.communicator.service.neomedia.SDES_CIPHER_SUITES";

    SrtpCryptoAttribute getInAttribute();

    SrtpCryptoAttribute[] getInitiatorCryptoAttributes();

    SrtpCryptoAttribute getOutAttribute();

    Iterable<String> getSupportedCryptoSuites();

    SrtpCryptoAttribute initiatorSelectAttribute(Iterable<SrtpCryptoAttribute> iterable);

    SrtpCryptoAttribute responderSelectAttribute(Iterable<SrtpCryptoAttribute> iterable);

    void setEnabledCiphers(Iterable<String> iterable);
}
