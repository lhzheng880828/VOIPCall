package org.jitsi.bouncycastle.operator.jcajce;

import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.jitsi.bouncycastle.jcajce.DefaultJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.NamedJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.ProviderJcaJceHelper;
import org.jitsi.bouncycastle.operator.AsymmetricKeyWrapper;
import org.jitsi.bouncycastle.operator.GenericKey;
import org.jitsi.bouncycastle.operator.OperatorException;

public class JceAsymmetricKeyWrapper extends AsymmetricKeyWrapper {
    private Map extraMappings;
    private OperatorHelper helper;
    private PublicKey publicKey;
    private SecureRandom random;

    public JceAsymmetricKeyWrapper(PublicKey publicKey) {
        super(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()).getAlgorithm());
        this.helper = new OperatorHelper(new DefaultJcaJceHelper());
        this.extraMappings = new HashMap();
        this.publicKey = publicKey;
    }

    public JceAsymmetricKeyWrapper(X509Certificate x509Certificate) {
        this(x509Certificate.getPublicKey());
    }

    public byte[] generateWrappedKey(GenericKey genericKey) throws OperatorException {
        Cipher createAsymmetricWrapper = this.helper.createAsymmetricWrapper(getAlgorithmIdentifier().getAlgorithm(), this.extraMappings);
        byte[] bArr = null;
        try {
            createAsymmetricWrapper.init(3, this.publicKey, this.random);
            bArr = createAsymmetricWrapper.wrap(OperatorUtils.getJceKey(genericKey));
        } catch (IllegalStateException | UnsupportedOperationException | GeneralSecurityException | ProviderException e) {
        }
        if (bArr != null) {
            return bArr;
        }
        try {
            createAsymmetricWrapper.init(1, this.publicKey, this.random);
            return createAsymmetricWrapper.doFinal(OperatorUtils.getJceKey(genericKey).getEncoded());
        } catch (GeneralSecurityException e2) {
            throw new OperatorException("unable to encrypt contents key", e2);
        }
    }

    public JceAsymmetricKeyWrapper setAlgorithmMapping(ASN1ObjectIdentifier aSN1ObjectIdentifier, String str) {
        this.extraMappings.put(aSN1ObjectIdentifier, str);
        return this;
    }

    public JceAsymmetricKeyWrapper setProvider(String str) {
        this.helper = new OperatorHelper(new NamedJcaJceHelper(str));
        return this;
    }

    public JceAsymmetricKeyWrapper setProvider(Provider provider) {
        this.helper = new OperatorHelper(new ProviderJcaJceHelper(provider));
        return this;
    }

    public JceAsymmetricKeyWrapper setSecureRandom(SecureRandom secureRandom) {
        this.random = secureRandom;
        return this;
    }
}
