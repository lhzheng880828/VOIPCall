package org.jitsi.bouncycastle.operator;

import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;

public interface KeyWrapper {
    byte[] generateWrappedKey(GenericKey genericKey) throws OperatorException;

    AlgorithmIdentifier getAlgorithmIdentifier();
}
