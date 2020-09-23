package org.jitsi.gov.nist.javax.sip.stack;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.InternalErrorHandler;
import org.jitsi.gov.nist.core.NameValueList;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.javax.sip.DialogExt;
import org.jitsi.gov.nist.javax.sip.ListeningPointImpl;
import org.jitsi.gov.nist.javax.sip.SipListenerExt;
import org.jitsi.gov.nist.javax.sip.SipProviderImpl;
import org.jitsi.gov.nist.javax.sip.SipStackImpl;
import org.jitsi.gov.nist.javax.sip.Utils;
import org.jitsi.gov.nist.javax.sip.address.AddressImpl;
import org.jitsi.gov.nist.javax.sip.address.SipUri;
import org.jitsi.gov.nist.javax.sip.header.Authorization;
import org.jitsi.gov.nist.javax.sip.header.CSeq;
import org.jitsi.gov.nist.javax.sip.header.Contact;
import org.jitsi.gov.nist.javax.sip.header.ContactList;
import org.jitsi.gov.nist.javax.sip.header.From;
import org.jitsi.gov.nist.javax.sip.header.MaxForwards;
import org.jitsi.gov.nist.javax.sip.header.RAck;
import org.jitsi.gov.nist.javax.sip.header.RSeq;
import org.jitsi.gov.nist.javax.sip.header.Reason;
import org.jitsi.gov.nist.javax.sip.header.RecordRoute;
import org.jitsi.gov.nist.javax.sip.header.RecordRouteList;
import org.jitsi.gov.nist.javax.sip.header.Require;
import org.jitsi.gov.nist.javax.sip.header.Route;
import org.jitsi.gov.nist.javax.sip.header.RouteList;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.TimeStamp;
import org.jitsi.gov.nist.javax.sip.header.To;
import org.jitsi.gov.nist.javax.sip.header.Via;
import org.jitsi.gov.nist.javax.sip.message.MessageFactoryImpl;
import org.jitsi.gov.nist.javax.sip.message.SIPMessage;
import org.jitsi.gov.nist.javax.sip.message.SIPRequest;
import org.jitsi.gov.nist.javax.sip.message.SIPResponse;
import org.jitsi.gov.nist.javax.sip.parser.AddressParser;
import org.jitsi.gov.nist.javax.sip.parser.CallIDParser;
import org.jitsi.gov.nist.javax.sip.parser.ContactParser;
import org.jitsi.gov.nist.javax.sip.parser.RecordRouteParser;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.DialogDoesNotExistException;
import org.jitsi.javax.sip.DialogState;
import org.jitsi.javax.sip.IOExceptionEvent;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.ListeningPoint;
import org.jitsi.javax.sip.ObjectInUseException;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.Transaction;
import org.jitsi.javax.sip.TransactionDoesNotExistException;
import org.jitsi.javax.sip.TransactionState;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.address.Hop;
import org.jitsi.javax.sip.address.SipURI;
import org.jitsi.javax.sip.header.CallIdHeader;
import org.jitsi.javax.sip.header.ContactHeader;
import org.jitsi.javax.sip.header.EventHeader;
import org.jitsi.javax.sip.header.Header;
import org.jitsi.javax.sip.header.OptionTag;
import org.jitsi.javax.sip.header.ProxyAuthorizationHeader;
import org.jitsi.javax.sip.header.ReasonHeader;
import org.jitsi.javax.sip.header.RequireHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;

public class SIPDialog implements Dialog, DialogExt {
    public static final int CONFIRMED_STATE = 1;
    private static final int DIALOG_LINGER_TIME = 8;
    public static final int EARLY_STATE = 0;
    public static final int NULL_STATE = -1;
    public static final int TERMINATED_STATE = 3;
    /* access modifiers changed from: private|static */
    public static StackLogger logger = CommonLogger.getLogger(SIPDialog.class);
    private static final long serialVersionUID = -1429794423085204069L;
    private transient int ackLine;
    protected transient boolean ackProcessed;
    private transient Semaphore ackSem;
    private int ackSemTakenFor;
    private transient Object applicationData;
    public transient long auditTag;
    protected transient boolean byeSent;
    protected CallIdHeader callIdHeader;
    protected String callIdHeaderString;
    protected Contact contactHeader;
    protected String contactHeaderStringified;
    /* access modifiers changed from: private|transient */
    public transient DialogDeleteIfNoAckSentTask dialogDeleteIfNoAckSentTask;
    private transient DialogDeleteTask dialogDeleteTask;
    protected String dialogId;
    /* access modifiers changed from: private */
    public int dialogState;
    private transient boolean dialogTerminatedEventDelivered;
    protected transient String earlyDialogId;
    private int earlyDialogTimeout;
    private EarlyStateTimerTask earlyStateTimerTask;
    protected EventHeader eventHeader;
    private transient Set<SIPDialogEventListener> eventListeners;
    protected SIPTransaction firstTransaction;
    protected String firstTransactionId;
    protected boolean firstTransactionIsServerTransaction;
    protected String firstTransactionMergeId;
    protected String firstTransactionMethod;
    protected int firstTransactionPort;
    protected boolean firstTransactionSecure;
    protected boolean firstTransactionSeen;
    /* access modifiers changed from: private|transient */
    public transient long highestSequenceNumberAcknowledged;
    protected String hisTag;
    protected transient boolean isAcknowledged;
    protected transient boolean isAssigned;
    protected boolean isBackToBackUserAgent;
    protected Long lastAckReceivedCSeqNumber;
    protected transient SIPRequest lastAckSent;
    protected transient long lastInviteOkReceived;
    protected long lastResponseCSeqNumber;
    protected String lastResponseDialogId;
    protected String lastResponseFromTag;
    protected String lastResponseMethod;
    protected Integer lastResponseStatusCode;
    protected String lastResponseToTag;
    private Via lastResponseTopMostVia;
    protected SIPTransaction lastTransaction;
    protected Address localParty;
    protected String localPartyStringified;
    protected long localSequenceNumber;
    protected String method;
    protected String myTag;
    protected transient long nextSeqno;
    protected long originalLocalSequenceNumber;
    private transient SIPRequest originalRequest;
    protected transient RecordRouteList originalRequestRecordRouteHeaders;
    protected transient String originalRequestRecordRouteHeadersString;
    private boolean pendingRouteUpdateOn202Response;
    private transient int prevRetransmissionTicks;
    protected ProxyAuthorizationHeader proxyAuthorizationHeader;
    protected boolean reInviteFlag;
    protected transient int reInviteWaitTime;
    private boolean releaseReferences;
    protected Address remoteParty;
    protected String remotePartyStringified;
    protected long remoteSequenceNumber;
    protected Address remoteTarget;
    protected String remoteTargetStringified;
    private Set<String> responsesReceivedInForkingCase;
    private transient int retransmissionTicksLeft;
    protected RouteList routeList;
    protected boolean sequenceNumberValidation;
    protected boolean serverTransactionFlag;
    /* access modifiers changed from: private|transient */
    public transient SipProviderImpl sipProvider;
    /* access modifiers changed from: private|transient */
    public transient SIPTransactionStack sipStack;
    private transient String stackTrace;
    protected boolean terminateOnBye;
    protected transient DialogTimerTask timerTask;
    private Semaphore timerTaskLock;

    class DialogDeleteIfNoAckSentTask extends SIPStackTimerTask implements Serializable {
        private long seqno;

        public DialogDeleteIfNoAckSentTask(long seqno) {
            this.seqno = seqno;
        }

        public void runTask() {
            if (SIPDialog.this.highestSequenceNumberAcknowledged < this.seqno) {
                SIPDialog.this.dialogDeleteIfNoAckSentTask = null;
                if (SIPDialog.this.isBackToBackUserAgent) {
                    if (SIPDialog.logger.isLoggingEnabled()) {
                        SIPDialog.logger.logError("ACK Was not sent. Sending BYE");
                    }
                    if (SIPDialog.this.sipProvider.getSipListener() instanceof SipListenerExt) {
                        SIPDialog.this.raiseErrorEvent(2);
                        return;
                    }
                    try {
                        Request byeRequest = SIPDialog.this.createRequest("BYE");
                        if (MessageFactoryImpl.getDefaultUserAgentHeader() != null) {
                            byeRequest.addHeader(MessageFactoryImpl.getDefaultUserAgentHeader());
                        }
                        ReasonHeader reasonHeader = new Reason();
                        reasonHeader.setProtocol("SIP");
                        reasonHeader.setCause(1025);
                        reasonHeader.setText("Timed out waiting to send ACK");
                        byeRequest.addHeader(reasonHeader);
                        SIPDialog.this.sendRequest(SIPDialog.this.getSipProvider().getNewClientTransaction(byeRequest));
                        return;
                    } catch (Exception e) {
                        SIPDialog.this.delete();
                        return;
                    }
                }
                if (SIPDialog.logger.isLoggingEnabled()) {
                    SIPDialog.logger.logError("ACK Was not sent. killing dialog");
                }
                if (SIPDialog.this.sipProvider.getSipListener() instanceof SipListenerExt) {
                    SIPDialog.this.raiseErrorEvent(2);
                } else {
                    SIPDialog.this.delete();
                }
            }
        }
    }

    class DialogDeleteTask extends SIPStackTimerTask implements Serializable {
        DialogDeleteTask() {
        }

        public void runTask() {
            SIPDialog.this.delete();
        }
    }

    class DialogTimerTask extends SIPStackTimerTask implements Serializable {
        int nRetransmissions = 0;
        SIPServerTransaction transaction;

        public DialogTimerTask(SIPServerTransaction transaction) {
            this.transaction = transaction;
        }

        public void runTask() {
            SIPDialog dialog = SIPDialog.this;
            if (SIPDialog.logger.isLoggingEnabled(32)) {
                SIPDialog.logger.logDebug("Running dialog timer");
            }
            this.nRetransmissions++;
            SIPServerTransaction transaction = this.transaction;
            if (this.nRetransmissions > SIPDialog.this.sipStack.getAckTimeoutFactor() * 1) {
                if (SIPDialog.this.getSipProvider().getSipListener() == null || !(SIPDialog.this.getSipProvider().getSipListener() instanceof SipListenerExt)) {
                    SIPDialog.this.delete();
                } else {
                    SIPDialog.this.raiseErrorEvent(1);
                }
                if (!(transaction == null || transaction.getState() == TransactionState.TERMINATED)) {
                    transaction.raiseErrorEvent(1);
                }
            } else if (!(transaction == null || dialog.isAckSeen() || SIPDialog.this.lastResponseStatusCode.intValue() / 100 != 2)) {
                SIPTransactionStack stack;
                try {
                    if (dialog.toRetransmitFinalResponse(transaction.T2)) {
                        transaction.resendLastResponseAsBytes();
                    }
                    stack = dialog.sipStack;
                    if (SIPDialog.logger.isLoggingEnabled(32)) {
                        SIPDialog.logger.logDebug("resend 200 response from " + dialog);
                    }
                } catch (IOException e) {
                    SIPDialog.this.raiseIOException(transaction.getPeerAddress(), transaction.getPeerPort(), transaction.getPeerProtocol());
                    stack = dialog.sipStack;
                    if (SIPDialog.logger.isLoggingEnabled(32)) {
                        SIPDialog.logger.logDebug("resend 200 response from " + dialog);
                    }
                } catch (Throwable th) {
                    stack = dialog.sipStack;
                    if (SIPDialog.logger.isLoggingEnabled(32)) {
                        SIPDialog.logger.logDebug("resend 200 response from " + dialog);
                    }
                    transaction.fireTimer();
                }
                transaction.fireTimer();
            }
            if (dialog.isAckSeen() || dialog.dialogState == 3) {
                this.transaction = null;
                SIPDialog.this.getStack().getTimer().cancel(this);
            }
        }

        public void cleanUpBeforeCancel() {
            this.transaction = null;
            SIPDialog.this.lastAckSent = null;
            SIPDialog.this.cleanUpOnAck();
            super.cleanUpBeforeCancel();
        }
    }

    class EarlyStateTimerTask extends SIPStackTimerTask implements Serializable {
        public void runTask() {
            try {
                if (SIPDialog.this.getState().equals(DialogState.EARLY)) {
                    SIPDialog.this.raiseErrorEvent(4);
                } else if (SIPDialog.logger.isLoggingEnabled(32)) {
                    SIPDialog.logger.logDebug("EarlyStateTimerTask : Dialog state is " + SIPDialog.this.getState());
                }
            } catch (Exception ex) {
                SIPDialog.logger.logError("Unexpected exception delivering event", ex);
            }
        }
    }

    class LingerTimer extends SIPStackTimerTask implements Serializable {
        LingerTimer() {
        }

        public void runTask() {
            SIPDialog.this.sipStack.removeDialog(SIPDialog.this);
            if (((SipStackImpl) SIPDialog.this.getStack()).isReEntrantListener()) {
                SIPDialog.this.cleanUp();
            }
        }
    }

    public class ReInviteSender implements Runnable, Serializable {
        private static final long serialVersionUID = 1019346148741070635L;
        ClientTransaction ctx;

        public void terminate() {
            try {
                this.ctx.terminate();
                Thread.currentThread().interrupt();
            } catch (ObjectInUseException e) {
                SIPDialog.logger.logError("unexpected error", e);
            }
        }

        public ReInviteSender(ClientTransaction ctx) {
            this.ctx = ctx;
        }

        /* JADX WARNING: Unknown top exception splitter block from list: {B:30:0x0095=Splitter:B:30:0x0095, B:20:0x0069=Splitter:B:20:0x0069} */
        public void run() {
            /*
            r13 = this;
            r12 = 0;
            r8 = 0;
            r6 = java.lang.System.currentTimeMillis();	 Catch:{ Exception -> 0x00cc }
            r2 = 0;
            r5 = org.jitsi.gov.nist.javax.sip.stack.SIPDialog.this;	 Catch:{ Exception -> 0x00cc }
            r5 = r5.takeAckSem();	 Catch:{ Exception -> 0x00cc }
            if (r5 != 0) goto L_0x004b;
        L_0x0010:
            r5 = org.jitsi.gov.nist.javax.sip.stack.SIPDialog.logger;	 Catch:{ Exception -> 0x00cc }
            r5 = r5.isLoggingEnabled();	 Catch:{ Exception -> 0x00cc }
            if (r5 == 0) goto L_0x0023;
        L_0x001a:
            r5 = org.jitsi.gov.nist.javax.sip.stack.SIPDialog.logger;	 Catch:{ Exception -> 0x00cc }
            r10 = "Could not send re-INVITE time out ClientTransaction";
            r5.logError(r10);	 Catch:{ Exception -> 0x00cc }
        L_0x0023:
            r5 = r13.ctx;	 Catch:{ Exception -> 0x00cc }
            r5 = (org.jitsi.gov.nist.javax.sip.stack.SIPClientTransaction) r5;	 Catch:{ Exception -> 0x00cc }
            r5.fireTimeoutTimer();	 Catch:{ Exception -> 0x00cc }
            r5 = org.jitsi.gov.nist.javax.sip.stack.SIPDialog.this;	 Catch:{ Exception -> 0x00cc }
            r5 = r5.sipProvider;	 Catch:{ Exception -> 0x00cc }
            r5 = r5.getSipListener();	 Catch:{ Exception -> 0x00cc }
            if (r5 == 0) goto L_0x0095;
        L_0x0036:
            r5 = org.jitsi.gov.nist.javax.sip.stack.SIPDialog.this;	 Catch:{ Exception -> 0x00cc }
            r5 = r5.sipProvider;	 Catch:{ Exception -> 0x00cc }
            r5 = r5.getSipListener();	 Catch:{ Exception -> 0x00cc }
            r5 = r5 instanceof org.jitsi.gov.nist.javax.sip.SipListenerExt;	 Catch:{ Exception -> 0x00cc }
            if (r5 == 0) goto L_0x0095;
        L_0x0044:
            r2 = 1;
            r5 = org.jitsi.gov.nist.javax.sip.stack.SIPDialog.this;	 Catch:{ Exception -> 0x00cc }
            r10 = 3;
            r5.raiseErrorEvent(r10);	 Catch:{ Exception -> 0x00cc }
        L_0x004b:
            r5 = org.jitsi.gov.nist.javax.sip.stack.SIPDialog.this;	 Catch:{ Exception -> 0x00cc }
            r5 = r5.getState();	 Catch:{ Exception -> 0x00cc }
            r10 = org.jitsi.javax.sip.DialogState.TERMINATED;	 Catch:{ Exception -> 0x00cc }
            if (r5 == r10) goto L_0x005b;
        L_0x0055:
            r10 = java.lang.System.currentTimeMillis();	 Catch:{ Exception -> 0x00cc }
            r8 = r10 - r6;
        L_0x005b:
            r10 = 0;
            r5 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));
            if (r5 == 0) goto L_0x0069;
        L_0x0061:
            r5 = org.jitsi.gov.nist.javax.sip.stack.SIPDialog.this;	 Catch:{ InterruptedException -> 0x00db }
            r5 = r5.reInviteWaitTime;	 Catch:{ InterruptedException -> 0x00db }
            r10 = (long) r5;	 Catch:{ InterruptedException -> 0x00db }
            java.lang.Thread.sleep(r10);	 Catch:{ InterruptedException -> 0x00db }
        L_0x0069:
            r5 = org.jitsi.gov.nist.javax.sip.stack.SIPDialog.this;	 Catch:{ Exception -> 0x00cc }
            r5 = r5.getState();	 Catch:{ Exception -> 0x00cc }
            r10 = org.jitsi.javax.sip.DialogState.TERMINATED;	 Catch:{ Exception -> 0x00cc }
            if (r5 == r10) goto L_0x007d;
        L_0x0073:
            if (r2 != 0) goto L_0x007d;
        L_0x0075:
            r5 = org.jitsi.gov.nist.javax.sip.stack.SIPDialog.this;	 Catch:{ Exception -> 0x00cc }
            r10 = r13.ctx;	 Catch:{ Exception -> 0x00cc }
            r11 = 1;
            r5.sendRequest(r10, r11);	 Catch:{ Exception -> 0x00cc }
        L_0x007d:
            r5 = org.jitsi.gov.nist.javax.sip.stack.SIPDialog.logger;	 Catch:{ Exception -> 0x00cc }
            r10 = 32;
            r5 = r5.isLoggingEnabled(r10);	 Catch:{ Exception -> 0x00cc }
            if (r5 == 0) goto L_0x0092;
        L_0x0089:
            r5 = org.jitsi.gov.nist.javax.sip.stack.SIPDialog.logger;	 Catch:{ Exception -> 0x00cc }
            r10 = "re-INVITE successfully sent";
            r5.logDebug(r10);	 Catch:{ Exception -> 0x00cc }
        L_0x0092:
            r13.ctx = r12;
            return;
        L_0x0095:
            r5 = org.jitsi.gov.nist.javax.sip.stack.SIPDialog.this;	 Catch:{ Exception -> 0x00cc }
            r10 = "BYE";
            r1 = r5.createRequest(r10);	 Catch:{ Exception -> 0x00cc }
            r5 = org.jitsi.gov.nist.javax.sip.message.MessageFactoryImpl.getDefaultUserAgentHeader();	 Catch:{ Exception -> 0x00cc }
            if (r5 == 0) goto L_0x00aa;
        L_0x00a3:
            r5 = org.jitsi.gov.nist.javax.sip.message.MessageFactoryImpl.getDefaultUserAgentHeader();	 Catch:{ Exception -> 0x00cc }
            r1.addHeader(r5);	 Catch:{ Exception -> 0x00cc }
        L_0x00aa:
            r4 = new org.jitsi.gov.nist.javax.sip.header.Reason;	 Catch:{ Exception -> 0x00cc }
            r4.m1200init();	 Catch:{ Exception -> 0x00cc }
            r5 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;
            r4.setCause(r5);	 Catch:{ Exception -> 0x00cc }
            r5 = "Timed out waiting to re-INVITE";
            r4.setText(r5);	 Catch:{ Exception -> 0x00cc }
            r1.addHeader(r4);	 Catch:{ Exception -> 0x00cc }
            r5 = org.jitsi.gov.nist.javax.sip.stack.SIPDialog.this;	 Catch:{ Exception -> 0x00cc }
            r5 = r5.getSipProvider();	 Catch:{ Exception -> 0x00cc }
            r0 = r5.getNewClientTransaction(r1);	 Catch:{ Exception -> 0x00cc }
            r5 = org.jitsi.gov.nist.javax.sip.stack.SIPDialog.this;	 Catch:{ Exception -> 0x00cc }
            r5.sendRequest(r0);	 Catch:{ Exception -> 0x00cc }
            goto L_0x0092;
        L_0x00cc:
            r3 = move-exception;
            r5 = org.jitsi.gov.nist.javax.sip.stack.SIPDialog.logger;	 Catch:{ all -> 0x00d7 }
            r10 = "Error sending re-INVITE";
            r5.logError(r10, r3);	 Catch:{ all -> 0x00d7 }
            goto L_0x0092;
        L_0x00d7:
            r5 = move-exception;
            r13.ctx = r12;
            throw r5;
        L_0x00db:
            r3 = move-exception;
            r5 = org.jitsi.gov.nist.javax.sip.stack.SIPDialog.logger;	 Catch:{ Exception -> 0x00cc }
            r10 = 32;
            r5 = r5.isLoggingEnabled(r10);	 Catch:{ Exception -> 0x00cc }
            if (r5 == 0) goto L_0x0092;
        L_0x00e8:
            r5 = org.jitsi.gov.nist.javax.sip.stack.SIPDialog.logger;	 Catch:{ Exception -> 0x00cc }
            r10 = "Interrupted sleep";
            r5.logDebug(r10);	 Catch:{ Exception -> 0x00cc }
            goto L_0x0092;
            */
            throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.javax.sip.stack.SIPDialog$ReInviteSender.run():void");
        }
    }

