package net.java.sip.communicator.impl.protocol.jabber;

import net.java.sip.communicator.service.protocol.ChatRoom;
import net.java.sip.communicator.service.protocol.ChatRoomInvitation;

public class ChatRoomInvitationJabberImpl implements ChatRoomInvitation {
    private ChatRoom chatRoom;
    private String inviter;
    private byte[] password;
    private String reason;

    public ChatRoomInvitationJabberImpl(ChatRoom targetChatRoom, String inviter, String reason, byte[] password) {
        this.chatRoom = targetChatRoom;
        this.inviter = inviter;
        this.reason = reason;
        this.password = password;
    }

    public ChatRoom getTargetChatRoom() {
        return this.chatRoom;
    }

    public String getInviter() {
        return this.inviter;
    }

    public String getReason() {
        return this.reason;
    }

    public byte[] getChatRoomPassword() {
        return this.password;
    }
}
