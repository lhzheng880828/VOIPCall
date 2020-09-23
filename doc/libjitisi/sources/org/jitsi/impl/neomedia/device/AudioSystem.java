package org.jitsi.impl.neomedia.device;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.media.CaptureDeviceInfo;
import javax.media.MediaLocator;
import javax.media.Renderer;
import javax.media.format.AudioFormat;
import org.jitsi.android.util.javax.sound.sampled.AudioInputStream;
import org.jitsi.android.util.javax.sound.sampled.UnsupportedAudioFileException;
import org.jitsi.impl.neomedia.jmfext.media.renderer.audio.AbstractAudioRenderer;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.resources.ResourceManagementService;
import org.jitsi.util.Logger;

public abstract class AudioSystem extends DeviceSystem {
    public static final int FEATURE_AGC = 16;
    public static final int FEATURE_DENOISE = 2;
    public static final int FEATURE_ECHO_CANCELLATION = 4;
    public static final int FEATURE_NOTIFY_AND_PLAYBACK_DEVICES = 8;
    public static final String LOCATOR_PROTOCOL_AUDIORECORD = "audiorecord";
    public static final String LOCATOR_PROTOCOL_JAVASOUND = "javasound";
    public static final String LOCATOR_PROTOCOL_MACCOREAUDIO = "maccoreaudio";
    public static final String LOCATOR_PROTOCOL_OPENSLES = "opensles";
    public static final String LOCATOR_PROTOCOL_PORTAUDIO = "portaudio";
    public static final String LOCATOR_PROTOCOL_PULSEAUDIO = "pulseaudio";
    public static final String LOCATOR_PROTOCOL_WASAPI = "wasapi";
    private static final String PNAME_AGC = "automaticgaincontrol";
    protected static final String PNAME_DENOISE = "denoise";
    protected static final String PNAME_ECHOCANCEL = "echocancel";
    private static Logger logger = Logger.getLogger(AudioSystem.class);
    private Devices[] devices;

    public enum DataFlow {
        CAPTURE,
        NOTIFY,
        PLAYBACK
    }

    public static AudioSystem getAudioSystem(String locatorProtocol) {
        AudioSystem[] audioSystems = getAudioSystems();
        if (audioSystems == null) {
            return null;
        }
        for (AudioSystem audioSystem : audioSystems) {
            if (audioSystem.getLocatorProtocol().equalsIgnoreCase(locatorProtocol)) {
                return audioSystem;
            }
        }
        return null;
    }

    public static AudioSystem[] getAudioSystems() {
        List<AudioSystem> audioSystems;
        DeviceSystem[] deviceSystems = DeviceSystem.getDeviceSystems(MediaType.AUDIO);
        if (deviceSystems == null) {
            audioSystems = null;
        } else {
            audioSystems = new ArrayList(deviceSystems.length);
            for (DeviceSystem deviceSystem : deviceSystems) {
                if (deviceSystem instanceof AudioSystem) {
                    audioSystems.add((AudioSystem) deviceSystem);
                }
            }
        }
        if (audioSystems == null) {
            return null;
        }
        return (AudioSystem[]) audioSystems.toArray(new AudioSystem[audioSystems.size()]);
    }

    protected AudioSystem(String locatorProtocol) throws Exception {
        this(locatorProtocol, 0);
    }

    protected AudioSystem(String locatorProtocol, int features) throws Exception {
        super(MediaType.AUDIO, locatorProtocol, features);
    }

    public Renderer createRenderer() {
        return createRenderer(true);
    }

    public Renderer createRenderer(boolean playback) {
        ThreadDeath t;
        String className = getRendererClassName();
        if (className == null) {
            return null;
        }
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (Throwable t2) {
            if (t2 instanceof ThreadDeath) {
                t = (ThreadDeath) t2;
            } else {
                clazz = null;
                logger.error("Failed to get class " + className, t2);
            }
        }
        if (clazz == null) {
            return null;
        }
        if (!Renderer.class.isAssignableFrom(clazz)) {
            return null;
        }
        boolean superCreateRenderer;
        Renderer renderer;
        if ((getFeatures() & 8) == 0 || !AbstractAudioRenderer.class.isAssignableFrom(clazz)) {
            superCreateRenderer = true;
            renderer = null;
        } else {
            Constructor<?> constructor = null;
            try {
                constructor = clazz.getConstructor(new Class[]{Boolean.TYPE});
            } catch (NoSuchMethodException | SecurityException e) {
            }
            if (constructor != null) {
                superCreateRenderer = false;
                try {
                    renderer = (Renderer) constructor.newInstance(new Object[]{Boolean.valueOf(playback)});
                } catch (Throwable t22) {
                    if (t22 instanceof ThreadDeath) {
                        t = (ThreadDeath) t22;
                    } else {
                        renderer = null;
                        logger.error("Failed to initialize a new " + className + " instance", t22);
                    }
                }
                if (!(renderer == null || playback)) {
                    CaptureDeviceInfo device = getSelectedDevice(DataFlow.NOTIFY);
                    if (device == null) {
                        renderer = null;
                    } else {
                        MediaLocator locator = device.getLocator();
                        if (locator != null) {
                            ((AbstractAudioRenderer) renderer).setLocator(locator);
                        }
                    }
                }
            } else {
                superCreateRenderer = true;
                renderer = null;
            }
        }
        if (superCreateRenderer && renderer == null) {
            return super.createRenderer();
        }
        return renderer;
    }

