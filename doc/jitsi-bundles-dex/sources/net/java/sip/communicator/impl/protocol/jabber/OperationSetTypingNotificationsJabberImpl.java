package net.java.sip.communicator.impl.protocol.jabber;

import net.java.sip.communicator.impl.protocol.sip.SipStatusEnum;
import net.java.sip.communicator.service.protocol.AbstractOperationSetTypingNotifications;
import net.java.sip.communicator.service.protocol.ChatRoom;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationSetBasicInstantMessaging;
import net.java.sip.communicator.service.protocol.OperationSetMultiUserChat;
import net.java.sip.communicator.service.protocol.OperationSetPersistentPresence;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.util.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.ChatState;
import org.jivesoftware.smackx.ChatStateListener;
import org.jivesoftware.smackx.ChatStateManager;
import org.jivesoftware.smackx.MessageEventManager;
import org.jivesoftware.smackx.MessageEventNotificationListener;
import org.jivesoftware.smackx.MessageEventRequestListener;

public class OperationSetTypingNotificationsJabberImpl extends AbstractOperationSetTypingNotifications<ProtocolProviderServiceJabberImpl> {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(OperationSetTypingNotificationsJabberImpl.class);
    /* access modifiers changed from: private */
    public MessageEventManager messageEventManager = null;
    /* access modifiers changed from: private */
    public OperationSetBasicInstantMessagingJabberImpl opSetBasicIM = null;
    /* access modifiers changed from: private */
    public OperationSetPersistentPresenceJabberImpl opSetPersPresence = null;
    private ProviderRegListener providerRegListener = new ProviderRegListener();
    /* access modifiers changed from: private */
    public SmackChatManagerListener smackChatManagerListener = null;
    /* access modifiers changed from: private */
    public SmackChatStateListener smackChatStateListener = null;

    private class IncomingMessageEventsListener implements MessageEventNotificationListener {
        private IncomingMessageEventsListener() {
        }

        public void deliveredNotification(String from, String packetID) {
        }

        public void displayedNotification(String from, String packetID) {
        }

        public void composingNotification(String from, String packetID) {
            Contact sourceContact = OperationSetTypingNotificationsJabberImpl.this.opSetPersPresence.findContactByID(StringUtils.parseBareAddress(from));
            if (sourceContact == null) {
                sourceContact = OperationSetTypingNotificationsJabberImpl.this.opSetPersPresence.createVolatileContact(from);
            }
            OperationSetTypingNotificationsJabberImpl.this.fireTypingNotificationsEvent(sourceContact, 1);
        }

        public void offlineNotification(String from, String packetID) {
        }

        public void cancelledNotification(String from, String packetID) {
            Contact sourceContact = OperationSetTypingNotificationsJabberImpl.this.opSetPersPresence.findContactByID(StringUtils.parseBareAddress(from));
            if (sourceContact == null) {
                sourceContact = OperationSetTypingNotificationsJabberImpl.this.opSetPersPresence.createVolatileContact(from);
            }
            OperationSetTypingNotificationsJabberImpl.this.fireTypingNotificationsEvent(sourceContact, 4);
        }
    }

    private class JabberMessageEventRequestListener implements MessageEventRequestListener {
        private JabberMessageEventRequestListener() {
        }

        public void deliveredNotificationRequested(String from, String packetID, MessageEventManager messageEventManager) {
            messageEventManager.sendDeliveredNotification(from, packetID);
        }

        public void displayedNotificationRequested(String from, String packetID, MessageEventManager messageEventManager) {
            messageEventManager.sendDisplayedNotification(from, packetID);
        }

        public void composingNotificationRequested(String from, String packetID, MessageEventManager messageEventManager) {
        }

        public void offlineNotificationRequested(String from, String packetID, MessageEventManager messageEventManager) {
        }
    }

    private class ProviderRegListener implements RegistrationStateChangeListener {
        private ProviderRegListener() {
        }

