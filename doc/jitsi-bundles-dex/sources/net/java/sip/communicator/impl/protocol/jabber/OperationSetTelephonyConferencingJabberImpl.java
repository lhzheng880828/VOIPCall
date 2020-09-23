package net.java.sip.communicator.impl.protocol.jabber;

import java.util.Arrays;
import java.util.Iterator;
import net.java.sip.communicator.impl.protocol.jabber.extensions.coin.CoinIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CoinPacketExtension;
import net.java.sip.communicator.service.protocol.Call;
import net.java.sip.communicator.service.protocol.CallPeer;
import net.java.sip.communicator.service.protocol.CallPeerState;
import net.java.sip.communicator.service.protocol.CallState;
import net.java.sip.communicator.service.protocol.ChatRoom;
import net.java.sip.communicator.service.protocol.ConferenceDescription;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetMultiUserChat;
import net.java.sip.communicator.service.protocol.OperationSetVideoBridge;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.CallChangeEvent;
import net.java.sip.communicator.service.protocol.event.CallChangeListener;
import net.java.sip.communicator.service.protocol.event.CallPeerEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.service.protocol.media.AbstractOperationSetTelephonyConferencing;
import net.java.sip.communicator.service.protocol.media.ConferenceInfoDocument;
import net.java.sip.communicator.service.protocol.media.MediaAwareCallConference;
import net.java.sip.communicator.service.protocol.media.MediaAwareCallPeer;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.util.xml.XMLException;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.util.StringUtils;

public class OperationSetTelephonyConferencingJabberImpl extends AbstractOperationSetTelephonyConferencing<ProtocolProviderServiceJabberImpl, OperationSetBasicTelephonyJabberImpl, CallJabberImpl, CallPeerJabberImpl, String> implements RegistrationStateChangeListener, PacketListener, PacketFilter {
    private static final int COIN_MIN_INTERVAL = 200;
    private static final Logger logger = Logger.getLogger(OperationSetTelephonyConferencingJabberImpl.class);
    private final Object lock = new Object();

    public OperationSetTelephonyConferencingJabberImpl(ProtocolProviderServiceJabberImpl parentProvider) {
        super(parentProvider);
    }

