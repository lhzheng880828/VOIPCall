package org.jitsi.gov.nist.javax.sip.message;

import java.text.ParseException;
import org.jitsi.javax.sip.header.CSeqHeader;
import org.jitsi.javax.sip.header.CallIdHeader;
import org.jitsi.javax.sip.header.ContentLengthHeader;
import org.jitsi.javax.sip.header.ContentTypeHeader;
import org.jitsi.javax.sip.header.FromHeader;
import org.jitsi.javax.sip.header.ToHeader;
import org.jitsi.javax.sip.header.ViaHeader;
import org.jitsi.javax.sip.message.Message;

public interface MessageExt extends Message {
    Object getApplicationData();

    CSeqHeader getCSeqHeader();

    CallIdHeader getCallIdHeader();

    ContentLengthHeader getContentLengthHeader();

    ContentTypeHeader getContentTypeHeader();

    String getFirstLine();

    FromHeader getFromHeader();

    MultipartMimeContent getMultipartMimeContent() throws ParseException;

    ToHeader getToHeader();

    ViaHeader getTopmostViaHeader();

    void setApplicationData(Object obj);
}
