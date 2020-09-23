package net.java.sip.communicator.impl.protocol.sip;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.TimerTask;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.javax.sip.stack.SIPServerTransaction;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.ResponseEvent;
import org.jitsi.javax.sip.ServerTransaction;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.SipProvider;
import org.jitsi.javax.sip.TransactionUnavailableException;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.header.CSeqHeader;
import org.jitsi.javax.sip.header.CallIdHeader;
import org.jitsi.javax.sip.header.ContentTypeHeader;
import org.jitsi.javax.sip.header.EventHeader;
import org.jitsi.javax.sip.header.ExpiresHeader;
import org.jitsi.javax.sip.header.FromHeader;
import org.jitsi.javax.sip.header.Header;
import org.jitsi.javax.sip.header.MaxForwardsHeader;
import org.jitsi.javax.sip.header.SubscriptionStateHeader;
import org.jitsi.javax.sip.header.ViaHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;

public abstract class EventPackageNotifier extends EventPackageSupport {
    private static final int SUBSCRIBE_MIN_EXPIRE = 120;
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(EventPackageNotifier.class);
    private final SipMessageFactory messageFactory;

    public static abstract class Subscription extends Subscription {
        public abstract byte[] createNotifyContent(String str, String str2);

        public Subscription(Address fromAddress, String eventId) {
            super(fromAddress, eventId);
        }
    }

    public interface SubscriptionFilter {
        boolean accept(Subscription subscription);
    }

    private class SubscriptionTimeoutTask extends TimerTask {
        private final Subscription subscription;

        public SubscriptionTimeoutTask(Subscription subscription) {
            this.subscription = subscription;
        }

        public void run() {
            if (this.subscription.getDialog() == null) {
                EventPackageNotifier.logger.warn("null dialog associated with " + this.subscription + ", can't send the closing NOTIFY");
                return;
            }
            try {
                EventPackageNotifier.this.notify(this.subscription, SubscriptionStateHeader.TERMINATED, SubscriptionStateHeader.TIMEOUT);
            } catch (OperationFailedException ofex) {
                EventPackageNotifier.logger.error("Failed to timeout subscription " + this.subscription, ofex);
            }
        }
    }

    public abstract Subscription createSubscription(Address address, String str);

    public EventPackageNotifier(ProtocolProviderServiceSipImpl protocolProvider, String eventPackage, int subscriptionDuration, String contentSubType, TimerScheduler timer) {
        super(protocolProvider, eventPackage, subscriptionDuration, contentSubType, timer);
        this.messageFactory = protocolProvider.getMessageFactory();
    }

    /* access modifiers changed from: protected */
    public ClientTransaction createNotify(Dialog dialog, byte[] content, String subscriptionState, String reason) throws OperationFailedException {
        Request req = this.messageFactory.createRequest(dialog, "NOTIFY");
        Address toAddress = dialog.getRemoteTarget();
        if (toAddress == null) {
            toAddress = dialog.getRemoteParty();
        }
        try {
            ArrayList<ViaHeader> viaHeaders = this.protocolProvider.getLocalViaHeaders(toAddress);
            MaxForwardsHeader maxForwards = this.protocolProvider.getMaxForwardsHeader();
            try {
                EventHeader evHeader = this.protocolProvider.getHeaderFactory().createEventHeader(this.eventPackage);
                try {
                    SubscriptionStateHeader sStateHeader = this.protocolProvider.getHeaderFactory().createSubscriptionStateHeader(subscriptionState);
                    if (!(reason == null || reason.trim().length() == 0)) {
                        sStateHeader.setReasonCode(reason);
                    }
                    try {
                        ContentTypeHeader cTypeHeader = this.protocolProvider.getHeaderFactory().createContentTypeHeader(SIPServerTransaction.CONTENT_TYPE_APPLICATION, this.contentSubType);
                        req.setHeader(maxForwards);
                        req.setHeader(evHeader);
                        req.setHeader(sStateHeader);
                        try {
                            ClientTransaction transac = this.protocolProvider.getDefaultJainSipProvider().getNewClientTransaction(req);
                            req.setHeader((Header) viaHeaders.get(0));
                            try {
                                req.setContent(content, cTypeHeader);
                                return transac;
                            } catch (ParseException e) {
                                logger.error("Failed to add the presence document", e);
                                throw new OperationFailedException("Can't add the presence document to the request", 4, e);
                            }
                        } catch (TransactionUnavailableException ex) {
                            logger.error("Failed to create subscriptionTransaction. This is most probably a network connection error.", ex);
                            throw new OperationFailedException("Failed to create subscriptionTransaction.", 2, ex);
                        }
                    } catch (ParseException e2) {
                        logger.error("can't create the Content-Type header", e2);
                        throw new OperationFailedException("Can't create the Content-Type header", 4, e2);
                    }
                } catch (ParseException e22) {
                    logger.error("can't create the Subscription-State header", e22);
                    throw new OperationFailedException("Can't create the Subscription-State header", 4, e22);
                }
            } catch (ParseException e222) {
                logger.error("Can't create the Event header", e222);
                throw new OperationFailedException("Can't create the Event header", 4, e222);
            }
        } catch (OperationFailedException e3) {
            logger.error("Can't retrive the via headers or the max forwards header", e3);
            throw new OperationFailedException("Can't retrive the via headers or the max forwards header", 4, e3);
        }
    }

