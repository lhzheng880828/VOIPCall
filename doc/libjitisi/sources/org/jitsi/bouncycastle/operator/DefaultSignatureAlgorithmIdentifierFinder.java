package org.jitsi.bouncycastle.operator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jitsi.bouncycastle.asn1.ASN1Encodable;
import org.jitsi.bouncycastle.asn1.ASN1Integer;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.DERNull;
import org.jitsi.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.pkcs.RSASSAPSSparams;
import org.jitsi.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.jitsi.bouncycastle.util.Strings;

public class DefaultSignatureAlgorithmIdentifierFinder implements SignatureAlgorithmIdentifierFinder {
    private static final ASN1ObjectIdentifier ENCRYPTION_DSA = X9ObjectIdentifiers.id_dsa_with_sha1;
    private static final ASN1ObjectIdentifier ENCRYPTION_ECDSA = X9ObjectIdentifiers.ecdsa_with_SHA1;
    private static final ASN1ObjectIdentifier ENCRYPTION_ECGOST3410 = CryptoProObjectIdentifiers.gostR3410_2001;
    private static final ASN1ObjectIdentifier ENCRYPTION_GOST3410 = CryptoProObjectIdentifiers.gostR3410_94;
    private static final ASN1ObjectIdentifier ENCRYPTION_RSA = PKCSObjectIdentifiers.rsaEncryption;
    private static final ASN1ObjectIdentifier ENCRYPTION_RSA_PSS = PKCSObjectIdentifiers.id_RSASSA_PSS;
    private static Map algorithms = new HashMap();
    private static Map digestOids = new HashMap();
    private static Set noParams = new HashSet();
    private static Map params = new HashMap();
    private static Set pkcs15RsaEncryption = new HashSet();

