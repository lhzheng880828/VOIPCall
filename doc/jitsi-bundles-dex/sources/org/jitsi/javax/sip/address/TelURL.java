package org.jitsi.javax.sip.address;

import java.text.ParseException;
import org.jitsi.javax.sip.header.Parameters;

public interface TelURL extends URI, Parameters {
    String getIsdnSubAddress();

    String getPhoneContext();

    String getPhoneNumber();

    String getPostDial();

    boolean isGlobal();

    void setGlobal(boolean z);

    void setIsdnSubAddress(String str) throws ParseException;

    void setPhoneContext(String str) throws ParseException;

    void setPhoneNumber(String str) throws ParseException;

    void setPostDial(String str) throws ParseException;

    String toString();
}
