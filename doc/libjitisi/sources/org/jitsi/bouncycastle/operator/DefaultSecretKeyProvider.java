package org.jitsi.bouncycastle.operator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.ntt.NTTObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.cms.CMSEnvelopedGenerator;
import org.jitsi.bouncycastle.util.Integers;

public class DefaultSecretKeyProvider implements SecretKeySizeProvider {
    public static final SecretKeySizeProvider INSTANCE = new DefaultSecretKeyProvider();
    private static final Map KEY_SIZES;

    static {
        HashMap hashMap = new HashMap();
        hashMap.put(new ASN1ObjectIdentifier(CMSEnvelopedGenerator.CAST5_CBC), Integers.valueOf(128));
        hashMap.put(PKCSObjectIdentifiers.des_EDE3_CBC.getId(), Integers.valueOf(192));
        hashMap.put(NISTObjectIdentifiers.id_aes128_CBC, Integers.valueOf(128));
        hashMap.put(NISTObjectIdentifiers.id_aes192_CBC, Integers.valueOf(192));
        hashMap.put(NISTObjectIdentifiers.id_aes256_CBC, Integers.valueOf(256));
        hashMap.put(NTTObjectIdentifiers.id_camellia128_cbc, Integers.valueOf(128));
        hashMap.put(NTTObjectIdentifiers.id_camellia192_cbc, Integers.valueOf(192));
        hashMap.put(NTTObjectIdentifiers.id_camellia256_cbc, Integers.valueOf(256));
        KEY_SIZES = Collections.unmodifiableMap(hashMap);
    }

    public int getKeySize(AlgorithmIdentifier algorithmIdentifier) {
        Integer num = (Integer) KEY_SIZES.get(algorithmIdentifier.getAlgorithm());
        return num != null ? num.intValue() : -1;
    }
}
