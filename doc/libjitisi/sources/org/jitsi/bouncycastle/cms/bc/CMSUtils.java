package org.jitsi.bouncycastle.cms.bc;

import org.jitsi.bouncycastle.crypto.CipherParameters;
import org.jitsi.bouncycastle.crypto.params.KeyParameter;
import org.jitsi.bouncycastle.operator.GenericKey;

class CMSUtils {
    CMSUtils() {
    }

    static CipherParameters getBcKey(GenericKey genericKey) {
        if (genericKey.getRepresentation() instanceof CipherParameters) {
            return (CipherParameters) genericKey.getRepresentation();
        }
        if (genericKey.getRepresentation() instanceof byte[]) {
            return new KeyParameter((byte[]) genericKey.getRepresentation());
        }
        throw new IllegalArgumentException("unknown generic key type");
    }
}
