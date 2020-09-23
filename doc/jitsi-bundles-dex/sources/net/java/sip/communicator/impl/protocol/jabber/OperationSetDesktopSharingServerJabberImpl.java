package net.java.sip.communicator.impl.protocol.jabber;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import net.java.sip.communicator.impl.protocol.jabber.extensions.inputevt.InputEvtAction;
import net.java.sip.communicator.impl.protocol.jabber.extensions.inputevt.InputEvtIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.inputevt.RemoteControlExtension;
import net.java.sip.communicator.service.hid.HIDService;
import net.java.sip.communicator.service.protocol.Call;
import net.java.sip.communicator.service.protocol.CallPeer;
import net.java.sip.communicator.service.protocol.CallPeerState;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetDesktopSharingServer;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.CallPeerAdapter;
import net.java.sip.communicator.service.protocol.event.CallPeerChangeEvent;
import net.java.sip.communicator.service.protocol.event.CallPeerListener;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.service.protocol.media.MediaAwareCall;
import net.java.sip.communicator.util.Logger;
import org.jitsi.android.util.java.awt.event.ComponentEvent;
import org.jitsi.android.util.java.awt.event.KeyEvent;
import org.jitsi.android.util.java.awt.event.MouseEvent;
import org.jitsi.android.util.java.awt.event.MouseWheelEvent;
import org.jitsi.javax.sip.message.Response;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.MediaUseCase;
import org.jitsi.service.neomedia.device.MediaDevice;
import org.jitsi.service.neomedia.format.VideoMediaFormat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.packet.DiscoverInfo;

public class OperationSetDesktopSharingServerJabberImpl extends OperationSetDesktopStreamingJabberImpl implements OperationSetDesktopSharingServer, RegistrationStateChangeListener, PacketListener, PacketFilter {
    private static final Logger logger = Logger.getLogger(OperationSetDesktopSharingServerJabberImpl.class);
    private final CallPeerListener callPeerListener = new CallPeerAdapter() {
        public void peerStateChanged(CallPeerChangeEvent evt) {
            CallPeer peer = evt.getSourceCallPeer();
            CallPeerState state = peer.getState();
            if (state == null) {
                return;
            }
            if (state.equals(CallPeerState.DISCONNECTED) || state.equals(CallPeerState.FAILED)) {
                OperationSetDesktopSharingServerJabberImpl.this.disableRemoteControl(peer);
            }
        }
    };
    private List<String> callPeers = new ArrayList();
    private HIDService hidService = null;

    public OperationSetDesktopSharingServerJabberImpl(OperationSetBasicTelephonyJabberImpl basicTelephony) {
        super(basicTelephony);
        ((ProtocolProviderServiceJabberImpl) this.parentProvider).addRegistrationStateChangeListener(this);
        this.hidService = JabberActivator.getHIDService();
    }

    public Call createVideoCall(String uri, MediaDevice device) throws OperationFailedException, ParseException {
        MediaAwareCall<?, ?, ?> call = (MediaAwareCall) super.createVideoCall(uri, device);
        this.size = ((VideoMediaFormat) call.getDefaultDevice(MediaType.VIDEO).getFormat()).getSize();
        this.origin = OperationSetDesktopStreamingJabberImpl.getOriginForMediaDevice(device);
        return call;
    }

    public Call createVideoCall(Contact callee, MediaDevice device) throws OperationFailedException {
        MediaAwareCall<?, ?, ?> call = (MediaAwareCall) super.createVideoCall(callee, device);
        this.size = ((VideoMediaFormat) call.getDefaultDevice(MediaType.VIDEO).getFormat()).getSize();
        this.origin = OperationSetDesktopStreamingJabberImpl.getOriginForMediaDevice(device);
        return call;
    }

    /* access modifiers changed from: protected */
    public Call createOutgoingVideoCall(String calleeAddress) throws OperationFailedException {
        return createOutgoingVideoCall(calleeAddress, null);
    }

