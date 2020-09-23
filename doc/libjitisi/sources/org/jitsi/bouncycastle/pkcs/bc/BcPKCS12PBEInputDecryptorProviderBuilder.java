package org.jitsi.bouncycastle.pkcs.bc;

import java.io.InputStream;
import org.jitsi.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.crypto.ExtendedDigest;
import org.jitsi.bouncycastle.crypto.digests.SHA1Digest;
import org.jitsi.bouncycastle.crypto.generators.PKCS12ParametersGenerator;
import org.jitsi.bouncycastle.crypto.io.CipherInputStream;
import org.jitsi.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.jitsi.bouncycastle.operator.GenericKey;
import org.jitsi.bouncycastle.operator.InputDecryptor;
import org.jitsi.bouncycastle.operator.InputDecryptorProvider;

public class BcPKCS12PBEInputDecryptorProviderBuilder {
    /* access modifiers changed from: private */
    public ExtendedDigest digest;

    public BcPKCS12PBEInputDecryptorProviderBuilder() {
        this(new SHA1Digest());
    }

    public BcPKCS12PBEInputDecryptorProviderBuilder(ExtendedDigest extendedDigest) {
        this.digest = extendedDigest;
    }

    public InputDecryptorProvider build(final char[] cArr) {
        return new InputDecryptorProvider() {
            public InputDecryptor get(final AlgorithmIdentifier algorithmIdentifier) {
                final PaddedBufferedBlockCipher engine = PKCS12PBEUtils.getEngine(algorithmIdentifier.getAlgorithm());
                engine.init(false, PKCS12PBEUtils.createCipherParameters(algorithmIdentifier.getAlgorithm(), BcPKCS12PBEInputDecryptorProviderBuilder.this.digest, engine.getBlockSize(), PKCS12PBEParams.getInstance(algorithmIdentifier.getParameters()), cArr));
                return new InputDecryptor() {
                    public AlgorithmIdentifier getAlgorithmIdentifier() {
                        return algorithmIdentifier;
                    }

                    public InputStream getInputStream(InputStream inputStream) {
                        return new CipherInputStream(inputStream, engine);
                    }

                    public GenericKey getKey() {
                        return new GenericKey(PKCS12ParametersGenerator.PKCS12PasswordToBytes(cArr));
                    }
                };
            }
        };
    }
}
