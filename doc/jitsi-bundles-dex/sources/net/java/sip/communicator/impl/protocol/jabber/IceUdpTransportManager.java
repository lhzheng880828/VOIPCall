package net.java.sip.communicator.impl.protocol.jabber;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CandidatePacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CandidateType;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.IceUdpTransportPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RtpDescriptionPacketExtension;
import net.java.sip.communicator.service.netaddr.NetworkAddressManagerService;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.StunServerDescriptor;
import net.java.sip.communicator.service.protocol.UserCredentials;
import net.java.sip.communicator.service.protocol.media.TransportManager;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.PortTracker;
import org.ice4j.Transport;
import org.ice4j.TransportAddress;
import org.ice4j.ice.Agent;
import org.ice4j.ice.Candidate;
import org.ice4j.ice.CandidatePair;
import org.ice4j.ice.Component;
import org.ice4j.ice.IceMediaStream;
import org.ice4j.ice.IceProcessingState;
import org.ice4j.ice.LocalCandidate;
import org.ice4j.ice.RemoteCandidate;
import org.ice4j.ice.harvest.StunCandidateHarvester;
import org.ice4j.ice.harvest.TurnCandidateHarvester;
import org.ice4j.ice.harvest.UPNPHarvester;
import org.ice4j.security.LongTermCredential;
import org.jitsi.service.neomedia.DefaultStreamConnector;
import org.jitsi.service.neomedia.MediaStreamTarget;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.StreamConnector;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.util.StringUtils;
import org.xmpp.jnodes.smack.SmackServiceNode;

public class IceUdpTransportManager extends TransportManagerJabberImpl implements PropertyChangeListener {
    private static final int[] COMPONENT_IDS = new int[]{1, 2};
    protected static final String DEFAULT_STUN_SERVER_ADDRESS = "stun.jitsi.net";
    protected static final int DEFAULT_STUN_SERVER_PORT = 3478;
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(IceUdpTransportManager.class);
    protected List<ContentPacketExtension> cpeList;
    protected final Agent iceAgent = createIceAgent();

    public IceUdpTransportManager(CallPeerJabberImpl callPeer) {
        super(callPeer);
        this.iceAgent.addStateChangeListener(this);
    }

