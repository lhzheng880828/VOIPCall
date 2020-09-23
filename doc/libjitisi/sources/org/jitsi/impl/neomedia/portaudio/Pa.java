package org.jitsi.impl.neomedia.portaudio;

import java.lang.reflect.UndeclaredThrowableException;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.util.Logger;
import org.jitsi.util.OSUtils;
import org.jitsi.util.StringUtils;

public final class Pa {
    public static final int DEFAULT_MILLIS_PER_BUFFER = 20;
    public static final double DEFAULT_SAMPLE_RATE = 44100.0d;
    public static final long FRAMES_PER_BUFFER_UNSPECIFIED = 0;
    public static final double LATENCY_HIGH = -1.0d;
    public static final double LATENCY_LOW = -2.0d;
    public static final double LATENCY_UNSPECIFIED = 0.0d;
    public static final int MMSYSERR_NODRIVER = 6;
    private static final String PROP_SUGGESTED_LATENCY = "net.java.sip.communicator.impl.neomedia.portaudio.suggestedLatency";
    public static final long SAMPLE_FORMAT_FLOAT32 = 1;
    public static final long SAMPLE_FORMAT_INT16 = 8;
    public static final long SAMPLE_FORMAT_INT24 = 4;
    public static final long SAMPLE_FORMAT_INT32 = 2;
    public static final long SAMPLE_FORMAT_INT8 = 16;
    public static final long SAMPLE_FORMAT_UINT8 = 32;
    public static final long STREAM_FLAGS_CLIP_OFF = 1;
    public static final long STREAM_FLAGS_DITHER_OFF = 2;
    public static final long STREAM_FLAGS_NEVER_DROP_INPUT = 4;
    public static final long STREAM_FLAGS_NO_FLAG = 0;
    public static final long STREAM_FLAGS_PLATFORM_SPECIFIC_FLAGS = -65536;
    public static final long STREAM_FLAGS_PRIME_OUTPUT_BUFFERS_USING_STREAM_CALLBACK = 8;
    private static Runnable devicesChangedCallback = null;
    private static final Logger logger = Logger.getLogger(Pa.class);
    public static final int paNoDevice = -1;
    public static final int paNoError = 0;
    public static final int paTimedOut = -9987;
    public static final int paUnanticipatedHostError = -9999;

    public enum HostApiTypeId {
        paAL(9),
        paALSA(8),
        paASIO(3),
        paAudioScienceHPI(14),
        paBeOS(10),
        paCoreAudio(5),
        paDirectSound(1),
        paInDevelopment(0),
        paJACK(12),
        paMME(2),
        paOSS(7),
        paSoundManager(4),
        paWASAPI(13),
        paWDMKS(11);
        
        private final int value;

        public static HostApiTypeId valueOf(int value) {
            for (HostApiTypeId hati : values()) {
                if (hati.value == value) {
                    return hati;
                }
            }
            return null;
        }

        private HostApiTypeId(int value) {
            this.value = value;
        }
    }

    public static native void AbortStream(long j) throws PortAudioException;

    public static native void CloseStream(long j) throws PortAudioException;

    public static native double DeviceInfo_getDefaultHighInputLatency(long j);

    public static native double DeviceInfo_getDefaultHighOutputLatency(long j);

    public static native double DeviceInfo_getDefaultLowInputLatency(long j);

    public static native double DeviceInfo_getDefaultLowOutputLatency(long j);

    public static native double DeviceInfo_getDefaultSampleRate(long j);

    public static native byte[] DeviceInfo_getDeviceUIDBytes(long j);

    public static native int DeviceInfo_getHostApi(long j);

    public static native int DeviceInfo_getMaxInputChannels(long j);

    public static native int DeviceInfo_getMaxOutputChannels(long j);

    private static native byte[] DeviceInfo_getNameBytes(long j);

    public static native byte[] DeviceInfo_getTransportTypeBytes(long j);

    public static native int GetDefaultInputDevice();

    public static native int GetDefaultOutputDevice();

    public static native int GetDeviceCount() throws PortAudioException;

    public static native long GetDeviceInfo(int i);

    public static native long GetHostApiInfo(int i);

    public static native int GetSampleSize(long j);

    public static native long GetStreamReadAvailable(long j);

    public static native long GetStreamWriteAvailable(long j);

    public static native int HostApiInfo_getDefaultInputDevice(long j);

    public static native int HostApiInfo_getDefaultOutputDevice(long j);

