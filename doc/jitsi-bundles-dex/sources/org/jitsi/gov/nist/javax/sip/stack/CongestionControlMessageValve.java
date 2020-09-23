package org.jitsi.gov.nist.javax.sip.stack;

import java.io.IOException;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.javax.sip.SipStackImpl;
import org.jitsi.gov.nist.javax.sip.message.SIPRequest;
import org.jitsi.gov.nist.javax.sip.message.SIPResponse;
import org.jitsi.javax.sip.SipStack;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;

public class CongestionControlMessageValve implements SIPMessageValve {
    private static StackLogger logger = CommonLogger.getLogger(CongestionControlMessageValve.class);
    protected int dropResponseStatus;
    protected int serverTransactionTableHighwaterMark;
    protected SipStackImpl sipStack;

    public boolean processRequest(SIPRequest request, MessageChannel messageChannel) {
        boolean undropableMethod;
        String requestMethod = request.getMethod();
        if (requestMethod.equals("BYE") || requestMethod.equals("ACK") || requestMethod.equals(Request.PRACK) || requestMethod.equals(Request.CANCEL)) {
            undropableMethod = true;
        } else {
            undropableMethod = false;
        }
        if (undropableMethod || this.serverTransactionTableHighwaterMark > this.sipStack.getServerTransactionTableSize()) {
            return true;
        }
        if (this.dropResponseStatus <= 0) {
            return false;
        }
        SIPResponse response = request.createResponse(this.dropResponseStatus);
        try {
            messageChannel.sendMessage(response);
            return false;
        } catch (IOException e) {
            logger.logError("Failed to send congestion control error response" + response, e);
            return false;
        }
    }

    public boolean processResponse(Response response, MessageChannel messageChannel) {
        return true;
    }

    public void destroy() {
        logger.logInfo("Destorying the congestion control valve " + this);
    }

    public void init(SipStack stack) {
        this.sipStack = (SipStackImpl) stack;
        logger.logInfo("Initializing congestion control valve");
        this.serverTransactionTableHighwaterMark = new Integer(this.sipStack.getConfigurationProperties().getProperty("org.jitsi.gov.nist.javax.sip.MAX_SERVER_TRANSACTIONS", "10000")).intValue();
        this.dropResponseStatus = new Integer(this.sipStack.getConfigurationProperties().getProperty("DROP_RESPONSE_STATUS", "503")).intValue();
    }
}
