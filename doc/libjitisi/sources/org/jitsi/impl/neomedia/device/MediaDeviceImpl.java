package org.jitsi.impl.neomedia.device;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.media.CaptureDeviceInfo;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSourceException;
import javax.media.control.FormatControl;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.MediaServiceImpl;
import org.jitsi.impl.neomedia.MediaUtils;
import org.jitsi.impl.neomedia.NeomediaServiceUtils;
import org.jitsi.impl.neomedia.format.MediaFormatImpl;
import org.jitsi.impl.neomedia.jmfext.media.protocol.AbstractPullBufferCaptureDevice;
import org.jitsi.impl.neomedia.protocol.CaptureDeviceDelegatePushBufferDataSource;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.QualityPreset;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.service.neomedia.codec.EncodingConfiguration;
import org.jitsi.service.neomedia.device.ScreenDevice;
import org.jitsi.service.neomedia.format.MediaFormat;
import org.jitsi.util.Logger;

public class MediaDeviceImpl extends AbstractMediaDevice {
    private static final Logger logger = Logger.getLogger(MediaDeviceImpl.class);
    private final CaptureDeviceInfo captureDeviceInfo;
    private final MediaType mediaType;

    public static CaptureDevice createTracingCaptureDevice(CaptureDevice captureDevice, final Logger logger) {
        if (captureDevice instanceof PushBufferDataSource) {
            return new CaptureDeviceDelegatePushBufferDataSource(captureDevice) {
                public void connect() throws IOException {
                    super.connect();
                    if (logger.isTraceEnabled()) {
                        logger.trace("Connected " + MediaDeviceImpl.toString(this.captureDevice));
                    }
                }

                public void disconnect() {
                    super.disconnect();
                    if (logger.isTraceEnabled()) {
                        logger.trace("Disconnected " + MediaDeviceImpl.toString(this.captureDevice));
                    }
                }

                public void start() throws IOException {
                    super.start();
                    if (logger.isTraceEnabled()) {
                        logger.trace("Started " + MediaDeviceImpl.toString(this.captureDevice));
                    }
                }

                public void stop() throws IOException {
                    super.stop();
                    if (logger.isTraceEnabled()) {
                        logger.trace("Stopped " + MediaDeviceImpl.toString(this.captureDevice));
                    }
                }
            };
        }
        return captureDevice;
    }

    /* access modifiers changed from: private|static */
    public static String toString(CaptureDevice captureDevice) {
        StringBuffer str = new StringBuffer();
        str.append("CaptureDevice with hashCode ");
        str.append(captureDevice.hashCode());
        str.append(" and captureDeviceInfo ");
        CaptureDeviceInfo captureDeviceInfo = captureDevice.getCaptureDeviceInfo();
        MediaLocator mediaLocator = captureDeviceInfo.getLocator();
        if (mediaLocator != null) {
            Object captureDeviceInfo2 = mediaLocator;
        }
        str.append(captureDeviceInfo2);
        return str.toString();
    }

    public MediaDeviceImpl(CaptureDeviceInfo captureDeviceInfo, MediaType mediaType) {
        if (captureDeviceInfo == null) {
            throw new NullPointerException("captureDeviceInfo");
        } else if (mediaType == null) {
            throw new NullPointerException("mediaType");
        } else {
            this.captureDeviceInfo = captureDeviceInfo;
            this.mediaType = mediaType;
        }
    }

    public MediaDeviceImpl(MediaType mediaType) {
        this.captureDeviceInfo = null;
        this.mediaType = mediaType;
    }

    /* access modifiers changed from: protected */
    public CaptureDevice createCaptureDevice() {
        CaptureDevice captureDevice = null;
        if (!getDirection().allowsSending()) {
            return captureDevice;
        }
        CaptureDeviceInfo captureDeviceInfo = getCaptureDeviceInfo();
        Throwable exception = null;
        try {
            captureDevice = (CaptureDevice) Manager.createDataSource(captureDeviceInfo.getLocator());
        } catch (IOException ioe) {
            exception = ioe;
        } catch (NoDataSourceException ndse) {
            exception = ndse;
        }
        if (exception != null) {
            logger.error("Failed to create CaptureDevice from CaptureDeviceInfo " + captureDeviceInfo, exception);
            return captureDevice;
        }
        if (captureDevice instanceof AbstractPullBufferCaptureDevice) {
            ((AbstractPullBufferCaptureDevice) captureDevice).setCaptureDeviceInfo(captureDeviceInfo);
        }
        if (logger.isTraceEnabled()) {
            return createTracingCaptureDevice(captureDevice, logger);
        }
        return captureDevice;
    }

