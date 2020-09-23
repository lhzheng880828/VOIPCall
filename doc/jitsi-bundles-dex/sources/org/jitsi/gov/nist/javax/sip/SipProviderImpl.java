package org.jitsi.gov.nist.javax.sip;

import java.io.IOException;
import java.text.ParseException;
import java.util.EventObject;
import java.util.Iterator;
import java.util.TooManyListenersException;
import java.util.concurrent.ConcurrentHashMap;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.InternalErrorHandler;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.javax.sip.DialogTimeoutEvent.Reason;
import org.jitsi.gov.nist.javax.sip.address.RouterExt;
import org.jitsi.gov.nist.javax.sip.header.CallID;
import org.jitsi.gov.nist.javax.sip.header.Via;
import org.jitsi.gov.nist.javax.sip.message.SIPMessage;
import org.jitsi.gov.nist.javax.sip.message.SIPRequest;
import org.jitsi.gov.nist.javax.sip.message.SIPResponse;
import org.jitsi.gov.nist.javax.sip.stack.HopImpl;
import org.jitsi.gov.nist.javax.sip.stack.MessageChannel;
import org.jitsi.gov.nist.javax.sip.stack.SIPClientTransaction;
import org.jitsi.gov.nist.javax.sip.stack.SIPDialog;
import org.jitsi.gov.nist.javax.sip.stack.SIPDialogErrorEvent;
import org.jitsi.gov.nist.javax.sip.stack.SIPDialogEventListener;
import org.jitsi.gov.nist.javax.sip.stack.SIPServerTransaction;
import org.jitsi.gov.nist.javax.sip.stack.SIPTransaction;
import org.jitsi.gov.nist.javax.sip.stack.SIPTransactionErrorEvent;
import org.jitsi.gov.nist.javax.sip.stack.SIPTransactionEventListener;
import org.jitsi.gov.nist.javax.sip.stack.SIPTransactionStack;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.DialogState;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.ListeningPoint;
import org.jitsi.javax.sip.ObjectInUseException;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.ResponseEvent;
import org.jitsi.javax.sip.ServerTransaction;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.SipListener;
import org.jitsi.javax.sip.SipProvider;
import org.jitsi.javax.sip.SipStack;
import org.jitsi.javax.sip.Timeout;
import org.jitsi.javax.sip.TimeoutEvent;
import org.jitsi.javax.sip.Transaction;
import org.jitsi.javax.sip.TransactionAlreadyExistsException;
import org.jitsi.javax.sip.TransactionUnavailableException;
import org.jitsi.javax.sip.address.Hop;
import org.jitsi.javax.sip.header.CallIdHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;

public class SipProviderImpl implements SipProvider, SipProviderExt, SIPTransactionEventListener, SIPDialogEventListener {
    private static StackLogger logger = CommonLogger.getLogger(SipProviderImpl.class);
    private String IN6_ADDR_ANY = "::0";
    private String IN_ADDR_ANY = "0.0.0.0";
    private String address;
    private boolean automaticDialogSupportEnabled;
    private boolean dialogErrorsAutomaticallyHandled = true;
    private EventScanner eventScanner;
    private ConcurrentHashMap listeningPoints;
    private int port;
    private SipListener sipListener;
    protected SipStackImpl sipStack;

    private SipProviderImpl() {
    }

