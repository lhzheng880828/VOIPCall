package net.java.sip.communicator.impl.protocol.sip;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.sdp.Attribute;
import javax.sdp.MediaDescription;
import javax.sdp.SdpParseException;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CryptoPacketExtension;
import net.java.sip.communicator.impl.protocol.sip.sdp.SdpUtils;
import net.java.sip.communicator.service.protocol.CallPeerState;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.event.CallPeerAdapter;
import net.java.sip.communicator.service.protocol.event.CallPeerChangeEvent;
import net.java.sip.communicator.service.protocol.event.CallPeerListener;
import net.java.sip.communicator.service.protocol.media.MediaAwareCall;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.javax.sip.stack.SIPTransaction;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.ServerTransaction;
import org.jitsi.javax.sip.SipProvider;
import org.jitsi.javax.sip.Transaction;
import org.jitsi.javax.sip.TransactionUnavailableException;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.header.CallInfoHeader;
import org.jitsi.javax.sip.message.Message;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.QualityPreset;
import org.jitsi.service.neomedia.SrtpControlType;

public class CallSipImpl extends MediaAwareCall<CallPeerSipImpl, OperationSetBasicTelephonySipImpl, ProtocolProviderServiceSipImpl> implements CallPeerListener {
    private static final int DEFAULT_RETRANSMITS_RINGING_INTERVAL = 500;
    private static final int MAX_RETRANSMISSIONS = 3;
    private static final String RETRANSMITS_RINGING_INTERVAL = "net.java.sip.communicator.impl.protocol.sip.RETRANSMITS_RINGING_INTERVAL";
    private static final Logger logger = Logger.getLogger(CallSipImpl.class);
    private QualityPreset initialQualityPreferences;
    private final SipMessageFactory messageFactory = getProtocolProvider().getMessageFactory();
    private final int retransmitsRingingInterval;

    private class RingingResponseTask extends TimerTask {
        private final CallPeerSipImpl peer;
        private final Response response;
        private final ServerTransaction serverTran;
        private CallPeerAdapter stateListener;
        private final Timer timer;

        RingingResponseTask(Response response, ServerTransaction serverTran, CallPeerSipImpl peer, Timer timer, CallPeerAdapter stateListener) {
            this.response = response;
            this.serverTran = serverTran;
            this.peer = peer;
            this.timer = timer;
            this.stateListener = stateListener;
        }

        public void run() {
            try {
                this.serverTran.sendResponse(this.response);
            } catch (Exception e) {
                this.timer.cancel();
                this.peer.removeCallPeerListener(this.stateListener);
            }
        }
    }

    protected CallSipImpl(OperationSetBasicTelephonySipImpl parentOpSet) {
        super(parentOpSet);
        ConfigurationService cfg = SipActivator.getConfigurationService();
        int retransmitsRingingInterval = 500;
        if (cfg != null) {
            retransmitsRingingInterval = cfg.getInt(RETRANSMITS_RINGING_INTERVAL, 500);
        }
        this.retransmitsRingingInterval = retransmitsRingingInterval;
        parentOpSet.getActiveCallsRepository().addCall(this);
    }

    /* access modifiers changed from: protected */
    public void conferenceFocusChanged(boolean oldValue, boolean newValue) {
        try {
            reInvite();
        } catch (OperationFailedException ofe) {
            logger.info("Failed to re-INVITE this Call: " + this, ofe);
        } finally {
            CallSipImpl.super.conferenceFocusChanged(oldValue, newValue);
        }
    }

    public boolean contains(Dialog dialog) {
        return findCallPeer(dialog) != null;
    }