    private SIPDialog(SipProviderImpl provider) {
        this.auditTag = 0;
        this.ackSem = new Semaphore(1);
        this.reInviteWaitTime = 100;
        this.highestSequenceNumberAcknowledged = -1;
        this.sequenceNumberValidation = true;
        this.timerTaskLock = new Semaphore(1);
        this.firstTransactionPort = 5060;
        this.earlyDialogTimeout = Response.RINGING;
        this.responsesReceivedInForkingCase = new HashSet(0);
        this.terminateOnBye = true;
        this.routeList = new RouteList();
        this.dialogState = -1;
        this.localSequenceNumber = 0;
        this.remoteSequenceNumber = -1;
        this.sipProvider = provider;
        this.eventListeners = new CopyOnWriteArraySet();
        this.earlyDialogTimeout = ((SIPTransactionStack) provider.getSipStack()).getEarlyDialogTimeout();
    }

    private void recordStackTrace() {
        StringWriter stringWriter = new StringWriter();
        new Exception().printStackTrace(new PrintWriter(stringWriter));
        String stackTraceSignature = Integer.toString(Math.abs(new Random().nextInt()));
        logger.logDebug("TraceRecord = " + stackTraceSignature);
        this.stackTrace = "TraceRecord = " + stackTraceSignature + Separators.COLON + stringWriter.getBuffer().toString();
    }