    /* access modifiers changed from: protected */
    public Agent createIceAgent() {
        long startGatheringHarvesterTime = System.currentTimeMillis();
        CallPeerJabberImpl peer = (CallPeerJabberImpl) getCallPeer();
        ProtocolProviderServiceJabberImpl provider = (ProtocolProviderServiceJabberImpl) peer.getProtocolProvider();
        NetworkAddressManagerService namSer = getNetAddrMgr();
        boolean atLeastOneStunServer = false;
        Agent agent = namSer.createIceAgent();
        agent.setControlling(!peer.isInitiator());
        JabberAccountIDImpl accID = (JabberAccountIDImpl) provider.getAccountID();
        if (accID.isStunServerDiscoveryEnabled()) {
            String username = StringUtils.parseName(provider.getOurJID());
            String password = JabberActivator.getProtocolProviderFactory().loadPassword(accID);
            UserCredentials credentials = provider.getUserCredentials();
            if (credentials != null) {
                password = credentials.getPasswordAsString();
            }
            if (password == null) {
                credentials = new UserCredentials();
                credentials.setUserName(accID.getUserID());
                credentials = provider.getAuthority().obtainCredentials(accID.getDisplayName(), credentials, 0);
                if (credentials == null) {
                    return null;
                }
                char[] pass = credentials.getPassword();
                if (pass == null) {
                    return null;
                }
                String str = new String(pass);
                if (credentials.isPasswordPersistent()) {
                    JabberActivator.getProtocolProviderFactory().storePassword(accID, str);
                }
            }
            StunCandidateHarvester autoHarvester = namSer.discoverStunServer(accID.getService(), org.jitsi.util.StringUtils.getUTF8Bytes(username), org.jitsi.util.StringUtils.getUTF8Bytes(password));
            if (logger.isInfoEnabled()) {
                logger.info("Auto discovered harvester is " + autoHarvester);
            }
            if (autoHarvester != null) {
                atLeastOneStunServer = true;
                agent.addCandidateHarvester(autoHarvester);
            }
        }
        for (StunServerDescriptor desc : accID.getStunServers()) {
            TransportAddress addr = new TransportAddress(desc.getAddress(), desc.getPort(), Transport.UDP);
            if (addr.getAddress() == null) {
                logger.info("Unresolved address for " + addr);
            } else {
                StunCandidateHarvester harvester;
                if (desc.isTurnSupported()) {
                    harvester = new TurnCandidateHarvester(addr, new LongTermCredential(desc.getUsername(), desc.getPassword()));
                } else {
                    harvester = new StunCandidateHarvester(addr);
                }
                if (logger.isInfoEnabled()) {
                    logger.info("Adding pre-configured harvester " + harvester);
                }
                atLeastOneStunServer = true;
                agent.addCandidateHarvester(harvester);
            }
        }
        if (!atLeastOneStunServer && accID.isUseDefaultStunServer()) {
            agent.addCandidateHarvester(new StunCandidateHarvester(new TransportAddress(DEFAULT_STUN_SERVER_ADDRESS, DEFAULT_STUN_SERVER_PORT, Transport.UDP)));
        }
        if (accID.isJingleNodesRelayEnabled()) {
            SmackServiceNode serviceNode = provider.getJingleNodesServiceNode();
            if (serviceNode != null) {
                agent.addCandidateHarvester(new JingleNodesHarvester(serviceNode));
            }
        }
        if (accID.isUPNPEnabled()) {
            agent.addCandidateHarvester(new UPNPHarvester());
        }
        long stopGatheringHarvesterTime = System.currentTimeMillis();
        if (!logger.isInfoEnabled()) {
            return agent;
        }
        logger.info("End gathering harvester within " + (stopGatheringHarvesterTime - startGatheringHarvesterTime) + " ms");
        return agent;
    }

    /* access modifiers changed from: protected */
    public StreamConnector doCreateStreamConnector(MediaType mediaType) throws OperationFailedException {
        TransportManagerJabberImpl delegate = findTransportManagerEstablishingConnectivityWithJitsiVideobridge();
        if (delegate != null && delegate != this) {
            return delegate.doCreateStreamConnector(mediaType);
        }
        DatagramSocket[] streamConnectorSockets = getStreamConnectorSockets(mediaType);
        return streamConnectorSockets == null ? super.doCreateStreamConnector(mediaType) : new DefaultStreamConnector(streamConnectorSockets[0], streamConnectorSockets[1]);
    }

    public StreamConnector getStreamConnector(MediaType mediaType) throws OperationFailedException {
        StreamConnector streamConnector = super.getStreamConnector(mediaType);
        if (streamConnector == null) {
            return streamConnector;
        }
        DatagramSocket[] streamConnectorSockets = getStreamConnectorSockets(mediaType);
        if (streamConnectorSockets == null) {
            return streamConnector;
        }
        if (streamConnector.getDataSocket() == streamConnectorSockets[0] && streamConnector.getControlSocket() == streamConnectorSockets[1]) {
            return streamConnector;
        }
        closeStreamConnector(mediaType);
        return super.getStreamConnector(mediaType);
    }

    private DatagramSocket[] getStreamConnectorSockets(MediaType mediaType) {
        IceMediaStream stream = this.iceAgent.getStream(mediaType.toString());
        if (stream != null) {
            DatagramSocket[] streamConnectorSockets = new DatagramSocket[COMPONENT_IDS.length];
            int streamConnectorSocketCount = 0;
            for (int i = 0; i < COMPONENT_IDS.length; i++) {
                Component component = stream.getComponent(COMPONENT_IDS[i]);
                if (component != null) {
                    CandidatePair selectedPair = component.getSelectedPair();
                    if (selectedPair != null) {
                        DatagramSocket streamConnectorSocket = selectedPair.getLocalCandidate().getDatagramSocket();
                        if (streamConnectorSocket != null) {
                            streamConnectorSockets[i] = streamConnectorSocket;
                            streamConnectorSocketCount++;
                        }
                    }
                }
            }
            if (streamConnectorSocketCount > 0) {
                return streamConnectorSockets;
            }
        }
        return null;
    }

