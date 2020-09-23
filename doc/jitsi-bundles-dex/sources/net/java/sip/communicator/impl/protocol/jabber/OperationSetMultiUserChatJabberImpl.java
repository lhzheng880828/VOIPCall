package net.java.sip.communicator.impl.protocol.jabber;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.java.sip.communicator.service.protocol.AbstractOperationSetMultiUserChat;
import net.java.sip.communicator.service.protocol.ChatRoom;
import net.java.sip.communicator.service.protocol.ChatRoomInvitation;
import net.java.sip.communicator.service.protocol.ChatRoomMember;
import net.java.sip.communicator.service.protocol.ChatRoomMemberRole;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationNotSupportedException;
import net.java.sip.communicator.service.protocol.OperationSetMultiUserChat;
import net.java.sip.communicator.service.protocol.OperationSetPersistentPresence;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.ContactPropertyChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.service.protocol.event.SubscriptionEvent;
import net.java.sip.communicator.service.protocol.event.SubscriptionListener;
import net.java.sip.communicator.service.protocol.event.SubscriptionMovedEvent;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.InvitationRejectionListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

public class OperationSetMultiUserChatJabberImpl extends AbstractOperationSetMultiUserChat implements SubscriptionListener {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(OperationSetMultiUserChatJabberImpl.class);
    /* access modifiers changed from: private|final */
    public final Hashtable<String, ChatRoom> chatRoomCache = new Hashtable();
    /* access modifiers changed from: private|final */
    public final ProtocolProviderServiceJabberImpl jabberProvider;
    private OperationSetPersistentPresenceJabberImpl opSetPersPresence = null;
    private final RegistrationStateListener providerRegListener = new RegistrationStateListener();

    private class RegistrationStateListener implements RegistrationStateChangeListener {
        private RegistrationStateListener() {
        }

        public void registrationStateChanged(RegistrationStateChangeEvent evt) {
            if (evt.getNewState() == RegistrationState.REGISTERED) {
                if (OperationSetMultiUserChatJabberImpl.logger.isDebugEnabled()) {
                    OperationSetMultiUserChatJabberImpl.logger.debug("adding an Invitation listener to the smack muc");
                }
                MultiUserChat.addInvitationListener(OperationSetMultiUserChatJabberImpl.this.jabberProvider.getConnection(), new SmackInvitationListener());
            } else if (evt.getNewState() == RegistrationState.UNREGISTERED || evt.getNewState() == RegistrationState.CONNECTION_FAILED) {
                OperationSetMultiUserChatJabberImpl.this.chatRoomCache.clear();
            } else if (evt.getNewState() == RegistrationState.UNREGISTERING) {
                for (ChatRoom room : OperationSetMultiUserChatJabberImpl.this.getCurrentlyJoinedChatRooms()) {
                    room.leave();
                }
            }
        }
    }

    private class SmackInvitationListener implements InvitationListener {
        private SmackInvitationListener() {
        }

        public void invitationReceived(Connection conn, String room, String inviter, String reason, String password, Message message) {
            try {
                ChatRoomJabberImpl chatRoom = (ChatRoomJabberImpl) OperationSetMultiUserChatJabberImpl.this.findRoom(room);
                if (password != null) {
                    OperationSetMultiUserChatJabberImpl.this.fireInvitationEvent(chatRoom, inviter, reason, password.getBytes());
                } else {
                    OperationSetMultiUserChatJabberImpl.this.fireInvitationEvent(chatRoom, inviter, reason, null);
                }
            } catch (OperationFailedException e) {
                OperationSetMultiUserChatJabberImpl.logger.error("Failed to find room with name: " + room, e);
            } catch (OperationNotSupportedException e2) {
                OperationSetMultiUserChatJabberImpl.logger.error("Failed to find room with name: " + room, e2);
            }
        }
    }

    private class SmackInvitationRejectionListener implements InvitationRejectionListener {
        private ChatRoom chatRoom;

        public SmackInvitationRejectionListener(ChatRoom chatRoom) {
            this.chatRoom = chatRoom;
        }

        public void invitationDeclined(String invitee, String reason) {
            OperationSetMultiUserChatJabberImpl.this.fireInvitationRejectedEvent(this.chatRoom, invitee, reason);
        }
    }

    OperationSetMultiUserChatJabberImpl(ProtocolProviderServiceJabberImpl jabberProvider) {
        this.jabberProvider = jabberProvider;
        jabberProvider.addRegistrationStateChangeListener(this.providerRegListener);
        this.opSetPersPresence = (OperationSetPersistentPresenceJabberImpl) jabberProvider.getOperationSet(OperationSetPersistentPresence.class);
        this.opSetPersPresence.addSubscriptionListener(this);
    }

    public void addSmackInvitationRejectionListener(MultiUserChat muc, ChatRoom chatRoom) {
        muc.addInvitationRejectionListener(new SmackInvitationRejectionListener(chatRoom));
    }

