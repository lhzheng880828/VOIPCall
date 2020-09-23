package org.jitsi.gov.nist.javax.sip.message;

import org.jitsi.javax.sip.header.ContentDispositionHeader;
import org.jitsi.javax.sip.header.ContentTypeHeader;

public interface Content {
    Object getContent();

    ContentDispositionHeader getContentDispositionHeader();

    ContentTypeHeader getContentTypeHeader();

    void setContent(Object obj);

    String toString();
}
