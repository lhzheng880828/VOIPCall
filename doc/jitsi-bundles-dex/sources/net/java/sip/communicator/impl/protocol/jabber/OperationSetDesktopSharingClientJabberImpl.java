package net.java.sip.communicator.impl.protocol.jabber;

import java.util.List;
import net.java.sip.communicator.impl.protocol.jabber.extensions.inputevt.InputEvtAction;
import net.java.sip.communicator.impl.protocol.jabber.extensions.inputevt.InputEvtIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.inputevt.RemoteControlExtension;
import net.java.sip.communicator.service.protocol.AbstractOperationSetDesktopSharingClient;
import net.java.sip.communicator.service.protocol.CallPeer;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.service.protocol.event.RemoteControlListener;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.event.ComponentEvent;
import org.jitsi.android.util.java.awt.event.KeyEvent;
import org.jitsi.android.util.java.awt.event.MouseEvent;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.packet.DiscoverInfo;

public class OperationSetDesktopSharingClientJabberImpl extends AbstractOperationSetDesktopSharingClient<ProtocolProviderServiceJabberImpl> implements RegistrationStateChangeListener, PacketListener, PacketFilter {
    public OperationSetDesktopSharingClientJabberImpl(ProtocolProviderServiceJabberImpl parentProvider) {
        super(parentProvider);
        parentProvider.addRegistrationStateChangeListener(this);
    }

    public void sendKeyboardEvent(CallPeer callPeer, KeyEvent event) {
        sendRemoteControlExtension(callPeer, new RemoteControlExtension((ComponentEvent) event));
    }

    public void sendMouseEvent(CallPeer callPeer, MouseEvent event) {
        sendRemoteControlExtension(callPeer, new RemoteControlExtension((ComponentEvent) event));
    }

    public void sendMouseEvent(CallPeer callPeer, MouseEvent event, Dimension videoPanelSize) {
        sendRemoteControlExtension(callPeer, new RemoteControlExtension(event, videoPanelSize));
    }

    private void sendRemoteControlExtension(CallPeer callPeer, RemoteControlExtension payload) {
        DiscoverInfo discoverInfo = ((CallPeerJabberImpl) callPeer).getDiscoveryInfo();
        if (((ProtocolProviderServiceJabberImpl) this.parentProvider).getDiscoveryManager().includesFeature(InputEvtIQ.NAMESPACE_CLIENT) && discoverInfo != null && discoverInfo.containsFeature(InputEvtIQ.NAMESPACE_SERVER)) {
            InputEvtIQ inputIQ = new InputEvtIQ();
            inputIQ.setAction(InputEvtAction.NOTIFY);
            inputIQ.setType(Type.SET);
            inputIQ.setFrom(((ProtocolProviderServiceJabberImpl) this.parentProvider).getOurJID());
            inputIQ.setTo(callPeer.getAddress());
            inputIQ.addRemoteControl(payload);
            ((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection().sendPacket(inputIQ);
        }
    }

    public void registrationStateChanged(RegistrationStateChangeEvent evt) {
        OperationSetDesktopSharingServerJabberImpl.registrationStateChanged(evt, this, this, ((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection());
    }

    public void processPacket(Packet packet) {
        InputEvtIQ inputIQ = (InputEvtIQ) packet;
        if (inputIQ.getType() == Type.SET && inputIQ.getAction() != InputEvtAction.NOTIFY) {
            ((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection().sendPacket(IQ.createResultIQ(inputIQ));
            String callPeerID = inputIQ.getFrom();
            if (callPeerID != null) {
                CallPeer callPeer = getListenerCallPeer(callPeerID);
                if (callPeer == null) {
                    return;
                }
                if (inputIQ.getAction() == InputEvtAction.START) {
                    fireRemoteControlGranted(callPeer);
                } else if (inputIQ.getAction() == InputEvtAction.STOP) {
                    fireRemoteControlRevoked(callPeer);
                }
            }
        }
    }

    public boolean accept(Packet packet) {
        return packet instanceof InputEvtIQ;
    }

    /* access modifiers changed from: protected */
    public CallPeer getListenerCallPeer(String callPeerAddress) {
        List<RemoteControlListener> listeners = getListeners();
        for (int i = 0; i < listeners.size(); i++) {
            CallPeerJabberImpl callPeer = (CallPeerJabberImpl) ((RemoteControlListener) listeners.get(i)).getCallPeer();
            if (callPeer.getAddress().equals(callPeerAddress)) {
                return callPeer;
            }
        }
        return null;
    }
}
