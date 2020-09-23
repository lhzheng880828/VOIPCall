package org.jivesoftware.smackx.pubsub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.pubsub.ItemsExtension.ItemsElementType;
import org.jivesoftware.smackx.pubsub.packet.PubSub;
import org.jivesoftware.smackx.pubsub.packet.SyncPacketSend;

public class LeafNode extends Node {
    LeafNode(Connection connection, String nodeName) {
        super(connection, nodeName);
    }

    public DiscoverItems discoverItems() throws XMPPException {
        DiscoverItems items = new DiscoverItems();
        items.setTo(this.to);
        items.setNode(getId());
        return (DiscoverItems) SyncPacketSend.getReply(this.con, items);
    }

    public <T extends Item> List<T> getItems() throws XMPPException {
        return ((ItemsExtension) ((PubSub) SyncPacketSend.getReply(this.con, createPubsubPacket(Type.GET, new GetItemsRequest(getId())))).getExtension(PubSubElementType.ITEMS)).getItems();
    }

    public <T extends Item> List<T> getItems(String subscriptionId) throws XMPPException {
        return ((ItemsExtension) ((PubSub) SyncPacketSend.getReply(this.con, createPubsubPacket(Type.GET, new GetItemsRequest(getId(), subscriptionId)))).getExtension(PubSubElementType.ITEMS)).getItems();
    }

    public <T extends Item> List<T> getItems(Collection<String> ids) throws XMPPException {
        List itemList = new ArrayList(ids.size());
        for (String id : ids) {
            itemList.add(new Item(id));
        }
        return ((ItemsExtension) ((PubSub) SyncPacketSend.getReply(this.con, createPubsubPacket(Type.GET, new ItemsExtension(ItemsElementType.items, getId(), itemList)))).getExtension(PubSubElementType.ITEMS)).getItems();
    }

    public <T extends Item> List<T> getItems(int maxItems) throws XMPPException {
        return ((ItemsExtension) ((PubSub) SyncPacketSend.getReply(this.con, createPubsubPacket(Type.GET, new GetItemsRequest(getId(), maxItems)))).getExtension(PubSubElementType.ITEMS)).getItems();
    }

    public <T extends Item> List<T> getItems(int maxItems, String subscriptionId) throws XMPPException {
        return ((ItemsExtension) ((PubSub) SyncPacketSend.getReply(this.con, createPubsubPacket(Type.GET, new GetItemsRequest(getId(), subscriptionId, maxItems)))).getExtension(PubSubElementType.ITEMS)).getItems();
    }

    public void publish() {
        this.con.sendPacket(createPubsubPacket(Type.SET, new NodeExtension(PubSubElementType.PUBLISH, getId())));
    }

    public <T extends Item> void publish(T t) {
        Object t2;
        Collection items = new ArrayList(1);
        if (t2 == null) {
            t2 = new Item();
        }
        items.add(t2);
        publish(items);
    }

    public <T extends Item> void publish(Collection<T> items) {
        this.con.sendPacket(createPubsubPacket(Type.SET, new PublishItem(getId(), (Collection) items)));
    }

    public void send() throws XMPPException {
        SyncPacketSend.getReply(this.con, createPubsubPacket(Type.SET, new NodeExtension(PubSubElementType.PUBLISH, getId())));
    }

    public <T extends Item> void send(T t) throws XMPPException {
        Object t2;
        Collection items = new ArrayList(1);
        if (t2 == null) {
            t2 = new Item();
        }
        items.add(t2);
        send(items);
    }

    public <T extends Item> void send(Collection<T> items) throws XMPPException {
        SyncPacketSend.getReply(this.con, createPubsubPacket(Type.SET, new PublishItem(getId(), (Collection) items)));
    }

    public void deleteAllItems() throws XMPPException {
        SyncPacketSend.getReply(this.con, createPubsubPacket(Type.SET, new NodeExtension(PubSubElementType.PURGE_OWNER, getId()), PubSubElementType.PURGE_OWNER.getNamespace()));
    }

    public void deleteItem(String itemId) throws XMPPException {
        Collection items = new ArrayList(1);
        items.add(itemId);
        deleteItem(items);
    }

    public void deleteItem(Collection<String> itemIds) throws XMPPException {
        List items = new ArrayList(itemIds.size());
        for (String id : itemIds) {
            items.add(new Item(id));
        }
        SyncPacketSend.getReply(this.con, createPubsubPacket(Type.SET, new ItemsExtension(ItemsElementType.retract, getId(), items)));
    }
}
