package org.jitsi.bouncycastle.openssl;

import java.io.IOException;

interface PEMKeyPairParser {
    PEMKeyPair parse(byte[] bArr) throws IOException;
}
