package org.jivesoftware.smackx.pubsub;

import java.util.Collections;
import java.util.List;
import org.jitsi.gov.nist.core.Separators;

public class SubscriptionsExtension extends NodeExtension {
    protected List<Subscription> items = Collections.EMPTY_LIST;

    public SubscriptionsExtension(List<Subscription> subList) {
        super(PubSubElementType.SUBSCRIPTIONS);
        if (subList != null) {
            this.items = subList;
        }
    }

    public SubscriptionsExtension(String nodeId, List<Subscription> subList) {
        super(PubSubElementType.SUBSCRIPTIONS, nodeId);
        if (subList != null) {
            this.items = subList;
        }
    }

    public List<Subscription> getSubscriptions() {
        return this.items;
    }

    public String toXML() {
        if (this.items == null || this.items.size() == 0) {
            return super.toXML();
        }
        StringBuilder builder = new StringBuilder(Separators.LESS_THAN);
        builder.append(getElementName());
        if (getNode() != null) {
            builder.append(" node='");
            builder.append(getNode());
            builder.append(Separators.QUOTE);
        }
        builder.append(Separators.GREATER_THAN);
        for (Subscription item : this.items) {
            builder.append(item.toXML());
        }
        builder.append("</");
        builder.append(getElementName());
        builder.append(Separators.GREATER_THAN);
        return builder.toString();
    }
}