    private ClientTransaction createNotify(Dialog dialog, Subscription subscription, String subscriptionState, String reason) throws OperationFailedException {
        if (dialog == null) {
            dialog = subscription.getDialog();
            if (dialog == null) {
                throw new OperationFailedException("the dialog of the subscription is null", 4);
            }
        }
        return createNotify(dialog, subscription.createNotifyContent(subscriptionState, reason), subscriptionState, reason);
    }

    /* access modifiers changed from: protected */
    public Subscription getSubscription(Address fromAddress, String eventId) {
        return (Subscription) super.getSubscription(fromAddress, eventId);
    }

    /* access modifiers changed from: protected */
    public Subscription getSubscription(String callId) {
        return (Subscription) super.getSubscription(callId);
    }

    public void notify(Subscription subscription, String subscriptionState, String reason) throws OperationFailedException {
        String callId;
        Dialog dialog = subscription.getDialog();
        synchronized (dialog) {
            ClientTransaction transac = createNotify(dialog, subscription, subscriptionState, reason);
            callId = dialog.getCallId().getCallId();
            try {
                dialog.sendRequest(transac);
            } catch (SipException sex) {
                logger.error("Failed to send NOTIFY request.", sex);
                throw new OperationFailedException("Failed to send NOTIFY request.", 2, sex);
            }
        }
        if (SubscriptionStateHeader.TERMINATED.equals(subscriptionState)) {
            removeSubscription(callId, (Subscription) subscription);
        }
    }

    public void notifyAll(String subscriptionState, String reason) throws OperationFailedException {
        notifyAll(subscriptionState, reason, null);
    }

    public void notifyAll(String subscriptionState, String reason, SubscriptionFilter filter) throws OperationFailedException {
        for (Subscription subscription : getSubscriptions()) {
            Subscription s = (Subscription) subscription;
            if (filter == null || filter.accept(s)) {
                notify(s, subscriptionState, reason);
            }
        }
    }

