package org.jitsi.gov.nist.javax.sip.stack;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.Host;
import org.jitsi.gov.nist.core.HostPort;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.core.ServerLogger;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.core.ThreadAuditor;
import org.jitsi.gov.nist.core.ThreadAuditor.ThreadHandle;
import org.jitsi.gov.nist.core.net.AddressResolver;
import org.jitsi.gov.nist.core.net.DefaultNetworkLayer;
import org.jitsi.gov.nist.core.net.NetworkLayer;
import org.jitsi.gov.nist.javax.sip.DefaultAddressResolver;
import org.jitsi.gov.nist.javax.sip.ListeningPointImpl;
import org.jitsi.gov.nist.javax.sip.LogRecordFactory;
import org.jitsi.gov.nist.javax.sip.SIPConstants;
import org.jitsi.gov.nist.javax.sip.SipListenerExt;
import org.jitsi.gov.nist.javax.sip.SipProviderImpl;
import org.jitsi.gov.nist.javax.sip.SipStackImpl;
import org.jitsi.gov.nist.javax.sip.Utils;
import org.jitsi.gov.nist.javax.sip.header.Event;
import org.jitsi.gov.nist.javax.sip.header.extensions.JoinHeader;
import org.jitsi.gov.nist.javax.sip.header.extensions.ReplacesHeader;
import org.jitsi.gov.nist.javax.sip.message.SIPRequest;
import org.jitsi.gov.nist.javax.sip.message.SIPResponse;
import org.jitsi.gov.nist.javax.sip.parser.MessageParserFactory;
import org.jitsi.gov.nist.javax.sip.stack.timers.SipTimer;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.DialogState;
import org.jitsi.javax.sip.DialogTerminatedEvent;
import org.jitsi.javax.sip.ServerTransaction;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.SipListener;
import org.jitsi.javax.sip.TransactionState;
import org.jitsi.javax.sip.TransactionTerminatedEvent;
import org.jitsi.javax.sip.address.Hop;
import org.jitsi.javax.sip.address.Router;
import org.jitsi.javax.sip.header.CallIdHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;

public abstract class SIPTransactionStack implements SIPTransactionEventListener, SIPDialogEventListener {
    public static final int BASE_TIMER_INTERVAL = 500;
    public static final int CONNECTION_LINGER_TIME = 8;
    protected static final Set<String> dialogCreatingMethods = new HashSet();
    private static StackLogger logger = CommonLogger.getLogger(SIPTransactionStack.class);
    protected static Executor selfRoutingThreadpoolExecutor;
    private AtomicInteger activeClientTransactionCount;
    protected AddressResolver addressResolver;
    protected boolean aggressiveCleanup;
    protected boolean cacheClientConnections;
    protected boolean cacheServerConnections;
    protected boolean cancelClientTransactionChecked;
    protected boolean checkBranchId;
    protected ClientAuthType clientAuth;
    protected ConcurrentHashMap<String, SIPClientTransaction> clientTransactionTable;
    protected int clientTransactionTableHiwaterMark;
    protected int clientTransactionTableLowaterMark;
    protected DefaultRouter defaultRouter;
    protected boolean deliverRetransmittedAckToListener;
    private boolean deliverTerminatedEventForAck;
    private boolean deliverUnsolicitedNotify;
    protected ConcurrentHashMap<String, SIPDialog> dialogTable;
    protected int dialogTimeoutFactor;
    protected ConcurrentHashMap<String, SIPDialog> earlyDialogTable;
    protected int earlyDialogTimeout;
    /* access modifiers changed from: private */
    public ConcurrentHashMap<String, SIPClientTransaction> forkedClientTransactionTable;
    protected HashSet<String> forkedEvents;
    protected boolean generateTimeStampHeader;
    protected IOHandler ioHandler;
    protected boolean isAutomaticDialogErrorHandlingEnabled;
    protected boolean isAutomaticDialogSupportEnabled;
    protected boolean isBackToBackUserAgent;
    protected boolean isDialogTerminatedEventDeliveredForNullDialog;
    protected LogRecordFactory logRecordFactory;
    protected boolean logStackTraceOnMessageSend;
    protected int maxConnections;
    protected int maxContentLength;
    protected int maxForkTime;
    protected int maxListenerResponseTime;
    protected int maxMessageSize;
    private ConcurrentHashMap<String, SIPServerTransaction> mergeTable;
    public MessageParserFactory messageParserFactory;
    public MessageProcessorFactory messageProcessorFactory;
    private Collection<MessageProcessor> messageProcessors;
    protected long minKeepAliveInterval;
    protected boolean needsLogging;
    protected NetworkLayer networkLayer;
    private boolean non2XXAckPassedToListener;
    protected String outboundProxy;
    private ConcurrentHashMap<String, SIPServerTransaction> pendingTransactions;
    protected int readTimeout;
    protected int receiveUdpBufferSize;
    private ExecutorService reinviteExecutor;
    protected boolean remoteTagReassignmentAllowed;
    protected ConcurrentHashMap<String, SIPServerTransaction> retransmissionAlertTransactions;
    protected boolean rfc2543Supported;
    protected Router router;
    protected String routerPath;
    protected int sendUdpBufferSize;
    protected ConcurrentHashMap<String, SIPDialog> serverDialogMergeTestTable;
    protected ServerLogger serverLogger;
    protected ConcurrentHashMap<String, SIPServerTransaction> serverTransactionTable;
    protected int serverTransactionTableHighwaterMark;
    protected int serverTransactionTableLowaterMark;
    public SIPEventInterceptor sipEventInterceptor;
    protected StackMessageFactory sipMessageFactory;
    public SIPMessageValve sipMessageValve;
    protected String stackAddress;
    protected int stackCongenstionControlTimeout;
    protected InetAddress stackInetAddress;
    protected String stackName;
    private int tcpPostParsingThreadPoolSize;
    private ConcurrentHashMap<String, SIPServerTransaction> terminatedServerTransactionsPendingAck;
    protected ThreadAuditor threadAuditor;
    protected int threadPoolSize;
    private SipTimer timer;
    protected boolean toExit;
    boolean udpFlag;
    protected boolean unlimitedClientTransactionTableSize;
    protected boolean unlimitedServerTransactionTableSize;
    protected boolean useRouterForAll;

    protected class PingTimer extends SIPStackTimerTask {
        ThreadHandle threadHandle;

        public PingTimer(ThreadHandle a_oThreadHandle) {
            this.threadHandle = a_oThreadHandle;
        }

        public void runTask() {
            if (SIPTransactionStack.this.getTimer() != null) {
                if (this.threadHandle == null) {
                    this.threadHandle = SIPTransactionStack.this.getThreadAuditor().addCurrentThread();
                }
                this.threadHandle.ping();
                SIPTransactionStack.this.getTimer().schedule(new PingTimer(this.threadHandle), this.threadHandle.getPingIntervalInMillisecs());
            }
        }
    }

    class RemoveForkedTransactionTimerTask extends SIPStackTimerTask {
        private final String forkId;

        public RemoveForkedTransactionTimerTask(String forkId) {
            this.forkId = forkId;
        }

        public void runTask() {
            SIPTransactionStack.this.forkedClientTransactionTable.remove(this.forkId);
        }
    }

    private static class SameThreadExecutor implements Executor {
        private SameThreadExecutor() {
        }

        public void execute(Runnable command) {
            command.run();
        }
    }

    public abstract SipListener getSipListener();

    static {
        dialogCreatingMethods.add(Request.REFER);
        dialogCreatingMethods.add("INVITE");
        dialogCreatingMethods.add("SUBSCRIBE");
    }

    public Executor getSelfRoutingThreadpoolExecutor() {
        if (selfRoutingThreadpoolExecutor == null) {
            if (this.threadPoolSize <= 0) {
                selfRoutingThreadpoolExecutor = new SameThreadExecutor();
            } else {
                selfRoutingThreadpoolExecutor = Executors.newFixedThreadPool(this.threadPoolSize);
            }
        }
        return selfRoutingThreadpoolExecutor;
    }

