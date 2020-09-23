package net.java.sip.communicator.impl.protocol.jabber;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import net.java.sip.communicator.impl.protocol.jabber.extensions.ConferenceDescriptionPacketExtension;
import net.java.sip.communicator.impl.protocol.sip.SipStatusEnum;
import net.java.sip.communicator.service.protocol.AbstractChatRoom;
import net.java.sip.communicator.service.protocol.ChatRoom;
import net.java.sip.communicator.service.protocol.ChatRoomConfigurationForm;
import net.java.sip.communicator.service.protocol.ChatRoomMember;
import net.java.sip.communicator.service.protocol.ChatRoomMemberRole;
import net.java.sip.communicator.service.protocol.ConferenceDescription;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetBasicTelephony;
import net.java.sip.communicator.service.protocol.OperationSetMultiUserChat;
import net.java.sip.communicator.service.protocol.OperationSetPersistentPresence;
import net.java.sip.communicator.service.protocol.OperationSetServerStoredAccountInfo;
import net.java.sip.communicator.service.protocol.PresenceStatus;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.DisplayNameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.GenericDetail;
import net.java.sip.communicator.service.protocol.event.ChatRoomLocalUserRoleChangeEvent;
import net.java.sip.communicator.service.protocol.event.ChatRoomLocalUserRoleListener;
import net.java.sip.communicator.service.protocol.event.ChatRoomMemberPresenceChangeEvent;
import net.java.sip.communicator.service.protocol.event.ChatRoomMemberPresenceListener;
import net.java.sip.communicator.service.protocol.event.ChatRoomMemberPropertyChangeEvent;
import net.java.sip.communicator.service.protocol.event.ChatRoomMemberPropertyChangeListener;
import net.java.sip.communicator.service.protocol.event.ChatRoomMemberRoleChangeEvent;
import net.java.sip.communicator.service.protocol.event.ChatRoomMemberRoleListener;
import net.java.sip.communicator.service.protocol.event.ChatRoomMessageDeliveredEvent;
import net.java.sip.communicator.service.protocol.event.ChatRoomMessageDeliveryFailedEvent;
import net.java.sip.communicator.service.protocol.event.ChatRoomMessageListener;
import net.java.sip.communicator.service.protocol.event.ChatRoomMessageReceivedEvent;
import net.java.sip.communicator.service.protocol.event.ChatRoomPropertyChangeEvent;
import net.java.sip.communicator.service.protocol.event.ChatRoomPropertyChangeFailedEvent;
import net.java.sip.communicator.service.protocol.event.ChatRoomPropertyChangeListener;
import net.java.sip.communicator.util.ConfigurationUtils;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.message.Response;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.FromMatchesFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.MessageEventManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.muc.SubjectUpdatedListener;
import org.jivesoftware.smackx.muc.UserStatusListener;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.MUCUser;
import org.jivesoftware.smackx.packet.MessageEvent;

public class ChatRoomJabberImpl extends AbstractChatRoom {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(ChatRoomJabberImpl.class);
    /* access modifiers changed from: private|final */
    public final Hashtable<String, ChatRoomMember> banList = new Hashtable();
    private List<CallJabberImpl> chatRoomConferenceCalls = new ArrayList();
    private ChatRoomConfigurationFormJabberImpl configForm;
    private InvitationRejectionListeners invitationRejectionListeners = new InvitationRejectionListeners();
    /* access modifiers changed from: private */
    public Presence lastPresenceSent = null;
    private final Vector<ChatRoomLocalUserRoleListener> localUserRoleListeners = new Vector();
    private final Vector<ChatRoomMemberPresenceListener> memberListeners = new Vector();
    private final Vector<ChatRoomMemberPropertyChangeListener> memberPropChangeListeners = new Vector();
    private final Vector<ChatRoomMemberRoleListener> memberRoleListeners = new Vector();
    /* access modifiers changed from: private|final */
    public final Hashtable<String, ChatRoomMemberJabberImpl> members = new Hashtable();
    private final Vector<ChatRoomMessageListener> messageListeners = new Vector();
    /* access modifiers changed from: private */
    public MultiUserChat multiUserChat = null;
    /* access modifiers changed from: private */
    public String nickname;
    /* access modifiers changed from: private */
    public String oldSubject;
    /* access modifiers changed from: private|final */
    public final OperationSetMultiUserChatJabberImpl opSetMuc;
    private final Vector<ChatRoomPropertyChangeListener> propertyChangeListeners = new Vector();
    /* access modifiers changed from: private|final */
    public final ProtocolProviderServiceJabberImpl provider;
    private ConferenceDescription publishedConference = null;
    /* access modifiers changed from: private */
    public ConferenceDescriptionPacketExtension publishedConferenceExt = null;
    private ChatRoomMemberRole role = null;

    private class InvitationRejectionListeners implements PacketListener {
        private InvitationRejectionListeners() {
        }

        public void processPacket(Packet packet) {
            MUCUser mucUser = ChatRoomJabberImpl.this.getMUCUserExtension(packet);
            if (mucUser != null && mucUser.getDecline() != null && ((Message) packet).getType() != Type.error) {
                ChatRoomMemberJabberImpl member = new ChatRoomMemberJabberImpl(ChatRoomJabberImpl.this, ChatRoomJabberImpl.this.getName(), ChatRoomJabberImpl.this.getName());
                String from = mucUser.getDecline().getFrom();
                OperationSetPersistentPresenceJabberImpl presenceOpSet = (OperationSetPersistentPresenceJabberImpl) ChatRoomJabberImpl.this.provider.getOperationSet(OperationSetPersistentPresence.class);
                if (presenceOpSet != null) {
                    Contact c = presenceOpSet.findContactByID(StringUtils.parseBareAddress(from));
                    if (!(c == null || from.contains(c.getDisplayName()))) {
                        from = c.getDisplayName() + " (" + from + Separators.RPAREN;
                    }
                }
                ChatRoomJabberImpl.this.fireMessageEvent(new ChatRoomMessageReceivedEvent(ChatRoomJabberImpl.this, member, new Date(), ChatRoomJabberImpl.this.createMessage(JabberActivator.getResources().getI18NString("service.gui.INVITATION_REJECTED", new String[]{from, mucUser.getDecline().getReason()})), 3));
            }
        }
    }

    private class MemberListener implements ParticipantStatusListener {
        private MemberListener() {
        }

        public void banned(String participant, String actor, String reason) {
            if (ChatRoomJabberImpl.logger.isInfoEnabled()) {
                ChatRoomJabberImpl.logger.info(participant + " has been banned from " + ChatRoomJabberImpl.this.getName() + " chat room.");
            }
            ChatRoomMemberJabberImpl member = ChatRoomJabberImpl.this.smackParticipantToScMember(participant);
            if (member != null) {
                String participantName = StringUtils.parseResource(participant);
                synchronized (ChatRoomJabberImpl.this.members) {
                    ChatRoomJabberImpl.this.members.remove(participantName);
                }
                ChatRoomJabberImpl.this.banList.put(participant, member);
                ChatRoomJabberImpl.this.fireMemberRoleEvent(member, member.getCurrentRole(), ChatRoomMemberRole.OUTCAST);
            }
        }

