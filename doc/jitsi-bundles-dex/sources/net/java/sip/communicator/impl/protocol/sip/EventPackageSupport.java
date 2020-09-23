package net.java.sip.communicator.impl.protocol.sip;

import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimerTask;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.util.Logger;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.ServerTransaction;
import org.jitsi.javax.sip.SipProvider;
import org.jitsi.javax.sip.TransactionAlreadyExistsException;
import org.jitsi.javax.sip.TransactionUnavailableException;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.message.Response;

public class EventPackageSupport extends MethodProcessorAdapter {
    private static final Logger logger = Logger.getLogger(EventPackageSupport.class);
    protected final String contentSubType;
    protected final String eventPackage;
    protected final ProtocolProviderServiceSipImpl protocolProvider;
    protected final int subscriptionDuration;
    private final Map<String, Subscription> subscriptions = new HashMap();
    protected final TimerScheduler timer;

    protected static class Subscription {
        protected final Address address;
        private Dialog dialog;
        protected final String eventId;
        private TimerTask timerTask;

        public Subscription(Address address, String eventId) {
            if (address == null) {
                throw new NullPointerException("address");
            }
            this.address = address;
            this.eventId = eventId;
        }

        /* access modifiers changed from: protected */
        public boolean addressEquals(Address address) {
            return getAddress().equals(address);
        }

        /* access modifiers changed from: protected */
        public boolean equals(Address address, String eventId) {
            if (addressEquals(address)) {
                String thisEventId = getEventId();
                if ((thisEventId == null && eventId == null) || (thisEventId != null && thisEventId.equals(eventId))) {
                    return true;
                }
            }
            return false;
        }

        public final Address getAddress() {
            return this.address;
        }

        /* access modifiers changed from: protected */
        public Dialog getDialog() {
            return this.dialog;
        }

        public final String getEventId() {
            return this.eventId;
        }

        /* access modifiers changed from: protected */
        public void removed() {
            setDialog(null);
            setTimerTask(null);
        }

        /* access modifiers changed from: protected */
        public void setDialog(Dialog dialog) {
            this.dialog = dialog;
        }

        /* access modifiers changed from: protected */
        public void setTimerTask(TimerTask timerTask) {
            if (this.timerTask != timerTask) {
                if (this.timerTask != null) {
                    this.timerTask.cancel();
                }
                this.timerTask = timerTask;
            }
        }
    }

    protected EventPackageSupport(ProtocolProviderServiceSipImpl protocolProvider, String eventPackage, int subscriptionDuration, String contentSubType, TimerScheduler timer) {
        this.protocolProvider = protocolProvider;
        this.eventPackage = eventPackage;
        this.subscriptionDuration = subscriptionDuration;
        this.contentSubType = contentSubType;
        if (timer == null) {
            timer = new TimerScheduler();
        }
        this.timer = timer;
        this.protocolProvider.registerEvent(this.eventPackage);
        this.protocolProvider.registerMethodProcessor("SUBSCRIBE", this);
        this.protocolProvider.registerMethodProcessor("NOTIFY", this);
    }

    /* access modifiers changed from: protected */
    public void addSubscription(String callId, Subscription subscription) {
        synchronized (this.subscriptions) {
            Subscription existingSubscription = (Subscription) this.subscriptions.get(callId);
            if (existingSubscription != null) {
                removeSubscription(callId, existingSubscription);
            }
            this.subscriptions.put(callId, subscription);
        }
    }

    public final String getEventPackage() {
        return this.eventPackage;
    }

    static ServerTransaction getOrCreateServerTransaction(RequestEvent requestEvent) {
        ServerTransaction serverTransaction = null;
        try {
            return SipStackSharing.getOrCreateServerTransaction(requestEvent);
        } catch (TransactionAlreadyExistsException ex) {
            logger.error("Failed to create a new servertransaction for an incoming request\n(Next message contains the request)", ex);
            return serverTransaction;
        } catch (TransactionUnavailableException ex2) {
            logger.error("Failed to create a new servertransaction for an incoming request\n(Next message contains the request)", ex2);
            return serverTransaction;
        }
    }

