package org.jitsi.javax.sip;

import java.io.Serializable;
import org.jitsi.javax.sip.message.Request;

public interface Transaction extends Serializable {
    Object getApplicationData();

    String getBranchId();

    Dialog getDialog();

    Request getRequest();

    int getRetransmitTimer() throws UnsupportedOperationException;

    TransactionState getState();

    void setApplicationData(Object obj);

    void setRetransmitTimer(int i) throws UnsupportedOperationException;

    void terminate() throws ObjectInUseException;
}
