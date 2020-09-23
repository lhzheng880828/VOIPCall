package org.jitsi.bouncycastle.cms;

import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;

public interface CMSSignatureEncryptionAlgorithmFinder {
    AlgorithmIdentifier findEncryptionAlgorithm(AlgorithmIdentifier algorithmIdentifier);
}
