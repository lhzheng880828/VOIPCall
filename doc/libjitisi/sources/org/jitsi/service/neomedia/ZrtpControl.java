package org.jitsi.service.neomedia;

public interface ZrtpControl extends SrtpControl {
    public static final String PROTO_NAME = SrtpControlType.ZRTP.toString();

    String getCipherString();

    int getCurrentProtocolVersion();

    String getHelloHash(int i);

    String[] getHelloHashSep(int i);

    int getNumberSupportedVersions();

    String getPeerHelloHash();

    byte[] getPeerZid();

    String getPeerZidString();

    String getSecurityString();

    long getTimeoutValue();

    boolean isSecurityVerified();

    void setSASVerification(boolean z);
}
