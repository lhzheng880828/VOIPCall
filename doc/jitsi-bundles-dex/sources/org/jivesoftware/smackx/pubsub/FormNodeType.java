package org.jivesoftware.smackx.pubsub;

import org.jivesoftware.smackx.pubsub.packet.PubSubNamespace;

public enum FormNodeType {
    CONFIGURE_OWNER,
    CONFIGURE,
    OPTIONS,
    DEFAULT;

    public PubSubElementType getNodeElement() {
        return PubSubElementType.valueOf(toString());
    }

    public static FormNodeType valueOfFromElementName(String elem, String configNamespace) {
        if ("configure".equals(elem) && PubSubNamespace.OWNER.getXmlns().equals(configNamespace)) {
            return CONFIGURE_OWNER;
        }
        return valueOf(elem.toUpperCase());
    }
}
