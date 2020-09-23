package net.java.sip.communicator.impl.protocol.jabber;

import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard.WhiteboardObjectCircleJabberImpl;
import net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard.WhiteboardObjectImageJabberImpl;
import net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard.WhiteboardObjectJabberImpl;
import net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard.WhiteboardObjectLineJabberImpl;
import net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard.WhiteboardObjectPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard.WhiteboardObjectPathJabberImpl;
import net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard.WhiteboardObjectPolyLineJabberImpl;
import net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard.WhiteboardObjectPolygonJabberImpl;
import net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard.WhiteboardObjectRectJabberImpl;
import net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard.WhiteboardObjectTextJabberImpl;
import net.java.sip.communicator.impl.protocol.jabber.extensions.whiteboard.WhiteboardSessionPacketExtension;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetPresence;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.service.protocol.WhiteboardParticipant;
import net.java.sip.communicator.service.protocol.WhiteboardParticipantState;
import net.java.sip.communicator.service.protocol.WhiteboardSession;
import net.java.sip.communicator.service.protocol.WhiteboardSessionState;
import net.java.sip.communicator.service.protocol.event.WhiteboardChangeEvent;
import net.java.sip.communicator.service.protocol.event.WhiteboardChangeListener;
import net.java.sip.communicator.service.protocol.event.WhiteboardObjectDeletedEvent;
import net.java.sip.communicator.service.protocol.event.WhiteboardObjectDeliveredEvent;
import net.java.sip.communicator.service.protocol.event.WhiteboardObjectDeliveryFailedEvent;
import net.java.sip.communicator.service.protocol.event.WhiteboardObjectListener;
import net.java.sip.communicator.service.protocol.event.WhiteboardObjectModifiedEvent;
import net.java.sip.communicator.service.protocol.event.WhiteboardObjectReceivedEvent;
import net.java.sip.communicator.service.protocol.event.WhiteboardParticipantChangeEvent;
import net.java.sip.communicator.service.protocol.event.WhiteboardParticipantEvent;
import net.java.sip.communicator.service.protocol.event.WhiteboardParticipantListener;
import net.java.sip.communicator.service.protocol.whiteboardobjects.WhiteboardObject;
import net.java.sip.communicator.service.protocol.whiteboardobjects.WhiteboardObjectCircle;
import net.java.sip.communicator.service.protocol.whiteboardobjects.WhiteboardObjectImage;
import net.java.sip.communicator.service.protocol.whiteboardobjects.WhiteboardObjectLine;
import net.java.sip.communicator.service.protocol.whiteboardobjects.WhiteboardObjectPath;
import net.java.sip.communicator.service.protocol.whiteboardobjects.WhiteboardObjectPolyLine;
import net.java.sip.communicator.service.protocol.whiteboardobjects.WhiteboardObjectPolygon;
import net.java.sip.communicator.service.protocol.whiteboardobjects.WhiteboardObjectRect;
import net.java.sip.communicator.service.protocol.whiteboardobjects.WhiteboardObjectText;
import net.java.sip.communicator.util.Logger;
import org.jitsi.javax.sip.message.Response;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.MessageEventManager;
import org.jivesoftware.smackx.packet.MessageEvent;

public class WhiteboardSessionJabberImpl implements WhiteboardParticipantListener, WhiteboardSession {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(WhiteboardSessionJabberImpl.class);
    /* access modifiers changed from: private */
    public ProtocolProviderServiceJabberImpl jabberProvider = null;
    private Vector<WhiteboardObjectListener> messageListeners = new Vector();
    private Chat smackChat;
    /* access modifiers changed from: private */
    public Hashtable<String, WhiteboardParticipant> wbParticipants = new Hashtable();
    private String whiteboardID = null;
    private Vector<WhiteboardChangeListener> whiteboardListeners = new Vector();
    private final Vector<WhiteboardObject> whiteboardObjects = new Vector();
    private OperationSetWhiteboardingJabberImpl whiteboardOpSet;
    private WhiteboardSessionState whiteboardState = WhiteboardSessionState.WHITEBOARD_INITIALIZATION;