    protected SIPTransactionStack() {
        this.earlyDialogTimeout = Response.RINGING;
        this.unlimitedServerTransactionTableSize = true;
        this.unlimitedClientTransactionTableSize = true;
        this.serverTransactionTableHighwaterMark = 5000;
        this.serverTransactionTableLowaterMark = 4000;
        this.clientTransactionTableHiwaterMark = 1000;
        this.clientTransactionTableLowaterMark = 800;
        this.activeClientTransactionCount = new AtomicInteger(0);
        this.deliverRetransmittedAckToListener = false;
        this.rfc2543Supported = true;
        this.threadAuditor = new ThreadAuditor();
        this.cancelClientTransactionChecked = true;
        this.remoteTagReassignmentAllowed = true;
        this.logStackTraceOnMessageSend = true;
        this.stackCongenstionControlTimeout = 0;
        this.isBackToBackUserAgent = false;
        this.isAutomaticDialogErrorHandlingEnabled = true;
        this.isDialogTerminatedEventDeliveredForNullDialog = false;
        this.maxForkTime = 0;
        this.deliverUnsolicitedNotify = false;
        this.deliverTerminatedEventForAck = false;
        this.clientAuth = ClientAuthType.Default;
        this.tcpPostParsingThreadPoolSize = 0;
        this.minKeepAliveInterval = -1;
        this.dialogTimeoutFactor = 64;
        this.aggressiveCleanup = false;
        this.reinviteExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
            private int threadCount = 0;

            public Thread newThread(Runnable pRunnable) {
                Object[] objArr = new Object[2];
                objArr[0] = "ReInviteSender";
                int i = this.threadCount;
                this.threadCount = i + 1;
                objArr[1] = Integer.valueOf(i);
                return new Thread(pRunnable, String.format("%s-%d", objArr));
            }
        });
        this.toExit = false;
        this.forkedEvents = new HashSet();
        this.threadPoolSize = -1;
        this.cacheServerConnections = true;
        this.cacheClientConnections = true;
        this.maxConnections = -1;
        this.messageProcessors = new CopyOnWriteArrayList();
        this.ioHandler = new IOHandler(this);
        this.readTimeout = -1;
        this.maxListenerResponseTime = -1;
        this.addressResolver = new DefaultAddressResolver();
        this.dialogTable = new ConcurrentHashMap();
        this.earlyDialogTable = new ConcurrentHashMap();
        this.serverDialogMergeTestTable = new ConcurrentHashMap();
        this.clientTransactionTable = new ConcurrentHashMap();
        this.serverTransactionTable = new ConcurrentHashMap();
        this.terminatedServerTransactionsPendingAck = new ConcurrentHashMap();
        this.mergeTable = new ConcurrentHashMap();
        this.retransmissionAlertTransactions = new ConcurrentHashMap();
        this.pendingTransactions = new ConcurrentHashMap();
        this.forkedClientTransactionTable = new ConcurrentHashMap();
    }

    /* access modifiers changed from: protected */
    public void reInit() {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Re-initializing !");
        }
        this.messageProcessors = new CopyOnWriteArrayList();
        this.ioHandler = new IOHandler(this);
        this.pendingTransactions = new ConcurrentHashMap();
        this.clientTransactionTable = new ConcurrentHashMap();
        this.serverTransactionTable = new ConcurrentHashMap();
        this.retransmissionAlertTransactions = new ConcurrentHashMap();
        this.mergeTable = new ConcurrentHashMap();
        this.dialogTable = new ConcurrentHashMap();
        this.earlyDialogTable = new ConcurrentHashMap();
        this.serverDialogMergeTestTable = new ConcurrentHashMap();
        this.terminatedServerTransactionsPendingAck = new ConcurrentHashMap();
        this.forkedClientTransactionTable = new ConcurrentHashMap();
        this.activeClientTransactionCount = new AtomicInteger(0);
    }

    public SocketAddress getLocalAddressForTcpDst(InetAddress dst, int dstPort, InetAddress localAddress, int localPort) throws IOException {
        return this.ioHandler.getLocalAddressForTcpDst(dst, dstPort, localAddress, localPort);
    }

    public SocketAddress getLocalAddressForTlsDst(InetAddress dst, int dstPort, InetAddress localAddress) throws IOException {
        TLSMessageProcessor tlsProcessor = null;
        for (MessageProcessor processor : getMessageProcessors()) {
            if (processor instanceof TLSMessageProcessor) {
                tlsProcessor = (TLSMessageProcessor) processor;
                break;
            }
        }
        if (tlsProcessor == null) {
            return null;
        }
        return this.ioHandler.getLocalAddressForTlsDst(dst, dstPort, localAddress, (TLSMessageChannel) tlsProcessor.createMessageChannel(dst, dstPort));
    }

    public void disableLogging() {
        logger.disableLogging();
    }

    public void enableLogging() {
        logger.enableLogging();
    }

    public void printDialogTable() {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("dialog table  = " + this.dialogTable);
        }
    }

    public SIPServerTransaction getRetransmissionAlertTransaction(String dialogId) {
        return (SIPServerTransaction) this.retransmissionAlertTransactions.get(dialogId);
    }

    public static boolean isDialogCreated(String method) {
        return dialogCreatingMethods.contains(method);
    }

    public void addExtensionMethod(String extensionMethod) {
        if (!extensionMethod.equals("NOTIFY")) {
            dialogCreatingMethods.add(Utils.toUpperCase(extensionMethod.trim()));
        } else if (logger.isLoggingEnabled(32)) {
            logger.logDebug("NOTIFY Supported Natively");
        }
    }

    public SIPDialog putDialog(SIPDialog dialog) {
        String dialogId = dialog.getDialogId();
        if (this.dialogTable.containsKey(dialogId)) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("putDialog: dialog already exists" + dialogId + " in table = " + this.dialogTable.get(dialogId));
            }
            return (SIPDialog) this.dialogTable.get(dialogId);
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("putDialog dialogId=" + dialogId + " dialog = " + dialog);
        }
        dialog.setStack(this);
        if (logger.isLoggingEnabled(32)) {
            logger.logStackTrace();
        }
        this.dialogTable.put(dialogId, dialog);
        if (dialog.getMergeId() != null) {
            this.serverDialogMergeTestTable.put(dialog.getMergeId(), dialog);
        }
        return dialog;
    }

    public SIPDialog createDialog(SIPTransaction transaction) {
        if (!(transaction instanceof SIPClientTransaction)) {
            return new SIPDialog(transaction);
        }
        String dialogId = ((SIPRequest) transaction.getRequest()).getDialogId(false);
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("createDialog dialogId=" + dialogId);
        }
        SIPDialog retval;
        if (this.earlyDialogTable.get(dialogId) != null) {
            SIPDialog dialog = (SIPDialog) this.earlyDialogTable.get(dialogId);
            if (dialog.getState() == null || dialog.getState() == DialogState.EARLY) {
                retval = dialog;
                if (!logger.isLoggingEnabled(32)) {
                    return retval;
                }
                logger.logDebug("createDialog early Dialog found : earlyDialogId=" + dialogId + " earlyDialog= " + dialog);
                return retval;
            }
            retval = new SIPDialog(transaction);
            this.earlyDialogTable.put(dialogId, retval);
            return retval;
        }
        retval = new SIPDialog(transaction);
        this.earlyDialogTable.put(dialogId, retval);
        if (!logger.isLoggingEnabled(32)) {
            return retval;
        }
        logger.logDebug("createDialog early Dialog not found : earlyDialogId=" + dialogId + " created one " + retval);
        return retval;
    }

    public SIPDialog createDialog(SIPClientTransaction transaction, SIPResponse sipResponse) {
        SIPDialog retval;
        String originalDialogId = ((SIPRequest) transaction.getRequest()).getDialogId(false);
        String earlyDialogId = sipResponse.getDialogId(false);
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("createDialog originalDialogId=" + originalDialogId);
            logger.logDebug("createDialog earlyDialogId=" + earlyDialogId);
            logger.logDebug("createDialog default Dialog=" + transaction.getDefaultDialog());
            if (transaction.getDefaultDialog() != null) {
                logger.logDebug("createDialog default Dialog Id=" + transaction.getDefaultDialog().getDialogId());
            }
        }
        SIPDialog earlyDialog = (SIPDialog) this.earlyDialogTable.get(originalDialogId);
        if (earlyDialog == null || transaction == null || !(transaction.getDefaultDialog() == null || transaction.getDefaultDialog().getDialogId().equals(originalDialogId))) {
            retval = new SIPDialog(transaction, sipResponse);
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("createDialog early Dialog not found : earlyDialogId=" + earlyDialogId + " created one " + retval);
            }
        } else {
            retval = earlyDialog;
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("createDialog early Dialog found : earlyDialogId=" + originalDialogId + " earlyDialog= " + retval);
            }
            if (sipResponse.isFinalResponse()) {
                this.earlyDialogTable.remove(originalDialogId);
            }
        }
        return retval;
    }

    public SIPDialog createDialog(SipProviderImpl sipProvider, SIPResponse sipResponse) {
        return new SIPDialog(sipProvider, sipResponse);
    }

    public void removeDialog(SIPDialog dialog) {
        String id = dialog.getDialogId();
        String earlyId = dialog.getEarlyDialogId();
        if (earlyId != null) {
            this.earlyDialogTable.remove(earlyId);
            this.dialogTable.remove(earlyId);
        }
        String mergeId = dialog.getMergeId();
        if (mergeId != null) {
            this.serverDialogMergeTestTable.remove(mergeId);
        }
        if (id != null) {
            if (this.dialogTable.get(id) == dialog) {
                this.dialogTable.remove(id);
            }
            if (!dialog.testAndSetIsDialogTerminatedEventDelivered()) {
                dialog.getSipProvider().handleEvent(new DialogTerminatedEvent(dialog.getSipProvider(), dialog), null);
            }
        } else if (this.isDialogTerminatedEventDeliveredForNullDialog && !dialog.testAndSetIsDialogTerminatedEventDelivered()) {
            dialog.getSipProvider().handleEvent(new DialogTerminatedEvent(dialog.getSipProvider(), dialog), null);
        }
    }

    public SIPDialog getEarlyDialog(String dialogId) {
        SIPDialog sipDialog = (SIPDialog) this.earlyDialogTable.get(dialogId);
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("getEarlyDialog(" + dialogId + ") : returning " + sipDialog);
        }
        return sipDialog;
    }

    public SIPDialog getDialog(String dialogId) {
        SIPDialog sipDialog = (SIPDialog) this.dialogTable.get(dialogId);
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("getDialog(" + dialogId + ") : returning " + sipDialog);
        }
        return sipDialog;
    }

    public void removeDialog(String dialogId) {
        if (logger.isLoggingEnabled()) {
            logger.logWarning("Silently removing dialog from table");
        }
        this.dialogTable.remove(dialogId);
    }

    public SIPClientTransaction findSubscribeTransaction(SIPRequest notifyMessage, ListeningPointImpl listeningPoint) {
        try {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("ct table size = " + this.clientTransactionTable.size());
            }
            String thisToTag = notifyMessage.getTo().getTag();
            if (thisToTag == null) {
                return null;
            }
            Event eventHdr = (Event) notifyMessage.getHeader("Event");
            if (eventHdr == null) {
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("event Header is null -- returning null");
                }
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("findSubscribeTransaction : returning " + null);
                }
                return null;
            }
            for (SIPClientTransaction ct : this.clientTransactionTable.values()) {
                if (ct.getMethod().equals("SUBSCRIBE")) {
                    String fromTag = ct.getOriginalRequestFromTag();
                    Event hisEvent = ct.getOriginalRequestEvent();
                    if (hisEvent == null) {
                        continue;
                    } else {
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("ct.fromTag = " + fromTag);
                            logger.logDebug("thisToTag = " + thisToTag);
                            logger.logDebug("hisEvent = " + hisEvent);
                            logger.logDebug("eventHdr " + eventHdr);
                        }
                        if (fromTag.equalsIgnoreCase(thisToTag) && hisEvent != null && eventHdr.match(hisEvent) && notifyMessage.getCallId().getCallId().equalsIgnoreCase(ct.getOriginalRequestCallId())) {
                            if (!isDeliverUnsolicitedNotify()) {
                                ct.acquireSem();
                            }
                            SIPClientTransaction retval = ct;
                            if (!logger.isLoggingEnabled(32)) {
                                return ct;
                            }
                            logger.logDebug("findSubscribeTransaction : returning " + retval);
                            return ct;
                        }
                    }
                }
            }
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("findSubscribeTransaction : returning " + null);
            }
            return null;
        } finally {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("findSubscribeTransaction : returning " + null);
            }
        }
    }

    public void addTransactionPendingAck(SIPServerTransaction serverTransaction) {
        String branchId = ((SIPRequest) serverTransaction.getRequest()).getTopmostVia().getBranch();
        if (branchId != null) {
            this.terminatedServerTransactionsPendingAck.put(branchId, serverTransaction);
        }
    }

    public SIPServerTransaction findTransactionPendingAck(SIPRequest ackMessage) {
        return (SIPServerTransaction) this.terminatedServerTransactionsPendingAck.get(ackMessage.getTopmostVia().getBranch());
    }

    public boolean removeTransactionPendingAck(SIPServerTransaction serverTransaction) {
        String branchId = serverTransaction.getBranchId();
        if (branchId == null || !this.terminatedServerTransactionsPendingAck.containsKey(branchId)) {
            return false;
        }
        this.terminatedServerTransactionsPendingAck.remove(branchId);
        return true;
    }

    public boolean isTransactionPendingAck(SIPServerTransaction serverTransaction) {
        return this.terminatedServerTransactionsPendingAck.contains(((SIPRequest) serverTransaction.getRequest()).getTopmostVia().getBranch());
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:11:0x0075=Splitter:B:11:0x0075, B:33:0x0119=Splitter:B:33:0x0119} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0183  */
    public org.jitsi.gov.nist.javax.sip.stack.SIPTransaction findTransaction(org.jitsi.gov.nist.javax.sip.message.SIPMessage r14, boolean r15) {
        /*
        r13 = this;
        r12 = 32;
        r5 = 0;
        if (r15 == 0) goto L_0x00b4;
    L_0x0005:
        r8 = r14.getTopmostVia();	 Catch:{ all -> 0x0159 }
        r9 = r8.getBranch();	 Catch:{ all -> 0x0159 }
        if (r9 == 0) goto L_0x0075;
    L_0x000f:
        r4 = r14.getTransactionId();	 Catch:{ all -> 0x0159 }
        r9 = r13.serverTransactionTable;	 Catch:{ all -> 0x0159 }
        r9 = r9.get(r4);	 Catch:{ all -> 0x0159 }
        r0 = r9;
        r0 = (org.jitsi.gov.nist.javax.sip.stack.SIPTransaction) r0;	 Catch:{ all -> 0x0159 }
        r5 = r0;
        r9 = logger;	 Catch:{ all -> 0x0159 }
        r10 = 32;
        r9 = r9.isLoggingEnabled(r10);	 Catch:{ all -> 0x0159 }
        if (r9 == 0) goto L_0x004b;
    L_0x0027:
        r9 = logger;	 Catch:{ all -> 0x0159 }
        r10 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0159 }
        r10.<init>();	 Catch:{ all -> 0x0159 }
        r11 = "serverTx: looking for key ";
        r10 = r10.append(r11);	 Catch:{ all -> 0x0159 }
        r10 = r10.append(r4);	 Catch:{ all -> 0x0159 }
        r11 = " existing=";
        r10 = r10.append(r11);	 Catch:{ all -> 0x0159 }
        r11 = r13.serverTransactionTable;	 Catch:{ all -> 0x0159 }
        r10 = r10.append(r11);	 Catch:{ all -> 0x0159 }
        r10 = r10.toString();	 Catch:{ all -> 0x0159 }
        r9.logDebug(r10);	 Catch:{ all -> 0x0159 }
    L_0x004b:
        r9 = "z9hg4bk";
        r9 = r4.startsWith(r9);	 Catch:{ all -> 0x0159 }
        if (r9 == 0) goto L_0x0075;
    L_0x0053:
        r9 = logger;
        r9 = r9.isLoggingEnabled(r12);
        if (r9 == 0) goto L_0x0073;
    L_0x005b:
        r9 = logger;
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "findTransaction: returning  : ";
        r10 = r10.append(r11);
        r10 = r10.append(r5);
        r10 = r10.toString();
        r9.logDebug(r10);
    L_0x0073:
        r6 = r5;
    L_0x0074:
        return r6;
    L_0x0075:
        r9 = r13.serverTransactionTable;	 Catch:{ all -> 0x0159 }
        r9 = r9.values();	 Catch:{ all -> 0x0159 }
        r3 = r9.iterator();	 Catch:{ all -> 0x0159 }
    L_0x007f:
        r9 = r3.hasNext();	 Catch:{ all -> 0x0159 }
        if (r9 == 0) goto L_0x017b;
    L_0x0085:
        r7 = r3.next();	 Catch:{ all -> 0x0159 }
        r7 = (org.jitsi.gov.nist.javax.sip.stack.SIPServerTransaction) r7;	 Catch:{ all -> 0x0159 }
        r9 = r7.isMessagePartOfTransaction(r14);	 Catch:{ all -> 0x0159 }
        if (r9 == 0) goto L_0x007f;
    L_0x0091:
        r5 = r7;
        r9 = logger;
        r9 = r9.isLoggingEnabled(r12);
        if (r9 == 0) goto L_0x00b2;
    L_0x009a:
        r9 = logger;
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "findTransaction: returning  : ";
        r10 = r10.append(r11);
        r10 = r10.append(r5);
        r10 = r10.toString();
        r9.logDebug(r10);
    L_0x00b2:
        r6 = r5;
        goto L_0x0074;
    L_0x00b4:
        r8 = r14.getTopmostVia();	 Catch:{ all -> 0x0159 }
        r9 = r8.getBranch();	 Catch:{ all -> 0x0159 }
        if (r9 == 0) goto L_0x0119;
    L_0x00be:
        r4 = r14.getTransactionId();	 Catch:{ all -> 0x0159 }
        r9 = logger;	 Catch:{ all -> 0x0159 }
        r10 = 32;
        r9 = r9.isLoggingEnabled(r10);	 Catch:{ all -> 0x0159 }
        if (r9 == 0) goto L_0x00e4;
    L_0x00cc:
        r9 = logger;	 Catch:{ all -> 0x0159 }
        r10 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0159 }
        r10.<init>();	 Catch:{ all -> 0x0159 }
        r11 = "clientTx: looking for key ";
        r10 = r10.append(r11);	 Catch:{ all -> 0x0159 }
        r10 = r10.append(r4);	 Catch:{ all -> 0x0159 }
        r10 = r10.toString();	 Catch:{ all -> 0x0159 }
        r9.logDebug(r10);	 Catch:{ all -> 0x0159 }
    L_0x00e4:
        r9 = r13.clientTransactionTable;	 Catch:{ all -> 0x0159 }
        r9 = r9.get(r4);	 Catch:{ all -> 0x0159 }
        r0 = r9;
        r0 = (org.jitsi.gov.nist.javax.sip.stack.SIPTransaction) r0;	 Catch:{ all -> 0x0159 }
        r5 = r0;
        r9 = "z9hg4bk";
        r9 = r4.startsWith(r9);	 Catch:{ all -> 0x0159 }
        if (r9 == 0) goto L_0x0119;
    L_0x00f6:
        r9 = logger;
        r9 = r9.isLoggingEnabled(r12);
        if (r9 == 0) goto L_0x0116;
    L_0x00fe:
        r9 = logger;
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "findTransaction: returning  : ";
        r10 = r10.append(r11);
        r10 = r10.append(r5);
        r10 = r10.toString();
        r9.logDebug(r10);
    L_0x0116:
        r6 = r5;
        goto L_0x0074;
    L_0x0119:
        r9 = r13.clientTransactionTable;	 Catch:{ all -> 0x0159 }
        r9 = r9.values();	 Catch:{ all -> 0x0159 }
        r2 = r9.iterator();	 Catch:{ all -> 0x0159 }
    L_0x0123:
        r9 = r2.hasNext();	 Catch:{ all -> 0x0159 }
        if (r9 == 0) goto L_0x017b;
    L_0x0129:
        r1 = r2.next();	 Catch:{ all -> 0x0159 }
        r1 = (org.jitsi.gov.nist.javax.sip.stack.SIPClientTransaction) r1;	 Catch:{ all -> 0x0159 }
        r9 = r1.isMessagePartOfTransaction(r14);	 Catch:{ all -> 0x0159 }
        if (r9 == 0) goto L_0x0123;
    L_0x0135:
        r5 = r1;
        r9 = logger;
        r9 = r9.isLoggingEnabled(r12);
        if (r9 == 0) goto L_0x0156;
    L_0x013e:
        r9 = logger;
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "findTransaction: returning  : ";
        r10 = r10.append(r11);
        r10 = r10.append(r5);
        r10 = r10.toString();
        r9.logDebug(r10);
    L_0x0156:
        r6 = r5;
        goto L_0x0074;
    L_0x0159:
        r9 = move-exception;
        r10 = logger;
        r10 = r10.isLoggingEnabled(r12);
        if (r10 == 0) goto L_0x017a;
    L_0x0162:
        r10 = logger;
        r11 = new java.lang.StringBuilder;
        r11.<init>();
        r12 = "findTransaction: returning  : ";
        r11 = r11.append(r12);
        r11 = r11.append(r5);
        r11 = r11.toString();
        r10.logDebug(r11);
    L_0x017a:
        throw r9;
    L_0x017b:
        r9 = logger;
        r9 = r9.isLoggingEnabled(r12);
        if (r9 == 0) goto L_0x019b;
    L_0x0183:
        r9 = logger;
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "findTransaction: returning  : ";
        r10 = r10.append(r11);
        r10 = r10.append(r5);
        r10 = r10.toString();
        r9.logDebug(r10);
    L_0x019b:
        r6 = r5;
        goto L_0x0074;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.javax.sip.stack.SIPTransactionStack.findTransaction(org.jitsi.gov.nist.javax.sip.message.SIPMessage, boolean):org.jitsi.gov.nist.javax.sip.stack.SIPTransaction");
    }

    public SIPTransaction findTransaction(String transactionId, boolean isServer) {
        if (isServer) {
            return (SIPTransaction) this.serverTransactionTable.get(transactionId);
        }
        return (SIPTransaction) this.clientTransactionTable.get(transactionId);
    }

    public SIPTransaction findCancelTransaction(SIPRequest cancelRequest, boolean isServer) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("findCancelTransaction request= \n" + cancelRequest + "\nfindCancelRequest isServer=" + isServer);
        }
        if (isServer) {
            for (SIPTransaction transaction : this.serverTransactionTable.values()) {
                SIPServerTransaction sipServerTransaction = (SIPServerTransaction) transaction;
                if (sipServerTransaction.doesCancelMatchTransaction(cancelRequest)) {
                    return sipServerTransaction;
                }
            }
        }
        for (SIPTransaction transaction2 : this.clientTransactionTable.values()) {
            SIPTransaction sipClientTransaction = (SIPClientTransaction) transaction2;
            if (sipClientTransaction.doesCancelMatchTransaction(cancelRequest)) {
                return sipClientTransaction;
            }
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Could not find transaction for cancel request");
        }
        return null;
    }

    protected SIPTransactionStack(StackMessageFactory messageFactory) {
        this();
        this.sipMessageFactory = messageFactory;
    }

    public SIPServerTransaction findPendingTransaction(String transactionId) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("looking for pending tx for :" + transactionId);
        }
        return (SIPServerTransaction) this.pendingTransactions.get(transactionId);
    }

    public boolean findMergedTransaction(SIPRequest sipRequest) {
        if (!sipRequest.getMethod().equals("INVITE")) {
            return false;
        }
        String mergeId = sipRequest.getMergeId();
        if (mergeId == null) {
            return false;
        }
        SIPServerTransaction mergedTransaction = (SIPServerTransaction) this.mergeTable.get(mergeId);
        if (mergedTransaction != null && !mergedTransaction.isMessagePartOfTransaction(sipRequest)) {
            return true;
        }
        SIPDialog serverDialog = (SIPDialog) this.serverDialogMergeTestTable.get(mergeId);
        if (serverDialog != null && serverDialog.firstTransactionIsServerTransaction && serverDialog.getState() == DialogState.CONFIRMED) {
            return true;
        }
        return false;
    }

    public void removePendingTransaction(SIPServerTransaction tr) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("removePendingTx: " + tr.getTransactionId());
        }
        this.pendingTransactions.remove(tr.getTransactionId());
    }

    public void removeFromMergeTable(SIPServerTransaction tr) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Removing tx from merge table ");
        }
        String key = ((SIPRequest) tr.getRequest()).getMergeId();
        if (key != null) {
            this.mergeTable.remove(key);
        }
    }

    public void putInMergeTable(SIPServerTransaction sipTransaction, SIPRequest sipRequest) {
        String mergeKey = sipRequest.getMergeId();
        if (mergeKey != null) {
            this.mergeTable.put(mergeKey, sipTransaction);
        }
    }

    public void mapTransaction(SIPServerTransaction transaction) {
        if (!transaction.isMapped) {
            addTransactionHash(transaction);
            transaction.isMapped = true;
        }
    }

    public ServerRequestInterface newSIPServerRequest(SIPRequest requestReceived, MessageChannel requestMessageChannel) {
        String key = requestReceived.getTransactionId();
        requestReceived.setMessageChannel(requestMessageChannel);
        if (this.sipMessageValve == null || this.sipMessageValve.processRequest(requestReceived, requestMessageChannel)) {
            ServerRequestInterface currentTransaction = (SIPServerTransaction) findTransaction(key, true);
            if (currentTransaction == null || !currentTransaction.isMessagePartOfTransaction(requestReceived)) {
                currentTransaction = null;
                if (!key.toLowerCase().startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE)) {
                    Iterator<SIPServerTransaction> transactionIterator = this.serverTransactionTable.values().iterator();
                    while (transactionIterator.hasNext() && currentTransaction == null) {
                        SIPServerTransaction nextTransaction = (SIPServerTransaction) transactionIterator.next();
                        if (nextTransaction.isMessagePartOfTransaction(requestReceived)) {
                            SIPServerTransaction currentTransaction2 = nextTransaction;
                        }
                    }
                }
                if (currentTransaction == null) {
                    currentTransaction = findPendingTransaction(key);
                    if (currentTransaction != null) {
                        requestReceived.setTransaction(currentTransaction);
                        if (currentTransaction == null || !currentTransaction.acquireSem()) {
                            return null;
                        }
                        return currentTransaction;
                    }
                    currentTransaction = createServerTransaction(requestMessageChannel);
                    if (currentTransaction != null) {
                        currentTransaction.setOriginalRequest(requestReceived);
                        requestReceived.setTransaction(currentTransaction);
                    }
                }
            }
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("newSIPServerRequest( " + requestReceived.getMethod() + Separators.COLON + requestReceived.getTopmostVia().getBranch() + "):" + currentTransaction);
            }
            if (currentTransaction != null) {
                currentTransaction.setRequestInterface(this.sipMessageFactory.newSIPServerRequest(requestReceived, currentTransaction));
            }
            if (currentTransaction != null && currentTransaction.acquireSem()) {
                return currentTransaction;
            }
            if (currentTransaction == null) {
                return null;
            }
            try {
                if (currentTransaction.isMessagePartOfTransaction(requestReceived) && currentTransaction.getMethod().equals(requestReceived.getMethod())) {
                    SIPResponse trying = requestReceived.createResponse(100);
                    trying.removeContent();
                    currentTransaction.getMessageChannel().sendMessage(trying);
                }
            } catch (Exception e) {
                if (logger.isLoggingEnabled()) {
                    logger.logError("Exception occured sending TRYING");
                }
            }
            return null;
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Request dropped by the SIP message valve. Request = " + requestReceived);
        }
        return null;
    }

    public ServerResponseInterface newSIPServerResponse(SIPResponse responseReceived, MessageChannel responseMessageChannel) {
        if (this.sipMessageValve == null || this.sipMessageValve.processResponse(responseReceived, responseMessageChannel)) {
            String key = responseReceived.getTransactionId();
            ServerResponseInterface currentTransaction = (SIPClientTransaction) findTransaction(key, false);
            if (currentTransaction == null || !(currentTransaction.isMessagePartOfTransaction(responseReceived) || key.startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE))) {
                Iterator<SIPClientTransaction> transactionIterator = this.clientTransactionTable.values().iterator();
                currentTransaction = null;
                while (transactionIterator.hasNext() && currentTransaction == null) {
                    SIPClientTransaction nextTransaction = (SIPClientTransaction) transactionIterator.next();
                    if (nextTransaction.isMessagePartOfTransaction(responseReceived)) {
                        currentTransaction = nextTransaction;
                    }
                }
                if (currentTransaction == null) {
                    if (logger.isLoggingEnabled(16)) {
                        responseMessageChannel.logResponse(responseReceived, System.currentTimeMillis(), "before processing");
                    }
                    return this.sipMessageFactory.newSIPServerResponse(responseReceived, responseMessageChannel);
                }
            }
            boolean acquired = currentTransaction.acquireSem();
            if (logger.isLoggingEnabled(16)) {
                currentTransaction.logResponse(responseReceived, System.currentTimeMillis(), "before processing");
            }
            if (acquired) {
                ServerResponseInterface sri = this.sipMessageFactory.newSIPServerResponse(responseReceived, currentTransaction);
                if (sri != null) {
                    currentTransaction.setResponseInterface(sri);
                } else {
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("returning null - serverResponseInterface is null!");
                    }
                    currentTransaction.releaseSem();
                    return null;
                }
            } else if (logger.isLoggingEnabled(32)) {
                logger.logDebug("Could not aquire semaphore !!");
            }
            if (acquired) {
                return currentTransaction;
            }
            return null;
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Response dropped by the SIP message valve. Response = " + responseReceived);
        }
        return null;
    }

    public MessageChannel createMessageChannel(SIPRequest request, MessageProcessor mp, Hop nextHop) throws IOException {
        Host targetHost = new Host();
        targetHost.setHostname(nextHop.getHost());
        HostPort targetHostPort = new HostPort();
        targetHostPort.setHost(targetHost);
        targetHostPort.setPort(nextHop.getPort());
        MessageChannel mc = mp.createMessageChannel(targetHostPort);
        if (mc == null) {
            return null;
        }
        MessageChannel returnChannel = createClientTransaction(request, mc);
        ((SIPClientTransaction) returnChannel).setViaPort(nextHop.getPort());
        ((SIPClientTransaction) returnChannel).setViaHost(nextHop.getHost());
        addTransactionHash(returnChannel);
        return returnChannel;
    }

    public SIPClientTransaction createClientTransaction(SIPRequest sipRequest, MessageChannel encapsulatedMessageChannel) {
        SIPClientTransaction ct = new SIPClientTransaction(this, encapsulatedMessageChannel);
        ct.setOriginalRequest(sipRequest);
        return ct;
    }

    public SIPServerTransaction createServerTransaction(MessageChannel encapsulatedMessageChannel) {
        if (this.unlimitedServerTransactionTableSize) {
            return new SIPServerTransaction(this, encapsulatedMessageChannel);
        }
        if (Math.random() > 1.0d - ((double) (((float) (this.serverTransactionTable.size() - this.serverTransactionTableLowaterMark)) / ((float) (this.serverTransactionTableHighwaterMark - this.serverTransactionTableLowaterMark))))) {
            return null;
        }
        return new SIPServerTransaction(this, encapsulatedMessageChannel);
    }

    public int getClientTransactionTableSize() {
        return this.clientTransactionTable.size();
    }

    public int getServerTransactionTableSize() {
        return this.serverTransactionTable.size();
    }

    public void addTransaction(SIPClientTransaction clientTransaction) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("added transaction " + clientTransaction);
        }
        addTransactionHash(clientTransaction);
    }

    public void removeTransaction(SIPTransaction sipTransaction) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Removing Transaction = " + sipTransaction.getTransactionId() + " transaction = " + sipTransaction);
        }
        Object sipProvider;
        if (sipTransaction instanceof SIPServerTransaction) {
            if (logger.isLoggingEnabled(32)) {
                logger.logStackTrace();
            }
            Object removed = this.serverTransactionTable.remove(sipTransaction.getTransactionId());
            String method = sipTransaction.getMethod();
            removePendingTransaction((SIPServerTransaction) sipTransaction);
            removeTransactionPendingAck((SIPServerTransaction) sipTransaction);
            if (method.equalsIgnoreCase("INVITE")) {
                removeFromMergeTable((SIPServerTransaction) sipTransaction);
            }
            sipProvider = sipTransaction.getSipProvider();
            if (removed != null && sipTransaction.testAndSetTransactionTerminatedEvent()) {
                sipProvider.handleEvent(new TransactionTerminatedEvent(sipProvider, (ServerTransaction) sipTransaction), sipTransaction);
                return;
            }
            return;
        }
        String key = sipTransaction.getTransactionId();
        SIPClientTransaction removed2 = this.clientTransactionTable.remove(key);
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("REMOVED client tx " + removed2 + " KEY = " + key);
        }
        if (removed2 != null) {
            SIPClientTransaction clientTx = removed2;
            String forkId = clientTx.getForkId();
            if (!(forkId == null || !clientTx.isInviteTransaction() || this.maxForkTime == 0)) {
                this.timer.schedule(new RemoveForkedTransactionTimerTask(forkId), (long) (this.maxForkTime * 1000));
                clientTx.stopExpiresTimer();
            }
        }
        if (removed2 != null && sipTransaction.testAndSetTransactionTerminatedEvent()) {
            sipProvider = sipTransaction.getSipProvider();
            sipProvider.handleEvent(new TransactionTerminatedEvent(sipProvider, (ClientTransaction) sipTransaction), sipTransaction);
        }
    }

    public void addTransaction(SIPServerTransaction serverTransaction) throws IOException {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("added transaction " + serverTransaction);
        }
        serverTransaction.map();
        addTransactionHash(serverTransaction);
    }

    private void addTransactionHash(SIPTransaction sipTransaction) {
        SIPRequest sipRequest = sipTransaction.getOriginalRequest();
        String key;
        if (sipTransaction instanceof SIPClientTransaction) {
            if (this.unlimitedClientTransactionTableSize) {
                this.activeClientTransactionCount.incrementAndGet();
            } else if (this.activeClientTransactionCount.get() > this.clientTransactionTableHiwaterMark) {
                try {
                    synchronized (this.clientTransactionTable) {
                        this.clientTransactionTable.wait();
                        this.activeClientTransactionCount.incrementAndGet();
                    }
                } catch (Exception ex) {
                    if (logger.isLoggingEnabled()) {
                        logger.logError("Exception occured while waiting for room", ex);
                    }
                }
            }
            key = sipRequest.getTransactionId();
            this.clientTransactionTable.put(key, (SIPClientTransaction) sipTransaction);
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug(" putTransactionHash :  key = " + key);
                return;
            }
            return;
        }
        key = sipRequest.getTransactionId();
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug(" putTransactionHash :  key = " + key);
        }
        this.serverTransactionTable.put(key, (SIPServerTransaction) sipTransaction);
    }

    /* access modifiers changed from: protected */
    public void decrementActiveClientTransactionCount() {
        if (this.activeClientTransactionCount.decrementAndGet() <= this.clientTransactionTableLowaterMark && !this.unlimitedClientTransactionTableSize) {
            synchronized (this.clientTransactionTable) {
                this.clientTransactionTable.notify();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void removeTransactionHash(SIPTransaction sipTransaction) {
        if (sipTransaction.getOriginalRequest() != null) {
            String key;
            if (sipTransaction instanceof SIPClientTransaction) {
                key = sipTransaction.getTransactionId();
                if (logger.isLoggingEnabled(32)) {
                    logger.logStackTrace();
                    logger.logDebug("removing client Tx : " + key);
                }
                this.clientTransactionTable.remove(key);
            } else if (sipTransaction instanceof SIPServerTransaction) {
                key = sipTransaction.getTransactionId();
                this.serverTransactionTable.remove(key);
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("removing server Tx : " + key);
                }
            }
        }
    }

    public synchronized void transactionErrorEvent(SIPTransactionErrorEvent transactionErrorEvent) {
        SIPTransaction transaction = (SIPTransaction) transactionErrorEvent.getSource();
        if (transactionErrorEvent.getErrorID() == 2) {
            transaction.setState(5);
            if (transaction instanceof SIPServerTransaction) {
                ((SIPServerTransaction) transaction).collectionTime = 0;
            }
            transaction.disableTimeoutTimer();
            transaction.disableRetransmissionTimer();
        }
    }

    public synchronized void dialogErrorEvent(SIPDialogErrorEvent dialogErrorEvent) {
        SIPDialog sipDialog = (SIPDialog) dialogErrorEvent.getSource();
        SipListener sipListener = ((SipStackImpl) this).getSipListener();
        if (!(sipDialog == null || (sipListener instanceof SipListenerExt))) {
            sipDialog.delete();
        }
    }

    public void stopStack() {
        this.toExit = true;
        if (this.timer != null) {
            this.timer.stop();
        }
        this.pendingTransactions.clear();
        synchronized (this) {
            notifyAll();
        }
        synchronized (this.clientTransactionTable) {
            this.clientTransactionTable.notifyAll();
        }
        MessageProcessor[] processorList = getMessageProcessors();
        for (MessageProcessor removeMessageProcessor : processorList) {
            removeMessageProcessor(removeMessageProcessor);
        }
        this.ioHandler.closeAll();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        this.clientTransactionTable.clear();
        this.serverTransactionTable.clear();
        this.dialogTable.clear();
        this.serverLogger.closeLogFile();
    }

    public void putPendingTransaction(SIPServerTransaction tr) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("putPendingTransaction: " + tr);
        }
        this.pendingTransactions.put(tr.getTransactionId(), tr);
    }

    public NetworkLayer getNetworkLayer() {
        if (this.networkLayer == null) {
            return DefaultNetworkLayer.SINGLETON;
        }
        return this.networkLayer;
    }

    @Deprecated
    public boolean isLoggingEnabled() {
        return logger == null ? false : logger.isLoggingEnabled();
    }

    @Deprecated
    public boolean isLoggingEnabled(int level) {
        return logger == null ? false : logger.isLoggingEnabled(level);
    }

    @Deprecated
    public StackLogger getStackLogger() {
        return logger;
    }

    public ServerLogger getServerLogger() {
        return this.serverLogger;
    }

    public int getMaxMessageSize() {
        return this.maxMessageSize;
    }

    public void setSingleThreaded() {
        this.threadPoolSize = 1;
    }

    public int getTcpPostParsingThreadPoolSize() {
        return this.tcpPostParsingThreadPoolSize;
    }

    public void setTcpPostParsingThreadPoolSize(int tcpPostParsingThreadPoolSize) {
        this.tcpPostParsingThreadPoolSize = tcpPostParsingThreadPoolSize;
    }

    public void setThreadPoolSize(int size) {
        this.threadPoolSize = size;
    }

    public void setMaxConnections(int nconnections) {
        this.maxConnections = nconnections;
    }

    public Hop getNextHop(SIPRequest sipRequest) throws SipException {
        if (this.useRouterForAll) {
            if (this.router != null) {
                return this.router.getNextHop(sipRequest);
            }
            return null;
        } else if (sipRequest.getRequestURI().isSipURI() || sipRequest.getRouteHeaders() != null) {
            return this.defaultRouter.getNextHop(sipRequest);
        } else {
            if (this.router != null) {
                return this.router.getNextHop(sipRequest);
            }
            return null;
        }
    }

    public void setStackName(String stackName) {
        this.stackName = stackName;
    }

    /* access modifiers changed from: protected */
    public void setHostAddress(String stackAddress) throws UnknownHostException {
        if (stackAddress.indexOf(58) == stackAddress.lastIndexOf(58) || stackAddress.trim().charAt(0) == '[') {
            this.stackAddress = stackAddress;
        } else {
            this.stackAddress = '[' + stackAddress + ']';
        }
        this.stackInetAddress = InetAddress.getByName(stackAddress);
    }

    public String getHostAddress() {
        return this.stackAddress;
    }

    /* access modifiers changed from: protected */
    public void setRouter(Router router) {
        this.router = router;
    }

    public Router getRouter(SIPRequest request) {
        if (request.getRequestLine() == null) {
            return this.defaultRouter;
        }
        if (this.useRouterForAll) {
            return this.router;
        }
        if (request.getRequestURI().getScheme().equals("sip") || request.getRequestURI().getScheme().equals("sips")) {
            return this.defaultRouter;
        }
        if (this.router != null) {
            return this.router;
        }
        return this.defaultRouter;
    }

    public Router getRouter() {
        return this.router;
    }

    public boolean isAlive() {
        return !this.toExit;
    }

    /* access modifiers changed from: protected */
    public void addMessageProcessor(MessageProcessor newMessageProcessor) throws IOException {
        this.messageProcessors.add(newMessageProcessor);
    }

    /* access modifiers changed from: protected */
    public void removeMessageProcessor(MessageProcessor oldMessageProcessor) {
        if (this.messageProcessors.remove(oldMessageProcessor)) {
            oldMessageProcessor.stop();
        }
    }

    /* access modifiers changed from: protected */
    public MessageProcessor[] getMessageProcessors() {
        return (MessageProcessor[]) this.messageProcessors.toArray(new MessageProcessor[0]);
    }

    /* access modifiers changed from: protected */
    public MessageProcessor createMessageProcessor(InetAddress ipAddress, int port, String transport) throws IOException {
        MessageProcessor newMessageProcessor = this.messageProcessorFactory.createMessageProcessor(this, ipAddress, port, transport);
        addMessageProcessor(newMessageProcessor);
        return newMessageProcessor;
    }

    /* access modifiers changed from: protected */
    public void setMessageFactory(StackMessageFactory messageFactory) {
        this.sipMessageFactory = messageFactory;
    }

    public MessageChannel createRawMessageChannel(String sourceIpAddress, int sourcePort, Hop nextHop) throws UnknownHostException {
        MessageProcessor nextProcessor;
        Host targetHost = new Host();
        targetHost.setHostname(nextHop.getHost());
        HostPort targetHostPort = new HostPort();
        targetHostPort.setHost(targetHost);
        targetHostPort.setPort(nextHop.getPort());
        MessageChannel newChannel = null;
        Iterator processorIterator = this.messageProcessors.iterator();
        while (processorIterator.hasNext() && newChannel == null) {
            nextProcessor = (MessageProcessor) processorIterator.next();
            if (nextHop.getTransport().equalsIgnoreCase(nextProcessor.getTransport()) && sourceIpAddress.equals(nextProcessor.getIpAddress().getHostAddress()) && sourcePort == nextProcessor.getPort()) {
                try {
                    newChannel = nextProcessor.createMessageChannel(targetHostPort);
                } catch (UnknownHostException ex) {
                    if (logger.isLoggingEnabled()) {
                        logger.logException(ex);
                    }
                    throw ex;
                } catch (IOException e) {
                    if (logger.isLoggingEnabled()) {
                        logger.logException(e);
                    }
                }
            }
        }
        if (newChannel == null && logger.isLoggingEnabled(32)) {
            logger.logDebug("newChanne is null, messageProcessors.size = " + this.messageProcessors.size());
            processorIterator = this.messageProcessors.iterator();
            while (processorIterator.hasNext() && newChannel == null) {
                nextProcessor = (MessageProcessor) processorIterator.next();
                logger.logDebug("nextProcessor:" + nextProcessor + "| transport = " + nextProcessor.getTransport() + " ipAddress=" + nextProcessor.getIpAddress() + " port=" + nextProcessor.getPort());
            }
            logger.logDebug("More info on newChannel=null");
            logger.logDebug("nextHop=" + nextHop + " sourceIp=" + sourceIpAddress + " sourcePort=" + sourcePort + " targetHostPort=" + targetHostPort);
        }
        return newChannel;
    }

    public boolean isEventForked(String ename) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("isEventForked: " + ename + " returning " + this.forkedEvents.contains(ename));
        }
        return this.forkedEvents.contains(ename);
    }

    public AddressResolver getAddressResolver() {
        return this.addressResolver;
    }

    public void setAddressResolver(AddressResolver addressResolver) {
        this.addressResolver = addressResolver;
    }

    public void setLogRecordFactory(LogRecordFactory logRecordFactory) {
        this.logRecordFactory = logRecordFactory;
    }

    public ThreadAuditor getThreadAuditor() {
        return this.threadAuditor;
    }

    public String auditStack(Set activeCallIDs, long leakedDialogTimer, long leakedTransactionTimer) {
        String leakedDialogs = auditDialogs(activeCallIDs, leakedDialogTimer);
        String leakedServerTransactions = auditTransactions(this.serverTransactionTable, leakedTransactionTimer);
        String leakedClientTransactions = auditTransactions(this.clientTransactionTable, leakedTransactionTimer);
        if (leakedDialogs == null && leakedServerTransactions == null && leakedClientTransactions == null) {
            return null;
        }
        StringBuilder append = new StringBuilder().append("SIP Stack Audit:\n");
        if (leakedDialogs == null) {
            leakedDialogs = "";
        }
        append = append.append(leakedDialogs);
        if (leakedServerTransactions == null) {
            leakedServerTransactions = "";
        }
        append = append.append(leakedServerTransactions);
        if (leakedClientTransactions == null) {
            leakedClientTransactions = "";
        }
        return append.append(leakedClientTransactions).toString();
    }

    private String auditDialogs(Set activeCallIDs, long leakedDialogTimer) {
        LinkedList dialogs;
        String auditReport = "  Leaked dialogs:\n";
        int leakedDialogs = 0;
        long currentTime = System.currentTimeMillis();
        synchronized (this.dialogTable) {
            dialogs = new LinkedList(this.dialogTable.values());
        }
        Iterator it = dialogs.iterator();
        while (it.hasNext()) {
            SIPDialog itDialog = (SIPDialog) it.next();
            CallIdHeader callIdHeader = itDialog != null ? itDialog.getCallId() : null;
            String callID = callIdHeader != null ? callIdHeader.getCallId() : null;
            if (!(itDialog == null || callID == null || activeCallIDs.contains(callID))) {
                if (itDialog.auditTag == 0) {
                    itDialog.auditTag = currentTime;
                } else if (currentTime - itDialog.auditTag >= leakedDialogTimer) {
                    leakedDialogs++;
                    DialogState dialogState = itDialog.getState();
                    String dialogReport = "dialog id: " + itDialog.getDialogId() + ", dialog state: " + (dialogState != null ? dialogState.toString() : "null");
                    auditReport = auditReport + "    " + dialogReport + Separators.RETURN;
                    itDialog.setState(3);
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("auditDialogs: leaked " + dialogReport);
                    }
                }
            }
        }
        if (leakedDialogs > 0) {
            return auditReport + "    Total: " + Integer.toString(leakedDialogs) + " leaked dialogs detected and removed.\n";
        }
        return null;
    }

    private String auditTransactions(ConcurrentHashMap transactionsMap, long a_nLeakedTransactionTimer) {
        String auditReport = "  Leaked transactions:\n";
        int leakedTransactions = 0;
        long currentTime = System.currentTimeMillis();
        Iterator it = new LinkedList(transactionsMap.values()).iterator();
        while (it.hasNext()) {
            SIPTransaction sipTransaction = (SIPTransaction) it.next();
            if (sipTransaction != null) {
                if (sipTransaction.auditTag == 0) {
                    sipTransaction.auditTag = currentTime;
                } else if (currentTime - sipTransaction.auditTag >= a_nLeakedTransactionTimer) {
                    leakedTransactions++;
                    TransactionState transactionState = sipTransaction.getState();
                    SIPRequest origRequest = sipTransaction.getOriginalRequest();
                    String origRequestMethod = origRequest != null ? origRequest.getMethod() : null;
                    StringBuilder append = new StringBuilder().append(sipTransaction.getClass().getName()).append(", state: ").append(transactionState != null ? transactionState.toString() : "null").append(", OR: ");
                    if (origRequestMethod == null) {
                        origRequestMethod = "null";
                    }
                    String transactionReport = append.append(origRequestMethod).toString();
                    auditReport = auditReport + "    " + transactionReport + Separators.RETURN;
                    removeTransaction(sipTransaction);
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("auditTransactions: leaked " + transactionReport);
                    }
                }
            }
        }
        if (leakedTransactions > 0) {
            return auditReport + "    Total: " + Integer.toString(leakedTransactions) + " leaked transactions detected and removed.\n";
        }
        return null;
    }

    public void setNon2XXAckPassedToListener(boolean passToListener) {
        this.non2XXAckPassedToListener = passToListener;
    }

    public boolean isNon2XXAckPassedToListener() {
        return this.non2XXAckPassedToListener;
    }

    public int getActiveClientTransactionCount() {
        return this.activeClientTransactionCount.get();
    }

    public boolean isRfc2543Supported() {
        return this.rfc2543Supported;
    }

    public boolean isCancelClientTransactionChecked() {
        return this.cancelClientTransactionChecked;
    }

    public boolean isRemoteTagReassignmentAllowed() {
        return this.remoteTagReassignmentAllowed;
    }

    public Collection<Dialog> getDialogs() {
        HashSet<Dialog> dialogs = new HashSet();
        dialogs.addAll(this.dialogTable.values());
        dialogs.addAll(this.earlyDialogTable.values());
        return dialogs;
    }

    public Collection<Dialog> getDialogs(DialogState state) {
        HashSet<Dialog> matchingDialogs = new HashSet();
        if (DialogState.EARLY.equals(state)) {
            matchingDialogs.addAll(this.earlyDialogTable.values());
        } else {
            for (SIPDialog dialog : this.dialogTable.values()) {
                if (dialog.getState() != null && dialog.getState().equals(state)) {
                    matchingDialogs.add(dialog);
                }
            }
        }
        return matchingDialogs;
    }

    public Dialog getReplacesDialog(ReplacesHeader replacesHeader) {
        String cid = replacesHeader.getCallId();
        String fromTag = replacesHeader.getFromTag();
        String toTag = replacesHeader.getToTag();
        for (SIPDialog dialog : this.dialogTable.values()) {
            if (dialog.getCallId().getCallId().equals(cid) && fromTag.equalsIgnoreCase(dialog.lastResponseFromTag) && toTag.equalsIgnoreCase(dialog.lastResponseToTag)) {
                return dialog;
            }
        }
        StringBuilder dialogId = new StringBuilder(cid);
        if (toTag != null) {
            dialogId.append(Separators.COLON);
            dialogId.append(toTag);
        }
        if (fromTag != null) {
            dialogId.append(Separators.COLON);
            dialogId.append(fromTag);
        }
        String did = dialogId.toString().toLowerCase();
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Looking for dialog " + did);
        }
        Dialog replacesDialog = (Dialog) this.dialogTable.get(did);
        if (replacesDialog == null) {
            for (SIPClientTransaction ctx : this.clientTransactionTable.values()) {
                if (ctx.getDialog(did) != null) {
                    replacesDialog = ctx.getDialog(did);
                    break;
                }
            }
        }
        return replacesDialog;
    }

    public Dialog getJoinDialog(JoinHeader joinHeader) {
        String cid = joinHeader.getCallId();
        String fromTag = joinHeader.getFromTag();
        String toTag = joinHeader.getToTag();
        StringBuilder retval = new StringBuilder(cid);
        if (toTag != null) {
            retval.append(Separators.COLON);
            retval.append(toTag);
        }
        if (fromTag != null) {
            retval.append(Separators.COLON);
            retval.append(fromTag);
        }
        return (Dialog) this.dialogTable.get(retval.toString().toLowerCase());
    }

    public void setTimer(SipTimer timer) {
        this.timer = timer;
    }

    public SipTimer getTimer() throws IllegalStateException {
        return this.timer;
    }

    public int getReceiveUdpBufferSize() {
        return this.receiveUdpBufferSize;
    }

    public void setReceiveUdpBufferSize(int receiveUdpBufferSize) {
        this.receiveUdpBufferSize = receiveUdpBufferSize;
    }

    public int getSendUdpBufferSize() {
        return this.sendUdpBufferSize;
    }

    public void setSendUdpBufferSize(int sendUdpBufferSize) {
        this.sendUdpBufferSize = sendUdpBufferSize;
    }

    public boolean checkBranchId() {
        return this.checkBranchId;
    }

    public void setLogStackTraceOnMessageSend(boolean logStackTraceOnMessageSend) {
        this.logStackTraceOnMessageSend = logStackTraceOnMessageSend;
    }

    public boolean isLogStackTraceOnMessageSend() {
        return this.logStackTraceOnMessageSend;
    }

    public void setDeliverDialogTerminatedEventForNullDialog() {
        this.isDialogTerminatedEventDeliveredForNullDialog = true;
    }

    public void addForkedClientTransaction(SIPClientTransaction clientTransaction) {
        String forkId = ((SIPRequest) clientTransaction.getRequest()).getForkId();
        clientTransaction.setForkId(forkId);
        this.forkedClientTransactionTable.put(forkId, clientTransaction);
    }

    public SIPClientTransaction getForkedTransaction(String transactionId) {
        return (SIPClientTransaction) this.forkedClientTransactionTable.get(transactionId);
    }

    public void setDeliverUnsolicitedNotify(boolean deliverUnsolicitedNotify) {
        this.deliverUnsolicitedNotify = deliverUnsolicitedNotify;
    }

    public boolean isDeliverUnsolicitedNotify() {
        return this.deliverUnsolicitedNotify;
    }

    public void setDeliverTerminatedEventForAck(boolean deliverTerminatedEventForAck) {
        this.deliverTerminatedEventForAck = deliverTerminatedEventForAck;
    }

    public boolean isDeliverTerminatedEventForAck() {
        return this.deliverTerminatedEventForAck;
    }

    public long getMinKeepAliveInterval() {
        return this.minKeepAliveInterval;
    }

    public void setMaxForkTime(int maxForkTime) {
        this.maxForkTime = maxForkTime;
    }

    public int getMaxForkTime() {
        return this.maxForkTime;
    }

    public boolean isDeliverRetransmittedAckToListener() {
        return this.deliverRetransmittedAckToListener;
    }

    public int getAckTimeoutFactor() {
        if (getSipListener() == null || !(getSipListener() instanceof SipListenerExt)) {
            return 64;
        }
        return this.dialogTimeoutFactor;
    }

    public ExecutorService getReinviteExecutor() {
        return this.reinviteExecutor;
    }

    public void setMessageParserFactory(MessageParserFactory messageParserFactory) {
        this.messageParserFactory = messageParserFactory;
    }

    public MessageParserFactory getMessageParserFactory() {
        return this.messageParserFactory;
    }

    public void setMessageProcessorFactory(MessageProcessorFactory messageProcessorFactory) {
        this.messageProcessorFactory = messageProcessorFactory;
    }

    public MessageProcessorFactory getMessageProcessorFactory() {
        return this.messageProcessorFactory;
    }

    public void setAggressiveCleanup(boolean aggressiveCleanup) {
        this.aggressiveCleanup = aggressiveCleanup;
    }

    public boolean isAggressiveCleanup() {
        return this.aggressiveCleanup;
    }

    public int getEarlyDialogTimeout() {
        return this.earlyDialogTimeout;
    }

    public void setClientAuth(ClientAuthType clientAuth) {
        this.clientAuth = clientAuth;
    }

    public ClientAuthType getClientAuth() {
        return this.clientAuth;
    }
}
