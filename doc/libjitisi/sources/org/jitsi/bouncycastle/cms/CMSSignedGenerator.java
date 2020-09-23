package org.jitsi.bouncycastle.cms;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jitsi.bouncycastle.asn1.ASN1Encodable;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.ASN1Primitive;
import org.jitsi.bouncycastle.asn1.ASN1Set;
import org.jitsi.bouncycastle.asn1.DERSet;
import org.jitsi.bouncycastle.asn1.DERTaggedObject;
import org.jitsi.bouncycastle.asn1.cms.AttributeTable;
import org.jitsi.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.cms.OtherRevocationInfoFormat;
import org.jitsi.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.asn1.x509.AttributeCertificate;
import org.jitsi.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.jitsi.bouncycastle.cert.X509AttributeCertificateHolder;
import org.jitsi.bouncycastle.cert.X509CRLHolder;
import org.jitsi.bouncycastle.cert.X509CertificateHolder;
import org.jitsi.bouncycastle.jce.interfaces.GOST3410PrivateKey;
import org.jitsi.bouncycastle.util.Arrays;
import org.jitsi.bouncycastle.util.Store;
import org.jitsi.bouncycastle.x509.X509AttributeCertificate;
import org.jitsi.bouncycastle.x509.X509Store;

public class CMSSignedGenerator {
    public static final String DATA = CMSObjectIdentifiers.data.getId();
    public static final String DIGEST_GOST3411 = CryptoProObjectIdentifiers.gostR3411.getId();
    public static final String DIGEST_MD5 = PKCSObjectIdentifiers.md5.getId();
    public static final String DIGEST_RIPEMD128 = TeleTrusTObjectIdentifiers.ripemd128.getId();
    public static final String DIGEST_RIPEMD160 = TeleTrusTObjectIdentifiers.ripemd160.getId();
    public static final String DIGEST_RIPEMD256 = TeleTrusTObjectIdentifiers.ripemd256.getId();
    public static final String DIGEST_SHA1 = OIWObjectIdentifiers.idSHA1.getId();
    public static final String DIGEST_SHA224 = NISTObjectIdentifiers.id_sha224.getId();
    public static final String DIGEST_SHA256 = NISTObjectIdentifiers.id_sha256.getId();
    public static final String DIGEST_SHA384 = NISTObjectIdentifiers.id_sha384.getId();
    public static final String DIGEST_SHA512 = NISTObjectIdentifiers.id_sha512.getId();
    private static final Map EC_ALGORITHMS = new HashMap();
    public static final String ENCRYPTION_DSA = X9ObjectIdentifiers.id_dsa_with_sha1.getId();
    public static final String ENCRYPTION_ECDSA = X9ObjectIdentifiers.ecdsa_with_SHA1.getId();
    private static final String ENCRYPTION_ECDSA_WITH_SHA1 = X9ObjectIdentifiers.ecdsa_with_SHA1.getId();
    private static final String ENCRYPTION_ECDSA_WITH_SHA224 = X9ObjectIdentifiers.ecdsa_with_SHA224.getId();
    private static final String ENCRYPTION_ECDSA_WITH_SHA256 = X9ObjectIdentifiers.ecdsa_with_SHA256.getId();
    private static final String ENCRYPTION_ECDSA_WITH_SHA384 = X9ObjectIdentifiers.ecdsa_with_SHA384.getId();
    private static final String ENCRYPTION_ECDSA_WITH_SHA512 = X9ObjectIdentifiers.ecdsa_with_SHA512.getId();
    public static final String ENCRYPTION_ECGOST3410 = CryptoProObjectIdentifiers.gostR3410_2001.getId();
    public static final String ENCRYPTION_GOST3410 = CryptoProObjectIdentifiers.gostR3410_94.getId();
    public static final String ENCRYPTION_RSA = PKCSObjectIdentifiers.rsaEncryption.getId();
    public static final String ENCRYPTION_RSA_PSS = PKCSObjectIdentifiers.id_RSASSA_PSS.getId();
    private static final Set NO_PARAMS = new HashSet();
    protected List _signers;
    protected List certs;
    protected List crls;
    protected Map digests;
    protected final SecureRandom rand;
    protected List signerGens;

    static {
        NO_PARAMS.add(ENCRYPTION_DSA);
        NO_PARAMS.add(ENCRYPTION_ECDSA);
        NO_PARAMS.add(ENCRYPTION_ECDSA_WITH_SHA1);
        NO_PARAMS.add(ENCRYPTION_ECDSA_WITH_SHA224);
        NO_PARAMS.add(ENCRYPTION_ECDSA_WITH_SHA256);
        NO_PARAMS.add(ENCRYPTION_ECDSA_WITH_SHA384);
        NO_PARAMS.add(ENCRYPTION_ECDSA_WITH_SHA512);
        EC_ALGORITHMS.put(DIGEST_SHA1, ENCRYPTION_ECDSA_WITH_SHA1);
        EC_ALGORITHMS.put(DIGEST_SHA224, ENCRYPTION_ECDSA_WITH_SHA224);
        EC_ALGORITHMS.put(DIGEST_SHA256, ENCRYPTION_ECDSA_WITH_SHA256);
        EC_ALGORITHMS.put(DIGEST_SHA384, ENCRYPTION_ECDSA_WITH_SHA384);
        EC_ALGORITHMS.put(DIGEST_SHA512, ENCRYPTION_ECDSA_WITH_SHA512);
    }

