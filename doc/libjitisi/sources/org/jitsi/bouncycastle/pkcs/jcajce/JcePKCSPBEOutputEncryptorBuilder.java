package org.jitsi.bouncycastle.pkcs.jcajce;

import java.io.OutputStream;
import java.security.Provider;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.ASN1Primitive;
import org.jitsi.bouncycastle.asn1.bc.BCObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.pkcs.EncryptionScheme;
import org.jitsi.bouncycastle.asn1.pkcs.KeyDerivationFunc;
import org.jitsi.bouncycastle.asn1.pkcs.PBES2Parameters;
import org.jitsi.bouncycastle.asn1.pkcs.PBKDF2Params;
import org.jitsi.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.jitsi.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.crypto.PBEParametersGenerator;
import org.jitsi.bouncycastle.jcajce.DefaultJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.JcaJceHelper;
import org.jitsi.bouncycastle.jcajce.NamedJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.ProviderJcaJceHelper;
import org.jitsi.bouncycastle.operator.DefaultSecretKeyProvider;
import org.jitsi.bouncycastle.operator.GenericKey;
import org.jitsi.bouncycastle.operator.OperatorCreationException;
import org.jitsi.bouncycastle.operator.OutputEncryptor;
import org.jitsi.bouncycastle.operator.SecretKeySizeProvider;

public class JcePKCSPBEOutputEncryptorBuilder {
    private ASN1ObjectIdentifier algorithm;
    private JcaJceHelper helper = new DefaultJcaJceHelper();
    private ASN1ObjectIdentifier keyEncAlgorithm;
    private SecretKeySizeProvider keySizeProvider = DefaultSecretKeyProvider.INSTANCE;
    private SecureRandom random;

    public JcePKCSPBEOutputEncryptorBuilder(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        if (isPKCS12(aSN1ObjectIdentifier)) {
            this.algorithm = aSN1ObjectIdentifier;
            this.keyEncAlgorithm = aSN1ObjectIdentifier;
            return;
        }
        this.algorithm = PKCSObjectIdentifiers.id_PBES2;
        this.keyEncAlgorithm = aSN1ObjectIdentifier;
    }

    /* access modifiers changed from: private */
    public boolean isPKCS12(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return aSN1ObjectIdentifier.on(PKCSObjectIdentifiers.pkcs_12PbeIds) || aSN1ObjectIdentifier.on(BCObjectIdentifiers.bc_pbe_sha1_pkcs12) || aSN1ObjectIdentifier.on(BCObjectIdentifiers.bc_pbe_sha256_pkcs12);
    }

    public OutputEncryptor build(final char[] cArr) throws OperatorCreationException {
        if (this.random == null) {
            this.random = new SecureRandom();
        }
        byte[] bArr = new byte[20];
        this.random.nextBytes(bArr);
        try {
            Cipher createCipher;
            AlgorithmIdentifier algorithmIdentifier;
            SecretKey generateSecret;
            if (this.algorithm.on(PKCSObjectIdentifiers.pkcs_12PbeIds)) {
                PBEKeySpec pBEKeySpec = new PBEKeySpec(cArr);
                SecretKeyFactory createSecretKeyFactory = this.helper.createSecretKeyFactory(this.algorithm.getId());
                PBEParameterSpec pBEParameterSpec = new PBEParameterSpec(bArr, 1024);
                generateSecret = createSecretKeyFactory.generateSecret(pBEKeySpec);
                createCipher = this.helper.createCipher(this.algorithm.getId());
                createCipher.init(1, generateSecret, pBEParameterSpec);
                algorithmIdentifier = new AlgorithmIdentifier(this.algorithm, new PKCS12PBEParams(bArr, 1024));
            } else if (this.algorithm.equals(PKCSObjectIdentifiers.id_PBES2)) {
                generateSecret = this.helper.createSecretKeyFactory(PKCSObjectIdentifiers.id_PBKDF2.getId()).generateSecret(new PBEKeySpec(cArr, bArr, 1024, this.keySizeProvider.getKeySize(new AlgorithmIdentifier(this.keyEncAlgorithm))));
                createCipher = this.helper.createCipher(this.keyEncAlgorithm.getId());
                createCipher.init(1, generateSecret, this.random);
                algorithmIdentifier = new AlgorithmIdentifier(this.algorithm, new PBES2Parameters(new KeyDerivationFunc(PKCSObjectIdentifiers.id_PBKDF2, new PBKDF2Params(bArr, 1024)), new EncryptionScheme(this.keyEncAlgorithm, ASN1Primitive.fromByteArray(createCipher.getParameters().getEncoded()))));
            } else {
                throw new OperatorCreationException("unrecognised algorithm");
            }
            return new OutputEncryptor() {
                public AlgorithmIdentifier getAlgorithmIdentifier() {
                    return algorithmIdentifier;
                }

                public GenericKey getKey() {
                    return JcePKCSPBEOutputEncryptorBuilder.this.isPKCS12(algorithmIdentifier.getAlgorithm()) ? new GenericKey(algorithmIdentifier, PBEParametersGenerator.PKCS5PasswordToBytes(cArr)) : new GenericKey(algorithmIdentifier, PBEParametersGenerator.PKCS12PasswordToBytes(cArr));
                }

                public OutputStream getOutputStream(OutputStream outputStream) {
                    return new CipherOutputStream(outputStream, createCipher);
                }
            };
        } catch (Exception e) {
            throw new OperatorCreationException("unable to create OutputEncryptor: " + e.getMessage(), e);
        }
    }

    public JcePKCSPBEOutputEncryptorBuilder setKeySizeProvider(SecretKeySizeProvider secretKeySizeProvider) {
        this.keySizeProvider = secretKeySizeProvider;
        return this;
    }

    public JcePKCSPBEOutputEncryptorBuilder setProvider(String str) {
        this.helper = new NamedJcaJceHelper(str);
        return this;
    }

    public JcePKCSPBEOutputEncryptorBuilder setProvider(Provider provider) {
        this.helper = new ProviderJcaJceHelper(provider);
        return this;
    }
}
