package net.java.sip.communicator.impl.protocol.jabber;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriConferenceIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriConferenceIQ.Channel;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriConferenceIQ.Content;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension.CreatorEnum;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension.SendersEnum;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.DtlsFingerprintPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.IceUdpTransportPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.InputEvtPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.PayloadTypePacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RtpDescriptionPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.jinglesdp.JingleUtils;
import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.CallConference;
import net.java.sip.communicator.service.protocol.CallPeerState;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetDesktopSharingClient;
import net.java.sip.communicator.service.protocol.media.MediaAwareCallConference;
import net.java.sip.communicator.service.protocol.media.SrtpControls;
import net.java.sip.communicator.util.Logger;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.service.neomedia.DtlsControl;
import org.jitsi.service.neomedia.DtlsControl.Setup;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.MediaStream;
import org.jitsi.service.neomedia.MediaStreamTarget;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.QualityControl;
import org.jitsi.service.neomedia.QualityPreset;
import org.jitsi.service.neomedia.RTPExtension;
import org.jitsi.service.neomedia.SrtpControl;
import org.jitsi.service.neomedia.SrtpControlType;
import org.jitsi.service.neomedia.StreamConnector;
import org.jitsi.service.neomedia.VideoMediaStream;
import org.jitsi.service.neomedia.device.MediaDevice;
import org.jitsi.service.neomedia.format.MediaFormat;
import org.jivesoftware.smackx.packet.DiscoverInfo;

public class CallPeerMediaHandlerJabberImpl extends AbstractCallPeerMediaHandlerJabberGTalkImpl<CallPeerJabberImpl> {
    private static final Logger logger = Logger.getLogger(CallPeerMediaHandlerJabberImpl.class);
    private final Map<String, ContentPacketExtension> localContentMap = new LinkedHashMap();
    private final QualityControlWrapper qualityControls;
    private final Map<String, ContentPacketExtension> remoteContentMap = new LinkedHashMap();
    private boolean remotelyOnHold = false;
    private boolean supportQualityControls = false;
    private String[] supportedTransports = null;
    private final Object supportedTransportsSyncRoot = new Object();
    private TransportManagerJabberImpl transportManager;
    private final Object transportManagerSyncRoot = new Object();

    private static boolean isFeatureSupported(ScServiceDiscoveryManager discoveryManager, DiscoverInfo discoverInfo, String feature) {
        return discoveryManager.includesFeature(feature) && (discoverInfo == null || discoverInfo.containsFeature(feature));
    }

    public CallPeerMediaHandlerJabberImpl(CallPeerJabberImpl peer) {
        super(peer);
        this.qualityControls = new QualityControlWrapper(peer);
    }

    private MediaDirection calculatePostHoldDirection(MediaStream stream) {
        MediaDirection streamDirection = stream.getDirection();
        if (streamDirection.allowsSending()) {
            return streamDirection;
        }
        MediaDirection postHoldDir = JingleUtils.getDirection((ContentPacketExtension) this.remoteContentMap.get(stream.getName()), !((CallPeerJabberImpl) getPeer()).isInitiator());
        MediaDevice device = stream.getDevice();
        postHoldDir = postHoldDir.and(getDirectionUserPreference(device.getMediaType()));
        if (isLocallyOnHold()) {
            postHoldDir = postHoldDir.and(MediaDirection.SENDONLY);
        }
        return postHoldDir.and(device.getDirection());
    }

    public synchronized void close() {
        super.close();
        OperationSetDesktopSharingClientJabberImpl client = (OperationSetDesktopSharingClientJabberImpl) ((ProtocolProviderServiceJabberImpl) ((CallPeerJabberImpl) getPeer()).getProtocolProvider()).getOperationSet(OperationSetDesktopSharingClient.class);
        if (client != null) {
            client.fireRemoteControlRevoked(getPeer());
        }
    }

    private ContentPacketExtension createContent(MediaDevice dev) throws OperationFailedException {
        MediaType mediaType = dev.getMediaType();
        MediaDirection direction = dev.getDirection();
        CallPeerJabberImpl peer = (CallPeerJabberImpl) getPeer();
        if (!(MediaType.VIDEO.equals(mediaType) && isRTPTranslationEnabled(mediaType))) {
            direction = direction.and(getDirectionUserPreference(mediaType));
        }
        CallJabberImpl call = (CallJabberImpl) peer.getCall();
        if (call.isConferenceFocus()) {
            for (CallPeerJabberImpl anotherPeer : call.getCallPeerList()) {
                if (anotherPeer != peer && anotherPeer.getDirection(mediaType).allowsReceiving()) {
                    direction = direction.or(MediaDirection.SENDONLY);
                    break;
                }
            }
        }
        if (isLocallyOnHold()) {
            direction = direction.and(MediaDirection.SENDONLY);
        }
        QualityPreset sendQualityPreset = null;
        QualityPreset receiveQualityPreset = null;
        if (this.qualityControls != null) {
            sendQualityPreset = this.qualityControls.getRemoteReceivePreset();
            receiveQualityPreset = this.qualityControls.getRemoteSendMaxPreset();
        }
        if (direction == MediaDirection.INACTIVE) {
            return null;
        }
        ContentPacketExtension content = createContentForOffer(getLocallySupportedFormats(dev, sendQualityPreset, receiveQualityPreset), direction, dev.getSupportedExtensions());
        RtpDescriptionPacketExtension description = JingleUtils.getRtpDescription(content);
        setDtlsEncryptionOnContent(mediaType, content, null);
        if (call.getConference().isJitsiVideobridge()) {
            return content;
        }
        setSDesEncryptionOnDescription(mediaType, description, null);
        setZrtpEncryptionOnDescription(mediaType, description, null);
        return content;
    }

    public ContentPacketExtension createContentForMedia(MediaType mediaType) throws OperationFailedException {
        MediaDevice dev = getDefaultDevice(mediaType);
        if (isDeviceActive(dev)) {
            return createContent(dev);
        }
        return null;
    }