    public MediaStreamTarget getStreamTarget(MediaType mediaType) {
        TransportManagerJabberImpl delegate = findTransportManagerEstablishingConnectivityWithJitsiVideobridge();
        if (delegate != null && delegate != this) {
            return delegate.getStreamTarget(mediaType);
        }
        IceMediaStream stream = this.iceAgent.getStream(mediaType.toString());
        if (stream == null) {
            return null;
        }
        InetSocketAddress[] streamTargetAddresses = new InetSocketAddress[COMPONENT_IDS.length];
        int streamTargetAddressCount = 0;
        for (int i = 0; i < COMPONENT_IDS.length; i++) {
            Component component = stream.getComponent(COMPONENT_IDS[i]);
            if (component != null) {
                CandidatePair selectedPair = component.getSelectedPair();
                if (selectedPair != null) {
                    InetSocketAddress streamTargetAddress = selectedPair.getRemoteCandidate().getTransportAddress();
                    if (streamTargetAddress != null) {
                        streamTargetAddresses[i] = streamTargetAddress;
                        streamTargetAddressCount++;
                    }
                }
            }
        }
        if (streamTargetAddressCount > 0) {
            return new MediaStreamTarget(streamTargetAddresses[0], streamTargetAddresses[1]);
        }
        return null;
    }

    public String getXmlNamespace() {
        return "urn:xmpp:jingle:transports:ice-udp:1";
    }

    /* access modifiers changed from: protected */
    public PacketExtension createTransportPacketExtension() {
        return new IceUdpTransportPacketExtension();
    }

    /* access modifiers changed from: protected */
    public PacketExtension startCandidateHarvest(ContentPacketExtension theirContent, ContentPacketExtension ourContent, TransportInfoSender transportInfoSender, String media) throws OperationFailedException {
        if (transportInfoSender == null) {
            return createTransportForStartCandidateHarvest(media);
        }
        PacketExtension pe = createTransportPacketExtension();
        ContentPacketExtension transportInfoContent = new ContentPacketExtension();
        for (String name : ourContent.getAttributeNames()) {
            Object value = ourContent.getAttribute(name);
            if (value != null) {
                transportInfoContent.setAttribute(name, value);
            }
        }
        transportInfoContent.addChildExtension(createTransportForStartCandidateHarvest(media));
        Collection<ContentPacketExtension> transportInfoContents = new LinkedList();
        transportInfoContents.add(transportInfoContent);
        transportInfoSender.sendTransportInfo(transportInfoContents);
        return pe;
    }

    public void startCandidateHarvest(List<ContentPacketExtension> theirOffer, List<ContentPacketExtension> ourAnswer, TransportInfoSender transportInfoSender) throws OperationFailedException {
        this.cpeList = ourAnswer;
        super.startCandidateHarvest(theirOffer, ourAnswer, transportInfoSender);
    }

    /* access modifiers changed from: protected */
    public PacketExtension createTransport(IceMediaStream stream) {
        IceUdpTransportPacketExtension transport = new IceUdpTransportPacketExtension();
        Agent iceAgent = stream.getParentAgent();
        transport.setUfrag(iceAgent.getLocalUfrag());
        transport.setPassword(iceAgent.getLocalPassword());
        for (Component component : stream.getComponents()) {
            for (Candidate<?> candidate : component.getLocalCandidates()) {
                transport.addCandidate(createCandidate(candidate));
            }
        }
        return transport;
    }

    /* access modifiers changed from: protected */
    public PacketExtension createTransport(String media) throws OperationFailedException {
        IceMediaStream iceStream = this.iceAgent.getStream(media);
        if (iceStream == null) {
            iceStream = createIceStream(media);
        }
        return createTransport(iceStream);
    }

