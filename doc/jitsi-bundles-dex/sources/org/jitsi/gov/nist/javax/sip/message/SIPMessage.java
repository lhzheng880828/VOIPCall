package org.jitsi.gov.nist.javax.sip.message;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jitsi.gov.nist.core.GenericObject;
import org.jitsi.gov.nist.core.InternalErrorHandler;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.SIPConstants;
import org.jitsi.gov.nist.javax.sip.Utils;
import org.jitsi.gov.nist.javax.sip.header.AlertInfo;
import org.jitsi.gov.nist.javax.sip.header.Authorization;
import org.jitsi.gov.nist.javax.sip.header.CSeq;
import org.jitsi.gov.nist.javax.sip.header.CallID;
import org.jitsi.gov.nist.javax.sip.header.Contact;
import org.jitsi.gov.nist.javax.sip.header.ContactList;
import org.jitsi.gov.nist.javax.sip.header.ContentLength;
import org.jitsi.gov.nist.javax.sip.header.ContentType;
import org.jitsi.gov.nist.javax.sip.header.ErrorInfo;
import org.jitsi.gov.nist.javax.sip.header.ErrorInfoList;
import org.jitsi.gov.nist.javax.sip.header.From;
import org.jitsi.gov.nist.javax.sip.header.InReplyTo;
import org.jitsi.gov.nist.javax.sip.header.MaxForwards;
import org.jitsi.gov.nist.javax.sip.header.Priority;
import org.jitsi.gov.nist.javax.sip.header.ProxyAuthenticate;
import org.jitsi.gov.nist.javax.sip.header.ProxyAuthorization;
import org.jitsi.gov.nist.javax.sip.header.ProxyRequire;
import org.jitsi.gov.nist.javax.sip.header.ProxyRequireList;
import org.jitsi.gov.nist.javax.sip.header.RSeq;
import org.jitsi.gov.nist.javax.sip.header.RecordRouteList;
import org.jitsi.gov.nist.javax.sip.header.RetryAfter;
import org.jitsi.gov.nist.javax.sip.header.Route;
import org.jitsi.gov.nist.javax.sip.header.RouteList;
import org.jitsi.gov.nist.javax.sip.header.SIPETag;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.SIPHeaderList;
import org.jitsi.gov.nist.javax.sip.header.SIPHeaderNamesCache;
import org.jitsi.gov.nist.javax.sip.header.SIPIfMatch;
import org.jitsi.gov.nist.javax.sip.header.Server;
import org.jitsi.gov.nist.javax.sip.header.Subject;
import org.jitsi.gov.nist.javax.sip.header.To;
import org.jitsi.gov.nist.javax.sip.header.Unsupported;
import org.jitsi.gov.nist.javax.sip.header.UserAgent;
import org.jitsi.gov.nist.javax.sip.header.Via;
import org.jitsi.gov.nist.javax.sip.header.ViaList;
import org.jitsi.gov.nist.javax.sip.header.WWWAuthenticate;
import org.jitsi.gov.nist.javax.sip.header.Warning;
import org.jitsi.gov.nist.javax.sip.parser.ParserFactory;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.header.CSeqHeader;
import org.jitsi.javax.sip.header.CallIdHeader;
import org.jitsi.javax.sip.header.ContentDispositionHeader;
import org.jitsi.javax.sip.header.ContentEncodingHeader;
import org.jitsi.javax.sip.header.ContentLanguageHeader;
import org.jitsi.javax.sip.header.ContentLengthHeader;
import org.jitsi.javax.sip.header.ContentTypeHeader;
import org.jitsi.javax.sip.header.ExpiresHeader;
import org.jitsi.javax.sip.header.FromHeader;
import org.jitsi.javax.sip.header.Header;
import org.jitsi.javax.sip.header.MaxForwardsHeader;
import org.jitsi.javax.sip.header.RecordRouteHeader;
import org.jitsi.javax.sip.header.ToHeader;
import org.jitsi.javax.sip.header.ViaHeader;
import org.jitsi.javax.sip.message.Message;
import org.jitsi.javax.sip.message.Request;

public abstract class SIPMessage extends MessageObject implements Message, MessageExt {
    private static final String AUTHORIZATION_LOWERCASE = SIPHeaderNamesCache.toLowerCase("Authorization");
    private static final String CONTACT_LOWERCASE = SIPHeaderNamesCache.toLowerCase("Contact");
    private static final String CONTENT_DISPOSITION_LOWERCASE = SIPHeaderNamesCache.toLowerCase("Content-Disposition");
    private static final String CONTENT_ENCODING_LOWERCASE = SIPHeaderNamesCache.toLowerCase("Content-Encoding");
    private static final String CONTENT_LANGUAGE_LOWERCASE = SIPHeaderNamesCache.toLowerCase("Content-Language");
    private static final String CONTENT_TYPE_LOWERCASE = SIPHeaderNamesCache.toLowerCase("Content-Type");
    private static final String ERROR_LOWERCASE = SIPHeaderNamesCache.toLowerCase("Error-Info");
    private static final String EXPIRES_LOWERCASE = SIPHeaderNamesCache.toLowerCase("Expires");
    private static final String RECORDROUTE_LOWERCASE = SIPHeaderNamesCache.toLowerCase("Record-Route");
    private static final String ROUTE_LOWERCASE = SIPHeaderNamesCache.toLowerCase("Route");
    private static final String VIA_LOWERCASE = SIPHeaderNamesCache.toLowerCase("Via");
    protected Object applicationData;
    protected CSeq cSeqHeader;
    protected CallID callIdHeader;
    private String contentEncodingCharset = MessageFactoryImpl.getDefaultContentEncodingCharset();
    protected ContentLength contentLengthHeader;
    protected String forkId;
    protected From fromHeader;
    protected Map<String, SIPHeader> headerTable = new ConcurrentHashMap();
    protected ConcurrentLinkedQueue<SIPHeader> headers = new ConcurrentLinkedQueue();
    private InetAddress localAddress;
    private int localPort;
    protected MaxForwards maxForwardsHeader;
    protected String messageContent;
    protected byte[] messageContentBytes;
    protected Object messageContentObject;
    protected boolean nullRequest;
    private InetAddress remoteAddress;
    private int remotePort;
    protected int size;
    protected To toHeader;
    protected LinkedList<String> unrecognizedHeaders = new LinkedList();

