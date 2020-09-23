package org.jitsi.gov.nist.javax.sip.stack;

import java.io.IOException;
import java.net.InetAddress;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLPeerUnverifiedException;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.InternalErrorHandler;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.javax.sip.SIPConstants;
import org.jitsi.gov.nist.javax.sip.SipProviderImpl;
import org.jitsi.gov.nist.javax.sip.SipStackImpl;
import org.jitsi.gov.nist.javax.sip.TransactionExt;
import org.jitsi.gov.nist.javax.sip.address.AddressFactoryImpl;
import org.jitsi.gov.nist.javax.sip.header.Via;
import org.jitsi.gov.nist.javax.sip.message.SIPMessage;
import org.jitsi.gov.nist.javax.sip.message.SIPRequest;
import org.jitsi.gov.nist.javax.sip.message.SIPResponse;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.IOExceptionEvent;
import org.jitsi.javax.sip.ListeningPoint;
import org.jitsi.javax.sip.Transaction;
import org.jitsi.javax.sip.TransactionState;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;

public abstract class SIPTransaction extends MessageChannel implements Transaction, TransactionExt {
    public static final TransactionState CALLING_STATE = TransactionState.CALLING;
    public static final TransactionState COMPLETED_STATE = TransactionState.COMPLETED;
    public static final TransactionState CONFIRMED_STATE = TransactionState.CONFIRMED;
    public static final TransactionState INITIAL_STATE = null;
    protected static final int MAXIMUM_RETRANSMISSION_TICK_COUNT = 8;
    public static final TransactionState PROCEEDING_STATE = TransactionState.PROCEEDING;
    protected static final int T1 = 1;
    public static final TransactionState TERMINATED_STATE = TransactionState.TERMINATED;
    protected static final int TIMER_A = 1;
    protected static final int TIMER_B = 64;
    protected static final int TIMER_F = 64;
    protected static final int TIMER_H = 64;
    protected static final int TIMER_J = 64;
    public static final TransactionState TRYING_STATE = TransactionState.TRYING;
    /* access modifiers changed from: private|static */
    public static StackLogger logger = CommonLogger.getLogger(SIPTransaction.class);
    protected int BASE_TIMER_INTERVAL = 500;
    protected int T2 = (4000 / this.BASE_TIMER_INTERVAL);
    protected int T4 = (5000 / this.BASE_TIMER_INTERVAL);
    protected int TIMER_D = (32000 / this.BASE_TIMER_INTERVAL);
    protected int TIMER_I = this.T4;
    protected int TIMER_K = this.T4;
    protected transient Object applicationData;
    public long auditTag = 0;
    private String branch;
    protected int collectionTime;
    private int currentState = -1;
    private Boolean dialogCreatingTransaction = null;
    private transient MessageChannel encapsulatedChannel;
    private transient Set<SIPTransactionEventListener> eventListeners;
    public ExpiresTimerTask expiresTimerTask;
    private String forkId = null;
    private Boolean inviteTransaction = null;
    protected boolean isMapped;
    protected SIPResponse lastResponse;
    private String method;
    protected SIPRequest originalRequest;
    protected String originalRequestBranch;
    protected byte[] originalRequestBytes;
    protected long originalRequestCSeqNumber;
    protected boolean originalRequestHasPort;
    private boolean releaseReferences;
    private transient int retransmissionTimerLastTickCount;
    private transient int retransmissionTimerTicksLeft;
    private TransactionSemaphore semaphore;
    protected transient SIPTransactionStack sipStack;
    private boolean terminatedEventDelivered;
    protected int timeoutTimerTicksLeft;
    protected boolean toListener;
    protected String transactionId;
    protected AtomicBoolean transactionTimerStarted = new AtomicBoolean(false);

    class LingerTimer extends SIPStackTimerTask {
        public LingerTimer() {
            if (SIPTransaction.logger.isLoggingEnabled(32)) {
                SIPTransaction.logger.logDebug("LingerTimer : " + SIPTransaction.this.getTransactionId());
            }
        }

        public void runTask() {
            SIPTransaction.this.cleanUp();
        }
    }

