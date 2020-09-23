package org.jitsi.bouncycastle.openssl;

import org.jitsi.bouncycastle.operator.OperatorCreationException;

public interface PEMDecryptorProvider {
    PEMDecryptor get(String str) throws OperatorCreationException;
}
