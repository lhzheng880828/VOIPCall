package org.jitsi.gov.nist.javax.sip.message;

import org.jitsi.javax.sip.header.ContentTypeHeader;
import org.jitsi.javax.sip.header.ServerHeader;
import org.jitsi.javax.sip.header.UserAgentHeader;
import org.jitsi.javax.sip.message.MessageFactory;

public interface MessageFactoryExt extends MessageFactory {
    MultipartMimeContent createMultipartMimeContent(ContentTypeHeader contentTypeHeader, String[] strArr, String[] strArr2, String[] strArr3);

    void setDefaultContentEncodingCharset(String str) throws NullPointerException, IllegalArgumentException;

    void setDefaultServerHeader(ServerHeader serverHeader);

    void setDefaultUserAgentHeader(UserAgentHeader userAgentHeader);
}
