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
import org.jitsi.impl.neomedia.control.DiagnosticsControl;
import org.jitsi.impl.neomedia.device.AudioSystem.DataFlow;
import org.jitsi.impl.neomedia.jmfext.media.renderer.audio.PortAudioRenderer;
import org.jitsi.impl.neomedia.portaudio.Pa;
import org.jitsi.util.Logger;

public class PortAudioSystem extends AudioSystem {
    private static final String LOCATOR_PROTOCOL = "portaudio";
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(PortAudioSystem.class);
    private static int paOpenStream = 0;
    private static final Object paOpenStreamSyncRoot = new Object();
    private static int paUpdateAvailableDeviceList = 0;
    private static final List<WeakReference<PaUpdateAvailableDeviceListListener>> paUpdateAvailableDeviceListListeners = new LinkedList();
    private static final Object paUpdateAvailableDeviceListSyncRoot = new Object();
    private Runnable devicesChangedCallback;

    public interface PaUpdateAvailableDeviceListListener extends EventListener {
        void didPaUpdateAvailableDeviceList() throws Exception;

        void willPaUpdateAvailableDeviceList() throws Exception;
    }

    public static void addPaUpdateAvailableDeviceListListener(PaUpdateAvailableDeviceListListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        synchronized (paUpdateAvailableDeviceListListeners) {
            Iterator<WeakReference<PaUpdateAvailableDeviceListListener>> i = paUpdateAvailableDeviceListListeners.iterator();
            boolean add = true;
            while (i.hasNext()) {
                PaUpdateAvailableDeviceListListener l = (PaUpdateAvailableDeviceListListener) ((WeakReference) i.next()).get();
                if (l == null) {
                    i.remove();
                } else if (l.equals(listener)) {
                    add = false;
                }
            }
            if (add) {
                paUpdateAvailableDeviceListListeners.add(new WeakReference(listener));
            }
        }
    }

    public static void didPaOpenStream() {
        synchronized (paOpenStreamSyncRoot) {
            paOpenStream--;
            if (paOpenStream < 0) {
                paOpenStream = 0;
            }
            paOpenStreamSyncRoot.notifyAll();
        }
    }

    private static void didPaUpdateAvailableDeviceList() {
        synchronized (paOpenStreamSyncRoot) {
            paUpdateAvailableDeviceList--;
            if (paUpdateAvailableDeviceList < 0) {
                paUpdateAvailableDeviceList = 0;
            }
            paOpenStreamSyncRoot.notifyAll();
        }
        firePaUpdateAvailableDeviceListEvent(false);
    }

    private static void firePaUpdateAvailableDeviceListEvent(boolean will) {
        ThreadDeath t;
        try {
            synchronized (paUpdateAvailableDeviceListListeners) {
                List<WeakReference<PaUpdateAvailableDeviceListListener>> ls = new ArrayList(paUpdateAvailableDeviceListListeners);
            }
            for (WeakReference<PaUpdateAvailableDeviceListListener> wr : ls) {
                PaUpdateAvailableDeviceListListener l = (PaUpdateAvailableDeviceListListener) wr.get();
                if (l != null) {
                    if (will) {
                        l.willPaUpdateAvailableDeviceList();
                    } else {
                        l.didPaUpdateAvailableDeviceList();
                    }
                }
            }
        } catch (Throwable t2) {
            if (t2 instanceof ThreadDeath) {
                t = (ThreadDeath) t2;
            }
        }
    }