    private CandidatePacketExtension createCandidate(Candidate<?> candidate) {
        CandidatePacketExtension packet = new CandidatePacketExtension();
        packet.setFoundation(candidate.getFoundation());
        Component component = candidate.getParentComponent();
        packet.setComponent(component.getComponentID());
        packet.setProtocol(candidate.getTransport().toString());
        packet.setPriority(candidate.getPriority());
        packet.setGeneration(component.getParentStream().getParentAgent().getGeneration());
        TransportAddress transportAddress = candidate.getTransportAddress();
        packet.setID(getNextID());
        packet.setIP(transportAddress.getHostAddress());
        packet.setPort(transportAddress.getPort());
        packet.setType(CandidateType.valueOf(candidate.getType().toString()));
        TransportAddress relAddr = candidate.getRelatedAddress();
        if (relAddr != null) {
            packet.setRelAddr(relAddr.getHostAddress());
            packet.setRelPort(relAddr.getPort());
        }
        packet.setNetwork(0);
        return packet;
    }

    /* access modifiers changed from: protected */
    public IceMediaStream createIceStream(String media) throws OperationFailedException {
        try {
            PortTracker portTracker = getPortTracker(media);
            IceMediaStream stream = getNetAddrMgr().createIceStream(portTracker.getPort(), media, this.iceAgent);
            try {
                portTracker.setNextPort(((LocalCandidate) stream.getComponent(2).getLocalCandidates().get(0)).getTransportAddress().getPort() + 1);
            } catch (Throwable t) {
                logger.debug("Determining next port didn't work: ", t);
            }
            return stream;
        } catch (Exception ex) {
            throw new OperationFailedException("Failed to initialize stream " + media, 4, ex);
        }
    }

    public List<ContentPacketExtension> wrapupCandidateHarvest() {
        return this.cpeList;
    }

    private static NetworkAddressManagerService getNetAddrMgr() {
        return JabberActivator.getNetworkAddressManagerService();
    }

