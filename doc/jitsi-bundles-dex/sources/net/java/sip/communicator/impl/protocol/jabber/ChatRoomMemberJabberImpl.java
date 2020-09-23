package net.java.sip.communicator.impl.protocol.jabber;

import net.java.sip.communicator.service.protocol.ChatRoom;
import net.java.sip.communicator.service.protocol.ChatRoomMember;
import net.java.sip.communicator.service.protocol.ChatRoomMemberRole;
import net.java.sip.communicator.service.protocol.ConferenceDescription;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationSetPersistentPresence;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.Occupant;

public class ChatRoomMemberJabberImpl implements ChatRoomMember {
    private byte[] avatar;
    private ConferenceDescription conferenceDescription = null;
    private Contact contact;
    private final ChatRoomJabberImpl containingRoom;
    private final String jabberID;
    private String nickName;
    private ChatRoomMemberRole role;

    public ChatRoomMemberJabberImpl(ChatRoomJabberImpl containingChatRoom, String nickName, String jabberID) {
        this.jabberID = jabberID;
        this.nickName = nickName;
        this.containingRoom = containingChatRoom;
        this.contact = ((OperationSetPersistentPresenceJabberImpl) containingChatRoom.getParentProvider().getOperationSet(OperationSetPersistentPresence.class)).findContactByID(StringUtils.parseBareAddress(jabberID));
        if (this.contact != null) {
            this.avatar = this.contact.getImage();
        }
        getRole();
    }

    public ChatRoom getChatRoom() {
        return this.containingRoom;
    }

    public String getJabberID() {
        return this.jabberID;
    }

    public String getContactAddress() {
        return StringUtils.parseBareAddress(this.jabberID);
    }

    public String getName() {
        return this.nickName;
    }

    /* access modifiers changed from: protected */
    public void setName(String newNick) {
        if (newNick == null || newNick.length() <= 0) {
            throw new IllegalArgumentException("a room member nickname could not be null");
        }
        this.nickName = newNick;
    }

    public ProtocolProviderService getProtocolProvider() {
        return this.containingRoom.getParentProvider();
    }

    public ChatRoomMemberRole getRole() {
        if (this.role == null) {
            Occupant o = this.containingRoom.getMultiUserChat().getOccupant(this.containingRoom.getIdentifier() + Separators.SLASH + this.nickName);
            if (o == null) {
                return ChatRoomMemberRole.GUEST;
            }
            this.role = ChatRoomJabberImpl.smackRoleToScRole(o.getRole(), o.getAffiliation());
        }
        return this.role;
    }

    /* access modifiers changed from: 0000 */
    public ChatRoomMemberRole getCurrentRole() {
        return this.role;
    }

    public void setRole(ChatRoomMemberRole role) {
        this.role = role;
    }

    public byte[] getAvatar() {
        return this.avatar;
    }

    public void setAvatar(byte[] avatar) {
        this.avatar = avatar;
    }

    public Contact getContact() {
        return this.contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }
}
