package org.jitsi.impl.neomedia.device;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.Renderer;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import org.jitsi.impl.neomedia.MediaServiceImpl;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.util.Logger;
import org.jitsi.util.OSUtils;
import org.jitsi.util.event.PropertyChangeNotifier;

public abstract class DeviceSystem extends PropertyChangeNotifier {
    public static final int FEATURE_REINITIALIZE = 1;
    public static final String LOCATOR_PROTOCOL_ANDROIDCAMERA = "androidcamera";
    public static final String LOCATOR_PROTOCOL_CIVIL = "civil";
    public static final String LOCATOR_PROTOCOL_DIRECTSHOW = "directshow";
    public static final String LOCATOR_PROTOCOL_IMGSTREAMING = "imgstreaming";
    public static final String LOCATOR_PROTOCOL_MEDIARECORDER = "mediarecorder";
    public static final String LOCATOR_PROTOCOL_QUICKTIME = "quicktime";
    public static final String LOCATOR_PROTOCOL_VIDEO4LINUX2 = "video4linux2";
    public static final String PROP_DEVICES = "devices";
    private static List<DeviceSystem> deviceSystems = new LinkedList();
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(DeviceSystem.class);
    private static List<CaptureDeviceInfo> preInitializeDevices;
    private final int features;
    private final String locatorProtocol;
    private final MediaType mediaType;

    public abstract void doInitialize() throws Exception;

    protected static List<CaptureDeviceInfo> filterDeviceListByLocatorProtocol(List<CaptureDeviceInfo> deviceList, String locatorProtocol) {
        if (deviceList != null && deviceList.size() > 0) {
            Iterator<CaptureDeviceInfo> deviceListIter = deviceList.iterator();
            while (deviceListIter.hasNext()) {
                MediaLocator locator = ((CaptureDeviceInfo) deviceListIter.next()).getLocator();
                if (locator == null || !locatorProtocol.equalsIgnoreCase(locator.getProtocol())) {
                    deviceListIter.remove();
                }
            }
        }
        return deviceList;
    }

    public static DeviceSystem[] getDeviceSystems(MediaType mediaType) {
        List<DeviceSystem> ret;
        synchronized (deviceSystems) {
            ret = new ArrayList(deviceSystems.size());
            for (DeviceSystem deviceSystem : deviceSystems) {
                if (deviceSystem.getMediaType().equals(mediaType)) {
                    ret.add(deviceSystem);
                }
            }
        }
        return (DeviceSystem[]) ret.toArray(new DeviceSystem[ret.size()]);
    }

    public static void initializeDeviceSystems() {
        if (MediaServiceImpl.isMediaTypeSupportEnabled(MediaType.AUDIO)) {
            if (logger.isInfoEnabled()) {
                logger.info("Initializing audio devices");
            }
            initializeDeviceSystems(MediaType.AUDIO);
        }
        if (MediaServiceImpl.isMediaTypeSupportEnabled(MediaType.VIDEO)) {
            if (logger.isInfoEnabled()) {
                logger.info("Initializing video devices");
            }
            initializeDeviceSystems(MediaType.VIDEO);
        }
    }

