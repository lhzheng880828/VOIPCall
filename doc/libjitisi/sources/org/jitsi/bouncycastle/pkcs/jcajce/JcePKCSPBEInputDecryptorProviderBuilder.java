package org.jitsi.bouncycastle.pkcs.jcajce;

import java.io.InputStream;
import java.security.Provider;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.ASN1OctetString;
import org.jitsi.bouncycastle.asn1.pkcs.PBES2Parameters;
import org.jitsi.bouncycastle.asn1.pkcs.PBKDF2Params;
import org.jitsi.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.jitsi.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.jcajce.DefaultJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.JcaJceHelper;
import org.jitsi.bouncycastle.jcajce.NamedJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.ProviderJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.provider.symmetric.util.BCPBEKey;
import org.jitsi.bouncycastle.operator.DefaultSecretKeyProvider;
import org.jitsi.bouncycastle.operator.GenericKey;
import org.jitsi.bouncycastle.operator.InputDecryptor;
import org.jitsi.bouncycastle.operator.InputDecryptorProvider;
import org.jitsi.bouncycastle.operator.OperatorCreationException;
import org.jitsi.bouncycastle.operator.SecretKeySizeProvider;
import org.jitsi.bouncycastle.operator.jcajce.JceGenericKey;

public class JcePKCSPBEInputDecryptorProviderBuilder {
    /* access modifiers changed from: private */
    public JcaJceHelper helper = new DefaultJcaJceHelper();
    /* access modifiers changed from: private */
    public SecretKeySizeProvider keySizeProvider = DefaultSecretKeyProvider.INSTANCE;
    /* access modifiers changed from: private */
    public boolean wrongPKCS12Zero = false;

    public InputDecryptorProvider build(final char[] cArr) {
        return new InputDecryptorProvider() {
            /* access modifiers changed from: private */
            public Cipher cipher;
            /* access modifiers changed from: private */
            public AlgorithmIdentifier encryptionAlg;
            /* access modifiers changed from: private */
            public SecretKey key;

            public InputDecryptor get(AlgorithmIdentifier algorithmIdentifier) throws OperatorCreationException {
                ASN1ObjectIdentifier algorithm = algorithmIdentifier.getAlgorithm();
                try {
                    if (algorithm.on(PKCSObjectIdentifiers.pkcs_12PbeIds)) {
                        PKCS12PBEParams instance = PKCS12PBEParams.getInstance(algorithmIdentifier.getParameters());
                        PBEKeySpec pBEKeySpec = new PBEKeySpec(cArr);
                        SecretKeyFactory createSecretKeyFactory = JcePKCSPBEInputDecryptorProviderBuilder.this.helper.createSecretKeyFactory(algorithm.getId());
                        PBEParameterSpec pBEParameterSpec = new PBEParameterSpec(instance.getIV(), instance.getIterations().intValue());
                        this.key = createSecretKeyFactory.generateSecret(pBEKeySpec);
                        if (this.key instanceof BCPBEKey) {
                            ((BCPBEKey) this.key).setTryWrongPKCS12Zero(JcePKCSPBEInputDecryptorProviderBuilder.this.wrongPKCS12Zero);
                        }
                        this.cipher = JcePKCSPBEInputDecryptorProviderBuilder.this.helper.createCipher(algorithm.getId());
                        this.cipher.init(2, this.key, pBEParameterSpec);
                        this.encryptionAlg = algorithmIdentifier;
                    } else if (algorithm.equals(PKCSObjectIdentifiers.id_PBES2)) {
                        PBES2Parameters instance2 = PBES2Parameters.getInstance(algorithmIdentifier.getParameters());
                        PBKDF2Params instance3 = PBKDF2Params.getInstance(instance2.getKeyDerivationFunc().getParameters());
                        this.key = JcePKCSPBEInputDecryptorProviderBuilder.this.helper.createSecretKeyFactory(instance2.getKeyDerivationFunc().getAlgorithm().getId()).generateSecret(new PBEKeySpec(cArr, instance3.getSalt(), instance3.getIterationCount().intValue(), JcePKCSPBEInputDecryptorProviderBuilder.this.keySizeProvider.getKeySize(AlgorithmIdentifier.getInstance(instance2.getEncryptionScheme()))));
                        this.cipher = JcePKCSPBEInputDecryptorProviderBuilder.this.helper.createCipher(instance2.getEncryptionScheme().getAlgorithm().getId());
                        this.encryptionAlg = AlgorithmIdentifier.getInstance(instance2.getEncryptionScheme());
                        this.cipher.init(2, this.key, new IvParameterSpec(ASN1OctetString.getInstance(instance2.getEncryptionScheme().getParameters()).getOctets()));
                    }
                    return new InputDecryptor() {
                        public AlgorithmIdentifier getAlgorithmIdentifier() {
                            return AnonymousClass1.this.encryptionAlg;
                        }

                        public InputStream getInputStream(InputStream inputStream) {
                            return new CipherInputStream(inputStream, AnonymousClass1.this.cipher);
                        }

                        public GenericKey getKey() {
                            return new JceGenericKey(AnonymousClass1.this.encryptionAlg, AnonymousClass1.this.key);
                        }
                    };
                } catch (Exception e) {
                    throw new OperatorCreationException("unable to create InputDecryptor: " + e.getMessage(), e);
                }
            }
        };
    }

    public JcePKCSPBEInputDecryptorProviderBuilder setKeySizeProvider(SecretKeySizeProvider secretKeySizeProvider) {
        this.keySizeProvider = secretKeySizeProvider;
        return this;
    }

    public JcePKCSPBEInputDecryptorProviderBuilder setProvider(String str) {
        this.helper = new NamedJcaJceHelper(str);
        return this;
    }

    public JcePKCSPBEInputDecryptorProviderBuilder setProvider(Provider provider) {
        this.helper = new ProviderJcaJceHelper(provider);
        return this;
    }

    public JcePKCSPBEInputDecryptorProviderBuilder setTryWrongPKCS12Zero(boolean z) {
        this.wrongPKCS12Zero = z;
        return this;
    }
}