        public void adminGranted(String participant) {
            ChatRoomMemberJabberImpl member = ChatRoomJabberImpl.this.smackParticipantToScMember(participant);
            if (member != null) {
                ChatRoomJabberImpl.this.fireMemberRoleEvent(member, member.getCurrentRole(), ChatRoomMemberRole.ADMINISTRATOR);
            }
        }

        public void adminRevoked(String participant) {
            ChatRoomMemberJabberImpl member = ChatRoomJabberImpl.this.smackParticipantToScMember(participant);
            if (member != null) {
                ChatRoomJabberImpl.this.fireMemberRoleEvent(member, member.getCurrentRole(), ChatRoomMemberRole.MEMBER);
            }
        }

        public void joined(String participant) {
            if (ChatRoomJabberImpl.logger.isInfoEnabled()) {
                ChatRoomJabberImpl.logger.info(participant + " has joined the " + ChatRoomJabberImpl.this.getName() + " chat room.");
            }
            String participantName = StringUtils.parseResource(participant);
            if (!ChatRoomJabberImpl.this.nickname.equals(participantName) && !ChatRoomJabberImpl.this.members.containsKey(participantName) && !ChatRoomJabberImpl.this.members.contains(participantName)) {
                Occupant occupant = ChatRoomJabberImpl.this.multiUserChat.getOccupant(participant);
                ChatRoomMemberJabberImpl member = new ChatRoomMemberJabberImpl(ChatRoomJabberImpl.this, occupant.getNick(), occupant.getJid());
                ChatRoomJabberImpl.this.members.put(participantName, member);
                ChatRoomJabberImpl.this.fireMemberPresenceEvent(member, "MemberJoined", null);
            }
        }

        public void left(String participant) {
            if (ChatRoomJabberImpl.logger.isInfoEnabled()) {
                ChatRoomJabberImpl.logger.info(participant + " has left the " + ChatRoomJabberImpl.this.getName() + " chat room.");
            }
            ChatRoomMember member = ChatRoomJabberImpl.this.smackParticipantToScMember(participant);
            if (member != null) {
                String participantName = StringUtils.parseResource(participant);
                synchronized (ChatRoomJabberImpl.this.members) {
                    ChatRoomJabberImpl.this.members.remove(participantName);
                }
                ChatRoomJabberImpl.this.fireMemberPresenceEvent(member, "MemberLeft", null);
            }
        }

        public void nicknameChanged(String participant, String newNickname) {
            ChatRoomMember member = ChatRoomJabberImpl.this.smackParticipantToScMember(participant);
            if (member != null) {
                if (ChatRoomJabberImpl.this.nickname.equals(ChatRoomJabberImpl.getNickName(member.getName()))) {
                    ChatRoomJabberImpl.this.nickname = ChatRoomJabberImpl.getNickName(newNickname);
                }
                ((ChatRoomMemberJabberImpl) member).setName(newNickname);
                String participantName = StringUtils.parseResource(participant);
                synchronized (ChatRoomJabberImpl.this.members) {
                    ChatRoomJabberImpl.this.members.put(newNickname, (ChatRoomMemberJabberImpl) ChatRoomJabberImpl.this.members.remove(participantName));
                }
                ChatRoomJabberImpl.this.fireMemberPropertyChangeEvent(new ChatRoomMemberPropertyChangeEvent(member, ChatRoomJabberImpl.this, "MemberNickname", participantName, newNickname));
            }
        }

        public void ownershipRevoked(String participant) {
            ChatRoomMemberJabberImpl member = ChatRoomJabberImpl.this.smackParticipantToScMember(participant);
            if (member != null) {
                ChatRoomJabberImpl.this.fireMemberRoleEvent(member, member.getCurrentRole(), ChatRoomMemberRole.MEMBER);
            }
        }

        public void kicked(String participant, String actor, String reason) {
            ChatRoomMember member = ChatRoomJabberImpl.this.smackParticipantToScMember(participant);
            ChatRoomMember actorMember = ChatRoomJabberImpl.this.smackParticipantToScMember(actor);
            if (member != null) {
                String participantName = StringUtils.parseResource(participant);
                synchronized (ChatRoomJabberImpl.this.members) {
                    ChatRoomJabberImpl.this.members.remove(participantName);
                }
                ChatRoomJabberImpl.this.fireMemberPresenceEvent(member, actorMember, "MemberKicked", reason);
            }
        }

        public void moderatorGranted(String participant) {
            ChatRoomMemberJabberImpl member = ChatRoomJabberImpl.this.smackParticipantToScMember(participant);
            if (member != null) {
                ChatRoomJabberImpl.this.fireMemberRoleEvent(member, member.getCurrentRole(), ChatRoomMemberRole.MODERATOR);
            }
        }

        public void voiceRevoked(String participant) {
            ChatRoomMemberJabberImpl member = ChatRoomJabberImpl.this.smackParticipantToScMember(participant);
            if (member != null) {
                ChatRoomJabberImpl.this.fireMemberRoleEvent(member, member.getCurrentRole(), ChatRoomMemberRole.SILENT_MEMBER);
            }
        }

        public void membershipGranted(String participant) {
            ChatRoomMemberJabberImpl member = ChatRoomJabberImpl.this.smackParticipantToScMember(participant);
            if (member != null) {
                ChatRoomJabberImpl.this.fireMemberRoleEvent(member, member.getCurrentRole(), ChatRoomMemberRole.MEMBER);
            }
        }

        public void moderatorRevoked(String participant) {
            ChatRoomMemberJabberImpl member = ChatRoomJabberImpl.this.smackParticipantToScMember(participant);
            if (member != null) {
                ChatRoomJabberImpl.this.fireMemberRoleEvent(member, member.getCurrentRole(), ChatRoomMemberRole.MEMBER);
            }
        }

        public void voiceGranted(String participant) {
            ChatRoomMemberJabberImpl member = ChatRoomJabberImpl.this.smackParticipantToScMember(participant);
            if (member != null) {
                ChatRoomJabberImpl.this.fireMemberRoleEvent(member, member.getCurrentRole(), ChatRoomMemberRole.MEMBER);
            }
        }

        public void membershipRevoked(String participant) {
            ChatRoomMemberJabberImpl member = ChatRoomJabberImpl.this.smackParticipantToScMember(participant);
            if (member != null) {
                ChatRoomJabberImpl.this.fireMemberRoleEvent(member, member.getCurrentRole(), ChatRoomMemberRole.GUEST);
            }
        }

        public void ownershipGranted(String participant) {
            ChatRoomMemberJabberImpl member = ChatRoomJabberImpl.this.smackParticipantToScMember(participant);
            if (member != null) {
                ChatRoomJabberImpl.this.fireMemberRoleEvent(member, member.getCurrentRole(), ChatRoomMemberRole.OWNER);
            }
        }
    }

    private class PresenceInterceptor implements PacketInterceptor {
        private PresenceInterceptor() {
        }

