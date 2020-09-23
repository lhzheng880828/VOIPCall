package org.jitsi.impl.neomedia;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.media.control.BufferControl;
import javax.media.format.AudioFormat;
import org.jitsi.impl.neomedia.device.AudioMediaDeviceSession;
import org.jitsi.impl.neomedia.device.MediaDeviceSession;
import org.jitsi.impl.neomedia.transform.csrc.CsrcTransformEngine;
import org.jitsi.impl.neomedia.transform.csrc.SsrcTransformEngine;
import org.jitsi.impl.neomedia.transform.dtmf.DtmfTransformEngine;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.AudioMediaStream;
import org.jitsi.service.neomedia.DTMFInbandTone;
import org.jitsi.service.neomedia.DTMFMethod;
import org.jitsi.service.neomedia.DTMFRtpTone;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.MediaService;
import org.jitsi.service.neomedia.RTPExtension;
import org.jitsi.service.neomedia.SrtpControl;
import org.jitsi.service.neomedia.StreamConnector;
import org.jitsi.service.neomedia.VolumeControl;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.service.neomedia.device.MediaDevice;
import org.jitsi.service.neomedia.event.CsrcAudioLevelListener;
import org.jitsi.service.neomedia.event.DTMFListener;
import org.jitsi.service.neomedia.event.DTMFToneEvent;
import org.jitsi.service.neomedia.event.SimpleAudioLevelListener;
import org.jitsi.service.protocol.DTMFTone;
import org.jitsi.util.Logger;
import org.jitsi.util.event.PropertyChangeNotifier;

public class AudioMediaStreamImpl extends MediaStreamImpl implements AudioMediaStream, PropertyChangeListener {
    private static final AudioFormat[] CUSTOM_CODEC_FORMATS = new AudioFormat[]{new AudioFormat("ALAW/rtp", 8000.0d, 8, 1, -1, 1), new AudioFormat(Constants.G722_RTP, 8000.0d, -1, 1)};
    private static final Logger logger = Logger.getLogger(AudioMediaStreamImpl.class);
    private final PropertyChangeNotifier audioSystemChangeNotifier;
    private CsrcAudioLevelListener csrcAudioLevelListener;
    private final List<DTMFListener> dtmfListeners = new ArrayList();
    private DtmfTransformEngine dtmfTransformEngine;
    private SimpleAudioLevelListener localUserAudioLevelListener;
    private VolumeControl outputVolumeControl;
    private SsrcTransformEngine ssrcTransformEngine;
    private SimpleAudioLevelListener streamAudioLevelListener;

    public AudioMediaStreamImpl(StreamConnector connector, MediaDevice device, SrtpControl srtpControl) {
        super(connector, device, srtpControl);
        MediaService mediaService = LibJitsi.getMediaService();
        if (mediaService instanceof PropertyChangeNotifier) {
            this.audioSystemChangeNotifier = (PropertyChangeNotifier) mediaService;
            this.audioSystemChangeNotifier.addPropertyChangeListener(this);
            return;
        }
        this.audioSystemChangeNotifier = null;
    }

    public void addDTMFListener(DTMFListener listener) {
        if (listener != null && !this.dtmfListeners.contains(listener)) {
            this.dtmfListeners.add(listener);
        }
    }

    public void addRTPExtension(byte extensionID, RTPExtension rtpExtension) {
        byte b = (byte) -1;
        super.addRTPExtension(extensionID, rtpExtension);
        CsrcTransformEngine csrcEngine = getCsrcEngine();
        SsrcTransformEngine ssrcEngine = this.ssrcTransformEngine;
        if (csrcEngine != null || ssrcEngine != null) {
            Map<Byte, RTPExtension> activeRTPExtensions = getActiveRTPExtensions();
            Byte csrcExtID = null;
            MediaDirection csrcDir = MediaDirection.INACTIVE;
            Byte ssrcExtID = null;
            MediaDirection ssrcDir = MediaDirection.INACTIVE;
            if (!(activeRTPExtensions == null || activeRTPExtensions.isEmpty())) {
                for (Entry<Byte, RTPExtension> e : activeRTPExtensions.entrySet()) {
                    RTPExtension ext = (RTPExtension) e.getValue();
                    String uri = ext.getURI().toString();
                    if (RTPExtension.CSRC_AUDIO_LEVEL_URN.equals(uri)) {
                        csrcExtID = (Byte) e.getKey();
                        csrcDir = ext.getDirection();
                    } else if (RTPExtension.SSRC_AUDIO_LEVEL_URN.equals(uri)) {
                        ssrcExtID = (Byte) e.getKey();
                        ssrcDir = ext.getDirection();
                    }
                }
            }
            if (csrcEngine != null) {
                csrcEngine.setCsrcAudioLevelExtensionID(csrcExtID == null ? (byte) -1 : csrcExtID.byteValue(), csrcDir);
            }
            if (ssrcEngine != null) {
                if (ssrcExtID != null) {
                    b = ssrcExtID.byteValue();
                }
                ssrcEngine.setSsrcAudioLevelExtensionID(b, ssrcDir);
            }
        }
    }

