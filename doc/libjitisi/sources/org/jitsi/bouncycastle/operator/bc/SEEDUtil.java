package org.jitsi.bouncycastle.operator.bc;

import org.jitsi.bouncycastle.asn1.kisa.KISAObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;

class SEEDUtil {
    SEEDUtil() {
    }

    static AlgorithmIdentifier determineKeyEncAlg() {
        return new AlgorithmIdentifier(KISAObjectIdentifiers.id_npki_app_cmsSeed_wrap);
    }
}
