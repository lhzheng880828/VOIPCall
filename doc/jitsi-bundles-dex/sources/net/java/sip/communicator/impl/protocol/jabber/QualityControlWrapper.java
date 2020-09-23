package net.java.sip.communicator.impl.protocol.jabber;

import net.java.sip.communicator.service.protocol.media.AbstractQualityControlWrapper;
import net.java.sip.communicator.util.Logger;
import org.jitsi.service.neomedia.QualityControl;
import org.jitsi.service.neomedia.QualityPreset;
import org.jitsi.service.protocol.OperationFailedException;

public class QualityControlWrapper extends AbstractQualityControlWrapper<CallPeerJabberImpl> {
    private static final Logger logger = Logger.getLogger(QualityControlWrapper.class);

    QualityControlWrapper(CallPeerJabberImpl peer) {
        super(peer);
    }

    public void setPreferredRemoteSendMaxPreset(QualityPreset preset) throws OperationFailedException {
        QualityControl qControls = getMediaQualityControl();
        if (qControls != null) {
            qControls.setRemoteSendMaxPreset(preset);
            try {
                ((CallPeerJabberImpl) this.peer).sendModifyVideoResolutionContent();
            } catch (Throwable cause) {
                String message = "Failed to re-invite for video quality change.";
                logger.error(message, cause);
                OperationFailedException operationFailedException = new OperationFailedException(message, 4, cause);
            }
        }
    }
}
