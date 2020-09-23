package org.jitsi.bouncycastle.operator.bc;

import java.security.SecureRandom;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.crypto.InvalidCipherTextException;
import org.jitsi.bouncycastle.crypto.Wrapper;
import org.jitsi.bouncycastle.crypto.params.KeyParameter;
import org.jitsi.bouncycastle.operator.GenericKey;
import org.jitsi.bouncycastle.operator.OperatorException;
import org.jitsi.bouncycastle.operator.SymmetricKeyUnwrapper;

public class BcSymmetricKeyUnwrapper extends SymmetricKeyUnwrapper {
    private SecureRandom random;
    private Wrapper wrapper;
    private KeyParameter wrappingKey;

    public BcSymmetricKeyUnwrapper(AlgorithmIdentifier algorithmIdentifier, Wrapper wrapper, KeyParameter keyParameter) {
        super(algorithmIdentifier);
        this.wrapper = wrapper;
        this.wrappingKey = keyParameter;
    }

    public GenericKey generateUnwrappedKey(AlgorithmIdentifier algorithmIdentifier, byte[] bArr) throws OperatorException {
        this.wrapper.init(false, this.wrappingKey);
        try {
            return new GenericKey(algorithmIdentifier, this.wrapper.unwrap(bArr, 0, bArr.length));
        } catch (InvalidCipherTextException e) {
            throw new OperatorException("unable to unwrap key: " + e.getMessage(), e);
        }
    }

    public BcSymmetricKeyUnwrapper setSecureRandom(SecureRandom secureRandom) {
        this.random = secureRandom;
        return this;
    }
}
