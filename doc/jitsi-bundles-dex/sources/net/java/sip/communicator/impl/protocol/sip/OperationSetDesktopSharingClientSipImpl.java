package net.java.sip.communicator.impl.protocol.sip;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.Queue;
import net.java.sip.communicator.impl.protocol.sip.EventPackageNotifier.Subscription;
import net.java.sip.communicator.impl.protocol.sip.EventPackageNotifier.SubscriptionFilter;
import net.java.sip.communicator.service.protocol.AbstractOperationSetDesktopSharingClient;
import net.java.sip.communicator.service.protocol.CallPeer;
import net.java.sip.communicator.service.protocol.CallPeerState;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetBasicTelephony;
import net.java.sip.communicator.service.protocol.event.CallPeerAdapter;
import net.java.sip.communicator.service.protocol.event.CallPeerChangeEvent;
import net.java.sip.communicator.service.protocol.event.CallPeerListener;
import net.java.sip.communicator.util.Logger;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.Point;
import org.jitsi.android.util.java.awt.event.KeyEvent;
import org.jitsi.android.util.java.awt.event.MouseEvent;
import org.jitsi.android.util.java.awt.event.MouseWheelEvent;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.header.CallIdHeader;
import org.jitsi.javax.sip.message.Response;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;

public class OperationSetDesktopSharingClientSipImpl extends AbstractOperationSetDesktopSharingClient<ProtocolProviderServiceSipImpl> {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(OperationSetDesktopSharingClientSipImpl.class);
    /* access modifiers changed from: private|final */
    public final CallPeerListener callPeerListener = new CallPeerAdapter() {
        public void peerStateChanged(CallPeerChangeEvent evt) {
            CallPeer peer = evt.getSourceCallPeer();
            CallPeerState state = peer.getState();
            if (CallPeerState.DISCONNECTED.equals(state) || CallPeerState.FAILED.equals(state)) {
                try {
                    OperationSetDesktopSharingClientSipImpl.this.notifier.removeSubscription(((ProtocolProviderServiceSipImpl) OperationSetDesktopSharingClientSipImpl.this.parentProvider).parseAddressString(peer.getAddress()));
                } catch (ParseException e) {
                }
            }
        }
    };
    /* access modifiers changed from: private|final */
    public final Queue<String> inputEvents = new LinkedList();
    private final Object inputSync = new Object();
    /* access modifiers changed from: private|final */
    public final EventPackageNotifier notifier = new EventPackageNotifier((ProtocolProviderServiceSipImpl) this.parentProvider, "remote-control", DesktopSharingProtocolSipImpl.SUBSCRIPTION_DURATION, DesktopSharingProtocolSipImpl.CONTENT_SUB_TYPE, this.timer) {
        /* access modifiers changed from: protected */
        public Subscription createSubscription(Address fromAddress, String eventId) {
            return new RemoteControlNotifierSubscription(fromAddress, eventId);
        }

        public boolean processRequest(RequestEvent requestEvent) {
            boolean ret = super.processRequest(requestEvent);
            if (!(requestEvent == null || requestEvent.getDialog() == null || requestEvent.getDialog().getCallId() == null)) {
                Subscription subs = getSubscription(requestEvent.getDialog().getCallId().getCallId());
                if (subs instanceof RemoteControlNotifierSubscription) {
                    OperationSetDesktopSharingClientSipImpl.this.fireRemoteControlGranted(((RemoteControlNotifierSubscription) subs).getCallPeer());
                }
            }
            return ret;
        }

        /* access modifiers changed from: protected */
        public void removeSubscription(Response response, String eventId, ClientTransaction clientTransaction) {
            Subscription ret = getSubscription(((CallIdHeader) response.getHeader("Call-ID")).getCallId());
            if (ret instanceof RemoteControlNotifierSubscription) {
                OperationSetDesktopSharingClientSipImpl.this.fireRemoteControlRevoked(((RemoteControlNotifierSubscription) ret).getCallPeer());
            }
            super.removeSubscription(response, eventId, clientTransaction);
        }
    };
    private final TimerScheduler timer = new TimerScheduler();

    private class RemoteControlNotifierSubscription extends Subscription {
        private CallPeerSipImpl callPeer = null;

        public RemoteControlNotifierSubscription(Address fromAddress, String eventId) {
            super(fromAddress, eventId);
        }

