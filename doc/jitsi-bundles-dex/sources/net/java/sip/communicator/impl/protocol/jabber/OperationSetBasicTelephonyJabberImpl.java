package net.java.sip.communicator.impl.protocol.jabber;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.java.sip.communicator.impl.protocol.jabber.extensions.CallIdPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.ConferenceDescriptionPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CoinPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleAction;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleIQProvider;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.Reason;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ReasonPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RtpDescriptionPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.SessionInfoPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.TransferPacketExtension;
import net.java.sip.communicator.service.protocol.AbstractCallPeer;
import net.java.sip.communicator.service.protocol.Call;
import net.java.sip.communicator.service.protocol.CallConference;
import net.java.sip.communicator.service.protocol.CallPeer;
import net.java.sip.communicator.service.protocol.CallPeerState;
import net.java.sip.communicator.service.protocol.CallState;
import net.java.sip.communicator.service.protocol.ChatRoom;
import net.java.sip.communicator.service.protocol.ConferenceDescription;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetAdvancedTelephony;
import net.java.sip.communicator.service.protocol.OperationSetSecureSDesTelephony;
import net.java.sip.communicator.service.protocol.OperationSetSecureZrtpTelephony;
import net.java.sip.communicator.service.protocol.PresenceStatus;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.TransferAuthority;
import net.java.sip.communicator.service.protocol.event.CallChangeEvent;
import net.java.sip.communicator.service.protocol.event.CallChangeListener;
import net.java.sip.communicator.service.protocol.event.CallPeerEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.service.protocol.media.AbstractOperationSetBasicTelephony;
import net.java.sip.communicator.service.protocol.media.MediaAwareCallPeer;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.message.Response;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;

public class OperationSetBasicTelephonyJabberImpl extends AbstractOperationSetBasicTelephony<ProtocolProviderServiceJabberImpl> implements RegistrationStateChangeListener, PacketListener, PacketFilter, OperationSetSecureSDesTelephony, OperationSetSecureZrtpTelephony, OperationSetAdvancedTelephony<ProtocolProviderServiceJabberImpl> {
    private static final String GOOGLE_VOICE_DOMAIN = "voice.google.com";
    private static final Logger logger = Logger.getLogger(OperationSetBasicTelephonyJabberImpl.class);
    private ActiveCallsRepositoryJabberGTalkImpl<CallJabberImpl, CallPeerJabberImpl> activeCallsRepository = new ActiveCallsRepositoryJabberGTalkImpl(this);
    private final ProtocolProviderServiceJabberImpl protocolProvider;

    public OperationSetBasicTelephonyJabberImpl(ProtocolProviderServiceJabberImpl protocolProvider) {
        this.protocolProvider = protocolProvider;
        this.protocolProvider.addRegistrationStateChangeListener(this);
    }

    public void registrationStateChanged(RegistrationStateChangeEvent evt) {
        RegistrationState registrationState = evt.getNewState();
        if (registrationState == RegistrationState.REGISTERING) {
            ProviderManager.getInstance().addIQProvider(JingleIQ.ELEMENT_NAME, "urn:xmpp:jingle:1", new JingleIQProvider());
            subscribeForJinglePackets();
            if (logger.isInfoEnabled()) {
                logger.info("Jingle : ON ");
            }
        } else if (registrationState == RegistrationState.UNREGISTERED) {
            unsubscribeForJinglePackets();
            if (logger.isInfoEnabled()) {
                logger.info("Jingle : OFF ");
            }
        }
    }

    public Call createCall(String callee, CallConference conference) throws OperationFailedException {
        CallJabberImpl call = new CallJabberImpl(this);
        if (conference != null) {
            call.setConference(conference);
        }
        CallPeer callPeer = createOutgoingCall(call, callee);
        if (callPeer == null) {
            throw new OperationFailedException("Failed to create outgoing call because no peer was created", 4);
        }
        Call callOfCallPeer = callPeer.getCall();
        if (!(callOfCallPeer == call || conference == null)) {
            callOfCallPeer.setConference(conference);
        }
        return callOfCallPeer;
    }

