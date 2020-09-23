package org.jitsi.service.neomedia;

import org.jitsi.service.protocol.OperationFailedException;

public interface QualityControl {
    QualityPreset getRemoteReceivePreset();

    QualityPreset getRemoteSendMaxPreset();

    QualityPreset getRemoteSendMinPreset();

    void setPreferredRemoteSendMaxPreset(QualityPreset qualityPreset) throws OperationFailedException;

    void setRemoteSendMaxPreset(QualityPreset qualityPreset);
}
