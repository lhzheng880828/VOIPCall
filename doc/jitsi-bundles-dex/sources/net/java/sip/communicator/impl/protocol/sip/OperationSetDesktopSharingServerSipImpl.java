package net.java.sip.communicator.impl.protocol.sip;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.java.sip.communicator.impl.protocol.sip.EventPackageSubscriber.Subscription;
import net.java.sip.communicator.service.hid.HIDService;
import net.java.sip.communicator.service.protocol.Call;
import net.java.sip.communicator.service.protocol.CallPeer;
import net.java.sip.communicator.service.protocol.CallPeerState;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetDesktopSharingServer;
import net.java.sip.communicator.service.protocol.event.CallPeerAdapter;
import net.java.sip.communicator.service.protocol.event.CallPeerChangeEvent;
import net.java.sip.communicator.service.protocol.event.CallPeerListener;
import net.java.sip.communicator.util.Logger;
import org.jitsi.android.util.java.awt.event.ComponentEvent;
import org.jitsi.android.util.java.awt.event.KeyEvent;
import org.jitsi.android.util.java.awt.event.MouseEvent;
import org.jitsi.android.util.java.awt.event.MouseWheelEvent;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.DialogState;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.ResponseEvent;
import org.jitsi.javax.sip.header.CSeqHeader;
import org.jitsi.javax.sip.header.SubscriptionStateHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.device.MediaDevice;
import org.jitsi.service.neomedia.format.VideoMediaFormat;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class OperationSetDesktopSharingServerSipImpl extends OperationSetDesktopStreamingSipImpl implements OperationSetDesktopSharingServer, MethodProcessorListener {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(OperationSetDesktopSharingServerSipImpl.class);
    private final CallPeerListener callPeerListener = new CallPeerAdapter() {
        public void peerStateChanged(CallPeerChangeEvent evt) {
            CallPeer peer = evt.getSourceCallPeer();
            CallPeerState state = peer.getState();
            if (OperationSetDesktopSharingServerSipImpl.this.remoteControlEnabled && state != null) {
                if (state.equals(CallPeerState.DISCONNECTED) || state.equals(CallPeerState.FAILED)) {
                    OperationSetDesktopSharingServerSipImpl.this.remoteControlEnabled = false;
                    try {
                        OperationSetDesktopSharingServerSipImpl.this.subscriber.removeSubscription(OperationSetDesktopSharingServerSipImpl.this.parentProvider.parseAddressString(peer.getAddress()));
                    } catch (ParseException e) {
                    }
                }
            }
        }
    };
    private HIDService hidService = null;
    /* access modifiers changed from: private|final */
    public final ProtocolProviderServiceSipImpl parentProvider;
    /* access modifiers changed from: private */
    public boolean remoteControlEnabled = false;
    /* access modifiers changed from: private|final */
    public final EventPackageSubscriber subscriber;
    private final TimerScheduler timer = new TimerScheduler();

    private class RemoteControlSubscriberSubscription extends Subscription {
        private final CallPeerSipImpl callPeer;

        public RemoteControlSubscriberSubscription(CallPeerSipImpl callPeer) {
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
            if (requestEvent.getDialog() == this.callPeer.getDialog() && rawContent != null) {
                Document document = null;
                Throwable exception = null;
                try {
                    document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(rawContent));
                } catch (IOException ioe) {
                    exception = ioe;
                } catch (ParserConfigurationException pce) {
                    exception = pce;
                } catch (SAXException saxe) {
                    exception = saxe;
                }
                if (exception != null) {
                    OperationSetDesktopSharingServerSipImpl.logger.error("Failed to parse remote-info XML", exception);
                    return;
                }
                for (ComponentEvent evt : DesktopSharingProtocolSipImpl.parse(document.getDocumentElement(), OperationSetDesktopSharingServerSipImpl.this.size, OperationSetDesktopSharingServerSipImpl.this.getOrigin())) {
                    if (evt instanceof MouseEvent) {
                        OperationSetDesktopSharingServerSipImpl.this.processMouseEvent((MouseEvent) evt);
                    } else if (evt instanceof KeyEvent) {
                        OperationSetDesktopSharingServerSipImpl.this.processKeyboardEvent((KeyEvent) evt);
                    }
                }
            }
        }

        /* access modifiers changed from: protected */
        public void processFailureResponse(ResponseEvent responseEvent, int statusCode) {
            OperationSetDesktopSharingServerSipImpl.this.remoteControlEnabled = false;
        }

        /* access modifiers changed from: protected */
        public void processSuccessResponse(ResponseEvent responseEvent, int statusCode) {
            switch (statusCode) {
                case Response.OK /*200*/:
                case Response.ACCEPTED /*202*/:
                    OperationSetDesktopSharingServerSipImpl.this.remoteControlEnabled = true;
                    return;
                default:
                    return;
            }
        }

        /* access modifiers changed from: protected */
        public void processTerminatedRequest(RequestEvent requestEvent, String reasonCode) {
            if (SubscriptionStateHeader.DEACTIVATED.equals(reasonCode)) {
                try {
                    OperationSetDesktopSharingServerSipImpl.this.subscriber.poll(this);
                } catch (OperationFailedException ofe) {
                    OperationSetDesktopSharingServerSipImpl.logger.error("Failed to renew the remote-control subscription " + this, ofe);
                }
            }
        }
    }

    public OperationSetDesktopSharingServerSipImpl(OperationSetBasicTelephonySipImpl basicTelephony) {
        super(basicTelephony);
        this.parentProvider = basicTelephony.getProtocolProvider();
        this.hidService = SipActivator.getHIDService();
        this.subscriber = new EventPackageSubscriber(this.parentProvider, "remote-control", DesktopSharingProtocolSipImpl.SUBSCRIPTION_DURATION, DesktopSharingProtocolSipImpl.CONTENT_SUB_TYPE, this.timer, 60);
    }

    public Call createVideoCall(String uri, MediaDevice device) throws OperationFailedException, ParseException {
        CallSipImpl call = (CallSipImpl) super.createVideoCall(uri, device);
        CallPeerSipImpl callPeer = (CallPeerSipImpl) call.getCallPeers().next();
        callPeer.addMethodProcessorListener(this);
        callPeer.addCallPeerListener(this.callPeerListener);
        this.size = ((VideoMediaFormat) call.getDefaultDevice(MediaType.VIDEO).getFormat()).getSize();
        this.origin = OperationSetDesktopStreamingSipImpl.getOriginForMediaDevice(device);
        return call;
    }

    public Call createVideoCall(Contact callee, MediaDevice device) throws OperationFailedException {
        CallSipImpl call = (CallSipImpl) super.createVideoCall(callee, device);
        CallPeerSipImpl callPeer = (CallPeerSipImpl) call.getCallPeers().next();
        callPeer.addMethodProcessorListener(this);
        callPeer.addCallPeerListener(this.callPeerListener);
        this.size = ((VideoMediaFormat) call.getDefaultDevice(MediaType.VIDEO).getFormat()).getSize();
        this.origin = OperationSetDesktopStreamingSipImpl.getOriginForMediaDevice(device);
        return call;
    }

    public void enableRemoteControl(CallPeer callPeer) {
        try {
            this.subscriber.subscribe(new RemoteControlSubscriberSubscription((CallPeerSipImpl) callPeer));
        } catch (OperationFailedException ofe) {
            logger.error("Failed to create or send a remote-control subscription", ofe);
        }
    }

    public void disableRemoteControl(CallPeer callPeer) {
        try {
            this.subscriber.unsubscribe(this.parentProvider.parseAddressString(callPeer.getAddress()), false);
        } catch (ParseException ex) {
            logger.error("Failed to parse address", ex);
        } catch (OperationFailedException ofe) {
            logger.error("Failed to create or send a remote-control unsubscription", ofe);
            return;
        }
        this.remoteControlEnabled = false;
    }

    public void requestProcessed(CallPeerSipImpl sourceCallPeer, Request request, Response response) {
    }

    public void responseProcessed(CallPeerSipImpl sourceCallPeer, Response response, Request request) {
        if (Response.OK == response.getStatusCode()) {
            CSeqHeader cseqHeader = (CSeqHeader) response.getHeader("CSeq");
            if (cseqHeader == null || "INVITE".equalsIgnoreCase(cseqHeader.getMethod())) {
            }
        }
    }

    public void processKeyboardEvent(KeyEvent event) {
        if (this.remoteControlEnabled && this.hidService != null) {
            if (event.getKeyChar() == 0 || event.getID() != Response.BAD_REQUEST) {
                int keycode = event.getKeyCode();
                if (keycode != 0) {
                    switch (event.getID()) {
                        case Response.UNAUTHORIZED /*401*/:
                            this.hidService.keyPress(keycode);
                            return;
                        case Response.PAYMENT_REQUIRED /*402*/:
                            this.hidService.keyRelease(keycode);
                            return;
                        default:
                            return;
                    }
                }
                return;
            }
            this.hidService.keyPress(event.getKeyChar());
            this.hidService.keyRelease(event.getKeyChar());
        }
    }

    public void processMouseEvent(MouseEvent event) {
        if (this.remoteControlEnabled && this.hidService != null) {
            switch (event.getID()) {
                case Response.NOT_IMPLEMENTED /*501*/:
                    this.hidService.mousePress(event.getModifiers());
                    return;
                case Response.BAD_GATEWAY /*502*/:
                    this.hidService.mouseRelease(event.getModifiers());
                    return;
                case Response.SERVICE_UNAVAILABLE /*503*/:
                    this.hidService.mouseMove(event.getX(), event.getY());
                    return;
                case 507:
                    this.hidService.mouseWheel(((MouseWheelEvent) event).getWheelRotation());
                    return;
                default:
                    return;
            }
        }
    }

    public boolean isRemoteControlAvailable(CallPeer callPeer) {
        return true;
    }
}
