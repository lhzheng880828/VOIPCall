package org.jitsi.bouncycastle.operator.bc;

import java.io.OutputStream;
import java.security.SecureRandom;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.crypto.CryptoException;
import org.jitsi.bouncycastle.crypto.Signer;
import org.jitsi.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.jitsi.bouncycastle.crypto.params.ParametersWithRandom;
import org.jitsi.bouncycastle.operator.ContentSigner;
import org.jitsi.bouncycastle.operator.OperatorCreationException;
import org.jitsi.bouncycastle.operator.RuntimeOperatorException;

public abstract class BcContentSignerBuilder {
    private AlgorithmIdentifier digAlgId;
    protected BcDigestProvider digestProvider = BcDefaultDigestProvider.INSTANCE;
    private SecureRandom random;
    /* access modifiers changed from: private */
    public AlgorithmIdentifier sigAlgId;

    public BcContentSignerBuilder(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2) {
        this.sigAlgId = algorithmIdentifier;
        this.digAlgId = algorithmIdentifier2;
    }

    public ContentSigner build(AsymmetricKeyParameter asymmetricKeyParameter) throws OperatorCreationException {
        final Signer createSigner = createSigner(this.sigAlgId, this.digAlgId);
        if (this.random != null) {
            createSigner.init(true, new ParametersWithRandom(asymmetricKeyParameter, this.random));
        } else {
            createSigner.init(true, asymmetricKeyParameter);
        }
        return new ContentSigner() {
            private BcSignerOutputStream stream = new BcSignerOutputStream(createSigner);

            public AlgorithmIdentifier getAlgorithmIdentifier() {
                return BcContentSignerBuilder.this.sigAlgId;
            }

            public OutputStream getOutputStream() {
                return this.stream;
            }

            public byte[] getSignature() {
                try {
                    return this.stream.getSignature();
                } catch (CryptoException e) {
                    throw new RuntimeOperatorException("exception obtaining signature: " + e.getMessage(), e);
                }
            }
        };
    }

    public abstract Signer createSigner(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2) throws OperatorCreationException;

    public BcContentSignerBuilder setSecureRandom(SecureRandom secureRandom) {
        this.random = secureRandom;
        return this;
    }
}
