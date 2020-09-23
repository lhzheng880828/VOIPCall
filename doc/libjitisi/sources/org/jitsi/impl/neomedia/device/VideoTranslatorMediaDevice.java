package org.jitsi.impl.neomedia.device;

import java.util.LinkedList;
import java.util.List;
import javax.media.Format;
import javax.media.Player;
import javax.media.Processor;
import javax.media.protocol.DataSource;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.impl.neomedia.AbstractRTPConnector;
import org.jitsi.impl.neomedia.format.MediaFormatImpl;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.QualityPreset;
import org.jitsi.service.neomedia.codec.EncodingConfiguration;
import org.jitsi.service.neomedia.device.MediaDevice;
import org.jitsi.service.neomedia.device.MediaDeviceWrapper;
import org.jitsi.service.neomedia.format.MediaFormat;
import org.jitsi.util.event.VideoEvent;
import org.jitsi.util.event.VideoListener;

public class VideoTranslatorMediaDevice extends AbstractMediaDevice implements MediaDeviceWrapper, VideoListener {
    private final MediaDeviceImpl device;
    /* access modifiers changed from: private */
    public VideoMediaDeviceSession deviceSession;
    private final List<MediaStreamMediaDeviceSession> streamDeviceSessions = new LinkedList();

    private class MediaStreamMediaDeviceSession extends VideoMediaDeviceSession {
        public MediaStreamMediaDeviceSession() {
            super(VideoTranslatorMediaDevice.this);
        }

        public void close() {
            super.close();
            VideoTranslatorMediaDevice.this.close(this);
        }

        /* access modifiers changed from: protected */
        public DataSource createCaptureDevice() {
            return VideoTranslatorMediaDevice.this.createOutputDataSource();
        }

        /* access modifiers changed from: protected */
        public Player createLocalPlayer(DataSource captureDevice) {
            synchronized (VideoTranslatorMediaDevice.this) {
                if (VideoTranslatorMediaDevice.this.deviceSession != null) {
                    captureDevice = VideoTranslatorMediaDevice.this.deviceSession.getCaptureDevice();
                }
            }
            return super.createLocalPlayer(captureDevice);
        }

        /* access modifiers changed from: protected */
        public Processor createProcessor() {
            return null;
        }

        public DataSource getOutputDataSource() {
            return getConnectedCaptureDevice();
        }

        public void setConnector(AbstractRTPConnector rtpConnector) {
            super.setConnector(rtpConnector);
            if (VideoTranslatorMediaDevice.this.deviceSession != null) {
                VideoTranslatorMediaDevice.this.deviceSession.addRTCPFeedbackCreateListner(this);
            }
        }

        /* access modifiers changed from: protected */
        public void startedDirectionChanged(MediaDirection oldValue, MediaDirection newValue) {
            super.startedDirectionChanged(oldValue, newValue);
            VideoTranslatorMediaDevice.this.updateDeviceSessionStartedDirection();
        }

        public Component getLocalVisualComponent() {
            if (VideoTranslatorMediaDevice.this.deviceSession != null) {
                return VideoTranslatorMediaDevice.this.deviceSession.getLocalVisualComponent();
            }
            return null;
        }

        /* access modifiers changed from: protected */
        public Component createLocalVisualComponent() {
            if (VideoTranslatorMediaDevice.this.deviceSession != null) {
                return VideoTranslatorMediaDevice.this.deviceSession.createLocalVisualComponent();
            }
            return null;
        }

        /* access modifiers changed from: protected */
        public Player getLocalPlayer() {
            if (VideoTranslatorMediaDevice.this.deviceSession != null) {
                return VideoTranslatorMediaDevice.this.deviceSession.getLocalPlayer();
            }
            return null;
        }

        /* access modifiers changed from: protected */
        public void disposeLocalPlayer(Player player) {
        }
    }

    public VideoTranslatorMediaDevice(MediaDeviceImpl device) {
        this.device = device;
    }