    class TransactionSemaphore {
        private static final long serialVersionUID = -1634100711669020804L;
        ReentrantLock lock = null;
        Semaphore sem = null;

        public TransactionSemaphore() {
            if (((SipStackImpl) SIPTransaction.this.getSIPStack()).isReEntrantListener()) {
                this.lock = new ReentrantLock();
            } else {
                this.sem = new Semaphore(1, true);
            }
        }

        public boolean acquire() {
            try {
                if (((SipStackImpl) SIPTransaction.this.getSIPStack()).isReEntrantListener()) {
                    this.lock.lock();
                } else {
                    this.sem.acquire();
                }
                return true;
            } catch (Exception ex) {
                SIPTransaction.logger.logError("Unexpected exception acquiring sem", ex);
                InternalErrorHandler.handleException(ex);
                return false;
            }
        }

        public boolean tryAcquire() {
            try {
                if (((SipStackImpl) SIPTransaction.this.getSIPStack()).isReEntrantListener()) {
                    return this.lock.tryLock((long) SIPTransaction.this.sipStack.maxListenerResponseTime, TimeUnit.SECONDS);
                }
                return this.sem.tryAcquire((long) SIPTransaction.this.sipStack.maxListenerResponseTime, TimeUnit.SECONDS);
            } catch (Exception ex) {
                SIPTransaction.logger.logError("Unexpected exception trying acquiring sem", ex);
                InternalErrorHandler.handleException(ex);
                return false;
            }
        }

        public void release() {
            try {
                if (!((SipStackImpl) SIPTransaction.this.getSIPStack()).isReEntrantListener()) {
                    this.sem.release();
                } else if (this.lock.isHeldByCurrentThread()) {
                    this.lock.unlock();
                }
            } catch (Exception ex) {
                SIPTransaction.logger.logError("Unexpected exception releasing sem", ex);
            }
        }
    }

    public abstract void cleanUp();

    public abstract void fireRetransmissionTimer();

    public abstract void fireTimeoutTimer();

    public abstract Dialog getDialog();

    public abstract boolean isMessagePartOfTransaction(SIPMessage sIPMessage);

    public abstract void setDialog(SIPDialog sIPDialog, String str);

    public abstract void startTransactionTimer();

    public String getBranchId() {
        return this.branch;
    }

