package net.java.sip.communicator.impl.protocol.sip;

import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Iterator;
import javax.sdp.SdpConstants;
import net.java.sip.communicator.service.protocol.Call;
import net.java.sip.communicator.service.protocol.CallConference;
import net.java.sip.communicator.service.protocol.CallPeer;
import net.java.sip.communicator.service.protocol.CallPeerState;
import net.java.sip.communicator.service.protocol.CallState;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetAdvancedTelephony;
import net.java.sip.communicator.service.protocol.OperationSetBasicAutoAnswer;
import net.java.sip.communicator.service.protocol.OperationSetPersistentPresence;
import net.java.sip.communicator.service.protocol.OperationSetSecureSDesTelephony;
import net.java.sip.communicator.service.protocol.OperationSetSecureZrtpTelephony;
import net.java.sip.communicator.service.protocol.TransferAuthority;
import net.java.sip.communicator.service.protocol.event.CallChangeAdapter;
import net.java.sip.communicator.service.protocol.event.CallChangeListener;
import net.java.sip.communicator.service.protocol.media.AbstractOperationSetBasicTelephony;
import net.java.sip.communicator.service.protocol.media.MediaAwareCallPeer;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.header.HeaderFactoryImpl;
import org.jitsi.gov.nist.javax.sip.header.extensions.ReferencesHeader;
import org.jitsi.gov.nist.javax.sip.header.extensions.Replaces;
import org.jitsi.gov.nist.javax.sip.header.extensions.ReplacesHeader;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.DialogState;
import org.jitsi.javax.sip.DialogTerminatedEvent;
import org.jitsi.javax.sip.IOExceptionEvent;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.ResponseEvent;
import org.jitsi.javax.sip.ServerTransaction;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.SipProvider;
import org.jitsi.javax.sip.TimeoutEvent;
import org.jitsi.javax.sip.TransactionAlreadyExistsException;
import org.jitsi.javax.sip.TransactionTerminatedEvent;
import org.jitsi.javax.sip.TransactionUnavailableException;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.address.SipURI;
import org.jitsi.javax.sip.header.CSeqHeader;
import org.jitsi.javax.sip.header.ContactHeader;
import org.jitsi.javax.sip.header.ContentTypeHeader;
import org.jitsi.javax.sip.header.EventHeader;
import org.jitsi.javax.sip.header.FromHeader;
import org.jitsi.javax.sip.header.HeaderFactory;
import org.jitsi.javax.sip.header.ReferToHeader;
import org.jitsi.javax.sip.header.SubscriptionStateHeader;
import org.jitsi.javax.sip.header.WarningHeader;
import org.jitsi.javax.sip.message.Message;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;

public class OperationSetBasicTelephonySipImpl extends AbstractOperationSetBasicTelephony<ProtocolProviderServiceSipImpl> implements MethodProcessor, OperationSetAdvancedTelephony<ProtocolProviderServiceSipImpl>, OperationSetSecureZrtpTelephony, OperationSetSecureSDesTelephony {
    private static final Logger logger = Logger.getLogger(OperationSetBasicTelephonySipImpl.class);
    private final ActiveCallsRepositorySipImpl activeCallsRepository = new ActiveCallsRepositorySipImpl(this);
    private final SipMessageFactory messageFactory;
    private final ProtocolProviderServiceSipImpl protocolProvider;
    private TransferAuthority transferAuthority = null;

    public OperationSetBasicTelephonySipImpl(ProtocolProviderServiceSipImpl protocolProvider) {
        this.protocolProvider = protocolProvider;
        this.messageFactory = protocolProvider.getMessageFactory();
        protocolProvider.registerMethodProcessor("INVITE", this);
        protocolProvider.registerMethodProcessor(Request.CANCEL, this);
        protocolProvider.registerMethodProcessor("ACK", this);
        protocolProvider.registerMethodProcessor("BYE", this);
        protocolProvider.registerMethodProcessor(Request.REFER, this);
        protocolProvider.registerMethodProcessor("NOTIFY", this);
        protocolProvider.registerEvent(ReferencesHeader.REFER);
    }