    public ChatRoom createChatRoom(String roomName, Map<String, Object> roomProperties) throws OperationFailedException, OperationNotSupportedException {
        XMPPException ex;
        assertSupportedAndConnected();
        ChatRoom room = null;
        if (roomName == null) {
            roomName = "chatroom-" + StringUtils.randomString(4);
        } else {
            room = findRoom(roomName);
        }
        if (room != null) {
            return room;
        }
        if (logger.isInfoEnabled()) {
            logger.info("Find room returns null.");
        }
        if (getXmppConnection().getHost().toLowerCase().contains("google")) {
            roomName = "private-chat-" + UUID.randomUUID().toString() + "@groupchat.google.com";
        }
        try {
            MultiUserChat muc = new MultiUserChat(getXmppConnection(), getCanonicalRoomName(roomName));
            try {
                muc.create(StringUtils.parseName(getXmppConnection().getUser()));
                boolean isPrivate = false;
                if (roomProperties != null) {
                    Object isPrivateObject = roomProperties.get("isPrivate");
                    if (isPrivateObject != null) {
                        isPrivate = isPrivateObject.equals(Boolean.valueOf(true));
                    }
                }
                Form form;
                if (isPrivate) {
                    try {
                        Form initForm = muc.getConfigurationForm();
                        form = initForm.createAnswerForm();
                        Iterator<FormField> fieldIterator = initForm.getFields();
                        while (fieldIterator.hasNext()) {
                            FormField initField = (FormField) fieldIterator.next();
                            if (!(initField == null || initField.getVariable() == null || initField.getType() == FormField.TYPE_FIXED || initField.getType() == "hidden")) {
                                FormField submitField = form.getField(initField.getVariable());
                                if (submitField != null) {
                                    Iterator<String> value = initField.getValues();
                                    while (value.hasNext()) {
                                        submitField.addValue((String) value.next());
                                    }
                                }
                            }
                        }
                        String[] fields = new String[]{"muc#roomconfig_membersonly", "muc#roomconfig_allowinvites", "muc#roomconfig_publicroom"};
                        Boolean[] values = new Boolean[]{Boolean.valueOf(true), Boolean.valueOf(true), Boolean.valueOf(false)};
                        for (int i = 0; i < fields.length; i++) {
                            FormField field = new FormField(fields[i]);
                            field.setType(FormField.TYPE_BOOLEAN);
                            form.addField(field);
                            form.setAnswer(fields[i], values[i].booleanValue());
                        }
                        muc.sendConfigurationForm(form);
                    } catch (XMPPException e) {
                        logger.error("Failed to send config form.", e);
                    }
                } else {
                    form = new Form(Form.TYPE_SUBMIT);
                    muc.sendConfigurationForm(form);
                }
                room = createLocalChatRoomInstance(muc);
                room.setLocalUserRole(ChatRoomMemberRole.OWNER);
                return room;
            } catch (XMPPException e2) {
                ex = e2;
                MultiUserChat multiUserChat = muc;
                logger.error("Failed to create chat room.", ex);
                throw new OperationFailedException("Failed to create chat room", ex.getXMPPError().getCode(), ex.getCause());
            }
        } catch (XMPPException e3) {
            ex = e3;
            logger.error("Failed to create chat room.", ex);
            throw new OperationFailedException("Failed to create chat room", ex.getXMPPError().getCode(), ex.getCause());
        }
    }

    private ChatRoom createLocalChatRoomInstance(MultiUserChat muc) {
        ChatRoomJabberImpl chatRoom;
        synchronized (this.chatRoomCache) {
            chatRoom = new ChatRoomJabberImpl(muc, this.jabberProvider);
            cacheChatRoom(chatRoom);
            addSmackInvitationRejectionListener(muc, chatRoom);
        }
        return chatRoom;
    }

    public synchronized ChatRoom findRoom(String roomName) throws OperationFailedException, OperationNotSupportedException {
        Object room;
        assertSupportedAndConnected();
        String canonicalRoomName = getCanonicalRoomName(roomName);
        ChatRoom room2 = (ChatRoom) this.chatRoomCache.get(canonicalRoomName);
        if (room2 != null) {
            room = room2;
        } else {
            room2 = new ChatRoomJabberImpl(new MultiUserChat(getXmppConnection(), canonicalRoomName), this.jabberProvider);
            this.chatRoomCache.put(canonicalRoomName, room2);
            ChatRoom room3 = room2;
        }
        return room3;
    }

    public List<ChatRoom> getCurrentlyJoinedChatRooms() {
        List<ChatRoom> joinedRooms;
        synchronized (this.chatRoomCache) {
            joinedRooms = new LinkedList(this.chatRoomCache.values());
            Iterator<ChatRoom> joinedRoomsIter = joinedRooms.iterator();
            while (joinedRoomsIter.hasNext()) {
                if (!((ChatRoom) joinedRoomsIter.next()).isJoined()) {
                    joinedRoomsIter.remove();
                }
            }
        }
        return joinedRooms;
    }

