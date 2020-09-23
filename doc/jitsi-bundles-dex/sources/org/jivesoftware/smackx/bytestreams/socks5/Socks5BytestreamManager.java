package org.jivesoftware.smackx.bytestreams.socks5;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl;
import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.XMPPError.Condition;
import org.jivesoftware.smack.util.SyncPacketSend;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.bytestreams.BytestreamListener;
import org.jivesoftware.smackx.bytestreams.BytestreamManager;
import org.jivesoftware.smackx.bytestreams.socks5.packet.Bytestream;
import org.jivesoftware.smackx.bytestreams.socks5.packet.Bytestream.StreamHost;
import org.jivesoftware.smackx.packet.DiscoverInfo.Identity;
import org.jivesoftware.smackx.packet.DiscoverItems.Item;

public final class Socks5BytestreamManager implements BytestreamManager {
    public static final String NAMESPACE = "http://jabber.org/protocol/bytestreams";
    private static final String SESSION_ID_PREFIX = "js5_";
    private static final Map<Connection, Socks5BytestreamManager> managers = new HashMap();
    private static final Random randomGenerator = new Random();
    private final List<BytestreamListener> allRequestListeners = Collections.synchronizedList(new LinkedList());
    private final Connection connection;
    private List<String> ignoredBytestreamRequests = Collections.synchronizedList(new LinkedList());
    private final InitiationListener initiationListener;
    private String lastWorkingProxy = null;
    private final List<String> proxyBlacklist = Collections.synchronizedList(new LinkedList());
    private int proxyConnectionTimeout = MetaContactListServiceImpl.CONTACT_LIST_MODIFICATION_TIMEOUT;
    private boolean proxyPrioritizationEnabled = true;
    private int targetResponseTimeout = MetaContactListServiceImpl.CONTACT_LIST_MODIFICATION_TIMEOUT;
    private final Map<String, BytestreamListener> userListeners = new ConcurrentHashMap();

    static {
        Connection.addConnectionCreationListener(new ConnectionCreationListener() {
            public void connectionCreated(Connection connection) {
                final Socks5BytestreamManager manager = Socks5BytestreamManager.getBytestreamManager(connection);
                connection.addConnectionListener(new AbstractConnectionListener() {
                    public void connectionClosed() {
                        manager.disableService();
                    }
                });
            }
        });
    }

    public static synchronized Socks5BytestreamManager getBytestreamManager(Connection connection) {
        Socks5BytestreamManager manager;
        synchronized (Socks5BytestreamManager.class) {
            if (connection == null) {
                manager = null;
            } else {
                manager = (Socks5BytestreamManager) managers.get(connection);
                if (manager == null) {
                    manager = new Socks5BytestreamManager(connection);
                    managers.put(connection, manager);
                    manager.activate();
                }
            }
        }
        return manager;
    }

    private Socks5BytestreamManager(Connection connection) {
        this.connection = connection;
        this.initiationListener = new InitiationListener(this);
    }

    public void addIncomingBytestreamListener(BytestreamListener listener) {
        this.allRequestListeners.add(listener);
    }

    public void removeIncomingBytestreamListener(BytestreamListener listener) {
        this.allRequestListeners.remove(listener);
    }

    public void addIncomingBytestreamListener(BytestreamListener listener, String initiatorJID) {
        this.userListeners.put(initiatorJID, listener);
    }

    public void removeIncomingBytestreamListener(String initiatorJID) {
        this.userListeners.remove(initiatorJID);
    }

    public void ignoreBytestreamRequestOnce(String sessionID) {
        this.ignoredBytestreamRequests.add(sessionID);
    }

    public synchronized void disableService() {
        this.connection.removePacketListener(this.initiationListener);
        this.initiationListener.shutdown();
        this.allRequestListeners.clear();
        this.userListeners.clear();
        this.lastWorkingProxy = null;
        this.proxyBlacklist.clear();
        this.ignoredBytestreamRequests.clear();
        managers.remove(this.connection);
        if (managers.size() == 0) {
            Socks5Proxy.getSocks5Proxy().stop();
        }
        ServiceDiscoveryManager serviceDiscoveryManager = ServiceDiscoveryManager.getInstanceFor(this.connection);
        if (serviceDiscoveryManager != null) {
            serviceDiscoveryManager.removeFeature(NAMESPACE);
        }
    }

