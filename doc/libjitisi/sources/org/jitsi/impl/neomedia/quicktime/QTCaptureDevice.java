package org.jitsi.impl.neomedia.quicktime;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class QTCaptureDevice extends NSObject {
    private static final QTFormatDescription[] NO_FORMAT_DESCRIPTIONS = new QTFormatDescription[0];
    private static final QTCaptureDevice[] NO_INPUT_DEVICES = new QTCaptureDevice[0];
    private static final Map<QTMediaType, List<QTCaptureDevice>> inputDevices = new HashMap();

    private static native void close(long j);

    private static native long[] formatDescriptions(long j);

    private static native long[] inputDevicesWithMediaType(String str);

    private static native boolean isConnected(long j);

    private static native String localizedDisplayName(long j);

    private static native boolean open(long j) throws NSErrorException;

    private static native String uniqueID(long j);

    public QTCaptureDevice(long ptr) {
        super(ptr);
    }

    public void close() {
        close(getPtr());
    }

    public static QTCaptureDevice deviceWithUniqueID(String deviceUID) {
        QTCaptureDevice deviceWithUniqueID = deviceWithUniqueID(deviceUID, inputDevicesWithMediaType(QTMediaType.Video));
        if (deviceWithUniqueID == null) {
            return deviceWithUniqueID(deviceUID, inputDevicesWithMediaType(QTMediaType.Sound));
        }
        return deviceWithUniqueID;
    }

    private static QTCaptureDevice deviceWithUniqueID(String deviceUID, QTCaptureDevice[] inputDevices) {
        if (inputDevices != null) {
            for (QTCaptureDevice inputDevice : inputDevices) {
                if (deviceUID.equals(inputDevice.uniqueID())) {
                    return inputDevice;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        release();
    }

    public QTFormatDescription[] formatDescriptions() {
        long[] formatDescriptionPtrs = formatDescriptions(getPtr());
        if (formatDescriptionPtrs == null) {
            return NO_FORMAT_DESCRIPTIONS;
        }
        int formatDescriptionCount = formatDescriptionPtrs.length;
        if (formatDescriptionCount == 0) {
            return NO_FORMAT_DESCRIPTIONS;
        }
        QTFormatDescription[] formatDescriptions = new QTFormatDescription[formatDescriptionCount];
        for (int i = 0; i < formatDescriptionCount; i++) {
            formatDescriptions[i] = new QTFormatDescription(formatDescriptionPtrs[i]);
        }
        return formatDescriptions;
    }

    public static QTCaptureDevice[] inputDevicesWithMediaType(QTMediaType mediaType) {
        long[] inputDevicePtrs = inputDevicesWithMediaType(mediaType.name());
        int inputDeviceCount = inputDevicePtrs == null ? 0 : inputDevicePtrs.length;
        QTCaptureDevice[] inputDevicesWithMediaType;
        if (inputDeviceCount == 0) {
            inputDevicesWithMediaType = NO_INPUT_DEVICES;
            inputDevices.remove(mediaType);
            return inputDevicesWithMediaType;
        }
        long inputDevicePtr;
        inputDevicesWithMediaType = new QTCaptureDevice[inputDeviceCount];
        List<QTCaptureDevice> cachedInputDevicesWithMediaType = (List) inputDevices.get(mediaType);
        if (cachedInputDevicesWithMediaType == null) {
            cachedInputDevicesWithMediaType = new LinkedList();
            inputDevices.put(mediaType, cachedInputDevicesWithMediaType);
        }
        for (int i = 0; i < inputDeviceCount; i++) {
            inputDevicePtr = inputDevicePtrs[i];
            QTCaptureDevice inputDevice = null;
            for (QTCaptureDevice cachedInputDevice : cachedInputDevicesWithMediaType) {
                if (inputDevicePtr == cachedInputDevice.getPtr()) {
                    inputDevice = cachedInputDevice;
                    break;
                }
            }
            if (inputDevice == null) {
                inputDevice = new QTCaptureDevice(inputDevicePtr);
                cachedInputDevicesWithMediaType.add(inputDevice);
            } else {
                NSObject.release(inputDevicePtr);
            }
            inputDevicesWithMediaType[i] = inputDevice;
        }
        Iterator<QTCaptureDevice> cachedInputDeviceIter = cachedInputDevicesWithMediaType.iterator();
        while (cachedInputDeviceIter.hasNext()) {
            long cachedInputDevicePtr = ((QTCaptureDevice) cachedInputDeviceIter.next()).getPtr();
            boolean remove = true;
            for (long inputDevicePtr2 : inputDevicePtrs) {
                if (cachedInputDevicePtr == inputDevicePtr2) {
                    remove = false;
                    break;
                }
            }
            if (remove) {
                cachedInputDeviceIter.remove();
            }
        }
        return inputDevicesWithMediaType;
    }

    public boolean isConnected() {
        return isConnected(getPtr());
    }

    public String localizedDisplayName() {
        return localizedDisplayName(getPtr());
    }

    public boolean open() throws NSErrorException {
        return open(getPtr());
    }

    public String uniqueID() {
        return uniqueID(getPtr());
    }
}
