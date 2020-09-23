package org.jivesoftware.smackx.pubsub;

import java.util.Collections;
import java.util.List;

public class ItemDeleteEvent extends SubscriptionEvent {
    private List<String> itemIds = Collections.EMPTY_LIST;

    public ItemDeleteEvent(String nodeId, List<String> deletedItemIds, List<String> subscriptionIds) {
        super(nodeId, subscriptionIds);
        if (deletedItemIds == null) {
            throw new IllegalArgumentException("deletedItemIds cannot be null");
        }
        this.itemIds = deletedItemIds;
    }

    public List<String> getItemIds() {
        return Collections.unmodifiableList(this.itemIds);
    }

    public String toString() {
        return getClass().getName() + "  [subscriptions: " + getSubscriptions() + "], [Deleted Items: " + this.itemIds + ']';
    }
}