    protected CMSSignedGenerator() {
        this(new SecureRandom());
    }

    protected CMSSignedGenerator(SecureRandom secureRandom) {
        this.certs = new ArrayList();
        this.crls = new ArrayList();
        this._signers = new ArrayList();
        this.signerGens = new ArrayList();
        this.digests = new HashMap();
        this.rand = secureRandom;
    }

    public void addAttributeCertificate(X509AttributeCertificateHolder x509AttributeCertificateHolder) throws CMSException {
        this.certs.add(new DERTaggedObject(false, 2, x509AttributeCertificateHolder.toASN1Structure()));
    }

    public void addAttributeCertificates(Store store) throws CMSException {
        this.certs.addAll(CMSUtils.getAttributeCertificatesFromStore(store));
    }

    public void addAttributeCertificates(X509Store x509Store) throws CMSException {
        try {
            for (X509AttributeCertificate encoded : x509Store.getMatches(null)) {
                this.certs.add(new DERTaggedObject(false, 2, AttributeCertificate.getInstance(ASN1Primitive.fromByteArray(encoded.getEncoded()))));
            }
        } catch (IllegalArgumentException e) {
            throw new CMSException("error processing attribute certs", e);
        } catch (IOException e2) {
            throw new CMSException("error processing attribute certs", e2);
        }
    }

    public void addCRL(X509CRLHolder x509CRLHolder) {
        this.crls.add(x509CRLHolder.toASN1Structure());
    }

    public void addCRLs(Store store) throws CMSException {
        this.crls.addAll(CMSUtils.getCRLsFromStore(store));
    }

    public void addCertificate(X509CertificateHolder x509CertificateHolder) throws CMSException {
        this.certs.add(x509CertificateHolder.toASN1Structure());
    }

    public void addCertificates(Store store) throws CMSException {
        this.certs.addAll(CMSUtils.getCertificatesFromStore(store));
    }

    public void addCertificatesAndCRLs(CertStore certStore) throws CertStoreException, CMSException {
        this.certs.addAll(CMSUtils.getCertificatesFromStore(certStore));
        this.crls.addAll(CMSUtils.getCRLsFromStore(certStore));
    }

    public void addOtherRevocationInfo(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1Encodable aSN1Encodable) {
        this.crls.add(new DERTaggedObject(false, 1, new OtherRevocationInfoFormat(aSN1ObjectIdentifier, aSN1Encodable)));
    }

    public void addOtherRevocationInfo(ASN1ObjectIdentifier aSN1ObjectIdentifier, Store store) {
        this.crls.addAll(CMSUtils.getOthersFromStore(aSN1ObjectIdentifier, store));
    }

    public void addSignerInfoGenerator(SignerInfoGenerator signerInfoGenerator) {
        this.signerGens.add(signerInfoGenerator);
    }

    public void addSigners(SignerInformationStore signerInformationStore) {
        for (Object add : signerInformationStore.getSigners()) {
            this._signers.add(add);
        }
    }

    /* access modifiers changed from: protected */
    public ASN1Set getAttributeSet(AttributeTable attributeTable) {
        return attributeTable != null ? new DERSet(attributeTable.toASN1EncodableVector()) : null;
    }

    /* access modifiers changed from: protected */
    public Map getBaseParameters(ASN1ObjectIdentifier aSN1ObjectIdentifier, AlgorithmIdentifier algorithmIdentifier, byte[] bArr) {
        HashMap hashMap = new HashMap();
        hashMap.put(CMSAttributeTableGenerator.CONTENT_TYPE, aSN1ObjectIdentifier);
        hashMap.put(CMSAttributeTableGenerator.DIGEST_ALGORITHM_IDENTIFIER, algorithmIdentifier);
        hashMap.put(CMSAttributeTableGenerator.DIGEST, Arrays.clone(bArr));
        return hashMap;
    }

    /* access modifiers changed from: protected */
    public String getEncOID(PrivateKey privateKey, String str) {
        if ((privateKey instanceof RSAPrivateKey) || "RSA".equalsIgnoreCase(privateKey.getAlgorithm())) {
            return ENCRYPTION_RSA;
        }
        String str2;
        if ((privateKey instanceof DSAPrivateKey) || "DSA".equalsIgnoreCase(privateKey.getAlgorithm())) {
            str2 = ENCRYPTION_DSA;
            if (str.equals(DIGEST_SHA1)) {
                return str2;
            }
            throw new IllegalArgumentException("can't mix DSA with anything but SHA1");
        } else if (!"ECDSA".equalsIgnoreCase(privateKey.getAlgorithm()) && !"EC".equalsIgnoreCase(privateKey.getAlgorithm())) {
            return ((privateKey instanceof GOST3410PrivateKey) || "GOST3410".equalsIgnoreCase(privateKey.getAlgorithm())) ? ENCRYPTION_GOST3410 : "ECGOST3410".equalsIgnoreCase(privateKey.getAlgorithm()) ? ENCRYPTION_ECGOST3410 : null;
        } else {
            str2 = (String) EC_ALGORITHMS.get(str);
            if (str2 != null) {
                return str2;
            }
            throw new IllegalArgumentException("can't mix ECDSA with anything but SHA family digests");
        }
    }

    public Map getGeneratedDigests() {
        return new HashMap(this.digests);
    }
}
