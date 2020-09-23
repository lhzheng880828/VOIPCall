package net.java.sip.communicator.impl.protocol.jabber;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard.WhiteboardObjectJabberProvider;
import net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard.WhiteboardObjectPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard.WhiteboardSessionPacketExtension;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationNotSupportedException;
import net.java.sip.communicator.service.protocol.OperationSetPresence;
import net.java.sip.communicator.service.protocol.OperationSetWhiteboarding;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.WhiteboardInvitation;
import net.java.sip.communicator.service.protocol.WhiteboardParticipant;
import net.java.sip.communicator.service.protocol.WhiteboardSession;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.service.protocol.event.WhiteboardInvitationListener;
import net.java.sip.communicator.service.protocol.event.WhiteboardInvitationReceivedEvent;
import net.java.sip.communicator.service.protocol.event.WhiteboardInvitationRejectionListener;
import net.java.sip.communicator.service.protocol.event.WhiteboardSessionPresenceChangeEvent;
import net.java.sip.communicator.service.protocol.event.WhiteboardSessionPresenceListener;
import net.java.sip.communicator.service.protocol.whiteboardobjects.WhiteboardObject;
import net.java.sip.communicator.util.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;

public class OperationSetWhiteboardingJabberImpl implements OperationSetWhiteboarding {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(OperationSetWhiteboardingJabberImpl.class);
    private Vector<WhiteboardInvitationListener> invitationListeners = new Vector();
    private Vector<WhiteboardInvitationRejectionListener> invitationRejectionListeners = new Vector();
    /* access modifiers changed from: private */
    public ProtocolProviderServiceJabberImpl jabberProvider = null;
    private Vector<WhiteboardSessionPresenceListener> presenceListeners = new Vector();
    /* access modifiers changed from: private */
    public OperationSetPersistentPresenceJabberImpl presenceOpSet;
    /* access modifiers changed from: private */
    public Vector<WhiteboardSession> whiteboardSessions = new Vector();

    private class RegistrationStateListener implements RegistrationStateChangeListener {
        private RegistrationStateListener() {
        }

        public void registrationStateChanged(RegistrationStateChangeEvent evt) {
            if (evt.getNewState() == RegistrationState.REGISTERED) {
                OperationSetWhiteboardingJabberImpl.this.presenceOpSet = (OperationSetPersistentPresenceJabberImpl) OperationSetWhiteboardingJabberImpl.this.jabberProvider.getOperationSet(OperationSetPresence.class);
                OperationSetWhiteboardingJabberImpl.this.jabberProvider.getConnection().addPacketListener(new WhiteboardSmackMessageListener(), new PacketExtensionFilter(WhiteboardObjectPacketExtension.ELEMENT_NAME, "http://jabber.org/protocol/swb"));
            }
        }
    }

    private class WhiteboardSmackMessageListener implements PacketListener {
        private WhiteboardSmackMessageListener() {
        }

        public void processPacket(Packet packet) {
            if (packet instanceof Message) {
                PacketExtension ext = packet.getExtension(WhiteboardObjectPacketExtension.ELEMENT_NAME, "http://jabber.org/protocol/swb");
                Message msg = (Message) packet;
                if (ext != null) {
                    WhiteboardSessionJabberImpl session;
                    String fromUserID = StringUtils.parseBareAddress(msg.getFrom());
                    int i = 0;
                    while (i < OperationSetWhiteboardingJabberImpl.this.whiteboardSessions.size()) {
                        session = (WhiteboardSessionJabberImpl) OperationSetWhiteboardingJabberImpl.this.whiteboardSessions.get(i);
                        if (!session.isJoined() || !session.isParticipantContained(fromUserID)) {
                            i++;
                        } else {
                            return;
                        }
                    }
                    WhiteboardObjectPacketExtension newMessage = (WhiteboardObjectPacketExtension) ext;
                    session = new WhiteboardSessionJabberImpl(OperationSetWhiteboardingJabberImpl.this.jabberProvider, OperationSetWhiteboardingJabberImpl.this);
                    OperationSetWhiteboardingJabberImpl.this.whiteboardSessions.add(session);
                    ContactJabberImpl sourceContact = (ContactJabberImpl) OperationSetWhiteboardingJabberImpl.this.presenceOpSet.findContactByID(fromUserID);
                    if (sourceContact == null) {
                        if (OperationSetWhiteboardingJabberImpl.logger.isDebugEnabled()) {
                            OperationSetWhiteboardingJabberImpl.logger.debug("Received a message from an unknown contact: " + fromUserID);
                        }
                        sourceContact = OperationSetWhiteboardingJabberImpl.this.presenceOpSet.createVolatileContact(fromUserID);
                    }
                    session.addWhiteboardParticipant(new WhiteboardParticipantJabberImpl(sourceContact, session));
                    OperationSetWhiteboardingJabberImpl.this.fireInvitationEvent(session, newMessage.getWhiteboardObject(), fromUserID, null, null);
                }
            }
        }
    }