    private ContentPacketExtension createContentForOffer(List<MediaFormat> supportedFormats, MediaDirection direction, List<RTPExtension> supportedExtensions) {
        boolean z;
        CreatorEnum creatorEnum = CreatorEnum.initiator;
        String mediaType = ((MediaFormat) supportedFormats.get(0)).getMediaType().toString();
        if (((CallPeerJabberImpl) getPeer()).isInitiator()) {
            z = false;
        } else {
            z = true;
        }
        ContentPacketExtension content = JingleUtils.createDescription(creatorEnum, mediaType, JingleUtils.getSenders(direction, z), supportedFormats, supportedExtensions, getDynamicPayloadTypes(), getRtpExtensionsRegistry());
        this.localContentMap.put(content.getName(), content);
        return content;
    }

    public List<ContentPacketExtension> createContentList() throws OperationFailedException {
        List<ContentPacketExtension> mediaDescs = new ArrayList();
        boolean jitsiVideobridge = ((CallJabberImpl) ((CallPeerJabberImpl) getPeer()).getCall()).getConference().isJitsiVideobridge();
        for (MediaType mediaType : MediaType.values()) {
            MediaDevice dev = getDefaultDevice(mediaType);
            if (isDeviceActive(dev)) {
                MediaDirection direction = dev.getDirection();
                if (!(MediaType.VIDEO.equals(mediaType) && isRTPTranslationEnabled(mediaType))) {
                    direction = direction.and(getDirectionUserPreference(mediaType));
                }
                if (isLocallyOnHold()) {
                    direction = direction.and(MediaDirection.SENDONLY);
                }
                if (MediaDirection.RECVONLY.equals(direction)) {
                    direction = MediaDirection.INACTIVE;
                }
                if (direction != MediaDirection.INACTIVE) {
                    ContentPacketExtension content = createContentForOffer(getLocallySupportedFormats(dev), direction, dev.getSupportedExtensions());
                    RtpDescriptionPacketExtension description = JingleUtils.getRtpDescription(content);
                    setDtlsEncryptionOnContent(mediaType, content, null);
                    if (!jitsiVideobridge) {
                        setSDesEncryptionOnDescription(mediaType, description, null);
                        setZrtpEncryptionOnDescription(mediaType, description, null);
                    }
                    if (description.getMedia().equals(MediaType.VIDEO.toString()) && getLocalInputEvtAware()) {
                        content.addChildExtension(new InputEvtPacketExtension());
                    }
                    mediaDescs.add(content);
                }
            }
        }
        if (mediaDescs.isEmpty()) {
            ProtocolProviderServiceJabberImpl.throwOperationFailedException("We couldn't find any active Audio/Video devices and couldn't create a call", 1, null, logger);
        }
        return harvestCandidates(null, mediaDescs, null);
    }

    public List<ContentPacketExtension> createContentList(MediaType mediaType) throws OperationFailedException {
        MediaDevice dev = getDefaultDevice(mediaType);
        List<ContentPacketExtension> mediaDescs = new ArrayList();
        if (isDeviceActive(dev)) {
            ContentPacketExtension content = createContent(dev);
            if (content != null) {
                mediaDescs.add(content);
            }
        }
        if (mediaDescs.isEmpty()) {
            ProtocolProviderServiceJabberImpl.throwOperationFailedException("We couldn't find any active Audio/Video devices and couldn't create a call", 1, null, logger);
        }
        return harvestCandidates(null, mediaDescs, null);
    }

    /* access modifiers changed from: protected */
    public void firePropertyChange(String property, Object oldValue, Object newValue) {
        super.firePropertyChange(property, oldValue, newValue);
    }

    public Iterable<ContentPacketExtension> generateSessionAccept() throws OperationFailedException {
        ContentPacketExtension ourContent;
        TransportManagerJabberImpl transportManager = getTransportManager();
        Iterable<ContentPacketExtension> sessAccept = transportManager.wrapupCandidateHarvest();
        CallPeerJabberImpl peer = (CallPeerJabberImpl) getPeer();
        Map<ContentPacketExtension, RtpDescriptionPacketExtension> contents = new HashMap();
        for (ContentPacketExtension ourContent2 : sessAccept) {
            contents.put(ourContent2, JingleUtils.getRtpDescription(ourContent2));
        }
        boolean masterStreamSet = false;
        for (Entry<ContentPacketExtension, RtpDescriptionPacketExtension> en : contents.entrySet()) {
            ourContent2 = (ContentPacketExtension) en.getKey();
            RtpDescriptionPacketExtension description = (RtpDescriptionPacketExtension) en.getValue();
            MediaType type = MediaType.parseString(description.getMedia());
            StreamConnector connector = transportManager.getStreamConnector(type);
            MediaDevice dev = getDefaultDevice(type);
            if (isDeviceActive(dev)) {
                MediaStreamTarget target = transportManager.getStreamTarget(type);
                MediaDirection direction = JingleUtils.getDirection(ourContent2, !peer.isInitiator());
                if (MediaType.VIDEO.equals(type) && ((isLocalVideoTransmissionEnabled() || isRTPTranslationEnabled(type)) && dev.getDirection().allowsSending())) {
                    direction = MediaDirection.SENDRECV;
                    ourContent2.setSenders(SendersEnum.both);
                }
                String contentName = ourContent2.getName();
                RtpDescriptionPacketExtension theirDescription = JingleUtils.getRtpDescription((ContentPacketExtension) this.remoteContentMap.get(contentName));
                MediaFormat format = null;
                List<MediaFormat> localFormats = getLocallySupportedFormats(dev);
                for (PayloadTypePacketExtension payload : theirDescription.getPayloadTypes()) {
                    format = JingleUtils.payloadTypeToMediaFormat(payload, getDynamicPayloadTypes());
                    if (format != null && localFormats.contains(format)) {
                        break;
                    }
                }
                if (format == null) {
                    ProtocolProviderServiceJabberImpl.throwOperationFailedException("No matching codec.", 11, null, logger);
                }
                List<RTPExtension> rtpExtensions = JingleUtils.extractRTPExtensions(description, getRtpExtensionsRegistry());
                Map<String, String> adv = format.getAdvancedAttributes();
                if (adv != null) {
                    for (Entry<String, String> f : adv.entrySet()) {
                        if (((String) f.getKey()).equals("imageattr")) {
                            this.supportQualityControls = true;
                        }
                    }
                }
                boolean masterStream = false;
                if (!masterStreamSet) {
                    if (contents.size() > 1) {
                        if (type.equals(MediaType.AUDIO)) {
                            masterStream = true;
                            masterStreamSet = true;
                        }
                    } else {
                        masterStream = true;
                        masterStreamSet = true;
                    }
                }
                initStream(contentName, connector, dev, format, target, direction, rtpExtensions, masterStream);
            }
        }
        return sessAccept;
    }

