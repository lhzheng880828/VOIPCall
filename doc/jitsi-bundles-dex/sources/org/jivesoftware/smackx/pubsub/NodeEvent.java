package org.jivesoftware.smackx.pubsub;

public abstract class NodeEvent {
    private String nodeId;

    protected NodeEvent(String id) {
        this.nodeId = id;
    }

    public String getNodeId() {
        return this.nodeId;
    }
}
