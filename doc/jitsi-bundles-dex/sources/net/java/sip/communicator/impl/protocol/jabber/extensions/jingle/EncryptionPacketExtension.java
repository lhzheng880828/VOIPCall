package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import java.util.ArrayList;
import java.util.List;
import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;
import org.jivesoftware.smack.packet.PacketExtension;

public class EncryptionPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "encryption";
    public static final String NAMESPACE = "urn:xmpp:jingle:apps:rtp:1";
    public static final String REQUIRED_ATTR_NAME = "required";
    private List<CryptoPacketExtension> cryptoList = new ArrayList();

    public EncryptionPacketExtension() {
        super("urn:xmpp:jingle:apps:rtp:1", ELEMENT_NAME);
    }

    public void addCrypto(CryptoPacketExtension crypto) {
        if (!this.cryptoList.contains(crypto)) {
            this.cryptoList.add(crypto);
        }
    }

    public List<CryptoPacketExtension> getCryptoList() {
        return this.cryptoList;
    }

    public void setRequired(boolean required) {
        if (required) {
            super.setAttribute("required", Boolean.valueOf(required));
        } else {
            super.removeAttribute("required");
        }
    }

    public boolean isRequired() {
        String required = getAttributeAsString("required");
        return Boolean.valueOf(required).booleanValue() || "1".equals(required);
    }

    public List<? extends PacketExtension> getChildExtensions() {
        List<PacketExtension> ret = new ArrayList();
        ret.addAll(super.getChildExtensions());
        return ret;
    }

    public void addChildExtension(PacketExtension childExtension) {
        super.addChildExtension(childExtension);
        if (childExtension instanceof CryptoPacketExtension) {
            addCrypto((CryptoPacketExtension) childExtension);
        }
    }
}
