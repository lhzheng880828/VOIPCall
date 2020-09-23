package org.jivesoftware.smackx.pubsub;

import java.util.Collections;
import java.util.List;

public abstract class SubscriptionEvent extends NodeEvent {
    private List<String> subIds = Collections.EMPTY_LIST;

    protected SubscriptionEvent(String nodeId) {
        super(nodeId);
    }

    protected SubscriptionEvent(String nodeId, List<String> subscriptionIds) {
        super(nodeId);
        if (subscriptionIds != null) {
            this.subIds = subscriptionIds;
        }
    }

    public List<String> getSubscriptions() {
        return Collections.unmodifiableList(this.subIds);
    }

    /* access modifiers changed from: protected */
    public void setSubscriptions(List<String> subscriptionIds) {
        if (subscriptionIds != null) {
            this.subIds = subscriptionIds;
        }
    }
}
