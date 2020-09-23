package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

public class ZrtpHashPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "zrtp-hash";
    public static final String NAMESPACE = "urn:xmpp:jingle:apps:rtp:zrtp:1";
    public static final String VERSION_ATTR_NAME = "version";

    public ZrtpHashPacketExtension() {
        super("urn:xmpp:jingle:apps:rtp:zrtp:1", "zrtp-hash");
    }

    public String getVersion() {
        return getAttributeAsString("version");
    }

    public void setVersion(String version) {
        setAttribute("version", version);
    }

    public String getValue() {
        return getText();
    }

    public void setValue(String value) {
        setText(value);
    }
}
