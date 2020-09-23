package org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi;

public class HResultException extends Exception {
    private final int hresult;

    public HResultException(int hresult) {
        this(hresult, toString(hresult));
    }

    public HResultException(int hresult, String message) {
        super(message);
        this.hresult = hresult;
    }

    public int getHResult() {
        return this.hresult;
    }

    public static String toString(int hresult) {
        return "0x" + Long.toHexString(((long) hresult) & 4294967295L);
    }
}
