package net.java.sip.communicator.impl.protocol.sip;

import org.jitsi.javax.sip.DialogTerminatedEvent;
import org.jitsi.javax.sip.IOExceptionEvent;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.ResponseEvent;
import org.jitsi.javax.sip.TimeoutEvent;
import org.jitsi.javax.sip.TransactionTerminatedEvent;

public class MethodProcessorAdapter implements MethodProcessor {
    public boolean processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        return false;
    }

    public boolean processIOException(IOExceptionEvent exceptionEvent) {
        return false;
    }

    public boolean processRequest(RequestEvent requestEvent) {
        return false;
    }

    public boolean processResponse(ResponseEvent responseEvent) {
        return false;
    }

    public boolean processTimeout(TimeoutEvent timeoutEvent) {
        return false;
    }

    public boolean processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        return false;
    }
}