        public void interceptPacket(Packet packet) {
            if (packet instanceof Presence) {
                ChatRoomJabberImpl.this.setConferenceDescriptionPacketExtension(packet, ChatRoomJabberImpl.this.publishedConferenceExt);
                ChatRoomJabberImpl.this.lastPresenceSent = (Presence) packet;
            }
        }
    }

    private class PresenceListener implements PacketListener {
        private ChatRoom chatRoom;

        public PresenceListener(ChatRoom chatRoom) {
            this.chatRoom = chatRoom;
        }

        public void processPacket(Packet packet) {
            if (packet != null && (packet instanceof Presence) && packet.getError() == null) {
                Presence presence = (Presence) packet;
                if ((ChatRoomJabberImpl.this.multiUserChat.getRoom() + Separators.SLASH + ChatRoomJabberImpl.this.multiUserChat.getNickname()).equals(presence.getFrom())) {
                    processOwnPresence(presence);
                    return;
                } else {
                    processOtherPresence(presence);
                    return;
                }
            }
            ChatRoomJabberImpl.logger.warn("Unable to handle packet: " + packet);
        }

        private void processOwnPresence(Presence presence) {
            MUCUser mucUser = ChatRoomJabberImpl.this.getMUCUserExtension(presence);
            if (mucUser != null) {
                String affiliation = mucUser.getItem().getAffiliation();
                String role = mucUser.getItem().getRole();
                if (mucUser.getStatus() == null || !"201".equals(mucUser.getStatus().getCode())) {
                    ChatRoomMemberRole jitsiRole = ChatRoomJabberImpl.smackRoleToScRole(role, affiliation);
                    if (jitsiRole == ChatRoomMemberRole.MODERATOR || jitsiRole == ChatRoomMemberRole.OWNER || jitsiRole == ChatRoomMemberRole.ADMINISTRATOR) {
                        ChatRoomJabberImpl.this.setLocalUserRole(jitsiRole, true);
                        return;
                    }
                    return;
                }
                try {
                    ChatRoomJabberImpl.this.multiUserChat.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
                } catch (XMPPException e) {
                    ChatRoomJabberImpl.logger.error("Failed to send config form.", e);
                }
                ChatRoomJabberImpl.this.opSetMuc.addSmackInvitationRejectionListener(ChatRoomJabberImpl.this.multiUserChat, this.chatRoom);
                if (affiliation.equalsIgnoreCase(ChatRoomMemberRole.OWNER.getRoleName().toLowerCase())) {
                    ChatRoomJabberImpl.this.setLocalUserRole(ChatRoomMemberRole.OWNER, true);
                } else {
                    ChatRoomJabberImpl.this.setLocalUserRole(ChatRoomMemberRole.MODERATOR, true);
                }
            }
        }

        private void processOtherPresence(Presence presence) {
            PacketExtension ext = presence.getExtension(ConferenceDescriptionPacketExtension.NAMESPACE);
            if (presence.isAvailable() && ext != null) {
                ConferenceDescription cd = ((ConferenceDescriptionPacketExtension) ext).toConferenceDescription();
                String from = presence.getFrom();
                String participantName = null;
                if (from != null) {
                    participantName = StringUtils.parseResource(from);
                }
                ChatRoomMember member = (ChatRoomMember) ChatRoomJabberImpl.this.members.get(participantName);
                if (!ChatRoomJabberImpl.this.processConferenceDescription(cd, participantName)) {
                    return;
                }
                if (member != null) {
                    if (ChatRoomJabberImpl.logger.isDebugEnabled()) {
                        ChatRoomJabberImpl.logger.debug("Received " + cd + " from " + participantName + "in " + ChatRoomJabberImpl.this.multiUserChat.getRoom());
                    }
                    ChatRoomJabberImpl.this.fireConferencePublishedEvent(member, cd, 1);
                    return;
                }
                ChatRoomJabberImpl.logger.warn("Received a ConferenceDescription from an unknown member (" + participantName + ") in " + ChatRoomJabberImpl.this.multiUserChat.getRoom());
            }
        }
    }

    private class SmackMessageListener implements PacketListener {
        private static final String LAST_SEEN_DELAYED_MESSAGE_PROP = "lastSeenDelayedMessage";
        private Date lastSeenDelayedMessage;

        private SmackMessageListener() {
            this.lastSeenDelayedMessage = null;
        }

        public void processPacket(Packet packet) {
            if (packet instanceof Message) {
                Date timeStamp;
                Message msg = (Message) packet;
                DelayInformation delay = (DelayInformation) msg.getExtension("x", "jabber:x:delay");
                if (delay != null) {
                    timeStamp = delay.getStamp();
                    if (this.lastSeenDelayedMessage == null) {
                        try {
                            this.lastSeenDelayedMessage = new Date(Long.parseLong(ConfigurationUtils.getChatRoomProperty(ChatRoomJabberImpl.this.provider, ChatRoomJabberImpl.this.getIdentifier(), LAST_SEEN_DELAYED_MESSAGE_PROP)));
                        } catch (Throwable th) {
                        }
                    }
                    if (this.lastSeenDelayedMessage == null || timeStamp.after(this.lastSeenDelayedMessage)) {
                        ConfigurationUtils.updateChatRoomProperty(ChatRoomJabberImpl.this.provider, ChatRoomJabberImpl.this.getIdentifier(), LAST_SEEN_DELAYED_MESSAGE_PROP, String.valueOf(timeStamp.getTime()));
                        this.lastSeenDelayedMessage = timeStamp;
                    } else {
                        return;
                    }
                }
                timeStamp = new Date();
                String msgBody = msg.getBody();
                if (msgBody != null) {
                    ChatRoomMember member;
                    int messageReceivedEventType = 1;
                    String msgFrom = msg.getFrom();
                    String fromUserName = StringUtils.parseResource(msgFrom);
                    if (msgFrom.equals(ChatRoomJabberImpl.this.getName())) {
                        messageReceivedEventType = 3;
                        member = new ChatRoomMemberJabberImpl(ChatRoomJabberImpl.this, ChatRoomJabberImpl.this.getName(), ChatRoomJabberImpl.this.getName());
                    } else {
                        member = ChatRoomJabberImpl.this.smackParticipantToScMember(msgFrom);
                    }
                    if (member == null) {
                        member = new ChatRoomMemberJabberImpl(ChatRoomJabberImpl.this, fromUserName, msgFrom);
                    }
                    if (ChatRoomJabberImpl.logger.isDebugEnabled() && ChatRoomJabberImpl.logger.isDebugEnabled()) {
                        ChatRoomJabberImpl.logger.debug("Received from " + fromUserName + " the message " + msg.toXML());
                    }
                    net.java.sip.communicator.service.protocol.Message newMessage = ChatRoomJabberImpl.this.createMessage(msgBody);
                    if (ChatRoomJabberImpl.this.getUserNickname().equals(fromUserName)) {
                        ChatRoomMessageDeliveredEvent chatRoomMessageDeliveredEvent = new ChatRoomMessageDeliveredEvent(ChatRoomJabberImpl.this, timeStamp, newMessage, 1);
                        if (delay != null) {
                            chatRoomMessageDeliveredEvent.setHistoryMessage(true);
                        }
                        ChatRoomJabberImpl.this.fireMessageEvent(chatRoomMessageDeliveredEvent);
                    } else if (msg.getType() == Type.error) {
                        if (ChatRoomJabberImpl.logger.isInfoEnabled()) {
                            ChatRoomJabberImpl.logger.info("Message error received from " + fromUserName);
                        }
                        XMPPError error = packet.getError();
                        int errorCode = error.getCode();
                        int errorResultCode = 1;
                        String errorReason = error.getMessage();
                        if (errorCode == 503) {
                            MessageEvent msgEvent = (MessageEvent) packet.getExtension("x", "jabber:x:event");
                            if (msgEvent != null && msgEvent.isOffline()) {
                                errorResultCode = 5;
                            }
                        }
                        ChatRoomJabberImpl.this.fireMessageEvent(new ChatRoomMessageDeliveryFailedEvent(ChatRoomJabberImpl.this, member, errorResultCode, errorReason, new Date(), newMessage));
                    } else {
                        ChatRoomMessageReceivedEvent msgReceivedEvt = new ChatRoomMessageReceivedEvent(ChatRoomJabberImpl.this, member, timeStamp, newMessage, messageReceivedEventType);
                        if (delay != null) {
                            msgReceivedEvt.setHistoryMessage(true);
                        }
                        if (messageReceivedEventType == 1 && newMessage.getContent().contains(ChatRoomJabberImpl.this.getUserNickname() + Separators.COLON)) {
                            msgReceivedEvt.setImportantMessage(true);
                        }
                        ChatRoomJabberImpl.this.fireMessageEvent(msgReceivedEvt);
                    }
                }
            }
        }
    }

