package org.jitsi.gov.nist.javax.sip;

import org.jitsi.javax.sip.ServerTransaction;

public interface ServerTransactionExt extends ServerTransaction, TransactionExt {
    ServerTransaction getCanceledInviteTransaction();
}
