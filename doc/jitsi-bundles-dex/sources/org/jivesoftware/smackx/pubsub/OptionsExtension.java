package org.jivesoftware.smackx.pubsub;

import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smackx.pubsub.util.XmlUtils;

public class OptionsExtension extends NodeExtension {
    protected String id;
    protected String jid;

    public OptionsExtension(String subscriptionJid) {
        this(subscriptionJid, null, null);
    }

    public OptionsExtension(String subscriptionJid, String nodeId) {
        this(subscriptionJid, nodeId, null);
    }

    public OptionsExtension(String jid, String nodeId, String subscriptionId) {
        super(PubSubElementType.OPTIONS, nodeId);
        this.jid = jid;
        this.id = subscriptionId;
    }

    public String getJid() {
        return this.jid;
    }

    public String getId() {
        return this.id;
    }

    public String toXML() {
        StringBuilder builder = new StringBuilder(Separators.LESS_THAN);
        builder.append(getElementName());
        XmlUtils.appendAttribute(builder, "jid", this.jid);
        if (getNode() != null) {
            XmlUtils.appendAttribute(builder, "node", getNode());
        }
        if (this.id != null) {
            XmlUtils.appendAttribute(builder, "subid", this.id);
        }
        builder.append("/>");
        return builder.toString();
    }
}
