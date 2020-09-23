package org.jitsi.impl.neomedia.device;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;
import javax.media.PlugInManager;
import javax.media.Renderer;
import javax.media.format.VideoFormat;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.MediaServiceImpl;
import org.jitsi.impl.neomedia.codec.video.AVFrameFormat;
import org.jitsi.impl.neomedia.device.AudioSystem.DataFlow;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.MediaUseCase;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.util.Logger;
import org.jitsi.util.OSUtils;
import org.jitsi.util.event.PropertyChangeNotifier;

public class DeviceConfiguration extends PropertyChangeNotifier implements PropertyChangeListener {
    public static final String AUDIO_CAPTURE_DEVICE = "captureDevice";
    public static final String AUDIO_NOTIFY_DEVICE = "notifyDevice";
    public static final String AUDIO_PLAYBACK_DEVICE = "playbackDevice";
    private static final String[] CUSTOM_RENDERERS;
    public static final boolean DEFAULT_AUDIO_DENOISE = true;
    public static final boolean DEFAULT_AUDIO_ECHOCANCEL = true;
    public static final long DEFAULT_AUDIO_ECHOCANCEL_FILTER_LENGTH_IN_MILLIS = 100;
    public static final int DEFAULT_VIDEO_BITRATE = 128;
    public static final int DEFAULT_VIDEO_FRAMERATE = -1;
    public static final int DEFAULT_VIDEO_HEIGHT = 480;
    public static final int DEFAULT_VIDEO_RTP_PACING_THRESHOLD = 256;
    public static final int DEFAULT_VIDEO_WIDTH = 640;
    static final String PROP_AUDIO_ECHOCANCEL_FILTER_LENGTH_IN_MILLIS = "net.java.sip.communicator.impl.neomedia.echocancel.filterLengthInMillis";
    public static final String PROP_AUDIO_SYSTEM = "net.java.sip.communicator.impl.neomedia.audioSystem";
    public static final String PROP_AUDIO_SYSTEM_DEVICES = "net.java.sip.communicator.impl.neomedia.audioSystem.devices";
    private static final String PROP_VIDEO_BITRATE = "net.java.sip.communicator.impl.neomedia.video.bitrate";
    private static final String PROP_VIDEO_DEVICE = "net.java.sip.communicator.impl.neomedia.videoDevice";
    private static final String PROP_VIDEO_FRAMERATE = "net.java.sip.communicator.impl.neomedia.video.framerate";
    private static final String PROP_VIDEO_HEIGHT = "net.java.sip.communicator.impl.neomedia.video.height";
    public static final String PROP_VIDEO_RTP_PACING_THRESHOLD = "net.java.sip.communicator.impl.neomedia.video.maxbandwidth";
    private static final String PROP_VIDEO_WIDTH = "net.java.sip.communicator.impl.neomedia.video.width";
    public static final Dimension[] SUPPORTED_RESOLUTIONS = new Dimension[]{new Dimension(160, 100), new Dimension(176, 144), new Dimension(320, 200), new Dimension(320, 240), new Dimension(352, 288), new Dimension(DEFAULT_VIDEO_WIDTH, DEFAULT_VIDEO_HEIGHT), new Dimension(1280, 720)};
    public static final String VIDEO_CAPTURE_DEVICE = "VIDEO_CAPTURE_DEVICE";
    private AudioSystem audioSystem;
    private int frameRate = -1;
    private final Logger logger = Logger.getLogger(DeviceConfiguration.class);
    private final boolean setAudioSystemIsDisabled;
    private int videoBitrate = -1;
    private CaptureDeviceInfo videoCaptureDevice;
    private int videoMaxBandwidth = -1;
    private Dimension videoSize;

    static {
        String str;
        String str2 = null;
        String[] strArr = new String[7];
        if (OSUtils.IS_ANDROID) {
            str = ".audio.AudioTrackRenderer";
        } else {
            str = null;
        }
        strArr[0] = str;
        if (OSUtils.IS_ANDROID) {
            str = ".audio.OpenSLESRenderer";
        } else {
            str = null;
        }
        strArr[1] = str;
        if (OSUtils.IS_LINUX) {
            str = ".audio.PulseAudioRenderer";
        } else {
            str = null;
        }
        strArr[2] = str;
        if (OSUtils.IS_WINDOWS) {
            str = ".audio.WASAPIRenderer";
        } else {
            str = null;
        }
        strArr[3] = str;
        if (OSUtils.IS_ANDROID) {
            str = null;
        } else {
            str = ".audio.PortAudioRenderer";
        }
        strArr[4] = str;
        if (OSUtils.IS_ANDROID) {
            str2 = ".video.SurfaceRenderer";
        }
        strArr[5] = str2;
        strArr[6] = ".video.JAWTRenderer";
        CUSTOM_RENDERERS = strArr;
    }

