package org.jitsi.javax.sip.header;

import java.text.ParseException;
import org.jitsi.javax.sip.address.URI;

public interface CallInfoHeader extends Parameters, Header {
    public static final String NAME = "Call-Info";

    URI getInfo();

    String getPurpose();

    void setInfo(URI uri);

    void setPurpose(String str) throws ParseException;
}
