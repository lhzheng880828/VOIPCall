package net.java.sip.communicator.impl.protocol.sip;

import java.text.ParseException;
import java.util.TimerTask;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.javax.sip.stack.SIPServerTransaction;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.DialogState;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.ResponseEvent;
import org.jitsi.javax.sip.ServerTransaction;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.SipProvider;
import org.jitsi.javax.sip.TimeoutEvent;
import org.jitsi.javax.sip.TransactionUnavailableException;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.header.CSeqHeader;
import org.jitsi.javax.sip.header.CallIdHeader;
import org.jitsi.javax.sip.header.ContentTypeHeader;
import org.jitsi.javax.sip.header.EventHeader;
import org.jitsi.javax.sip.header.ExpiresHeader;
import org.jitsi.javax.sip.header.FromHeader;
import org.jitsi.javax.sip.header.HeaderFactory;
import org.jitsi.javax.sip.header.MinExpiresHeader;
import org.jitsi.javax.sip.header.SubscriptionStateHeader;
import org.jitsi.javax.sip.header.ToHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;

public class EventPackageSubscriber extends EventPackageSupport {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(EventPackageSubscriber.class);
    private final SipMessageFactory messageFactory;
    private final int refreshMargin;

    public static abstract class Subscription extends Subscription {
        public abstract void processActiveRequest(RequestEvent requestEvent, byte[] bArr);

        public abstract void processFailureResponse(ResponseEvent responseEvent, int i);

        public abstract void processSuccessResponse(ResponseEvent responseEvent, int i);

        public abstract void processTerminatedRequest(RequestEvent requestEvent, String str);

        public Subscription(Address toAddress) {
            this(toAddress, null);
        }

        public Subscription(Address toAddress, String eventId) {
            super(toAddress, eventId);
        }
    }

    private class SubscriptionRefreshTask extends TimerTask {
        private final Subscription subscription;

        public SubscriptionRefreshTask(Subscription subscription) {
            this.subscription = subscription;
        }

        public void run() {
            Dialog dialog = this.subscription.getDialog();
            if (dialog == null) {
                EventPackageSubscriber.logger.warn("null dialog associated with " + this.subscription + ", can't refresh the subscription");
                return;
            }
            try {
                try {
                    dialog.sendRequest(EventPackageSubscriber.this.createSubscription(this.subscription, dialog, EventPackageSubscriber.this.subscriptionDuration));
                } catch (SipException e) {
                    EventPackageSubscriber.logger.error("Can't send the request", e);
                }
            } catch (OperationFailedException e2) {
                EventPackageSubscriber.logger.error("Failed to create subscriptionTransaction.", e2);
            }
        }
    }

    public EventPackageSubscriber(ProtocolProviderServiceSipImpl protocolProvider, String eventPackage, int subscriptionDuration, String contentSubType, TimerScheduler timer, int refreshMargin) {
        super(protocolProvider, eventPackage, subscriptionDuration, contentSubType, timer);
        this.refreshMargin = refreshMargin;
        this.messageFactory = protocolProvider.getMessageFactory();
    }

    /* access modifiers changed from: private */
    public ClientTransaction createSubscription(Subscription subscription, Dialog dialog, int expires) throws OperationFailedException {
        Request req = this.messageFactory.createRequest(dialog, "SUBSCRIBE");
        if (dialog.getRemoteTarget() == null) {
            Address toAddress = dialog.getRemoteParty();
        }
        req.setHeader(this.protocolProvider.getMaxForwardsHeader());
        try {
            ClientTransaction transac = this.protocolProvider.getDefaultJainSipProvider().getNewClientTransaction(req);
            populateSubscribeRequest(req, subscription, expires);
            return transac;
        } catch (TransactionUnavailableException ex) {
            logger.error("Failed to create subscriptionTransaction.\nThis is most probably a network connection error.", ex);
            throw new OperationFailedException("Failed to create the subscription transaction", 2);
        }
    }

