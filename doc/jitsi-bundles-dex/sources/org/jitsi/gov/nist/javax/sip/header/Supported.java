package org.jitsi.gov.nist.javax.sip.header;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.header.SupportedHeader;

public class Supported extends SIPHeader implements SupportedHeader {
    private static final long serialVersionUID = -7679667592702854542L;
    protected String optionTag;

    public Supported() {
        super("Supported");
        this.optionTag = null;
    }

    public Supported(String option_tag) {
        super("Supported");
        this.optionTag = option_tag;
    }

    public String encode() {
        String retval = this.headerName + Separators.COLON;
        if (this.optionTag != null) {
            retval = retval + Separators.SP + this.optionTag;
        }
        return retval + Separators.NEWLINE;
    }

    public StringBuilder encodeBody(StringBuilder retval) {
        return this.optionTag != null ? retval.append(this.optionTag) : retval.append("");
    }

    public void setOptionTag(String optionTag) throws ParseException {
        if (optionTag == null) {
            throw new NullPointerException("JAIN-SIP Exception, Supported, setOptionTag(), the optionTag parameter is null");
        }
        this.optionTag = optionTag;
    }

    public String getOptionTag() {
        return this.optionTag;
    }
}
