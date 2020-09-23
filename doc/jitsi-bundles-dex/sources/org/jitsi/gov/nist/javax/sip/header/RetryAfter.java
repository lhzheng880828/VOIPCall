package org.jitsi.gov.nist.javax.sip.header;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.header.RetryAfterHeader;

public class RetryAfter extends ParametersHeader implements RetryAfterHeader {
    public static final String DURATION = "duration";
    private static final long serialVersionUID = -1029458515616146140L;
    protected String comment;
    protected Integer retryAfter = new Integer(0);

    public RetryAfter() {
        super("Retry-After");
    }

    public StringBuilder encodeBody(StringBuilder retval) {
        if (this.retryAfter != null) {
            retval.append(this.retryAfter);
        }
        if (this.comment != null) {
            retval.append(" (" + this.comment + Separators.RPAREN);
        }
        if (!this.parameters.isEmpty()) {
            retval.append(Separators.SEMICOLON);
            this.parameters.encode(retval);
        }
        return retval;
    }

    public boolean hasComment() {
        return this.comment != null;
    }

    public void removeComment() {
        this.comment = null;
    }

    public void removeDuration() {
        super.removeParameter("duration");
    }

    public void setRetryAfter(int retryAfter) throws InvalidArgumentException {
        if (retryAfter < 0) {
            throw new InvalidArgumentException("invalid parameter " + retryAfter);
        }
        this.retryAfter = Integer.valueOf(retryAfter);
    }

    public int getRetryAfter() {
        return this.retryAfter.intValue();
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) throws ParseException {
        if (comment == null) {
            throw new NullPointerException("the comment parameter is null");
        }
        this.comment = comment;
    }

    public void setDuration(int duration) throws InvalidArgumentException {
        if (duration < 0) {
            throw new InvalidArgumentException("the duration parameter is <0");
        }
        setParameter("duration", duration);
    }

    public int getDuration() {
        if (getParameter("duration") == null) {
            return -1;
        }
        return super.getParameterAsInt("duration");
    }
}
