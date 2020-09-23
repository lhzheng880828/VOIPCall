package net.java.sip.communicator.impl.protocol.jabber;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriConferenceIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriConferenceIQ.Channel;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriConferenceIQ.Content;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriStreamConnector;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CoinPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.DtlsFingerprintPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.IceUdpTransportPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JinglePacketFactory;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.PayloadTypePacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.Reason;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RtpDescriptionPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.TransferPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.jinglesdp.JingleUtils;
import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.CallPeerState;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetBasicAutoAnswer;
import net.java.sip.communicator.service.protocol.OperationSetBasicTelephony;
import net.java.sip.communicator.service.protocol.media.MediaHandler;
import net.java.sip.communicator.util.Logger;
import org.jitsi.service.neomedia.DtlsControl;
import org.jitsi.service.neomedia.DtlsControl.Setup;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.SrtpControlType;
import org.jitsi.service.neomedia.StreamConnector;
import org.jitsi.service.neomedia.StreamConnectorFactory;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.packet.DiscoverInfo;

public class CallJabberImpl extends AbstractCallJabberGTalkImpl<CallPeerJabberImpl> {
    private static final Logger logger = Logger.getLogger(CallJabberImpl.class);
    private ColibriConferenceIQ colibri;
    private MediaHandler colibriMediaHandler;
    private final List<WeakReference<ColibriStreamConnector>> colibriStreamConnectors;
    private String jitsiVideobridge;

    protected CallJabberImpl(OperationSetBasicTelephonyJabberImpl parentOpSet) {
        super(parentOpSet);
        int mediaTypeValueCount = MediaType.values().length;
        this.colibriStreamConnectors = new ArrayList(mediaTypeValueCount);
        for (int i = 0; i < mediaTypeValueCount; i++) {
            this.colibriStreamConnectors.add(null);
        }
        parentOpSet.getActiveCallsRepository().addCall(this);
    }