        public void registrationStateChanged(RegistrationStateChangeEvent evt) {
            if (OperationSetTypingNotificationsJabberImpl.logger.isDebugEnabled()) {
                OperationSetTypingNotificationsJabberImpl.logger.debug("The provider changed state from: " + evt.getOldState() + " to: " + evt.getNewState());
            }
            if (evt.getNewState() == RegistrationState.REGISTERED) {
                OperationSetTypingNotificationsJabberImpl.this.opSetPersPresence = (OperationSetPersistentPresenceJabberImpl) ((ProtocolProviderServiceJabberImpl) OperationSetTypingNotificationsJabberImpl.this.parentProvider).getOperationSet(OperationSetPersistentPresence.class);
                OperationSetTypingNotificationsJabberImpl.this.opSetBasicIM = (OperationSetBasicInstantMessagingJabberImpl) ((ProtocolProviderServiceJabberImpl) OperationSetTypingNotificationsJabberImpl.this.parentProvider).getOperationSet(OperationSetBasicInstantMessaging.class);
                OperationSetTypingNotificationsJabberImpl.this.messageEventManager = new MessageEventManager(((ProtocolProviderServiceJabberImpl) OperationSetTypingNotificationsJabberImpl.this.parentProvider).getConnection());
                OperationSetTypingNotificationsJabberImpl.this.messageEventManager.addMessageEventRequestListener(new JabberMessageEventRequestListener());
                OperationSetTypingNotificationsJabberImpl.this.messageEventManager.addMessageEventNotificationListener(new IncomingMessageEventsListener());
                ChatStateManager.getInstance(((ProtocolProviderServiceJabberImpl) OperationSetTypingNotificationsJabberImpl.this.parentProvider).getConnection());
                if (OperationSetTypingNotificationsJabberImpl.this.smackChatManagerListener == null) {
                    OperationSetTypingNotificationsJabberImpl.this.smackChatManagerListener = new SmackChatManagerListener();
                }
                ((ProtocolProviderServiceJabberImpl) OperationSetTypingNotificationsJabberImpl.this.parentProvider).getConnection().getChatManager().addChatListener(OperationSetTypingNotificationsJabberImpl.this.smackChatManagerListener);
            } else if (evt.getNewState() == RegistrationState.UNREGISTERED || evt.getNewState() == RegistrationState.AUTHENTICATION_FAILED || evt.getNewState() == RegistrationState.CONNECTION_FAILED) {
                if (!(((ProtocolProviderServiceJabberImpl) OperationSetTypingNotificationsJabberImpl.this.parentProvider).getConnection() == null || ((ProtocolProviderServiceJabberImpl) OperationSetTypingNotificationsJabberImpl.this.parentProvider).getConnection().getChatManager() == null)) {
                    ((ProtocolProviderServiceJabberImpl) OperationSetTypingNotificationsJabberImpl.this.parentProvider).getConnection().getChatManager().removeChatListener(OperationSetTypingNotificationsJabberImpl.this.smackChatManagerListener);
                }
                OperationSetTypingNotificationsJabberImpl.this.smackChatManagerListener = null;
                if (OperationSetTypingNotificationsJabberImpl.this.messageEventManager != null) {
                    OperationSetTypingNotificationsJabberImpl.this.messageEventManager.destroy();
                    OperationSetTypingNotificationsJabberImpl.this.messageEventManager = null;
                }
            }
        }
    }

    private class SmackChatManagerListener implements ChatManagerListener {
        private SmackChatManagerListener() {
        }

        public void chatCreated(Chat chat, boolean isLocal) {
            if (OperationSetTypingNotificationsJabberImpl.logger.isTraceEnabled()) {
                OperationSetTypingNotificationsJabberImpl.logger.trace("Created a chat with " + chat.getParticipant() + " local=" + isLocal);
            }
            if (OperationSetTypingNotificationsJabberImpl.this.smackChatStateListener == null) {
                OperationSetTypingNotificationsJabberImpl.this.smackChatStateListener = new SmackChatStateListener();
            }
            chat.addMessageListener(OperationSetTypingNotificationsJabberImpl.this.smackChatStateListener);
        }
    }

    private class SmackChatStateListener implements ChatStateListener {
        private SmackChatStateListener() {
        }

