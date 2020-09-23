package org.jitsi.service.neomedia;

public enum SrtpControlType {
    DTLS_SRTP("DTLS-SRTP"),
    MIKEY("MIKEY"),
    SDES("SDES"),
    ZRTP("ZRTP");
    
    private final String protoName;

    private SrtpControlType(String protoName) {
        this.protoName = protoName;
    }

    public String toString() {
        return this.protoName;
    }
}
