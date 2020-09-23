package org.jitsi.gov.nist.javax.sip.stack;

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.HostPort;
import org.jitsi.gov.nist.core.InternalErrorHandler;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.javax.sip.SIPConstants;
import org.jitsi.gov.nist.javax.sip.ServerTransactionExt;
import org.jitsi.gov.nist.javax.sip.header.RSeq;
import org.jitsi.gov.nist.javax.sip.header.Via;
import org.jitsi.gov.nist.javax.sip.message.SIPMessage;
import org.jitsi.gov.nist.javax.sip.message.SIPRequest;
import org.jitsi.gov.nist.javax.sip.message.SIPResponse;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.ListeningPoint;
import org.jitsi.javax.sip.ObjectInUseException;
import org.jitsi.javax.sip.ServerTransaction;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.Timeout;
import org.jitsi.javax.sip.TimeoutEvent;
import org.jitsi.javax.sip.TransactionState;
import org.jitsi.javax.sip.address.Hop;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;

public class SIPServerTransaction extends SIPTransaction implements ServerRequestInterface, ServerTransaction, ServerTransactionExt {
    public static final String CONTENT_SUBTYPE_SDP = "sdp";
    public static final String CONTENT_TYPE_APPLICATION = "application";
    private static boolean interlockProvisionalResponses = true;
    /* access modifiers changed from: private|static */
    public static StackLogger logger = CommonLogger.getLogger(SIPServerTransaction.class);
    private SIPDialog dialog;
    protected String dialogId;
    private SIPServerTransaction inviteTransaction;
    protected boolean isAckSeen;
    private byte[] lastResponseAsBytes;
    private String lastResponseHost;
    private int lastResponsePort;
    private int lastResponseStatusCode;
    private String lastResponseTransport;
    private String originalRequestFromTag;
    private HostPort originalRequestSentBy;
    private long pendingReliableCSeqNumber;
    private long pendingReliableRSeqNumber;
    private byte[] pendingReliableResponseAsBytes;
    private String pendingReliableResponseMethod;
    private SIPClientTransaction pendingSubscribeTransaction;
    private Semaphore provisionalResponseSem = new Semaphore(1);
    private ProvisionalResponseTask provisionalResponseTask;
    private transient ServerRequestInterface requestOf;
    private boolean retransmissionAlertEnabled;
    private RetransmissionAlertTimerTask retransmissionAlertTimerTask;
    private int rseqNumber = -1;

    class ListenerExecutionMaxTimer extends SIPStackTimerTask {
        SIPServerTransaction serverTransaction = SIPServerTransaction.this;

        ListenerExecutionMaxTimer() {
        }

        public void runTask() {
            try {
                if (this.serverTransaction.getInternalState() < 0) {
                    this.serverTransaction.terminate();
                    SIPTransactionStack sipStack = this.serverTransaction.getSIPStack();
                    sipStack.removePendingTransaction(this.serverTransaction);
                    sipStack.removeTransaction(this.serverTransaction);
                }
            } catch (Exception ex) {
                SIPServerTransaction.logger.logError("unexpected exception", ex);
            }
        }
    }

    class ProvisionalResponseTask extends SIPStackTimerTask {
        int ticks = 1;
        int ticksLeft = this.ticks;

        public void runTask() {
            SIPServerTransaction serverTransaction = SIPServerTransaction.this;
            if (serverTransaction.isTerminated()) {
                SIPServerTransaction.this.sipStack.getTimer().cancel(this);
                return;
            }
            this.ticksLeft--;
            if (this.ticksLeft == -1) {
                serverTransaction.fireReliableResponseRetransmissionTimer();
                this.ticksLeft = this.ticks * 2;
                this.ticks = this.ticksLeft;
                if (this.ticksLeft >= 64) {
                    SIPServerTransaction.this.sipStack.getTimer().cancel(this);
                    SIPServerTransaction.this.setState(5);
                    SIPServerTransaction.this.fireTimeoutTimer();
                }
            }
        }
    }

    class RetransmissionAlertTimerTask extends SIPStackTimerTask {
        String dialogId;
        int ticks = 1;
        int ticksLeft = this.ticks;

        public RetransmissionAlertTimerTask(String dialogId) {
        }

        public void runTask() {
            SIPServerTransaction serverTransaction = SIPServerTransaction.this;
            this.ticksLeft--;
            if (this.ticksLeft == -1) {
                serverTransaction.fireRetransmissionTimer();
                this.ticksLeft = this.ticks * 2;
            }
        }
    }

    class SendTrying extends SIPStackTimerTask {
        protected SendTrying() {
            if (SIPServerTransaction.logger.isLoggingEnabled(32)) {
                SIPServerTransaction.logger.logDebug("scheduled timer for " + SIPServerTransaction.this);
            }
        }

        public void runTask() {
            SIPServerTransaction serverTransaction = SIPServerTransaction.this;
            int realState = serverTransaction.getRealState();
            if (realState < 0 || 1 == realState) {
                if (SIPServerTransaction.logger.isLoggingEnabled(32)) {
                    SIPServerTransaction.logger.logDebug(" sending Trying current state = " + serverTransaction.getRealState());
                }
                try {
                    serverTransaction.sendMessage(serverTransaction.getOriginalRequest().createResponse(100, "Trying"));
                    if (SIPServerTransaction.logger.isLoggingEnabled(32)) {
                        SIPServerTransaction.logger.logDebug(" trying sent " + serverTransaction.getRealState());
                    }
                } catch (IOException e) {
                    if (SIPServerTransaction.logger.isLoggingEnabled()) {
                        SIPServerTransaction.logger.logError("IO error sending  TRYING");
                    }
                }
            }
        }
    }

    class TransactionTimer extends SIPStackTimerTask {
        public TransactionTimer() {
            if (SIPServerTransaction.logger.isLoggingEnabled(32)) {
                SIPServerTransaction.logger.logDebug("TransactionTimer() : " + SIPServerTransaction.this.getTransactionId());
            }
        }

