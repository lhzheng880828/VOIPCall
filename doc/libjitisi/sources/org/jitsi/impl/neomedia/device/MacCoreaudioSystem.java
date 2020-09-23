package org.jitsi.impl.neomedia.device;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.format.AudioFormat;
import org.jitsi.impl.neomedia.MediaUtils;
import org.jitsi.impl.neomedia.device.AudioSystem.DataFlow;
import org.jitsi.impl.neomedia.jmfext.media.renderer.audio.MacCoreaudioRenderer;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.util.Logger;

public class MacCoreaudioSystem extends AudioSystem {
    private static final String LOCATOR_PROTOCOL = "maccoreaudio";
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(MacCoreaudioSystem.class);
    private static int openStream = 0;
    private static final Object openStreamSyncRoot = new Object();
    private static int updateAvailableDeviceList = 0;
    private static final List<WeakReference<UpdateAvailableDeviceListListener>> updateAvailableDeviceListListeners = new LinkedList();
    private static final Object updateAvailableDeviceListSyncRoot = new Object();
    private Runnable devicesChangedCallback;

    public interface UpdateAvailableDeviceListListener extends EventListener {
        void didUpdateAvailableDeviceList() throws Exception;

        void willUpdateAvailableDeviceList() throws Exception;
    }

    public static void addUpdateAvailableDeviceListListener(UpdateAvailableDeviceListListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        synchronized (updateAvailableDeviceListListeners) {
            Iterator<WeakReference<UpdateAvailableDeviceListListener>> i = updateAvailableDeviceListListeners.iterator();
            boolean add = true;
            while (i.hasNext()) {
                UpdateAvailableDeviceListListener l = (UpdateAvailableDeviceListListener) ((WeakReference) i.next()).get();
                if (l == null) {
                    i.remove();
                } else if (l.equals(listener)) {
                    add = false;
                }
            }
            if (add) {
                updateAvailableDeviceListListeners.add(new WeakReference(listener));
            }
        }
    }

    public static void didOpenStream() {
        synchronized (openStreamSyncRoot) {
            openStream--;
            if (openStream < 0) {
                openStream = 0;
            }
            openStreamSyncRoot.notifyAll();
        }
    }

    private static void didUpdateAvailableDeviceList() {
        synchronized (openStreamSyncRoot) {
            updateAvailableDeviceList--;
            if (updateAvailableDeviceList < 0) {
                updateAvailableDeviceList = 0;
            }
            openStreamSyncRoot.notifyAll();
        }
        fireUpdateAvailableDeviceListEvent(false);
    }

    private static void fireUpdateAvailableDeviceListEvent(boolean will) {
        ThreadDeath t;
        try {
            synchronized (updateAvailableDeviceListListeners) {
                List<WeakReference<UpdateAvailableDeviceListListener>> ls = new ArrayList(updateAvailableDeviceListListeners);
            }
            for (WeakReference<UpdateAvailableDeviceListListener> wr : ls) {
                UpdateAvailableDeviceListListener l = (UpdateAvailableDeviceListListener) wr.get();
                if (l != null) {
                    if (will) {
                        l.willUpdateAvailableDeviceList();
                    } else {
                        l.didUpdateAvailableDeviceList();
                    }
                }
            }
        } catch (Throwable t2) {
            if (t2 instanceof ThreadDeath) {
                t = (ThreadDeath) t2;
            }
        }
    }

    private static double getSupportedSampleRate(boolean input, String deviceUID, boolean isEchoCancel) {
        double supportedSampleRate = (double) MacCoreAudioDevice.getNominalSampleRate(deviceUID, false, isEchoCancel);
        if (supportedSampleRate >= MediaUtils.MAX_AUDIO_SAMPLE_RATE) {
            return 44100.0d;
        }
        return supportedSampleRate;
    }

