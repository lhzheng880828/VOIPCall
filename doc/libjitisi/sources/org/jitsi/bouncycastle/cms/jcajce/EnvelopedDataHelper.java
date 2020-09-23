package org.jitsi.bouncycastle.cms.jcajce;

import java.io.IOException;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.RC2ParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.jitsi.bouncycastle.asn1.ASN1Encodable;
import org.jitsi.bouncycastle.asn1.ASN1Null;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.ASN1OctetString;
import org.jitsi.bouncycastle.asn1.ASN1Primitive;
import org.jitsi.bouncycastle.asn1.DERNull;
import org.jitsi.bouncycastle.asn1.DEROctetString;
import org.jitsi.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.pkcs.RC2CBCParameter;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.cms.CMSAlgorithm;
import org.jitsi.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.jitsi.bouncycastle.cms.CMSEnvelopedGenerator;
import org.jitsi.bouncycastle.cms.CMSException;
import org.jitsi.bouncycastle.operator.GenericKey;
import org.jitsi.bouncycastle.operator.SymmetricKeyUnwrapper;
import org.jitsi.bouncycastle.operator.jcajce.JceAsymmetricKeyUnwrapper;

class EnvelopedDataHelper {
    protected static final Map BASE_CIPHER_NAMES = new HashMap();
    protected static final Map CIPHER_ALG_NAMES = new HashMap();
    protected static final Map MAC_ALG_NAMES = new HashMap();
    private static final short[] rc2Ekb = new short[]{(short) 93, (short) 190, (short) 155, (short) 139, (short) 17, (short) 153, (short) 110, (short) 77, (short) 89, (short) 243, (short) 133, (short) 166, (short) 63, (short) 183, (short) 131, (short) 197, (short) 228, (short) 115, (short) 107, (short) 58, (short) 104, (short) 90, (short) 192, (short) 71, (short) 160, (short) 100, (short) 52, (short) 12, (short) 241, (short) 208, (short) 82, (short) 165, (short) 185, (short) 30, (short) 150, (short) 67, (short) 65, (short) 216, (short) 212, (short) 44, (short) 219, (short) 248, (short) 7, (short) 119, (short) 42, (short) 202, (short) 235, (short) 239, (short) 16, (short) 28, (short) 22, (short) 13, (short) 56, (short) 114, (short) 47, (short) 137, (short) 193, (short) 249, (short) 128, (short) 196, (short) 109, (short) 174, (short) 48, (short) 61, (short) 206, (short) 32, (short) 99, (short) 254, (short) 230, (short) 26, (short) 199, (short) 184, (short) 80, (short) 232, (short) 36, (short) 23, (short) 252, (short) 37, (short) 111, (short) 187, (short) 106, (short) 163, (short) 68, (short) 83, (short) 217, (short) 162, (short) 1, (short) 171, (short) 188, (short) 182, (short) 31, (short) 152, (short) 238, (short) 154, (short) 167, (short) 45, (short) 79, (short) 158, (short) 142, (short) 172, (short) 224, (short) 198, (short) 73, (short) 70, (short) 41, (short) 244, (short) 148, (short) 138, (short) 175, (short) 225, (short) 91, (short) 195, (short) 179, (short) 123, (short) 87, (short) 209, (short) 124, (short) 156, (short) 237, (short) 135, (short) 64, (short) 140, (short) 226, (short) 203, (short) 147, (short) 20, (short) 201, (short) 97, (short) 46, (short) 229, (short) 204, (short) 246, (short) 94, (short) 168, (short) 92, (short) 214, (short) 117, (short) 141, (short) 98, (short) 149, (short) 88, (short) 105, (short) 118, (short) 161, (short) 74, (short) 181, (short) 85, (short) 9, (short) 120, (short) 51, (short) 130, (short) 215, (short) 221, (short) 121, (short) 245, (short) 27, (short) 11, (short) 222, (short) 38, (short) 33, (short) 40, (short) 116, (short) 4, (short) 151, (short) 86, (short) 223, (short) 60, (short) 240, (short) 55, (short) 57, (short) 220, (short) 255, (short) 6, (short) 164, (short) 234, (short) 66, (short) 8, (short) 218, (short) 180, (short) 113, (short) 176, (short) 207, (short) 18, (short) 122, (short) 78, (short) 250, (short) 108, (short) 29, (short) 132, (short) 0, (short) 200, (short) 127, (short) 145, (short) 69, (short) 170, (short) 43, (short) 194, (short) 177, (short) 143, (short) 213, (short) 186, (short) 242, (short) 173, (short) 25, (short) 178, (short) 103, (short) 54, (short) 247, (short) 15, (short) 10, (short) 146, (short) 125, (short) 227, (short) 157, (short) 233, (short) 144, (short) 62, (short) 35, (short) 39, (short) 102, (short) 19, (short) 236, (short) 129, (short) 21, (short) 189, (short) 34, (short) 191, (short) 159, (short) 126, (short) 169, (short) 81, (short) 75, (short) 76, (short) 251, (short) 2, (short) 211, (short) 112, (short) 134, (short) 49, (short) 231, (short) 59, (short) 5, (short) 3, (short) 84, (short) 96, (short) 72, (short) 101, (short) 24, (short) 210, (short) 205, (short) 95, (short) 50, (short) 136, (short) 14, (short) 53, (short) 253};
    private static final short[] rc2Table = new short[]{(short) 189, (short) 86, (short) 234, (short) 242, (short) 162, (short) 241, (short) 172, (short) 42, (short) 176, (short) 147, (short) 209, (short) 156, (short) 27, (short) 51, (short) 253, (short) 208, (short) 48, (short) 4, (short) 182, (short) 220, (short) 125, (short) 223, (short) 50, (short) 75, (short) 247, (short) 203, (short) 69, (short) 155, (short) 49, (short) 187, (short) 33, (short) 90, (short) 65, (short) 159, (short) 225, (short) 217, (short) 74, (short) 77, (short) 158, (short) 218, (short) 160, (short) 104, (short) 44, (short) 195, (short) 39, (short) 95, (short) 128, (short) 54, (short) 62, (short) 238, (short) 251, (short) 149, (short) 26, (short) 254, (short) 206, (short) 168, (short) 52, (short) 169, (short) 19, (short) 240, (short) 166, (short) 63, (short) 216, (short) 12, (short) 120, (short) 36, (short) 175, (short) 35, (short) 82, (short) 193, (short) 103, (short) 23, (short) 245, (short) 102, (short) 144, (short) 231, (short) 232, (short) 7, (short) 184, (short) 96, (short) 72, (short) 230, (short) 30, (short) 83, (short) 243, (short) 146, (short) 164, (short) 114, (short) 140, (short) 8, (short) 21, (short) 110, (short) 134, (short) 0, (short) 132, (short) 250, (short) 244, (short) 127, (short) 138, (short) 66, (short) 25, (short) 246, (short) 219, (short) 205, (short) 20, (short) 141, (short) 80, (short) 18, (short) 186, (short) 60, (short) 6, (short) 78, (short) 236, (short) 179, (short) 53, (short) 17, (short) 161, (short) 136, (short) 142, (short) 43, (short) 148, (short) 153, (short) 183, (short) 113, (short) 116, (short) 211, (short) 228, (short) 191, (short) 58, (short) 222, (short) 150, (short) 14, (short) 188, (short) 10, (short) 237, (short) 119, (short) 252, (short) 55, (short) 107, (short) 3, (short) 121, (short) 137, (short) 98, (short) 198, (short) 215, (short) 192, (short) 210, (short) 124, (short) 106, (short) 139, (short) 34, (short) 163, (short) 91, (short) 5, (short) 93, (short) 2, (short) 117, (short) 213, (short) 97, (short) 227, (short) 24, (short) 143, (short) 85, (short) 81, (short) 173, (short) 31, (short) 11, (short) 94, (short) 133, (short) 229, (short) 194, (short) 87, (short) 99, (short) 202, (short) 61, (short) 108, (short) 180, (short) 197, (short) 204, (short) 112, (short) 178, (short) 145, (short) 89, (short) 13, (short) 71, (short) 32, (short) 200, (short) 79, (short) 88, (short) 224, (short) 1, (short) 226, (short) 22, (short) 56, (short) 196, (short) 111, (short) 59, (short) 15, (short) 101, (short) 70, (short) 190, (short) 126, (short) 45, (short) 123, (short) 130, (short) 249, (short) 64, (short) 181, (short) 29, (short) 115, (short) 248, (short) 235, (short) 38, (short) 199, (short) 135, (short) 151, (short) 37, (short) 84, (short) 177, (short) 40, (short) 170, (short) 152, (short) 157, (short) 165, (short) 100, (short) 109, (short) 122, (short) 212, (short) 16, (short) 129, (short) 68, (short) 239, (short) 73, (short) 214, (short) 174, (short) 46, (short) 221, (short) 118, (short) 92, (short) 47, (short) 167, (short) 28, (short) 201, (short) 9, (short) 105, (short) 154, (short) 131, (short) 207, (short) 41, (short) 57, (short) 185, (short) 233, (short) 76, (short) 255, (short) 67, (short) 171};
    private JcaJceExtHelper helper;