    public static void initializeDeviceSystems(MediaType mediaType) {
        String[] classNames;
        String str = null;
        String str2;
        switch (mediaType) {
            case AUDIO:
                classNames = new String[7];
                if (OSUtils.IS_ANDROID) {
                    str2 = ".AudioRecordSystem";
                } else {
                    str2 = null;
                }
                classNames[0] = str2;
                if (OSUtils.IS_ANDROID) {
                    str2 = ".OpenSLESSystem";
                } else {
                    str2 = null;
                }
                classNames[1] = str2;
                if (OSUtils.IS_LINUX || OSUtils.IS_FREEBSD) {
                    str2 = ".PulseAudioSystem";
                } else {
                    str2 = null;
                }
                classNames[2] = str2;
                if (OSUtils.IS_WINDOWS) {
                    str2 = ".WASAPISystem";
                } else {
                    str2 = null;
                }
                classNames[3] = str2;
                if (OSUtils.IS_ANDROID) {
                    str2 = null;
                } else {
                    str2 = ".PortAudioSystem";
                }
                classNames[4] = str2;
                if (OSUtils.IS_MAC) {
                    str = ".MacCoreaudioSystem";
                }
                classNames[5] = str;
                classNames[6] = ".NoneAudioSystem";
                break;
            case VIDEO:
                classNames = new String[6];
                classNames[0] = OSUtils.IS_ANDROID ? ".MediaRecorderSystem" : null;
                if (OSUtils.IS_ANDROID) {
                    str2 = ".AndroidCameraSystem";
                } else {
                    str2 = null;
                }
                classNames[1] = str2;
                if (OSUtils.IS_LINUX || OSUtils.IS_FREEBSD) {
                    str2 = ".Video4Linux2System";
                } else {
                    str2 = null;
                }
                classNames[2] = str2;
                if (OSUtils.IS_MAC) {
                    str2 = ".QuickTimeSystem";
                } else {
                    str2 = null;
                }
                classNames[3] = str2;
                if (OSUtils.IS_WINDOWS) {
                    str = ".DirectShowSystem";
                }
                classNames[4] = str;
                classNames[5] = ".ImgStreamingSystem";
                break;
            default:
                throw new IllegalArgumentException("mediaType");
        }
        initializeDeviceSystems(classNames);
    }

    private static void initializeDeviceSystems(String[] classNames) {
        ThreadDeath t;
        synchronized (deviceSystems) {
            String packageName = null;
            for (String className : classNames) {
                String className2;
                if (className2 != null) {
                    boolean reinitialize;
                    if (className2.startsWith(".")) {
                        if (packageName == null) {
                            packageName = DeviceSystem.class.getPackage().getName();
                        }
                        className2 = packageName + className2;
                    }
                    DeviceSystem deviceSystem = null;
                    for (DeviceSystem aDeviceSystem : deviceSystems) {
                        if (aDeviceSystem.getClass().getName().equals(className2)) {
                            deviceSystem = aDeviceSystem;
                            break;
                        }
                    }
                    if (deviceSystem == null) {
                        reinitialize = false;
                        Object o = null;
                        try {
                            o = Class.forName(className2).newInstance();
                        } catch (Throwable t2) {
                            if (t2 instanceof ThreadDeath) {
                                t = (ThreadDeath) t2;
                            } else {
                                logger.warn("Failed to reinitialize " + className2, t2);
                            }
                        }
                        if (o instanceof DeviceSystem) {
                            deviceSystem = (DeviceSystem) o;
                            if (!deviceSystems.contains(deviceSystem)) {
                                deviceSystems.add(deviceSystem);
                            }
                        }
                    } else {
                        reinitialize = true;
                    }
                    if (reinitialize && (deviceSystem.getFeatures() & 1) != 0) {
                        invokeDeviceSystemInitialize(deviceSystem);
                    }
                }
            }
        }
    }

    static void invokeDeviceSystemInitialize(DeviceSystem deviceSystem) throws Exception {
        invokeDeviceSystemInitialize(deviceSystem, false);
    }

    static void invokeDeviceSystemInitialize(final DeviceSystem deviceSystem, boolean asynchronous) throws Exception {
        if (OSUtils.IS_WINDOWS || asynchronous) {
            final String className = deviceSystem.getClass().getName();
            final Throwable[] exception = new Throwable[1];
            Thread thread = new Thread(className + ".initialize()") {
                public void run() {
                    boolean loggerIsTraceEnabled = DeviceSystem.logger.isTraceEnabled();
                    if (loggerIsTraceEnabled) {
                        try {
                            DeviceSystem.logger.trace("Will initialize " + className);
                        } catch (Throwable t) {
                            exception[0] = t;
                            if (t instanceof ThreadDeath) {
                                ThreadDeath t2 = (ThreadDeath) t;
                            } else {
                                return;
                            }
                        }
                    }
                    deviceSystem.initialize();
                    if (loggerIsTraceEnabled) {
                        DeviceSystem.logger.trace("Did initialize " + className);
                    }
                }
            };
            thread.setDaemon(true);
            thread.start();
            if (!asynchronous) {
                boolean interrupted = false;
                while (thread.isAlive()) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
                Throwable t = exception[0];
                if (t == null) {
                    return;
                }
                if (t instanceof Exception) {
                    throw ((Exception) t);
                }
                throw new UndeclaredThrowableException(t);
            }
            return;
        }
        deviceSystem.initialize();
    }

