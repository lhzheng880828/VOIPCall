package org.jitsi.gov.nist.javax.sip.message;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.LinkedList;
import org.jitsi.gov.nist.core.InternalErrorHandler;
import org.jitsi.gov.nist.javax.sip.header.StatusLine;
import org.jitsi.javax.sip.message.Response;

public class SIPResponse extends SIPMessage implements Response, ResponseExt {
    private boolean isRetransmission = true;
    protected StatusLine statusLine;

    public static String getReasonPhrase(int rc) {
        switch (rc) {
            case Response.TRYING /*100*/:
                return "Trying";
            case Response.RINGING /*180*/:
                return "Ringing";
            case Response.CALL_IS_BEING_FORWARDED /*181*/:
                return "Call is being forwarded";
            case Response.QUEUED /*182*/:
                return "Queued";
            case Response.SESSION_PROGRESS /*183*/:
                return "Session progress";
            case Response.OK /*200*/:
                return "OK";
            case Response.ACCEPTED /*202*/:
                return "Accepted";
            case 300:
                return "Multiple choices";
            case 301:
                return "Moved permanently";
            case 302:
                return "Moved Temporarily";
            case 305:
                return "Use proxy";
            case Response.ALTERNATIVE_SERVICE /*380*/:
                return "Alternative service";
            case Response.BAD_REQUEST /*400*/:
                return "Bad request";
            case Response.UNAUTHORIZED /*401*/:
                return "Unauthorized";
            case Response.PAYMENT_REQUIRED /*402*/:
                return "Payment required";
            case Response.FORBIDDEN /*403*/:
                return "Forbidden";
            case Response.NOT_FOUND /*404*/:
                return "Not found";
            case Response.METHOD_NOT_ALLOWED /*405*/:
                return "Method not allowed";
            case Response.NOT_ACCEPTABLE /*406*/:
                return "Not acceptable";
            case Response.PROXY_AUTHENTICATION_REQUIRED /*407*/:
                return "Proxy Authentication required";
            case Response.REQUEST_TIMEOUT /*408*/:
                return "Request timeout";
            case Response.GONE /*410*/:
                return "Gone";
            case Response.CONDITIONAL_REQUEST_FAILED /*412*/:
                return "Conditional request failed";
            case Response.REQUEST_ENTITY_TOO_LARGE /*413*/:
                return "Request entity too large";
            case Response.REQUEST_URI_TOO_LONG /*414*/:
                return "Request-URI too large";
            case Response.UNSUPPORTED_MEDIA_TYPE /*415*/:
                return "Unsupported media type";
            case Response.UNSUPPORTED_URI_SCHEME /*416*/:
                return "Unsupported URI Scheme";
            case Response.BAD_EXTENSION /*420*/:
                return "Bad extension";
            case Response.EXTENSION_REQUIRED /*421*/:
                return "Etension Required";
            case Response.INTERVAL_TOO_BRIEF /*423*/:
                return "Interval too brief";
            case Response.TEMPORARILY_UNAVAILABLE /*480*/:
                return "Temporarily Unavailable";
            case Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST /*481*/:
                return "Call leg/Transaction does not exist";
            case Response.LOOP_DETECTED /*482*/:
                return "Loop detected";
            case Response.TOO_MANY_HOPS /*483*/:
                return "Too many hops";
            case Response.ADDRESS_INCOMPLETE /*484*/:
                return "Address incomplete";
            case Response.AMBIGUOUS /*485*/:
                return "Ambiguous";
            case Response.BUSY_HERE /*486*/:
                return "Busy here";
            case Response.REQUEST_TERMINATED /*487*/:
                return "Request Terminated";
            case Response.NOT_ACCEPTABLE_HERE /*488*/:
                return "Not Acceptable here";
            case Response.BAD_EVENT /*489*/:
                return "Bad Event";
            case Response.REQUEST_PENDING /*491*/:
                return "Request Pending";
            case Response.UNDECIPHERABLE /*493*/:
                return "Undecipherable";
            case 500:
                return "Server Internal Error";
            case Response.NOT_IMPLEMENTED /*501*/:
                return "Not implemented";
            case Response.BAD_GATEWAY /*502*/:
                return "Bad gateway";
            case Response.SERVICE_UNAVAILABLE /*503*/:
                return "Service unavailable";
            case Response.SERVER_TIMEOUT /*504*/:
                return "Gateway timeout";
            case Response.VERSION_NOT_SUPPORTED /*505*/:
                return "SIP version not supported";
            case Response.MESSAGE_TOO_LARGE /*513*/:
                return "Message Too Large";
            case Response.BUSY_EVERYWHERE /*600*/:
                return "Busy everywhere";
            case Response.DECLINE /*603*/:
                return "Decline";
            case Response.DOES_NOT_EXIST_ANYWHERE /*604*/:
                return "Does not exist anywhere";
            case Response.SESSION_NOT_ACCEPTABLE /*606*/:
                return "Session Not acceptable";
            default:
                return "Unknown Status";
        }
    }