    /* access modifiers changed from: protected */
    public Subscription getSubscription(Address toAddress, String eventId) {
        synchronized (this.subscriptions) {
            for (Subscription subscription : this.subscriptions.values()) {
                if (subscription.equals(toAddress, eventId)) {
                    return subscription;
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public Subscription getSubscription(String callId) {
        Subscription subscription;
        synchronized (this.subscriptions) {
            subscription = (Subscription) this.subscriptions.get(callId);
        }
        return subscription;
    }

    /* access modifiers changed from: protected */
    public Subscription[] getSubscriptions() {
        Subscription[] subscriptionArr;
        synchronized (this.subscriptions) {
            Collection<Subscription> subscriptions = this.subscriptions.values();
            subscriptionArr = (Subscription[]) subscriptions.toArray(new Subscription[subscriptions.size()]);
        }
        return subscriptionArr;
    }

    /* access modifiers changed from: protected */
    public void processAuthenticationChallenge(ClientTransaction clientTransaction, Response response, SipProvider jainSipProvider) throws OperationFailedException {
        processAuthenticationChallenge(this.protocolProvider, clientTransaction, response, jainSipProvider);
    }

    static void processAuthenticationChallenge(ProtocolProviderServiceSipImpl protocolProvider, ClientTransaction clientTransaction, Response response, SipProvider jainSipProvider) throws OperationFailedException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Authenticating a message request.");
            }
            ClientTransaction retryTran = protocolProvider.getSipSecurityManager().handleChallenge(response, clientTransaction, jainSipProvider);
            if (retryTran != null) {
                retryTran.sendRequest();
            } else if (logger.isTraceEnabled()) {
                logger.trace("No password supplied or error occured!");
            }
        } catch (Exception exc) {
            logger.error("We failed to authenticate a message request.", exc);
            throw new OperationFailedException("Failed to authenticate a message request", 4, exc);
        }
    }

    public void removeSubscription(Address toAddress) {
        removeSubscription(toAddress, null);
    }

    public boolean removeSubscription(Address toAddress, String eventId) {
        boolean removed = false;
        synchronized (this.subscriptions) {
            Iterator<Entry<String, Subscription>> subscriptionIter = this.subscriptions.entrySet().iterator();
            while (subscriptionIter.hasNext()) {
                Subscription subscription = (Subscription) ((Entry) subscriptionIter.next()).getValue();
                if (subscription.equals(toAddress, eventId)) {
                    subscriptionIter.remove();
                    removed = true;
                    subscription.removed();
                }
            }
        }
        return removed;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Missing block: B:15:?, code skipped:
            return false;
     */
    public boolean removeSubscription(java.lang.String r5, net.java.sip.communicator.impl.protocol.sip.EventPackageSupport.Subscription r6) {
        /*
        r4 = this;
        r3 = r4.subscriptions;
        monitor-enter(r3);
        r2 = r4.subscriptions;	 Catch:{ all -> 0x0026 }
        r1 = r2.get(r5);	 Catch:{ all -> 0x0026 }
        r1 = (net.java.sip.communicator.impl.protocol.sip.EventPackageSupport.Subscription) r1;	 Catch:{ all -> 0x0026 }
        if (r1 == 0) goto L_0x0023;
    L_0x000d:
        r2 = r1.equals(r6);	 Catch:{ all -> 0x0026 }
        if (r2 == 0) goto L_0x0023;
    L_0x0013:
        r2 = r4.subscriptions;	 Catch:{ all -> 0x0026 }
        r2 = r2.remove(r5);	 Catch:{ all -> 0x0026 }
        r0 = r2;
        r0 = (net.java.sip.communicator.impl.protocol.sip.EventPackageSupport.Subscription) r0;	 Catch:{ all -> 0x0026 }
        r6 = r0;
        r6.removed();	 Catch:{ all -> 0x0026 }
        r2 = 1;
        monitor-exit(r3);	 Catch:{ all -> 0x0026 }
    L_0x0022:
        return r2;
    L_0x0023:
        monitor-exit(r3);	 Catch:{ all -> 0x0026 }
        r2 = 0;
        goto L_0x0022;
    L_0x0026:
        r2 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x0026 }
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.sip.EventPackageSupport.removeSubscription(java.lang.String, net.java.sip.communicator.impl.protocol.sip.EventPackageSupport$Subscription):boolean");
    }

    public static boolean sendNotImplementedResponse(ProtocolProviderServiceSipImpl provider, RequestEvent requestEvent) {
        ServerTransaction serverTransaction = getOrCreateServerTransaction(requestEvent);
        if (serverTransaction == null) {
            return false;
        }
        try {
            try {
                serverTransaction.sendResponse(provider.getMessageFactory().createResponse(Response.NOT_IMPLEMENTED, requestEvent.getRequest()));
                return true;
            } catch (Exception e) {
                logger.error("Error while sending the response 501", e);
                return false;
            }
        } catch (ParseException e2) {
            logger.error("Error while creating 501 response", e2);
            return false;
        }
    }
}
