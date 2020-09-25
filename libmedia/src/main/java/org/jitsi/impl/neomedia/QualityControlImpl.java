/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.impl.neomedia;

import org.jitsi.impl.neomedia.device.DeviceConfiguration;
import org.jitsi.service.neomedia.QualityControl;
import org.jitsi.service.neomedia.QualityPreset;
import org.jitsi.service.protocol.OperationFailedException;
import org.jitsi.util.Logger;

import java.awt.Dimension;

/**
 * Implements {@link QualityControl} for the purposes of
 * {@link VideoMediaStreamImpl}.
 *
 * @author Damian Minkov
 * @author Lyubomir Marinov
 */
class QualityControlImpl
    implements QualityControl
{
    /**
     * The <tt>Logger</tt> used by the <tt>QualityControlImpl</tt> class and its
     * instances for logging output.
     */
    private static final Logger logger
        = Logger.getLogger(QualityControlImpl.class);

    /**
     * This is the local settings from the configuration panel.
     */
    private QualityPreset localSettingsPreset;

    /**
     * The maximum values for resolution, framerate, etc.
     */
    private QualityPreset maxPreset;

    /**
     * The current used preset.
     */
    private QualityPreset preset;

    /**
     * Sets the preset.
     *
     * @param preset the desired video settings
     * @throws OperationFailedException
     */
    private void setRemoteReceivePreset(QualityPreset preset)
        throws OperationFailedException
    {
        QualityPreset preferredSendPreset = getPreferredSendPreset();

        if (preset.compareTo(preferredSendPreset) > 0)
            this.preset = preferredSendPreset;
        else
        {
            this.preset = preset;

            java.awt.Dimension resolution;

            if (logger.isInfoEnabled()
                    && (preset != null)
                    && ((resolution = preset.getResolution()) != null))
            {
                logger.info(
                        "video send resolution: " + resolution.width + "x"
                            + resolution.height);
            }
        }
    }

    /**
     * The current preset.
     *
     * @return the current preset
     */
    public QualityPreset getRemoteReceivePreset()
    {
        return preset;
    }

    /**
     * The minimum resolution values for remote part.
     *
     * @return minimum resolution values for remote part.
     */
    public QualityPreset getRemoteSendMinPreset()
    {
        /* We do not support such a value at the time of this writing. */
        return null;
    }

    /**
     * The max resolution values for remote part.
     *
     * @return max resolution values for remote part.
     */
    public QualityPreset getRemoteSendMaxPreset()
    {
        return maxPreset;
    }

    /**
     * Does nothing specific locally.
     *
     * @param preset the max preset
     * @throws OperationFailedException not thrown.
     */
    public void setPreferredRemoteSendMaxPreset(QualityPreset preset)
        throws OperationFailedException
    {
        setRemoteSendMaxPreset(preset);
    }

    /**
     * Changes remote send preset, the one we will receive.
     *
     * @param preset
     */
    public void setRemoteSendMaxPreset(QualityPreset preset)
    {
        this.maxPreset = preset;
    }

    /**
     * Gets the local setting of capture.
     *
     * @return the local setting of capture
     */
    private QualityPreset getPreferredSendPreset()
    {
        if(localSettingsPreset == null)
        {
            DeviceConfiguration devCfg
                = NeomediaServiceUtils
                    .getMediaServiceImpl()
                        .getDeviceConfiguration();

            localSettingsPreset
                = new QualityPreset(
                        devCfg.getVideoSize(),
                        devCfg.getFrameRate());
        }
        return localSettingsPreset;
    }

    /**
     * Sets maximum resolution.
     *
     * @param res
     */
    public void setRemoteReceiveResolution(Dimension res)
    {
        try
        {
            setRemoteReceivePreset(new QualityPreset(res == null? null: new java.awt.Dimension(res.width, res.height)));
        }
        catch(OperationFailedException ofe)
        {
            logger.warn("Failed to set remote receive resolution", ofe);
        }
    }
}
