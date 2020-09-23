package org.jivesoftware.smackx.pubsub;

import org.jitsi.gov.nist.core.Separators;

public class Subscription extends NodeExtension {
    protected boolean configRequired;
    protected String id;
    protected String jid;
    protected State state;

    public enum State {
        subscribed,
        unconfigured,
        pending,
        none
    }

    public Subscription(String subscriptionJid) {
        this(subscriptionJid, null, null, null);
    }

    public Subscription(String subscriptionJid, String nodeId) {
        this(subscriptionJid, nodeId, null, null);
    }

    public Subscription(String jid, String nodeId, String subscriptionId, State state) {
        super(PubSubElementType.SUBSCRIPTION, nodeId);
        this.configRequired = false;
        this.jid = jid;
        this.id = subscriptionId;
        this.state = state;
    }

    public Subscription(String jid, String nodeId, String subscriptionId, State state, boolean configRequired) {
        super(PubSubElementType.SUBSCRIPTION, nodeId);
        this.configRequired = false;
        this.jid = jid;
        this.id = subscriptionId;
        this.state = state;
        this.configRequired = configRequired;
    }

    public String getJid() {
        return this.jid;
    }

    public String getId() {
        return this.id;
    }

    public State getState() {
        return this.state;
    }

    public boolean isConfigRequired() {
        return this.configRequired;
    }

    public String toXML() {
        StringBuilder builder = new StringBuilder("<subscription");
        appendAttribute(builder, "jid", this.jid);
        if (getNode() != null) {
            appendAttribute(builder, "node", getNode());
        }
        if (this.id != null) {
            appendAttribute(builder, "subid", this.id);
        }
        if (this.state != null) {
            appendAttribute(builder, "subscription", this.state.toString());
        }
        builder.append("/>");
        return builder.toString();
    }

    private void appendAttribute(StringBuilder builder, String att, String value) {
        builder.append(Separators.SP);
        builder.append(att);
        builder.append("='");
        builder.append(value);
        builder.append(Separators.QUOTE);
    }
}
