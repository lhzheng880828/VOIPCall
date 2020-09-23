package net.java.sip.communicator.impl.protocol.jabber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.java.sip.communicator.impl.protocol.jabber.extensions.caps.CapsPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.caps.EntityCapsManager;
import net.java.sip.communicator.impl.protocol.jabber.extensions.caps.EntityCapsManager.Caps;
import net.java.sip.communicator.service.protocol.OperationSetContactCapabilities;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.NodeInformationProvider;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.DiscoverInfo.Identity;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.packet.DiscoverItems.Item;

public class ScServiceDiscoveryManager implements PacketInterceptor, NodeInformationProvider {
    private static final boolean CACHE_NON_CAPS = true;
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(ScServiceDiscoveryManager.class);
    private final EntityCapsManager capsManager;
    private final XMPPConnection connection;
    private final ServiceDiscoveryManager discoveryManager;
    private final List<String> extCapabilities = new ArrayList();
    private final List<String> features;
    private final List<Identity> identities;
    /* access modifiers changed from: private|final */
    public final Map<String, DiscoverInfo> nonCapsCache = new ConcurrentHashMap();
    /* access modifiers changed from: private|final */
    public final ProtocolProviderServiceJabberImpl parentProvider;
    private DiscoveryInfoRetriever retriever = new DiscoveryInfoRetriever();
    private final List<String> unmodifiableFeatures;

    private class DiscoveryInfoRetriever implements Runnable {
        private OperationSetContactCapabilitiesJabberImpl capabilitiesOpSet;
        private Map<String, Caps> entities;
        private Thread retrieverThread;
        private boolean stopped;

        private DiscoveryInfoRetriever() {
            this.stopped = true;
            this.retrieverThread = null;
            this.entities = new HashMap();
        }