    /* access modifiers changed from: protected */
    public void notifyCallPeers(Call call) {
        if (call.isConferenceFocus()) {
            synchronized (this.lock) {
                Iterator<? extends CallPeer> i = call.getCallPeers();
                while (i.hasNext()) {
                    notify((CallPeer) i.next());
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void notify(CallPeer callPeer) {
        if (callPeer instanceof CallPeerJabberImpl) {
            CallPeerState peerState = callPeer.getState();
            if (peerState != CallPeerState.CONNECTING && peerState != CallPeerState.UNKNOWN && peerState != CallPeerState.INITIATING_CALL && peerState != CallPeerState.DISCONNECTED && peerState != CallPeerState.FAILED) {
                final CallPeerJabberImpl callPeerJabber = (CallPeerJabberImpl) callPeer;
                final long timeSinceLastCoin = System.currentTimeMillis() - callPeerJabber.getLastConferenceInfoSentTimestamp();
                if (timeSinceLastCoin >= 200) {
                    ConferenceInfoDocument diff;
                    String to = ((OperationSetBasicTelephonyJabberImpl) getBasicTelephony()).getFullCalleeURI(callPeer.getAddress());
                    try {
                        if (!((ProtocolProviderServiceJabberImpl) this.parentProvider).getDiscoveryManager().discoverInfo(to).containsFeature(ProtocolProviderServiceJabberImpl.URN_XMPP_JINGLE_COIN)) {
                            logger.info(callPeer.getAddress() + " does not support COIN");
                            callPeerJabber.setConfInfoScheduled(false);
                            return;
                        }
                    } catch (XMPPException xmppe) {
                        logger.warn("Failed to retrieve DiscoverInfo for " + to, xmppe);
                    }
                    ConferenceInfoDocument currentConfInfo = getCurrentConferenceInfo(callPeerJabber);
                    ConferenceInfoDocument lastSentConfInfo = callPeerJabber.getLastConferenceInfoSent();
                    if (lastSentConfInfo == null) {
                        diff = currentConfInfo;
                    } else {
                        diff = getConferenceInfoDiff(lastSentConfInfo, currentConfInfo);
                    }
                    if (diff != null) {
                        int newVersion = lastSentConfInfo == null ? 1 : lastSentConfInfo.getVersion() + 1;
                        diff.setVersion(newVersion);
                        IQ iq = getConferenceInfo(callPeerJabber, diff);
                        if (iq != null) {
                            ((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection().sendPacket(iq);
                            currentConfInfo.setVersion(newVersion);
                            callPeerJabber.setLastConferenceInfoSent(currentConfInfo);
                            callPeerJabber.setLastConferenceInfoSentTimestamp(System.currentTimeMillis());
                        }
                    }
                    callPeerJabber.setConfInfoScheduled(false);
                } else if (!callPeerJabber.isConfInfoScheduled()) {
                    logger.info("Scheduling to send a COIN to " + callPeerJabber);
                    callPeerJabber.setConfInfoScheduled(true);
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                Thread.sleep(201 - timeSinceLastCoin);
                            } catch (InterruptedException e) {
                            }
                            OperationSetTelephonyConferencingJabberImpl.this.notify(callPeerJabber);
                        }
                    }).start();
                }
            }
        }
    }

    private IQ getConferenceInfo(CallPeerJabberImpl callPeer, final ConferenceInfoDocument confInfo) {
        if (callPeer.getSID() == null) {
            return null;
        }
        IQ iq = new IQ() {
            public String getChildElementXML() {
                return confInfo.toXml();
            }
        };
        iq.setFrom(((ProtocolProviderServiceJabberImpl) ((CallJabberImpl) callPeer.getCall()).getProtocolProvider()).getOurJID());
        iq.setTo(callPeer.getAddress());
        iq.setType(Type.SET);
        return iq;
    }

    public void registrationStateChanged(RegistrationStateChangeEvent evt) {
        OperationSetTelephonyConferencingJabberImpl.super.registrationStateChanged(evt);
        RegistrationState registrationState = evt.getNewState();
        if (RegistrationState.REGISTERED.equals(registrationState)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Subscribes to Coin packets");
            }
            subscribeForCoinPackets();
        } else if (RegistrationState.UNREGISTERED.equals(registrationState)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Unsubscribes to Coin packets");
            }
            unsubscribeForCoinPackets();
        }
    }

    /* access modifiers changed from: protected */
    public CallJabberImpl createOutgoingCall() throws OperationFailedException {
        return new CallJabberImpl((OperationSetBasicTelephonyJabberImpl) getBasicTelephony());
    }

    /* access modifiers changed from: protected */
    public CallPeer doInviteCalleeToCall(String calleeAddress, CallJabberImpl call) throws OperationFailedException {
        return ((OperationSetBasicTelephonyJabberImpl) getBasicTelephony()).createOutgoingCall(call, calleeAddress, Arrays.asList(new PacketExtension[]{new CoinPacketExtension(true)}));
    }

    /* access modifiers changed from: protected */
    public String parseAddressString(String calleeAddressString) throws OperationFailedException {
        return ((OperationSetBasicTelephonyJabberImpl) getBasicTelephony()).getFullCalleeURI(calleeAddressString);
    }