    private static void fixRenderers() {
        Vector<String> renderers = PlugInManager.getPlugInList(null, null, 4);
        PlugInManager.removePlugIn("com.sun.media.renderer.audio.JavaSoundRenderer", 4);
        if (OSUtils.IS_WINDOWS) {
            if (OSUtils.IS_WINDOWS32 && (OSUtils.IS_WINDOWS_VISTA || OSUtils.IS_WINDOWS_7)) {
                if (renderers.contains("com.sun.media.renderer.video.GDIRenderer")) {
                    PlugInManager.removePlugIn("com.sun.media.renderer.video.DDRenderer", 4);
                }
            } else if (OSUtils.IS_WINDOWS64) {
                PlugInManager.removePlugIn("com.sun.media.renderer.video.GDIRenderer", 4);
                PlugInManager.removePlugIn("com.sun.media.renderer.video.DDRenderer", 4);
            }
        } else if (!OSUtils.IS_LINUX32) {
            if (renderers.contains("com.sun.media.renderer.video.LightWeightRenderer") || renderers.contains("com.sun.media.renderer.video.AWTRenderer")) {
                PlugInManager.removePlugIn("com.sun.media.renderer.video.XLibRenderer", 4);
            }
        }
    }

    public DeviceConfiguration() {
        boolean z = false;
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        if (cfg != null && cfg.getBoolean(MediaServiceImpl.DISABLE_SET_AUDIO_SYSTEM_PNAME, false)) {
            z = true;
        }
        this.setAudioSystemIsDisabled = z;
        try {
            DeviceSystem.initializeDeviceSystems();
            extractConfiguredCaptureDevices();
        } catch (Exception ex) {
            this.logger.error("Failed to initialize media.", ex);
        }
        if (cfg != null) {
            cfg.addPropertyChangeListener(PROP_VIDEO_HEIGHT, this);
            cfg.addPropertyChangeListener(PROP_VIDEO_WIDTH, this);
            cfg.addPropertyChangeListener(PROP_VIDEO_FRAMERATE, this);
            cfg.addPropertyChangeListener(PROP_VIDEO_RTP_PACING_THRESHOLD, this);
        }
        registerCustomRenderers();
        fixRenderers();
        addDeviceSystemPropertyChangeListener();
    }

    private void addDeviceSystemPropertyChangeListener() {
        for (MediaType mediaType : MediaType.values()) {
            DeviceSystem[] deviceSystems = DeviceSystem.getDeviceSystems(mediaType);
            if (deviceSystems != null) {
                for (DeviceSystem deviceSystem : deviceSystems) {
                    if ((deviceSystem.getFeatures() & 1) != 0) {
                        deviceSystem.addPropertyChangeListener(this);
                    }
                }
            }
        }
    }

    private void extractConfiguredAudioCaptureDevices() {
        if (MediaServiceImpl.isMediaTypeSupportEnabled(MediaType.AUDIO)) {
            if (this.logger.isInfoEnabled()) {
                this.logger.info("Looking for configured audio devices.");
            }
            AudioSystem[] availableAudioSystems = getAvailableAudioSystems();
            if (availableAudioSystems != null && availableAudioSystems.length != 0) {
                AudioSystem audioSystem = getAudioSystem();
                if (!(audioSystem == null || this.setAudioSystemIsDisabled)) {
                    audioSystem = null;
                }
                if (audioSystem == null) {
                    ConfigurationService cfg = LibJitsi.getConfigurationService();
                    if (cfg != null) {
                        String locatorProtocol = cfg.getString(PROP_AUDIO_SYSTEM);
                        if (locatorProtocol != null) {
                            for (AudioSystem availableAudioSystem : availableAudioSystems) {
                                if (locatorProtocol.equalsIgnoreCase(availableAudioSystem.getLocatorProtocol())) {
                                    audioSystem = availableAudioSystem;
                                    break;
                                }
                            }
                            if (this.setAudioSystemIsDisabled && audioSystem == null) {
                                audioSystem = AudioSystem.getAudioSystem(locatorProtocol);
                            }
                        }
                    }
                    if (audioSystem == null) {
                        audioSystem = availableAudioSystems[0];
                    }
                    setAudioSystem(audioSystem, false);
                }
            }
        }
    }

