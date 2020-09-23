package org.jitsi.bouncycastle.operator.bc;

import java.security.SecureRandom;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.crypto.Wrapper;
import org.jitsi.bouncycastle.crypto.params.KeyParameter;
import org.jitsi.bouncycastle.crypto.params.ParametersWithRandom;
import org.jitsi.bouncycastle.operator.GenericKey;
import org.jitsi.bouncycastle.operator.OperatorException;
import org.jitsi.bouncycastle.operator.SymmetricKeyWrapper;

public class BcSymmetricKeyWrapper extends SymmetricKeyWrapper {
    private SecureRandom random;
    private Wrapper wrapper;
    private KeyParameter wrappingKey;

    public BcSymmetricKeyWrapper(AlgorithmIdentifier algorithmIdentifier, Wrapper wrapper, KeyParameter keyParameter) {
        super(algorithmIdentifier);
        this.wrapper = wrapper;
        this.wrappingKey = keyParameter;
    }

    public byte[] generateWrappedKey(GenericKey genericKey) throws OperatorException {
        byte[] keyBytes = OperatorUtils.getKeyBytes(genericKey);
        if (this.random == null) {
            this.wrapper.init(true, this.wrappingKey);
        } else {
            this.wrapper.init(true, new ParametersWithRandom(this.wrappingKey, this.random));
        }
        return this.wrapper.wrap(keyBytes, 0, keyBytes.length);
    }

    public BcSymmetricKeyWrapper setSecureRandom(SecureRandom secureRandom) {
        this.random = secureRandom;
        return this;
    }
}