    /* access modifiers changed from: private|declared_synchronized */
    public synchronized void close(MediaStreamMediaDeviceSession streamDeviceSession) {
        this.streamDeviceSessions.remove(streamDeviceSession);
        if (this.deviceSession != null) {
            this.deviceSession.removeRTCPFeedbackCreateListner(streamDeviceSession);
        }
        if (this.streamDeviceSessions.isEmpty()) {
            if (this.deviceSession != null) {
                this.deviceSession.removeVideoListener(this);
                this.deviceSession.close();
            }
            this.deviceSession = null;
        } else {
            updateDeviceSessionStartedDirection();
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized DataSource createOutputDataSource() {
        if (this.deviceSession == null) {
            MediaFormatImpl<? extends Format> format = null;
            MediaDirection startedDirection = MediaDirection.INACTIVE;
            for (MediaStreamMediaDeviceSession streamDeviceSession : this.streamDeviceSessions) {
                MediaFormatImpl<? extends Format> streamFormat = streamDeviceSession.getFormat();
                if (streamFormat != null && format == null) {
                    format = streamFormat;
                }
                startedDirection = startedDirection.or(streamDeviceSession.getStartedDirection());
            }
            MediaDeviceSession newDeviceSession = this.device.createSession();
            if (newDeviceSession instanceof VideoMediaDeviceSession) {
                this.deviceSession = (VideoMediaDeviceSession) newDeviceSession;
                this.deviceSession.addVideoListener(this);
                for (MediaStreamMediaDeviceSession streamDeviceSession2 : this.streamDeviceSessions) {
                    this.deviceSession.addRTCPFeedbackCreateListner(streamDeviceSession2);
                }
            }
            if (format != null) {
                this.deviceSession.setFormat(format);
            }
            this.deviceSession.start(startedDirection);
        }
        return this.deviceSession == null ? null : this.deviceSession.getOutputDataSource();
    }

    public synchronized MediaDeviceSession createSession() {
        MediaStreamMediaDeviceSession streamDeviceSession;
        streamDeviceSession = new MediaStreamMediaDeviceSession();
        this.streamDeviceSessions.add(streamDeviceSession);
        return streamDeviceSession;
    }

    public MediaDirection getDirection() {
        return this.device.getDirection();
    }

    public MediaFormat getFormat() {
        return this.device.getFormat();
    }

    public MediaType getMediaType() {
        return this.device.getMediaType();
    }

    public List<MediaFormat> getSupportedFormats(QualityPreset localPreset, QualityPreset remotePreset) {
        return this.device.getSupportedFormats(localPreset, remotePreset);
    }

    public List<MediaFormat> getSupportedFormats(QualityPreset localPreset, QualityPreset remotePreset, EncodingConfiguration encodingConfiguration) {
        return this.device.getSupportedFormats(localPreset, remotePreset, encodingConfiguration);
    }

    public MediaDevice getWrappedDevice() {
        return this.device;
    }

    /* access modifiers changed from: private|declared_synchronized */
    public synchronized void updateDeviceSessionStartedDirection() {
        if (this.deviceSession != null) {
            MediaDirection startDirection = MediaDirection.INACTIVE;
            for (MediaStreamMediaDeviceSession streamDeviceSession : this.streamDeviceSessions) {
                startDirection = startDirection.or(streamDeviceSession.getStartedDirection());
            }
            this.deviceSession.start(startDirection);
            MediaDirection stopDirection = MediaDirection.INACTIVE;
            if (!startDirection.allowsReceiving()) {
                stopDirection = stopDirection.or(MediaDirection.RECVONLY);
            }
            if (!startDirection.allowsSending()) {
                stopDirection = stopDirection.or(MediaDirection.SENDONLY);
            }
            this.deviceSession.stop(stopDirection);
        }
    }

    public void videoAdded(VideoEvent event) {
        for (MediaStreamMediaDeviceSession sds : this.streamDeviceSessions) {
            sds.fireVideoEvent(event, false);
        }
    }

    public void videoRemoved(VideoEvent event) {
        for (MediaStreamMediaDeviceSession sds : this.streamDeviceSessions) {
            sds.fireVideoEvent(event, false);
        }
    }

    public void videoUpdate(VideoEvent event) {
        for (MediaStreamMediaDeviceSession sds : this.streamDeviceSessions) {
            sds.fireVideoEvent(event, false);
        }
    }
}
