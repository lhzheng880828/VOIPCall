package org.jitsi.gov.nist.javax.sip.header.ims;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.header.ParametersHeader;
import org.jitsi.javax.sip.header.ExtensionHeader;

public class PChargingVector extends ParametersHeader implements PChargingVectorHeader, SIPHeaderNamesIms, ExtensionHeader {
    public PChargingVector() {
        super("P-Charging-Vector");
    }

    public StringBuilder encodeBody(StringBuilder encoding) {
        getNameValue(ParameterNamesIms.ICID_VALUE).encode(encoding);
        if (this.parameters.containsKey(ParameterNamesIms.ICID_GENERATED_AT)) {
            encoding.append(Separators.SEMICOLON).append(ParameterNamesIms.ICID_GENERATED_AT).append(Separators.EQUALS).append(getICIDGeneratedAt());
        }
        if (this.parameters.containsKey(ParameterNamesIms.TERM_IOI)) {
            encoding.append(Separators.SEMICOLON).append(ParameterNamesIms.TERM_IOI).append(Separators.EQUALS).append(getTerminatingIOI());
        }
        if (this.parameters.containsKey(ParameterNamesIms.ORIG_IOI)) {
            encoding.append(Separators.SEMICOLON).append(ParameterNamesIms.ORIG_IOI).append(Separators.EQUALS).append(getOriginatingIOI());
        }
        return encoding;
    }

    public String getICID() {
        return getParameter(ParameterNamesIms.ICID_VALUE);
    }

    public void setICID(String icid) throws ParseException {
        if (icid == null) {
            throw new NullPointerException("JAIN-SIP Exception, P-Charging-Vector, setICID(), the icid parameter is null.");
        }
        setParameter(ParameterNamesIms.ICID_VALUE, icid);
    }

    public String getICIDGeneratedAt() {
        return getParameter(ParameterNamesIms.ICID_GENERATED_AT);
    }

    public void setICIDGeneratedAt(String host) throws ParseException {
        if (host == null) {
            throw new NullPointerException("JAIN-SIP Exception, P-Charging-Vector, setICIDGeneratedAt(), the host parameter is null.");
        }
        setParameter(ParameterNamesIms.ICID_GENERATED_AT, host);
    }

    public String getOriginatingIOI() {
        return getParameter(ParameterNamesIms.ORIG_IOI);
    }

    public void setOriginatingIOI(String origIOI) throws ParseException {
        if (origIOI == null || origIOI.length() == 0) {
            removeParameter(ParameterNamesIms.ORIG_IOI);
        } else {
            setParameter(ParameterNamesIms.ORIG_IOI, origIOI);
        }
    }

    public String getTerminatingIOI() {
        return getParameter(ParameterNamesIms.TERM_IOI);
    }

    public void setTerminatingIOI(String termIOI) throws ParseException {
        if (termIOI == null || termIOI.length() == 0) {
            removeParameter(ParameterNamesIms.TERM_IOI);
        } else {
            setParameter(ParameterNamesIms.TERM_IOI, termIOI);
        }
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }
}
