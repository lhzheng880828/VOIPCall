package net.java.sip.communicator.impl.protocol.jabber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import net.java.sip.communicator.impl.protocol.sip.SipStatusEnum;
import net.java.sip.communicator.service.protocol.AbstractOperationSetPersistentPresence;
import net.java.sip.communicator.service.protocol.AuthorizationHandler;
import net.java.sip.communicator.service.protocol.AuthorizationRequest;
import net.java.sip.communicator.service.protocol.AuthorizationResponse;
import net.java.sip.communicator.service.protocol.ChatRoom;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.ContactGroup;
import net.java.sip.communicator.service.protocol.ContactResource;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetMultiUserChat;
import net.java.sip.communicator.service.protocol.PresenceStatus;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.ContactResourceEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.service.protocol.event.ServerStoredGroupListener;
import net.java.sip.communicator.service.protocol.jabberconstants.JabberStatusEnum;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.packet.VCard;

public class OperationSetPersistentPresenceJabberImpl extends AbstractOperationSetPersistentPresence<ProtocolProviderServiceJabberImpl> {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(OperationSetPersistentPresenceJabberImpl.class);
    private static Map<String, Mode> scToJabberModesMappings = new Hashtable();
    private static Map<String, Integer> statusToPriorityMappings = new Hashtable();
    /* access modifiers changed from: private */
    public ContactChangesListener contactChangesListener = null;
    /* access modifiers changed from: private */
    public PresenceStatus currentStatus = ((ProtocolProviderServiceJabberImpl) this.parentProvider).getJabberStatusEnum().getStatus(SipStatusEnum.OFFLINE);
    private String currentStatusMessage = "";
    private final MobileIndicator mobileIndicator;
    private int resourcePriorityAvailable = 30;
    /* access modifiers changed from: private|final */
    public final ServerStoredContactListJabberImpl ssContactList;
    /* access modifiers changed from: private */
    public JabberSubscriptionListener subscribtionPacketListener = null;
    private VCardTempXUpdatePresenceExtension vCardTempXUpdatePresenceExtension = null;

    class ContactChangesListener implements RosterListener {
        private final Map<String, TreeSet<Presence>> statuses = new Hashtable();
        private boolean storeEvents = false;
        private List<Presence> storedPresences = null;

        ContactChangesListener() {
        }

        public void entriesAdded(Collection<String> collection) {
        }

        public void entriesUpdated(Collection<String> collection) {
        }

        public void entriesDeleted(Collection<String> collection) {
        }

        public void rosterError(XMPPError error, Packet packet) {
        }

        public void presenceChanged(Presence presence) {
            firePresenceStatusChanged(presence);
        }

        /* access modifiers changed from: 0000 */
        public void storeEvents() {
            this.storedPresences = new ArrayList();
            this.storeEvents = true;
        }

        /* access modifiers changed from: 0000 */
        public void processStoredEvents() {
            this.storeEvents = false;
            for (Presence p : this.storedPresences) {
                firePresenceStatusChanged(p);
            }
            this.storedPresences.clear();
            this.storedPresences = null;
        }

