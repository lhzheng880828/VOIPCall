package org.jitsi.bouncycastle.operator.bc;

import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.crypto.Signer;
import org.jitsi.bouncycastle.crypto.signers.RSADigestSigner;
import org.jitsi.bouncycastle.operator.OperatorCreationException;

public class BcRSAContentSignerBuilder extends BcContentSignerBuilder {
    public BcRSAContentSignerBuilder(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2) {
        super(algorithmIdentifier, algorithmIdentifier2);
    }

    /* access modifiers changed from: protected */
    public Signer createSigner(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2) throws OperatorCreationException {
        return new RSADigestSigner(this.digestProvider.get(algorithmIdentifier2));
    }
}
