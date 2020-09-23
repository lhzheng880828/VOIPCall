package org.jitsi.gov.nist.javax.sip;

import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.javax.sip.message.SIPMessage;
import org.jitsi.gov.nist.javax.sip.message.SIPRequest;
import org.jitsi.gov.nist.javax.sip.message.SIPResponse;
import org.jitsi.gov.nist.javax.sip.stack.MessageChannel;
import org.jitsi.gov.nist.javax.sip.stack.SIPTransaction;
import org.jitsi.gov.nist.javax.sip.stack.SIPTransactionStack;
import org.jitsi.gov.nist.javax.sip.stack.ServerRequestInterface;
import org.jitsi.gov.nist.javax.sip.stack.ServerResponseInterface;
import org.jitsi.gov.nist.javax.sip.stack.StackMessageFactory;

class NistSipMessageFactoryImpl implements StackMessageFactory {
    private static StackLogger logger = CommonLogger.getLogger(NistSipMessageFactoryImpl.class);
    private SIPTransactionStack sipStack;

    public ServerRequestInterface newSIPServerRequest(SIPRequest sipRequest, MessageChannel messageChannel) {
        if (messageChannel == null || sipRequest == null) {
            throw new IllegalArgumentException("Null Arg!");
        }
        DialogFilter retval = new DialogFilter(messageChannel.getSIPStack());
        if (messageChannel instanceof SIPTransaction) {
            retval.transactionChannel = (SIPTransaction) messageChannel;
        }
        retval.listeningPoint = messageChannel.getMessageProcessor().getListeningPoint();
        if (retval.listeningPoint == null) {
            return null;
        }
        if (!logger.isLoggingEnabled(32)) {
            return retval;
        }
        logger.logDebug("Returning request interface for " + sipRequest.getFirstLine() + Separators.SP + retval + " messageChannel = " + messageChannel);
        return retval;
    }

    public ServerResponseInterface newSIPServerResponse(SIPResponse sipResponse, MessageChannel messageChannel) {
        SIPTransaction tr = messageChannel.getSIPStack().findTransaction((SIPMessage) sipResponse, false);
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Found Transaction " + tr + " for " + sipResponse);
        }
        if (tr != null) {
            if (tr.getInternalState() < 0) {
                if (!logger.isLoggingEnabled(32)) {
                    return null;
                }
                logger.logDebug("Dropping response - null transaction state");
                return null;
            } else if (3 == tr.getInternalState() && sipResponse.getStatusCode() / 100 == 1) {
                if (!logger.isLoggingEnabled(32)) {
                    return null;
                }
                logger.logDebug("Dropping response - late arriving " + sipResponse.getStatusCode());
                return null;
            }
        }
        ServerResponseInterface retval = new DialogFilter(this.sipStack);
        retval.transactionChannel = tr;
        retval.listeningPoint = messageChannel.getMessageProcessor().getListeningPoint();
        return retval;
    }

    public NistSipMessageFactoryImpl(SIPTransactionStack sipStackImpl) {
        this.sipStack = sipStackImpl;
    }
}