        /* access modifiers changed from: 0000 */
        public void firePresenceStatusChanged(Presence presence) {
            if (!this.storeEvents || this.storedPresences == null) {
                try {
                    Presence currentPresence;
                    String userID = StringUtils.parseBareAddress(presence.getFrom());
                    for (ChatRoom chatRoom : ((OperationSetMultiUserChat) ((ProtocolProviderServiceJabberImpl) OperationSetPersistentPresenceJabberImpl.this.parentProvider).getOperationSet(OperationSetMultiUserChat.class)).getCurrentlyJoinedChatRooms()) {
                        if (chatRoom.getName().equals(userID)) {
                            userID = presence.getFrom();
                            break;
                        }
                    }
                    if (OperationSetPersistentPresenceJabberImpl.logger.isDebugEnabled()) {
                        OperationSetPersistentPresenceJabberImpl.logger.debug("Received a status update for buddy=" + userID);
                    }
                    TreeSet<Presence> userStats = (TreeSet) this.statuses.get(userID);
                    if (userStats == null) {
                        userStats = new TreeSet(new Comparator<Presence>() {
                            public int compare(Presence o1, Presence o2) {
                                int res = o2.getPriority() - o1.getPriority();
                                if (res == 0) {
                                    return OperationSetPersistentPresenceJabberImpl.jabberStatusToPresenceStatus(o2, (ProtocolProviderServiceJabberImpl) OperationSetPersistentPresenceJabberImpl.this.parentProvider).getStatus() - OperationSetPersistentPresenceJabberImpl.jabberStatusToPresenceStatus(o1, (ProtocolProviderServiceJabberImpl) OperationSetPersistentPresenceJabberImpl.this.parentProvider).getStatus();
                                }
                                return res;
                            }
                        });
                        this.statuses.put(userID, userStats);
                    } else {
                        String resource = StringUtils.parseResource(presence.getFrom());
                        Iterator<Presence> iter = userStats.iterator();
                        while (iter.hasNext()) {
                            if (StringUtils.parseResource(((Presence) iter.next()).getFrom()).equals(resource)) {
                                iter.remove();
                            }
                        }
                    }
                    if (!OperationSetPersistentPresenceJabberImpl.jabberStatusToPresenceStatus(presence, (ProtocolProviderServiceJabberImpl) OperationSetPersistentPresenceJabberImpl.this.parentProvider).equals(((ProtocolProviderServiceJabberImpl) OperationSetPersistentPresenceJabberImpl.this.parentProvider).getJabberStatusEnum().getStatus(SipStatusEnum.OFFLINE))) {
                        userStats.add(presence);
                    }
                    if (userStats.size() == 0) {
                        currentPresence = presence;
                        this.statuses.remove(userID);
                    } else {
                        currentPresence = (Presence) userStats.first();
                    }
                    ContactJabberImpl sourceContact = OperationSetPersistentPresenceJabberImpl.this.ssContactList.findContactById(userID);
                    if (sourceContact == null) {
                        OperationSetPersistentPresenceJabberImpl.logger.warn("No source contact found for id=" + userID);
                        return;
                    }
                    sourceContact.setStatusMessage(currentPresence.getStatus());
                    OperationSetPersistentPresenceJabberImpl.this.updateContactStatus(sourceContact, OperationSetPersistentPresenceJabberImpl.jabberStatusToPresenceStatus(currentPresence, (ProtocolProviderServiceJabberImpl) OperationSetPersistentPresenceJabberImpl.this.parentProvider));
                    return;
                } catch (IllegalStateException ex) {
                    OperationSetPersistentPresenceJabberImpl.logger.error("Failed changing status", ex);
                    return;
                } catch (IllegalArgumentException ex2) {
                    OperationSetPersistentPresenceJabberImpl.logger.error("Failed changing status", ex2);
                    return;
                }
            }
            this.storedPresences.add(presence);
        }
    }

    private class JabberSubscriptionListener implements PacketListener {
        private List<String> earlySubscriptions;
        /* access modifiers changed from: private */
        public AuthorizationHandler handler;

        private JabberSubscriptionListener() {
            this.handler = null;
            this.earlySubscriptions = new ArrayList();
        }

        /* access modifiers changed from: private|declared_synchronized */
        public synchronized void setHandler(AuthorizationHandler handler) {
            this.handler = handler;
            handleEarlySubscribeReceived();
        }

        public void processPacket(Packet packet) {
            Presence presence = (Presence) packet;
            if (presence != null) {
                Type presenceType = presence.getType();
                String fromID = presence.getFrom();
                ContactJabberImpl contact;
                if (presenceType == Type.subscribe) {
                    synchronized (this) {
                        if (this.handler == null) {
                            this.earlySubscriptions.add(fromID);
                            return;
                        }
                        handleSubscribeReceived(fromID);
                    }
                } else if (presenceType == Type.unsubscribed) {
                    if (OperationSetPersistentPresenceJabberImpl.logger.isTraceEnabled()) {
                        OperationSetPersistentPresenceJabberImpl.logger.trace(fromID + " does not allow your subscription");
                    }
                    if (this.handler == null) {
                        OperationSetPersistentPresenceJabberImpl.logger.warn("No to handle unsubscribed AuthorizationHandler for " + fromID);
                        return;
                    }
                    contact = OperationSetPersistentPresenceJabberImpl.this.ssContactList.findContactById(fromID);
                    if (contact != null) {
                        this.handler.processAuthorizationResponse(new AuthorizationResponse(AuthorizationResponse.REJECT, ""), contact);
                        try {
                            OperationSetPersistentPresenceJabberImpl.this.ssContactList.removeContact(contact);
                        } catch (OperationFailedException e) {
                            OperationSetPersistentPresenceJabberImpl.logger.error("Cannot remove contact that unsubscribed.");
                        }
                    }
                } else if (presenceType != Type.subscribed) {
                } else {
                    if (this.handler == null) {
                        OperationSetPersistentPresenceJabberImpl.logger.warn("No AuthorizationHandler to handle subscribed for " + fromID);
                        return;
                    }
                    contact = OperationSetPersistentPresenceJabberImpl.this.ssContactList.findContactById(fromID);
                    this.handler.processAuthorizationResponse(new AuthorizationResponse(AuthorizationResponse.ACCEPT, ""), contact);
                }
            }
        }

        private void handleEarlySubscribeReceived() {
            for (String from : this.earlySubscriptions) {
                handleSubscribeReceived(from);
            }
            this.earlySubscriptions.clear();
        }

