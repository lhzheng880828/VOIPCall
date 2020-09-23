package net.java.sip.communicator.impl.protocol.jabber;

import net.java.sip.communicator.service.protocol.Call;
import net.java.sip.communicator.service.protocol.CallPeer;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.media.AbstractOperationSetVideoTelephony;
import net.java.sip.communicator.util.Logger;
import org.jitsi.service.neomedia.QualityControl;

public class OperationSetVideoTelephonyJabberImpl extends AbstractOperationSetVideoTelephony<OperationSetBasicTelephonyJabberImpl, ProtocolProviderServiceJabberImpl, CallJabberImpl, CallPeerJabberImpl> {
    private static final Logger logger = Logger.getLogger(OperationSetVideoTelephonyJabberImpl.class);

    public OperationSetVideoTelephonyJabberImpl(OperationSetBasicTelephonyJabberImpl basicTelephony) {
        super(basicTelephony);
    }

    public void setLocalVideoAllowed(Call call, boolean allowed) throws OperationFailedException {
        OperationSetVideoTelephonyJabberImpl.super.setLocalVideoAllowed(call, allowed);
        ((CallJabberImpl) call).modifyVideoContent();
    }

    public Call createVideoCall(String uri) throws OperationFailedException {
        return createOutgoingVideoCall(uri);
    }

    public Call createVideoCall(Contact callee) throws OperationFailedException {
        return createOutgoingVideoCall(callee.getAddress());
    }

    /* access modifiers changed from: protected */
    public Call createOutgoingVideoCall(String calleeAddress) throws OperationFailedException {
        if (logger.isInfoEnabled()) {
            logger.info("creating outgoing video call...");
        }
        if (((ProtocolProviderServiceJabberImpl) this.parentProvider).getConnection() == null) {
            throw new OperationFailedException("Failed to create OutgoingJingleSession.\nwe don't have a valid XMPPConnection.", 4);
        }
        CallJabberImpl call = new CallJabberImpl((OperationSetBasicTelephonyJabberImpl) this.basicTelephony);
        call.setLocalVideoAllowed(true, getMediaUseCase());
        return ((OperationSetBasicTelephonyJabberImpl) this.basicTelephony).createOutgoingCall(call, calleeAddress).getCall();
    }

    public void answerVideoCallPeer(CallPeer peer) throws OperationFailedException {
        CallPeerJabberImpl callPeer = (CallPeerJabberImpl) peer;
        ((CallJabberImpl) callPeer.getCall()).setLocalVideoAllowed(true, getMediaUseCase());
        callPeer.answer();
    }

    public QualityControl getQualityControl(CallPeer peer) {
        if (peer instanceof CallPeerJabberImpl) {
            return ((CallPeerMediaHandlerJabberImpl) ((CallPeerJabberImpl) peer).getMediaHandler()).getQualityControl();
        }
        return null;
    }
}
