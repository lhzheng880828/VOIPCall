package net.java.sip.communicator.impl.protocol.jabber;

import java.util.Iterator;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriConferenceIQ;
import net.java.sip.communicator.service.protocol.Call;
import net.java.sip.communicator.service.protocol.CallPeer;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationNotSupportedException;
import net.java.sip.communicator.service.protocol.OperationSetBasicTelephony;
import net.java.sip.communicator.service.protocol.OperationSetTelephonyConferencing;
import net.java.sip.communicator.service.protocol.OperationSetVideoBridge;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.service.protocol.media.MediaAwareCallConference;
import net.java.sip.communicator.util.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;

public class OperationSetVideoBridgeImpl implements OperationSetVideoBridge, PacketFilter, PacketListener, RegistrationStateChangeListener {
    private static final Logger logger = Logger.getLogger(OperationSetVideoBridgeImpl.class);
    private final ProtocolProviderServiceJabberImpl protocolProvider;

    public OperationSetVideoBridgeImpl(ProtocolProviderServiceJabberImpl protocolProvider) {
        this.protocolProvider = protocolProvider;
        this.protocolProvider.addRegistrationStateChangeListener(this);
    }

    public boolean accept(Packet packet) {
        return packet instanceof ColibriConferenceIQ;
    }

    public Call createConfCall(String[] callees) throws OperationFailedException, OperationNotSupportedException {
        return ((OperationSetTelephonyConferencing) this.protocolProvider.getOperationSet(OperationSetTelephonyConferencing.class)).createConfCall(callees, new MediaAwareCallConference(true));
    }

    public CallPeer inviteCalleeToCall(String uri, Call call) throws OperationFailedException, OperationNotSupportedException {
        return ((OperationSetTelephonyConferencing) this.protocolProvider.getOperationSet(OperationSetTelephonyConferencing.class)).inviteCalleeToCall(uri, call);
    }

    public boolean isActive() {
        String jitsiVideobridge = this.protocolProvider.getJitsiVideobridge();
        return jitsiVideobridge != null && jitsiVideobridge.length() > 0;
    }

    private void processColibriConferenceIQ(ColibriConferenceIQ conferenceIQ) {
        if (Type.SET.equals(conferenceIQ.getType()) && conferenceIQ.getID() != null) {
            OperationSetBasicTelephony<?> basicTelephony = (OperationSetBasicTelephony) this.protocolProvider.getOperationSet(OperationSetBasicTelephony.class);
            if (basicTelephony != null) {
                Iterator<? extends Call> i = basicTelephony.getActiveCalls();
                while (i.hasNext()) {
                    Call call = (Call) i.next();
                    if (call instanceof CallJabberImpl) {
                        CallJabberImpl callJabberImpl = (CallJabberImpl) call;
                        MediaAwareCallConference conference = callJabberImpl.getConference();
                        if (conference != null && conference.isJitsiVideobridge() && callJabberImpl.processColibriConferenceIQ(conferenceIQ)) {
                            return;
                        }
                    }
                }
            }
        }
    }

    public void processPacket(Packet packet) {
        IQ iq = (IQ) packet;
        if (iq.getType() == Type.SET) {
            this.protocolProvider.getConnection().sendPacket(IQ.createResultIQ(iq));
        }
        boolean interrupted = false;
        try {
            processColibriConferenceIQ((ColibriConferenceIQ) iq);
        } catch (Throwable t) {
            logger.error("An error occurred during the processing of a " + packet.getClass().getName() + " packet", t);
            if (t instanceof InterruptedException) {
                interrupted = true;
            } else if (t instanceof ThreadDeath) {
                ThreadDeath t2 = (ThreadDeath) t;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    public void registrationStateChanged(RegistrationStateChangeEvent ev) {
        RegistrationState registrationState = ev.getNewState();
        if (RegistrationState.REGISTERED.equals(registrationState)) {
            this.protocolProvider.getConnection().addPacketListener(this, this);
        } else if (RegistrationState.UNREGISTERED.equals(registrationState)) {
            XMPPConnection connection = this.protocolProvider.getConnection();
            if (connection != null) {
                connection.removePacketListener(this);
            }
        }
    }
}