    public void audioLevelsReceived(long[] audioLevels) {
        CsrcAudioLevelListener csrcAudioLevelListener = this.csrcAudioLevelListener;
        if (csrcAudioLevelListener != null) {
            csrcAudioLevelListener.audioLevelsReceived(audioLevels);
        }
    }

    public void close() {
        super.close();
        if (this.dtmfTransformEngine != null) {
            this.dtmfTransformEngine.close();
            this.dtmfTransformEngine = null;
        }
        if (this.ssrcTransformEngine != null) {
            this.ssrcTransformEngine.close();
            this.ssrcTransformEngine = null;
        }
        if (this.audioSystemChangeNotifier != null) {
            this.audioSystemChangeNotifier.removePropertyChangeListener(this);
        }
    }

    /* access modifiers changed from: protected */
    public void configureRTPManagerBufferControl(StreamRTPManager rtpManager, BufferControl bufferControl) {
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        long bufferLength = 120;
        if (cfg != null) {
            String bufferLengthStr = cfg.getString("net.java.sip.communicator.impl.neomedia.RECEIVE_BUFFER_LENGTH");
            if (bufferLengthStr != null) {
                try {
                    if (bufferLengthStr.length() > 0) {
                        bufferLength = Long.parseLong(bufferLengthStr);
                    }
                } catch (NumberFormatException nfe) {
                    logger.warn(bufferLengthStr + " is not a valid receive buffer length/long value", nfe);
                }
            }
        }
        bufferLength = bufferControl.setBufferLength(bufferLength);
        if (logger.isTraceEnabled()) {
            logger.trace("Set receiver buffer length to " + bufferLength);
        }
        long minimumThreshold = bufferLength / 2;
        bufferControl.setEnabledThreshold(minimumThreshold > 0);
        bufferControl.setMinimumThreshold(minimumThreshold);
    }

    /* access modifiers changed from: protected */
    public DtmfTransformEngine createDtmfTransformEngine() {
        if (this.dtmfTransformEngine == null) {
            this.dtmfTransformEngine = new DtmfTransformEngine(this);
        }
        return this.dtmfTransformEngine;
    }

    /* access modifiers changed from: protected */
    public SsrcTransformEngine createSsrcTransformEngine() {
        if (this.ssrcTransformEngine == null) {
            this.ssrcTransformEngine = new SsrcTransformEngine(this);
        }
        return this.ssrcTransformEngine;
    }

    /* access modifiers changed from: protected */
    public void deviceSessionChanged(MediaDeviceSession oldValue, MediaDeviceSession newValue) {
        AudioMediaDeviceSession deviceSession;
        if (oldValue != null) {
            try {
                deviceSession = (AudioMediaDeviceSession) oldValue;
                if (this.localUserAudioLevelListener != null) {
                    deviceSession.setLocalUserAudioLevelListener(null);
                }
                if (this.streamAudioLevelListener != null) {
                    deviceSession.setStreamAudioLevelListener(null);
                }
            } catch (Throwable th) {
                super.deviceSessionChanged(oldValue, newValue);
            }
        }
        if (newValue != null) {
            deviceSession = (AudioMediaDeviceSession) newValue;
            if (this.localUserAudioLevelListener != null) {
                deviceSession.setLocalUserAudioLevelListener(this.localUserAudioLevelListener);
            }
            if (this.streamAudioLevelListener != null) {
                deviceSession.setStreamAudioLevelListener(this.streamAudioLevelListener);
            }
            if (this.outputVolumeControl != null) {
                deviceSession.setOutputVolumeControl(this.outputVolumeControl);
            }
        }
        super.deviceSessionChanged(oldValue, newValue);
    }