    private class WhiteboardSmackMessageListener implements PacketListener {
        private WhiteboardSmackMessageListener() {
        }

        public void processPacket(Packet packet) {
            if (packet instanceof Message) {
                PacketExtension objectExt = packet.getExtension(WhiteboardObjectPacketExtension.ELEMENT_NAME, "http://jabber.org/protocol/swb");
                PacketExtension sessionExt = packet.getExtension(WhiteboardSessionPacketExtension.ELEMENT_NAME, "http://jabber.org/protocol/swb");
                Message msg = (Message) packet;
                if (sessionExt != null) {
                    WhiteboardSessionPacketExtension sessionMessage = (WhiteboardSessionPacketExtension) sessionExt;
                    if (sessionMessage.getAction().equals(WhiteboardSessionPacketExtension.ACTION_LEAVE)) {
                        WhiteboardSessionJabberImpl.this.fireWhiteboardParticipantEvent(WhiteboardSessionJabberImpl.this.findWhiteboardParticipantFromContactAddress(sessionMessage.getContactAddress()), 2);
                    }
                }
                if (objectExt != null) {
                    String fromUserID = StringUtils.parseBareAddress(msg.getFrom());
                    if (WhiteboardSessionJabberImpl.logger.isDebugEnabled()) {
                        WhiteboardSessionJabberImpl.logger.debug("Received from " + fromUserID + " the message " + msg.toXML());
                    }
                    OperationSetPersistentPresenceJabberImpl presenceOpSet = (OperationSetPersistentPresenceJabberImpl) WhiteboardSessionJabberImpl.this.jabberProvider.getOperationSet(OperationSetPresence.class);
                    if (presenceOpSet != null) {
                        Contact sourceContact = presenceOpSet.findContactByID(fromUserID);
                        if (WhiteboardSessionJabberImpl.this.wbParticipants.containsKey(sourceContact.getAddress())) {
                            WhiteboardObjectPacketExtension newMessage = (WhiteboardObjectPacketExtension) objectExt;
                            if (msg.getType() == Type.error) {
                                if (WhiteboardSessionJabberImpl.logger.isInfoEnabled()) {
                                    WhiteboardSessionJabberImpl.logger.info("WBObject error received from " + fromUserID);
                                }
                                int errorResultCode = 1;
                                if (packet.getError().getCode() == Response.SERVICE_UNAVAILABLE) {
                                    MessageEvent msgEvent = (MessageEvent) packet.getExtension("x", "jabber:x:event");
                                    if (msgEvent != null && msgEvent.isOffline()) {
                                        errorResultCode = 5;
                                    }
                                }
                                WhiteboardSessionJabberImpl.this.fireMessageEvent(new WhiteboardObjectDeliveryFailedEvent(WhiteboardSessionJabberImpl.this, newMessage.getWhiteboardObject(), sourceContact, errorResultCode, new Date()));
                            } else if (newMessage.getAction().equals(WhiteboardObjectPacketExtension.ACTION_DELETE)) {
                                WhiteboardSessionJabberImpl.this.fireMessageEvent(new WhiteboardObjectDeletedEvent(WhiteboardSessionJabberImpl.this, newMessage.getWhiteboardObjectID(), sourceContact, new Date()));
                            } else if (newMessage.getAction().equals(WhiteboardObjectPacketExtension.ACTION_DRAW)) {
                                WhiteboardSessionJabberImpl.this.fireMessageEvent(new WhiteboardObjectReceivedEvent(WhiteboardSessionJabberImpl.this, newMessage.getWhiteboardObject(), sourceContact, new Date()));
                            }
                        }
                    }
                }
            }
        }
    }

    public WhiteboardSessionJabberImpl(ProtocolProviderServiceJabberImpl sourceProvider, OperationSetWhiteboardingJabberImpl opSet) {
        this.jabberProvider = sourceProvider;
        this.whiteboardOpSet = opSet;
        this.whiteboardID = String.valueOf(System.currentTimeMillis()) + String.valueOf(super.hashCode());
    }

