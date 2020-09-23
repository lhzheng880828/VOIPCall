package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

public class TransferPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "transfer";
    public static final String FROM_ATTR_NAME = "from";
    public static final String NAMESPACE = "urn:xmpp:jingle:transfer:0";
    public static final String SID_ATTR_NAME = "sid";
    public static final String TO_ATTR_NAME = "to";

    public TransferPacketExtension() {
        super("urn:xmpp:jingle:transfer:0", "transfer");
    }

    public String getFrom() {
        return getAttributeAsString("from");
    }

    public void setFrom(String from) {
        setAttribute("from", from);
    }

    public String getSID() {
        return getAttributeAsString("sid");
    }

    public void setSID(String sid) {
        setAttribute("sid", sid);
    }

    public String getTo() {
        return getAttributeAsString("to");
    }

    public void setTo(String to) {
        setAttribute("to", to);
    }
}
