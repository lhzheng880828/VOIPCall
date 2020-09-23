package net.java.sip.communicator.impl.protocol.sip;

import net.java.sip.communicator.service.protocol.OperationSetIncomingDTMF;
import net.java.sip.communicator.service.protocol.event.DTMFListener;

public class OperationSetIncomingDTMFSipImpl implements OperationSetIncomingDTMF {
    private OperationSetDTMFSipImpl opsetDTMFSip;

    OperationSetIncomingDTMFSipImpl(ProtocolProviderServiceSipImpl provider, OperationSetDTMFSipImpl opsetDTMFSip) {
        this.opsetDTMFSip = opsetDTMFSip;
    }

    public void addDTMFListener(DTMFListener listener) {
        this.opsetDTMFSip.getDtmfModeInfo().addDTMFListener(listener);
    }

    public void removeDTMFListener(DTMFListener listener) {
        this.opsetDTMFSip.getDtmfModeInfo().removeDTMFListener(listener);
    }
}
