package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

public class ContentPacketExtension extends AbstractPacketExtension {
    public static final String CREATOR_ATTR_NAME = "creator";
    public static final String DISPOSITION_ATTR_NAME = "disposition";
    public static final String ELEMENT_NAME = "content";
    public static final String NAME_ATTR_NAME = "name";
    public static final String SENDERS_ATTR_NAME = "senders";

    public enum CreatorEnum {
        initiator,
        responder
    }

    public enum SendersEnum {
        initiator,
        none,
        responder,
        both
    }

    public ContentPacketExtension() {
        super(null, "content");
    }

    public ContentPacketExtension(CreatorEnum creator, String disposition, String name, SendersEnum senders) {
        super(null, "content");
        super.setAttribute(CREATOR_ATTR_NAME, creator);
        super.setAttribute(DISPOSITION_ATTR_NAME, disposition);
        super.setAttribute("name", name);
        super.setAttribute("senders", senders);
    }

    public ContentPacketExtension(CreatorEnum creator, String name) {
        super(null, "content");
        super.setAttribute(CREATOR_ATTR_NAME, creator);
        super.setAttribute("name", name);
    }

    public CreatorEnum getCreator() {
        return CreatorEnum.valueOf(getAttributeAsString(CREATOR_ATTR_NAME));
    }

    public void setCreator(CreatorEnum creator) {
        setAttribute(CREATOR_ATTR_NAME, creator);
    }

    public String getDisposition() {
        return getAttributeAsString(DISPOSITION_ATTR_NAME);
    }

    public void setDisposition(String disposition) {
        setAttribute(DISPOSITION_ATTR_NAME, disposition);
    }

    public String getName() {
        return getAttributeAsString("name");
    }

    public void setName(String name) {
        setAttribute("name", name);
    }

    public SendersEnum getSenders() {
        Object attributeVal = getAttribute("senders");
        return attributeVal == null ? null : SendersEnum.valueOf(attributeVal.toString());
    }

    public void setSenders(SendersEnum senders) {
        if (senders == null) {
            senders = SendersEnum.both;
        }
        setAttribute("senders", senders.toString());
    }
}
