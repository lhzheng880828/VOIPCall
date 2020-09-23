package net.java.sip.communicator.impl.protocol.jabber;

import net.java.sip.communicator.service.protocol.AbstractMessage;

public class MessageJabberImpl extends AbstractMessage {
    public MessageJabberImpl(String content, String contentType, String contentEncoding, String subject) {
        super(content, contentType, contentEncoding, subject);
    }

    public MessageJabberImpl(String content, String contentType, String contentEncoding, String subject, String messageUID) {
        super(content, contentType, contentEncoding, subject, messageUID);
    }
}
