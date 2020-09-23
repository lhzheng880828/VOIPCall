package org.jitsi.bouncycastle.operator.bc;

import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.crypto.Signer;
import org.jitsi.bouncycastle.crypto.signers.DSADigestSigner;
import org.jitsi.bouncycastle.crypto.signers.DSASigner;
import org.jitsi.bouncycastle.operator.OperatorCreationException;

public class BcDSAContentSignerBuilder extends BcContentSignerBuilder {
    public BcDSAContentSignerBuilder(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2) {
        super(algorithmIdentifier, algorithmIdentifier2);
    }

    /* access modifiers changed from: protected */
    public Signer createSigner(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2) throws OperatorCreationException {
        return new DSADigestSigner(new DSASigner(), this.digestProvider.get(algorithmIdentifier2));
    }
}
