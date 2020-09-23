package org.jitsi.impl.neomedia;

import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.device.DeviceConfiguration;
import org.jitsi.service.neomedia.QualityControl;
import org.jitsi.service.neomedia.QualityPreset;
import org.jitsi.service.protocol.OperationFailedException;
import org.jitsi.util.Logger;

class QualityControlImpl implements QualityControl {
    private static final Logger logger = Logger.getLogger(QualityControlImpl.class);
    private QualityPreset localSettingsPreset;
    private QualityPreset maxPreset;
    private QualityPreset preset;

    QualityControlImpl() {
    }

    private void setRemoteReceivePreset(QualityPreset preset) throws OperationFailedException {
        QualityPreset preferredSendPreset = getPreferredSendPreset();
        if (preset.compareTo(preferredSendPreset) > 0) {
            this.preset = preferredSendPreset;
            return;
        }
        this.preset = preset;
        if (logger.isInfoEnabled() && preset != null) {
            Dimension resolution = preset.getResolution();
            if (resolution != null) {
                logger.info("video send resolution: " + resolution.width + "x" + resolution.height);
            }
        }
    }

    public QualityPreset getRemoteReceivePreset() {
        return this.preset;
    }

    public QualityPreset getRemoteSendMinPreset() {
        return null;
    }

    public QualityPreset getRemoteSendMaxPreset() {
        return this.maxPreset;
    }

    public void setPreferredRemoteSendMaxPreset(QualityPreset preset) throws OperationFailedException {
        setRemoteSendMaxPreset(preset);
    }

    public void setRemoteSendMaxPreset(QualityPreset preset) {
        this.maxPreset = preset;
    }

    private QualityPreset getPreferredSendPreset() {
        if (this.localSettingsPreset == null) {
            DeviceConfiguration devCfg = NeomediaServiceUtils.getMediaServiceImpl().getDeviceConfiguration();
            this.localSettingsPreset = new QualityPreset(devCfg.getVideoSize(), (float) devCfg.getFrameRate());
        }
        return this.localSettingsPreset;
    }

    public void setRemoteReceiveResolution(Dimension res) {
        try {
            setRemoteReceivePreset(new QualityPreset(res));
        } catch (OperationFailedException ofe) {
            logger.warn("Failed to set remote receive resolution", ofe);
        }
    }
}