    private void extractConfiguredCaptureDevices() {
        extractConfiguredAudioCaptureDevices();
        extractConfiguredVideoCaptureDevices();
    }

    private CaptureDeviceInfo extractConfiguredVideoCaptureDevice(Format format) {
        List<CaptureDeviceInfo> videoCaptureDevices = CaptureDeviceManager.getDeviceList(format);
        CaptureDeviceInfo videoCaptureDevice = null;
        if (videoCaptureDevices.size() > 0) {
            ConfigurationService cfg = LibJitsi.getConfigurationService();
            String videoDevName = cfg == null ? null : cfg.getString(PROP_VIDEO_DEVICE);
            if (videoDevName != null) {
                for (CaptureDeviceInfo captureDeviceInfo : videoCaptureDevices) {
                    if (videoDevName.equals(captureDeviceInfo.getName())) {
                        videoCaptureDevice = captureDeviceInfo;
                        break;
                    }
                }
            }
            videoCaptureDevice = (CaptureDeviceInfo) videoCaptureDevices.get(0);
            if (videoCaptureDevice != null && this.logger.isInfoEnabled()) {
                this.logger.info("Found " + videoCaptureDevice.getName() + " as a " + format + " video capture device.");
            }
        }
        return videoCaptureDevice;
    }

    private void extractConfiguredVideoCaptureDevices() {
        if (MediaServiceImpl.isMediaTypeSupportEnabled(MediaType.VIDEO)) {
            ConfigurationService cfg = LibJitsi.getConfigurationService();
            if (NoneAudioSystem.LOCATOR_PROTOCOL.equalsIgnoreCase(cfg == null ? null : cfg.getString(PROP_VIDEO_DEVICE))) {
                this.videoCaptureDevice = null;
                return;
            }
            if (this.logger.isInfoEnabled()) {
                this.logger.info("Scanning for configured Video Devices.");
            }
            for (Format format : new Format[]{new AVFrameFormat(), new VideoFormat(Constants.ANDROID_SURFACE), new VideoFormat(VideoFormat.RGB), new VideoFormat(VideoFormat.YUV), new VideoFormat(Constants.H264)}) {
                this.videoCaptureDevice = extractConfiguredVideoCaptureDevice(format);
                if (this.videoCaptureDevice != null) {
                    break;
                }
            }
            if (this.videoCaptureDevice == null && this.logger.isInfoEnabled()) {
                this.logger.info("No Video Device was found.");
            }
        }
    }

    public CaptureDeviceInfo2 getAudioCaptureDevice() {
        AudioSystem audioSystem = getAudioSystem();
        return audioSystem == null ? null : audioSystem.getSelectedDevice(DataFlow.CAPTURE);
    }

    public CaptureDeviceInfo getAudioNotifyDevice() {
        AudioSystem audioSystem = getAudioSystem();
        return audioSystem == null ? null : audioSystem.getSelectedDevice(DataFlow.NOTIFY);
    }

    public AudioSystem getAudioSystem() {
        return this.audioSystem;
    }

    public List<CaptureDeviceInfo2> getAvailableAudioCaptureDevices() {
        return this.audioSystem.getDevices(DataFlow.CAPTURE);
    }

