package org.jivesoftware.smackx.pubsub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.Header;
import org.jivesoftware.smackx.packet.HeadersExtension;
import org.jivesoftware.smackx.pubsub.listener.ItemDeleteListener;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.jivesoftware.smackx.pubsub.listener.NodeConfigListener;
import org.jivesoftware.smackx.pubsub.packet.PubSub;
import org.jivesoftware.smackx.pubsub.packet.PubSubNamespace;
import org.jivesoftware.smackx.pubsub.packet.SyncPacketSend;
import org.jivesoftware.smackx.pubsub.util.NodeUtils;

public abstract class Node {
    protected Connection con;
    protected ConcurrentHashMap<NodeConfigListener, PacketListener> configEventToListenerMap = new ConcurrentHashMap();
    protected String id;
    protected ConcurrentHashMap<ItemDeleteListener, PacketListener> itemDeleteToListenerMap = new ConcurrentHashMap();
    protected ConcurrentHashMap<ItemEventListener, PacketListener> itemEventToListenerMap = new ConcurrentHashMap();
    protected String to;

    class EventContentFilter implements PacketFilter {
        private String firstElement;
        private String secondElement;

        EventContentFilter(String elementName) {
            this.firstElement = elementName;
        }

        EventContentFilter(String firstLevelEelement, String secondLevelElement) {
            this.firstElement = firstLevelEelement;
            this.secondElement = secondLevelElement;
        }