    public SIPDialog(SIPTransaction transaction) {
        this(transaction.getSipProvider());
        SIPRequest sipRequest = (SIPRequest) transaction.getRequest();
        this.callIdHeader = sipRequest.getCallId();
        this.earlyDialogId = sipRequest.getDialogId(false);
        if (transaction == null) {
            throw new NullPointerException("Null tx");
        }
        this.sipStack = transaction.sipStack;
        this.sipProvider = transaction.getSipProvider();
        if (this.sipProvider == null) {
            throw new NullPointerException("Null Provider!");
        }
        this.isBackToBackUserAgent = this.sipStack.isBackToBackUserAgent;
        addTransaction(transaction);
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Creating a dialog : " + this);
            logger.logDebug("provider port = " + this.sipProvider.getListeningPoint().getPort());
            logger.logStackTrace();
        }
        addEventListener(this.sipStack);
        this.releaseReferences = this.sipStack.isAggressiveCleanup();
    }

    public SIPDialog(SIPClientTransaction transaction, SIPResponse sipResponse) {
        this((SIPTransaction) transaction);
        if (sipResponse == null) {
            throw new NullPointerException("Null SipResponse");
        }
        setLastResponse(transaction, sipResponse);
        this.isBackToBackUserAgent = this.sipStack.isBackToBackUserAgent;
    }

    public SIPDialog(SipProviderImpl sipProvider, SIPResponse sipResponse) {
        this(sipProvider);
        this.sipStack = (SIPTransactionStack) sipProvider.getSipStack();
        setLastResponse(null, sipResponse);
        this.localSequenceNumber = sipResponse.getCSeq().getSeqNumber();
        this.originalLocalSequenceNumber = this.localSequenceNumber;
        setLocalTag(sipResponse.getFrom().getTag());
        setRemoteTag(sipResponse.getTo().getTag());
        this.localParty = sipResponse.getFrom().getAddress();
        this.remoteParty = sipResponse.getTo().getAddress();
        this.method = sipResponse.getCSeq().getMethod();
        this.callIdHeader = sipResponse.getCallId();
        this.serverTransactionFlag = false;
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Creating a dialog : " + this);
            logger.logStackTrace();
        }
        this.isBackToBackUserAgent = this.sipStack.isBackToBackUserAgent;
        addEventListener(this.sipStack);
        this.releaseReferences = this.sipStack.isAggressiveCleanup();
    }

    private void printRouteList() {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("this : " + this);
            logger.logDebug("printRouteList : " + this.routeList.encode());
        }
    }

    /* access modifiers changed from: private */
    public void raiseIOException(String host, int port, String protocol) {
        this.sipProvider.handleEvent(new IOExceptionEvent(this, host, port, protocol), null);
        setState(3);
    }

    /* access modifiers changed from: private */
    public void raiseErrorEvent(int dialogTimeoutError) {
        SIPDialogErrorEvent newErrorEvent = new SIPDialogErrorEvent(this, dialogTimeoutError);
        synchronized (this.eventListeners) {
            for (SIPDialogEventListener nextListener : this.eventListeners) {
                nextListener.dialogErrorEvent(newErrorEvent);
            }
        }
        this.eventListeners.clear();
        if (!(dialogTimeoutError == 2 || dialogTimeoutError == 1 || dialogTimeoutError == 4 || dialogTimeoutError == 3)) {
            delete();
        }
        stopTimer();
    }

    /* access modifiers changed from: protected */
    public void setRemoteParty(SIPMessage sipMessage) {
        if (isServer()) {
            this.remoteParty = sipMessage.getFrom().getAddress();
        } else {
            this.remoteParty = sipMessage.getTo().getAddress();
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("settingRemoteParty " + this.remoteParty);
        }
    }

    private void addRoute(RecordRouteList recordRouteList) {
        Iterator it;
        SipURI sipUri;
        try {
            ListIterator li;
            RecordRoute rr;
            Route route;
            if (isServer()) {
                this.routeList = new RouteList();
                li = recordRouteList.listIterator();
                while (li.hasNext()) {
                    rr = (RecordRoute) li.next();
                    if (true) {
                        route = new Route();
                        route.setAddress((AddressImpl) ((AddressImpl) rr.getAddress()).clone());
                        route.setParameters((NameValueList) rr.getParameters().clone());
                        this.routeList.add((SIPHeader) route);
                    }
                }
            } else {
                this.routeList = new RouteList();
                li = recordRouteList.listIterator(recordRouteList.size());
                while (li.hasPrevious()) {
                    rr = (RecordRoute) li.previous();
                    if (true) {
                        route = new Route();
                        route.setAddress((AddressImpl) ((AddressImpl) rr.getAddress()).clone());
                        route.setParameters((NameValueList) rr.getParameters().clone());
                        this.routeList.add((SIPHeader) route);
                    }
                }
            }
            if (logger.isLoggingEnabled()) {
                it = this.routeList.iterator();
                while (it.hasNext()) {
                    sipUri = (SipURI) ((Route) it.next()).getAddress().getURI();
                    if (sipUri.hasLrParam()) {
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("route = " + sipUri);
                        }
                    } else if (logger.isLoggingEnabled()) {
                        logger.logWarning("NON LR route in Route set detected for dialog : " + this);
                        logger.logStackTrace();
                    }
                }
            }
        } catch (Throwable th) {
            Throwable th2 = th;
            if (logger.isLoggingEnabled()) {
                it = this.routeList.iterator();
                while (it.hasNext()) {
                    sipUri = (SipURI) ((Route) it.next()).getAddress().getURI();
                    if (sipUri.hasLrParam()) {
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("route = " + sipUri);
                        }
                    } else if (logger.isLoggingEnabled()) {
                        logger.logWarning("NON LR route in Route set detected for dialog : " + this);
                        logger.logStackTrace();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setRemoteTarget(ContactHeader contact) {
        this.remoteTarget = contact.getAddress();
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Dialog.setRemoteTarget: " + this.remoteTarget);
            logger.logStackTrace();
        }
    }

    private synchronized void addRoute(SIPResponse sipResponse) {
        try {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("setContact: dialogState: " + this + "state = " + getState());
            }
            if (sipResponse.getStatusCode() != 100) {
                if (this.dialogState != 3) {
                    ContactList contactList;
                    if (this.dialogState == 1) {
                        if (sipResponse.getStatusCode() / 100 == 2 && !isServer()) {
                            contactList = sipResponse.getContactHeaders();
                            if (contactList != null && SIPRequest.isTargetRefresh(sipResponse.getCSeq().getMethod())) {
                                setRemoteTarget((ContactHeader) contactList.getFirst());
                            }
                        }
                        if (!this.pendingRouteUpdateOn202Response) {
                            if (logger.isLoggingEnabled(32)) {
                                logger.logStackTrace();
                            }
                        }
                    }
                    if (!isServer() || this.pendingRouteUpdateOn202Response) {
                        if (!(getState() == DialogState.CONFIRMED || getState() == DialogState.TERMINATED) || this.pendingRouteUpdateOn202Response) {
                            RecordRouteList rrlist = sipResponse.getRecordRouteHeaders();
                            if (rrlist != null) {
                                addRoute(rrlist);
                            } else {
                                this.routeList = new RouteList();
                            }
                        }
                        contactList = sipResponse.getContactHeaders();
                        if (contactList != null) {
                            setRemoteTarget((ContactHeader) contactList.getFirst());
                        }
                    }
                    if (logger.isLoggingEnabled(32)) {
                        logger.logStackTrace();
                    }
                } else if (logger.isLoggingEnabled(32)) {
                    logger.logStackTrace();
                }
            }
        } finally {
            if (logger.isLoggingEnabled(32)) {
                logger.logStackTrace();
            }
        }
    }

    private synchronized RouteList getRouteList() {
        RouteList routeList;
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("getRouteList " + this);
        }
        routeList = new RouteList();
        routeList = new RouteList();
        if (this.routeList != null) {
            ListIterator li = this.routeList.listIterator();
            while (li.hasNext()) {
                routeList.add((SIPHeader) (Route) ((Route) li.next()).clone());
            }
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("----- ");
            logger.logDebug("getRouteList for " + this);
            if (routeList != null) {
                logger.logDebug("RouteList = " + routeList.encode());
            }
            if (this.routeList != null) {
                logger.logDebug("myRouteList = " + this.routeList.encode());
            }
            logger.logDebug("----- ");
        }
        return routeList;
    }

    /* access modifiers changed from: 0000 */
    public void setRouteList(RouteList routeList) {
        this.routeList = routeList;
    }

    /* JADX WARNING: Removed duplicated region for block: B:81:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x024b  */
    private void sendAck(org.jitsi.javax.sip.message.Request r15, boolean r16) throws org.jitsi.javax.sip.SipException {
        /*
        r14 = this;
        r0 = r15;
        r0 = (org.jitsi.gov.nist.javax.sip.message.SIPRequest) r0;
        r9 = logger;
        r10 = 32;
        r9 = r9.isLoggingEnabled(r10);
        if (r9 == 0) goto L_0x0025;
    L_0x000d:
        r9 = logger;
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "sendAck";
        r10 = r10.append(r11);
        r10 = r10.append(r14);
        r10 = r10.toString();
        r9.logDebug(r10);
    L_0x0025:
        r9 = r0.getMethod();
        r10 = "ACK";
        r9 = r9.equals(r10);
        if (r9 != 0) goto L_0x0039;
    L_0x0031:
        r9 = new org.jitsi.javax.sip.SipException;
        r10 = "Bad request method -- should be ACK";
        r9.m1626init(r10);
        throw r9;
    L_0x0039:
        r9 = r14.getState();
        if (r9 == 0) goto L_0x0049;
    L_0x003f:
        r9 = r14.getState();
        r9 = r9.getValue();
        if (r9 != 0) goto L_0x0095;
    L_0x0049:
        r9 = logger;
        r10 = 4;
        r9 = r9.isLoggingEnabled(r10);
        if (r9 == 0) goto L_0x0078;
    L_0x0052:
        r9 = logger;
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "Bad Dialog State for ";
        r10 = r10.append(r11);
        r10 = r10.append(r14);
        r11 = " dialogID = ";
        r10 = r10.append(r11);
        r11 = r14.getDialogId();
        r10 = r10.append(r11);
        r10 = r10.toString();
        r9.logError(r10);
    L_0x0078:
        r9 = new org.jitsi.javax.sip.SipException;
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "Bad dialog state ";
        r10 = r10.append(r11);
        r11 = r14.getState();
        r10 = r10.append(r11);
        r10 = r10.toString();
        r9.m1626init(r10);
        throw r9;
    L_0x0095:
        r9 = r14.getCallId();
        r10 = r9.getCallId();
        r9 = r15;
        r9 = (org.jitsi.gov.nist.javax.sip.message.SIPRequest) r9;
        r9 = r9.getCallId();
        r9 = r9.getCallId();
        r9 = r10.equals(r9);
        if (r9 != 0) goto L_0x0114;
    L_0x00ae:
        r9 = logger;
        r10 = 32;
        r9 = r9.isLoggingEnabled(r10);
        if (r9 == 0) goto L_0x010c;
    L_0x00b8:
        r9 = logger;
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "CallID ";
        r10 = r10.append(r11);
        r11 = r14.getCallId();
        r10 = r10.append(r11);
        r10 = r10.toString();
        r9.logError(r10);
        r9 = logger;
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "RequestCallID = ";
        r10 = r10.append(r11);
        r11 = r0.getCallId();
        r11 = r11.getCallId();
        r10 = r10.append(r11);
        r10 = r10.toString();
        r9.logError(r10);
        r9 = logger;
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "dialog =  ";
        r10 = r10.append(r11);
        r10 = r10.append(r14);
        r10 = r10.toString();
        r9.logError(r10);
    L_0x010c:
        r9 = new org.jitsi.javax.sip.SipException;
        r10 = "Bad call ID in request";
        r9.m1626init(r10);
        throw r9;
    L_0x0114:
        r9 = logger;	 Catch:{ ParseException -> 0x01a0 }
        r10 = 32;
        r9 = r9.isLoggingEnabled(r10);	 Catch:{ ParseException -> 0x01a0 }
        if (r9 == 0) goto L_0x016e;
    L_0x011e:
        r9 = logger;	 Catch:{ ParseException -> 0x01a0 }
        r10 = new java.lang.StringBuilder;	 Catch:{ ParseException -> 0x01a0 }
        r10.<init>();	 Catch:{ ParseException -> 0x01a0 }
        r11 = "setting from tag For outgoing ACK= ";
        r10 = r10.append(r11);	 Catch:{ ParseException -> 0x01a0 }
        r11 = r14.getLocalTag();	 Catch:{ ParseException -> 0x01a0 }
        r10 = r10.append(r11);	 Catch:{ ParseException -> 0x01a0 }
        r10 = r10.toString();	 Catch:{ ParseException -> 0x01a0 }
        r9.logDebug(r10);	 Catch:{ ParseException -> 0x01a0 }
        r9 = logger;	 Catch:{ ParseException -> 0x01a0 }
        r10 = new java.lang.StringBuilder;	 Catch:{ ParseException -> 0x01a0 }
        r10.<init>();	 Catch:{ ParseException -> 0x01a0 }
        r11 = "setting To tag for outgoing ACK = ";
        r10 = r10.append(r11);	 Catch:{ ParseException -> 0x01a0 }
        r11 = r14.getRemoteTag();	 Catch:{ ParseException -> 0x01a0 }
        r10 = r10.append(r11);	 Catch:{ ParseException -> 0x01a0 }
        r10 = r10.toString();	 Catch:{ ParseException -> 0x01a0 }
        r9.logDebug(r10);	 Catch:{ ParseException -> 0x01a0 }
        r9 = logger;	 Catch:{ ParseException -> 0x01a0 }
        r10 = new java.lang.StringBuilder;	 Catch:{ ParseException -> 0x01a0 }
        r10.<init>();	 Catch:{ ParseException -> 0x01a0 }
        r11 = "ack = ";
        r10 = r10.append(r11);	 Catch:{ ParseException -> 0x01a0 }
        r10 = r10.append(r0);	 Catch:{ ParseException -> 0x01a0 }
        r10 = r10.toString();	 Catch:{ ParseException -> 0x01a0 }
        r9.logDebug(r10);	 Catch:{ ParseException -> 0x01a0 }
    L_0x016e:
        r9 = r14.getLocalTag();	 Catch:{ ParseException -> 0x01a0 }
        if (r9 == 0) goto L_0x017f;
    L_0x0174:
        r9 = r0.getFrom();	 Catch:{ ParseException -> 0x01a0 }
        r10 = r14.getLocalTag();	 Catch:{ ParseException -> 0x01a0 }
        r9.setTag(r10);	 Catch:{ ParseException -> 0x01a0 }
    L_0x017f:
        r9 = r14.getRemoteTag();	 Catch:{ ParseException -> 0x01a0 }
        if (r9 == 0) goto L_0x0190;
    L_0x0185:
        r9 = r0.getTo();	 Catch:{ ParseException -> 0x01a0 }
        r10 = r14.getRemoteTag();	 Catch:{ ParseException -> 0x01a0 }
        r9.setTag(r10);	 Catch:{ ParseException -> 0x01a0 }
    L_0x0190:
        r9 = r14.sipStack;
        r4 = r9.getNextHop(r0);
        if (r4 != 0) goto L_0x01ab;
    L_0x0198:
        r9 = new org.jitsi.javax.sip.SipException;
        r10 = "No route!";
        r9.m1626init(r10);
        throw r9;
    L_0x01a0:
        r1 = move-exception;
        r9 = new org.jitsi.javax.sip.SipException;
        r10 = r1.getMessage();
        r9.m1626init(r10);
        throw r9;
    L_0x01ab:
        r9 = logger;	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r10 = 32;
        r9 = r9.isLoggingEnabled(r10);	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        if (r9 == 0) goto L_0x01cd;
    L_0x01b5:
        r9 = logger;	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r10 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r10.<init>();	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r11 = "hop = ";
        r10 = r10.append(r11);	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r10 = r10.append(r4);	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r10 = r10.toString();	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r9.logDebug(r10);	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
    L_0x01cd:
        r9 = r14.sipProvider;	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r10 = r4.getTransport();	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r6 = r9.getListeningPoint(r10);	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r6 = (org.jitsi.gov.nist.javax.sip.ListeningPointImpl) r6;	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        if (r6 != 0) goto L_0x01ff;
    L_0x01db:
        r9 = new org.jitsi.javax.sip.SipException;	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r10 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r10.<init>();	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r11 = "No listening point for this provider registered at ";
        r10 = r10.append(r11);	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r10 = r10.append(r4);	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r10 = r10.toString();	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r9.m1626init(r10);	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        throw r9;	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
    L_0x01f4:
        r1 = move-exception;
        if (r16 == 0) goto L_0x0298;
    L_0x01f7:
        r9 = new org.jitsi.javax.sip.SipException;
        r10 = "Could not send ack";
        r9.m1627init(r10, r1);
        throw r9;
    L_0x01ff:
        r9 = r4.getHost();	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r5 = java.net.InetAddress.getByName(r9);	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r9 = r6.getMessageProcessor();	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r10 = r4.getPort();	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r7 = r9.createMessageChannel(r5, r10);	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r8 = 0;
        r15 = (org.jitsi.gov.nist.javax.sip.message.SIPRequest) r15;	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r9 = r15.getCSeq();	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r2 = r9.getSeqNumber();	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r9 = r14.isAckSent(r2);	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        if (r9 != 0) goto L_0x0225;
    L_0x0224:
        r8 = 1;
    L_0x0225:
        r14.setLastAckSent(r0);	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r7.sendMessage(r0);	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r9 = 1;
        r14.isAcknowledged = r9;	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r10 = r14.highestSequenceNumberAcknowledged;	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r9 = r0.getCSeq();	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r12 = r9.getSeqNumber();	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r10 = java.lang.Math.max(r10, r12);	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r14.highestSequenceNumberAcknowledged = r10;	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        if (r8 == 0) goto L_0x025c;
    L_0x0240:
        r9 = r14.isBackToBackUserAgent;	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        if (r9 == 0) goto L_0x025c;
    L_0x0244:
        r14.releaseAckSem();	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
    L_0x0247:
        r9 = r14.dialogDeleteTask;
        if (r9 == 0) goto L_0x025b;
    L_0x024b:
        r9 = r14.getStack();
        r9 = r9.getTimer();
        r10 = r14.dialogDeleteTask;
        r9.cancel(r10);
        r9 = 0;
        r14.dialogDeleteTask = r9;
    L_0x025b:
        return;
    L_0x025c:
        r9 = logger;	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r10 = 32;
        r9 = r9.isLoggingEnabled(r10);	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        if (r9 == 0) goto L_0x0247;
    L_0x0266:
        r9 = logger;	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r10 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r10.<init>();	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r11 = "Not releasing ack sem for ";
        r10 = r10.append(r11);	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r10 = r10.append(r14);	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r11 = " isAckSent ";
        r10 = r10.append(r11);	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r10 = r10.append(r8);	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r10 = r10.toString();	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        r9.logDebug(r10);	 Catch:{ IOException -> 0x01f4, SipException -> 0x0289, Exception -> 0x02a8 }
        goto L_0x0247;
    L_0x0289:
        r1 = move-exception;
        r9 = logger;
        r9 = r9.isLoggingEnabled();
        if (r9 == 0) goto L_0x0297;
    L_0x0292:
        r9 = logger;
        r9.logException(r1);
    L_0x0297:
        throw r1;
    L_0x0298:
        r9 = r4.getHost();
        r10 = r4.getPort();
        r11 = r4.getTransport();
        r14.raiseIOException(r9, r10, r11);
        goto L_0x0247;
    L_0x02a8:
        r1 = move-exception;
        r9 = logger;
        r9 = r9.isLoggingEnabled();
        if (r9 == 0) goto L_0x02b6;
    L_0x02b1:
        r9 = logger;
        r9.logException(r1);
    L_0x02b6:
        r9 = new org.jitsi.javax.sip.SipException;
        r10 = "Could not create message channel";
        r9.m1627init(r10, r1);
        throw r9;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.javax.sip.stack.SIPDialog.sendAck(org.jitsi.javax.sip.message.Request, boolean):void");
    }

    /* access modifiers changed from: 0000 */
    public void setStack(SIPTransactionStack sipStack) {
        this.sipStack = sipStack;
    }

    /* access modifiers changed from: 0000 */
    public SIPTransactionStack getStack() {
        return this.sipStack;
    }

    /* access modifiers changed from: 0000 */
    public boolean isTerminatedOnBye() {
        return this.terminateOnBye;
    }

    /* access modifiers changed from: 0000 */
    public void ackReceived(long cseqNumber) {
        if (!isAckSeen()) {
            SIPServerTransaction tr = getInviteTransaction();
            if (tr != null) {
                if (tr.getCSeq() == cseqNumber) {
                    acquireTimerTaskSem();
                    try {
                        if (this.timerTask != null) {
                            getStack().getTimer().cancel(this.timerTask);
                            this.timerTask = null;
                        }
                        releaseTimerTaskSem();
                        if (this.dialogDeleteTask != null) {
                            getStack().getTimer().cancel(this.dialogDeleteTask);
                            this.dialogDeleteTask = null;
                        }
                        this.lastAckReceivedCSeqNumber = Long.valueOf(cseqNumber);
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("ackReceived for " + tr.getMethod());
                            this.ackLine = logger.getLineCount();
                            printDebugInfo();
                        }
                        if (this.isBackToBackUserAgent) {
                            releaseAckSem();
                        }
                        setState(1);
                    } catch (Throwable th) {
                        releaseTimerTaskSem();
                    }
                }
            } else if (logger.isLoggingEnabled(32)) {
                logger.logDebug("tr is null -- not updating the ack state");
            }
        } else if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Ack already seen for response -- dropping");
        }
    }

    /* access modifiers changed from: declared_synchronized */
    public synchronized boolean testAndSetIsDialogTerminatedEventDelivered() {
        boolean retval;
        retval = this.dialogTerminatedEventDelivered;
        this.dialogTerminatedEventDelivered = true;
        return retval;
    }

    public void addEventListener(SIPDialogEventListener newListener) {
        this.eventListeners.add(newListener);
    }

    public void removeEventListener(SIPDialogEventListener oldListener) {
        this.eventListeners.remove(oldListener);
    }

    public void setApplicationData(Object applicationData) {
        this.applicationData = applicationData;
    }

    public Object getApplicationData() {
        return this.applicationData;
    }

    public synchronized void requestConsumed() {
        this.nextSeqno = getRemoteSeqNumber() + 1;
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Request Consumed -- next consumable Request Seqno = " + this.nextSeqno);
        }
    }

    public synchronized boolean isRequestConsumable(SIPRequest dialogRequest) {
        boolean z = true;
        synchronized (this) {
            if (dialogRequest.getMethod().equals("ACK")) {
                throw new RuntimeException("Illegal method");
            }
            if (isSequnceNumberValidation()) {
                if (this.remoteSequenceNumber >= dialogRequest.getCSeq().getSeqNumber()) {
                    z = false;
                }
            }
        }
        return z;
    }

    public void doDeferredDelete() {
        if (this.sipStack.getTimer() == null) {
            setState(3);
            return;
        }
        this.dialogDeleteTask = new DialogDeleteTask();
        if (this.sipStack.getTimer() == null || !this.sipStack.getTimer().isStarted()) {
            delete();
        } else {
            this.sipStack.getTimer().schedule(this.dialogDeleteTask, 32000);
        }
    }

    public void setState(int state) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Setting dialog state for " + this + "newState = " + state);
            logger.logStackTrace();
            if (!(state == -1 || state == this.dialogState || !logger.isLoggingEnabled())) {
                logger.logDebug(this + "  old dialog state is " + getState());
                logger.logDebug(this + "  New dialog state is " + DialogState.getObject(state));
            }
        }
        if (state == 0) {
            addEventListener(getSipProvider());
        }
        this.dialogState = state;
        if (state == 3) {
            removeEventListener(getSipProvider());
            if (this.sipStack.getTimer() != null && this.sipStack.getTimer().isStarted()) {
                this.sipStack.getTimer().schedule(new LingerTimer(), 8000);
            }
            stopTimer();
        }
    }

    public void printDebugInfo() {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("isServer = " + isServer());
            logger.logDebug("localTag = " + getLocalTag());
            logger.logDebug("remoteTag = " + getRemoteTag());
            logger.logDebug("localSequenceNumer = " + getLocalSeqNumber());
            logger.logDebug("remoteSequenceNumer = " + getRemoteSeqNumber());
            logger.logDebug("ackLine:" + getRemoteTag() + Separators.SP + this.ackLine);
        }
    }

    public boolean isAckSeen() {
        boolean z = true;
        if (this.lastAckReceivedCSeqNumber == null && this.lastResponseStatusCode.intValue() == Response.OK) {
            if (!logger.isLoggingEnabled(32)) {
                return false;
            }
            logger.logDebug(this + "lastAckReceived is null -- returning false");
            return false;
        } else if (this.lastResponseMethod == null) {
            if (!logger.isLoggingEnabled(32)) {
                return false;
            }
            logger.logDebug(this + "lastResponse is null -- returning false");
            return false;
        } else if (this.lastAckReceivedCSeqNumber != null || this.lastResponseStatusCode.intValue() / 100 <= 2) {
            if (this.lastAckReceivedCSeqNumber == null || this.lastAckReceivedCSeqNumber.longValue() < getRemoteSeqNumber()) {
                z = false;
            }
            return z;
        } else {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug(this + "lastResponse statusCode " + this.lastResponseStatusCode);
            }
            return true;
        }
    }

    public SIPRequest getLastAckSent() {
        return this.lastAckSent;
    }

    public boolean isAckSent(long cseqNo) {
        if (getLastTransaction() == null || !(getLastTransaction() instanceof ClientTransaction)) {
            return true;
        }
        if (getLastAckSent() == null) {
            return false;
        }
        if (cseqNo > getLastAckSent().getCSeq().getSeqNumber()) {
            return false;
        }
        return true;
    }

    @Deprecated
    public Transaction getFirstTransaction() {
        throw new UnsupportedOperationException("This method has been deprecated and is no longer supported");
    }

    public Transaction getFirstTransactionInt() {
        if (this.firstTransaction != null) {
            return this.firstTransaction;
        }
        return this.sipStack.findTransaction(this.firstTransactionId, this.firstTransactionIsServerTransaction);
    }

    public Iterator getRouteSet() {
        if (this.routeList == null) {
            return new LinkedList().listIterator();
        }
        return getRouteList().listIterator();
    }

    public synchronized void addRoute(SIPRequest sipRequest) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("setContact: dialogState: " + this + "state = " + getState());
        }
        if (this.dialogState == 1 && SIPRequest.isTargetRefresh(sipRequest.getMethod())) {
            doTargetRefresh(sipRequest);
        }
        if (!(this.dialogState == 1 || this.dialogState == 3)) {
            if (sipRequest.getToTag() == null) {
                RecordRouteList rrlist = sipRequest.getRecordRouteHeaders();
                if (rrlist != null) {
                    addRoute(rrlist);
                } else {
                    this.routeList = new RouteList();
                }
                ContactList contactList = sipRequest.getContactHeaders();
                if (contactList != null) {
                    setRemoteTarget((ContactHeader) contactList.getFirst());
                }
            }
        }
    }

    public void setDialogId(String dialogId) {
        if (this.firstTransaction != null) {
            this.firstTransaction.setDialog(this, dialogId);
        }
        this.dialogId = dialogId;
    }

    public static SIPDialog createFromNOTIFY(SIPClientTransaction subscribeTx, SIPTransaction notifyST) {
        SIPDialog d = new SIPDialog(notifyST);
        d.serverTransactionFlag = false;
        d.lastTransaction = subscribeTx;
        d.storeFirstTransactionInfo(d, subscribeTx);
        d.terminateOnBye = false;
        d.localSequenceNumber = subscribeTx.getCSeq();
        SIPRequest not = (SIPRequest) notifyST.getRequest();
        d.remoteSequenceNumber = not.getCSeq().getSeqNumber();
        d.setDialogId(not.getDialogId(true));
        d.setLocalTag(not.getToTag());
        d.setRemoteTag(not.getFromTag());
        d.setLastResponse(subscribeTx, subscribeTx.getLastResponse());
        d.localParty = not.getTo().getAddress();
        d.remoteParty = not.getFrom().getAddress();
        d.addRoute(not);
        d.setState(1);
        return d;
    }

    public boolean isServer() {
        if (this.firstTransactionSeen) {
            return this.firstTransactionIsServerTransaction;
        }
        return this.serverTransactionFlag;
    }

    /* access modifiers changed from: protected */
    public boolean isReInvite() {
        return this.reInviteFlag;
    }

    public String getDialogId() {
        if (this.dialogId == null && this.lastResponseDialogId != null) {
            this.dialogId = this.lastResponseDialogId;
        }
        return this.dialogId;
    }

    /* access modifiers changed from: protected */
    public void storeFirstTransactionInfo(SIPDialog dialog, SIPTransaction transaction) {
        dialog.firstTransactionSeen = true;
        dialog.firstTransaction = transaction;
        dialog.firstTransactionIsServerTransaction = transaction.isServerTransaction();
        if (dialog.firstTransactionIsServerTransaction) {
            dialog.firstTransactionSecure = transaction.getRequest().getRequestURI().getScheme().equalsIgnoreCase("sips");
        } else {
            dialog.firstTransactionSecure = ((SIPClientTransaction) transaction).getOriginalRequestScheme().equalsIgnoreCase("sips");
        }
        dialog.firstTransactionPort = transaction.getPort();
        dialog.firstTransactionId = transaction.getBranchId();
        dialog.firstTransactionMethod = transaction.getMethod();
        if ((transaction instanceof SIPServerTransaction) && dialog.firstTransactionMethod.equals("INVITE")) {
            dialog.firstTransactionMergeId = ((SIPRequest) transaction.getRequest()).getMergeId();
        }
        if (transaction.isServerTransaction()) {
            SIPResponse response = ((SIPServerTransaction) transaction).getLastResponse();
            dialog.contactHeader = response != null ? response.getContactHeader() : null;
        } else {
            SIPClientTransaction ct = (SIPClientTransaction) transaction;
            if (ct != null) {
                dialog.contactHeader = ct.getOriginalRequestContact();
            }
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("firstTransaction = " + dialog.firstTransaction);
            logger.logDebug("firstTransactionIsServerTransaction = " + this.firstTransactionIsServerTransaction);
            logger.logDebug("firstTransactionSecure = " + this.firstTransactionSecure);
            logger.logDebug("firstTransactionPort = " + this.firstTransactionPort);
            logger.logDebug("firstTransactionId = " + this.firstTransactionId);
            logger.logDebug("firstTransactionMethod = " + this.firstTransactionMethod);
            logger.logDebug("firstTransactionMergeId = " + this.firstTransactionMergeId);
        }
    }

    public boolean addTransaction(SIPTransaction transaction) {
        SIPRequest sipRequest = transaction.getOriginalRequest();
        if (this.firstTransactionSeen && !this.firstTransactionId.equals(transaction.getBranchId()) && transaction.getMethod().equals(this.firstTransactionMethod)) {
            setReInviteFlag(true);
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("SipDialog.addTransaction() " + this + " transaction = " + transaction);
        }
        if (!this.firstTransactionSeen) {
            storeFirstTransactionInfo(this, transaction);
            if (sipRequest.getMethod().equals("SUBSCRIBE")) {
                this.eventHeader = (EventHeader) sipRequest.getHeader("Event");
            }
            setLocalParty(sipRequest);
            setRemoteParty(sipRequest);
            setCallId(sipRequest);
            if (this.originalRequest == null && transaction.isInviteTransaction()) {
                this.originalRequest = sipRequest;
            } else if (this.originalRequest != null) {
                this.originalRequestRecordRouteHeaders = sipRequest.getRecordRouteHeaders();
            }
            if (this.method == null) {
                this.method = sipRequest.getMethod();
            }
            if (transaction instanceof SIPServerTransaction) {
                this.hisTag = sipRequest.getFrom().getTag();
            } else {
                setLocalSequenceNumber(sipRequest.getCSeq().getSeqNumber());
                this.originalLocalSequenceNumber = this.localSequenceNumber;
                setLocalTag(sipRequest.getFrom().getTag());
                if (this.myTag == null && logger.isLoggingEnabled()) {
                    logger.logError("The request's From header is missing the required Tag parameter.");
                }
            }
        } else if (transaction.getMethod().equals(this.firstTransactionMethod) && this.firstTransactionIsServerTransaction != transaction.isServerTransaction()) {
            storeFirstTransactionInfo(this, transaction);
            setLocalParty(sipRequest);
            setRemoteParty(sipRequest);
            setCallId(sipRequest);
            if (transaction.isInviteTransaction()) {
                this.originalRequest = sipRequest;
            } else {
                this.originalRequestRecordRouteHeaders = sipRequest.getRecordRouteHeaders();
            }
            this.method = sipRequest.getMethod();
        } else if (this.firstTransaction == null && transaction.isInviteTransaction()) {
            this.firstTransaction = transaction;
        }
        if (transaction instanceof SIPServerTransaction) {
            setRemoteSequenceNumber(sipRequest.getCSeq().getSeqNumber());
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("isBackToBackUserAgent = " + this.isBackToBackUserAgent);
        }
        if (transaction.isInviteTransaction()) {
            this.lastTransaction = transaction;
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Transaction Added " + this + this.myTag + Separators.SLASH + this.hisTag);
            logger.logDebug("TID = " + transaction.getTransactionId() + Separators.SLASH + transaction.isServerTransaction());
            logger.logStackTrace();
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void setRemoteTag(String hisTag) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("setRemoteTag(): " + this + " remoteTag = " + this.hisTag + " new tag = " + hisTag);
        }
        if (this.hisTag == null || hisTag == null || hisTag.equals(this.hisTag)) {
            if (hisTag != null) {
                this.hisTag = hisTag;
            } else if (logger.isLoggingEnabled()) {
                logger.logWarning("setRemoteTag : called with null argument ");
            }
        } else if (getState() != DialogState.EARLY) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("Dialog is already established -- ignoring remote tag re-assignment");
            }
        } else if (this.sipStack.isRemoteTagReassignmentAllowed()) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("UNSAFE OPERATION !  tag re-assignment " + this.hisTag + " trying to set to " + hisTag + " can cause unexpected effects ");
            }
            boolean removed = false;
            if (this.sipStack.getDialog(this.dialogId) == this) {
                this.sipStack.removeDialog(this.dialogId);
                removed = true;
            }
            this.dialogId = null;
            this.hisTag = hisTag;
            if (removed) {
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("ReInserting Dialog");
                }
                this.sipStack.putDialog(this);
            }
        }
    }

    public SIPTransaction getLastTransaction() {
        return this.lastTransaction;
    }

    public SIPServerTransaction getInviteTransaction() {
        DialogTimerTask t = this.timerTask;
        if (t != null) {
            return t.transaction;
        }
        return null;
    }

    private void setLocalSequenceNumber(long lCseq) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("setLocalSequenceNumber: original  " + this.localSequenceNumber + " new  = " + lCseq);
        }
        if (lCseq <= this.localSequenceNumber) {
            throw new RuntimeException("Sequence number should not decrease !");
        }
        this.localSequenceNumber = lCseq;
    }

    public void setRemoteSequenceNumber(long rCseq) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("setRemoteSeqno " + this + Separators.SLASH + rCseq);
        }
        this.remoteSequenceNumber = rCseq;
    }

    public void incrementLocalSequenceNumber() {
        this.localSequenceNumber++;
    }

    public int getRemoteSequenceNumber() {
        return (int) this.remoteSequenceNumber;
    }

    public int getLocalSequenceNumber() {
        return (int) this.localSequenceNumber;
    }

    public long getOriginalLocalSequenceNumber() {
        return this.originalLocalSequenceNumber;
    }

    public long getLocalSeqNumber() {
        return this.localSequenceNumber;
    }

    public long getRemoteSeqNumber() {
        return this.remoteSequenceNumber;
    }

    public String getLocalTag() {
        return this.myTag;
    }

    public String getRemoteTag() {
        return this.hisTag;
    }

    /* access modifiers changed from: protected */
    public void setLocalTag(String mytag) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("set Local tag " + mytag + " dialog = " + this);
            logger.logStackTrace();
        }
        this.myTag = mytag;
    }

    public void delete() {
        setState(3);
    }

    public CallIdHeader getCallId() {
        if (this.callIdHeader == null && this.callIdHeaderString != null) {
            try {
                this.callIdHeader = (CallIdHeader) new CallIDParser(this.callIdHeaderString).parse();
            } catch (ParseException e) {
                logger.logError("error reparsing the call id header", e);
            }
        }
        return this.callIdHeader;
    }

    private void setCallId(SIPRequest sipRequest) {
        this.callIdHeader = sipRequest.getCallId();
    }

    public Address getLocalParty() {
        if (this.localParty == null && this.localPartyStringified != null) {
            try {
                this.localParty = new AddressParser(this.localPartyStringified).address(true);
            } catch (ParseException e) {
                logger.logError("error reparsing the localParty", e);
            }
        }
        return this.localParty;
    }

    /* access modifiers changed from: protected */
    public void setLocalParty(SIPMessage sipMessage) {
        if (isServer()) {
            this.localParty = sipMessage.getTo().getAddress();
        } else {
            this.localParty = sipMessage.getFrom().getAddress();
        }
    }

    public Address getRemoteParty() {
        if (this.remoteParty == null && this.remotePartyStringified != null) {
            try {
                this.remoteParty = new AddressParser(this.remotePartyStringified).address(true);
            } catch (ParseException e) {
                logger.logError("error reparsing the remoteParty", e);
            }
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("gettingRemoteParty " + this.remoteParty);
        }
        return this.remoteParty;
    }

    public Address getRemoteTarget() {
        if (this.remoteTarget == null && this.remoteTargetStringified != null) {
            try {
                this.remoteTarget = new AddressParser(this.remoteTargetStringified).address(true);
            } catch (ParseException e) {
                logger.logError("error reparsing the remoteTarget", e);
            }
        }
        return this.remoteTarget;
    }

    public DialogState getState() {
        if (this.dialogState == -1) {
            return null;
        }
        return DialogState.getObject(this.dialogState);
    }

    public boolean isSecure() {
        return this.firstTransactionSecure;
    }

    public void sendAck(Request request) throws SipException {
        sendAck(request, true);
    }

    public Request createRequest(String method) throws SipException {
        if (method.equals("ACK") || method.equals(Request.PRACK)) {
            throw new SipException("Invalid method specified for createRequest:" + method);
        } else if (this.lastResponseTopMostVia != null) {
            return createRequest(method, this.lastResponseTopMostVia.getTransport());
        } else {
            throw new SipException("Dialog not yet established -- no response!");
        }
    }

    /* JADX WARNING: Missing block: B:13:0x0037, code skipped:
            if (r17.equalsIgnoreCase("BYE") != false) goto L_0x0039;
     */
    /* JADX WARNING: Missing block: B:19:0x0051, code skipped:
            if (r17.equalsIgnoreCase("BYE") != false) goto L_0x0053;
     */
    private org.jitsi.gov.nist.javax.sip.message.SIPRequest createRequest(java.lang.String r17, java.lang.String r18) throws org.jitsi.javax.sip.SipException {
        /*
        r16 = this;
        if (r17 == 0) goto L_0x0004;
    L_0x0002:
        if (r18 != 0) goto L_0x000c;
    L_0x0004:
        r2 = new java.lang.NullPointerException;
        r12 = "null argument";
        r2.<init>(r12);
        throw r2;
    L_0x000c:
        r2 = "CANCEL";
        r0 = r17;
        r2 = r0.equals(r2);
        if (r2 == 0) goto L_0x001e;
    L_0x0016:
        r2 = new org.jitsi.javax.sip.SipException;
        r12 = "Dialog.createRequest(): Invalid request";
        r2.m1626init(r12);
        throw r2;
    L_0x001e:
        r2 = r16.getState();
        if (r2 == 0) goto L_0x0053;
    L_0x0024:
        r2 = r16.getState();
        r2 = r2.getValue();
        r12 = 3;
        if (r2 != r12) goto L_0x0039;
    L_0x002f:
        r2 = "BYE";
        r0 = r17;
        r2 = r0.equalsIgnoreCase(r2);
        if (r2 == 0) goto L_0x0053;
    L_0x0039:
        r2 = r16.isServer();
        if (r2 == 0) goto L_0x007e;
    L_0x003f:
        r2 = r16.getState();
        r2 = r2.getValue();
        if (r2 != 0) goto L_0x007e;
    L_0x0049:
        r2 = "BYE";
        r0 = r17;
        r2 = r0.equalsIgnoreCase(r2);
        if (r2 == 0) goto L_0x007e;
    L_0x0053:
        r2 = new org.jitsi.javax.sip.SipException;
        r12 = new java.lang.StringBuilder;
        r12.<init>();
        r13 = "Dialog  ";
        r12 = r12.append(r13);
        r13 = r16.getDialogId();
        r12 = r12.append(r13);
        r13 = " not yet established or terminated ";
        r12 = r12.append(r13);
        r13 = r16.getState();
        r12 = r12.append(r13);
        r12 = r12.toString();
        r2.m1626init(r12);
        throw r2;
    L_0x007e:
        r3 = 0;
        r2 = r16.getRemoteTarget();
        if (r2 == 0) goto L_0x00ef;
    L_0x0085:
        r2 = r16.getRemoteTarget();
        r2 = r2.getURI();
        r3 = r2.clone();
        r3 = (org.jitsi.gov.nist.javax.sip.address.SipUri) r3;
    L_0x0093:
        r5 = new org.jitsi.gov.nist.javax.sip.header.CSeq;
        r5.m1138init();
        r0 = r17;
        r5.setMethod(r0);	 Catch:{ Exception -> 0x0101 }
        r12 = r16.getLocalSeqNumber();	 Catch:{ Exception -> 0x0101 }
        r5.setSeqNumber(r12);	 Catch:{ Exception -> 0x0101 }
    L_0x00a4:
        r0 = r16;
        r2 = r0.sipProvider;
        r0 = r18;
        r10 = r2.getListeningPoint(r0);
        r10 = (org.jitsi.gov.nist.javax.sip.ListeningPointImpl) r10;
        if (r10 != 0) goto L_0x0115;
    L_0x00b2:
        r2 = logger;
        r2 = r2.isLoggingEnabled();
        if (r2 == 0) goto L_0x00d4;
    L_0x00ba:
        r2 = logger;
        r12 = new java.lang.StringBuilder;
        r12.<init>();
        r13 = "Cannot find listening point for transport ";
        r12 = r12.append(r13);
        r0 = r18;
        r12 = r12.append(r0);
        r12 = r12.toString();
        r2.logError(r12);
    L_0x00d4:
        r2 = new org.jitsi.javax.sip.SipException;
        r12 = new java.lang.StringBuilder;
        r12.<init>();
        r13 = "Cannot find listening point for transport ";
        r12 = r12.append(r13);
        r0 = r18;
        r12 = r12.append(r0);
        r12 = r12.toString();
        r2.m1626init(r12);
        throw r2;
    L_0x00ef:
        r2 = r16.getRemoteParty();
        r2 = r2.getURI();
        r3 = r2.clone();
        r3 = (org.jitsi.gov.nist.javax.sip.address.SipUri) r3;
        r3.clearUriParms();
        goto L_0x0093;
    L_0x0101:
        r9 = move-exception;
        r2 = logger;
        r2 = r2.isLoggingEnabled();
        if (r2 == 0) goto L_0x0111;
    L_0x010a:
        r2 = logger;
        r12 = "Unexpected error";
        r2.logError(r12);
    L_0x0111:
        org.jitsi.gov.nist.core.InternalErrorHandler.handleException(r9);
        goto L_0x00a4;
    L_0x0115:
        r4 = r10.getViaHeader();
        r6 = new org.jitsi.gov.nist.javax.sip.header.From;
        r6.m1173init();
        r2 = r16.getLocalParty();
        r6.setAddress(r2);
        r7 = new org.jitsi.gov.nist.javax.sip.header.To;
        r7.m1236init();
        r2 = r16.getRemoteParty();
        r7.setAddress(r2);
        r2 = r16;
        r11 = r2.createRequest(r3, r4, r5, r6, r7);
        r2 = org.jitsi.gov.nist.javax.sip.message.SIPRequest.isTargetRefresh(r17);
        if (r2 == 0) goto L_0x0163;
    L_0x013d:
        r0 = r16;
        r2 = r0.sipProvider;
        r12 = r10.getTransport();
        r2 = r2.getListeningPoint(r12);
        r2 = (org.jitsi.gov.nist.javax.sip.ListeningPointImpl) r2;
        r8 = r2.createContactHeader();
        r2 = r8.getAddress();
        r2 = r2.getURI();
        r2 = (org.jitsi.javax.sip.address.SipURI) r2;
        r12 = r16.isSecure();
        r2.setSecure(r12);
        r11.setHeader(r8);
    L_0x0163:
        r2 = r11.getCSeq();	 Catch:{ InvalidArgumentException -> 0x01ac }
        r0 = r2;
        r0 = (org.jitsi.gov.nist.javax.sip.header.CSeq) r0;	 Catch:{ InvalidArgumentException -> 0x01ac }
        r5 = r0;
        r0 = r16;
        r12 = r0.localSequenceNumber;	 Catch:{ InvalidArgumentException -> 0x01ac }
        r14 = 1;
        r12 = r12 + r14;
        r5.setSeqNumber(r12);	 Catch:{ InvalidArgumentException -> 0x01ac }
    L_0x0175:
        r2 = "SUBSCRIBE";
        r0 = r17;
        r2 = r0.equals(r2);
        if (r2 == 0) goto L_0x018c;
    L_0x017f:
        r0 = r16;
        r2 = r0.eventHeader;
        if (r2 == 0) goto L_0x018c;
    L_0x0185:
        r0 = r16;
        r2 = r0.eventHeader;
        r11.addHeader(r2);
    L_0x018c:
        r2 = r16.getLocalTag();	 Catch:{ ParseException -> 0x01b5 }
        if (r2 == 0) goto L_0x01b1;
    L_0x0192:
        r2 = r16.getLocalTag();	 Catch:{ ParseException -> 0x01b5 }
        r6.setTag(r2);	 Catch:{ ParseException -> 0x01b5 }
    L_0x0199:
        r2 = r16.getRemoteTag();	 Catch:{ ParseException -> 0x01b5 }
        if (r2 == 0) goto L_0x01ba;
    L_0x019f:
        r2 = r16.getRemoteTag();	 Catch:{ ParseException -> 0x01b5 }
        r7.setTag(r2);	 Catch:{ ParseException -> 0x01b5 }
    L_0x01a6:
        r0 = r16;
        r0.updateRequest(r11);
        return r11;
    L_0x01ac:
        r9 = move-exception;
        org.jitsi.gov.nist.core.InternalErrorHandler.handleException(r9);
        goto L_0x0175;
    L_0x01b1:
        r6.removeTag();	 Catch:{ ParseException -> 0x01b5 }
        goto L_0x0199;
    L_0x01b5:
        r9 = move-exception;
        org.jitsi.gov.nist.core.InternalErrorHandler.handleException(r9);
        goto L_0x01a6;
    L_0x01ba:
        r7.removeTag();	 Catch:{ ParseException -> 0x01b5 }
        goto L_0x01a6;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.javax.sip.stack.SIPDialog.createRequest(java.lang.String, java.lang.String):org.jitsi.gov.nist.javax.sip.message.SIPRequest");
    }

    public SIPRequest createRequest(SipUri requestURI, Via via, CSeq cseq, From from, To to) {
        SIPRequest newRequest = new SIPRequest();
        String method = cseq.getMethod();
        newRequest.setMethod(method);
        newRequest.setRequestURI(requestURI);
        setBranch(via, method);
        newRequest.setHeader((Header) via);
        newRequest.setHeader((Header) cseq);
        newRequest.setHeader((Header) from);
        newRequest.setHeader((Header) to);
        newRequest.setHeader((Header) getCallId());
        try {
            newRequest.attachHeader(new MaxForwards(70), false);
        } catch (Exception e) {
        }
        if (MessageFactoryImpl.getDefaultUserAgentHeader() != null) {
            newRequest.setHeader((Header) MessageFactoryImpl.getDefaultUserAgentHeader());
        }
        return newRequest;
    }

    private final void setBranch(Via via, String method) {
        String branch;
        if (method.equals("ACK")) {
            if (getLastResponseStatusCode().intValue() >= 300) {
                branch = this.lastResponseTopMostVia.getBranch();
            } else {
                branch = Utils.getInstance().generateBranchId();
            }
        } else if (method.equals(Request.CANCEL)) {
            branch = this.lastResponseTopMostVia.getBranch();
        } else {
            return;
        }
        try {
            via.setBranch(branch);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void sendRequest(ClientTransaction clientTransactionId) throws TransactionDoesNotExistException, SipException {
        sendRequest(clientTransactionId, !this.isBackToBackUserAgent);
    }

    public void sendRequest(ClientTransaction clientTransactionId, boolean allowInterleaving) throws TransactionDoesNotExistException, SipException {
        if (clientTransactionId == null) {
            throw new NullPointerException("null parameter");
        } else if (allowInterleaving || !clientTransactionId.getRequest().getMethod().equals("INVITE")) {
            SIPRequest dialogRequest = ((SIPClientTransaction) clientTransactionId).getOriginalRequest();
            this.proxyAuthorizationHeader = (ProxyAuthorizationHeader) dialogRequest.getHeader("Proxy-Authorization");
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("dialog.sendRequest  dialog = " + this + "\ndialogRequest = \n" + dialogRequest);
            }
            if (dialogRequest.getMethod().equals("ACK") || dialogRequest.getMethod().equals(Request.CANCEL)) {
                throw new SipException("Bad Request Method. " + dialogRequest.getMethod());
            } else if (this.byeSent && isTerminatedOnBye() && !dialogRequest.getMethod().equals("BYE")) {
                if (logger.isLoggingEnabled()) {
                    logger.logError("BYE already sent for " + this);
                }
                throw new SipException("Cannot send request; BYE already sent");
            } else {
                if (dialogRequest.getTopmostVia() == null) {
                    dialogRequest.addHeader((Header) ((SIPClientTransaction) clientTransactionId).getOutgoingViaHeader());
                }
                if (getCallId().getCallId().equalsIgnoreCase(dialogRequest.getCallId().getCallId())) {
                    ((SIPClientTransaction) clientTransactionId).setDialog(this, this.dialogId);
                    addTransaction((SIPTransaction) clientTransactionId);
                    ((SIPClientTransaction) clientTransactionId).isMapped = true;
                    From from = (From) dialogRequest.getFrom();
                    To to = (To) dialogRequest.getTo();
                    if (getLocalTag() == null || from.getTag() == null || from.getTag().equals(getLocalTag())) {
                        if (!(getRemoteTag() == null || to.getTag() == null || to.getTag().equals(getRemoteTag()) || !logger.isLoggingEnabled())) {
                            logger.logWarning("To header tag mismatch expecting " + getRemoteTag());
                        }
                        if (getLocalTag() == null && dialogRequest.getMethod().equals("NOTIFY")) {
                            if (getMethod().equals("SUBSCRIBE")) {
                                setLocalTag(from.getTag());
                            } else {
                                throw new SipException("Trying to send NOTIFY without SUBSCRIBE Dialog!");
                            }
                        }
                        try {
                            if (getLocalTag() != null) {
                                from.setTag(getLocalTag());
                            }
                            if (getRemoteTag() != null) {
                                to.setTag(getRemoteTag());
                            }
                        } catch (ParseException ex) {
                            InternalErrorHandler.handleException(ex);
                        }
                        Hop hop = ((SIPClientTransaction) clientTransactionId).getNextHop();
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("Using hop = " + hop.getHost() + " : " + hop.getPort());
                        }
                        try {
                            MessageChannel messageChannel = this.sipStack.createRawMessageChannel(getSipProvider().getListeningPoint(hop.getTransport()).getIPAddress(), this.firstTransactionPort, hop);
                            MessageChannel oldChannel = ((SIPClientTransaction) clientTransactionId).getMessageChannel();
                            oldChannel.uncache();
                            if (!this.sipStack.cacheClientConnections) {
                                oldChannel.useCount--;
                                if (logger.isLoggingEnabled(32)) {
                                    logger.logDebug("oldChannel: useCount " + oldChannel.useCount);
                                }
                            }
                            if (messageChannel == null) {
                                if (logger.isLoggingEnabled(32)) {
                                    logger.logDebug("Null message channel using outbound proxy !");
                                }
                                Hop outboundProxy = this.sipStack.getRouter(dialogRequest).getOutboundProxy();
                                if (outboundProxy == null) {
                                    throw new SipException("No route found! hop=" + hop);
                                }
                                messageChannel = this.sipStack.createRawMessageChannel(getSipProvider().getListeningPoint(outboundProxy.getTransport()).getIPAddress(), this.firstTransactionPort, outboundProxy);
                                if (messageChannel != null) {
                                    ((SIPClientTransaction) clientTransactionId).setEncapsulatedChannel(messageChannel);
                                }
                            } else {
                                ((SIPClientTransaction) clientTransactionId).setEncapsulatedChannel(messageChannel);
                                if (logger.isLoggingEnabled(32)) {
                                    logger.logDebug("using message channel " + messageChannel);
                                }
                            }
                            if (messageChannel != null) {
                                messageChannel.useCount++;
                            }
                            if (!(this.sipStack.cacheClientConnections || oldChannel == null || oldChannel.useCount > 0)) {
                                oldChannel.close();
                            }
                            try {
                                this.localSequenceNumber++;
                                dialogRequest.getCSeq().setSeqNumber(getLocalSeqNumber());
                            } catch (InvalidArgumentException ex2) {
                                logger.logFatalError(ex2.getMessage());
                            }
                            try {
                                ((SIPClientTransaction) clientTransactionId).sendMessage(dialogRequest);
                                if (dialogRequest.getMethod().equals("BYE")) {
                                    this.byeSent = true;
                                    if (isTerminatedOnBye()) {
                                        setState(3);
                                        return;
                                    }
                                    return;
                                }
                                return;
                            } catch (IOException ex3) {
                                throw new SipException("error sending message", ex3);
                            }
                        } catch (Exception ex4) {
                            if (logger.isLoggingEnabled()) {
                                logger.logException(ex4);
                            }
                            throw new SipException("Could not create message channel", ex4);
                        }
                    }
                    throw new SipException("From tag mismatch expecting  " + getLocalTag());
                }
                if (logger.isLoggingEnabled()) {
                    logger.logError("CallID " + getCallId());
                    logger.logError("RequestCallID = " + dialogRequest.getCallId().getCallId());
                    logger.logError("dialog =  " + this);
                }
                throw new SipException("Bad call ID in request");
            }
        } else {
            this.sipStack.getReinviteExecutor().execute(new ReInviteSender(clientTransactionId));
        }
    }

    /* access modifiers changed from: private */
    public boolean toRetransmitFinalResponse(int T2) {
        int i = this.retransmissionTicksLeft - 1;
        this.retransmissionTicksLeft = i;
        if (i != 0) {
            return false;
        }
        if (this.prevRetransmissionTicks * 2 <= T2) {
            this.retransmissionTicksLeft = this.prevRetransmissionTicks * 2;
        } else {
            this.retransmissionTicksLeft = this.prevRetransmissionTicks;
        }
        this.prevRetransmissionTicks = this.retransmissionTicksLeft;
        return true;
    }

    /* access modifiers changed from: protected */
    public void setRetransmissionTicks() {
        this.retransmissionTicksLeft = 1;
        this.prevRetransmissionTicks = 1;
    }

    public void resendAck() throws SipException {
        if (getLastAckSent() != null) {
            if (getLastAckSent().getHeader("Timestamp") != null && this.sipStack.generateTimeStampHeader) {
                TimeStamp ts = new TimeStamp();
                try {
                    ts.setTimeStamp((float) System.currentTimeMillis());
                    getLastAckSent().setHeader((Header) ts);
                } catch (InvalidArgumentException e) {
                }
            }
            sendAck(getLastAckSent(), false);
        }
    }

    public String getMethod() {
        return this.method;
    }

    /* access modifiers changed from: protected */
    public void startTimer(SIPServerTransaction transaction) {
        if (this.timerTask == null || this.timerTask.transaction != transaction) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("Starting dialog timer for " + getDialogId());
            }
            acquireTimerTaskSem();
            try {
                if (this.timerTask != null) {
                    this.timerTask.transaction = transaction;
                } else {
                    this.timerTask = new DialogTimerTask(transaction);
                    if (this.sipStack.getTimer() != null && this.sipStack.getTimer().isStarted()) {
                        this.sipStack.getTimer().scheduleWithFixedDelay(this.timerTask, 500, 500);
                    }
                }
                releaseTimerTaskSem();
                setRetransmissionTicks();
            } catch (Throwable th) {
                releaseTimerTaskSem();
            }
        } else if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Timer already running for " + getDialogId());
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    public void stopTimer() {
        /*
        r2 = this;
        r2.acquireTimerTaskSem();	 Catch:{ Exception -> 0x0034 }
        r0 = r2.timerTask;	 Catch:{ all -> 0x002f }
        if (r0 == 0) goto L_0x0017;
    L_0x0007:
        r0 = r2.getStack();	 Catch:{ all -> 0x002f }
        r0 = r0.getTimer();	 Catch:{ all -> 0x002f }
        r1 = r2.timerTask;	 Catch:{ all -> 0x002f }
        r0.cancel(r1);	 Catch:{ all -> 0x002f }
        r0 = 0;
        r2.timerTask = r0;	 Catch:{ all -> 0x002f }
    L_0x0017:
        r0 = r2.earlyStateTimerTask;	 Catch:{ all -> 0x002f }
        if (r0 == 0) goto L_0x002b;
    L_0x001b:
        r0 = r2.getStack();	 Catch:{ all -> 0x002f }
        r0 = r0.getTimer();	 Catch:{ all -> 0x002f }
        r1 = r2.earlyStateTimerTask;	 Catch:{ all -> 0x002f }
        r0.cancel(r1);	 Catch:{ all -> 0x002f }
        r0 = 0;
        r2.earlyStateTimerTask = r0;	 Catch:{ all -> 0x002f }
    L_0x002b:
        r2.releaseTimerTaskSem();	 Catch:{ Exception -> 0x0034 }
    L_0x002e:
        return;
    L_0x002f:
        r0 = move-exception;
        r2.releaseTimerTaskSem();	 Catch:{ Exception -> 0x0034 }
        throw r0;	 Catch:{ Exception -> 0x0034 }
    L_0x0034:
        r0 = move-exception;
        goto L_0x002e;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.javax.sip.stack.SIPDialog.stopTimer():void");
    }

    public Request createPrack(Response relResponse) throws DialogDoesNotExistException, SipException {
        if (getState() == null || getState().equals(DialogState.TERMINATED)) {
            throw new DialogDoesNotExistException("Dialog not initialized or terminated");
        } else if (((RSeq) relResponse.getHeader("RSeq")) == null) {
            throw new SipException("Missing RSeq Header");
        } else {
            try {
                SIPResponse sipResponse = (SIPResponse) relResponse;
                SIPRequest sipRequest = createRequest(Request.PRACK, sipResponse.getTopmostVia().getTransport());
                sipRequest.setToTag(sipResponse.getTo().getTag());
                RAck rack = new RAck();
                RSeq rseq = (RSeq) relResponse.getHeader("RSeq");
                rack.setMethod(sipResponse.getCSeq().getMethod());
                rack.setCSequenceNumber((long) ((int) sipResponse.getCSeq().getSeqNumber()));
                rack.setRSequenceNumber(rseq.getSeqNumber());
                sipRequest.setHeader((Header) rack);
                if (this.proxyAuthorizationHeader == null) {
                    return sipRequest;
                }
                sipRequest.addHeader((Header) this.proxyAuthorizationHeader);
                return sipRequest;
            } catch (Exception ex) {
                InternalErrorHandler.handleException(ex);
                return null;
            }
        }
    }

    private void updateRequest(SIPRequest sipRequest) {
        RouteList rl = getRouteList();
        if (rl.size() > 0) {
            sipRequest.setHeader((Header) rl);
        } else {
            sipRequest.removeHeader("Route");
        }
        if (MessageFactoryImpl.getDefaultUserAgentHeader() != null) {
            sipRequest.setHeader((Header) MessageFactoryImpl.getDefaultUserAgentHeader());
        }
        if (this.proxyAuthorizationHeader != null && sipRequest.getHeader("Proxy-Authorization") == null) {
            sipRequest.setHeader((Header) this.proxyAuthorizationHeader);
        }
    }

    public Request createAck(long cseqno) throws InvalidArgumentException, SipException {
        if (!this.method.equals("INVITE")) {
            throw new SipException("Dialog was not created with an INVITE" + this.method);
        } else if (cseqno <= 0) {
            throw new InvalidArgumentException("bad cseq <= 0 ");
        } else if (cseqno > 4294967295L) {
            throw new InvalidArgumentException("bad cseq > 4294967295");
        } else if (getRemoteTarget() == null) {
            throw new SipException("Cannot create ACK - no remote Target!");
        } else {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("createAck " + this + " cseqno " + cseqno);
            }
            if (this.lastInviteOkReceived < cseqno) {
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("WARNING : Attempt to crete ACK without OK " + this);
                    logger.logDebug("LAST RESPONSE = " + getLastResponseStatusCode());
                }
                throw new SipException("Dialog not yet established -- no OK response!");
            }
            try {
                SipURI uri4transport;
                ListeningPointImpl lp;
                if (this.routeList == null || this.routeList.isEmpty()) {
                    uri4transport = (SipURI) getRemoteTarget().getURI();
                } else {
                    uri4transport = (SipURI) ((Route) this.routeList.getFirst()).getAddress().getURI();
                }
                String transport = uri4transport.getTransportParam();
                if (transport != null) {
                    lp = (ListeningPointImpl) this.sipProvider.getListeningPoint(transport);
                } else if (uri4transport.isSecure()) {
                    lp = (ListeningPointImpl) this.sipProvider.getListeningPoint(ListeningPoint.TLS);
                } else {
                    lp = (ListeningPointImpl) this.sipProvider.getListeningPoint(ListeningPoint.UDP);
                    if (lp == null) {
                        lp = (ListeningPointImpl) this.sipProvider.getListeningPoint(ListeningPoint.TCP);
                    }
                }
                if (lp == null) {
                    if (logger.isLoggingEnabled()) {
                        logger.logError("remoteTargetURI " + getRemoteTarget().getURI());
                        logger.logError("uri4transport = " + uri4transport);
                        logger.logError("No LP found for transport=" + transport);
                    }
                    throw new SipException("Cannot create ACK - no ListeningPoint for transport towards next hop found:" + transport);
                }
                SIPRequest sipRequest = new SIPRequest();
                sipRequest.setMethod("ACK");
                sipRequest.setRequestURI((SipUri) getRemoteTarget().getURI().clone());
                sipRequest.setCallId(getCallId());
                sipRequest.setCSeq(new CSeq(cseqno, "ACK"));
                List<Via> vias = new ArrayList();
                Via via = this.lastResponseTopMostVia;
                via.removeParameters();
                if (!(this.originalRequest == null || this.originalRequest.getTopmostVia() == null)) {
                    NameValueList originalRequestParameters = this.originalRequest.getTopmostVia().getParameters();
                    if (originalRequestParameters != null && originalRequestParameters.size() > 0) {
                        via.setParameters((NameValueList) originalRequestParameters.clone());
                    }
                }
                via.setBranch(Utils.getInstance().generateBranchId());
                vias.add(via);
                sipRequest.setVia(vias);
                From from = new From();
                from.setAddress(getLocalParty());
                from.setTag(this.myTag);
                sipRequest.setFrom(from);
                To to = new To();
                to.setAddress(getRemoteParty());
                if (this.hisTag != null) {
                    to.setTag(this.hisTag);
                }
                sipRequest.setTo(to);
                sipRequest.setMaxForwards(new MaxForwards(70));
                if (this.originalRequest != null) {
                    Authorization authorization = this.originalRequest.getAuthorization();
                    if (authorization != null) {
                        sipRequest.setHeader((Header) authorization);
                    }
                    this.originalRequestRecordRouteHeaders = this.originalRequest.getRecordRouteHeaders();
                    this.originalRequest = null;
                }
                updateRequest(sipRequest);
                return sipRequest;
            } catch (Exception ex) {
                InternalErrorHandler.handleException(ex);
                throw new SipException("unexpected exception ", ex);
            }
        }
    }

    public SipProviderImpl getSipProvider() {
        return this.sipProvider;
    }

    public void setSipProvider(SipProviderImpl sipProvider) {
        this.sipProvider = sipProvider;
    }

    public void setResponseTags(SIPResponse sipResponse) {
        if (getLocalTag() == null && getRemoteTag() == null) {
            String responseFromTag = sipResponse.getFromTag();
            if (responseFromTag != null) {
                if (responseFromTag.equals(getLocalTag())) {
                    sipResponse.setToTag(getRemoteTag());
                } else if (responseFromTag.equals(getRemoteTag())) {
                    sipResponse.setToTag(getLocalTag());
                }
            } else if (logger.isLoggingEnabled()) {
                logger.logWarning("No from tag in response! Not RFC 3261 compatible.");
            }
        }
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:98:0x02e0=Splitter:B:98:0x02e0, B:188:0x04d8=Splitter:B:188:0x04d8} */
    public void setLastResponse(org.jitsi.gov.nist.javax.sip.stack.SIPTransaction r11, org.jitsi.gov.nist.javax.sip.message.SIPResponse r12) {
        /*
        r10 = this;
        r9 = 2;
        r8 = 1;
        r5 = r12.getCallId();
        r10.callIdHeader = r5;
        r4 = r12.getStatusCode();
        r5 = 100;
        if (r4 != r5) goto L_0x0020;
    L_0x0010:
        r5 = logger;
        r5 = r5.isLoggingEnabled();
        if (r5 == 0) goto L_0x001f;
    L_0x0018:
        r5 = logger;
        r6 = "Invalid status code - 100 in setLastResponse - ignoring";
        r5.logWarning(r6);
    L_0x001f:
        return;
    L_0x0020:
        r5 = java.lang.Integer.valueOf(r4);	 Catch:{ all -> 0x0266 }
        r10.lastResponseStatusCode = r5;	 Catch:{ all -> 0x0266 }
        r5 = r12.getTopmostVia();	 Catch:{ all -> 0x0266 }
        r10.lastResponseTopMostVia = r5;	 Catch:{ all -> 0x0266 }
        r5 = r12.getCSeqHeader();	 Catch:{ all -> 0x0266 }
        r5 = r5.getMethod();	 Catch:{ all -> 0x0266 }
        r10.lastResponseMethod = r5;	 Catch:{ all -> 0x0266 }
        r5 = r12.getCSeq();	 Catch:{ all -> 0x0266 }
        r6 = r5.getSeqNumber();	 Catch:{ all -> 0x0266 }
        r10.lastResponseCSeqNumber = r6;	 Catch:{ all -> 0x0266 }
        r5 = r12.getToTag();	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x004c;
    L_0x0046:
        r5 = r12.getToTag();	 Catch:{ all -> 0x0266 }
        r10.lastResponseToTag = r5;	 Catch:{ all -> 0x0266 }
    L_0x004c:
        r5 = r12.getFromTag();	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x0058;
    L_0x0052:
        r5 = r12.getFromTag();	 Catch:{ all -> 0x0266 }
        r10.lastResponseFromTag = r5;	 Catch:{ all -> 0x0266 }
    L_0x0058:
        if (r11 == 0) goto L_0x0064;
    L_0x005a:
        r5 = r11.isServerTransaction();	 Catch:{ all -> 0x0266 }
        r5 = r12.getDialogId(r5);	 Catch:{ all -> 0x0266 }
        r10.lastResponseDialogId = r5;	 Catch:{ all -> 0x0266 }
    L_0x0064:
        r10.setAssigned();	 Catch:{ all -> 0x0266 }
        r5 = logger;	 Catch:{ all -> 0x0266 }
        r6 = 32;
        r5 = r5.isLoggingEnabled(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x0095;
    L_0x0071:
        r5 = logger;	 Catch:{ all -> 0x0266 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0266 }
        r6.<init>();	 Catch:{ all -> 0x0266 }
        r7 = "sipDialog: setLastResponse:";
        r6 = r6.append(r7);	 Catch:{ all -> 0x0266 }
        r6 = r6.append(r10);	 Catch:{ all -> 0x0266 }
        r7 = " lastResponse = ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x0266 }
        r7 = r10.lastResponseStatusCode;	 Catch:{ all -> 0x0266 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0266 }
        r6 = r6.toString();	 Catch:{ all -> 0x0266 }
        r5.logDebug(r6);	 Catch:{ all -> 0x0266 }
    L_0x0095:
        r5 = r10.getState();	 Catch:{ all -> 0x0266 }
        r6 = org.jitsi.javax.sip.DialogState.TERMINATED;	 Catch:{ all -> 0x0266 }
        if (r5 != r6) goto L_0x0140;
    L_0x009d:
        r5 = logger;	 Catch:{ all -> 0x0266 }
        r6 = 32;
        r5 = r5.isLoggingEnabled(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x00ae;
    L_0x00a7:
        r5 = logger;	 Catch:{ all -> 0x0266 }
        r6 = "sipDialog: setLastResponse -- dialog is terminated - ignoring ";
        r5.logDebug(r6);	 Catch:{ all -> 0x0266 }
    L_0x00ae:
        r5 = r10.lastResponseMethod;	 Catch:{ all -> 0x0266 }
        r6 = "INVITE";
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x00c6;
    L_0x00b8:
        r5 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
        if (r4 != r5) goto L_0x00c6;
    L_0x00bc:
        r6 = r10.lastResponseCSeqNumber;	 Catch:{ all -> 0x0266 }
        r8 = r10.lastInviteOkReceived;	 Catch:{ all -> 0x0266 }
        r6 = java.lang.Math.max(r6, r8);	 Catch:{ all -> 0x0266 }
        r10.lastInviteOkReceived = r6;	 Catch:{ all -> 0x0266 }
    L_0x00c6:
        r5 = r12.getCSeq();
        r5 = r5.getMethod();
        r6 = "INVITE";
        r5 = r5.equals(r6);
        if (r5 == 0) goto L_0x001f;
    L_0x00d6:
        r5 = r11 instanceof org.jitsi.javax.sip.ClientTransaction;
        if (r5 == 0) goto L_0x001f;
    L_0x00da:
        r10.acquireTimerTaskSem();
        r5 = r10.getState();	 Catch:{ all -> 0x06a2 }
        r6 = org.jitsi.javax.sip.DialogState.EARLY;	 Catch:{ all -> 0x06a2 }
        if (r5 != r6) goto L_0x06a7;
    L_0x00e5:
        r5 = r10.earlyStateTimerTask;	 Catch:{ all -> 0x06a2 }
        if (r5 == 0) goto L_0x00f4;
    L_0x00e9:
        r5 = r10.sipStack;	 Catch:{ all -> 0x06a2 }
        r5 = r5.getTimer();	 Catch:{ all -> 0x06a2 }
        r6 = r10.earlyStateTimerTask;	 Catch:{ all -> 0x06a2 }
        r5.cancel(r6);	 Catch:{ all -> 0x06a2 }
    L_0x00f4:
        r5 = logger;	 Catch:{ all -> 0x06a2 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x06a2 }
        r6.<init>();	 Catch:{ all -> 0x06a2 }
        r7 = "EarlyStateTimerTask craeted ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x06a2 }
        r7 = r10.earlyDialogTimeout;	 Catch:{ all -> 0x06a2 }
        r7 = r7 * 1000;
        r6 = r6.append(r7);	 Catch:{ all -> 0x06a2 }
        r6 = r6.toString();	 Catch:{ all -> 0x06a2 }
        r5.logDebug(r6);	 Catch:{ all -> 0x06a2 }
        r5 = new org.jitsi.gov.nist.javax.sip.stack.SIPDialog$EarlyStateTimerTask;	 Catch:{ all -> 0x06a2 }
        r5.m1569init();	 Catch:{ all -> 0x06a2 }
        r10.earlyStateTimerTask = r5;	 Catch:{ all -> 0x06a2 }
        r5 = r10.sipStack;	 Catch:{ all -> 0x06a2 }
        r5 = r5.getTimer();	 Catch:{ all -> 0x06a2 }
        if (r5 == 0) goto L_0x013b;
    L_0x011f:
        r5 = r10.sipStack;	 Catch:{ all -> 0x06a2 }
        r5 = r5.getTimer();	 Catch:{ all -> 0x06a2 }
        r5 = r5.isStarted();	 Catch:{ all -> 0x06a2 }
        if (r5 == 0) goto L_0x013b;
    L_0x012b:
        r5 = r10.sipStack;	 Catch:{ all -> 0x06a2 }
        r5 = r5.getTimer();	 Catch:{ all -> 0x06a2 }
        r6 = r10.earlyStateTimerTask;	 Catch:{ all -> 0x06a2 }
        r7 = r10.earlyDialogTimeout;	 Catch:{ all -> 0x06a2 }
        r7 = r7 * 1000;
        r8 = (long) r7;	 Catch:{ all -> 0x06a2 }
        r5.schedule(r6, r8);	 Catch:{ all -> 0x06a2 }
    L_0x013b:
        r10.releaseTimerTaskSem();
        goto L_0x001f;
    L_0x0140:
        r5 = logger;	 Catch:{ all -> 0x0266 }
        r6 = 32;
        r5 = r5.isLoggingEnabled(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x01d1;
    L_0x014a:
        r5 = logger;	 Catch:{ all -> 0x0266 }
        r5.logStackTrace();	 Catch:{ all -> 0x0266 }
        r5 = logger;	 Catch:{ all -> 0x0266 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0266 }
        r6.<init>();	 Catch:{ all -> 0x0266 }
        r7 = "cseqMethod = ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x0266 }
        r7 = r10.lastResponseMethod;	 Catch:{ all -> 0x0266 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0266 }
        r6 = r6.toString();	 Catch:{ all -> 0x0266 }
        r5.logDebug(r6);	 Catch:{ all -> 0x0266 }
        r5 = logger;	 Catch:{ all -> 0x0266 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0266 }
        r6.<init>();	 Catch:{ all -> 0x0266 }
        r7 = "dialogState = ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x0266 }
        r7 = r10.getState();	 Catch:{ all -> 0x0266 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0266 }
        r6 = r6.toString();	 Catch:{ all -> 0x0266 }
        r5.logDebug(r6);	 Catch:{ all -> 0x0266 }
        r5 = logger;	 Catch:{ all -> 0x0266 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0266 }
        r6.<init>();	 Catch:{ all -> 0x0266 }
        r7 = "method = ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x0266 }
        r7 = r10.getMethod();	 Catch:{ all -> 0x0266 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0266 }
        r6 = r6.toString();	 Catch:{ all -> 0x0266 }
        r5.logDebug(r6);	 Catch:{ all -> 0x0266 }
        r5 = logger;	 Catch:{ all -> 0x0266 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0266 }
        r6.<init>();	 Catch:{ all -> 0x0266 }
        r7 = "statusCode = ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x0266 }
        r6 = r6.append(r4);	 Catch:{ all -> 0x0266 }
        r6 = r6.toString();	 Catch:{ all -> 0x0266 }
        r5.logDebug(r6);	 Catch:{ all -> 0x0266 }
        r5 = logger;	 Catch:{ all -> 0x0266 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0266 }
        r6.<init>();	 Catch:{ all -> 0x0266 }
        r7 = "transaction = ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x0266 }
        r6 = r6.append(r11);	 Catch:{ all -> 0x0266 }
        r6 = r6.toString();	 Catch:{ all -> 0x0266 }
        r5.logDebug(r6);	 Catch:{ all -> 0x0266 }
    L_0x01d1:
        if (r11 == 0) goto L_0x01d7;
    L_0x01d3:
        r5 = r11 instanceof org.jitsi.javax.sip.ClientTransaction;	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x04f2;
    L_0x01d7:
        r5 = r10.lastResponseMethod;	 Catch:{ all -> 0x0266 }
        r5 = org.jitsi.gov.nist.javax.sip.stack.SIPTransactionStack.isDialogCreated(r5);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x041b;
    L_0x01df:
        r5 = r10.getState();	 Catch:{ all -> 0x0266 }
        if (r5 != 0) goto L_0x02e0;
    L_0x01e5:
        r5 = r4 / 100;
        if (r5 != r8) goto L_0x02e0;
    L_0x01e9:
        r5 = 0;
        r10.setState(r5);	 Catch:{ all -> 0x0266 }
        r5 = r12.getToTag();	 Catch:{ all -> 0x0266 }
        if (r5 != 0) goto L_0x01f9;
    L_0x01f3:
        r5 = r10.sipStack;	 Catch:{ all -> 0x0266 }
        r5 = r5.rfc2543Supported;	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x0216;
    L_0x01f9:
        r5 = r10.getRemoteTag();	 Catch:{ all -> 0x0266 }
        if (r5 != 0) goto L_0x0216;
    L_0x01ff:
        r5 = r12.getToTag();	 Catch:{ all -> 0x0266 }
        r10.setRemoteTag(r5);	 Catch:{ all -> 0x0266 }
        r5 = 0;
        r5 = r12.getDialogId(r5);	 Catch:{ all -> 0x0266 }
        r10.setDialogId(r5);	 Catch:{ all -> 0x0266 }
        r5 = r10.sipStack;	 Catch:{ all -> 0x0266 }
        r5.putDialog(r10);	 Catch:{ all -> 0x0266 }
        r10.addRoute(r12);	 Catch:{ all -> 0x0266 }
    L_0x0216:
        r5 = r10.getState();	 Catch:{ all -> 0x0266 }
        r6 = org.jitsi.javax.sip.DialogState.CONFIRMED;	 Catch:{ all -> 0x0266 }
        if (r5 == r6) goto L_0x045c;
    L_0x021e:
        r5 = r10.getState();	 Catch:{ all -> 0x0266 }
        r6 = org.jitsi.javax.sip.DialogState.TERMINATED;	 Catch:{ all -> 0x0266 }
        if (r5 == r6) goto L_0x045c;
    L_0x0226:
        r5 = r10.getOriginalRequestRecordRouteHeaders();	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x045c;
    L_0x022c:
        r5 = r10.getOriginalRequestRecordRouteHeaders();	 Catch:{ all -> 0x0266 }
        r6 = r10.getOriginalRequestRecordRouteHeaders();	 Catch:{ all -> 0x0266 }
        r6 = r6.size();	 Catch:{ all -> 0x0266 }
        r1 = r5.listIterator(r6);	 Catch:{ all -> 0x0266 }
    L_0x023c:
        r5 = r1.hasPrevious();	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x045c;
    L_0x0242:
        r3 = r1.previous();	 Catch:{ all -> 0x0266 }
        r3 = (org.jitsi.gov.nist.javax.sip.header.RecordRoute) r3;	 Catch:{ all -> 0x0266 }
        r5 = r10.routeList;	 Catch:{ all -> 0x0266 }
        r2 = r5.getFirst();	 Catch:{ all -> 0x0266 }
        r2 = (org.jitsi.gov.nist.javax.sip.header.Route) r2;	 Catch:{ all -> 0x0266 }
        if (r2 == 0) goto L_0x045c;
    L_0x0252:
        r5 = r3.getAddress();	 Catch:{ all -> 0x0266 }
        r6 = r2.getAddress();	 Catch:{ all -> 0x0266 }
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x045c;
    L_0x0260:
        r5 = r10.routeList;	 Catch:{ all -> 0x0266 }
        r5.removeFirst();	 Catch:{ all -> 0x0266 }
        goto L_0x023c;
    L_0x0266:
        r5 = move-exception;
        r6 = r12.getCSeq();
        r6 = r6.getMethod();
        r7 = "INVITE";
        r6 = r6.equals(r7);
        if (r6 == 0) goto L_0x02df;
    L_0x0277:
        r6 = r11 instanceof org.jitsi.javax.sip.ClientTransaction;
        if (r6 == 0) goto L_0x02df;
    L_0x027b:
        r10.acquireTimerTaskSem();
        r6 = r10.getState();	 Catch:{ all -> 0x0689 }
        r7 = org.jitsi.javax.sip.DialogState.EARLY;	 Catch:{ all -> 0x0689 }
        if (r6 != r7) goto L_0x068e;
    L_0x0286:
        r6 = r10.earlyStateTimerTask;	 Catch:{ all -> 0x0689 }
        if (r6 == 0) goto L_0x0295;
    L_0x028a:
        r6 = r10.sipStack;	 Catch:{ all -> 0x0689 }
        r6 = r6.getTimer();	 Catch:{ all -> 0x0689 }
        r7 = r10.earlyStateTimerTask;	 Catch:{ all -> 0x0689 }
        r6.cancel(r7);	 Catch:{ all -> 0x0689 }
    L_0x0295:
        r6 = logger;	 Catch:{ all -> 0x0689 }
        r7 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0689 }
        r7.<init>();	 Catch:{ all -> 0x0689 }
        r8 = "EarlyStateTimerTask craeted ";
        r7 = r7.append(r8);	 Catch:{ all -> 0x0689 }
        r8 = r10.earlyDialogTimeout;	 Catch:{ all -> 0x0689 }
        r8 = r8 * 1000;
        r7 = r7.append(r8);	 Catch:{ all -> 0x0689 }
        r7 = r7.toString();	 Catch:{ all -> 0x0689 }
        r6.logDebug(r7);	 Catch:{ all -> 0x0689 }
        r6 = new org.jitsi.gov.nist.javax.sip.stack.SIPDialog$EarlyStateTimerTask;	 Catch:{ all -> 0x0689 }
        r6.m1569init();	 Catch:{ all -> 0x0689 }
        r10.earlyStateTimerTask = r6;	 Catch:{ all -> 0x0689 }
        r6 = r10.sipStack;	 Catch:{ all -> 0x0689 }
        r6 = r6.getTimer();	 Catch:{ all -> 0x0689 }
        if (r6 == 0) goto L_0x02dc;
    L_0x02c0:
        r6 = r10.sipStack;	 Catch:{ all -> 0x0689 }
        r6 = r6.getTimer();	 Catch:{ all -> 0x0689 }
        r6 = r6.isStarted();	 Catch:{ all -> 0x0689 }
        if (r6 == 0) goto L_0x02dc;
    L_0x02cc:
        r6 = r10.sipStack;	 Catch:{ all -> 0x0689 }
        r6 = r6.getTimer();	 Catch:{ all -> 0x0689 }
        r7 = r10.earlyStateTimerTask;	 Catch:{ all -> 0x0689 }
        r8 = r10.earlyDialogTimeout;	 Catch:{ all -> 0x0689 }
        r8 = r8 * 1000;
        r8 = (long) r8;	 Catch:{ all -> 0x0689 }
        r6.schedule(r7, r8);	 Catch:{ all -> 0x0689 }
    L_0x02dc:
        r10.releaseTimerTaskSem();
    L_0x02df:
        throw r5;
    L_0x02e0:
        r5 = r10.getState();	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x0329;
    L_0x02e6:
        r5 = r10.getState();	 Catch:{ all -> 0x0266 }
        r6 = org.jitsi.javax.sip.DialogState.EARLY;	 Catch:{ all -> 0x0266 }
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x0329;
    L_0x02f2:
        r5 = r4 / 100;
        if (r5 != r8) goto L_0x0329;
    L_0x02f6:
        r5 = r10.lastResponseMethod;	 Catch:{ all -> 0x0266 }
        r6 = r10.getMethod();	 Catch:{ all -> 0x0266 }
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x0216;
    L_0x0302:
        if (r11 == 0) goto L_0x0216;
    L_0x0304:
        r5 = r12.getToTag();	 Catch:{ all -> 0x0266 }
        if (r5 != 0) goto L_0x0310;
    L_0x030a:
        r5 = r10.sipStack;	 Catch:{ all -> 0x0266 }
        r5 = r5.rfc2543Supported;	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x0216;
    L_0x0310:
        r5 = r12.getToTag();	 Catch:{ all -> 0x0266 }
        r10.setRemoteTag(r5);	 Catch:{ all -> 0x0266 }
        r5 = 0;
        r5 = r12.getDialogId(r5);	 Catch:{ all -> 0x0266 }
        r10.setDialogId(r5);	 Catch:{ all -> 0x0266 }
        r5 = r10.sipStack;	 Catch:{ all -> 0x0266 }
        r5.putDialog(r10);	 Catch:{ all -> 0x0266 }
        r10.addRoute(r12);	 Catch:{ all -> 0x0266 }
        goto L_0x0216;
    L_0x0329:
        r5 = r4 / 100;
        if (r5 != r9) goto L_0x03f1;
    L_0x032d:
        r5 = logger;	 Catch:{ all -> 0x0266 }
        r6 = 32;
        r5 = r5.isLoggingEnabled(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x0351;
    L_0x0337:
        r5 = logger;	 Catch:{ all -> 0x0266 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0266 }
        r6.<init>();	 Catch:{ all -> 0x0266 }
        r7 = "pendingRouteUpdateOn202Response : ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x0266 }
        r7 = r10.pendingRouteUpdateOn202Response;	 Catch:{ all -> 0x0266 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0266 }
        r6 = r6.toString();	 Catch:{ all -> 0x0266 }
        r5.logDebug(r6);	 Catch:{ all -> 0x0266 }
    L_0x0351:
        r5 = r10.lastResponseMethod;	 Catch:{ all -> 0x0266 }
        r6 = r10.getMethod();	 Catch:{ all -> 0x0266 }
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x03d5;
    L_0x035d:
        r5 = r12.getToTag();	 Catch:{ all -> 0x0266 }
        if (r5 != 0) goto L_0x0369;
    L_0x0363:
        r5 = r10.sipStack;	 Catch:{ all -> 0x0266 }
        r5 = r5.rfc2543Supported;	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x03d5;
    L_0x0369:
        r5 = r10.getState();	 Catch:{ all -> 0x0266 }
        r6 = org.jitsi.javax.sip.DialogState.CONFIRMED;	 Catch:{ all -> 0x0266 }
        if (r5 != r6) goto L_0x038f;
    L_0x0371:
        r5 = r10.getState();	 Catch:{ all -> 0x0266 }
        r6 = org.jitsi.javax.sip.DialogState.CONFIRMED;	 Catch:{ all -> 0x0266 }
        if (r5 != r6) goto L_0x03d5;
    L_0x0379:
        r5 = r10.lastResponseMethod;	 Catch:{ all -> 0x0266 }
        r6 = "SUBSCRIBE";
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x03d5;
    L_0x0383:
        r5 = r10.pendingRouteUpdateOn202Response;	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x03d5;
    L_0x0387:
        r5 = r12.getStatusCode();	 Catch:{ all -> 0x0266 }
        r6 = 202; // 0xca float:2.83E-43 double:1.0E-321;
        if (r5 != r6) goto L_0x03d5;
    L_0x038f:
        r5 = r10.getState();	 Catch:{ all -> 0x0266 }
        r6 = org.jitsi.javax.sip.DialogState.CONFIRMED;	 Catch:{ all -> 0x0266 }
        if (r5 == r6) goto L_0x03b2;
    L_0x0397:
        r5 = r12.getToTag();	 Catch:{ all -> 0x0266 }
        r10.setRemoteTag(r5);	 Catch:{ all -> 0x0266 }
        r5 = 0;
        r5 = r12.getDialogId(r5);	 Catch:{ all -> 0x0266 }
        r10.setDialogId(r5);	 Catch:{ all -> 0x0266 }
        r5 = r10.sipStack;	 Catch:{ all -> 0x0266 }
        r5.putDialog(r10);	 Catch:{ all -> 0x0266 }
        r10.addRoute(r12);	 Catch:{ all -> 0x0266 }
        r5 = 1;
        r10.setState(r5);	 Catch:{ all -> 0x0266 }
    L_0x03b2:
        r5 = r10.lastResponseMethod;	 Catch:{ all -> 0x0266 }
        r6 = "SUBSCRIBE";
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x03d5;
    L_0x03bc:
        r5 = r12.getStatusCode();	 Catch:{ all -> 0x0266 }
        r6 = 202; // 0xca float:2.83E-43 double:1.0E-321;
        if (r5 != r6) goto L_0x03d5;
    L_0x03c4:
        r5 = r10.pendingRouteUpdateOn202Response;	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x03d5;
    L_0x03c8:
        r5 = r12.getToTag();	 Catch:{ all -> 0x0266 }
        r10.setRemoteTag(r5);	 Catch:{ all -> 0x0266 }
        r10.addRoute(r12);	 Catch:{ all -> 0x0266 }
        r5 = 0;
        r10.pendingRouteUpdateOn202Response = r5;	 Catch:{ all -> 0x0266 }
    L_0x03d5:
        r5 = r10.lastResponseMethod;	 Catch:{ all -> 0x0266 }
        r6 = "INVITE";
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x0216;
    L_0x03df:
        r5 = r12.getCSeq();	 Catch:{ all -> 0x0266 }
        r6 = r5.getSeqNumber();	 Catch:{ all -> 0x0266 }
        r8 = r10.lastInviteOkReceived;	 Catch:{ all -> 0x0266 }
        r6 = java.lang.Math.max(r6, r8);	 Catch:{ all -> 0x0266 }
        r10.lastInviteOkReceived = r6;	 Catch:{ all -> 0x0266 }
        goto L_0x0216;
    L_0x03f1:
        r5 = 300; // 0x12c float:4.2E-43 double:1.48E-321;
        if (r4 < r5) goto L_0x0216;
    L_0x03f5:
        r5 = 699; // 0x2bb float:9.8E-43 double:3.454E-321;
        if (r4 > r5) goto L_0x0216;
    L_0x03f9:
        r5 = r10.getState();	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x0415;
    L_0x03ff:
        r5 = r10.lastResponseMethod;	 Catch:{ all -> 0x0266 }
        r6 = r10.getMethod();	 Catch:{ all -> 0x0266 }
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x0216;
    L_0x040b:
        r5 = r10.getState();	 Catch:{ all -> 0x0266 }
        r5 = r5.getValue();	 Catch:{ all -> 0x0266 }
        if (r5 != 0) goto L_0x0216;
    L_0x0415:
        r5 = 3;
        r10.setState(r5);	 Catch:{ all -> 0x0266 }
        goto L_0x0216;
    L_0x041b:
        r5 = r10.lastResponseMethod;	 Catch:{ all -> 0x0266 }
        r6 = "NOTIFY";
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x04d8;
    L_0x0425:
        r5 = r10.getMethod();	 Catch:{ all -> 0x0266 }
        r6 = "SUBSCRIBE";
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0266 }
        if (r5 != 0) goto L_0x043d;
    L_0x0431:
        r5 = r10.getMethod();	 Catch:{ all -> 0x0266 }
        r6 = "REFER";
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x04d8;
    L_0x043d:
        r5 = r12.getStatusCode();	 Catch:{ all -> 0x0266 }
        r5 = r5 / 100;
        if (r5 != r9) goto L_0x04d8;
    L_0x0445:
        r5 = r10.getState();	 Catch:{ all -> 0x0266 }
        if (r5 != 0) goto L_0x04d8;
    L_0x044b:
        r5 = 1;
        r5 = r12.getDialogId(r5);	 Catch:{ all -> 0x0266 }
        r10.setDialogId(r5);	 Catch:{ all -> 0x0266 }
        r5 = r10.sipStack;	 Catch:{ all -> 0x0266 }
        r5.putDialog(r10);	 Catch:{ all -> 0x0266 }
        r5 = 1;
        r10.setState(r5);	 Catch:{ all -> 0x0266 }
    L_0x045c:
        r5 = r12.getCSeq();
        r5 = r5.getMethod();
        r6 = "INVITE";
        r5 = r5.equals(r6);
        if (r5 == 0) goto L_0x001f;
    L_0x046c:
        r5 = r11 instanceof org.jitsi.javax.sip.ClientTransaction;
        if (r5 == 0) goto L_0x001f;
    L_0x0470:
        r10.acquireTimerTaskSem();
        r5 = r10.getState();	 Catch:{ all -> 0x04d3 }
        r6 = org.jitsi.javax.sip.DialogState.EARLY;	 Catch:{ all -> 0x04d3 }
        if (r5 != r6) goto L_0x06bb;
    L_0x047b:
        r5 = r10.earlyStateTimerTask;	 Catch:{ all -> 0x04d3 }
        if (r5 == 0) goto L_0x048a;
    L_0x047f:
        r5 = r10.sipStack;	 Catch:{ all -> 0x04d3 }
        r5 = r5.getTimer();	 Catch:{ all -> 0x04d3 }
        r6 = r10.earlyStateTimerTask;	 Catch:{ all -> 0x04d3 }
        r5.cancel(r6);	 Catch:{ all -> 0x04d3 }
    L_0x048a:
        r5 = logger;	 Catch:{ all -> 0x04d3 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x04d3 }
        r6.<init>();	 Catch:{ all -> 0x04d3 }
        r7 = "EarlyStateTimerTask craeted ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x04d3 }
        r7 = r10.earlyDialogTimeout;	 Catch:{ all -> 0x04d3 }
        r7 = r7 * 1000;
        r6 = r6.append(r7);	 Catch:{ all -> 0x04d3 }
        r6 = r6.toString();	 Catch:{ all -> 0x04d3 }
        r5.logDebug(r6);	 Catch:{ all -> 0x04d3 }
        r5 = new org.jitsi.gov.nist.javax.sip.stack.SIPDialog$EarlyStateTimerTask;	 Catch:{ all -> 0x04d3 }
        r5.m1569init();	 Catch:{ all -> 0x04d3 }
        r10.earlyStateTimerTask = r5;	 Catch:{ all -> 0x04d3 }
        r5 = r10.sipStack;	 Catch:{ all -> 0x04d3 }
        r5 = r5.getTimer();	 Catch:{ all -> 0x04d3 }
        if (r5 == 0) goto L_0x013b;
    L_0x04b5:
        r5 = r10.sipStack;	 Catch:{ all -> 0x04d3 }
        r5 = r5.getTimer();	 Catch:{ all -> 0x04d3 }
        r5 = r5.isStarted();	 Catch:{ all -> 0x04d3 }
        if (r5 == 0) goto L_0x013b;
    L_0x04c1:
        r5 = r10.sipStack;	 Catch:{ all -> 0x04d3 }
        r5 = r5.getTimer();	 Catch:{ all -> 0x04d3 }
        r6 = r10.earlyStateTimerTask;	 Catch:{ all -> 0x04d3 }
        r7 = r10.earlyDialogTimeout;	 Catch:{ all -> 0x04d3 }
        r7 = r7 * 1000;
        r8 = (long) r7;	 Catch:{ all -> 0x04d3 }
        r5.schedule(r6, r8);	 Catch:{ all -> 0x04d3 }
        goto L_0x013b;
    L_0x04d3:
        r5 = move-exception;
        r10.releaseTimerTaskSem();
        throw r5;
    L_0x04d8:
        r5 = r10.lastResponseMethod;	 Catch:{ all -> 0x0266 }
        r6 = "BYE";
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x045c;
    L_0x04e2:
        r5 = r4 / 100;
        if (r5 != r9) goto L_0x045c;
    L_0x04e6:
        r5 = r10.isTerminatedOnBye();	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x045c;
    L_0x04ec:
        r5 = 3;
        r10.setState(r5);	 Catch:{ all -> 0x0266 }
        goto L_0x045c;
    L_0x04f2:
        r5 = r10.lastResponseMethod;	 Catch:{ all -> 0x0266 }
        r6 = "BYE";
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x050c;
    L_0x04fc:
        r5 = r4 / 100;
        if (r5 != r9) goto L_0x050c;
    L_0x0500:
        r5 = r10.isTerminatedOnBye();	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x050c;
    L_0x0506:
        r5 = 3;
        r10.setState(r5);	 Catch:{ all -> 0x0266 }
        goto L_0x045c;
    L_0x050c:
        r0 = 0;
        r5 = r10.getLocalTag();	 Catch:{ all -> 0x0266 }
        if (r5 != 0) goto L_0x053d;
    L_0x0513:
        r5 = r12.getTo();	 Catch:{ all -> 0x0266 }
        r5 = r5.getTag();	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x053d;
    L_0x051d:
        r5 = r10.lastResponseMethod;	 Catch:{ all -> 0x0266 }
        r5 = org.jitsi.gov.nist.javax.sip.stack.SIPTransactionStack.isDialogCreated(r5);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x053d;
    L_0x0525:
        r5 = r10.lastResponseMethod;	 Catch:{ all -> 0x0266 }
        r6 = r10.getMethod();	 Catch:{ all -> 0x0266 }
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x053d;
    L_0x0531:
        r5 = r12.getTo();	 Catch:{ all -> 0x0266 }
        r5 = r5.getTag();	 Catch:{ all -> 0x0266 }
        r10.setLocalTag(r5);	 Catch:{ all -> 0x0266 }
        r0 = 1;
    L_0x053d:
        r5 = r4 / 100;
        if (r5 == r9) goto L_0x0599;
    L_0x0541:
        r5 = r4 / 100;
        if (r5 != r8) goto L_0x055a;
    L_0x0545:
        if (r0 == 0) goto L_0x045c;
    L_0x0547:
        r5 = 0;
        r10.setState(r5);	 Catch:{ all -> 0x0266 }
        r5 = 1;
        r5 = r12.getDialogId(r5);	 Catch:{ all -> 0x0266 }
        r10.setDialogId(r5);	 Catch:{ all -> 0x0266 }
        r5 = r10.sipStack;	 Catch:{ all -> 0x0266 }
        r5.putDialog(r10);	 Catch:{ all -> 0x0266 }
        goto L_0x045c;
    L_0x055a:
        r5 = 489; // 0x1e9 float:6.85E-43 double:2.416E-321;
        if (r4 != r5) goto L_0x0585;
    L_0x055e:
        r5 = r10.lastResponseMethod;	 Catch:{ all -> 0x0266 }
        r6 = "NOTIFY";
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0266 }
        if (r5 != 0) goto L_0x0572;
    L_0x0568:
        r5 = r10.lastResponseMethod;	 Catch:{ all -> 0x0266 }
        r6 = "SUBSCRIBE";
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x0585;
    L_0x0572:
        r5 = logger;	 Catch:{ all -> 0x0266 }
        r6 = 32;
        r5 = r5.isLoggingEnabled(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x045c;
    L_0x057c:
        r5 = logger;	 Catch:{ all -> 0x0266 }
        r6 = "RFC 3265 : Not setting dialog to TERMINATED for 489";
        r5.logDebug(r6);	 Catch:{ all -> 0x0266 }
        goto L_0x045c;
    L_0x0585:
        r5 = r10.isReInvite();	 Catch:{ all -> 0x0266 }
        if (r5 != 0) goto L_0x045c;
    L_0x058b:
        r5 = r10.getState();	 Catch:{ all -> 0x0266 }
        r6 = org.jitsi.javax.sip.DialogState.CONFIRMED;	 Catch:{ all -> 0x0266 }
        if (r5 == r6) goto L_0x045c;
    L_0x0593:
        r5 = 3;
        r10.setState(r5);	 Catch:{ all -> 0x0266 }
        goto L_0x045c;
    L_0x0599:
        r5 = r10.dialogState;	 Catch:{ all -> 0x0266 }
        if (r5 > 0) goto L_0x05bf;
    L_0x059d:
        r5 = r10.lastResponseMethod;	 Catch:{ all -> 0x0266 }
        r6 = "INVITE";
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0266 }
        if (r5 != 0) goto L_0x05bb;
    L_0x05a7:
        r5 = r10.lastResponseMethod;	 Catch:{ all -> 0x0266 }
        r6 = "SUBSCRIBE";
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0266 }
        if (r5 != 0) goto L_0x05bb;
    L_0x05b1:
        r5 = r10.lastResponseMethod;	 Catch:{ all -> 0x0266 }
        r6 = "REFER";
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x05bf;
    L_0x05bb:
        r5 = 1;
        r10.setState(r5);	 Catch:{ all -> 0x0266 }
    L_0x05bf:
        if (r0 == 0) goto L_0x05ce;
    L_0x05c1:
        r5 = 1;
        r5 = r12.getDialogId(r5);	 Catch:{ all -> 0x0266 }
        r10.setDialogId(r5);	 Catch:{ all -> 0x0266 }
        r5 = r10.sipStack;	 Catch:{ all -> 0x0266 }
        r5.putDialog(r10);	 Catch:{ all -> 0x0266 }
    L_0x05ce:
        r5 = r11.getInternalState();	 Catch:{ all -> 0x0266 }
        r6 = 5;
        if (r5 == r6) goto L_0x045c;
    L_0x05d5:
        r5 = r12.getStatusCode();	 Catch:{ all -> 0x0266 }
        r6 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
        if (r5 != r6) goto L_0x045c;
    L_0x05dd:
        r5 = r10.lastResponseMethod;	 Catch:{ all -> 0x0266 }
        r6 = "INVITE";
        r5 = r5.equals(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x045c;
    L_0x05e7:
        r5 = r10.isBackToBackUserAgent;	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x045c;
    L_0x05eb:
        r5 = r10.takeAckSem();	 Catch:{ all -> 0x0266 }
        if (r5 != 0) goto L_0x045c;
    L_0x05f1:
        r5 = logger;	 Catch:{ all -> 0x0266 }
        r6 = 32;
        r5 = r5.isLoggingEnabled(r6);	 Catch:{ all -> 0x0266 }
        if (r5 == 0) goto L_0x0602;
    L_0x05fb:
        r5 = logger;	 Catch:{ all -> 0x0266 }
        r6 = "Delete dialog -- cannot acquire ackSem";
        r5.logDebug(r6);	 Catch:{ all -> 0x0266 }
    L_0x0602:
        r5 = 5;
        r10.raiseErrorEvent(r5);	 Catch:{ all -> 0x0266 }
        r5 = logger;	 Catch:{ all -> 0x0266 }
        r6 = "IntenalError : Ack Sem already acquired ";
        r5.logError(r6);	 Catch:{ all -> 0x0266 }
        r5 = r12.getCSeq();
        r5 = r5.getMethod();
        r6 = "INVITE";
        r5 = r5.equals(r6);
        if (r5 == 0) goto L_0x001f;
    L_0x061d:
        r5 = r11 instanceof org.jitsi.javax.sip.ClientTransaction;
        if (r5 == 0) goto L_0x001f;
    L_0x0621:
        r10.acquireTimerTaskSem();
        r5 = r10.getState();	 Catch:{ all -> 0x0684 }
        r6 = org.jitsi.javax.sip.DialogState.EARLY;	 Catch:{ all -> 0x0684 }
        if (r5 != r6) goto L_0x06cf;
    L_0x062c:
        r5 = r10.earlyStateTimerTask;	 Catch:{ all -> 0x0684 }
        if (r5 == 0) goto L_0x063b;
    L_0x0630:
        r5 = r10.sipStack;	 Catch:{ all -> 0x0684 }
        r5 = r5.getTimer();	 Catch:{ all -> 0x0684 }
        r6 = r10.earlyStateTimerTask;	 Catch:{ all -> 0x0684 }
        r5.cancel(r6);	 Catch:{ all -> 0x0684 }
    L_0x063b:
        r5 = logger;	 Catch:{ all -> 0x0684 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0684 }
        r6.<init>();	 Catch:{ all -> 0x0684 }
        r7 = "EarlyStateTimerTask craeted ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x0684 }
        r7 = r10.earlyDialogTimeout;	 Catch:{ all -> 0x0684 }
        r7 = r7 * 1000;
        r6 = r6.append(r7);	 Catch:{ all -> 0x0684 }
        r6 = r6.toString();	 Catch:{ all -> 0x0684 }
        r5.logDebug(r6);	 Catch:{ all -> 0x0684 }
        r5 = new org.jitsi.gov.nist.javax.sip.stack.SIPDialog$EarlyStateTimerTask;	 Catch:{ all -> 0x0684 }
        r5.m1569init();	 Catch:{ all -> 0x0684 }
        r10.earlyStateTimerTask = r5;	 Catch:{ all -> 0x0684 }
        r5 = r10.sipStack;	 Catch:{ all -> 0x0684 }
        r5 = r5.getTimer();	 Catch:{ all -> 0x0684 }
        if (r5 == 0) goto L_0x013b;
    L_0x0666:
        r5 = r10.sipStack;	 Catch:{ all -> 0x0684 }
        r5 = r5.getTimer();	 Catch:{ all -> 0x0684 }
        r5 = r5.isStarted();	 Catch:{ all -> 0x0684 }
        if (r5 == 0) goto L_0x013b;
    L_0x0672:
        r5 = r10.sipStack;	 Catch:{ all -> 0x0684 }
        r5 = r5.getTimer();	 Catch:{ all -> 0x0684 }
        r6 = r10.earlyStateTimerTask;	 Catch:{ all -> 0x0684 }
        r7 = r10.earlyDialogTimeout;	 Catch:{ all -> 0x0684 }
        r7 = r7 * 1000;
        r8 = (long) r7;	 Catch:{ all -> 0x0684 }
        r5.schedule(r6, r8);	 Catch:{ all -> 0x0684 }
        goto L_0x013b;
    L_0x0684:
        r5 = move-exception;
        r10.releaseTimerTaskSem();
        throw r5;
    L_0x0689:
        r5 = move-exception;
        r10.releaseTimerTaskSem();
        throw r5;
    L_0x068e:
        r6 = r10.earlyStateTimerTask;	 Catch:{ all -> 0x0689 }
        if (r6 == 0) goto L_0x02dc;
    L_0x0692:
        r6 = r10.sipStack;	 Catch:{ all -> 0x0689 }
        r6 = r6.getTimer();	 Catch:{ all -> 0x0689 }
        r7 = r10.earlyStateTimerTask;	 Catch:{ all -> 0x0689 }
        r6.cancel(r7);	 Catch:{ all -> 0x0689 }
        r6 = 0;
        r10.earlyStateTimerTask = r6;	 Catch:{ all -> 0x0689 }
        goto L_0x02dc;
    L_0x06a2:
        r5 = move-exception;
        r10.releaseTimerTaskSem();
        throw r5;
    L_0x06a7:
        r5 = r10.earlyStateTimerTask;	 Catch:{ all -> 0x06a2 }
        if (r5 == 0) goto L_0x013b;
    L_0x06ab:
        r5 = r10.sipStack;	 Catch:{ all -> 0x06a2 }
        r5 = r5.getTimer();	 Catch:{ all -> 0x06a2 }
        r6 = r10.earlyStateTimerTask;	 Catch:{ all -> 0x06a2 }
        r5.cancel(r6);	 Catch:{ all -> 0x06a2 }
        r5 = 0;
        r10.earlyStateTimerTask = r5;	 Catch:{ all -> 0x06a2 }
        goto L_0x013b;
    L_0x06bb:
        r5 = r10.earlyStateTimerTask;	 Catch:{ all -> 0x04d3 }
        if (r5 == 0) goto L_0x013b;
    L_0x06bf:
        r5 = r10.sipStack;	 Catch:{ all -> 0x04d3 }
        r5 = r5.getTimer();	 Catch:{ all -> 0x04d3 }
        r6 = r10.earlyStateTimerTask;	 Catch:{ all -> 0x04d3 }
        r5.cancel(r6);	 Catch:{ all -> 0x04d3 }
        r5 = 0;
        r10.earlyStateTimerTask = r5;	 Catch:{ all -> 0x04d3 }
        goto L_0x013b;
    L_0x06cf:
        r5 = r10.earlyStateTimerTask;	 Catch:{ all -> 0x0684 }
        if (r5 == 0) goto L_0x013b;
    L_0x06d3:
        r5 = r10.sipStack;	 Catch:{ all -> 0x0684 }
        r5 = r5.getTimer();	 Catch:{ all -> 0x0684 }
        r6 = r10.earlyStateTimerTask;	 Catch:{ all -> 0x0684 }
        r5.cancel(r6);	 Catch:{ all -> 0x0684 }
        r5 = 0;
        r10.earlyStateTimerTask = r5;	 Catch:{ all -> 0x0684 }
        goto L_0x013b;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.javax.sip.stack.SIPDialog.setLastResponse(org.jitsi.gov.nist.javax.sip.stack.SIPTransaction, org.jitsi.gov.nist.javax.sip.message.SIPResponse):void");
    }

    public void startRetransmitTimer(SIPServerTransaction sipServerTx, Response response) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("startRetransmitTimer() " + response.getStatusCode() + " method " + sipServerTx.getMethod());
        }
        if (sipServerTx.isInviteTransaction() && response.getStatusCode() / 100 == 2) {
            startTimer(sipServerTx);
        }
    }

    private void doTargetRefresh(SIPMessage sipMessage) {
        ContactList contactList = sipMessage.getContactHeaders();
        if (contactList != null) {
            setRemoteTarget((Contact) contactList.getFirst());
        }
    }

    private static final boolean optionPresent(ListIterator l, String option) {
        while (l.hasNext()) {
            OptionTag opt = (OptionTag) l.next();
            if (opt != null && option.equalsIgnoreCase(opt.getOptionTag())) {
                return true;
            }
        }
        return false;
    }

    public Response createReliableProvisionalResponse(int statusCode) throws InvalidArgumentException, SipException {
        if (!this.firstTransactionIsServerTransaction) {
            throw new SipException("Not a Server Dialog!");
        } else if (statusCode <= 100 || statusCode > 199) {
            throw new InvalidArgumentException("Bad status code ");
        } else {
            SIPRequest request = this.originalRequest;
            if (request.getMethod().equals("INVITE")) {
                ListIterator<SIPHeader> list = request.getHeaders("Supported");
                if (list == null || !optionPresent(list, "100rel")) {
                    list = request.getHeaders("Require");
                    if (list == null || !optionPresent(list, "100rel")) {
                        throw new SipException("No Supported/Require 100rel header in the request");
                    }
                }
                SIPResponse response = request.createResponse(statusCode);
                Require require = new Require();
                try {
                    require.setOptionTag("100rel");
                } catch (Exception ex) {
                    InternalErrorHandler.handleException(ex);
                }
                response.addHeader((Header) require);
                new RSeq().setSeqNumber(1);
                RecordRouteList rrl = request.getRecordRouteHeaders();
                if (rrl != null) {
                    response.setHeader((Header) (RecordRouteList) rrl.clone());
                }
                return response;
            }
            throw new SipException("Bad method");
        }
    }

    public boolean handlePrack(SIPRequest prackRequest) {
        if (isServer()) {
            SIPServerTransaction sipServerTransaction = (SIPServerTransaction) getFirstTransactionInt();
            if (sipServerTransaction.getReliableProvisionalResponse() != null) {
                RAck rack = (RAck) prackRequest.getHeader("RAck");
                if (rack == null) {
                    if (!logger.isLoggingEnabled(32)) {
                        return false;
                    }
                    logger.logDebug("Dropping Prack -- rack header not found");
                    return false;
                } else if (rack.getMethod().equals(sipServerTransaction.getPendingReliableResponseMethod())) {
                    if (rack.getCSeqNumberLong() != sipServerTransaction.getPendingReliableCSeqNumber()) {
                        if (!logger.isLoggingEnabled(32)) {
                            return false;
                        }
                        logger.logDebug("Dropping Prack -- CSeq Header does not match PRACK");
                        return false;
                    } else if (rack.getRSequenceNumber() == sipServerTransaction.getPendingReliableRSeqNumber()) {
                        return sipServerTransaction.prackRecieved();
                    } else {
                        if (!logger.isLoggingEnabled(32)) {
                            return false;
                        }
                        logger.logDebug("Dropping Prack -- RSeq Header does not match PRACK");
                        return false;
                    }
                } else if (!logger.isLoggingEnabled(32)) {
                    return false;
                } else {
                    logger.logDebug("Dropping Prack -- CSeq Header does not match PRACK");
                    return false;
                }
            } else if (!logger.isLoggingEnabled(32)) {
                return false;
            } else {
                logger.logDebug("Dropping Prack -- ReliableResponse not found");
                return false;
            }
        } else if (!logger.isLoggingEnabled(32)) {
            return false;
        } else {
            logger.logDebug("Dropping Prack -- not a server Dialog");
            return false;
        }
    }

    public void sendReliableProvisionalResponse(Response relResponse) throws SipException {
        if (isServer()) {
            SIPResponse sipResponse = (SIPResponse) relResponse;
            if (relResponse.getStatusCode() == 100) {
                throw new SipException("Cannot send 100 as a reliable provisional response");
            } else if (relResponse.getStatusCode() / 100 > 2) {
                throw new SipException("Response code is not a 1xx response - should be in the range 101 to 199 ");
            } else if (sipResponse.getToTag() == null) {
                throw new SipException("Badly formatted response -- To tag mandatory for Reliable Provisional Response");
            } else {
                ListIterator requireList = relResponse.getHeaders("Require");
                boolean found = false;
                if (requireList != null) {
                    while (requireList.hasNext() && !found) {
                        if (((RequireHeader) requireList.next()).getOptionTag().equalsIgnoreCase("100rel")) {
                            found = true;
                        }
                    }
                }
                if (!found) {
                    relResponse.addHeader(new Require("100rel"));
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("Require header with optionTag 100rel is needed -- adding one");
                    }
                }
                SIPServerTransaction serverTransaction = (SIPServerTransaction) getFirstTransactionInt();
                setLastResponse(serverTransaction, sipResponse);
                setDialogId(sipResponse.getDialogId(true));
                serverTransaction.sendReliableProvisionalResponse(relResponse);
                startRetransmitTimer(serverTransaction, relResponse);
                return;
            }
        }
        throw new SipException("Not a Server Dialog");
    }

    public void terminateOnBye(boolean terminateFlag) throws SipException {
        this.terminateOnBye = terminateFlag;
    }

    public void setAssigned() {
        this.isAssigned = true;
    }

    public boolean isAssigned() {
        return this.isAssigned;
    }

    public Contact getMyContactHeader() {
        if (this.contactHeader == null && this.contactHeaderStringified != null) {
            try {
                this.contactHeader = (Contact) new ContactParser(this.contactHeaderStringified).parse();
            } catch (ParseException e) {
                logger.logError("error reparsing the contact header", e);
            }
        }
        return this.contactHeader;
    }

    public boolean handleAck(SIPServerTransaction ackTransaction) {
        if (isAckSeen() && getRemoteSeqNumber() == ackTransaction.getCSeq()) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("ACK already seen by dialog -- dropping Ack retransmission");
            }
            acquireTimerTaskSem();
            try {
                if (this.timerTask != null) {
                    getStack().getTimer().cancel(this.timerTask);
                    this.timerTask = null;
                }
                releaseTimerTaskSem();
                return false;
            } catch (Throwable th) {
                releaseTimerTaskSem();
            }
        } else if (getState() == DialogState.TERMINATED) {
            if (!logger.isLoggingEnabled(32)) {
                return false;
            }
            logger.logDebug("Dialog is terminated -- dropping ACK");
            return false;
        } else if (this.lastResponseStatusCode != null && this.lastResponseStatusCode.intValue() / 100 == 2 && this.lastResponseMethod.equals("INVITE") && this.lastResponseCSeqNumber == ackTransaction.getCSeq()) {
            ackTransaction.setDialog(this, this.lastResponseDialogId);
            ackReceived(ackTransaction.getCSeq());
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("ACK for 2XX response --- sending to TU ");
            }
            return true;
        } else {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug(" INVITE transaction not found  -- Discarding ACK");
            }
            if (!isBackToBackUserAgent()) {
                return false;
            }
            releaseAckSem();
            return false;
        }
    }

    /* access modifiers changed from: 0000 */
    public String getEarlyDialogId() {
        return this.earlyDialogId;
    }

    /* access modifiers changed from: 0000 */
    public void releaseAckSem() {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("releaseAckSem-enter]]" + this + " sem=" + this.ackSem + " b2bua=" + this.isBackToBackUserAgent);
            logger.logStackTrace();
        }
        if (this.isBackToBackUserAgent) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("releaseAckSem]]" + this + " sem=" + this.ackSem);
                logger.logStackTrace();
            }
            if (this.ackSem.availablePermits() == 0) {
                this.ackSem.release();
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("releaseAckSem]]" + this + " sem=" + this.ackSem);
                }
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean takeAckSem() {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("[takeAckSem " + this + " sem=" + this.ackSem);
        }
        try {
            if (this.ackSem.tryAcquire(2, TimeUnit.SECONDS)) {
                if (logger.isLoggingEnabled(32)) {
                    recordStackTrace();
                }
                return true;
            }
            if (logger.isLoggingEnabled()) {
                logger.logError("Cannot aquire ACK semaphore ");
            }
            if (!logger.isLoggingEnabled(32)) {
                return false;
            }
            logger.logDebug("Semaphore previously acquired at " + this.stackTrace + " sem=" + this.ackSem);
            logger.logStackTrace();
            return false;
        } catch (InterruptedException e) {
            logger.logError("Cannot aquire ACK semaphore");
            return false;
        }
    }

    private void setLastAckSent(SIPRequest lastAckSent) {
        this.lastAckSent = lastAckSent;
    }

    public boolean isAtleastOneAckSent() {
        return this.isAcknowledged;
    }

    public boolean isBackToBackUserAgent() {
        return this.isBackToBackUserAgent;
    }

    public synchronized void doDeferredDeleteIfNoAckSent(long seqno) {
        if (this.sipStack.getTimer() == null) {
            setState(3);
        } else if (this.dialogDeleteIfNoAckSentTask == null) {
            this.dialogDeleteIfNoAckSentTask = new DialogDeleteIfNoAckSentTask(seqno);
            if (this.sipStack.getTimer() != null && this.sipStack.getTimer().isStarted()) {
                this.sipStack.getTimer().schedule(this.dialogDeleteIfNoAckSentTask, (long) (this.sipStack.getAckTimeoutFactor() * 500));
            }
        }
    }

    public void setBackToBackUserAgent() {
        this.isBackToBackUserAgent = true;
    }

    /* access modifiers changed from: 0000 */
    public EventHeader getEventHeader() {
        return this.eventHeader;
    }

    /* access modifiers changed from: 0000 */
    public void setEventHeader(EventHeader eventHeader) {
        this.eventHeader = eventHeader;
    }

    /* access modifiers changed from: 0000 */
    public void setServerTransactionFlag(boolean serverTransactionFlag) {
        this.serverTransactionFlag = serverTransactionFlag;
    }

    /* access modifiers changed from: protected */
    public void setReInviteFlag(boolean reInviteFlag) {
        this.reInviteFlag = reInviteFlag;
    }

    public boolean isSequnceNumberValidation() {
        return this.sequenceNumberValidation;
    }

    public void disableSequenceNumberValidation() {
        this.sequenceNumberValidation = false;
    }

    public void acquireTimerTaskSem() {
        boolean acquired;
        try {
            acquired = this.timerTaskLock.tryAcquire(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            acquired = false;
        }
        if (!acquired) {
            throw new IllegalStateException("Impossible to acquire the dialog timer task lock");
        }
    }

    public void releaseTimerTaskSem() {
        this.timerTaskLock.release();
    }

    public String getMergeId() {
        return this.firstTransactionMergeId;
    }

    public void setPendingRouteUpdateOn202Response(SIPRequest sipRequest) {
        this.pendingRouteUpdateOn202Response = true;
        String toTag = sipRequest.getToHeader().getTag();
        if (toTag != null) {
            setRemoteTag(toTag);
        }
    }

    public String getLastResponseMethod() {
        return this.lastResponseMethod;
    }

    public Integer getLastResponseStatusCode() {
        return this.lastResponseStatusCode;
    }

    public long getLastResponseCSeqNumber() {
        return this.lastResponseCSeqNumber;
    }

    /* access modifiers changed from: protected */
    public void cleanUpOnAck() {
        if (isReleaseReferences()) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("cleanupOnAck : " + getDialogId());
            }
            if (this.originalRequest != null) {
                if (this.originalRequestRecordRouteHeaders != null) {
                    this.originalRequestRecordRouteHeadersString = this.originalRequestRecordRouteHeaders.toString();
                }
                this.originalRequestRecordRouteHeaders = null;
                this.originalRequest = null;
            }
            if (this.firstTransaction != null) {
                if (this.firstTransaction.getOriginalRequest() != null) {
                    this.firstTransaction.getOriginalRequest().cleanUp();
                }
                this.firstTransaction = null;
            }
            if (this.lastTransaction != null) {
                if (this.lastTransaction.getOriginalRequest() != null) {
                    this.lastTransaction.getOriginalRequest().cleanUp();
                }
                this.lastTransaction = null;
            }
            if (this.callIdHeader != null) {
                this.callIdHeaderString = this.callIdHeader.toString();
                this.callIdHeader = null;
            }
            if (this.contactHeader != null) {
                this.contactHeaderStringified = this.contactHeader.toString();
                this.contactHeader = null;
            }
            if (this.remoteTarget != null) {
                this.remoteTargetStringified = this.remoteTarget.toString();
                this.remoteTarget = null;
            }
            if (this.remoteParty != null) {
                this.remotePartyStringified = this.remoteParty.toString();
                this.remoteParty = null;
            }
            if (this.localParty != null) {
                this.localPartyStringified = this.localParty.toString();
                this.localParty = null;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void cleanUp() {
        if (isReleaseReferences()) {
            cleanUpOnAck();
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("dialog cleanup : " + getDialogId());
            }
            if (this.eventListeners != null) {
                this.eventListeners.clear();
            }
            this.timerTaskLock = null;
            this.ackSem = null;
            this.contactHeader = null;
            this.eventHeader = null;
            this.firstTransactionId = null;
            this.firstTransactionMethod = null;
            this.lastAckSent = null;
            this.lastResponseDialogId = null;
            this.lastResponseMethod = null;
            this.lastResponseTopMostVia = null;
            if (this.originalRequestRecordRouteHeaders != null) {
                this.originalRequestRecordRouteHeaders.clear();
                this.originalRequestRecordRouteHeaders = null;
                this.originalRequestRecordRouteHeadersString = null;
            }
            if (this.routeList != null) {
                this.routeList.clear();
                this.routeList = null;
            }
            this.responsesReceivedInForkingCase.clear();
        }
    }

    /* access modifiers changed from: protected */
    public RecordRouteList getOriginalRequestRecordRouteHeaders() {
        if (this.originalRequestRecordRouteHeaders == null && this.originalRequestRecordRouteHeadersString != null) {
            try {
                this.originalRequestRecordRouteHeaders = (RecordRouteList) new RecordRouteParser(this.originalRequestRecordRouteHeadersString).parse();
            } catch (ParseException e) {
                logger.logError("error reparsing the originalRequest RecordRoute Headers", e);
            }
            this.originalRequestRecordRouteHeadersString = null;
        }
        return this.originalRequestRecordRouteHeaders;
    }

    public Via getLastResponseTopMostVia() {
        return this.lastResponseTopMostVia;
    }

    public boolean isReleaseReferences() {
        return this.releaseReferences;
    }

    public void setReleaseReferences(boolean releaseReferences) {
        this.releaseReferences = releaseReferences;
    }

    public void setEarlyDialogTimeoutSeconds(int seconds) {
        if (seconds <= 0) {
            throw new IllegalArgumentException("Invalid value " + seconds);
        }
        this.earlyDialogTimeout = seconds;
    }

    public void checkRetransmissionForForking(SIPResponse response) {
        boolean isRetransmission = !this.responsesReceivedInForkingCase.add(new StringBuilder().append(response.getStatusCode()).append(Separators.SLASH).append(response.getCSeq().getSeqNumber()).append(Separators.SLASH).append(response.getCSeqHeader().getMethod()).toString());
        response.setRetransmission(isRetransmission);
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("marking response as retransmission " + isRetransmission + " for dialog " + this);
        }
    }
}