    private void subscribeForCoinPackets() {
        ((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection().addPacketListener(this, this);
    }

    private void unsubscribeForCoinPackets() {
        XMPPConnection connection = ((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection();
        if (connection != null) {
            connection.removePacketListener(this);
        }
    }

    public boolean accept(Packet packet) {
        return packet instanceof CoinIQ;
    }

    public void processPacket(Packet packet) {
        CoinIQ coinIQ = (CoinIQ) packet;
        String errorMessage = null;
        Type type = coinIQ.getType();
        if (type == Type.SET) {
            ((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection().sendPacket(IQ.createResultIQ(coinIQ));
        } else if (type == Type.ERROR) {
            XMPPError error = coinIQ.getError();
            if (error != null) {
                String msg = error.getMessage();
                errorMessage = (msg != null ? msg + Separators.SP : "") + "Error code: " + error.getCode();
            }
            logger.error("Received error in COIN packet. " + errorMessage);
        }
        String sid = coinIQ.getSID();
        if (sid != null) {
            CallPeerJabberImpl callPeer = (CallPeerJabberImpl) ((OperationSetBasicTelephonyJabberImpl) getBasicTelephony()).getActiveCallsRepository().findCallPeer(sid);
            if (callPeer == null) {
                return;
            }
            if (type == Type.ERROR) {
                callPeer.fireConferenceMemberErrorEvent(errorMessage);
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Processing COIN from " + coinIQ.getFrom() + " (version=" + coinIQ.getVersion() + Separators.RPAREN);
            }
            handleCoin(callPeer, coinIQ);
        }
    }

    private void handleCoin(CallPeerJabberImpl callPeer, CoinIQ coinIQ) {
        try {
            setConferenceInfoXML(callPeer, coinIQ.getChildElementXML());
        } catch (XMLException e) {
            logger.error("Could not handle received COIN from " + callPeer + ": " + coinIQ);
        }
    }

    /* access modifiers changed from: protected */
    public ConferenceInfoDocument getCurrentConferenceInfo(MediaAwareCallPeer<?, ?, ?> callPeer) {
        ConferenceInfoDocument confInfo = OperationSetTelephonyConferencingJabberImpl.super.getCurrentConferenceInfo(callPeer);
        if ((callPeer instanceof CallPeerJabberImpl) && confInfo != null) {
            confInfo.setSid(((CallPeerJabberImpl) callPeer).getSID());
        }
        return confInfo;
    }

    /* access modifiers changed from: protected */
    public String getLocalEntity(CallPeer callPeer) {
        String chatRoomName = StringUtils.parseBareAddress(((CallPeerJabberImpl) callPeer).getSessionIQ().getFrom());
        ChatRoom room = ((OperationSetMultiUserChatJabberImpl) ((ProtocolProviderServiceJabberImpl) this.parentProvider).getOperationSet(OperationSetMultiUserChat.class)).getChatRoom(chatRoomName);
        if (room != null) {
            return "xmpp:" + chatRoomName + Separators.SLASH + room.getUserNickname();
        }
        return "xmpp:" + ((ProtocolProviderServiceJabberImpl) this.parentProvider).getOurJID();
    }

    /* access modifiers changed from: protected */
    public String getLocalDisplayName() {
        return null;
    }

    public ConferenceDescription setupConference(final ChatRoom chatRoom) {
        OperationSetVideoBridge videoBridge = (OperationSetVideoBridge) ((ProtocolProviderServiceJabberImpl) this.parentProvider).getOperationSet(OperationSetVideoBridge.class);
        boolean isVideobridge = videoBridge != null && videoBridge.isActive();
        CallJabberImpl call = new CallJabberImpl((OperationSetBasicTelephonyJabberImpl) getBasicTelephony());
        call.setAutoAnswer(true);
        String uri = "xmpp:" + chatRoom.getIdentifier() + Separators.SLASH + chatRoom.getUserNickname();
        ConferenceDescription cd = new ConferenceDescription(uri, call.getCallID());
        call.addCallChangeListener(new CallChangeListener() {
            public void callStateChanged(CallChangeEvent ev) {
                if (CallState.CALL_ENDED.equals(ev.getNewValue())) {
                    chatRoom.publishConference(null, null);
                }
            }

            public void callPeerRemoved(CallPeerEvent ev) {
            }

            public void callPeerAdded(CallPeerEvent ev) {
            }
        });
        if (isVideobridge) {
            call.setConference(new MediaAwareCallConference(true));
            cd.addTransport("urn:xmpp:jingle:transports:raw-udp:1");
        }
        if (logger.isInfoEnabled()) {
            logger.info("Setup a conference with uri=" + uri + " and callid=" + call.getCallID() + ". Videobridge in use: " + isVideobridge);
        }
        return cd;
    }
}
