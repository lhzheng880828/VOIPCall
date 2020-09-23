package org.jitsi.bouncycastle.operator.bc;

import java.io.IOException;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.jitsi.bouncycastle.crypto.Signer;
import org.jitsi.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.jitsi.bouncycastle.crypto.signers.RSADigestSigner;
import org.jitsi.bouncycastle.crypto.util.PublicKeyFactory;
import org.jitsi.bouncycastle.operator.DigestAlgorithmIdentifierFinder;
import org.jitsi.bouncycastle.operator.OperatorCreationException;

public class BcRSAContentVerifierProviderBuilder extends BcContentVerifierProviderBuilder {
    private DigestAlgorithmIdentifierFinder digestAlgorithmFinder;

    public BcRSAContentVerifierProviderBuilder(DigestAlgorithmIdentifierFinder digestAlgorithmIdentifierFinder) {
        this.digestAlgorithmFinder = digestAlgorithmIdentifierFinder;
    }

    /* access modifiers changed from: protected */
    public Signer createSigner(AlgorithmIdentifier algorithmIdentifier) throws OperatorCreationException {
        return new RSADigestSigner(this.digestProvider.get(this.digestAlgorithmFinder.find(algorithmIdentifier)));
    }

    /* access modifiers changed from: protected */
    public AsymmetricKeyParameter extractKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo) throws IOException {
        return PublicKeyFactory.createKey(subjectPublicKeyInfo);
    }
}