    public InputStream getAudioInputStream(String uri) throws IOException {
        ResourceManagementService resources = LibJitsi.getResourceManagementService();
        URL url = resources == null ? null : resources.getSoundURLForPath(uri);
        if (url == null) {
            try {
                url = new URL(uri);
            } catch (MalformedURLException e) {
                return null;
            } catch (UnsupportedAudioFileException uafe) {
                logger.error("Unsupported format of audio stream " + url, uafe);
                return null;
            }
        }
        return org.jitsi.android.util.javax.sound.sampled.AudioSystem.getAudioInputStream(url);
    }

    public CaptureDeviceInfo2 getDevice(DataFlow dataFlow, MediaLocator locator) {
        return this.devices[dataFlow.ordinal()].getDevice(locator);
    }

    public List<CaptureDeviceInfo2> getDevices(DataFlow dataFlow) {
        return this.devices[dataFlow.ordinal()].getDevices();
    }

    public AudioFormat getFormat(InputStream audioInputStream) {
        if (!(audioInputStream instanceof AudioInputStream)) {
            return null;
        }
        org.jitsi.android.util.javax.sound.sampled.AudioFormat af = ((AudioInputStream) audioInputStream).getFormat();
        return new AudioFormat(AudioFormat.LINEAR, (double) af.getSampleRate(), af.getSampleSizeInBits(), af.getChannels());
    }

    /* access modifiers changed from: protected */
    public String getPropertyName(String basePropertyName) {
        return "net.java.sip.communicator.impl.neomedia.audioSystem." + getLocatorProtocol() + "." + basePropertyName;
    }

    public CaptureDeviceInfo2 getSelectedDevice(DataFlow dataFlow) {
        return this.devices[dataFlow.ordinal()].getSelectedDevice(getDevices(dataFlow));
    }

    public boolean isAutomaticGainControl() {
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        boolean value = (getFeatures() & 16) == 16;
        if (cfg != null) {
            return cfg.getBoolean(getPropertyName(PNAME_AGC), value);
        }
        return value;
    }

    public boolean isDenoise() {
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        boolean value = (getFeatures() & 2) == 2;
        if (cfg != null) {
            return cfg.getBoolean(getPropertyName(PNAME_DENOISE), value);
        }
        return value;
    }

    public boolean isEchoCancel() {
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        boolean value = (getFeatures() & 4) == 4;
        if (cfg != null) {
            return cfg.getBoolean(getPropertyName(PNAME_ECHOCANCEL), value);
        }
        return value;
    }

    /* access modifiers changed from: protected */
    public void postInitialize() {
        try {
            postInitializeSpecificDevices(DataFlow.CAPTURE);
            if ((getFeatures() & 8) != 0) {
                postInitializeSpecificDevices(DataFlow.NOTIFY);
                postInitializeSpecificDevices(DataFlow.PLAYBACK);
            }
            super.postInitialize();
        } catch (Throwable th) {
            super.postInitialize();
        }
    }

    /* access modifiers changed from: protected */
    public void postInitializeSpecificDevices(DataFlow dataFlow) {
        List<CaptureDeviceInfo2> activeDevices = getDevices(dataFlow);
        Devices devices = this.devices[dataFlow.ordinal()];
        devices.setDevice(devices.getSelectedDevice(activeDevices), false);
    }

    /* access modifiers changed from: protected */
    public void preInitialize() {
        super.preInitialize();
        if (this.devices == null) {
            this.devices = new Devices[3];
            this.devices[DataFlow.CAPTURE.ordinal()] = new CaptureDevices(this);
            this.devices[DataFlow.NOTIFY.ordinal()] = new NotifyDevices(this);
            this.devices[DataFlow.PLAYBACK.ordinal()] = new PlaybackDevices(this);
        }
    }

    /* access modifiers changed from: 0000 */
    public void propertyChange(String property, Object oldValue, Object newValue) {
        firePropertyChange(property, oldValue, newValue);
    }

    public void setAutomaticGainControl(boolean automaticGainControl) {
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        if (cfg != null) {
            cfg.setProperty(getPropertyName(PNAME_AGC), Boolean.valueOf(automaticGainControl));
        }
    }

    /* access modifiers changed from: protected */
    public void setCaptureDevices(List<CaptureDeviceInfo2> captureDevices) {
        this.devices[DataFlow.CAPTURE.ordinal()].setDevices(captureDevices);
    }

    public void setDenoise(boolean denoise) {
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        if (cfg != null) {
            cfg.setProperty(getPropertyName(PNAME_DENOISE), Boolean.valueOf(denoise));
        }
    }

    public void setDevice(DataFlow dataFlow, CaptureDeviceInfo2 device, boolean save) {
        this.devices[dataFlow.ordinal()].setDevice(device, save);
    }

    public void setEchoCancel(boolean echoCancel) {
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        if (cfg != null) {
            cfg.setProperty(getPropertyName(PNAME_ECHOCANCEL), Boolean.valueOf(echoCancel));
        }
    }

    /* access modifiers changed from: protected */
    public void setPlaybackDevices(List<CaptureDeviceInfo2> playbackDevices) {
        this.devices[DataFlow.PLAYBACK.ordinal()].setDevices(playbackDevices);
        this.devices[DataFlow.NOTIFY.ordinal()].setDevices(playbackDevices);
    }
}
