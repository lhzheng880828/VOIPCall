package org.jitsi.gov.nist.javax.sip.header.extensions;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.header.CallIdentifier;
import org.jitsi.gov.nist.javax.sip.header.ParametersHeader;
import org.jitsi.javax.sip.header.ExtensionHeader;

public class Join extends ParametersHeader implements ExtensionHeader, JoinHeader {
    public static final String NAME = "Join";
    private static final long serialVersionUID = -840116548918120056L;
    public String callId;
    public CallIdentifier callIdentifier;

    public Join() {
        super("Join");
    }

    public Join(String callId) throws IllegalArgumentException {
        super("Join");
        this.callIdentifier = new CallIdentifier(callId);
    }

    public StringBuilder encodeBody(StringBuilder retval) {
        if (this.callId != null) {
            retval.append(this.callId);
            if (!this.parameters.isEmpty()) {
                retval.append(Separators.SEMICOLON);
                this.parameters.encode(retval);
            }
        }
        return retval;
    }

    public String getCallId() {
        return this.callId;
    }

    public CallIdentifier getCallIdentifer() {
        return this.callIdentifier;
    }

    public void setCallId(String cid) {
        this.callId = cid;
    }

    public void setCallIdentifier(CallIdentifier cid) {
        this.callIdentifier = cid;
    }

    public String getToTag() {
        if (this.parameters == null) {
            return null;
        }
        return getParameter("to-tag");
    }

    public void setToTag(String t) throws ParseException {
        if (t == null) {
            throw new NullPointerException("null tag ");
        } else if (t.trim().equals("")) {
            throw new ParseException("bad tag", 0);
        } else {
            setParameter("to-tag", t);
        }
    }

    public boolean hasToTag() {
        return hasParameter("to-tag");
    }

    public void removeToTag() {
        this.parameters.delete("to-tag");
    }

    public String getFromTag() {
        if (this.parameters == null) {
            return null;
        }
        return getParameter("from-tag");
    }

    public void setFromTag(String t) throws ParseException {
        if (t == null) {
            throw new NullPointerException("null tag ");
        } else if (t.trim().equals("")) {
            throw new ParseException("bad tag", 0);
        } else {
            setParameter("from-tag", t);
        }
    }

    public boolean hasFromTag() {
        return hasParameter("from-tag");
    }

    public void removeFromTag() {
        this.parameters.delete("from-tag");
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }
}
