package org.jitsi.bouncycastle.cert.crmf.jcajce;

import java.io.IOException;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import org.jitsi.bouncycastle.asn1.ASN1Encodable;
import org.jitsi.bouncycastle.asn1.ASN1Null;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.ASN1OctetString;
import org.jitsi.bouncycastle.asn1.ASN1Primitive;
import org.jitsi.bouncycastle.asn1.DERNull;
import org.jitsi.bouncycastle.asn1.iana.IANAObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.jitsi.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.jitsi.bouncycastle.cert.crmf.CRMFException;
import org.jitsi.bouncycastle.cms.CMSAlgorithm;
import org.jitsi.bouncycastle.jcajce.JcaJceHelper;

class CRMFHelper {
    protected static final Map BASE_CIPHER_NAMES = new HashMap();
    protected static final Map CIPHER_ALG_NAMES = new HashMap();
    protected static final Map DIGEST_ALG_NAMES = new HashMap();
    protected static final Map KEY_ALG_NAMES = new HashMap();
    protected static final Map MAC_ALG_NAMES = new HashMap();
    private JcaJceHelper helper;

    interface JCECallback {
        Object doInJCE() throws CRMFException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidParameterSpecException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException;
    }

    static {
        BASE_CIPHER_NAMES.put(PKCSObjectIdentifiers.des_EDE3_CBC, "DESEDE");
        BASE_CIPHER_NAMES.put(NISTObjectIdentifiers.id_aes128_CBC, "AES");
        BASE_CIPHER_NAMES.put(NISTObjectIdentifiers.id_aes192_CBC, "AES");
        BASE_CIPHER_NAMES.put(NISTObjectIdentifiers.id_aes256_CBC, "AES");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.DES_EDE3_CBC, "DESEDE/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.AES128_CBC, "AES/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.AES192_CBC, "AES/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.AES256_CBC, "AES/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(new ASN1ObjectIdentifier(PKCSObjectIdentifiers.rsaEncryption.getId()), "RSA/ECB/PKCS1Padding");
        DIGEST_ALG_NAMES.put(OIWObjectIdentifiers.idSHA1, "SHA1");
        DIGEST_ALG_NAMES.put(NISTObjectIdentifiers.id_sha224, "SHA224");
        DIGEST_ALG_NAMES.put(NISTObjectIdentifiers.id_sha256, "SHA256");
        DIGEST_ALG_NAMES.put(NISTObjectIdentifiers.id_sha384, "SHA384");
        DIGEST_ALG_NAMES.put(NISTObjectIdentifiers.id_sha512, "SHA512");
        MAC_ALG_NAMES.put(IANAObjectIdentifiers.hmacSHA1, "HMACSHA1");
        MAC_ALG_NAMES.put(PKCSObjectIdentifiers.id_hmacWithSHA1, "HMACSHA1");
        MAC_ALG_NAMES.put(PKCSObjectIdentifiers.id_hmacWithSHA224, "HMACSHA224");
        MAC_ALG_NAMES.put(PKCSObjectIdentifiers.id_hmacWithSHA256, "HMACSHA256");
        MAC_ALG_NAMES.put(PKCSObjectIdentifiers.id_hmacWithSHA384, "HMACSHA384");
        MAC_ALG_NAMES.put(PKCSObjectIdentifiers.id_hmacWithSHA512, "HMACSHA512");
        KEY_ALG_NAMES.put(PKCSObjectIdentifiers.rsaEncryption, "RSA");
        KEY_ALG_NAMES.put(X9ObjectIdentifiers.id_dsa, "DSA");
    }

    CRMFHelper(JcaJceHelper jcaJceHelper) {
        this.helper = jcaJceHelper;
    }

    static Object execute(JCECallback jCECallback) throws CRMFException {
        try {
            return jCECallback.doInJCE();
        } catch (NoSuchAlgorithmException e) {
            throw new CRMFException("can't find algorithm.", e);
        } catch (InvalidKeyException e2) {
            throw new CRMFException("key invalid in message.", e2);
        } catch (NoSuchProviderException e3) {
            throw new CRMFException("can't find provider.", e3);
        } catch (NoSuchPaddingException e4) {
            throw new CRMFException("required padding not supported.", e4);
        } catch (InvalidAlgorithmParameterException e5) {
            throw new CRMFException("algorithm parameters invalid.", e5);
        } catch (InvalidParameterSpecException e6) {
            throw new CRMFException("MAC algorithm parameter spec invalid.", e6);
        }
    }

    /* access modifiers changed from: 0000 */
    public AlgorithmParameterGenerator createAlgorithmParameterGenerator(ASN1ObjectIdentifier aSN1ObjectIdentifier) throws GeneralSecurityException {
        String str = (String) BASE_CIPHER_NAMES.get(aSN1ObjectIdentifier);
        if (str != null) {
            try {
                return this.helper.createAlgorithmParameterGenerator(str);
            } catch (NoSuchAlgorithmException e) {
            }
        }
        return this.helper.createAlgorithmParameterGenerator(aSN1ObjectIdentifier.getId());
    }

