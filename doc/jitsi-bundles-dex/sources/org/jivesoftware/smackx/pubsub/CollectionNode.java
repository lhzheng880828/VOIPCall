package org.jivesoftware.smackx.pubsub;

import org.jivesoftware.smack.Connection;

public class CollectionNode extends Node {
    CollectionNode(Connection connection, String nodeId) {
        super(connection, nodeId);
    }
}
