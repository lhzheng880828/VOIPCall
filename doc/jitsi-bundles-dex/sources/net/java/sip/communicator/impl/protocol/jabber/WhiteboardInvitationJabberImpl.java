package net.java.sip.communicator.impl.protocol.jabber;

import net.java.sip.communicator.service.protocol.WhiteboardInvitation;
import net.java.sip.communicator.service.protocol.WhiteboardSession;
import net.java.sip.communicator.service.protocol.whiteboardobjects.WhiteboardObject;

public class WhiteboardInvitationJabberImpl implements WhiteboardInvitation {
    private WhiteboardObject firstWhiteboardObject;
    private String inviter;
    private byte[] password;
    private String reason;
    private WhiteboardSession whiteboardSession;

    public WhiteboardInvitationJabberImpl(WhiteboardSession targetWhiteboard, WhiteboardObject firstWhiteboardObject, String inviter, String reason, byte[] password) {
        this.whiteboardSession = targetWhiteboard;
        this.firstWhiteboardObject = firstWhiteboardObject;
        this.inviter = inviter;
        this.reason = reason;
        this.password = password;
    }

    public WhiteboardSession getTargetWhiteboard() {
        return this.whiteboardSession;
    }

    public String getInviter() {
        return this.inviter;
    }

    public String getReason() {
        return this.reason;
    }

    public byte[] getWhiteboardPassword() {
        return this.password;
    }

    public WhiteboardObject getWhiteboardInitialObject() {
        return this.firstWhiteboardObject;
    }
}
