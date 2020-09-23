package org.jitsi.bouncycastle.operator.bc;

import java.security.SecureRandom;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.crypto.AsymmetricBlockCipher;
import org.jitsi.bouncycastle.crypto.InvalidCipherTextException;
import org.jitsi.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.jitsi.bouncycastle.crypto.params.ParametersWithRandom;
import org.jitsi.bouncycastle.operator.AsymmetricKeyWrapper;
import org.jitsi.bouncycastle.operator.GenericKey;
import org.jitsi.bouncycastle.operator.OperatorException;

public abstract class BcAsymmetricKeyWrapper extends AsymmetricKeyWrapper {
    private AsymmetricKeyParameter publicKey;
    private SecureRandom random;

    public BcAsymmetricKeyWrapper(AlgorithmIdentifier algorithmIdentifier, AsymmetricKeyParameter asymmetricKeyParameter) {
        super(algorithmIdentifier);
        this.publicKey = asymmetricKeyParameter;
    }

    public abstract AsymmetricBlockCipher createAsymmetricWrapper(ASN1ObjectIdentifier aSN1ObjectIdentifier);

    public byte[] generateWrappedKey(GenericKey genericKey) throws OperatorException {
        AsymmetricBlockCipher createAsymmetricWrapper = createAsymmetricWrapper(getAlgorithmIdentifier().getAlgorithm());
        AsymmetricKeyParameter asymmetricKeyParameter = this.publicKey;
        if (this.random != null) {
            ParametersWithRandom parametersWithRandom = new ParametersWithRandom(asymmetricKeyParameter, this.random);
        }
        try {
            byte[] keyBytes = OperatorUtils.getKeyBytes(genericKey);
            createAsymmetricWrapper.init(true, this.publicKey);
            return createAsymmetricWrapper.processBlock(keyBytes, 0, keyBytes.length);
        } catch (InvalidCipherTextException e) {
            throw new OperatorException("unable to encrypt contents key", e);
        }
    }

    public BcAsymmetricKeyWrapper setSecureRandom(SecureRandom secureRandom) {
        this.random = secureRandom;
        return this;
    }
}
