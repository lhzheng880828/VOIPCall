package org.jitsi.gov.nist.javax.sip.header;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.jitsi.javax.sip.header.UserAgentHeader;

public class UserAgent extends SIPHeader implements UserAgentHeader {
    private static final long serialVersionUID = 4561239179796364295L;
    protected List productTokens = new LinkedList();

    private StringBuilder encodeProduct(StringBuilder tokens) {
        ListIterator it = this.productTokens.listIterator();
        while (it.hasNext()) {
            tokens.append((String) it.next());
        }
        return tokens;
    }

    public void addProductToken(String pt) {
        this.productTokens.add(pt);
    }

    public UserAgent() {
        super("User-Agent");
    }

    public StringBuilder encodeBody(StringBuilder buffer) {
        return encodeProduct(buffer);
    }

    public ListIterator getProduct() {
        if (this.productTokens == null || this.productTokens.isEmpty()) {
            return null;
        }
        return this.productTokens.listIterator();
    }

    public void setProduct(List product) throws ParseException {
        if (product == null) {
            throw new NullPointerException("JAIN-SIP Exception, UserAgent, setProduct(), the  product parameter is null");
        }
        this.productTokens = product;
    }

    public Object clone() {
        UserAgent retval = (UserAgent) super.clone();
        if (this.productTokens != null) {
            retval.productTokens = new LinkedList(this.productTokens);
        }
        return retval;
    }
}