    public ContentPacketExtension getLocalContent(String contentType) {
        for (String key : this.localContentMap.keySet()) {
            ContentPacketExtension content = (ContentPacketExtension) this.localContentMap.get(key);
            if (JingleUtils.getRtpDescription(content).getMedia().equals(contentType)) {
                return content;
            }
        }
        return null;
    }

    public Iterable<ContentPacketExtension> getLocalContentList() {
        return this.localContentMap.values();
    }

    public QualityControl getQualityControl() {
        if (this.supportQualityControls) {
            return this.qualityControls;
        }
        return null;
    }

    public ContentPacketExtension getRemoteContent(String contentType) {
        for (String key : this.remoteContentMap.keySet()) {
            ContentPacketExtension content = (ContentPacketExtension) this.remoteContentMap.get(key);
            if (JingleUtils.getRtpDescription(content).getMedia().equals(contentType)) {
                return content;
            }
        }
        return null;
    }

    public long getRemoteSSRC(MediaType mediaType) {
        int[] ssrcs = getRemoteSSRCs(mediaType);
        if (ssrcs.length != 0) {
            return 4294967295L & ((long) ssrcs[ssrcs.length - 1]);
        }
        return ((CallPeerJabberImpl) getPeer()).isJitsiVideobridge() ? -1 : super.getRemoteSSRC(mediaType);
    }