        public void stateChanged(Chat chat, ChatState state, Message message) {
            if (OperationSetTypingNotificationsJabberImpl.logger.isTraceEnabled()) {
                OperationSetTypingNotificationsJabberImpl.logger.trace(chat.getParticipant() + " entered the " + state.name() + " state.");
            }
            String fromID = StringUtils.parseBareAddress(chat.getParticipant());
            boolean isPrivateMessagingAddress = false;
            for (ChatRoom chatRoom : ((OperationSetMultiUserChat) ((ProtocolProviderServiceJabberImpl) OperationSetTypingNotificationsJabberImpl.this.parentProvider).getOperationSet(OperationSetMultiUserChat.class)).getCurrentlyJoinedChatRooms()) {
                if (chatRoom.getName().equals(fromID)) {
                    isPrivateMessagingAddress = true;
                    break;
                }
            }
            OperationSetPersistentPresenceJabberImpl access$200 = OperationSetTypingNotificationsJabberImpl.this.opSetPersPresence;
            if (isPrivateMessagingAddress) {
                fromID = message.getFrom();
            }
            Contact sourceContact = access$200.findContactByID(fromID);
            if (sourceContact == null) {
                sourceContact = OperationSetTypingNotificationsJabberImpl.this.opSetPersPresence.createVolatileContact(chat.getParticipant(), isPrivateMessagingAddress);
            }
            int evtCode = 0;
            if (ChatState.composing.equals(state)) {
                evtCode = 1;
            } else if (ChatState.paused.equals(state) || ChatState.active.equals(state)) {
                evtCode = 3;
            } else if (ChatState.inactive.equals(state) || ChatState.gone.equals(state)) {
                evtCode = 4;
            }
            if (message.getError() != null) {
                OperationSetTypingNotificationsJabberImpl.this.fireTypingNotificationsDeliveryFailedEvent(sourceContact, evtCode);
            } else if (evtCode != 0) {
                OperationSetTypingNotificationsJabberImpl.this.fireTypingNotificationsEvent(sourceContact, evtCode);
            } else {
                OperationSetTypingNotificationsJabberImpl.logger.warn("Unknown typing state!");
            }
        }

        public void processMessage(Chat chat, Message msg) {
            if (OperationSetTypingNotificationsJabberImpl.logger.isTraceEnabled()) {
                OperationSetTypingNotificationsJabberImpl.logger.trace("ignoring a process message");
            }
        }
    }

    OperationSetTypingNotificationsJabberImpl(ProtocolProviderServiceJabberImpl provider) {
        super(provider);
        provider.addRegistrationStateChangeListener(this.providerRegListener);
    }

    public void sendTypingNotification(Contact notifiedContact, int typingState) throws IllegalStateException, IllegalArgumentException {
        assertConnected();
        if (notifiedContact instanceof ContactJabberImpl) {
            sendXep85ChatState(notifiedContact, typingState);
            return;
        }
        throw new IllegalArgumentException("The specified contact is not a Jabber contact." + notifiedContact);
    }

    private void sendXep85ChatState(Contact contact, int state) {
        String toJID = null;
        if (null == null) {
            toJID = this.opSetBasicIM.getJidForAddress(contact.getAddress());
        }
        if (toJID != null) {
            ChatState chatState;
            if (logger.isTraceEnabled()) {
                logger.trace("Sending XEP-0085 chat state=" + state + " to " + toJID);
            }
            Chat chat = ((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection().getChatManager().createChat(toJID, null);
            if (state == 1) {
                chatState = ChatState.composing;
            } else if (state == 4) {
                chatState = ChatState.inactive;
            } else if (state == 3) {
                chatState = ChatState.paused;
            } else {
                chatState = ChatState.gone;
            }
            try {
                ChatStateManager.getInstance(((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection()).setCurrentState(chatState, chat);
            } catch (XMPPException exc) {
                logger.warn("Failed to send state [" + state + "] to [" + contact.getAddress() + "].", exc);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void assertConnected() throws IllegalStateException {
        if (!(this.parentProvider == null || ((ProtocolProviderServiceJabberImpl) this.parentProvider).isRegistered() || !this.opSetPersPresence.getPresenceStatus().isOnline())) {
            this.opSetPersPresence.fireProviderStatusChangeEvent(this.opSetPersPresence.getPresenceStatus(), ((ProtocolProviderServiceJabberImpl) this.parentProvider).getJabberStatusEnum().getStatus(SipStatusEnum.OFFLINE));
        }
        OperationSetTypingNotificationsJabberImpl.super.assertConnected();
    }
}