    public boolean processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        EventHeader eventHeader = (EventHeader) request.getHeader("Event");
        if (eventHeader == null || !this.eventPackage.equalsIgnoreCase(eventHeader.getEventType())) {
            return false;
        }
        if (!"SUBSCRIBE".equals(request.getMethod())) {
            return false;
        }
        ServerTransaction serverTransaction = EventPackageSupport.getOrCreateServerTransaction(requestEvent);
        if (serverTransaction == null) {
            return false;
        }
        ExpiresHeader expHeader = request.getExpires();
        int expires = expHeader == null ? this.subscriptionDuration : expHeader.getExpires();
        String callId = ((CallIdHeader) request.getHeader("Call-ID")).getCallId();
        Response response;
        if (expires >= SUBSCRIBE_MIN_EXPIRE || expires <= 0) {
            Dialog dialog;
            TimerTask subscriptionTimeoutTask;
            Subscription subscription = getSubscription(callId);
            if (!(subscription == null || expires == 0)) {
                dialog = subscription.getDialog();
                if (dialog.equals(serverTransaction.getDialog())) {
                    subscriptionTimeoutTask = new SubscriptionTimeoutTask(subscription);
                    subscription.setTimerTask(subscriptionTimeoutTask);
                    this.timer.schedule(subscriptionTimeoutTask, (long) (expires * 1000));
                    try {
                        response = this.protocolProvider.getMessageFactory().createResponse(Response.OK, request);
                        try {
                            response.setHeader(this.protocolProvider.getHeaderFactory().createExpiresHeader(expires));
                            try {
                                serverTransaction.sendResponse(response);
                                return true;
                            } catch (Exception e) {
                                logger.error("Error while sending the response 200", e);
                                return false;
                            }
                        } catch (InvalidArgumentException e2) {
                            logger.error("Can't create the expires header");
                            return false;
                        }
                    } catch (Exception e3) {
                        logger.error("Error while creating the response 200", e3);
                        return false;
                    }
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("refreshing subscription " + subscription + ", we will remove the first subscription");
                }
                try {
                    ClientTransaction transac = createNotify(dialog, (Subscription) subscription, SubscriptionStateHeader.TERMINATED, SubscriptionStateHeader.REJECTED);
                    removeSubscription(callId, subscription);
                    try {
                        dialog.sendRequest(transac);
                    } catch (Exception e32) {
                        logger.error("Can't send the request", e32);
                        return false;
                    }
                } catch (OperationFailedException e4) {
                    logger.error("failed to create the new notify", e4);
                    return false;
                }
            }
            if (subscription == null) {
                subscription = createSubscription(((FromHeader) request.getHeader("From")).getAddress(), eventHeader.getEventId());
            }
            synchronized (subscription) {
                subscription.setDialog(serverTransaction.getDialog());
                dialog = subscription.getDialog();
            }
            if (expires == 0) {
                removeSubscription(callId, subscription);
                try {
                    response = this.protocolProvider.getMessageFactory().createResponse(Response.OK, request);
                    try {
                        response.setHeader(this.protocolProvider.getHeaderFactory().createExpiresHeader(0));
                        try {
                            serverTransaction.sendResponse(response);
                            try {
                                try {
                                    dialog.sendRequest(createNotify(dialog, (Subscription) subscription, SubscriptionStateHeader.TERMINATED, SubscriptionStateHeader.TIMEOUT));
                                    return true;
                                } catch (Exception e322) {
                                    logger.error("Can't send the request", e322);
                                    return false;
                                }
                            } catch (OperationFailedException e42) {
                                logger.error("failed to create the new notify", e42);
                                return false;
                            }
                        } catch (Exception e3222) {
                            logger.error("Error while sending the response 200", e3222);
                            return false;
                        }
                    } catch (InvalidArgumentException e5) {
                        logger.error("Can't create the expires header", e5);
                        return false;
                    }
                } catch (Exception e32222) {
                    logger.error("Error while creating the response 200", e32222);
                    return false;
                }
            }
            try {
                response = this.protocolProvider.getMessageFactory().createResponse(Response.OK, request);
                try {
                    response.setHeader(this.protocolProvider.getHeaderFactory().createExpiresHeader(expires));
                    try {
                        serverTransaction.sendResponse(response);
                        addSubscription(callId, subscription);
                        synchronized (dialog) {
                            try {
                                try {
                                    dialog.sendRequest(createNotify(dialog, (Subscription) subscription, "active", null));
                                } catch (Exception e322222) {
                                    logger.error("Can't send the request", e322222);
                                    return false;
                                }
                            } catch (OperationFailedException e422) {
                                logger.error("failed to create the new notify", e422);
                                return false;
                            }
                        }
                        subscriptionTimeoutTask = new SubscriptionTimeoutTask(subscription);
                        subscription.setTimerTask(subscriptionTimeoutTask);
                        this.timer.schedule(subscriptionTimeoutTask, (long) (expires * 1000));
                        return true;
                    } catch (Exception e3222222) {
                        logger.error("Error while sending the response 200", e3222222);
                        return false;
                    }
                } catch (InvalidArgumentException e52) {
                    logger.error("Can't create the expires header", e52);
                    return false;
                }
            } catch (Exception e32222222) {
                logger.error("Error while creating the response 200", e32222222);
                return false;
            }
        }
        try {
            response = this.protocolProvider.getMessageFactory().createResponse(Response.INTERVAL_TOO_BRIEF, request);
            try {
                response.setHeader(this.protocolProvider.getHeaderFactory().createMinExpiresHeader(SUBSCRIBE_MIN_EXPIRE));
                try {
                    serverTransaction.sendResponse(response);
                    return true;
                } catch (Exception e322222222) {
                    logger.error("Error while sending the response 423", e322222222);
                    return false;
                }
            } catch (InvalidArgumentException e522) {
                logger.error("can't create the min expires header", e522);
                return false;
            }
        } catch (Exception e3222222222) {
            logger.error("Error while creating the response 423", e3222222222);
            return false;
        }
    }

    public boolean processResponse(ResponseEvent responseEvent) {
        Response response = responseEvent.getResponse();
        CSeqHeader cseqHeader = (CSeqHeader) response.getHeader("CSeq");
        if (cseqHeader == null) {
            logger.error("An incoming response did not contain a CSeq header");
            return false;
        } else if (!"NOTIFY".equals(cseqHeader.getMethod())) {
            return false;
        } else {
            ClientTransaction clientTransaction = responseEvent.getClientTransaction();
            Request notifyRequest = clientTransaction.getRequest();
            String eventId = null;
            if (notifyRequest != null) {
                EventHeader eventHeader = (EventHeader) notifyRequest.getHeader("Event");
                if (eventHeader == null || !this.eventPackage.equalsIgnoreCase(eventHeader.getEventType())) {
                    return false;
                }
                eventId = eventHeader.getEventId();
            }
            switch (response.getStatusCode()) {
                case Response.OK /*200*/:
                    break;
                case Response.UNAUTHORIZED /*401*/:
                case Response.PROXY_AUTHENTICATION_REQUIRED /*407*/:
                    try {
                        processAuthenticationChallenge(clientTransaction, response, (SipProvider) responseEvent.getSource());
                        break;
                    } catch (OperationFailedException e) {
                        logger.error("can't handle the challenge", e);
                        removeSubscription(response, eventId, clientTransaction);
                        break;
                    }
                default:
                    if (logger.isDebugEnabled()) {
                        logger.debug("error received from the network" + response);
                    }
                    removeSubscription(response, eventId, clientTransaction);
                    break;
            }
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void removeSubscription(Response response, String eventId, ClientTransaction clientTransaction) {
        String callId = ((CallIdHeader) response.getHeader("Call-ID")).getCallId();
        Subscription subscription = getSubscription(callId);
        if (subscription != null) {
            synchronized (subscription) {
                if (subscription.getDialog().equals(clientTransaction.getDialog())) {
                    removeSubscription(callId, (Subscription) subscription);
                }
            }
        }
    }
}