    private CallPeerSipImpl createCallPeerFor(Transaction containingTransaction, SipProvider sourceProvider) {
        CallPeerSipImpl callPeer = new CallPeerSipImpl(containingTransaction.getDialog().getRemoteParty(), this, containingTransaction, sourceProvider);
        addCallPeer(callPeer);
        boolean incomingCall = containingTransaction instanceof ServerTransaction;
        callPeer.setState(incomingCall ? CallPeerState.INCOMING_CALL : CallPeerState.INITIATING_CALL);
        if (getCallPeerCount() == 1) {
            Map<MediaType, MediaDirection> mediaDirections = new HashMap();
            mediaDirections.put(MediaType.AUDIO, MediaDirection.INACTIVE);
            mediaDirections.put(MediaType.VIDEO, MediaDirection.INACTIVE);
            boolean hasZrtp = false;
            boolean hasSdes = false;
            try {
                Request inviteReq = containingTransaction.getRequest();
                if (!(inviteReq == null || inviteReq.getRawContent() == null)) {
                    for (MediaDescription mediaDescription : SdpUtils.extractMediaDescriptions(SdpUtils.parseSdpString(SdpUtils.getContentAsString(inviteReq)))) {
                        mediaDirections.put(SdpUtils.getMediaType(mediaDescription), SdpUtils.getDirection(mediaDescription));
                        if (!hasZrtp) {
                            hasZrtp = mediaDescription.getAttribute("zrtp-hash") != null;
                        }
                        if (!hasSdes) {
                            Iterator i$ = mediaDescription.getAttributes(true).iterator();
                            while (i$.hasNext()) {
                                if (CryptoPacketExtension.ELEMENT_NAME.equals(((Attribute) i$.next()).getName())) {
                                    hasSdes = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (SdpParseException spe) {
                logger.error("Failed to parse SDP attribute", spe);
            } catch (Throwable t) {
                logger.warn("Error getting media types", t);
            }
            ((OperationSetBasicTelephonySipImpl) getParentOperationSet()).fireCallEvent(incomingCall ? 2 : 1, this, mediaDirections);
            if (hasZrtp) {
                ((CallPeerMediaHandlerSipImpl) callPeer.getMediaHandler()).addAdvertisedEncryptionMethod(SrtpControlType.ZRTP);
            }
            if (hasSdes) {
                ((CallPeerMediaHandlerSipImpl) callPeer.getMediaHandler()).addAdvertisedEncryptionMethod(SrtpControlType.SDES);
            }
        }
        return callPeer;
    }

    public CallPeerSipImpl findCallPeer(Dialog dialog) {
        Iterator<CallPeerSipImpl> callPeers = getCallPeers();
        if (logger.isTraceEnabled()) {
            logger.trace("Looking for peer with dialog: " + dialog + "among " + getCallPeerCount() + " calls");
        }
        while (callPeers.hasNext()) {
            CallPeerSipImpl cp = (CallPeerSipImpl) callPeers.next();
            if (cp.getDialog() == dialog) {
                if (!logger.isTraceEnabled()) {
                    return cp;
                }
                logger.trace("Returning cp=" + cp);
                return cp;
            } else if (logger.isTraceEnabled()) {
                logger.trace("Ignoring cp=" + cp + " because cp.dialog=" + cp.getDialog() + " while dialog=" + dialog);
            }
        }
        return null;
    }

    public ProtocolProviderServiceSipImpl getProtocolProvider() {
        return (ProtocolProviderServiceSipImpl) CallSipImpl.super.getProtocolProvider();
    }

    public CallPeerSipImpl invite(Address calleeAddress, Message cause) throws OperationFailedException {
        Request invite = this.messageFactory.createInviteRequest(calleeAddress, cause);
        ClientTransaction inviteTransaction = null;
        SipProvider jainSipProvider = getProtocolProvider().getDefaultJainSipProvider();
        try {
            inviteTransaction = jainSipProvider.getNewClientTransaction(invite);
        } catch (TransactionUnavailableException ex) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to create inviteTransaction.\nThis is most probably a network connection error.", 4, ex, logger);
        }
        CallPeerSipImpl callPeer = createCallPeerFor(inviteTransaction, jainSipProvider);
        CallPeerMediaHandlerSipImpl mediaHandler = (CallPeerMediaHandlerSipImpl) callPeer.getMediaHandler();
        mediaHandler.setLocalVideoTransmissionEnabled(this.localVideoAllowed);
        if (this.initialQualityPreferences != null) {
            mediaHandler.setSupportQualityControls(true);
            mediaHandler.getQualityControl().setRemoteSendMaxPreset(this.initialQualityPreferences);
        }
        try {
            callPeer.invite();
            return callPeer;
        } catch (OperationFailedException ex2) {
            callPeer.setState(CallPeerState.FAILED);
            throw ex2;
        }
    }

    public CallPeerSipImpl processInvite(SipProvider jainSipProvider, ServerTransaction serverTran) {
        Request invite = serverTran.getRequest();
        final CallPeerSipImpl peer = createCallPeerFor(serverTran, jainSipProvider);
        CallInfoHeader infoHeader = (CallInfoHeader) invite.getHeader("Call-Info");
        String alternativeIMPPAddress = null;
        if (!(infoHeader == null || infoHeader.getParameter("purpose") == null || !infoHeader.getParameter("purpose").equals("impp"))) {
            alternativeIMPPAddress = infoHeader.getInfo().toString();
        }
        if (alternativeIMPPAddress != null) {
            peer.setAlternativeIMPPAddress(alternativeIMPPAddress);
        }
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("will send ringing response: ");
            }
            Response response = this.messageFactory.createResponse(Response.RINGING, invite);
            serverTran.sendResponse(response);
            if ((serverTran instanceof SIPTransaction) && !((SIPTransaction) serverTran).isReliable()) {
                final Timer timer = new Timer();
                CallPeerAdapter stateListener = new CallPeerAdapter() {
                    public void peerStateChanged(CallPeerChangeEvent evt) {
                        if (!evt.getNewValue().equals(CallPeerState.INCOMING_CALL)) {
                            timer.cancel();
                            peer.removeCallPeerListener(this);
                        }
                    }
                };
                int interval = this.retransmitsRingingInterval;
                int delay = 0;
                for (int i = 0; i < 3; i++) {
                    delay += interval;
                    timer.schedule(new RingingResponseTask(response, serverTran, peer, timer, stateListener), (long) delay);
                    interval *= 2;
                }
                peer.addCallPeerListener(stateListener);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("sent a ringing response: " + response);
            }
        } catch (Exception ex) {
            logger.error("Error while trying to send a request", ex);
            peer.setState(CallPeerState.FAILED, "Internal Error: " + ex.getMessage());
        }
        return peer;
    }

    public void processReplacingInvite(SipProvider jainSipProvider, ServerTransaction serverTransaction, CallPeerSipImpl callPeerToReplace) {
        CallPeerSipImpl newCallPeer = createCallPeerFor(serverTransaction, jainSipProvider);
        try {
            newCallPeer.answer();
            try {
                callPeerToReplace.hangup();
            } catch (OperationFailedException ex) {
                logger.error("Failed to hangup the referer " + callPeerToReplace, ex);
                callPeerToReplace.setState(CallPeerState.FAILED, "Internal Error: " + ex);
            }
        } catch (OperationFailedException ex2) {
            logger.error("Failed to auto-answer the referred call peer " + newCallPeer, ex2);
        }
    }

    public void reInvite() throws OperationFailedException {
        Iterator<CallPeerSipImpl> peers = getCallPeers();
        while (peers.hasNext()) {
            ((CallPeerSipImpl) peers.next()).sendReInvite();
        }
    }

    public void setInitialQualityPreferences(QualityPreset qualityPreferences) {
        this.initialQualityPreferences = qualityPreferences;
    }
}
