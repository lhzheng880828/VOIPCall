package org.jitsi.gov.nist.javax.sip.message;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jitsi.gov.nist.core.InternalErrorHandler;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.SIPConstants;
import org.jitsi.gov.nist.javax.sip.address.GenericURI;
import org.jitsi.gov.nist.javax.sip.address.SipUri;
import org.jitsi.gov.nist.javax.sip.header.CSeq;
import org.jitsi.gov.nist.javax.sip.header.RecordRouteList;
import org.jitsi.gov.nist.javax.sip.header.RequestLine;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.SIPHeaderList;
import org.jitsi.gov.nist.javax.sip.header.To;
import org.jitsi.gov.nist.javax.sip.header.Via;
import org.jitsi.gov.nist.javax.sip.header.ViaList;
import org.jitsi.gov.nist.javax.sip.stack.SIPTransactionStack;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.address.URI;
import org.jitsi.javax.sip.header.Header;
import org.jitsi.javax.sip.header.ServerHeader;
import org.jitsi.javax.sip.message.Request;

public class SIPRequest extends SIPMessage implements Request, RequestExt {
    private static final String DEFAULT_TRANSPORT = "udp";
    private static final String DEFAULT_USER = "ip";
    protected static final Set<String> headersToIncludeInResponse = new HashSet(0);
    private static final Map<String, String> nameTable = new ConcurrentHashMap(15);
    private static final long serialVersionUID = 3360720013577322927L;
    private static final Set<String> targetRefreshMethods = new HashSet();
    private transient Object inviteTransaction;
    private transient Object messageChannel;
    protected RequestLine requestLine;
    private transient Object transactionPointer;

    static {
        targetRefreshMethods.add("INVITE");
        targetRefreshMethods.add(Request.UPDATE);
        targetRefreshMethods.add("SUBSCRIBE");
        targetRefreshMethods.add("NOTIFY");
        targetRefreshMethods.add(Request.REFER);
        putName("INVITE");
        putName("BYE");
        putName(Request.CANCEL);
        putName("ACK");
        putName(Request.PRACK);
        putName(Request.INFO);
        putName("MESSAGE");
        putName("NOTIFY");
        putName("OPTIONS");
        putName(Request.PRACK);
        putName("PUBLISH");
        putName(Request.REFER);
        putName("REGISTER");
        putName("SUBSCRIBE");
        putName(Request.UPDATE);
        headersToIncludeInResponse.add("From".toLowerCase());
        headersToIncludeInResponse.add("To".toLowerCase());
        headersToIncludeInResponse.add("Via".toLowerCase());
        headersToIncludeInResponse.add("Record-Route".toLowerCase());
        headersToIncludeInResponse.add("Call-ID".toLowerCase());
        headersToIncludeInResponse.add("CSeq".toLowerCase());
        headersToIncludeInResponse.add("Timestamp".toLowerCase());
    }

    private static void putName(String name) {
        nameTable.put(name, name);
    }

    public static boolean isTargetRefresh(String ucaseMethod) {
        return targetRefreshMethods.contains(ucaseMethod);
    }

    public static boolean isDialogCreating(String ucaseMethod) {
        return SIPTransactionStack.isDialogCreated(ucaseMethod);
    }

    public static String getCannonicalName(String method) {
        if (nameTable.containsKey(method)) {
            return (String) nameTable.get(method);
        }
        return method;
    }

    public RequestLine getRequestLine() {
        return this.requestLine;
    }

    public void setRequestLine(RequestLine requestLine) {
        this.requestLine = requestLine;
    }

    public String debugDump() {
        String superstring = super.debugDump();
        this.stringRepresentation = "";
        sprint(SIPRequest.class.getName());
        sprint("{");
        if (this.requestLine != null) {
            sprint(this.requestLine.debugDump());
        }
        sprint(superstring);
        sprint("}");
        return this.stringRepresentation;
    }

