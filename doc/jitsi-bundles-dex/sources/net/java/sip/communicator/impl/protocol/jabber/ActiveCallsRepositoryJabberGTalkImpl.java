package net.java.sip.communicator.impl.protocol.jabber;

import java.util.Iterator;
import net.java.sip.communicator.impl.protocol.jabber.AbstractCallJabberGTalkImpl;
import net.java.sip.communicator.impl.protocol.jabber.AbstractCallPeerJabberGTalkImpl;
import net.java.sip.communicator.service.protocol.ActiveCallsRepository;
import net.java.sip.communicator.service.protocol.Call;
import net.java.sip.communicator.service.protocol.event.CallChangeEvent;

public class ActiveCallsRepositoryJabberGTalkImpl<T extends AbstractCallJabberGTalkImpl<U>, U extends AbstractCallPeerJabberGTalkImpl<T, ?, ?>> extends ActiveCallsRepository<T, OperationSetBasicTelephonyJabberImpl> {
    public ActiveCallsRepositoryJabberGTalkImpl(OperationSetBasicTelephonyJabberImpl opSet) {
        super(opSet);
    }

    public T findSID(String sid) {
        Iterator<T> calls = getActiveCalls();
        while (calls.hasNext()) {
            AbstractCallJabberGTalkImpl call = (AbstractCallJabberGTalkImpl) calls.next();
            if (call.containsSID(sid)) {
                return call;
            }
        }
        return null;
    }

    public T findCallId(String callid) {
        Iterator<T> calls = getActiveCalls();
        while (calls.hasNext()) {
            AbstractCallJabberGTalkImpl call = (AbstractCallJabberGTalkImpl) calls.next();
            if (call.getCallID().equals(callid)) {
                return call;
            }
        }
        return null;
    }

    public U findCallPeer(String sid) {
        Iterator<T> calls = getActiveCalls();
        while (calls.hasNext()) {
            U peer = ((AbstractCallJabberGTalkImpl) calls.next()).getPeer(sid);
            if (peer != null) {
                return peer;
            }
        }
        return null;
    }

    public U findCallPeerBySessInitPacketID(String id) {
        Iterator<T> calls = getActiveCalls();
        while (calls.hasNext()) {
            U peer = ((AbstractCallJabberGTalkImpl) calls.next()).getPeerBySessInitPacketID(id);
            if (peer != null) {
                return peer;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void fireCallEvent(int eventID, Call sourceCall, CallChangeEvent cause) {
        ((OperationSetBasicTelephonyJabberImpl) this.parentOperationSet).fireCallEvent(eventID, sourceCall);
    }
}
