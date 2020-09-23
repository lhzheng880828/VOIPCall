package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

public class DtlsFingerprintPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "fingerprint";
    private static final String HASH_ATTR_NAME = "hash";
    public static final String NAMESPACE = "urn:xmpp:jingle:apps:dtls:0";
    private static final String REQUIRED_ATTR_NAME = "required";

    public DtlsFingerprintPacketExtension() {
        super("urn:xmpp:jingle:apps:dtls:0", ELEMENT_NAME);
    }

    public String getFingerprint() {
        return getText();
    }

    public String getHash() {
        return getAttributeAsString(HASH_ATTR_NAME);
    }

    public boolean getRequired() {
        String attr = getAttributeAsString("required");
        return attr == null ? false : Boolean.parseBoolean(attr);
    }

    public void setFingerprint(String fingerprint) {
        setText(fingerprint);
    }

    public void setHash(String hash) {
        setAttribute(HASH_ATTR_NAME, hash);
    }

    public void setRequired(boolean required) {
        setAttribute("required", Boolean.valueOf(required));
    }
}