    /* access modifiers changed from: protected */
    public DataSource createOutputDataSource() {
        return getDirection().allowsSending() ? (DataSource) createCaptureDevice() : null;
    }

    public CaptureDeviceInfo getCaptureDeviceInfo() {
        return this.captureDeviceInfo;
    }

    public String getCaptureDeviceInfoLocatorProtocol() {
        CaptureDeviceInfo cdi = getCaptureDeviceInfo();
        if (cdi != null) {
            MediaLocator locator = cdi.getLocator();
            if (locator != null) {
                return locator.getProtocol();
            }
        }
        return null;
    }

    public MediaDirection getDirection() {
        if (getCaptureDeviceInfo() != null) {
            return MediaDirection.SENDRECV;
        }
        return MediaType.AUDIO.equals(getMediaType()) ? MediaDirection.INACTIVE : MediaDirection.RECVONLY;
    }

    public MediaFormat getFormat() {
        CaptureDevice captureDevice = createCaptureDevice();
        if (captureDevice != null) {
            MediaType mediaType = getMediaType();
            for (FormatControl formatControl : captureDevice.getFormatControls()) {
                MediaFormat format = MediaFormatImpl.createInstance(formatControl.getFormat());
                if (format != null && format.getMediaType().equals(mediaType)) {
                    return format;
                }
            }
        }
        return null;
    }

    public MediaType getMediaType() {
        return this.mediaType;
    }

    public List<MediaFormat> getSupportedFormats(EncodingConfiguration encodingConfiguration) {
        return getSupportedFormats(null, null, encodingConfiguration);
    }

    public List<MediaFormat> getSupportedFormats(QualityPreset sendPreset, QualityPreset receivePreset) {
        return getSupportedFormats(sendPreset, receivePreset, NeomediaServiceUtils.getMediaServiceImpl().getCurrentEncodingConfiguration());
    }

    public List<MediaFormat> getSupportedFormats(QualityPreset sendPreset, QualityPreset receivePreset, EncodingConfiguration encodingConfiguration) {
        MediaServiceImpl mediaServiceImpl = NeomediaServiceUtils.getMediaServiceImpl();
        MediaFormat[] enabledEncodings = encodingConfiguration.getEnabledEncodings(getMediaType());
        List<MediaFormat> supportedFormats = new ArrayList();
        if (enabledEncodings != null) {
            for (MediaFormat f : enabledEncodings) {
                MediaFormat f2;
                if (Constants.H264.equalsIgnoreCase(f2.getEncoding())) {
                    Dimension receiveSize;
                    Map<String, String> h264AdvancedAttributes = f2.getAdvancedAttributes();
                    if (h264AdvancedAttributes == null) {
                        h264AdvancedAttributes = new HashMap();
                    }
                    CaptureDeviceInfo captureDeviceInfo = getCaptureDeviceInfo();
                    Dimension sendSize = null;
                    if (captureDeviceInfo != null) {
                        MediaLocator captureDeviceInfoLocator = captureDeviceInfo.getLocator();
                        if (!(captureDeviceInfoLocator == null || DeviceSystem.LOCATOR_PROTOCOL_IMGSTREAMING.equals(captureDeviceInfoLocator.getProtocol()) || sendPreset == null)) {
                            sendSize = sendPreset.getResolution();
                        }
                    }
                    if (receivePreset != null) {
                        receiveSize = receivePreset.getResolution();
                    } else {
                        ScreenDevice screen = mediaServiceImpl.getDefaultScreenDevice();
                        receiveSize = screen == null ? null : screen.getSize();
                    }
                    h264AdvancedAttributes.put("imageattr", MediaUtils.createImageAttr(sendSize, receiveSize));
                    f2 = mediaServiceImpl.getFormatFactory().createMediaFormat(f2.getEncoding(), f2.getClockRate(), f2.getFormatParameters(), h264AdvancedAttributes);
                }
                if (f2 != null) {
                    supportedFormats.add(f2);
                }
            }
        }
        return supportedFormats;
    }

    public String toString() {
        CaptureDeviceInfo captureDeviceInfo = getCaptureDeviceInfo();
        return captureDeviceInfo == null ? super.toString() : captureDeviceInfo.toString();
    }
}
