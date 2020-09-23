package org.jitsi.gov.nist.javax.sip;

import java.io.IOException;
import java.util.EventObject;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.HostPort;
import org.jitsi.gov.nist.core.InternalErrorHandler;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.javax.sip.address.SipUri;
import org.jitsi.gov.nist.javax.sip.header.Contact;
import org.jitsi.gov.nist.javax.sip.header.Event;
import org.jitsi.gov.nist.javax.sip.header.RetryAfter;
import org.jitsi.gov.nist.javax.sip.header.Route;
import org.jitsi.gov.nist.javax.sip.header.RouteList;
import org.jitsi.gov.nist.javax.sip.message.MessageFactoryImpl;
import org.jitsi.gov.nist.javax.sip.message.SIPRequest;
import org.jitsi.gov.nist.javax.sip.message.SIPResponse;
import org.jitsi.gov.nist.javax.sip.stack.MessageChannel;
import org.jitsi.gov.nist.javax.sip.stack.SIPClientTransaction;
import org.jitsi.gov.nist.javax.sip.stack.SIPDialog;
import org.jitsi.gov.nist.javax.sip.stack.SIPServerTransaction;
import org.jitsi.gov.nist.javax.sip.stack.SIPTransaction;
import org.jitsi.gov.nist.javax.sip.stack.SIPTransactionStack;
import org.jitsi.gov.nist.javax.sip.stack.ServerRequestInterface;
import org.jitsi.gov.nist.javax.sip.stack.ServerResponseInterface;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.DialogState;
import org.jitsi.javax.sip.ListeningPoint;
import org.jitsi.javax.sip.ObjectInUseException;
import org.jitsi.javax.sip.ServerTransaction;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.TransactionState;
import org.jitsi.javax.sip.header.Header;
import org.jitsi.javax.sip.header.ReferToHeader;
import org.jitsi.javax.sip.header.ServerHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;

class DialogFilter implements ServerRequestInterface, ServerResponseInterface {
    private static StackLogger logger = CommonLogger.getLogger(DialogFilter.class);
    protected ListeningPointImpl listeningPoint;
    private SIPTransactionStack sipStack;
    protected SIPTransaction transactionChannel;

    public DialogFilter(SIPTransactionStack sipStack) {
        this.sipStack = sipStack;
    }

    private void sendRequestPendingResponse(SIPRequest sipRequest, SIPServerTransaction transaction) {
        if (transaction.getState() != TransactionState.TERMINATED) {
            Response sipResponse = sipRequest.createResponse(Response.REQUEST_PENDING);
            ServerHeader serverHeader = MessageFactoryImpl.getDefaultServerHeader();
            if (serverHeader != null) {
                sipResponse.setHeader((Header) serverHeader);
            }
            try {
                RetryAfter retryAfter = new RetryAfter();
                retryAfter.setRetryAfter(1);
                sipResponse.setHeader((Header) retryAfter);
                if (sipRequest.getMethod().equals("INVITE")) {
                    this.sipStack.addTransactionPendingAck(transaction);
                }
                transaction.sendResponse(sipResponse);
                transaction.releaseSem();
            } catch (Exception ex) {
                logger.logError("Problem sending error response", ex);
                transaction.releaseSem();
                this.sipStack.removeTransaction(transaction);
            }
        }
    }

    private void sendBadRequestResponse(SIPRequest sipRequest, SIPServerTransaction transaction, String reasonPhrase) {
        if (transaction.getState() != TransactionState.TERMINATED) {
            Response sipResponse = sipRequest.createResponse(Response.BAD_REQUEST);
            if (reasonPhrase != null) {
                sipResponse.setReasonPhrase(reasonPhrase);
            }
            ServerHeader serverHeader = MessageFactoryImpl.getDefaultServerHeader();
            if (serverHeader != null) {
                sipResponse.setHeader((Header) serverHeader);
            }
            try {
                if (sipRequest.getMethod().equals("INVITE")) {
                    this.sipStack.addTransactionPendingAck(transaction);
                }
                transaction.sendResponse(sipResponse);
                transaction.releaseSem();
            } catch (Exception ex) {
                logger.logError("Problem sending error response", ex);
                transaction.releaseSem();
                this.sipStack.removeTransaction(transaction);
            }
        }
    }

    private void sendCallOrTransactionDoesNotExistResponse(SIPRequest sipRequest, SIPServerTransaction transaction) {
        if (transaction.getState() != TransactionState.TERMINATED) {
            Response sipResponse = sipRequest.createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
            ServerHeader serverHeader = MessageFactoryImpl.getDefaultServerHeader();
            if (serverHeader != null) {
                sipResponse.setHeader((Header) serverHeader);
            }
            try {
                if (sipRequest.getMethod().equals("INVITE")) {
                    this.sipStack.addTransactionPendingAck(transaction);
                }
                transaction.sendResponse(sipResponse);
                transaction.releaseSem();
            } catch (Exception ex) {
                logger.logError("Problem sending error response", ex);
                transaction.releaseSem();
                this.sipStack.removeTransaction(transaction);
            }
        }
    }