    public void closeColibriStreamConnector(CallPeerJabberImpl peer, MediaType mediaType, ColibriStreamConnector colibriStreamConnector) {
        colibriStreamConnector.close();
        synchronized (this.colibriStreamConnectors) {
            int index = mediaType.ordinal();
            WeakReference<ColibriStreamConnector> weakReference = (WeakReference) this.colibriStreamConnectors.get(index);
            if (weakReference != null && colibriStreamConnector.equals(weakReference.get())) {
                this.colibriStreamConnectors.set(index, null);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void conferenceFocusChanged(boolean oldValue, boolean newValue) {
        try {
            Iterator<CallPeerJabberImpl> peers = getCallPeers();
            while (peers.hasNext()) {
                CallPeerJabberImpl callPeer = (CallPeerJabberImpl) peers.next();
                if (callPeer.getState() == CallPeerState.CONNECTED) {
                    callPeer.sendCoinSessionInfo();
                }
            }
        } finally {
            super.conferenceFocusChanged(oldValue, newValue);
        }
    }

    public ColibriConferenceIQ createColibriChannels(CallPeerJabberImpl peer, Map<ContentPacketExtension, ContentPacketExtension> contentMap) throws OperationFailedException {
        if (!getConference().isJitsiVideobridge()) {
            return null;
        }
        MediaType mediaType;
        String jitsiVideobridge;
        CallPeerMediaHandlerJabberImpl peerMediaHandler = (CallPeerMediaHandlerJabberImpl) peer.getMediaHandler();
        if (peerMediaHandler.getMediaHandler() != this.colibriMediaHandler) {
            for (MediaType mediaType2 : MediaType.values()) {
                if (peerMediaHandler.getStream(mediaType2) != null) {
                    return null;
                }
            }
        }
        ProtocolProviderServiceJabberImpl protocolProvider = (ProtocolProviderServiceJabberImpl) getProtocolProvider();
        if (this.colibri == null) {
            jitsiVideobridge = getJitsiVideobridge();
        } else {
            jitsiVideobridge = this.colibri.getFrom();
        }
        if (jitsiVideobridge == null || jitsiVideobridge.length() == 0) {
            logger.error("Failed to allocate colibri channels: no videobridge found.");
            return null;
        }
        ContentPacketExtension localContent;
        ContentPacketExtension remoteContent;
        ContentPacketExtension cpe;
        String contentName;
        Content content;
        Content content2;
        if (this.colibriMediaHandler == null) {
            this.colibriMediaHandler = new MediaHandler();
        }
        peerMediaHandler.setMediaHandler(this.colibriMediaHandler);
        Packet conferenceRequest = new ColibriConferenceIQ();
        if (this.colibri != null) {
            conferenceRequest.setID(this.colibri.getID());
        }
        for (Entry<ContentPacketExtension, ContentPacketExtension> e : contentMap.entrySet()) {
            localContent = (ContentPacketExtension) e.getKey();
            remoteContent = (ContentPacketExtension) e.getValue();
            if (remoteContent == null) {
                cpe = localContent;
            } else {
                cpe = remoteContent;
            }
            RtpDescriptionPacketExtension rdpe = (RtpDescriptionPacketExtension) cpe.getFirstChildOfType(RtpDescriptionPacketExtension.class);
            String media = rdpe.getMedia();
            mediaType2 = MediaType.parseString(media);
            contentName = mediaType2.toString();
            content = new Content(contentName);
            conferenceRequest.addContent(content);
            boolean requestLocalChannel = true;
            if (this.colibri != null) {
                content2 = this.colibri.getContent(contentName);
                if (content2 != null && content2.getChannelCount() > 0) {
                    requestLocalChannel = false;
                }
            }
            boolean peerIsInitiator = peer.isInitiator();
            if (requestLocalChannel) {
                Channel localChannelRequest = new Channel();
                localChannelRequest.setEndpoint(protocolProvider.getOurJID());
                localChannelRequest.setInitiator(Boolean.valueOf(peerIsInitiator));
                for (PayloadTypePacketExtension ptpe : rdpe.getPayloadTypes()) {
                    localChannelRequest.addPayloadType(ptpe);
                }
                setTransportOnChannel(peer, media, localChannelRequest);
                setDtlsEncryptionOnChannel(jitsiVideobridge, peer, mediaType2, localChannelRequest);
                ensureTransportOnChannel(localChannelRequest, peer);
                content.addChannel(localChannelRequest);
            }
            Channel remoteChannelRequest = new Channel();
            remoteChannelRequest.setEndpoint(peer.getAddress());
            remoteChannelRequest.setInitiator(Boolean.valueOf(!peerIsInitiator));
            for (PayloadTypePacketExtension ptpe2 : rdpe.getPayloadTypes()) {
                remoteChannelRequest.addPayloadType(ptpe2);
            }
            setTransportOnChannel(media, localContent, remoteContent, peer, remoteChannelRequest);
            setDtlsEncryptionOnChannel(mediaType2, localContent, remoteContent, peer, remoteChannelRequest);
            ensureTransportOnChannel(remoteChannelRequest, peer);
            content.addChannel(remoteChannelRequest);
        }
        XMPPConnection connection = protocolProvider.getConnection();
        PacketCollector packetCollector = connection.createPacketCollector(new PacketIDFilter(conferenceRequest.getPacketID()));
        conferenceRequest.setTo(jitsiVideobridge);
        conferenceRequest.setType(Type.GET);
        connection.sendPacket(conferenceRequest);
        Packet response = packetCollector.nextResult((long) SmackConfiguration.getPacketReplyTimeout());
        packetCollector.cancel();
        if (response == null) {
            logger.error("Failed to allocate colibri channels: response is null. Maybe the response timed out.");
            return null;
        } else if (response.getError() != null) {
            logger.error("Failed to allocate colibri channels: " + response.getError());
            return null;
        } else if (response instanceof ColibriConferenceIQ) {
            Content contentResponse;
            ColibriConferenceIQ conferenceResponse = (ColibriConferenceIQ) response;
            String conferenceResponseID = conferenceResponse.getID();
            if (this.colibri == null) {
                this.colibri = new ColibriConferenceIQ();
                this.colibri.setFrom(conferenceResponse.getFrom());
            }
            String colibriID = this.colibri.getID();
            if (colibriID == null) {
                this.colibri.setID(conferenceResponseID);
            } else if (!colibriID.equals(conferenceResponseID)) {
                throw new IllegalStateException("conference.id");
            }
            for (Content contentResponse2 : conferenceResponse.getContents()) {
                contentName = contentResponse2.getName();
                content2 = this.colibri.getOrCreateContent(contentName);
                for (Channel channelResponse : contentResponse2.getChannels()) {
                    int channelIndex = content2.getChannelCount();
                    content2.addChannel(channelResponse);
                    if (channelIndex == 0) {
                        TransportManagerJabberImpl transportManager = peerMediaHandler.getTransportManager();
                        transportManager.isEstablishingConnectivityWithJitsiVideobridge = true;
                        transportManager.startConnectivityEstablishmentWithJitsiVideobridge = true;
                        addDtlsAdvertisedEncryptions(peer, channelResponse, MediaType.parseString(contentName));
                    }
                }
            }
            ColibriConferenceIQ conferenceResult = new ColibriConferenceIQ();
            conferenceResult.setFrom(this.colibri.getFrom());
            conferenceResult.setID(conferenceResponseID);
            for (Entry<ContentPacketExtension, ContentPacketExtension> e2 : contentMap.entrySet()) {
                localContent = (ContentPacketExtension) e2.getKey();
                remoteContent = (ContentPacketExtension) e2.getValue();
                if (remoteContent == null) {
                    cpe = localContent;
                } else {
                    cpe = remoteContent;
                }
                contentResponse2 = conferenceResponse.getContent(MediaType.parseString(((RtpDescriptionPacketExtension) cpe.getFirstChildOfType(RtpDescriptionPacketExtension.class)).getMedia()).toString());
                if (contentResponse2 != null) {
                    contentName = contentResponse2.getName();
                    content = new Content(contentName);
                    conferenceResult.addContent(content);
                    content2 = this.colibri.getContent(contentName);
                    Channel localChannel = null;
                    if (content2 != null && content2.getChannelCount() > 0) {
                        localChannel = content2.getChannel(0);
                        content.addChannel(localChannel);
                    }
                    String localChannelID = localChannel == null ? null : localChannel.getID();
                    for (Channel channelResponse2 : contentResponse2.getChannels()) {
                        if (localChannelID != null) {
                            if (localChannelID.equals(channelResponse2.getID())) {
                            }
                        }
                        content.addChannel(channelResponse2);
                    }
                }
            }
            return conferenceResult;
        } else {
            logger.error("Failed to allocate colibri channels: response is not a colibri conference");
            return null;
        }
    }

    public ColibriStreamConnector createColibriStreamConnector(CallPeerJabberImpl peer, MediaType mediaType, Channel channel, StreamConnectorFactory factory) {
        String channelID = channel.getID();
        if (channelID == null) {
            throw new IllegalArgumentException("channel");
        } else if (this.colibri == null) {
            throw new IllegalStateException("colibri");
        } else {
            Content content = this.colibri.getContent(mediaType.toString());
            if (content == null) {
                throw new IllegalArgumentException("mediaType");
            } else if (content.getChannelCount() < 1 || !channelID.equals(content.getChannel(0).getID())) {
                throw new IllegalArgumentException("channel");
            } else {
                ColibriStreamConnector colibriStreamConnector;
                synchronized (this.colibriStreamConnectors) {
                    int index = mediaType.ordinal();
                    WeakReference<ColibriStreamConnector> weakReference = (WeakReference) this.colibriStreamConnectors.get(index);
                    if (weakReference == null) {
                        colibriStreamConnector = null;
                    } else {
                        colibriStreamConnector = (ColibriStreamConnector) weakReference.get();
                    }
                    if (colibriStreamConnector == null) {
                        StreamConnector streamConnector = factory.createStreamConnector();
                        if (streamConnector != null) {
                            colibriStreamConnector = new ColibriStreamConnector(streamConnector);
                            this.colibriStreamConnectors.set(index, new WeakReference(colibriStreamConnector));
                        }
                    }
                }
                return colibriStreamConnector;
            }
        }
    }

    public void expireColibriChannels(CallPeerJabberImpl peer, ColibriConferenceIQ conference) {
        if (this.colibri != null) {
            String conferenceID = this.colibri.getID();
            if (conferenceID.equals(conference.getID())) {
                Content colibriContent;
                Content contentRequest;
                Channel colibriChannel;
                Channel channelRequest;
                ColibriConferenceIQ conferenceRequest = new ColibriConferenceIQ();
                conferenceRequest.setID(conferenceID);
                for (Content content : conference.getContents()) {
                    colibriContent = this.colibri.getContent(content.getName());
                    if (colibriContent != null) {
                        contentRequest = conferenceRequest.getOrCreateContent(colibriContent.getName());
                        for (Channel channel : content.getChannels()) {
                            colibriChannel = colibriContent.getChannel(channel.getID());
                            if (colibriChannel != null) {
                                channelRequest = new Channel();
                                channelRequest.setExpire(0);
                                channelRequest.setID(colibriChannel.getID());
                                contentRequest.addChannel(channelRequest);
                            }
                        }
                    }
                }
                for (Content contentRequest2 : conferenceRequest.getContents()) {
                    colibriContent = this.colibri.getContent(contentRequest2.getName());
                    for (Channel channelRequest2 : contentRequest2.getChannels()) {
                        colibriContent.removeChannel(colibriContent.getChannel(channelRequest2.getID()));
                        if (colibriContent.getChannelCount() == 1) {
                            colibriChannel = colibriContent.getChannel(0);
                            channelRequest2 = new Channel();
                            channelRequest2.setExpire(0);
                            channelRequest2.setID(colibriChannel.getID());
                            contentRequest2.addChannel(channelRequest2);
                            colibriContent.removeChannel(colibriChannel);
                            break;
                        }
                    }
                }
                conferenceRequest.setTo(this.colibri.getFrom());
                conferenceRequest.setType(Type.SET);
                ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getConnection().sendPacket(conferenceRequest);
            }
        }
    }

    public void setChannelDirection(String channelID, MediaType mediaType, MediaDirection direction) {
        if (this.colibri != null && channelID != null) {
            Content content = this.colibri.getContent(mediaType.toString());
            if (content != null && content.getChannel(channelID) != null) {
                Channel requestChannel = new Channel();
                requestChannel.setID(channelID);
                requestChannel.setDirection(direction);
                Content requestContent = new Content();
                requestContent.setName(mediaType.toString());
                requestContent.addChannel(requestChannel);
                ColibriConferenceIQ conferenceRequest = new ColibriConferenceIQ();
                conferenceRequest.setID(this.colibri.getID());
                conferenceRequest.setTo(this.colibri.getFrom());
                conferenceRequest.setType(Type.SET);
                conferenceRequest.addContent(requestContent);
                ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getConnection().sendPacket(conferenceRequest);
            }
        }
    }

    public CallPeerJabberImpl initiateSession(String calleeJID, DiscoverInfo discoverInfo, Iterable<PacketExtension> sessionInitiateExtensions, Collection<String> supportedTransports) throws OperationFailedException {
        CallPeerJabberImpl callPeer = new CallPeerJabberImpl(calleeJID, this);
        callPeer.setDiscoveryInfo(discoverInfo);
        addCallPeer(callPeer);
        callPeer.setState(CallPeerState.INITIATING_CALL);
        if (getCallPeerCount() == 1) {
            ((OperationSetBasicTelephonyJabberImpl) this.parentOpSet).fireCallEvent(1, this);
        }
        CallPeerMediaHandlerJabberImpl mediaHandler = (CallPeerMediaHandlerJabberImpl) callPeer.getMediaHandler();
        mediaHandler.setSupportedTransports(supportedTransports);
        mediaHandler.setLocalVideoTransmissionEnabled(this.localVideoAllowed);
        mediaHandler.setLocalInputEvtAware(getLocalInputEvtAware());
        callPeer.setState(CallPeerState.CONNECTING);
        boolean sessionInitiated = false;
        try {
            callPeer.initiateSession(sessionInitiateExtensions);
            sessionInitiated = true;
            return callPeer;
        } finally {
            if (!sessionInitiated) {
                callPeer.setState(CallPeerState.FAILED);
            }
        }
    }

    public void modifyVideoContent() throws OperationFailedException {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating video content for " + this);
        }
        boolean change = false;
        for (CallPeerJabberImpl peer : getCallPeerList()) {
            change |= peer.sendModifyVideoContent();
        }
        if (change) {
            fireCallChangeEvent("CallParticipantsChanged", null, null);
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean processColibriConferenceIQ(ColibriConferenceIQ conferenceIQ) {
        if (this.colibri == null || !conferenceIQ.getID().equals(this.colibri.getID())) {
            return false;
        }
        for (MediaType mediaType : MediaType.values()) {
            String contentName = mediaType.toString();
            Content content = conferenceIQ.getContent(contentName);
            if (content != null) {
                Content thisContent = this.colibri.getContent(contentName);
                if (thisContent != null && thisContent.getChannelCount() > 0) {
                    Channel channel = content.getChannel(thisContent.getChannel(0).getID());
                    if (channel != null) {
                        content.removeChannel(channel);
                    }
                }
            }
        }
        for (CallPeerJabberImpl callPeer : getCallPeerList()) {
            callPeer.processColibriConferenceIQ(conferenceIQ);
        }
        return true;
    }

    public CallPeerJabberImpl processSessionInitiate(JingleIQ jingleIQ) {
        String remoteParty = jingleIQ.getInitiator();
        boolean autoAnswer = false;
        CallPeerJabberImpl attendant = null;
        OperationSetBasicTelephonyJabberImpl basicTelephony = null;
        if (remoteParty == null) {
            remoteParty = jingleIQ.getFrom();
        }
        CallPeerJabberImpl callPeer = new CallPeerJabberImpl(remoteParty, this, jingleIQ);
        addCallPeer(callPeer);
        try {
            TransferPacketExtension transfer = (TransferPacketExtension) jingleIQ.getExtension("transfer", "urn:xmpp:jingle:transfer:0");
            if (transfer != null) {
                String sid = transfer.getSID();
                if (sid != null) {
                    ProtocolProviderServiceJabberImpl protocolProvider = (ProtocolProviderServiceJabberImpl) getProtocolProvider();
                    basicTelephony = (OperationSetBasicTelephonyJabberImpl) protocolProvider.getOperationSet(OperationSetBasicTelephony.class);
                    CallJabberImpl attendantCall = (CallJabberImpl) basicTelephony.getActiveCallsRepository().findSID(sid);
                    if (attendantCall != null) {
                        attendant = (CallPeerJabberImpl) attendantCall.getPeer(sid);
                        if (attendant != null && basicTelephony.getFullCalleeURI(attendant.getAddress()).equals(transfer.getFrom()) && protocolProvider.getOurJID().equals(transfer.getTo())) {
                            autoAnswer = true;
                        }
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("Failed to hang up on attendant as part of session transfer", t);
            if (t instanceof ThreadDeath) {
                ThreadDeath t2 = (ThreadDeath) t;
            }
        }
        CoinPacketExtension coin = (CoinPacketExtension) jingleIQ.getExtension("conference-info", "");
        if (coin != null) {
            callPeer.setConferenceFocus(Boolean.parseBoolean((String) coin.getAttribute(CoinPacketExtension.ISFOCUS_ATTR_NAME)));
        }
        callPeer.processSessionInitiate(jingleIQ);
        if (((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getAccountID().getAccountPropertyBoolean("MODE_PARANOIA", false) && ((CallPeerMediaHandlerJabberImpl) callPeer.getMediaHandler()).getAdvertisedEncryptionMethods().length == 0) {
            String reasonText = JabberActivator.getResources().getI18NString("service.gui.security.encryption.required");
            Packet errResp = JinglePacketFactory.createSessionTerminate(jingleIQ.getTo(), jingleIQ.getFrom(), jingleIQ.getSID(), Reason.SECURITY_ERROR, reasonText);
            callPeer.setState(CallPeerState.FAILED, reasonText);
            ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getConnection().sendPacket(errResp);
            return null;
        }
        if (callPeer.getState() == CallPeerState.FAILED) {
            return null;
        }
        callPeer.setState(CallPeerState.INCOMING_CALL);
        if (autoAnswer) {
            try {
                callPeer.answer();
            } catch (Exception e) {
                logger.info("Exception occurred while answer transferred call", e);
                callPeer = null;
            }
            try {
                basicTelephony.hangupCallPeer(attendant);
            } catch (OperationFailedException e2) {
                logger.error("Failed to hang up on attendant as part of session transfer", e2);
            }
            return callPeer;
        }
        List<ContentPacketExtension> offer = callPeer.getSessionIQ().getContentList();
        Map<MediaType, MediaDirection> directions = new HashMap();
        directions.put(MediaType.AUDIO, MediaDirection.INACTIVE);
        directions.put(MediaType.VIDEO, MediaDirection.INACTIVE);
        for (ContentPacketExtension c : offer) {
            String contentName = c.getName();
            MediaDirection remoteDirection = JingleUtils.getDirection(c, callPeer.isInitiator());
            if (MediaType.AUDIO.toString().equals(contentName)) {
                directions.put(MediaType.AUDIO, remoteDirection);
            } else if (MediaType.VIDEO.toString().equals(contentName)) {
                directions.put(MediaType.VIDEO, remoteDirection);
            }
        }
        if (getCallPeerCount() == 1) {
            ((OperationSetBasicTelephonyJabberImpl) this.parentOpSet).fireCallEvent(2, this, directions);
        }
        OperationSetAutoAnswerJabberImpl autoAnswerOpSet = (OperationSetAutoAnswerJabberImpl) ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getOperationSet(OperationSetBasicAutoAnswer.class);
        if (autoAnswerOpSet != null) {
            autoAnswerOpSet.autoAnswer(this, directions);
        }
        return callPeer;
    }

    private boolean addDtlsAdvertisedEncryptions(CallPeerJabberImpl peer, Channel channel, MediaType mediaType) {
        CallPeerMediaHandlerJabberImpl peerMediaHandler = (CallPeerMediaHandlerJabberImpl) peer.getMediaHandler();
        DtlsControl dtlsControl = (DtlsControl) peerMediaHandler.getSrtpControls().get(mediaType, SrtpControlType.DTLS_SRTP);
        if (dtlsControl != null) {
            dtlsControl.setSetup(peer.isInitiator() ? Setup.ACTIVE : Setup.PASSIVE);
        }
        return peerMediaHandler.addDtlsAdvertisedEncryptions(true, channel.getTransport(), mediaType);
    }

    private void setDtlsEncryptionOnChannel(MediaType mediaType, ContentPacketExtension localContent, ContentPacketExtension remoteContent, CallPeerJabberImpl peer, Channel channel) {
        AccountID accountID = ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getAccountID();
        if (accountID.getAccountPropertyBoolean("DEFAULT_ENCRYPTION", true) && accountID.isEncryptionProtocolEnabled(DtlsControl.PROTO_NAME) && remoteContent != null) {
            IceUdpTransportPacketExtension remoteTransport = (IceUdpTransportPacketExtension) remoteContent.getFirstChildOfType(IceUdpTransportPacketExtension.class);
            if (remoteTransport != null) {
                List<DtlsFingerprintPacketExtension> remoteFingerprints = remoteTransport.getChildExtensionsOfType(DtlsFingerprintPacketExtension.class);
                if (!remoteFingerprints.isEmpty()) {
                    IceUdpTransportPacketExtension localTransport = ensureTransportOnChannel(channel, peer);
                    if (localTransport != null && localTransport.getChildExtensionsOfType(DtlsFingerprintPacketExtension.class).isEmpty()) {
                        for (DtlsFingerprintPacketExtension remoteFingerprint : remoteFingerprints) {
                            DtlsFingerprintPacketExtension localFingerprint = new DtlsFingerprintPacketExtension();
                            localFingerprint.setFingerprint(remoteFingerprint.getFingerprint());
                            localFingerprint.setHash(remoteFingerprint.getHash());
                            localTransport.addChildExtension(localFingerprint);
                        }
                    }
                }
            }
        }
    }

    private void setDtlsEncryptionOnChannel(String jitsiVideobridge, CallPeerJabberImpl peer, MediaType mediaType, Channel channel) {
        ProtocolProviderServiceJabberImpl protocolProvider = (ProtocolProviderServiceJabberImpl) getProtocolProvider();
        AccountID accountID = protocolProvider.getAccountID();
        if (accountID.getAccountPropertyBoolean("DEFAULT_ENCRYPTION", true) && accountID.isEncryptionProtocolEnabled(DtlsControl.PROTO_NAME) && protocolProvider.isFeatureSupported(jitsiVideobridge, "urn:xmpp:jingle:apps:dtls:0")) {
            DtlsControl dtlsControl = (DtlsControl) ((CallPeerMediaHandlerJabberImpl) peer.getMediaHandler()).getSrtpControls().getOrCreate(mediaType, SrtpControlType.DTLS_SRTP);
            if (dtlsControl != null) {
                IceUdpTransportPacketExtension transport = ensureTransportOnChannel(channel, peer);
                if (transport != null) {
                    setDtlsEncryptionOnTransport(dtlsControl, transport);
                }
            }
        }
    }

    static void setDtlsEncryptionOnTransport(DtlsControl dtlsControl, IceUdpTransportPacketExtension localTransport) {
        String fingerprint = dtlsControl.getLocalFingerprint();
        String hash = dtlsControl.getLocalFingerprintHashFunction();
        DtlsFingerprintPacketExtension fingerprintPE = (DtlsFingerprintPacketExtension) localTransport.getFirstChildOfType(DtlsFingerprintPacketExtension.class);
        if (fingerprintPE == null) {
            fingerprintPE = new DtlsFingerprintPacketExtension();
            localTransport.addChildExtension(fingerprintPE);
        }
        fingerprintPE.setFingerprint(fingerprint);
        fingerprintPE.setHash(hash);
    }

    private void setTransportOnChannel(CallPeerJabberImpl peer, String media, Channel channel) throws OperationFailedException {
        PacketExtension transport = ((CallPeerMediaHandlerJabberImpl) peer.getMediaHandler()).getTransportManager().createTransport(media);
        if (transport instanceof IceUdpTransportPacketExtension) {
            channel.setTransport((IceUdpTransportPacketExtension) transport);
        }
    }

    private void setTransportOnChannel(String media, ContentPacketExtension localContent, ContentPacketExtension remoteContent, CallPeerJabberImpl peer, Channel channel) throws OperationFailedException {
        if (remoteContent != null) {
            channel.setTransport(TransportManagerJabberImpl.cloneTransportAndCandidates((IceUdpTransportPacketExtension) remoteContent.getFirstChildOfType(IceUdpTransportPacketExtension.class)));
        }
    }

    private IceUdpTransportPacketExtension ensureTransportOnChannel(Channel channel, CallPeerJabberImpl peer) {
        IceUdpTransportPacketExtension transport = channel.getTransport();
        if (transport != null) {
            return transport;
        }
        PacketExtension pe = ((CallPeerMediaHandlerJabberImpl) peer.getMediaHandler()).getTransportManager().createTransportPacketExtension();
        if (!(pe instanceof IceUdpTransportPacketExtension)) {
            return transport;
        }
        transport = (IceUdpTransportPacketExtension) pe;
        channel.setTransport(transport);
        return transport;
    }

    public String getJitsiVideobridge() {
        if (this.jitsiVideobridge == null && getConference().isJitsiVideobridge()) {
            String jitsiVideobridge = ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getJitsiVideobridge();
            if (jitsiVideobridge != null) {
                this.jitsiVideobridge = jitsiVideobridge;
            }
        }
        return this.jitsiVideobridge;
    }
}
