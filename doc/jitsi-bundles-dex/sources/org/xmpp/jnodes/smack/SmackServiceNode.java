package org.xmpp.jnodes.smack;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.packet.DiscoverItems.Item;
import org.xmpp.jnodes.RelayChannel;
import org.xmpp.jnodes.smack.TrackerEntry.Policy;

public class SmackServiceNode implements ConnectionListener, PacketListener {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    /* access modifiers changed from: private|final */
    public final ConcurrentHashMap<String, RelayChannel> channels = new ConcurrentHashMap();
    private final XMPPConnection connection;
    private final AtomicInteger ids = new AtomicInteger(0);
    private final ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(1);
    /* access modifiers changed from: private */
    public long timeout = 60000;
    private final Map<String, TrackerEntry> trackerEntries = Collections.synchronizedMap(new LinkedHashMap());

    public static class MappedNodes {
        final Map<String, TrackerEntry> relayEntries = Collections.synchronizedMap(new LinkedHashMap());
        final Map<String, TrackerEntry> trackerEntries = Collections.synchronizedMap(new LinkedHashMap());

        public Map<String, TrackerEntry> getRelayEntries() {
            return this.relayEntries;
        }

        public Map<String, TrackerEntry> getTrackerEntries() {
            return this.trackerEntries;
        }
    }

    static {
        ProviderManager.getInstance().addIQProvider("channel", JingleChannelIQ.NAMESPACE, new JingleNodesProvider());
        ProviderManager.getInstance().addIQProvider(JingleTrackerIQ.NAME, "http://jabber.org/protocol/jinglenodes", new JingleTrackerProvider());
    }

    public SmackServiceNode(XMPPConnection connection, long timeout) {
        this.connection = connection;
        this.timeout = timeout;
        setup();
    }

    public SmackServiceNode(String server, int port, long timeout) {
        ConnectionConfiguration conf = new ConnectionConfiguration(server, port, server);
        conf.setSASLAuthenticationEnabled(false);
        conf.setSecurityMode(SecurityMode.disabled);
        this.connection = new XMPPConnection(conf);
        this.timeout = timeout;
    }

    public void connect(String user, String password) throws XMPPException {
        connect(user, password, false, SubscriptionMode.accept_all);
    }

    public void connect(String user, String password, boolean tryCreateAccount, SubscriptionMode mode) throws XMPPException {
        this.connection.connect();
        this.connection.addConnectionListener(this);
        if (tryCreateAccount) {
            try {
                this.connection.getAccountManager().createAccount(user, password);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
            } catch (XMPPException e2) {
            }
        }
        this.connection.login(user, password);
        this.connection.getRoster().setSubscriptionMode(mode);
        setup();
    }

