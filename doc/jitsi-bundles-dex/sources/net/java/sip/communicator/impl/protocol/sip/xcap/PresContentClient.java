package net.java.sip.communicator.impl.protocol.sip.xcap;

import java.net.URI;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.prescontent.ContentType;

public interface PresContentClient {
    public static final String CONTENT_TYPE = "application/vnd.oma.pres-content+xml";
    public static final String DOCUMENT_FORMAT = "oma_status-icon/users/%1s/%2s";
    public static final String NAMESPACE = "urn:oma:xml:prs:pres-content";

    void deletePresContent(String str) throws XCapException;

    byte[] getImage(URI uri) throws XCapException;

    ContentType getPresContent(String str) throws XCapException;

    URI getPresContentImageUri(String str);

    void putPresContent(ContentType contentType, String str) throws XCapException;
}