    public void setStatusCode(int statusCode) throws ParseException {
        if (statusCode < 100 || statusCode > 699) {
            throw new ParseException("bad status code", 0);
        }
        if (this.statusLine == null) {
            this.statusLine = new StatusLine();
        }
        this.statusLine.setStatusCode(statusCode);
    }

    public StatusLine getStatusLine() {
        return this.statusLine;
    }

    public int getStatusCode() {
        return this.statusLine.getStatusCode();
    }

    public void setReasonPhrase(String reasonPhrase) {
        if (reasonPhrase == null) {
            throw new IllegalArgumentException("Bad reason phrase");
        }
        if (this.statusLine == null) {
            this.statusLine = new StatusLine();
        }
        this.statusLine.setReasonPhrase(reasonPhrase);
    }

    public String getReasonPhrase() {
        if (this.statusLine == null || this.statusLine.getReasonPhrase() == null) {
            return "";
        }
        return this.statusLine.getReasonPhrase();
    }

    public static boolean isFinalResponse(int rc) {
        return rc >= Response.OK && rc < 700;
    }

    public boolean isFinalResponse() {
        return isFinalResponse(this.statusLine.getStatusCode());
    }

    public void setStatusLine(StatusLine sl) {
        this.statusLine = sl;
    }

    public String debugDump() {
        String superstring = super.debugDump();
        this.stringRepresentation = "";
        sprint(SIPResponse.class.getCanonicalName());
        sprint("{");
        if (this.statusLine != null) {
            sprint(this.statusLine.debugDump());
        }
        sprint(superstring);
        sprint("}");
        return this.stringRepresentation;
    }

    public void checkHeaders() throws ParseException {
        if (getCSeq() == null) {
            throw new ParseException("CSeq Is missing ", 0);
        } else if (getTo() == null) {
            throw new ParseException("To Is missing ", 0);
        } else if (getFrom() == null) {
            throw new ParseException("From Is missing ", 0);
        } else if (getViaHeaders() == null) {
            throw new ParseException("Via Is missing ", 0);
        } else if (getCallId() == null) {
            throw new ParseException("Call-ID Is missing ", 0);
        } else if (getStatusCode() > 699) {
            throw new ParseException("Unknown error code!" + getStatusCode(), 0);
        }
    }

    public String encode() {
        if (this.statusLine != null) {
            return this.statusLine.encode() + super.encode();
        }
        return super.encode();
    }

    public StringBuilder encodeMessage(StringBuilder retval) {
        if (this.statusLine == null) {
            return super.encodeSIPHeaders(retval);
        }
        this.statusLine.encode(retval);
        super.encodeSIPHeaders(retval);
        return retval;
    }

    public LinkedList getMessageAsEncodedStrings() {
        LinkedList retval = super.getMessageAsEncodedStrings();
        if (this.statusLine != null) {
            retval.addFirst(this.statusLine.encode());
        }
        return retval;
    }

    public Object clone() {
        SIPResponse retval = (SIPResponse) super.clone();
        if (this.statusLine != null) {
            retval.statusLine = (StatusLine) this.statusLine.clone();
        }
        return retval;
    }

    public boolean equals(Object other) {
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        if (this.statusLine.equals(((SIPResponse) other).statusLine) && super.equals(other)) {
            return true;
        }
        return false;
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
        SIPResponse that = (SIPResponse) matchObj;
        StatusLine rline = that.statusLine;
        if (this.statusLine == null && rline != null) {
            return false;
        }
        if (this.statusLine == rline) {
            return super.match(matchObj);
        }
        if (this.statusLine.match(that.statusLine) && super.match(matchObj)) {
            return true;
        }
        return false;
    }

    public byte[] encodeAsBytes(String transport) {
        byte[] slbytes = null;
        if (this.statusLine != null) {
            try {
                slbytes = this.statusLine.encode().getBytes("UTF-8");
            } catch (UnsupportedEncodingException ex) {
                InternalErrorHandler.handleException(ex);
            }
        }
        byte[] superbytes = super.encodeAsBytes(transport);
        byte[] retval = new byte[(slbytes.length + superbytes.length)];
        System.arraycopy(slbytes, 0, retval, 0, slbytes.length);
        System.arraycopy(superbytes, 0, retval, slbytes.length, superbytes.length);
        return retval;
    }

    public String getFirstLine() {
        if (this.statusLine == null) {
            return null;
        }
        return this.statusLine.encode();
    }

    public void setSIPVersion(String sipVersion) {
        this.statusLine.setSipVersion(sipVersion);
    }

    public String getSIPVersion() {
        return this.statusLine.getSipVersion();
    }

    public String toString() {
        if (this.statusLine == null) {
            return "";
        }
        return this.statusLine.encode() + super.encode();
    }

    public void cleanUp() {
        super.cleanUp();
    }

    public void setRetransmission(boolean isRetransmission) {
        this.isRetransmission = isRetransmission;
    }

    public boolean isRetransmission() {
        return this.isRetransmission;
    }
}