    public Call createCall(String callee, CallConference conference) throws OperationFailedException, ParseException {
        return createOutgoingCall(this.protocolProvider.parseAddressString(callee), null, conference);
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized CallSipImpl createOutgoingCall() throws OperationFailedException {
        assertRegistered();
        return new CallSipImpl(this);
    }

    private synchronized CallSipImpl createOutgoingCall(Address calleeAddress, Message cause, CallConference conference) throws OperationFailedException {
        CallSipImpl call;
        call = createOutgoingCall();
        if (logger.isInfoEnabled()) {
            logger.info("Creating outgoing call to " + calleeAddress);
        }
        if (conference != null) {
            call.setConference(conference);
        }
        call.invite(calleeAddress, cause);
        return call;
    }

    public Iterator<CallSipImpl> getActiveCalls() {
        return this.activeCallsRepository.getActiveCalls();
    }

    /* access modifiers changed from: protected */
    public ActiveCallsRepositorySipImpl getActiveCallsRepository() {
        return this.activeCallsRepository;
    }

    public void putOffHold(CallPeer peer) throws OperationFailedException {
        putOnHold(peer, false);
    }

    public void putOnHold(CallPeer peer) throws OperationFailedException {
        putOnHold(peer, true);
    }

    private synchronized void putOnHold(CallPeer peer, boolean on) throws OperationFailedException {
        ((CallPeerSipImpl) peer).putOnHold(on);
    }

    public boolean processRequest(RequestEvent requestEvent) {
        ServerTransaction serverTransaction = requestEvent.getServerTransaction();
        SipProvider jainSipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        String requestMethod = request.getMethod();
        if (serverTransaction == null) {
            try {
                serverTransaction = SipStackSharing.getOrCreateServerTransaction(requestEvent);
            } catch (TransactionAlreadyExistsException ex) {
                logger.error("Failed to create a new servertransaction for an incoming request\n(Next message contains the request)", ex);
                return false;
            } catch (TransactionUnavailableException ex2) {
                logger.error("Failed to create a new servertransaction for an incoming request\n(Next message contains the request)", ex2);
                return false;
            }
        }
        if (requestMethod.equals("INVITE")) {
            if (logger.isDebugEnabled()) {
                logger.debug("received INVITE");
            }
            DialogState dialogState = serverTransaction.getDialog().getState();
            if (dialogState == null || dialogState.equals(DialogState.CONFIRMED)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("request is an INVITE. Dialog state=" + dialogState);
                }
                processInvite(jainSipProvider, serverTransaction);
                return true;
            }
            logger.error("reINVITEs while the dialog is not confirmed are not currently supported.");
            return false;
        } else if (requestMethod.equals("ACK")) {
            processAck(serverTransaction, request);
            return true;
        } else if (requestMethod.equals("BYE")) {
            processBye(serverTransaction, request);
            return true;
        } else if (requestMethod.equals(Request.CANCEL)) {
            processCancel(serverTransaction, request);
            return true;
        } else if (requestMethod.equals(Request.REFER)) {
            if (logger.isDebugEnabled()) {
                logger.debug("received REFER");
            }
            processRefer(serverTransaction, request, jainSipProvider);
            return true;
        } else if (!requestMethod.equals("NOTIFY")) {
            return false;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("received NOTIFY");
            }
            return processNotify(serverTransaction, request);
        }
    }

    public boolean processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        return false;
    }

    public boolean processResponse(ResponseEvent responseEvent) {
        ClientTransaction clientTransaction = responseEvent.getClientTransaction();
        Response response = responseEvent.getResponse();
        CSeqHeader cseq = (CSeqHeader) response.getHeader("CSeq");
        if (cseq == null) {
            logger.error("An incoming response did not contain a CSeq header");
        }
        String method = cseq.getMethod();
        SipProvider sourceProvider = (SipProvider) responseEvent.getSource();
        int responseStatusCode = response.getStatusCode();
        boolean processed = false;
        CallPeerSipImpl callPeer;
        switch (responseStatusCode) {
            case Response.TRYING /*100*/:
                processTrying(clientTransaction, response);
                processed = true;
                break;
            case Response.RINGING /*180*/:
                processRinging(clientTransaction, response);
                processed = true;
                break;
            case Response.SESSION_PROGRESS /*183*/:
                processSessionProgress(clientTransaction, response);
                processed = true;
                break;
            case Response.OK /*200*/:
                if (method.equals("INVITE")) {
                    processInviteOK(clientTransaction, response);
                    processed = true;
                    break;
                }
                break;
            case Response.ACCEPTED /*202*/:
                if (Request.REFER.equals(method)) {
                    processReferAccepted(clientTransaction, response);
                    processed = true;
                    break;
                }
                break;
            case 301:
            case 302:
                callPeer = this.activeCallsRepository.findCallPeer(clientTransaction.getDialog());
                if (callPeer != null) {
                    ContactHeader contactHeader = (ContactHeader) response.getHeader("Contact");
                    if (contactHeader != null) {
                        Address redirectAddress = contactHeader.getAddress();
                        if (!callPeer.getPeerAddress().getURI().equals(redirectAddress.getURI())) {
                            try {
                                ((CallSipImpl) callPeer.getCall()).invite(redirectAddress, null);
                            } catch (OperationFailedException exc) {
                                logger.error("Call forward failed for address " + contactHeader.getAddress(), exc);
                                callPeer.setState(CallPeerState.DISCONNECTED, "Call forwarded failed. " + exc.getMessage());
                            }
                            callPeer.setState(CallPeerState.DISCONNECTED, "Call forwarded. " + response.getReasonPhrase());
                            processed = true;
                            break;
                        }
                        logger.error("Redirect loop detected for: " + callPeer.getPeerAddress().getURI());
                        callPeer.setState(CallPeerState.FAILED, "Redirect loop detected for: " + callPeer.getPeerAddress().getURI());
                        return true;
                    }
                    logger.error("Received a forward with no Contact destination: " + response.getStatusCode() + Separators.SP + response.getReasonPhrase());
                    callPeer.setState(CallPeerState.FAILED, response.getReasonPhrase());
                    return true;
                }
                logger.error("Failed to find a forwarded call peer.");
                return true;
            case Response.UNAUTHORIZED /*401*/:
            case Response.PROXY_AUTHENTICATION_REQUIRED /*407*/:
                processAuthenticationChallenge(clientTransaction, response, sourceProvider);
                processed = true;
                break;
            case Response.BUSY_HERE /*486*/:
            case Response.BUSY_EVERYWHERE /*600*/:
            case Response.DECLINE /*603*/:
                processBusyHere(clientTransaction, response);
                processed = true;
                break;
            case Response.REQUEST_TERMINATED /*487*/:
                callPeer = this.activeCallsRepository.findCallPeer(clientTransaction.getDialog());
                if (callPeer != null) {
                    String reasonPhrase = response.getReasonPhrase();
                    if (reasonPhrase == null || reasonPhrase.trim().length() == 0) {
                        reasonPhrase = "Request terminated by server!";
                    }
                    callPeer.setState(CallPeerState.FAILED, reasonPhrase);
                }
                processed = true;
                break;
            default:
                int responseStatusCodeRange = responseStatusCode / 100;
                Request request = responseEvent.getClientTransaction().getRequest();
                if (responseStatusCode != 500 || !isRemoteControlNotification(request)) {
                    callPeer = this.activeCallsRepository.findCallPeer(clientTransaction.getDialog());
                    if (responseStatusCodeRange != 4 && responseStatusCodeRange != 5 && responseStatusCodeRange != 6) {
                        if (responseStatusCodeRange == 2 || responseStatusCodeRange == 3) {
                            logger.error("Received an non-supported final response: " + response.getStatusCode() + Separators.SP + response.getReasonPhrase());
                            if (callPeer != null) {
                                callPeer.setState(CallPeerState.FAILED, response.getReasonPhrase());
                            }
                            processed = true;
                            break;
                        }
                    }
                    String reason = response.getReasonPhrase();
                    WarningHeader warningHeader = (WarningHeader) response.getHeader("Warning");
                    if (warningHeader != null) {
                        reason = warningHeader.getText();
                        logger.error("Received error: " + response.getStatusCode() + Separators.SP + response.getReasonPhrase() + Separators.SP + warningHeader.getText() + "-" + warningHeader.getAgent() + "-" + warningHeader.getName());
                    } else {
                        logger.error("Received error: " + response.getStatusCode() + Separators.SP + response.getReasonPhrase());
                    }
                    if (callPeer != null) {
                        callPeer.setState(CallPeerState.FAILED, reason);
                    }
                    processed = true;
                    break;
                }
                return true;
                break;
        }
        return processed;
    }

    private void processReferAccepted(ClientTransaction clientTransaction, Response accepted) {
        try {
            EventPackageUtils.addSubscription(clientTransaction.getDialog(), ReferencesHeader.REFER);
        } catch (SipException ex) {
            logger.error("Failed to make Accepted REFER response keep the dialog alive after BYE:\n" + accepted, ex);
        }
    }

    private void processTrying(ClientTransaction clientTransaction, Response response) {
        CallPeerSipImpl callPeer = this.activeCallsRepository.findCallPeer(clientTransaction.getDialog());
        if (callPeer != null) {
            CallPeerState callPeerState = callPeer.getState();
            if (!CallPeerState.CONNECTED.equals(callPeerState) && !CallPeerState.isOnHold(callPeerState)) {
                callPeer.setState(CallPeerState.CONNECTING);
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("Received a stray trying response.");
        }
    }

    private void processRinging(ClientTransaction clientTransaction, Response response) {
        CallPeerSipImpl callPeer = this.activeCallsRepository.findCallPeer(clientTransaction.getDialog());
        if (callPeer != null) {
            ContactHeader remotePartyContactHeader = (ContactHeader) response.getHeader("Contact");
            if (remotePartyContactHeader != null) {
                String displayName = remotePartyContactHeader.getAddress().getDisplayName();
                if (displayName != null && displayName.trim().length() > 0) {
                    callPeer.setDisplayName(displayName);
                }
            }
            callPeer.setState(CallPeerState.ALERTING_REMOTE_SIDE);
        } else if (logger.isDebugEnabled()) {
            logger.debug("Received a stray trying response.");
        }
    }

    private void processSessionProgress(ClientTransaction tran, Response response) {
        CallPeerSipImpl callPeer = this.activeCallsRepository.findCallPeer(tran.getDialog());
        if (callPeer.getState() == CallPeerState.CONNECTING_WITH_EARLY_MEDIA) {
            logger.warn("Ignoring invite 183 since call peer is already exchanging early media.");
        } else {
            callPeer.processSessionProgress(tran, response);
        }
    }

    private void processInviteOK(ClientTransaction clientTransaction, Response ok) {
        Dialog dialog = clientTransaction.getDialog();
        CallPeerSipImpl callPeer = this.activeCallsRepository.findCallPeer(dialog);
        if (callPeer == null) {
            callPeer = this.activeCallsRepository.findCallPeer(clientTransaction.getBranchId(), ok.getHeader("Call-ID"));
            if (callPeer != null) {
                callPeer.setDialog(dialog);
            } else if (logger.isDebugEnabled()) {
                logger.debug("Received a stray ok response.");
                return;
            } else {
                return;
            }
        }
        callPeer.processInviteOK(clientTransaction, ok);
    }

    private void processBusyHere(ClientTransaction clientTransaction, Response busyHere) {
        CallPeerSipImpl callPeer = this.activeCallsRepository.findCallPeer(clientTransaction.getDialog());
        if (callPeer != null) {
            callPeer.setState(CallPeerState.BUSY);
        } else if (logger.isDebugEnabled()) {
            logger.debug("Received a stray busyHere response.");
        }
    }

    private void processAuthenticationChallenge(ClientTransaction clientTransaction, Response response, SipProvider jainSipProvider) {
        CallPeerSipImpl callPeer = this.activeCallsRepository.findCallPeer(clientTransaction.getDialog());
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Authenticating an INVITE request.");
            }
            ClientTransaction retryTran = this.protocolProvider.getSipSecurityManager().handleChallenge(response, clientTransaction, jainSipProvider);
            if (retryTran != null) {
                if (callPeer != null) {
                    callPeer.handleAuthenticationChallenge(retryTran);
                }
                retryTran.sendRequest();
            } else if (logger.isTraceEnabled()) {
                logger.trace("No password supplied or error occured!");
            }
        } catch (Exception exc) {
            if (callPeer != null) {
                callPeer.logAndFail("Failed to authenticate.", exc);
            }
        }
    }

    public boolean processTimeout(TimeoutEvent timeoutEvent) {
        if (timeoutEvent.isServerTransaction()) {
            return false;
        }
        CallPeerSipImpl callPeer = this.activeCallsRepository.findCallPeer(timeoutEvent.getClientTransaction().getDialog());
        if (callPeer == null) {
            if (!logger.isDebugEnabled()) {
                return false;
            }
            logger.debug("Got a headless timeout event." + timeoutEvent);
            return false;
        } else if (isRemoteControlNotification(timeoutEvent.getClientTransaction().getRequest())) {
            return true;
        } else {
            try {
                hangupCallPeer(callPeer, Response.REQUEST_TIMEOUT, "The remote party has not replied!The call will be disconnected");
            } catch (Throwable th) {
                callPeer.setState(CallPeerState.FAILED, "The remote party has not replied!The call will be disconnected");
            }
            return true;
        }
    }

    public boolean processIOException(IOExceptionEvent exceptionEvent) {
        logger.error("Got an asynchronous exception event. host=" + exceptionEvent.getHost() + " port=" + exceptionEvent.getPort());
        return true;
    }

    public boolean processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        CallPeerSipImpl callPeer = this.activeCallsRepository.findCallPeer(dialogTerminatedEvent.getDialog());
        if (callPeer == null) {
            return false;
        }
        callPeer.setState(CallPeerState.DISCONNECTED);
        return true;
    }

    private void processInvite(SipProvider sourceProvider, ServerTransaction serverTransaction) {
        Request invite = serverTransaction.getRequest();
        CallPeerSipImpl existingPeer = this.activeCallsRepository.findCallPeer(serverTransaction.getDialog());
        OperationSetAutoAnswerSipImpl autoAnswerOpSet = (OperationSetAutoAnswerSipImpl) this.protocolProvider.getOperationSet(OperationSetBasicAutoAnswer.class);
        if (existingPeer != null) {
            existingPeer.processReInvite(serverTransaction);
            return;
        }
        ReplacesHeader replacesHeader = (ReplacesHeader) invite.getHeader("Replaces");
        if (replacesHeader != null) {
            existingPeer = this.activeCallsRepository.findCallPeer(replacesHeader.getCallId(), replacesHeader.getToTag(), replacesHeader.getFromTag());
            if (existingPeer != null) {
                ((CallSipImpl) existingPeer.getCall()).processReplacingInvite(sourceProvider, serverTransaction, existingPeer);
            } else {
                this.protocolProvider.sayErrorSilently(serverTransaction, Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
            }
        } else if (autoAnswerOpSet == null || !autoAnswerOpSet.forwardCall(invite, serverTransaction)) {
            CallSipImpl call = new CallSipImpl(this);
            if (!failServerTranForInsufficientSecurity(call.processInvite(sourceProvider, serverTransaction), serverTransaction) && autoAnswerOpSet != null) {
                autoAnswerOpSet.autoAnswer(call);
            }
        }
    }

    private boolean failServerTranForInsufficientSecurity(MediaAwareCallPeer<?, ?, ?> peer, ServerTransaction serverTransaction) {
        if (!getProtocolProvider().getAccountID().getAccountPropertyBoolean("MODE_PARANOIA", false) || peer.getMediaHandler().getAdvertisedEncryptionMethods().length != 0) {
            return false;
        }
        String reasonText = SipActivator.getResources().getI18NString("service.gui.security.encryption.required");
        peer.setState(CallPeerState.FAILED, reasonText, Response.SESSION_NOT_ACCEPTABLE);
        WarningHeader warning = null;
        try {
            warning = this.protocolProvider.getHeaderFactory().createWarningHeader(this.protocolProvider.getAccountID().getService(), WarningHeader.MISCELLANEOUS_WARNING, reasonText);
        } catch (InvalidArgumentException e) {
            logger.error("Cannot create warning header", e);
        } catch (ParseException e2) {
            logger.error("Cannot create warning header", e2);
        }
        try {
            this.protocolProvider.sayError(serverTransaction, Response.SESSION_NOT_ACCEPTABLE, warning);
        } catch (OperationFailedException e3) {
            logger.error("Cannot send 606 error!", e3);
        }
        return true;
    }

    private void processBye(ServerTransaction serverTransaction, Request byeRequest) {
        CallPeerSipImpl callPeer = this.activeCallsRepository.findCallPeer(serverTransaction.getDialog());
        if (callPeer != null) {
            callPeer.processBye(serverTransaction);
        } else if (logger.isDebugEnabled()) {
            logger.debug("Received a stray bye request.");
        }
    }

    private void processCancel(ServerTransaction serverTransaction, Request cancelRequest) {
        CallPeerSipImpl callPeer = this.activeCallsRepository.findCallPeer(serverTransaction.getDialog());
        if (callPeer != null) {
            callPeer.processCancel(serverTransaction);
        } else if (logger.isDebugEnabled()) {
            logger.debug("received a stray CANCEL req. ignoring");
        }
    }

    private void processAck(ServerTransaction serverTransaction, Request ackRequest) {
        CallPeerSipImpl peer = this.activeCallsRepository.findCallPeer(serverTransaction.getDialog());
        if (peer != null) {
            peer.processAck(serverTransaction, ackRequest);
        } else if (logger.isDebugEnabled()) {
            logger.debug("didn't find an ack's call, returning");
        }
    }

    private void processRefer(ServerTransaction serverTransaction, Request referRequest, SipProvider sipProvider) {
        ReferToHeader referToHeader = (ReferToHeader) referRequest.getHeader(ReferToHeader.NAME);
        if (referToHeader == null) {
            logger.error("No Refer-To header in REFER request:\n" + referRequest);
            return;
        }
        Address referToAddress = referToHeader.getAddress();
        if (referToAddress == null) {
            logger.error("No address in REFER request Refer-To header:\n" + referRequest);
            return;
        }
        CallSipImpl referToCall;
        Dialog dialog = serverTransaction.getDialog();
        CallPeerSipImpl callPeer = this.activeCallsRepository.findCallPeer(dialog);
        if (callPeer == null) {
            if (this.transferAuthority == null) {
                logger.warn("Ignoring REFER request without call for request:" + referRequest);
                try {
                    serverTransaction.terminate();
                    return;
                } catch (Throwable th) {
                    logger.warn("Failed to properly terminate transaction for a rogue request. Well ... so be it Request:" + referRequest);
                    return;
                }
            }
            boolean allowTransfer;
            FromHeader fromHeader = (FromHeader) referRequest.getHeader("From");
            OperationSetPresenceSipImpl opSetPersPresence = (OperationSetPresenceSipImpl) this.protocolProvider.getOperationSet(OperationSetPersistentPresence.class);
            Contact from = null;
            if (opSetPersPresence != null) {
                from = opSetPersPresence.resolveContactID(fromHeader.getAddress().getURI().toString());
            }
            if (from == null && opSetPersPresence != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("received a message from an unknown contact: " + fromHeader.getAddress().getURI().toString());
                }
                from = opSetPersPresence.createVolatileContact(fromHeader.getAddress().getURI().toString());
            }
            if (from != null) {
                allowTransfer = this.transferAuthority.processTransfer(from, referToAddress.getURI().toString());
            } else {
                allowTransfer = this.transferAuthority.processTransfer(fromHeader.getAddress().getURI().toString(), referToAddress.getURI().toString());
            }
            if (!allowTransfer) {
                try {
                    try {
                        serverTransaction.sendResponse(this.protocolProvider.getMessageFactory().createResponse(Response.DECLINE, referRequest));
                        return;
                    } catch (Exception e) {
                        logger.error("Error while sending the response 603", e);
                        return;
                    }
                } catch (ParseException e2) {
                    logger.error("Error while creating 603 response", e2);
                    return;
                }
            }
        }
        Response accepted = null;
        try {
            accepted = this.protocolProvider.getMessageFactory().createResponse(Response.ACCEPTED, referRequest);
        } catch (ParseException ex) {
            logger.error("Failed to create Accepted response to REFER request:\n" + referRequest, ex);
        }
        boolean removeSubscription = false;
        if (accepted != null) {
            Throwable failure = null;
            try {
                serverTransaction.sendResponse(accepted);
            } catch (InvalidArgumentException ex2) {
                failure = ex2;
            } catch (SipException ex22) {
                failure = ex22;
            }
            if (failure != null) {
                accepted = null;
                logger.error("Failed to send Accepted response to REFER request:\n" + referRequest, failure);
            } else {
                try {
                    removeSubscription = EventPackageUtils.addSubscription(dialog, referRequest);
                } catch (SipException ex222) {
                    logger.error("Failed to make the REFER request keep the dialog alive after BYE:\n" + referRequest, ex222);
                }
                try {
                    sendReferNotifyRequest(dialog, "active", null, "SIP/2.0 100 Trying", sipProvider);
                } catch (OperationFailedException e3) {
                }
            }
        }
        if (callPeer != null) {
            callPeer.setState(CallPeerState.REFERRED);
        }
        try {
            referToCall = createOutgoingCall(referToAddress, referRequest, null);
        } catch (OperationFailedException ex3) {
            referToCall = null;
            logger.error("Failed to create outgoing call to " + referToAddress, ex3);
        }
        final CallSipImpl referToCallListenerSource = referToCall;
        final boolean sendNotifyRequest = accepted != null;
        final Request subscription = removeSubscription ? referRequest : null;
        final Dialog dialog2 = dialog;
        final SipProvider sipProvider2 = sipProvider;
        CallChangeListener referToCallListener = new CallChangeAdapter() {
            private boolean done;

            /* JADX WARNING: Missing block: B:4:0x000d, code skipped:
            if (r7.getEventType().equals("CallState") == false) goto L_0x000f;
     */
            public synchronized void callStateChanged(net.java.sip.communicator.service.protocol.event.CallChangeEvent r7) {
                /*
                r6 = this;
                monitor-enter(r6);
                if (r7 == 0) goto L_0x0011;
            L_0x0003:
                r0 = r7.getEventType();	 Catch:{ all -> 0x0034 }
                r1 = "CallState";
                r0 = r0.equals(r1);	 Catch:{ all -> 0x0034 }
                if (r0 != 0) goto L_0x0011;
            L_0x000f:
                monitor-exit(r6);
                return;
            L_0x0011:
                r0 = r6.done;	 Catch:{ all -> 0x0034 }
                if (r0 != 0) goto L_0x000f;
            L_0x0015:
                r0 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicTelephonySipImpl.this;	 Catch:{ all -> 0x0034 }
                r1 = r7;	 Catch:{ all -> 0x0034 }
                r2 = r8;	 Catch:{ all -> 0x0034 }
                r3 = r9;	 Catch:{ all -> 0x0034 }
                r4 = r10;	 Catch:{ all -> 0x0034 }
                r5 = r11;	 Catch:{ all -> 0x0034 }
                r0 = r0.referToCallStateChanged(r1, r2, r3, r4, r5);	 Catch:{ all -> 0x0034 }
                if (r0 == 0) goto L_0x000f;
            L_0x0027:
                r0 = 1;
                r6.done = r0;	 Catch:{ all -> 0x0034 }
                r0 = r7;	 Catch:{ all -> 0x0034 }
                if (r0 == 0) goto L_0x000f;
            L_0x002e:
                r0 = r7;	 Catch:{ all -> 0x0034 }
                r0.removeCallChangeListener(r6);	 Catch:{ all -> 0x0034 }
                goto L_0x000f;
            L_0x0034:
                r0 = move-exception;
                monitor-exit(r6);
                throw r0;
                */
                throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.sip.OperationSetBasicTelephonySipImpl$AnonymousClass1.callStateChanged(net.java.sip.communicator.service.protocol.event.CallChangeEvent):void");
            }
        };
        if (referToCall != null) {
            referToCall.addCallChangeListener(referToCallListener);
        }
        referToCallListener.callStateChanged(null);
    }

    private boolean processNotify(ServerTransaction serverTransaction, Request notifyRequest) {
        String message;
        EventHeader eventHeader = (EventHeader) notifyRequest.getHeader("Event");
        if (eventHeader == null || !ReferencesHeader.REFER.equals(eventHeader.getEventType())) {
            return false;
        }
        SubscriptionStateHeader ssHeader = (SubscriptionStateHeader) notifyRequest.getHeader("Subscription-State");
        if (ssHeader == null) {
            logger.error("NOTIFY of refer event typewith no Subscription-State header.");
            return false;
        }
        Dialog dialog = serverTransaction.getDialog();
        CallPeerSipImpl peer = this.activeCallsRepository.findCallPeer(dialog);
        if (peer != null) {
            try {
                serverTransaction.sendResponse(this.messageFactory.createResponse(Response.OK, notifyRequest));
                if (SubscriptionStateHeader.TERMINATED.equalsIgnoreCase(ssHeader.getState()) && !EventPackageUtils.removeSubscriptionThenIsDialogAlive(dialog, ReferencesHeader.REFER)) {
                    peer.setState(CallPeerState.DISCONNECTED);
                }
                if (!(CallPeerState.DISCONNECTED.equals(peer.getState()) || EventPackageUtils.isByeProcessed(dialog))) {
                    try {
                        peer.hangup();
                    } catch (OperationFailedException ex) {
                        logger.error("Failed to send BYE in response to refer NOTIFY request.", ex);
                    }
                }
                return true;
            } catch (ParseException ex2) {
                message = "Failed to create OK response to refer NOTIFY.";
                logger.error(message, ex2);
                peer.setState(CallPeerState.DISCONNECTED, message);
                return false;
            } catch (Exception ex3) {
                message = "Failed to send OK response to refer NOTIFY request.";
                logger.error(message, ex3);
                peer.setState(CallPeerState.DISCONNECTED, message);
                return false;
            }
        } else if (!logger.isDebugEnabled()) {
            return false;
        } else {
            logger.debug("Received a stray refer NOTIFY request.");
            return false;
        }
    }

    /* access modifiers changed from: private */
    public boolean referToCallStateChanged(CallSipImpl referToCall, boolean sendNotifyRequest, Dialog dialog, SipProvider sipProvider, Object subscription) {
        CallState referToCallState = referToCall == null ? null : referToCall.getCallState();
        if (CallState.CALL_INITIALIZATION.equals(referToCallState)) {
            return false;
        }
        if (sendNotifyRequest) {
            try {
                sendReferNotifyRequest(dialog, SubscriptionStateHeader.TERMINATED, SubscriptionStateHeader.NO_RESOURCE, CallState.CALL_IN_PROGRESS.equals(referToCallState) ? "SIP/2.0 200 OK" : "SIP/2.0 603 Declined", sipProvider);
            } catch (OperationFailedException e) {
            }
        }
        if (!EventPackageUtils.removeSubscriptionThenIsDialogAlive(dialog, subscription)) {
            CallPeerSipImpl callPeer = this.activeCallsRepository.findCallPeer(dialog);
            if (callPeer != null) {
                callPeer.setState(CallPeerState.DISCONNECTED);
            }
        }
        return true;
    }

    private void sendReferNotifyRequest(Dialog dialog, String subscriptionState, String reasonCode, Object content, SipProvider sipProvider) throws OperationFailedException {
        Request notify = this.messageFactory.createRequest(dialog, "NOTIFY");
        HeaderFactory headerFactory = this.protocolProvider.getHeaderFactory();
        String eventType = ReferencesHeader.REFER;
        try {
            notify.setHeader(headerFactory.createEventHeader(eventType));
        } catch (ParseException ex) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to create " + eventType + " Event header.", 4, ex, logger);
        }
        SubscriptionStateHeader ssHeader = null;
        try {
            ssHeader = headerFactory.createSubscriptionStateHeader(subscriptionState);
            if (reasonCode != null) {
                ssHeader.setReasonCode(reasonCode);
            }
        } catch (ParseException ex2) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to create " + subscriptionState + " Subscription-State header.", 4, ex2, logger);
        }
        notify.setHeader(ssHeader);
        ContentTypeHeader ctHeader = null;
        try {
            ctHeader = headerFactory.createContentTypeHeader("message", "sipfrag");
        } catch (ParseException ex22) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to create Content-Type header.", 4, ex22, logger);
        }
        try {
            notify.setContent(content, ctHeader);
        } catch (ParseException ex222) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to set NOTIFY body/content.", 4, ex222, logger);
        }
        this.protocolProvider.sendInDialogRequest(sipProvider, notify, dialog);
    }

    public void hangupCallPeer(CallPeer peer) throws ClassCastException, OperationFailedException {
        hangupCallPeer(peer, Response.OK, null);
    }

    public synchronized void hangupCallPeer(CallPeer peer, int reasonCode, String reason) throws ClassCastException, OperationFailedException {
        ((CallPeerSipImpl) peer).hangup(reasonCode, reason);
    }

    public synchronized void answerCallPeer(CallPeer peer) throws OperationFailedException, ClassCastException {
        ((CallPeerSipImpl) peer).answer();
    }

    public String toString() {
        return getClass().getSimpleName() + "-[dn=" + this.protocolProvider.getOurDisplayName() + " addr=[" + this.protocolProvider.getRegistrarConnection().getAddressOfRecord() + "]";
    }

    public synchronized void shutdown() {
        if (logger.isTraceEnabled()) {
            logger.trace("Ending all active calls.");
        }
        Iterator<CallSipImpl> activeCalls = this.activeCallsRepository.getActiveCalls();
        while (activeCalls.hasNext()) {
            Iterator<? extends CallPeer> callPeers = ((CallSipImpl) activeCalls.next()).getCallPeers();
            while (callPeers.hasNext()) {
                CallPeer peer = (CallPeer) callPeers.next();
                try {
                    hangupCallPeer(peer);
                } catch (Exception ex) {
                    logger.warn("Failed to properly hangup particpant " + peer, ex);
                }
            }
        }
    }

    public boolean isSecure(CallPeer peer) {
        return ((CallPeerMediaHandlerSipImpl) ((CallPeerSipImpl) peer).getMediaHandler()).isSecure();
    }

    private void transfer(CallPeer peer, Address target) throws OperationFailedException {
        CallPeerSipImpl sipPeer = (CallPeerSipImpl) peer;
        Dialog dialog = sipPeer.getDialog();
        Request refer = this.messageFactory.createRequest(dialog, Request.REFER);
        HeaderFactory headerFactory = this.protocolProvider.getHeaderFactory();
        refer.setHeader(headerFactory.createReferToHeader(target));
        refer.addHeader(((HeaderFactoryImpl) headerFactory).createReferredByHeader(sipPeer.getPeerAddress()));
        this.protocolProvider.sendInDialogRequest(sipPeer.getJainSipProvider(), refer, dialog);
    }

    public void transfer(CallPeer transferee, CallPeer transferTarget) throws OperationFailedException {
        Address targetAddress = parseAddressString(transferTarget.getAddress());
        Dialog targetDialog = ((CallPeerSipImpl) transferTarget).getDialog();
        String remoteTag = targetDialog.getRemoteTag();
        String localTag = targetDialog.getLocalTag();
        Replaces replacesHeader = null;
        SipURI sipURI = (SipURI) targetAddress.getURI();
        try {
            HeaderFactoryImpl headerFactoryImpl = (HeaderFactoryImpl) this.protocolProvider.getHeaderFactory();
            String callId = targetDialog.getCallId().getCallId();
            if (remoteTag == null) {
                remoteTag = SdpConstants.RESERVED;
            }
            if (localTag == null) {
                localTag = SdpConstants.RESERVED;
            }
            replacesHeader = (Replaces) headerFactoryImpl.createReplacesHeader(callId, remoteTag, localTag);
        } catch (ParseException ex) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to create Replaces header for target dialog " + targetDialog, 11, ex, logger);
        }
        try {
            sipURI.setHeader("Replaces", URLEncoder.encode(replacesHeader.encodeBody(new StringBuilder()).toString(), "UTF-8"));
        } catch (Exception ex2) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to set Replaces header " + replacesHeader + " to SipURI " + sipURI, 4, ex2, logger);
        }
        putOnHold(transferee);
        putOnHold(transferTarget);
        transfer(transferee, targetAddress);
    }

    public void transfer(CallPeer peer, String target) throws OperationFailedException {
        transfer(peer, parseAddressString(target));
    }

    private Address parseAddressString(String addressString) throws OperationFailedException {
        Address address = null;
        try {
            return this.protocolProvider.parseAddressString(addressString);
        } catch (ParseException ex) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to parse address string " + addressString, 11, ex, logger);
            return address;
        }
    }

    private void assertRegistered() throws OperationFailedException {
        if (!this.protocolProvider.isRegistered()) {
            throw new OperationFailedException("The protocol provider should be registered before placing an outgoing call.", 3);
        }
    }

    public ProtocolProviderServiceSipImpl getProtocolProvider() {
        return this.protocolProvider;
    }

    private boolean isRemoteControlNotification(Request request) {
        if (request.getMethod().equals("NOTIFY")) {
            return new String(request.getRawContent()).startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<remote-control>");
        }
        return false;
    }

    public void setTransferAuthority(TransferAuthority authority) {
        this.transferAuthority = authority;
    }
}