    protected SIPTransaction(SIPTransactionStack newParentStack, MessageChannel newEncapsulatedChannel) {
        this.sipStack = newParentStack;
        this.semaphore = new TransactionSemaphore();
        this.encapsulatedChannel = newEncapsulatedChannel;
        if (isReliable()) {
            MessageChannel messageChannel = this.encapsulatedChannel;
            messageChannel.useCount++;
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("use count for encapsulated channel" + this + Separators.SP + this.encapsulatedChannel.useCount);
            }
        }
        this.currentState = -1;
        disableRetransmissionTimer();
        disableTimeoutTimer();
        this.eventListeners = new CopyOnWriteArraySet();
        addEventListener(newParentStack);
        this.releaseReferences = this.sipStack.isAggressiveCleanup();
    }

    public void setOriginalRequest(SIPRequest newOriginalRequest) {
        String newTransactionId = newOriginalRequest.getTransactionId();
        if (!(this.originalRequest == null || this.originalRequest.getTransactionId().equals(newTransactionId))) {
            this.sipStack.removeTransactionHash(this);
        }
        this.originalRequest = newOriginalRequest;
        this.originalRequestCSeqNumber = newOriginalRequest.getCSeq().getSeqNumber();
        Via topmostVia = newOriginalRequest.getTopmostVia();
        this.originalRequestBranch = topmostVia.getBranch();
        this.originalRequestHasPort = topmostVia.hasPort();
        if (topmostVia.getPort() == -1) {
            if (topmostVia.getTransport().equalsIgnoreCase(ListeningPoint.TLS)) {
            }
        }
        this.method = newOriginalRequest.getMethod();
        this.transactionId = newTransactionId;
        this.originalRequest.setTransaction(this);
        String newBranch = topmostVia.getBranch();
        if (newBranch != null) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("Setting Branch id : " + newBranch);
            }
            setBranch(newBranch);
            return;
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Branch id is null - compute TID!" + newOriginalRequest.encode());
        }
        setBranch(newTransactionId);
    }

    public SIPRequest getOriginalRequest() {
        return this.originalRequest;
    }

    public Request getRequest() {
        if (isReleaseReferences() && this.originalRequest == null && this.originalRequestBytes != null) {
            if (logger.isLoggingEnabled(8)) {
                logger.logWarning("reparsing original request " + this.originalRequestBytes + " since it was eagerly cleaned up, but beware this is not efficient with the aggressive flag set !");
            }
            try {
                this.originalRequest = (SIPRequest) this.sipStack.getMessageParserFactory().createMessageParser(this.sipStack).parseSIPMessage(this.originalRequestBytes, true, false, null);
            } catch (ParseException e) {
                logger.logError("message " + this.originalRequestBytes + " could not be reparsed !");
            }
        }
        return this.originalRequest;
    }

    public final boolean isDialogCreatingTransaction() {
        if (this.dialogCreatingTransaction == null) {
            boolean z = isInviteTransaction() || getMethod().equals("SUBSCRIBE") || getMethod().equals(Request.REFER);
            this.dialogCreatingTransaction = Boolean.valueOf(z);
        }
        return this.dialogCreatingTransaction.booleanValue();
    }

    public final boolean isInviteTransaction() {
        if (this.inviteTransaction == null) {
            this.inviteTransaction = Boolean.valueOf(getMethod().equals("INVITE"));
        }
        return this.inviteTransaction.booleanValue();
    }

    public final boolean isCancelTransaction() {
        return getMethod().equals(Request.CANCEL);
    }

    public final boolean isByeTransaction() {
        return getMethod().equals("BYE");
    }

    public MessageChannel getMessageChannel() {
        return this.encapsulatedChannel;
    }

    public final void setBranch(String newBranch) {
        this.branch = newBranch;
    }

    public final String getBranch() {
        if (this.branch == null) {
            this.branch = this.originalRequestBranch;
        }
        return this.branch;
    }

    public final String getMethod() {
        return this.method;
    }

    public final long getCSeq() {
        return this.originalRequestCSeqNumber;
    }

    public void setState(int newState) {
        if (!(this.currentState != 3 || newState == 5 || newState == 4)) {
            newState = 3;
        }
        if (this.currentState == 4 && newState != 5) {
            newState = 4;
        }
        if (this.currentState != 5) {
            this.currentState = newState;
        } else {
            newState = this.currentState;
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Transaction:setState " + newState + Separators.SP + this + " branchID = " + getBranch() + " isClient = " + (this instanceof SIPClientTransaction));
            logger.logStackTrace();
        }
    }

    public int getInternalState() {
        return this.currentState;
    }

    public TransactionState getState() {
        if (this.currentState < 0) {
            return null;
        }
        return TransactionState.getObject(this.currentState);
    }

    /* access modifiers changed from: protected|final */
    public final void enableRetransmissionTimer() {
        enableRetransmissionTimer(1);
    }

    /* access modifiers changed from: protected|final */
    public final void enableRetransmissionTimer(int tickCount) {
        if (isInviteTransaction() && (this instanceof SIPClientTransaction)) {
            this.retransmissionTimerTicksLeft = tickCount;
        } else {
            this.retransmissionTimerTicksLeft = Math.min(tickCount, 8);
        }
        this.retransmissionTimerLastTickCount = this.retransmissionTimerTicksLeft;
    }

    /* access modifiers changed from: protected|final */
    public final void disableRetransmissionTimer() {
        this.retransmissionTimerTicksLeft = -1;
    }

    /* access modifiers changed from: protected|final */
    public final void enableTimeoutTimer(int tickCount) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("enableTimeoutTimer " + this + " tickCount " + tickCount + " currentTickCount = " + this.timeoutTimerTicksLeft);
        }
        this.timeoutTimerTicksLeft = tickCount;
    }

    /* access modifiers changed from: protected|final */
    public final void disableTimeoutTimer() {
        this.timeoutTimerTicksLeft = -1;
    }

    /* access modifiers changed from: final */
    public final void fireTimer() {
        int i;
        if (this.timeoutTimerTicksLeft != -1) {
            i = this.timeoutTimerTicksLeft - 1;
            this.timeoutTimerTicksLeft = i;
            if (i == 0) {
                fireTimeoutTimer();
            }
        }
        if (this.retransmissionTimerTicksLeft != -1) {
            i = this.retransmissionTimerTicksLeft - 1;
            this.retransmissionTimerTicksLeft = i;
            if (i == 0) {
                enableRetransmissionTimer(this.retransmissionTimerLastTickCount * 2);
                fireRetransmissionTimer();
            }
        }
    }

    public final boolean isTerminated() {
        return this.currentState == 5;
    }

    public String getHost() {
        return this.encapsulatedChannel.getHost();
    }

    public String getKey() {
        return this.encapsulatedChannel.getKey();
    }

    public int getPort() {
        return this.encapsulatedChannel.getPort();
    }

    public SIPTransactionStack getSIPStack() {
        return this.sipStack;
    }

    public String getPeerAddress() {
        return this.encapsulatedChannel.getPeerAddress();
    }

    public int getPeerPort() {
        return this.encapsulatedChannel.getPeerPort();
    }

    public int getPeerPacketSourcePort() {
        return this.encapsulatedChannel.getPeerPacketSourcePort();
    }

    public InetAddress getPeerPacketSourceAddress() {
        return this.encapsulatedChannel.getPeerPacketSourceAddress();
    }

    /* access modifiers changed from: protected */
    public InetAddress getPeerInetAddress() {
        return this.encapsulatedChannel.getPeerInetAddress();
    }

    /* access modifiers changed from: protected */
    public String getPeerProtocol() {
        return this.encapsulatedChannel.getPeerProtocol();
    }

    public String getTransport() {
        return this.encapsulatedChannel.getTransport();
    }

    public boolean isReliable() {
        return this.encapsulatedChannel.isReliable();
    }

    public Via getViaHeader() {
        Via channelViaHeader = super.getViaHeader();
        try {
            channelViaHeader.setBranch(this.branch);
        } catch (ParseException e) {
        }
        return channelViaHeader;
    }

    public void sendMessage(final SIPMessage messageToSend) throws IOException {
        try {
            final RawMessageChannel channel = this.encapsulatedChannel;
            for (MessageProcessor messageProcessor : this.sipStack.getMessageProcessors()) {
                if (messageProcessor.getIpAddress().getHostAddress().toString().equals(getPeerAddress()) && messageProcessor.getPort() == getPeerPort() && messageProcessor.getTransport().equalsIgnoreCase(getPeerProtocol())) {
                    if (channel instanceof TCPMessageChannel) {
                        getSIPStack().getSelfRoutingThreadpoolExecutor().execute(new Runnable() {
                            public void run() {
                                try {
                                    ((TCPMessageChannel) channel).processMessage((SIPMessage) messageToSend.clone(), SIPTransaction.this.getPeerInetAddress());
                                } catch (Exception ex) {
                                    if (SIPTransaction.logger.isLoggingEnabled(4)) {
                                        SIPTransaction.logger.logError("Error self routing message cause by: ", ex);
                                    }
                                }
                            }
                        });
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("Self routing message");
                        }
                        startTransactionTimer();
                    } else if (channel instanceof RawMessageChannel) {
                        try {
                            getSIPStack().getSelfRoutingThreadpoolExecutor().execute(new Runnable() {
                                public void run() {
                                    try {
                                        channel.processMessage((SIPMessage) messageToSend.clone());
                                    } catch (Exception ex) {
                                        if (SIPTransaction.logger.isLoggingEnabled(4)) {
                                            SIPTransaction.logger.logError("Error self routing message cause by: ", ex);
                                        }
                                    }
                                }
                            });
                        } catch (Exception e) {
                            logger.logError("Error passing message in self routing", e);
                        }
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("Self routing message");
                        }
                        startTransactionTimer();
                    }
                }
            }
            this.encapsulatedChannel.sendMessage(messageToSend, getPeerInetAddress(), getPeerPort());
        } catch (Exception e2) {
            logger.logError("Error passing message in self routing", e2);
        } catch (Throwable th) {
            startTransactionTimer();
        }
        startTransactionTimer();
    }

    /* access modifiers changed from: protected */
    public void sendMessage(byte[] messageBytes, InetAddress receiverAddress, int receiverPort, boolean retry) throws IOException {
        throw new IOException("Cannot send unparsed message through Transaction Channel!");
    }

    public void addEventListener(SIPTransactionEventListener newListener) {
        this.eventListeners.add(newListener);
    }

    public void removeEventListener(SIPTransactionEventListener oldListener) {
        this.eventListeners.remove(oldListener);
    }

    /* access modifiers changed from: protected */
    public void raiseErrorEvent(int errorEventID) {
        SIPTransactionErrorEvent newErrorEvent = new SIPTransactionErrorEvent(this, errorEventID);
        synchronized (this.eventListeners) {
            for (SIPTransactionEventListener nextListener : this.eventListeners) {
                nextListener.transactionErrorEvent(newErrorEvent);
            }
        }
        if (errorEventID != 3) {
            this.eventListeners.clear();
            setState(5);
            if ((this instanceof SIPServerTransaction) && isByeTransaction() && getDialog() != null) {
                ((SIPDialog) getDialog()).setState(3);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isServerTransaction() {
        return this instanceof SIPServerTransaction;
    }

    public int getRetransmitTimer() {
        return 500;
    }

    public String getViaHost() {
        return getViaHeader().getHost();
    }

    public SIPResponse getLastResponse() {
        return this.lastResponse;
    }

    public Response getResponse() {
        return this.lastResponse;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public int hashCode() {
        if (this.transactionId == null) {
            return -1;
        }
        return this.transactionId.hashCode();
    }

    public int getViaPort() {
        return getViaHeader().getPort();
    }

    public boolean doesCancelMatchTransaction(SIPRequest requestToTest) {
        boolean transactionMatches = false;
        SIPRequest origRequest = getOriginalRequest();
        if (origRequest == null || getMethod().equals(Request.CANCEL)) {
            return false;
        }
        Via topViaHeader = requestToTest.getTopmostVia();
        if (topViaHeader != null) {
            String messageBranch = topViaHeader.getBranch();
            if (!(messageBranch == null || messageBranch.toLowerCase().startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE))) {
                messageBranch = null;
            }
            if (messageBranch == null || getBranch() == null) {
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("testing against " + origRequest);
                }
                if (origRequest.getRequestURI().equals(requestToTest.getRequestURI()) && origRequest.getTo().equals(requestToTest.getTo()) && origRequest.getFrom().equals(requestToTest.getFrom()) && origRequest.getCallId().getCallId().equals(requestToTest.getCallId().getCallId()) && origRequest.getCSeq().getSeqNumber() == requestToTest.getCSeq().getSeqNumber() && topViaHeader.equals(origRequest.getTopmostVia())) {
                    transactionMatches = true;
                }
            } else if (getBranch().equalsIgnoreCase(messageBranch) && topViaHeader.getSentBy().equals(origRequest.getTopmostVia().getSentBy())) {
                transactionMatches = true;
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("returning  true");
                }
            }
        }
        if (transactionMatches) {
            setPassToListener();
        }
        return transactionMatches;
    }

    public void setRetransmitTimer(int retransmitTimer) {
        if (retransmitTimer <= 0) {
            throw new IllegalArgumentException("Retransmit timer must be positive!");
        } else if (this.transactionTimerStarted.get()) {
            throw new IllegalStateException("Transaction timer is already started");
        } else {
            this.BASE_TIMER_INTERVAL = retransmitTimer;
        }
    }

    public void close() {
        this.encapsulatedChannel.close();
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Closing " + this.encapsulatedChannel);
        }
    }

    public boolean isSecure() {
        return this.encapsulatedChannel.isSecure();
    }

    public MessageProcessor getMessageProcessor() {
        return this.encapsulatedChannel.getMessageProcessor();
    }

    public void setApplicationData(Object applicationData) {
        this.applicationData = applicationData;
    }

    public Object getApplicationData() {
        return this.applicationData;
    }

    public void setEncapsulatedChannel(MessageChannel messageChannel) {
        this.encapsulatedChannel = messageChannel;
        if (this instanceof SIPClientTransaction) {
            this.encapsulatedChannel.setEncapsulatedClientTransaction((SIPClientTransaction) this);
        }
    }

    public SipProviderImpl getSipProvider() {
        return getMessageProcessor().getListeningPoint().getProvider();
    }

    public void raiseIOExceptionEvent() {
        setState(5);
        getSipProvider().handleEvent(new IOExceptionEvent(this, getPeerAddress(), getPeerPort(), getTransport()), this);
    }

    public boolean acquireSem() {
        boolean retval;
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("acquireSem [[[[" + this);
            logger.logStackTrace();
        }
        if (this.sipStack.maxListenerResponseTime == -1) {
            retval = this.semaphore.acquire();
        } else {
            retval = this.semaphore.tryAcquire();
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("acquireSem() returning : " + retval);
        }
        return retval;
    }

    public void releaseSem() {
        try {
            this.toListener = false;
            semRelease();
        } catch (Exception ex) {
            logger.logError("Unexpected exception releasing sem", ex);
        }
    }

    /* access modifiers changed from: protected */
    public void semRelease() {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("semRelease ]]]]" + this);
            logger.logStackTrace();
        }
        this.semaphore.release();
    }

    public boolean passToListener() {
        return this.toListener;
    }

    public void setPassToListener() {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("setPassToListener()");
        }
        this.toListener = true;
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized boolean testAndSetTransactionTerminatedEvent() {
        boolean retval = true;
        synchronized (this) {
            if (this.terminatedEventDelivered) {
                retval = false;
            }
            this.terminatedEventDelivered = true;
        }
        return retval;
    }

    public String getCipherSuite() throws UnsupportedOperationException {
        if (!(getMessageChannel() instanceof TLSMessageChannel)) {
            throw new UnsupportedOperationException("Not a TLS channel");
        } else if (((TLSMessageChannel) getMessageChannel()).getHandshakeCompletedListener() == null) {
            return null;
        } else {
            if (((TLSMessageChannel) getMessageChannel()).getHandshakeCompletedListener().getHandshakeCompletedEvent() == null) {
                return null;
            }
            return ((TLSMessageChannel) getMessageChannel()).getHandshakeCompletedListener().getHandshakeCompletedEvent().getCipherSuite();
        }
    }

    public Certificate[] getLocalCertificates() throws UnsupportedOperationException {
        if (!(getMessageChannel() instanceof TLSMessageChannel)) {
            throw new UnsupportedOperationException("Not a TLS channel");
        } else if (((TLSMessageChannel) getMessageChannel()).getHandshakeCompletedListener() == null) {
            return null;
        } else {
            if (((TLSMessageChannel) getMessageChannel()).getHandshakeCompletedListener().getHandshakeCompletedEvent() == null) {
                return null;
            }
            return ((TLSMessageChannel) getMessageChannel()).getHandshakeCompletedListener().getHandshakeCompletedEvent().getLocalCertificates();
        }
    }

    public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
        if (!(getMessageChannel() instanceof TLSMessageChannel)) {
            throw new UnsupportedOperationException("Not a TLS channel");
        } else if (((TLSMessageChannel) getMessageChannel()).getHandshakeCompletedListener() == null) {
            return null;
        } else {
            if (((TLSMessageChannel) getMessageChannel()).getHandshakeCompletedListener().getHandshakeCompletedEvent() == null) {
                return null;
            }
            return ((TLSMessageChannel) getMessageChannel()).getHandshakeCompletedListener().getHandshakeCompletedEvent().getPeerCertificates();
        }
    }

    public List<String> extractCertIdentities() throws SSLPeerUnverifiedException {
        if (getMessageChannel() instanceof TLSMessageChannel) {
            List<String> certIdentities = new ArrayList();
            Certificate[] certs = getPeerCertificates();
            if (certs != null) {
                Certificate[] arr$ = certs;
                int len$ = arr$.length;
                int i$ = 0;
                while (true) {
                    int i$2 = i$;
                    if (i$2 >= len$) {
                        break;
                    }
                    X509Certificate x509cert = (X509Certificate) arr$[i$2];
                    Collection<List<?>> subjAltNames = null;
                    try {
                        subjAltNames = x509cert.getSubjectAlternativeNames();
                    } catch (CertificateParsingException ex) {
                        if (logger.isLoggingEnabled()) {
                            logger.logError("Error parsing TLS certificate", ex);
                        }
                    }
                    Integer dnsNameType = Integer.valueOf(2);
                    Integer uriNameType = Integer.valueOf(6);
                    if (subjAltNames != null) {
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("found subjAltNames: " + subjAltNames);
                        }
                        for (List<?> altName : subjAltNames) {
                            if (altName.get(0).equals(uriNameType)) {
                                try {
                                    String altHostName = new AddressFactoryImpl().createSipURI((String) altName.get(1)).getHost();
                                    if (logger.isLoggingEnabled(32)) {
                                        logger.logDebug("found uri " + altName.get(1) + ", hostName " + altHostName);
                                    }
                                    certIdentities.add(altHostName);
                                } catch (ParseException e) {
                                    if (logger.isLoggingEnabled()) {
                                        logger.logError("certificate contains invalid uri: " + altName.get(1));
                                    }
                                }
                            }
                        }
                        if (certIdentities.isEmpty()) {
                            for (List<?> altName2 : subjAltNames) {
                                if (altName2.get(0).equals(dnsNameType)) {
                                    if (logger.isLoggingEnabled(32)) {
                                        logger.logDebug("found dns " + altName2.get(1));
                                    }
                                    certIdentities.add(altName2.get(1).toString());
                                }
                            }
                        }
                    } else {
                        String dname = x509cert.getSubjectDN().getName();
                        String cname = "";
                        try {
                            Matcher matcher = Pattern.compile(".*CN\\s*=\\s*([\\w*\\.]+).*").matcher(dname);
                            if (matcher.matches()) {
                                cname = matcher.group(1);
                                if (logger.isLoggingEnabled(32)) {
                                    logger.logDebug("found CN: " + cname + " from DN: " + dname);
                                }
                                certIdentities.add(cname);
                            }
                        } catch (Exception ex2) {
                            if (logger.isLoggingEnabled()) {
                                logger.logError("exception while extracting CN", ex2);
                            }
                        }
                    }
                    i$ = i$2 + 1;
                }
            } else if (logger.isLoggingEnabled(32)) {
                logger.logDebug("No certificates available");
            }
            return certIdentities;
        }
        throw new UnsupportedOperationException("Not a TLS channel");
    }

    public boolean isReleaseReferences() {
        return this.releaseReferences;
    }

    public void setReleaseReferences(boolean releaseReferences) {
        this.releaseReferences = releaseReferences;
    }

    public int getTimerD() {
        return this.TIMER_D;
    }

    public int getTimerT2() {
        return this.T2;
    }

    public int getTimerT4() {
        return this.T4;
    }

    public void setTimerD(int interval) {
        if (interval < 32000) {
            throw new IllegalArgumentException("To be RFC 3261 compliant, the value of Timer D should be at least 32s");
        }
        this.TIMER_D = interval / this.BASE_TIMER_INTERVAL;
    }

    public void setTimerT2(int interval) {
        this.T2 = interval / this.BASE_TIMER_INTERVAL;
    }

    public void setTimerT4(int interval) {
        this.T4 = interval / this.BASE_TIMER_INTERVAL;
        this.TIMER_I = this.T4;
        this.TIMER_K = this.T4;
    }

    public void setForkId(String forkId) {
        this.forkId = forkId;
    }

    public String getForkId() {
        return this.forkId;
    }
}
