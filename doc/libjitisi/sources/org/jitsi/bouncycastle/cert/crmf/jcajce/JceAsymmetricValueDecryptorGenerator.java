package org.jitsi.bouncycastle.cert.crmf.jcajce;

import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.ProviderException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.cert.crmf.CRMFException;
import org.jitsi.bouncycastle.cert.crmf.ValueDecryptorGenerator;
import org.jitsi.bouncycastle.jcajce.DefaultJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.NamedJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.ProviderJcaJceHelper;
import org.jitsi.bouncycastle.operator.InputDecryptor;

public class JceAsymmetricValueDecryptorGenerator implements ValueDecryptorGenerator {
    private CRMFHelper helper = new CRMFHelper(new DefaultJcaJceHelper());
    private PrivateKey recipientKey;

    public JceAsymmetricValueDecryptorGenerator(PrivateKey privateKey) {
        this.recipientKey = privateKey;
    }

    private Key extractSecretKey(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2, byte[] bArr) throws CRMFException {
        Key key = null;
        try {
            Cipher createCipher = this.helper.createCipher(algorithmIdentifier.getAlgorithm());
            try {
                createCipher.init(4, this.recipientKey);
                key = createCipher.unwrap(bArr, algorithmIdentifier2.getAlgorithm().getId(), 3);
            } catch (IllegalStateException | UnsupportedOperationException | GeneralSecurityException | ProviderException e) {
            }
            if (key != null) {
                return key;
            }
            createCipher.init(2, this.recipientKey);
            return new SecretKeySpec(createCipher.doFinal(bArr), algorithmIdentifier2.getAlgorithm().getId());
        } catch (InvalidKeyException e2) {
            throw new CRMFException("key invalid in message.", e2);
        } catch (IllegalBlockSizeException e3) {
            throw new CRMFException("illegal blocksize in message.", e3);
        } catch (BadPaddingException e4) {
            throw new CRMFException("bad padding in message.", e4);
        }
    }

    public InputDecryptor getValueDecryptor(AlgorithmIdentifier algorithmIdentifier, final AlgorithmIdentifier algorithmIdentifier2, byte[] bArr) throws CRMFException {
        final Cipher createContentCipher = this.helper.createContentCipher(extractSecretKey(algorithmIdentifier, algorithmIdentifier2, bArr), algorithmIdentifier2);
        return new InputDecryptor() {
            public AlgorithmIdentifier getAlgorithmIdentifier() {
                return algorithmIdentifier2;
            }

            public InputStream getInputStream(InputStream inputStream) {
                return new CipherInputStream(inputStream, createContentCipher);
            }
        };
    }

    public JceAsymmetricValueDecryptorGenerator setProvider(String str) {
        this.helper = new CRMFHelper(new NamedJcaJceHelper(str));
        return this;
    }

    public JceAsymmetricValueDecryptorGenerator setProvider(Provider provider) {
        this.helper = new CRMFHelper(new ProviderJcaJceHelper(provider));
        return this;
    }
}
