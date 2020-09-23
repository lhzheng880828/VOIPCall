package org.jitsi.gov.nist.javax.sip.stack;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.InternalErrorHandler;
import org.jitsi.gov.nist.core.NameValueList;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.javax.sip.ClientTransactionExt;
import org.jitsi.gov.nist.javax.sip.SIPConstants;
import org.jitsi.gov.nist.javax.sip.SipProviderImpl;
import org.jitsi.gov.nist.javax.sip.SipStackImpl;
import org.jitsi.gov.nist.javax.sip.Utils;
import org.jitsi.gov.nist.javax.sip.address.AddressImpl;
import org.jitsi.gov.nist.javax.sip.header.Contact;
import org.jitsi.gov.nist.javax.sip.header.Event;
import org.jitsi.gov.nist.javax.sip.header.Expires;
import org.jitsi.gov.nist.javax.sip.header.RecordRoute;
import org.jitsi.gov.nist.javax.sip.header.RecordRouteList;
import org.jitsi.gov.nist.javax.sip.header.Route;
import org.jitsi.gov.nist.javax.sip.header.RouteList;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.TimeStamp;
import org.jitsi.gov.nist.javax.sip.header.To;
import org.jitsi.gov.nist.javax.sip.header.Via;
import org.jitsi.gov.nist.javax.sip.message.SIPMessage;
import org.jitsi.gov.nist.javax.sip.message.SIPRequest;
import org.jitsi.gov.nist.javax.sip.message.SIPResponse;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.DialogState;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.ObjectInUseException;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.Timeout;
import org.jitsi.javax.sip.TimeoutEvent;
import org.jitsi.javax.sip.TransactionState;
import org.jitsi.javax.sip.address.Hop;
import org.jitsi.javax.sip.address.SipURI;
import org.jitsi.javax.sip.address.URI;
import org.jitsi.javax.sip.header.Header;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;

public class SIPClientTransaction extends SIPTransaction implements ServerResponseInterface, ClientTransaction, ClientTransactionExt {
    /* access modifiers changed from: private|static */
    public static StackLogger logger = CommonLogger.getLogger(SIPClientTransaction.class);
    private int callingStateTimeoutCount;
    private SIPDialog defaultDialog;
    private String defaultDialogId;
    private SIPRequest lastRequest;
    private Hop nextHop;
    private boolean notifyOnRetransmit;
    private String originalRequestCallId;
    private Contact originalRequestContact;
    private Event originalRequestEventHeader;
    private String originalRequestFromTag;
    private String originalRequestScheme;
    private transient ServerResponseInterface respondTo;
    private Set<Integer> responsesReceived = new HashSet(2);
    private Set<String> sipDialogs;
    private boolean timeoutIfStillInCallingState;
    private AtomicBoolean timerKStarted = new AtomicBoolean(false);
    private SIPStackTimerTask transactionTimer;
    private boolean transactionTimerCancelled = false;
    private Object transactionTimerLock = new Object();
    private String viaHost;
    private int viaPort;

    class ExpiresTimerTask extends SIPStackTimerTask {
        public void runTask() {
            SIPClientTransaction ct = SIPClientTransaction.this;
            SipProviderImpl provider = ct.getSipProvider();
            if (ct.getState() != TransactionState.TERMINATED) {
                provider.handleEvent(new TimeoutEvent(SIPClientTransaction.this.getSipProvider(), SIPClientTransaction.this, Timeout.TRANSACTION), ct);
                return;
            }
            SIPClientTransaction sIPClientTransaction = SIPClientTransaction.this;
            if (SIPClientTransaction.logger.isLoggingEnabled(32)) {
                sIPClientTransaction = SIPClientTransaction.this;
                SIPClientTransaction.logger.logDebug("state = " + ct.getState());
            }
        }
    }

    public class TransactionTimer extends SIPStackTimerTask {
        public void runTask() {
            SIPClientTransaction clientTransaction = SIPClientTransaction.this;
            SIPTransactionStack sipStack = clientTransaction.sipStack;
            if (clientTransaction.isTerminated()) {
                try {
                    sipStack.getTimer().cancel(this);
                } catch (IllegalStateException e) {
                    if (!sipStack.isAlive()) {
                        return;
                    }
                }
                SIPClientTransaction.this.cleanUpOnTerminated();
                return;
            }
            clientTransaction.fireTimer();
        }
    }