    public int getTargetResponseTimeout() {
        if (this.targetResponseTimeout <= 0) {
            this.targetResponseTimeout = MetaContactListServiceImpl.CONTACT_LIST_MODIFICATION_TIMEOUT;
        }
        return this.targetResponseTimeout;
    }

    public void setTargetResponseTimeout(int targetResponseTimeout) {
        this.targetResponseTimeout = targetResponseTimeout;
    }

    public int getProxyConnectionTimeout() {
        if (this.proxyConnectionTimeout <= 0) {
            this.proxyConnectionTimeout = MetaContactListServiceImpl.CONTACT_LIST_MODIFICATION_TIMEOUT;
        }
        return this.proxyConnectionTimeout;
    }

    public void setProxyConnectionTimeout(int proxyConnectionTimeout) {
        this.proxyConnectionTimeout = proxyConnectionTimeout;
    }

    public boolean isProxyPrioritizationEnabled() {
        return this.proxyPrioritizationEnabled;
    }

    public void setProxyPrioritizationEnabled(boolean proxyPrioritizationEnabled) {
        this.proxyPrioritizationEnabled = proxyPrioritizationEnabled;
    }

    public Socks5BytestreamSession establishSession(String targetJID) throws XMPPException, IOException, InterruptedException {
        return establishSession(targetJID, getNextSessionID());
    }

    public Socks5BytestreamSession establishSession(String targetJID, String sessionID) throws XMPPException, IOException, InterruptedException {
        if (supportsSocks5(targetJID)) {
            List<StreamHost> streamHosts = determineStreamHostInfos(determineProxies());
            String digest = Socks5Utils.createDigest(sessionID, this.connection.getUser(), targetJID);
            if (streamHosts.isEmpty()) {
                throw new XMPPException("no SOCKS5 proxies available");
            }
            if (this.proxyPrioritizationEnabled && this.lastWorkingProxy != null) {
                StreamHost selectedStreamHost = null;
                for (StreamHost streamHost : streamHosts) {
                    if (streamHost.getJID().equals(this.lastWorkingProxy)) {
                        selectedStreamHost = streamHost;
                        break;
                    }
                }
                if (selectedStreamHost != null) {
                    streamHosts.remove(selectedStreamHost);
                    streamHosts.add(0, selectedStreamHost);
                }
            }
            Socks5Proxy socks5Proxy = Socks5Proxy.getSocks5Proxy();
            try {
                socks5Proxy.addTransfer(digest);
                Bytestream initiation = createBytestreamInitiation(sessionID, targetJID, streamHosts);
                StreamHost usedStreamHost = initiation.getStreamHost(((Bytestream) SyncPacketSend.getReply(this.connection, initiation, (long) getTargetResponseTimeout())).getUsedHost().getJID());
                if (usedStreamHost == null) {
                    throw new XMPPException("Remote user responded with unknown host");
                }
                Socket socket = new Socks5ClientForInitiator(usedStreamHost, digest, this.connection, sessionID, targetJID).getSocket(getProxyConnectionTimeout());
                this.lastWorkingProxy = usedStreamHost.getJID();
                Socks5BytestreamSession socks5BytestreamSession = new Socks5BytestreamSession(socket, usedStreamHost.getJID().equals(this.connection.getUser()));
                socks5Proxy.removeTransfer(digest);
                return socks5BytestreamSession;
            } catch (TimeoutException e) {
                throw new IOException("Timeout while connecting to SOCKS5 proxy");
            } catch (Throwable th) {
                socks5Proxy.removeTransfer(digest);
            }
        } else {
            throw new XMPPException(targetJID + " doesn't support SOCKS5 Bytestream");
        }
    }

    private boolean supportsSocks5(String targetJID) throws XMPPException {
        return ServiceDiscoveryManager.getInstanceFor(this.connection).discoverInfo(targetJID).containsFeature(NAMESPACE);
    }