        public void run() {
            try {
                this.stopped = false;
                while (!this.stopped) {
                    Entry<String, Caps> entityToProcess = null;
                    synchronized (this.entities) {
                        if (this.entities.size() == 0) {
                            try {
                                this.entities.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                        Iterator<Entry<String, Caps>> iter = this.entities.entrySet().iterator();
                        if (iter.hasNext()) {
                            entityToProcess = (Entry) iter.next();
                            iter.remove();
                        }
                    }
                    if (entityToProcess != null) {
                        requestDiscoveryInfo((String) entityToProcess.getKey(), (Caps) entityToProcess.getValue());
                    }
                }
            } catch (Throwable t) {
                ScServiceDiscoveryManager.logger.error("Error requesting discovery info, thread ended unexpectedly", t);
            }
        }

        private void requestDiscoveryInfo(String entityID, Caps caps) {
            try {
                boolean fireEvent;
                DiscoverInfo discoverInfo = ScServiceDiscoveryManager.this.discoverInfo(entityID, caps == null ? null : caps.getNodeVer());
                if (!(caps == null || caps.isValid(discoverInfo))) {
                    if (!caps.hash.equals("")) {
                        ScServiceDiscoveryManager.logger.error("Invalid DiscoverInfo for " + caps.getNodeVer() + ": " + discoverInfo);
                    }
                    caps = null;
                }
                if (caps == null) {
                    ScServiceDiscoveryManager.this.nonCapsCache.put(entityID, discoverInfo);
                    fireEvent = true;
                } else {
                    EntityCapsManager.addDiscoverInfoByCaps(caps, discoverInfo);
                    fireEvent = true;
                }
                if (fireEvent && this.capabilitiesOpSet != null) {
                    this.capabilitiesOpSet.fireContactCapabilitiesChanged(entityID);
                }
            } catch (XMPPException ex) {
                if (ScServiceDiscoveryManager.logger.isTraceEnabled()) {
                    ScServiceDiscoveryManager.logger.error("Error requesting discover info for " + entityID, ex);
                }
            }
        }

        public void addEntityForRetrieve(String entityID, Caps caps) {
            synchronized (this.entities) {
                if (!this.entities.containsKey(entityID)) {
                    this.entities.put(entityID, caps);
                    this.entities.notifyAll();
                    if (this.retrieverThread == null) {
                        start();
                    }
                }
            }
        }

        private void start() {
            this.capabilitiesOpSet = (OperationSetContactCapabilitiesJabberImpl) ScServiceDiscoveryManager.this.parentProvider.getOperationSet(OperationSetContactCapabilities.class);
            this.retrieverThread = new Thread(this, ScServiceDiscoveryManager.class.getName());
            this.retrieverThread.setDaemon(true);
            this.retrieverThread.start();
        }

        /* access modifiers changed from: 0000 */
        public void stop() {
            synchronized (this.entities) {
                this.stopped = true;
                this.entities.notifyAll();
                this.retrieverThread = null;
            }
        }
    }

    public ScServiceDiscoveryManager(ProtocolProviderServiceJabberImpl parentProvider, String[] featuresToRemove, String[] featuresToAdd) {
        this.parentProvider = parentProvider;
        this.connection = parentProvider.getConnection();
        this.discoveryManager = ServiceDiscoveryManager.getInstanceFor(this.connection);
        this.features = new ArrayList();
        this.unmodifiableFeatures = Collections.unmodifiableList(this.features);
        this.identities = new ArrayList();
        Identity identity = new Identity("client", ServiceDiscoveryManager.getIdentityName());
        identity.setType(ServiceDiscoveryManager.getIdentityType());
        this.identities.add(identity);
        this.discoveryManager.addFeature(CapsPacketExtension.NAMESPACE);
        if (featuresToRemove != null) {
            for (String featureToRemove : featuresToRemove) {
                this.discoveryManager.removeFeature(featureToRemove);
            }
        }
        if (featuresToAdd != null) {
            for (String featureToAdd : featuresToAdd) {
                if (!this.discoveryManager.includesFeature(featureToAdd)) {
                    this.discoveryManager.addFeature(featureToAdd);
                }
            }
        }
        this.capsManager = new EntityCapsManager();
        this.capsManager.addPacketListener(this.connection);
        initFeatures();
        updateEntityCapsVersion();
        this.connection.addPacketInterceptor(this, new PacketTypeFilter(Presence.class));
    }

    public void addFeature(String feature) {
        synchronized (this.features) {
            this.features.add(feature);
            this.discoveryManager.addFeature(feature);
        }
        updateEntityCapsVersion();
    }

    private void updateEntityCapsVersion() {
        if (this.connection != null && this.capsManager != null) {
            this.capsManager.calculateEntityCapsVersion(getOwnDiscoverInfo());
        }
    }

    public List<String> getFeatures() {
        return this.unmodifiableFeatures;
    }

    public DiscoverInfo getOwnDiscoverInfo() {
        DiscoverInfo di = new DiscoverInfo();
        di.setType(Type.RESULT);
        di.setNode(this.capsManager.getNode() + Separators.POUND + getEntityCapsVersion());
        addDiscoverInfoTo(di);
        return di;
    }

    private String getEntityCapsVersion() {
        return this.capsManager == null ? null : this.capsManager.getCapsVersion();
    }

    private void addDiscoverInfoTo(DiscoverInfo response) {
        Identity identity = new Identity("client", ServiceDiscoveryManager.getIdentityName());
        identity.setType(ServiceDiscoveryManager.getIdentityType());
        response.addIdentity(identity);
        if (!response.containsFeature(CapsPacketExtension.NAMESPACE)) {
            response.addFeature(CapsPacketExtension.NAMESPACE);
        }
        Iterable<String> features = getFeatures();
        synchronized (features) {
            for (String feature : features) {
                if (!response.containsFeature(feature)) {
                    response.addFeature(feature);
                }
            }
        }
    }

    public boolean includesFeature(String feature) {
        return this.discoveryManager.includesFeature(feature);
    }

    public void removeFeature(String feature) {
        synchronized (this.features) {
            this.features.remove(feature);
            this.discoveryManager.removeFeature(feature);
        }
        updateEntityCapsVersion();
    }

    public void addExtFeature(String ext) {
        synchronized (this.extCapabilities) {
            this.extCapabilities.add(ext);
        }
    }

    public void removeExtFeature(String ext) {
        synchronized (this.extCapabilities) {
            this.extCapabilities.remove(ext);
        }
    }

    public synchronized String getExtFeatures() {
        StringBuilder bldr;
        bldr = new StringBuilder("");
        for (String e : this.extCapabilities) {
            bldr.append(e);
            bldr.append(Separators.SP);
        }
        return bldr.toString();
    }

    public void interceptPacket(Packet packet) {
        if ((packet instanceof Presence) && this.capsManager != null) {
            CapsPacketExtension caps = new CapsPacketExtension(getExtFeatures(), this.capsManager.getNode(), CapsPacketExtension.HASH_METHOD, getEntityCapsVersion());
            this.discoveryManager.setNodeInformationProvider(caps.getNode() + Separators.POUND + caps.getVersion(), this);
            packet.addExtension(caps);
        }
    }

    public List<Item> getNodeItems() {
        return null;
    }

    public List<String> getNodeFeatures() {
        return getFeatures();
    }

    public List<Identity> getNodeIdentities() {
        return this.identities;
    }

    private void initFeatures() {
        Iterator<String> defaultFeatures = this.discoveryManager.getFeatures();
        synchronized (this.features) {
            while (defaultFeatures.hasNext()) {
                this.features.add((String) defaultFeatures.next());
            }
        }
    }

    public DiscoverInfo discoverInfo(String entityID) throws XMPPException {
        DiscoverInfo discoverInfo = this.capsManager.getDiscoverInfoByUser(entityID);
        if (discoverInfo != null) {
            return discoverInfo;
        }
        Caps caps = this.capsManager.getCapsByUser(entityID);
        if (caps == null || !caps.isValid(discoverInfo)) {
            discoverInfo = (DiscoverInfo) this.nonCapsCache.get(entityID);
            if (discoverInfo != null) {
                return discoverInfo;
            }
        }
        discoverInfo = discoverInfo(entityID, caps == null ? null : caps.getNodeVer());
        if (!(caps == null || caps.isValid(discoverInfo))) {
            if (!caps.hash.equals("")) {
                logger.error("Invalid DiscoverInfo for " + caps.getNodeVer() + ": " + discoverInfo);
            }
            caps = null;
        }
        if (caps == null) {
            this.nonCapsCache.put(entityID, discoverInfo);
        } else {
            EntityCapsManager.addDiscoverInfoByCaps(caps, discoverInfo);
        }
        return discoverInfo;
    }

    public DiscoverInfo discoverInfoNonBlocking(String entityID) throws XMPPException {
        DiscoverInfo discoverInfo = this.capsManager.getDiscoverInfoByUser(entityID);
        if (discoverInfo != null) {
            return discoverInfo;
        }
        Caps caps = this.capsManager.getCapsByUser(entityID);
        if (caps == null || !caps.isValid(discoverInfo)) {
            discoverInfo = (DiscoverInfo) this.nonCapsCache.get(entityID);
            if (discoverInfo != null) {
                return discoverInfo;
            }
        }
        this.retriever.addEntityForRetrieve(entityID, caps);
        return null;
    }

    /* access modifiers changed from: private */
    public DiscoverInfo discoverInfo(String entityID, String node) throws XMPPException {
        return this.discoveryManager.discoverInfo(entityID, node);
    }

    public DiscoverItems discoverItems(String entityID) throws XMPPException {
        return this.discoveryManager.discoverItems(entityID);
    }

    public DiscoverItems discoverItems(String entityID, String node) throws XMPPException {
        return this.discoveryManager.discoverItems(entityID, node);
    }

    public boolean supportsFeature(String jid, String feature) {
        try {
            DiscoverInfo info = discoverInfo(jid);
            if (info == null || !info.containsFeature(feature)) {
                return false;
            }
            return true;
        } catch (XMPPException ex) {
            logger.info("failed to retrieve disco info for " + jid + " feature " + feature, ex);
            return false;
        }
    }

    public EntityCapsManager getCapsManager() {
        return this.capsManager;
    }

    public void stop() {
        if (this.retriever != null) {
            this.retriever.stop();
        }
    }
}
