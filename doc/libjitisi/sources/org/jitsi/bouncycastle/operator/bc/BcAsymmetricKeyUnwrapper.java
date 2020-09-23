package org.jitsi.bouncycastle.operator.bc;

import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.crypto.AsymmetricBlockCipher;
import org.jitsi.bouncycastle.crypto.InvalidCipherTextException;
import org.jitsi.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.jitsi.bouncycastle.operator.AsymmetricKeyUnwrapper;
import org.jitsi.bouncycastle.operator.GenericKey;
import org.jitsi.bouncycastle.operator.OperatorException;

public abstract class BcAsymmetricKeyUnwrapper extends AsymmetricKeyUnwrapper {
    private AsymmetricKeyParameter privateKey;

    public BcAsymmetricKeyUnwrapper(AlgorithmIdentifier algorithmIdentifier, AsymmetricKeyParameter asymmetricKeyParameter) {
        super(algorithmIdentifier);
        this.privateKey = asymmetricKeyParameter;
    }

    public abstract AsymmetricBlockCipher createAsymmetricUnwrapper(ASN1ObjectIdentifier aSN1ObjectIdentifier);

    public GenericKey generateUnwrappedKey(AlgorithmIdentifier algorithmIdentifier, byte[] bArr) throws OperatorException {
        AsymmetricBlockCipher createAsymmetricUnwrapper = createAsymmetricUnwrapper(getAlgorithmIdentifier().getAlgorithm());
        createAsymmetricUnwrapper.init(false, this.privateKey);
        try {
            byte[] processBlock = createAsymmetricUnwrapper.processBlock(bArr, 0, bArr.length);
            return algorithmIdentifier.getAlgorithm().equals(PKCSObjectIdentifiers.des_EDE3_CBC) ? new GenericKey(algorithmIdentifier, processBlock) : new GenericKey(algorithmIdentifier, processBlock);
        } catch (InvalidCipherTextException e) {
            throw new OperatorException("unable to recover secret key: " + e.getMessage(), e);
        }
    }
}
