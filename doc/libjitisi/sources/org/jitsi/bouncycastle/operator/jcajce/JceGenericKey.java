package org.jitsi.bouncycastle.operator.jcajce;

import java.security.Key;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.operator.GenericKey;

public class JceGenericKey extends GenericKey {
    public JceGenericKey(AlgorithmIdentifier algorithmIdentifier, Key key) {
        super(algorithmIdentifier, getRepresentation(key));
    }

    private static Object getRepresentation(Key key) {
        Object encoded = key.getEncoded();
        return encoded != null ? encoded : key;
    }
}
