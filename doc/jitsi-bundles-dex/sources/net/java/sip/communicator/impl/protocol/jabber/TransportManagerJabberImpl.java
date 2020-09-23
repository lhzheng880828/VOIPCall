package net.java.sip.communicator.impl.protocol.jabber;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriConferenceIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriConferenceIQ.Channel;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriConferenceIQ.Content;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriStreamConnector;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CandidatePacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.IceUdpTransportPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RemoteCandidatePacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RtpDescriptionPacketExtension;
import net.java.sip.communicator.service.protocol.Call;
import net.java.sip.communicator.service.protocol.CallConference;
import net.java.sip.communicator.service.protocol.CallPeer;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.media.TransportManager;
import net.java.sip.communicator.util.Logger;
import org.jitsi.service.neomedia.MediaStreamTarget;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.StreamConnector;
import org.jitsi.service.neomedia.StreamConnectorFactory;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.PacketExtension;

public abstract class TransportManagerJabberImpl extends TransportManager<CallPeerJabberImpl> {
    private static final Logger logger = Logger.getLogger(TransportManagerJabberImpl.class);
    private static int nextID = 1;
    private ColibriConferenceIQ colibri;
    private int currentGeneration = 0;
    boolean isEstablishingConnectivityWithJitsiVideobridge = false;
    boolean startConnectivityEstablishmentWithJitsiVideobridge = false;

    public abstract PacketExtension createTransport(String str) throws OperationFailedException;

    public abstract PacketExtension createTransportPacketExtension();

    public abstract MediaStreamTarget getStreamTarget(MediaType mediaType);

    public abstract String getXmlNamespace();

    public abstract void removeContent(String str);

    public abstract PacketExtension startCandidateHarvest(ContentPacketExtension contentPacketExtension, ContentPacketExtension contentPacketExtension2, TransportInfoSender transportInfoSender, String str) throws OperationFailedException;

    public abstract List<ContentPacketExtension> wrapupCandidateHarvest();

    protected TransportManagerJabberImpl(CallPeerJabberImpl callPeer) {
        super(callPeer);
    }

    /* access modifiers changed from: protected */
    public InetAddress getIntendedDestination(CallPeerJabberImpl peer) {
        return ((ProtocolProviderServiceJabberImpl) peer.getProtocolProvider()).getNextHop();
    }

    /* access modifiers changed from: protected */
    public String getNextID() {
        int nextID;
        synchronized (TransportManagerJabberImpl.class) {
            nextID = nextID;
            nextID = nextID + 1;
        }
        return Integer.toString(nextID);
    }

    /* access modifiers changed from: protected */
    public int getCurrentGeneration() {
        return this.currentGeneration;
    }

    /* access modifiers changed from: protected */
    public void incrementGeneration() {
        this.currentGeneration++;
    }

    /* access modifiers changed from: protected */
    public void sendTransportInfoToJitsiVideobridge(Map<String, IceUdpTransportPacketExtension> map) {
        boolean initiator;
        CallPeerJabberImpl peer = (CallPeerJabberImpl) getCallPeer();
        if (peer.isInitiator()) {
            initiator = false;
        } else {
            initiator = true;
        }
        ColibriConferenceIQ conferenceRequest = null;
        for (Entry<String, IceUdpTransportPacketExtension> e : map.entrySet()) {
            String media = (String) e.getKey();
            Channel channel = getColibriChannel(MediaType.parseString(media), false);
            if (channel != null) {
                IceUdpTransportPacketExtension transport;
                try {
                    transport = cloneTransportAndCandidates((IceUdpTransportPacketExtension) e.getValue());
                } catch (OperationFailedException e2) {
                    transport = null;
                }
                if (transport != null) {
                    Channel channelRequest = new Channel();
                    channelRequest.setID(channel.getID());
                    channelRequest.setInitiator(Boolean.valueOf(initiator));
                    channelRequest.setTransport(transport);
                    if (conferenceRequest == null) {
                        if (this.colibri != null) {
                            String id = this.colibri.getID();
                            if (id == null || id.length() == 0) {
                                break;
                            }
                            conferenceRequest = new ColibriConferenceIQ();
                            conferenceRequest.setID(id);
                            conferenceRequest.setTo(this.colibri.getFrom());
                            conferenceRequest.setType(Type.SET);
                        } else {
                            break;
                        }
                    }
                    conferenceRequest.getOrCreateContent(media).addChannel(channelRequest);
                } else {
                    continue;
                }
            }
        }
        if (conferenceRequest != null) {
            ((ProtocolProviderServiceJabberImpl) peer.getProtocolProvider()).getConnection().sendPacket(conferenceRequest);
        }
    }