    /* access modifiers changed from: 0000 */
    public AlgorithmParameters createAlgorithmParameters(ASN1ObjectIdentifier aSN1ObjectIdentifier) throws NoSuchAlgorithmException, NoSuchProviderException {
        String str = (String) BASE_CIPHER_NAMES.get(aSN1ObjectIdentifier);
        if (str != null) {
            try {
                return this.helper.createAlgorithmParameters(str);
            } catch (NoSuchAlgorithmException e) {
            }
        }
        return this.helper.createAlgorithmParameters(aSN1ObjectIdentifier.getId());
    }

    /* access modifiers changed from: 0000 */
    public Cipher createCipher(ASN1ObjectIdentifier aSN1ObjectIdentifier) throws CRMFException {
        try {
            String str = (String) CIPHER_ALG_NAMES.get(aSN1ObjectIdentifier);
            if (str != null) {
                try {
                    return this.helper.createCipher(str);
                } catch (NoSuchAlgorithmException e) {
                }
            }
            return this.helper.createCipher(aSN1ObjectIdentifier.getId());
        } catch (GeneralSecurityException e2) {
            throw new CRMFException("cannot create cipher: " + e2.getMessage(), e2);
        }
    }

    /* access modifiers changed from: 0000 */
    public Cipher createContentCipher(final Key key, final AlgorithmIdentifier algorithmIdentifier) throws CRMFException {
        return (Cipher) execute(new JCECallback() {
            public Object doInJCE() throws CRMFException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidParameterSpecException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
                Cipher createCipher = CRMFHelper.this.createCipher(algorithmIdentifier.getAlgorithm());
                ASN1Primitive aSN1Primitive = (ASN1Primitive) algorithmIdentifier.getParameters();
                ASN1ObjectIdentifier algorithm = algorithmIdentifier.getAlgorithm();
                if (aSN1Primitive != null && !(aSN1Primitive instanceof ASN1Null)) {
                    try {
                        AlgorithmParameters createAlgorithmParameters = CRMFHelper.this.createAlgorithmParameters(algorithmIdentifier.getAlgorithm());
                        createAlgorithmParameters.init(aSN1Primitive.getEncoded(), "ASN.1");
                        createCipher.init(2, key, createAlgorithmParameters);
                    } catch (IOException e) {
                        throw new CRMFException("error decoding algorithm parameters.", e);
                    } catch (NoSuchAlgorithmException e2) {
                        if (algorithm.equals(CMSAlgorithm.DES_EDE3_CBC) || algorithm.equals(CMSAlgorithm.IDEA_CBC) || algorithm.equals(CMSAlgorithm.AES128_CBC) || algorithm.equals(CMSAlgorithm.AES192_CBC) || algorithm.equals(CMSAlgorithm.AES256_CBC)) {
                            createCipher.init(2, key, new IvParameterSpec(ASN1OctetString.getInstance(aSN1Primitive).getOctets()));
                        } else {
                            throw e2;
                        }
                    }
                } else if (algorithm.equals(CMSAlgorithm.DES_EDE3_CBC) || algorithm.equals(CMSAlgorithm.IDEA_CBC) || algorithm.equals(CMSAlgorithm.CAST5_CBC)) {
                    createCipher.init(2, key, new IvParameterSpec(new byte[8]));
                } else {
                    createCipher.init(2, key);
                }
                return createCipher;
            }
        });
    }

    /* access modifiers changed from: 0000 */
    public MessageDigest createDigest(ASN1ObjectIdentifier aSN1ObjectIdentifier) throws CRMFException {
        try {
            String str = (String) DIGEST_ALG_NAMES.get(aSN1ObjectIdentifier);
            if (str != null) {
                try {
                    return this.helper.createDigest(str);
                } catch (NoSuchAlgorithmException e) {
                }
            }
            return this.helper.createDigest(aSN1ObjectIdentifier.getId());
        } catch (GeneralSecurityException e2) {
            throw new CRMFException("cannot create cipher: " + e2.getMessage(), e2);
        }
    }

    /* access modifiers changed from: 0000 */
    public KeyFactory createKeyFactory(ASN1ObjectIdentifier aSN1ObjectIdentifier) throws CRMFException {
        try {
            String str = (String) KEY_ALG_NAMES.get(aSN1ObjectIdentifier);
            if (str != null) {
                try {
                    return this.helper.createKeyFactory(str);
                } catch (NoSuchAlgorithmException e) {
                }
            }
            return this.helper.createKeyFactory(aSN1ObjectIdentifier.getId());
        } catch (GeneralSecurityException e2) {
            throw new CRMFException("cannot create cipher: " + e2.getMessage(), e2);
        }
    }

    public KeyGenerator createKeyGenerator(ASN1ObjectIdentifier aSN1ObjectIdentifier) throws CRMFException {
        try {
            String str = (String) BASE_CIPHER_NAMES.get(aSN1ObjectIdentifier);
            if (str != null) {
                try {
                    return this.helper.createKeyGenerator(str);
                } catch (NoSuchAlgorithmException e) {
                }
            }
            return this.helper.createKeyGenerator(aSN1ObjectIdentifier.getId());
        } catch (GeneralSecurityException e2) {
            throw new CRMFException("cannot create key generator: " + e2.getMessage(), e2);
        }
    }

    /* access modifiers changed from: 0000 */
    public Mac createMac(ASN1ObjectIdentifier aSN1ObjectIdentifier) throws CRMFException {
        try {
            String str = (String) MAC_ALG_NAMES.get(aSN1ObjectIdentifier);
            if (str != null) {
                try {
                    return this.helper.createMac(str);
                } catch (NoSuchAlgorithmException e) {
                }
            }
            return this.helper.createMac(aSN1ObjectIdentifier.getId());
        } catch (GeneralSecurityException e2) {
            throw new CRMFException("cannot create mac: " + e2.getMessage(), e2);
        }
    }

    /* access modifiers changed from: 0000 */
    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    public java.security.AlgorithmParameters generateParameters(org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier r5, javax.crypto.SecretKey r6, java.security.SecureRandom r7) throws org.jitsi.bouncycastle.cert.crmf.CRMFException {
        /*
        r4 = this;
        r0 = r4.createAlgorithmParameterGenerator(r5);	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
        r1 = org.jitsi.bouncycastle.cms.CMSAlgorithm.RC2_CBC;	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
        r1 = r5.equals(r1);	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
        if (r1 == 0) goto L_0x0022;
    L_0x000c:
        r1 = 8;
        r1 = new byte[r1];	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
        r7.nextBytes(r1);	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
        r2 = new javax.crypto.spec.RC2ParameterSpec;	 Catch:{ InvalidAlgorithmParameterException -> 0x0027 }
        r3 = r6.getEncoded();	 Catch:{ InvalidAlgorithmParameterException -> 0x0027 }
        r3 = r3.length;	 Catch:{ InvalidAlgorithmParameterException -> 0x0027 }
        r3 = r3 * 8;
        r2.<init>(r3, r1);	 Catch:{ InvalidAlgorithmParameterException -> 0x0027 }
        r0.init(r2, r7);	 Catch:{ InvalidAlgorithmParameterException -> 0x0027 }
    L_0x0022:
        r0 = r0.generateParameters();	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
    L_0x0026:
        return r0;
    L_0x0027:
        r0 = move-exception;
        r1 = new org.jitsi.bouncycastle.cert.crmf.CRMFException;	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
        r2 = new java.lang.StringBuilder;	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
        r2.<init>();	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
        r3 = "parameters generation error: ";
        r2 = r2.append(r3);	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
        r2 = r2.append(r0);	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
        r2 = r2.toString();	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
        r1.m1005init(r2, r0);	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
        throw r1;	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
    L_0x0041:
        r0 = move-exception;
        r0 = 0;
        goto L_0x0026;
    L_0x0044:
        r0 = move-exception;
        r1 = new org.jitsi.bouncycastle.cert.crmf.CRMFException;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "exception creating algorithm parameter generator: ";
        r2 = r2.append(r3);
        r2 = r2.append(r0);
        r2 = r2.toString();
        r1.m1005init(r2, r0);
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.bouncycastle.cert.crmf.jcajce.CRMFHelper.generateParameters(org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier, javax.crypto.SecretKey, java.security.SecureRandom):java.security.AlgorithmParameters");
    }

    /* access modifiers changed from: 0000 */
    public AlgorithmIdentifier getAlgorithmIdentifier(ASN1ObjectIdentifier aSN1ObjectIdentifier, AlgorithmParameters algorithmParameters) throws CRMFException {
        ASN1Encodable fromByteArray;
        if (algorithmParameters != null) {
            try {
                fromByteArray = ASN1Primitive.fromByteArray(algorithmParameters.getEncoded("ASN.1"));
            } catch (IOException e) {
                throw new CRMFException("cannot encode parameters: " + e.getMessage(), e);
            }
        }
        fromByteArray = DERNull.INSTANCE;
        return new AlgorithmIdentifier(aSN1ObjectIdentifier, fromByteArray);
    }

    /* access modifiers changed from: 0000 */
    public PublicKey toPublicKey(SubjectPublicKeyInfo subjectPublicKeyInfo) throws CRMFException {
        try {
            return createKeyFactory(subjectPublicKeyInfo.getAlgorithm().getAlgorithm()).generatePublic(new X509EncodedKeySpec(subjectPublicKeyInfo.getEncoded()));
        } catch (Exception e) {
            throw new CRMFException("invalid key: " + e.getMessage(), e);
        }
    }
}