        /* access modifiers changed from: protected */
        public byte[] createNotifyContent(String subscriptionState, String reason) {
            if (getCallPeer() == null) {
                OperationSetDesktopSharingClientSipImpl.logger.error("Failed to find the CallPeer of the remote-controlsubscription " + this);
                return null;
            }
            String xml = (String) OperationSetDesktopSharingClientSipImpl.this.inputEvents.poll();
            if (xml == null) {
                xml = new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?><remote-control />");
            }
            try {
                return xml.getBytes("UTF-8");
            } catch (UnsupportedEncodingException uee) {
                OperationSetDesktopSharingClientSipImpl.logger.warn("Failed to gets bytes from String for the UTF-8 charset", uee);
                return xml.getBytes();
            }
        }

        /* access modifiers changed from: private */
        public CallPeerSipImpl getCallPeer() {
            if (this.callPeer == null) {
                Dialog dialog = getDialog();
                if (dialog != null) {
                    OperationSetBasicTelephony<?> basicTelephony = (OperationSetBasicTelephony) ((ProtocolProviderServiceSipImpl) OperationSetDesktopSharingClientSipImpl.this.parentProvider).getOperationSet(OperationSetBasicTelephony.class);
                    if (basicTelephony != null) {
                        this.callPeer = ((OperationSetBasicTelephonySipImpl) basicTelephony).getActiveCallsRepository().findCallPeer(dialog);
                        if (this.callPeer != null) {
                            this.callPeer.addCallPeerListener(OperationSetDesktopSharingClientSipImpl.this.callPeerListener);
                        }
                    }
                }
            }
            return this.callPeer;
        }
    }

    public OperationSetDesktopSharingClientSipImpl(ProtocolProviderServiceSipImpl parentProvider) {
        super(parentProvider);
    }

    private void notifySubscriptions(final CallPeer callPeer) {
        try {
            this.notifier.notifyAll("active", null, new SubscriptionFilter() {
                public boolean accept(Subscription subscription) {
                    return (subscription instanceof RemoteControlNotifierSubscription) && callPeer.getAddress().equals(((RemoteControlNotifierSubscription) subscription).getCallPeer().getAddress());
                }
            });
        } catch (OperationFailedException ofe) {
            logger.error("Failed to notify the remote-control subscriptions", ofe);
        }
    }

    public void sendKeyboardEvent(CallPeer callPeer, KeyEvent event) {
        int keyCode;
        int keyChar = event.getKeyChar();
        if (keyChar == InBandBytestreamManager.MAXIMUM_BLOCK_SIZE) {
            keyCode = event.getKeyCode();
        } else {
            keyCode = keyChar;
        }
        if (keyCode != 0) {
            String msg;
            switch (event.getID()) {
                case Response.BAD_REQUEST /*400*/:
                    msg = DesktopSharingProtocolSipImpl.getKeyTypedXML(keyCode);
                    break;
                case Response.UNAUTHORIZED /*401*/:
                    msg = DesktopSharingProtocolSipImpl.getKeyPressedXML(keyCode);
                    break;
                case Response.PAYMENT_REQUIRED /*402*/:
                    msg = DesktopSharingProtocolSipImpl.getKeyReleasedXML(keyCode);
                    break;
                default:
                    return;
            }
            synchronized (this.inputSync) {
                this.inputEvents.add(msg);
                notifySubscriptions(callPeer);
            }
        }
    }

    public void sendMouseEvent(CallPeer callPeer, MouseEvent event, Dimension videoPanelSize) {
        if (event.getID() == Response.SERVICE_UNAVAILABLE || event.getID() == 506) {
            Point p = event.getPoint();
            String msg = DesktopSharingProtocolSipImpl.getMouseMovedXML(p.getX() / ((double) videoPanelSize.width), p.getY() / ((double) videoPanelSize.height));
            synchronized (this.inputSync) {
                this.inputEvents.add(msg);
                notifySubscriptions(callPeer);
            }
            return;
        }
        sendMouseEvent(callPeer, event);
    }

    public void sendMouseEvent(CallPeer callPeer, MouseEvent event) {
        String msg;
        switch (event.getID()) {
            case Response.NOT_IMPLEMENTED /*501*/:
                msg = DesktopSharingProtocolSipImpl.getMousePressedXML(event.getModifiers());
                break;
            case Response.BAD_GATEWAY /*502*/:
                msg = DesktopSharingProtocolSipImpl.getMouseReleasedXML(event.getModifiers());
                break;
            case 507:
                msg = DesktopSharingProtocolSipImpl.getMouseWheelXML(((MouseWheelEvent) event).getWheelRotation());
                break;
            default:
                return;
        }
        synchronized (this.inputSync) {
            this.inputEvents.add(msg);
            notifySubscriptions(callPeer);
        }
    }
}