    private static double getSupportedSampleRate(boolean input, int deviceIndex, int channelCount, long sampleFormat) {
        long deviceInfo = Pa.GetDeviceInfo(deviceIndex);
        if (deviceInfo == 0) {
            return 44100.0d;
        }
        double defaultSampleRate = Pa.DeviceInfo_getDefaultSampleRate(deviceInfo);
        if (defaultSampleRate >= MediaUtils.MAX_AUDIO_SAMPLE_RATE) {
            return defaultSampleRate;
        }
        long streamParameters = Pa.StreamParameters_new(deviceIndex, channelCount, sampleFormat, Pa.LATENCY_UNSPECIFIED);
        if (streamParameters == 0) {
            return defaultSampleRate;
        }
        long inputParameters;
        long outputParameters;
        if (input) {
            inputParameters = streamParameters;
            outputParameters = 0;
        } else {
            inputParameters = 0;
            outputParameters = streamParameters;
        }
        try {
            double supportedSampleRate;
            if (Pa.IsFormatSupported(inputParameters, outputParameters, 44100.0d)) {
                supportedSampleRate = 44100.0d;
            } else {
                supportedSampleRate = defaultSampleRate;
            }
            Pa.StreamParameters_free(streamParameters);
            return supportedSampleRate;
        } catch (Throwable th) {
            Pa.StreamParameters_free(streamParameters);
        }
    }

    public static void monitorFunctionalHealth(DiagnosticsControl diagnosticsControl) {
    }

    public static void removePaUpdateAvailableDeviceListListener(PaUpdateAvailableDeviceListListener listener) {
        if (listener != null) {
            synchronized (paUpdateAvailableDeviceListListeners) {
                Iterator<WeakReference<PaUpdateAvailableDeviceListListener>> i = paUpdateAvailableDeviceListListeners.iterator();
                while (i.hasNext()) {
                    PaUpdateAvailableDeviceListListener l = (PaUpdateAvailableDeviceListListener) ((WeakReference) i.next()).get();
                    if (l == null || l.equals(listener)) {
                        i.remove();
                    }
                }
            }
        }
    }