    private static void waitForOpenStream() {
        boolean interrupted = false;
        while (openStream > 0) {
            try {
                openStreamSyncRoot.wait();
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private static void waitForUpdateAvailableDeviceList() {
        boolean interrupted = false;
        while (updateAvailableDeviceList > 0) {
            try {
                openStreamSyncRoot.wait();
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    public static void willOpenStream() {
        synchronized (openStreamSyncRoot) {
            waitForUpdateAvailableDeviceList();
            openStream++;
            openStreamSyncRoot.notifyAll();
        }
    }

    private static void willUpdateAvailableDeviceList() {
        synchronized (openStreamSyncRoot) {
            waitForOpenStream();
            updateAvailableDeviceList++;
            openStreamSyncRoot.notifyAll();
        }
        fireUpdateAvailableDeviceListEvent(true);
    }

    MacCoreaudioSystem() throws Exception {
        super("maccoreaudio", 13);
    }

    private void bubbleUpUsbDevices(List<CaptureDeviceInfo2> devices) {
        if (!devices.isEmpty()) {
            CaptureDeviceInfo2 d;
            List<CaptureDeviceInfo2> nonUsbDevices = new ArrayList(devices.size());
            Iterator<CaptureDeviceInfo2> i = devices.iterator();
            while (i.hasNext()) {
                d = (CaptureDeviceInfo2) i.next();
                if (!d.isSameTransportType("USB")) {
                    nonUsbDevices.add(d);
                    i.remove();
                }
            }
            if (!nonUsbDevices.isEmpty()) {
                for (CaptureDeviceInfo2 d2 : nonUsbDevices) {
                    devices.add(d2);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void doInitialize() throws Exception {
        if (CoreAudioDevice.isLoaded) {
            int i;
            CaptureDeviceInfo2 cdi;
            if (this.devicesChangedCallback == null) {
                CoreAudioDevice.initDevices();
            }
            String defaultInputdeviceUID = MacCoreAudioDevice.getDefaultInputDeviceUID();
            String defaultOutputdeviceUID = MacCoreAudioDevice.getDefaultOutputDeviceUID();
            List<CaptureDeviceInfo2> captureAndPlaybackDevices = new LinkedList();
            List<CaptureDeviceInfo2> captureDevices = new LinkedList();
            List<CaptureDeviceInfo2> playbackDevices = new LinkedList();
            boolean loggerIsDebugEnabled = logger.isDebugEnabled();
            String[] deviceUIDList = MacCoreAudioDevice.getDeviceUIDList();
            for (String deviceUID : deviceUIDList) {
                String name = CoreAudioDevice.getDeviceName(deviceUID);
                boolean isInputDevice = MacCoreAudioDevice.isInputDevice(deviceUID);
                boolean isOutputDevice = MacCoreAudioDevice.isOutputDevice(deviceUID);
                String transportType = MacCoreAudioDevice.getTransportType(deviceUID);
                String modelIdentifier = null;
                String locatorRemainder = name;
                if (deviceUID != null) {
                    modelIdentifier = CoreAudioDevice.getDeviceModelIdentifier(deviceUID);
                    locatorRemainder = deviceUID;
                }
                List<CaptureDeviceInfo2> existingCdis = getDevices(DataFlow.CAPTURE);
                cdi = null;
                if (existingCdis != null) {
                    for (CaptureDeviceInfo2 existingCdi : existingCdis) {
                        String id = existingCdi.getIdentifier();
                        if (id.equals(deviceUID) || id.equals(name)) {
                            if (((AudioFormat) existingCdi.getFormats()[0]).getSampleRate() == getSupportedSampleRate(true, deviceUID, isEchoCancelActivated())) {
                                cdi = existingCdi;
                                break;
                            }
                        }
                    }
                }
                if (cdi == null) {
                    double supportedSampleRate;
                    MediaLocator mediaLocator = new MediaLocator("maccoreaudio:#" + locatorRemainder);
                    Format[] formatArr = new Format[1];
                    String str = AudioFormat.LINEAR;
                    if (isInputDevice) {
                        supportedSampleRate = getSupportedSampleRate(true, deviceUID, isEchoCancelActivated());
                    } else {
                        supportedSampleRate = 44100.0d;
                    }
                    formatArr[0] = new AudioFormat(str, supportedSampleRate, 16, 1, 0, 1, -1, -1.0d, Format.byteArray);
                    CaptureDeviceInfo2 captureDeviceInfo2 = new CaptureDeviceInfo2(name, mediaLocator, formatArr, deviceUID, transportType, modelIdentifier);
                }
                boolean isDefaultInputDevice = deviceUID.equals(defaultInputdeviceUID);
                boolean isDefaultOutputDevice = deviceUID.equals(defaultOutputdeviceUID);
                if (isInputDevice) {
                    List<CaptureDeviceInfo2> devices;
                    if (isOutputDevice) {
                        devices = captureAndPlaybackDevices;
                    } else {
                        devices = captureDevices;
                    }
                    if (isDefaultInputDevice || (isOutputDevice && isDefaultOutputDevice)) {
                        devices.add(0, cdi);
                        if (loggerIsDebugEnabled) {
                            logger.debug("Added default capture device: " + name);
                        }
                    } else {
                        devices.add(cdi);
                        if (loggerIsDebugEnabled) {
                            logger.debug("Added capture device: " + name);
                        }
                    }
                    if (loggerIsDebugEnabled && isInputDevice) {
                        if (isDefaultOutputDevice) {
                            logger.debug("Added default playback device: " + name);
                        } else {
                            logger.debug("Added playback device: " + name);
                        }
                    }
                } else if (isOutputDevice) {
                    if (isDefaultOutputDevice) {
                        playbackDevices.add(0, cdi);
                        if (loggerIsDebugEnabled) {
                            logger.debug("Added default playback device: " + name);
                        }
                    } else {
                        playbackDevices.add(cdi);
                        if (loggerIsDebugEnabled) {
                            logger.debug("Added playback device: " + name);
                        }
                    }
                }
            }
            bubbleUpUsbDevices(captureDevices);
            bubbleUpUsbDevices(playbackDevices);
            if (!(captureDevices.isEmpty() || playbackDevices.isEmpty())) {
                matchDevicesByName(captureDevices, playbackDevices);
            }
            if (!captureAndPlaybackDevices.isEmpty()) {
                bubbleUpUsbDevices(captureAndPlaybackDevices);
                for (i = captureAndPlaybackDevices.size() - 1; i >= 0; i--) {
                    cdi = (CaptureDeviceInfo2) captureAndPlaybackDevices.get(i);
                    captureDevices.add(0, cdi);
                    playbackDevices.add(0, cdi);
                }
            }
            setCaptureDevices(captureDevices);
            setPlaybackDevices(playbackDevices);
            if (this.devicesChangedCallback == null) {
                this.devicesChangedCallback = new Runnable() {
                    public void run() {
                        try {
                            MacCoreaudioSystem.this.reinitialize();
                        } catch (Throwable t) {
                            if (t instanceof ThreadDeath) {
                                ThreadDeath t2 = (ThreadDeath) t;
                            } else {
                                MacCoreaudioSystem.logger.warn("Failed to reinitialize MacCoreaudio devices", t);
                            }
                        }
                    }
                };
                CoreAudioDevice.setDevicesChangedCallback(this.devicesChangedCallback);
                return;
            }
            return;
        }
        String message = "MacOSX CoreAudio library is not loaded";
        if (logger.isInfoEnabled()) {
            logger.info(message);
        }
        throw new Exception(message);
    }

    /* access modifiers changed from: protected */
    public String getRendererClassName() {
        return MacCoreaudioRenderer.class.getName();
    }

    private void matchDevicesByName(List<CaptureDeviceInfo2> captureDevices, List<CaptureDeviceInfo2> playbackDevices) {
        Iterator<CaptureDeviceInfo2> captureIter = captureDevices.iterator();
        Pattern pattern = Pattern.compile("array|headphones|microphone|speakers|\\p{Space}|\\(|\\)", 2);
        LinkedList<CaptureDeviceInfo2> captureDevicesWithPlayback = new LinkedList();
        LinkedList<CaptureDeviceInfo2> playbackDevicesWithCapture = new LinkedList();
        int count = 0;
        while (captureIter.hasNext()) {
            CaptureDeviceInfo2 captureDevice = (CaptureDeviceInfo2) captureIter.next();
            String captureName = captureDevice.getName();
            if (captureName != null) {
                captureName = pattern.matcher(captureName).replaceAll("");
                if (captureName.length() != 0) {
                    Iterator<CaptureDeviceInfo2> playbackIter = playbackDevices.iterator();
                    CaptureDeviceInfo2 matchingPlaybackDevice = null;
                    while (playbackIter.hasNext()) {
                        CaptureDeviceInfo2 playbackDevice = (CaptureDeviceInfo2) playbackIter.next();
                        String playbackName = playbackDevice.getName();
                        if (playbackName != null && captureName.equals(pattern.matcher(playbackName).replaceAll(""))) {
                            playbackIter.remove();
                            matchingPlaybackDevice = playbackDevice;
                            break;
                        }
                    }
                    if (matchingPlaybackDevice != null) {
                        captureIter.remove();
                        captureDevicesWithPlayback.add(captureDevice);
                        playbackDevicesWithCapture.add(matchingPlaybackDevice);
                        count++;
                    }
                }
            }
        }
        for (int i = count - 1; i >= 0; i--) {
            captureDevices.add(0, captureDevicesWithPlayback.get(i));
            playbackDevices.add(0, playbackDevicesWithCapture.get(i));
        }
    }

    /* access modifiers changed from: private */
    public void reinitialize() throws Exception {
        synchronized (updateAvailableDeviceListSyncRoot) {
            willUpdateAvailableDeviceList();
            didUpdateAvailableDeviceList();
        }
        DeviceSystem.invokeDeviceSystemInitialize(this);
    }

    public String toString() {
        return "Core Audio";
    }

    public static boolean isEchoCancelActivated() {
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        if (cfg != null) {
            return cfg.getBoolean("net.java.sip.communicator.impl.neomedia.audioSystem.maccoreaudio.echocancel", false);
        }
        return false;
    }

    public boolean isEchoCancel() {
        return isEchoCancelActivated();
    }

    public void setEchoCancel(boolean echoCancel) {
        super.setEchoCancel(echoCancel);
        try {
            reinitialize();
        } catch (Exception ex) {
            logger.warn("Failed to reinitialize MacCoreaudio devices", ex);
        }
    }
}