    public void startCandidateHarvest(List<ContentPacketExtension> theirOffer, List<ContentPacketExtension> ourAnswer, TransportInfoSender transportInfoSender) throws OperationFailedException {
        List<ContentPacketExtension> cpes;
        ContentPacketExtension cpe;
        CallPeerJabberImpl peer = (CallPeerJabberImpl) getCallPeer();
        CallJabberImpl call = (CallJabberImpl) peer.getCall();
        boolean isJitsiVideobridge = call.getConference().isJitsiVideobridge();
        if (theirOffer == null) {
            cpes = ourAnswer;
        } else {
            cpes = theirOffer;
        }
        if (isJitsiVideobridge) {
            Map<ContentPacketExtension, ContentPacketExtension> contentMap = new LinkedHashMap();
            for (ContentPacketExtension cpe2 : cpes) {
                MediaType mediaType = MediaType.parseString(((RtpDescriptionPacketExtension) cpe2.getFirstChildOfType(RtpDescriptionPacketExtension.class)).getMedia());
                if (this.colibri == null || this.colibri.getContent(mediaType.toString()) == null) {
                    ContentPacketExtension local;
                    ContentPacketExtension remote;
                    if (cpes == ourAnswer) {
                        local = cpe2;
                        remote = theirOffer == null ? null : findContentByName(theirOffer, cpe2.getName());
                    } else {
                        local = findContentByName(ourAnswer, cpe2.getName());
                        remote = cpe2;
                    }
                    contentMap.put(local, remote);
                }
            }
            if (!contentMap.isEmpty()) {
                if (this.colibri == null) {
                    this.colibri = new ColibriConferenceIQ();
                }
                for (Entry<ContentPacketExtension, ContentPacketExtension> e : contentMap.entrySet()) {
                    cpe2 = (ContentPacketExtension) e.getValue();
                    if (cpe2 == null) {
                        cpe2 = (ContentPacketExtension) e.getKey();
                    }
                    this.colibri.getOrCreateContent(((RtpDescriptionPacketExtension) cpe2.getFirstChildOfType(RtpDescriptionPacketExtension.class)).getMedia());
                }
                ColibriConferenceIQ conferenceResult = call.createColibriChannels(peer, contentMap);
                if (conferenceResult != null) {
                    String videobridgeID = this.colibri.getID();
                    String conferenceResultID = conferenceResult.getID();
                    if (videobridgeID == null) {
                        this.colibri.setID(conferenceResultID);
                    } else if (!videobridgeID.equals(conferenceResultID)) {
                        throw new IllegalStateException("conference.id");
                    }
                    String videobridgeFrom = conferenceResult.getFrom();
                    if (!(videobridgeFrom == null || videobridgeFrom.length() == 0)) {
                        this.colibri.setFrom(videobridgeFrom);
                    }
                    for (Content contentResult : conferenceResult.getContents()) {
                        Content content = this.colibri.getOrCreateContent(contentResult.getName());
                        for (Channel channelResult : contentResult.getChannels()) {
                            if (content.getChannel(channelResult.getID()) == null) {
                                content.addChannel(channelResult);
                            }
                        }
                    }
                } else {
                    ProtocolProviderServiceJabberImpl.throwOperationFailedException("Failed to allocate colibri channel.", 1, null, logger);
                }
            }
        }
        for (ContentPacketExtension cpe22 : cpes) {
            String contentName = cpe22.getName();
            ContentPacketExtension ourContent = findContentByName(ourAnswer, contentName);
            if (ourContent != null) {
                ContentPacketExtension theirContent;
                if (theirOffer == null) {
                    theirContent = null;
                } else {
                    theirContent = findContentByName(theirOffer, contentName);
                }
                PacketExtension pe = startCandidateHarvest(theirContent, ourContent, transportInfoSender, ((RtpDescriptionPacketExtension) ourContent.getFirstChildOfType(RtpDescriptionPacketExtension.class)).getMedia());
                if (pe != null) {
                    ourContent.addChildExtension(pe);
                }
            }
        }
    }