    private List<String> determineProxies() throws XMPPException {
        ServiceDiscoveryManager serviceDiscoveryManager = ServiceDiscoveryManager.getInstanceFor(this.connection);
        List<String> proxies = new ArrayList();
        Iterator<Item> itemIterator = serviceDiscoveryManager.discoverItems(this.connection.getServiceName()).getItems();
        while (itemIterator.hasNext()) {
            Item item = (Item) itemIterator.next();
            if (!this.proxyBlacklist.contains(item.getEntityID())) {
                try {
                    Iterator<Identity> identities = serviceDiscoveryManager.discoverInfo(item.getEntityID()).getIdentities();
                    while (identities.hasNext()) {
                        Identity identity = (Identity) identities.next();
                        if ("proxy".equalsIgnoreCase(identity.getCategory()) && "bytestreams".equalsIgnoreCase(identity.getType())) {
                            proxies.add(item.getEntityID());
                            break;
                        }
                        this.proxyBlacklist.add(item.getEntityID());
                    }
                } catch (XMPPException e) {
                    this.proxyBlacklist.add(item.getEntityID());
                }
            }
        }
        return proxies;
    }

    private List<StreamHost> determineStreamHostInfos(List<String> proxies) {
        List<StreamHost> streamHosts = new ArrayList();
        List<StreamHost> localProxies = getLocalStreamHost();
        if (localProxies != null) {
            streamHosts.addAll(localProxies);
        }
        for (String proxy : proxies) {
            try {
                streamHosts.addAll(((Bytestream) SyncPacketSend.getReply(this.connection, createStreamHostRequest(proxy))).getStreamHosts());
            } catch (XMPPException e) {
                this.proxyBlacklist.add(proxy);
            }
        }
        return streamHosts;
    }

    private Bytestream createStreamHostRequest(String proxy) {
        Bytestream request = new Bytestream();
        request.setType(Type.GET);
        request.setTo(proxy);
        return request;
    }

    private List<StreamHost> getLocalStreamHost() {
        Socks5Proxy socks5Server = Socks5Proxy.getSocks5Proxy();
        if (socks5Server.isRunning()) {
            List<String> addresses = socks5Server.getLocalAddresses();
            int port = socks5Server.getPort();
            if (addresses.size() >= 1) {
                List<StreamHost> arrayList = new ArrayList();
                for (String address : addresses) {
                    StreamHost streamHost = new StreamHost(this.connection.getUser(), address);
                    streamHost.setPort(port);
                    arrayList.add(streamHost);
                }
                return arrayList;
            }
        }
        return null;
    }

    private Bytestream createBytestreamInitiation(String sessionID, String targetJID, List<StreamHost> streamHosts) {
        Bytestream initiation = new Bytestream(sessionID);
        for (StreamHost streamHost : streamHosts) {
            initiation.addStreamHost(streamHost);
        }
        initiation.setType(Type.SET);
        initiation.setTo(targetJID);
        return initiation;
    }

    /* access modifiers changed from: protected */
    public void replyRejectPacket(IQ packet) {
        this.connection.sendPacket(IQ.createErrorResponse(packet, new XMPPError(Condition.no_acceptable)));
    }

    private void activate() {
        this.connection.addPacketListener(this.initiationListener, this.initiationListener.getFilter());
        enableService();
    }

    private void enableService() {
        ServiceDiscoveryManager manager = ServiceDiscoveryManager.getInstanceFor(this.connection);
        if (!manager.includesFeature(NAMESPACE)) {
            manager.addFeature(NAMESPACE);
        }
    }

    private String getNextSessionID() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(SESSION_ID_PREFIX);
        buffer.append(Math.abs(randomGenerator.nextLong()));
        return buffer.toString();
    }

    /* access modifiers changed from: protected */
    public Connection getConnection() {
        return this.connection;
    }

    /* access modifiers changed from: protected */
    public BytestreamListener getUserListener(String initiator) {
        return (BytestreamListener) this.userListeners.get(initiator);
    }

    /* access modifiers changed from: protected */
    public List<BytestreamListener> getAllRequestListeners() {
        return this.allRequestListeners;
    }

    /* access modifiers changed from: protected */
    public List<String> getIgnoredBytestreamRequests() {
        return this.ignoredBytestreamRequests;
    }
}