        public void runTask() {
            if (SIPServerTransaction.this.isTerminated()) {
                try {
                    SIPServerTransaction.this.sipStack.getTimer().cancel(this);
                } catch (IllegalStateException e) {
                    if (!SIPServerTransaction.this.sipStack.isAlive()) {
                        return;
                    }
                }
                SIPServerTransaction.this.sipStack.getTimer().schedule(new LingerTimer(), 8000);
            } else {
                SIPServerTransaction.this.fireTimer();
            }
            if (SIPServerTransaction.this.originalRequest != null) {
                SIPServerTransaction.this.originalRequest.cleanUp();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void sendResponse(SIPResponse transactionResponse) throws IOException {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("sipServerTransaction::sendResponse " + transactionResponse.getFirstLine());
        }
        try {
            if (isReliable()) {
                getMessageChannel().sendMessage(transactionResponse);
            } else {
                Via via = transactionResponse.getTopmostVia();
                String transport = via.getTransport();
                if (transport == null) {
                    throw new IOException("missing transport!");
                }
                String host;
                int port = via.getRPort();
                if (port == -1) {
                    port = via.getPort();
                }
                if (port == -1) {
                    if (transport.equalsIgnoreCase(ListeningPoint.TLS)) {
                        port = 5061;
                    } else {
                        port = 5060;
                    }
                }
                if (via.getMAddr() != null) {
                    host = via.getMAddr();
                } else {
                    host = via.getParameter("received");
                    if (host == null) {
                        host = via.getHost();
                    }
                }
                Hop hop = this.sipStack.addressResolver.resolveAddress(new HopImpl(host, port, transport));
                MessageChannel messageChannel = getSIPStack().createRawMessageChannel(getSipProvider().getListeningPoint(hop.getTransport()).getIPAddress(), getPort(), hop);
                if (messageChannel != null) {
                    messageChannel.sendMessage(transactionResponse);
                    this.lastResponseHost = host;
                    this.lastResponsePort = port;
                    this.lastResponseTransport = transport;
                } else {
                    throw new IOException("Could not create a message channel for " + hop + " with source IP:Port " + getSipProvider().getListeningPoint(hop.getTransport()).getIPAddress() + Separators.COLON + getPort());
                }
            }
            this.lastResponseAsBytes = transactionResponse.encodeAsBytes(getTransport());
            this.lastResponse = null;
        } finally {
            startTransactionTimer();
        }
    }

    protected SIPServerTransaction(SIPTransactionStack sipStack, MessageChannel newChannelToUse) {
        super(sipStack, newChannelToUse);
        if (sipStack.maxListenerResponseTime != -1) {
            sipStack.getTimer().schedule(new ListenerExecutionMaxTimer(), (long) (sipStack.maxListenerResponseTime * 1000));
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Creating Server Transaction" + getBranchId());
            logger.logStackTrace();
        }
    }

    public void setRequestInterface(ServerRequestInterface newRequestOf) {
        this.requestOf = newRequestOf;
    }

    public MessageChannel getResponseChannel() {
        return this;
    }

    public boolean isMessagePartOfTransaction(SIPMessage messageToTest) {
        String method = messageToTest.getCSeq().getMethod();
        SIPRequest origRequest = getOriginalRequest();
        if (!isInviteTransaction() && isTerminated()) {
            return false;
        }
        Via topViaHeader = messageToTest.getTopmostVia();
        if (topViaHeader == null) {
            return false;
        }
        String messageBranch = topViaHeader.getBranch();
        if (!(messageBranch == null || messageBranch.toLowerCase().startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE))) {
            messageBranch = null;
        }
        if (messageBranch == null || getBranch() == null) {
            origRequest = (SIPRequest) getRequest();
            String originalFromTag = origRequest.getFromTag();
            String thisFromTag = messageToTest.getFrom().getTag();
            boolean skipFrom = originalFromTag == null || thisFromTag == null;
            String originalToTag = origRequest.getToTag();
            String thisToTag = messageToTest.getTo().getTag();
            boolean skipTo = originalToTag == null || thisToTag == null;
            boolean isResponse = messageToTest instanceof SIPResponse;
            if (messageToTest.getCSeq().getMethod().equalsIgnoreCase(Request.CANCEL) && !origRequest.getCSeq().getMethod().equalsIgnoreCase(Request.CANCEL)) {
                return false;
            }
            if (!isResponse && !origRequest.getRequestURI().equals(((SIPRequest) messageToTest).getRequestURI())) {
                return false;
            }
            if (!skipFrom && (originalFromTag == null || !originalFromTag.equalsIgnoreCase(thisFromTag))) {
                return false;
            }
            if ((!skipTo && (originalToTag == null || !originalToTag.equalsIgnoreCase(thisToTag))) || !origRequest.getCallId().getCallId().equalsIgnoreCase(messageToTest.getCallId().getCallId()) || origRequest.getCSeq().getSeqNumber() != messageToTest.getCSeq().getSeqNumber()) {
                return false;
            }
            if ((!messageToTest.getCSeq().getMethod().equals(Request.CANCEL) || getMethod().equals(messageToTest.getCSeq().getMethod())) && topViaHeader.equals(origRequest.getTopmostVia())) {
                return true;
            }
            return false;
        } else if (!method.equals(Request.CANCEL)) {
            return origRequest != null ? getBranch().equalsIgnoreCase(messageBranch) && topViaHeader.getSentBy().equals(origRequest.getTopmostVia().getSentBy()) : getBranch().equalsIgnoreCase(messageBranch) && topViaHeader.getSentBy().equals(this.originalRequestSentBy);
        } else {
            if (getMethod().equals(Request.CANCEL) && getBranch().equalsIgnoreCase(messageBranch) && topViaHeader.getSentBy().equals(origRequest.getTopmostVia().getSentBy())) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void map() {
        int realState = getRealState();
        if (realState < 0 || realState == 1) {
            if (!isInviteTransaction() || this.isMapped || this.sipStack.getTimer() == null) {
                this.isMapped = true;
            } else {
                this.isMapped = true;
                this.sipStack.getTimer().schedule(new SendTrying(), 200);
            }
        }
        this.sipStack.removePendingTransaction(this);
    }

    public boolean isTransactionMapped() {
        return this.isMapped;
    }

    public void processRequest(SIPRequest transactionRequest, MessageChannel sourceChannel) {
        boolean toTu = false;
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("processRequest: " + transactionRequest.getFirstLine());
            logger.logDebug("tx state = " + getRealState());
        }
        try {
            if (getRealState() < 0) {
                setOriginalRequest(transactionRequest);
                setState(1);
                toTu = true;
                setPassToListener();
                if (isInviteTransaction() && this.isMapped) {
                    sendMessage(transactionRequest.createResponse(100, "Trying"));
                }
            } else if (isInviteTransaction() && 3 == getRealState() && transactionRequest.getMethod().equals("ACK")) {
                setState(4);
                disableRetransmissionTimer();
                if (isReliable()) {
                    setState(5);
                } else {
                    enableTimeoutTimer(this.TIMER_I);
                }
                if (this.sipStack.isNon2XXAckPassedToListener()) {
                    this.requestOf.processRequest(transactionRequest, this);
                    return;
                }
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("ACK received for server Tx " + getTransactionId() + " not delivering to application!");
                }
                semRelease();
                return;
            } else if (transactionRequest.getMethod().equals(getMethod())) {
                if (2 == getRealState() || 3 == getRealState()) {
                    semRelease();
                    resendLastResponseAsBytes();
                } else if (transactionRequest.getMethod().equals("ACK")) {
                    if (this.requestOf != null) {
                        this.requestOf.processRequest(transactionRequest, this);
                    } else {
                        semRelease();
                    }
                }
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("completed processing retransmitted request : " + transactionRequest.getFirstLine() + this + " txState = " + getState() + " lastResponse = " + this.lastResponseAsBytes);
                    return;
                }
                return;
            }
            if (3 == getRealState() || 5 == getRealState() || this.requestOf == null) {
                if (SIPTransactionStack.isDialogCreated(getMethod()) && getRealState() == 5 && transactionRequest.getMethod().equals("ACK") && this.requestOf != null) {
                    SIPDialog thisDialog = (SIPDialog) getDialog();
                    if (thisDialog == null || !thisDialog.ackProcessed) {
                        if (thisDialog != null) {
                            thisDialog.ackReceived(transactionRequest.getCSeq().getSeqNumber());
                            thisDialog.ackProcessed = true;
                        }
                        this.requestOf.processRequest(transactionRequest, this);
                    } else {
                        semRelease();
                    }
                } else if (transactionRequest.getMethod().equals(Request.CANCEL)) {
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("Too late to cancel Transaction");
                    }
                    semRelease();
                    try {
                        sendMessage(transactionRequest.createResponse(Response.OK));
                    } catch (IOException e) {
                    }
                }
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("Dropping request " + getRealState());
                }
            } else if (getMethod().equals(transactionRequest.getMethod())) {
                if (toTu) {
                    this.requestOf.processRequest(transactionRequest, this);
                } else {
                    semRelease();
                }
            } else if (this.requestOf != null) {
                this.requestOf.processRequest(transactionRequest, this);
            } else {
                semRelease();
            }
        } catch (IOException e2) {
            if (logger.isLoggingEnabled()) {
                logger.logError("IOException ", e2);
            }
            semRelease();
            raiseIOExceptionEvent();
        }
    }

    public void sendMessage(SIPMessage messageToSend) throws IOException {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("sipServerTransaction::sendMessage " + messageToSend.getFirstLine());
        }
        SIPResponse transactionResponse = (SIPResponse) messageToSend;
        int statusCode = transactionResponse.getStatusCode();
        try {
            if (this.originalRequestBranch != null) {
                transactionResponse.getTopmostVia().setBranch(getBranch());
            } else {
                transactionResponse.getTopmostVia().removeParameter("branch");
            }
            if (!this.originalRequestHasPort) {
                transactionResponse.getTopmostVia().removePort();
            }
            if (!transactionResponse.getCSeq().getMethod().equals(getMethod())) {
                sendResponse(transactionResponse);
            } else if (checkStateTimers(statusCode)) {
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("sendMessage : tx = " + this + " getState = " + getState());
                }
                this.lastResponse = transactionResponse;
                this.lastResponseStatusCode = transactionResponse.getStatusCode();
                sendResponse(transactionResponse);
            } else if (logger.isLoggingEnabled(32)) {
                logger.logDebug("checkStateTimers returned false -- not sending message");
            }
            startTransactionTimer();
        } catch (IOException e) {
            setState(5);
            this.collectionTime = 0;
            throw e;
        } catch (ParseException ex) {
            logger.logError("UnexpectedException", ex);
            throw new IOException("Unexpected exception");
        } catch (Throwable th) {
            startTransactionTimer();
        }
    }

    private boolean checkStateTimers(int statusCode) {
        if (getRealState() == 1) {
            if (statusCode / 100 == 1) {
                setState(2);
            } else if (Response.OK <= statusCode && statusCode <= 699) {
                if (isInviteTransaction()) {
                    if (statusCode / 100 == 2) {
                        disableRetransmissionTimer();
                        disableTimeoutTimer();
                        this.collectionTime = 64;
                        cleanUpOnTimer();
                        setState(5);
                        if (getDialog() != null) {
                            ((SIPDialog) getDialog()).setRetransmissionTicks();
                        }
                    } else {
                        setState(3);
                        if (!isReliable()) {
                            enableRetransmissionTimer();
                        }
                        cleanUpOnTimer();
                        enableTimeoutTimer(64);
                    }
                } else if (isReliable() || getInternalState() == 3) {
                    cleanUpOnTimer();
                    setState(5);
                    startTransactionTimerJ(0);
                } else {
                    setState(3);
                    startTransactionTimerJ(64);
                    cleanUpOnTimer();
                }
            }
        } else if (getRealState() == 2) {
            if (isInviteTransaction()) {
                if (statusCode / 100 == 2) {
                    disableRetransmissionTimer();
                    disableTimeoutTimer();
                    this.collectionTime = 64;
                    cleanUpOnTimer();
                    setState(5);
                    if (getDialog() != null) {
                        ((SIPDialog) getDialog()).setRetransmissionTicks();
                    }
                } else if (300 <= statusCode && statusCode <= 699) {
                    setState(3);
                    if (!isReliable()) {
                        enableRetransmissionTimer();
                    }
                    cleanUpOnTimer();
                    enableTimeoutTimer(64);
                }
            } else if (Response.OK <= statusCode && statusCode <= 699) {
                setState(3);
                if (isReliable()) {
                    setState(5);
                    startTransactionTimerJ(0);
                } else {
                    disableRetransmissionTimer();
                    startTransactionTimerJ(64);
                }
                cleanUpOnTimer();
            }
        } else if (3 == getRealState()) {
            return false;
        }
        return true;
    }

    public String getViaHost() {
        return super.getViaHost();
    }

    public int getViaPort() {
        return super.getViaPort();
    }

    /* access modifiers changed from: protected */
    public void fireRetransmissionTimer() {
        try {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("fireRetransmissionTimer() -- " + this + " state " + getState());
            }
            if (!isInviteTransaction()) {
                return;
            }
            if (this.lastResponse != null || this.lastResponseAsBytes != null) {
                if (this.retransmissionAlertEnabled && !this.sipStack.isTransactionPendingAck(this)) {
                    Object sipProvider = getSipProvider();
                    sipProvider.handleEvent(new TimeoutEvent(sipProvider, (ServerTransaction) this, Timeout.RETRANSMIT), this);
                } else if (this.lastResponseStatusCode / 100 >= 2 && !this.isAckSeen) {
                    resendLastResponseAsBytes();
                }
            }
        } catch (IOException e) {
            if (logger.isLoggingEnabled()) {
                logger.logException(e);
            }
            raiseErrorEvent(2);
        }
    }

    public void resendLastResponseAsBytes() throws IOException {
        if (this.lastResponse != null) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("resend last response " + this.lastResponse);
            }
            sendMessage(this.lastResponse);
        } else if (this.lastResponseAsBytes != null) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("resend last response " + new String(this.lastResponseAsBytes));
            }
            if (isReliable()) {
                if (logger.isLoggingEnabled(16)) {
                    try {
                        getMessageChannel().logMessage((SIPResponse) this.sipStack.getMessageParserFactory().createMessageParser(this.sipStack).parseSIPMessage(this.lastResponseAsBytes, true, false, null), getPeerInetAddress(), getPeerPort(), System.currentTimeMillis());
                    } catch (ParseException e) {
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("couldn't reparse last response " + new String(this.lastResponseAsBytes));
                        }
                    }
                }
                getMessageChannel().sendMessage(this.lastResponseAsBytes, getPeerInetAddress(), getPeerPort(), false);
                return;
            }
            Hop hop = this.sipStack.addressResolver.resolveAddress(new HopImpl(this.lastResponseHost, this.lastResponsePort, this.lastResponseTransport));
            MessageChannel messageChannel = getSIPStack().createRawMessageChannel(getSipProvider().getListeningPoint(hop.getTransport()).getIPAddress(), getPort(), hop);
            if (messageChannel != null) {
                if (logger.isLoggingEnabled(16)) {
                    try {
                        getMessageChannel().logMessage((SIPResponse) this.sipStack.getMessageParserFactory().createMessageParser(this.sipStack).parseSIPMessage(this.lastResponseAsBytes, true, false, null), getPeerInetAddress(), getPeerPort(), System.currentTimeMillis());
                    } catch (ParseException e2) {
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("couldn't reparse last response " + new String(this.lastResponseAsBytes));
                        }
                    }
                }
                messageChannel.sendMessage(this.lastResponseAsBytes, InetAddress.getByName(hop.getHost()), hop.getPort(), false);
                return;
            }
            throw new IOException("Could not create a message channel for " + hop + " with source IP:Port " + getSipProvider().getListeningPoint(hop.getTransport()).getIPAddress() + Separators.COLON + getPort());
        }
    }

    /* access modifiers changed from: private */
    public void fireReliableResponseRetransmissionTimer() {
        try {
            resendLastResponseAsBytes();
        } catch (IOException e) {
            if (logger.isLoggingEnabled()) {
                logger.logException(e);
            }
            setState(5);
            raiseErrorEvent(2);
        }
    }

    /* access modifiers changed from: protected */
    public void fireTimeoutTimer() {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("SIPServerTransaction.fireTimeoutTimer this = " + this + " current state = " + getRealState() + " method = " + getMethod());
        }
        if (!isInviteTransaction() || !this.sipStack.removeTransactionPendingAck(this)) {
            SIPDialog dialog = (SIPDialog) getDialog();
            if (SIPTransactionStack.isDialogCreated(getMethod()) && (getRealState() == 0 || 1 == getRealState())) {
                dialog.setState(3);
            } else if (getMethod().equals("BYE") && dialog != null && dialog.isTerminatedOnBye()) {
                dialog.setState(3);
            }
            if (3 == getRealState() && isInviteTransaction()) {
                raiseErrorEvent(1);
                setState(5);
                this.sipStack.removeTransaction(this);
            } else if (3 == getRealState() && !isInviteTransaction()) {
                setState(5);
                if (getMethod().equals(Request.CANCEL)) {
                    this.sipStack.removeTransaction(this);
                } else {
                    cleanUp();
                }
            } else if (4 == getRealState() && isInviteTransaction()) {
                setState(5);
                this.sipStack.removeTransaction(this);
            } else if (!isInviteTransaction() && (3 == getRealState() || 4 == getRealState())) {
                setState(5);
            } else if (isInviteTransaction() && 5 == getRealState()) {
                raiseErrorEvent(1);
                if (dialog != null) {
                    dialog.setState(3);
                }
            }
        } else if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Found tx pending ACK - returning");
        }
    }

    public int getLastResponseStatusCode() {
        return this.lastResponseStatusCode;
    }

    public void setOriginalRequest(SIPRequest originalRequest) {
        super.setOriginalRequest(originalRequest);
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    public void sendResponse(org.jitsi.javax.sip.message.Response r23) throws org.jitsi.javax.sip.SipException {
        /*
        r22 = this;
        r20 = r23;
        r20 = (org.jitsi.gov.nist.javax.sip.message.SIPResponse) r20;
        r10 = r22.getDialog();
        r10 = (org.jitsi.gov.nist.javax.sip.stack.SIPDialog) r10;
        if (r23 != 0) goto L_0x0014;
    L_0x000c:
        r2 = new java.lang.NullPointerException;
        r3 = "null response";
        r2.<init>(r3);
        throw r2;
    L_0x0014:
        r20.checkHeaders();	 Catch:{ ParseException -> 0x0033 }
        r2 = r20.getCSeq();
        r19 = r2.getMethod();
        r2 = r22.getMethod();
        r0 = r19;
        r2 = r0.equals(r2);
        if (r2 != 0) goto L_0x003e;
    L_0x002b:
        r2 = new org.jitsi.javax.sip.SipException;
        r3 = "CSeq method does not match Request method of request that created the tx.";
        r2.m1626init(r3);
        throw r2;
    L_0x0033:
        r13 = move-exception;
        r2 = new org.jitsi.javax.sip.SipException;
        r3 = r13.getMessage();
        r2.m1626init(r3);
        throw r2;
    L_0x003e:
        r21 = r23.getStatusCode();
        r2 = r22.getMethod();
        r3 = "SUBSCRIBE";
        r2 = r2.equals(r3);
        if (r2 == 0) goto L_0x0089;
    L_0x004e:
        r2 = r21 / 100;
        r3 = 2;
        if (r2 != r3) goto L_0x0089;
    L_0x0053:
        r2 = "Expires";
        r0 = r23;
        r2 = r0.getHeader(r2);
        if (r2 != 0) goto L_0x0065;
    L_0x005d:
        r2 = new org.jitsi.javax.sip.SipException;
        r3 = "Expires header is mandatory in 2xx response of SUBSCRIBE";
        r2.m1626init(r3);
        throw r2;
    L_0x0065:
        r2 = r22.getOriginalRequest();
        r17 = r2.getExpires();
        r17 = (org.jitsi.gov.nist.javax.sip.header.Expires) r17;
        r18 = r23.getExpires();
        r18 = (org.jitsi.gov.nist.javax.sip.header.Expires) r18;
        if (r17 == 0) goto L_0x0089;
    L_0x0077:
        r2 = r18.getExpires();
        r3 = r17.getExpires();
        if (r2 <= r3) goto L_0x0089;
    L_0x0081:
        r2 = new org.jitsi.javax.sip.SipException;
        r3 = "Response Expires time exceeds request Expires time : See RFC 3265 3.1.1";
        r2.m1626init(r3);
        throw r2;
    L_0x0089:
        r2 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
        r0 = r21;
        if (r0 != r2) goto L_0x00ab;
    L_0x008f:
        r2 = "INVITE";
        r0 = r19;
        r2 = r0.equals(r2);
        if (r2 == 0) goto L_0x00ab;
    L_0x0099:
        r2 = "Contact";
        r0 = r20;
        r2 = r0.getHeader(r2);
        if (r2 != 0) goto L_0x00ab;
    L_0x00a3:
        r2 = new org.jitsi.javax.sip.SipException;
        r3 = "Contact Header is mandatory for the OK to the INVITE";
        r2.m1626init(r3);
        throw r2;
    L_0x00ab:
        r2 = r23;
        r2 = (org.jitsi.gov.nist.javax.sip.message.SIPMessage) r2;
        r0 = r22;
        r2 = r0.isMessagePartOfTransaction(r2);
        if (r2 != 0) goto L_0x00bf;
    L_0x00b7:
        r2 = new org.jitsi.javax.sip.SipException;
        r3 = "Response does not belong to this transaction.";
        r2.m1626init(r3);
        throw r2;
    L_0x00bf:
        r0 = r23;
        r0 = (org.jitsi.gov.nist.javax.sip.message.SIPResponse) r0;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2 = r0;
        r9 = r2.getContentTypeHeader();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r0 = r22;
        r2 = r0.pendingReliableResponseAsBytes;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 == 0) goto L_0x016e;
    L_0x00ce:
        r2 = r22.getDialog();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 == 0) goto L_0x016e;
    L_0x00d4:
        r2 = r22.getInternalState();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = 5;
        if (r2 == r3) goto L_0x016e;
    L_0x00db:
        r2 = r21 / 100;
        r3 = 2;
        if (r2 != r3) goto L_0x016e;
    L_0x00e0:
        if (r9 == 0) goto L_0x016e;
    L_0x00e2:
        r2 = r9.getContentType();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = "application";
        r2 = r2.equalsIgnoreCase(r3);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 == 0) goto L_0x016e;
    L_0x00ee:
        r2 = r9.getContentSubType();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = "sdp";
        r2 = r2.equalsIgnoreCase(r3);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 == 0) goto L_0x016e;
    L_0x00fa:
        r2 = interlockProvisionalResponses;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 != 0) goto L_0x012a;
    L_0x00fe:
        r2 = new org.jitsi.javax.sip.SipException;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = "cannot send response -- unacked povisional";
        r2.m1626init(r3);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        throw r2;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
    L_0x0106:
        r13 = move-exception;
        r2 = logger;
        r2 = r2.isLoggingEnabled();
        if (r2 == 0) goto L_0x0114;
    L_0x010f:
        r2 = logger;
        r2.logException(r13);
    L_0x0114:
        r2 = 5;
        r0 = r22;
        r0.setState(r2);
        r2 = 2;
        r0 = r22;
        r0.raiseErrorEvent(r2);
        r2 = new org.jitsi.javax.sip.SipException;
        r3 = r13.getMessage();
        r2.m1626init(r3);
        throw r2;
    L_0x012a:
        r0 = r22;
        r2 = r0.provisionalResponseSem;	 Catch:{ InterruptedException -> 0x0140 }
        r4 = 1;
        r3 = java.util.concurrent.TimeUnit.SECONDS;	 Catch:{ InterruptedException -> 0x0140 }
        r8 = r2.tryAcquire(r4, r3);	 Catch:{ InterruptedException -> 0x0140 }
        if (r8 != 0) goto L_0x018e;
    L_0x0138:
        r2 = new org.jitsi.javax.sip.SipException;	 Catch:{ InterruptedException -> 0x0140 }
        r3 = "cannot send response -- unacked povisional";
        r2.m1626init(r3);	 Catch:{ InterruptedException -> 0x0140 }
        throw r2;	 Catch:{ InterruptedException -> 0x0140 }
    L_0x0140:
        r13 = move-exception;
        r2 = logger;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = "Interrupted acuqiring PRACK sem";
        r2.logError(r3);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2 = new org.jitsi.javax.sip.SipException;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = "Cannot aquire PRACK sem";
        r2.m1626init(r3);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        throw r2;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
    L_0x0150:
        r14 = move-exception;
        r2 = logger;
        r2 = r2.isLoggingEnabled();
        if (r2 == 0) goto L_0x015e;
    L_0x0159:
        r2 = logger;
        r2.logException(r14);
    L_0x015e:
        r2 = 5;
        r0 = r22;
        r0.setState(r2);
        r2 = new org.jitsi.javax.sip.SipException;
        r3 = r14.getMessage();
        r2.m1626init(r3);
        throw r2;
    L_0x016e:
        r0 = r22;
        r2 = r0.pendingReliableResponseAsBytes;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 == 0) goto L_0x018e;
    L_0x0174:
        r2 = r20.isFinalResponse();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 == 0) goto L_0x018e;
    L_0x017a:
        r0 = r22;
        r2 = r0.sipStack;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2 = r2.getTimer();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r0 = r22;
        r3 = r0.provisionalResponseTask;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2.cancel(r3);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2 = 0;
        r0 = r22;
        r0.provisionalResponseTask = r2;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
    L_0x018e:
        if (r10 == 0) goto L_0x026b;
    L_0x0190:
        r2 = r21 / 100;
        r3 = 2;
        if (r2 != r3) goto L_0x01b6;
    L_0x0195:
        r2 = org.jitsi.gov.nist.javax.sip.stack.SIPTransactionStack.isDialogCreated(r19);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 == 0) goto L_0x01b6;
    L_0x019b:
        r2 = r10.getLocalTag();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 != 0) goto L_0x01d4;
    L_0x01a1:
        r2 = r20.getToTag();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 != 0) goto L_0x01d4;
    L_0x01a7:
        r2 = r20.getTo();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = org.jitsi.gov.nist.javax.sip.Utils.getInstance();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = r3.generateTag();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2.setTag(r3);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
    L_0x01b6:
        r2 = r20.getCallId();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2 = r2.getCallId();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = r10.getCallId();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = r3.getCallId();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2 = r2.equals(r3);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 != 0) goto L_0x026b;
    L_0x01cc:
        r2 = new org.jitsi.javax.sip.SipException;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = "Dialog mismatch!";
        r2.m1626init(r3);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        throw r2;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
    L_0x01d4:
        r2 = r10.getLocalTag();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 == 0) goto L_0x0226;
    L_0x01da:
        r2 = r20.getToTag();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 != 0) goto L_0x0226;
    L_0x01e0:
        r2 = logger;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = 32;
        r2 = r2.isLoggingEnabled(r3);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 == 0) goto L_0x021c;
    L_0x01ea:
        r2 = logger;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3.<init>();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r4 = "assigning toTag : serverTransaction = ";
        r3 = r3.append(r4);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r0 = r22;
        r3 = r3.append(r0);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r4 = " dialog ";
        r3 = r3.append(r4);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = r3.append(r10);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r4 = " tag = ";
        r3 = r3.append(r4);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r4 = r10.getLocalTag();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = r3.append(r4);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = r3.toString();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2.logDebug(r3);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
    L_0x021c:
        r2 = r10.getLocalTag();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r0 = r20;
        r0.setToTag(r2);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        goto L_0x01b6;
    L_0x0226:
        r2 = r10.getLocalTag();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 == 0) goto L_0x01b6;
    L_0x022c:
        r2 = r20.getToTag();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 == 0) goto L_0x01b6;
    L_0x0232:
        r2 = r10.getLocalTag();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = r20.getToTag();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2 = r2.equals(r3);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 != 0) goto L_0x01b6;
    L_0x0240:
        r2 = new org.jitsi.javax.sip.SipException;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3.<init>();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r4 = "Tag mismatch dialogTag is ";
        r3 = r3.append(r4);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r4 = r10.getLocalTag();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = r3.append(r4);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r4 = " responseTag is ";
        r3 = r3.append(r4);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r4 = r20.getToTag();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = r3.append(r4);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = r3.toString();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2.m1626init(r3);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        throw r2;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
    L_0x026b:
        r0 = r22;
        r15 = r0.originalRequestFromTag;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2 = r22.getRequest();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 == 0) goto L_0x027f;
    L_0x0275:
        r2 = r22.getRequest();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2 = (org.jitsi.gov.nist.javax.sip.message.SIPRequest) r2;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r15 = r2.getFromTag();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
    L_0x027f:
        if (r15 == 0) goto L_0x0299;
    L_0x0281:
        r2 = r20.getFromTag();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 == 0) goto L_0x0299;
    L_0x0287:
        r2 = r20.getFromTag();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2 = r2.equals(r15);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 != 0) goto L_0x0299;
    L_0x0291:
        r2 = new org.jitsi.javax.sip.SipException;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = "From tag of request does not match response from tag";
        r2.m1626init(r3);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        throw r2;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
    L_0x0299:
        if (r15 == 0) goto L_0x02ef;
    L_0x029b:
        r2 = r20.getFrom();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2.setTag(r15);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
    L_0x02a2:
        if (r10 == 0) goto L_0x0301;
    L_0x02a4:
        r2 = 100;
        r0 = r21;
        if (r0 == r2) goto L_0x0301;
    L_0x02aa:
        r0 = r20;
        r10.setResponseTags(r0);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r16 = r10.getState();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r0 = r23;
        r0 = (org.jitsi.gov.nist.javax.sip.message.SIPResponse) r0;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2 = r0;
        r0 = r22;
        r10.setLastResponse(r0, r2);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r16 != 0) goto L_0x02d9;
    L_0x02bf:
        r2 = r10.getState();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = org.jitsi.javax.sip.DialogState.TERMINATED;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 != r3) goto L_0x02d9;
    L_0x02c7:
        r12 = new org.jitsi.javax.sip.DialogTerminatedEvent;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2 = r10.getSipProvider();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r12.m1633init(r2, r10);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2 = r10.getSipProvider();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r0 = r22;
        r2.handleEvent(r12, r0);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
    L_0x02d9:
        r0 = r23;
        r0 = (org.jitsi.gov.nist.javax.sip.message.SIPResponse) r0;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2 = r0;
        r0 = r22;
        r0.sendMessage(r2);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r10 == 0) goto L_0x02ee;
    L_0x02e5:
        r23 = (org.jitsi.gov.nist.javax.sip.message.SIPResponse) r23;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r0 = r22;
        r1 = r23;
        r10.startRetransmitTimer(r0, r1);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
    L_0x02ee:
        return;
    L_0x02ef:
        r2 = logger;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = 32;
        r2 = r2.isLoggingEnabled(r3);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 == 0) goto L_0x02a2;
    L_0x02f9:
        r2 = logger;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r3 = "WARNING -- Null From tag in request!!";
        r2.logDebug(r3);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        goto L_0x02a2;
    L_0x0301:
        if (r10 != 0) goto L_0x02d9;
    L_0x0303:
        r2 = r22.isInviteTransaction();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 == 0) goto L_0x02d9;
    L_0x0309:
        r0 = r22;
        r2 = r0.retransmissionAlertEnabled;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 == 0) goto L_0x02d9;
    L_0x030f:
        r0 = r22;
        r2 = r0.retransmissionAlertTimerTask;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        if (r2 != 0) goto L_0x02d9;
    L_0x0315:
        r2 = r23.getStatusCode();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2 = r2 / 100;
        r3 = 2;
        if (r2 != r3) goto L_0x02d9;
    L_0x031e:
        r0 = r23;
        r0 = (org.jitsi.gov.nist.javax.sip.message.SIPResponse) r0;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2 = r0;
        r3 = 1;
        r11 = r2.getDialogId(r3);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2 = new org.jitsi.gov.nist.javax.sip.stack.SIPServerTransaction$RetransmissionAlertTimerTask;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r0 = r22;
        r2.m1582init(r11);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r0 = r22;
        r0.retransmissionAlertTimerTask = r2;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r0 = r22;
        r2 = r0.sipStack;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2 = r2.retransmissionAlertTransactions;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r0 = r22;
        r2.put(r11, r0);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r0 = r22;
        r2 = r0.sipStack;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r2 = r2.getTimer();	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r0 = r22;
        r3 = r0.retransmissionAlertTimerTask;	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        r4 = 0;
        r6 = 500; // 0x1f4 float:7.0E-43 double:2.47E-321;
        r2.scheduleWithFixedDelay(r3, r4, r6);	 Catch:{ IOException -> 0x0106, ParseException -> 0x0150 }
        goto L_0x02d9;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.javax.sip.stack.SIPServerTransaction.sendResponse(org.jitsi.javax.sip.message.Response):void");
    }

    /* access modifiers changed from: protected */
    public int getRealState() {
        return super.getInternalState();
    }

    public TransactionState getState() {
        if (isInviteTransaction() && 1 == super.getInternalState()) {
            return TransactionState.PROCEEDING;
        }
        return super.getState();
    }

    public void setState(int newState) {
        if (newState == 5 && isReliable() && !getSIPStack().cacheServerConnections) {
            this.collectionTime = 64;
        }
        super.setState(newState);
    }

    /* access modifiers changed from: protected */
    public void startTransactionTimer() {
        if ((getMethod().equalsIgnoreCase("INVITE") || getMethod().equalsIgnoreCase(Request.CANCEL) || getMethod().equalsIgnoreCase("ACK")) && this.transactionTimerStarted.compareAndSet(false, true) && this.sipStack.getTimer() != null && this.sipStack.getTimer().isStarted()) {
            SIPStackTimerTask myTimer = new TransactionTimer();
            if (this.sipStack.getTimer() != null && this.sipStack.getTimer().isStarted()) {
                this.sipStack.getTimer().scheduleWithFixedDelay(myTimer, (long) this.BASE_TIMER_INTERVAL, (long) this.BASE_TIMER_INTERVAL);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void startTransactionTimerJ(long time) {
        if (this.transactionTimerStarted.compareAndSet(false, true) && this.sipStack.getTimer() != null && this.sipStack.getTimer().isStarted()) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("starting TransactionTimerJ() : " + getTransactionId() + " time " + time);
            }
            SIPStackTimerTask task = new SIPStackTimerTask() {
                public void runTask() {
                    if (SIPServerTransaction.logger.isLoggingEnabled(32)) {
                        SIPServerTransaction.logger.logDebug("executing TransactionTimerJ() : " + SIPServerTransaction.this.getTransactionId());
                    }
                    SIPServerTransaction.this.fireTimeoutTimer();
                    SIPServerTransaction.this.cleanUp();
                    if (SIPServerTransaction.this.originalRequest != null) {
                        SIPServerTransaction.this.originalRequest.cleanUp();
                    }
                }
            };
            if (time > 0) {
                this.sipStack.getTimer().schedule(task, (1 * time) * ((long) this.BASE_TIMER_INTERVAL));
            } else {
                task.runTask();
            }
        }
    }

    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(getClass())) {
            return false;
        }
        return getBranch().equalsIgnoreCase(((SIPServerTransaction) other).getBranch());
    }

    public Dialog getDialog() {
        if (this.dialog != null || this.dialogId == null) {
            return this.dialog;
        }
        return this.sipStack.getDialog(this.dialogId);
    }

    public void setDialog(SIPDialog sipDialog, String dialogId) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("setDialog " + this + " dialog = " + sipDialog);
        }
        this.dialog = sipDialog;
        this.dialogId = dialogId;
        if (dialogId != null) {
            sipDialog.setAssigned();
        }
        if (this.retransmissionAlertEnabled && this.retransmissionAlertTimerTask != null) {
            this.sipStack.getTimer().cancel(this.retransmissionAlertTimerTask);
            if (this.retransmissionAlertTimerTask.dialogId != null) {
                this.sipStack.retransmissionAlertTransactions.remove(this.retransmissionAlertTimerTask.dialogId);
            }
            this.retransmissionAlertTimerTask = null;
        }
        this.retransmissionAlertEnabled = false;
    }

    public void terminate() throws ObjectInUseException {
        setState(5);
        if (this.retransmissionAlertTimerTask != null) {
            this.sipStack.getTimer().cancel(this.retransmissionAlertTimerTask);
            if (this.retransmissionAlertTimerTask.dialogId != null) {
                this.sipStack.retransmissionAlertTransactions.remove(this.retransmissionAlertTimerTask.dialogId);
            }
            this.retransmissionAlertTimerTask = null;
        }
    }

    /* access modifiers changed from: protected */
    public void sendReliableProvisionalResponse(Response relResponse) throws SipException {
        if (this.pendingReliableResponseAsBytes != null) {
            throw new SipException("Unacknowledged response");
        }
        SIPResponse reliableResponse = (SIPResponse) relResponse;
        this.pendingReliableResponseAsBytes = reliableResponse.encodeAsBytes(getTransport());
        this.pendingReliableResponseMethod = reliableResponse.getCSeq().getMethod();
        this.pendingReliableCSeqNumber = reliableResponse.getCSeq().getSeqNumber();
        RSeq rseq = (RSeq) relResponse.getHeader("RSeq");
        if (relResponse.getHeader("RSeq") == null) {
            rseq = new RSeq();
            relResponse.setHeader(rseq);
        }
        try {
            if (this.rseqNumber < 0) {
                this.rseqNumber = (int) (Math.random() * 1000.0d);
            }
            this.rseqNumber++;
            rseq.setSeqNumber((long) this.rseqNumber);
            this.pendingReliableRSeqNumber = rseq.getSeqNumber();
            this.lastResponse = (SIPResponse) relResponse;
            if (getDialog() == null || !interlockProvisionalResponses || this.provisionalResponseSem.tryAcquire(1, TimeUnit.SECONDS)) {
                this.provisionalResponseTask = new ProvisionalResponseTask();
                this.sipStack.getTimer().scheduleWithFixedDelay(this.provisionalResponseTask, 0, 500);
                sendMessage((SIPMessage) relResponse);
                return;
            }
            throw new SipException("Unacknowledged reliable response");
        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
        }
    }

    public byte[] getReliableProvisionalResponse() {
        return this.pendingReliableResponseAsBytes;
    }

    public boolean prackRecieved() {
        if (this.pendingReliableResponseAsBytes == null) {
            return false;
        }
        if (this.provisionalResponseTask != null) {
            this.sipStack.getTimer().cancel(this.provisionalResponseTask);
            this.provisionalResponseTask = null;
        }
        this.pendingReliableResponseAsBytes = null;
        if (interlockProvisionalResponses && getDialog() != null) {
            this.provisionalResponseSem.release();
        }
        return true;
    }

    public void enableRetransmissionAlerts() throws SipException {
        if (getDialog() != null) {
            throw new SipException("Dialog associated with tx");
        } else if (isInviteTransaction()) {
            this.retransmissionAlertEnabled = true;
        } else {
            throw new SipException("Request Method must be INVITE");
        }
    }

    public boolean isRetransmissionAlertEnabled() {
        return this.retransmissionAlertEnabled;
    }

    public void disableRetransmissionAlerts() {
        if (this.retransmissionAlertTimerTask != null && this.retransmissionAlertEnabled) {
            this.sipStack.getTimer().cancel(this.retransmissionAlertTimerTask);
            this.retransmissionAlertEnabled = false;
            String dialogId = this.retransmissionAlertTimerTask.dialogId;
            if (dialogId != null) {
                this.sipStack.retransmissionAlertTransactions.remove(dialogId);
            }
            this.retransmissionAlertTimerTask = null;
        }
    }

    public void setAckSeen() {
        this.isAckSeen = true;
    }

    public boolean ackSeen() {
        return this.isAckSeen;
    }

    public void setMapped(boolean b) {
        this.isMapped = true;
    }

    public void setPendingSubscribe(SIPClientTransaction pendingSubscribeClientTx) {
        this.pendingSubscribeTransaction = pendingSubscribeClientTx;
    }

    public void releaseSem() {
        if (this.pendingSubscribeTransaction != null) {
            if (!this.sipStack.isDeliverUnsolicitedNotify()) {
                this.pendingSubscribeTransaction.releaseSem();
            }
        } else if (this.inviteTransaction != null && getMethod().equals(Request.CANCEL)) {
            this.inviteTransaction.releaseSem();
        }
        super.releaseSem();
    }

    public void setInviteTransaction(SIPServerTransaction st) {
        this.inviteTransaction = st;
    }

    public SIPServerTransaction getCanceledInviteTransaction() {
        return this.inviteTransaction;
    }

    public void scheduleAckRemoval() throws IllegalStateException {
        if (getMethod() == null || !getMethod().equals("ACK")) {
            throw new IllegalStateException("Method is null[" + (getMethod() == null) + "] or method is not ACK[" + getMethod() + "]");
        }
        startTransactionTimer();
    }

    public void cleanUp() {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("removing" + this);
        }
        if (isReleaseReferences()) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("cleanup : " + getTransactionId());
            }
            if (this.originalRequest == null && this.originalRequestBytes != null) {
                try {
                    this.originalRequest = (SIPRequest) this.sipStack.getMessageParserFactory().createMessageParser(this.sipStack).parseSIPMessage(this.originalRequestBytes, true, false, null);
                } catch (ParseException e) {
                    logger.logError("message " + this.originalRequestBytes + "could not be reparsed !");
                }
            } else if (this.originalRequest != null && this.originalRequestBytes == null) {
                this.originalRequestBytes = this.originalRequest.encodeAsBytes(getTransport());
            }
            this.sipStack.removeTransaction(this);
            cleanUpOnTimer();
            this.originalRequestFromTag = null;
            this.originalRequestSentBy = null;
            if (this.originalRequest != null) {
                this.originalRequest = null;
            }
            if (!(isReliable() || this.inviteTransaction == null)) {
                this.inviteTransaction = null;
            }
            this.lastResponse = null;
            if (!this.sipStack.cacheServerConnections) {
                MessageChannel messageChannel = getMessageChannel();
                int i = messageChannel.useCount - 1;
                messageChannel.useCount = i;
                if (i <= 0) {
                    close();
                    return;
                }
            }
            if (logger.isLoggingEnabled(32) && !this.sipStack.cacheServerConnections && isReliable()) {
                logger.logDebug("Use Count = " + getMessageChannel().useCount);
                return;
            }
            return;
        }
        this.sipStack.removeTransaction(this);
    }

    /* access modifiers changed from: protected */
    public void cleanUpOnTimer() {
        if (isReleaseReferences()) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("cleanup on timer : " + getTransactionId());
            }
            if (this.dialog != null && getMethod().equals(Request.CANCEL)) {
                this.dialogId = this.dialog.getDialogId();
            }
            this.dialog = null;
            if (!(this.inviteTransaction == null || getMethod().equals(Request.CANCEL))) {
                this.inviteTransaction.releaseSem();
                this.inviteTransaction = null;
            }
            if (this.originalRequest != null) {
                this.originalRequest.setTransaction(null);
                this.originalRequest.setInviteTransaction(null);
                if (!getMethod().equalsIgnoreCase("INVITE")) {
                    if (this.originalRequestSentBy == null) {
                        this.originalRequestSentBy = this.originalRequest.getTopmostVia().getSentBy();
                    }
                    if (this.originalRequestFromTag == null) {
                        this.originalRequestFromTag = this.originalRequest.getFromTag();
                    }
                }
                if (this.originalRequestBytes == null) {
                    this.originalRequestBytes = this.originalRequest.encodeAsBytes(getTransport());
                }
                if (!(getMethod().equalsIgnoreCase("INVITE") || getMethod().equalsIgnoreCase(Request.CANCEL))) {
                    this.originalRequest = null;
                }
            }
            if (this.lastResponse != null) {
                this.lastResponseAsBytes = this.lastResponse.encodeAsBytes(getTransport());
                this.lastResponse = null;
            }
            this.pendingReliableResponseAsBytes = null;
            this.pendingReliableResponseMethod = null;
            this.pendingSubscribeTransaction = null;
            this.provisionalResponseSem = null;
            this.retransmissionAlertTimerTask = null;
            this.requestOf = null;
            this.messageProcessor = null;
        }
    }

    public String getPendingReliableResponseMethod() {
        return this.pendingReliableResponseMethod;
    }

    public long getPendingReliableCSeqNumber() {
        return this.pendingReliableCSeqNumber;
    }

    public long getPendingReliableRSeqNumber() {
        return this.pendingReliableRSeqNumber;
    }
}
