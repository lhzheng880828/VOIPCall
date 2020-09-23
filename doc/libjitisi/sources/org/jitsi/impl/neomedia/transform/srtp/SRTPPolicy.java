package org.jitsi.impl.neomedia.transform.srtp;

public class SRTPPolicy {
    public static final int AESCM_ENCRYPTION = 1;
    public static final int AESF8_ENCRYPTION = 2;
    public static final int HMACSHA1_AUTHENTICATION = 1;
    public static final int NULL_AUTHENTICATION = 0;
    public static final int NULL_ENCRYPTION = 0;
    public static final int SKEIN_AUTHENTICATION = 2;
    public static final int TWOFISHF8_ENCRYPTION = 4;
    public static final int TWOFISH_ENCRYPTION = 3;
    private int authKeyLength;
    private int authTagLength;
    private int authType;
    private int encKeyLength;
    private int encType;
    private int saltKeyLength;

    public SRTPPolicy(int encType, int encKeyLength, int authType, int authKeyLength, int authTagLength, int saltKeyLength) {
        this.encType = encType;
        this.encKeyLength = encKeyLength;
        this.authType = authType;
        this.authKeyLength = authKeyLength;
        this.authTagLength = authTagLength;
        this.saltKeyLength = saltKeyLength;
    }

    public int getAuthKeyLength() {
        return this.authKeyLength;
    }

    public void setAuthKeyLength(int authKeyLength) {
        this.authKeyLength = authKeyLength;
    }

    public int getAuthTagLength() {
        return this.authTagLength;
    }

    public void setAuthTagLength(int authTagLength) {
        this.authTagLength = authTagLength;
    }

    public int getAuthType() {
        return this.authType;
    }

    public void setAuthType(int authType) {
        this.authType = authType;
    }

    public int getEncKeyLength() {
        return this.encKeyLength;
    }

    public void setEncKeyLength(int encKeyLength) {
        this.encKeyLength = encKeyLength;
    }

    public int getEncType() {
        return this.encType;
    }

    public void setEncType(int encType) {
        this.encType = encType;
    }

    public int getSaltKeyLength() {
        return this.saltKeyLength;
    }

    public void setSaltKeyLength(int keyLength) {
        this.saltKeyLength = keyLength;
    }
}