        public boolean accept(Packet packet) {
            if (!(packet instanceof Message)) {
                return false;
            }
            EventElement event = (EventElement) packet.getExtension("event", PubSubNamespace.EVENT.getXmlns());
            if (event == null) {
                return false;
            }
            NodeExtension embedEvent = event.getEvent();
            if (embedEvent == null) {
                return false;
            }
            if (embedEvent.getElementName().equals(this.firstElement)) {
                if (!embedEvent.getNode().equals(Node.this.getId())) {
                    return false;
                }
                if (this.secondElement == null) {
                    return true;
                }
                if (embedEvent instanceof EmbeddedPacketExtension) {
                    List<PacketExtension> secondLevelList = ((EmbeddedPacketExtension) embedEvent).getExtensions();
                    if (secondLevelList.size() == 0) {
                        return true;
                    }
                    if (secondLevelList.size() > 0 && ((PacketExtension) secondLevelList.get(0)).getElementName().equals(this.secondElement)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public class ItemDeleteTranslator implements PacketListener {
        private ItemDeleteListener listener;

        public ItemDeleteTranslator(ItemDeleteListener eventListener) {
            this.listener = eventListener;
        }

        public void processPacket(Packet packet) {
            EventElement event = (EventElement) packet.getExtension("event", PubSubNamespace.EVENT.getXmlns());
            if (((PacketExtension) event.getExtensions().get(0)).getElementName().equals(PubSubElementType.PURGE_EVENT.getElementName())) {
                this.listener.handlePurge();
                return;
            }
            ItemsExtension itemsElem = (ItemsExtension) event.getEvent();
            Collection<? extends PacketExtension> pubItems = itemsElem.getItems();
            Iterator<RetractItem> it = pubItems.iterator();
            List<String> items = new ArrayList(pubItems.size());
            while (it.hasNext()) {
                items.add(((RetractItem) it.next()).getId());
            }
            this.listener.handleDeletedItems(new ItemDeleteEvent(itemsElem.getNode(), items, Node.getSubscriptionIds(packet)));
        }
    }

    public class ItemEventTranslator implements PacketListener {
        private ItemEventListener listener;

        public ItemEventTranslator(ItemEventListener eventListener) {
            this.listener = eventListener;
        }

        public void processPacket(Packet packet) {
            ItemsExtension itemsElem = (ItemsExtension) ((EventElement) packet.getExtension("event", PubSubNamespace.EVENT.getXmlns())).getEvent();
            DelayInformation delay = (DelayInformation) packet.getExtension("delay", "urn:xmpp:delay");
            if (delay == null) {
                delay = (DelayInformation) packet.getExtension("x", "jabber:x:delay");
            }
            this.listener.handlePublishedItems(new ItemPublishEvent(itemsElem.getNode(), itemsElem.getItems(), Node.getSubscriptionIds(packet), delay == null ? null : delay.getStamp()));
        }
    }

    public class NodeConfigTranslator implements PacketListener {
        private NodeConfigListener listener;

        public NodeConfigTranslator(NodeConfigListener eventListener) {
            this.listener = eventListener;
        }

        public void processPacket(Packet packet) {
            this.listener.handleNodeConfiguration((ConfigurationEvent) ((EventElement) packet.getExtension("event", PubSubNamespace.EVENT.getXmlns())).getEvent());
        }
    }

    Node(Connection connection, String nodeName) {
        this.con = connection;
        this.id = nodeName;
    }

    /* access modifiers changed from: 0000 */
    public void setTo(String toAddress) {
        this.to = toAddress;
    }

    public String getId() {
        return this.id;
    }

    public ConfigureForm getNodeConfiguration() throws XMPPException {
        return NodeUtils.getFormFromPacket(sendPubsubPacket(Type.GET, new NodeExtension(PubSubElementType.CONFIGURE_OWNER, getId()), PubSubNamespace.OWNER), PubSubElementType.CONFIGURE_OWNER);
    }

    public void sendConfigurationForm(Form submitForm) throws XMPPException {
        SyncPacketSend.getReply(this.con, createPubsubPacket(Type.SET, new FormNode(FormNodeType.CONFIGURE_OWNER, getId(), submitForm), PubSubNamespace.OWNER));
    }

    public DiscoverInfo discoverInfo() throws XMPPException {
        DiscoverInfo info = new DiscoverInfo();
        info.setTo(this.to);
        info.setNode(getId());
        return (DiscoverInfo) SyncPacketSend.getReply(this.con, info);
    }

    public List<Subscription> getSubscriptions() throws XMPPException {
        return ((SubscriptionsExtension) ((PubSub) sendPubsubPacket(Type.GET, new NodeExtension(PubSubElementType.SUBSCRIPTIONS, getId()))).getExtension(PubSubElementType.SUBSCRIPTIONS)).getSubscriptions();
    }

    public Subscription subscribe(String jid) throws XMPPException {
        return (Subscription) ((PubSub) sendPubsubPacket(Type.SET, new SubscribeExtension(jid, getId()))).getExtension(PubSubElementType.SUBSCRIPTION);
    }

    public Subscription subscribe(String jid, SubscribeForm subForm) throws XMPPException {
        PubSub request = createPubsubPacket(Type.SET, new SubscribeExtension(jid, getId()));
        request.addExtension(new FormNode(FormNodeType.OPTIONS, subForm));
        return (Subscription) ((PubSub) PubSubManager.sendPubsubPacket(this.con, jid, Type.SET, request)).getExtension(PubSubElementType.SUBSCRIPTION);
    }

    public void unsubscribe(String jid) throws XMPPException {
        unsubscribe(jid, null);
    }

    public void unsubscribe(String jid, String subscriptionId) throws XMPPException {
        sendPubsubPacket(Type.SET, new UnsubscribeExtension(jid, getId(), subscriptionId));
    }

    public SubscribeForm getSubscriptionOptions(String jid) throws XMPPException {
        return getSubscriptionOptions(jid, null);
    }

    public SubscribeForm getSubscriptionOptions(String jid, String subscriptionId) throws XMPPException {
        return new SubscribeForm(((FormNode) ((PubSub) sendPubsubPacket(Type.GET, new OptionsExtension(jid, getId(), subscriptionId))).getExtension(PubSubElementType.OPTIONS)).getForm());
    }

    public void addItemEventListener(ItemEventListener listener) {
        PacketListener conListener = new ItemEventTranslator(listener);
        this.itemEventToListenerMap.put(listener, conListener);
        this.con.addPacketListener(conListener, new EventContentFilter(EventElementType.items.toString(), "item"));
    }

    public void removeItemEventListener(ItemEventListener listener) {
        PacketListener conListener = (PacketListener) this.itemEventToListenerMap.remove(listener);
        if (conListener != null) {
            this.con.removePacketListener(conListener);
        }
    }

    public void addConfigurationListener(NodeConfigListener listener) {
        PacketListener conListener = new NodeConfigTranslator(listener);
        this.configEventToListenerMap.put(listener, conListener);
        this.con.addPacketListener(conListener, new EventContentFilter(EventElementType.configuration.toString()));
    }

    public void removeConfigurationListener(NodeConfigListener listener) {
        PacketListener conListener = (PacketListener) this.configEventToListenerMap.remove(listener);
        if (conListener != null) {
            this.con.removePacketListener(conListener);
        }
    }

    public void addItemDeleteListener(ItemDeleteListener listener) {
        PacketListener delListener = new ItemDeleteTranslator(listener);
        this.itemDeleteToListenerMap.put(listener, delListener);
        this.con.addPacketListener(delListener, new OrFilter(new EventContentFilter(EventElementType.items.toString(), "retract"), new EventContentFilter(EventElementType.purge.toString())));
    }

    public void removeItemDeleteListener(ItemDeleteListener listener) {
        PacketListener conListener = (PacketListener) this.itemDeleteToListenerMap.remove(listener);
        if (conListener != null) {
            this.con.removePacketListener(conListener);
        }
    }

    public String toString() {
        return super.toString() + Separators.SP + getClass().getName() + " id: " + this.id;
    }

    /* access modifiers changed from: protected */
    public PubSub createPubsubPacket(Type type, PacketExtension ext) {
        return createPubsubPacket(type, ext, null);
    }

    /* access modifiers changed from: protected */
    public PubSub createPubsubPacket(Type type, PacketExtension ext, PubSubNamespace ns) {
        return PubSubManager.createPubsubPacket(this.to, type, ext, ns);
    }

    /* access modifiers changed from: protected */
    public Packet sendPubsubPacket(Type type, NodeExtension ext) throws XMPPException {
        return PubSubManager.sendPubsubPacket(this.con, this.to, type, (PacketExtension) ext);
    }

    /* access modifiers changed from: protected */
    public Packet sendPubsubPacket(Type type, NodeExtension ext, PubSubNamespace ns) throws XMPPException {
        return PubSubManager.sendPubsubPacket(this.con, this.to, type, (PacketExtension) ext, ns);
    }

    /* access modifiers changed from: private|static */
    public static List<String> getSubscriptionIds(Packet packet) {
        HeadersExtension headers = (HeadersExtension) packet.getExtension("headers", HeadersExtension.NAMESPACE);
        List<String> values = null;
        if (headers != null) {
            values = new ArrayList(headers.getHeaders().size());
            for (Header header : headers.getHeaders()) {
                values.add(header.getValue());
            }
        }
        return values;
    }
}
