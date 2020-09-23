package net.java.sip.communicator.impl.protocol.sip;

import org.jitsi.javax.sip.DialogTerminatedEvent;
import org.jitsi.javax.sip.IOExceptionEvent;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.ResponseEvent;
import org.jitsi.javax.sip.TimeoutEvent;
import org.jitsi.javax.sip.TransactionTerminatedEvent;

public interface MethodProcessor {
    boolean processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent);

    boolean processIOException(IOExceptionEvent iOExceptionEvent);

    boolean processRequest(RequestEvent requestEvent);

    boolean processResponse(ResponseEvent responseEvent);

    boolean processTimeout(TimeoutEvent timeoutEvent);

    boolean processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent);
}