    private class SmackSubjectUpdatedListener implements SubjectUpdatedListener {
        private SmackSubjectUpdatedListener() {
        }

        public void subjectUpdated(String subject, String from) {
            if (ChatRoomJabberImpl.logger.isInfoEnabled()) {
                ChatRoomJabberImpl.logger.info("Subject updated to " + subject);
            }
            if (!(subject == null || subject.equals(ChatRoomJabberImpl.this.oldSubject))) {
                ChatRoomJabberImpl.this.firePropertyChangeEvent(new ChatRoomPropertyChangeEvent(ChatRoomJabberImpl.this, "ChatRoomSubject", ChatRoomJabberImpl.this.oldSubject, subject));
            }
            ChatRoomJabberImpl.this.oldSubject = subject;
        }
    }

    private class UserListener implements UserStatusListener {
        private UserListener() {
        }

        public void kicked(String actor, String reason) {
            ChatRoomJabberImpl.this.opSetMuc.fireLocalUserPresenceEvent(ChatRoomJabberImpl.this, "LocalUserKicked", reason);
            ChatRoomJabberImpl.this.leave();
        }

        public void voiceGranted() {
            ChatRoomJabberImpl.this.setLocalUserRole(ChatRoomMemberRole.MEMBER);
        }

        public void voiceRevoked() {
            ChatRoomJabberImpl.this.setLocalUserRole(ChatRoomMemberRole.SILENT_MEMBER);
        }

        public void banned(String actor, String reason) {
            ChatRoomJabberImpl.this.opSetMuc.fireLocalUserPresenceEvent(ChatRoomJabberImpl.this, "LocalUserDropped", reason);
            ChatRoomJabberImpl.this.leave();
        }

        public void membershipGranted() {
            ChatRoomJabberImpl.this.setLocalUserRole(ChatRoomMemberRole.MEMBER);
        }

        public void membershipRevoked() {
            ChatRoomJabberImpl.this.setLocalUserRole(ChatRoomMemberRole.GUEST);
        }

        public void moderatorGranted() {
            ChatRoomJabberImpl.this.setLocalUserRole(ChatRoomMemberRole.MODERATOR);
        }

        public void moderatorRevoked() {
            ChatRoomJabberImpl.this.setLocalUserRole(ChatRoomMemberRole.MEMBER);
        }

        public void ownershipGranted() {
            ChatRoomJabberImpl.this.setLocalUserRole(ChatRoomMemberRole.OWNER);
        }

        public void ownershipRevoked() {
            ChatRoomJabberImpl.this.setLocalUserRole(ChatRoomMemberRole.MEMBER);
        }

        public void adminGranted() {
            ChatRoomJabberImpl.this.setLocalUserRole(ChatRoomMemberRole.ADMINISTRATOR);
        }

        public void adminRevoked() {
            ChatRoomJabberImpl.this.setLocalUserRole(ChatRoomMemberRole.MEMBER);
        }
    }

    public ChatRoomJabberImpl(MultiUserChat multiUserChat, ProtocolProviderServiceJabberImpl provider) {
        this.multiUserChat = multiUserChat;
        this.provider = provider;
        this.opSetMuc = (OperationSetMultiUserChatJabberImpl) provider.getOperationSet(OperationSetMultiUserChat.class);
        this.oldSubject = multiUserChat.getSubject();
        multiUserChat.addSubjectUpdatedListener(new SmackSubjectUpdatedListener());
        multiUserChat.addMessageListener(new SmackMessageListener());
        multiUserChat.addParticipantStatusListener(new MemberListener());
        multiUserChat.addUserStatusListener(new UserListener());
        multiUserChat.addPresenceInterceptor(new PresenceInterceptor());
        this.provider.getConnection().addPacketListener(this.invitationRejectionListeners, new PacketTypeFilter(Message.class));
    }

    /* access modifiers changed from: private */
    public MUCUser getMUCUserExtension(Packet packet) {
        if (packet != null) {
            return (MUCUser) packet.getExtension("x", "http://jabber.org/protocol/muc#user");
        }
        return null;
    }

    public void addPropertyChangeListener(ChatRoomPropertyChangeListener listener) {
        synchronized (this.propertyChangeListeners) {
            if (!this.propertyChangeListeners.contains(listener)) {
                this.propertyChangeListeners.add(listener);
            }
        }
    }

    public void removePropertyChangeListener(ChatRoomPropertyChangeListener listener) {
        synchronized (this.propertyChangeListeners) {
            this.propertyChangeListeners.remove(listener);
        }
    }

    public void addMemberPropertyChangeListener(ChatRoomMemberPropertyChangeListener listener) {
        synchronized (this.memberPropChangeListeners) {
            if (!this.memberPropChangeListeners.contains(listener)) {
                this.memberPropChangeListeners.add(listener);
            }
        }
    }

    public void removeMemberPropertyChangeListener(ChatRoomMemberPropertyChangeListener listener) {
        synchronized (this.memberPropChangeListeners) {
            this.memberPropChangeListeners.remove(listener);
        }
    }

    public void addMessageListener(ChatRoomMessageListener listener) {
        synchronized (this.messageListeners) {
            if (!this.messageListeners.contains(listener)) {
                this.messageListeners.add(listener);
            }
        }
    }