    public List<String> getExistingChatRooms() throws OperationFailedException, OperationNotSupportedException {
        assertSupportedAndConnected();
        List<String> list = new LinkedList();
        try {
            for (String serviceName : MultiUserChat.getServiceNames(getXmppConnection())) {
                List<HostedRoom> roomsOnThisService = new LinkedList();
                try {
                    roomsOnThisService.addAll(MultiUserChat.getHostedRooms(getXmppConnection(), serviceName));
                    for (HostedRoom jid : roomsOnThisService) {
                        list.add(jid.getJid());
                    }
                } catch (XMPPException ex) {
                    logger.error("Failed to retrieve rooms for serviceName=" + serviceName, ex);
                }
            }
            return list;
        } catch (XMPPException ex2) {
            throw new OperationFailedException("Failed to retrieve Jabber conference service names", 1, ex2);
        }
    }

    public boolean isMultiChatSupportedByContact(Contact contact) {
        if (contact.getProtocolProvider().getOperationSet(OperationSetMultiUserChat.class) != null) {
            return true;
        }
        return false;
    }

    public boolean isPrivateMessagingContact(String contactAddress) {
        return this.opSetPersPresence.isPrivateMessagingContact(contactAddress);
    }

    public void rejectInvitation(ChatRoomInvitation invitation, String rejectReason) {
        MultiUserChat.decline(this.jabberProvider.getConnection(), invitation.getTargetChatRoom().getName(), invitation.getInviter(), rejectReason);
    }

    private XMPPConnection getXmppConnection() {
        return this.jabberProvider == null ? null : this.jabberProvider.getConnection();
    }

    private void assertSupportedAndConnected() throws OperationFailedException, OperationNotSupportedException {
        if (!this.jabberProvider.isRegistered() || !getXmppConnection().isConnected()) {
            throw new OperationFailedException("Provider not connected to jabber server", 2);
        }
    }

    private String getCanonicalRoomName(String roomName) throws OperationFailedException {
        if (roomName.indexOf(64) > 0) {
            return roomName;
        }
        try {
            Iterator<String> serviceNamesIter = MultiUserChat.getServiceNames(getXmppConnection()).iterator();
            if (serviceNamesIter.hasNext()) {
                return roomName + Separators.AT + ((String) serviceNamesIter.next());
            }
            throw new OperationFailedException("Failed to retrieve MultiUserChat service names.", 1);
        } catch (XMPPException ex) {
            logger.error("Failed to retrieve conference service name for user: " + this.jabberProvider.getAccountID().getUserID() + " on server: " + this.jabberProvider.getAccountID().getService(), ex);
            throw new OperationFailedException("Failed to retrieve conference service name for user: " + this.jabberProvider.getAccountID().getUserID() + " on server: " + this.jabberProvider.getAccountID().getService(), 1, ex);
        }
    }

    private void cacheChatRoom(ChatRoom chatRoom) {
        this.chatRoomCache.put(chatRoom.getName(), chatRoom);
    }

    public ChatRoomJabberImpl getChatRoom(String chatRoomName) {
        return (ChatRoomJabberImpl) this.chatRoomCache.get(chatRoomName);
    }

    public List<String> getCurrentlyJoinedChatRooms(ChatRoomMember chatRoomMember) throws OperationFailedException, OperationNotSupportedException {
        assertSupportedAndConnected();
        Iterator<String> joinedRoomsIter = MultiUserChat.getJoinedRooms(getXmppConnection(), chatRoomMember.getContactAddress());
        List<String> joinedRooms = new ArrayList();
        while (joinedRoomsIter.hasNext()) {
            joinedRooms.add(joinedRoomsIter.next());
        }
        return joinedRooms;
    }

    public void fireInvitationEvent(ChatRoom targetChatRoom, String inviter, String reason, byte[] password) {
        fireInvitationReceived(new ChatRoomInvitationJabberImpl(targetChatRoom, inviter, reason, password));
    }

    public void contactModified(ContactPropertyChangeEvent evt) {
        updateChatRoomMembers(evt.getSourceContact());
    }

    public void subscriptionCreated(SubscriptionEvent evt) {
        updateChatRoomMembers(evt.getSourceContact());
    }

    public void subscriptionFailed(SubscriptionEvent evt) {
    }

    public void subscriptionMoved(SubscriptionMovedEvent evt) {
    }

    public void subscriptionRemoved(SubscriptionEvent evt) {
    }

    public void subscriptionResolved(SubscriptionEvent evt) {
    }

    private void updateChatRoomMembers(Contact contact) {
        Enumeration<ChatRoom> chatRooms = this.chatRoomCache.elements();
        while (chatRooms.hasMoreElements()) {
            ChatRoomMemberJabberImpl member = ((ChatRoomJabberImpl) chatRooms.nextElement()).findMemberForNickName(contact.getAddress());
            if (member != null) {
                member.setContact(contact);
                member.setAvatar(contact.getImage());
            }
        }
    }
}
