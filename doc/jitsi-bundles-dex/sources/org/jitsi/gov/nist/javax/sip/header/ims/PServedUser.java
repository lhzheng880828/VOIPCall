package org.jitsi.gov.nist.javax.sip.header.ims;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.address.AddressImpl;
import org.jitsi.gov.nist.javax.sip.header.AddressParametersHeader;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.header.ExtensionHeader;

public class PServedUser extends AddressParametersHeader implements PServedUserHeader, SIPHeaderNamesIms, ExtensionHeader {
    public PServedUser(AddressImpl address) {
        super("P-Served-User");
        this.address = address;
    }

    public PServedUser() {
        super("P-Served-User");
    }

    public String getRegistrationState() {
        return getParameter(ParameterNamesIms.REGISTRATION_STATE);
    }

    public String getSessionCase() {
        return getParameter(ParameterNamesIms.SESSION_CASE);
    }

    public void setRegistrationState(String registrationState) {
        if (registrationState == null) {
            throw new NullPointerException("regstate Parameter value is null");
        } else if (registrationState.equals("reg") || registrationState.equals("unreg")) {
            try {
                setParameter(ParameterNamesIms.REGISTRATION_STATE, registrationState);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            try {
                throw new InvalidArgumentException("Value can be either reg or unreg");
            } catch (InvalidArgumentException e2) {
                e2.printStackTrace();
            }
        }
    }

    public void setSessionCase(String sessionCase) {
        if (sessionCase == null) {
            throw new NullPointerException("sess-case Parameter value is null");
        } else if (sessionCase.equals("orig") || sessionCase.equals("term")) {
            try {
                setParameter(ParameterNamesIms.SESSION_CASE, sessionCase);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            try {
                throw new InvalidArgumentException("Value can be either orig or term");
            } catch (InvalidArgumentException e2) {
                e2.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: protected */
    public StringBuilder encodeBody(StringBuilder retval) {
        retval.append(this.address.encode());
        if (this.parameters.containsKey(ParameterNamesIms.REGISTRATION_STATE)) {
            retval.append(Separators.SEMICOLON).append(ParameterNamesIms.REGISTRATION_STATE).append(Separators.EQUALS).append(getRegistrationState());
        }
        if (this.parameters.containsKey(ParameterNamesIms.SESSION_CASE)) {
            retval.append(Separators.SEMICOLON).append(ParameterNamesIms.SESSION_CASE).append(Separators.EQUALS).append(getSessionCase());
        }
        return retval;
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }

    public boolean equals(Object other) {
        if (!(other instanceof PServedUser)) {
            return false;
        }
        PServedUserHeader psu = (PServedUserHeader) other;
        return getAddress().equals(((PServedUser) other).getAddress());
    }

    public Object clone() {
        return (PServedUser) super.clone();
    }
}