        private void handleSubscribeReceived(final String fromID) {
            new Thread(new Runnable() {
                public void run() {
                    if (OperationSetPersistentPresenceJabberImpl.logger.isTraceEnabled()) {
                        OperationSetPersistentPresenceJabberImpl.logger.trace(fromID + " wants to add you to its contact list");
                    }
                    ContactJabberImpl srcContact = OperationSetPersistentPresenceJabberImpl.this.ssContactList.findContactById(fromID);
                    Type responsePresenceType = null;
                    if (srcContact == null) {
                        srcContact = OperationSetPersistentPresenceJabberImpl.this.createVolatileContact(fromID);
                    } else if (srcContact.isPersistent()) {
                        responsePresenceType = Type.subscribed;
                    }
                    if (responsePresenceType == null) {
                        AuthorizationResponse response = JabberSubscriptionListener.this.handler.processAuthorisationRequest(new AuthorizationRequest(), srcContact);
                        if (response != null) {
                            if (response.getResponseCode().equals(AuthorizationResponse.ACCEPT)) {
                                responsePresenceType = Type.subscribed;
                                if (OperationSetPersistentPresenceJabberImpl.logger.isInfoEnabled()) {
                                    OperationSetPersistentPresenceJabberImpl.logger.info("Sending Accepted Subscription");
                                }
                            } else if (response.getResponseCode().equals(AuthorizationResponse.REJECT)) {
                                responsePresenceType = Type.unsubscribed;
                                if (OperationSetPersistentPresenceJabberImpl.logger.isInfoEnabled()) {
                                    OperationSetPersistentPresenceJabberImpl.logger.info("Sending Rejected Subscription");
                                }
                            }
                        }
                    }
                    if (responsePresenceType != null) {
                        Presence responsePacket = new Presence(responsePresenceType);
                        responsePacket.setTo(fromID);
                        ((ProtocolProviderServiceJabberImpl) OperationSetPersistentPresenceJabberImpl.this.parentProvider).getConnection().sendPacket(responsePacket);
                    }
                }
            }).start();
        }
    }

    private class RegistrationStateListener implements RegistrationStateChangeListener {
        private RegistrationStateListener() {
        }

        public void registrationStateChanged(RegistrationStateChangeEvent evt) {
            if (OperationSetPersistentPresenceJabberImpl.logger.isDebugEnabled()) {
                OperationSetPersistentPresenceJabberImpl.logger.debug("The Jabber provider changed state from: " + evt.getOldState() + " to: " + evt.getNewState());
            }
            if (evt.getNewState() == RegistrationState.REGISTERING) {
                ((ProtocolProviderServiceJabberImpl) OperationSetPersistentPresenceJabberImpl.this.parentProvider).getConnection().addPacketListener(new ServerStoredListInit(), new PacketTypeFilter(RosterPacket.class));
                if (OperationSetPersistentPresenceJabberImpl.this.subscribtionPacketListener == null) {
                    OperationSetPersistentPresenceJabberImpl.this.subscribtionPacketListener = new JabberSubscriptionListener();
                    ((ProtocolProviderServiceJabberImpl) OperationSetPersistentPresenceJabberImpl.this.parentProvider).getConnection().addPacketListener(OperationSetPersistentPresenceJabberImpl.this.subscribtionPacketListener, new PacketTypeFilter(Presence.class));
                }
            } else if (evt.getNewState() == RegistrationState.REGISTERED) {
                OperationSetPersistentPresenceJabberImpl.this.fireProviderStatusChangeEvent(OperationSetPersistentPresenceJabberImpl.this.currentStatus, ((ProtocolProviderServiceJabberImpl) OperationSetPersistentPresenceJabberImpl.this.parentProvider).getJabberStatusEnum().getStatus("Available"));
                OperationSetPersistentPresenceJabberImpl.this.createContactPhotoPresenceListener();
                OperationSetPersistentPresenceJabberImpl.this.createAccountPhotoPresenceInterceptor();
            } else if (evt.getNewState() == RegistrationState.UNREGISTERED || evt.getNewState() == RegistrationState.AUTHENTICATION_FAILED || evt.getNewState() == RegistrationState.CONNECTION_FAILED) {
                PresenceStatus oldStatus = OperationSetPersistentPresenceJabberImpl.this.currentStatus;
                OperationSetPersistentPresenceJabberImpl.this.currentStatus = ((ProtocolProviderServiceJabberImpl) OperationSetPersistentPresenceJabberImpl.this.parentProvider).getJabberStatusEnum().getStatus(SipStatusEnum.OFFLINE);
                OperationSetPersistentPresenceJabberImpl.this.fireProviderStatusChangeEvent(oldStatus, OperationSetPersistentPresenceJabberImpl.this.currentStatus);
                OperationSetPersistentPresenceJabberImpl.this.ssContactList.cleanup();
                OperationSetPersistentPresenceJabberImpl.this.subscribtionPacketListener = null;
                XMPPConnection connection = ((ProtocolProviderServiceJabberImpl) OperationSetPersistentPresenceJabberImpl.this.parentProvider).getConnection();
                if (connection != null) {
                    connection.getRoster().removeRosterListener(OperationSetPersistentPresenceJabberImpl.this.contactChangesListener);
                }
                OperationSetPersistentPresenceJabberImpl.this.contactChangesListener = null;
            }
        }
    }

