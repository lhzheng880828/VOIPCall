package net.java.sip.communicator.impl.protocol.sip;

import java.net.InetSocketAddress;
import java.net.URL;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CoinPacketExtension;
import net.java.sip.communicator.impl.protocol.sip.sdp.SdpUtils;
import net.java.sip.communicator.service.protocol.CallPeerState;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetPresence;
import net.java.sip.communicator.service.protocol.media.AbstractOperationSetTelephonyConferencing;
import net.java.sip.communicator.service.protocol.media.MediaAwareCallPeer;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.header.ContentLength;
import org.jitsi.gov.nist.javax.sip.header.ContentType;
import org.jitsi.gov.nist.javax.sip.stack.SIPServerTransaction;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.DialogState;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.ServerTransaction;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.SipProvider;
import org.jitsi.javax.sip.Transaction;
import org.jitsi.javax.sip.TransactionUnavailableException;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.address.SipURI;
import org.jitsi.javax.sip.address.URI;
import org.jitsi.javax.sip.header.ContactHeader;
import org.jitsi.javax.sip.header.ContentLengthHeader;
import org.jitsi.javax.sip.header.ContentTypeHeader;
import org.jitsi.javax.sip.header.ReasonHeader;
import org.jitsi.javax.sip.message.Message;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.MediaStream;
import org.jitsi.service.neomedia.MediaType;

public class CallPeerSipImpl extends MediaAwareCallPeer<CallSipImpl, CallPeerMediaHandlerSipImpl, ProtocolProviderServiceSipImpl> {
    static final String PICTURE_FAST_UPDATE_CONTENT_SUB_TYPE = "media_control+xml";
    private static final Logger logger = Logger.getLogger(CallPeerSipImpl.class);
    private Dialog jainSipDialog = null;
    private SipProvider jainSipProvider = null;
    private Transaction latestInviteTransaction = null;
    private final SipMessageFactory messageFactory;
    private final List<MethodProcessorListener> methodProcessorListeners = new LinkedList();
    private Address peerAddress = null;
    private boolean sendPictureFastUpdate = "signaling".equals(SipActivator.getConfigurationService().getString("net.java.sip.communicator.impl.neomedia.codec.video.h264.preferredKeyFrameRequester", "rtcp"));
    private InetSocketAddress transportAddress = null;

    public CallPeerSipImpl(Address peerAddress, CallSipImpl owningCall, Transaction containingTransaction, SipProvider sourceProvider) {
        super(owningCall);
        this.peerAddress = peerAddress;
        this.messageFactory = ((ProtocolProviderServiceSipImpl) getProtocolProvider()).getMessageFactory();
        CallPeerSipImpl.super.setMediaHandler(new CallPeerMediaHandlerSipImpl(this) {
            /* access modifiers changed from: protected */
            public boolean requestKeyFrame() {
                return CallPeerSipImpl.this.requestKeyFrame();
            }
        });
        setDialog(containingTransaction.getDialog());
        setLatestInviteTransaction(containingTransaction);
        setJainSipProvider(sourceProvider);
    }

    public String getAddress() {
        SipURI sipURI = (SipURI) this.peerAddress.getURI();
        return sipURI.getUser() + Separators.AT + sipURI.getHost();
    }

    public String getURI() {
        return getPeerAddress().getURI().toString();
    }

    public Address getPeerAddress() {
        Dialog dialog = getDialog();
        if (dialog != null) {
            Address remoteParty = dialog.getRemoteParty();
            if (remoteParty != null) {
                this.peerAddress = remoteParty;
            }
        }
        return this.peerAddress;
    }

