package org.jitsi.gov.nist.javax.sip.header.extensions;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.header.ParametersHeader;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.header.ExtensionHeader;

public class MinSE extends ParametersHeader implements ExtensionHeader, MinSEHeader {
    public static final String NAME = "Min-SE";
    private static final long serialVersionUID = 3134344915465784267L;
    public int expires;

    public MinSE() {
        super("Min-SE");
    }

    public StringBuilder encodeBody(StringBuilder retval) {
        retval.append(Integer.toString(this.expires));
        if (!this.parameters.isEmpty()) {
            retval.append(Separators.SEMICOLON);
            this.parameters.encode(retval);
        }
        return retval;
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }

    public int getExpires() {
        return this.expires;
    }

    public void setExpires(int expires) throws InvalidArgumentException {
        if (expires < 0) {
            throw new InvalidArgumentException("bad argument " + expires);
        }
        this.expires = expires;
    }
}