    public AudioSystem[] getAvailableAudioSystems() {
        AudioSystem[] audioSystems = AudioSystem.getAudioSystems();
        if (audioSystems == null || audioSystems.length == 0) {
            return audioSystems;
        }
        List<AudioSystem> audioSystemsWithDevices = new ArrayList();
        for (AudioSystem audioSystem : audioSystems) {
            if (!NoneAudioSystem.LOCATOR_PROTOCOL.equalsIgnoreCase(audioSystem.getLocatorProtocol())) {
                List<CaptureDeviceInfo2> captureDevices = audioSystem.getDevices(DataFlow.CAPTURE);
                if (captureDevices == null || captureDevices.size() <= 0) {
                    if ((audioSystem.getFeatures() & 8) != 0) {
                        List<CaptureDeviceInfo2> notifyDevices = audioSystem.getDevices(DataFlow.NOTIFY);
                        if (notifyDevices == null || notifyDevices.size() <= 0) {
                            List<CaptureDeviceInfo2> playbackDevices = audioSystem.getDevices(DataFlow.PLAYBACK);
                            if (playbackDevices != null) {
                                if (playbackDevices.size() <= 0) {
                                }
                            }
                        }
                    }
                }
            }
            audioSystemsWithDevices.add(audioSystem);
        }
        int audioSystemsWithDevicesCount = audioSystemsWithDevices.size();
        return audioSystemsWithDevicesCount != audioSystems.length ? (AudioSystem[]) audioSystemsWithDevices.toArray(new AudioSystem[audioSystemsWithDevicesCount]) : audioSystems;
    }

    public List<CaptureDeviceInfo> getAvailableVideoCaptureDevices(MediaUseCase useCase) {
        Format[] formats = new Format[]{new AVFrameFormat(), new VideoFormat(Constants.ANDROID_SURFACE), new VideoFormat(VideoFormat.RGB), new VideoFormat(VideoFormat.YUV), new VideoFormat(Constants.H264)};
        Set<CaptureDeviceInfo> videoCaptureDevices = new HashSet();
        for (Format format : formats) {
            Vector<CaptureDeviceInfo> cdis = CaptureDeviceManager.getDeviceList(format);
            if (useCase != MediaUseCase.ANY) {
                Iterator i$ = cdis.iterator();
                while (i$.hasNext()) {
                    CaptureDeviceInfo cdi = (CaptureDeviceInfo) i$.next();
                    if ((DeviceSystem.LOCATOR_PROTOCOL_IMGSTREAMING.equalsIgnoreCase(cdi.getLocator().getProtocol()) ? MediaUseCase.DESKTOP : MediaUseCase.CALL).equals(useCase)) {
                        videoCaptureDevices.add(cdi);
                    }
                }
            } else {
                videoCaptureDevices.addAll(cdis);
            }
        }
        return new ArrayList(videoCaptureDevices);
    }

    public long getEchoCancelFilterLengthInMillis() {
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        if (cfg != null) {
            return cfg.getLong(PROP_AUDIO_ECHOCANCEL_FILTER_LENGTH_IN_MILLIS, 100);
        }
        return 100;
    }

    public int getFrameRate() {
        if (this.frameRate == -1) {
            ConfigurationService cfg = LibJitsi.getConfigurationService();
            int value = -1;
            if (cfg != null) {
                value = cfg.getInt(PROP_VIDEO_FRAMERATE, -1);
            }
            this.frameRate = value;
        }
        return this.frameRate;
    }

    public int getVideoBitrate() {
        if (this.videoBitrate == -1) {
            ConfigurationService cfg = LibJitsi.getConfigurationService();
            int value = 128;
            if (cfg != null) {
                value = cfg.getInt(PROP_VIDEO_BITRATE, 128);
            }
            if (value > 0) {
                this.videoBitrate = value;
            } else {
                this.videoBitrate = 128;
            }
        }
        return this.videoBitrate;
    }

    public CaptureDeviceInfo getVideoCaptureDevice(MediaUseCase useCase) {
        switch (useCase) {
            case ANY:
            case CALL:
                return this.videoCaptureDevice;
            case DESKTOP:
                List<CaptureDeviceInfo> devs = getAvailableVideoCaptureDevices(MediaUseCase.DESKTOP);
                if (devs.size() > 0) {
                    return (CaptureDeviceInfo) devs.get(0);
                }
                return null;
            default:
                return null;
        }
    }

    public int getVideoRTPPacingThreshold() {
        if (this.videoMaxBandwidth == -1) {
            ConfigurationService cfg = LibJitsi.getConfigurationService();
            int value = 256;
            if (cfg != null) {
                value = cfg.getInt(PROP_VIDEO_RTP_PACING_THRESHOLD, 256);
            }
            if (value > 0) {
                this.videoMaxBandwidth = value;
            } else {
                this.videoMaxBandwidth = 256;
            }
        }
        return this.videoMaxBandwidth;
    }

