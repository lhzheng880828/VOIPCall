package org.jitsi.javax.sip;

import java.io.Serializable;
import java.util.Iterator;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.header.CallIdHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;

public interface Dialog extends Serializable {
    Request createAck(long j) throws InvalidArgumentException, SipException;

    Request createPrack(Response response) throws DialogDoesNotExistException, SipException;

    Response createReliableProvisionalResponse(int i) throws InvalidArgumentException, SipException;

    Request createRequest(String str) throws SipException;

    void delete();

    Object getApplicationData();

    CallIdHeader getCallId();

    String getDialogId();

    Transaction getFirstTransaction();

    Address getLocalParty();

    long getLocalSeqNumber();

    int getLocalSequenceNumber();

    String getLocalTag();

    Address getRemoteParty();

    long getRemoteSeqNumber();

    int getRemoteSequenceNumber();

    String getRemoteTag();

    Address getRemoteTarget();

    Iterator getRouteSet();

    DialogState getState();

    void incrementLocalSequenceNumber();

    boolean isSecure();

    boolean isServer();

    void sendAck(Request request) throws SipException;

    void sendReliableProvisionalResponse(Response response) throws SipException;

    void sendRequest(ClientTransaction clientTransaction) throws TransactionDoesNotExistException, SipException;

    void setApplicationData(Object obj);

    void terminateOnBye(boolean z) throws SipException;
}
