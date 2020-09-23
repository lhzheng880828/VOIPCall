package org.jitsi.impl.neomedia.device;

import org.jitsi.util.StringUtils;

public class MacCoreAudioDevice extends CoreAudioDevice {
    public static final int DEFAULT_MILLIS_PER_BUFFER = 20;
    public static final double DEFAULT_SAMPLE_RATE = 44100.0d;

    public static native int countInputChannels(String str);

    public static native int countOutputChannels(String str);

    public static native byte[] getDefaultInputDeviceUIDBytes();

    public static native byte[] getDefaultOutputDeviceUIDBytes();

    public static native String[] getDeviceUIDList();

    public static native float getMaximalNominalSampleRate(String str, boolean z, boolean z2);

    public static native float getMinimalNominalSampleRate(String str, boolean z, boolean z2);

    public static native float getNominalSampleRate(String str, boolean z, boolean z2);

    public static native byte[] getTransportTypeBytes(String str);

    public static native boolean isInputDevice(String str);

    public static native boolean isOutputDevice(String str);

    public static native long startStream(String str, Object obj, float f, int i, int i2, boolean z, boolean z2, boolean z3, boolean z4, boolean z5);

    public static native void stopStream(String str, long j);

    public static String getTransportType(String deviceUID) {
        if (deviceUID != null) {
            return StringUtils.newString(getTransportTypeBytes(deviceUID));
        }
        throw new NullPointerException("deviceUID");
    }

    public static String getDefaultInputDeviceUID() {
        return StringUtils.newString(getDefaultInputDeviceUIDBytes());
    }

    public static String getDefaultOutputDeviceUID() {
        return StringUtils.newString(getDefaultOutputDeviceUIDBytes());
    }
}
