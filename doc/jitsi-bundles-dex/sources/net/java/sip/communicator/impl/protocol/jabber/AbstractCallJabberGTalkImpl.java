package net.java.sip.communicator.impl.protocol.jabber;

import net.java.sip.communicator.impl.protocol.jabber.AbstractCallPeerJabberGTalkImpl;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.media.MediaAwareCall;

public abstract class AbstractCallJabberGTalkImpl<T extends AbstractCallPeerJabberGTalkImpl<?, ?, ?>> extends MediaAwareCall<T, OperationSetBasicTelephonyJabberImpl, ProtocolProviderServiceJabberImpl> {
    private boolean localInputEvtAware = false;

    public abstract void modifyVideoContent() throws OperationFailedException;

    protected AbstractCallJabberGTalkImpl(OperationSetBasicTelephonyJabberImpl parentOpSet) {
        super(parentOpSet);
    }

    public void setLocalInputEvtAware(boolean enable) {
        this.localInputEvtAware = enable;
    }

    public boolean getLocalInputEvtAware() {
        return this.localInputEvtAware;
    }

    public T getPeer(String sid) {
        for (AbstractCallPeerJabberGTalkImpl peer : getCallPeerList()) {
            if (peer.getSID().equals(sid)) {
                return peer;
            }
        }
        return null;
    }

    public boolean containsSID(String sid) {
        return getPeer(sid) != null;
    }

    public T getPeerBySessInitPacketID(String id) {
        for (AbstractCallPeerJabberGTalkImpl peer : getCallPeerList()) {
            if (peer.getSessInitID().equals(id)) {
                return peer;
            }
        }
        return null;
    }
}
