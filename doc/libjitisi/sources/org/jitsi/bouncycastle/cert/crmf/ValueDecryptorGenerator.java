package org.jitsi.bouncycastle.cert.crmf;

import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.operator.InputDecryptor;

public interface ValueDecryptorGenerator {
    InputDecryptor getValueDecryptor(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2, byte[] bArr) throws CRMFException;
}