    public OperationSetWhiteboardingJabberImpl(ProtocolProviderServiceJabberImpl provider) {
        this.jabberProvider = provider;
        provider.addRegistrationStateChangeListener(new RegistrationStateListener());
        ProviderManager pManager = ProviderManager.getInstance();
        pManager.addExtensionProvider(WhiteboardObjectPacketExtension.ELEMENT_NAME, "http://jabber.org/protocol/swb", new WhiteboardObjectJabberProvider());
        pManager.addExtensionProvider(WhiteboardSessionPacketExtension.ELEMENT_NAME, "http://jabber.org/protocol/swb", new WhiteboardObjectJabberProvider());
    }

    public void addInvitationListener(WhiteboardInvitationListener listener) {
        synchronized (this.invitationListeners) {
            if (!this.invitationListeners.contains(listener)) {
                this.invitationListeners.add(listener);
            }
        }
    }

    public void removeInvitationListener(WhiteboardInvitationListener listener) {
        synchronized (this.invitationListeners) {
            this.invitationListeners.remove(listener);
        }
    }

    public void addInvitationRejectionListener(WhiteboardInvitationRejectionListener listener) {
        synchronized (this.invitationRejectionListeners) {
            if (!this.invitationRejectionListeners.contains(listener)) {
                this.invitationRejectionListeners.add(listener);
            }
        }
    }

    public void removeInvitationRejectionListener(WhiteboardInvitationRejectionListener listener) {
        synchronized (this.invitationRejectionListeners) {
            this.invitationRejectionListeners.remove(listener);
        }
    }

    public void addPresenceListener(WhiteboardSessionPresenceListener listener) {
        synchronized (this.presenceListeners) {
            if (!this.presenceListeners.contains(listener)) {
                this.presenceListeners.add(listener);
            }
        }
    }

    public void removePresenceListener(WhiteboardSessionPresenceListener listener) {
        synchronized (this.presenceListeners) {
            this.presenceListeners.remove(listener);
        }
    }

    public WhiteboardSession createWhiteboardSession(String sessionName, Hashtable<Object, Object> hashtable) throws OperationFailedException, OperationNotSupportedException {
        WhiteboardSessionJabberImpl session = new WhiteboardSessionJabberImpl(this.jabberProvider, this);
        this.whiteboardSessions.add(session);
        return session;
    }

    public WhiteboardSession findWhiteboardSession(String sessionName) throws OperationFailedException, OperationNotSupportedException {
        return null;
    }

    public List<WhiteboardSession> getCurrentlyJoinedWhiteboards() {
        List<WhiteboardSession> joinedWhiteboards;
        synchronized (this.whiteboardSessions) {
            joinedWhiteboards = new LinkedList(this.whiteboardSessions);
            Iterator<WhiteboardSession> joinedWhiteboardsIter = this.whiteboardSessions.iterator();
            while (joinedWhiteboardsIter.hasNext()) {
                if (!((WhiteboardSession) joinedWhiteboardsIter.next()).isJoined()) {
                    joinedWhiteboardsIter.remove();
                }
            }
        }
        return joinedWhiteboards;
    }

    public List<WhiteboardSession> getCurrentlyJoinedWhiteboards(WhiteboardParticipant participant) throws OperationFailedException, OperationNotSupportedException {
        return null;
    }

    public boolean isWhiteboardingSupportedByContact(Contact contact) {
        if (contact.getProtocolProvider().getOperationSet(OperationSetWhiteboarding.class) != null) {
            return true;
        }
        return false;
    }

    public void rejectInvitation(WhiteboardInvitation invitation, String rejectReason) {
    }

    public void fireInvitationEvent(WhiteboardSession targetWhiteboard, WhiteboardObject whiteboardObject, String inviter, String reason, byte[] password) {
        WhiteboardInvitationReceivedEvent evt = new WhiteboardInvitationReceivedEvent(this, new WhiteboardInvitationJabberImpl(targetWhiteboard, whiteboardObject, inviter, reason, password), new Date(System.currentTimeMillis()));
        if (logger.isDebugEnabled()) {
            logger.debug("Dispatching a WhiteboardInvitation event to " + this.invitationListeners.size() + " listeners. event is: " + evt.toString());
        }
        synchronized (this.invitationListeners) {
            Iterable<WhiteboardInvitationListener> listeners = new ArrayList(this.invitationListeners);
        }
        for (WhiteboardInvitationListener listener : listeners) {
            listener.invitationReceived(evt);
        }
    }

    public void fireWhiteboardSessionPresenceEvent(WhiteboardSession session, String eventType, String reason) {
        WhiteboardSessionPresenceChangeEvent evt = new WhiteboardSessionPresenceChangeEvent(this, session, eventType, reason);
        synchronized (this.presenceListeners) {
            Iterable<WhiteboardSessionPresenceListener> listeners = new ArrayList(this.presenceListeners);
        }
        for (WhiteboardSessionPresenceListener listener : listeners) {
            listener.whiteboardSessionPresenceChanged(evt);
        }
    }
}
