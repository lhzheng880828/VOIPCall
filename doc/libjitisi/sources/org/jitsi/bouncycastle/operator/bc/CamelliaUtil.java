package org.jitsi.bouncycastle.operator.bc;

import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.ntt.NTTObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.crypto.params.KeyParameter;

class CamelliaUtil {
    CamelliaUtil() {
    }

    static AlgorithmIdentifier determineKeyEncAlg(KeyParameter keyParameter) {
        ASN1ObjectIdentifier aSN1ObjectIdentifier;
        int length = keyParameter.getKey().length * 8;
        if (length == 128) {
            aSN1ObjectIdentifier = NTTObjectIdentifiers.id_camellia128_wrap;
        } else if (length == 192) {
            aSN1ObjectIdentifier = NTTObjectIdentifiers.id_camellia192_wrap;
        } else if (length == 256) {
            aSN1ObjectIdentifier = NTTObjectIdentifiers.id_camellia256_wrap;
        } else {
            throw new IllegalArgumentException("illegal keysize in Camellia");
        }
        return new AlgorithmIdentifier(aSN1ObjectIdentifier);
    }
}
