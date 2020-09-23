package org.jitsi.impl.neomedia.device;

import org.jitsi.util.Logger;
import org.jitsi.util.OSUtils;
import org.jitsi.util.StringUtils;

public class CoreAudioDevice {
    private static Runnable devicesChangedCallback;
    public static boolean isLoaded;
    private static final Logger logger = Logger.getLogger(CoreAudioDevice.class);

    public static native void freeDevices();

    public static native byte[] getDeviceModelIdentifierBytes(String str);

    public static native byte[] getDeviceNameBytes(String str);

    public static native float getInputDeviceVolume(String str);

    public static native float getOutputDeviceVolume(String str);

    public static native int initDevices();

    public static native int setInputDeviceVolume(String str, float f);

    public static native int setOutputDeviceVolume(String str, float f);

    static {
        boolean isLoaded = false;
        try {
            if (OSUtils.IS_MAC) {
                System.loadLibrary("jnmaccoreaudio");
                isLoaded = true;
                WebrtcAec.init();
            } else if (OSUtils.IS_WINDOWS_VISTA || OSUtils.IS_WINDOWS_7 || OSUtils.IS_WINDOWS_8) {
                System.loadLibrary("jnwincoreaudio");
                isLoaded = true;
            }
        } catch (NullPointerException npe) {
            logger.info("Failed to load CoreAudioDevice library: ", npe);
        } catch (SecurityException se) {
            logger.info("Failed to load CoreAudioDevice library: ", se);
        } catch (UnsatisfiedLinkError ule) {
            logger.info("Failed to load CoreAudioDevice library: ", ule);
        }
        isLoaded = isLoaded;
    }

    public static String getDeviceModelIdentifier(String deviceUID) {
        if (deviceUID != null) {
            return StringUtils.newString(getDeviceModelIdentifierBytes(deviceUID));
        }
        throw new NullPointerException("deviceUID");
    }

    public static String getDeviceName(String deviceUID) {
        return StringUtils.newString(getDeviceNameBytes(deviceUID));
    }

    public static void devicesChangedCallback() {
        Runnable devicesChangedCallback = devicesChangedCallback;
        if (devicesChangedCallback != null) {
            devicesChangedCallback.run();
        }
    }

    public static void setDevicesChangedCallback(Runnable devicesChangedCallback) {
        devicesChangedCallback = devicesChangedCallback;
    }

    public static void log(byte[] error) {
        logger.info(StringUtils.newString(error));
    }
}
