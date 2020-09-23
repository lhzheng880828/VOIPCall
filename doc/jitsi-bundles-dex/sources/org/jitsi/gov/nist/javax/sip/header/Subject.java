package org.jitsi.gov.nist.javax.sip.header;

import java.text.ParseException;
import org.jitsi.javax.sip.header.SubjectHeader;

public class Subject extends SIPHeader implements SubjectHeader {
    private static final long serialVersionUID = -6479220126758862528L;
    protected String subject;

    public Subject() {
        super("Subject");
    }

    public StringBuilder encodeBody(StringBuilder retval) {
        if (this.subject != null) {
            return retval.append(this.subject);
        }
        return retval.append("");
    }

    public void setSubject(String subject) throws ParseException {
        if (subject == null) {
            throw new NullPointerException("JAIN-SIP Exception,  Subject, setSubject(), the subject parameter is null");
        }
        this.subject = subject;
    }

    public String getSubject() {
        return this.subject;
    }
}