    public void startCandidateHarvest(List<ContentPacketExtension> ourOffer, TransportInfoSender transportInfoSender) throws OperationFailedException {
        startCandidateHarvest(null, ourOffer, transportInfoSender);
    }

    public static ContentPacketExtension findContentByName(Iterable<ContentPacketExtension> cpExtList, String name) {
        for (ContentPacketExtension cpExt : cpExtList) {
            if (cpExt.getName().equals(name)) {
                return cpExt;
            }
        }
        return null;
    }

    public boolean startConnectivityEstablishment(Iterable<ContentPacketExtension> iterable) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean startConnectivityEstablishment(Map<String, IceUdpTransportPacketExtension> map) {
        return true;
    }

    public void wrapupConnectivityEstablishment() throws OperationFailedException {
    }

    /* access modifiers changed from: protected */
    public ContentPacketExtension removeContent(Iterable<ContentPacketExtension> contents, String name) {
        Iterator<ContentPacketExtension> contentIter = contents.iterator();
        while (contentIter.hasNext()) {
            ContentPacketExtension content = (ContentPacketExtension) contentIter.next();
            if (name.equals(content.getName())) {
                contentIter.remove();
                RtpDescriptionPacketExtension rtpDescription = (RtpDescriptionPacketExtension) content.getFirstChildOfType(RtpDescriptionPacketExtension.class);
                if (rtpDescription == null) {
                    return content;
                }
                closeStreamConnector(MediaType.parseString(rtpDescription.getMedia()));
                return content;
            }
        }
        return null;
    }

    private static <T extends AbstractPacketExtension> T clone(T src) throws Exception {
        AbstractPacketExtension dst = (AbstractPacketExtension) src.getClass().newInstance();
        for (String name : src.getAttributeNames()) {
            dst.setAttribute(name, src.getAttribute(name));
        }
        dst.setNamespace(src.getNamespace());
        dst.setText(src.getText());
        return dst;
    }

    static IceUdpTransportPacketExtension cloneTransportAndCandidates(IceUdpTransportPacketExtension src) throws OperationFailedException {
        if (src == null) {
            return null;
        }
        try {
            IceUdpTransportPacketExtension dst = (IceUdpTransportPacketExtension) clone(src);
            for (CandidatePacketExtension srcCand : src.getCandidateList()) {
                if (!(srcCand instanceof RemoteCandidatePacketExtension)) {
                    dst.addCandidate((CandidatePacketExtension) clone(srcCand));
                }
            }
            return dst;
        } catch (Exception e) {
            ProtocolProviderServiceJabberImpl.throwOperationFailedException("Failed to close transport and candidates.", 1, e, logger);
            return null;
        }
    }

    public void close() {
        for (MediaType mediaType : MediaType.values()) {
            closeStreamConnector(mediaType);
        }
    }

