package org.jivesoftware.smackx.pubsub;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.jivesoftware.smackx.pubsub.Item;

public class ItemPublishEvent<T extends Item> extends SubscriptionEvent {
    private List<T> items;
    private Date originalDate;

    public ItemPublishEvent(String nodeId, List<T> eventItems) {
        super(nodeId);
        this.items = eventItems;
    }

    public ItemPublishEvent(String nodeId, List<T> eventItems, List<String> subscriptionIds) {
        super(nodeId, subscriptionIds);
        this.items = eventItems;
    }

    public ItemPublishEvent(String nodeId, List<T> eventItems, List<String> subscriptionIds, Date publishedDate) {
        super(nodeId, subscriptionIds);
        this.items = eventItems;
        if (publishedDate != null) {
            this.originalDate = publishedDate;
        }
    }

    public List<T> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    public boolean isDelayed() {
        return this.originalDate != null;
    }

    public Date getPublishedDate() {
        return this.originalDate;
    }

    public String toString() {
        return getClass().getName() + "  [subscriptions: " + getSubscriptions() + "], [Delayed: " + (isDelayed() ? this.originalDate.toString() : "false") + ']';
    }
}