    private void setup() {
        this.scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                for (RelayChannel c : SmackServiceNode.this.channels.values()) {
                    long current = System.currentTimeMillis();
                    long db = current - c.getLastReceivedTimeB();
                    if (current - c.getLastReceivedTimeA() > SmackServiceNode.this.timeout || db > SmackServiceNode.this.timeout) {
                        SmackServiceNode.this.removeChannel(c);
                    }
                }
            }
        }, this.timeout, this.timeout, TimeUnit.MILLISECONDS);
        this.connection.addPacketListener(this, new PacketFilter() {
            public boolean accept(Packet packet) {
                return (packet instanceof JingleChannelIQ) || (packet instanceof JingleTrackerIQ);
            }
        });
    }

    public void connectionClosed() {
        closeAllChannels();
        this.scheduledExecutor.shutdownNow();
    }

    private void closeAllChannels() {
        for (RelayChannel c : this.channels.values()) {
            removeChannel(c);
        }
    }

    /* access modifiers changed from: private */
    public void removeChannel(RelayChannel c) {
        this.channels.remove(c.getAttachment());
        c.close();
    }

    public void connectionClosedOnError(Exception e) {
        closeAllChannels();
    }

    public void reconnectingIn(int i) {
    }

    public void reconnectionSuccessful() {
    }

    public void reconnectionFailed(Exception e) {
    }

    /* access modifiers changed from: protected */
    public IQ createUdpChannel(JingleChannelIQ iq) {
        try {
            RelayChannel rc = RelayChannel.createLocalRelayChannel("0.0.0.0", MetaContactListServiceImpl.CONTACT_LIST_MODIFICATION_TIMEOUT, 40000);
            String sId = String.valueOf(this.ids.incrementAndGet());
            rc.setAttachment(sId);
            this.channels.put(sId, rc);
            JingleChannelIQ result = new JingleChannelIQ();
            result.setType(Type.RESULT);
            result.setTo(iq.getFrom());
            result.setFrom(iq.getTo());
            result.setPacketID(iq.getPacketID());
            result.setHost(rc.getIp());
            result.setLocalport(rc.getPortA());
            result.setRemoteport(rc.getPortB());
            result.setId(sId);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return JingleChannelIQ.createEmptyError();
        }
    }

    public void processPacket(Packet packet) {
        if (packet instanceof JingleChannelIQ) {
            JingleChannelIQ request = (JingleChannelIQ) packet;
            if (request.isRequest()) {
                this.connection.sendPacket(createUdpChannel(request));
            }
        } else if ((packet instanceof JingleTrackerIQ) && ((JingleTrackerIQ) packet).isRequest()) {
            JingleTrackerIQ result = createKnownNodes();
            result.setPacketID(packet.getPacketID());
            result.setFrom(packet.getTo());
            result.setTo(packet.getFrom());
            this.connection.sendPacket(result);
        }
    }

    public XMPPConnection getConnection() {
        return this.connection;
    }

    public static JingleChannelIQ getChannel(XMPPConnection xmppConnection, String serviceNode) {
        if (xmppConnection == null || !xmppConnection.isConnected()) {
            return null;
        }
        JingleChannelIQ iq = new JingleChannelIQ();
        iq.setFrom(xmppConnection.getUser());
        iq.setTo(serviceNode);
        PacketCollector collector = xmppConnection.createPacketCollector(new PacketIDFilter(iq.getPacketID()));
        xmppConnection.sendPacket(iq);
        JingleChannelIQ result = (JingleChannelIQ) collector.nextResult(Math.round(((double) SmackConfiguration.getPacketReplyTimeout()) * 1.5d));
        collector.cancel();
        return result;
    }

    public static JingleTrackerIQ getServices(XMPPConnection xmppConnection, String serviceNode) {
        if (xmppConnection == null || !xmppConnection.isConnected()) {
            return null;
        }
        JingleTrackerIQ iq = new JingleTrackerIQ();
        iq.setFrom(xmppConnection.getUser());
        iq.setTo(serviceNode);
        PacketCollector collector = xmppConnection.createPacketCollector(new PacketIDFilter(iq.getPacketID()));
        xmppConnection.sendPacket(iq);
        Packet result = collector.nextResult(Math.round(((double) SmackConfiguration.getPacketReplyTimeout()) * 1.5d));
        collector.cancel();
        return result instanceof JingleTrackerIQ ? (JingleTrackerIQ) result : null;
    }

    public static void deepSearch(XMPPConnection xmppConnection, int maxEntries, String startPoint, MappedNodes mappedNodes, int maxDepth, int maxSearchNodes, String protocol, ConcurrentHashMap<String, String> visited) {
        if (xmppConnection != null && xmppConnection.isConnected() && mappedNodes.getRelayEntries().size() <= maxEntries && maxDepth > 0 && !startPoint.equals(xmppConnection.getUser()) && visited.size() <= maxSearchNodes) {
            JingleTrackerIQ result = getServices(xmppConnection, startPoint);
            visited.put(startPoint, startPoint);
            if (result != null && result.getType().equals(Type.RESULT)) {
                for (TrackerEntry entry : result.getEntries()) {
                    if (entry.getType().equals(TrackerEntry.Type.tracker)) {
                        mappedNodes.getTrackerEntries().put(entry.getJid(), entry);
                        deepSearch(xmppConnection, maxEntries, entry.getJid(), mappedNodes, maxDepth - 1, maxSearchNodes, protocol, visited);
                    } else if (entry.getType().equals(TrackerEntry.Type.relay)) {
                        if (protocol != null) {
                            if (!protocol.equals(entry.getProtocol())) {
                            }
                        }
                        mappedNodes.getRelayEntries().put(entry.getJid(), entry);
                    }
                }
            }
        }
    }

    public static MappedNodes aSyncSearchServices(XMPPConnection xmppConnection, int maxEntries, int maxDepth, int maxSearchNodes, String protocol, boolean searchBuddies) {
        final MappedNodes mappedNodes = new MappedNodes();
        final XMPPConnection xMPPConnection = xmppConnection;
        final int i = maxEntries;
        final int i2 = maxDepth;
        final int i3 = maxSearchNodes;
        final String str = protocol;
        final boolean z = searchBuddies;
        executorService.submit(new Runnable() {
            public void run() {
                SmackServiceNode.searchServices(new ConcurrentHashMap(), xMPPConnection, i, i2, i3, str, z, mappedNodes);
            }
        });
        return mappedNodes;
    }

    public static MappedNodes searchServices(XMPPConnection xmppConnection, int maxEntries, int maxDepth, int maxSearchNodes, String protocol, boolean searchBuddies) {
        return searchServices(new ConcurrentHashMap(), xmppConnection, maxEntries, maxDepth, maxSearchNodes, protocol, searchBuddies, new MappedNodes());
    }

    /* access modifiers changed from: private|static */
    public static MappedNodes searchServices(ConcurrentHashMap<String, String> visited, XMPPConnection xmppConnection, int maxEntries, int maxDepth, int maxSearchNodes, String protocol, boolean searchBuddies, MappedNodes mappedNodes) {
        if (xmppConnection == null || !xmppConnection.isConnected()) {
            return null;
        }
        searchDiscoItems(xmppConnection, maxEntries, xmppConnection.getServiceName(), mappedNodes, maxDepth - 1, maxSearchNodes, protocol, visited);
        deepSearch(xmppConnection, maxEntries, xmppConnection.getHost(), mappedNodes, maxDepth - 1, maxSearchNodes, protocol, visited);
        if (xmppConnection.getRoster() == null || !searchBuddies) {
            return mappedNodes;
        }
        for (RosterEntry re : xmppConnection.getRoster().getEntries()) {
            Iterator<Presence> i = xmppConnection.getRoster().getPresences(re.getUser());
            while (i.hasNext()) {
                Presence presence = (Presence) i.next();
                if (presence.isAvailable()) {
                    deepSearch(xmppConnection, maxEntries, presence.getFrom(), mappedNodes, maxDepth - 1, maxSearchNodes, protocol, visited);
                }
            }
        }
        return mappedNodes;
    }

    private static void searchDiscoItems(XMPPConnection xmppConnection, int maxEntries, String startPoint, MappedNodes mappedNodes, int maxDepth, int maxSearchNodes, String protocol, ConcurrentHashMap<String, String> visited) {
        DiscoverItems items = new DiscoverItems();
        items.setTo(startPoint);
        PacketCollector collector = xmppConnection.createPacketCollector(new PacketIDFilter(items.getPacketID()));
        xmppConnection.sendPacket(items);
        DiscoverItems result = (DiscoverItems) collector.nextResult(Math.round(((double) SmackConfiguration.getPacketReplyTimeout()) * 1.5d));
        if (result != null) {
            Iterator<Item> i = result.getItems();
            Item item = i.hasNext() ? (Item) i.next() : null;
            while (item != null) {
                deepSearch(xmppConnection, maxEntries, item.getEntityID(), mappedNodes, maxDepth, maxSearchNodes, protocol, visited);
                item = i.hasNext() ? (Item) i.next() : null;
            }
        }
        collector.cancel();
    }

    /* access modifiers changed from: 0000 */
    public ConcurrentHashMap<String, RelayChannel> getChannels() {
        return this.channels;
    }

    public JingleTrackerIQ createKnownNodes() {
        JingleTrackerIQ iq = new JingleTrackerIQ();
        iq.setType(Type.RESULT);
        for (TrackerEntry entry : this.trackerEntries.values()) {
            if (!entry.getPolicy().equals(Policy._roster)) {
                iq.addEntry(entry);
            }
        }
        return iq;
    }

    public void addTrackerEntry(TrackerEntry entry) {
        this.trackerEntries.put(entry.getJid(), entry);
    }

    public void addEntries(MappedNodes entries) {
        for (TrackerEntry t : entries.getRelayEntries().values()) {
            addTrackerEntry(t);
        }
        for (TrackerEntry t2 : entries.getTrackerEntries().values()) {
            addTrackerEntry(t2);
        }
    }

    public Map<String, TrackerEntry> getTrackerEntries() {
        return this.trackerEntries;
    }

    public TrackerEntry getPreferedRelay() {
        for (TrackerEntry trackerEntry : this.trackerEntries.values()) {
            if (TrackerEntry.Type.relay.equals(trackerEntry.getType())) {
                return trackerEntry;
            }
        }
        return null;
    }
}