    private class ServerStoredListInit implements Runnable, PacketListener {
        private ServerStoredListInit() {
        }

        public void run() {
            ((ProtocolProviderServiceJabberImpl) OperationSetPersistentPresenceJabberImpl.this.parentProvider).getConnection().removePacketListener(this);
            OperationSetPersistentPresenceJabberImpl.this.contactChangesListener = new ContactChangesListener();
            OperationSetPersistentPresenceJabberImpl.this.ssContactList.init(OperationSetPersistentPresenceJabberImpl.this.contactChangesListener);
            ((ProtocolProviderServiceJabberImpl) OperationSetPersistentPresenceJabberImpl.this.parentProvider).startJingleNodesDiscovery();
        }

        public void processPacket(Packet packet) {
            if (packet.getError() == null) {
                new Thread(this, getClass().getName()).start();
            }
        }
    }

    static {
        scToJabberModesMappings.put(SipStatusEnum.AWAY, Mode.away);
        scToJabberModesMappings.put(SipStatusEnum.ON_THE_PHONE, Mode.away);
        scToJabberModesMappings.put("Extended Away", Mode.xa);
        scToJabberModesMappings.put("Do Not Disturb", Mode.dnd);
        scToJabberModesMappings.put("Free For Chat", Mode.chat);
        scToJabberModesMappings.put("Available", Mode.available);
    }

    public OperationSetPersistentPresenceJabberImpl(ProtocolProviderServiceJabberImpl provider, InfoRetreiver infoRetreiver) {
        super(provider);
        initializePriorities();
        this.ssContactList = new ServerStoredContactListJabberImpl(this, provider, infoRetreiver);
        ((ProtocolProviderServiceJabberImpl) this.parentProvider).addRegistrationStateChangeListener(new RegistrationStateListener());
        this.mobileIndicator = new MobileIndicator((ProtocolProviderServiceJabberImpl) this.parentProvider, this.ssContactList);
    }

    public void addServerStoredGroupChangeListener(ServerStoredGroupListener listener) {
        this.ssContactList.addGroupListener(listener);
    }

    public void createServerStoredContactGroup(ContactGroup parent, String groupName) throws OperationFailedException {
        assertConnected();
        if (parent.canContainSubgroups()) {
            this.ssContactList.createGroup(groupName);
            return;
        }
        throw new IllegalArgumentException("The specified contact group cannot contain child groups. Group:" + parent);
    }

    public synchronized ContactJabberImpl createVolatileContact(String id) {
        return createVolatileContact(id, false);
    }

    public synchronized ContactJabberImpl createVolatileContact(String id, boolean isPrivateMessagingContact) {
        ContactJabberImpl sourceContact;
        ContactJabberImpl sourceContact2;
        ContactGroupJabberImpl notInContactListGroup = this.ssContactList.getNonPersistentGroup();
        if (notInContactListGroup != null) {
            sourceContact2 = notInContactListGroup.findContact(StringUtils.parseBareAddress(id));
            if (sourceContact2 != null) {
                sourceContact = sourceContact2;
            }
        }
        sourceContact2 = this.ssContactList.createVolatileContact(id, isPrivateMessagingContact);
        if (isPrivateMessagingContact && StringUtils.parseResource(id) != null) {
            updateResources(sourceContact2, false);
        }
        sourceContact = sourceContact2;
        return sourceContact;
    }

    public Contact createUnresolvedContact(String address, String persistentData, ContactGroup parentGroup) {
        if ((parentGroup instanceof ContactGroupJabberImpl) || (parentGroup instanceof RootContactGroupJabberImpl)) {
            ContactJabberImpl contact = this.ssContactList.createUnresolvedContact(parentGroup, address);
            contact.setPersistentData(persistentData);
            return contact;
        }
        throw new IllegalArgumentException("Argument is not an jabber contact group (group=" + parentGroup + Separators.RPAREN);
    }

    public Contact createUnresolvedContact(String address, String persistentData) {
        return createUnresolvedContact(address, persistentData, getServerStoredContactListRoot());
    }

    public ContactGroup createUnresolvedContactGroup(String groupUID, String persistentData, ContactGroup parentGroup) {
        return this.ssContactList.createUnresolvedContactGroup(groupUID);
    }

    public Contact findContactByID(String contactID) {
        return this.ssContactList.findContactById(contactID);
    }

    public String getCurrentStatusMessage() {
        return this.currentStatusMessage;
    }

    public Contact getLocalContact() {
        return null;
    }

