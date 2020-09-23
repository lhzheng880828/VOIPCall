package org.jitsi.bouncycastle.operator.bc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jitsi.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.crypto.ExtendedDigest;
import org.jitsi.bouncycastle.crypto.digests.GOST3411Digest;
import org.jitsi.bouncycastle.crypto.digests.MD2Digest;
import org.jitsi.bouncycastle.crypto.digests.MD4Digest;
import org.jitsi.bouncycastle.crypto.digests.MD5Digest;
import org.jitsi.bouncycastle.crypto.digests.RIPEMD128Digest;
import org.jitsi.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.jitsi.bouncycastle.crypto.digests.RIPEMD256Digest;
import org.jitsi.bouncycastle.crypto.digests.SHA1Digest;
import org.jitsi.bouncycastle.crypto.digests.SHA224Digest;
import org.jitsi.bouncycastle.crypto.digests.SHA256Digest;
import org.jitsi.bouncycastle.crypto.digests.SHA384Digest;
import org.jitsi.bouncycastle.crypto.digests.SHA512Digest;
import org.jitsi.bouncycastle.operator.OperatorCreationException;

public class BcDefaultDigestProvider implements BcDigestProvider {
    public static final BcDigestProvider INSTANCE = new BcDefaultDigestProvider();
    private static final Map lookup = createTable();

    private BcDefaultDigestProvider() {
    }

    private static Map createTable() {
        HashMap hashMap = new HashMap();
        hashMap.put(OIWObjectIdentifiers.idSHA1, new BcDigestProvider() {
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new SHA1Digest();
            }
        });
        hashMap.put(NISTObjectIdentifiers.id_sha224, new BcDigestProvider() {
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new SHA224Digest();
            }
        });
        hashMap.put(NISTObjectIdentifiers.id_sha256, new BcDigestProvider() {
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new SHA256Digest();
            }
        });
        hashMap.put(NISTObjectIdentifiers.id_sha384, new BcDigestProvider() {
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new SHA384Digest();
            }
        });
        hashMap.put(NISTObjectIdentifiers.id_sha512, new BcDigestProvider() {
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new SHA512Digest();
            }
        });
        hashMap.put(PKCSObjectIdentifiers.md5, new BcDigestProvider() {
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new MD5Digest();
            }
        });
        hashMap.put(PKCSObjectIdentifiers.md4, new BcDigestProvider() {
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new MD4Digest();
            }
        });
        hashMap.put(PKCSObjectIdentifiers.md2, new BcDigestProvider() {
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new MD2Digest();
            }
        });
        hashMap.put(CryptoProObjectIdentifiers.gostR3411, new BcDigestProvider() {
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new GOST3411Digest();
            }
        });
        hashMap.put(TeleTrusTObjectIdentifiers.ripemd128, new BcDigestProvider() {
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new RIPEMD128Digest();
            }
        });
        hashMap.put(TeleTrusTObjectIdentifiers.ripemd160, new BcDigestProvider() {
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new RIPEMD160Digest();
            }
        });
        hashMap.put(TeleTrusTObjectIdentifiers.ripemd256, new BcDigestProvider() {
            public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) {
                return new RIPEMD256Digest();
            }
        });
        return Collections.unmodifiableMap(hashMap);
    }

    public ExtendedDigest get(AlgorithmIdentifier algorithmIdentifier) throws OperatorCreationException {
        BcDigestProvider bcDigestProvider = (BcDigestProvider) lookup.get(algorithmIdentifier.getAlgorithm());
        if (bcDigestProvider != null) {
            return bcDigestProvider.get(algorithmIdentifier);
        }
        throw new OperatorCreationException("cannot recognise digest");
    }
}
