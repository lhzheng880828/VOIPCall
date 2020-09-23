package org.jitsi.javax.sip;

import java.util.EventListener;

public interface SipListener extends EventListener {
    void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent);

    void processIOException(IOExceptionEvent iOExceptionEvent);

    void processRequest(RequestEvent requestEvent);

    void processResponse(ResponseEvent responseEvent);

    void processTimeout(TimeoutEvent timeoutEvent);

    void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent);
}
