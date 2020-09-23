package net.java.sip.communicator.impl.protocol.jabber;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.service.protocol.WhiteboardParticipant;
import net.java.sip.communicator.service.protocol.WhiteboardParticipantState;
import net.java.sip.communicator.service.protocol.WhiteboardSession;
import net.java.sip.communicator.service.protocol.event.WhiteboardParticipantChangeEvent;
import net.java.sip.communicator.service.protocol.event.WhiteboardParticipantListener;
import net.java.sip.communicator.util.Logger;

public class WhiteboardParticipantJabberImpl implements WhiteboardParticipant {
    private static final Logger logger = Logger.getLogger(WhiteboardParticipantJabberImpl.class);
    protected Date currentStateStartDate = new Date();
    private byte[] image;
    private ContactJabberImpl participant = null;
    private String participantID;
    private WhiteboardSessionJabberImpl whiteboard;
    protected final List<WhiteboardParticipantListener> whiteboardParticipantListeners = new ArrayList();
    protected WhiteboardParticipantState whiteboardParticipantState = WhiteboardParticipantState.UNKNOWN;

    public WhiteboardParticipantJabberImpl(ContactJabberImpl participant, WhiteboardSessionJabberImpl owningWhiteboard) {
        this.participant = participant;
        this.whiteboard = owningWhiteboard;
        this.whiteboard.addWhiteboardParticipant(this);
        this.participantID = String.valueOf(System.currentTimeMillis()) + String.valueOf(hashCode());
    }

    public String getContactAddress() {
        return this.participant.getAddress();
    }

    public WhiteboardParticipantState getState() {
        return this.whiteboardParticipantState;
    }

    /* access modifiers changed from: protected */
    public void setState(WhiteboardParticipantState newState, String reason) {
        WhiteboardParticipantState oldState = getState();
        if (oldState != newState) {
            this.whiteboardParticipantState = newState;
            this.currentStateStartDate = new Date();
            fireWhiteboardParticipantChangeEvent("WhiteboardParticipantStatusChange", oldState, newState);
        }
    }

    /* access modifiers changed from: protected */
    public void setState(WhiteboardParticipantState newState) {
        setState(newState, null);
    }

    public Date getCurrentStateStartDate() {
        return this.currentStateStartDate;
    }

    public String getDisplayName() {
        String displayName = this.participant.getDisplayName();
        return displayName == null ? "" : displayName;
    }

    /* access modifiers changed from: protected */
    public void setDisplayName(String displayName) {
        fireWhiteboardParticipantChangeEvent("WhiteboardParticipantDisplayNameChange", getDisplayName(), displayName);
    }

    public byte[] getImage() {
        return this.image;
    }

    /* access modifiers changed from: protected */
    public void setImage(byte[] image) {
        byte[] oldImage = getImage();
        this.image = image;
        fireWhiteboardParticipantChangeEvent("WhiteboardParticipantImageChange", oldImage, image);
    }

    public String getParticipantID() {
        return this.participantID;
    }

    /* access modifiers changed from: protected */
    public void setParticipantID(String participantID) {
        this.participantID = participantID;
    }

    public WhiteboardSession getWhiteboardSession() {
        return this.whiteboard;
    }

    /* access modifiers changed from: protected */
    public void setWhiteboard(WhiteboardSessionJabberImpl whiteboard) {
        this.whiteboard = whiteboard;
    }

    public ProtocolProviderService getProtocolProvider() {
        return getWhiteboardSession().getProtocolProvider();
    }

    public Contact getContact() {
        return this.participant;
    }

    public void addWhiteboardParticipantListener(WhiteboardParticipantListener listener) {
        synchronized (this.whiteboardParticipantListeners) {
            if (!this.whiteboardParticipantListeners.contains(listener)) {
                this.whiteboardParticipantListeners.add(listener);
            }
        }
    }

    public void removeWhiteboardParticipantListener(WhiteboardParticipantListener listener) {
        synchronized (this.whiteboardParticipantListeners) {
            if (listener == null) {
                return;
            }
            this.whiteboardParticipantListeners.remove(listener);
        }
    }

    /* access modifiers changed from: protected */
    public void fireWhiteboardParticipantChangeEvent(String eventType, Object oldValue, Object newValue) {
        fireWhiteboardParticipantChangeEvent(eventType, oldValue, newValue, null);
    }

    /* access modifiers changed from: protected */
    public void fireWhiteboardParticipantChangeEvent(String eventType, Object oldValue, Object newValue, String reason) {
        WhiteboardParticipantChangeEvent evt = new WhiteboardParticipantChangeEvent(this, eventType, oldValue, newValue, reason);
        if (logger.isDebugEnabled()) {
            logger.debug("Dispatching a WhiteboardParticipantChangeEvent event to " + this.whiteboardParticipantListeners.size() + " listeners. event is: " + evt.toString());
        }
        synchronized (this.whiteboardParticipantListeners) {
            Iterable<WhiteboardParticipantListener> listeners = new ArrayList(this.whiteboardParticipantListeners);
        }
        for (WhiteboardParticipantListener listener : listeners) {
            if (eventType.equals("WhiteboardParticipantDisplayNameChange")) {
                listener.participantDisplayNameChanged(evt);
            } else if (eventType.equals("WhiteboardParticipantImageChange")) {
                listener.participantImageChanged(evt);
            } else if (eventType.equals("WhiteboardParticipantStatusChange")) {
                listener.participantStateChanged(evt);
            }
        }
    }

    public String toString() {
        return getDisplayName();
    }

    public String getName() {
        return this.participant.getDisplayName();
    }

    public void setWhiteboardSession(WhiteboardSessionJabberImpl session) {
        this.whiteboard = session;
    }
}