    private ClientTransaction createSubscription(Subscription subscription, int expires) throws OperationFailedException {
        Address toAddress = subscription.getAddress();
        HeaderFactory headerFactory = this.protocolProvider.getHeaderFactory();
        CallIdHeader callIdHeader = this.protocolProvider.getDefaultJainSipProvider().getNewCallId();
        try {
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1, "SUBSCRIBE");
            try {
                FromHeader fromHeader = headerFactory.createFromHeader(this.protocolProvider.getOurSipAddress(toAddress), SipMessageFactory.generateLocalTag());
                ToHeader toHeader = headerFactory.createToHeader(toAddress, null);
                try {
                    Request req = this.protocolProvider.getMessageFactory().createRequest(toHeader.getAddress().getURI(), "SUBSCRIBE", callIdHeader, cSeqHeader, fromHeader, toHeader, this.protocolProvider.getLocalViaHeaders(toAddress), this.protocolProvider.getMaxForwardsHeader());
                    populateSubscribeRequest(req, subscription, expires);
                    try {
                        return this.protocolProvider.getDefaultJainSipProvider().getNewClientTransaction(req);
                    } catch (TransactionUnavailableException ex) {
                        logger.error("Failed to create subscribe transaction.\nThis is most probably a network connection error.", ex);
                        throw new OperationFailedException("Failed to create the subscription transaction", 2);
                    }
                } catch (ParseException ex2) {
                    logger.error("Failed to create message Request!", ex2);
                    throw new OperationFailedException("Failed to create message Request!", 4, ex2);
                }
            } catch (ParseException ex22) {
                logger.error("An unexpected error occurred whileconstructing the FromHeader or ToHeader", ex22);
                throw new OperationFailedException("An unexpected error occurred whileconstructing the FromHeader or ToHeader", 4, ex22);
            }
        } catch (InvalidArgumentException ex3) {
            logger.error("An unexpected error occurred whileconstructing the CSeqHeader", ex3);
            throw new OperationFailedException("An unexpected error occurred whileconstructing the CSeqHeader", 4, ex3);
        } catch (ParseException ex222) {
            logger.error("An unexpected error occurred whileconstructing the CSeqHeader", ex222);
            throw new OperationFailedException("An unexpected error occurred whileconstructing the CSeqHeader", 4, ex222);
        }
    }

    /* access modifiers changed from: protected */
    public Subscription getSubscription(Address toAddress, String eventId) {
        return (Subscription) super.getSubscription(toAddress, eventId);
    }

    /* access modifiers changed from: protected */
    public Subscription getSubscription(String callId) {
        return (Subscription) super.getSubscription(callId);
    }

    public void poll(Subscription subscription) throws OperationFailedException {
        if (getSubscription(subscription.getAddress(), subscription.getEventId()) == null) {
            subscribe(subscription);
        }
    }

    private void populateSubscribeRequest(Request req, Subscription subscription, int expires) throws OperationFailedException {
        HeaderFactory headerFactory = this.protocolProvider.getHeaderFactory();
        try {
            EventHeader evHeader = headerFactory.createEventHeader(this.eventPackage);
            String eventId = subscription.getEventId();
            if (eventId != null) {
                evHeader.setEventId(eventId);
            }
            req.setHeader(evHeader);
            try {
                req.setHeader(headerFactory.createAcceptHeader(SIPServerTransaction.CONTENT_TYPE_APPLICATION, this.contentSubType));
                try {
                    req.setHeader(headerFactory.createExpiresHeader(expires));
                } catch (InvalidArgumentException e) {
                    logger.error("Invalid expires value: " + expires, e);
                    throw new OperationFailedException("An unexpected error occurred whileconstructing the ExpiresHeader", 4, e);
                }
            } catch (ParseException e2) {
                logger.error("wrong accept header", e2);
                throw new OperationFailedException("An unexpected error occurred whileconstructing the AcceptHeader", 4, e2);
            }
        } catch (ParseException e22) {
            logger.error("An unexpected error occurred whileconstructing the EventHeader", e22);
            throw new OperationFailedException("An unexpected error occurred whileconstructing the EventHeader", 4, e22);
        }
    }

    public boolean processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        EventHeader eventHeader = (EventHeader) request.getHeader("Event");
        if (eventHeader == null || !this.eventPackage.equalsIgnoreCase(eventHeader.getEventType())) {
            return false;
        }
        if (!"NOTIFY".equals(request.getMethod())) {
            return false;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("notify received");
        }
        SubscriptionStateHeader sstateHeader = (SubscriptionStateHeader) request.getHeader("Subscription-State");
        if (sstateHeader == null) {
            logger.error("no subscription state in this request");
            return false;
        }
        String sstate = sstateHeader.getState();
        ServerTransaction serverTransaction = EventPackageSupport.getOrCreateServerTransaction(requestEvent);
        String callId = ((CallIdHeader) request.getHeader("Call-ID")).getCallId();
        Subscription subscription = getSubscription(callId);
        if (subscription != null || SubscriptionStateHeader.TERMINATED.equalsIgnoreCase(sstate)) {
            ContentTypeHeader ctheader = (ContentTypeHeader) request.getHeader("Content-Type");
            if (!(ctheader == null || ctheader.getContentSubType().equalsIgnoreCase(this.contentSubType))) {
                try {
                    Response response = this.protocolProvider.getMessageFactory().createResponse(Response.UNSUPPORTED_MEDIA_TYPE, request);
                    try {
                        response.setHeader(this.protocolProvider.getHeaderFactory().createAcceptHeader(SIPServerTransaction.CONTENT_TYPE_APPLICATION, this.contentSubType));
                        try {
                            serverTransaction.sendResponse(response);
                        } catch (SipException e) {
                            logger.error("failed to send the response", e);
                        } catch (InvalidArgumentException e2) {
                            logger.error("invalid argument provided while trying to send the response", e2);
                        }
                    } catch (ParseException e3) {
                        logger.error("failed to create the accept header", e3);
                        return false;
                    }
                } catch (ParseException e32) {
                    logger.error("failed to create the OK response", e32);
                    return false;
                }
            }
            if (SubscriptionStateHeader.TERMINATED.equalsIgnoreCase(sstate) && subscription != null) {
                removeSubscription(callId, (Subscription) subscription);
                subscription.processTerminatedRequest(requestEvent, sstateHeader.getReasonCode());
            }
            try {
                try {
                    serverTransaction.sendResponse(this.protocolProvider.getMessageFactory().createResponse(Response.OK, request));
                } catch (SipException e4) {
                    logger.error("failed to send the response", e4);
                } catch (InvalidArgumentException e22) {
                    logger.error("invalid argument provided while trying to send the response", e22);
                }
                if (subscription != null) {
                    subscription.processActiveRequest(requestEvent, request.getRawContent());
                }
                return true;
            } catch (ParseException e322) {
                logger.error("failed to create the OK response", e322);
                return false;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("subscription not found for callId " + callId);
        }
        try {
            try {
                serverTransaction.sendResponse(this.protocolProvider.getMessageFactory().createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST, request));
            } catch (SipException e42) {
                logger.error("failed to send the response", e42);
            } catch (InvalidArgumentException e222) {
                logger.error("invalid argument provided while trying to send the response", e222);
            }
            return true;
        } catch (ParseException e3222) {
            logger.error("failed to create the 481 response", e3222);
            return false;
        }
    }

    public boolean processResponse(ResponseEvent responseEvent) {
        Response response = responseEvent.getResponse();
        CSeqHeader cseqHeader = (CSeqHeader) response.getHeader("CSeq");
        if (cseqHeader == null) {
            logger.error("An incoming response did not contain a CSeq header");
            return false;
        } else if (!"SUBSCRIBE".equals(cseqHeader.getMethod())) {
            return false;
        } else {
            ClientTransaction clientTransaction = responseEvent.getClientTransaction();
            Request request = clientTransaction.getRequest();
            if (request != null) {
                EventHeader eventHeader = (EventHeader) request.getHeader("Event");
                if (eventHeader == null || !this.eventPackage.equalsIgnoreCase(eventHeader.getEventType())) {
                    return false;
                }
            }
            String callId = ((CallIdHeader) response.getHeader("Call-ID")).getCallId();
            Subscription subscription = getSubscription(callId);
            ExpiresHeader expHeader = response.getExpires();
            int statusCode = response.getStatusCode();
            SipProvider sourceProvider = (SipProvider) responseEvent.getSource();
            if ((expHeader == null || expHeader.getExpires() != 0) && subscription != null) {
                if (statusCode < 200 || statusCode >= 300) {
                    if (statusCode < 300 || statusCode >= 400) {
                        if (statusCode >= 400) {
                            if (statusCode == 423) {
                                MinExpiresHeader min = (MinExpiresHeader) response.getHeader("Min-Expires");
                                if (min == null) {
                                    logger.error("no minimal expires value in this 423 response");
                                    return false;
                                }
                                try {
                                    request.getExpires().setExpires(min.getExpires());
                                    try {
                                        try {
                                            this.protocolProvider.getDefaultJainSipProvider().getNewClientTransaction(request).sendRequest();
                                            return true;
                                        } catch (SipException e) {
                                            logger.error("can't send the new request", e);
                                            return false;
                                        }
                                    } catch (TransactionUnavailableException e2) {
                                        logger.error("can't create the client transaction", e2);
                                        return false;
                                    }
                                } catch (InvalidArgumentException e3) {
                                    logger.error("can't set the new expires value", e3);
                                    return false;
                                }
                            } else if (statusCode == 401 || statusCode == 407) {
                                try {
                                    processAuthenticationChallenge(clientTransaction, response, sourceProvider);
                                } catch (OperationFailedException e4) {
                                    logger.error("can't handle the challenge", e4);
                                    removeSubscription(callId, subscription);
                                    subscription.processFailureResponse(responseEvent, statusCode);
                                }
                            } else {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("error received from the network:\n" + response);
                                }
                                removeSubscription(callId, subscription);
                                subscription.processFailureResponse(responseEvent, statusCode);
                            }
                        }
                    } else if (logger.isInfoEnabled()) {
                        logger.info("Response to subscribe to " + subscription.getAddress() + ": " + response.getReasonPhrase());
                    }
                } else if (statusCode == 200 || statusCode == 202) {
                    if (expHeader == null) {
                        logger.error("no Expires header in this response");
                        return false;
                    }
                    SubscriptionRefreshTask refreshTask = new SubscriptionRefreshTask(subscription);
                    subscription.setTimerTask(refreshTask);
                    int refreshDelay = expHeader.getExpires();
                    if (refreshDelay >= this.refreshMargin * 2) {
                        refreshDelay -= this.refreshMargin;
                    }
                    this.timer.schedule(refreshTask, (long) (refreshDelay * 1000));
                    subscription.setDialog(clientTransaction.getDialog());
                    subscription.processSuccessResponse(responseEvent, statusCode);
                }
                return true;
            } else if (statusCode == 401 || statusCode == 407) {
                try {
                    processAuthenticationChallenge(clientTransaction, response, sourceProvider);
                    return true;
                } catch (OperationFailedException e42) {
                    logger.error("can't handle the challenge", e42);
                    return false;
                }
            } else if (statusCode == 200 || statusCode == 202) {
                return false;
            } else {
                return true;
            }
        }
    }

    public boolean processTimeout(TimeoutEvent timeoutEvent) {
        this.protocolProvider.notifyConnectionFailed();
        return true;
    }

    public void subscribe(Subscription subscription) throws OperationFailedException {
        Dialog dialog = subscription.getDialog();
        if (dialog != null && DialogState.TERMINATED.equals(dialog.getState())) {
            dialog = null;
        }
        ClientTransaction subscribeTransaction = null;
        if (dialog == null) {
            try {
                subscribeTransaction = createSubscription(subscription, this.subscriptionDuration);
            } catch (OperationFailedException ex) {
                ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to create the subscription", 4, ex, logger);
            }
        } else {
            subscribeTransaction = createSubscription(subscription, dialog, this.subscriptionDuration);
        }
        String callId = ((CallIdHeader) subscribeTransaction.getRequest().getHeader("Call-ID")).getCallId();
        addSubscription(callId, subscription);
        if (dialog == null) {
            try {
                subscribeTransaction.sendRequest();
                return;
            } catch (SipException ex2) {
                removeSubscription(callId, (Subscription) subscription);
                ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to send the subscription", 2, ex2, logger);
                return;
            }
        }
        dialog.sendRequest(subscribeTransaction);
    }

    public void unsubscribe(Address toAddress, boolean assertSubscribed) throws IllegalArgumentException, OperationFailedException {
        unsubscribe(toAddress, null, assertSubscribed);
    }

    public void unsubscribe(Address toAddress, String eventId, boolean assertSubscribed) throws IllegalArgumentException, OperationFailedException {
        Subscription subscription = getSubscription(toAddress, eventId);
        if (subscription != null) {
            Dialog dialog = subscription.getDialog();
            if (dialog != null) {
                String callId = dialog.getCallId().getCallId();
                try {
                    ClientTransaction subscribeTransaction = createSubscription(subscription, dialog, 0);
                    removeSubscription(callId, (Subscription) subscription);
                    try {
                        dialog.sendRequest(subscribeTransaction);
                    } catch (SipException e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Can't send the request", e);
                        }
                        throw new OperationFailedException("Failed to send the subscription message", 2, e);
                    }
                } catch (OperationFailedException e2) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("failed to create the unsubscription", e2);
                    }
                    throw e2;
                }
            }
        } else if (assertSubscribed) {
            throw new IllegalArgumentException("trying to unregister a not registered contact");
        }
    }
}