    public CallJabberImpl createCall(ConferenceDescription cd, final ChatRoom chatRoom) throws OperationFailedException {
        final CallJabberImpl call = new CallJabberImpl(this);
        ((ChatRoomJabberImpl) chatRoom).addConferenceCall(call);
        call.addCallChangeListener(new CallChangeListener() {
            public void callPeerAdded(CallPeerEvent ev) {
            }

            public void callPeerRemoved(CallPeerEvent ev) {
            }

            public void callStateChanged(CallChangeEvent ev) {
                if (CallState.CALL_ENDED.equals(ev.getNewValue())) {
                    ((ChatRoomJabberImpl) chatRoom).removeConferenceCall(call);
                }
            }
        });
        String remoteJid = cd.getUri();
        if (remoteJid.startsWith("xmpp:")) {
            remoteJid = remoteJid.substring(5, remoteJid.length());
        }
        List<PacketExtension> sessionInitiateExtensions = new ArrayList(2);
        String callid = cd.getCallId();
        if (callid != null) {
            sessionInitiateExtensions.add(new CallIdPacketExtension(callid));
        }
        call.initiateSession(remoteJid, null, sessionInitiateExtensions, cd.getSupportedTransports());
        return call;
    }

    /* access modifiers changed from: 0000 */
    public AbstractCallPeer<?, ?> createOutgoingCall(CallJabberImpl call, String calleeAddress) throws OperationFailedException {
        return createOutgoingCall(call, calleeAddress, null);
    }

    /* access modifiers changed from: 0000 */
    public AbstractCallPeer<?, ?> createOutgoingCall(CallJabberImpl call, String calleeAddress, Iterable<PacketExtension> iterable) throws OperationFailedException {
        return createOutgoingCall(call, calleeAddress, null, null);
    }

