package org.jitsi.bouncycastle.cms;

import java.io.IOException;
import java.security.Provider;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.jitsi.bouncycastle.asn1.ASN1Encodable;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.ASN1Primitive;
import org.jitsi.bouncycastle.asn1.ASN1Sequence;
import org.jitsi.bouncycastle.asn1.ASN1Set;
import org.jitsi.bouncycastle.asn1.ASN1TaggedObject;
import org.jitsi.bouncycastle.asn1.DERNull;
import org.jitsi.bouncycastle.asn1.cms.OtherRevocationInfoFormat;
import org.jitsi.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.eac.EACObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.asn1.x509.AttributeCertificate;
import org.jitsi.bouncycastle.asn1.x509.Certificate;
import org.jitsi.bouncycastle.asn1.x509.CertificateList;
import org.jitsi.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.jitsi.bouncycastle.cert.X509AttributeCertificateHolder;
import org.jitsi.bouncycastle.cert.X509CRLHolder;
import org.jitsi.bouncycastle.cert.X509CertificateHolder;
import org.jitsi.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.jitsi.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.jitsi.bouncycastle.util.CollectionStore;
import org.jitsi.bouncycastle.util.Store;
import org.jitsi.bouncycastle.x509.NoSuchStoreException;
import org.jitsi.bouncycastle.x509.X509CollectionStoreParameters;
import org.jitsi.bouncycastle.x509.X509Store;
import org.jitsi.bouncycastle.x509.X509V2AttributeCertificate;

class CMSSignedHelper {
    static final CMSSignedHelper INSTANCE = new CMSSignedHelper();
    private static final Map digestAlgs = new HashMap();
    private static final Map digestAliases = new HashMap();
    private static final Map encryptionAlgs = new HashMap();

