package net.java.sip.communicator.impl.protocol.jabber;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.List;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriConferenceIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CoinPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension.SendersEnum;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleAction;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JinglePacketFactory;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.Reason;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ReasonPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RtpDescriptionPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.SessionInfoPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.SessionInfoType;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.TransferPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.TransferredPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.jinglesdp.JingleUtils;
import net.java.sip.communicator.service.protocol.CallPeerState;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetBasicTelephony;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.MediaStream;
import org.jitsi.service.neomedia.MediaType;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.packet.DiscoverInfo;

public class CallPeerJabberImpl extends AbstractCallPeerJabberGTalkImpl<CallJabberImpl, CallPeerMediaHandlerJabberImpl, JingleIQ> {
    private static final Logger logger = Logger.getLogger(CallPeerJabberImpl.class);
    private SendersEnum audioSenders;
    private boolean cancelled;
    /* access modifiers changed from: private|final */
    public final Object candSyncRoot;
    /* access modifiers changed from: private */
    public boolean contentAddWithNoCands;
    private boolean sessionInitiateProcessed;
    private final Object sessionInitiateSyncRoot;
    private final Object sidSyncRoot;
    private SendersEnum videoSenders;

    public CallPeerJabberImpl(String peerAddress, CallJabberImpl owningCall) {
        super(peerAddress, owningCall);
        this.cancelled = false;
        this.candSyncRoot = new Object();
        this.contentAddWithNoCands = false;
        this.sessionInitiateProcessed = false;
        this.sessionInitiateSyncRoot = new Object();
        this.sidSyncRoot = new Object();
        this.audioSenders = SendersEnum.none;
        this.videoSenders = SendersEnum.none;
        setMediaHandler(new CallPeerMediaHandlerJabberImpl(this));
    }

    public CallPeerJabberImpl(String peerAddress, CallJabberImpl owningCall, JingleIQ sessionIQ) {
        this(peerAddress, owningCall);
        this.sessionInitIQ = sessionIQ;
    }

    public synchronized void answer() throws OperationFailedException {
        String reasonText;
        JingleIQ errResp;
        CallPeerMediaHandlerJabberImpl mediaHandler = (CallPeerMediaHandlerJabberImpl) getMediaHandler();
        try {
            mediaHandler.getTransportManager().wrapupConnectivityEstablishment();
            Iterable<ContentPacketExtension> answer = mediaHandler.generateSessionAccept();
            for (ContentPacketExtension c : answer) {
                setSenders(getMediaType(c), c.getSenders());
            }
            ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getConnection().sendPacket(JinglePacketFactory.createSessionAccept(((JingleIQ) this.sessionInitIQ).getTo(), ((JingleIQ) this.sessionInitIQ).getFrom(), getSID(), answer));
            try {
                mediaHandler.start();
                setState(CallPeerState.CONNECTED);
            } catch (UndeclaredThrowableException e) {
                Throwable exc = e.getUndeclaredThrowable();
                logger.info("Failed to establish a connection", exc);
                reasonText = "Error: " + exc.getMessage();
                errResp = JinglePacketFactory.createSessionTerminate(((JingleIQ) this.sessionInitIQ).getTo(), ((JingleIQ) this.sessionInitIQ).getFrom(), ((JingleIQ) this.sessionInitIQ).getSID(), Reason.GENERAL_ERROR, reasonText);
                setState(CallPeerState.FAILED, reasonText);
                ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getConnection().sendPacket(errResp);
            }
        } catch (Exception exc2) {
            logger.info("Failed to answer an incoming call", exc2);
            reasonText = "Error: " + exc2.getMessage();
            errResp = JinglePacketFactory.createSessionTerminate(((JingleIQ) this.sessionInitIQ).getTo(), ((JingleIQ) this.sessionInitIQ).getFrom(), ((JingleIQ) this.sessionInitIQ).getSID(), Reason.FAILED_APPLICATION, reasonText);
            setState(CallPeerState.FAILED, reasonText);
            ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getConnection().sendPacket(errResp);
        }
        return;
    }

    public String getSID() {
        return this.sessionInitIQ != null ? ((JingleIQ) this.sessionInitIQ).getSID() : null;
    }

    public JingleIQ getSessionIQ() {
        return (JingleIQ) this.sessionInitIQ;
    }