    public abstract StringBuilder encodeMessage(StringBuilder stringBuilder);

    public abstract String getFirstLine();

    public abstract String getSIPVersion();

    public abstract void setSIPVersion(String str) throws ParseException;

    public abstract String toString();

    public static boolean isRequestHeader(SIPHeader sipHeader) {
        return (sipHeader instanceof AlertInfo) || (sipHeader instanceof InReplyTo) || (sipHeader instanceof Authorization) || (sipHeader instanceof MaxForwards) || (sipHeader instanceof UserAgent) || (sipHeader instanceof Priority) || (sipHeader instanceof ProxyAuthorization) || (sipHeader instanceof ProxyRequire) || (sipHeader instanceof ProxyRequireList) || (sipHeader instanceof Route) || (sipHeader instanceof RouteList) || (sipHeader instanceof Subject) || (sipHeader instanceof SIPIfMatch);
    }

    public static boolean isResponseHeader(SIPHeader sipHeader) {
        return (sipHeader instanceof ErrorInfo) || (sipHeader instanceof ProxyAuthenticate) || (sipHeader instanceof Server) || (sipHeader instanceof Unsupported) || (sipHeader instanceof RetryAfter) || (sipHeader instanceof Warning) || (sipHeader instanceof WWWAuthenticate) || (sipHeader instanceof SIPETag) || (sipHeader instanceof RSeq);
    }

    public LinkedList<String> getMessageAsEncodedStrings() {
        LinkedList<String> retval = new LinkedList();
        Iterator<SIPHeader> li = this.headers.iterator();
        while (li.hasNext()) {
            SIPHeader sipHeader = (SIPHeader) li.next();
            if (sipHeader instanceof SIPHeaderList) {
                retval.addAll(((SIPHeaderList) sipHeader).getHeadersAsEncodedStrings());
            } else {
                retval.add(sipHeader.encode());
            }
        }
        return retval;
    }

    /* access modifiers changed from: protected */
    public StringBuilder encodeSIPHeaders(StringBuilder encoding) {
        Iterator<SIPHeader> it = this.headers.iterator();
        while (it.hasNext()) {
            SIPHeader siphdr = (SIPHeader) it.next();
            if (!(siphdr instanceof ContentLength)) {
                siphdr.encode(encoding);
            }
        }
        return this.contentLengthHeader.encode(encoding).append(Separators.NEWLINE);
    }

    public final String getDialogId(boolean isServer) {
        return getDialogId(isServer, ((To) getTo()).getTag());
    }

    public final String getDialogId(boolean isServer, String toTag) {
        From from = (From) getFrom();
        StringBuffer retval = new StringBuffer(((CallID) getCallId()).getCallId());
        if (isServer) {
            if (toTag != null) {
                retval.append(Separators.COLON);
                retval.append(toTag);
            }
            if (from.getTag() != null) {
                retval.append(Separators.COLON);
                retval.append(from.getTag());
            }
        } else {
            if (from.getTag() != null) {
                retval.append(Separators.COLON);
                retval.append(from.getTag());
            }
            if (toTag != null) {
                retval.append(Separators.COLON);
                retval.append(toTag);
            }
        }
        return retval.toString().toLowerCase();
    }