    public String getDisplayName() {
        String displayName = getPeerAddress().getDisplayName();
        if (displayName == null) {
            Contact contact = getContact();
            if (contact != null) {
                displayName = contact.getDisplayName();
            } else {
                URI peerURI = getPeerAddress().getURI();
                if (peerURI instanceof SipURI) {
                    String userName = ((SipURI) peerURI).getUser();
                    if (userName != null && userName.length() > 0) {
                        displayName = userName;
                    }
                }
                if (displayName == null) {
                    displayName = peerURI.toString();
                }
            }
        }
        if (displayName.startsWith("sip:")) {
            return displayName.substring(4);
        }
        return displayName;
    }

    public void setDisplayName(String displayName) {
        String oldName = getDisplayName();
        try {
            this.peerAddress.setDisplayName(displayName);
            fireCallPeerChangeEvent("CallPeerDisplayNameChange", oldName, displayName);
        } catch (ParseException ex) {
            logger.error(ex.getMessage(), ex);
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    public void setDialog(Dialog dialog) {
        this.jainSipDialog = dialog;
    }

    public Dialog getDialog() {
        return this.jainSipDialog;
    }

    public void setLatestInviteTransaction(Transaction transaction) {
        this.latestInviteTransaction = transaction;
    }

    public Transaction getLatestInviteTransaction() {
        return this.latestInviteTransaction;
    }

    public void setJainSipProvider(SipProvider jainSipProvider) {
        this.jainSipProvider = jainSipProvider;
    }

    public SipProvider getJainSipProvider() {
        return this.jainSipProvider;
    }

    public void setTransportAddress(InetSocketAddress transportAddress) {
        InetSocketAddress oldTransportAddress = this.transportAddress;
        this.transportAddress = transportAddress;
        fireCallPeerChangeEvent("CallPeerAddressChange", oldTransportAddress, transportAddress);
    }

    public Contact getContact() {
        if (getCall() == null) {
            return null;
        }
        OperationSetPresenceSipImpl opSetPresence = (OperationSetPresenceSipImpl) ((CallSipImpl) getCall()).getProtocolProvider().getOperationSet(OperationSetPresence.class);
        return opSetPresence != null ? opSetPresence.resolveContactID(getAddress()) : null;
    }

    public URL getCallInfoURL() {
        return ((CallPeerMediaHandlerSipImpl) getMediaHandler()).getCallInfoURL();
    }

    /* access modifiers changed from: 0000 */
    public void processPictureFastUpdate(ClientTransaction clientTransaction, Response response) {
        if (response.getStatusCode() != Response.OK && this.sendPictureFastUpdate) {
            this.sendPictureFastUpdate = false;
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean processPictureFastUpdate(ServerTransaction serverTransaction, Request request) throws OperationFailedException {
        CallPeerMediaHandlerSipImpl mediaHandler = (CallPeerMediaHandlerSipImpl) getMediaHandler();
        boolean requested = mediaHandler == null ? false : mediaHandler.processKeyFrameRequest();
        try {
            Response response = ((ProtocolProviderServiceSipImpl) getProtocolProvider()).getMessageFactory().createResponse(Response.OK, request);
            if (!requested) {
                try {
                    response.setContent("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n<media_control>\r\n<general_error>\r\nFailed to process picture_fast_update request.\r\n</general_error>\r\n</media_control>", new ContentType(SIPServerTransaction.CONTENT_TYPE_APPLICATION, PICTURE_FAST_UPDATE_CONTENT_SUB_TYPE));
                } catch (ParseException pe) {
                    throw new OperationFailedException("Failed to set content of OK Response.", 4, pe);
                }
            }
            try {
                serverTransaction.sendResponse(response);
                return true;
            } catch (Exception e) {
                throw new OperationFailedException("Failed to send OK Response.", 4, e);
            }
        } catch (ParseException pe2) {
            throw new OperationFailedException("Failed to create OK Response.", 4, pe2);
        }
    }

    public void processReInvite(ServerTransaction serverTransaction) {
        Request invite = serverTransaction.getRequest();
        setLatestInviteTransaction(serverTransaction);
        String sdpOffer = null;
        ContentLengthHeader cl = invite.getContentLength();
        if (cl != null && cl.getContentLength() > 0) {
            sdpOffer = SdpUtils.getContentAsString(invite);
        }
        try {
            String sdpAnswer;
            Response response = this.messageFactory.createResponse(Response.OK, invite);
            reflectConferenceFocus(response);
            if (sdpOffer != null) {
                sdpAnswer = ((CallPeerMediaHandlerSipImpl) getMediaHandler()).processOffer(sdpOffer);
            } else {
                sdpAnswer = ((CallPeerMediaHandlerSipImpl) getMediaHandler()).createOffer();
            }
            response.setContent(sdpAnswer, ((ProtocolProviderServiceSipImpl) getProtocolProvider()).getHeaderFactory().createContentTypeHeader(SIPServerTransaction.CONTENT_TYPE_APPLICATION, SIPServerTransaction.CONTENT_SUBTYPE_SDP));
            if (logger.isTraceEnabled()) {
                logger.trace("will send an OK response: " + response);
            }
            serverTransaction.sendResponse(response);
            if (logger.isDebugEnabled()) {
                logger.debug("OK response sent");
            }
            reevalRemoteHoldStatus();
            fireRequestProcessed(invite, response);
        } catch (Exception ex) {
            logger.error("Error while trying to send a response", ex);
            setState(CallPeerState.FAILED, "Internal Error: " + ex.getMessage());
            ((ProtocolProviderServiceSipImpl) getProtocolProvider()).sayErrorSilently(serverTransaction, 500);
        }
    }

    public void processBye(ServerTransaction byeTran) {
        boolean dialogIsAlive;
        Request byeRequest = byeTran.getRequest();
        Response ok = null;
        try {
            ok = this.messageFactory.createResponse(Response.OK, byeRequest);
        } catch (ParseException ex) {
            logger.error("Error while trying to send a response to a bye", ex);
        }
        if (ok != null) {
            try {
                byeTran.sendResponse(ok);
                if (logger.isDebugEnabled()) {
                    logger.debug("sent response " + ok);
                }
            } catch (Exception ex2) {
                logger.error("Failed to send an OK response to BYE request,exception was:\n", ex2);
            }
        }
        try {
            dialogIsAlive = EventPackageUtils.processByeThenIsDialogAlive(byeTran.getDialog());
        } catch (SipException ex3) {
            dialogIsAlive = false;
            logger.error("Failed to determine whether the dialog should stay alive.", ex3);
        }
        if (dialogIsAlive) {
            ((CallPeerMediaHandlerSipImpl) getMediaHandler()).close();
            return;
        }
        ReasonHeader reasonHeader = (ReasonHeader) byeRequest.getHeader("Reason");
        if (reasonHeader != null) {
            setState(CallPeerState.DISCONNECTED, reasonHeader.getText(), reasonHeader.getCause());
        } else {
            setState(CallPeerState.DISCONNECTED);
        }
    }

    public void processCancel(ServerTransaction serverTransaction) {
        Request cancel = serverTransaction.getRequest();
        try {
            Response ok = this.messageFactory.createResponse(Response.OK, cancel);
            serverTransaction.sendResponse(ok);
            if (logger.isDebugEnabled()) {
                logger.debug("sent an ok response to a CANCEL request:\n" + ok);
            }
            try {
                Transaction tran = getLatestInviteTransaction();
                if (tran instanceof ServerTransaction) {
                    ServerTransaction inviteTran = (ServerTransaction) tran;
                    Response requestTerminated = this.messageFactory.createResponse(Response.REQUEST_TERMINATED, getLatestInviteTransaction().getRequest());
                    inviteTran.sendResponse(requestTerminated);
                    if (logger.isDebugEnabled()) {
                        logger.debug("sent request terminated response:\n" + requestTerminated);
                    }
                    ReasonHeader reasonHeader = (ReasonHeader) cancel.getHeader("Reason");
                    if (reasonHeader != null) {
                        setState(CallPeerState.DISCONNECTED, reasonHeader.getText(), reasonHeader.getCause());
                        return;
                    } else {
                        setState(CallPeerState.DISCONNECTED);
                        return;
                    }
                }
                logger.error("Received a misplaced CANCEL request!");
            } catch (ParseException ex) {
                logger.error("Failed to create a REQUEST_TERMINATED Response to an INVITE request.", ex);
            } catch (Exception ex2) {
                logger.error("Failed to send an REQUEST_TERMINATED Response to an INVITE request.", ex2);
            }
        } catch (ParseException ex3) {
            logAndFail("Failed to create an OK Response to a CANCEL.", ex3);
        } catch (Exception ex22) {
            logAndFail("Failed to send an OK Response to a CANCEL.", ex22);
        }
    }

    public void processAck(ServerTransaction serverTransaction, Request ack) {
        ContentLengthHeader contentLength = ack.getContentLength();
        if (contentLength != null && contentLength.getContentLength() > 0) {
            try {
                ((CallPeerMediaHandlerSipImpl) getMediaHandler()).processAnswer(SdpUtils.getContentAsString(ack));
            } catch (Exception exc) {
                logAndFail("There was an error parsing the SDP description of " + getDisplayName() + Separators.LPAREN + getAddress() + Separators.RPAREN, exc);
                return;
            }
        }
        if (!CallPeerState.isOnHold(getState())) {
            setState(CallPeerState.CONNECTED);
            ((CallPeerMediaHandlerSipImpl) getMediaHandler()).start();
            if (getCall() != null && isMute() != ((CallSipImpl) getCall()).isMute()) {
                setMute(((CallSipImpl) getCall()).isMute());
            }
        }
    }

    public void processSessionProgress(ClientTransaction tran, Response response) {
        if (response.getContentLength().getContentLength() != 0) {
            ContentTypeHeader contentTypeHeader = (ContentTypeHeader) response.getHeader("Content-Type");
            if (contentTypeHeader.getContentType().equalsIgnoreCase(SIPServerTransaction.CONTENT_TYPE_APPLICATION) && contentTypeHeader.getContentSubType().equalsIgnoreCase(SIPServerTransaction.CONTENT_SUBTYPE_SDP)) {
                try {
                    ((CallPeerMediaHandlerSipImpl) getMediaHandler()).processAnswer(SdpUtils.getContentAsString(response));
                    setState(CallPeerState.CONNECTING_WITH_EARLY_MEDIA);
                    ((CallPeerMediaHandlerSipImpl) getMediaHandler()).start();
                    setMute(true);
                    return;
                } catch (Exception exc) {
                    logAndFail("There was an error parsing the SDP description of " + getDisplayName() + Separators.LPAREN + getAddress() + Separators.RPAREN, exc);
                    return;
                }
            }
            logger.warn("Ignoring invite 183 since call peer is already exchanging early media.");
        } else if (logger.isDebugEnabled()) {
            logger.debug("Ignoring a 183 with no content");
        }
    }

    public void processInviteOK(ClientTransaction clientTransaction, Response ok) {
        try {
            ((ProtocolProviderServiceSipImpl) getProtocolProvider()).sendAck(clientTransaction);
            try {
                if (!CallPeerState.CONNECTING_WITH_EARLY_MEDIA.equals(getState())) {
                    ((CallPeerMediaHandlerSipImpl) getMediaHandler()).processAnswer(SdpUtils.getContentAsString(ok));
                }
                if (!CallPeerState.isOnHold(getState())) {
                    setState(CallPeerState.CONNECTED);
                    ((CallPeerMediaHandlerSipImpl) getMediaHandler()).start();
                    if (isMute() != ((CallSipImpl) getCall()).isMute()) {
                        setMute(((CallSipImpl) getCall()).isMute());
                    }
                }
                fireResponseProcessed(ok, null);
            } catch (Exception exc) {
                logger.error("There was an error parsing the SDP description of " + getDisplayName() + Separators.LPAREN + getAddress() + Separators.RPAREN, exc);
                try {
                    setState(CallPeerState.CONNECTED, "Error:" + exc.getLocalizedMessage());
                    hangup();
                } catch (Exception e) {
                } finally {
                    logAndFail("Remote party sent a faulty session description.", exc);
                }
            }
        } catch (InvalidArgumentException ex) {
            logAndFail("Error creating an ACK (CSeq?)", ex);
        } catch (SipException ex2) {
            logAndFail("Failed to create ACK request!", ex2);
        }
    }

    private void pictureFastUpdate() throws OperationFailedException {
        Request info = ((ProtocolProviderServiceSipImpl) getProtocolProvider()).getMessageFactory().createRequest(getDialog(), Request.INFO);
        ContentType ct = new ContentType(SIPServerTransaction.CONTENT_TYPE_APPLICATION, PICTURE_FAST_UPDATE_CONTENT_SUB_TYPE);
        String content = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n<media_control>\r\n<vc_primitive>\r\n<to_encoder>\r\n<picture_fast_update/>\r\n</to_encoder>\r\n</vc_primitive>\r\n</media_control>";
        info.setContentLength(new ContentLength(content.length()));
        try {
            info.setContent(content.getBytes(), ct);
            try {
                ClientTransaction clientTransaction = getJainSipProvider().getNewClientTransaction(info);
                try {
                    if (getDialog().getState() == DialogState.TERMINATED) {
                        logger.warn("Trying to send a dtmf tone inside a TERMINATED dialog.");
                        return;
                    }
                    getDialog().sendRequest(clientTransaction);
                    if (logger.isDebugEnabled()) {
                        logger.debug("sent request:\n" + info);
                    }
                } catch (SipException ex) {
                    throw new OperationFailedException("Failed to send the INFO request", 2, ex);
                }
            } catch (TransactionUnavailableException ex2) {
                logger.error("Failed to construct a client transaction from the INFO request", ex2);
                throw new OperationFailedException("Failed to construct a client transaction from the INFO request", 4, ex2);
            }
        } catch (ParseException ex3) {
            logger.error("Failed to construct the INFO request", ex3);
            throw new OperationFailedException("Failed to construct a client the INFO request", 4, ex3);
        }
    }

    public void hangup() throws OperationFailedException {
        hangup(Response.OK, null);
    }

    public void hangup(int reasonCode, String reason) throws OperationFailedException {
        if (!CallPeerState.DISCONNECTED.equals(getState()) && !CallPeerState.FAILED.equals(getState())) {
            boolean failed = reasonCode != Response.OK;
            CallPeerState peerState = getState();
            if (peerState.equals(CallPeerState.CONNECTED) || CallPeerState.isOnHold(peerState)) {
                try {
                    if (!sayBye(reasonCode, reason)) {
                        setDisconnectedState(failed, reason);
                    }
                } catch (Throwable ex) {
                    logger.error("Error while trying to hangup, trying to handle!", ex);
                    setDisconnectedState(true, null);
                    if (ex instanceof OperationFailedException) {
                        OperationFailedException ex2 = (OperationFailedException) ex;
                    }
                }
            } else if (CallPeerState.CONNECTING.equals(getState()) || CallPeerState.CONNECTING_WITH_EARLY_MEDIA.equals(getState()) || CallPeerState.ALERTING_REMOTE_SIDE.equals(getState())) {
                if (getLatestInviteTransaction() != null) {
                    sayCancel();
                }
                setDisconnectedState(failed, reason);
            } else if (peerState.equals(CallPeerState.INCOMING_CALL)) {
                setDisconnectedState(failed, reason);
                sayBusyHere();
            } else if (peerState.equals(CallPeerState.BUSY)) {
                setDisconnectedState(failed, reason);
            } else if (peerState.equals(CallPeerState.FAILED)) {
                setDisconnectedState(failed, reason);
            } else {
                setDisconnectedState(failed, reason);
                logger.error("Could not determine call peer state!");
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("Ignoring a request to hangup a call peer that is already DISCONNECTED");
        }
    }

    private void sayBusyHere() throws OperationFailedException {
        if (getLatestInviteTransaction() instanceof ServerTransaction) {
            Response busyHere = null;
            try {
                busyHere = this.messageFactory.createResponse(Response.BUSY_HERE, getLatestInviteTransaction().getRequest());
            } catch (ParseException ex) {
                ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to create the BUSY_HERE response!", 4, ex, logger);
            }
            try {
                ((ServerTransaction) getLatestInviteTransaction()).sendResponse(busyHere);
                if (logger.isDebugEnabled()) {
                    logger.debug("sent response:\n" + busyHere);
                    return;
                }
                return;
            } catch (Exception ex2) {
                ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to send the BUSY_HERE response", 2, ex2, logger);
                return;
            }
        }
        logger.error("Cannot send BUSY_HERE in a client transaction");
        throw new OperationFailedException("Cannot send BUSY_HERE in a client transaction", 4);
    }

    private void sayCancel() throws OperationFailedException {
        if (getLatestInviteTransaction() instanceof ServerTransaction) {
            logger.error("Cannot cancel a server transaction");
            throw new OperationFailedException("Cannot cancel a server transaction", 4);
        }
        try {
            Request cancel = ((ClientTransaction) getLatestInviteTransaction()).createCancel();
            getJainSipProvider().getNewClientTransaction(cancel).sendRequest();
            if (logger.isDebugEnabled()) {
                logger.debug("sent request:\n" + cancel);
            }
        } catch (SipException ex) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to send the CANCEL request", 2, ex, logger);
        }
    }

    private boolean sayBye(int reasonCode, String reason) throws OperationFailedException {
        Dialog dialog = getDialog();
        Request bye = this.messageFactory.createRequest(dialog, "BYE");
        if (!(reasonCode == Response.OK || reason == null)) {
            int sipCode = convertReasonCodeToSIPCode(reasonCode);
            if (sipCode != -1) {
                try {
                    bye.setHeader(((ProtocolProviderServiceSipImpl) getProtocolProvider()).getHeaderFactory().createReasonHeader("SIP", sipCode, reason));
                } catch (Throwable e) {
                    logger.error("Cannot set reason header", e);
                }
            }
        }
        ((ProtocolProviderServiceSipImpl) getProtocolProvider()).sendInDialogRequest(getJainSipProvider(), bye, dialog);
        try {
            return EventPackageUtils.processByeThenIsDialogAlive(dialog);
        } catch (SipException ex) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to determine whether the dialog should stay alive.", 4, ex, logger);
            return false;
        }
    }

    private static int convertReasonCodeToSIPCode(int reasonCode) {
        switch (reasonCode) {
            case Response.OK /*200*/:
                return Response.ACCEPTED;
            case Response.REQUEST_TIMEOUT /*408*/:
                return Response.REQUEST_TIMEOUT;
            case Response.BUSY_HERE /*486*/:
                return Response.BUSY_HERE;
            case 609:
                return Response.SESSION_NOT_ACCEPTABLE;
            default:
                return -1;
        }
    }

    public synchronized void answer() throws OperationFailedException {
        Transaction transaction = getLatestInviteTransaction();
        if (transaction == null || !(transaction instanceof ServerTransaction)) {
            setState(CallPeerState.DISCONNECTED);
            throw new OperationFailedException("Failed to extract a ServerTransaction from the call's associated dialog!", 4);
        }
        CallPeerState peerState = getState();
        if (!peerState.equals(CallPeerState.CONNECTED) && !CallPeerState.isOnHold(peerState)) {
            ServerTransaction serverTransaction = (ServerTransaction) transaction;
            Request invite = serverTransaction.getRequest();
            Response ok = null;
            try {
                ok = this.messageFactory.createResponse(Response.OK, invite);
                reflectConferenceFocus(ok);
            } catch (ParseException ex) {
                setState(CallPeerState.DISCONNECTED);
                ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to construct an OK response to an INVITE request", 4, ex, logger);
            }
            ContentTypeHeader contentTypeHeader = null;
            try {
                contentTypeHeader = ((ProtocolProviderServiceSipImpl) getProtocolProvider()).getHeaderFactory().createContentTypeHeader(SIPServerTransaction.CONTENT_TYPE_APPLICATION, SIPServerTransaction.CONTENT_SUBTYPE_SDP);
            } catch (ParseException ex2) {
                setState(CallPeerState.DISCONNECTED);
                ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to create a content type header for the OK response", 4, ex2, logger);
            }
            String sdpOffer = null;
            try {
                String sdp;
                ContentLengthHeader cl = invite.getContentLength();
                if (cl != null && cl.getContentLength() > 0) {
                    sdpOffer = SdpUtils.getContentAsString(invite);
                }
                if (sdpOffer == null || sdpOffer.length() <= 0) {
                    sdp = ((CallPeerMediaHandlerSipImpl) getMediaHandler()).createOffer();
                } else {
                    sdp = ((CallPeerMediaHandlerSipImpl) getMediaHandler()).processOffer(sdpOffer);
                }
                ok.setContent(sdp, contentTypeHeader);
                try {
                    serverTransaction.sendResponse(ok);
                    if (logger.isDebugEnabled()) {
                        logger.debug("sent response\n" + ok);
                    }
                } catch (Exception ex3) {
                    setState(CallPeerState.DISCONNECTED);
                    ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to send an OK response to an INVITE request", 2, ex3, logger);
                }
                fireRequestProcessed(invite, ok);
                if (CallPeerState.INCOMING_CALL.equals(getState())) {
                    if (sdpOffer == null || sdpOffer.length() <= 0) {
                        setState(CallPeerState.CONNECTING_INCOMING_CALL);
                    } else {
                        setState(CallPeerState.CONNECTING_INCOMING_CALL_WITH_MEDIA);
                    }
                }
            } catch (Exception ex32) {
                logger.error("Failed to create an SDP description for an OK response to an INVITE request!", ex32);
                ((ProtocolProviderServiceSipImpl) getProtocolProvider()).sayError(serverTransaction, Response.NOT_ACCEPTABLE_HERE);
                setState(CallPeerState.FAILED, ex32.getMessage());
            }
        } else if (logger.isInfoEnabled()) {
            logger.info("Ignoring user request to answer a CallPeer that is already connected. CP:");
        }
    }

    public void putOnHold(boolean onHold) throws OperationFailedException {
        CallPeerMediaHandlerSipImpl mediaHandler = (CallPeerMediaHandlerSipImpl) getMediaHandler();
        mediaHandler.setLocallyOnHold(onHold);
        try {
            sendReInvite(mediaHandler.createOffer());
        } catch (Exception ex) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to create SDP offer to hold.", 4, ex, logger);
        }
        reevalLocalHoldStatus();
    }

    /* access modifiers changed from: 0000 */
    public void sendReInvite() throws OperationFailedException {
        sendReInvite(((CallPeerMediaHandlerSipImpl) getMediaHandler()).createOffer());
    }

    private void sendReInvite(String sdpOffer) throws OperationFailedException {
        Dialog dialog = getDialog();
        Request invite = this.messageFactory.createRequest(dialog, "INVITE");
        try {
            invite.setContent(sdpOffer, ((ProtocolProviderServiceSipImpl) getProtocolProvider()).getHeaderFactory().createContentTypeHeader(SIPServerTransaction.CONTENT_TYPE_APPLICATION, SIPServerTransaction.CONTENT_SUBTYPE_SDP));
            reflectConferenceFocus(invite);
        } catch (ParseException ex) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to parse SDP offer for the new invite.", 4, ex, logger);
        }
        ((ProtocolProviderServiceSipImpl) getProtocolProvider()).sendInDialogRequest(getJainSipProvider(), invite, dialog);
    }

    public void invite() throws OperationFailedException {
        try {
            ClientTransaction inviteTran = (ClientTransaction) getLatestInviteTransaction();
            Request invite = inviteTran.getRequest();
            invite.setContent(((CallPeerMediaHandlerSipImpl) getMediaHandler()).createOffer(), ((ProtocolProviderServiceSipImpl) getProtocolProvider()).getHeaderFactory().createContentTypeHeader(SIPServerTransaction.CONTENT_TYPE_APPLICATION, SIPServerTransaction.CONTENT_SUBTYPE_SDP));
            reflectConferenceFocus(invite);
            inviteTran.sendRequest();
            if (logger.isDebugEnabled()) {
                logger.debug("sent request:\n" + inviteTran.getRequest());
            }
        } catch (Exception ex) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("An error occurred while sending invite request", 2, ex, logger);
        }
    }

