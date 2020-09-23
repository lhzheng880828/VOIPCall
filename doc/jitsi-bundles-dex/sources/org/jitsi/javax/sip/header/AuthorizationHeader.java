package org.jitsi.javax.sip.header;

import java.text.ParseException;
import org.jitsi.javax.sip.address.URI;

public interface AuthorizationHeader extends Parameters, Header {
    public static final String NAME = "Authorization";

    String getAlgorithm();

    String getCNonce();

    String getNonce();

    int getNonceCount();

    String getOpaque();

    String getQop();

    String getRealm();

    String getResponse();

    String getScheme();

    URI getURI();

    String getUsername();

    void setAlgorithm(String str) throws ParseException;

    void setCNonce(String str) throws ParseException;

    void setNonce(String str) throws ParseException;

    void setNonceCount(int i) throws ParseException;

    void setOpaque(String str) throws ParseException;

    void setQop(String str) throws ParseException;

    void setRealm(String str) throws ParseException;

    void setResponse(String str) throws ParseException;

    void setScheme(String str);

    void setURI(URI uri);

    void setUsername(String str) throws ParseException;
}