    public void checkHeaders() throws ParseException {
        String prefix = "Missing a required header : ";
        if (getCSeq() == null) {
            throw new ParseException(prefix + "CSeq", 0);
        } else if (getTo() == null) {
            throw new ParseException(prefix + "To", 0);
        } else if (this.callIdHeader == null || this.callIdHeader.getCallId() == null || this.callIdHeader.getCallId().equals("")) {
            throw new ParseException(prefix + "Call-ID", 0);
        } else if (getFrom() == null) {
            throw new ParseException(prefix + "From", 0);
        } else if (getViaHeaders() == null) {
            throw new ParseException(prefix + "Via", 0);
        } else if (getMaxForwards() == null) {
            throw new ParseException(prefix + "Max-Forwards", 0);
        } else if (getTopmostVia() == null) {
            throw new ParseException("No via header in request! ", 0);
        } else {
            if (getMethod().equals("NOTIFY")) {
                if (getHeader("Subscription-State") == null) {
                    throw new ParseException(prefix + "Subscription-State", 0);
                } else if (getHeader("Event") == null) {
                    throw new ParseException(prefix + "Event", 0);
                }
            } else if (getMethod().equals("PUBLISH") && getHeader("Event") == null) {
                throw new ParseException(prefix + "Event", 0);
            }
            String method = this.requestLine.getMethod();
            if (SIPTransactionStack.isDialogCreated(method)) {
                if (getContactHeader() == null && getToTag() == null) {
                    throw new ParseException(prefix + "Contact", 0);
                } else if (this.requestLine.getUri() instanceof SipUri) {
                    if ("sips".equalsIgnoreCase(((SipUri) this.requestLine.getUri()).getScheme())) {
                        SipUri sipUri = (SipUri) getContactHeader().getAddress().getURI();
                        if (!sipUri.getScheme().equals("sips")) {
                            throw new ParseException("Scheme for contact should be sips:" + sipUri, 0);
                        }
                    }
                }
            }
            if (this.requestLine != null && method != null && getCSeq().getMethod() != null && method.compareTo(getCSeq().getMethod()) != 0) {
                throw new ParseException("CSEQ method mismatch with  Request-Line ", 0);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setDefaults() {
        if (this.requestLine != null) {
            String method = this.requestLine.getMethod();
            if (method != null) {
                GenericURI u = this.requestLine.getUri();
                if (u == null) {
                    return;
                }
                if ((method.compareTo("REGISTER") == 0 || method.compareTo("INVITE") == 0) && (u instanceof SipUri)) {
                    SipUri sipUri = (SipUri) u;
                    sipUri.setUserParam("ip");
                    try {
                        sipUri.setTransportParam("udp");
                    } catch (ParseException e) {
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setRequestLineDefaults() {
        if (this.requestLine.getMethod() == null) {
            CSeq cseq = (CSeq) getCSeq();
            if (cseq != null) {
                this.requestLine.setMethod(getCannonicalName(cseq.getMethod()));
            }
        }
    }

    public URI getRequestURI() {
        if (this.requestLine == null) {
            return null;
        }
        return this.requestLine.getUri();
    }

    public void setRequestURI(URI uri) {
        if (uri == null) {
            throw new NullPointerException("Null request URI");
        }
        if (this.requestLine == null) {
            this.requestLine = new RequestLine();
        }
        this.requestLine.setUri((GenericURI) uri);
        this.nullRequest = false;
    }

    public void setMethod(String method) {
        if (method == null) {
            throw new IllegalArgumentException("null method");
        }
        if (this.requestLine == null) {
            this.requestLine = new RequestLine();
        }
        String meth = getCannonicalName(method);
        this.requestLine.setMethod(meth);
        if (this.cSeqHeader != null) {
            try {
                this.cSeqHeader.setMethod(meth);
            } catch (ParseException e) {
            }
        }
    }

    public String getMethod() {
        if (this.requestLine == null) {
            return null;
        }
        return this.requestLine.getMethod();
    }

    public String encode() {
        if (this.requestLine != null) {
            setRequestLineDefaults();
            return this.requestLine.encode() + super.encode();
        } else if (isNullRequest()) {
            return "\r\n\r\n";
        } else {
            return super.encode();
        }
    }

    public StringBuilder encodeMessage(StringBuilder retval) {
        if (this.requestLine != null) {
            setRequestLineDefaults();
            this.requestLine.encode(retval);
            encodeSIPHeaders(retval);
            return retval;
        } else if (!isNullRequest()) {
            return encodeSIPHeaders(retval);
        } else {
            retval.append("\r\n\r\n");
            return retval;
        }
    }

    public String toString() {
        return encode();
    }

    public Object clone() {
        SIPRequest retval = (SIPRequest) super.clone();
        retval.transactionPointer = null;
        if (this.requestLine != null) {
            retval.requestLine = (RequestLine) this.requestLine.clone();
        }
        return retval;
    }

    public boolean equals(Object other) {
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        if (this.requestLine.equals(((SIPRequest) other).requestLine) && super.equals(other)) {
            return true;
        }
        return false;
    }

    public LinkedList getMessageAsEncodedStrings() {
        LinkedList retval = super.getMessageAsEncodedStrings();
        if (this.requestLine != null) {
            setRequestLineDefaults();
            retval.addFirst(this.requestLine.encode());
        }
        return retval;
    }

    public boolean match(Object matchObj) {
        if (matchObj == null) {
            return true;
        }
        if (!matchObj.getClass().equals(getClass())) {
            return false;
        }
        if (matchObj == this) {
            return true;
        }
        SIPRequest that = (SIPRequest) matchObj;
        RequestLine rline = that.requestLine;
        if (this.requestLine == null && rline != null) {
            return false;
        }
        if (this.requestLine == rline) {
            return super.match(matchObj);
        }
        if (this.requestLine.match(that.requestLine) && super.match(matchObj)) {
            return true;
        }
        return false;
    }

    public byte[] encodeAsBytes(String transport) {
        if (isNullRequest()) {
            return "\r\n\r\n".getBytes();
        }
        if (this.requestLine == null) {
            return new byte[0];
        }
        byte[] rlbytes = null;
        if (this.requestLine != null) {
            try {
                rlbytes = this.requestLine.encode().getBytes("UTF-8");
            } catch (UnsupportedEncodingException ex) {
                InternalErrorHandler.handleException(ex);
            }
        }
        byte[] superbytes = super.encodeAsBytes(transport);
        byte[] retval = new byte[(rlbytes.length + superbytes.length)];
        System.arraycopy(rlbytes, 0, retval, 0, rlbytes.length);
        System.arraycopy(superbytes, 0, retval, rlbytes.length, superbytes.length);
        return retval;
    }

    public SIPResponse createResponse(int statusCode) {
        return createResponse(statusCode, SIPResponse.getReasonPhrase(statusCode));
    }

    public SIPResponse createResponse(int statusCode, String reasonPhrase) {
        SIPResponse newResponse = new SIPResponse();
        try {
            newResponse.setStatusCode(statusCode);
            if (reasonPhrase != null) {
                newResponse.setReasonPhrase(reasonPhrase);
            } else {
                newResponse.setReasonPhrase(SIPResponse.getReasonPhrase(statusCode));
            }
            for (String headerName : headersToIncludeInResponse) {
                SIPHeader nextHeader = (SIPHeader) this.headerTable.get(headerName);
                if (nextHeader != null && (!(nextHeader instanceof RecordRouteList) || ((nextHeader instanceof RecordRouteList) && mustCopyRR(statusCode)))) {
                    try {
                        newResponse.attachHeader((SIPHeader) nextHeader.clone(), false);
                    } catch (SIPDuplicateHeaderException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (MessageFactoryImpl.getDefaultServerHeader() != null) {
                newResponse.setHeader((Header) MessageFactoryImpl.getDefaultServerHeader());
            }
            ServerHeader server = MessageFactoryImpl.getDefaultServerHeader();
            if (server != null) {
                newResponse.setHeader((Header) server);
            }
            return newResponse;
        } catch (ParseException e2) {
            throw new IllegalArgumentException("Bad code " + statusCode);
        }
    }

    /* access modifiers changed from: protected|final */
    public final boolean mustCopyRR(int code) {
        if (code <= 100 || code >= 300 || !isDialogCreating(getMethod()) || getToTag() != null) {
            return false;
        }
        return true;
    }

    public SIPRequest createCancelRequest() throws SipException {
        if (getMethod().equals("INVITE")) {
            SIPRequest cancel = new SIPRequest();
            cancel.setRequestLine((RequestLine) this.requestLine.clone());
            cancel.setMethod(Request.CANCEL);
            cancel.setHeader((Header) this.callIdHeader.clone());
            cancel.setHeader((Header) this.toHeader.clone());
            cancel.setHeader((Header) this.cSeqHeader.clone());
            try {
                cancel.getCSeq().setMethod(Request.CANCEL);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            cancel.setHeader((Header) this.fromHeader.clone());
            cancel.addFirst((Header) getTopmostVia().clone());
            cancel.setHeader((Header) this.maxForwardsHeader.clone());
            if (getRouteHeaders() != null) {
                cancel.setHeader((Header) (SIPHeaderList) getRouteHeaders().clone());
            }
            if (MessageFactoryImpl.getDefaultUserAgentHeader() != null) {
                cancel.setHeader((Header) MessageFactoryImpl.getDefaultUserAgentHeader());
            }
            return cancel;
        }
        throw new SipException("Attempt to create CANCEL for " + getMethod());
    }

    public SIPRequest createAckRequest(To responseToHeader) {
        SIPRequest newRequest = (SIPRequest) clone();
        newRequest.setMethod("ACK");
        newRequest.removeHeader("Route");
        newRequest.removeHeader("Proxy-Authorization");
        newRequest.removeContent();
        newRequest.removeHeader("Content-Type");
        try {
            newRequest.getCSeq().setMethod("ACK");
        } catch (ParseException e) {
        }
        if (responseToHeader != null) {
            newRequest.setTo(responseToHeader);
        }
        newRequest.removeHeader("Contact");
        newRequest.removeHeader("Expires");
        ViaList via = newRequest.getViaHeaders();
        if (via != null && via.size() > 1) {
            for (int i = 2; i < via.size(); i++) {
                via.remove(i);
            }
        }
        if (MessageFactoryImpl.getDefaultUserAgentHeader() != null) {
            newRequest.setHeader((Header) MessageFactoryImpl.getDefaultUserAgentHeader());
        }
        return newRequest;
    }

    public final SIPRequest createErrorAck(To responseToHeader) throws SipException, ParseException {
        SIPRequest newRequest = new SIPRequest();
        newRequest.setRequestLine((RequestLine) this.requestLine.clone());
        newRequest.setMethod("ACK");
        newRequest.setHeader((Header) this.callIdHeader.clone());
        newRequest.setHeader((Header) this.maxForwardsHeader.clone());
        newRequest.setHeader((Header) this.fromHeader.clone());
        newRequest.setHeader((Header) responseToHeader.clone());
        newRequest.addFirst((Header) getTopmostVia().clone());
        newRequest.setHeader((Header) this.cSeqHeader.clone());
        newRequest.getCSeq().setMethod("ACK");
        if (getRouteHeaders() != null) {
            newRequest.setHeader((SIPHeaderList) getRouteHeaders().clone());
        }
        if (MessageFactoryImpl.getDefaultUserAgentHeader() != null) {
            newRequest.setHeader((Header) MessageFactoryImpl.getDefaultUserAgentHeader());
        }
        return newRequest;
    }

    public String getViaHost() {
        return ((Via) getViaHeaders().getFirst()).getHost();
    }

    public int getViaPort() {
        Via via = (Via) getViaHeaders().getFirst();
        if (via.hasPort()) {
            return via.getPort();
        }
        return 5060;
    }

    public String getFirstLine() {
        if (this.requestLine == null) {
            return null;
        }
        return this.requestLine.encode();
    }

    public void setSIPVersion(String sipVersion) throws ParseException {
        if (sipVersion == null || !sipVersion.equalsIgnoreCase(SIPConstants.SIP_VERSION_STRING)) {
            throw new ParseException("sipVersion", 0);
        }
        this.requestLine.setSipVersion(sipVersion);
    }

    public String getSIPVersion() {
        return this.requestLine.getSipVersion();
    }

    public Object getTransaction() {
        return this.transactionPointer;
    }

    public void setTransaction(Object transaction) {
        this.transactionPointer = transaction;
    }

    public Object getMessageChannel() {
        return this.messageChannel;
    }

    public void setMessageChannel(Object messageChannel) {
        this.messageChannel = messageChannel;
    }

    public String getMergeId() {
        String fromTag = getFromTag();
        String cseq = this.cSeqHeader.toString();
        String callId = this.callIdHeader.getCallId();
        String requestUri = getRequestURI().toString();
        if (fromTag != null) {
            return requestUri + Separators.COLON + fromTag + Separators.COLON + cseq + Separators.COLON + callId;
        }
        return null;
    }

    public void setInviteTransaction(Object inviteTransaction) {
        this.inviteTransaction = inviteTransaction;
    }

    public Object getInviteTransaction() {
        return this.inviteTransaction;
    }

    public void cleanUp() {
        super.cleanUp();
    }
}