    private void reflectConferenceFocus(Message message) throws ParseException {
        ContactHeader contactHeader = (ContactHeader) message.getHeader("Contact");
        if (contactHeader == null) {
            return;
        }
        if (((CallSipImpl) getCall()).isConferenceFocus()) {
            contactHeader.setParameter(CoinPacketExtension.ISFOCUS_ATTR_NAME, null);
        } else {
            contactHeader.removeParameter(CoinPacketExtension.ISFOCUS_ATTR_NAME);
        }
    }

    /* access modifiers changed from: 0000 */
    public void addMethodProcessorListener(MethodProcessorListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        synchronized (this.methodProcessorListeners) {
            if (!this.methodProcessorListeners.contains(listener)) {
                this.methodProcessorListeners.add(listener);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void fireRequestProcessed(Request request, Response response) {
        synchronized (this.methodProcessorListeners) {
            Iterable<MethodProcessorListener> listeners = new LinkedList(this.methodProcessorListeners);
        }
        for (MethodProcessorListener listener : listeners) {
            listener.requestProcessed(this, request, response);
        }
    }

    /* access modifiers changed from: protected */
    public void fireResponseProcessed(Response response, Request request) {
        synchronized (this.methodProcessorListeners) {
            Iterable<MethodProcessorListener> listeners = new LinkedList(this.methodProcessorListeners);
        }
        for (MethodProcessorListener listener : listeners) {
            listener.responseProcessed(this, response, request);
        }
    }

    /* access modifiers changed from: 0000 */
    public void removeMethodProcessorListener(MethodProcessorListener listener) {
        if (listener != null) {
            synchronized (this.methodProcessorListeners) {
                this.methodProcessorListeners.remove(listener);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean requestKeyFrame() {
        if (!this.sendPictureFastUpdate) {
            return false;
        }
        try {
            pictureFastUpdate();
            return true;
        } catch (OperationFailedException e) {
            return false;
        }
    }

    public void handleAuthenticationChallenge(ClientTransaction retryTran) {
        setDialog(retryTran.getDialog());
        setLatestInviteTransaction(retryTran);
        setJainSipProvider(this.jainSipProvider);
    }

    private void setDisconnectedState(boolean failed, String reason) {
        if (failed) {
            setState(CallPeerState.FAILED, reason);
        } else {
            setState(CallPeerState.DISCONNECTED, reason);
        }
    }

    public String getEntity() {
        return AbstractOperationSetTelephonyConferencing.stripParametersFromAddress(getURI());
    }

    public MediaDirection getDirection(MediaType mediaType) {
        MediaStream stream = ((CallPeerMediaHandlerSipImpl) getMediaHandler()).getStream(mediaType);
        if (stream == null) {
            return MediaDirection.INACTIVE;
        }
        MediaDirection direction = stream.getDirection();
        if (direction == null) {
            return MediaDirection.INACTIVE;
        }
        return direction;
    }
}