    public Iterator<WhiteboardParticipant> getWhiteboardParticipants() {
        return new LinkedList(this.wbParticipants.values()).iterator();
    }

    public int getWhiteboardParticipantsCount() {
        return this.wbParticipants.size();
    }

    public void join() throws OperationFailedException {
        this.jabberProvider.getConnection().addPacketListener(new WhiteboardSmackMessageListener(), new PacketExtensionFilter("http://jabber.org/protocol/swb"));
        this.whiteboardOpSet.fireWhiteboardSessionPresenceEvent(this, "LocalUserJoined", null);
    }

    public void join(byte[] password) throws OperationFailedException {
    }

    public boolean isJoined() {
        return true;
    }

    public void leave() {
        try {
            assertConnected();
            Message msg = new Message();
            msg.addExtension(new WhiteboardSessionPacketExtension(this, this.jabberProvider.getAccountID().getAccountAddress(), WhiteboardSessionPacketExtension.ACTION_LEAVE));
            MessageEventManager.addNotificationsRequests(msg, true, false, false, true);
            this.smackChat.sendMessage(msg);
        } catch (XMPPException ex) {
            ex.printStackTrace();
            logger.error("message not send", ex);
        }
        this.whiteboardOpSet.fireWhiteboardSessionPresenceEvent(this, "LocalUserLeft", null);
    }

    public void invite(String contactAddress) {
        OperationSetPersistentPresenceJabberImpl presenceOpSet = (OperationSetPersistentPresenceJabberImpl) this.jabberProvider.getOperationSet(OperationSetPresence.class);
        if (presenceOpSet != null) {
            ContactJabberImpl sourceContact = (ContactJabberImpl) presenceOpSet.findContactByID(contactAddress);
            if (sourceContact == null) {
                sourceContact = presenceOpSet.createVolatileContact(contactAddress);
            }
            addWhiteboardParticipant(new WhiteboardParticipantJabberImpl(sourceContact, this));
            try {
                sendWhiteboardObject(createWhiteboardObject("WHITEBOARDOBJECTLINE"));
            } catch (OperationFailedException e) {
                logger.error("Could not send an invite whiteboard object.", e);
            }
        }
    }

    public WhiteboardSession getWhiteboardSession() {
        return this;
    }

    public void participantStateChanged(WhiteboardParticipantChangeEvent evt) {
        WhiteboardParticipantState newValue = evt.getNewValue();
        if (newValue == WhiteboardParticipantState.DISCONNECTED || newValue == WhiteboardParticipantState.FAILED) {
            removeWhiteboardParticipant(evt.getSourceWhiteboardParticipant());
        }
    }

    public void participantDisplayNameChanged(WhiteboardParticipantChangeEvent evt) {
    }

    public void participantAddressChanged(WhiteboardParticipantChangeEvent evt) {
    }

    public void participantTransportAddressChanged(WhiteboardParticipantChangeEvent evt) {
    }

    public void participantImageChanged(WhiteboardParticipantChangeEvent evt) {
    }

    public void addWhiteboardParticipant(WhiteboardParticipant wbParticipant) {
        if (!this.wbParticipants.containsKey(wbParticipant.getContactAddress())) {
            wbParticipant.addWhiteboardParticipantListener(this);
            this.wbParticipants.put(wbParticipant.getContactAddress(), wbParticipant);
            this.smackChat = this.jabberProvider.getConnection().getChatManager().createChat(wbParticipant.getContactAddress(), null);
            fireWhiteboardParticipantEvent(wbParticipant, 1);
        }
    }

    public void removeWhiteboardParticipant(WhiteboardParticipant wbParticipant) {
        if (this.wbParticipants.containsKey(wbParticipant.getContactAddress())) {
            this.wbParticipants.remove(wbParticipant.getContactAddress());
            if (wbParticipant instanceof WhiteboardParticipantJabberImpl) {
                ((WhiteboardParticipantJabberImpl) wbParticipant).setWhiteboardSession(null);
            }
            wbParticipant.removeWhiteboardParticipantListener(this);
            fireWhiteboardParticipantEvent(wbParticipant, 2);
            if (this.wbParticipants.isEmpty()) {
                setWhiteboardSessionState(WhiteboardSessionState.WHITEBOARD_ENDED);
            }
        }
    }