    public void hangup(boolean failed, String reasonText, PacketExtension reasonOtherExtension) {
        CallPeerState prevPeerState = getState();
        if (!CallPeerState.DISCONNECTED.equals(prevPeerState) && !CallPeerState.FAILED.equals(prevPeerState)) {
            setState(failed ? CallPeerState.FAILED : CallPeerState.DISCONNECTED, reasonText);
            JingleIQ responseIQ = null;
            if (prevPeerState.equals(CallPeerState.CONNECTED) || CallPeerState.isOnHold(prevPeerState)) {
                responseIQ = JinglePacketFactory.createBye(((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getOurJID(), this.peerJID, getSID());
            } else if (CallPeerState.CONNECTING.equals(prevPeerState) || CallPeerState.CONNECTING_WITH_EARLY_MEDIA.equals(prevPeerState) || CallPeerState.ALERTING_REMOTE_SIDE.equals(prevPeerState)) {
                if (getSID() == null) {
                    synchronized (this.sidSyncRoot) {
                        this.cancelled = true;
                    }
                    return;
                }
                responseIQ = JinglePacketFactory.createCancel(((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getOurJID(), this.peerJID, getSID());
            } else if (prevPeerState.equals(CallPeerState.INCOMING_CALL)) {
                responseIQ = JinglePacketFactory.createBusy(((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getOurJID(), this.peerJID, getSID());
            } else if (!(prevPeerState.equals(CallPeerState.BUSY) || prevPeerState.equals(CallPeerState.FAILED))) {
                logger.info("Could not determine call peer state!");
            }
            if (responseIQ != null) {
                if (reasonOtherExtension != null) {
                    ReasonPacketExtension reason = (ReasonPacketExtension) responseIQ.getExtension("reason", "");
                    if (reason != null) {
                        reason.setOtherExtension(reasonOtherExtension);
                    } else if (reasonOtherExtension instanceof ReasonPacketExtension) {
                        responseIQ.setReason((ReasonPacketExtension) reasonOtherExtension);
                    }
                }
                ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getConnection().sendPacket(responseIQ);
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("Ignoring a request to hangup a call peer that is already DISCONNECTED");
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void initiateSession(Iterable<PacketExtension> sessionInitiateExtensions) throws OperationFailedException {
        this.initiator = false;
        List<ContentPacketExtension> offer = ((CallPeerMediaHandlerJabberImpl) getMediaHandler()).createContentList();
        ProtocolProviderServiceJabberImpl protocolProvider = (ProtocolProviderServiceJabberImpl) getProtocolProvider();
        synchronized (this.sidSyncRoot) {
            this.sessionInitIQ = JinglePacketFactory.createSessionInitiate(protocolProvider.getOurJID(), this.peerJID, JingleIQ.generateSID(), offer);
            if (this.cancelled) {
                ((CallPeerMediaHandlerJabberImpl) getMediaHandler()).getTransportManager().close();
            } else {
                if (sessionInitiateExtensions != null) {
                    for (PacketExtension sessionInitiateExtension : sessionInitiateExtensions) {
                        ((JingleIQ) this.sessionInitIQ).addExtension(sessionInitiateExtension);
                    }
                }
                protocolProvider.getConnection().sendPacket(this.sessionInitIQ);
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void processColibriConferenceIQ(ColibriConferenceIQ conferenceIQ) {
        ((CallPeerMediaHandlerJabberImpl) getMediaHandler()).processColibriConferenceIQ(conferenceIQ);
    }

    public void processContentAccept(JingleIQ content) {
        List<ContentPacketExtension> contents = content.getContentList();
        CallPeerMediaHandlerJabberImpl mediaHandler = (CallPeerMediaHandlerJabberImpl) getMediaHandler();
        try {
            mediaHandler.getTransportManager().wrapupConnectivityEstablishment();
            mediaHandler.processAnswer(contents);
            for (ContentPacketExtension c : contents) {
                setSenders(getMediaType(c), c.getSenders());
            }
            mediaHandler.start();
        } catch (Exception e) {
            logger.warn("Failed to process a content-accept", e);
            String reason = "Error: " + e.getMessage();
            JingleIQ errResp = JinglePacketFactory.createSessionTerminate(((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getOurJID(), this.peerJID, ((JingleIQ) this.sessionInitIQ).getSID(), Reason.INCOMPATIBLE_PARAMETERS, reason);
            setState(CallPeerState.FAILED, reason);
            ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getConnection().sendPacket(errResp);
        }
    }

    public void processContentAdd(final JingleIQ content) {
        CallPeerMediaHandlerJabberImpl mediaHandler = (CallPeerMediaHandlerJabberImpl) getMediaHandler();
        List<ContentPacketExtension> contents = content.getContentList();
        boolean noCands = false;
        MediaStream oldVideoStream = mediaHandler.getStream(MediaType.VIDEO);
        if (logger.isInfoEnabled()) {
            logger.info("Looking for candidates in content-add.");
        }
        Iterable<ContentPacketExtension> answerContents;
        JingleIQ contentIQ;
        try {
            if (!this.contentAddWithNoCands) {
                mediaHandler.processOffer(contents);
                for (ContentPacketExtension c : contents) {
                    if (JingleUtils.getFirstCandidate(c, 1) == null) {
                        this.contentAddWithNoCands = true;
                        noCands = true;
                    }
                }
            }
            if (noCands) {
                new Thread() {
                    public void run() {
                        try {
                            synchronized (CallPeerJabberImpl.this.candSyncRoot) {
                                CallPeerJabberImpl.this.candSyncRoot.wait();
                            }
                        } catch (InterruptedException e) {
                        }
                        CallPeerJabberImpl.this.processContentAdd(content);
                        CallPeerJabberImpl.this.contentAddWithNoCands = false;
                    }
                }.start();
                if (logger.isInfoEnabled()) {
                    logger.info("No candidates found in content-add, started new thread.");
                    return;
                }
                return;
            }
            mediaHandler.getTransportManager().wrapupConnectivityEstablishment();
            if (logger.isInfoEnabled()) {
                logger.info("Wrapping up connectivity establishment");
            }
            answerContents = mediaHandler.generateSessionAccept();
            contentIQ = null;
            if (contentIQ == null) {
                contentIQ = JinglePacketFactory.createContentAccept(((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getOurJID(), this.peerJID, getSID(), answerContents);
                for (ContentPacketExtension c2 : answerContents) {
                    setSenders(getMediaType(c2), c2.getSenders());
                }
            }
            ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getConnection().sendPacket(contentIQ);
            mediaHandler.start();
            if (oldVideoStream == null && mediaHandler.getStream(MediaType.VIDEO) != null && mediaHandler.isRTPTranslationEnabled(MediaType.VIDEO)) {
                try {
                    ((CallJabberImpl) getCall()).modifyVideoContent();
                } catch (OperationFailedException ofe) {
                    logger.error("Failed to enable RTP translation", ofe);
                }
            }
        } catch (Exception e) {
            logger.warn("Exception occurred", e);
            answerContents = null;
            contentIQ = JinglePacketFactory.createContentReject(((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getOurJID(), this.peerJID, getSID(), null);
        }
    }

    public void processContentModify(JingleIQ content) {
        boolean modify = false;
        ContentPacketExtension ext = (ContentPacketExtension) content.getContentList().get(0);
        MediaType mediaType = getMediaType(ext);
        try {
            if (ext.getFirstChildOfType(RtpDescriptionPacketExtension.class) != null) {
                modify = true;
            }
            ((CallPeerMediaHandlerJabberImpl) getMediaHandler()).reinitContent(ext.getName(), ext, modify);
            setSenders(mediaType, ext.getSenders());
            if (MediaType.VIDEO.equals(mediaType)) {
                ((CallJabberImpl) getCall()).modifyVideoContent();
            }
        } catch (Exception e) {
            logger.info("Failed to process an incoming content-modify", e);
            String reason = "Error: " + e.getMessage();
            JingleIQ errResp = JinglePacketFactory.createSessionTerminate(((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getOurJID(), this.peerJID, ((JingleIQ) this.sessionInitIQ).getSID(), Reason.INCOMPATIBLE_PARAMETERS, reason);
            setState(CallPeerState.FAILED, reason);
            ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getConnection().sendPacket(errResp);
        }
    }

    public void processContentReject(JingleIQ content) {
        if (content.getContentList().isEmpty()) {
            JingleIQ errResp = JinglePacketFactory.createSessionTerminate(((JingleIQ) this.sessionInitIQ).getTo(), ((JingleIQ) this.sessionInitIQ).getFrom(), ((JingleIQ) this.sessionInitIQ).getSID(), Reason.INCOMPATIBLE_PARAMETERS, "Error: content rejected");
            setState(CallPeerState.FAILED, "Error: content rejected");
            ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getConnection().sendPacket(errResp);
        }
    }

    public void processContentRemove(JingleIQ content) {
        List<ContentPacketExtension> contents = content.getContentList();
        boolean videoContentRemoved = false;
        if (!contents.isEmpty()) {
            CallPeerMediaHandlerJabberImpl mediaHandler = (CallPeerMediaHandlerJabberImpl) getMediaHandler();
            for (ContentPacketExtension c : contents) {
                mediaHandler.removeContent(c.getName());
                MediaType mediaType = getMediaType(c);
                setSenders(mediaType, SendersEnum.none);
                if (MediaType.VIDEO.equals(mediaType)) {
                    videoContentRemoved = true;
                }
            }
        }
        if (videoContentRemoved) {
            try {
                ((CallJabberImpl) getCall()).modifyVideoContent();
            } catch (Exception e) {
                logger.warn("Failed to update Jingle sessions");
            }
        }
    }

    public void processSessionAccept(JingleIQ sessionInitIQ) {
        this.sessionInitIQ = sessionInitIQ;
        CallPeerMediaHandlerJabberImpl mediaHandler = (CallPeerMediaHandlerJabberImpl) getMediaHandler();
        List<ContentPacketExtension> answer = sessionInitIQ.getContentList();
        try {
            mediaHandler.getTransportManager().wrapupConnectivityEstablishment();
            mediaHandler.processAnswer(answer);
            for (ContentPacketExtension c : answer) {
                setSenders(getMediaType(c), c.getSenders());
            }
            setState(CallPeerState.CONNECTED);
            mediaHandler.start();
            sendModifyVideoContent();
        } catch (Exception exc) {
            if (logger.isInfoEnabled()) {
                logger.info("Failed to process a session-accept", exc);
            }
            JingleIQ errResp = JinglePacketFactory.createSessionTerminate(sessionInitIQ.getTo(), sessionInitIQ.getFrom(), sessionInitIQ.getSID(), Reason.INCOMPATIBLE_PARAMETERS, exc.getClass().getName() + ": " + exc.getMessage());
            setState(CallPeerState.FAILED, "Error: " + exc.getMessage());
            ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getConnection().sendPacket(errResp);
        }
    }

    public void processSessionInfo(SessionInfoPacketExtension info) {
        switch (info.getType()) {
            case ringing:
                setState(CallPeerState.ALERTING_REMOTE_SIDE);
                return;
            case hold:
                ((CallPeerMediaHandlerJabberImpl) getMediaHandler()).setRemotelyOnHold(true);
                reevalRemoteHoldStatus();
                return;
            case unhold:
            case active:
                ((CallPeerMediaHandlerJabberImpl) getMediaHandler()).setRemotelyOnHold(false);
                reevalRemoteHoldStatus();
                return;
            default:
                logger.warn("Received SessionInfoPacketExtension of unknown type");
                return;
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void processSessionInitiate(JingleIQ sessionInitIQ) {
        this.sessionInitIQ = sessionInitIQ;
        this.initiator = true;
        try {
            ((CallPeerMediaHandlerJabberImpl) getMediaHandler()).processOffer(sessionInitIQ.getContentList());
            CoinPacketExtension coin = null;
            for (PacketExtension ext : sessionInitIQ.getExtensions()) {
                if (ext.getElementName().equals("conference-info")) {
                    coin = (CoinPacketExtension) ext;
                    break;
                }
            }
            if (coin != null) {
                setConferenceFocus(Boolean.parseBoolean((String) coin.getAttribute(CoinPacketExtension.ISFOCUS_ATTR_NAME)));
            }
            if (getDiscoveryInfo() == null) {
                retrieveDiscoveryInfo(sessionInitIQ.getFrom());
            }
            if (logger.isTraceEnabled()) {
                logger.trace("will send ringing response: ");
            }
            ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getConnection().sendPacket(JinglePacketFactory.createRinging(sessionInitIQ));
            synchronized (this.sessionInitiateSyncRoot) {
                this.sessionInitiateProcessed = true;
                this.sessionInitiateSyncRoot.notify();
            }
            DiscoverInfo discoverInfo = getDiscoveryInfo();
            if (discoverInfo != null && discoverInfo.containsFeature(ProtocolProviderServiceJabberImpl.URN_IETF_RFC_3264)) {
                ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getConnection().sendPacket(JinglePacketFactory.createDescriptionInfo(sessionInitIQ.getTo(), sessionInitIQ.getFrom(), sessionInitIQ.getSID(), ((CallPeerMediaHandlerJabberImpl) getMediaHandler()).getLocalContentList()));
            }
        } catch (Exception ex) {
            logger.info("Failed to process an incoming session initiate", ex);
            String reasonText = "Error: " + ex.getMessage();
            JingleIQ errResp = JinglePacketFactory.createSessionTerminate(sessionInitIQ.getTo(), sessionInitIQ.getFrom(), sessionInitIQ.getSID(), Reason.INCOMPATIBLE_PARAMETERS, reasonText);
            setState(CallPeerState.FAILED, reasonText);
            ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getConnection().sendPacket(errResp);
        }
        return;
    }

    public void processSessionTerminate(JingleIQ jingleIQ) {
        String reasonStr = "Call ended by remote side.";
        ReasonPacketExtension reasonExt = jingleIQ.getReason();
        if (reasonExt != null) {
            Reason reason = reasonExt.getReason();
            if (reason != null) {
                reasonStr = reasonStr + " Reason: " + reason.toString() + Separators.DOT;
            }
            String text = reasonExt.getText();
            if (text != null) {
                reasonStr = reasonStr + Separators.SP + text;
            }
        }
        setState(CallPeerState.DISCONNECTED, reasonStr);
    }

    public void processTransfer(TransferPacketExtension transfer) throws OperationFailedException {
        String attendantAddress = transfer.getFrom();
        if (attendantAddress == null) {
            throw new OperationFailedException("Session transfer must contain a 'from' attribute value.", 11);
        }
        String calleeAddress = transfer.getTo();
        if (calleeAddress == null) {
            throw new OperationFailedException("Session transfer must contain a 'to' attribute value.", 11);
        }
        if (!((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getConnection().getRoster().contains(StringUtils.parseBareAddress(calleeAddress))) {
            String failedMessage = "Tranfer impossible:\nAccount roster does not contain tansfer peer: " + StringUtils.parseBareAddress(calleeAddress);
            setState(CallPeerState.FAILED, failedMessage);
            logger.info(failedMessage);
        }
        OperationSetBasicTelephonyJabberImpl basicTelephony = (OperationSetBasicTelephonyJabberImpl) ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getOperationSet(OperationSetBasicTelephony.class);
        CallJabberImpl calleeCall = new CallJabberImpl(basicTelephony);
        TransferPacketExtension calleeTransfer = new TransferPacketExtension();
        String sid = transfer.getSID();
        calleeTransfer.setFrom(attendantAddress);
        if (sid != null) {
            calleeTransfer.setSID(sid);
            calleeTransfer.setTo(calleeAddress);
        }
        basicTelephony.createOutgoingCall(calleeCall, calleeAddress, Arrays.asList(new PacketExtension[]{calleeTransfer}));
    }

    public void processTransportInfo(JingleIQ jingleIQ) {
        try {
            if (isInitiator()) {
                synchronized (this.sessionInitiateSyncRoot) {
                    if (!this.sessionInitiateProcessed) {
                        try {
                            this.sessionInitiateSyncRoot.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
            ((CallPeerMediaHandlerJabberImpl) getMediaHandler()).processTransportInfo(jingleIQ.getContentList());
            synchronized (this.candSyncRoot) {
                this.candSyncRoot.notify();
            }
        } catch (OperationFailedException ofe) {
            logger.warn("Failed to process an incoming transport-info", ofe);
            String reasonText = "Error: " + ofe.getMessage();
            JingleIQ errResp = JinglePacketFactory.createSessionTerminate(((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getOurJID(), this.peerJID, ((JingleIQ) this.sessionInitIQ).getSID(), Reason.GENERAL_ERROR, reasonText);
            setState(CallPeerState.FAILED, reasonText);
            ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getConnection().sendPacket(errResp);
        }
    }

    public void putOnHold(boolean onHold) throws OperationFailedException {
        SessionInfoType type;
        ((CallPeerMediaHandlerJabberImpl) getMediaHandler()).setLocallyOnHold(onHold);
        if (onHold) {
            type = SessionInfoType.hold;
        } else {
            type = SessionInfoType.unhold;
            ((CallPeerMediaHandlerJabberImpl) getMediaHandler()).reinitAllContents();
        }
        reevalLocalHoldStatus();
        ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getConnection().sendPacket(JinglePacketFactory.createSessionInfo(((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getOurJID(), this.peerJID, getSID(), type));
    }

    private void sendAddVideoContent() {
        try {
            ProtocolProviderServiceJabberImpl protocolProvider = (ProtocolProviderServiceJabberImpl) getProtocolProvider();
            protocolProvider.getConnection().sendPacket(JinglePacketFactory.createContentAdd(protocolProvider.getOurJID(), this.peerJID, getSID(), ((CallPeerMediaHandlerJabberImpl) getMediaHandler()).createContentList(MediaType.VIDEO)));
        } catch (Exception exc) {
            logger.warn("Failed to gather content for video type", exc);
        }
    }

    public void sendCoinSessionInfo() {
        JingleIQ sessionInfoIQ = JinglePacketFactory.createSessionInfo(((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getOurJID(), this.peerJID, getSID());
        sessionInfoIQ.addExtension(new CoinPacketExtension(((CallJabberImpl) getCall()).isConferenceFocus()));
        ((ProtocolProviderServiceJabberImpl) getProtocolProvider()).getConnection().sendPacket(sessionInfoIQ);
    }

    private MediaDirection getDirectionForJingle(MediaType mediaType) {
        MediaDirection direction = MediaDirection.INACTIVE;
        CallPeerMediaHandlerJabberImpl mediaHandler = (CallPeerMediaHandlerJabberImpl) getMediaHandler();
        if ((MediaType.AUDIO == mediaType && mediaHandler.isLocalAudioTransmissionEnabled()) || (MediaType.VIDEO == mediaType && isLocalVideoStreaming())) {
            direction = direction.or(MediaDirection.SENDONLY);
        }
        SendersEnum senders = getSenders(mediaType);
        if (senders == null || senders == SendersEnum.both || ((isInitiator() && senders == SendersEnum.initiator) || (!isInitiator() && senders == SendersEnum.responder))) {
            direction = direction.or(MediaDirection.RECVONLY);
        }
        if (!((CallJabberImpl) getCall()).isConferenceFocus()) {
            return direction;
        }
        for (CallPeerJabberImpl peer : ((CallJabberImpl) getCall()).getCallPeerList()) {
            if (peer != this) {
                senders = peer.getSenders(mediaType);
                if (senders == null || senders == SendersEnum.both || ((peer.isInitiator() && senders == SendersEnum.initiator) || (!peer.isInitiator() && senders == SendersEnum.responder))) {
                    return direction.or(MediaDirection.SENDONLY);
                }
            }
        }
        return direction;
    }

    public boolean sendModifyVideoContent() {
        CallPeerMediaHandlerJabberImpl mediaHandler = (CallPeerMediaHandlerJabberImpl) getMediaHandler();
        MediaDirection direction = getDirectionForJingle(MediaType.VIDEO);
        ContentPacketExtension remoteContent = mediaHandler.getLocalContent(MediaType.VIDEO.toString());
        if (remoteContent == null) {
            if (direction == MediaDirection.INACTIVE) {
                return false;
            }
            if (getState() != CallPeerState.CONNECTED) {
                return false;
            }
            if (logger.isInfoEnabled()) {
                logger.info("Adding video content for " + this);
            }
            sendAddVideoContent();
            return true;
        } else if (direction == MediaDirection.INACTIVE) {
            sendRemoveVideoContent();
            return true;
        } else {
            SendersEnum senders = getSenders(MediaType.VIDEO);
            if (senders == null) {
                senders = SendersEnum.both;
            }
            SendersEnum newSenders = SendersEnum.none;
            if (MediaDirection.SENDRECV == direction) {
                newSenders = SendersEnum.both;
            } else if (MediaDirection.RECVONLY == direction) {
                newSenders = isInitiator() ? SendersEnum.initiator : SendersEnum.responder;
            } else if (MediaDirection.SENDONLY == direction) {
                newSenders = isInitiator() ? SendersEnum.responder : SendersEnum.initiator;
            }
            ContentPacketExtension ext = new ContentPacketExtension();
            String remoteContentName = remoteContent.getName();
            ext.setSenders(newSenders);
            ext.setCreator(remoteContent.getCreator());
            ext.setName(remoteContentName);
            if (newSenders != senders) {
                if (logger.isInfoEnabled()) {
                    logger.info("Sending content modify, senders: " + senders + "->" + newSenders);
                }
                ProtocolProviderServiceJabberImpl protocolProvider = (ProtocolProviderServiceJabberImpl) getProtocolProvider();
                protocolProvider.getConnection().sendPacket(JinglePacketFactory.createContentModify(protocolProvider.getOurJID(), this.peerJID, getSID(), ext));
            }
            try {
                mediaHandler.reinitContent(remoteContentName, ext, false);
                mediaHandler.start();
            } catch (Exception e) {
                logger.warn("Exception occurred during media reinitialization", e);
            }
            if (newSenders == senders) {
                return false;
            }
            return true;
        }
    }

    public void sendModifyVideoResolutionContent() {
        CallPeerMediaHandlerJabberImpl mediaHandler = (CallPeerMediaHandlerJabberImpl) getMediaHandler();
        ContentPacketExtension remoteContent = mediaHandler.getRemoteContent(MediaType.VIDEO.toString());
        logger.info("send modify-content to change resolution");
        try {
            ContentPacketExtension content = mediaHandler.createContentForMedia(MediaType.VIDEO);
            SendersEnum senders = remoteContent.getSenders();
            if (senders != null) {
                content.setSenders(senders);
            }
            ProtocolProviderServiceJabberImpl protocolProvider = (ProtocolProviderServiceJabberImpl) getProtocolProvider();
            protocolProvider.getConnection().sendPacket(JinglePacketFactory.createContentModify(protocolProvider.getOurJID(), this.peerJID, getSID(), content));
            try {
                mediaHandler.reinitContent(remoteContent.getName(), content, false);
                mediaHandler.start();
            } catch (Exception e) {
                logger.warn("Exception occurred when media reinitialization", e);
            }
        } catch (Exception e2) {
            logger.warn("Failed to gather content for video type", e2);
        }
    }

    private void sendRemoveVideoContent() {
        CallPeerMediaHandlerJabberImpl mediaHandler = (CallPeerMediaHandlerJabberImpl) getMediaHandler();
        ContentPacketExtension content = new ContentPacketExtension();
        ContentPacketExtension remoteContent = mediaHandler.getRemoteContent(MediaType.VIDEO.toString());
        if (remoteContent != null) {
            String remoteContentName = remoteContent.getName();
            content.setName(remoteContentName);
            content.setCreator(remoteContent.getCreator());
            content.setSenders(remoteContent.getSenders());
            ProtocolProviderServiceJabberImpl protocolProvider = (ProtocolProviderServiceJabberImpl) getProtocolProvider();
            protocolProvider.getConnection().sendPacket(JinglePacketFactory.createContentRemove(protocolProvider.getOurJID(), this.peerJID, getSID(), Arrays.asList(new ContentPacketExtension[]{content})));
            mediaHandler.removeContent(remoteContentName);
            setSenders(MediaType.VIDEO, SendersEnum.none);
        }
    }

    /* access modifiers changed from: protected */
    public void sendTransportInfo(Iterable<ContentPacketExtension> contents) {
        if (!this.cancelled) {
            JingleIQ transportInfo = new JingleIQ();
            for (ContentPacketExtension content : contents) {
                transportInfo.addContent(content);
            }
            ProtocolProviderServiceJabberImpl protocolProvider = (ProtocolProviderServiceJabberImpl) getProtocolProvider();
            transportInfo.setAction(JingleAction.TRANSPORT_INFO);
            transportInfo.setFrom(protocolProvider.getOurJID());
            transportInfo.setSID(getSID());
            transportInfo.setTo(getAddress());
            transportInfo.setType(Type.SET);
            PacketCollector collector = protocolProvider.getConnection().createPacketCollector(new PacketIDFilter(transportInfo.getPacketID()));
            protocolProvider.getConnection().sendPacket(transportInfo);
            collector.nextResult((long) SmackConfiguration.getPacketReplyTimeout());
            collector.cancel();
        }
    }

    public void setState(CallPeerState newState, String reason, int reasonCode) {
        CallPeerState oldState = getState();
        try {
            if (CallPeerState.DISCONNECTED.equals(newState) || CallPeerState.FAILED.equals(newState)) {
                ((CallPeerMediaHandlerJabberImpl) getMediaHandler()).getTransportManager().close();
            }
            super.setState(newState, reason, reasonCode);
            if (CallPeerState.isOnHold(oldState) && CallPeerState.CONNECTED.equals(newState)) {
                try {
                    ((CallJabberImpl) getCall()).modifyVideoContent();
                } catch (OperationFailedException e) {
                    logger.error("Failed to update call video state after 'hold' status removed for " + this);
                }
            }
        } catch (Throwable th) {
            super.setState(newState, reason, reasonCode);
        }
    }

    /* access modifiers changed from: protected */
    public void transfer(String to, String sid) throws OperationFailedException {
        JingleIQ transferSessionInfo = new JingleIQ();
        ProtocolProviderServiceJabberImpl protocolProvider = (ProtocolProviderServiceJabberImpl) getProtocolProvider();
        transferSessionInfo.setAction(JingleAction.SESSION_INFO);
        transferSessionInfo.setFrom(protocolProvider.getOurJID());
        transferSessionInfo.setSID(getSID());
        transferSessionInfo.setTo(getAddress());
        transferSessionInfo.setType(Type.SET);
        TransferPacketExtension transfer = new TransferPacketExtension();
        if (sid != null) {
            transfer.setFrom(protocolProvider.getOurJID());
            transfer.setSID(sid);
            CallPeerJabberImpl callPeer = ((OperationSetBasicTelephonyJabberImpl) protocolProvider.getOperationSet(OperationSetBasicTelephony.class)).getActiveCallPeer(sid);
            if (!(callPeer == null || CallPeerState.isOnHold(callPeer.getState()))) {
                callPeer.putOnHold(true);
            }
            if (!CallPeerState.isOnHold(getState())) {
                putOnHold(true);
            }
        }
        transfer.setTo(to);
        transferSessionInfo.addExtension(transfer);
        PacketCollector collector = protocolProvider.getConnection().createPacketCollector(new PacketIDFilter(transferSessionInfo.getPacketID()));
        protocolProvider.getConnection().sendPacket(transferSessionInfo);
        Packet result = collector.nextResult((long) SmackConfiguration.getPacketReplyTimeout());
        if (result == null) {
            throw new OperationFailedException("No response to the \"transfer\" request.", 11);
        } else if (((IQ) result).getType() != Type.RESULT) {
            throw new OperationFailedException("Remote peer does not manage call \"transfer\".Response to the \"transfer\" request is: " + ((IQ) result).getType(), 11);
        } else {
            String message = (sid == null ? "Unattended" : "Attended") + " transfer to: " + to;
            hangup(false, message, new ReasonPacketExtension(Reason.SUCCESS, message, new TransferredPacketExtension()));
        }
    }

    public String getEntity() {
        return getAddress();
    }

    public MediaDirection getDirection(MediaType mediaType) {
        SendersEnum senders = getSenders(mediaType);
        if (senders == SendersEnum.none) {
            return MediaDirection.INACTIVE;
        }
        if (senders == null || senders == SendersEnum.both) {
            return MediaDirection.SENDRECV;
        }
        return senders == SendersEnum.initiator ? isInitiator() ? MediaDirection.RECVONLY : MediaDirection.SENDONLY : isInitiator() ? MediaDirection.SENDONLY : MediaDirection.RECVONLY;
    }

    public SendersEnum getSenders(MediaType mediaType) {
        if (MediaType.AUDIO.equals(mediaType)) {
            return this.audioSenders;
        }
        if (MediaType.VIDEO.equals(mediaType)) {
            return this.videoSenders;
        }
        throw new IllegalArgumentException("mediaType");
    }

    public void setSenders(MediaType mediaType, SendersEnum senders) {
        if (mediaType != null) {
            if (MediaType.AUDIO.equals(mediaType)) {
                this.audioSenders = senders;
            } else if (MediaType.VIDEO.equals(mediaType)) {
                this.videoSenders = senders;
            } else {
                throw new IllegalArgumentException("mediaType");
            }
        }
    }

    public MediaType getMediaType(ContentPacketExtension content) {
        String contentName = content.getName();
        if (contentName == null) {
            return null;
        }
        MediaType mediaType = JingleUtils.getMediaType(content);
        if (mediaType != null) {
            return mediaType;
        }
        CallPeerMediaHandlerJabberImpl mediaHandler = (CallPeerMediaHandlerJabberImpl) getMediaHandler();
        for (MediaType m : MediaType.values()) {
            ContentPacketExtension sessionContent = mediaHandler.getRemoteContent(m.toString());
            if (sessionContent == null) {
                sessionContent = mediaHandler.getLocalContent(m.toString());
            }
            if (sessionContent != null && contentName.equals(sessionContent.getName())) {
                return m;
            }
        }
        return mediaType;
    }
}
