package javax.media.cdm;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import javax.media.CaptureDeviceInfo;
import javax.media.Format;
import net.sf.fmj.registry.Registry;

public class CaptureDeviceManager extends javax.media.CaptureDeviceManager {
    public static synchronized boolean addDevice(CaptureDeviceInfo newDevice) {
        boolean addDevice;
        synchronized (CaptureDeviceManager.class) {
            addDevice = Registry.getInstance().addDevice(newDevice);
        }
        return addDevice;
    }

    public static synchronized void commit() throws IOException {
        synchronized (CaptureDeviceManager.class) {
            Registry.getInstance().commit();
        }
    }

    public static synchronized CaptureDeviceInfo getDevice(String deviceName) {
        CaptureDeviceInfo captureDeviceInfo;
        synchronized (CaptureDeviceManager.class) {
            Iterator i$ = getDeviceList().iterator();
            while (i$.hasNext()) {
                captureDeviceInfo = (CaptureDeviceInfo) i$.next();
                if (captureDeviceInfo.getName().equals(deviceName)) {
                    break;
                }
            }
            captureDeviceInfo = null;
        }
        return captureDeviceInfo;
    }

    public static synchronized Vector<CaptureDeviceInfo> getDeviceList() {
        Vector deviceList;
        synchronized (CaptureDeviceManager.class) {
            deviceList = Registry.getInstance().getDeviceList();
        }
        return deviceList;
    }

    public static synchronized Vector<CaptureDeviceInfo> getDeviceList(Format format) {
        Vector<CaptureDeviceInfo> result;
        synchronized (CaptureDeviceManager.class) {
            result = new Vector();
            Iterator it = getDeviceList().iterator();
            while (it.hasNext()) {
                CaptureDeviceInfo captureDeviceInfo = (CaptureDeviceInfo) it.next();
                if (format == null) {
                    result.add(captureDeviceInfo);
                } else {
                    for (Format aFormat : captureDeviceInfo.getFormats()) {
                        if (format.matches(aFormat)) {
                            result.add(captureDeviceInfo);
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    public static synchronized boolean removeDevice(CaptureDeviceInfo device) {
        boolean removeDevice;
        synchronized (CaptureDeviceManager.class) {
            removeDevice = Registry.getInstance().removeDevice(device);
        }
        return removeDevice;
    }
}
