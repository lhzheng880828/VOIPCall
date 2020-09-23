package net.java.sip.communicator.impl.protocol.sip;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Iterator;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CoinPacketExtension;
import net.java.sip.communicator.impl.protocol.sip.EventPackageNotifier.Subscription;
import net.java.sip.communicator.impl.protocol.sip.EventPackageNotifier.SubscriptionFilter;
import net.java.sip.communicator.impl.protocol.sip.sdp.SdpUtils;
import net.java.sip.communicator.service.protocol.Call;
import net.java.sip.communicator.service.protocol.CallPeer;
import net.java.sip.communicator.service.protocol.CallPeerState;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.event.CallPeerAdapter;
import net.java.sip.communicator.service.protocol.event.CallPeerChangeEvent;
import net.java.sip.communicator.service.protocol.event.CallPeerEvent;
import net.java.sip.communicator.service.protocol.event.CallPeerListener;
import net.java.sip.communicator.service.protocol.media.AbstractOperationSetTelephonyConferencing;
import net.java.sip.communicator.service.protocol.media.ConferenceInfoDocument;
import net.java.sip.communicator.util.Logger;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.DialogState;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.ResponseEvent;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.header.CSeqHeader;
import org.jitsi.javax.sip.header.ContactHeader;
import org.jitsi.javax.sip.header.SubscriptionStateHeader;
import org.jitsi.javax.sip.message.Message;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;
import org.jitsi.util.xml.XMLException;

public class OperationSetTelephonyConferencingSipImpl extends AbstractOperationSetTelephonyConferencing<ProtocolProviderServiceSipImpl, OperationSetBasicTelephonySipImpl, CallSipImpl, CallPeerSipImpl, Address> implements MethodProcessorListener {
    private static final String CONTENT_SUB_TYPE = "conference-info+xml";
    private static final String EVENT_PACKAGE = "conference";
    private static final int MIN_NOTIFY_INTERVAL = 200;
    private static final int REFRESH_MARGIN = 60;
    private static final int SUBSCRIPTION_DURATION = 3600;
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(OperationSetTelephonyConferencingSipImpl.class);
    private final CallPeerListener callPeerStateListener = new CallPeerAdapter() {
        public void peerStateChanged(CallPeerChangeEvent evt) {
            CallPeer peer = evt.getSourceCallPeer();
            if (peer != null && peer.getState() == CallPeerState.CONNECTED && peer.isConferenceFocus()) {
                try {
                    OperationSetTelephonyConferencingSipImpl.this.subscriber.subscribe(new ConferenceSubscriberSubscription((CallPeerSipImpl) peer));
                } catch (OperationFailedException ofe) {
                    OperationSetTelephonyConferencingSipImpl.logger.error("Failed to create or send a conference subscription to " + peer, ofe);
                }
                peer.removeCallPeerListener(this);
            }
        }
    };
    private final EventPackageNotifier notifier = new ConferenceEventPackageNotifier((ProtocolProviderServiceSipImpl) this.parentProvider, this.timer);
    /* access modifiers changed from: private|final */
    public final EventPackageSubscriber subscriber = new EventPackageSubscriber((ProtocolProviderServiceSipImpl) this.parentProvider, "conference", 3600, CONTENT_SUB_TYPE, this.timer, 60);
    private final TimerScheduler timer = new TimerScheduler();

    private class ConferenceEventPackageNotifier extends EventPackageNotifier {
        ConferenceEventPackageNotifier(ProtocolProviderServiceSipImpl protocolProvider, TimerScheduler timer) {
            super(protocolProvider, "conference", 3600, OperationSetTelephonyConferencingSipImpl.CONTENT_SUB_TYPE, timer);
        }

        /* access modifiers changed from: protected */
        public Subscription createSubscription(Address fromAddress, String eventId) {
            return new ConferenceNotifierSubscription(fromAddress, eventId);
        }