    public void fireDTMFEvent(DTMFRtpTone tone, boolean end) {
        DTMFToneEvent ev = new DTMFToneEvent(this, tone);
        for (DTMFListener listener : this.dtmfListeners) {
            if (end) {
                listener.dtmfToneReceptionEnded(ev);
            } else {
                listener.dtmfToneReceptionStarted(ev);
            }
        }
    }

    public AudioMediaDeviceSession getDeviceSession() {
        return (AudioMediaDeviceSession) super.getDeviceSession();
    }

    public int getLastMeasuredAudioLevel(long ssrc) {
        AudioMediaDeviceSession devSession = getDeviceSession();
        if (devSession == null) {
            return -1;
        }
        if (ssrc == getLocalSourceID()) {
            return devSession.getLastMeasuredLocalUserAudioLevel();
        }
        return devSession.getLastMeasuredAudioLevel(ssrc);
    }

    /* access modifiers changed from: protected */
    public int getPriority() {
        return 3;
    }

    public void propertyChange(PropertyChangeEvent ev) {
        if (this.sendStreamsAreCreated) {
            recreateSendStreams();
        } else {
            start();
        }
    }

    /* access modifiers changed from: protected */
    public void registerCustomCodecFormats(StreamRTPManager rtpManager) {
        super.registerCustomCodecFormats(rtpManager);
        for (AudioFormat format : CUSTOM_CODEC_FORMATS) {
            if (logger.isDebugEnabled()) {
                logger.debug("registering format " + format + " with RTP manager");
            }
            rtpManager.addFormat(format, MediaUtils.getRTPPayloadType(format.getEncoding(), format.getSampleRate()));
        }
    }

    public void removeDTMFListener(DTMFListener listener) {
        this.dtmfListeners.remove(listener);
    }

    public void setCsrcAudioLevelListener(CsrcAudioLevelListener listener) {
        this.csrcAudioLevelListener = listener;
    }

    public void setLocalUserAudioLevelListener(SimpleAudioLevelListener listener) {
        if (this.localUserAudioLevelListener != listener) {
            this.localUserAudioLevelListener = listener;
            AudioMediaDeviceSession deviceSession = getDeviceSession();
            if (deviceSession != null) {
                deviceSession.setLocalUserAudioLevelListener(this.localUserAudioLevelListener);
            }
        }
    }

    public void setOutputVolumeControl(VolumeControl outputVolumeControl) {
        if (this.outputVolumeControl != outputVolumeControl) {
            this.outputVolumeControl = outputVolumeControl;
            AudioMediaDeviceSession deviceSession = getDeviceSession();
            if (deviceSession != null) {
                deviceSession.setOutputVolumeControl(this.outputVolumeControl);
            }
        }
    }

    public void setStreamAudioLevelListener(SimpleAudioLevelListener listener) {
        if (this.streamAudioLevelListener != listener) {
            this.streamAudioLevelListener = listener;
            AudioMediaDeviceSession deviceSession = getDeviceSession();
            if (deviceSession != null) {
                deviceSession.setStreamAudioLevelListener(this.streamAudioLevelListener);
            }
        }
    }

    public void startSendingDTMF(DTMFTone tone, DTMFMethod dtmfMethod, int minimalToneDuration, int maximalToneDuration, int volume) {
        switch (dtmfMethod) {
            case INBAND_DTMF:
                MediaDeviceSession deviceSession = getDeviceSession();
                if (deviceSession != null) {
                    deviceSession.addDTMF(DTMFInbandTone.mapTone(tone));
                    return;
                }
                return;
            case RTP_DTMF:
                if (this.dtmfTransformEngine != null) {
                    DTMFRtpTone t = DTMFRtpTone.mapTone(tone);
                    if (t != null) {
                        this.dtmfTransformEngine.startSending(t, minimalToneDuration, maximalToneDuration, volume);
                        return;
                    }
                    return;
                }
                return;
            case SIP_INFO_DTMF:
                return;
            default:
                throw new IllegalArgumentException("dtmfMethod");
        }
    }

    public void stopSendingDTMF(DTMFMethod dtmfMethod) {
        switch (dtmfMethod) {
            case INBAND_DTMF:
            case SIP_INFO_DTMF:
                return;
            case RTP_DTMF:
                if (this.dtmfTransformEngine != null) {
                    this.dtmfTransformEngine.stopSendingDTMF();
                    return;
                }
                return;
            default:
                throw new IllegalArgumentException("dtmfMethod");
        }
    }
}