    /* access modifiers changed from: 0000 */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x012b  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0160  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0135  */
    /* JADX WARNING: Missing block: B:24:0x00c7, code skipped:
            if (r5.equals(r20.substring(r20.indexOf(64) + 1)) == false) goto L_0x00c9;
     */
    public net.java.sip.communicator.service.protocol.AbstractCallPeer<?, ?> createOutgoingCall(net.java.sip.communicator.impl.protocol.jabber.CallJabberImpl r19, java.lang.String r20, java.lang.String r21, java.lang.Iterable<org.jivesoftware.smack.packet.PacketExtension> r22) throws net.java.sip.communicator.service.protocol.OperationFailedException {
        /*
        r18 = this;
        r15 = logger;
        r15 = r15.isInfoEnabled();
        if (r15 == 0) goto L_0x0024;
    L_0x0008:
        r15 = logger;
        r16 = new java.lang.StringBuilder;
        r16.<init>();
        r17 = "Creating outgoing call to ";
        r16 = r16.append(r17);
        r0 = r16;
        r1 = r20;
        r16 = r0.append(r1);
        r16 = r16.toString();
        r15.info(r16);
    L_0x0024:
        r0 = r18;
        r15 = r0.protocolProvider;
        r15 = r15.getConnection();
        if (r15 == 0) goto L_0x0030;
    L_0x002e:
        if (r19 != 0) goto L_0x003a;
    L_0x0030:
        r15 = new net.java.sip.communicator.service.protocol.OperationFailedException;
        r16 = "Failed to create OutgoingJingleSession. We don't have a valid XMPPConnection.";
        r17 = 4;
        r15.<init>(r16, r17);
        throw r15;
    L_0x003a:
        r0 = r18;
        r15 = r0.protocolProvider;
        r8 = r15.isGmailOrGoogleAppsAccount();
        r9 = 0;
        if (r8 == 0) goto L_0x0065;
    L_0x0045:
        r15 = "@";
        r0 = r20;
        r15 = r0.contains(r15);
        if (r15 != 0) goto L_0x0117;
    L_0x004f:
        r15 = new java.lang.StringBuilder;
        r15.<init>();
        r0 = r20;
        r15 = r15.append(r0);
        r16 = "@voice.google.com";
        r15 = r15.append(r16);
        r20 = r15.toString();
        r9 = 1;
    L_0x0065:
        r15 = r18.getProtocolProvider();
        r3 = r15.getAccountID();
        r15 = 64;
        r0 = r20;
        r15 = r0.indexOf(r15);
        r16 = -1;
        r0 = r16;
        if (r15 != r0) goto L_0x00ab;
    L_0x007b:
        r15 = "OVERRIDE_PHONE_SUFFIX";
        r12 = r3.getAccountPropertyString(r15);
        r13 = 0;
        if (r12 == 0) goto L_0x008a;
    L_0x0084:
        r15 = r12.length();
        if (r15 != 0) goto L_0x0124;
    L_0x008a:
        r15 = r3.getUserID();
        r13 = org.jivesoftware.smack.util.StringUtils.parseServer(r15);
    L_0x0092:
        r15 = new java.lang.StringBuilder;
        r15.<init>();
        r0 = r20;
        r15 = r15.append(r0);
        r16 = "@";
        r15 = r15.append(r16);
        r15 = r15.append(r13);
        r20 = r15.toString();
    L_0x00ab:
        r15 = "TELEPHONY_BYPASS_GTALK_CAPS";
        r5 = r3.getAccountPropertyString(r15);
        if (r5 == 0) goto L_0x00c9;
    L_0x00b3:
        r15 = 64;
        r0 = r20;
        r15 = r0.indexOf(r15);
        r15 = r15 + 1;
        r0 = r20;
        r15 = r0.substring(r15);
        r15 = r5.equals(r15);
        if (r15 != 0) goto L_0x00cb;
    L_0x00c9:
        if (r9 == 0) goto L_0x0127;
    L_0x00cb:
        r4 = 1;
    L_0x00cc:
        r15 = r18.getProtocolProvider();
        r16 = net.java.sip.communicator.service.protocol.OperationSetMultiUserChat.class;
        r15 = r15.getOperationSet(r16);
        r15 = (net.java.sip.communicator.service.protocol.OperationSetMultiUserChat) r15;
        r0 = r20;
        r10 = r15.isPrivateMessagingContact(r0);
        r15 = r18.getProtocolProvider();
        r15 = r15.getConnection();
        r15 = r15.getRoster();
        r16 = org.jivesoftware.smack.util.StringUtils.parseBareAddress(r20);
        r15 = r15.contains(r16);
        if (r15 != 0) goto L_0x0129;
    L_0x00f4:
        if (r10 != 0) goto L_0x0129;
    L_0x00f6:
        if (r4 != 0) goto L_0x0129;
    L_0x00f8:
        r15 = new net.java.sip.communicator.service.protocol.OperationFailedException;
        r16 = new java.lang.StringBuilder;
        r16.<init>();
        r0 = r16;
        r1 = r20;
        r16 = r0.append(r1);
        r17 = " does not belong to our contact list";
        r16 = r16.append(r17);
        r16 = r16.toString();
        r17 = 404; // 0x194 float:5.66E-43 double:1.996E-321;
        r15.<init>(r16, r17);
        throw r15;
    L_0x0117:
        r15 = "voice.google.com";
        r0 = r20;
        r15 = r0.endsWith(r15);
        if (r15 == 0) goto L_0x0065;
    L_0x0121:
        r9 = 1;
        goto L_0x0065;
    L_0x0124:
        r13 = r12;
        goto L_0x0092;
    L_0x0127:
        r4 = 0;
        goto L_0x00cc;
    L_0x0129:
        if (r21 != 0) goto L_0x0133;
    L_0x012b:
        r0 = r18;
        r1 = r20;
        r21 = r0.discoverFullJid(r1, r4);
    L_0x0133:
        if (r21 != 0) goto L_0x0160;
    L_0x0135:
        r15 = new net.java.sip.communicator.service.protocol.OperationFailedException;
        r16 = new java.lang.StringBuilder;
        r16.<init>();
        r17 = "Failed to create outgoing call to ";
        r16 = r16.append(r17);
        r0 = r16;
        r1 = r20;
        r16 = r0.append(r1);
        r17 = ". Could not find a resource which supports ";
        r16 = r16.append(r17);
        r17 = "Jingle or Google Talk";
        r16 = r16.append(r17);
        r16 = r16.toString();
        r17 = 4;
        r15.<init>(r16, r17);
        throw r15;
    L_0x0160:
        r6 = 0;
        r0 = r18;
        r15 = r0.protocolProvider;	 Catch:{ XMPPException -> 0x01a4 }
        r15 = r15.getDiscoveryManager();	 Catch:{ XMPPException -> 0x01a4 }
        r0 = r21;
        r6 = r15.discoverInfo(r0);	 Catch:{ XMPPException -> 0x01a4 }
    L_0x016f:
        if (r6 == 0) goto L_0x01c4;
    L_0x0171:
        r15 = logger;
        r15 = r15.isInfoEnabled();
        if (r15 == 0) goto L_0x0195;
    L_0x0179:
        r15 = logger;
        r16 = new java.lang.StringBuilder;
        r16.<init>();
        r0 = r16;
        r1 = r21;
        r16 = r0.append(r1);
        r17 = ": jingle supported ";
        r16 = r16.append(r17);
        r16 = r16.toString();
        r15.info(r16);
    L_0x0195:
        r11 = 0;
        if (r6 == 0) goto L_0x01a3;
    L_0x0198:
        r15 = 0;
        r0 = r19;
        r1 = r21;
        r2 = r22;
        r11 = r0.initiateSession(r1, r6, r2, r15);	 Catch:{ Throwable -> 0x020d }
    L_0x01a3:
        return r11;
    L_0x01a4:
        r7 = move-exception;
        r15 = logger;
        r16 = new java.lang.StringBuilder;
        r16.<init>();
        r17 = "could not retrieve info for ";
        r16 = r16.append(r17);
        r0 = r16;
        r1 = r21;
        r16 = r0.append(r1);
        r16 = r16.toString();
        r0 = r16;
        r15.warn(r0, r7);
        goto L_0x016f;
    L_0x01c4:
        r15 = logger;
        r15 = r15.isInfoEnabled();
        if (r15 == 0) goto L_0x01e8;
    L_0x01cc:
        r15 = logger;
        r16 = new java.lang.StringBuilder;
        r16.<init>();
        r0 = r16;
        r1 = r21;
        r16 = r0.append(r1);
        r17 = ": jingle not supported?";
        r16 = r16.append(r17);
        r16 = r16.toString();
        r15.info(r16);
    L_0x01e8:
        r15 = new net.java.sip.communicator.service.protocol.OperationFailedException;
        r16 = new java.lang.StringBuilder;
        r16.<init>();
        r17 = "Failed to create an outgoing call.\n";
        r16 = r16.append(r17);
        r0 = r16;
        r1 = r21;
        r16 = r0.append(r1);
        r17 = " does not support jingle or Google Talk";
        r16 = r16.append(r17);
        r16 = r16.toString();
        r17 = 4;
        r15.<init>(r16, r17);
        throw r15;
    L_0x020d:
        r14 = move-exception;
        r15 = r14 instanceof java.lang.ThreadDeath;
        if (r15 == 0) goto L_0x0215;
    L_0x0212:
        r14 = (java.lang.ThreadDeath) r14;
        throw r14;
    L_0x0215:
        r15 = new java.lang.StringBuilder;
        r15.<init>();
        r16 = "Failed to create a call to ";
        r15 = r15.append(r16);
        r0 = r21;
        r15 = r15.append(r0);
        r15 = r15.toString();
        r16 = 4;
        r17 = logger;
        r0 = r16;
        r1 = r17;
        net.java.sip.communicator.impl.protocol.jabber.ProtocolProviderServiceJabberImpl.throwOperationFailedException(r15, r0, r14, r1);
        goto L_0x01a3;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.jabber.OperationSetBasicTelephonyJabberImpl.createOutgoingCall(net.java.sip.communicator.impl.protocol.jabber.CallJabberImpl, java.lang.String, java.lang.String, java.lang.Iterable):net.java.sip.communicator.service.protocol.AbstractCallPeer");
    }

    private String discoverFullJid(String calleeAddress, boolean isAlwaysCallGtalk) {
        String fullCalleeURI = null;
        DiscoverInfo discoverInfo = null;
        int bestPriority = -1;
        boolean isGingle = false;
        String gingleURI = null;
        PresenceStatus jabberStatus = null;
        Iterator<Presence> it = getProtocolProvider().getConnection().getRoster().getPresences(calleeAddress);
        while (it.hasNext()) {
            Presence presence = (Presence) it.next();
            int priority = presence.getPriority() == Integer.MIN_VALUE ? 0 : presence.getPriority();
            String calleeURI = presence.getFrom();
            try {
                discoverInfo = this.protocolProvider.getDiscoveryManager().discoverInfo(calleeURI);
            } catch (XMPPException ex) {
                logger.warn("could not retrieve info for " + fullCalleeURI, ex);
            }
            boolean hasGtalkCaps = getProtocolProvider().isExtFeatureListSupported(calleeURI, ProtocolProviderServiceJabberImpl.CAPS_GTALK_WEB_VOICE);
            if (discoverInfo == null || !discoverInfo.containsFeature("urn:xmpp:jingle:1")) {
                if (this.protocolProvider.isGTalkTesting() && (hasGtalkCaps || isAlwaysCallGtalk)) {
                    if (priority > bestPriority) {
                        bestPriority = priority;
                        isGingle = true;
                        gingleURI = calleeURI;
                        jabberStatus = OperationSetPersistentPresenceJabberImpl.jabberStatusToPresenceStatus(presence, this.protocolProvider);
                    } else if (priority == bestPriority && jabberStatus != null && OperationSetPersistentPresenceJabberImpl.jabberStatusToPresenceStatus(presence, this.protocolProvider).compareTo(jabberStatus) > 0) {
                        isGingle = true;
                        gingleURI = calleeURI;
                        jabberStatus = OperationSetPersistentPresenceJabberImpl.jabberStatusToPresenceStatus(presence, this.protocolProvider);
                    }
                }
            } else if (priority > bestPriority) {
                bestPriority = priority;
                fullCalleeURI = calleeURI;
                isGingle = false;
                jabberStatus = OperationSetPersistentPresenceJabberImpl.jabberStatusToPresenceStatus(presence, this.protocolProvider);
            } else if (priority == bestPriority && jabberStatus != null) {
                PresenceStatus tempStatus = OperationSetPersistentPresenceJabberImpl.jabberStatusToPresenceStatus(presence, this.protocolProvider);
                if (tempStatus.compareTo(jabberStatus) > 0) {
                    fullCalleeURI = calleeURI;
                    isGingle = false;
                    jabberStatus = tempStatus;
                }
            }
        }
        if (isGingle) {
            fullCalleeURI = gingleURI;
        }
        if (logger.isInfoEnabled()) {
            logger.info("Full JID for outgoing call: " + fullCalleeURI + ", priority " + bestPriority);
        }
        return fullCalleeURI;
    }

    /* access modifiers changed from: 0000 */
    public String getFullCalleeURI(String calleeAddress) {
        return calleeAddress.indexOf(47) > 0 ? calleeAddress : this.protocolProvider.getConnection().getRoster().getPresence(calleeAddress).getFrom();
    }

    public Iterator<CallJabberImpl> getActiveCalls() {
        return this.activeCallsRepository.getActiveCalls();
    }

    public CallPeerJabberImpl getActiveCallPeer(String sid) {
        return (CallPeerJabberImpl) this.activeCallsRepository.findCallPeer(sid);
    }

    public synchronized void putOffHold(CallPeer peer) throws OperationFailedException {
        putOnHold(peer, false);
    }

    public synchronized void putOnHold(CallPeer peer) throws OperationFailedException {
        putOnHold(peer, true);
    }

    private void putOnHold(CallPeer peer, boolean on) throws OperationFailedException {
        if (peer instanceof CallPeerJabberImpl) {
            ((CallPeerJabberImpl) peer).putOnHold(on);
        }
    }

    public synchronized void hangupCallPeer(CallPeer peer) throws ClassCastException, OperationFailedException {
        hangupCallPeer(peer, Response.OK, null);
    }

    public void hangupCallPeer(CallPeer peer, int reasonCode, String reasonText) {
        boolean failed = reasonCode != Response.OK;
        ReasonPacketExtension reasonPacketExt = null;
        if (failed && reasonText != null) {
            Reason reason = convertReasonCodeToSIPCode(reasonCode);
            if (reason != null) {
                reasonPacketExt = new ReasonPacketExtension(reason, reasonText, null);
            }
        }
        if (peer instanceof CallPeerJabberImpl) {
            ((CallPeerJabberImpl) peer).hangup(failed, reasonText, reasonPacketExt);
        }
    }

    private static Reason convertReasonCodeToSIPCode(int reasonCode) {
        switch (reasonCode) {
            case Response.OK /*200*/:
                return Reason.SUCCESS;
            case Response.REQUEST_TIMEOUT /*408*/:
                return Reason.TIMEOUT;
            case Response.BUSY_HERE /*486*/:
                return Reason.BUSY;
            case 609:
                return Reason.SECURITY_ERROR;
            default:
                return null;
        }
    }

    public void answerCallPeer(CallPeer peer) throws OperationFailedException {
        if (peer instanceof CallPeerJabberImpl) {
            ((CallPeerJabberImpl) peer).answer();
        }
    }

    public void shutdown() {
        if (logger.isTraceEnabled()) {
            logger.trace("Ending all active calls. ");
        }
        Iterator<CallJabberImpl> activeCalls = this.activeCallsRepository.getActiveCalls();
        while (activeCalls.hasNext()) {
            Iterator<CallPeerJabberImpl> callPeers = ((CallJabberImpl) activeCalls.next()).getCallPeers();
            while (callPeers.hasNext()) {
                CallPeer peer = (CallPeer) callPeers.next();
                try {
                    hangupCallPeer(peer);
                } catch (Exception ex) {
                    logger.warn("Failed to properly hangup peer " + peer, ex);
                }
            }
        }
    }

    private void subscribeForJinglePackets() {
        this.protocolProvider.getConnection().addPacketListener(this, this);
    }

    private void unsubscribeForJinglePackets() {
        XMPPConnection connection = this.protocolProvider.getConnection();
        if (connection != null) {
            connection.removePacketListener(this);
        }
    }

    public boolean accept(Packet packet) {
        if (!(packet instanceof JingleIQ)) {
            AbstractCallPeer<?, ?> callPeer = this.activeCallsRepository.findCallPeerBySessInitPacketID(packet.getPacketID());
            if (callPeer == null) {
                return false;
            }
            XMPPError error = packet.getError();
            if (error == null) {
                return false;
            }
            String message;
            String errorMessage = error.getMessage();
            logger.error("Received an error: code=" + error.getCode() + " message=" + errorMessage);
            if (errorMessage == null) {
                Roster roster = getProtocolProvider().getConnection().getRoster();
                String packetFrom = packet.getFrom();
                message = "Service unavailable";
                if (!roster.contains(packetFrom)) {
                    message = message + ": try adding the contact " + packetFrom + " to your contact list first.";
                }
            } else {
                message = errorMessage;
            }
            callPeer.setState(CallPeerState.FAILED, message);
            return false;
        } else if (!(packet instanceof JingleIQ)) {
            return false;
        } else {
            JingleIQ jingleIQ = (JingleIQ) packet;
            if (jingleIQ.getAction() == JingleAction.SESSION_INITIATE) {
                return jingleIQ.containsContentChildOfType(RtpDescriptionPacketExtension.class);
            }
            if (this.activeCallsRepository.findSID(jingleIQ.getSID()) != null) {
                return true;
            }
            return false;
        }
    }

    public void processPacket(Packet packet) {
        IQ iq = (IQ) packet;
        if (iq.getType() == Type.SET) {
            this.protocolProvider.getConnection().sendPacket(IQ.createResultIQ(iq));
        }
        try {
            if (iq instanceof JingleIQ) {
                processJingleIQ((JingleIQ) iq);
            }
        } catch (Throwable t) {
            if (logger.isInfoEnabled()) {
                String packetClass;
                if (iq instanceof JingleIQ) {
                    packetClass = "Jingle";
                } else {
                    packetClass = packet.getClass().getSimpleName();
                }
                logger.info("Error while handling incoming " + packetClass + " packet: ", t);
            }
            if (t instanceof ThreadDeath) {
                ThreadDeath t2 = (ThreadDeath) t;
            }
        }
    }

    private void processJingleIQ(JingleIQ jingleIQ) {
        CallPeerJabberImpl callPeer = (CallPeerJabberImpl) this.activeCallsRepository.findCallPeer(jingleIQ.getSID());
        if (jingleIQ.getType() == Type.ERROR) {
            logger.error("Received error");
            XMPPError error = jingleIQ.getError();
            String message = "Remote party returned an error!";
            if (error != null) {
                String errorStr = "code=" + error.getCode() + " message=" + error.getMessage();
                message = message + Separators.RETURN + errorStr;
                logger.error(Separators.SP + errorStr);
            }
            if (callPeer != null) {
                callPeer.setState(CallPeerState.FAILED, message);
                return;
            }
            return;
        }
        JingleAction action = jingleIQ.getAction();
        TransferPacketExtension transfer;
        if (action == JingleAction.SESSION_INITIATE) {
            transfer = (TransferPacketExtension) jingleIQ.getExtension("transfer", "urn:xmpp:jingle:transfer:0");
            CallIdPacketExtension callidExt = (CallIdPacketExtension) jingleIQ.getExtension("callid", ConferenceDescriptionPacketExtension.NAMESPACE);
            CallJabberImpl call = null;
            if (transfer != null) {
                String sid = transfer.getSID();
                if (sid != null) {
                    CallJabberImpl attendantCall = (CallJabberImpl) getActiveCallsRepository().findSID(sid);
                    if (attendantCall != null) {
                        CallPeerJabberImpl attendant = (CallPeerJabberImpl) attendantCall.getPeer(sid);
                        if (attendant != null && getFullCalleeURI(attendant.getAddress()).equals(transfer.getFrom()) && this.protocolProvider.getOurJID().equals(transfer.getTo())) {
                            call = attendantCall;
                        }
                    }
                }
            }
            if (callidExt != null) {
                String callid = callidExt.getText();
                if (callid != null) {
                    call = (CallJabberImpl) getActiveCallsRepository().findCallId(callid);
                }
            }
            if (!(transfer == null || callidExt == null)) {
                logger.warn("Received a session-initiate with both 'transfer' and 'callid' extensions. Ignored 'transfer' and used 'callid'.");
            }
            if (call == null) {
                call = new CallJabberImpl(this);
            }
            final CallJabberImpl finalCall = call;
            final JingleIQ jingleIQ2 = jingleIQ;
            new Thread() {
                public void run() {
                    finalCall.processSessionInitiate(jingleIQ2);
                }
            }.start();
        } else if (callPeer == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Received a stray trying response.");
            }
        } else if (action == JingleAction.SESSION_TERMINATE) {
            callPeer.processSessionTerminate(jingleIQ);
        } else if (action == JingleAction.SESSION_ACCEPT) {
            callPeer.processSessionAccept(jingleIQ);
        } else if (action == JingleAction.SESSION_INFO) {
            SessionInfoPacketExtension info = jingleIQ.getSessionInfo();
            if (info != null) {
                callPeer.processSessionInfo(info);
                return;
            }
            PacketExtension packetExtension = jingleIQ.getExtension("transfer", "urn:xmpp:jingle:transfer:0");
            if (packetExtension instanceof TransferPacketExtension) {
                transfer = (TransferPacketExtension) packetExtension;
                if (transfer.getFrom() == null) {
                    transfer.setFrom(jingleIQ.getFrom());
                }
                try {
                    callPeer.processTransfer(transfer);
                } catch (OperationFailedException ofe) {
                    logger.error("Failed to transfer to " + transfer.getTo(), ofe);
                }
            }
            packetExtension = jingleIQ.getExtension("conference-info", "");
            if (packetExtension instanceof CoinPacketExtension) {
                callPeer.setConferenceFocus(Boolean.parseBoolean(((CoinPacketExtension) packetExtension).getAttributeAsString(CoinPacketExtension.ISFOCUS_ATTR_NAME)));
            }
        } else if (action == JingleAction.CONTENT_ACCEPT) {
            callPeer.processContentAccept(jingleIQ);
        } else if (action == JingleAction.CONTENT_ADD) {
            callPeer.processContentAdd(jingleIQ);
        } else if (action == JingleAction.CONTENT_MODIFY) {
            callPeer.processContentModify(jingleIQ);
        } else if (action == JingleAction.CONTENT_REJECT) {
            callPeer.processContentReject(jingleIQ);
        } else if (action == JingleAction.CONTENT_REMOVE) {
            callPeer.processContentRemove(jingleIQ);
        } else if (action == JingleAction.TRANSPORT_INFO) {
            callPeer.processTransportInfo(jingleIQ);
        }
    }

