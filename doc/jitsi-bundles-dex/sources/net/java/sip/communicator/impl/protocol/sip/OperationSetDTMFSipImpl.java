package net.java.sip.communicator.impl.protocol.sip;

import net.java.sip.communicator.impl.protocol.sip.dtmf.DTMFInfo;
import net.java.sip.communicator.service.protocol.CallPeer;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.media.AbstractOperationSetDTMF;
import net.java.sip.communicator.util.Logger;
import org.jitsi.service.neomedia.AudioMediaStream;
import org.jitsi.service.neomedia.DTMFMethod;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.protocol.DTMFTone;

public class OperationSetDTMFSipImpl extends AbstractOperationSetDTMF {
    private static final Logger logger = Logger.getLogger(OperationSetDTMFSipImpl.class);
    private final DTMFInfo dtmfModeInfo;

    public OperationSetDTMFSipImpl(ProtocolProviderServiceSipImpl pps) {
        super(pps);
        this.dtmfModeInfo = new DTMFInfo(pps);
    }

    public synchronized void startSendingDTMF(CallPeer callPeer, DTMFTone tone) throws OperationFailedException {
        if (callPeer == null || tone == null) {
            throw new NullPointerException("Argument is null");
        } else if (callPeer instanceof CallPeerSipImpl) {
            CallPeerSipImpl cp = (CallPeerSipImpl) callPeer;
            if (this.dtmfMethod == DTMFMethod.SIP_INFO_DTMF) {
                this.dtmfModeInfo.startSendingDTMF(cp, tone);
            } else {
                DTMFMethod cpDTMFMethod = this.dtmfMethod;
                if (this.dtmfMethod == DTMFMethod.AUTO_DTMF) {
                    if (isRFC4733Active(cp)) {
                        cpDTMFMethod = DTMFMethod.RTP_DTMF;
                    } else {
                        cpDTMFMethod = DTMFMethod.INBAND_DTMF;
                    }
                }
                if (this.dtmfMethod == DTMFMethod.RTP_DTMF && !isRFC4733Active(cp)) {
                    logger.debug("RTP DTMF used without telephon-event capacities");
                }
                ((AudioMediaStream) ((CallPeerMediaHandlerSipImpl) cp.getMediaHandler()).getStream(MediaType.AUDIO)).startSendingDTMF(tone, cpDTMFMethod, this.minimalToneDuration, this.maximalToneDuration, this.volume);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public synchronized void stopSendingDTMF(CallPeer callPeer) {
        if (callPeer == null) {
            throw new NullPointerException("Argument is null");
        } else if (callPeer instanceof CallPeerSipImpl) {
            CallPeerSipImpl cp = (CallPeerSipImpl) callPeer;
            if (this.dtmfMethod == DTMFMethod.SIP_INFO_DTMF) {
                this.dtmfModeInfo.stopSendingDTMF(cp);
            } else {
                DTMFMethod cpDTMFMethod = this.dtmfMethod;
                if (this.dtmfMethod == DTMFMethod.AUTO_DTMF) {
                    if (isRFC4733Active(cp)) {
                        cpDTMFMethod = DTMFMethod.RTP_DTMF;
                    } else {
                        cpDTMFMethod = DTMFMethod.INBAND_DTMF;
                    }
                }
                if (this.dtmfMethod == DTMFMethod.RTP_DTMF && !isRFC4733Active(cp)) {
                    logger.debug("RTP DTMF used without telephon-event capacities");
                }
                ((AudioMediaStream) ((CallPeerMediaHandlerSipImpl) cp.getMediaHandler()).getStream(MediaType.AUDIO)).stopSendingDTMF(cpDTMFMethod);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    /* access modifiers changed from: 0000 */
    public DTMFInfo getDtmfModeInfo() {
        return this.dtmfModeInfo;
    }
}
