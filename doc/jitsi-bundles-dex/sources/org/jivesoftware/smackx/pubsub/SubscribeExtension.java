package org.jivesoftware.smackx.pubsub;

import org.jitsi.gov.nist.core.Separators;

public class SubscribeExtension extends NodeExtension {
    protected String jid;

    public SubscribeExtension(String subscribeJid) {
        super(PubSubElementType.SUBSCRIBE);
        this.jid = subscribeJid;
    }

    public SubscribeExtension(String subscribeJid, String nodeId) {
        super(PubSubElementType.SUBSCRIBE, nodeId);
        this.jid = subscribeJid;
    }

    public String getJid() {
        return this.jid;
    }

    public String toXML() {
        StringBuilder builder = new StringBuilder(Separators.LESS_THAN);
        builder.append(getElementName());
        if (getNode() != null) {
            builder.append(" node='");
            builder.append(getNode());
            builder.append(Separators.QUOTE);
        }
        builder.append(" jid='");
        builder.append(getJid());
        builder.append("'/>");
        return builder.toString();
    }
}
