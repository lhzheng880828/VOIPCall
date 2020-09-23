package org.jitsi.gov.nist.javax.sip;

import java.util.EventObject;
import java.util.LinkedList;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.javax.sip.message.SIPMessage;
import org.jitsi.gov.nist.javax.sip.message.SIPRequest;
import org.jitsi.gov.nist.javax.sip.message.SIPResponse;
import org.jitsi.gov.nist.javax.sip.stack.SIPClientTransaction;
import org.jitsi.gov.nist.javax.sip.stack.SIPDialog;
import org.jitsi.gov.nist.javax.sip.stack.SIPServerTransaction;
import org.jitsi.gov.nist.javax.sip.stack.SIPTransaction;
import org.jitsi.javax.sip.DialogState;
import org.jitsi.javax.sip.DialogTerminatedEvent;
import org.jitsi.javax.sip.IOExceptionEvent;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.ResponseEvent;
import org.jitsi.javax.sip.SipListener;
import org.jitsi.javax.sip.TimeoutEvent;
import org.jitsi.javax.sip.TransactionTerminatedEvent;
import org.jitsi.javax.sip.message.Response;

class EventScanner implements Runnable {
    private static StackLogger logger = CommonLogger.getLogger(EventScanner.class);
    private int[] eventMutex = new int[]{0};
    private boolean isStopped;
    private LinkedList pendingEvents = new LinkedList();
    private int refCount;
    private SipStackImpl sipStack;

    public void incrementRefcount() {
        synchronized (this.eventMutex) {
            this.refCount++;
        }
    }

    public EventScanner(SipStackImpl sipStackImpl) {
        Thread myThread = new Thread(this);
        myThread.setDaemon(false);
        this.sipStack = sipStackImpl;
        myThread.setName("EventScannerThread");
        myThread.start();
    }