    private void sendLoopDetectedResponse(SIPRequest sipRequest, SIPServerTransaction transaction) {
        Response sipResponse = sipRequest.createResponse(Response.LOOP_DETECTED);
        if (transaction.getState() != TransactionState.TERMINATED) {
            ServerHeader serverHeader = MessageFactoryImpl.getDefaultServerHeader();
            if (serverHeader != null) {
                sipResponse.setHeader((Header) serverHeader);
            }
            try {
                this.sipStack.addTransactionPendingAck(transaction);
                transaction.sendResponse(sipResponse);
                transaction.releaseSem();
            } catch (Exception ex) {
                logger.logError("Problem sending error response", ex);
                transaction.releaseSem();
                this.sipStack.removeTransaction(transaction);
            }
        }
    }

    private void sendServerInternalErrorResponse(SIPRequest sipRequest, SIPServerTransaction transaction) {
        if (transaction.getState() != TransactionState.TERMINATED) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("Sending 500 response for out of sequence message");
            }
            Response sipResponse = sipRequest.createResponse(500);
            sipResponse.setReasonPhrase("Request out of order");
            if (MessageFactoryImpl.getDefaultServerHeader() != null) {
                sipResponse.setHeader((Header) MessageFactoryImpl.getDefaultServerHeader());
            }
            try {
                RetryAfter retryAfter = new RetryAfter();
                retryAfter.setRetryAfter(10);
                sipResponse.setHeader((Header) retryAfter);
                this.sipStack.addTransactionPendingAck(transaction);
                transaction.sendResponse(sipResponse);
                transaction.releaseSem();
            } catch (Exception ex) {
                logger.logError("Problem sending response", ex);
                transaction.releaseSem();
                this.sipStack.removeTransaction(transaction);
            }
        }
    }

    public void processRequest(SIPRequest sipRequest, MessageChannel incomingMessageChannel) {
        if (logger.isLoggingEnabled(32) && this.listeningPoint != null) {
            logger.logDebug("PROCESSING INCOMING REQUEST " + sipRequest + " transactionChannel = " + this.transactionChannel + " listening point = " + this.listeningPoint.getIPAddress() + Separators.COLON + this.listeningPoint.getPort());
        }
        if (this.listeningPoint != null) {
            SIPTransactionStack sipStack = this.transactionChannel.getSIPStack();
            SipProviderImpl sipProvider = this.listeningPoint.getProvider();
            if (sipProvider != null) {
                if (sipStack == null) {
                    InternalErrorHandler.handleException("Egads! no sip stack!");
                }
                SIPTransaction transaction = (SIPServerTransaction) this.transactionChannel;
                if (transaction != null && logger.isLoggingEnabled(32)) {
                    logger.logDebug("transaction state = " + transaction.getState());
                }
                String dialogId = sipRequest.getDialogId(true);
                SIPDialog dialog = sipStack.getDialog(dialogId);
                if (!(dialog == null || sipProvider == dialog.getSipProvider())) {
                    Contact contact = dialog.getMyContactHeader();
                    if (contact != null) {
                        SipUri contactUri = (SipUri) contact.getAddress().getURI();
                        String ipAddress = contactUri.getHost();
                        int contactPort = contactUri.getPort();
                        String contactTransport = contactUri.getTransportParam();
                        if (contactTransport == null) {
                            contactTransport = "udp";
                        }
                        if (contactPort == -1) {
                            if (contactTransport.equals("udp") || contactTransport.equals("tcp")) {
                                contactPort = 5060;
                            } else {
                                contactPort = 5061;
                            }
                        }
                        if (!(ipAddress == null || (ipAddress.equals(this.listeningPoint.getIPAddress()) && contactPort == this.listeningPoint.getPort()))) {
                            if (logger.isLoggingEnabled(32)) {
                                logger.logDebug("nulling dialog -- listening point mismatch!  " + contactPort + "  lp port = " + this.listeningPoint.getPort());
                            }
                            dialog = null;
                        }
                    }
                }
                if (sipProvider.isDialogErrorsAutomaticallyHandled() && sipRequest.getToTag() == null && sipStack.findMergedTransaction(sipRequest)) {
                    sendLoopDetectedResponse(sipRequest, transaction);
                    return;
                }
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("dialogId = " + dialogId);
                    logger.logDebug("dialog = " + dialog);
                }
                if (!(sipRequest.getHeader("Route") == null || transaction.getDialog() == null)) {
                    RouteList routes = sipRequest.getRouteHeaders();
                    HostPort hostPort = ((SipUri) ((Route) routes.getFirst()).getAddress().getURI()).getHostPort();
                    int port;
                    if (hostPort.hasPort()) {
                        port = hostPort.getPort();
                    } else if (this.listeningPoint.getTransport().equalsIgnoreCase(ListeningPoint.TLS)) {
                        port = 5061;
                    } else {
                        port = 5060;
                    }
                    String host = hostPort.getHost().encode();
                    if ((host.equals(this.listeningPoint.getIPAddress()) || host.equalsIgnoreCase(this.listeningPoint.getSentBy())) && port == this.listeningPoint.getPort()) {
                        if (routes.size() == 1) {
                            sipRequest.removeHeader("Route");
                        } else {
                            routes.removeFirst();
                        }
                    }
                }
                String sipRequestMethod = sipRequest.getMethod();
                SIPTransaction lastTransaction;
                SIPServerTransaction st;
                SIPResponse response;
                if (sipRequestMethod.equals(Request.REFER) && dialog != null && sipProvider.isDialogErrorsAutomaticallyHandled()) {
                    if (((ReferToHeader) sipRequest.getHeader(ReferToHeader.NAME)) == null) {
                        sendBadRequestResponse(sipRequest, transaction, "Refer-To header is missing");
                        return;
                    }
                    lastTransaction = dialog.getLastTransaction();
                    if (lastTransaction != null && sipProvider.isDialogErrorsAutomaticallyHandled()) {
                        String lastTransactionMethod = lastTransaction.getMethod();
                        if (lastTransaction instanceof SIPServerTransaction) {
                            if ((lastTransaction.getInternalState() == 2 || lastTransaction.getInternalState() == 1) && lastTransactionMethod.equals("INVITE")) {
                                sendRequestPendingResponse(sipRequest, transaction);
                                return;
                            }
                        } else if (!(lastTransaction == null || !(lastTransaction instanceof SIPClientTransaction) || !lastTransactionMethod.equals("INVITE") || lastTransaction.getInternalState() == 5 || lastTransaction.getInternalState() == 3)) {
                            sendRequestPendingResponse(sipRequest, transaction);
                            return;
                        }
                    }
                } else if (sipRequestMethod.equals(Request.UPDATE)) {
                    if (sipProvider.isAutomaticDialogSupportEnabled() && dialog == null) {
                        sendCallOrTransactionDoesNotExistResponse(sipRequest, transaction);
                        return;
                    }
                } else if (sipRequestMethod.equals("ACK")) {
                    if (transaction == null || !transaction.isInviteTransaction()) {
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("Processing ACK for dialog " + dialog);
                        }
                        SIPServerTransaction ackTransaction;
                        if (dialog == null) {
                            if (logger.isLoggingEnabled(32)) {
                                logger.logDebug("Dialog does not exist " + sipRequest.getFirstLine() + " isServerTransaction = " + true);
                            }
                            st = sipStack.getRetransmissionAlertTransaction(dialogId);
                            if (st != null && st.isRetransmissionAlertEnabled()) {
                                st.disableRetransmissionAlerts();
                            }
                            ackTransaction = sipStack.findTransactionPendingAck(sipRequest);
                            if (ackTransaction != null) {
                                if (logger.isLoggingEnabled(32)) {
                                    logger.logDebug("Found Tx pending ACK");
                                }
                                try {
                                    ackTransaction.setAckSeen();
                                    sipStack.removeTransaction(ackTransaction);
                                    sipStack.removeTransactionPendingAck(ackTransaction);
                                    return;
                                } catch (Exception ex) {
                                    if (logger.isLoggingEnabled()) {
                                        logger.logError("Problem terminating transaction", ex);
                                        return;
                                    }
                                    return;
                                }
                            }
                        } else if (dialog.handleAck(transaction)) {
                            dialog.addTransaction(transaction);
                            transaction.passToListener();
                            dialog.addRoute(sipRequest);
                            transaction.setDialog(dialog, dialogId);
                            if (sipRequest.getMethod().equals("INVITE") && sipProvider.isDialogErrorsAutomaticallyHandled()) {
                                sipStack.putInMergeTable(transaction, sipRequest);
                            }
                            if (sipStack.isDeliverTerminatedEventForAck()) {
                                try {
                                    sipStack.addTransaction((SIPServerTransaction) transaction);
                                    transaction.scheduleAckRemoval();
                                } catch (IOException e) {
                                }
                            } else {
                                transaction.setMapped(true);
                            }
                        } else if (dialog.isSequnceNumberValidation()) {
                            if (logger.isLoggingEnabled(32)) {
                                logger.logDebug("Dropping ACK - cannot find a transaction or dialog");
                            }
                            ackTransaction = sipStack.findTransactionPendingAck(sipRequest);
                            if (ackTransaction != null) {
                                if (logger.isLoggingEnabled(32)) {
                                    logger.logDebug("Found Tx pending ACK");
                                }
                                try {
                                    ackTransaction.setAckSeen();
                                    sipStack.removeTransaction(ackTransaction);
                                    sipStack.removeTransactionPendingAck(ackTransaction);
                                } catch (Exception ex2) {
                                    if (logger.isLoggingEnabled()) {
                                        logger.logError("Problem terminating transaction", ex2);
                                    }
                                }
                            }
                            if (!sipStack.isDeliverRetransmittedAckToListener()) {
                                return;
                            }
                            if (!(ackTransaction == null || sipStack.isNon2XXAckPassedToListener())) {
                                return;
                            }
                        } else {
                            if (logger.isLoggingEnabled(32)) {
                                logger.logDebug("Dialog exists with loose dialog validation " + sipRequest.getFirstLine() + " isServerTransaction = " + true + " dialog = " + dialog.getDialogId());
                            }
                            st = sipStack.getRetransmissionAlertTransaction(dialogId);
                            if (st != null && st.isRetransmissionAlertEnabled()) {
                                st.disableRetransmissionAlerts();
                            }
                            ackTransaction = sipStack.findTransactionPendingAck(sipRequest);
                            if (ackTransaction != null) {
                                if (logger.isLoggingEnabled(32)) {
                                    logger.logDebug("Found Tx pending ACK");
                                }
                                try {
                                    ackTransaction.setAckSeen();
                                    sipStack.removeTransaction(ackTransaction);
                                    sipStack.removeTransactionPendingAck(ackTransaction);
                                } catch (Exception ex22) {
                                    if (logger.isLoggingEnabled()) {
                                        logger.logError("Problem terminating transaction", ex22);
                                    }
                                }
                            }
                        }
                    } else if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("Processing ACK for INVITE Tx ");
                    }
                } else if (sipRequestMethod.equals(Request.PRACK)) {
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("Processing PRACK for dialog " + dialog);
                    }
                    if (dialog == null && sipProvider.isAutomaticDialogSupportEnabled()) {
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("Dialog does not exist " + sipRequest.getFirstLine() + " isServerTransaction = " + true);
                        }
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("Sending 481 for PRACK - automatic dialog support is enabled -- cant find dialog!");
                        }
                        try {
                            sipProvider.sendResponse(sipRequest.createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST));
                        } catch (SipException e2) {
                            logger.logError("error sending response", e2);
                        }
                        if (transaction != null) {
                            sipStack.removeTransaction(transaction);
                            transaction.releaseSem();
                            return;
                        }
                        return;
                    } else if (dialog != null) {
                        if (dialog.handlePrack(sipRequest)) {
                            try {
                                sipStack.addTransaction((SIPServerTransaction) transaction);
                                dialog.addTransaction(transaction);
                                dialog.addRoute(sipRequest);
                                transaction.setDialog(dialog, dialogId);
                            } catch (Exception ex222) {
                                InternalErrorHandler.handleException(ex222);
                            }
                        } else {
                            if (logger.isLoggingEnabled(32)) {
                                logger.logDebug("Dropping out of sequence PRACK ");
                            }
                            if (transaction != null) {
                                sipStack.removeTransaction(transaction);
                                transaction.releaseSem();
                                return;
                            }
                            return;
                        }
                    } else if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("Processing PRACK without a DIALOG -- this must be a proxy element");
                    }
                } else if (sipRequestMethod.equals("BYE")) {
                    if (dialog != null && !dialog.isRequestConsumable(sipRequest)) {
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("Dropping out of sequence BYE " + dialog.getRemoteSeqNumber() + Separators.SP + sipRequest.getCSeq().getSeqNumber());
                        }
                        if (dialog.getRemoteSeqNumber() >= sipRequest.getCSeq().getSeqNumber() && transaction.getInternalState() == 1) {
                            sendServerInternalErrorResponse(sipRequest, transaction);
                        }
                        if (transaction != null) {
                            sipStack.removeTransaction(transaction);
                            return;
                        }
                        return;
                    } else if (dialog == null && sipProvider.isAutomaticDialogSupportEnabled()) {
                        response = sipRequest.createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
                        response.setReasonPhrase("Dialog Not Found");
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("dropping request -- automatic dialog support enabled and dialog does not exist!");
                        }
                        try {
                            transaction.sendResponse((Response) response);
                        } catch (SipException ex2222) {
                            logger.logError("Error in sending response", ex2222);
                        }
                        if (transaction != null) {
                            sipStack.removeTransaction(transaction);
                            transaction.releaseSem();
                            return;
                        }
                        return;
                    } else {
                        if (!(transaction == null || dialog == null)) {
                            try {
                                if (sipProvider == dialog.getSipProvider()) {
                                    sipStack.addTransaction((SIPServerTransaction) transaction);
                                    dialog.addTransaction(transaction);
                                    transaction.setDialog(dialog, dialogId);
                                }
                            } catch (IOException ex22222) {
                                InternalErrorHandler.handleException(ex22222);
                            }
                        }
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("BYE Tx = " + transaction + " isMapped =" + transaction.isTransactionMapped());
                        }
                    }
                } else if (sipRequestMethod.equals(Request.CANCEL)) {
                    st = (SIPServerTransaction) sipStack.findCancelTransaction(sipRequest, true);
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("Got a CANCEL, InviteServerTx = " + st + " cancel Server Tx ID = " + transaction + " isMapped = " + transaction.isTransactionMapped());
                    }
                    if (sipRequest.getMethod().equals(Request.CANCEL)) {
                        if (st != null && st.getInternalState() == 5) {
                            if (logger.isLoggingEnabled(32)) {
                                logger.logDebug("Too late to cancel Transaction");
                            }
                            try {
                                transaction.sendResponse((Response) sipRequest.createResponse(Response.OK));
                                return;
                            } catch (Exception ex222222) {
                                if (ex222222.getCause() != null && (ex222222.getCause() instanceof IOException)) {
                                    st.raiseIOExceptionEvent();
                                    return;
                                }
                                return;
                            }
                        } else if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("Cancel transaction = " + st);
                        }
                    }
                    if (transaction != null && st != null && st.getDialog() != null) {
                        transaction.setDialog((SIPDialog) st.getDialog(), dialogId);
                        dialog = (SIPDialog) st.getDialog();
                    } else if (st == null && sipProvider.isAutomaticDialogSupportEnabled() && transaction != null) {
                        response = sipRequest.createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("dropping request -- automatic dialog support enabled and INVITE ST does not exist!");
                        }
                        try {
                            sipProvider.sendResponse(response);
                        } catch (SipException ex2222222) {
                            InternalErrorHandler.handleException(ex2222222);
                        }
                        if (transaction != null) {
                            sipStack.removeTransaction(transaction);
                            transaction.releaseSem();
                            return;
                        }
                        return;
                    }
                    if (!(st == null || transaction == null)) {
                        try {
                            sipStack.addTransaction((SIPServerTransaction) transaction);
                            transaction.setPassToListener();
                            transaction.setInviteTransaction(st);
                            st.acquireSem();
                        } catch (Exception ex22222222) {
                            InternalErrorHandler.handleException(ex22222222);
                        }
                    }
                } else if (sipRequestMethod.equals("INVITE")) {
                    if (dialog == null) {
                        lastTransaction = null;
                    } else {
                        lastTransaction = dialog.getInviteTransaction();
                    }
                    if (dialog == null || transaction == null || lastTransaction == null || sipRequest.getCSeq().getSeqNumber() <= dialog.getRemoteSeqNumber() || !(lastTransaction instanceof SIPServerTransaction) || !sipProvider.isDialogErrorsAutomaticallyHandled() || !dialog.isSequnceNumberValidation() || !lastTransaction.isInviteTransaction() || lastTransaction.getInternalState() == 3 || lastTransaction.getInternalState() == 5 || lastTransaction.getInternalState() == 4) {
                        lastTransaction = dialog == null ? null : dialog.getLastTransaction();
                        if (dialog != null && sipProvider.isDialogErrorsAutomaticallyHandled() && lastTransaction != null && lastTransaction.isInviteTransaction() && (lastTransaction instanceof ClientTransaction) && lastTransaction.getLastResponse() != null && lastTransaction.getLastResponse().getStatusCode() == 200 && !dialog.isAckSent(lastTransaction.getLastResponse().getCSeq().getSeqNumber())) {
                            if (logger.isLoggingEnabled(32)) {
                                logger.logDebug("Sending 491 response for client Dialog ACK not sent.");
                            }
                            sendRequestPendingResponse(sipRequest, transaction);
                            return;
                        } else if (dialog != null && lastTransaction != null && sipProvider.isDialogErrorsAutomaticallyHandled() && lastTransaction.isInviteTransaction() && (lastTransaction instanceof ServerTransaction) && (lastTransaction.getInternalState() == 2 || lastTransaction.getInternalState() == 1)) {
                            if (logger.isLoggingEnabled(32)) {
                                logger.logDebug("Sending 491 response. Last transaction is in PROCEEDING state.");
                                logger.logDebug("last Transaction state = " + lastTransaction + " state " + lastTransaction.getState());
                            }
                            sendRequestPendingResponse(sipRequest, transaction);
                            return;
                        }
                    }
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("Sending 500 response for out of sequence message");
                    }
                    sendServerInternalErrorResponse(sipRequest, transaction);
                    return;
                }
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("CHECK FOR OUT OF SEQ MESSAGE " + dialog + " transaction " + transaction);
                }
                if (!(dialog == null || transaction == null || sipRequestMethod.equals("BYE") || sipRequestMethod.equals(Request.CANCEL) || sipRequestMethod.equals("ACK") || sipRequestMethod.equals(Request.PRACK))) {
                    if (dialog.isRequestConsumable(sipRequest)) {
                        try {
                            if (sipProvider == dialog.getSipProvider()) {
                                sipStack.addTransaction((SIPServerTransaction) transaction);
                                if (dialog.addTransaction(transaction)) {
                                    dialog.addRoute(sipRequest);
                                    transaction.setDialog(dialog, dialogId);
                                } else {
                                    return;
                                }
                            }
                        } catch (IOException e3) {
                            transaction.raiseIOExceptionEvent();
                            sipStack.removeTransaction(transaction);
                            return;
                        }
                    }
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("Dropping out of sequence message " + dialog.getRemoteSeqNumber() + Separators.SP + sipRequest.getCSeq());
                    }
                    if (dialog.getRemoteSeqNumber() <= sipRequest.getCSeq().getSeqNumber() || !sipProvider.isDialogErrorsAutomaticallyHandled()) {
                        try {
                            transaction.terminate();
                            return;
                        } catch (ObjectInUseException e4) {
                            if (logger.isLoggingEnabled()) {
                                logger.logError("Unexpected exception", e4);
                                return;
                            }
                            return;
                        }
                    }
                    sendServerInternalErrorResponse(sipRequest, transaction);
                    return;
                }
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug(sipRequest.getMethod() + " transaction.isMapped = " + transaction.isTransactionMapped());
                }
                EventObject requestEventExt;
                if (dialog == null && sipRequestMethod.equals("NOTIFY")) {
                    SIPTransaction pendingSubscribeClientTx = sipStack.findSubscribeTransaction(sipRequest, this.listeningPoint);
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("PROCESSING NOTIFY  DIALOG == null " + pendingSubscribeClientTx);
                    }
                    if (sipProvider.isAutomaticDialogSupportEnabled() && pendingSubscribeClientTx == null && !sipStack.isDeliverUnsolicitedNotify()) {
                        try {
                            if (logger.isLoggingEnabled(32)) {
                                logger.logDebug("Could not find Subscription for Notify Tx.");
                            }
                            Response errorResponse = sipRequest.createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
                            errorResponse.setReasonPhrase("Subscription does not exist");
                            sipProvider.sendResponse(errorResponse);
                            return;
                        } catch (Exception ex222222222) {
                            logger.logError("Exception while sending error response statelessly", ex222222222);
                            return;
                        }
                    } else if (pendingSubscribeClientTx != null) {
                        transaction.setPendingSubscribe(pendingSubscribeClientTx);
                        SIPDialog subscriptionDialog = pendingSubscribeClientTx.getDefaultDialog();
                        if (subscriptionDialog == null || subscriptionDialog.getDialogId() == null || !subscriptionDialog.getDialogId().equals(dialogId)) {
                            if (subscriptionDialog == null || subscriptionDialog.getDialogId() != null) {
                                subscriptionDialog = pendingSubscribeClientTx.getDialog(dialogId);
                            } else {
                                subscriptionDialog.setDialogId(dialogId);
                            }
                            if (logger.isLoggingEnabled(32)) {
                                logger.logDebug("PROCESSING NOTIFY Subscribe DIALOG " + subscriptionDialog);
                            }
                            if (subscriptionDialog == null && ((sipProvider.isAutomaticDialogSupportEnabled() || pendingSubscribeClientTx.getDefaultDialog() != null) && sipStack.isEventForked(((Event) sipRequest.getHeader("Event")).getEventType()))) {
                                subscriptionDialog = SIPDialog.createFromNOTIFY(pendingSubscribeClientTx, transaction);
                            }
                            if (subscriptionDialog != null) {
                                transaction.setDialog(subscriptionDialog, dialogId);
                                if (subscriptionDialog.getState() != DialogState.CONFIRMED) {
                                    subscriptionDialog.setPendingRouteUpdateOn202Response(sipRequest);
                                }
                                subscriptionDialog.setState(DialogState.CONFIRMED.getValue());
                                sipStack.putDialog(subscriptionDialog);
                                pendingSubscribeClientTx.setDialog(subscriptionDialog, dialogId);
                                if (!transaction.isTransactionMapped()) {
                                    this.sipStack.mapTransaction(transaction);
                                    transaction.setPassToListener();
                                    try {
                                        this.sipStack.addTransaction((SIPServerTransaction) transaction);
                                    } catch (Exception e5) {
                                    }
                                }
                            }
                        } else {
                            transaction.setDialog(subscriptionDialog, dialogId);
                            dialog = subscriptionDialog;
                            if (!transaction.isTransactionMapped()) {
                                this.sipStack.mapTransaction(transaction);
                                transaction.setPassToListener();
                                try {
                                    this.sipStack.addTransaction((SIPServerTransaction) transaction);
                                } catch (Exception e6) {
                                }
                            }
                            sipStack.putDialog(subscriptionDialog);
                            if (pendingSubscribeClientTx != null) {
                                subscriptionDialog.addTransaction(pendingSubscribeClientTx);
                                pendingSubscribeClientTx.setDialog(subscriptionDialog, dialogId);
                            }
                        }
                        if (transaction == null || !transaction.isTransactionMapped()) {
                            requestEventExt = new RequestEventExt(sipProvider, null, subscriptionDialog, sipRequest);
                        } else {
                            requestEventExt = new RequestEventExt(sipProvider, transaction, subscriptionDialog, sipRequest);
                        }
                    } else {
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("could not find subscribe tx");
                        }
                        requestEventExt = new RequestEventExt(sipProvider, null, null, sipRequest);
                    }
                } else if (transaction == null || !transaction.isTransactionMapped()) {
                    requestEventExt = new RequestEventExt(sipProvider, null, dialog, sipRequest);
                } else {
                    requestEventExt = new RequestEventExt(sipProvider, transaction, dialog, sipRequest);
                }
                ((RequestEventExt) sipEvent).setRemoteIpAddress(sipRequest.getRemoteAddress().getHostAddress());
                ((RequestEventExt) sipEvent).setRemotePort(sipRequest.getRemotePort());
                sipProvider.handleEvent(sipEvent, transaction);
            } else if (logger.isLoggingEnabled(32)) {
                logger.logDebug("No provider - dropping !!");
            }
        } else if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Dropping message: No listening point registered!");
        }
    }

    public void processResponse(SIPResponse response, MessageChannel incomingMessageChannel, SIPDialog dialog) {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("PROCESSING INCOMING RESPONSE" + response.encodeMessage(new StringBuilder()));
        }
        if (this.listeningPoint == null) {
            if (logger.isLoggingEnabled()) {
                logger.logError("Dropping message: No listening point registered!");
            }
        } else if (!this.sipStack.checkBranchId() || Utils.getInstance().responseBelongsToUs(response)) {
            SipProviderImpl sipProvider = this.listeningPoint.getProvider();
            if (sipProvider == null) {
                if (logger.isLoggingEnabled()) {
                    logger.logError("Dropping message:  no provider");
                }
            } else if (sipProvider.getSipListener() != null) {
                SIPClientTransaction transaction = this.transactionChannel;
                SIPTransactionStack sipStackImpl = sipProvider.sipStack;
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("Transaction = " + transaction);
                }
                SIPClientTransaction forked;
                if (transaction == null) {
                    if (dialog != null) {
                        if (response.getStatusCode() / 100 != 2) {
                            if (logger.isLoggingEnabled(32)) {
                                logger.logDebug("Response is not a final response and dialog is found for response -- dropping response!");
                                return;
                            }
                            return;
                        } else if (dialog.getState() != DialogState.TERMINATED) {
                            boolean ackAlreadySent = false;
                            if (dialog.isAckSeen() && dialog.getLastAckSent() != null && dialog.getLastAckSent().getCSeq().getSeqNumber() == response.getCSeq().getSeqNumber()) {
                                ackAlreadySent = true;
                            }
                            if (ackAlreadySent && response.getCSeq().getMethod().equals(dialog.getMethod())) {
                                try {
                                    if (logger.isLoggingEnabled(32)) {
                                        logger.logDebug("Retransmission of OK detected: Resending last ACK");
                                    }
                                    dialog.resendAck();
                                    return;
                                } catch (SipException ex) {
                                    logger.logError("could not resend ack", ex);
                                }
                            }
                        } else if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("Dialog is terminated -- dropping response!");
                            return;
                        } else {
                            return;
                        }
                    }
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("could not find tx, handling statelessly Dialog =  " + dialog);
                    }
                    ResponseEventExt sipEvent = new ResponseEventExt(sipProvider, transaction, dialog, response);
                    if (this.sipStack.getMaxForkTime() != 0 && SIPTransactionStack.isDialogCreated(response.getCSeqHeader().getMethod())) {
                        forked = this.sipStack.getForkedTransaction(response.getForkId());
                        if (!(dialog == null || forked == null)) {
                            dialog.checkRetransmissionForForking(response);
                            if (!(forked.getDefaultDialog() == null || dialog.equals(forked.getDefaultDialog()))) {
                                if (logger.isLoggingEnabled(32)) {
                                    logger.logDebug("forked dialog " + dialog + " original tx " + forked + " original dialog " + forked.getDefaultDialog());
                                }
                                sipEvent.setOriginalTransaction(forked);
                                sipEvent.setForkedResponse(true);
                            }
                        }
                    }
                    sipEvent.setRetransmission(response.isRetransmission());
                    sipEvent.setRemoteIpAddress(response.getRemoteAddress().getHostAddress());
                    sipEvent.setRemotePort(response.getRemotePort());
                    sipProvider.handleEvent(sipEvent, transaction);
                    return;
                }
                ResponseEventExt responseEvent = new ResponseEventExt(sipProvider, transaction, dialog, response);
                if (this.sipStack.getMaxForkTime() != 0 && SIPTransactionStack.isDialogCreated(response.getCSeqHeader().getMethod())) {
                    forked = this.sipStack.getForkedTransaction(response.getForkId());
                    if (!(dialog == null || forked == null)) {
                        dialog.checkRetransmissionForForking(response);
                        if (!(forked.getDefaultDialog() == null || dialog.equals(forked.getDefaultDialog()))) {
                            if (logger.isLoggingEnabled(32)) {
                                logger.logDebug("forked dialog " + dialog + " original tx " + forked + " original dialog " + forked.getDefaultDialog());
                            }
                            responseEvent.setOriginalTransaction(forked);
                            responseEvent.setForkedResponse(true);
                        }
                    }
                }
                if (!(dialog == null || response.getStatusCode() == 100)) {
                    dialog.setLastResponse(transaction, response);
                    transaction.setDialog(dialog, dialog.getDialogId());
                }
                responseEvent.setRetransmission(response.isRetransmission());
                responseEvent.setRemoteIpAddress(response.getRemoteAddress().getHostAddress());
                responseEvent.setRemotePort(response.getRemotePort());
                sipProvider.handleEvent(responseEvent, transaction);
            } else if (logger.isLoggingEnabled()) {
                logger.logError("No listener -- dropping response!");
            }
        } else if (logger.isLoggingEnabled()) {
            logger.logError("Dropping response - topmost VIA header does not originate from this stack");
        }
    }

    public String getProcessingInfo() {
        return null;
    }

    public void processResponse(SIPResponse sipResponse, MessageChannel incomingChannel) {
        String dialogID = sipResponse.getDialogId(false);
        SIPDialog sipDialog = this.sipStack.getDialog(dialogID);
        String method = sipResponse.getCSeq().getMethod();
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("PROCESSING INCOMING RESPONSE: " + sipResponse.encodeMessage(new StringBuilder()));
        }
        if (!this.sipStack.checkBranchId() || Utils.getInstance().responseBelongsToUs(sipResponse)) {
            if (this.listeningPoint != null) {
                SipProviderImpl sipProvider = this.listeningPoint.getProvider();
                if (sipProvider == null) {
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("Dropping message:  no provider");
                    }
                } else if (sipProvider.getSipListener() != null) {
                    ClientTransactionExt originalTx;
                    SIPClientTransaction transaction = this.transactionChannel;
                    if (sipDialog == null && transaction != null) {
                        sipDialog = transaction.getDialog(dialogID);
                        if (sipDialog != null && sipDialog.getState() == DialogState.TERMINATED) {
                            sipDialog = null;
                        }
                    }
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("Transaction = " + transaction + " sipDialog = " + sipDialog);
                    }
                    if (this.transactionChannel != null) {
                        String originalFrom = ((SIPRequest) this.transactionChannel.getRequest()).getFromTag();
                        if (((originalFrom == null ? 1 : 0) ^ (sipResponse.getFrom().getTag() == null ? 1 : 0)) != 0) {
                            if (logger.isLoggingEnabled(32)) {
                                logger.logDebug("From tag mismatch -- dropping response");
                                return;
                            }
                            return;
                        } else if (!(originalFrom == null || originalFrom.equalsIgnoreCase(sipResponse.getFrom().getTag()))) {
                            if (logger.isLoggingEnabled(32)) {
                                logger.logDebug("From tag mismatch -- dropping response");
                                return;
                            }
                            return;
                        }
                    }
                    boolean createDialog = false;
                    if (SIPTransactionStack.isDialogCreated(method) && sipResponse.getStatusCode() != 100 && sipResponse.getFrom().getTag() != null && sipResponse.getTo().getTag() != null && sipDialog == null) {
                        if (sipProvider.isAutomaticDialogSupportEnabled()) {
                            createDialog = true;
                        } else {
                            originalTx = this.sipStack.getForkedTransaction(sipResponse.getForkId());
                            if (!(originalTx == null || originalTx.getDefaultDialog() == null)) {
                                if (logger.isLoggingEnabled(32)) {
                                    logger.logDebug("Need to create dialog for response = " + sipResponse);
                                }
                                createDialog = true;
                            }
                        }
                        if (createDialog) {
                            if (this.transactionChannel == null) {
                                if (logger.isLoggingEnabled(32)) {
                                    logger.logDebug("Creating dialog for forked response " + sipResponse);
                                }
                                sipDialog = this.sipStack.createDialog(sipProvider, sipResponse);
                            } else if (sipDialog == null) {
                                if (logger.isLoggingEnabled(32)) {
                                    logger.logDebug("Creating dialog for forked response " + sipResponse);
                                }
                                sipDialog = this.sipStack.createDialog((SIPClientTransaction) this.transactionChannel, sipResponse);
                                this.transactionChannel.setDialog(sipDialog, sipResponse.getDialogId(false));
                            }
                        }
                    } else if (!(sipDialog == null || transaction != null || sipDialog.getState() == DialogState.TERMINATED)) {
                        if (sipResponse.getStatusCode() / 100 != 2) {
                            if (logger.isLoggingEnabled(32)) {
                                logger.logDebug("status code != 200 ; statusCode = " + sipResponse.getStatusCode());
                            }
                        } else if (sipDialog.getState() == DialogState.TERMINATED) {
                            if (logger.isLoggingEnabled(32)) {
                                logger.logDebug("Dialog is terminated -- dropping response!");
                            }
                            if (sipResponse.getStatusCode() / 100 == 2 && sipResponse.getCSeq().getMethod().equals("INVITE")) {
                                try {
                                    sipDialog.sendAck(sipDialog.createAck(sipResponse.getCSeq().getSeqNumber()));
                                    return;
                                } catch (Exception ex) {
                                    logger.logError("Error creating ack", ex);
                                    return;
                                }
                            }
                            return;
                        } else {
                            boolean ackAlreadySent = false;
                            if (sipDialog.getLastAckSent() != null && sipDialog.getLastAckSent().getCSeq().getSeqNumber() == sipResponse.getCSeq().getSeqNumber() && sipResponse.getDialogId(false).equals(sipDialog.getLastAckSent().getDialogId(false))) {
                                ackAlreadySent = true;
                            }
                            if (ackAlreadySent && sipResponse.getCSeq().getMethod().equals(sipDialog.getMethod())) {
                                try {
                                    if (logger.isLoggingEnabled(32)) {
                                        logger.logDebug("resending ACK");
                                    }
                                    sipDialog.resendAck();
                                    return;
                                } catch (SipException e) {
                                }
                            }
                        }
                    }
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("sending response to TU for processing ");
                    }
                    ResponseEventExt responseEvent = new ResponseEventExt(sipProvider, transaction, sipDialog, sipResponse);
                    responseEvent.setRemoteIpAddress(sipResponse.getRemoteAddress().getHostAddress());
                    responseEvent.setRemotePort(sipResponse.getRemotePort());
                    if (this.sipStack.getMaxForkTime() != 0 && SIPTransactionStack.isDialogCreated(sipResponse.getCSeqHeader().getMethod())) {
                        originalTx = this.sipStack.getForkedTransaction(sipResponse.getForkId());
                        if (!(sipDialog == null || originalTx == null)) {
                            sipDialog.checkRetransmissionForForking(sipResponse);
                            if (!(originalTx.getDefaultDialog() == null || sipDialog.equals(originalTx.getDefaultDialog()))) {
                                if (logger.isLoggingEnabled(32)) {
                                    logger.logDebug("forked dialog " + sipDialog + " original tx " + originalTx + " original dialog " + originalTx.getDefaultDialog());
                                }
                                responseEvent.setOriginalTransaction(originalTx);
                                responseEvent.setForkedResponse(true);
                            }
                        }
                    }
                    if (!(sipDialog == null || sipResponse.getStatusCode() == 100 || sipResponse.getTo().getTag() == null)) {
                        sipDialog.setLastResponse(transaction, sipResponse);
                    }
                    responseEvent.setRetransmission(sipResponse.isRetransmission());
                    responseEvent.setRemoteIpAddress(sipResponse.getRemoteAddress().getHostAddress());
                    sipProvider.handleEvent(responseEvent, transaction);
                } else if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("Dropping message:  no sipListener registered!");
                }
            } else if (logger.isLoggingEnabled(32)) {
                logger.logDebug("Dropping message: No listening point registered!");
            }
        } else if (logger.isLoggingEnabled()) {
            logger.logError("Detected stray response -- dropping");
        }
    }
}
