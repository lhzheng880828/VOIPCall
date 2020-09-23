package javax.media;

import java.io.Serializable;
import java.util.Arrays;

public class CaptureDeviceInfo implements Serializable {
    protected Format[] formats;
    protected MediaLocator locator;
    protected String name;

    public CaptureDeviceInfo(String name, MediaLocator locator, Format[] formats) {
        this.name = name;
        this.locator = locator;
        this.formats = formats;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CaptureDeviceInfo)) {
            return false;
        }
        CaptureDeviceInfo cdi = (CaptureDeviceInfo) obj;
        String name = getName();
        String cdiName = cdi.getName();
        if (name == null) {
            if (cdiName != null) {
                return false;
            }
        } else if (!name.equals(cdiName)) {
            return false;
        }
        MediaLocator locator = getLocator();
        MediaLocator cdiLocator = cdi.getLocator();
        if (locator == null) {
            if (cdiLocator != null) {
                return false;
            }
        } else if (!locator.equals(cdiLocator)) {
            return false;
        }
        return Arrays.equals(getFormats(), cdi.getFormats());
    }

    public Format[] getFormats() {
        return this.formats;
    }

    public MediaLocator getLocator() {
        return this.locator;
    }

    public String getName() {
        return this.name;
    }

    public int hashCode() {
        int hashCode = 0;
        String name = getName();
        if (name != null) {
            hashCode = 0 + name.hashCode();
        }
        MediaLocator locator = getLocator();
        if (locator != null) {
            hashCode += locator.hashCode();
        }
        Format[] formats = getFormats();
        if (formats != null) {
            for (Format format : formats) {
                if (format != null) {
                    hashCode += format.hashCode();
                }
            }
        }
        return hashCode;
    }

    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append(this.name);
        b.append(" : ");
        b.append(this.locator);
        b.append("\n");
        if (this.formats != null) {
            for (Object append : this.formats) {
                b.append(append);
                b.append("\n");
            }
        }
        return b.toString();
    }
}