    public void setWhiteboardSessionState(WhiteboardSessionState newState) {
        WhiteboardSessionState oldState = getWhiteboardSessionState();
        if (oldState != newState) {
            this.whiteboardState = newState;
            fireWhiteboardChangeEvent("WhiteboardState", oldState, newState);
        }
    }

    public WhiteboardSessionState getWhiteboardSessionState() {
        return this.whiteboardState;
    }

    public void addWhiteboardObjectListener(WhiteboardObjectListener listener) {
        synchronized (this.messageListeners) {
            if (!this.messageListeners.contains(listener)) {
                this.messageListeners.add(listener);
            }
        }
    }

    public void removeWhiteboardObjectListener(WhiteboardObjectListener listener) {
        synchronized (this.messageListeners) {
            this.messageListeners.remove(listener);
        }
    }

    public WhiteboardObject createWhiteboardObject(String name) {
        WhiteboardObjectJabberImpl wbObj = null;
        if (logger.isDebugEnabled()) {
            logger.debug("[log] WhiteboardObjectXXX.NAME: " + name);
        }
        if (name.equals("WHITEBOARDOBJECTPATH")) {
            wbObj = new WhiteboardObjectPathJabberImpl();
        } else if (name.equals("WHITEBOARDOBJECTPOLYLINE")) {
            wbObj = new WhiteboardObjectPolyLineJabberImpl();
        } else if (name.equals("WHITEBOARDOBJECTPOLYGON")) {
            wbObj = new WhiteboardObjectPolygonJabberImpl();
        } else if (name.equals("WHITEBOARDOBJECTLINE")) {
            wbObj = new WhiteboardObjectLineJabberImpl();
        } else if (name.equals("WHITEBOARDOBJECTRECT")) {
            wbObj = new WhiteboardObjectRectJabberImpl();
        } else if (name.equals("WHITEBOARDOBJECTCIRCLE")) {
            wbObj = new WhiteboardObjectCircleJabberImpl();
        } else if (name.equals("WHITEBOARDOBJECTTEXT")) {
            wbObj = new WhiteboardObjectTextJabberImpl();
        } else if (name.equals("WHITEBOARDOBJECTIMAGE")) {
            wbObj = new WhiteboardObjectImageJabberImpl();
        }
        this.whiteboardObjects.add(wbObj);
        return wbObj;
    }

    public String getWhiteboardID() {
        return this.whiteboardID;
    }

    public boolean isOfflineMessagingSupported() {
        return true;
    }

    public void moveWhiteboardObject(WhiteboardObject obj) throws OperationFailedException {
        WhiteboardObject wbObj = updateWhiteboardObjects(obj);
        if (wbObj != null) {
            sendWhiteboardObject(wbObj);
        }
    }

    public void deleteWhiteboardObject(WhiteboardObject obj) throws OperationFailedException {
        Iterator<WhiteboardParticipant> participants = getWhiteboardParticipants();
        if (participants.hasNext()) {
            Contact contact = ((WhiteboardParticipantJabberImpl) participants.next()).getContact();
            try {
                assertConnected();
                Message msg = new Message();
                msg.addExtension(new WhiteboardObjectPacketExtension(obj.getID(), WhiteboardObjectPacketExtension.ACTION_DELETE));
                MessageEventManager.addNotificationsRequests(msg, true, false, false, true);
                this.smackChat.sendMessage(msg);
                fireMessageEvent(new WhiteboardObjectDeliveredEvent(this, obj, contact, new Date()));
                int i = 0;
                while (i < this.whiteboardObjects.size()) {
                    if (((WhiteboardObjectJabberImpl) this.whiteboardObjects.get(i)).getID().equals(obj.getID())) {
                        this.whiteboardObjects.remove(i);
                    } else {
                        i++;
                    }
                }
            } catch (XMPPException ex) {
                ex.printStackTrace();
                logger.error("message not send", ex);
            }
        }
    }

