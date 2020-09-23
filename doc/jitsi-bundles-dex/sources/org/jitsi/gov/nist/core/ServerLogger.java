package org.jitsi.gov.nist.core;

import java.util.Properties;
import org.jitsi.gov.nist.javax.sip.message.SIPMessage;
import org.jitsi.javax.sip.SipStack;

public interface ServerLogger extends LogLevels {
    void closeLogFile();

    void logException(Exception exception);

    void logMessage(SIPMessage sIPMessage, String str, String str2, String str3, boolean z);

    void logMessage(SIPMessage sIPMessage, String str, String str2, String str3, boolean z, long j);

    void logMessage(SIPMessage sIPMessage, String str, String str2, boolean z, long j);

    void setSipStack(SipStack sipStack);

    void setStackProperties(Properties properties);
}