    protected DeviceSystem(MediaType mediaType, String locatorProtocol) throws Exception {
        this(mediaType, locatorProtocol, 0);
    }

    protected DeviceSystem(MediaType mediaType, String locatorProtocol, int features) throws Exception {
        if (mediaType == null) {
            throw new NullPointerException("mediaType");
        } else if (locatorProtocol == null) {
            throw new NullPointerException("locatorProtocol");
        } else {
            this.mediaType = mediaType;
            this.locatorProtocol = locatorProtocol;
            this.features = features;
            invokeDeviceSystemInitialize(this);
        }
    }

    public Renderer createRenderer() {
        String className = getRendererClassName();
        if (className != null) {
            try {
                return (Renderer) Class.forName(className).newInstance();
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    ThreadDeath t2 = (ThreadDeath) t;
                } else {
                    logger.error("Failed to initialize a new " + className + " instance", t);
                }
            }
        }
        return null;
    }

    public final int getFeatures() {
        return this.features;
    }

    public Format getFormat() {
        switch (getMediaType()) {
            case AUDIO:
                return new AudioFormat(null);
            case VIDEO:
                return new VideoFormat(null);
            default:
                return null;
        }
    }

    public final String getLocatorProtocol() {
        return this.locatorProtocol;
    }

    public final MediaType getMediaType() {
        return this.mediaType;
    }

    /* access modifiers changed from: protected */
    public String getRendererClassName() {
        return null;
    }

    /* access modifiers changed from: protected|final|declared_synchronized */
    public final synchronized void initialize() throws Exception {
        preInitialize();
        try {
            doInitialize();
            postInitialize();
        } catch (Throwable th) {
            postInitialize();
        }
    }

    /* access modifiers changed from: protected */
    public void postInitialize() {
        int postInitializeDeviceCount = 0;
        try {
            Format format = getFormat();
            if (format != null) {
                List<CaptureDeviceInfo> postInitializeDevices = new ArrayList(CaptureDeviceManager.getDeviceList(format));
                if (preInitializeDevices != null) {
                    Iterator<CaptureDeviceInfo> preIter = preInitializeDevices.iterator();
                    while (preIter.hasNext()) {
                        if (postInitializeDevices.remove(preIter.next())) {
                            preIter.remove();
                        }
                    }
                }
                int preInitializeDeviceCount = preInitializeDevices == null ? 0 : preInitializeDevices.size();
                if (postInitializeDevices != null) {
                    postInitializeDeviceCount = postInitializeDevices.size();
                }
                if (!(preInitializeDeviceCount == 0 && postInitializeDeviceCount == 0)) {
                    firePropertyChange(PROP_DEVICES, preInitializeDevices, postInitializeDevices);
                }
            }
            preInitializeDevices = null;
        } catch (Throwable th) {
            preInitializeDevices = null;
        }
    }

    /* access modifiers changed from: protected */
    public void preInitialize() {
        Format format = getFormat();
        if (format != null) {
            List<CaptureDeviceInfo> cdis = CaptureDeviceManager.getDeviceList(format);
            preInitializeDevices = new ArrayList(cdis);
            if (cdis != null && cdis.size() > 0) {
                boolean commit = false;
                for (CaptureDeviceInfo cdi : filterDeviceListByLocatorProtocol(cdis, getLocatorProtocol())) {
                    CaptureDeviceManager.removeDevice(cdi);
                    commit = true;
                }
                if (commit && !MediaServiceImpl.isJmfRegistryDisableLoad()) {
                    try {
                        CaptureDeviceManager.commit();
                    } catch (IOException ioe) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Failed to commit CaptureDeviceManager", ioe);
                        }
                    }
                }
            }
        }
    }

    public String toString() {
        return getLocatorProtocol();
    }
}