    public Dimension getVideoSize() {
        if (this.videoSize == null) {
            ConfigurationService cfg = LibJitsi.getConfigurationService();
            int height = DEFAULT_VIDEO_HEIGHT;
            int width = DEFAULT_VIDEO_WIDTH;
            if (cfg != null) {
                height = cfg.getInt(PROP_VIDEO_HEIGHT, DEFAULT_VIDEO_HEIGHT);
                width = cfg.getInt(PROP_VIDEO_WIDTH, DEFAULT_VIDEO_WIDTH);
            }
            this.videoSize = new Dimension(width, height);
        }
        return this.videoSize;
    }

    public void propertyChange(PropertyChangeEvent ev) {
        String propertyName = ev.getPropertyName();
        if ("captureDevice".equals(propertyName) || "notifyDevice".equals(propertyName) || "playbackDevice".equals(propertyName)) {
            extractConfiguredAudioCaptureDevices();
            AudioSystem audioSystem = getAudioSystem();
            if (audioSystem != null) {
                CaptureDeviceInfo device;
                CaptureDeviceInfo oldValue = (CaptureDeviceInfo) ev.getOldValue();
                CaptureDeviceInfo newValue = (CaptureDeviceInfo) ev.getNewValue();
                if (oldValue == null) {
                    device = newValue;
                } else {
                    device = oldValue;
                }
                if (device == null || device.getLocator().getProtocol().equals(audioSystem.getLocatorProtocol())) {
                    firePropertyChange(propertyName, oldValue, newValue);
                }
            }
        } else if (DeviceSystem.PROP_DEVICES.equals(propertyName)) {
            if (ev.getSource() instanceof AudioSystem) {
                extractConfiguredAudioCaptureDevices();
                firePropertyChange(PROP_AUDIO_SYSTEM_DEVICES, ev.getOldValue(), (List) ev.getNewValue());
            }
        } else if (PROP_VIDEO_FRAMERATE.equals(propertyName)) {
            this.frameRate = -1;
        } else if (PROP_VIDEO_HEIGHT.equals(propertyName) || PROP_VIDEO_WIDTH.equals(propertyName)) {
            this.videoSize = null;
        } else if (PROP_VIDEO_RTP_PACING_THRESHOLD.equals(propertyName)) {
            this.videoMaxBandwidth = -1;
        }
    }

    private void registerCustomRenderers() {
        Vector<String> renderers = PlugInManager.getPlugInList(null, null, 4);
        boolean audioSupportIsDisabled = !MediaServiceImpl.isMediaTypeSupportEnabled(MediaType.AUDIO);
        boolean videoSupportIsDisabled = !MediaServiceImpl.isMediaTypeSupportEnabled(MediaType.VIDEO);
        boolean commit = false;
        for (String customRenderer : CUSTOM_RENDERERS) {
            String customRenderer2;
            if (customRenderer2 != null) {
                if (customRenderer2.startsWith(".")) {
                    customRenderer2 = "org.jitsi.impl.neomedia.jmfext.media.renderer" + customRenderer2;
                }
                if (!(audioSupportIsDisabled && customRenderer2.contains(".audio.")) && (!(videoSupportIsDisabled && customRenderer2.contains(".video.")) && (renderers == null || !renderers.contains(customRenderer2)))) {
                    try {
                        PlugInManager.addPlugIn(customRenderer2, ((Renderer) Class.forName(customRenderer2).newInstance()).getSupportedInputFormats(), null, 4);
                        commit = true;
                    } catch (Throwable t) {
                        this.logger.error("Failed to register custom Renderer " + customRenderer2 + " with JMF.", t);
                    }
                }
            }
        }
        Vector<String> plugins = PlugInManager.getPlugInList(null, null, 4);
        if (plugins != null) {
            int pluginBeginIndex = 0;
            int pluginIndex = plugins.size() - 1;
            while (pluginIndex >= pluginBeginIndex) {
                String plugin = (String) plugins.get(pluginIndex);
                if (plugin.startsWith("org.jitsi.") || plugin.startsWith("net.java.sip.communicator.")) {
                    plugins.remove(pluginIndex);
                    plugins.add(0, plugin);
                    pluginBeginIndex++;
                    commit = true;
                } else {
                    pluginIndex--;
                }
            }
            PlugInManager.setPlugInList(plugins, 4);
            if (this.logger.isTraceEnabled()) {
                this.logger.trace("Reordered plug-in list:" + plugins);
            }
        }
        if (commit && !MediaServiceImpl.isJmfRegistryDisableLoad()) {
            try {
                PlugInManager.commit();
            } catch (IOException e) {
                this.logger.warn("Failed to commit changes to the JMF plug-in list.");
            }
        }
    }