    interface JCECallback {
        Object doInJCE() throws CMSException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidParameterSpecException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException;
    }

    static {
        BASE_CIPHER_NAMES.put(CMSAlgorithm.DES_CBC, "DES");
        BASE_CIPHER_NAMES.put(CMSAlgorithm.DES_EDE3_CBC, "DESEDE");
        BASE_CIPHER_NAMES.put(CMSAlgorithm.AES128_CBC, "AES");
        BASE_CIPHER_NAMES.put(CMSAlgorithm.AES192_CBC, "AES");
        BASE_CIPHER_NAMES.put(CMSAlgorithm.AES256_CBC, "AES");
        BASE_CIPHER_NAMES.put(CMSAlgorithm.RC2_CBC, "RC2");
        BASE_CIPHER_NAMES.put(CMSAlgorithm.CAST5_CBC, "CAST5");
        BASE_CIPHER_NAMES.put(CMSAlgorithm.CAMELLIA128_CBC, "Camellia");
        BASE_CIPHER_NAMES.put(CMSAlgorithm.CAMELLIA192_CBC, "Camellia");
        BASE_CIPHER_NAMES.put(CMSAlgorithm.CAMELLIA256_CBC, "Camellia");
        BASE_CIPHER_NAMES.put(CMSAlgorithm.SEED_CBC, "SEED");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.DES_CBC, "DES/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.DES_EDE3_CBC, "DESEDE/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.AES128_CBC, "AES/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.AES192_CBC, "AES/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.AES256_CBC, "AES/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(PKCSObjectIdentifiers.rsaEncryption, "RSA/ECB/PKCS1Padding");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.CAST5_CBC, "CAST5/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.CAMELLIA128_CBC, "Camellia/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.CAMELLIA192_CBC, "Camellia/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.CAMELLIA256_CBC, "Camellia/CBC/PKCS5Padding");
        CIPHER_ALG_NAMES.put(CMSAlgorithm.SEED_CBC, "SEED/CBC/PKCS5Padding");
        MAC_ALG_NAMES.put(CMSAlgorithm.DES_EDE3_CBC, "DESEDEMac");
        MAC_ALG_NAMES.put(CMSAlgorithm.AES128_CBC, "AESMac");
        MAC_ALG_NAMES.put(CMSAlgorithm.AES192_CBC, "AESMac");
        MAC_ALG_NAMES.put(CMSAlgorithm.AES256_CBC, "AESMac");
        MAC_ALG_NAMES.put(CMSAlgorithm.RC2_CBC, "RC2Mac");
    }

    EnvelopedDataHelper(JcaJceExtHelper jcaJceExtHelper) {
        this.helper = jcaJceExtHelper;
    }

    static Object execute(JCECallback jCECallback) throws CMSException {
        try {
            return jCECallback.doInJCE();
        } catch (NoSuchAlgorithmException e) {
            throw new CMSException("can't find algorithm.", e);
        } catch (InvalidKeyException e2) {
            throw new CMSException("key invalid in message.", e2);
        } catch (NoSuchProviderException e3) {
            throw new CMSException("can't find provider.", e3);
        } catch (NoSuchPaddingException e4) {
            throw new CMSException("required padding not supported.", e4);
        } catch (InvalidAlgorithmParameterException e5) {
            throw new CMSException("algorithm parameters invalid.", e5);
        } catch (InvalidParameterSpecException e6) {
            throw new CMSException("MAC algorithm parameter spec invalid.", e6);
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

    public JceAsymmetricKeyUnwrapper createAsymmetricUnwrapper(AlgorithmIdentifier algorithmIdentifier, PrivateKey privateKey) {
        return this.helper.createAsymmetricUnwrapper(algorithmIdentifier, privateKey);
    }

    /* access modifiers changed from: 0000 */
    public Cipher createCipher(ASN1ObjectIdentifier aSN1ObjectIdentifier) throws CMSException {
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
            throw new CMSException("cannot create cipher: " + e2.getMessage(), e2);
        }
    }

    /* access modifiers changed from: 0000 */
    public Cipher createContentCipher(final Key key, final AlgorithmIdentifier algorithmIdentifier) throws CMSException {
        return (Cipher) execute(new JCECallback() {
            public Object doInJCE() throws CMSException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidParameterSpecException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
                Cipher createCipher = EnvelopedDataHelper.this.createCipher(algorithmIdentifier.getAlgorithm());
                ASN1Encodable parameters = algorithmIdentifier.getParameters();
                String id = algorithmIdentifier.getAlgorithm().getId();
                if (parameters != null && !(parameters instanceof ASN1Null)) {
                    try {
                        AlgorithmParameters createAlgorithmParameters = EnvelopedDataHelper.this.createAlgorithmParameters(algorithmIdentifier.getAlgorithm());
                        createAlgorithmParameters.init(parameters.toASN1Primitive().getEncoded(), "ASN.1");
                        createCipher.init(2, key, createAlgorithmParameters);
                    } catch (IOException e) {
                        throw new CMSException("error decoding algorithm parameters.", e);
                    } catch (NoSuchAlgorithmException e2) {
                        if (id.equals(CMSAlgorithm.DES_CBC.getId()) || id.equals(CMSEnvelopedDataGenerator.DES_EDE3_CBC) || id.equals(CMSEnvelopedGenerator.IDEA_CBC) || id.equals(CMSEnvelopedDataGenerator.AES128_CBC) || id.equals(CMSEnvelopedDataGenerator.AES192_CBC) || id.equals(CMSEnvelopedDataGenerator.AES256_CBC)) {
                            createCipher.init(2, key, new IvParameterSpec(ASN1OctetString.getInstance(parameters).getOctets()));
                        } else {
                            throw e2;
                        }
                    }
                } else if (id.equals(CMSAlgorithm.DES_CBC.getId()) || id.equals(CMSEnvelopedDataGenerator.DES_EDE3_CBC) || id.equals(CMSEnvelopedGenerator.IDEA_CBC) || id.equals(CMSEnvelopedGenerator.CAST5_CBC)) {
                    createCipher.init(2, key, new IvParameterSpec(new byte[8]));
                } else {
                    createCipher.init(2, key);
                }
                return createCipher;
            }
        });
    }

    /* access modifiers changed from: 0000 */
    public Mac createContentMac(final Key key, final AlgorithmIdentifier algorithmIdentifier) throws CMSException {
        return (Mac) execute(new JCECallback() {
            public Object doInJCE() throws CMSException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidParameterSpecException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
                Mac createMac = EnvelopedDataHelper.this.createMac(algorithmIdentifier.getAlgorithm());
                ASN1Encodable parameters = algorithmIdentifier.getParameters();
                algorithmIdentifier.getAlgorithm().getId();
                if (parameters == null || (parameters instanceof ASN1Null)) {
                    createMac.init(key);
                } else {
                    try {
                        AlgorithmParameters createAlgorithmParameters = EnvelopedDataHelper.this.createAlgorithmParameters(algorithmIdentifier.getAlgorithm());
                        createAlgorithmParameters.init(parameters.toASN1Primitive().getEncoded(), "ASN.1");
                        createMac.init(key, createAlgorithmParameters.getParameterSpec(IvParameterSpec.class));
                    } catch (IOException e) {
                        throw new CMSException("error decoding algorithm parameters.", e);
                    } catch (NoSuchAlgorithmException e2) {
                        throw e2;
                    }
                }
                return createMac;
            }
        });
    }

    /* access modifiers changed from: 0000 */
    public KeyAgreement createKeyAgreement(ASN1ObjectIdentifier aSN1ObjectIdentifier) throws CMSException {
        try {
            String str = (String) BASE_CIPHER_NAMES.get(aSN1ObjectIdentifier);
            if (str != null) {
                try {
                    return this.helper.createKeyAgreement(str);
                } catch (NoSuchAlgorithmException e) {
                }
            }
            return this.helper.createKeyAgreement(aSN1ObjectIdentifier.getId());
        } catch (GeneralSecurityException e2) {
            throw new CMSException("cannot create key pair generator: " + e2.getMessage(), e2);
        }
    }

    public KeyFactory createKeyFactory(ASN1ObjectIdentifier aSN1ObjectIdentifier) throws CMSException {
        try {
            String str = (String) BASE_CIPHER_NAMES.get(aSN1ObjectIdentifier);
            if (str != null) {
                try {
                    return this.helper.createKeyFactory(str);
                } catch (NoSuchAlgorithmException e) {
                }
            }
            return this.helper.createKeyFactory(aSN1ObjectIdentifier.getId());
        } catch (GeneralSecurityException e2) {
            throw new CMSException("cannot create key factory: " + e2.getMessage(), e2);
        }
    }

    public KeyGenerator createKeyGenerator(ASN1ObjectIdentifier aSN1ObjectIdentifier) throws CMSException {
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
            throw new CMSException("cannot create key generator: " + e2.getMessage(), e2);
        }
    }

    /* access modifiers changed from: 0000 */
    public KeyPairGenerator createKeyPairGenerator(ASN1ObjectIdentifier aSN1ObjectIdentifier) throws CMSException {
        try {
            String str = (String) BASE_CIPHER_NAMES.get(aSN1ObjectIdentifier);
            if (str != null) {
                try {
                    return this.helper.createKeyPairGenerator(str);
                } catch (NoSuchAlgorithmException e) {
                }
            }
            return this.helper.createKeyPairGenerator(aSN1ObjectIdentifier.getId());
        } catch (GeneralSecurityException e2) {
            throw new CMSException("cannot create key pair generator: " + e2.getMessage(), e2);
        }
    }

    /* access modifiers changed from: 0000 */
    public Mac createMac(ASN1ObjectIdentifier aSN1ObjectIdentifier) throws CMSException {
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
            throw new CMSException("cannot create mac: " + e2.getMessage(), e2);
        }
    }

    /* access modifiers changed from: 0000 */
    public Cipher createRFC3211Wrapper(ASN1ObjectIdentifier aSN1ObjectIdentifier) throws CMSException {
        String str = (String) BASE_CIPHER_NAMES.get(aSN1ObjectIdentifier);
        if (str == null) {
            throw new CMSException("no name for " + aSN1ObjectIdentifier);
        }
        try {
            return this.helper.createCipher(str + "RFC3211Wrap");
        } catch (GeneralSecurityException e) {
            throw new CMSException("cannot create cipher: " + e.getMessage(), e);
        }
    }

    public SymmetricKeyUnwrapper createSymmetricUnwrapper(AlgorithmIdentifier algorithmIdentifier, SecretKey secretKey) {
        return this.helper.createSymmetricUnwrapper(algorithmIdentifier, secretKey);
    }

    /* access modifiers changed from: 0000 */
    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    public java.security.AlgorithmParameters generateParameters(org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier r5, javax.crypto.SecretKey r6, java.security.SecureRandom r7) throws org.jitsi.bouncycastle.cms.CMSException {
        /*
        r4 = this;
        r0 = r4.createAlgorithmParameterGenerator(r5);	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
        r1 = org.jitsi.bouncycastle.cms.CMSEnvelopedDataGenerator.RC2_CBC;	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
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
        r1 = new org.jitsi.bouncycastle.cms.CMSException;	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
        r2 = new java.lang.StringBuilder;	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
        r2.<init>();	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
        r3 = "parameters generation error: ";
        r2 = r2.append(r3);	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
        r2 = r2.append(r0);	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
        r2 = r2.toString();	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
        r1.m1197init(r2, r0);	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
        throw r1;	 Catch:{ NoSuchAlgorithmException -> 0x0041, GeneralSecurityException -> 0x0044 }
    L_0x0041:
        r0 = move-exception;
        r0 = 0;
        goto L_0x0026;
    L_0x0044:
        r0 = move-exception;
        r1 = new org.jitsi.bouncycastle.cms.CMSException;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "exception creating algorithm parameter generator: ";
        r2 = r2.append(r3);
        r2 = r2.append(r0);
        r2 = r2.toString();
        r1.m1197init(r2, r0);
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.bouncycastle.cms.jcajce.EnvelopedDataHelper.generateParameters(org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier, javax.crypto.SecretKey, java.security.SecureRandom):java.security.AlgorithmParameters");
    }

    /* access modifiers changed from: 0000 */
    public AlgorithmIdentifier getAlgorithmIdentifier(ASN1ObjectIdentifier aSN1ObjectIdentifier, AlgorithmParameters algorithmParameters) throws CMSException {
        ASN1Encodable fromByteArray;
        if (algorithmParameters != null) {
            try {
                fromByteArray = ASN1Primitive.fromByteArray(algorithmParameters.getEncoded("ASN.1"));
            } catch (IOException e) {
                throw new CMSException("cannot encode parameters: " + e.getMessage(), e);
            }
        }
        fromByteArray = DERNull.INSTANCE;
        return new AlgorithmIdentifier(aSN1ObjectIdentifier, fromByteArray);
    }

    public AlgorithmIdentifier getAlgorithmIdentifier(ASN1ObjectIdentifier aSN1ObjectIdentifier, AlgorithmParameterSpec algorithmParameterSpec) {
        if (algorithmParameterSpec instanceof IvParameterSpec) {
            return new AlgorithmIdentifier(aSN1ObjectIdentifier, new DEROctetString(((IvParameterSpec) algorithmParameterSpec).getIV()));
        }
        if (algorithmParameterSpec instanceof RC2ParameterSpec) {
            RC2ParameterSpec rC2ParameterSpec = (RC2ParameterSpec) algorithmParameterSpec;
            int effectiveKeyBits = ((RC2ParameterSpec) algorithmParameterSpec).getEffectiveKeyBits();
            if (effectiveKeyBits == -1) {
                return new AlgorithmIdentifier(aSN1ObjectIdentifier, new RC2CBCParameter(rC2ParameterSpec.getIV()));
            }
            if (effectiveKeyBits < 256) {
                effectiveKeyBits = rc2Table[effectiveKeyBits];
            }
            return new AlgorithmIdentifier(aSN1ObjectIdentifier, new RC2CBCParameter(effectiveKeyBits, rC2ParameterSpec.getIV()));
        }
        throw new IllegalStateException("unknown parameter spec: " + algorithmParameterSpec);
    }

    /* access modifiers changed from: 0000 */
    public String getBaseCipherName(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        String str = (String) BASE_CIPHER_NAMES.get(aSN1ObjectIdentifier);
        return str == null ? aSN1ObjectIdentifier.getId() : str;
    }

    /* access modifiers changed from: 0000 */
    public Key getJceKey(ASN1ObjectIdentifier aSN1ObjectIdentifier, GenericKey genericKey) {
        if (genericKey.getRepresentation() instanceof Key) {
            return (Key) genericKey.getRepresentation();
        }
        if (genericKey.getRepresentation() instanceof byte[]) {
            return new SecretKeySpec((byte[]) genericKey.getRepresentation(), getBaseCipherName(aSN1ObjectIdentifier));
        }
        throw new IllegalArgumentException("unknown generic key type");
    }

    /* access modifiers changed from: 0000 */
    public Key getJceKey(GenericKey genericKey) {
        if (genericKey.getRepresentation() instanceof Key) {
            return (Key) genericKey.getRepresentation();
        }
        if (genericKey.getRepresentation() instanceof byte[]) {
            return new SecretKeySpec((byte[]) genericKey.getRepresentation(), "ENC");
        }
        throw new IllegalArgumentException("unknown generic key type");
    }
}
