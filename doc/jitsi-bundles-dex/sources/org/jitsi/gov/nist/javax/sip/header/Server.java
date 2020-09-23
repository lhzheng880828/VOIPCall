package org.jitsi.gov.nist.javax.sip.header;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.jitsi.javax.sip.header.ServerHeader;

public class Server extends SIPHeader implements ServerHeader {
    private static final long serialVersionUID = -3587764149383342973L;
    protected List productTokens = new LinkedList();

    private StringBuilder encodeProduct(StringBuilder tokens) {
        ListIterator it = this.productTokens.listIterator();
        while (it.hasNext()) {
            tokens.append((String) it.next());
            if (!it.hasNext()) {
                break;
            }
            tokens.append('/');
        }
        return tokens;
    }

    public void addProductToken(String pt) {
        this.productTokens.add(pt);
    }

    public Server() {
        super("Server");
    }

    public StringBuilder encodeBody(StringBuilder retval) {
        return encodeProduct(retval);
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
}
