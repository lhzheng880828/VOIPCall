package org.jitsi.bouncycastle.openssl;

public interface PEMDecryptor {
    byte[] decrypt(byte[] bArr, byte[] bArr2) throws PEMException;
}