    static {
        addEntries(NISTObjectIdentifiers.dsa_with_sha224, "SHA224", "DSA");
        addEntries(NISTObjectIdentifiers.dsa_with_sha256, "SHA256", "DSA");
        addEntries(NISTObjectIdentifiers.dsa_with_sha384, "SHA384", "DSA");
        addEntries(NISTObjectIdentifiers.dsa_with_sha512, "SHA512", "DSA");
        addEntries(OIWObjectIdentifiers.dsaWithSHA1, "SHA1", "DSA");
        addEntries(OIWObjectIdentifiers.md4WithRSA, "MD4", "RSA");
        addEntries(OIWObjectIdentifiers.md4WithRSAEncryption, "MD4", "RSA");
        addEntries(OIWObjectIdentifiers.md5WithRSA, "MD5", "RSA");
        addEntries(OIWObjectIdentifiers.sha1WithRSA, "SHA1", "RSA");
        addEntries(PKCSObjectIdentifiers.md2WithRSAEncryption, "MD2", "RSA");
        addEntries(PKCSObjectIdentifiers.md4WithRSAEncryption, "MD4", "RSA");
        addEntries(PKCSObjectIdentifiers.md5WithRSAEncryption, "MD5", "RSA");
        addEntries(PKCSObjectIdentifiers.sha1WithRSAEncryption, "SHA1", "RSA");
        addEntries(PKCSObjectIdentifiers.sha224WithRSAEncryption, "SHA224", "RSA");
        addEntries(PKCSObjectIdentifiers.sha256WithRSAEncryption, "SHA256", "RSA");
        addEntries(PKCSObjectIdentifiers.sha384WithRSAEncryption, "SHA384", "RSA");
        addEntries(PKCSObjectIdentifiers.sha512WithRSAEncryption, "SHA512", "RSA");
        addEntries(X9ObjectIdentifiers.ecdsa_with_SHA1, "SHA1", "ECDSA");
        addEntries(X9ObjectIdentifiers.ecdsa_with_SHA224, "SHA224", "ECDSA");
        addEntries(X9ObjectIdentifiers.ecdsa_with_SHA256, "SHA256", "ECDSA");
        addEntries(X9ObjectIdentifiers.ecdsa_with_SHA384, "SHA384", "ECDSA");
        addEntries(X9ObjectIdentifiers.ecdsa_with_SHA512, "SHA512", "ECDSA");
        addEntries(X9ObjectIdentifiers.id_dsa_with_sha1, "SHA1", "DSA");
        addEntries(EACObjectIdentifiers.id_TA_ECDSA_SHA_1, "SHA1", "ECDSA");
        addEntries(EACObjectIdentifiers.id_TA_ECDSA_SHA_224, "SHA224", "ECDSA");
        addEntries(EACObjectIdentifiers.id_TA_ECDSA_SHA_256, "SHA256", "ECDSA");
        addEntries(EACObjectIdentifiers.id_TA_ECDSA_SHA_384, "SHA384", "ECDSA");
        addEntries(EACObjectIdentifiers.id_TA_ECDSA_SHA_512, "SHA512", "ECDSA");
        addEntries(EACObjectIdentifiers.id_TA_RSA_v1_5_SHA_1, "SHA1", "RSA");
        addEntries(EACObjectIdentifiers.id_TA_RSA_v1_5_SHA_256, "SHA256", "RSA");
        addEntries(EACObjectIdentifiers.id_TA_RSA_PSS_SHA_1, "SHA1", "RSAandMGF1");
        addEntries(EACObjectIdentifiers.id_TA_RSA_PSS_SHA_256, "SHA256", "RSAandMGF1");
        encryptionAlgs.put(X9ObjectIdentifiers.id_dsa.getId(), "DSA");
        encryptionAlgs.put(PKCSObjectIdentifiers.rsaEncryption.getId(), "RSA");
        encryptionAlgs.put(TeleTrusTObjectIdentifiers.teleTrusTRSAsignatureAlgorithm, "RSA");
        encryptionAlgs.put(X509ObjectIdentifiers.id_ea_rsa.getId(), "RSA");
        encryptionAlgs.put(CMSSignedDataGenerator.ENCRYPTION_RSA_PSS, "RSAandMGF1");
        encryptionAlgs.put(CryptoProObjectIdentifiers.gostR3410_94.getId(), "GOST3410");
        encryptionAlgs.put(CryptoProObjectIdentifiers.gostR3410_2001.getId(), "ECGOST3410");
        encryptionAlgs.put("1.3.6.1.4.1.5849.1.6.2", "ECGOST3410");
        encryptionAlgs.put("1.3.6.1.4.1.5849.1.1.5", "GOST3410");
        encryptionAlgs.put(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001.getId(), "ECGOST3410");
        encryptionAlgs.put(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94.getId(), "GOST3410");
        digestAlgs.put(PKCSObjectIdentifiers.md2.getId(), "MD2");
        digestAlgs.put(PKCSObjectIdentifiers.md4.getId(), "MD4");
        digestAlgs.put(PKCSObjectIdentifiers.md5.getId(), "MD5");
        digestAlgs.put(OIWObjectIdentifiers.idSHA1.getId(), "SHA1");
        digestAlgs.put(NISTObjectIdentifiers.id_sha224.getId(), "SHA224");
        digestAlgs.put(NISTObjectIdentifiers.id_sha256.getId(), "SHA256");
        digestAlgs.put(NISTObjectIdentifiers.id_sha384.getId(), "SHA384");
        digestAlgs.put(NISTObjectIdentifiers.id_sha512.getId(), "SHA512");
        digestAlgs.put(TeleTrusTObjectIdentifiers.ripemd128.getId(), "RIPEMD128");
        digestAlgs.put(TeleTrusTObjectIdentifiers.ripemd160.getId(), "RIPEMD160");
        digestAlgs.put(TeleTrusTObjectIdentifiers.ripemd256.getId(), "RIPEMD256");
        digestAlgs.put(CryptoProObjectIdentifiers.gostR3411.getId(), "GOST3411");
        digestAlgs.put("1.3.6.1.4.1.5849.1.2.1", "GOST3411");
        digestAliases.put("SHA1", new String[]{"SHA-1"});
        digestAliases.put("SHA224", new String[]{"SHA-224"});
        digestAliases.put("SHA256", new String[]{"SHA-256"});
        digestAliases.put("SHA384", new String[]{"SHA-384"});
        digestAliases.put("SHA512", new String[]{"SHA-512"});
    }

