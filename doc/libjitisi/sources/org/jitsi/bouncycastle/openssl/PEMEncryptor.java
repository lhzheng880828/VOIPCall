package org.jitsi.bouncycastle.openssl;

public interface PEMEncryptor {
    byte[] encrypt(byte[] bArr) throws PEMException;

    String getAlgorithm();

    byte[] getIV();
}
