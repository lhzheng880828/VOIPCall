package org.jivesoftware.smackx.pubsub;

import org.jitsi.gov.nist.core.Separators;

public class GetItemsRequest extends NodeExtension {
    protected int maxItems;
    protected String subId;

    public GetItemsRequest(String nodeId) {
        super(PubSubElementType.ITEMS, nodeId);
    }

    public GetItemsRequest(String nodeId, String subscriptionId) {
        super(PubSubElementType.ITEMS, nodeId);
        this.subId = subscriptionId;
    }

    public GetItemsRequest(String nodeId, int maxItemsToReturn) {
        super(PubSubElementType.ITEMS, nodeId);
        this.maxItems = maxItemsToReturn;
    }

    public GetItemsRequest(String nodeId, String subscriptionId, int maxItemsToReturn) {
        this(nodeId, maxItemsToReturn);
        this.subId = subscriptionId;
    }

    public String getSubscriptionId() {
        return this.subId;
    }

    public int getMaxItems() {
        return this.maxItems;
    }

    public String toXML() {
        StringBuilder builder = new StringBuilder(Separators.LESS_THAN);
        builder.append(getElementName());
        builder.append(" node='");
        builder.append(getNode());
        builder.append(Separators.QUOTE);
        if (getSubscriptionId() != null) {
            builder.append(" subid='");
            builder.append(getSubscriptionId());
            builder.append(Separators.QUOTE);
        }
        if (getMaxItems() > 0) {
            builder.append(" max_items='");
            builder.append(getMaxItems());
            builder.append(Separators.QUOTE);
        }
        builder.append("/>");
        return builder.toString();
    }
}
