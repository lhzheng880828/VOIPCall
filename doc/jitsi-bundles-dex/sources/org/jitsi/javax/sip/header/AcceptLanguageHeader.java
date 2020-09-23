package org.jitsi.javax.sip.header;

import java.util.Locale;
import org.jitsi.javax.sip.InvalidArgumentException;

public interface AcceptLanguageHeader extends Parameters, Header {
    public static final String NAME = "Accept-Language";

    Locale getAcceptLanguage();

    float getQValue();

    void setAcceptLanguage(Locale locale);

    void setQValue(float f) throws InvalidArgumentException;
}
