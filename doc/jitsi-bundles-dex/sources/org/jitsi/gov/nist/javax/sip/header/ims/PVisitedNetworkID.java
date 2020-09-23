package org.jitsi.gov.nist.javax.sip.header.ims;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.core.Token;
import org.jitsi.gov.nist.javax.sip.header.ParametersHeader;
import org.jitsi.javax.sip.header.ExtensionHeader;

public class PVisitedNetworkID extends ParametersHeader implements PVisitedNetworkIDHeader, SIPHeaderNamesIms, ExtensionHeader {
    private boolean isQuoted;
    private String networkID;

    public PVisitedNetworkID() {
        super("P-Visited-Network-ID");
    }

    public PVisitedNetworkID(String networkID) {
        super("P-Visited-Network-ID");
        setVisitedNetworkID(networkID);
    }

    public PVisitedNetworkID(Token tok) {
        super("P-Visited-Network-ID");
        setVisitedNetworkID(tok.getTokenValue());
    }

    public StringBuilder encodeBody(StringBuilder retval) {
        if (getVisitedNetworkID() != null) {
            if (this.isQuoted) {
                retval.append(Separators.DOUBLE_QUOTE).append(getVisitedNetworkID()).append(Separators.DOUBLE_QUOTE);
            } else {
                retval.append(getVisitedNetworkID());
            }
        }
        if (!this.parameters.isEmpty()) {
            retval.append(Separators.SEMICOLON);
            this.parameters.encode(retval);
        }
        return retval;
    }

    public void setVisitedNetworkID(String networkID) {
        if (networkID == null) {
            throw new NullPointerException(" the networkID parameter is null");
        }
        this.networkID = networkID;
        this.isQuoted = true;
    }

    public void setVisitedNetworkID(Token networkID) {
        if (networkID == null) {
            throw new NullPointerException(" the networkID parameter is null");
        }
        this.networkID = networkID.getTokenValue();
        this.isQuoted = false;
    }

    public String getVisitedNetworkID() {
        return this.networkID;
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }

    public boolean equals(Object other) {
        if (!(other instanceof PVisitedNetworkIDHeader)) {
            return false;
        }
        PVisitedNetworkIDHeader o = (PVisitedNetworkIDHeader) other;
        if (getVisitedNetworkID().equals(o.getVisitedNetworkID()) && equalParameters(o)) {
            return true;
        }
        return false;
    }

    public Object clone() {
        PVisitedNetworkID retval = (PVisitedNetworkID) super.clone();
        if (this.networkID != null) {
            retval.networkID = this.networkID;
        }
        retval.isQuoted = this.isQuoted;
        return retval;
    }
}
