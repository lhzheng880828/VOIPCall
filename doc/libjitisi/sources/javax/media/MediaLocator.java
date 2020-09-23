package javax.media;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

public class MediaLocator implements Serializable {
    private static final long serialVersionUID = -6747425113475481405L;
    private String locatorString;

    public MediaLocator(URL url) {
        this.locatorString = url.toExternalForm();
    }

    public MediaLocator(String locatorString) {
        if (locatorString == null) {
            throw new NullPointerException("locatorString");
        }
        this.locatorString = locatorString;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MediaLocator)) {
            return false;
        }
        MediaLocator ml = (MediaLocator) obj;
        if (this.locatorString != null) {
            z = this.locatorString.equals(ml.locatorString);
        } else if (ml.locatorString != null) {
            z = false;
        }
        return z;
    }

    public String getProtocol() {
        int colonIndex = this.locatorString.indexOf(58);
        if (colonIndex < 0) {
            return "";
        }
        return this.locatorString.substring(0, colonIndex);
    }

    public String getRemainder() {
        int colonIndex = this.locatorString.indexOf(58);
        if (colonIndex < 0) {
            return "";
        }
        return this.locatorString.substring(colonIndex + 1);
    }

    public URL getURL() throws MalformedURLException {
        return new URL(this.locatorString);
    }

    public int hashCode() {
        return this.locatorString == null ? 0 : this.locatorString.hashCode();
    }

    public String toExternalForm() {
        return this.locatorString;
    }

    public String toString() {
        return this.locatorString;
    }
}