        public void notify(Subscription subscription, String subscriptionState, String reason) throws OperationFailedException {
            ConferenceNotifierSubscription conferenceSubscription = (ConferenceNotifierSubscription) subscription;
            Dialog dialog = conferenceSubscription.getDialog();
            CallPeerSipImpl callPeer = conferenceSubscription.getCallPeer();
            if (callPeer == null) {
                throw new OperationFailedException("Failed to find the CallPeer of the conference subscription " + conferenceSubscription, 4);
            }
            final long timeSinceLastNotify = System.currentTimeMillis() - callPeer.getLastConferenceInfoSentTimestamp();
            if (timeSinceLastNotify >= 200) {
                ConferenceInfoDocument diff;
                ConferenceInfoDocument currentConfInfo = OperationSetTelephonyConferencingSipImpl.this.getCurrentConferenceInfo(callPeer);
                ConferenceInfoDocument lastSentConfInfo = callPeer.getLastConferenceInfoSent();
                if (lastSentConfInfo == null) {
                    diff = currentConfInfo;
                } else {
                    diff = OperationSetTelephonyConferencingSipImpl.this.getConferenceInfoDiff(lastSentConfInfo, currentConfInfo);
                }
                if (diff == null) {
                    callPeer.setConfInfoScheduled(false);
                    return;
                }
                int newVersion;
                byte[] notifyContent;
                String callId;
                if (lastSentConfInfo == null) {
                    newVersion = 1;
                } else {
                    newVersion = lastSentConfInfo.getVersion() + 1;
                }
                diff.setVersion(newVersion);
                String xml = diff.toXml();
                try {
                    notifyContent = xml.getBytes("UTF-8");
                } catch (UnsupportedEncodingException uee) {
                    OperationSetTelephonyConferencingSipImpl.logger.warn("Failed to gets bytes from String for the UTF-8 charset", uee);
                    notifyContent = xml.getBytes();
                }
                synchronized (dialog) {
                    ClientTransaction transac = createNotify(dialog, notifyContent, subscriptionState, reason);
                    callId = dialog.getCallId().getCallId();
                    try {
                        if (OperationSetTelephonyConferencingSipImpl.logger.isInfoEnabled()) {
                            OperationSetTelephonyConferencingSipImpl.logger.info("Sending conference-info NOTIFY (version " + newVersion + ") to " + callPeer);
                        }
                        dialog.sendRequest(transac);
                        currentConfInfo.setVersion(newVersion);
                        callPeer.setLastConferenceInfoSent(currentConfInfo);
                        callPeer.setLastConferenceInfoSentTimestamp(System.currentTimeMillis());
                    } catch (SipException sex) {
                        OperationSetTelephonyConferencingSipImpl.logger.error("Failed to send NOTIFY request.", sex);
                        throw new OperationFailedException("Failed to send NOTIFY request.", 2, sex);
                    }
                }
                if (SubscriptionStateHeader.TERMINATED.equals(subscriptionState)) {
                    removeSubscription(callId, (Subscription) subscription);
                }
                callPeer.setConfInfoScheduled(false);
            } else if (!callPeer.isConfInfoScheduled()) {
                OperationSetTelephonyConferencingSipImpl.logger.info("Scheduling to send a conference-info NOTIFY to " + callPeer);
                callPeer.setConfInfoScheduled(true);
                final Subscription subscription2 = subscription;
                final String str = subscriptionState;
                final String str2 = reason;
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(201 - timeSinceLastNotify);
                        } catch (InterruptedException e) {
                        }
                        try {
                            ConferenceEventPackageNotifier.this.notify(subscription2, str, str2);
                        } catch (OperationFailedException e2) {
                            OperationSetTelephonyConferencingSipImpl.logger.error("Failed to send NOTIFY request");
                        }
                    }
                }).start();
            }
        }
    }

    private class ConferenceNotifierSubscription extends Subscription {
        public ConferenceNotifierSubscription(Address fromAddress, String eventId) {
            super(fromAddress, eventId);
        }

        /* access modifiers changed from: protected */
        public byte[] createNotifyContent(String subscriptionState, String reason) {
            CallPeerSipImpl callPeer = getCallPeer();
            if (callPeer == null) {
                OperationSetTelephonyConferencingSipImpl.logger.error("Failed to find the CallPeer of the conference subscription " + this);
                return null;
            }
            ConferenceInfoDocument currentConfInfo = OperationSetTelephonyConferencingSipImpl.this.getCurrentConferenceInfo(callPeer);
            ConferenceInfoDocument lastSentConfInfo = callPeer.getLastConferenceInfoSent();
            ConferenceInfoDocument diff = currentConfInfo;
            if (diff == null) {
                return null;
            }
            int newVersion;
            if (lastSentConfInfo == null) {
                newVersion = 1;
            } else {
                newVersion = lastSentConfInfo.getVersion() + 1;
            }
            diff.setVersion(newVersion);
            currentConfInfo.setVersion(newVersion);
            callPeer.setLastConferenceInfoSent(currentConfInfo);
            callPeer.setLastConferenceInfoSentTimestamp(System.currentTimeMillis());
            String xml = diff.toXml();
            try {
                return xml.getBytes("UTF-8");
            } catch (UnsupportedEncodingException uee) {
                OperationSetTelephonyConferencingSipImpl.logger.warn("Failed to gets bytes from String for the UTF-8 charset", uee);
                return xml.getBytes();
            }
        }

        public CallSipImpl getCall() {
            CallPeerSipImpl callPeer = getCallPeer();
            return callPeer == null ? null : (CallSipImpl) callPeer.getCall();
        }

        /* access modifiers changed from: private */
        public CallPeerSipImpl getCallPeer() {
            Dialog dialog = getDialog();
            if (dialog != null) {
                OperationSetBasicTelephonySipImpl basicTelephony = (OperationSetBasicTelephonySipImpl) OperationSetTelephonyConferencingSipImpl.this.getBasicTelephony();
                if (basicTelephony != null) {
                    return basicTelephony.getActiveCallsRepository().findCallPeer(dialog);
                }
            }
            return null;
        }
    }

    private class ConferenceSubscriberSubscription extends EventPackageSubscriber.Subscription {
        private final CallPeerSipImpl callPeer;
        private int version = 0;

        public ConferenceSubscriberSubscription(CallPeerSipImpl callPeer) {
            super(callPeer.getPeerAddress());
            this.callPeer = callPeer;
        }

        /* access modifiers changed from: protected */
        public Dialog getDialog() {
            Dialog dialog = super.getDialog();
            if (dialog == null || DialogState.TERMINATED.equals(dialog.getState())) {
                return this.callPeer.getDialog();
            }
            return dialog;
        }

        /* access modifiers changed from: protected */
        public void processActiveRequest(RequestEvent requestEvent, byte[] rawContent) {
            if (rawContent != null) {
                try {
                    OperationSetTelephonyConferencingSipImpl.this.setConferenceInfoXML(this.callPeer, SdpUtils.getContentAsString(requestEvent.getRequest()));
                } catch (XMLException e) {
                    OperationSetTelephonyConferencingSipImpl.logger.error("Could not handle conference-info NOTIFY sent to us by " + this.callPeer);
                }
            }
        }

        /* access modifiers changed from: protected */
        public void processFailureResponse(ResponseEvent responseEvent, int statusCode) {
            this.callPeer.setConferenceFocus(false);
        }

        /* access modifiers changed from: protected */
        public void processSuccessResponse(ResponseEvent responseEvent, int statusCode) {
            switch (statusCode) {
                case 200:
                case Response.ACCEPTED /*202*/:
                    this.callPeer.setConferenceFocus(true);
                    return;
                default:
                    return;
            }
        }

        /* access modifiers changed from: protected */
        public void processTerminatedRequest(RequestEvent requestEvent, String reasonCode) {
            if (SubscriptionStateHeader.DEACTIVATED.equals(reasonCode)) {
                try {
                    OperationSetTelephonyConferencingSipImpl.this.subscriber.poll(this);
                } catch (OperationFailedException ofe) {
                    OperationSetTelephonyConferencingSipImpl.logger.error("Failed to renew the conference subscription " + this, ofe);
                }
            }
        }
    }

    public OperationSetTelephonyConferencingSipImpl(ProtocolProviderServiceSipImpl parentProvider) {
        super(parentProvider);
    }

    private static void append(StringBuffer stringBuffer, String... strings) {
        for (String str : strings) {
            stringBuffer.append(str);
        }
    }

    public void callPeerAdded(CallPeerEvent event) {
        OperationSetTelephonyConferencingSipImpl.super.callPeerAdded(event);
        CallPeer callPeer = event.getSourceCallPeer();
        if (callPeer instanceof CallPeerSipImpl) {
            ((CallPeerSipImpl) callPeer).addMethodProcessorListener(this);
        }
    }

    public void callPeerRemoved(CallPeerEvent event) {
        CallPeer callPeer = event.getSourceCallPeer();
        if (callPeer instanceof CallPeerSipImpl) {
            ((CallPeerSipImpl) callPeer).removeMethodProcessorListener(this);
        }
        OperationSetTelephonyConferencingSipImpl.super.callPeerRemoved(event);
    }

    /* access modifiers changed from: protected */
    public CallSipImpl createOutgoingCall() throws OperationFailedException {
        return ((OperationSetBasicTelephonySipImpl) getBasicTelephony()).createOutgoingCall();
    }

    /* access modifiers changed from: protected */
    public CallPeerSipImpl doInviteCalleeToCall(Address calleeAddress, CallSipImpl call) throws OperationFailedException {
        return call.invite(calleeAddress, null);
    }

    private void inviteCompleted(CallPeerSipImpl sourceCallPeer, Message remoteMessage, Message localMessage) {
        ContactHeader contactHeader = (ContactHeader) remoteMessage.getHeader("Contact");
        boolean conferenceFocus = false;
        if (contactHeader != null) {
            Iterator<?> parameterNameIter = contactHeader.getParameterNames();
            while (parameterNameIter.hasNext()) {
                if (CoinPacketExtension.ISFOCUS_ATTR_NAME.equalsIgnoreCase(parameterNameIter.next().toString())) {
                    conferenceFocus = true;
                    break;
                }
            }
        }
        sourceCallPeer.addCallPeerListener(this.callPeerStateListener);
        sourceCallPeer.setConferenceFocus(conferenceFocus);
        if (sourceCallPeer.isConferenceFocus() && sourceCallPeer.getState() == CallPeerState.CONNECTED) {
            try {
                this.subscriber.subscribe(new ConferenceSubscriberSubscription(sourceCallPeer));
            } catch (OperationFailedException ofe) {
                logger.error("Failed to create or send a conference subscription to " + sourceCallPeer, ofe);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyCallPeers(Call call) {
        notifyAll("active", null, call);
    }

    private void notifyAll(String subscriptionState, String reason, final Call call) {
        try {
            this.notifier.notifyAll(subscriptionState, reason, new SubscriptionFilter() {
                public boolean accept(Subscription subscription) {
                    return (subscription instanceof ConferenceNotifierSubscription) && call.equals(((ConferenceNotifierSubscription) subscription).getCall());
                }
            });
        } catch (OperationFailedException ofe) {
            logger.error("Failed to notify the conference subscriptions of " + call, ofe);
        }
    }

    /* access modifiers changed from: protected */
    public Address parseAddressString(String calleeAddressString) throws OperationFailedException {
        try {
            return ((ProtocolProviderServiceSipImpl) this.parentProvider).parseAddressString(calleeAddressString);
        } catch (ParseException pe) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to parse callee address " + calleeAddressString, 11, pe, logger);
            return null;
        }
    }

    public void requestProcessed(CallPeerSipImpl sourceCallPeer, Request request, Response response) {
        if ("INVITE".equalsIgnoreCase(request.getMethod()) && response != null && 200 == response.getStatusCode()) {
            inviteCompleted(sourceCallPeer, request, response);
        }
    }

    public void responseProcessed(CallPeerSipImpl sourceCallPeer, Response response, Request request) {
        if (200 == response.getStatusCode()) {
            CSeqHeader cseqHeader = (CSeqHeader) response.getHeader("CSeq");
            if (cseqHeader != null && "INVITE".equalsIgnoreCase(cseqHeader.getMethod())) {
                inviteCompleted(sourceCallPeer, response, request);
            }
        }
    }

    /* access modifiers changed from: protected */
    public String getLocalEntity(CallPeer callPeer) {
        if (callPeer instanceof CallPeerSipImpl) {
            Dialog dialog = ((CallPeerSipImpl) callPeer).getDialog();
            if (dialog != null) {
                Address localPartyAddress = dialog.getLocalParty();
                if (localPartyAddress != null) {
                    return stripParametersFromAddress(localPartyAddress.getURI().toString());
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public String getLocalDisplayName() {
        return ((ProtocolProviderServiceSipImpl) this.parentProvider).getOurDisplayName();
    }
}
