package org.jitsi.bouncycastle.openssl.jcajce;

import java.io.IOException;
import java.io.InputStream;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.Provider;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.jitsi.bouncycastle.asn1.pkcs.EncryptionScheme;
import org.jitsi.bouncycastle.asn1.pkcs.KeyDerivationFunc;
import org.jitsi.bouncycastle.asn1.pkcs.PBEParameter;
import org.jitsi.bouncycastle.asn1.pkcs.PBES2Parameters;
import org.jitsi.bouncycastle.asn1.pkcs.PBKDF2Params;
import org.jitsi.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.jcajce.DefaultJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.JcaJceHelper;
import org.jitsi.bouncycastle.jcajce.NamedJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.ProviderJcaJceHelper;
import org.jitsi.bouncycastle.openssl.PEMException;
import org.jitsi.bouncycastle.operator.InputDecryptor;
import org.jitsi.bouncycastle.operator.InputDecryptorProvider;
import org.jitsi.bouncycastle.operator.OperatorCreationException;

public class JceOpenSSLPKCS8DecryptorProviderBuilder {
    /* access modifiers changed from: private */
    public JcaJceHelper helper;

    public JceOpenSSLPKCS8DecryptorProviderBuilder() {
        this.helper = new DefaultJcaJceHelper();
        this.helper = new DefaultJcaJceHelper();
    }

    public InputDecryptorProvider build(final char[] cArr) throws OperatorCreationException {
        return new InputDecryptorProvider() {
            public InputDecryptor get(final AlgorithmIdentifier algorithmIdentifier) throws OperatorCreationException {
                try {
                    Cipher createCipher;
                    PBEKeySpec pBEKeySpec;
                    SecretKeyFactory createSecretKeyFactory;
                    PBEParameterSpec pBEParameterSpec;
                    if (PEMUtilities.isPKCS5Scheme2(algorithmIdentifier.getAlgorithm())) {
                        PBES2Parameters instance = PBES2Parameters.getInstance(algorithmIdentifier.getParameters());
                        KeyDerivationFunc keyDerivationFunc = instance.getKeyDerivationFunc();
                        EncryptionScheme encryptionScheme = instance.getEncryptionScheme();
                        PBKDF2Params pBKDF2Params = (PBKDF2Params) keyDerivationFunc.getParameters();
                        int intValue = pBKDF2Params.getIterationCount().intValue();
                        byte[] salt = pBKDF2Params.getSalt();
                        String id = encryptionScheme.getAlgorithm().getId();
                        SecretKey generateSecretKeyForPKCS5Scheme2 = PEMUtilities.generateSecretKeyForPKCS5Scheme2(id, cArr, salt, intValue);
                        createCipher = JceOpenSSLPKCS8DecryptorProviderBuilder.this.helper.createCipher(id);
                        AlgorithmParameters createAlgorithmParameters = JceOpenSSLPKCS8DecryptorProviderBuilder.this.helper.createAlgorithmParameters(id);
                        createAlgorithmParameters.init(encryptionScheme.getParameters().toASN1Primitive().getEncoded());
                        createCipher.init(2, generateSecretKeyForPKCS5Scheme2, createAlgorithmParameters);
                    } else if (PEMUtilities.isPKCS12(algorithmIdentifier.getAlgorithm())) {
                        PKCS12PBEParams instance2 = PKCS12PBEParams.getInstance(algorithmIdentifier.getParameters());
                        pBEKeySpec = new PBEKeySpec(cArr);
                        createSecretKeyFactory = JceOpenSSLPKCS8DecryptorProviderBuilder.this.helper.createSecretKeyFactory(algorithmIdentifier.getAlgorithm().getId());
                        pBEParameterSpec = new PBEParameterSpec(instance2.getIV(), instance2.getIterations().intValue());
                        createCipher = JceOpenSSLPKCS8DecryptorProviderBuilder.this.helper.createCipher(algorithmIdentifier.getAlgorithm().getId());
                        createCipher.init(2, createSecretKeyFactory.generateSecret(pBEKeySpec), pBEParameterSpec);
                    } else if (PEMUtilities.isPKCS5Scheme1(algorithmIdentifier.getAlgorithm())) {
                        PBEParameter instance3 = PBEParameter.getInstance(algorithmIdentifier.getParameters());
                        pBEKeySpec = new PBEKeySpec(cArr);
                        createSecretKeyFactory = JceOpenSSLPKCS8DecryptorProviderBuilder.this.helper.createSecretKeyFactory(algorithmIdentifier.getAlgorithm().getId());
                        pBEParameterSpec = new PBEParameterSpec(instance3.getSalt(), instance3.getIterationCount().intValue());
                        createCipher = JceOpenSSLPKCS8DecryptorProviderBuilder.this.helper.createCipher(algorithmIdentifier.getAlgorithm().getId());
                        createCipher.init(2, createSecretKeyFactory.generateSecret(pBEKeySpec), pBEParameterSpec);
                    } else {
                        throw new PEMException("Unknown algorithm: " + algorithmIdentifier.getAlgorithm());
                    }
                    return new InputDecryptor() {
                        public AlgorithmIdentifier getAlgorithmIdentifier() {
                            return algorithmIdentifier;
                        }

                        public InputStream getInputStream(InputStream inputStream) {
                            return new CipherInputStream(inputStream, createCipher);
                        }
                    };
                } catch (IOException e) {
                    throw new OperatorCreationException(algorithmIdentifier.getAlgorithm() + " not available: " + e.getMessage(), e);
                } catch (GeneralSecurityException e2) {
                    throw new OperatorCreationException(algorithmIdentifier.getAlgorithm() + " not available: " + e2.getMessage(), e2);
                }
            }
        };
    }

    public JceOpenSSLPKCS8DecryptorProviderBuilder setProvider(String str) {
        this.helper = new NamedJcaJceHelper(str);
        return this;
    }

    public JceOpenSSLPKCS8DecryptorProviderBuilder setProvider(Provider provider) {
        this.helper = new ProviderJcaJceHelper(provider);
        return this;
    }
}
