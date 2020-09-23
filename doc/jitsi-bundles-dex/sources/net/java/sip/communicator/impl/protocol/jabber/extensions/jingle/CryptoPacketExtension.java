package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import ch.imvs.sdes4j.srtp.SrtpCryptoAttribute;
import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

public class CryptoPacketExtension extends AbstractPacketExtension {
    public static final String CRYPTO_SUITE_ATTR_NAME = "crypto-suite";
    public static final String ELEMENT_NAME = "crypto";
    public static final String KEY_PARAMS_ATTR_NAME = "key-params";
    public static final String NAMESPACE = "urn:xmpp:jingle:apps:rtp:1";
    public static final String SESSION_PARAMS_ATTR_NAME = "session-params";
    public static final String TAG_ATTR_NAME = "tag";

    public CryptoPacketExtension() {
        super("urn:xmpp:jingle:apps:rtp:1", ELEMENT_NAME);
    }

    public CryptoPacketExtension(SrtpCryptoAttribute cryptoAttribute) {
        this();
        initialize(cryptoAttribute);
    }

    private void initialize(SrtpCryptoAttribute cryptoAttribute) {
        setTag(Integer.toString(cryptoAttribute.getTag()));
        setCryptoSuite(cryptoAttribute.getCryptoSuite().encode());
        setKeyParams(cryptoAttribute.getKeyParamsString());
        String sessionParamsString = cryptoAttribute.getSessionParamsString();
        if (sessionParamsString != null) {
            setSessionParams(sessionParamsString);
        }
    }

    public void setCryptoSuite(String cryptoSuite) {
        super.setAttribute(CRYPTO_SUITE_ATTR_NAME, cryptoSuite);
    }

    public String getCryptoSuite() {
        return getAttributeAsString(CRYPTO_SUITE_ATTR_NAME);
    }

    public boolean equalsCryptoSuite(String cryptoSuite) {
        return equalsStrings(getCryptoSuite(), cryptoSuite);
    }

    public void setKeyParams(String keyParams) {
        super.setAttribute(KEY_PARAMS_ATTR_NAME, keyParams);
    }

    public String getKeyParams() {
        return getAttributeAsString(KEY_PARAMS_ATTR_NAME);
    }

    public boolean equalsKeyParams(String keyParams) {
        return equalsStrings(getKeyParams(), keyParams);
    }

    public void setSessionParams(String sessionParams) {
        super.setAttribute(SESSION_PARAMS_ATTR_NAME, sessionParams);
    }

    public String getSessionParams() {
        return getAttributeAsString(SESSION_PARAMS_ATTR_NAME);
    }

    public boolean equalsSessionParams(String sessionParams) {
        return equalsStrings(getSessionParams(), sessionParams);
    }

    public void setTag(String tag) {
        super.setAttribute("tag", tag);
    }

    public String getTag() {
        return getAttributeAsString("tag");
    }

    public boolean equalsTag(String tag) {
        return equalsStrings(getTag(), tag);
    }

    public SrtpCryptoAttribute toSrtpCryptoAttribute() {
        return SrtpCryptoAttribute.create(getTag(), getCryptoSuite(), getKeyParams(), getSessionParams());
    }

    private static boolean equalsStrings(String string1, String string2) {
        return (string1 == null && string2 == null) || string1.equals(string2);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof CryptoPacketExtension)) {
            return false;
        }
        CryptoPacketExtension crypto = (CryptoPacketExtension) obj;
        if (crypto.equalsCryptoSuite(getCryptoSuite()) && crypto.equalsKeyParams(getKeyParams()) && crypto.equalsSessionParams(getSessionParams()) && crypto.equalsTag(getTag())) {
            return true;
        }
        return false;
    }
}
