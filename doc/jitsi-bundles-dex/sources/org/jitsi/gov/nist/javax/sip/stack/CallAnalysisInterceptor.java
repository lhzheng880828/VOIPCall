package org.jitsi.gov.nist.javax.sip.stack;

import java.util.Properties;
import org.jitsi.gov.nist.javax.sip.SipStackImpl;
import org.jitsi.gov.nist.javax.sip.stack.CallAnalyzer.MetricAnalysisConfiguration;
import org.jitsi.gov.nist.javax.sip.stack.CallAnalyzer.MetricReference;
import org.jitsi.javax.sip.SipStack;
import org.jitsi.javax.sip.message.Message;

public class CallAnalysisInterceptor implements SIPEventInterceptor {
    private static final MetricReference interceptorCheckpoint = new MetricReference("ick");
    private CallAnalyzer callAnalyzer;

    public void afterMessage(Message message) {
        this.callAnalyzer.leave(interceptorCheckpoint);
    }

    public void beforeMessage(Message message) {
        this.callAnalyzer.enter(interceptorCheckpoint);
    }

    public void destroy() {
        this.callAnalyzer.stop();
        this.callAnalyzer = null;
    }

    public void init(SipStack stack) {
        this.callAnalyzer = new CallAnalyzer((SipStackImpl) stack);
        Properties props = ((SipStackImpl) stack).getConfigurationProperties();
        this.callAnalyzer.configure(interceptorCheckpoint, new MetricAnalysisConfiguration(Long.valueOf(Long.parseLong(props.getProperty(CallAnalysisInterceptor.class.getName() + ".checkingInterval", "1000"))), Long.valueOf(Long.parseLong(props.getProperty(CallAnalysisInterceptor.class.getName() + ".minTimeBetweenDumps", "2000"))), Long.valueOf(Long.parseLong(props.getProperty(CallAnalysisInterceptor.class.getName() + ".minStuckTIme", "4000")))));
    }
}
