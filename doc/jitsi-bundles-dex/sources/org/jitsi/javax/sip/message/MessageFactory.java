package org.jitsi.javax.sip.message;

import java.text.ParseException;
import java.util.List;
import org.jitsi.javax.sip.address.URI;
import org.jitsi.javax.sip.header.CSeqHeader;
import org.jitsi.javax.sip.header.CallIdHeader;
import org.jitsi.javax.sip.header.ContentTypeHeader;
import org.jitsi.javax.sip.header.FromHeader;
import org.jitsi.javax.sip.header.MaxForwardsHeader;
import org.jitsi.javax.sip.header.ToHeader;

public interface MessageFactory {
    Request createRequest(String str) throws ParseException;

    Request createRequest(URI uri, String str, CallIdHeader callIdHeader, CSeqHeader cSeqHeader, FromHeader fromHeader, ToHeader toHeader, List list, MaxForwardsHeader maxForwardsHeader) throws ParseException;

    Request createRequest(URI uri, String str, CallIdHeader callIdHeader, CSeqHeader cSeqHeader, FromHeader fromHeader, ToHeader toHeader, List list, MaxForwardsHeader maxForwardsHeader, ContentTypeHeader contentTypeHeader, Object obj) throws ParseException;

    Request createRequest(URI uri, String str, CallIdHeader callIdHeader, CSeqHeader cSeqHeader, FromHeader fromHeader, ToHeader toHeader, List list, MaxForwardsHeader maxForwardsHeader, ContentTypeHeader contentTypeHeader, byte[] bArr) throws ParseException;

    Response createResponse(int i, CallIdHeader callIdHeader, CSeqHeader cSeqHeader, FromHeader fromHeader, ToHeader toHeader, List list, MaxForwardsHeader maxForwardsHeader) throws ParseException;

    Response createResponse(int i, CallIdHeader callIdHeader, CSeqHeader cSeqHeader, FromHeader fromHeader, ToHeader toHeader, List list, MaxForwardsHeader maxForwardsHeader, ContentTypeHeader contentTypeHeader, Object obj) throws ParseException;

    Response createResponse(int i, CallIdHeader callIdHeader, CSeqHeader cSeqHeader, FromHeader fromHeader, ToHeader toHeader, List list, MaxForwardsHeader maxForwardsHeader, ContentTypeHeader contentTypeHeader, byte[] bArr) throws ParseException;

    Response createResponse(int i, Request request) throws ParseException;

    Response createResponse(int i, Request request, ContentTypeHeader contentTypeHeader, Object obj) throws ParseException;

    Response createResponse(int i, Request request, ContentTypeHeader contentTypeHeader, byte[] bArr) throws ParseException;

    Response createResponse(String str) throws ParseException;
}