    public void removeMessageListener(ChatRoomMessageListener listener) {
        synchronized (this.messageListeners) {
            this.messageListeners.remove(listener);
        }
    }

    public void addMemberPresenceListener(ChatRoomMemberPresenceListener listener) {
        synchronized (this.memberListeners) {
            if (!this.memberListeners.contains(listener)) {
                this.memberListeners.add(listener);
            }
        }
    }

    public void removeMemberPresenceListener(ChatRoomMemberPresenceListener listener) {
        synchronized (this.memberListeners) {
            this.memberListeners.remove(listener);
        }
    }

    public synchronized void addConferenceCall(CallJabberImpl call) {
        if (!this.chatRoomConferenceCalls.contains(call)) {
            this.chatRoomConferenceCalls.add(call);
        }
    }

    public synchronized void removeConferenceCall(CallJabberImpl call) {
        if (this.chatRoomConferenceCalls.contains(call)) {
            this.chatRoomConferenceCalls.remove(call);
        }
    }

    public net.java.sip.communicator.service.protocol.Message createMessage(byte[] content, String contentType, String contentEncoding, String subject) {
        return new MessageJabberImpl(new String(content), contentType, contentEncoding, subject);
    }

    public net.java.sip.communicator.service.protocol.Message createMessage(String messageText) {
        return new MessageJabberImpl(messageText, "text/plain", "UTF-8", null);
    }

    public List<ChatRoomMember> getMembers() {
        LinkedList linkedList;
        synchronized (this.members) {
            linkedList = new LinkedList(this.members.values());
        }
        return linkedList;
    }

    public int getMembersCount() {
        return this.multiUserChat.getOccupantsCount();
    }

    public String getName() {
        return this.multiUserChat.getRoom();
    }

    public String getIdentifier() {
        return this.multiUserChat.getRoom();
    }

    public String getUserNickname() {
        return this.multiUserChat.getNickname();
    }

    public Contact getPrivateContactByNickname(String nickname) {
        OperationSetPersistentPresenceJabberImpl opSetPersPresence = (OperationSetPersistentPresenceJabberImpl) this.provider.getOperationSet(OperationSetPersistentPresence.class);
        String jid = getName() + Separators.SLASH + nickname;
        Contact sourceContact = opSetPersPresence.findContactByID(jid);
        if (sourceContact == null) {
            return opSetPersPresence.createVolatileContact(jid, true);
        }
        return sourceContact;
    }

    public String getSubject() {
        return this.multiUserChat.getSubject();
    }

    public void invite(String userAddress, String reason) {
        this.multiUserChat.invite(userAddress, reason);
    }

    public boolean isJoined() {
        return this.multiUserChat.isJoined();
    }

    public void join(byte[] password) throws OperationFailedException {
        joinAs(getOurDisplayName(), password);
    }

    public void join() throws OperationFailedException {
        joinAs(getOurDisplayName());
    }

    public void joinAs(String nickname, byte[] password) throws OperationFailedException {
        String errorMessage;
        assertConnected();
        this.nickname = getNickName(StringUtils.parseName(nickname));
        if (this.nickname.length() == 0) {
            this.nickname = nickname;
        }
        try {
            if (!this.multiUserChat.isJoined()) {
                this.provider.getConnection().addPacketListener(new PresenceListener(this), new AndFilter(new FromMatchesFilter(this.multiUserChat.getRoom()), new PacketTypeFilter(Presence.class)));
                if (password == null) {
                    this.multiUserChat.join(nickname);
                } else {
                    this.multiUserChat.join(nickname, new String(password));
                }
            } else if (!this.multiUserChat.getNickname().equals(nickname)) {
                this.multiUserChat.changeNickname(nickname);
            }
            ChatRoomMemberJabberImpl member = new ChatRoomMemberJabberImpl(this, nickname, this.provider.getAccountID().getAccountAddress());
            synchronized (this.members) {
                this.members.put(nickname, member);
            }
            this.opSetMuc.fireLocalUserPresenceEvent(this, "LocalUserJoined", null);
        } catch (XMPPException ex) {
            if (ex.getXMPPError() == null) {
                errorMessage = "Failed to join room " + getName() + " with nickname: " + nickname;
                logger.error(errorMessage, ex);
                throw new OperationFailedException(errorMessage, 1, ex);
            } else if (ex.getXMPPError().getCode() == Response.UNAUTHORIZED) {
                errorMessage = "Failed to join chat room " + getName() + " with nickname: " + nickname + ". The chat room requests a password.";
                logger.error(errorMessage, ex);
                throw new OperationFailedException(errorMessage, Response.UNAUTHORIZED, ex);
            } else if (ex.getXMPPError().getCode() == Response.PROXY_AUTHENTICATION_REQUIRED) {
                errorMessage = "Failed to join chat room " + getName() + " with nickname: " + nickname + ". The chat room requires registration.";
                logger.error(errorMessage, ex);
                throw new OperationFailedException(errorMessage, 13, ex);
            } else {
                errorMessage = "Failed to join room " + getName() + " with nickname: " + nickname;
                logger.error(errorMessage, ex);
                throw new OperationFailedException(errorMessage, 1, ex);
            }
        } catch (Throwable ex2) {
            errorMessage = "Failed to join room " + getName() + " with nickname: " + nickname;
            logger.error(errorMessage, ex2);
            OperationFailedException operationFailedException = new OperationFailedException(errorMessage, 1, ex2);
        }
    }

    public void joinAs(String nickname) throws OperationFailedException {
        joinAs(nickname, null);
    }

    private String getOurDisplayName() {
        OperationSetServerStoredAccountInfo accountInfoOpSet = (OperationSetServerStoredAccountInfo) this.provider.getOperationSet(OperationSetServerStoredAccountInfo.class);
        if (accountInfoOpSet == null) {
            return this.provider.getAccountID().getUserID();
        }
        DisplayNameDetail displayName = null;
        Iterator<GenericDetail> displayNameDetails = accountInfoOpSet.getDetails(DisplayNameDetail.class);
        if (displayNameDetails.hasNext()) {
            displayName = (DisplayNameDetail) displayNameDetails.next();
        }
        if (displayName == null) {
            return this.provider.getAccountID().getUserID();
        }
        String result = displayName.getString();
        if (result == null || result.length() == 0) {
            return this.provider.getAccountID().getUserID();
        }
        return result;
    }

    static ChatRoomMemberRole smackRoleToScRole(String smackRole, String affiliation) {
        if (affiliation != null) {
            if (affiliation.equals("admin")) {
                return ChatRoomMemberRole.ADMINISTRATOR;
            }
            if (affiliation.equals("owner")) {
                return ChatRoomMemberRole.OWNER;
            }
        }
        if (smackRole != null) {
            if (smackRole.equalsIgnoreCase("moderator")) {
                return ChatRoomMemberRole.MODERATOR;
            }
            if (smackRole.equalsIgnoreCase("participant")) {
                return ChatRoomMemberRole.MEMBER;
            }
        }
        return ChatRoomMemberRole.GUEST;
    }

