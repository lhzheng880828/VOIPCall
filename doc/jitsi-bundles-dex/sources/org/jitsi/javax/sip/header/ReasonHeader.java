package org.jitsi.javax.sip.header;

import java.text.ParseException;
import org.jitsi.javax.sip.InvalidArgumentException;

public interface ReasonHeader extends Parameters, Header {
    public static final String NAME = "Reason";

    int getCause();

    String getProtocol();

    String getText();

    void setCause(int i) throws InvalidArgumentException;

    void setProtocol(String str) throws ParseException;

    void setText(String str) throws ParseException;
}