    /* access modifiers changed from: protected */
    public Call createOutgoingVideoCall(String calleeAddress, MediaDevice videoDevice) throws OperationFailedException {
        String fullCalleeURI;
        boolean supported = false;
        if (calleeAddress.indexOf(47) > 0) {
            fullCalleeURI = calleeAddress;
        } else {
            fullCalleeURI = ((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection().getRoster().getPresence(calleeAddress).getFrom();
        }
        if (logger.isInfoEnabled()) {
            logger.info("creating outgoing desktop sharing call...");
        }
        try {
            if (((ProtocolProviderServiceJabberImpl) this.parentProvider).getDiscoveryManager().discoverInfo(fullCalleeURI).containsFeature(InputEvtIQ.NAMESPACE_CLIENT)) {
                if (logger.isInfoEnabled()) {
                    logger.info(fullCalleeURI + ": remote-control supported");
                }
                supported = true;
            } else if (logger.isInfoEnabled()) {
                logger.info(fullCalleeURI + ": remote-control not supported!");
            }
        } catch (XMPPException ex) {
            logger.warn("could not retrieve info for " + fullCalleeURI, ex);
        }
        if (((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection() == null) {
            throw new OperationFailedException("Failed to create OutgoingJingleSession.\nwe don't have a valid XMPPConnection.", 4);
        }
        CallJabberImpl call = new CallJabberImpl((OperationSetBasicTelephonyJabberImpl) this.basicTelephony);
        MediaUseCase useCase = getMediaUseCase();
        if (videoDevice != null) {
            call.setVideoDevice(videoDevice, useCase);
        }
        call.setLocalVideoAllowed(true, useCase);
        call.setLocalInputEvtAware(supported);
        ((OperationSetBasicTelephonyJabberImpl) this.basicTelephony).createOutgoingCall(call, calleeAddress);
        CallPeerJabberImpl callPeerJabberImpl = new CallPeerJabberImpl(calleeAddress, call);
        return call;
    }

    public void setLocalVideoAllowed(Call call, MediaDevice mediaDevice, boolean allowed) throws OperationFailedException {
        ((AbstractCallJabberGTalkImpl) call).setLocalInputEvtAware(allowed);
        super.setLocalVideoAllowed(call, mediaDevice, allowed);
    }

    public void enableRemoteControl(CallPeer callPeer) {
        callPeer.addCallPeerListener(this.callPeerListener);
        modifyRemoteControl(callPeer, true);
    }

    public void disableRemoteControl(CallPeer callPeer) {
        modifyRemoteControl(callPeer, false);
        callPeer.removeCallPeerListener(this.callPeerListener);
    }

    public void registrationStateChanged(RegistrationStateChangeEvent evt) {
        registrationStateChanged(evt, this, this, ((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection());
    }

    public static void registrationStateChanged(RegistrationStateChangeEvent evt, PacketListener packetListener, PacketFilter packetFilter, Connection connection) {
        if (connection != null) {
            if (evt.getNewState() == RegistrationState.REGISTERING) {
                connection.addPacketListener(packetListener, packetFilter);
            } else if (evt.getNewState() == RegistrationState.UNREGISTERING) {
                connection.removePacketListener(packetListener);
            }
        }
    }

    public void processPacket(Packet packet) {
        InputEvtIQ inputIQ = (InputEvtIQ) packet;
        if (inputIQ.getType() == Type.SET && inputIQ.getAction() == InputEvtAction.NOTIFY) {
            ((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection().sendPacket(IQ.createResultIQ(inputIQ));
            if (this.callPeers.contains(inputIQ.getFrom())) {
                for (RemoteControlExtension p : inputIQ.getRemoteControls()) {
                    processComponentEvent(p.getEvent());
                }
            }
        }
    }

    public boolean accept(Packet packet) {
        return packet instanceof InputEvtIQ;
    }

    public void processComponentEvent(ComponentEvent event) {
        if (event != null) {
            if (event instanceof KeyEvent) {
                processKeyboardEvent((KeyEvent) event);
            } else if (event instanceof MouseEvent) {
                processMouseEvent((MouseEvent) event);
            }
        }
    }

    public void processKeyboardEvent(KeyEvent event) {
        if (this.hidService == null) {
            return;
        }
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

    public void processMouseEvent(MouseEvent event) {
        if (this.hidService != null) {
            switch (event.getID()) {
                case Response.NOT_IMPLEMENTED /*501*/:
                    this.hidService.mousePress(event.getModifiers());
                    return;
                case Response.BAD_GATEWAY /*502*/:
                    this.hidService.mouseRelease(event.getModifiers());
                    return;
                case Response.SERVICE_UNAVAILABLE /*503*/:
                    int originX;
                    int originY;
                    if (this.origin != null) {
                        originX = this.origin.x;
                    } else {
                        originX = 0;
                    }
                    if (this.origin != null) {
                        originY = this.origin.y;
                    } else {
                        originY = 0;
                    }
                    this.hidService.mouseMove(originX + ((event.getX() * this.size.width) / 1000), originY + ((event.getY() * this.size.height) / 1000));
                    return;
                case 507:
                    this.hidService.mouseWheel(((MouseWheelEvent) event).getWheelRotation());
                    return;
                default:
                    return;
            }
        }
    }

    public void modifyRemoteControl(CallPeer callPeer, boolean enables) {
        synchronized (this.callPeers) {
            if (this.callPeers.contains(callPeer.getAddress()) != enables && isRemoteControlAvailable(callPeer)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Enables remote control: " + enables);
                }
                InputEvtIQ inputIQ = new InputEvtIQ();
                if (enables) {
                    inputIQ.setAction(InputEvtAction.START);
                } else {
                    inputIQ.setAction(InputEvtAction.STOP);
                }
                inputIQ.setType(Type.SET);
                inputIQ.setFrom(((ProtocolProviderServiceJabberImpl) this.parentProvider).getOurJID());
                inputIQ.setTo(callPeer.getAddress());
                Connection connection = ((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection();
                PacketCollector collector = connection.createPacketCollector(new PacketIDFilter(inputIQ.getPacketID()));
                connection.sendPacket(inputIQ);
                Packet p = collector.nextResult((long) SmackConfiguration.getPacketReplyTimeout());
                if (enables) {
                    receivedResponseToIqStart(callPeer, p);
                } else {
                    receivedResponseToIqStop(callPeer, p);
                }
                collector.cancel();
            }
        }
    }

    private void receivedResponseToIqStart(CallPeer callPeer, Packet p) {
        if (p == null || ((IQ) p).getType() != Type.RESULT) {
            logger.info("Remote peer has not received/accepted the START action to grant the remote desktop control." + (p == null ? "\n\tPacket is null (IQ request timeout)." : "\n\tPacket: " + p.toXML()));
        } else {
            this.callPeers.add(callPeer.getAddress());
        }
    }

    private void receivedResponseToIqStop(CallPeer callPeer, Packet p) {
        if (p == null || ((IQ) p).getType() == Type.ERROR) {
            logger.info("Remote peer has not received/accepted the STOP action to grant the remote desktop control." + (p == null ? "\n\tPacket is null (IQ request timeout)." : "\n\tPacket: " + p.toXML()));
        }
        this.callPeers.remove(callPeer.getAddress());
    }

    public boolean isRemoteControlAvailable(CallPeer callPeer) {
        DiscoverInfo discoverInfo = ((AbstractCallPeerJabberGTalkImpl) callPeer).getDiscoveryInfo();
        return ((ProtocolProviderServiceJabberImpl) this.parentProvider).getDiscoveryManager().includesFeature(InputEvtIQ.NAMESPACE_SERVER) && discoverInfo != null && discoverInfo.containsFeature(InputEvtIQ.NAMESPACE_CLIENT);
    }
}
