package org.jitsi.impl.neomedia.device;

import javax.media.CaptureDeviceInfo;
import javax.media.Format;
import javax.media.MediaLocator;

public class CaptureDeviceInfo2 extends CaptureDeviceInfo {
    private final String modelIdentifier;
    private final String transportType;
    private final String uid;

    public CaptureDeviceInfo2(CaptureDeviceInfo captureDeviceInfo, String uid, String transportType, String modelIdentifier) {
        this(captureDeviceInfo.getName(), captureDeviceInfo.getLocator(), captureDeviceInfo.getFormats(), uid, transportType, modelIdentifier);
    }

    public CaptureDeviceInfo2(String name, MediaLocator locator, Format[] formats, String uid, String transportType, String modelIdentifier) {
        super(name, locator, formats);
        this.uid = uid;
        this.transportType = transportType;
        this.modelIdentifier = modelIdentifier;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CaptureDeviceInfo2)) {
            return false;
        }
        CaptureDeviceInfo2 cdi2 = (CaptureDeviceInfo2) obj;
        MediaLocator locator = getLocator();
        MediaLocator cdi2Locator = cdi2.getLocator();
        if (locator == null) {
            if (cdi2Locator != null) {
                return false;
            }
        } else if (cdi2Locator == null) {
            return false;
        } else {
            String protocol = locator.getProtocol();
            String cdi2Protocol = cdi2Locator.getProtocol();
            if (protocol == null) {
                if (cdi2Protocol != null) {
                    return false;
                }
            } else if (cdi2Protocol == null || !protocol.equals(cdi2Protocol)) {
                return false;
            }
        }
        return getIdentifier().equals(cdi2.getIdentifier());
    }

    public String getIdentifier() {
        return this.uid == null ? this.name : this.uid;
    }

    public String getTransportType() {
        return this.transportType;
    }

    public String getUID() {
        return this.uid;
    }

    public String getModelIdentifier() {
        return this.modelIdentifier == null ? this.name : this.modelIdentifier;
    }

    public int hashCode() {
        return getIdentifier().hashCode();
    }

    public boolean isSameTransportType(String transportType) {
        if (this.transportType == null) {
            return transportType == null;
        } else {
            return this.transportType.equals(transportType);
        }
    }
}
