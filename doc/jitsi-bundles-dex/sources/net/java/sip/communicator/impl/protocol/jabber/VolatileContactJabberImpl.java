package net.java.sip.communicator.impl.protocol.jabber;

import net.java.sip.communicator.service.protocol.OperationSetMultiUserChat;
import org.jivesoftware.smack.util.StringUtils;

public class VolatileContactJabberImpl extends ContactJabberImpl {
    private String contactId;
    protected String displayName;
    private boolean isPrivateMessagingContact;

    VolatileContactJabberImpl(String id, ServerStoredContactListJabberImpl ssclCallback) {
        this(id, ssclCallback, false);
    }

    VolatileContactJabberImpl(String id, ServerStoredContactListJabberImpl ssclCallback, boolean isPrivateMessagingContact) {
        super(null, ssclCallback, false, false);
        this.contactId = null;
        this.isPrivateMessagingContact = false;
        this.displayName = null;
        this.isPrivateMessagingContact = isPrivateMessagingContact;
        if (this.isPrivateMessagingContact) {
            this.displayName = StringUtils.parseResource(id) + " from " + StringUtils.parseBareAddress(id);
            this.contactId = id;
            setJid(id);
            return;
        }
        this.contactId = StringUtils.parseBareAddress(id);
        if (StringUtils.parseResource(id) != null) {
            setJid(id);
        }
    }

    public String getAddress() {
        return this.contactId;
    }

    public String getDisplayName() {
        return this.isPrivateMessagingContact ? this.displayName : this.contactId;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer("VolatileJabberContact[ id=");
        buff.append(getAddress()).append("]");
        return buff.toString();
    }

    public boolean isPersistent() {
        return false;
    }

    public boolean isPrivateMessagingContact() {
        return this.isPrivateMessagingContact;
    }

    public String getPersistableAddress() {
        if (!this.isPrivateMessagingContact) {
            return getAddress();
        }
        ChatRoomMemberJabberImpl chatRoomMember = ((OperationSetMultiUserChatJabberImpl) getProtocolProvider().getOperationSet(OperationSetMultiUserChat.class)).getChatRoom(StringUtils.parseBareAddress(this.contactId)).findMemberForNickName(StringUtils.parseResource(this.contactId));
        return chatRoomMember == null ? null : StringUtils.parseBareAddress(chatRoomMember.getJabberID());
    }
}