    public boolean match(Object other) {
        if (other == null) {
            return true;
        }
        if (!other.getClass().equals(getClass())) {
            return false;
        }
        Iterator<SIPHeader> li = ((SIPMessage) other).getHeaders();
        while (li.hasNext()) {
            SIPHeader hisHeaders = (SIPHeader) li.next();
            List<SIPHeader> myHeaders = getHeaderList(hisHeaders.getHeaderName());
            if (myHeaders == null || myHeaders.size() == 0) {
                return false;
            }
            SIPHeader hisHeader;
            boolean found;
            if (hisHeaders instanceof SIPHeaderList) {
                ListIterator<?> outerIterator = ((SIPHeaderList) hisHeaders).listIterator();
                while (outerIterator.hasNext()) {
                    hisHeader = (SIPHeader) outerIterator.next();
                    if (!(hisHeader instanceof ContentLength)) {
                        ListIterator<?> innerIterator = myHeaders.listIterator();
                        found = false;
                        while (innerIterator.hasNext()) {
                            if (((SIPHeader) innerIterator.next()).match(hisHeader)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            return false;
                        }
                    }
                }
                continue;
            } else {
                hisHeader = hisHeaders;
                ListIterator<SIPHeader> innerIterator2 = myHeaders.listIterator();
                found = false;
                while (innerIterator2.hasNext()) {
                    if (((SIPHeader) innerIterator2.next()).match(hisHeader)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
        }
        return true;
    }

    public void merge(Object template) {
        if (template.getClass().equals(getClass())) {
            Object[] templateHeaders = ((SIPMessage) template).headers.toArray();
            for (SIPHeader hdr : templateHeaders) {
                List<SIPHeader> myHdrs = getHeaderList(hdr.getHeaderName());
                if (myHdrs == null) {
                    attachHeader(hdr);
                } else {
                    ListIterator<SIPHeader> it = myHdrs.listIterator();
                    while (it.hasNext()) {
                        ((SIPHeader) it.next()).merge(hdr);
                    }
                }
            }
            return;
        }
        throw new IllegalArgumentException("Bad class " + template.getClass());
    }

    public String encode() {
        StringBuilder encoding = new StringBuilder();
        Iterator<SIPHeader> it = this.headers.iterator();
        while (it.hasNext()) {
            SIPHeader siphdr = (SIPHeader) it.next();
            if (!(siphdr instanceof ContentLength)) {
                siphdr.encode(encoding);
            }
        }
        if (this.unrecognizedHeaders != null) {
            Iterator i$ = this.unrecognizedHeaders.iterator();
            while (i$.hasNext()) {
                encoding.append((String) i$.next()).append(Separators.NEWLINE);
            }
        }
        this.contentLengthHeader.encode(encoding).append(Separators.NEWLINE);
        if (this.messageContentObject != null) {
            encoding.append(getContent().toString());
        } else if (!(this.messageContent == null && this.messageContentBytes == null)) {
            String content = null;
            try {
                if (this.messageContent != null) {
                    content = this.messageContent;
                } else {
                    content = new String(this.messageContentBytes, getCharset());
                }
            } catch (UnsupportedEncodingException ex) {
                InternalErrorHandler.handleException(ex);
            }
            encoding.append(content);
        }
        return encoding.toString();
    }

    public byte[] encodeAsBytes(String transport) {
        if ((this instanceof SIPRequest) && ((SIPRequest) this).isNullRequest()) {
            return "\r\n\r\n".getBytes();
        }
        try {
            ((ViaHeader) getHeader("Via")).setTransport(transport);
        } catch (ParseException e) {
            InternalErrorHandler.handleException(e);
        }
        StringBuilder encoding = new StringBuilder();
        synchronized (this.headers) {
            Iterator<SIPHeader> it = this.headers.iterator();
            while (it.hasNext()) {
                SIPHeader siphdr = (SIPHeader) it.next();
                if (!(siphdr instanceof ContentLength)) {
                    siphdr.encode(encoding);
                }
            }
        }
        this.contentLengthHeader.encode(encoding);
        encoding.append(Separators.NEWLINE);
        byte[] retval = null;
        byte[] content = getRawContent();
        if (content != null) {
            byte[] msgarray = null;
            try {
                msgarray = encoding.toString().getBytes(getCharset());
            } catch (UnsupportedEncodingException ex) {
                InternalErrorHandler.handleException(ex);
            }
            retval = new byte[(msgarray.length + content.length)];
            System.arraycopy(msgarray, 0, retval, 0, msgarray.length);
            System.arraycopy(content, 0, retval, msgarray.length, content.length);
            return retval;
        }
        try {
            return encoding.toString().getBytes(getCharset());
        } catch (UnsupportedEncodingException ex2) {
            InternalErrorHandler.handleException(ex2);
            return retval;
        }
    }

    public Object clone() {
        SIPMessage retval = (SIPMessage) super.clone();
        retval.headerTable = new ConcurrentHashMap();
        retval.fromHeader = null;
        retval.toHeader = null;
        retval.cSeqHeader = null;
        retval.callIdHeader = null;
        retval.contentLengthHeader = null;
        retval.maxForwardsHeader = null;
        if (this.headers != null) {
            retval.headers = new ConcurrentLinkedQueue();
            Iterator<SIPHeader> iter = this.headers.iterator();
            while (iter.hasNext()) {
                retval.attachHeader((SIPHeader) ((SIPHeader) iter.next()).clone());
            }
        }
        if (this.messageContentBytes != null) {
            retval.messageContentBytes = (byte[]) this.messageContentBytes.clone();
        }
        if (this.messageContentObject != null) {
            retval.messageContentObject = GenericObject.makeClone(this.messageContentObject);
        }
        retval.unrecognizedHeaders = this.unrecognizedHeaders;
        retval.remoteAddress = this.remoteAddress;
        retval.remotePort = this.remotePort;
        return retval;
    }

    public String debugDump() {
        this.stringRepresentation = "";
        sprint("SIPMessage:");
        sprint("{");
        try {
            Field[] fields = getClass().getDeclaredFields();
            for (Field f : fields) {
                Class<?> fieldType = f.getType();
                String fieldName = f.getName();
                if (!(f.get(this) == null || !SIPHeader.class.isAssignableFrom(fieldType) || fieldName.compareTo("headers") == 0)) {
                    sprint(fieldName + Separators.EQUALS);
                    sprint(((SIPHeader) f.get(this)).debugDump());
                }
            }
        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
        }
        sprint("List of headers : ");
        sprint(this.headers.toString());
        sprint("messageContent = ");
        sprint("{");
        sprint(this.messageContent);
        sprint("}");
        if (getContent() != null) {
            sprint(getContent().toString());
        }
        sprint("}");
        return this.stringRepresentation;
    }

    public SIPMessage() {
        try {
            attachHeader(new ContentLength(0), false);
        } catch (Exception e) {
        }
    }

    private void attachHeader(SIPHeader h) {
        if (h == null) {
            throw new IllegalArgumentException("null header!");
        }
        try {
            if (!(h instanceof SIPHeaderList) || !((SIPHeaderList) h).isEmpty()) {
                attachHeader(h, false, false);
            }
        } catch (SIPDuplicateHeaderException e) {
        }
    }

    public void setHeader(Header sipHeader) {
        SIPHeader header = (SIPHeader) sipHeader;
        if (header == null) {
            throw new IllegalArgumentException("null header!");
        }
        try {
            if (!(header instanceof SIPHeaderList) || !((SIPHeaderList) header).isEmpty()) {
                removeHeader(header.getHeaderName());
                attachHeader(header, true, false);
            }
        } catch (SIPDuplicateHeaderException ex) {
            InternalErrorHandler.handleException(ex);
        }
    }

    public void setHeaders(List<SIPHeader> headers) {
        ListIterator<SIPHeader> listIterator = headers.listIterator();
        while (listIterator.hasNext()) {
            try {
                attachHeader((SIPHeader) listIterator.next(), false);
            } catch (SIPDuplicateHeaderException e) {
            }
        }
    }

    public void attachHeader(SIPHeader h, boolean replaceflag) throws SIPDuplicateHeaderException {
        attachHeader(h, replaceflag, false);
    }

    public void attachHeader(SIPHeader header, boolean replaceFlag, boolean top) throws SIPDuplicateHeaderException {
        if (header == null) {
            throw new NullPointerException("null header");
        }
        SIPHeader h;
        if (!ListMap.hasList(header) || SIPHeaderList.class.isAssignableFrom(header.getClass())) {
            h = header;
        } else {
            SIPHeader hdrList = ListMap.getList(header);
            hdrList.add(header);
            h = hdrList;
        }
        String headerNameLowerCase = SIPHeaderNamesCache.toLowerCase(h.getName());
        if (replaceFlag) {
            this.headerTable.remove(headerNameLowerCase);
        } else if (this.headerTable.containsKey(headerNameLowerCase) && !(h instanceof SIPHeaderList)) {
            if (h instanceof ContentLength) {
                try {
                    this.contentLengthHeader.setContentLength(((ContentLength) h).getContentLength());
                    return;
                } catch (InvalidArgumentException e) {
                    return;
                }
            }
            return;
        }
        SIPHeader originalHeader = (SIPHeader) getHeader(header.getName());
        if (originalHeader != null) {
            Iterator<SIPHeader> li = this.headers.iterator();
            while (li.hasNext()) {
                if (((SIPHeader) li.next()).equals(originalHeader)) {
                    li.remove();
                }
            }
        }
        if (!this.headerTable.containsKey(headerNameLowerCase)) {
            this.headerTable.put(headerNameLowerCase, h);
            this.headers.add(h);
        } else if (h instanceof SIPHeaderList) {
            SIPHeaderList<?> hdrlist = (SIPHeaderList) this.headerTable.get(headerNameLowerCase);
            if (hdrlist != null) {
                hdrlist.concatenate((SIPHeaderList) h, top);
            } else {
                this.headerTable.put(headerNameLowerCase, h);
            }
        } else {
            this.headerTable.put(headerNameLowerCase, h);
        }
        if (h instanceof From) {
            this.fromHeader = (From) h;
        } else if (h instanceof ContentLength) {
            this.contentLengthHeader = (ContentLength) h;
        } else if (h instanceof To) {
            this.toHeader = (To) h;
        } else if (h instanceof CSeq) {
            this.cSeqHeader = (CSeq) h;
        } else if (h instanceof CallID) {
            this.callIdHeader = (CallID) h;
        } else if (h instanceof MaxForwards) {
            this.maxForwardsHeader = (MaxForwards) h;
        }
    }

    public void removeHeader(String headerName, boolean top) {
        String headerNameLowerCase = SIPHeaderNamesCache.toLowerCase(headerName);
        SIPHeader toRemove = (SIPHeader) this.headerTable.get(headerNameLowerCase);
        if (toRemove != null) {
            Iterator<SIPHeader> li;
            if (toRemove instanceof SIPHeaderList) {
                SIPHeaderList<?> hdrList = (SIPHeaderList) toRemove;
                if (top) {
                    hdrList.removeFirst();
                } else {
                    hdrList.removeLast();
                }
                if (hdrList.isEmpty()) {
                    li = this.headers.iterator();
                    while (li.hasNext()) {
                        if (((SIPHeader) li.next()).getName().equalsIgnoreCase(headerNameLowerCase)) {
                            li.remove();
                        }
                    }
                    this.headerTable.remove(headerNameLowerCase);
                    return;
                }
                return;
            }
            this.headerTable.remove(headerNameLowerCase);
            if (toRemove instanceof From) {
                this.fromHeader = null;
            } else if (toRemove instanceof To) {
                this.toHeader = null;
            } else if (toRemove instanceof CSeq) {
                this.cSeqHeader = null;
            } else if (toRemove instanceof CallID) {
                this.callIdHeader = null;
            } else if (toRemove instanceof MaxForwards) {
                this.maxForwardsHeader = null;
            } else if (toRemove instanceof ContentLength) {
                this.contentLengthHeader = null;
            }
            li = this.headers.iterator();
            while (li.hasNext()) {
                if (((SIPHeader) li.next()).getName().equalsIgnoreCase(headerName)) {
                    li.remove();
                }
            }
        }
    }

    public void removeHeader(String headerName) {
        if (headerName == null) {
            throw new NullPointerException("null arg");
        }
        String headerNameLowerCase = SIPHeaderNamesCache.toLowerCase(headerName);
        SIPHeader removed = (SIPHeader) this.headerTable.remove(headerNameLowerCase);
        if (removed != null) {
            if (removed instanceof From) {
                this.fromHeader = null;
            } else if (removed instanceof To) {
                this.toHeader = null;
            } else if (removed instanceof CSeq) {
                this.cSeqHeader = null;
            } else if (removed instanceof CallID) {
                this.callIdHeader = null;
            } else if (removed instanceof MaxForwards) {
                this.maxForwardsHeader = null;
            } else if (removed instanceof ContentLength) {
                this.contentLengthHeader = null;
            }
            Iterator<SIPHeader> li = this.headers.iterator();
            while (li.hasNext()) {
                if (((SIPHeader) li.next()).getName().equalsIgnoreCase(headerNameLowerCase)) {
                    li.remove();
                }
            }
        }
    }

    public String getTransactionId() {
        Via topVia = getTopmostVia();
        if (topVia == null || topVia.getBranch() == null || !topVia.getBranch().toUpperCase().startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_UPPER_CASE)) {
            StringBuilder retval = new StringBuilder();
            From from = (From) getFrom();
            To to = (To) getTo();
            if (from.hasTag()) {
                retval.append(from.getTag()).append("-");
            }
            retval.append(this.callIdHeader.getCallId()).append("-");
            retval.append(this.cSeqHeader.getSequenceNumber()).append("-").append(this.cSeqHeader.getMethod());
            if (topVia != null) {
                retval.append("-").append(topVia.getSentBy().encode());
                if (!topVia.getSentBy().hasPort()) {
                    retval.append("-").append(5060);
                }
            }
            if (getCSeq().getMethod().equals(Request.CANCEL)) {
                retval.append(Request.CANCEL);
            }
            return retval.toString().toLowerCase().replace(Separators.COLON, "-").replace(Separators.AT, "-") + Utils.getSignature();
        } else if (getCSeq().getMethod().equals(Request.CANCEL)) {
            return (topVia.getBranch() + Separators.COLON + getCSeq().getMethod()).toLowerCase();
        } else {
            return topVia.getBranch().toLowerCase();
        }
    }

    public int hashCode() {
        if (this.callIdHeader != null) {
            return this.callIdHeader.getCallId().hashCode();
        }
        throw new RuntimeException("Invalid message! Cannot compute hashcode! call-id header is missing !");
    }

    public boolean hasContent() {
        return (this.messageContent == null && this.messageContentBytes == null) ? false : true;
    }

    public Iterator<SIPHeader> getHeaders() {
        return this.headers.iterator();
    }

    public Header getHeader(String headerName) {
        return getHeaderLowerCase(SIPHeaderNamesCache.toLowerCase(headerName));
    }

    /* access modifiers changed from: protected */
    public Header getHeaderLowerCase(String lowerCaseHeaderName) {
        if (lowerCaseHeaderName == null) {
            throw new NullPointerException("bad name");
        }
        SIPHeader sIPHeader = (SIPHeader) this.headerTable.get(lowerCaseHeaderName);
        if (sIPHeader instanceof SIPHeaderList) {
            return ((SIPHeaderList) sIPHeader).getFirst();
        }
        return sIPHeader;
    }

    public ContentType getContentTypeHeader() {
        return (ContentType) getHeaderLowerCase(CONTENT_TYPE_LOWERCASE);
    }

    public ContentLengthHeader getContentLengthHeader() {
        return getContentLength();
    }

    public FromHeader getFrom() {
        return this.fromHeader;
    }

    public ErrorInfoList getErrorInfoHeaders() {
        return (ErrorInfoList) getSIPHeaderListLowerCase(ERROR_LOWERCASE);
    }

    public ContactList getContactHeaders() {
        return (ContactList) getSIPHeaderListLowerCase(CONTACT_LOWERCASE);
    }

    public Contact getContactHeader() {
        ContactList clist = getContactHeaders();
        if (clist != null) {
            return (Contact) clist.getFirst();
        }
        return null;
    }

    public ViaList getViaHeaders() {
        return (ViaList) getSIPHeaderListLowerCase(VIA_LOWERCASE);
    }

    public void setVia(List viaList) {
        SIPHeaderList vList = new ViaList();
        ListIterator it = viaList.listIterator();
        while (it.hasNext()) {
            vList.add((SIPHeader) (Via) it.next());
        }
        setHeader(vList);
    }

    public void setHeader(SIPHeaderList<Via> sipHeaderList) {
        setHeader((Header) sipHeaderList);
    }

    public Via getTopmostVia() {
        if (getViaHeaders() == null) {
            return null;
        }
        return (Via) getViaHeaders().getFirst();
    }

    public CSeqHeader getCSeq() {
        return this.cSeqHeader;
    }

    public Authorization getAuthorization() {
        return (Authorization) getHeaderLowerCase(AUTHORIZATION_LOWERCASE);
    }

    public MaxForwardsHeader getMaxForwards() {
        return this.maxForwardsHeader;
    }

    public void setMaxForwards(MaxForwardsHeader maxForwards) {
        setHeader((Header) maxForwards);
    }

    public RouteList getRouteHeaders() {
        return (RouteList) getSIPHeaderListLowerCase(ROUTE_LOWERCASE);
    }

    public CallIdHeader getCallId() {
        return this.callIdHeader;
    }

    public void setCallId(CallIdHeader callId) {
        setHeader((Header) callId);
    }

    public void setCallId(String callId) throws ParseException {
        if (this.callIdHeader == null) {
            setHeader(new CallID());
        }
        this.callIdHeader.setCallId(callId);
    }

    public RecordRouteList getRecordRouteHeaders() {
        return (RecordRouteList) getSIPHeaderListLowerCase(RECORDROUTE_LOWERCASE);
    }

    public ToHeader getTo() {
        return this.toHeader;
    }

    public void setTo(ToHeader to) {
        setHeader((Header) to);
    }

    public void setFrom(FromHeader from) {
        setHeader((Header) from);
    }

    public ContentLengthHeader getContentLength() {
        return this.contentLengthHeader;
    }

    public String getMessageContent() throws UnsupportedEncodingException {
        if (this.messageContent == null && this.messageContentBytes == null) {
            return null;
        }
        if (this.messageContent == null) {
            this.messageContent = new String(this.messageContentBytes, getCharset());
        }
        return this.messageContent;
    }

    public byte[] getRawContent() {
        try {
            if (this.messageContentBytes == null) {
                if (this.messageContentObject != null) {
                    this.messageContentBytes = this.messageContentObject.toString().getBytes(getCharset());
                } else if (this.messageContent != null) {
                    this.messageContentBytes = this.messageContent.getBytes(getCharset());
                }
            }
            return this.messageContentBytes;
        } catch (UnsupportedEncodingException ex) {
            InternalErrorHandler.handleException(ex);
            return null;
        }
    }

    public void setMessageContent(String type, String subType, String messageContent) {
        if (messageContent == null) {
            throw new IllegalArgumentException("messgeContent is null");
        }
        setHeader(new ContentType(type, subType));
        this.messageContent = messageContent;
        this.messageContentBytes = null;
        this.messageContentObject = null;
        computeContentLength(messageContent);
    }

    public void setContent(Object content, ContentTypeHeader contentTypeHeader) throws ParseException {
        if (content == null) {
            throw new NullPointerException("null content");
        }
        setHeader((Header) contentTypeHeader);
        this.messageContent = null;
        this.messageContentBytes = null;
        this.messageContentObject = null;
        if (content instanceof String) {
            this.messageContent = (String) content;
        } else if (content instanceof byte[]) {
            this.messageContentBytes = (byte[]) content;
        } else {
            this.messageContentObject = content;
        }
        computeContentLength(content);
    }

    public Object getContent() {
        if (this.messageContentObject != null) {
            return this.messageContentObject;
        }
        if (this.messageContent != null) {
            return this.messageContent;
        }
        if (this.messageContentBytes != null) {
            return this.messageContentBytes;
        }
        return null;
    }

    public void setMessageContent(String type, String subType, byte[] messageContent) {
        setHeader(new ContentType(type, subType));
        setMessageContent(messageContent);
        computeContentLength(messageContent);
    }

    public void setMessageContent(byte[] content, boolean strict, boolean computeContentLength, int givenLength) throws ParseException {
        computeContentLength(content);
        if (computeContentLength || ((strict || this.contentLengthHeader.getContentLength() == givenLength) && this.contentLengthHeader.getContentLength() >= givenLength)) {
            this.messageContent = null;
            this.messageContentBytes = content;
            this.messageContentObject = null;
            return;
        }
        throw new ParseException("Invalid content length " + this.contentLengthHeader.getContentLength() + " / " + givenLength, 0);
    }

    public void setMessageContent(byte[] content) {
        computeContentLength(content);
        this.messageContentBytes = content;
        this.messageContent = null;
        this.messageContentObject = null;
    }

    public void setMessageContent(byte[] content, boolean computeContentLength, int givenLength) throws ParseException {
        computeContentLength(content);
        if (computeContentLength || this.contentLengthHeader.getContentLength() >= givenLength) {
            this.messageContentBytes = content;
            this.messageContent = null;
            this.messageContentObject = null;
            return;
        }
        throw new ParseException("Invalid content length " + this.contentLengthHeader.getContentLength() + " / " + givenLength, 0);
    }

    private void computeContentLength(Object content) {
        int length = 0;
        if (content != null) {
            if (content instanceof String) {
                try {
                    length = ((String) content).getBytes(getCharset()).length;
                } catch (UnsupportedEncodingException ex) {
                    InternalErrorHandler.handleException(ex);
                }
            } else {
                length = content instanceof byte[] ? ((byte[]) content).length : content.toString().length();
            }
        }
        try {
            this.contentLengthHeader.setContentLength(length);
        } catch (InvalidArgumentException e) {
        }
    }

    public void removeContent() {
        this.messageContent = null;
        this.messageContentBytes = null;
        this.messageContentObject = null;
        try {
            this.contentLengthHeader.setContentLength(0);
        } catch (InvalidArgumentException e) {
        }
    }

    public ListIterator<SIPHeader> getHeaders(String headerName) {
        if (headerName == null) {
            throw new NullPointerException("null headerName");
        }
        SIPHeader sipHeader = (SIPHeader) this.headerTable.get(SIPHeaderNamesCache.toLowerCase(headerName));
        if (sipHeader == null) {
            return new LinkedList().listIterator();
        }
        if (sipHeader instanceof SIPHeaderList) {
            return ((SIPHeaderList) sipHeader).listIterator();
        }
        return new HeaderIterator(this, sipHeader);
    }

    public String getHeaderAsFormattedString(String name) {
        String lowerCaseName = SIPHeaderNamesCache.toLowerCase(name);
        if (this.headerTable.containsKey(lowerCaseName)) {
            return ((SIPHeader) this.headerTable.get(lowerCaseName)).toString();
        }
        return getHeader(name).toString();
    }

    public SIPHeader getSIPHeaderListLowerCase(String lowerCaseHeaderName) {
        return (SIPHeader) this.headerTable.get(lowerCaseHeaderName);
    }

    private List<SIPHeader> getHeaderList(String headerName) {
        SIPHeader sipHeader = (SIPHeader) this.headerTable.get(SIPHeaderNamesCache.toLowerCase(headerName));
        if (sipHeader == null) {
            return null;
        }
        if (sipHeader instanceof SIPHeaderList) {
            return ((SIPHeaderList) sipHeader).getHeaderList();
        }
        List<SIPHeader> ll = new LinkedList();
        ll.add(sipHeader);
        return ll;
    }

    public boolean hasHeader(String headerName) {
        return this.headerTable.containsKey(SIPHeaderNamesCache.toLowerCase(headerName));
    }

    public boolean hasFromTag() {
        return (this.fromHeader == null || this.fromHeader.getTag() == null) ? false : true;
    }

    public boolean hasToTag() {
        return (this.toHeader == null || this.toHeader.getTag() == null) ? false : true;
    }

    public String getFromTag() {
        return this.fromHeader == null ? null : this.fromHeader.getTag();
    }

    public void setFromTag(String tag) {
        try {
            this.fromHeader.setTag(tag);
        } catch (ParseException e) {
        }
    }

    public void setToTag(String tag) {
        try {
            this.toHeader.setTag(tag);
        } catch (ParseException e) {
        }
    }

    public String getToTag() {
        return this.toHeader == null ? null : this.toHeader.getTag();
    }

    public void addHeader(Header sipHeader) {
        SIPHeader sh = (SIPHeader) sipHeader;
        try {
            if ((sipHeader instanceof ViaHeader) || (sipHeader instanceof RecordRouteHeader)) {
                attachHeader(sh, false, true);
            } else {
                attachHeader(sh, false, false);
            }
        } catch (SIPDuplicateHeaderException e) {
            try {
                if (sipHeader instanceof ContentLength) {
                    this.contentLengthHeader.setContentLength(((ContentLength) sipHeader).getContentLength());
                }
            } catch (InvalidArgumentException e2) {
            }
        }
    }

    public void addUnparsed(String unparsed) {
        getUnrecognizedHeadersList().add(unparsed);
    }

    public void addHeader(String sipHeader) {
        String hdrString = sipHeader.trim() + Separators.RETURN;
        try {
            attachHeader(ParserFactory.createParser(sipHeader).parse(), false);
        } catch (ParseException e) {
            getUnrecognizedHeadersList().add(hdrString);
        }
    }

    public ListIterator<String> getUnrecognizedHeaders() {
        return getUnrecognizedHeadersList().listIterator();
    }

    public ListIterator<String> getHeaderNames() {
        Iterator<SIPHeader> li = this.headers.iterator();
        LinkedList<String> retval = new LinkedList();
        while (li.hasNext()) {
            retval.add(((SIPHeader) li.next()).getName());
        }
        return retval.listIterator();
    }

    public boolean equals(Object other) {
        if (!other.getClass().equals(getClass())) {
            return false;
        }
        SIPMessage otherMessage = (SIPMessage) other;
        if (this.headerTable.size() != otherMessage.headerTable.size()) {
            return false;
        }
        for (SIPHeader mine : this.headerTable.values()) {
            SIPHeader his = (SIPHeader) otherMessage.headerTable.get(SIPHeaderNamesCache.toLowerCase(mine.getName()));
            if (his == null) {
                return false;
            }
            if (!his.equals(mine)) {
                return false;
            }
        }
        return true;
    }

    public ContentDispositionHeader getContentDisposition() {
        return (ContentDispositionHeader) getHeaderLowerCase(CONTENT_DISPOSITION_LOWERCASE);
    }

    public ContentEncodingHeader getContentEncoding() {
        return (ContentEncodingHeader) getHeaderLowerCase(CONTENT_ENCODING_LOWERCASE);
    }

    public ContentLanguageHeader getContentLanguage() {
        return (ContentLanguageHeader) getHeaderLowerCase(CONTENT_LANGUAGE_LOWERCASE);
    }

    public ExpiresHeader getExpires() {
        return (ExpiresHeader) getHeaderLowerCase(EXPIRES_LOWERCASE);
    }

    public void setExpires(ExpiresHeader expiresHeader) {
        setHeader((Header) expiresHeader);
    }

    public void setContentDisposition(ContentDispositionHeader contentDispositionHeader) {
        setHeader((Header) contentDispositionHeader);
    }

    public void setContentEncoding(ContentEncodingHeader contentEncodingHeader) {
        setHeader((Header) contentEncodingHeader);
    }

    public void setContentLanguage(ContentLanguageHeader contentLanguageHeader) {
        setHeader((Header) contentLanguageHeader);
    }

    public void setContentLength(ContentLengthHeader contentLength) {
        try {
            this.contentLengthHeader.setContentLength(contentLength.getContentLength());
        } catch (InvalidArgumentException e) {
        }
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return this.size;
    }

    public void addLast(Header header) throws SipException, NullPointerException {
        if (header == null) {
            throw new NullPointerException("null arg!");
        }
        try {
            attachHeader((SIPHeader) header, false, false);
        } catch (SIPDuplicateHeaderException e) {
            throw new SipException("Cannot add header - header already exists");
        }
    }

    public void addFirst(Header header) throws SipException, NullPointerException {
        if (header == null) {
            throw new NullPointerException("null arg!");
        }
        try {
            attachHeader((SIPHeader) header, false, true);
        } catch (SIPDuplicateHeaderException e) {
            throw new SipException("Cannot add header - header already exists");
        }
    }

    public void removeFirst(String headerName) throws NullPointerException {
        if (headerName == null) {
            throw new NullPointerException("Null argument Provided!");
        }
        removeHeader(headerName, true);
    }

    public void removeLast(String headerName) {
        if (headerName == null) {
            throw new NullPointerException("Null argument Provided!");
        }
        removeHeader(headerName, false);
    }

    public void setCSeq(CSeqHeader cseqHeader) {
        setHeader((Header) cseqHeader);
    }

    public void setApplicationData(Object applicationData) {
        this.applicationData = applicationData;
    }

    public Object getApplicationData() {
        return this.applicationData;
    }

    public MultipartMimeContent getMultipartMimeContent() throws ParseException {
        if (this.contentLengthHeader.getContentLength() == 0) {
            return null;
        }
        MultipartMimeContent retval = new MultipartMimeContentImpl(getContentTypeHeader());
        try {
            retval.createContentList(new String(getRawContent(), getCharset()));
            return retval;
        } catch (UnsupportedEncodingException e) {
            InternalErrorHandler.handleException(e);
            return null;
        }
    }

    public CallIdHeader getCallIdHeader() {
        return this.callIdHeader;
    }

    public FromHeader getFromHeader() {
        return this.fromHeader;
    }

    public ToHeader getToHeader() {
        return this.toHeader;
    }

    public ViaHeader getTopmostViaHeader() {
        return getTopmostVia();
    }

    public CSeqHeader getCSeqHeader() {
        return this.cSeqHeader;
    }

    /* access modifiers changed from: protected|final */
    public final String getCharset() {
        ContentType ct = getContentTypeHeader();
        if (ct == null) {
            return this.contentEncodingCharset;
        }
        String c = ct.getCharset();
        if (c != null) {
            return c;
        }
        return this.contentEncodingCharset;
    }

    public boolean isNullRequest() {
        return this.nullRequest;
    }

    public void setNullRequest() {
        this.nullRequest = true;
    }

    public String getForkId() {
        if (this.forkId != null) {
            return this.forkId;
        }
        String callId = getCallId().getCallId();
        String fromTag = getFromTag();
        if (fromTag == null) {
            throw new IllegalStateException("From tag is not yet set. Cannot compute forkId");
        }
        this.forkId = (callId + Separators.COLON + fromTag).toLowerCase();
        return this.forkId;
    }

    public void cleanUp() {
    }

    /* access modifiers changed from: protected */
    public void setUnrecognizedHeadersList(LinkedList<String> unrecognizedHeaders) {
        this.unrecognizedHeaders = unrecognizedHeaders;
    }

    /* access modifiers changed from: protected */
    public LinkedList<String> getUnrecognizedHeadersList() {
        if (this.unrecognizedHeaders == null) {
            this.unrecognizedHeaders = new LinkedList();
        }
        return this.unrecognizedHeaders;
    }

    public void setRemoteAddress(InetAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public InetAddress getRemoteAddress() {
        return this.remoteAddress;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public int getRemotePort() {
        return this.remotePort;
    }

    public void setLocalAddress(InetAddress localAddress) {
        this.localAddress = localAddress;
    }

    public InetAddress getLocalAddress() {
        return this.localAddress;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public int getLocalPort() {
        return this.localPort;
    }
}