    public PresenceStatus getPresenceStatus() {
        return this.currentStatus;
    }

    public ContactGroup getServerStoredContactListRoot() {
        return this.ssContactList.getRootGroup();
    }

    public Iterator<PresenceStatus> getSupportedStatusSet() {
        return ((ProtocolProviderServiceJabberImpl) this.parentProvider).getJabberStatusEnum().getSupportedStatusSet();
    }

    public boolean isPrivateMessagingContact(String contactAddress) {
        return this.ssContactList.isPrivateMessagingContact(contactAddress);
    }

    public void moveContactToGroup(Contact contactToMove, ContactGroup newParent) throws OperationFailedException {
        assertConnected();
        if (!(contactToMove instanceof ContactJabberImpl)) {
            throw new IllegalArgumentException("The specified contact is not an jabber contact." + contactToMove);
        } else if (newParent instanceof ContactGroupJabberImpl) {
            this.ssContactList.moveContact((ContactJabberImpl) contactToMove, (ContactGroupJabberImpl) newParent);
        } else {
            throw new IllegalArgumentException("The specified group is not an jabber contact group." + newParent);
        }
    }

    public void publishPresenceStatus(PresenceStatus status, String statusMessage) throws IllegalArgumentException, IllegalStateException, OperationFailedException {
        assertConnected();
        JabberStatusEnum jabberStatusEnum = ((ProtocolProviderServiceJabberImpl) this.parentProvider).getJabberStatusEnum();
        boolean isValidStatus = false;
        Iterator<PresenceStatus> supportedStatusIter = jabberStatusEnum.getSupportedStatusSet();
        while (supportedStatusIter.hasNext()) {
            if (((PresenceStatus) supportedStatusIter.next()).equals(status)) {
                isValidStatus = true;
                break;
            }
        }
        if (isValidStatus) {
            if (status.equals(jabberStatusEnum.getStatus(SipStatusEnum.OFFLINE))) {
                ((ProtocolProviderServiceJabberImpl) this.parentProvider).unregister();
            } else {
                Presence presence = new Presence(Type.available);
                presence.setMode(presenceStatusToJabberMode(status));
                presence.setPriority(getPriorityForPresenceStatus(status.getStatusName()));
                if (status.equals(jabberStatusEnum.getStatus(SipStatusEnum.ON_THE_PHONE))) {
                    presence.setStatus(SipStatusEnum.ON_THE_PHONE);
                } else {
                    presence.setStatus(statusMessage);
                }
                ((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection().sendPacket(presence);
            }
            fireProviderStatusChangeEvent(this.currentStatus, status);
            if (!getCurrentStatusMessage().equals(statusMessage)) {
                String oldStatusMessage = getCurrentStatusMessage();
                this.currentStatusMessage = statusMessage;
                fireProviderStatusMessageChangeEvent(oldStatusMessage, getCurrentStatusMessage());
                return;
            }
            return;
        }
        throw new IllegalArgumentException(status + " is not a valid Jabber status");
    }

    public PresenceStatus queryContactStatus(String contactIdentifier) throws IllegalArgumentException, IllegalStateException, OperationFailedException {
        assertConnected();
        XMPPConnection xmppConnection = ((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection();
        if (xmppConnection == null) {
            throw new IllegalArgumentException("The provider/account must be signed on in order to query the status of a contact in its roster");
        }
        Presence presence = xmppConnection.getRoster().getPresence(contactIdentifier);
        if (presence != null) {
            return jabberStatusToPresenceStatus(presence, (ProtocolProviderServiceJabberImpl) this.parentProvider);
        }
        return ((ProtocolProviderServiceJabberImpl) this.parentProvider).getJabberStatusEnum().getStatus(SipStatusEnum.OFFLINE);
    }

    public void removeServerStoredContactGroup(ContactGroup group) throws OperationFailedException {
        assertConnected();
        if (group instanceof ContactGroupJabberImpl) {
            this.ssContactList.removeGroup((ContactGroupJabberImpl) group);
            return;
        }
        throw new IllegalArgumentException("The specified group is not an jabber contact group: " + group);
    }

    public void removeServerStoredGroupChangeListener(ServerStoredGroupListener listener) {
        this.ssContactList.removeGroupListener(listener);
    }

    public void renameServerStoredContactGroup(ContactGroup group, String newName) {
        assertConnected();
        if (group instanceof ContactGroupJabberImpl) {
            this.ssContactList.renameGroup((ContactGroupJabberImpl) group, newName);
            return;
        }
        throw new IllegalArgumentException("The specified group is not an jabber contact group: " + group);
    }

    public void setAuthorizationHandler(AuthorizationHandler handler) {
        this.subscribtionPacketListener.setHandler(handler);
    }

    public void subscribe(ContactGroup parent, String contactIdentifier) throws IllegalArgumentException, IllegalStateException, OperationFailedException {
        assertConnected();
        if (parent instanceof ContactGroupJabberImpl) {
            this.ssContactList.addContact(parent, contactIdentifier);
            return;
        }
        throw new IllegalArgumentException("Argument is not an jabber contact group (group=" + parent + Separators.RPAREN);
    }

    public void subscribe(String contactIdentifier) throws IllegalArgumentException, IllegalStateException, OperationFailedException {
        assertConnected();
        this.ssContactList.addContact(contactIdentifier);
    }

    public void unsubscribe(Contact contact) throws IllegalArgumentException, IllegalStateException, OperationFailedException {
        assertConnected();
        if (contact instanceof ContactJabberImpl) {
            this.ssContactList.removeContact((ContactJabberImpl) contact);
            return;
        }
        throw new IllegalArgumentException("Argument is not an jabber contact (contact=" + contact + Separators.RPAREN);
    }

    public static PresenceStatus jabberStatusToPresenceStatus(Presence presence, ProtocolProviderServiceJabberImpl jabberProvider) {
        JabberStatusEnum jabberStatusEnum = jabberProvider.getJabberStatusEnum();
        if (presence.getMode() == null && presence.isAvailable()) {
            return jabberStatusEnum.getStatus("Available");
        }
        if (presence.getMode() == null && !presence.isAvailable()) {
            return jabberStatusEnum.getStatus(SipStatusEnum.OFFLINE);
        }
        Mode mode = presence.getMode();
        if (mode.equals(Mode.available)) {
            return jabberStatusEnum.getStatus("Available");
        }
        if (mode.equals(Mode.away)) {
            if (presence.getStatus() == null || !presence.getStatus().contains(SipStatusEnum.ON_THE_PHONE)) {
                return jabberStatusEnum.getStatus(SipStatusEnum.AWAY);
            }
            return jabberStatusEnum.getStatus(SipStatusEnum.ON_THE_PHONE);
        } else if (mode.equals(Mode.chat)) {
            return jabberStatusEnum.getStatus("Free For Chat");
        } else {
            if (mode.equals(Mode.dnd)) {
                return jabberStatusEnum.getStatus("Do Not Disturb");
            }
            if (mode.equals(Mode.xa)) {
                return jabberStatusEnum.getStatus("Extended Away");
            }
            if (presence.isAway()) {
                return jabberStatusEnum.getStatus(SipStatusEnum.AWAY);
            }
            if (presence.isAvailable()) {
                return jabberStatusEnum.getStatus("Available");
            }
            return jabberStatusEnum.getStatus(SipStatusEnum.OFFLINE);
        }
    }

    public static Mode presenceStatusToJabberMode(PresenceStatus status) {
        return (Mode) scToJabberModesMappings.get(status.getStatusName());
    }

    /* access modifiers changed from: 0000 */
    public void assertConnected() throws IllegalStateException {
        if (this.parentProvider == null) {
            throw new IllegalStateException("The provider must be non-null and signed on the Jabber service before being able to communicate.");
        } else if (!((ProtocolProviderServiceJabberImpl) this.parentProvider).isRegistered()) {
            if (this.currentStatus != null && this.currentStatus.isOnline()) {
                fireProviderStatusChangeEvent(this.currentStatus, ((ProtocolProviderServiceJabberImpl) this.parentProvider).getJabberStatusEnum().getStatus(SipStatusEnum.OFFLINE));
            }
            throw new IllegalStateException("The provider must be signed on the Jabber service before being able to communicate.");
        }
    }

    public void fireProviderStatusChangeEvent(PresenceStatus oldStatus, PresenceStatus newStatus) {
        if (!oldStatus.equals(newStatus)) {
            this.currentStatus = newStatus;
            OperationSetPersistentPresenceJabberImpl.super.fireProviderStatusChangeEvent(oldStatus, newStatus);
            PresenceStatus offlineStatus = ((ProtocolProviderServiceJabberImpl) this.parentProvider).getJabberStatusEnum().getStatus(SipStatusEnum.OFFLINE);
            if (newStatus.equals(offlineStatus)) {
                Iterator<Contact> contactsIter;
                Iterator<ContactGroup> groupsIter = getServerStoredContactListRoot().subgroups();
                while (groupsIter.hasNext()) {
                    contactsIter = ((ContactGroup) groupsIter.next()).contacts();
                    while (contactsIter.hasNext()) {
                        updateContactStatus((ContactJabberImpl) contactsIter.next(), offlineStatus);
                    }
                }
                contactsIter = getServerStoredContactListRoot().contacts();
                while (contactsIter.hasNext()) {
                    updateContactStatus((ContactJabberImpl) contactsIter.next(), offlineStatus);
                }
            }
        }
    }

    public void setDisplayName(Contact contact, String newName) throws IllegalArgumentException {
        assertConnected();
        if (contact instanceof ContactJabberImpl) {
            RosterEntry entry = ((ContactJabberImpl) contact).getSourceEntry();
            if (entry != null) {
                entry.setName(newName);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("Argument is not an jabber contact (contact=" + contact + Separators.RPAREN);
    }

    private boolean updateResources(ContactJabberImpl contact, boolean removeUnavailable) {
        if (!contact.isResolved() || ((contact instanceof VolatileContactJabberImpl) && ((VolatileContactJabberImpl) contact).isPrivateMessagingContact())) {
            return false;
        }
        boolean eventFired = false;
        Map<String, ContactResourceJabberImpl> resources = contact.getResourcesMap();
        if (((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection() != null && ((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection().isConnected()) {
            String fullJid;
            Iterator<Presence> it = ((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection().getRoster().getPresences(contact.getAddress());
            while (it.hasNext()) {
                Presence presence = (Presence) it.next();
                String resource = StringUtils.parseResource(presence.getFrom());
                if (resource != null && resource.length() > 0) {
                    fullJid = presence.getFrom();
                    ContactResourceJabberImpl contactResource = (ContactResourceJabberImpl) resources.get(fullJid);
                    PresenceStatus newPresenceStatus = jabberStatusToPresenceStatus(presence, (ProtocolProviderServiceJabberImpl) this.parentProvider);
                    if (contactResource == null) {
                        contactResource = new ContactResourceJabberImpl(fullJid, contact, resource, newPresenceStatus, presence.getPriority(), this.mobileIndicator.isMobileResource(resource, fullJid));
                        resources.put(fullJid, contactResource);
                        contact.fireContactResourceEvent(new ContactResourceEvent(contact, contactResource, 0));
                        eventFired = true;
                    } else {
                        boolean oldIndicator = contactResource.isMobile();
                        boolean newIndicator = this.mobileIndicator.isMobileResource(resource, fullJid);
                        int oldPriority = contactResource.getPriority();
                        contactResource.setMobile(newIndicator);
                        contactResource.setPriority(presence.getPriority());
                        if (oldPriority != contactResource.getPriority()) {
                            this.mobileIndicator.resourcesUpdated(contact);
                        }
                        if (contactResource.getPresenceStatus().getStatus() != newPresenceStatus.getStatus() || oldIndicator != newIndicator || oldPriority != contactResource.getPriority()) {
                            contactResource.setPresenceStatus(newPresenceStatus);
                            contact.fireContactResourceEvent(new ContactResourceEvent(contact, contactResource, 2));
                            eventFired = true;
                        }
                    }
                }
            }
            if (!removeUnavailable) {
                return eventFired;
            }
            for (String fullJid2 : resources.keySet()) {
                if (!((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection().getRoster().getPresenceResource(fullJid2).isAvailable()) {
                    ContactResource removedResource = (ContactResource) resources.get(fullJid2);
                    if (resources.containsKey(fullJid2)) {
                        resources.remove(fullJid2);
                        contact.fireContactResourceEvent(new ContactResourceEvent(contact, removedResource, 1));
                        eventFired = true;
                    }
                }
            }
            return eventFired;
        } else if (!removeUnavailable) {
            return false;
        } else {
            Iterator<Entry<String, ContactResourceJabberImpl>> iter = resources.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, ContactResourceJabberImpl> entry = (Entry) iter.next();
                iter.remove();
                contact.fireContactResourceEvent(new ContactResourceEvent(contact, (ContactResource) entry.getValue(), 1));
                eventFired = true;
            }
            return eventFired;
        }
    }

    /* access modifiers changed from: 0000 */
    public void firePresenceStatusChanged(Presence presence) {
        if (this.contactChangesListener != null) {
            this.contactChangesListener.firePresenceStatusChanged(presence);
        }
    }

    /* access modifiers changed from: private */
    public void updateContactStatus(ContactJabberImpl contact, PresenceStatus newStatus) {
        boolean oldMobileIndicator = contact.isMobile();
        boolean resourceUpdated = updateResources(contact, true);
        this.mobileIndicator.resourcesUpdated(contact);
        PresenceStatus oldStatus = contact.getPresenceStatus();
        if (!oldStatus.equals(newStatus) || oldMobileIndicator != contact.isMobile()) {
            contact.updatePresenceStatus(newStatus);
            if (logger.isDebugEnabled()) {
                logger.debug("Will Dispatch the contact status event.");
            }
            fireContactPresenceStatusChangeEvent(contact, contact.getParentContactGroup(), oldStatus, newStatus, resourceUpdated);
        }
    }

    public void createAccountPhotoPresenceInterceptor() {
        if (this.vCardTempXUpdatePresenceExtension == null) {
            byte[] avatar = null;
            try {
                VCard vCard = new VCard();
                vCard.load(((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection());
                avatar = vCard.getAvatar();
            } catch (XMPPException ex) {
                logger.info("Can not retrieve account avatar for " + ((ProtocolProviderServiceJabberImpl) this.parentProvider).getOurJID() + ": " + ex.getMessage());
            }
            this.vCardTempXUpdatePresenceExtension = new VCardTempXUpdatePresenceExtension(avatar);
            ((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection().addPacketInterceptor(this.vCardTempXUpdatePresenceExtension, new PacketTypeFilter(Presence.class));
        }
    }

    public void updateAccountPhotoPresenceExtension(byte[] imageBytes) {
        try {
            if (this.vCardTempXUpdatePresenceExtension.updateImage(imageBytes)) {
                publishPresenceStatus(this.currentStatus, this.currentStatusMessage);
            }
        } catch (OperationFailedException ex) {
            logger.info("Can not send presence extension to broadcast photo update", ex);
        }
    }

    public void createContactPhotoPresenceListener() {
        ((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection().addPacketListener(new PacketListener() {
            public void processPacket(Packet packet) {
                OperationSetPersistentPresenceJabberImpl.this.parseContactPhotoPresence(packet);
            }
        }, new AndFilter(new PacketTypeFilter(Presence.class), new PacketExtensionFilter("x", VCardTempXUpdatePresenceExtension.NAMESPACE)));
    }

    public void parseContactPhotoPresence(Packet packet) {
        String userID = StringUtils.parseBareAddress(packet.getFrom());
        ContactJabberImpl sourceContact = this.ssContactList.findContactById(userID);
        if (sourceContact != null) {
            byte[] currentAvatar = sourceContact.getImage(false);
            DefaultPacketExtension defaultPacketExtension = (DefaultPacketExtension) packet.getExtension("x", VCardTempXUpdatePresenceExtension.NAMESPACE);
            if (defaultPacketExtension != null) {
                try {
                    String packetPhotoSHA1 = defaultPacketExtension.getValue("photo");
                    if (packetPhotoSHA1 != null && !packetPhotoSHA1.equals(VCardTempXUpdatePresenceExtension.getImageSha1(currentAvatar))) {
                        byte[] newAvatar;
                        if (packetPhotoSHA1.length() != 0) {
                            VCard vCard = new VCard();
                            vCard.load(((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection(), userID);
                            newAvatar = vCard.getAvatar();
                        } else {
                            newAvatar = new byte[0];
                        }
                        sourceContact.setImage(newAvatar);
                        fireContactPropertyChangeEvent("Image", sourceContact, currentAvatar, newAvatar);
                    }
                } catch (XMPPException ex) {
                    logger.info("Cannot retrieve vCard from: " + packet.getFrom());
                    if (logger.isTraceEnabled()) {
                        logger.trace("vCard retrieval exception was: ", ex);
                    }
                }
            }
        }
    }

    private void initializePriorities() {
        try {
            this.resourcePriorityAvailable = Integer.parseInt(((ProtocolProviderServiceJabberImpl) this.parentProvider).getAccountID().getAccountPropertyString("RESOURCE_PRIORITY"));
        } catch (NumberFormatException ex) {
            logger.error("Wrong value for resource priority", ex);
        }
        addDefaultValue(SipStatusEnum.AWAY, -5);
        addDefaultValue("Extended Away", -10);
        addDefaultValue(SipStatusEnum.ON_THE_PHONE, -15);
        addDefaultValue("Do Not Disturb", -20);
        addDefaultValue("Free For Chat", 5);
    }

    private void addDefaultValue(String statusName, int availableShift) {
        String resourcePriority = getAccountPriorityForStatus(statusName);
        if (resourcePriority != null) {
            try {
                addPresenceToPriorityMapping(statusName, Integer.parseInt(resourcePriority));
                return;
            } catch (NumberFormatException ex) {
                logger.error("Wrong value for resource priority for status: " + statusName, ex);
                return;
            }
        }
        int priority = this.resourcePriorityAvailable + availableShift;
        if (priority <= 0) {
            priority = this.resourcePriorityAvailable;
        }
        addPresenceToPriorityMapping(statusName, priority);
    }

    private static void addPresenceToPriorityMapping(String statusName, int value) {
        statusToPriorityMappings.put(statusName.replaceAll(Separators.SP, "_").toUpperCase(), Integer.valueOf(value));
    }

    private int getPriorityForPresenceStatus(String statusName) {
        Integer priority = (Integer) statusToPriorityMappings.get(statusName.replaceAll(Separators.SP, "_").toUpperCase());
        if (priority == null) {
            return this.resourcePriorityAvailable;
        }
        return priority.intValue();
    }

    private String getAccountPriorityForStatus(String statusName) {
        return ((ProtocolProviderServiceJabberImpl) this.parentProvider).getAccountID().getAccountPropertyString("RESOURCE_PRIORITY_" + statusName.replaceAll(Separators.SP, "_").toUpperCase());
    }

    public ServerStoredContactListJabberImpl getSsContactList() {
        return this.ssContactList;
    }
}