    CMSSignedHelper() {
    }

    private static void addEntries(ASN1ObjectIdentifier aSN1ObjectIdentifier, String str, String str2) {
        digestAlgs.put(aSN1ObjectIdentifier.getId(), str);
        encryptionAlgs.put(aSN1ObjectIdentifier.getId(), str2);
    }

    /* access modifiers changed from: 0000 */
    public X509Store createAttributeStore(String str, Provider provider, Store store) throws NoSuchStoreException, CMSException {
        try {
            Collection<X509AttributeCertificateHolder> matches = store.getMatches(null);
            ArrayList arrayList = new ArrayList(matches.size());
            for (X509AttributeCertificateHolder encoded : matches) {
                arrayList.add(new X509V2AttributeCertificate(encoded.getEncoded()));
            }
            return X509Store.getInstance("AttributeCertificate/" + str, new X509CollectionStoreParameters(arrayList), provider);
        } catch (IllegalArgumentException e) {
            throw new CMSException("can't setup the X509Store", e);
        } catch (IOException e2) {
            throw new CMSException("can't setup the X509Store", e2);
        }
    }

    /* access modifiers changed from: 0000 */
    public X509Store createCRLsStore(String str, Provider provider, Store store) throws NoSuchStoreException, CMSException {
        try {
            JcaX509CRLConverter provider2 = new JcaX509CRLConverter().setProvider(provider);
            Collection<X509CRLHolder> matches = store.getMatches(null);
            ArrayList arrayList = new ArrayList(matches.size());
            for (X509CRLHolder crl : matches) {
                arrayList.add(provider2.getCRL(crl));
            }
            return X509Store.getInstance("CRL/" + str, new X509CollectionStoreParameters(arrayList), provider);
        } catch (IllegalArgumentException e) {
            throw new CMSException("can't setup the X509Store", e);
        } catch (CRLException e2) {
            throw new CMSException("can't setup the X509Store", e2);
        }
    }

    /* access modifiers changed from: 0000 */
    public X509Store createCertificateStore(String str, Provider provider, Store store) throws NoSuchStoreException, CMSException {
        try {
            JcaX509CertificateConverter provider2 = new JcaX509CertificateConverter().setProvider(provider);
            Collection<X509CertificateHolder> matches = store.getMatches(null);
            ArrayList arrayList = new ArrayList(matches.size());
            for (X509CertificateHolder certificate : matches) {
                arrayList.add(provider2.getCertificate(certificate));
            }
            return X509Store.getInstance("Certificate/" + str, new X509CollectionStoreParameters(arrayList), provider);
        } catch (IllegalArgumentException e) {
            throw new CMSException("can't setup the X509Store", e);
        } catch (CertificateException e2) {
            throw new CMSException("can't setup the X509Store", e2);
        }
    }

    /* access modifiers changed from: 0000 */
    public AlgorithmIdentifier fixAlgID(AlgorithmIdentifier algorithmIdentifier) {
        return algorithmIdentifier.getParameters() == null ? new AlgorithmIdentifier(algorithmIdentifier.getAlgorithm(), DERNull.INSTANCE) : algorithmIdentifier;
    }