    public void addEvent(EventWrapper eventWrapper) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("addEvent " + eventWrapper);
        }
        synchronized (this.eventMutex) {
            this.pendingEvents.add(eventWrapper);
            this.eventMutex.notify();
        }
    }

    public void stop() {
        synchronized (this.eventMutex) {
            if (this.refCount > 0) {
                this.refCount--;
            }
            if (this.refCount == 0) {
                this.isStopped = true;
                this.eventMutex.notify();
            }
        }
    }

    public void forceStop() {
        synchronized (this.eventMutex) {
            this.isStopped = true;
            this.refCount = 0;
            this.eventMutex.notify();
        }
    }

    public void deliverEvent(EventWrapper eventWrapper) {
        SipListener sipListener;
        EventObject sipEvent = eventWrapper.sipEvent;
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("sipEvent = " + sipEvent + "source = " + sipEvent.getSource());
        }
        if (sipEvent instanceof IOExceptionEvent) {
            sipListener = this.sipStack.getSipListener();
        } else {
            sipListener = ((SipProviderImpl) sipEvent.getSource()).getSipListener();
        }
        if (sipEvent instanceof RequestEvent) {
            SIPTransaction sIPTransaction;
            try {
                SIPRequest sipRequest = (SIPRequest) ((RequestEvent) sipEvent).getRequest();
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("deliverEvent : " + sipRequest.getFirstLine() + " transaction " + eventWrapper.transaction + " sipEvent.serverTx = " + ((RequestEvent) sipEvent).getServerTransaction());
                }
                SIPServerTransaction tx = (SIPServerTransaction) this.sipStack.findTransaction((SIPMessage) sipRequest, true);
                if (tx == null || tx.passToListener()) {
                    if (this.sipStack.findPendingTransaction(sipRequest.getTransactionId()) != null) {
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("transaction already exists!!");
                        }
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("Done processing Message " + ((SIPRequest) ((RequestEvent) sipEvent).getRequest()).getFirstLine());
                        }
                        if (eventWrapper.transaction != null && ((SIPServerTransaction) eventWrapper.transaction).passToListener()) {
                            ((SIPServerTransaction) eventWrapper.transaction).releaseSem();
                        }
                        if (eventWrapper.transaction != null) {
                            this.sipStack.removePendingTransaction((SIPServerTransaction) eventWrapper.transaction);
                        }
                        if (eventWrapper.transaction.getMethod().equals("ACK")) {
                            sIPTransaction = eventWrapper.transaction;
                            sIPTransaction.setState(5);
                        }
                        return;
                    }
                    this.sipStack.putPendingTransaction(eventWrapper.transaction);
                } else if (!sipRequest.getMethod().equals("ACK") || !tx.isInviteTransaction() || (tx.getLastResponseStatusCode() / 100 != 2 && !this.sipStack.isNon2XXAckPassedToListener())) {
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("transaction already exists! " + tx);
                    }
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("Done processing Message " + ((SIPRequest) ((RequestEvent) sipEvent).getRequest()).getFirstLine());
                    }
                    if (eventWrapper.transaction != null && ((SIPServerTransaction) eventWrapper.transaction).passToListener()) {
                        ((SIPServerTransaction) eventWrapper.transaction).releaseSem();
                    }
                    if (eventWrapper.transaction != null) {
                        this.sipStack.removePendingTransaction((SIPServerTransaction) eventWrapper.transaction);
                    }
                    if (eventWrapper.transaction.getMethod().equals("ACK")) {
                        sIPTransaction = eventWrapper.transaction;
                        sIPTransaction.setState(5);
                    }
                    return;
                } else if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("Detected broken client sending ACK with same branch! Passing...");
                }
                sipRequest.setTransaction(eventWrapper.transaction);
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("Calling listener " + sipRequest.getFirstLine());
                    logger.logDebug("Calling listener " + eventWrapper.transaction);
                }
                if (sipListener != null) {
                    sipListener.processRequest((RequestEvent) sipEvent);
                }
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("Done processing Message " + sipRequest.getFirstLine());
                }
                if (eventWrapper.transaction != null) {
                    SIPDialog dialog = (SIPDialog) eventWrapper.transaction.getDialog();
                    if (dialog != null) {
                        dialog.requestConsumed();
                    }
                }
            } catch (Exception ex) {
                logger.logException(ex);
            } catch (Throwable th) {
                Throwable th2 = th;
                if (logger.isLoggingEnabled(32)) {
                    StackLogger stackLogger = logger;
                    StackLogger stackLogger2 = stackLogger;
                    stackLogger2.logDebug("Done processing Message " + ((SIPRequest) ((RequestEvent) sipEvent).getRequest()).getFirstLine());
                }
                if (eventWrapper.transaction != null && ((SIPServerTransaction) eventWrapper.transaction).passToListener()) {
                    ((SIPServerTransaction) eventWrapper.transaction).releaseSem();
                }
                if (eventWrapper.transaction != null) {
                    this.sipStack.removePendingTransaction((SIPServerTransaction) eventWrapper.transaction);
                }
                if (eventWrapper.transaction.getMethod().equals("ACK")) {
                    eventWrapper.transaction.setState(5);
                }
            }
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("Done processing Message " + ((SIPRequest) ((RequestEvent) sipEvent).getRequest()).getFirstLine());
            }
            if (eventWrapper.transaction != null && ((SIPServerTransaction) eventWrapper.transaction).passToListener()) {
                ((SIPServerTransaction) eventWrapper.transaction).releaseSem();
            }
            if (eventWrapper.transaction != null) {
                this.sipStack.removePendingTransaction((SIPServerTransaction) eventWrapper.transaction);
            }
            if (eventWrapper.transaction.getMethod().equals("ACK")) {
                sIPTransaction = eventWrapper.transaction;
                sIPTransaction.setState(5);
            }
        } else if (sipEvent instanceof ResponseEvent) {
            try {
                ResponseEvent responseEvent = (ResponseEvent) sipEvent;
                SIPResponse sipResponse = (SIPResponse) responseEvent.getResponse();
                SIPDialog sipDialog = (SIPDialog) responseEvent.getDialog();
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("Calling listener " + sipListener + " for " + sipResponse.getFirstLine());
                }
                if (sipListener != null) {
                    SIPTransaction tx2 = eventWrapper.transaction;
                    if (tx2 != null) {
                        tx2.setPassToListener();
                    }
                    sipListener.processResponse((ResponseEvent) sipEvent);
                }
                if (sipDialog != null && ((sipDialog.getState() == null || !sipDialog.getState().equals(DialogState.TERMINATED)) && (sipResponse.getStatusCode() == Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST || sipResponse.getStatusCode() == Response.REQUEST_TIMEOUT))) {
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("Removing dialog on 408 or 481 response");
                    }
                    sipDialog.doDeferredDelete();
                }
                if (sipResponse.getCSeq().getMethod().equals("INVITE") && sipDialog != null && sipResponse.getStatusCode() == Response.OK) {
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("Warning! unacknowledged dialog. " + sipDialog.getState());
                    }
                    sipDialog.doDeferredDeleteIfNoAckSent(sipResponse.getCSeq().getSeqNumber());
                }
            } catch (Exception ex2) {
                logger.logException(ex2);
            } catch (Throwable th3) {
                if (eventWrapper.transaction != null && eventWrapper.transaction.passToListener()) {
                    eventWrapper.transaction.releaseSem();
                }
            }
            SIPClientTransaction ct = eventWrapper.transaction;
            if (!(ct == null || 3 != ct.getInternalState() || ct.getMethod().equals("INVITE"))) {
                ct.clearState();
            }
            if (eventWrapper.transaction != null && eventWrapper.transaction.passToListener()) {
                eventWrapper.transaction.releaseSem();
            }
        } else if (sipEvent instanceof TimeoutEvent) {
            if (sipListener != null) {
                try {
                    sipListener.processTimeout((TimeoutEvent) sipEvent);
                } catch (Exception ex22) {
                    logger.logException(ex22);
                }
            }
        } else if (sipEvent instanceof DialogTimeoutEvent) {
            if (sipListener != null) {
                try {
                    if (sipListener instanceof SipListenerExt) {
                        ((SipListenerExt) sipListener).processDialogTimeout((DialogTimeoutEvent) sipEvent);
                        return;
                    }
                } catch (Exception ex222) {
                    logger.logException(ex222);
                    return;
                }
            }
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("DialogTimeoutEvent not delivered");
            }
        } else if (sipEvent instanceof IOExceptionEvent) {
            if (sipListener != null) {
                try {
                    sipListener.processIOException((IOExceptionEvent) sipEvent);
                } catch (Exception ex2222) {
                    logger.logException(ex2222);
                }
            }
        } else if (sipEvent instanceof TransactionTerminatedEvent) {
            try {
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("About to deliver transactionTerminatedEvent");
                    logger.logDebug("tx = " + ((TransactionTerminatedEvent) sipEvent).getClientTransaction());
                    logger.logDebug("tx = " + ((TransactionTerminatedEvent) sipEvent).getServerTransaction());
                }
                if (sipListener != null) {
                    sipListener.processTransactionTerminated((TransactionTerminatedEvent) sipEvent);
                }
            } catch (AbstractMethodError e) {
                if (logger.isLoggingEnabled()) {
                    logger.logWarning("Unable to call sipListener.processTransactionTerminated");
                }
            } catch (Exception ex22222) {
                logger.logException(ex22222);
            }
        } else if (!(sipEvent instanceof DialogTerminatedEvent)) {
            logger.logFatalError("bad event" + sipEvent);
        } else if (sipListener != null) {
            try {
                sipListener.processDialogTerminated((DialogTerminatedEvent) sipEvent);
            } catch (AbstractMethodError e2) {
                if (logger.isLoggingEnabled()) {
                    logger.logWarning("Unable to call sipListener.processDialogTerminated");
                }
            } catch (Exception ex222222) {
                logger.logException(ex222222);
            }
        }
    }

    /* JADX WARNING: Missing block: B:15:0x0034, code skipped:
            if (logger.isLoggingEnabled(32) == false) goto L_?;
     */
    /* JADX WARNING: Missing block: B:17:0x0038, code skipped:
            if (r11.isStopped != false) goto L_?;
     */
    /* JADX WARNING: Missing block: B:18:0x003a, code skipped:
            r6 = logger;
            r7 = "Event scanner exited abnormally";
     */
    /* JADX WARNING: Missing block: B:38:?, code skipped:
            r4 = r2.listIterator();
     */
    /* JADX WARNING: Missing block: B:40:0x0085, code skipped:
            if (r4.hasNext() == false) goto L_0x000c;
     */
    /* JADX WARNING: Missing block: B:41:0x0087, code skipped:
            r1 = (org.jitsi.gov.nist.javax.sip.EventWrapper) r4.next();
     */
    /* JADX WARNING: Missing block: B:42:0x0095, code skipped:
            if (logger.isLoggingEnabled(32) == false) goto L_0x00bd;
     */
    /* JADX WARNING: Missing block: B:43:0x0097, code skipped:
            logger.logDebug("Processing " + r1 + "nevents " + r2.size());
     */
    /* JADX WARNING: Missing block: B:45:?, code skipped:
            deliverEvent(r1);
     */
    /* JADX WARNING: Missing block: B:74:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:75:?, code skipped:
            return;
     */
    public void run() {
        /*
        r11 = this;
        r10 = 32;
        r6 = r11.sipStack;	 Catch:{ all -> 0x00d2 }
        r6 = r6.getThreadAuditor();	 Catch:{ all -> 0x00d2 }
        r5 = r6.addCurrentThread();	 Catch:{ all -> 0x00d2 }
    L_0x000c:
        r1 = 0;
        r7 = r11.eventMutex;	 Catch:{ all -> 0x00d2 }
        monitor-enter(r7);	 Catch:{ all -> 0x00d2 }
    L_0x0010:
        r6 = r11.pendingEvents;	 Catch:{ all -> 0x00e7 }
        r6 = r6.isEmpty();	 Catch:{ all -> 0x00e7 }
        if (r6 == 0) goto L_0x0073;
    L_0x0018:
        r6 = r11.isStopped;	 Catch:{ all -> 0x00e7 }
        if (r6 == 0) goto L_0x0042;
    L_0x001c:
        r6 = logger;	 Catch:{ all -> 0x00e7 }
        r8 = 32;
        r6 = r6.isLoggingEnabled(r8);	 Catch:{ all -> 0x00e7 }
        if (r6 == 0) goto L_0x002d;
    L_0x0026:
        r6 = logger;	 Catch:{ all -> 0x00e7 }
        r8 = "Stopped event scanner!!";
        r6.logDebug(r8);	 Catch:{ all -> 0x00e7 }
    L_0x002d:
        monitor-exit(r7);	 Catch:{ all -> 0x00e7 }
        r6 = logger;
        r6 = r6.isLoggingEnabled(r10);
        if (r6 == 0) goto L_0x0041;
    L_0x0036:
        r6 = r11.isStopped;
        if (r6 != 0) goto L_0x0041;
    L_0x003a:
        r6 = logger;
        r7 = "Event scanner exited abnormally";
    L_0x003e:
        r6.logFatalError(r7);
    L_0x0041:
        return;
    L_0x0042:
        r5.ping();	 Catch:{ InterruptedException -> 0x004f }
        r6 = r11.eventMutex;	 Catch:{ InterruptedException -> 0x004f }
        r8 = r5.getPingIntervalInMillisecs();	 Catch:{ InterruptedException -> 0x004f }
        r6.wait(r8);	 Catch:{ InterruptedException -> 0x004f }
        goto L_0x0010;
    L_0x004f:
        r3 = move-exception;
        r6 = logger;	 Catch:{ all -> 0x00e7 }
        r8 = 32;
        r6 = r6.isLoggingEnabled(r8);	 Catch:{ all -> 0x00e7 }
        if (r6 == 0) goto L_0x0061;
    L_0x005a:
        r6 = logger;	 Catch:{ all -> 0x00e7 }
        r8 = "Interrupted!";
        r6.logDebug(r8);	 Catch:{ all -> 0x00e7 }
    L_0x0061:
        monitor-exit(r7);	 Catch:{ all -> 0x00e7 }
        r6 = logger;
        r6 = r6.isLoggingEnabled(r10);
        if (r6 == 0) goto L_0x0041;
    L_0x006a:
        r6 = r11.isStopped;
        if (r6 != 0) goto L_0x0041;
    L_0x006e:
        r6 = logger;
        r7 = "Event scanner exited abnormally";
        goto L_0x003e;
    L_0x0073:
        r2 = r11.pendingEvents;	 Catch:{ all -> 0x00e7 }
        r6 = new java.util.LinkedList;	 Catch:{ all -> 0x00e7 }
        r6.<init>();	 Catch:{ all -> 0x00e7 }
        r11.pendingEvents = r6;	 Catch:{ all -> 0x00e7 }
        monitor-exit(r7);	 Catch:{ all -> 0x00e7 }
        r4 = r2.listIterator();	 Catch:{ all -> 0x00d2 }
    L_0x0081:
        r6 = r4.hasNext();	 Catch:{ all -> 0x00d2 }
        if (r6 == 0) goto L_0x000c;
    L_0x0087:
        r1 = r4.next();	 Catch:{ all -> 0x00d2 }
        r1 = (org.jitsi.gov.nist.javax.sip.EventWrapper) r1;	 Catch:{ all -> 0x00d2 }
        r6 = logger;	 Catch:{ all -> 0x00d2 }
        r7 = 32;
        r6 = r6.isLoggingEnabled(r7);	 Catch:{ all -> 0x00d2 }
        if (r6 == 0) goto L_0x00bd;
    L_0x0097:
        r6 = logger;	 Catch:{ all -> 0x00d2 }
        r7 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00d2 }
        r7.<init>();	 Catch:{ all -> 0x00d2 }
        r8 = "Processing ";
        r7 = r7.append(r8);	 Catch:{ all -> 0x00d2 }
        r7 = r7.append(r1);	 Catch:{ all -> 0x00d2 }
        r8 = "nevents ";
        r7 = r7.append(r8);	 Catch:{ all -> 0x00d2 }
        r8 = r2.size();	 Catch:{ all -> 0x00d2 }
        r7 = r7.append(r8);	 Catch:{ all -> 0x00d2 }
        r7 = r7.toString();	 Catch:{ all -> 0x00d2 }
        r6.logDebug(r7);	 Catch:{ all -> 0x00d2 }
    L_0x00bd:
        r11.deliverEvent(r1);	 Catch:{ Exception -> 0x00c1 }
        goto L_0x0081;
    L_0x00c1:
        r0 = move-exception;
        r6 = logger;	 Catch:{ all -> 0x00d2 }
        r6 = r6.isLoggingEnabled();	 Catch:{ all -> 0x00d2 }
        if (r6 == 0) goto L_0x0081;
    L_0x00ca:
        r6 = logger;	 Catch:{ all -> 0x00d2 }
        r7 = "Unexpected exception caught while delivering event -- carrying on bravely";
        r6.logError(r7, r0);	 Catch:{ all -> 0x00d2 }
        goto L_0x0081;
    L_0x00d2:
        r6 = move-exception;
        r7 = logger;
        r7 = r7.isLoggingEnabled(r10);
        if (r7 == 0) goto L_0x00e6;
    L_0x00db:
        r7 = r11.isStopped;
        if (r7 != 0) goto L_0x00e6;
    L_0x00df:
        r7 = logger;
        r8 = "Event scanner exited abnormally";
        r7.logFatalError(r8);
    L_0x00e6:
        throw r6;
    L_0x00e7:
        r6 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x00e7 }
        throw r6;	 Catch:{ all -> 0x00d2 }
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.javax.sip.EventScanner.run():void");
    }
}
