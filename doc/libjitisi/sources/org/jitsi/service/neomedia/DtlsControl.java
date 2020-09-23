package org.jitsi.service.neomedia;

import java.util.Map;

public interface DtlsControl extends SrtpControl {
    public static final String PROTO_NAME = SrtpControlType.DTLS_SRTP.toString();
    public static final String UDP_TLS_RTP_SAVP = "UDP/TLS/RTP/SAVP";
    public static final String UDP_TLS_RTP_SAVPF = "UDP/TLS/RTP/SAVPF";

    public enum Setup {
        ACTIVE,
        ACTPASS,
        HOLDCONN,
        PASSIVE;

        public static Setup parseSetup(String s) {
            if (s == null) {
                throw new NullPointerException("s");
            }
            for (Setup v : values()) {
                if (v.toString().equalsIgnoreCase(s)) {
                    return v;
                }
            }
            throw new IllegalArgumentException(s);
        }

        public String toString() {
            return name().toLowerCase();
        }
    }

    String getLocalFingerprint();

    String getLocalFingerprintHashFunction();

    void setRemoteFingerprints(Map<String, String> map);

    void setSetup(Setup setup);
}