    /* access modifiers changed from: 0000 */
    public Store getAttributeCertificates(ASN1Set aSN1Set) {
        if (aSN1Set == null) {
            return new CollectionStore(new ArrayList());
        }
        ArrayList arrayList = new ArrayList(aSN1Set.size());
        Enumeration objects = aSN1Set.getObjects();
        while (objects.hasMoreElements()) {
            ASN1Primitive toASN1Primitive = ((ASN1Encodable) objects.nextElement()).toASN1Primitive();
            if (toASN1Primitive instanceof ASN1TaggedObject) {
                arrayList.add(new X509AttributeCertificateHolder(AttributeCertificate.getInstance(((ASN1TaggedObject) toASN1Primitive).getObject())));
            }
        }
        return new CollectionStore(arrayList);
    }

    /* access modifiers changed from: 0000 */
    public Store getCRLs(ASN1Set aSN1Set) {
        if (aSN1Set == null) {
            return new CollectionStore(new ArrayList());
        }
        ArrayList arrayList = new ArrayList(aSN1Set.size());
        Enumeration objects = aSN1Set.getObjects();
        while (objects.hasMoreElements()) {
            ASN1Primitive toASN1Primitive = ((ASN1Encodable) objects.nextElement()).toASN1Primitive();
            if (toASN1Primitive instanceof ASN1Sequence) {
                arrayList.add(new X509CRLHolder(CertificateList.getInstance(toASN1Primitive)));
            }
        }
        return new CollectionStore(arrayList);
    }

    /* access modifiers changed from: 0000 */
    public Store getCertificates(ASN1Set aSN1Set) {
        if (aSN1Set == null) {
            return new CollectionStore(new ArrayList());
        }
        ArrayList arrayList = new ArrayList(aSN1Set.size());
        Enumeration objects = aSN1Set.getObjects();
        while (objects.hasMoreElements()) {
            ASN1Primitive toASN1Primitive = ((ASN1Encodable) objects.nextElement()).toASN1Primitive();
            if (toASN1Primitive instanceof ASN1Sequence) {
                arrayList.add(new X509CertificateHolder(Certificate.getInstance(toASN1Primitive)));
            }
        }
        return new CollectionStore(arrayList);
    }

    /* access modifiers changed from: 0000 */
    public String getDigestAlgName(String str) {
        String str2 = (String) digestAlgs.get(str);
        return str2 != null ? str2 : str;
    }

    /* access modifiers changed from: 0000 */
    public String getEncryptionAlgName(String str) {
        String str2 = (String) encryptionAlgs.get(str);
        return str2 != null ? str2 : str;
    }

    /* access modifiers changed from: 0000 */
    public Store getOtherRevocationInfo(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1Set aSN1Set) {
        if (aSN1Set == null) {
            return new CollectionStore(new ArrayList());
        }
        ArrayList arrayList = new ArrayList(aSN1Set.size());
        Enumeration objects = aSN1Set.getObjects();
        while (objects.hasMoreElements()) {
            ASN1Primitive toASN1Primitive = ((ASN1Encodable) objects.nextElement()).toASN1Primitive();
            if (toASN1Primitive instanceof ASN1TaggedObject) {
                ASN1TaggedObject instance = ASN1TaggedObject.getInstance(toASN1Primitive);
                if (instance.getTagNo() == 1) {
                    OtherRevocationInfoFormat instance2 = OtherRevocationInfoFormat.getInstance(instance, false);
                    if (aSN1ObjectIdentifier.equals(instance2.getInfoFormat())) {
                        arrayList.add(instance2.getInfo());
                    }
                }
            }
        }
        return new CollectionStore(arrayList);
    }

    /* access modifiers changed from: 0000 */
    public void setSigningDigestAlgorithmMapping(ASN1ObjectIdentifier aSN1ObjectIdentifier, String str) {
        digestAlgs.put(aSN1ObjectIdentifier.getId(), str);
    }

    /* access modifiers changed from: 0000 */
    public void setSigningEncryptionAlgorithmMapping(ASN1ObjectIdentifier aSN1ObjectIdentifier, String str) {
        encryptionAlgs.put(aSN1ObjectIdentifier.getId(), str);
    }
}