    /* JADX WARNING: Missing block: B:22:?, code skipped:
            return r1;
     */
    public net.java.sip.communicator.impl.protocol.jabber.ChatRoomMemberJabberImpl smackParticipantToScMember(java.lang.String r6) {
        /*
        r5 = this;
        r2 = org.jivesoftware.smack.util.StringUtils.parseResource(r6);
        r4 = r5.members;
        monitor-enter(r4);
        r3 = r5.members;	 Catch:{ all -> 0x0040 }
        r3 = r3.values();	 Catch:{ all -> 0x0040 }
        r0 = r3.iterator();	 Catch:{ all -> 0x0040 }
    L_0x0011:
        r3 = r0.hasNext();	 Catch:{ all -> 0x0040 }
        if (r3 == 0) goto L_0x003d;
    L_0x0017:
        r1 = r0.next();	 Catch:{ all -> 0x0040 }
        r1 = (net.java.sip.communicator.impl.protocol.jabber.ChatRoomMemberJabberImpl) r1;	 Catch:{ all -> 0x0040 }
        r3 = r1.getName();	 Catch:{ all -> 0x0040 }
        r3 = r2.equals(r3);	 Catch:{ all -> 0x0040 }
        if (r3 != 0) goto L_0x003b;
    L_0x0027:
        r3 = r1.getContactAddress();	 Catch:{ all -> 0x0040 }
        r3 = r6.equals(r3);	 Catch:{ all -> 0x0040 }
        if (r3 != 0) goto L_0x003b;
    L_0x0031:
        r3 = r1.getContactAddress();	 Catch:{ all -> 0x0040 }
        r3 = r2.equals(r3);	 Catch:{ all -> 0x0040 }
        if (r3 == 0) goto L_0x0011;
    L_0x003b:
        monitor-exit(r4);	 Catch:{ all -> 0x0040 }
    L_0x003c:
        return r1;
    L_0x003d:
        monitor-exit(r4);	 Catch:{ all -> 0x0040 }
        r1 = 0;
        goto L_0x003c;
    L_0x0040:
        r3 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x0040 }
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.jabber.ChatRoomJabberImpl.smackParticipantToScMember(java.lang.String):net.java.sip.communicator.impl.protocol.jabber.ChatRoomMemberJabberImpl");
    }

    public void leave() {
        OperationSetBasicTelephonyJabberImpl basicTelephony = (OperationSetBasicTelephonyJabberImpl) this.provider.getOperationSet(OperationSetBasicTelephony.class);
        if (!(basicTelephony == null || this.publishedConference == null)) {
            ActiveCallsRepositoryJabberGTalkImpl<CallJabberImpl, CallPeerJabberImpl> activeRepository = basicTelephony.getActiveCallsRepository();
            String callid = this.publishedConference.getCallId();
            if (callid != null) {
                for (CallPeerJabberImpl peer : ((CallJabberImpl) activeRepository.findCallId(callid)).getCallPeerList()) {
                    peer.hangup(false, null, null);
                }
            }
        }
        synchronized (this.chatRoomConferenceCalls) {
            List<CallJabberImpl> tmpConferenceCalls = new ArrayList(this.chatRoomConferenceCalls);
            this.chatRoomConferenceCalls.clear();
        }
        for (CallJabberImpl call : tmpConferenceCalls) {
            for (CallPeerJabberImpl peer2 : call.getCallPeerList()) {
                peer2.hangup(false, null, null);
            }
        }
        clearCachedConferenceDescriptionList();
        XMPPConnection connection = this.provider.getConnection();
        if (connection != null) {
            try {
                this.multiUserChat.leave();
            } catch (Throwable e) {
                logger.warn("Error occured while leaving, maybe just disconnected before leaving", e);
            }
        }
        synchronized (this.members) {
            for (ChatRoomMember member : this.members.values()) {
                fireMemberPresenceEvent(member, "MemberLeft", "Local user has left the chat room.");
            }
            this.members.clear();
        }
        if (connection != null) {
            connection.removePacketListener(this.invitationRejectionListeners);
        }
        this.opSetMuc.fireLocalUserPresenceEvent(this, "LocalUserLeft", null);
    }

    public void sendMessage(net.java.sip.communicator.service.protocol.Message message) throws OperationFailedException {
        try {
            assertConnected();
            Message msg = new Message();
            msg.setBody(message.getContent());
            MessageEventManager.addNotificationsRequests(msg, true, false, false, true);
            this.multiUserChat.sendMessage(message.getContent());
        } catch (XMPPException ex) {
            logger.error("Failed to send message " + message, ex);
            throw new OperationFailedException("Failed to send message " + message, 1, ex);
        }
    }

    public void setSubject(String subject) throws OperationFailedException {
        try {
            this.multiUserChat.changeSubject(subject);
        } catch (XMPPException ex) {
            logger.error("Failed to change subject for chat room" + getName(), ex);
            throw new OperationFailedException("Failed to changed subject for chat room" + getName(), Response.FORBIDDEN, ex);
        }
    }

    public ProtocolProviderService getParentProvider() {
        return this.provider;
    }

    public ChatRoomMemberRole getUserRole() {
        if (this.role == null) {
            Occupant o = this.multiUserChat.getOccupant(this.multiUserChat.getRoom() + Separators.SLASH + this.multiUserChat.getNickname());
            if (o == null) {
                return ChatRoomMemberRole.GUEST;
            }
            this.role = smackRoleToScRole(o.getRole(), o.getAffiliation());
        }
        return this.role;
    }

    public void setLocalUserRole(ChatRoomMemberRole role) {
        setLocalUserRole(role, false);
    }

    public void setLocalUserRole(ChatRoomMemberRole role, boolean isInitial) {
        fireLocalUserRoleEvent(getUserRole(), role, isInitial);
        this.role = role;
    }

    public void addLocalUserRoleListener(ChatRoomLocalUserRoleListener listener) {
        synchronized (this.localUserRoleListeners) {
            if (!this.localUserRoleListeners.contains(listener)) {
                this.localUserRoleListeners.add(listener);
            }
        }
    }

    public void removelocalUserRoleListener(ChatRoomLocalUserRoleListener listener) {
        synchronized (this.localUserRoleListeners) {
            this.localUserRoleListeners.remove(listener);
        }
    }

    public void addMemberRoleListener(ChatRoomMemberRoleListener listener) {
        synchronized (this.memberRoleListeners) {
            if (!this.memberRoleListeners.contains(listener)) {
                this.memberRoleListeners.add(listener);
            }
        }
    }

    public void removeMemberRoleListener(ChatRoomMemberRoleListener listener) {
        synchronized (this.memberRoleListeners) {
            this.memberRoleListeners.remove(listener);
        }
    }

    public Iterator<ChatRoomMember> getBanList() throws OperationFailedException {
        return this.banList.values().iterator();
    }

    public void setUserNickname(String nickname) throws OperationFailedException {
        try {
            this.multiUserChat.changeNickname(nickname);
            int atIndex = nickname.lastIndexOf(Separators.AT);
            if (atIndex <= 0) {
                this.nickname = nickname;
            } else {
                this.nickname = nickname.substring(0, atIndex);
            }
        } catch (XMPPException e) {
            logger.error("Failed to change nickname for chat room: " + getName());
            throw new OperationFailedException("The " + nickname + "already exists in this chat room.", 10);
        }
    }

    public void banParticipant(ChatRoomMember chatRoomMember, String reason) throws OperationFailedException {
        try {
            this.multiUserChat.banUser(chatRoomMember.getContactAddress(), reason);
        } catch (XMPPException e) {
            logger.error("Failed to ban participant.", e);
            if (e.getXMPPError().getCode() == Response.METHOD_NOT_ALLOWED) {
                throw new OperationFailedException("Kicking an admin user or a chat room owner is a forbidden operation.", Response.FORBIDDEN);
            }
            throw new OperationFailedException("An error occured while trying to kick the participant.", 1);
        }
    }

    public void kickParticipant(ChatRoomMember member, String reason) throws OperationFailedException {
        try {
            this.multiUserChat.kickParticipant(member.getName(), reason);
        } catch (XMPPException e) {
            logger.error("Failed to kick participant.", e);
            if (e.getXMPPError().getCode() == Response.METHOD_NOT_ALLOWED) {
                throw new OperationFailedException("Kicking an admin user or a chat room owner is a forbidden operation.", Response.FORBIDDEN);
            } else if (e.getXMPPError().getCode() == Response.FORBIDDEN) {
                throw new OperationFailedException("The user that intended to kick another participant does not have enough privileges to do that.", 12);
            } else {
                throw new OperationFailedException("An error occured while trying to kick the participant.", 1);
            }
        }
    }

    /* access modifiers changed from: private */
    public void fireMemberPresenceEvent(ChatRoomMember member, String eventID, String eventReason) {
        Iterator<ChatRoomMemberPresenceListener> listeners;
        ChatRoomMemberPresenceChangeEvent evt = new ChatRoomMemberPresenceChangeEvent(this, member, eventID, eventReason);
        if (logger.isTraceEnabled()) {
            logger.trace("Will dispatch the following ChatRoom event: " + evt);
        }
        synchronized (this.memberListeners) {
            listeners = new ArrayList(this.memberListeners).iterator();
        }
        while (listeners.hasNext()) {
            ((ChatRoomMemberPresenceListener) listeners.next()).memberPresenceChanged(evt);
        }
    }

    /* access modifiers changed from: private */
    public void fireMemberPresenceEvent(ChatRoomMember member, ChatRoomMember actor, String eventID, String eventReason) {
        ChatRoomMemberPresenceChangeEvent evt = new ChatRoomMemberPresenceChangeEvent(this, member, actor, eventID, eventReason);
        if (logger.isTraceEnabled()) {
            logger.trace("Will dispatch the following ChatRoom event: " + evt);
        }
        synchronized (this.memberListeners) {
            Iterable<ChatRoomMemberPresenceListener> listeners = new ArrayList(this.memberListeners);
        }
        for (ChatRoomMemberPresenceListener listener : listeners) {
            listener.memberPresenceChanged(evt);
        }
    }

    /* access modifiers changed from: private */
    public void fireMemberRoleEvent(ChatRoomMember member, ChatRoomMemberRole previousRole, ChatRoomMemberRole newRole) {
        member.setRole(newRole);
        ChatRoomMemberRoleChangeEvent evt = new ChatRoomMemberRoleChangeEvent(this, member, previousRole, newRole);
        if (logger.isTraceEnabled()) {
            logger.trace("Will dispatch the following ChatRoom event: " + evt);
        }
        synchronized (this.memberRoleListeners) {
            Iterable<ChatRoomMemberRoleListener> listeners = new ArrayList(this.memberRoleListeners);
        }
        for (ChatRoomMemberRoleListener listener : listeners) {
            listener.memberRoleChanged(evt);
        }
    }

    /* access modifiers changed from: private */
    public void fireMessageEvent(EventObject evt) {
        synchronized (this.messageListeners) {
            Iterable<ChatRoomMessageListener> listeners = new ArrayList(this.messageListeners);
        }
        for (ChatRoomMessageListener listener : listeners) {
            try {
                if (evt instanceof ChatRoomMessageDeliveredEvent) {
                    listener.messageDelivered((ChatRoomMessageDeliveredEvent) evt);
                } else if (evt instanceof ChatRoomMessageReceivedEvent) {
                    listener.messageReceived((ChatRoomMessageReceivedEvent) evt);
                } else if (evt instanceof ChatRoomMessageDeliveryFailedEvent) {
                    listener.messageDeliveryFailed((ChatRoomMessageDeliveryFailedEvent) evt);
                }
            } catch (Throwable e) {
                logger.error("Error delivering multi chat message for " + listener, e);
            }
        }
    }

    public ConferenceDescription publishConference(ConferenceDescription cd, String name) {
        ConferenceDescriptionPacketExtension conferenceDescriptionPacketExtension = null;
        if (this.publishedConference != null) {
            cd = this.publishedConference;
            cd.setAvailable(false);
        } else {
            String displayName;
            if (name == null) {
                displayName = JabberActivator.getResources().getI18NString("service.gui.CHAT_CONFERENCE_ITEM_LABEL", new String[]{this.nickname});
            } else {
                displayName = name;
            }
            cd.setDisplayName(displayName);
        }
        ConferenceDescriptionPacketExtension ext = new ConferenceDescriptionPacketExtension(cd);
        if (this.lastPresenceSent != null) {
            ConferenceDescription conferenceDescription;
            setConferenceDescriptionPacketExtension(this.lastPresenceSent, ext);
            this.provider.getConnection().sendPacket(this.lastPresenceSent);
            if (cd == null || !cd.isAvailable()) {
                conferenceDescription = null;
            } else {
                conferenceDescription = cd;
            }
            this.publishedConference = conferenceDescription;
            if (this.publishedConference != null) {
                conferenceDescriptionPacketExtension = ext;
            }
            this.publishedConferenceExt = conferenceDescriptionPacketExtension;
            fireConferencePublishedEvent((ChatRoomMember) this.members.get(this.nickname), cd, 0);
            return cd;
        }
        logger.warn("Could not publish conference, lastPresenceSent is null.");
        this.publishedConference = null;
        this.publishedConferenceExt = null;
        return null;
    }

    /* access modifiers changed from: private */
    public void setConferenceDescriptionPacketExtension(Packet packet, ConferenceDescriptionPacketExtension ext) {
        while (true) {
            PacketExtension pe = packet.getExtension(ConferenceDescriptionPacketExtension.NAMESPACE);
            if (pe == null) {
                break;
            }
            packet.removeExtension(pe);
        }
        if (ext != null) {
            packet.addExtension(ext);
        }
    }

    private void fireLocalUserRoleEvent(ChatRoomMemberRole previousRole, ChatRoomMemberRole newRole, boolean isInitial) {
        ChatRoomLocalUserRoleChangeEvent evt = new ChatRoomLocalUserRoleChangeEvent(this, previousRole, newRole, isInitial);
        if (logger.isTraceEnabled()) {
            logger.trace("Will dispatch the following ChatRoom event: " + evt);
        }
        synchronized (this.localUserRoleListeners) {
            Iterable<ChatRoomLocalUserRoleListener> listeners = new ArrayList(this.localUserRoleListeners);
        }
        for (ChatRoomLocalUserRoleListener listener : listeners) {
            listener.localUserRoleChanged(evt);
        }
    }

    /* access modifiers changed from: private */
    public void firePropertyChangeEvent(PropertyChangeEvent evt) {
        synchronized (this.propertyChangeListeners) {
            Iterable<ChatRoomPropertyChangeListener> listeners = new ArrayList(this.propertyChangeListeners);
        }
        for (ChatRoomPropertyChangeListener listener : listeners) {
            if (evt instanceof ChatRoomPropertyChangeEvent) {
                listener.chatRoomPropertyChanged((ChatRoomPropertyChangeEvent) evt);
            } else if (evt instanceof ChatRoomPropertyChangeFailedEvent) {
                listener.chatRoomPropertyChangeFailed((ChatRoomPropertyChangeFailedEvent) evt);
            }
        }
    }

    public void fireMemberPropertyChangeEvent(ChatRoomMemberPropertyChangeEvent evt) {
        synchronized (this.memberPropChangeListeners) {
            Iterable<ChatRoomMemberPropertyChangeListener> listeners = new ArrayList(this.memberPropChangeListeners);
        }
        for (ChatRoomMemberPropertyChangeListener listener : listeners) {
            listener.chatRoomPropertyChanged(evt);
        }
    }

    private void assertConnected() throws IllegalStateException {
        if (this.provider == null) {
            throw new IllegalStateException("The provider must be non-null and signed on the service before being able to communicate.");
        } else if (!this.provider.isRegistered()) {
            throw new IllegalStateException("The provider must be signed on the service before being able to communicate.");
        }
    }

    public ChatRoomConfigurationForm getConfigurationForm() throws OperationFailedException {
        try {
            this.configForm = new ChatRoomConfigurationFormJabberImpl(this.multiUserChat, this.multiUserChat.getConfigurationForm());
            return this.configForm;
        } catch (XMPPException e) {
            if (e.getXMPPError().getCode() == Response.FORBIDDEN) {
                throw new OperationFailedException("Failed to obtain smack multi user chat config form.User doesn't have enough privileges to see the form.", 12, e);
            }
            throw new OperationFailedException("Failed to obtain smack multi user chat config form.", 1, e);
        }
    }

    public boolean isSystem() {
        return false;
    }

    public boolean isPersistent() {
        String roomName = this.multiUserChat.getRoom();
        try {
            DiscoverInfo info = ServiceDiscoveryManager.getInstanceFor(this.provider.getConnection()).discoverInfo(roomName);
            if (info != null) {
                return info.containsFeature("muc_persistent");
            }
            return false;
        } catch (Exception ex) {
            logger.warn("could not get persistent state for room :" + roomName + Separators.RETURN, ex);
            return false;
        }
    }

    public ChatRoomMemberJabberImpl findMemberForNickName(String jabberID) {
        ChatRoomMemberJabberImpl chatRoomMemberJabberImpl;
        synchronized (this.members) {
            chatRoomMemberJabberImpl = (ChatRoomMemberJabberImpl) this.members.get(jabberID);
        }
        return chatRoomMemberJabberImpl;
    }

    public void grantAdmin(String jid) {
        try {
            this.multiUserChat.grantAdmin(jid);
        } catch (XMPPException ex) {
            logger.error("An error occurs granting administrator privileges to a user.", ex);
        }
    }

    public void grantMembership(String jid) {
        try {
            this.multiUserChat.grantMembership(jid);
        } catch (XMPPException ex) {
            logger.error("An error occurs granting membership to a user", ex);
        }
    }

    public void grantModerator(String nickname) {
        try {
            this.multiUserChat.grantModerator(nickname);
        } catch (XMPPException ex) {
            logger.error("An error occurs granting moderator privileges to a user", ex);
        }
    }

    public void grantOwnership(String jid) {
        try {
            this.multiUserChat.grantOwnership(jid);
        } catch (XMPPException ex) {
            logger.error("An error occurs granting ownership privileges to a user", ex);
        }
    }

    public void grantVoice(String nickname) {
        try {
            this.multiUserChat.grantVoice(nickname);
        } catch (XMPPException ex) {
            logger.error("An error occurs granting voice to a visitor", ex);
        }
    }

    public void revokeAdmin(String jid) {
        try {
            this.multiUserChat.revokeAdmin(jid);
        } catch (XMPPException ex) {
            logger.error("n error occurs revoking administrator privileges to a user", ex);
        }
    }

    public void revokeMembership(String jid) {
        try {
            this.multiUserChat.revokeMembership(jid);
        } catch (XMPPException ex) {
            logger.error("An error occurs revoking membership to a user", ex);
        }
    }

    public void revokeModerator(String nickname) {
        try {
            this.multiUserChat.revokeModerator(nickname);
        } catch (XMPPException ex) {
            logger.error("n error occurs revoking moderator privileges from a user", ex);
        }
    }

    public void revokeOwnership(String jid) {
        try {
            this.multiUserChat.revokeOwnership(jid);
        } catch (XMPPException ex) {
            logger.error("An error occurs revoking ownership privileges from a user", ex);
        }
    }

    public void revokeVoice(String nickname) {
        try {
            this.multiUserChat.revokeVoice(nickname);
        } catch (XMPPException ex) {
            logger.info("An error occurs revoking voice from a participant", ex);
        }
    }

    static String getNickName(String participantAddress) {
        if (participantAddress == null) {
            return null;
        }
        int atIndex = participantAddress.lastIndexOf(Separators.AT);
        return atIndex > 0 ? participantAddress.substring(0, atIndex) : participantAddress;
    }

    /* access modifiers changed from: 0000 */
    public MultiUserChat getMultiUserChat() {
        return this.multiUserChat;
    }

    public void updatePrivateContactPresenceStatus(String nickname) {
        updatePrivateContactPresenceStatus((ContactJabberImpl) ((OperationSetPersistentPresenceJabberImpl) this.provider.getOperationSet(OperationSetPersistentPresence.class)).findContactByID(getName() + Separators.SLASH + nickname));
    }

    public void updatePrivateContactPresenceStatus(Contact contact) {
        OperationSetPersistentPresenceJabberImpl presenceOpSet = (OperationSetPersistentPresenceJabberImpl) this.provider.getOperationSet(OperationSetPersistentPresence.class);
        if (contact != null) {
            PresenceStatus oldContactStatus = contact.getPresenceStatus();
            PresenceStatus offlineStatus = this.provider.getJabberStatusEnum().getStatus(!this.members.containsKey(StringUtils.parseResource(contact.getAddress())) ? SipStatusEnum.OFFLINE : "Available");
            ((ContactJabberImpl) contact).updatePresenceStatus(offlineStatus);
            presenceOpSet.fireContactPresenceStatusChangeEvent(contact, contact.getParentContactGroup(), oldContactStatus, offlineStatus);
        }
    }
}