    /* access modifiers changed from: protected */
    public void stop() {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Exiting provider");
        }
        for (ListeningPointImpl listeningPoint : this.listeningPoints.values()) {
            listeningPoint.removeSipProvider();
        }
        this.eventScanner.stop();
    }

    public ListeningPoint getListeningPoint(String transport) {
        if (transport != null) {
            return (ListeningPoint) this.listeningPoints.get(transport.toUpperCase());
        }
        throw new NullPointerException("Null transport param");
    }

    public void handleEvent(EventObject sipEvent, SIPTransaction transaction) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("handleEvent " + sipEvent + "currentTransaction = " + transaction + "this.sipListener = " + getSipListener() + "sipEvent.source = " + sipEvent.getSource());
            Dialog dialog;
            if (sipEvent instanceof RequestEvent) {
                dialog = ((RequestEvent) sipEvent).getDialog();
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("Dialog = " + dialog);
                }
            } else if (sipEvent instanceof ResponseEvent) {
                dialog = ((ResponseEvent) sipEvent).getDialog();
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("Dialog = " + dialog);
                }
            }
            logger.logStackTrace();
        }
        EventWrapper eventWrapper = new EventWrapper(sipEvent, transaction);
        if (this.sipStack.isReEntrantListener()) {
            this.eventScanner.deliverEvent(eventWrapper);
        } else {
            this.eventScanner.addEvent(eventWrapper);
        }
    }

    protected SipProviderImpl(SipStackImpl sipStack) {
        this.eventScanner = sipStack.getEventScanner();
        this.sipStack = sipStack;
        this.eventScanner.incrementRefcount();
        this.listeningPoints = new ConcurrentHashMap();
        this.automaticDialogSupportEnabled = this.sipStack.isAutomaticDialogSupportEnabled();
        this.dialogErrorsAutomaticallyHandled = this.sipStack.isAutomaticDialogErrorHandlingEnabled();
    }

    /* access modifiers changed from: protected */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public void addSipListener(SipListener sipListener) throws TooManyListenersException {
        if (this.sipStack.sipListener == null) {
            this.sipStack.sipListener = sipListener;
        } else if (this.sipStack.sipListener != sipListener) {
            throw new TooManyListenersException("Stack already has a listener. Only one listener per stack allowed");
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("add SipListener " + sipListener);
        }
        this.sipListener = sipListener;
    }

    public ListeningPoint getListeningPoint() {
        if (this.listeningPoints.size() > 0) {
            return (ListeningPoint) this.listeningPoints.values().iterator().next();
        }
        return null;
    }

    public CallIdHeader getNewCallId() {
        String callId = Utils.getInstance().generateCallIdentifier(getListeningPoint().getIPAddress());
        CallID callid = new CallID();
        try {
            callid.setCallId(callId);
        } catch (ParseException e) {
        }
        return callid;
    }

    public ClientTransaction getNewClientTransaction(Request request) throws TransactionUnavailableException {
        if (request == null) {
            throw new NullPointerException("null request");
        } else if (this.sipStack.isAlive()) {
            SIPRequest sipRequest = (SIPRequest) request;
            if (sipRequest.getTransaction() != null) {
                throw new TransactionUnavailableException("Transaction already assigned to request");
            } else if (sipRequest.getMethod().equals("ACK")) {
                throw new TransactionUnavailableException("Cannot create client transaction for  ACK");
            } else {
                if (sipRequest.getTopmostVia() == null) {
                    request.setHeader(((ListeningPointImpl) getListeningPoint("udp")).getViaHeader());
                }
                try {
                    sipRequest.checkHeaders();
                    if (sipRequest.getTopmostVia().getBranch() == null || !sipRequest.getTopmostVia().getBranch().startsWith(SIPConstants.BRANCH_MAGIC_COOKIE) || this.sipStack.findTransaction((SIPRequest) request, false) == null) {
                        SIPClientTransaction ct;
                        if (request.getMethod().equalsIgnoreCase(Request.CANCEL)) {
                            ct = (SIPClientTransaction) this.sipStack.findCancelTransaction((SIPRequest) request, false);
                            if (ct != null) {
                                ClientTransaction retval = this.sipStack.createClientTransaction((SIPRequest) request, ct.getMessageChannel());
                                ((SIPTransaction) retval).addEventListener(this);
                                this.sipStack.addTransaction((SIPClientTransaction) retval);
                                if (ct.getDialog() == null) {
                                    return retval;
                                }
                                ((SIPClientTransaction) retval).setDialog((SIPDialog) ct.getDialog(), sipRequest.getDialogId(false));
                                return retval;
                            }
                        }
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("could not find existing transaction for " + ((SIPRequest) request).getFirstLine() + " creating a new one ");
                        }
                        try {
                            Hop hop = this.sipStack.getNextHop((SIPRequest) request);
                            if (hop == null) {
                                throw new TransactionUnavailableException("Cannot resolve next hop -- transaction unavailable");
                            }
                            String transport = hop.getTransport();
                            ListeningPointImpl listeningPoint = (ListeningPointImpl) getListeningPoint(transport);
                            SIPDialog dialog = this.sipStack.getDialog(sipRequest.getDialogId(false));
                            if (dialog != null && dialog.getState() == DialogState.TERMINATED) {
                                this.sipStack.removeDialog(dialog);
                            }
                            try {
                                if (sipRequest.getTopmostVia().getBranch() == null || !sipRequest.getTopmostVia().getBranch().startsWith(SIPConstants.BRANCH_MAGIC_COOKIE) || this.sipStack.checkBranchId()) {
                                    sipRequest.getTopmostVia().setBranch(Utils.getInstance().generateBranchId());
                                }
                                Via topmostVia = sipRequest.getTopmostVia();
                                if (topmostVia.getTransport() == null) {
                                    topmostVia.setTransport(transport);
                                }
                                if (topmostVia.getPort() == -1) {
                                    topmostVia.setPort(listeningPoint.getPort());
                                }
                                String branchId = sipRequest.getTopmostVia().getBranch();
                                ct = (SIPClientTransaction) this.sipStack.createMessageChannel(sipRequest, listeningPoint.getMessageProcessor(), hop);
                                if (ct == null) {
                                    throw new TransactionUnavailableException("Cound not create tx");
                                }
                                ct.setNextHop(hop);
                                ct.setOriginalRequest(sipRequest);
                                ct.setBranch(branchId);
                                if (SIPTransactionStack.isDialogCreated(request.getMethod())) {
                                    if (dialog != null) {
                                        ct.setDialog(dialog, sipRequest.getDialogId(false));
                                    } else if (isAutomaticDialogSupportEnabled()) {
                                        ct.setDialog(this.sipStack.createDialog(ct), sipRequest.getDialogId(false));
                                    }
                                } else if (dialog != null) {
                                    ct.setDialog(dialog, sipRequest.getDialogId(false));
                                }
                                ct.addEventListener(this);
                                return ct;
                            } catch (IOException ex) {
                                throw new TransactionUnavailableException("Could not resolve next hop or listening point unavailable! ", ex);
                            } catch (ParseException ex2) {
                                InternalErrorHandler.handleException(ex2);
                                throw new TransactionUnavailableException("Unexpected Exception FIXME! ", ex2);
                            } catch (InvalidArgumentException ex22) {
                                InternalErrorHandler.handleException(ex22);
                                throw new TransactionUnavailableException("Unexpected Exception FIXME! ", ex22);
                            }
                        } catch (SipException ex3) {
                            throw new TransactionUnavailableException("Cannot resolve next hop -- transaction unavailable", ex3);
                        }
                    }
                    throw new TransactionUnavailableException("Transaction already exists!");
                } catch (ParseException ex4) {
                    throw new TransactionUnavailableException(ex4.getMessage(), ex4);
                }
            }
        } else {
            throw new TransactionUnavailableException("Stack is stopped");
        }
    }

    public ServerTransaction getNewServerTransaction(Request request) throws TransactionAlreadyExistsException, TransactionUnavailableException {
        if (this.sipStack.isAlive()) {
            SIPRequest sipRequest = (SIPRequest) request;
            try {
                sipRequest.checkHeaders();
                if (request.getMethod().equals("ACK")) {
                    if (logger.isLoggingEnabled()) {
                        logger.logError("Creating server transaction for ACK -- makes no sense!");
                    }
                    throw new TransactionUnavailableException("Cannot create Server transaction for ACK ");
                } else if (sipRequest.getMethod().equals("NOTIFY") && sipRequest.getFromTag() != null && sipRequest.getToTag() == null && this.sipStack.findSubscribeTransaction(sipRequest, (ListeningPointImpl) getListeningPoint()) == null && !this.sipStack.isDeliverUnsolicitedNotify()) {
                    throw new TransactionUnavailableException("Cannot find matching Subscription (and gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY not set)");
                } else if (this.sipStack.acquireSem()) {
                    try {
                        SIPServerTransaction transaction;
                        SIPDialog dialog;
                        if (SIPTransactionStack.isDialogCreated(sipRequest.getMethod())) {
                            if (this.sipStack.findTransaction((SIPRequest) request, true) != null) {
                                throw new TransactionAlreadyExistsException("server transaction already exists!");
                            }
                            transaction = (SIPServerTransaction) ((SIPRequest) request).getTransaction();
                            if (transaction == null) {
                                throw new TransactionUnavailableException("Transaction not available");
                            }
                            if (transaction.getOriginalRequest() == null) {
                                transaction.setOriginalRequest(sipRequest);
                            }
                            this.sipStack.addTransaction(transaction);
                            transaction.addEventListener(this);
                            if (isAutomaticDialogSupportEnabled()) {
                                dialog = this.sipStack.getDialog(sipRequest.getDialogId(true));
                                if (dialog == null) {
                                    dialog = this.sipStack.createDialog(transaction);
                                }
                                transaction.setDialog(dialog, sipRequest.getDialogId(true));
                                if (sipRequest.getMethod().equals("INVITE") && isDialogErrorsAutomaticallyHandled()) {
                                    this.sipStack.putInMergeTable(transaction, sipRequest);
                                }
                                dialog.addRoute(sipRequest);
                                if (!(dialog.getRemoteTag() == null || dialog.getLocalTag() == null)) {
                                    this.sipStack.putDialog(dialog);
                                }
                            }
                        } else if (isAutomaticDialogSupportEnabled()) {
                            if (((SIPServerTransaction) this.sipStack.findTransaction((SIPRequest) request, true)) != null) {
                                throw new TransactionAlreadyExistsException("Transaction exists! ");
                            }
                            transaction = (SIPServerTransaction) ((SIPRequest) request).getTransaction();
                            if (transaction == null) {
                                throw new TransactionUnavailableException("Transaction not available!");
                            }
                            if (transaction.getOriginalRequest() == null) {
                                transaction.setOriginalRequest(sipRequest);
                            }
                            this.sipStack.addTransaction(transaction);
                            dialog = this.sipStack.getDialog(sipRequest.getDialogId(true));
                            if (dialog != null) {
                                dialog.addTransaction(transaction);
                                dialog.addRoute(sipRequest);
                                transaction.setDialog(dialog, sipRequest.getDialogId(true));
                            }
                        } else if (((SIPServerTransaction) this.sipStack.findTransaction((SIPRequest) request, true)) != null) {
                            throw new TransactionAlreadyExistsException("Transaction exists! ");
                        } else {
                            transaction = (SIPServerTransaction) ((SIPRequest) request).getTransaction();
                            if (transaction != null) {
                                if (transaction.getOriginalRequest() == null) {
                                    transaction.setOriginalRequest(sipRequest);
                                }
                                this.sipStack.mapTransaction(transaction);
                                dialog = this.sipStack.getDialog(sipRequest.getDialogId(true));
                                if (dialog != null) {
                                    dialog.addTransaction(transaction);
                                    dialog.addRoute(sipRequest);
                                    transaction.setDialog(dialog, sipRequest.getDialogId(true));
                                }
                                this.sipStack.releaseSem();
                                return transaction;
                            }
                            transaction = this.sipStack.createServerTransaction((MessageChannel) sipRequest.getMessageChannel());
                            if (transaction == null) {
                                throw new TransactionUnavailableException("Transaction unavailable -- too many servrer transactions");
                            }
                            transaction.setOriginalRequest(sipRequest);
                            this.sipStack.mapTransaction(transaction);
                            dialog = this.sipStack.getDialog(sipRequest.getDialogId(true));
                            if (dialog != null) {
                                dialog.addTransaction(transaction);
                                dialog.addRoute(sipRequest);
                                transaction.setDialog(dialog, sipRequest.getDialogId(true));
                            }
                            this.sipStack.releaseSem();
                            return transaction;
                        }
                        this.sipStack.releaseSem();
                        return transaction;
                    } catch (IOException e) {
                        throw new TransactionUnavailableException("Could not send back provisional response!");
                    } catch (IOException e2) {
                        throw new TransactionUnavailableException("Error sending provisional response");
                    } catch (Throwable th) {
                        this.sipStack.releaseSem();
                    }
                } else {
                    throw new TransactionUnavailableException("Transaction not available -- could not acquire stack lock");
                }
            } catch (ParseException ex) {
                throw new TransactionUnavailableException(ex.getMessage(), ex);
            }
        }
        throw new TransactionUnavailableException("Stack is stopped");
    }

    public SipStack getSipStack() {
        return this.sipStack;
    }

    public void removeSipListener(SipListener sipListener) {
        if (sipListener == getSipListener()) {
            this.sipListener = null;
        }
        boolean found = false;
        Iterator<SipProviderImpl> it = this.sipStack.getSipProviders();
        while (it.hasNext()) {
            if (((SipProviderImpl) it.next()).getSipListener() != null) {
                found = true;
            }
        }
        if (!found) {
            this.sipStack.sipListener = null;
        }
    }

    /* JADX WARNING: Missing block: B:65:?, code skipped:
            return;
     */
    public void sendRequest(org.jitsi.javax.sip.message.Request r13) throws org.jitsi.javax.sip.SipException {
        /*
        r12 = this;
        r11 = 32;
        r8 = r12.sipStack;
        r8 = r8.isAlive();
        if (r8 != 0) goto L_0x0012;
    L_0x000a:
        r8 = new org.jitsi.javax.sip.SipException;
        r9 = "Stack is stopped.";
        r8.m1626init(r9);
        throw r8;
    L_0x0012:
        r8 = r13;
        r8 = (org.jitsi.gov.nist.javax.sip.message.SIPRequest) r8;
        r8 = r8.getRequestLine();
        if (r8 == 0) goto L_0x0061;
    L_0x001b:
        r8 = r13.getMethod();
        r9 = "ACK";
        r8 = r8.equals(r9);
        if (r8 == 0) goto L_0x0061;
    L_0x0027:
        r9 = r12.sipStack;
        r8 = r13;
        r8 = (org.jitsi.gov.nist.javax.sip.message.SIPRequest) r8;
        r10 = 0;
        r8 = r8.getDialogId(r10);
        r1 = r9.getDialog(r8);
        if (r1 == 0) goto L_0x0061;
    L_0x0037:
        r8 = r1.getState();
        if (r8 == 0) goto L_0x0061;
    L_0x003d:
        r8 = logger;
        r8 = r8.isLoggingEnabled();
        if (r8 == 0) goto L_0x0061;
    L_0x0045:
        r8 = logger;
        r9 = new java.lang.StringBuilder;
        r9.<init>();
        r10 = "Dialog exists -- you may want to use Dialog.sendAck() ";
        r9 = r9.append(r10);
        r10 = r1.getState();
        r9 = r9.append(r10);
        r9 = r9.toString();
        r8.logWarning(r9);
    L_0x0061:
        r9 = r12.sipStack;
        r8 = r13;
        r8 = (org.jitsi.gov.nist.javax.sip.message.SIPRequest) r8;
        r8 = r9.getRouter(r8);
        r4 = r8.getNextHop(r13);
        if (r4 != 0) goto L_0x0078;
    L_0x0070:
        r8 = new org.jitsi.javax.sip.SipException;
        r9 = "could not determine next hop!";
        r8.m1626init(r9);
        throw r8;
    L_0x0078:
        r6 = r13;
        r6 = (org.jitsi.gov.nist.javax.sip.message.SIPRequest) r6;
        r8 = r6.isNullRequest();
        if (r8 != 0) goto L_0x008f;
    L_0x0081:
        r8 = r6.getTopmostVia();
        if (r8 != 0) goto L_0x008f;
    L_0x0087:
        r8 = new org.jitsi.javax.sip.SipException;
        r9 = "Invalid SipRequest -- no via header!";
        r8.m1626init(r9);
        throw r8;
    L_0x008f:
        r8 = r6.isNullRequest();	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        if (r8 != 0) goto L_0x00ac;
    L_0x0095:
        r7 = r6.getTopmostVia();	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        r0 = r7.getBranch();	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        if (r0 == 0) goto L_0x00a5;
    L_0x009f:
        r8 = r0.length();	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        if (r8 != 0) goto L_0x00ac;
    L_0x00a5:
        r8 = r6.getTransactionId();	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        r7.setBranch(r8);	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
    L_0x00ac:
        r5 = 0;
        r8 = r12.listeningPoints;	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        r9 = r4.getTransport();	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        r9 = r9.toUpperCase();	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        r8 = r8.containsKey(r9);	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        if (r8 == 0) goto L_0x00db;
    L_0x00bd:
        r8 = r12.sipStack;	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        r9 = r4.getTransport();	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        r9 = r12.getListeningPoint(r9);	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        r9 = r9.getIPAddress();	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        r10 = r4.getTransport();	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        r10 = r12.getListeningPoint(r10);	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        r10 = r10.getPort();	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        r5 = r8.createRawMessageChannel(r9, r10, r4);	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
    L_0x00db:
        if (r5 == 0) goto L_0x010f;
    L_0x00dd:
        r5.sendMessage(r6, r4);	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        r8 = logger;
        r8 = r8.isLoggingEnabled(r11);
        if (r8 == 0) goto L_0x010e;
    L_0x00e8:
        r8 = logger;
        r9 = new java.lang.StringBuilder;
        r9.<init>();
        r10 = "done sending ";
        r9 = r9.append(r10);
        r10 = r13.getMethod();
        r9 = r9.append(r10);
        r10 = " to hop ";
        r9 = r9.append(r10);
        r9 = r9.append(r4);
        r9 = r9.toString();
        r8.logDebug(r9);
    L_0x010e:
        return;
    L_0x010f:
        r8 = new org.jitsi.javax.sip.SipException;	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        r9 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        r9.<init>();	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        r10 = "Could not create a message channel for ";
        r9 = r9.append(r10);	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        r10 = r4.toString();	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        r9 = r9.append(r10);	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        r9 = r9.toString();	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        r8.m1626init(r9);	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
        throw r8;	 Catch:{ IOException -> 0x012c, ParseException -> 0x0172 }
    L_0x012c:
        r2 = move-exception;
        r8 = logger;	 Catch:{ all -> 0x0142 }
        r8 = r8.isLoggingEnabled();	 Catch:{ all -> 0x0142 }
        if (r8 == 0) goto L_0x013a;
    L_0x0135:
        r8 = logger;	 Catch:{ all -> 0x0142 }
        r8.logException(r2);	 Catch:{ all -> 0x0142 }
    L_0x013a:
        r8 = new org.jitsi.javax.sip.SipException;	 Catch:{ all -> 0x0142 }
        r9 = "IO Exception occured while Sending Request";
        r8.m1627init(r9, r2);	 Catch:{ all -> 0x0142 }
        throw r8;	 Catch:{ all -> 0x0142 }
    L_0x0142:
        r8 = move-exception;
        r9 = logger;
        r9 = r9.isLoggingEnabled(r11);
        if (r9 == 0) goto L_0x0171;
    L_0x014b:
        r9 = logger;
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "done sending ";
        r10 = r10.append(r11);
        r11 = r13.getMethod();
        r10 = r10.append(r11);
        r11 = " to hop ";
        r10 = r10.append(r11);
        r10 = r10.append(r4);
        r10 = r10.toString();
        r9.logDebug(r10);
    L_0x0171:
        throw r8;
    L_0x0172:
        r3 = move-exception;
        org.jitsi.gov.nist.core.InternalErrorHandler.handleException(r3);	 Catch:{ all -> 0x0142 }
        r8 = logger;
        r8 = r8.isLoggingEnabled(r11);
        if (r8 == 0) goto L_0x010e;
    L_0x017e:
        r8 = logger;
        r9 = new java.lang.StringBuilder;
        r9.<init>();
        r10 = "done sending ";
        r9 = r9.append(r10);
        r10 = r13.getMethod();
        r9 = r9.append(r10);
        r10 = " to hop ";
        r9 = r9.append(r10);
        r9 = r9.append(r4);
        r9 = r9.toString();
        r8.logDebug(r9);
        goto L_0x010e;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.javax.sip.SipProviderImpl.sendRequest(org.jitsi.javax.sip.message.Request):void");
    }

    public void sendResponse(Response response) throws SipException {
        if (this.sipStack.isAlive()) {
            SIPResponse sipResponse = (SIPResponse) response;
            Via via = sipResponse.getTopmostVia();
            if (via == null) {
                throw new SipException("No via header in response!");
            }
            SIPServerTransaction st = (SIPServerTransaction) this.sipStack.findTransaction((SIPMessage) response, true);
            if (st == null || st.getInternalState() == 5 || !isAutomaticDialogSupportEnabled()) {
                String transport = via.getTransport();
                String host = via.getReceived();
                if (host == null) {
                    host = via.getHost();
                }
                int port = via.getRPort();
                if (port == -1) {
                    port = via.getPort();
                    if (port == -1) {
                        if (transport.equalsIgnoreCase(ListeningPoint.TLS)) {
                            port = 5061;
                        } else {
                            port = 5060;
                        }
                    }
                }
                if (host.indexOf(Separators.COLON) > 0 && host.indexOf("[") < 0) {
                    host = "[" + host + "]";
                }
                Hop hop = this.sipStack.getAddressResolver().resolveAddress(new HopImpl(host, port, transport));
                try {
                    ListeningPointImpl listeningPoint = (ListeningPointImpl) getListeningPoint(transport);
                    if (listeningPoint == null) {
                        throw new SipException("whoopsa daisy! no listening point found for transport " + transport);
                    }
                    this.sipStack.createRawMessageChannel(getListeningPoint(hop.getTransport()).getIPAddress(), listeningPoint.port, hop).sendMessage(sipResponse);
                    return;
                } catch (IOException ex) {
                    throw new SipException(ex.getMessage());
                }
            }
            throw new SipException("Transaction exists -- cannot send response statelessly");
        }
        throw new SipException("Stack is stopped");
    }

    public synchronized void setListeningPoint(ListeningPoint listeningPoint) {
        if (listeningPoint == null) {
            throw new NullPointerException("Null listening point");
        }
        ListeningPointImpl lp = (ListeningPointImpl) listeningPoint;
        lp.sipProvider = this;
        String transport = lp.getTransport().toUpperCase();
        this.address = listeningPoint.getIPAddress();
        this.port = listeningPoint.getPort();
        this.listeningPoints.clear();
        this.listeningPoints.put(transport, listeningPoint);
    }

    public Dialog getNewDialog(Transaction transaction) throws SipException {
        if (transaction == null) {
            throw new NullPointerException("Null transaction!");
        } else if (!this.sipStack.isAlive()) {
            throw new SipException("Stack is stopped.");
        } else if (isAutomaticDialogSupportEnabled()) {
            throw new SipException(" Error - AUTOMATIC_DIALOG_SUPPORT is on");
        } else if (SIPTransactionStack.isDialogCreated(transaction.getRequest().getMethod())) {
            SIPDialog dialog;
            SIPTransaction sipTransaction = (SIPTransaction) transaction;
            if (transaction instanceof ServerTransaction) {
                SIPServerTransaction st = (SIPServerTransaction) transaction;
                Response response = st.getLastResponse();
                if (response == null || response.getStatusCode() == 100) {
                    SIPRequest sipRequest = (SIPRequest) transaction.getRequest();
                    dialog = this.sipStack.getDialog(sipRequest.getDialogId(true));
                    if (dialog == null) {
                        dialog = this.sipStack.createDialog((SIPTransaction) transaction);
                        dialog.addTransaction(sipTransaction);
                        dialog.addRoute(sipRequest);
                        sipTransaction.setDialog(dialog, null);
                    } else {
                        sipTransaction.setDialog(dialog, sipRequest.getDialogId(true));
                    }
                    if (sipRequest.getMethod().equals("INVITE") && isDialogErrorsAutomaticallyHandled()) {
                        this.sipStack.putInMergeTable(st, sipRequest);
                    }
                } else {
                    throw new SipException("Cannot set dialog after response has been sent");
                }
            }
            SIPClientTransaction sipClientTx = (SIPClientTransaction) transaction;
            if (sipClientTx.getLastResponse() == null) {
                if (this.sipStack.getDialog(((SIPRequest) sipClientTx.getRequest()).getDialogId(false)) != null) {
                    throw new SipException("Dialog already exists!");
                }
                dialog = this.sipStack.createDialog(sipTransaction);
                sipClientTx.setDialog(dialog, null);
            } else {
                throw new SipException("Cannot call this method after response is received!");
            }
            dialog.addEventListener(this);
            return dialog;
        } else {
            throw new SipException("Dialog cannot be created for this method " + transaction.getRequest().getMethod());
        }
    }

    public void transactionErrorEvent(SIPTransactionErrorEvent transactionErrorEvent) {
        SIPTransaction transaction = (SIPTransaction) transactionErrorEvent.getSource();
        Timeout timeout;
        TimeoutEvent ev;
        Hop hop;
        if (transactionErrorEvent.getErrorID() == 2) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("TransportError occured on " + transaction);
            }
            SIPClientTransaction errorObject = transactionErrorEvent.getSource();
            timeout = Timeout.TRANSACTION;
            if (errorObject instanceof SIPServerTransaction) {
                ev = new TimeoutEvent((Object) this, (ServerTransaction) errorObject, timeout);
            } else {
                hop = errorObject.getNextHop();
                if (this.sipStack.getRouter() instanceof RouterExt) {
                    ((RouterExt) this.sipStack.getRouter()).transactionTimeout(hop);
                }
                ev = new TimeoutEvent((Object) this, (ClientTransaction) errorObject, timeout);
            }
            handleEvent(ev, errorObject);
        } else if (transactionErrorEvent.getErrorID() == 1) {
            Object errorObject2 = transactionErrorEvent.getSource();
            timeout = Timeout.TRANSACTION;
            if (errorObject2 instanceof SIPServerTransaction) {
                ev = new TimeoutEvent((Object) this, (ServerTransaction) errorObject2, timeout);
            } else {
                hop = ((SIPClientTransaction) errorObject2).getNextHop();
                if (this.sipStack.getRouter() instanceof RouterExt) {
                    ((RouterExt) this.sipStack.getRouter()).transactionTimeout(hop);
                }
                ev = new TimeoutEvent((Object) this, (ClientTransaction) errorObject2, timeout);
            }
            handleEvent(ev, (SIPTransaction) errorObject2);
        } else if (transactionErrorEvent.getErrorID() == 3) {
            Transaction errorObject3 = transactionErrorEvent.getSource();
            if (errorObject3.getDialog() != null) {
                InternalErrorHandler.handleException("Unexpected event !", logger);
            }
            timeout = Timeout.RETRANSMIT;
            if (errorObject3 instanceof SIPServerTransaction) {
                ev = new TimeoutEvent((Object) this, (ServerTransaction) errorObject3, timeout);
            } else {
                ev = new TimeoutEvent((Object) this, (ClientTransaction) errorObject3, timeout);
            }
            handleEvent(ev, (SIPTransaction) errorObject3);
        }
    }

    public void dialogErrorEvent(SIPDialogErrorEvent dialogErrorEvent) {
        SIPDialog sipDialog = (SIPDialog) dialogErrorEvent.getSource();
        Reason reason = Reason.AckNotReceived;
        if (dialogErrorEvent.getErrorID() == 2) {
            reason = Reason.AckNotSent;
        } else if (dialogErrorEvent.getErrorID() == 3) {
            reason = Reason.ReInviteTimeout;
        } else if (dialogErrorEvent.getErrorID() == 4) {
            reason = Reason.EarlyStateTimeout;
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Dialog TimeoutError occured on " + sipDialog);
        }
        handleEvent(new DialogTimeoutEvent(this, sipDialog, reason), null);
    }

    public synchronized ListeningPoint[] getListeningPoints() {
        ListeningPoint[] retval;
        retval = new ListeningPointImpl[this.listeningPoints.size()];
        this.listeningPoints.values().toArray(retval);
        return retval;
    }

    public synchronized void addListeningPoint(ListeningPoint listeningPoint) throws ObjectInUseException {
        ListeningPointImpl lp = (ListeningPointImpl) listeningPoint;
        if (lp.sipProvider == null || lp.sipProvider == this) {
            String transport = lp.getTransport().toUpperCase();
            if (this.listeningPoints.isEmpty()) {
                this.address = listeningPoint.getIPAddress();
                this.port = listeningPoint.getPort();
            } else if (!(this.address.equals(listeningPoint.getIPAddress()) && this.port == listeningPoint.getPort())) {
                throw new ObjectInUseException("Provider already has different IP Address associated");
            }
            if (!this.listeningPoints.containsKey(transport) || this.listeningPoints.get(transport) == listeningPoint) {
                lp.sipProvider = this;
                this.listeningPoints.put(transport, lp);
            } else {
                throw new ObjectInUseException("Listening point already assigned for transport!");
            }
        }
        throw new ObjectInUseException("Listening point assigned to another provider");
    }

    public synchronized void removeListeningPoint(ListeningPoint listeningPoint) throws ObjectInUseException {
        ListeningPointImpl lp = (ListeningPointImpl) listeningPoint;
        if (lp.messageProcessor.inUse()) {
            throw new ObjectInUseException("Object is in use");
        }
        this.listeningPoints.remove(lp.getTransport().toUpperCase());
    }

    public synchronized void removeListeningPoints() {
        Iterator it = this.listeningPoints.values().iterator();
        while (it.hasNext()) {
            ((ListeningPointImpl) it.next()).messageProcessor.stop();
            it.remove();
        }
    }

    public void setAutomaticDialogSupportEnabled(boolean automaticDialogSupportEnabled) {
        this.automaticDialogSupportEnabled = automaticDialogSupportEnabled;
        if (this.automaticDialogSupportEnabled) {
            this.dialogErrorsAutomaticallyHandled = true;
        }
    }

    public boolean isAutomaticDialogSupportEnabled() {
        return this.automaticDialogSupportEnabled;
    }

    public void setDialogErrorsAutomaticallyHandled() {
        this.dialogErrorsAutomaticallyHandled = true;
    }

    public boolean isDialogErrorsAutomaticallyHandled() {
        return this.dialogErrorsAutomaticallyHandled;
    }

    public SipListener getSipListener() {
        return this.sipListener;
    }
}