    static {
        algorithms.put("MD2WITHRSAENCRYPTION", PKCSObjectIdentifiers.md2WithRSAEncryption);
        algorithms.put("MD2WITHRSA", PKCSObjectIdentifiers.md2WithRSAEncryption);
        algorithms.put("MD5WITHRSAENCRYPTION", PKCSObjectIdentifiers.md5WithRSAEncryption);
        algorithms.put("MD5WITHRSA", PKCSObjectIdentifiers.md5WithRSAEncryption);
        algorithms.put("SHA1WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha1WithRSAEncryption);
        algorithms.put("SHA1WITHRSA", PKCSObjectIdentifiers.sha1WithRSAEncryption);
        algorithms.put("SHA224WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha224WithRSAEncryption);
        algorithms.put("SHA224WITHRSA", PKCSObjectIdentifiers.sha224WithRSAEncryption);
        algorithms.put("SHA256WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha256WithRSAEncryption);
        algorithms.put("SHA256WITHRSA", PKCSObjectIdentifiers.sha256WithRSAEncryption);
        algorithms.put("SHA384WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha384WithRSAEncryption);
        algorithms.put("SHA384WITHRSA", PKCSObjectIdentifiers.sha384WithRSAEncryption);
        algorithms.put("SHA512WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha512WithRSAEncryption);
        algorithms.put("SHA512WITHRSA", PKCSObjectIdentifiers.sha512WithRSAEncryption);
        algorithms.put("SHA1WITHRSAANDMGF1", PKCSObjectIdentifiers.id_RSASSA_PSS);
        algorithms.put("SHA224WITHRSAANDMGF1", PKCSObjectIdentifiers.id_RSASSA_PSS);
        algorithms.put("SHA256WITHRSAANDMGF1", PKCSObjectIdentifiers.id_RSASSA_PSS);
        algorithms.put("SHA384WITHRSAANDMGF1", PKCSObjectIdentifiers.id_RSASSA_PSS);
        algorithms.put("SHA512WITHRSAANDMGF1", PKCSObjectIdentifiers.id_RSASSA_PSS);
        algorithms.put("RIPEMD160WITHRSAENCRYPTION", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd160);
        algorithms.put("RIPEMD160WITHRSA", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd160);
        algorithms.put("RIPEMD128WITHRSAENCRYPTION", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd128);
        algorithms.put("RIPEMD128WITHRSA", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd128);
        algorithms.put("RIPEMD256WITHRSAENCRYPTION", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd256);
        algorithms.put("RIPEMD256WITHRSA", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd256);
        algorithms.put("SHA1WITHDSA", X9ObjectIdentifiers.id_dsa_with_sha1);
        algorithms.put("DSAWITHSHA1", X9ObjectIdentifiers.id_dsa_with_sha1);
        algorithms.put("SHA224WITHDSA", NISTObjectIdentifiers.dsa_with_sha224);
        algorithms.put("SHA256WITHDSA", NISTObjectIdentifiers.dsa_with_sha256);
        algorithms.put("SHA384WITHDSA", NISTObjectIdentifiers.dsa_with_sha384);
        algorithms.put("SHA512WITHDSA", NISTObjectIdentifiers.dsa_with_sha512);
        algorithms.put("SHA1WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA1);
        algorithms.put("ECDSAWITHSHA1", X9ObjectIdentifiers.ecdsa_with_SHA1);
        algorithms.put("SHA224WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA224);
        algorithms.put("SHA256WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA256);
        algorithms.put("SHA384WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA384);
        algorithms.put("SHA512WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA512);
        algorithms.put("GOST3411WITHGOST3410", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94);
        algorithms.put("GOST3411WITHGOST3410-94", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94);
        algorithms.put("GOST3411WITHECGOST3410", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001);
        algorithms.put("GOST3411WITHECGOST3410-2001", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001);
        algorithms.put("GOST3411WITHGOST3410-2001", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA1);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA224);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA256);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA384);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA512);
        noParams.add(X9ObjectIdentifiers.id_dsa_with_sha1);
        noParams.add(NISTObjectIdentifiers.dsa_with_sha224);
        noParams.add(NISTObjectIdentifiers.dsa_with_sha256);
        noParams.add(NISTObjectIdentifiers.dsa_with_sha384);
        noParams.add(NISTObjectIdentifiers.dsa_with_sha512);
        noParams.add(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94);
        noParams.add(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001);
        pkcs15RsaEncryption.add(PKCSObjectIdentifiers.sha1WithRSAEncryption);
        pkcs15RsaEncryption.add(PKCSObjectIdentifiers.sha224WithRSAEncryption);
        pkcs15RsaEncryption.add(PKCSObjectIdentifiers.sha256WithRSAEncryption);
        pkcs15RsaEncryption.add(PKCSObjectIdentifiers.sha384WithRSAEncryption);
        pkcs15RsaEncryption.add(PKCSObjectIdentifiers.sha512WithRSAEncryption);
        pkcs15RsaEncryption.add(TeleTrusTObjectIdentifiers.rsaSignatureWithripemd128);
        pkcs15RsaEncryption.add(TeleTrusTObjectIdentifiers.rsaSignatureWithripemd160);
        pkcs15RsaEncryption.add(TeleTrusTObjectIdentifiers.rsaSignatureWithripemd256);
        params.put("SHA1WITHRSAANDMGF1", createPSSParams(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1, DERNull.INSTANCE), 20));
        params.put("SHA224WITHRSAANDMGF1", createPSSParams(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha224, DERNull.INSTANCE), 28));
        params.put("SHA256WITHRSAANDMGF1", createPSSParams(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256, DERNull.INSTANCE), 32));
        params.put("SHA384WITHRSAANDMGF1", createPSSParams(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha384, DERNull.INSTANCE), 48));
        params.put("SHA512WITHRSAANDMGF1", createPSSParams(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha512, DERNull.INSTANCE), 64));
        digestOids.put(PKCSObjectIdentifiers.sha224WithRSAEncryption, NISTObjectIdentifiers.id_sha224);
        digestOids.put(PKCSObjectIdentifiers.sha256WithRSAEncryption, NISTObjectIdentifiers.id_sha256);
        digestOids.put(PKCSObjectIdentifiers.sha384WithRSAEncryption, NISTObjectIdentifiers.id_sha384);
        digestOids.put(PKCSObjectIdentifiers.sha512WithRSAEncryption, NISTObjectIdentifiers.id_sha512);
        digestOids.put(PKCSObjectIdentifiers.md2WithRSAEncryption, PKCSObjectIdentifiers.md2);
        digestOids.put(PKCSObjectIdentifiers.md4WithRSAEncryption, PKCSObjectIdentifiers.md4);
        digestOids.put(PKCSObjectIdentifiers.md5WithRSAEncryption, PKCSObjectIdentifiers.md5);
        digestOids.put(PKCSObjectIdentifiers.sha1WithRSAEncryption, OIWObjectIdentifiers.idSHA1);
        digestOids.put(TeleTrusTObjectIdentifiers.rsaSignatureWithripemd128, TeleTrusTObjectIdentifiers.ripemd128);
        digestOids.put(TeleTrusTObjectIdentifiers.rsaSignatureWithripemd160, TeleTrusTObjectIdentifiers.ripemd160);
        digestOids.put(TeleTrusTObjectIdentifiers.rsaSignatureWithripemd256, TeleTrusTObjectIdentifiers.ripemd256);
        digestOids.put(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94, CryptoProObjectIdentifiers.gostR3411);
        digestOids.put(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001, CryptoProObjectIdentifiers.gostR3411);
    }

    private static RSASSAPSSparams createPSSParams(AlgorithmIdentifier algorithmIdentifier, int i) {
        return new RSASSAPSSparams(algorithmIdentifier, new AlgorithmIdentifier(PKCSObjectIdentifiers.id_mgf1, algorithmIdentifier), new ASN1Integer((long) i), new ASN1Integer(1));
    }

    private static AlgorithmIdentifier generate(String str) {
        String toUpperCase = Strings.toUpperCase(str);
        ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier) algorithms.get(toUpperCase);
        if (aSN1ObjectIdentifier == null) {
            throw new IllegalArgumentException("Unknown signature type requested: " + toUpperCase);
        }
        AlgorithmIdentifier algorithmIdentifier;
        AlgorithmIdentifier algorithmIdentifier2 = noParams.contains(aSN1ObjectIdentifier) ? new AlgorithmIdentifier(aSN1ObjectIdentifier) : params.containsKey(toUpperCase) ? new AlgorithmIdentifier(aSN1ObjectIdentifier, (ASN1Encodable) params.get(toUpperCase)) : new AlgorithmIdentifier(aSN1ObjectIdentifier, DERNull.INSTANCE);
        if (pkcs15RsaEncryption.contains(aSN1ObjectIdentifier)) {
            algorithmIdentifier = new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE);
        }
        if (algorithmIdentifier2.getAlgorithm().equals(PKCSObjectIdentifiers.id_RSASSA_PSS)) {
            ((RSASSAPSSparams) algorithmIdentifier2.getParameters()).getHashAlgorithm();
        } else {
            algorithmIdentifier = new AlgorithmIdentifier((ASN1ObjectIdentifier) digestOids.get(aSN1ObjectIdentifier), DERNull.INSTANCE);
        }
        return algorithmIdentifier2;
    }

    public AlgorithmIdentifier find(String str) {
        return generate(str);
    }
}
