package org.jitsi.gov.nist.javax.sip;

import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.address.Hop;

public interface ClientTransactionExt extends ClientTransaction, TransactionExt {
    void alertIfStillInCallingStateBy(int i);

    Dialog getDefaultDialog();

    Hop getNextHop();

    boolean isSecure();

    void setNotifyOnRetransmit(boolean z);
}