    /* access modifiers changed from: protected */
    public void closeStreamConnector(MediaType mediaType, StreamConnector streamConnector) {
        boolean superCloseStreamConnector = true;
        CallPeerJabberImpl peer;
        CallJabberImpl call;
        Content content;
        List<Channel> channels;
        ColibriConferenceIQ requestConferenceIQ;
        try {
            if (streamConnector instanceof ColibriStreamConnector) {
                peer = (CallPeerJabberImpl) getCallPeer();
                if (peer != null) {
                    call = (CallJabberImpl) peer.getCall();
                    if (call != null) {
                        superCloseStreamConnector = false;
                        call.closeColibriStreamConnector(peer, mediaType, (ColibriStreamConnector) streamConnector);
                    }
                }
            }
            if (superCloseStreamConnector) {
                TransportManagerJabberImpl.super.closeStreamConnector(mediaType, streamConnector);
            }
            if (this.colibri != null) {
                content = this.colibri.getContent(mediaType.toString());
                if (content != null) {
                    channels = content.getChannels();
                    if (channels.size() == 2) {
                        requestConferenceIQ = new ColibriConferenceIQ();
                        requestConferenceIQ.setID(this.colibri.getID());
                        requestConferenceIQ.getOrCreateContent(content.getName()).addChannel((Channel) channels.get(1));
                        this.colibri.removeContent(content);
                        peer = (CallPeerJabberImpl) getCallPeer();
                        if (peer != null) {
                            call = (CallJabberImpl) peer.getCall();
                            if (call != null) {
                                call.expireColibriChannels(peer, requestConferenceIQ);
                            }
                        }
                    }
                }
            }
        } catch (Throwable th) {
            Throwable th2 = th;
            if (this.colibri != null) {
                content = this.colibri.getContent(mediaType.toString());
                if (content != null) {
                    channels = content.getChannels();
                    if (channels.size() == 2) {
                        requestConferenceIQ = new ColibriConferenceIQ();
                        requestConferenceIQ.setID(this.colibri.getID());
                        requestConferenceIQ.getOrCreateContent(content.getName()).addChannel((Channel) channels.get(1));
                        this.colibri.removeContent(content);
                        peer = (CallPeerJabberImpl) getCallPeer();
                        if (peer != null) {
                            call = (CallJabberImpl) peer.getCall();
                            if (call != null) {
                                call.expireColibriChannels(peer, requestConferenceIQ);
                            }
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public StreamConnector createStreamConnector(final MediaType mediaType) throws OperationFailedException {
        Channel channel = getColibriChannel(mediaType, true);
        if (channel != null) {
            CallPeerJabberImpl peer = (CallPeerJabberImpl) getCallPeer();
            StreamConnector streamConnector = ((CallJabberImpl) peer.getCall()).createColibriStreamConnector(peer, mediaType, channel, new StreamConnectorFactory() {
                public StreamConnector createStreamConnector() {
                    try {
                        return TransportManagerJabberImpl.this.doCreateStreamConnector(mediaType);
                    } catch (OperationFailedException e) {
                        return null;
                    }
                }
            });
            if (streamConnector != null) {
                return streamConnector;
            }
        }
        return doCreateStreamConnector(mediaType);
    }

    /* access modifiers changed from: protected */
    public PacketExtension createTransportForStartCandidateHarvest(String media) throws OperationFailedException {
        if (!((CallPeerJabberImpl) getCallPeer()).isJitsiVideobridge()) {
            return createTransport(media);
        }
        Channel channel = getColibriChannel(MediaType.parseString(media), false);
        if (channel != null) {
            return cloneTransportAndCandidates(channel.getTransport());
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public StreamConnector doCreateStreamConnector(MediaType mediaType) throws OperationFailedException {
        return TransportManagerJabberImpl.super.createStreamConnector(mediaType);
    }

    /* access modifiers changed from: 0000 */
    public TransportManagerJabberImpl findTransportManagerEstablishingConnectivityWithJitsiVideobridge() {
        Call call = ((CallPeerJabberImpl) getCallPeer()).getCall();
        TransportManagerJabberImpl transportManager = null;
        if (call != null) {
            CallConference conference = call.getConference();
            if (conference != null && conference.isJitsiVideobridge()) {
                for (Call aCall : conference.getCalls()) {
                    Iterator<? extends CallPeer> callPeerIter = aCall.getCallPeers();
                    while (callPeerIter.hasNext()) {
                        CallPeer aCallPeer = (CallPeer) callPeerIter.next();
                        if (aCallPeer instanceof CallPeerJabberImpl) {
                            TransportManagerJabberImpl aTransportManager = ((CallPeerMediaHandlerJabberImpl) ((CallPeerJabberImpl) aCallPeer).getMediaHandler()).getTransportManager();
                            if (aTransportManager.isEstablishingConnectivityWithJitsiVideobridge) {
                                transportManager = aTransportManager;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return transportManager;
    }

    /* access modifiers changed from: 0000 */
    public Channel getColibriChannel(MediaType mediaType, boolean local) {
        if (this.colibri == null) {
            return null;
        }
        Content content = this.colibri.getContent(mediaType.toString());
        if (content == null) {
            return null;
        }
        List<Channel> channels = content.getChannels();
        if (channels.size() != 2) {
            return null;
        }
        return (Channel) channels.get(local ? 0 : 1);
    }
}
