package org.jitsi.javax.sip.message;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ListIterator;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.header.ContentDispositionHeader;
import org.jitsi.javax.sip.header.ContentEncodingHeader;
import org.jitsi.javax.sip.header.ContentLanguageHeader;
import org.jitsi.javax.sip.header.ContentLengthHeader;
import org.jitsi.javax.sip.header.ContentTypeHeader;
import org.jitsi.javax.sip.header.ExpiresHeader;
import org.jitsi.javax.sip.header.Header;

public interface Message extends Cloneable, Serializable {
    void addFirst(Header header) throws SipException, NullPointerException;

    void addHeader(Header header);

    void addLast(Header header) throws SipException, NullPointerException;

    Object clone();

    boolean equals(Object obj);

    Object getContent();

    ContentDispositionHeader getContentDisposition();

    ContentEncodingHeader getContentEncoding();

    ContentLanguageHeader getContentLanguage();

    ContentLengthHeader getContentLength();

    ExpiresHeader getExpires();

    Header getHeader(String str);

    ListIterator getHeaderNames();

    ListIterator getHeaders(String str);

    byte[] getRawContent();

    String getSIPVersion();

    ListIterator getUnrecognizedHeaders();

    int hashCode();

    void removeContent();

    void removeFirst(String str) throws NullPointerException;

    void removeHeader(String str);

    void removeLast(String str) throws NullPointerException;

    void setContent(Object obj, ContentTypeHeader contentTypeHeader) throws ParseException;

    void setContentDisposition(ContentDispositionHeader contentDispositionHeader);

    void setContentEncoding(ContentEncodingHeader contentEncodingHeader);

    void setContentLanguage(ContentLanguageHeader contentLanguageHeader);

    void setContentLength(ContentLengthHeader contentLengthHeader);

    void setExpires(ExpiresHeader expiresHeader);

    void setHeader(Header header);

    void setSIPVersion(String str) throws ParseException;

    String toString();
}
