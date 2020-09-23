package net.java.sip.communicator.impl.protocol.jabber;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.sdp.SdpConstants;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.util.StringUtils;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.packet.DiscoverItems.Item;
import org.xmpp.jnodes.smack.SmackServiceNode;
import org.xmpp.jnodes.smack.SmackServiceNode.MappedNodes;
import org.xmpp.jnodes.smack.TrackerEntry;

public class JingleNodesServiceDiscovery implements Runnable {
    private static final String JINGLE_NODES_SEARCH_PREFIXES_STOP_ON_FIRST_PROP = "net.java.sip.communicator.impl.protocol.jabber.JINGLE_NODES_SEARCH_PREFIXES_STOP_ON_FIRST";
    private static final String JINGLE_NODES_SEARCH_PREFIX_PROP = "net.java.sip.communicator.impl.protocol.jabber.JINGLE_NODES_SEARCH_PREFIXES";
    private static final Logger logger = Logger.getLogger(JingleNodesServiceDiscovery.class);
    private final JabberAccountIDImpl accountID;
    private final XMPPConnection connection;
    private final Object jingleNodesSyncRoot;
    private final SmackServiceNode service;

    JingleNodesServiceDiscovery(SmackServiceNode service, XMPPConnection connection, JabberAccountIDImpl accountID, Object syncRoot) {
        this.jingleNodesSyncRoot = syncRoot;
        this.service = service;
        this.connection = connection;
        this.accountID = accountID;
    }

    public void run() {
        synchronized (this.jingleNodesSyncRoot) {
            long start = System.currentTimeMillis();
            if (logger.isInfoEnabled()) {
                logger.info("Start Jingle Nodes discovery!");
            }
            String searchNodesWithPrefix = JabberActivator.getResources().getSettingsString(JINGLE_NODES_SEARCH_PREFIX_PROP);
            if (searchNodesWithPrefix == null || searchNodesWithPrefix.length() == 0) {
                searchNodesWithPrefix = JabberActivator.getConfigurationService().getString(JINGLE_NODES_SEARCH_PREFIX_PROP);
            }
            if (searchNodesWithPrefix == null || searchNodesWithPrefix.length() == 0 || searchNodesWithPrefix.equalsIgnoreCase("off")) {
                searchNodesWithPrefix = "";
            }
            MappedNodes nodes = searchServicesWithPrefix(this.service, this.connection, 6, 3, 20, "udp", this.accountID.isJingleNodesSearchBuddiesEnabled(), this.accountID.isJingleNodesAutoDiscoveryEnabled(), searchNodesWithPrefix);
            if (logger.isInfoEnabled()) {
                logger.info("Jingle Nodes discovery terminated! ");
                logger.info("Found " + (nodes != null ? Integer.valueOf(nodes.getRelayEntries().size()) : SdpConstants.RESERVED) + " Jingle Nodes relay for account: " + this.accountID.getAccountAddress() + " in " + (System.currentTimeMillis() - start) + " ms.");
            }
            if (nodes != null) {
                this.service.addEntries(nodes);
            }
        }
    }

    private MappedNodes searchServicesWithPrefix(SmackServiceNode service, XMPPConnection xmppConnection, int maxEntries, int maxDepth, int maxSearchNodes, String protocol, boolean searchBuddies, boolean autoDiscover, String prefix) {
        if (xmppConnection == null || !xmppConnection.isConnected()) {
            return null;
        }
        MappedNodes mappedNodes = new MappedNodes();
        ConcurrentHashMap<String, String> visited = new ConcurrentHashMap();
        for (Entry<String, TrackerEntry> entry : service.getTrackerEntries().entrySet()) {
            SmackServiceNode.deepSearch(xmppConnection, maxEntries, ((TrackerEntry) entry.getValue()).getJid(), mappedNodes, maxDepth - 1, maxSearchNodes, protocol, visited);
        }
        if (autoDiscover) {
            if (!searchDiscoItems(service, xmppConnection, maxEntries, xmppConnection.getServiceName(), mappedNodes, maxDepth - 1, maxSearchNodes, protocol, visited, prefix)) {
                return mappedNodes;
            }
            SmackServiceNode.deepSearch(xmppConnection, maxEntries, xmppConnection.getHost(), mappedNodes, maxDepth - 1, maxSearchNodes, protocol, visited);
            if (xmppConnection.getRoster() != null && searchBuddies) {
                for (RosterEntry re : xmppConnection.getRoster().getEntries()) {
                    Iterator<Presence> i = xmppConnection.getRoster().getPresences(re.getUser());
                    while (i.hasNext()) {
                        Presence presence = (Presence) i.next();
                        if (presence.isAvailable()) {
                            SmackServiceNode.deepSearch(xmppConnection, maxEntries, presence.getFrom(), mappedNodes, maxDepth - 1, maxSearchNodes, protocol, visited);
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean searchDiscoItems(SmackServiceNode service, XMPPConnection xmppConnection, int maxEntries, String startPoint, MappedNodes mappedNodes, int maxDepth, int maxSearchNodes, String protocol, ConcurrentHashMap<String, String> visited, String prefix) {
        String[] prefixes = prefix.split(Separators.COMMA);
        boolean stopOnFirst = true;
        String stopOnFirstDefaultValue = JabberActivator.getResources().getSettingsString(JINGLE_NODES_SEARCH_PREFIXES_STOP_ON_FIRST_PROP);
        if (stopOnFirstDefaultValue != null) {
            stopOnFirst = Boolean.parseBoolean(stopOnFirstDefaultValue);
        }
        stopOnFirst = JabberActivator.getConfigurationService().getBoolean(JINGLE_NODES_SEARCH_PREFIXES_STOP_ON_FIRST_PROP, stopOnFirst);
        DiscoverItems items = new DiscoverItems();
        items.setTo(startPoint);
        PacketCollector collector = xmppConnection.createPacketCollector(new PacketIDFilter(items.getPacketID()));
        xmppConnection.sendPacket(items);
        DiscoverItems result = (DiscoverItems) collector.nextResult(Math.round(((double) SmackConfiguration.getPacketReplyTimeout()) * 1.5d));
        if (result != null) {
            Iterator<Item> i = result.getItems();
            Item item = i.hasNext() ? (Item) i.next() : null;
            while (item != null) {
                for (String pref : prefixes) {
                    if (!StringUtils.isNullOrEmpty(pref) && item.getEntityID().startsWith(pref.trim())) {
                        SmackServiceNode.deepSearch(xmppConnection, maxEntries, item.getEntityID(), mappedNodes, maxDepth, maxSearchNodes, protocol, visited);
                        if (stopOnFirst) {
                            return false;
                        }
                    }
                }
                item = i.hasNext() ? (Item) i.next() : null;
            }
            i = result.getItems();
            item = i.hasNext() ? (Item) i.next() : null;
            while (item != null) {
                if (!visited.containsKey(item.getEntityID())) {
                    SmackServiceNode.deepSearch(xmppConnection, maxEntries, item.getEntityID(), mappedNodes, maxDepth, maxSearchNodes, protocol, visited);
                }
                if (stopOnFirst) {
                    return false;
                }
                item = i.hasNext() ? (Item) i.next() : null;
            }
        }
        collector.cancel();
        return true;
    }
}