    /* access modifiers changed from: protected */
    public ActiveCallsRepositoryJabberGTalkImpl<CallJabberImpl, CallPeerJabberImpl> getActiveCallsRepository() {
        return this.activeCallsRepository;
    }

    public ProtocolProviderServiceJabberImpl getProtocolProvider() {
        return this.protocolProvider;
    }

    public boolean isSecure(CallPeer peer) {
        return ((MediaAwareCallPeer) peer).getMediaHandler().isSecure();
    }

    public void transfer(CallPeer peer, CallPeer target) throws OperationFailedException {
        AbstractCallPeerJabberGTalkImpl<?, ?, ?> targetJabberGTalkImpl = (AbstractCallPeerJabberGTalkImpl) target;
        String to = getFullCalleeURI(targetJabberGTalkImpl.getAddress());
        try {
            if (!this.protocolProvider.getDiscoveryManager().discoverInfo(to).containsFeature("urn:xmpp:jingle:transfer:0")) {
                throw new OperationFailedException("Callee " + to + " does not support" + " XEP-0251: Jingle Session Transfer", 4);
            }
        } catch (XMPPException xmppe) {
            logger.warn("Failed to retrieve DiscoverInfo for " + to, xmppe);
        }
        transfer(peer, to, targetJabberGTalkImpl.getSID());
    }

    public void transfer(CallPeer peer, String target) throws OperationFailedException {
        transfer(peer, target, null);
    }

    private void transfer(CallPeer peer, String to, String sid) throws OperationFailedException {
        String caller = getFullCalleeURI(peer.getAddress());
        try {
            if (!this.protocolProvider.getDiscoveryManager().discoverInfo(caller).containsFeature("urn:xmpp:jingle:transfer:0")) {
                throw new OperationFailedException("Caller " + caller + " does not support" + " XEP-0251: Jingle Session Transfer", 4);
            }
        } catch (XMPPException xmppe) {
            logger.warn("Failed to retrieve DiscoverInfo for " + to, xmppe);
        }
        ((CallPeerJabberImpl) peer).transfer(getFullCalleeURI(to), sid);
    }

    public void setTransferAuthority(TransferAuthority authority) {
    }
}