    private int[] getRemoteSSRCs(MediaType mediaType) {
        Channel channel = getColibriChannel(mediaType);
        if (channel != null) {
            return channel.getSSRCs();
        }
        if (super.getRemoteSSRC(mediaType) == -1) {
            return ColibriConferenceIQ.NO_SSRCS;
        }
        return new int[]{(int) super.getRemoteSSRC(mediaType)};
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized TransportManagerJabberImpl getTransportManager() {
        TransportManagerJabberImpl transportManagerJabberImpl;
        if (this.transportManager == null) {
            CallPeerJabberImpl peer = (CallPeerJabberImpl) getPeer();
            if (peer.isInitiator()) {
                synchronized (this.transportManagerSyncRoot) {
                    try {
                        this.transportManagerSyncRoot.wait(5000);
                    } catch (InterruptedException e) {
                    }
                }
                if (this.transportManager == null) {
                    throw new IllegalStateException("The initiator is expected to specify the transport in their offer.");
                }
                transportManagerJabberImpl = this.transportManager;
            } else {
                int i;
                ProtocolProviderServiceJabberImpl protocolProvider = (ProtocolProviderServiceJabberImpl) peer.getProtocolProvider();
                ScServiceDiscoveryManager discoveryManager = protocolProvider.getDiscoveryManager();
                DiscoverInfo peerDiscoverInfo = peer.getDiscoveryInfo();
                synchronized (this.supportedTransportsSyncRoot) {
                    if (this.supportedTransports != null && this.supportedTransports.length > 0) {
                        for (i = 0; i < this.supportedTransports.length; i++) {
                            if ("urn:xmpp:jingle:transports:ice-udp:1".equals(this.supportedTransports[i])) {
                                this.transportManager = new IceUdpTransportManager(peer);
                                break;
                            }
                            if ("urn:xmpp:jingle:transports:raw-udp:1".equals(this.supportedTransports[i])) {
                                this.transportManager = new RawUdpTransportManager(peer);
                                break;
                            }
                        }
                        if (this.transportManager == null) {
                            logger.warn("Could not find a supported TransportManager in supportedTransports. Will try to select one based on disco#info.");
                        }
                    }
                }
                if (this.transportManager == null) {
                    String[] transports = new String[]{"urn:xmpp:jingle:transports:ice-udp:1", "urn:xmpp:jingle:transports:raw-udp:1"};
                    if (peer.isJitsiVideobridge() && ((CallJabberImpl) peer.getCall()) != null) {
                        String jitsiVideobridge = ((CallJabberImpl) peer.getCall()).getJitsiVideobridge();
                        if (!(jitsiVideobridge == null || protocolProvider.isFeatureSupported(jitsiVideobridge, "urn:xmpp:jingle:transports:ice-udp:1"))) {
                            for (i = transports.length - 1; i >= 0; i--) {
                                if ("urn:xmpp:jingle:transports:ice-udp:1".equals(transports[i])) {
                                    transports[i] = null;
                                }
                            }
                        }
                    }
                    for (String transport : transports) {
                        if (transport != null && isFeatureSupported(discoveryManager, peerDiscoverInfo, transport)) {
                            if ("urn:xmpp:jingle:transports:ice-udp:1".equals(transport)) {
                                this.transportManager = new IceUdpTransportManager(peer);
                            } else if ("urn:xmpp:jingle:transports:raw-udp:1".equals(transport)) {
                                this.transportManager = new RawUdpTransportManager(peer);
                            }
                            if (this.transportManager != null) {
                                break;
                            }
                        }
                    }
                    if (this.transportManager == null && logger.isDebugEnabled()) {
                        logger.debug("No known Jingle transport supported by Jabber call peer " + peer);
                    }
                }
            }
        }
        transportManagerJabberImpl = this.transportManager;
        return transportManagerJabberImpl;
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized TransportManagerJabberImpl queryTransportManager() {
        return this.transportManager;
    }

    public List<Component> getVisualComponents() {
        CallJabberImpl call = (CallJabberImpl) ((CallPeerJabberImpl) getPeer()).getCall();
        if (call != null) {
            MediaAwareCallConference conference = call.getConference();
            if (conference != null && conference.isJitsiVideobridge()) {
                MediaStream stream = getStream(MediaType.VIDEO);
                if (stream == null) {
                    return Collections.emptyList();
                }
                int[] remoteSSRCs = getRemoteSSRCs(MediaType.VIDEO);
                if (remoteSSRCs.length == 0) {
                    return Collections.emptyList();
                }
                VideoMediaStream videoStream = (VideoMediaStream) stream;
                List<Component> visualComponents = new LinkedList();
                for (int remoteSSRC : remoteSSRCs) {
                    Component visualComponent = videoStream.getVisualComponent(4294967295L & ((long) remoteSSRC));
                    if (visualComponent != null) {
                        visualComponents.add(visualComponent);
                    }
                }
                return visualComponents;
            }
        }
        return super.getVisualComponents();
    }

    private List<ContentPacketExtension> harvestCandidates(List<ContentPacketExtension> remote, List<ContentPacketExtension> local, TransportInfoSender transportInfoSender) throws OperationFailedException {
        long startCandidateHarvestTime = System.currentTimeMillis();
        TransportManagerJabberImpl transportManager = getTransportManager();
        if (remote != null) {
            transportManager.startCandidateHarvest(remote, local, transportInfoSender);
        } else if (transportInfoSender != null) {
            throw new IllegalArgumentException("transportInfoSender");
        } else {
            transportManager.startCandidateHarvest(local, transportInfoSender);
        }
        long stopCandidateHarvestTime = System.currentTimeMillis();
        if (logger.isInfoEnabled()) {
            logger.info("End candidate harvest within " + (stopCandidateHarvestTime - startCandidateHarvestTime) + " ms");
        }
        setDtlsEncryptionOnTransports(remote, local);
        if (transportManager.startConnectivityEstablishmentWithJitsiVideobridge) {
            Map map = new LinkedHashMap();
            for (MediaType mediaType : MediaType.values()) {
                Channel channel = transportManager.getColibriChannel(mediaType, true);
                if (channel != null) {
                    IceUdpTransportPacketExtension transport = channel.getTransport();
                    if (transport != null) {
                        map.put(mediaType.toString(), transport);
                    }
                }
            }
            if (!map.isEmpty()) {
                transportManager.startConnectivityEstablishmentWithJitsiVideobridge = false;
                transportManager.startConnectivityEstablishment(map);
            }
        }
        return transportManager.wrapupCandidateHarvest();
    }

    /* access modifiers changed from: protected */
    public MediaStream initStream(String streamName, StreamConnector connector, MediaDevice device, MediaFormat format, MediaStreamTarget target, MediaDirection direction, List<RTPExtension> rtpExtensions, boolean masterStream) throws OperationFailedException {
        MediaStream stream = super.initStream(connector, device, format, target, direction, rtpExtensions, masterStream);
        if (stream != null) {
            stream.setName(streamName);
        }
        return stream;
    }

    /* access modifiers changed from: protected */
    public void mediaHandlerPropertyChange(PropertyChangeEvent ev) {
        String propertyName = ev.getPropertyName();
        if ((!"AUDIO_REMOTE_SSRC".equals(propertyName) && !"VIDEO_REMOTE_SSRC".equals(propertyName)) || !((CallPeerJabberImpl) getPeer()).isJitsiVideobridge()) {
            super.mediaHandlerPropertyChange(ev);
        }
    }

    public void processAnswer(List<ContentPacketExtension> answer) throws OperationFailedException, IllegalArgumentException {
        processTransportInfo(answer);
        boolean masterStreamSet = false;
        for (ContentPacketExtension content : answer) {
            this.remoteContentMap.put(content.getName(), content);
            boolean masterStream = false;
            if (!masterStreamSet) {
                if (answer.size() > 1) {
                    if (MediaType.AUDIO.toString().equals(JingleUtils.getRtpDescription(content).getMedia())) {
                        masterStream = true;
                        masterStreamSet = true;
                    }
                } else {
                    masterStream = true;
                    masterStreamSet = true;
                }
            }
            processContent(content, false, masterStream);
        }
    }

    /* access modifiers changed from: 0000 */
    public void processColibriConferenceIQ(ColibriConferenceIQ conferenceIQ) {
        TransportManagerJabberImpl transportManager = this.transportManager;
        if (transportManager != null) {
            long oldAudioRemoteSSRC = getRemoteSSRC(MediaType.AUDIO);
            long oldVideoRemoteSSRC = getRemoteSSRC(MediaType.VIDEO);
            for (MediaType mediaType : MediaType.values()) {
                Channel dst = transportManager.getColibriChannel(mediaType, false);
                if (dst != null) {
                    Content content = conferenceIQ.getContent(mediaType.toString());
                    if (content != null) {
                        Channel src = content.getChannel(dst.getID());
                        if (src != null) {
                            int[] ssrcs = src.getSSRCs();
                            if (!Arrays.equals(dst.getSSRCs(), ssrcs)) {
                                dst.setSSRCs(ssrcs);
                            }
                        }
                    }
                }
            }
            long newAudioRemoteSSRC = getRemoteSSRC(MediaType.AUDIO);
            long newVideoRemoteSSRC = getRemoteSSRC(MediaType.VIDEO);
            if (oldAudioRemoteSSRC != newAudioRemoteSSRC) {
                firePropertyChange("AUDIO_REMOTE_SSRC", Long.valueOf(oldAudioRemoteSSRC), Long.valueOf(newAudioRemoteSSRC));
            }
            if (oldVideoRemoteSSRC != newVideoRemoteSSRC) {
                firePropertyChange("VIDEO_REMOTE_SSRC", Long.valueOf(oldVideoRemoteSSRC), Long.valueOf(newVideoRemoteSSRC));
            }
        }
    }

    private void processContent(ContentPacketExtension content, boolean modify, boolean masterStream) throws OperationFailedException, IllegalArgumentException {
        RtpDescriptionPacketExtension description = JingleUtils.getRtpDescription(content);
        MediaType mediaType = MediaType.parseString(description.getMedia());
        TransportManagerJabberImpl transportManager = getTransportManager();
        MediaStreamTarget target = transportManager.getStreamTarget(mediaType);
        if (target == null) {
            target = JingleUtils.extractDefaultTarget(content);
        }
        if (target == null || target.getDataAddress().getPort() == 0) {
            closeStream(mediaType);
            return;
        }
        List<MediaFormat> supportedFormats = JingleUtils.extractFormats(description, getDynamicPayloadTypes());
        MediaDevice dev = getDefaultDevice(mediaType);
        if (isDeviceActive(dev)) {
            MediaDirection devDirection = (dev == null ? MediaDirection.INACTIVE : dev.getDirection()).and(getDirectionUserPreference(mediaType));
            if (supportedFormats.isEmpty()) {
                ProtocolProviderServiceJabberImpl.throwOperationFailedException("Remote party sent an invalid Jingle answer.", 11, null, logger);
            }
            CallJabberImpl call = (CallJabberImpl) ((CallPeerJabberImpl) getPeer()).getCall();
            CallConference conference = call == null ? null : call.getConference();
            if (conference == null || !conference.isJitsiVideobridge()) {
                addZrtpAdvertisedEncryptions(true, description, mediaType);
                addSDesAdvertisedEncryptions(true, description, mediaType);
            }
            addDtlsAdvertisedEncryptions(true, content, mediaType);
            StreamConnector connector = transportManager.getStreamConnector(mediaType);
            MediaDirection remoteDirection = JingleUtils.getDirection(content, ((CallPeerJabberImpl) getPeer()).isInitiator());
            if (conference != null && conference.isConferenceFocus()) {
                for (CallPeerJabberImpl peer : call.getCallPeerList()) {
                    SendersEnum senders = peer.getSenders(mediaType);
                    boolean initiator = peer.isInitiator();
                    if (senders == null || SendersEnum.both == senders || ((initiator && SendersEnum.initiator == senders) || (!initiator && SendersEnum.responder == senders))) {
                        remoteDirection = remoteDirection.or(MediaDirection.SENDONLY);
                    }
                }
            }
            MediaDirection direction = devDirection.getDirectionForAnswer(remoteDirection);
            List<RTPExtension> rtpExtensions = intersectRTPExtensions(JingleUtils.extractRTPExtensions(description, getRtpExtensionsRegistry()), getExtensionsForType(mediaType));
            Map<String, String> adv = ((MediaFormat) supportedFormats.get(0)).getAdvancedAttributes();
            if (adv != null) {
                for (Entry<String, String> f : adv.entrySet()) {
                    if (((String) f.getKey()).equals("imageattr")) {
                        this.supportQualityControls = true;
                    }
                }
            }
            if (mediaType.equals(MediaType.VIDEO) && modify) {
                MediaStream stream = getStream(MediaType.VIDEO);
                if (!(stream == null || dev == null)) {
                    List<MediaFormat> fmts = supportedFormats;
                    if (fmts.size() > 0) {
                        ((VideoMediaStream) stream).updateQualityControl(((MediaFormat) fmts.get(0)).getAdvancedAttributes());
                    }
                }
                if (this.qualityControls != null) {
                    supportedFormats = dev == null ? null : intersectFormats(supportedFormats, getLocallySupportedFormats(dev, this.qualityControls.getRemoteSendMaxPreset(), this.qualityControls.getRemoteReceivePreset()));
                }
            }
            initStream(content.getName(), connector, dev, (MediaFormat) supportedFormats.get(0), target, direction, rtpExtensions, masterStream);
            return;
        }
        closeStream(mediaType);
    }

    public void processOffer(List<ContentPacketExtension> offer) throws OperationFailedException, IllegalArgumentException {
        List<ContentPacketExtension> answer = new ArrayList(offer.size());
        boolean atLeastOneValidDescription = false;
        for (ContentPacketExtension content : offer) {
            this.remoteContentMap.put(content.getName(), content);
            RtpDescriptionPacketExtension description = JingleUtils.getRtpDescription(content);
            MediaType mediaType = MediaType.parseString(description.getMedia());
            List<MediaFormat> remoteFormats = JingleUtils.extractFormats(description, getDynamicPayloadTypes());
            MediaDevice dev = getDefaultDevice(mediaType);
            MediaDirection devDirection = (dev == null ? MediaDirection.INACTIVE : dev.getDirection()).and(getDirectionUserPreference(mediaType));
            MediaDirection direction = devDirection.getDirectionForAnswer(JingleUtils.getDirection(content, ((CallPeerJabberImpl) getPeer()).isInitiator()));
            List<MediaFormat> mutuallySupportedFormats = intersectFormats(remoteFormats, getLocallySupportedFormats(dev));
            List<RTPExtension> rtpExtensions = intersectRTPExtensions(JingleUtils.extractRTPExtensions(description, getRtpExtensionsRegistry()), getExtensionsForType(mediaType));
            IceUdpTransportPacketExtension transport = (IceUdpTransportPacketExtension) content.getFirstChildOfType(IceUdpTransportPacketExtension.class);
            MediaStreamTarget target = null;
            try {
                target = JingleUtils.extractDefaultTarget(content);
            } catch (IllegalArgumentException e) {
                logger.warn("Fail to extract default target", e);
            }
            int targetDataPort = (target != null || transport == null) ? target != null ? target.getDataAddress().getPort() : 0 : -1;
            setTransportManager(transport.getNamespace());
            if (mutuallySupportedFormats.isEmpty() || devDirection == MediaDirection.INACTIVE || targetDataPort == 0) {
                closeStream(mediaType);
            } else {
                ContentPacketExtension ourContent = JingleUtils.createDescription(content.getCreator(), content.getName(), JingleUtils.getSenders(direction, !((CallPeerJabberImpl) getPeer()).isInitiator()), mutuallySupportedFormats, rtpExtensions, getDynamicPayloadTypes(), getRtpExtensionsRegistry());
                setAndAddPreferredEncryptionProtocol(mediaType, ourContent, content);
                if (content.getChildExtensionsOfType(InputEvtPacketExtension.class) != null) {
                    ourContent.addChildExtension(new InputEvtPacketExtension());
                }
                answer.add(ourContent);
                this.localContentMap.put(content.getName(), ourContent);
                atLeastOneValidDescription = true;
            }
        }
        if (!atLeastOneValidDescription) {
            ProtocolProviderServiceJabberImpl.throwOperationFailedException("Offer contained no media formats or no valid media descriptions.", 11, null, logger);
        }
        harvestCandidates(offer, answer, new TransportInfoSender() {
            public void sendTransportInfo(Iterable<ContentPacketExtension> contents) {
                ((CallPeerJabberImpl) CallPeerMediaHandlerJabberImpl.this.getPeer()).sendTransportInfo(contents);
            }
        });
        getTransportManager().startConnectivityEstablishment((Iterable) offer);
    }

    public void processTransportInfo(Iterable<ContentPacketExtension> contents) throws OperationFailedException {
        if (getTransportManager().startConnectivityEstablishment((Iterable) contents)) {
        }
    }

    public void reinitAllContents() throws OperationFailedException, IllegalArgumentException {
        boolean masterStreamSet = false;
        for (String key : this.remoteContentMap.keySet()) {
            ContentPacketExtension ext = (ContentPacketExtension) this.remoteContentMap.get(key);
            boolean masterStream = false;
            if (!masterStreamSet) {
                MediaType mediaType = MediaType.parseString(JingleUtils.getRtpDescription(ext).getMedia());
                if (this.remoteContentMap.size() <= 1) {
                    masterStream = true;
                    masterStreamSet = true;
                } else if (mediaType.equals(MediaType.AUDIO)) {
                    masterStream = true;
                    masterStreamSet = true;
                }
            }
            if (ext != null) {
                processContent(ext, false, masterStream);
            }
        }
    }

    public void reinitContent(String name, ContentPacketExtension content, boolean modify) throws OperationFailedException, IllegalArgumentException {
        ContentPacketExtension ext = (ContentPacketExtension) this.remoteContentMap.get(name);
        if (ext == null) {
            return;
        }
        if (modify) {
            processContent(content, modify, false);
            this.remoteContentMap.put(name, content);
            return;
        }
        ext.setSenders(content.getSenders());
        processContent(ext, modify, false);
        this.remoteContentMap.put(name, ext);
    }

    private void removeContent(Map<String, ContentPacketExtension> contentMap, String name) {
        ContentPacketExtension content = (ContentPacketExtension) contentMap.remove(name);
        if (content != null) {
            String media = JingleUtils.getRtpDescription(content).getMedia();
            if (media != null) {
                closeStream(MediaType.parseString(media));
            }
        }
    }

    public void removeContent(String name) {
        removeContent(this.localContentMap, name);
        removeContent(this.remoteContentMap, name);
        TransportManagerJabberImpl transportManager = queryTransportManager();
        if (transportManager != null) {
            transportManager.removeContent(name);
        }
    }

    public void setRemotelyOnHold(boolean onHold) {
        this.remotelyOnHold = onHold;
        for (MediaType mediaType : MediaType.values()) {
            MediaStream stream = getStream(mediaType);
            if (stream != null) {
                if (((CallPeerJabberImpl) getPeer()).isJitsiVideobridge()) {
                    MediaDirection direction;
                    Channel channel = getColibriChannel(mediaType);
                    if (this.remotelyOnHold) {
                        direction = MediaDirection.INACTIVE;
                    } else {
                        direction = MediaDirection.SENDRECV;
                    }
                    ((CallJabberImpl) ((CallPeerJabberImpl) getPeer()).getCall()).setChannelDirection(channel.getID(), mediaType, direction);
                } else if (this.remotelyOnHold) {
                    stream.setDirection(((CallJabberImpl) ((CallPeerJabberImpl) getPeer()).getCall()).isConferenceFocus() ? MediaDirection.INACTIVE : stream.getDirection().and(MediaDirection.RECVONLY));
                } else {
                    stream.setDirection(calculatePostHoldDirection(stream));
                }
            }
        }
    }

    public void setSupportQualityControls(boolean value) {
        this.supportQualityControls = value;
    }

    private void setTransportManager(String xmlns) throws IllegalArgumentException {
        if (this.transportManager == null || !this.transportManager.getXmlNamespace().equals(xmlns)) {
            CallPeerJabberImpl peer = (CallPeerJabberImpl) getPeer();
            if (((ProtocolProviderServiceJabberImpl) peer.getProtocolProvider()).getDiscoveryManager().includesFeature(xmlns)) {
                if (xmlns.equals("urn:xmpp:jingle:transports:ice-udp:1")) {
                    this.transportManager = new IceUdpTransportManager(peer);
                } else if (xmlns.equals("urn:xmpp:jingle:transports:raw-udp:1")) {
                    this.transportManager = new RawUdpTransportManager(peer);
                } else {
                    throw new IllegalArgumentException("Unsupported Jingle transport " + xmlns);
                }
                synchronized (this.transportManagerSyncRoot) {
                    this.transportManagerSyncRoot.notify();
                }
                return;
            }
            throw new IllegalArgumentException("Unsupported Jingle transport " + xmlns);
        }
    }

    public void start() throws IllegalStateException {
        try {
            wrapupConnectivityEstablishment();
            super.start();
        } catch (OperationFailedException ofe) {
            throw new UndeclaredThrowableException(ofe);
        }
    }

    /* access modifiers changed from: protected */
    public void throwOperationFailedException(String message, int errorCode, Throwable cause) throws OperationFailedException {
        ProtocolProviderServiceJabberImpl.throwOperationFailedException(message, errorCode, cause, logger);
    }

    private void wrapupConnectivityEstablishment() throws OperationFailedException {
        TransportManagerJabberImpl transportManager = getTransportManager();
        transportManager.wrapupConnectivityEstablishment();
        for (MediaType mediaType : MediaType.values()) {
            MediaStream stream = getStream(mediaType);
            if (stream != null) {
                stream.setConnector(transportManager.getStreamConnector(mediaType));
                stream.setTarget(transportManager.getStreamTarget(mediaType));
            }
        }
    }

    private Channel getColibriChannel(MediaType mediaType) {
        if (!((CallPeerJabberImpl) getPeer()).isJitsiVideobridge()) {
            return null;
        }
        TransportManagerJabberImpl transportManager = this.transportManager;
        if (transportManager != null) {
            return transportManager.getColibriChannel(mediaType, false);
        }
        return null;
    }

    public boolean isRemotelyOnHold() {
        return this.remotelyOnHold;
    }

    public void setLocallyOnHold(boolean locallyOnHold) {
        CallPeerJabberImpl peer = (CallPeerJabberImpl) getPeer();
        if (peer.isJitsiVideobridge()) {
            this.locallyOnHold = locallyOnHold;
            if (locallyOnHold || !CallPeerState.ON_HOLD_MUTUALLY.equals(peer.getState())) {
                for (MediaType mediaType : MediaType.values()) {
                    Channel channel = getColibriChannel(mediaType);
                    if (channel != null) {
                        ((CallJabberImpl) peer.getCall()).setChannelDirection(channel.getID(), mediaType, locallyOnHold ? MediaDirection.INACTIVE : MediaDirection.SENDRECV);
                    }
                }
                return;
            }
            return;
        }
        super.setLocallyOnHold(locallyOnHold);
    }

    private boolean addDtlsAdvertisedEncryptions(boolean isInitiator, ContentPacketExtension content, MediaType mediaType) {
        if (((CallPeerJabberImpl) getPeer()).isJitsiVideobridge()) {
            return false;
        }
        return addDtlsAdvertisedEncryptions(isInitiator, (IceUdpTransportPacketExtension) content.getFirstChildOfType(IceUdpTransportPacketExtension.class), mediaType);
    }

    /* access modifiers changed from: 0000 */
    public boolean addDtlsAdvertisedEncryptions(boolean isInitiator, IceUdpTransportPacketExtension remoteTransport, MediaType mediaType) {
        SrtpControls srtpControls = getSrtpControls();
        boolean b = false;
        if (remoteTransport != null) {
            List<DtlsFingerprintPacketExtension> remoteFingerpintPEs = remoteTransport.getChildExtensionsOfType(DtlsFingerprintPacketExtension.class);
            if (!remoteFingerpintPEs.isEmpty()) {
                AccountID accountID = ((ProtocolProviderServiceJabberImpl) ((CallPeerJabberImpl) getPeer()).getProtocolProvider()).getAccountID();
                if (accountID.getAccountPropertyBoolean("DEFAULT_ENCRYPTION", true) && accountID.isEncryptionProtocolEnabled(DtlsControl.PROTO_NAME)) {
                    DtlsControl dtlsControl;
                    Setup setup;
                    Map<String, String> remoteFingerprints = new LinkedHashMap();
                    for (DtlsFingerprintPacketExtension remoteFingerprintPE : remoteFingerpintPEs) {
                        remoteFingerprints.put(remoteFingerprintPE.getHash(), remoteFingerprintPE.getFingerprint());
                    }
                    if (isInitiator) {
                        dtlsControl = (DtlsControl) srtpControls.get(mediaType, SrtpControlType.DTLS_SRTP);
                        setup = Setup.PASSIVE;
                    } else {
                        dtlsControl = (DtlsControl) srtpControls.getOrCreate(mediaType, SrtpControlType.DTLS_SRTP);
                        setup = Setup.ACTIVE;
                    }
                    if (dtlsControl != null) {
                        dtlsControl.setRemoteFingerprints(remoteFingerprints);
                        dtlsControl.setSetup(setup);
                        removeAndCleanupOtherSrtpControls(mediaType, SrtpControlType.DTLS_SRTP);
                        addAdvertisedEncryptionMethod(SrtpControlType.DTLS_SRTP);
                        b = true;
                    }
                }
            }
        }
        if (!b) {
            SrtpControl dtlsControl2 = srtpControls.get(mediaType, SrtpControlType.DTLS_SRTP);
            if (dtlsControl2 != null) {
                srtpControls.remove(mediaType, SrtpControlType.DTLS_SRTP);
                dtlsControl2.cleanup();
            }
        }
        return b;
    }

    private void setAndAddPreferredEncryptionProtocol(MediaType mediaType, ContentPacketExtension localContent, ContentPacketExtension remoteContent) {
        for (String preferredEncryptionProtocol : ((ProtocolProviderServiceJabberImpl) ((CallPeerJabberImpl) getPeer()).getProtocolProvider()).getAccountID().getSortedEnabledEncryptionProtocolList()) {
            String protoName = preferredEncryptionProtocol.substring("ENCRYPTION_PROTOCOL".length() + 1);
            if (DtlsControl.PROTO_NAME.equals(protoName)) {
                addDtlsAdvertisedEncryptions(false, remoteContent, mediaType);
                if (setDtlsEncryptionOnContent(mediaType, localContent, remoteContent)) {
                    return;
                }
            } else {
                if (setAndAddPreferredEncryptionProtocol(protoName, mediaType, localContent == null ? null : JingleUtils.getRtpDescription(localContent), remoteContent == null ? null : JingleUtils.getRtpDescription(remoteContent))) {
                    return;
                }
            }
        }
    }

    private boolean setDtlsEncryptionOnContent(MediaType mediaType, ContentPacketExtension localContent, ContentPacketExtension remoteContent) {
        CallPeerJabberImpl peer = (CallPeerJabberImpl) getPeer();
        boolean b = false;
        if (peer.isJitsiVideobridge()) {
            return setDtlsEncryptionOnTransport(mediaType, localContent, remoteContent);
        }
        DtlsControl dtlsControl;
        ProtocolProviderServiceJabberImpl protocolProvider = (ProtocolProviderServiceJabberImpl) peer.getProtocolProvider();
        AccountID accountID = protocolProvider.getAccountID();
        SrtpControls srtpControls = getSrtpControls();
        if (accountID.getAccountPropertyBoolean("DEFAULT_ENCRYPTION", true) && accountID.isEncryptionProtocolEnabled(DtlsControl.PROTO_NAME)) {
            boolean addFingerprintToLocalTransport;
            if (remoteContent == null) {
                addFingerprintToLocalTransport = protocolProvider.isFeatureSupported(peer.getAddress(), "urn:xmpp:jingle:apps:dtls:0");
            } else {
                addFingerprintToLocalTransport = addDtlsAdvertisedEncryptions(false, remoteContent, mediaType);
            }
            if (addFingerprintToLocalTransport) {
                dtlsControl = (DtlsControl) srtpControls.getOrCreate(mediaType, SrtpControlType.DTLS_SRTP);
                if (dtlsControl != null) {
                    dtlsControl.setSetup(remoteContent == null ? Setup.PASSIVE : Setup.ACTIVE);
                    b = true;
                    setDtlsEncryptionOnTransport(mediaType, localContent, remoteContent);
                }
            }
        }
        if (!b) {
            dtlsControl = srtpControls.get(mediaType, SrtpControlType.DTLS_SRTP);
            if (dtlsControl != null) {
                srtpControls.remove(mediaType, SrtpControlType.DTLS_SRTP);
                dtlsControl.cleanup();
            }
        }
        return b;
    }

    private boolean setDtlsEncryptionOnTransport(MediaType mediaType, ContentPacketExtension localContent, ContentPacketExtension remoteContent) {
        IceUdpTransportPacketExtension localTransport = (IceUdpTransportPacketExtension) localContent.getFirstChildOfType(IceUdpTransportPacketExtension.class);
        boolean b = false;
        if (localTransport == null) {
            return 0;
        }
        CallPeerJabberImpl peer = (CallPeerJabberImpl) getPeer();
        if (peer.isJitsiVideobridge()) {
            ProtocolProviderServiceJabberImpl protocolProvider = (ProtocolProviderServiceJabberImpl) peer.getProtocolProvider();
            AccountID accountID = protocolProvider.getAccountID();
            if (accountID.getAccountPropertyBoolean("DEFAULT_ENCRYPTION", true) && accountID.isEncryptionProtocolEnabled(DtlsControl.PROTO_NAME)) {
                IceUdpTransportPacketExtension transport;
                Channel channel = getColibriChannel(mediaType);
                List<DtlsFingerprintPacketExtension> localFingerprints = null;
                if (channel != null) {
                    transport = channel.getTransport();
                    if (transport != null) {
                        localFingerprints = transport.getChildExtensionsOfType(DtlsFingerprintPacketExtension.class);
                    }
                }
                if (!(localFingerprints == null || localFingerprints.isEmpty())) {
                    if (remoteContent != null) {
                        transport = (IceUdpTransportPacketExtension) remoteContent.getFirstChildOfType(IceUdpTransportPacketExtension.class);
                        if (transport == null) {
                            localFingerprints = null;
                        } else if (transport.getChildExtensionsOfType(DtlsFingerprintPacketExtension.class).isEmpty()) {
                            localFingerprints = null;
                        }
                    } else if (!protocolProvider.isFeatureSupported(peer.getAddress(), "urn:xmpp:jingle:apps:dtls:0")) {
                        localFingerprints = null;
                    }
                    if (localFingerprints != null) {
                        if (localTransport.getChildExtensionsOfType(DtlsFingerprintPacketExtension.class).isEmpty()) {
                            for (DtlsFingerprintPacketExtension localFingerprint : localFingerprints) {
                                DtlsFingerprintPacketExtension fingerprintPE = new DtlsFingerprintPacketExtension();
                                fingerprintPE.setFingerprint(localFingerprint.getFingerprint());
                                fingerprintPE.setHash(localFingerprint.getHash());
                                localTransport.addChildExtension(fingerprintPE);
                            }
                        }
                        b = true;
                    }
                }
            }
        } else {
            DtlsControl dtlsControl = (DtlsControl) getSrtpControls().get(mediaType, SrtpControlType.DTLS_SRTP);
            if (dtlsControl != null) {
                CallJabberImpl.setDtlsEncryptionOnTransport(dtlsControl, localTransport);
                b = true;
            }
        }
        return b;
    }

    private void setDtlsEncryptionOnTransports(List<ContentPacketExtension> remoteContents, List<ContentPacketExtension> localContents) {
        for (ContentPacketExtension localContent : localContents) {
            if (JingleUtils.getRtpDescription(localContent) != null) {
                MediaType mediaType = JingleUtils.getMediaType(localContent);
                if (mediaType != null) {
                    ContentPacketExtension remoteContent;
                    if (remoteContents == null) {
                        remoteContent = null;
                    } else {
                        remoteContent = TransportManagerJabberImpl.findContentByName(remoteContents, localContent.getName());
                    }
                    setDtlsEncryptionOnTransport(mediaType, localContent, remoteContent);
                }
            }
        }
    }

    public void setSupportedTransports(Collection<String> transports) {
        if (transports != null) {
            String ice = "urn:xmpp:jingle:transports:ice-udp:1";
            String rawUdp = "urn:xmpp:jingle:transports:raw-udp:1";
            int size = 0;
            for (String transport : transports) {
                if (ice.equals(transport) || rawUdp.equals(transport)) {
                    size++;
                }
            }
            if (size > 0) {
                synchronized (this.supportedTransportsSyncRoot) {
                    this.supportedTransports = new String[size];
                    int i = 0;
                    if (transports.contains(ice)) {
                        this.supportedTransports[0] = ice;
                        i = 0 + 1;
                    }
                    if (transports.contains(rawUdp)) {
                        this.supportedTransports[i] = rawUdp;
                        i++;
                    }
                }
            }
        }
    }
}
