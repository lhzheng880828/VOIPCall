package org.jitsi.bouncycastle.operator.bc;

import org.jitsi.bouncycastle.crypto.engines.AESWrapEngine;
import org.jitsi.bouncycastle.crypto.params.KeyParameter;

public class BcAESSymmetricKeyWrapper extends BcSymmetricKeyWrapper {
    public BcAESSymmetricKeyWrapper(KeyParameter keyParameter) {
        super(AESUtil.determineKeyEncAlg(keyParameter), new AESWrapEngine(), keyParameter);
    }
}
