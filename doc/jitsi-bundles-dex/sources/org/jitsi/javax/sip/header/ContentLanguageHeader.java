package org.jitsi.javax.sip.header;

import java.util.Locale;

public interface ContentLanguageHeader extends Header {
    public static final String NAME = "Content-Language";

    Locale getContentLanguage();

    void setContentLanguage(Locale locale);
}