    public void setAudioSystem(AudioSystem audioSystem, boolean save) {
        if (this.audioSystem == audioSystem) {
            return;
        }
        if (this.setAudioSystemIsDisabled && save) {
            throw new IllegalStateException(MediaServiceImpl.DISABLE_SET_AUDIO_SYSTEM_PNAME);
        }
        if (this.audioSystem != null && (this.audioSystem.getFeatures() & 1) == 0) {
            this.audioSystem.removePropertyChangeListener(this);
        }
        AudioSystem oldValue = this.audioSystem;
        this.audioSystem = audioSystem;
        if (this.audioSystem != null) {
            this.audioSystem.addPropertyChangeListener(this);
        }
        if (save) {
            ConfigurationService cfg = LibJitsi.getConfigurationService();
            if (cfg != null) {
                if (this.audioSystem == null) {
                    cfg.removeProperty(PROP_AUDIO_SYSTEM);
                } else {
                    cfg.setProperty(PROP_AUDIO_SYSTEM, this.audioSystem.getLocatorProtocol());
                }
            }
        }
        firePropertyChange(PROP_AUDIO_SYSTEM, oldValue, this.audioSystem);
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        if (cfg == null) {
            return;
        }
        if (frameRate != -1) {
            cfg.setProperty(PROP_VIDEO_FRAMERATE, Integer.valueOf(frameRate));
        } else {
            cfg.removeProperty(PROP_VIDEO_FRAMERATE);
        }
    }

    public void setVideoBitrate(int videoBitrate) {
        this.videoBitrate = videoBitrate;
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        if (cfg == null) {
            return;
        }
        if (videoBitrate != 128) {
            cfg.setProperty(PROP_VIDEO_BITRATE, Integer.valueOf(videoBitrate));
        } else {
            cfg.removeProperty(PROP_VIDEO_BITRATE);
        }
    }

    public void setVideoCaptureDevice(CaptureDeviceInfo device, boolean save) {
        if (this.videoCaptureDevice != device) {
            CaptureDeviceInfo oldDevice = this.videoCaptureDevice;
            this.videoCaptureDevice = device;
            if (save) {
                ConfigurationService cfg = LibJitsi.getConfigurationService();
                if (cfg != null) {
                    cfg.setProperty(PROP_VIDEO_DEVICE, this.videoCaptureDevice == null ? NoneAudioSystem.LOCATOR_PROTOCOL : this.videoCaptureDevice.getName());
                }
            }
            firePropertyChange(VIDEO_CAPTURE_DEVICE, oldDevice, device);
        }
    }

    public void setVideoRTPPacingThreshold(int videoMaxBandwidth) {
        this.videoMaxBandwidth = videoMaxBandwidth;
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        if (cfg == null) {
            return;
        }
        if (videoMaxBandwidth != 256) {
            cfg.setProperty(PROP_VIDEO_RTP_PACING_THRESHOLD, Integer.valueOf(videoMaxBandwidth));
        } else {
            cfg.removeProperty(PROP_VIDEO_RTP_PACING_THRESHOLD);
        }
    }

    public void setVideoSize(Dimension videoSize) {
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        if (cfg != null) {
            if (videoSize.getHeight() == 480.0d && videoSize.getWidth() == 640.0d) {
                cfg.removeProperty(PROP_VIDEO_HEIGHT);
                cfg.removeProperty(PROP_VIDEO_WIDTH);
            } else {
                cfg.setProperty(PROP_VIDEO_HEIGHT, Integer.valueOf(videoSize.height));
                cfg.setProperty(PROP_VIDEO_WIDTH, Integer.valueOf(videoSize.width));
            }
        }
        this.videoSize = videoSize;
        firePropertyChange(VIDEO_CAPTURE_DEVICE, this.videoCaptureDevice, this.videoCaptureDevice);
    }
}
