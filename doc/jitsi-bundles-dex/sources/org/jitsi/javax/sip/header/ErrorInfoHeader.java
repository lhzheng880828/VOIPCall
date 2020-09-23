package org.jitsi.javax.sip.header;

import java.text.ParseException;
import org.jitsi.javax.sip.address.URI;

public interface ErrorInfoHeader extends Parameters, Header {
    public static final String NAME = "Error-Info";

    URI getErrorInfo();

    String getErrorMessage();

    void setErrorInfo(URI uri);

    void setErrorMessage(String str) throws ParseException;
}
