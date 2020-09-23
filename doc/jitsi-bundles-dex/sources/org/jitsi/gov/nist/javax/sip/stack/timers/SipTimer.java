package org.jitsi.gov.nist.javax.sip.stack.timers;

import java.util.Properties;
import org.jitsi.gov.nist.javax.sip.SipStackImpl;
import org.jitsi.gov.nist.javax.sip.stack.SIPStackTimerTask;

public interface SipTimer {
    boolean cancel(SIPStackTimerTask sIPStackTimerTask);

    boolean isStarted();

    boolean schedule(SIPStackTimerTask sIPStackTimerTask, long j);

    boolean scheduleWithFixedDelay(SIPStackTimerTask sIPStackTimerTask, long j, long j2);

    void start(SipStackImpl sipStackImpl, Properties properties);

    void stop();
}
