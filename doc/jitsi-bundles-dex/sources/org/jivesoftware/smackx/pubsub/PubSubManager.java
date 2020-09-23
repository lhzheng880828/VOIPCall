package org.jivesoftware.smackx.pubsub;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.DiscoverInfo.Identity;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.pubsub.packet.PubSub;
import org.jivesoftware.smackx.pubsub.packet.PubSubNamespace;
import org.jivesoftware.smackx.pubsub.packet.SyncPacketSend;
import org.jivesoftware.smackx.pubsub.util.NodeUtils;

public final class PubSubManager {
    private Connection con;
    private Map<String, Node> nodeMap = new ConcurrentHashMap();
    private String to;

    public PubSubManager(Connection connection) {
        this.con = connection;
        this.to = "pubsub." + connection.getServiceName();
    }

    public PubSubManager(Connection connection, String toAddress) {
        this.con = connection;
        this.to = toAddress;
    }

    public LeafNode createNode() throws XMPPException {
        LeafNode newNode = new LeafNode(this.con, ((NodeExtension) ((PubSub) sendPubsubPacket(Type.SET, new NodeExtension(PubSubElementType.CREATE))).getExtension("create", PubSubNamespace.BASIC.getXmlns())).getNode());
        newNode.setTo(this.to);
        this.nodeMap.put(newNode.getId(), newNode);
        return newNode;
    }

    public LeafNode createNode(String id) throws XMPPException {
        return (LeafNode) createNode(id, null);
    }

    public Node createNode(String name, Form config) throws XMPPException {
        PubSub request = createPubsubPacket(this.to, Type.SET, new NodeExtension(PubSubElementType.CREATE, name));
        boolean isLeafNode = true;
        if (config != null) {
            request.addExtension(new FormNode(FormNodeType.CONFIGURE, config));
            FormField nodeTypeField = config.getField(ConfigureNodeFields.node_type.getFieldName());
            if (nodeTypeField != null) {
                isLeafNode = ((String) nodeTypeField.getValues().next()).equals(NodeType.leaf.toString());
            }
        }
        sendPubsubPacket(this.con, this.to, Type.SET, request);
        Node newNode = isLeafNode ? new LeafNode(this.con, name) : new CollectionNode(this.con, name);
        newNode.setTo(this.to);
        this.nodeMap.put(newNode.getId(), newNode);
        return newNode;
    }

    public Node getNode(String id) throws XMPPException {
        Node node = (Node) this.nodeMap.get(id);
        if (node == null) {
            DiscoverInfo info = new DiscoverInfo();
            info.setTo(this.to);
            info.setNode(id);
            if (((Identity) ((DiscoverInfo) SyncPacketSend.getReply(this.con, info)).getIdentities().next()).getType().equals(NodeType.leaf.toString())) {
                node = new LeafNode(this.con, id);
            } else {
                node = new CollectionNode(this.con, id);
            }
            node.setTo(this.to);
            this.nodeMap.put(id, node);
        }
        return node;
    }

    public DiscoverItems discoverNodes(String nodeId) throws XMPPException {
        DiscoverItems items = new DiscoverItems();
        if (nodeId != null) {
            items.setNode(nodeId);
        }
        items.setTo(this.to);
        return (DiscoverItems) SyncPacketSend.getReply(this.con, items);
    }

    public List<Subscription> getSubscriptions() throws XMPPException {
        return ((SubscriptionsExtension) sendPubsubPacket(Type.GET, new NodeExtension(PubSubElementType.SUBSCRIPTIONS)).getExtension(PubSubElementType.SUBSCRIPTIONS.getElementName(), PubSubElementType.SUBSCRIPTIONS.getNamespace().getXmlns())).getSubscriptions();
    }

    public List<Affiliation> getAffiliations() throws XMPPException {
        return ((AffiliationsExtension) ((PubSub) sendPubsubPacket(Type.GET, new NodeExtension(PubSubElementType.AFFILIATIONS))).getExtension(PubSubElementType.AFFILIATIONS)).getAffiliations();
    }

    public void deleteNode(String nodeId) throws XMPPException {
        sendPubsubPacket(Type.SET, new NodeExtension(PubSubElementType.DELETE, nodeId), PubSubElementType.DELETE.getNamespace());
        this.nodeMap.remove(nodeId);
    }

    public ConfigureForm getDefaultConfiguration() throws XMPPException {
        return NodeUtils.getFormFromPacket((PubSub) sendPubsubPacket(Type.GET, new NodeExtension(PubSubElementType.DEFAULT), PubSubElementType.DEFAULT.getNamespace()), PubSubElementType.DEFAULT);
    }

    public DiscoverInfo getSupportedFeatures() throws XMPPException {
        return ServiceDiscoveryManager.getInstanceFor(this.con).discoverInfo(this.to);
    }

    private Packet sendPubsubPacket(Type type, PacketExtension ext, PubSubNamespace ns) throws XMPPException {
        return sendPubsubPacket(this.con, this.to, type, ext, ns);
    }

    private Packet sendPubsubPacket(Type type, PacketExtension ext) throws XMPPException {
        return sendPubsubPacket(type, ext, null);
    }

    static PubSub createPubsubPacket(String to, Type type, PacketExtension ext) {
        return createPubsubPacket(to, type, ext, null);
    }

    static PubSub createPubsubPacket(String to, Type type, PacketExtension ext, PubSubNamespace ns) {
        PubSub request = new PubSub();
        request.setTo(to);
        request.setType(type);
        if (ns != null) {
            request.setPubSubNamespace(ns);
        }
        request.addExtension(ext);
        return request;
    }

    static Packet sendPubsubPacket(Connection con, String to, Type type, PacketExtension ext) throws XMPPException {
        return sendPubsubPacket(con, to, type, ext, null);
    }

    static Packet sendPubsubPacket(Connection con, String to, Type type, PacketExtension ext, PubSubNamespace ns) throws XMPPException {
        return SyncPacketSend.getReply(con, createPubsubPacket(to, type, ext, ns));
    }

    static Packet sendPubsubPacket(Connection con, String to, Type type, PubSub packet) throws XMPPException {
        return sendPubsubPacket(con, to, type, packet, null);
    }

    static Packet sendPubsubPacket(Connection con, String to, Type type, PubSub packet, PubSubNamespace ns) throws XMPPException {
        return SyncPacketSend.getReply(con, packet);
    }
}
