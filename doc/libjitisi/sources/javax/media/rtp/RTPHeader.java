package javax.media.rtp;

import java.io.Serializable;

public class RTPHeader implements Serializable {
    public static final int VALUE_NOT_SET = -1;
    private byte[] extension;
    private boolean extensionPresent;
    private int extensionType = -1;

    public RTPHeader(boolean extensionPresent, int extensionType, byte[] extension) {
        this.extensionPresent = extensionPresent;
        this.extensionType = extensionType;
        this.extension = extension;
    }

    public RTPHeader(int marker) {
    }

    public byte[] getExtension() {
        return this.extension;
    }

    public int getExtensionType() {
        return this.extensionType;
    }

    public boolean isExtensionPresent() {
        return this.extensionPresent;
    }

    public void setExtension(byte[] e) {
        this.extension = e;
    }

    public void setExtensionPresent(boolean p) {
        this.extensionPresent = p;
    }

    public void setExtensionType(int t) {
        this.extensionType = t;
    }
}