    protected SIPClientTransaction(SIPTransactionStack newSIPStack, MessageChannel newChannelToUse) {
        super(newSIPStack, newChannelToUse);
        setBranch(Utils.getInstance().generateBranchId());
        this.messageProcessor = newChannelToUse.messageProcessor;
        setEncapsulatedChannel(newChannelToUse);
        this.notifyOnRetransmit = false;
        this.timeoutIfStillInCallingState = false;
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Creating clientTransaction " + this);
            logger.logStackTrace();
        }
        this.sipDialogs = new CopyOnWriteArraySet();
    }

    public void setResponseInterface(ServerResponseInterface newRespondTo) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Setting response interface for " + this + " to " + newRespondTo);
            if (newRespondTo == null) {
                logger.logStackTrace();
                logger.logDebug("WARNING -- setting to null!");
            }
        }
        this.respondTo = newRespondTo;
    }

    public MessageChannel getRequestChannel() {
        return this;
    }

    public boolean isMessagePartOfTransaction(SIPMessage messageToTest) {
        boolean rfc3261Compliant;
        Via topMostViaHeader = messageToTest.getTopmostVia();
        String messageBranch = topMostViaHeader.getBranch();
        if (getBranch() == null || messageBranch == null || !getBranch().toLowerCase().startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE) || !messageBranch.toLowerCase().startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE)) {
            rfc3261Compliant = false;
        } else {
            rfc3261Compliant = true;
        }
        if (3 == getInternalState()) {
            if (!rfc3261Compliant) {
                return getBranch().equals(messageToTest.getTransactionId());
            }
            if (getBranch().equalsIgnoreCase(topMostViaHeader.getBranch()) && getMethod().equals(messageToTest.getCSeq().getMethod())) {
                return true;
            }
            return false;
        } else if (isTerminated()) {
            return false;
        } else {
            if (rfc3261Compliant) {
                if (topMostViaHeader == null || !getBranch().equalsIgnoreCase(topMostViaHeader.getBranch())) {
                    return false;
                }
                return getMethod().equals(messageToTest.getCSeq().getMethod());
            } else if (getBranch() != null) {
                return getBranch().equalsIgnoreCase(messageToTest.getTransactionId());
            } else {
                return ((SIPRequest) getRequest()).getTransactionId().equalsIgnoreCase(messageToTest.getTransactionId());
            }
        }
    }

    public void sendMessage(SIPMessage messageToSend) throws IOException {
        try {
            SIPRequest transactionRequest = (SIPRequest) messageToSend;
            try {
                transactionRequest.getTopmostVia().setBranch(getBranch());
            } catch (ParseException e) {
            }
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("Sending Message " + messageToSend);
                logger.logDebug("TransactionState " + getState());
            }
            if ((2 == getInternalState() || getInternalState() == 0) && transactionRequest.getMethod().equals("ACK")) {
                if (isReliable()) {
                    setState(5);
                } else {
                    setState(3);
                }
                cleanUpOnTimer();
                super.sendMessage(transactionRequest);
                this.isMapped = true;
            } else {
                this.lastRequest = transactionRequest;
                if (getInternalState() < 0) {
                    setOriginalRequest(transactionRequest);
                    if (transactionRequest.getMethod().equals("INVITE")) {
                        setState(0);
                    } else if (transactionRequest.getMethod().equals("ACK")) {
                        setState(5);
                        cleanUpOnTimer();
                    } else {
                        setState(1);
                    }
                    if (!isReliable()) {
                        enableRetransmissionTimer();
                    }
                    if (isInviteTransaction()) {
                        enableTimeoutTimer(64);
                    } else {
                        enableTimeoutTimer(64);
                    }
                }
                super.sendMessage(transactionRequest);
                this.isMapped = true;
            }
            startTransactionTimer();
        } catch (IOException e2) {
            setState(5);
            throw e2;
        } catch (Throwable th) {
            this.isMapped = true;
            startTransactionTimer();
        }
    }

    public synchronized void processResponse(SIPResponse transactionResponse, MessageChannel sourceChannel, SIPDialog dialog) {
        if (getInternalState() >= 0) {
            if (!((3 == getInternalState() || 5 == getInternalState()) && transactionResponse.getStatusCode() / 100 == 1)) {
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("processing " + transactionResponse.getFirstLine() + "current state = " + getState());
                    logger.logDebug("dialog = " + dialog);
                }
                this.lastResponse = transactionResponse;
                try {
                    if (isInviteTransaction()) {
                        inviteClientTransaction(transactionResponse, sourceChannel, dialog);
                    } else {
                        nonInviteClientTransaction(transactionResponse, sourceChannel, dialog);
                    }
                } catch (IOException ex) {
                    if (logger.isLoggingEnabled()) {
                        logger.logException(ex);
                    }
                    setState(5);
                    raiseErrorEvent(2);
                }
            }
        }
    }

    private void nonInviteClientTransaction(SIPResponse transactionResponse, MessageChannel sourceChannel, SIPDialog sipDialog) throws IOException {
        int statusCode = transactionResponse.getStatusCode();
        if (1 == getInternalState()) {
            if (statusCode / 100 == 1) {
                setState(2);
                enableRetransmissionTimer(8);
                enableTimeoutTimer(64);
                if (this.respondTo != null) {
                    this.respondTo.processResponse(transactionResponse, this, sipDialog);
                } else {
                    semRelease();
                }
            } else if (Response.OK <= statusCode && statusCode <= 699) {
                if (isReliable()) {
                    setState(5);
                } else {
                    setState(3);
                    scheduleTimerK((long) this.TIMER_K);
                }
                if (this.respondTo != null) {
                    this.respondTo.processResponse(transactionResponse, this, sipDialog);
                } else {
                    semRelease();
                }
                if (isReliable() && 5 == getInternalState()) {
                    cleanUpOnTerminated();
                }
                cleanUpOnTimer();
            }
        } else if (2 != getInternalState()) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug(" Not sending response to TU! " + getState());
            }
            semRelease();
        } else if (statusCode / 100 == 1) {
            if (this.respondTo != null) {
                this.respondTo.processResponse(transactionResponse, this, sipDialog);
            } else {
                semRelease();
            }
        } else if (Response.OK <= statusCode && statusCode <= 699) {
            disableRetransmissionTimer();
            disableTimeoutTimer();
            if (isReliable()) {
                setState(5);
            } else {
                setState(3);
                scheduleTimerK((long) this.TIMER_K);
            }
            if (this.respondTo != null) {
                this.respondTo.processResponse(transactionResponse, this, sipDialog);
            } else {
                semRelease();
            }
            if (isReliable() && 5 == getInternalState()) {
                cleanUpOnTerminated();
            }
            cleanUpOnTimer();
        }
    }

    private void scheduleTimerK(long time) {
        if (this.transactionTimer != null && this.timerKStarted.compareAndSet(false, true)) {
            synchronized (this.transactionTimerLock) {
                if (!this.transactionTimerCancelled) {
                    this.sipStack.getTimer().cancel(this.transactionTimer);
                    this.transactionTimer = null;
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("starting TransactionTimerK() : " + getTransactionId() + " time " + time);
                    }
                    SIPStackTimerTask task = new SIPStackTimerTask() {
                        public void runTask() {
                            if (SIPClientTransaction.logger.isLoggingEnabled(32)) {
                                SIPClientTransaction.logger.logDebug("executing TransactionTimerJ() : " + SIPClientTransaction.this.getTransactionId());
                            }
                            SIPClientTransaction.this.fireTimeoutTimer();
                            SIPClientTransaction.this.cleanUpOnTerminated();
                        }
                    };
                    if (time > 0) {
                        this.sipStack.getTimer().schedule(task, ((long) this.BASE_TIMER_INTERVAL) * time);
                    } else {
                        task.runTask();
                    }
                    this.transactionTimerCancelled = true;
                }
            }
        }
    }

    private void inviteClientTransaction(SIPResponse transactionResponse, MessageChannel sourceChannel, SIPDialog dialog) throws IOException {
        int statusCode = transactionResponse.getStatusCode();
        if (5 == getInternalState()) {
            boolean ackAlreadySent = false;
            if (dialog != null && dialog.isAckSent(transactionResponse.getCSeq().getSeqNumber()) && dialog.getLastAckSent().getCSeq().getSeqNumber() == transactionResponse.getCSeq().getSeqNumber() && transactionResponse.getFromTag().equals(dialog.getLastAckSent().getFromTag())) {
                ackAlreadySent = true;
            }
            if (dialog != null && ackAlreadySent && transactionResponse.getCSeq().getMethod().equals(dialog.getMethod())) {
                try {
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("resending ACK");
                    }
                    dialog.resendAck();
                } catch (SipException e) {
                }
            }
            semRelease();
        } else if (getInternalState() == 0) {
            if (statusCode / 100 == 2) {
                disableRetransmissionTimer();
                disableTimeoutTimer();
                setState(5);
                if (this.respondTo != null) {
                    this.respondTo.processResponse(transactionResponse, this, dialog);
                } else {
                    semRelease();
                }
            } else if (statusCode / 100 == 1) {
                disableRetransmissionTimer();
                disableTimeoutTimer();
                setState(2);
                if (this.respondTo != null) {
                    this.respondTo.processResponse(transactionResponse, this, dialog);
                } else {
                    semRelease();
                }
            } else if (300 <= statusCode && statusCode <= 699) {
                try {
                    sendMessage((SIPRequest) createErrorAck());
                } catch (Exception ex) {
                    logger.logError("Unexpected Exception sending ACK -- sending error AcK ", ex);
                }
                if (getDialog() != null && ((SIPDialog) getDialog()).isBackToBackUserAgent()) {
                    ((SIPDialog) getDialog()).releaseAckSem();
                }
                if (isReliable()) {
                    setState(5);
                } else {
                    setState(3);
                    enableTimeoutTimer(this.TIMER_D);
                }
                if (this.respondTo != null) {
                    this.respondTo.processResponse(transactionResponse, this, dialog);
                } else {
                    semRelease();
                }
                cleanUpOnTimer();
            }
        } else if (2 == getInternalState()) {
            if (statusCode / 100 == 1) {
                if (this.respondTo != null) {
                    this.respondTo.processResponse(transactionResponse, this, dialog);
                } else {
                    semRelease();
                }
            } else if (statusCode / 100 == 2) {
                setState(5);
                if (this.respondTo != null) {
                    this.respondTo.processResponse(transactionResponse, this, dialog);
                } else {
                    semRelease();
                }
            } else if (300 <= statusCode && statusCode <= 699) {
                try {
                    sendMessage((SIPRequest) createErrorAck());
                } catch (Exception ex2) {
                    InternalErrorHandler.handleException(ex2);
                }
                if (getDialog() != null) {
                    ((SIPDialog) getDialog()).releaseAckSem();
                }
                if (isReliable()) {
                    setState(5);
                } else {
                    setState(3);
                    enableTimeoutTimer(this.TIMER_D);
                }
                cleanUpOnTimer();
                if (this.respondTo != null) {
                    this.respondTo.processResponse(transactionResponse, this, dialog);
                } else {
                    semRelease();
                }
            }
        } else if (3 == getInternalState() && 300 <= statusCode && statusCode <= 699) {
            try {
                sendMessage((SIPRequest) createErrorAck());
            } catch (Exception ex22) {
                InternalErrorHandler.handleException(ex22);
            } catch (Throwable th) {
                semRelease();
            }
            semRelease();
        }
    }

    public void sendRequest() throws SipException {
        SIPRequest sipRequest = getOriginalRequest();
        if (getInternalState() >= 0) {
            throw new SipException("Request already sent");
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("sendRequest() " + sipRequest);
        }
        try {
            sipRequest.checkHeaders();
            if (getMethod().equals("SUBSCRIBE") && sipRequest.getHeader("Expires") == null && logger.isLoggingEnabled()) {
                logger.logWarning("Expires header missing in outgoing subscribe -- Notifier will assume implied value on event package");
            }
            try {
                SIPDialog dialog;
                if (getMethod().equals(Request.CANCEL) && this.sipStack.isCancelClientTransactionChecked()) {
                    SIPClientTransaction ct = (SIPClientTransaction) this.sipStack.findCancelTransaction(getOriginalRequest(), false);
                    if (ct == null) {
                        throw new SipException("Could not find original tx to cancel. RFC 3261 9.1");
                    } else if (ct.getInternalState() < 0) {
                        throw new SipException("State is null no provisional response yet -- cannot cancel RFC 3261 9.1");
                    } else if (!ct.isInviteTransaction()) {
                        throw new SipException("Cannot cancel non-invite requests RFC 3261 9.1");
                    }
                } else if (getMethod().equals("BYE") || getMethod().equals("NOTIFY")) {
                    dialog = this.sipStack.getDialog(getOriginalRequest().getDialogId(false));
                    if (getSipProvider().isAutomaticDialogSupportEnabled() && dialog != null) {
                        throw new SipException("Dialog is present and AutomaticDialogSupport is enabled for  the provider -- Send the Request using the Dialog.sendRequest(transaction)");
                    }
                }
                if (isInviteTransaction()) {
                    dialog = getDefaultDialog();
                    if (!(dialog == null || !dialog.isBackToBackUserAgent() || dialog.takeAckSem())) {
                        throw new SipException("Failed to take ACK semaphore");
                    }
                }
                this.isMapped = true;
                int expiresTime = -1;
                if (sipRequest.getHeader("Expires") != null) {
                    expiresTime = ((Expires) sipRequest.getHeader("Expires")).getExpires();
                }
                if (getDefaultDialog() != null && isInviteTransaction() && expiresTime != -1 && this.expiresTimerTask == null) {
                    this.expiresTimerTask = new ExpiresTimerTask();
                    this.sipStack.getTimer().schedule(this.expiresTimerTask, (long) (expiresTime * 1000));
                }
                sendMessage(sipRequest);
            } catch (IOException ex) {
                String str;
                setState(5);
                if (this.expiresTimerTask != null) {
                    this.sipStack.getTimer().cancel(this.expiresTimerTask);
                }
                if (ex.getMessage() == null) {
                    str = "IO Error sending request";
                } else {
                    str = ex.getMessage();
                }
                throw new SipException(str, ex);
            }
        } catch (ParseException ex2) {
            if (logger.isLoggingEnabled()) {
                logger.logError("missing required header");
            }
            throw new SipException(ex2.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    public void fireRetransmissionTimer() {
        try {
            if (getInternalState() >= 0 && this.isMapped) {
                boolean inv = isInviteTransaction();
                int s = getInternalState();
                if (!(inv && s == 0)) {
                    if (!inv) {
                        if (!(1 == s || 2 == s)) {
                            return;
                        }
                    }
                    return;
                }
                if (this.lastRequest != null) {
                    if (this.sipStack.generateTimeStampHeader && this.lastRequest.getHeader("Timestamp") != null) {
                        long milisec = System.currentTimeMillis();
                        TimeStamp timeStamp = new TimeStamp();
                        try {
                            timeStamp.setTimeStamp((float) milisec);
                        } catch (InvalidArgumentException ex) {
                            InternalErrorHandler.handleException(ex);
                        }
                        this.lastRequest.setHeader((Header) timeStamp);
                    }
                    super.sendMessage(this.lastRequest);
                    if (this.notifyOnRetransmit) {
                        getSipProvider().handleEvent(new TimeoutEvent(getSipProvider(), (ClientTransaction) this, Timeout.RETRANSMIT), this);
                    }
                    if (this.timeoutIfStillInCallingState && getInternalState() == 0) {
                        this.callingStateTimeoutCount--;
                        if (this.callingStateTimeoutCount == 0) {
                            getSipProvider().handleEvent(new TimeoutEvent(getSipProvider(), (ClientTransaction) this, Timeout.RETRANSMIT), this);
                            this.timeoutIfStillInCallingState = false;
                        }
                    }
                }
            }
        } catch (IOException e) {
            raiseIOExceptionEvent();
            raiseErrorEvent(2);
        }
    }

    /* access modifiers changed from: protected */
    public void fireTimeoutTimer() {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("fireTimeoutTimer " + this);
        }
        SIPDialog dialog = (SIPDialog) getDialog();
        if (getInternalState() == 0 || 1 == getInternalState() || 2 == getInternalState()) {
            if (dialog == null || !(dialog.getState() == null || dialog.getState() == DialogState.EARLY)) {
                if (dialog != null && getMethod().equalsIgnoreCase("BYE") && dialog.isTerminatedOnBye()) {
                    dialog.delete();
                }
            } else if (SIPTransactionStack.isDialogCreated(getMethod())) {
                dialog.delete();
            }
        }
        if (3 == getInternalState() || 5 == getInternalState()) {
            setState(5);
            return;
        }
        raiseErrorEvent(1);
        if (getMethod().equalsIgnoreCase(Request.CANCEL)) {
            SIPClientTransaction inviteTx = (SIPClientTransaction) getOriginalRequest().getInviteTransaction();
            if (inviteTx == null) {
                return;
            }
            if ((inviteTx.getInternalState() == 0 || inviteTx.getInternalState() == 2) && inviteTx.getDialog() != null) {
                inviteTx.setState(5);
            }
        }
    }

    public Request createCancel() throws SipException {
        SIPRequest originalRequest = getOriginalRequest();
        if (originalRequest == null) {
            throw new SipException("Bad state " + getState());
        } else if (!originalRequest.getMethod().equals("INVITE")) {
            throw new SipException("Only INIVTE may be cancelled");
        } else if (originalRequest.getMethod().equalsIgnoreCase("ACK")) {
            throw new SipException("Cannot Cancel ACK!");
        } else {
            SIPRequest cancelRequest = originalRequest.createCancelRequest();
            cancelRequest.setInviteTransaction(this);
            return cancelRequest;
        }
    }

    public Request createAck() throws SipException {
        SIPRequest originalRequest = getOriginalRequest();
        if (originalRequest == null) {
            throw new SipException("bad state " + getState());
        } else if (getMethod().equalsIgnoreCase("ACK")) {
            throw new SipException("Cannot ACK an ACK!");
        } else if (this.lastResponse == null) {
            throw new SipException("bad Transaction state");
        } else if (this.lastResponse.getStatusCode() < Response.OK) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("lastResponse = " + this.lastResponse);
            }
            throw new SipException("Cannot ACK a provisional response!");
        } else {
            SIPRequest ackRequest = originalRequest.createAckRequest((To) this.lastResponse.getTo());
            RecordRouteList recordRouteList = this.lastResponse.getRecordRouteHeaders();
            if (recordRouteList != null) {
                Route route;
                ackRequest.removeHeader("Route");
                RouteList routeList = new RouteList();
                ListIterator<RecordRoute> li = recordRouteList.listIterator(recordRouteList.size());
                while (li.hasPrevious()) {
                    RecordRoute rr = (RecordRoute) li.previous();
                    route = new Route();
                    route.setAddress((AddressImpl) ((AddressImpl) rr.getAddress()).clone());
                    route.setParameters((NameValueList) rr.getParameters().clone());
                    routeList.add((SIPHeader) route);
                }
                Contact contact = null;
                if (this.lastResponse.getContactHeaders() != null) {
                    contact = (Contact) this.lastResponse.getContactHeaders().getFirst();
                }
                if (!((SipURI) ((Route) routeList.getFirst()).getAddress().getURI()).hasLrParam()) {
                    route = null;
                    if (contact != null) {
                        route = new Route();
                        route.setAddress((AddressImpl) ((AddressImpl) contact.getAddress()).clone());
                    }
                    Route firstRoute = (Route) routeList.getFirst();
                    routeList.removeFirst();
                    ackRequest.setRequestURI(firstRoute.getAddress().getURI());
                    if (route != null) {
                        routeList.add((SIPHeader) route);
                    }
                    ackRequest.addHeader((Header) routeList);
                } else if (contact != null) {
                    ackRequest.setRequestURI((URI) contact.getAddress().getURI().clone());
                    ackRequest.addHeader((Header) routeList);
                }
            } else if (!(this.lastResponse.getContactHeaders() == null || this.lastResponse.getStatusCode() / 100 == 3)) {
                ackRequest.setRequestURI((URI) ((Contact) this.lastResponse.getContactHeaders().getFirst()).getAddress().getURI().clone());
            }
            return ackRequest;
        }
    }

    private final Request createErrorAck() throws SipException, ParseException {
        SIPRequest originalRequest = getOriginalRequest();
        if (originalRequest == null) {
            throw new SipException("bad state " + getState());
        } else if (!isInviteTransaction()) {
            throw new SipException("Can only ACK an INVITE!");
        } else if (this.lastResponse == null) {
            throw new SipException("bad Transaction state");
        } else if (this.lastResponse.getStatusCode() >= Response.OK) {
            return originalRequest.createErrorAck((To) this.lastResponse.getTo());
        } else {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("lastResponse = " + this.lastResponse);
            }
            throw new SipException("Cannot ACK a provisional response!");
        }
    }

    public void setViaPort(int port) {
        this.viaPort = port;
    }

    public void setViaHost(String host) {
        this.viaHost = host;
    }

    public int getViaPort() {
        return this.viaPort;
    }

    public String getViaHost() {
        return this.viaHost;
    }

    public Via getOutgoingViaHeader() {
        return getMessageProcessor().getViaHeader();
    }

    public void clearState() {
    }

    public void setState(int newState) {
        if (newState == 5 && isReliable() && !getSIPStack().cacheClientConnections) {
            this.collectionTime = 64;
        }
        if (super.getInternalState() != 3 && (newState == 3 || newState == 5)) {
            this.sipStack.decrementActiveClientTransactionCount();
        }
        super.setState(newState);
    }

    /* access modifiers changed from: protected */
    public void startTransactionTimer() {
        if (this.transactionTimerStarted.compareAndSet(false, true) && this.sipStack.getTimer() != null) {
            synchronized (this.transactionTimerLock) {
                if (!this.transactionTimerCancelled) {
                    this.transactionTimer = new TransactionTimer();
                    this.sipStack.getTimer().scheduleWithFixedDelay(this.transactionTimer, (long) this.BASE_TIMER_INTERVAL, (long) this.BASE_TIMER_INTERVAL);
                }
            }
        }
    }

    public void terminate() throws ObjectInUseException {
        setState(5);
    }

    public void stopExpiresTimer() {
        if (this.expiresTimerTask != null) {
            this.sipStack.getTimer().cancel(this.expiresTimerTask);
            this.expiresTimerTask = null;
        }
    }

    public boolean checkFromTag(SIPResponse sipResponse) {
        String originalFromTag = getOriginalRequestFromTag();
        if (this.defaultDialog != null) {
            if (((originalFromTag == null ? 1 : 0) ^ (sipResponse.getFrom().getTag() == null ? 1 : 0)) != 0) {
                if (!logger.isLoggingEnabled(32)) {
                    return false;
                }
                logger.logDebug("From tag mismatch -- dropping response");
                return false;
            } else if (!(originalFromTag == null || originalFromTag.equalsIgnoreCase(sipResponse.getFrom().getTag()))) {
                if (!logger.isLoggingEnabled(32)) {
                    return false;
                }
                logger.logDebug("From tag mismatch -- dropping response");
                return false;
            }
        }
        return true;
    }

    public void processResponse(SIPResponse sipResponse, MessageChannel incomingChannel) {
        boolean isRetransmission;
        int code = sipResponse.getStatusCode();
        if (this.responsesReceived.add(Integer.valueOf(code))) {
            isRetransmission = false;
        } else {
            isRetransmission = true;
        }
        if (!(code != Response.SESSION_PROGRESS || this.lastResponse == null || sipResponse.toString().equals(this.lastResponse.toString()))) {
            isRetransmission = false;
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("marking response as retransmission " + isRetransmission + " for ctx " + this);
        }
        SIPDialog dialog = null;
        String method = sipResponse.getCSeq().getMethod();
        String dialogId = sipResponse.getDialogId(false);
        if (!method.equals(Request.CANCEL) || this.lastRequest == null) {
            dialog = getDialog(dialogId);
        } else {
            SIPClientTransaction ict = (SIPClientTransaction) this.lastRequest.getInviteTransaction();
            if (ict != null) {
                dialog = ict.defaultDialog;
            }
        }
        if (dialog == null) {
            if (code <= 100 || code >= 300 || ((sipResponse.getToTag() == null && !this.sipStack.isRfc2543Supported()) || !SIPTransactionStack.isDialogCreated(method))) {
                dialog = this.defaultDialog;
            } else {
                synchronized (this) {
                    if (this.defaultDialog != null) {
                        if (sipResponse.getFromTag() != null) {
                            String defaultDialogId = this.defaultDialog.getDialogId();
                            if (this.defaultDialog.getLastResponseMethod() == null || (method.equals("SUBSCRIBE") && this.defaultDialog.getLastResponseMethod().equals("NOTIFY") && defaultDialogId.equals(dialogId))) {
                                this.defaultDialog.setLastResponse(this, sipResponse);
                                dialog = this.defaultDialog;
                            } else {
                                dialog = this.sipStack.getDialog(dialogId);
                                if (dialog == null && this.defaultDialog.isAssigned()) {
                                    dialog = this.sipStack.createDialog(this, sipResponse);
                                }
                            }
                            if (dialog != null) {
                                setDialog(dialog, dialog.getDialogId());
                            } else {
                                logger.logError("dialog is unexpectedly null", new NullPointerException());
                            }
                        } else {
                            throw new RuntimeException("Response without from-tag");
                        }
                    } else if (this.sipStack.isAutomaticDialogSupportEnabled) {
                        dialog = this.sipStack.createDialog(this, sipResponse);
                        setDialog(dialog, dialog.getDialogId());
                    }
                }
            }
        } else if (5 != getInternalState()) {
            dialog.setLastResponse(this, sipResponse);
        }
        processResponse(sipResponse, incomingChannel, dialog);
    }

    public Dialog getDialog() {
        Dialog retval = null;
        SIPResponse localLastResponse = this.lastResponse;
        if (!(localLastResponse == null || localLastResponse.getFromTag() == null || localLastResponse.getToTag() == null || localLastResponse.getStatusCode() == 100)) {
            retval = getDialog(localLastResponse.getDialogId(false));
        }
        if (retval == null) {
            retval = getDefaultDialog();
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug(" sipDialogs =  " + this.sipDialogs + " default dialog " + getDefaultDialog() + " retval " + retval);
        }
        return retval;
    }

    public SIPDialog getDialog(String dialogId) {
        if (this.sipDialogs == null || !this.sipDialogs.contains(dialogId)) {
            return null;
        }
        SIPDialog retval = this.sipStack.getDialog(dialogId);
        if (retval == null) {
            return this.sipStack.getEarlyDialog(dialogId);
        }
        return retval;
    }

    public void setDialog(SIPDialog sipDialog, String dialogId) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("setDialog: " + dialogId + " sipDialog = " + sipDialog);
        }
        if (sipDialog == null) {
            if (logger.isLoggingEnabled(4)) {
                logger.logError("NULL DIALOG!!");
            }
            throw new NullPointerException("bad dialog null");
        }
        if (this.defaultDialog == null && this.defaultDialogId == null) {
            this.defaultDialog = sipDialog;
            if (isDialogCreatingTransaction() && getSIPStack().getMaxForkTime() != 0) {
                getSIPStack().addForkedClientTransaction(this);
            }
        }
        if (dialogId != null && sipDialog.getDialogId() != null && this.sipDialogs != null) {
            this.sipDialogs.add(dialogId);
        }
    }

    public SIPDialog getDefaultDialog() {
        SIPDialog dialog = this.defaultDialog;
        if (dialog != null || this.defaultDialogId == null) {
            return dialog;
        }
        return this.sipStack.getDialog(this.defaultDialogId);
    }

    public void setNextHop(Hop hop) {
        this.nextHop = hop;
    }

    public Hop getNextHop() {
        return this.nextHop;
    }

    public void setNotifyOnRetransmit(boolean notifyOnRetransmit) {
        this.notifyOnRetransmit = notifyOnRetransmit;
    }

    public boolean isNotifyOnRetransmit() {
        return this.notifyOnRetransmit;
    }

    public void alertIfStillInCallingStateBy(int count) {
        this.timeoutIfStillInCallingState = true;
        this.callingStateTimeoutCount = count;
    }

    /* access modifiers changed from: protected */
    public void cleanUpOnTimer() {
        if (isReleaseReferences()) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("cleanupOnTimer: " + getTransactionId());
            }
            if (this.defaultDialog != null) {
                String dialogId = this.defaultDialog.getDialogId();
                if (!(dialogId == null || this.sipStack.getDialog(dialogId) == null)) {
                    this.defaultDialogId = dialogId;
                    this.defaultDialog = null;
                }
            }
            if (this.originalRequest != null) {
                this.originalRequest.setTransaction(null);
                this.originalRequest.setInviteTransaction(null);
                this.originalRequest.cleanUp();
                if (this.originalRequestBytes == null) {
                    this.originalRequestBytes = this.originalRequest.encodeAsBytes(getTransport());
                }
                if (!(getMethod().equalsIgnoreCase("INVITE") || getMethod().equalsIgnoreCase(Request.CANCEL))) {
                    this.originalRequestFromTag = this.originalRequest.getFromTag();
                    this.originalRequestCallId = this.originalRequest.getCallId().getCallId();
                    this.originalRequestEventHeader = (Event) this.originalRequest.getHeader("Event");
                    this.originalRequestContact = this.originalRequest.getContactHeader();
                    this.originalRequestScheme = this.originalRequest.getRequestURI().getScheme();
                    this.originalRequest = null;
                }
            }
            if (!getMethod().equalsIgnoreCase("SUBSCRIBE")) {
                this.lastResponse = null;
            }
            this.lastRequest = null;
        }
    }

    public void cleanUp() {
        if (isReleaseReferences()) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("cleanup : " + getTransactionId());
            }
            if (this.defaultDialog != null) {
                this.defaultDialogId = this.defaultDialog.getDialogId();
                this.defaultDialog = null;
            }
            if (this.originalRequest != null && this.originalRequestBytes == null) {
                this.originalRequestBytes = this.originalRequest.encodeAsBytes(getTransport());
            }
            this.originalRequest = null;
            cleanUpOnTimer();
            this.originalRequestCallId = null;
            this.originalRequestEventHeader = null;
            this.originalRequestFromTag = null;
            this.originalRequestContact = null;
            this.originalRequestScheme = null;
            if (this.sipDialogs != null) {
                this.sipDialogs.clear();
            }
            this.responsesReceived.clear();
            this.respondTo = null;
            this.transactionTimer = null;
            this.lastResponse = null;
            this.transactionTimerLock = null;
            this.timerKStarted = null;
        }
    }

    /* access modifiers changed from: protected */
    public void cleanUpOnTerminated() {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("removing  = " + this + " isReliable " + isReliable());
        }
        if (isReleaseReferences() && this.originalRequest == null && this.originalRequestBytes != null) {
            try {
                this.originalRequest = (SIPRequest) this.sipStack.getMessageParserFactory().createMessageParser(this.sipStack).parseSIPMessage(this.originalRequestBytes, true, false, null);
            } catch (ParseException e) {
                logger.logError("message " + this.originalRequestBytes + " could not be reparsed !");
            }
        }
        this.sipStack.removeTransaction(this);
        if (this.sipStack.cacheClientConnections || !isReliable()) {
            if (logger.isLoggingEnabled() && isReliable()) {
                int useCount = getMessageChannel().useCount;
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("Client Use Count = " + useCount);
                }
            }
            if (((SipStackImpl) getSIPStack()).isReEntrantListener() && isReleaseReferences()) {
                cleanUp();
                return;
            }
            return;
        }
        MessageChannel messageChannel = getMessageChannel();
        int newUseCount = messageChannel.useCount - 1;
        messageChannel.useCount = newUseCount;
        if (newUseCount <= 0) {
            this.sipStack.getTimer().schedule(new LingerTimer(), 8000);
        }
    }

    public String getOriginalRequestFromTag() {
        if (this.originalRequest == null) {
            return this.originalRequestFromTag;
        }
        return this.originalRequest.getFromTag();
    }

    public String getOriginalRequestCallId() {
        if (this.originalRequest == null) {
            return this.originalRequestCallId;
        }
        return this.originalRequest.getCallId().getCallId();
    }

    public Event getOriginalRequestEvent() {
        if (this.originalRequest == null) {
            return this.originalRequestEventHeader;
        }
        return (Event) this.originalRequest.getHeader("Event");
    }

    public Contact getOriginalRequestContact() {
        if (this.originalRequest == null) {
            return this.originalRequestContact;
        }
        return this.originalRequest.getContactHeader();
    }

    public String getOriginalRequestScheme() {
        if (this.originalRequest == null) {
            return this.originalRequestScheme;
        }
        return this.originalRequest.getRequestURI().getScheme();
    }
}