    public static native int HostApiInfo_getDeviceCount(long j);

    public static native int HostApiInfo_getType(long j);

    private static native void Initialize() throws PortAudioException;

    public static native boolean IsFormatSupported(long j, long j2, double d);

    public static native long OpenStream(long j, long j2, double d, long j3, long j4, PortAudioStreamCallback portAudioStreamCallback) throws PortAudioException;

    public static native void ReadStream(long j, byte[] bArr, long j2) throws PortAudioException;

    public static native void StartStream(long j) throws PortAudioException;

    public static native void StopStream(long j) throws PortAudioException;

    public static native long StreamParameters_new(int i, int i2, long j, double d);

    public static native void UpdateAvailableDeviceList();

    public static native void WriteStream(long j, byte[] bArr, int i, long j2, int i2) throws PortAudioException;

    private static native void free(long j);

    public static native void setDenoise(long j, boolean z);

    public static native void setEchoFilterLengthInMillis(long j, long j2);

    static {
        System.loadLibrary("jnportaudio");
        try {
            Initialize();
        } catch (PortAudioException paex) {
            logger.error("Failed to initialize the PortAudio library.", paex);
            throw new UndeclaredThrowableException(paex);
        }
    }

    public static String DeviceInfo_getDeviceUID(long deviceInfo) {
        return StringUtils.newString(DeviceInfo_getDeviceUIDBytes(deviceInfo));
    }

    public static String DeviceInfo_getName(long deviceInfo) {
        return StringUtils.newString(DeviceInfo_getNameBytes(deviceInfo));
    }

    public static String DeviceInfo_getTransportType(long deviceInfo) {
        return StringUtils.newString(DeviceInfo_getTransportTypeBytes(deviceInfo));
    }

    public static void devicesChangedCallback() {
        Runnable devicesChangedCallback = devicesChangedCallback;
        if (devicesChangedCallback != null) {
            devicesChangedCallback.run();
        }
    }

    public static int getDeviceIndex(String deviceID, int minInputChannels, int minOutputChannels) {
        if (deviceID != null) {
            int deviceCount = 0;
            try {
                deviceCount = GetDeviceCount();
            } catch (PortAudioException e) {
            }
            int deviceIndex = 0;
            while (deviceIndex < deviceCount) {
                long deviceInfo = GetDeviceInfo(deviceIndex);
                String deviceUID = DeviceInfo_getDeviceUID(deviceInfo);
                if (deviceUID == null || deviceUID.length() == 0) {
                    deviceUID = DeviceInfo_getName(deviceInfo);
                }
                if (deviceID.equals(deviceUID) && ((minInputChannels <= 0 || DeviceInfo_getMaxInputChannels(deviceInfo) >= minInputChannels) && (minOutputChannels <= 0 || DeviceInfo_getMaxOutputChannels(deviceInfo) >= minOutputChannels))) {
                    return deviceIndex;
                }
                deviceIndex++;
            }
        }
        return -1;
    }

    public static long getPaSampleFormat(int sampleSizeInBits) {
        switch (sampleSizeInBits) {
            case 8:
                return 16;
            case 24:
                return 4;
            case 32:
                return 2;
            default:
                return 8;
        }
    }

    public static double getSuggestedLatency() {
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        if (cfg != null) {
            String suggestedLatencyString = cfg.getString(PROP_SUGGESTED_LATENCY);
            if (suggestedLatencyString != null) {
                try {
                    double suggestedLatency = Double.parseDouble(suggestedLatencyString);
                    if (suggestedLatency != LATENCY_UNSPECIFIED) {
                        return suggestedLatency;
                    }
                } catch (NumberFormatException nfe) {
                    logger.error("Failed to parse configuration property net.java.sip.communicator.impl.neomedia.portaudio.suggestedLatency value as a double", nfe);
                }
            }
        }
        if (OSUtils.IS_MAC || OSUtils.IS_LINUX) {
            return -1.0d;
        }
        return OSUtils.IS_WINDOWS ? 0.1d : LATENCY_UNSPECIFIED;
    }

    public static void setDevicesChangedCallback(Runnable devicesChangedCallback) {
        devicesChangedCallback = devicesChangedCallback;
    }

    public static void StreamParameters_free(long streamParameters) {
        free(streamParameters);
    }

    public static void WriteStream(long stream, byte[] buffer, long frames) throws PortAudioException {
        WriteStream(stream, buffer, 0, frames, 1);
    }

    private Pa() {
    }
}