    private static void waitForPaOpenStream() {
        boolean interrupted = false;
        while (paOpenStream > 0) {
            try {
                paOpenStreamSyncRoot.wait();
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private static void waitForPaUpdateAvailableDeviceList() {
        boolean interrupted = false;
        while (paUpdateAvailableDeviceList > 0) {
            try {
                paOpenStreamSyncRoot.wait();
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    public static void willPaOpenStream() {
        synchronized (paOpenStreamSyncRoot) {
            waitForPaUpdateAvailableDeviceList();
            paOpenStream++;
            paOpenStreamSyncRoot.notifyAll();
        }
    }

    private static void willPaUpdateAvailableDeviceList() {
        synchronized (paOpenStreamSyncRoot) {
            waitForPaOpenStream();
            paUpdateAvailableDeviceList++;
            paOpenStreamSyncRoot.notifyAll();
        }
        firePaUpdateAvailableDeviceListEvent(true);
    }

    PortAudioSystem() throws Exception {
        super("portaudio", 15);
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
        CaptureDeviceInfo2 cdi;
        int deviceCount = Pa.GetDeviceCount();
        long sampleFormat = Pa.getPaSampleFormat(16);
        int defaultInputDeviceIndex = Pa.GetDefaultInputDevice();
        int defaultOutputDeviceIndex = Pa.GetDefaultOutputDevice();
        List<CaptureDeviceInfo2> captureAndPlaybackDevices = new LinkedList();
        List<CaptureDeviceInfo2> captureDevices = new LinkedList();
        List<CaptureDeviceInfo2> playbackDevices = new LinkedList();
        boolean loggerIsDebugEnabled = logger.isDebugEnabled();
        if (CoreAudioDevice.isLoaded) {
            CoreAudioDevice.initDevices();
        }
        int deviceIndex = 0;
        while (deviceIndex < deviceCount) {
            String modelIdentifier;
            String locatorRemainder;
            long deviceInfo = Pa.GetDeviceInfo(deviceIndex);
            String name = Pa.DeviceInfo_getName(deviceInfo);
            if (name != null) {
                name = name.trim();
            }
            int maxInputChannels = Pa.DeviceInfo_getMaxInputChannels(deviceInfo);
            int maxOutputChannels = Pa.DeviceInfo_getMaxOutputChannels(deviceInfo);
            String transportType = Pa.DeviceInfo_getTransportType(deviceInfo);
            String deviceUID = Pa.DeviceInfo_getDeviceUID(deviceInfo);
            if (deviceUID == null) {
                modelIdentifier = null;
                locatorRemainder = name;
            } else {
                modelIdentifier = CoreAudioDevice.isLoaded ? CoreAudioDevice.getDeviceModelIdentifier(deviceUID) : null;
                locatorRemainder = deviceUID;
            }
            List<CaptureDeviceInfo2> existingCdis = getDevices(DataFlow.CAPTURE);
            cdi = null;
            if (existingCdis != null) {
                for (CaptureDeviceInfo2 existingCdi : existingCdis) {
                    String id = existingCdi.getIdentifier();
                    if (!id.equals(deviceUID)) {
                        if (id.equals(name)) {
                        }
                    }
                    cdi = existingCdi;
                }
            }
            if (cdi == null) {
                double supportedSampleRate;
                MediaLocator mediaLocator = new MediaLocator("portaudio:#" + locatorRemainder);
                Format[] formatArr = new Format[1];
                String str = AudioFormat.LINEAR;
                if (maxInputChannels > 0) {
                    supportedSampleRate = getSupportedSampleRate(true, deviceIndex, 1, sampleFormat);
                } else {
                    supportedSampleRate = 44100.0d;
                }
                formatArr[0] = new AudioFormat(str, supportedSampleRate, 16, 1, 0, 1, -1, -1.0d, Format.byteArray);
                CaptureDeviceInfo2 captureDeviceInfo2 = new CaptureDeviceInfo2(name, mediaLocator, formatArr, deviceUID, transportType, modelIdentifier);
            }
            if (maxInputChannels > 0) {
                List<CaptureDeviceInfo2> devices;
                if (maxOutputChannels > 0) {
                    devices = captureAndPlaybackDevices;
                } else {
                    devices = captureDevices;
                }
                if (deviceIndex == defaultInputDeviceIndex || (maxOutputChannels > 0 && deviceIndex == defaultOutputDeviceIndex)) {
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
                if (loggerIsDebugEnabled && maxInputChannels > 0) {
                    if (deviceIndex == defaultOutputDeviceIndex) {
                        logger.debug("Added default playback device: " + name);
                    } else {
                        logger.debug("Added playback device: " + name);
                    }
                }
            } else if (maxOutputChannels > 0) {
                if (deviceIndex == defaultOutputDeviceIndex) {
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
            deviceIndex++;
        }
        if (CoreAudioDevice.isLoaded) {
            CoreAudioDevice.freeDevices();
        }
        bubbleUpUsbDevices(captureDevices);
        bubbleUpUsbDevices(playbackDevices);
        if (!(captureDevices.isEmpty() || playbackDevices.isEmpty())) {
            matchDevicesByName(captureDevices, playbackDevices);
        }
        if (!captureAndPlaybackDevices.isEmpty()) {
            bubbleUpUsbDevices(captureAndPlaybackDevices);
            for (int i = captureAndPlaybackDevices.size() - 1; i >= 0; i--) {
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
                        PortAudioSystem.this.reinitialize();
                    } catch (Throwable t) {
                        if (t instanceof ThreadDeath) {
                            ThreadDeath t2 = (ThreadDeath) t;
                        } else {
                            PortAudioSystem.logger.warn("Failed to reinitialize PortAudio devices", t);
                        }
                    }
                }
            };
            Pa.setDevicesChangedCallback(this.devicesChangedCallback);
        }
    }

    /* access modifiers changed from: protected */
    public String getRendererClassName() {
        return PortAudioRenderer.class.getName();
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
        synchronized (paUpdateAvailableDeviceListSyncRoot) {
            willPaUpdateAvailableDeviceList();
            try {
                Pa.UpdateAvailableDeviceList();
                didPaUpdateAvailableDeviceList();
            } catch (Throwable th) {
                didPaUpdateAvailableDeviceList();
            }
        }
        DeviceSystem.invokeDeviceSystemInitialize(this);
    }

    public String toString() {
        return "PortAudio";
    }
}
