package org.jitsi.bouncycastle.operator;

import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;

public interface InputDecryptorProvider {
    InputDecryptor get(AlgorithmIdentifier algorithmIdentifier) throws OperatorCreationException;
}
