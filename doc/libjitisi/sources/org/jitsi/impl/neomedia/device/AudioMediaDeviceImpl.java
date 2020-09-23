package org.jitsi.impl.neomedia.device;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.media.CaptureDeviceInfo;
import javax.media.Format;
import javax.media.Renderer;
import javax.media.control.BufferControl;
import javax.media.control.FormatControl;
import javax.media.format.AudioFormat;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;
import org.jitsi.impl.neomedia.MediaUtils;
import org.jitsi.impl.neomedia.conference.AudioMixer;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.RTPExtension;
import org.jitsi.util.Logger;
import org.jitsi.util.OSUtils;

public class AudioMediaDeviceImpl extends MediaDeviceImpl {
    private static final Logger logger = Logger.getLogger(AudioMediaDeviceImpl.class);
    private AudioMixer captureDeviceSharing;
    private List<RTPExtension> rtpExtensions = null;

    public AudioMediaDeviceImpl() {
        super(MediaType.AUDIO);
    }

    public AudioMediaDeviceImpl(CaptureDeviceInfo captureDeviceInfo) {
        super(captureDeviceInfo, MediaType.AUDIO);
    }

    public void connect(DataSource captureDevice) throws IOException {
        super.connect(captureDevice);
        if (!OSUtils.IS_LINUX) {
            BufferControl bufferControl = (BufferControl) captureDevice.getControl(BufferControl.class.getName());
            if (bufferControl != null) {
                bufferControl.setBufferLength(60);
            }
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized CaptureDevice createCaptureDevice() {
        CaptureDevice captureDevice;
        captureDevice = null;
        if (getDirection().allowsSending()) {
            if (this.captureDeviceSharing == null) {
                String protocol = getCaptureDeviceInfoLocatorProtocol();
                boolean createCaptureDeviceIfNull = true;
                if (AudioSystem.LOCATOR_PROTOCOL_JAVASOUND.equalsIgnoreCase(protocol) || AudioSystem.LOCATOR_PROTOCOL_PORTAUDIO.equalsIgnoreCase(protocol)) {
                    captureDevice = superCreateCaptureDevice();
                    createCaptureDeviceIfNull = false;
                    if (captureDevice != null) {
                        this.captureDeviceSharing = createCaptureDeviceSharing(captureDevice);
                        captureDevice = this.captureDeviceSharing.createOutDataSource();
                    }
                }
                if (captureDevice == null && createCaptureDeviceIfNull) {
                    captureDevice = superCreateCaptureDevice();
                }
            } else {
                captureDevice = this.captureDeviceSharing.createOutDataSource();
            }
        }
        return captureDevice;
    }

    private AudioMixer createCaptureDeviceSharing(CaptureDevice captureDevice) {
        return new AudioMixer(captureDevice) {
            /* access modifiers changed from: protected */
            public void connect(DataSource dataSource, DataSource inputDataSource) throws IOException {
                if (inputDataSource == this.captureDevice) {
                    AudioMediaDeviceImpl.this.connect(dataSource);
                } else {
                    super.connect(dataSource, inputDataSource);
                }
            }
        };
    }

    /* access modifiers changed from: protected */
    public Renderer createRenderer() {
        Renderer renderer = null;
        try {
            String locatorProtocol = getCaptureDeviceInfoLocatorProtocol();
            if (locatorProtocol != null) {
                AudioSystem audioSystem = AudioSystem.getAudioSystem(locatorProtocol);
                if (audioSystem != null) {
                    renderer = audioSystem.createRenderer(true);
                }
            }
            if (renderer == null) {
                return super.createRenderer();
            }
            return renderer;
        } catch (Throwable th) {
            if (null == null) {
                renderer = super.createRenderer();
            }
        }
    }

    public List<RTPExtension> getSupportedExtensions() {
        if (this.rtpExtensions == null) {
            URI csrcAudioLevelURN;
            this.rtpExtensions = new ArrayList(1);
            try {
                csrcAudioLevelURN = new URI(RTPExtension.CSRC_AUDIO_LEVEL_URN);
            } catch (URISyntaxException e) {
                if (logger.isInfoEnabled()) {
                    logger.info("Aha! Someone messed with the source!", e);
                }
                csrcAudioLevelURN = null;
            }
            if (csrcAudioLevelURN != null) {
                this.rtpExtensions.add(new RTPExtension(csrcAudioLevelURN, MediaDirection.RECVONLY));
            }
        }
        return this.rtpExtensions;
    }

    private boolean isLessThanOrEqualToMaxAudioFormat(Format format) {
        if (format instanceof AudioFormat) {
            AudioFormat audioFormat = (AudioFormat) format;
            int channels = audioFormat.getChannels();
            if (channels == -1 || MediaUtils.MAX_AUDIO_CHANNELS == -1 || channels <= MediaUtils.MAX_AUDIO_CHANNELS) {
                double sampleRate = audioFormat.getSampleRate();
                if (sampleRate == -1.0d || MediaUtils.MAX_AUDIO_SAMPLE_RATE == -1.0d || sampleRate <= MediaUtils.MAX_AUDIO_SAMPLE_RATE) {
                    int sampleSizeInBits = audioFormat.getSampleSizeInBits();
                    if (sampleSizeInBits == -1 || MediaUtils.MAX_AUDIO_SAMPLE_SIZE_IN_BITS == -1 || sampleSizeInBits <= MediaUtils.MAX_AUDIO_SAMPLE_SIZE_IN_BITS) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public CaptureDevice superCreateCaptureDevice() {
        CaptureDevice captureDevice = super.createCaptureDevice();
        if (captureDevice != null) {
            try {
                FormatControl[] formatControls = captureDevice.getFormatControls();
                if (!(formatControls == null || formatControls.length == 0)) {
                    for (FormatControl formatControl : formatControls) {
                        Format format = formatControl.getFormat();
                        if (format == null || !isLessThanOrEqualToMaxAudioFormat(format)) {
                            Format[] supportedFormats = formatControl.getSupportedFormats();
                            AudioFormat supportedFormatToSet = null;
                            if (supportedFormats != null && supportedFormats.length != 0) {
                                for (Format supportedFormat : supportedFormats) {
                                    if (isLessThanOrEqualToMaxAudioFormat(supportedFormat)) {
                                        supportedFormatToSet = (AudioFormat) supportedFormat;
                                        break;
                                    }
                                }
                            }
                            if (!supportedFormatToSet.matches(format)) {
                                int channels = supportedFormatToSet.getChannels();
                                double sampleRate = supportedFormatToSet.getSampleRate();
                                int sampleSizeInBits = supportedFormatToSet.getSampleSizeInBits();
                                if (channels == -1) {
                                    channels = MediaUtils.MAX_AUDIO_CHANNELS;
                                }
                                if (sampleRate == -1.0d) {
                                    sampleRate = MediaUtils.MAX_AUDIO_SAMPLE_RATE;
                                }
                                if (sampleSizeInBits == -1) {
                                    sampleSizeInBits = MediaUtils.MAX_AUDIO_SAMPLE_SIZE_IN_BITS;
                                    if (sampleSizeInBits == -1) {
                                        sampleSizeInBits = 16;
                                    }
                                }
                                if (!(channels == -1 || sampleRate == -1.0d || sampleSizeInBits == -1)) {
                                    AudioFormat formatToSet = new AudioFormat(supportedFormatToSet.getEncoding(), sampleRate, sampleSizeInBits, channels);
                                    if (supportedFormatToSet.matches(formatToSet)) {
                                        formatControl.setFormat(supportedFormatToSet.intersects(formatToSet));
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    ThreadDeath t2 = (ThreadDeath) t;
                }
            }
        }
        return captureDevice;
    }
}