    public void sendWhiteboardObject(WhiteboardObject message) throws OperationFailedException {
        Iterator<WhiteboardParticipant> participants = getWhiteboardParticipants();
        if (participants.hasNext()) {
            Contact contact = ((WhiteboardParticipantJabberImpl) participants.next()).getContact();
            try {
                assertConnected();
                Message msg = new Message();
                msg.addExtension(new WhiteboardObjectPacketExtension((WhiteboardObjectJabberImpl) message, WhiteboardObjectPacketExtension.ACTION_DRAW));
                MessageEventManager.addNotificationsRequests(msg, true, false, false, true);
                this.smackChat.sendMessage(msg);
                fireMessageEvent(new WhiteboardObjectDeliveredEvent(this, message, contact, new Date()));
            } catch (XMPPException ex) {
                ex.printStackTrace();
                logger.error("message not send", ex);
            }
        }
    }

    private void assertConnected() throws IllegalStateException {
        if (this.jabberProvider == null) {
            throw new IllegalStateException("The provider must be non-null and signed on the service before being able to communicate.");
        } else if (!this.jabberProvider.isRegistered()) {
            throw new IllegalStateException("The provider must be signed on the service before being able to communicate.");
        }
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof WhiteboardSession)) {
            return false;
        }
        if (obj == this || ((WhiteboardSession) obj).getWhiteboardID().equals(getWhiteboardID())) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return getWhiteboardID().hashCode();
    }

    public String toString() {
        return "Whiteboard: id=" + getWhiteboardID() + " participants=" + getWhiteboardParticipantsCount();
    }

    public void addWhiteboardChangeListener(WhiteboardChangeListener listener) {
        synchronized (this.whiteboardListeners) {
            if (!this.whiteboardListeners.contains(listener)) {
                this.whiteboardListeners.add(listener);
            }
        }
    }

    public void removeWhiteboardChangeListener(WhiteboardChangeListener listener) {
        synchronized (this.whiteboardListeners) {
            this.whiteboardListeners.remove(listener);
        }
    }

    public ProtocolProviderService getProtocolProvider() {
        return this.jabberProvider;
    }

    public void fireWhiteboardParticipantEvent(WhiteboardParticipant sourceWhiteboardParticipant, int eventID) {
        WhiteboardParticipantEvent cpEvent = new WhiteboardParticipantEvent(this, sourceWhiteboardParticipant, eventID);
        if (logger.isDebugEnabled()) {
            logger.debug("Dispatching a WhiteboardParticipant event to " + this.whiteboardListeners.size() + " listeners. event is: " + cpEvent.toString());
        }
        synchronized (this.whiteboardListeners) {
            Iterable<WhiteboardChangeListener> listeners = new ArrayList(this.whiteboardListeners);
        }
        for (WhiteboardChangeListener listener : listeners) {
            if (eventID == 1) {
                listener.whiteboardParticipantAdded(cpEvent);
            } else if (eventID == 2) {
                listener.whiteboardParticipantRemoved(cpEvent);
            }
        }
    }

    public void fireWhiteboardChangeEvent(String type, Object oldValue, Object newValue) {
        WhiteboardChangeEvent ccEvent = new WhiteboardChangeEvent(this, type, oldValue, newValue);
        if (logger.isDebugEnabled()) {
            logger.debug("Dispatching a WhiteboardChange event to " + this.whiteboardListeners.size() + " listeners. event is: " + ccEvent.toString());
        }
        synchronized (this.whiteboardListeners) {
            Iterable<WhiteboardChangeListener> listeners = new ArrayList(this.whiteboardListeners);
        }
        for (WhiteboardChangeListener listener : listeners) {
            if (type.equals("WhiteboardState")) {
                listener.whiteboardStateChanged(ccEvent);
            }
        }
    }

    public Vector<WhiteboardObject> getWhiteboardObjects() {
        return this.whiteboardObjects;
    }

    public void setState(WhiteboardSessionState newState) {
        this.whiteboardState = newState;
    }

    public WhiteboardSessionState getState() {
        return this.whiteboardState;
    }

    public void fireMessageEvent(EventObject evt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Dispatching a WhiteboardMessageEvent event to " + this.messageListeners.size() + " listeners. event is: " + evt.toString());
        }
        synchronized (this.messageListeners) {
            Iterable<WhiteboardObjectListener> listeners = new ArrayList(this.messageListeners);
        }
        for (WhiteboardObjectListener listener : listeners) {
            WhiteboardObjectJabberImpl wbObj;
            if (evt instanceof WhiteboardObjectDeliveredEvent) {
                listener.whiteboardObjectDelivered((WhiteboardObjectDeliveredEvent) evt);
            } else if (evt instanceof WhiteboardObjectReceivedEvent) {
                wbObj = (WhiteboardObjectJabberImpl) ((WhiteboardObjectReceivedEvent) evt).getSourceWhiteboardObject();
                listener.whiteboardObjectReceived((WhiteboardObjectReceivedEvent) evt);
                this.whiteboardObjects.add(wbObj);
            } else if (evt instanceof WhiteboardObjectDeletedEvent) {
                String wbObjID = ((WhiteboardObjectDeletedEvent) evt).getId();
                listener.whiteboardObjectDeleted((WhiteboardObjectDeletedEvent) evt);
                int i = 0;
                while (i < this.whiteboardObjects.size()) {
                    if (((WhiteboardObjectJabberImpl) this.whiteboardObjects.get(i)).getID().equals(wbObjID)) {
                        this.whiteboardObjects.remove(i);
                    } else {
                        i++;
                    }
                }
            } else if (evt instanceof WhiteboardObjectModifiedEvent) {
                WhiteboardObjectModifiedEvent womevt = (WhiteboardObjectModifiedEvent) evt;
                wbObj = (WhiteboardObjectJabberImpl) womevt.getSourceWhiteboardObject();
                listener.whiteboardObjecModified(womevt);
                this.whiteboardObjects.remove(wbObj);
                this.whiteboardObjects.add(wbObj);
            } else if (evt instanceof WhiteboardObjectDeliveryFailedEvent) {
                listener.whiteboardObjectDeliveryFailed((WhiteboardObjectDeliveryFailedEvent) evt);
            }
        }
    }

    private WhiteboardObject updateWhiteboardObjects(WhiteboardObject ws) {
        WhiteboardObjectJabberImpl wbObj = null;
        int i = 0;
        while (i < this.whiteboardObjects.size()) {
            WhiteboardObjectJabberImpl wbObjTmp = (WhiteboardObjectJabberImpl) this.whiteboardObjects.get(i);
            if (wbObjTmp.getID().equals(ws.getID())) {
                wbObj = wbObjTmp;
                break;
            }
            i++;
        }
        if (wbObj == null) {
            return null;
        }
        if (ws instanceof WhiteboardObjectPath) {
            WhiteboardObjectPathJabberImpl obj = (WhiteboardObjectPathJabberImpl) wbObj;
            obj.setPoints(((WhiteboardObjectPath) ws).getPoints());
            obj.setColor(ws.getColor());
            obj.setThickness(ws.getThickness());
        } else if (ws instanceof WhiteboardObjectPolyLine) {
            WhiteboardObjectPolyLineJabberImpl obj2 = (WhiteboardObjectPolyLineJabberImpl) wbObj;
            obj2.setPoints(((WhiteboardObjectPolyLine) ws).getPoints());
            obj2.setColor(ws.getColor());
            obj2.setThickness(ws.getThickness());
        } else if (ws instanceof WhiteboardObjectPolygon) {
            WhiteboardObjectPolygonJabberImpl obj3 = (WhiteboardObjectPolygonJabberImpl) wbObj;
            obj3.setPoints(((WhiteboardObjectPolygon) ws).getPoints());
            obj3.setBackgroundColor(((WhiteboardObjectPolygon) ws).getBackgroundColor());
            obj3.setFill(((WhiteboardObjectPolygon) ws).isFill());
            obj3.setColor(ws.getColor());
            obj3.setThickness(ws.getThickness());
        } else if (ws instanceof WhiteboardObjectLine) {
            WhiteboardObjectLineJabberImpl obj4 = (WhiteboardObjectLineJabberImpl) wbObj;
            obj4.setWhiteboardPointStart(((WhiteboardObjectLine) ws).getWhiteboardPointStart());
            obj4.setWhiteboardPointEnd(((WhiteboardObjectLine) ws).getWhiteboardPointEnd());
            obj4.setColor(ws.getColor());
            obj4.setThickness(ws.getThickness());
        } else if (ws instanceof WhiteboardObjectRect) {
            WhiteboardObjectRectJabberImpl obj5 = (WhiteboardObjectRectJabberImpl) wbObj;
            obj5.setFill(((WhiteboardObjectRect) ws).isFill());
            obj5.setHeight(((WhiteboardObjectRect) ws).getHeight());
            obj5.setWhiteboardPoint(((WhiteboardObjectRect) ws).getWhiteboardPoint());
            obj5.setWidth(((WhiteboardObjectRect) ws).getWidth());
            obj5.setColor(ws.getColor());
            obj5.setThickness(ws.getThickness());
        } else if (ws instanceof WhiteboardObjectCircle) {
            WhiteboardObjectCircleJabberImpl obj6 = (WhiteboardObjectCircleJabberImpl) wbObj;
            obj6.setFill(((WhiteboardObjectCircle) ws).isFill());
            obj6.setRadius(((WhiteboardObjectCircle) ws).getRadius());
            obj6.setWhiteboardPoint(((WhiteboardObjectCircle) ws).getWhiteboardPoint());
            obj6.setBackgroundColor(((WhiteboardObjectCircle) ws).getBackgroundColor());
            obj6.setColor(ws.getColor());
            obj6.setThickness(ws.getThickness());
        } else if (ws instanceof WhiteboardObjectText) {
            WhiteboardObjectTextJabberImpl obj7 = (WhiteboardObjectTextJabberImpl) wbObj;
            obj7.setFontName(((WhiteboardObjectText) ws).getFontName());
            obj7.setFontSize(((WhiteboardObjectText) ws).getFontSize());
            obj7.setText(((WhiteboardObjectText) ws).getText());
            obj7.setWhiteboardPoint(((WhiteboardObjectText) ws).getWhiteboardPoint());
            obj7.setColor(ws.getColor());
            obj7.setThickness(ws.getThickness());
        } else if (ws instanceof WhiteboardObjectImage) {
            WhiteboardObjectImageJabberImpl obj8 = (WhiteboardObjectImageJabberImpl) wbObj;
            obj8.setBackgroundImage(((WhiteboardObjectImage) ws).getBackgroundImage());
            obj8.setHeight(((WhiteboardObjectImage) ws).getHeight());
            obj8.setWhiteboardPoint(((WhiteboardObjectImage) ws).getWhiteboardPoint());
            obj8.setWidth(((WhiteboardObjectImage) ws).getWidth());
            obj8.setColor(ws.getColor());
            obj8.setThickness(ws.getThickness());
        }
        this.whiteboardObjects.set(i, wbObj);
        return wbObj;
    }

    public String[] getSupportedWhiteboardObjects() {
        return new String[]{"WHITEBOARDOBJECTPATH", "WHITEBOARDOBJECTPOLYLINE", "WHITEBOARDOBJECTPOLYGON", "WHITEBOARDOBJECTLINE", "WHITEBOARDOBJECTRECT", "WHITEBOARDOBJECTCIRCLE", "WHITEBOARDOBJECTTEXT", "WHITEBOARDOBJECTIMAGE"};
    }

    public boolean isParticipantContained(String participantName) {
        if (this.wbParticipants.containsKey(participantName)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public WhiteboardParticipant findWhiteboardParticipantFromContactAddress(String contactAddress) {
        for (WhiteboardParticipant participant : this.wbParticipants.values()) {
            if (participant.getContactAddress().equals(contactAddress)) {
                return participant;
            }
        }
        return null;
    }
}
