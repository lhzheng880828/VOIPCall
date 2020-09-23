package org.jitsi.gov.nist.javax.sip.message;

import java.util.Iterator;
import org.jitsi.javax.sip.header.ContentTypeHeader;

public interface MultipartMimeContent {
    boolean add(Content content);

    void addContent(Content content);

    int getContentCount();

    ContentTypeHeader getContentTypeHeader();

    Iterator<Content> getContents();

    String toString();
}