    public synchronized boolean startConnectivityEstablishment(Iterable<ContentPacketExtension> remote) {
        boolean z;
        Map map = new LinkedHashMap();
        for (ContentPacketExtension content : remote) {
            IceUdpTransportPacketExtension transport = (IceUdpTransportPacketExtension) content.getFirstChildOfType(IceUdpTransportPacketExtension.class);
            RtpDescriptionPacketExtension description = (RtpDescriptionPacketExtension) content.getFirstChildOfType(RtpDescriptionPacketExtension.class);
            if (description == null && this.cpeList != null) {
                ContentPacketExtension localContent = TransportManagerJabberImpl.findContentByName(this.cpeList, content.getName());
                if (localContent != null) {
                    description = (RtpDescriptionPacketExtension) localContent.getFirstChildOfType(RtpDescriptionPacketExtension.class);
                }
            }
            if (description != null) {
                map.put(description.getMedia(), transport);
            }
        }
        if (((CallPeerJabberImpl) getCallPeer()).isJitsiVideobridge()) {
            sendTransportInfoToJitsiVideobridge(map);
            z = false;
        } else {
            z = startConnectivityEstablishment(map);
        }
        return z;
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized boolean startConnectivityEstablishment(Map<String, IceUdpTransportPacketExtension> remote) {
        boolean z;
        Component component;
        boolean iceAgentStateIsRunning = IceProcessingState.RUNNING.equals(this.iceAgent.getState());
        if (iceAgentStateIsRunning && logger.isInfoEnabled()) {
            logger.info("Update ICE remote candidates");
        }
        int generation = this.iceAgent.getGeneration();
        boolean startConnectivityEstablishment = false;
        for (Entry<String, IceUdpTransportPacketExtension> e : remote.entrySet()) {
            IceUdpTransportPacketExtension transport = (IceUdpTransportPacketExtension) e.getValue();
            List<CandidatePacketExtension> candidates = transport.getChildExtensionsOfType(CandidatePacketExtension.class);
            if (iceAgentStateIsRunning && candidates.size() == 0) {
                z = false;
                break;
            }
            Collections.sort(candidates);
            IceMediaStream stream = this.iceAgent.getStream((String) e.getKey());
            String ufrag = transport.getUfrag();
            if (ufrag != null) {
                stream.setRemoteUfrag(ufrag);
            }
            String password = transport.getPassword();
            if (password != null) {
                stream.setRemotePassword(password);
            }
            for (CandidatePacketExtension candidate : candidates) {
                if (candidate.getGeneration() == generation) {
                    component = stream.getComponent(candidate.getComponent());
                    TransportAddress relatedAddress = null;
                    String relAddr = candidate.getRelAddr();
                    if (relAddr != null) {
                        int relPort = candidate.getRelPort();
                        if (relPort != -1) {
                            TransportAddress transportAddress = new TransportAddress(relAddr, relPort, Transport.parse(candidate.getProtocol()));
                        }
                    }
                    RemoteCandidate remoteCandidate = new RemoteCandidate(new TransportAddress(candidate.getIP(), candidate.getPort(), Transport.parse(candidate.getProtocol())), component, org.ice4j.ice.CandidateType.parse(candidate.getType().toString()), candidate.getFoundation(), (long) candidate.getPriority(), component.findRemoteCandidate(relatedAddress));
                    if (iceAgentStateIsRunning) {
                        component.addUpdateRemoteCandidates(remoteCandidate);
                    } else {
                        component.addRemoteCandidate(remoteCandidate);
                        startConnectivityEstablishment = true;
                    }
                }
            }
        }
        if (iceAgentStateIsRunning) {
            for (IceMediaStream stream2 : this.iceAgent.getStreams()) {
                for (Component component2 : stream2.getComponents()) {
                    component2.updateRemoteCandidates();
                }
            }
        } else if (startConnectivityEstablishment) {
            for (IceMediaStream stream3 : this.iceAgent.getStreams()) {
                for (Component component22 : stream3.getComponents()) {
                    if (component22.getRemoteCandidateCount() < 1) {
                        startConnectivityEstablishment = false;
                        continue;
                        break;
                    }
                }
                if (!startConnectivityEstablishment) {
                    break;
                }
            }
            if (startConnectivityEstablishment) {
                this.iceAgent.startConnectivityEstablishment();
                z = true;
            }
        }
        z = false;
        return z;
    }

    public void wrapupConnectivityEstablishment() throws OperationFailedException {
        TransportManagerJabberImpl delegate = findTransportManagerEstablishingConnectivityWithJitsiVideobridge();
        if (delegate == null || delegate == this) {
            final Object iceProcessingStateSyncRoot = new Object();
            PropertyChangeListener stateChangeListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    Object newValue = evt.getNewValue();
                    if (IceProcessingState.COMPLETED.equals(newValue) || IceProcessingState.FAILED.equals(newValue) || IceProcessingState.TERMINATED.equals(newValue)) {
                        if (IceUdpTransportManager.logger.isTraceEnabled()) {
                            IceUdpTransportManager.logger.trace("ICE " + newValue);
                        }
                        Agent iceAgent = (Agent) evt.getSource();
                        iceAgent.removeStateChangeListener(this);
                        if (iceAgent == IceUdpTransportManager.this.iceAgent) {
                            synchronized (iceProcessingStateSyncRoot) {
                                iceProcessingStateSyncRoot.notify();
                            }
                        }
                    }
                }
            };
            this.iceAgent.addStateChangeListener(stateChangeListener);
            boolean interrupted = false;
            synchronized (iceProcessingStateSyncRoot) {
                while (IceProcessingState.RUNNING.equals(this.iceAgent.getState())) {
                    try {
                        iceProcessingStateSyncRoot.wait();
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
            this.iceAgent.removeStateChangeListener(stateChangeListener);
            if (IceProcessingState.FAILED.equals(this.iceAgent.getState())) {
                throw new OperationFailedException("Could not establish connection (ICE failed)", 1);
            }
        }
        delegate.wrapupConnectivityEstablishment();
        if (this.cpeList != null) {
            for (ContentPacketExtension content : this.cpeList) {
                IceUdpTransportPacketExtension transport = (IceUdpTransportPacketExtension) content.getFirstChildOfType(IceUdpTransportPacketExtension.class);
                if (transport != null) {
                    for (CandidatePacketExtension candidate : transport.getCandidateList()) {
                        transport.removeCandidate(candidate);
                    }
                    Collection<?> childExtensions = transport.getChildExtensionsOfType(CandidatePacketExtension.class);
                    if (childExtensions == null || childExtensions.isEmpty()) {
                        transport.removeAttribute(IceUdpTransportPacketExtension.UFRAG_ATTR_NAME);
                        transport.removeAttribute(IceUdpTransportPacketExtension.PWD_ATTR_NAME);
                    }
                }
            }
        }
    }

    public void removeContent(String name) {
        ContentPacketExtension content = removeContent(this.cpeList, name);
        if (content != null) {
            RtpDescriptionPacketExtension rtpDescription = (RtpDescriptionPacketExtension) content.getFirstChildOfType(RtpDescriptionPacketExtension.class);
            if (rtpDescription != null) {
                IceMediaStream stream = this.iceAgent.getStream(rtpDescription.getMedia());
                if (stream != null) {
                    this.iceAgent.removeStream(stream);
                }
            }
        }
    }

    public synchronized void close() {
        if (this.iceAgent != null) {
            this.iceAgent.removeStateChangeListener(this);
            this.iceAgent.free();
        }
    }

    public String getICECandidateExtendedType(String streamName) {
        return TransportManager.getICECandidateExtendedType(this.iceAgent, streamName);
    }

    public String getICEState() {
        return this.iceAgent.getState().toString();
    }

    public InetSocketAddress getICELocalHostAddress(String streamName) {
        if (this.iceAgent != null) {
            LocalCandidate localCandidate = this.iceAgent.getSelectedLocalCandidate(streamName);
            if (localCandidate != null) {
                return localCandidate.getHostAddress();
            }
        }
        return null;
    }

    public InetSocketAddress getICERemoteHostAddress(String streamName) {
        if (this.iceAgent != null) {
            RemoteCandidate remoteCandidate = this.iceAgent.getSelectedRemoteCandidate(streamName);
            if (remoteCandidate != null) {
                return remoteCandidate.getHostAddress();
            }
        }
        return null;
    }

    public InetSocketAddress getICELocalReflexiveAddress(String streamName) {
        if (this.iceAgent != null) {
            LocalCandidate localCandidate = this.iceAgent.getSelectedLocalCandidate(streamName);
            if (localCandidate != null) {
                return localCandidate.getReflexiveAddress();
            }
        }
        return null;
    }

    public InetSocketAddress getICERemoteReflexiveAddress(String streamName) {
        if (this.iceAgent != null) {
            RemoteCandidate remoteCandidate = this.iceAgent.getSelectedRemoteCandidate(streamName);
            if (remoteCandidate != null) {
                return remoteCandidate.getReflexiveAddress();
            }
        }
        return null;
    }

    public InetSocketAddress getICELocalRelayedAddress(String streamName) {
        if (this.iceAgent != null) {
            LocalCandidate localCandidate = this.iceAgent.getSelectedLocalCandidate(streamName);
            if (localCandidate != null) {
                return localCandidate.getRelayedAddress();
            }
        }
        return null;
    }

    public InetSocketAddress getICERemoteRelayedAddress(String streamName) {
        if (this.iceAgent != null) {
            RemoteCandidate remoteCandidate = this.iceAgent.getSelectedRemoteCandidate(streamName);
            if (remoteCandidate != null) {
                return remoteCandidate.getRelayedAddress();
            }
        }
        return null;
    }

    public long getTotalHarvestingTime() {
        return this.iceAgent == null ? 0 : this.iceAgent.getTotalHarvestingTime();
    }

    public long getHarvestingTime(String harvesterName) {
        return this.iceAgent == null ? 0 : this.iceAgent.getHarvestingTime(harvesterName);
    }

    public int getNbHarvesting() {
        return this.iceAgent == null ? 0 : this.iceAgent.getHarvestCount();
    }

    public int getNbHarvesting(String harvesterName) {
        return this.iceAgent == null ? 0 : this.iceAgent.getHarvestCount(harvesterName);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        ((CallPeerMediaHandlerJabberImpl) ((CallPeerJabberImpl) getCallPeer()).getMediaHandler()).firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
}
