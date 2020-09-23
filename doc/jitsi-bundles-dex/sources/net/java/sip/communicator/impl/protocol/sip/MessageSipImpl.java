package net.java.sip.communicator.impl.protocol.sip;

import net.java.sip.communicator.service.protocol.AbstractMessage;

public class MessageSipImpl extends AbstractMessage {
    public MessageSipImpl(String content, String contentType, String contentEncoding, String subject) {
        super(content, contentType, contentEncoding, subject);
    }
}
