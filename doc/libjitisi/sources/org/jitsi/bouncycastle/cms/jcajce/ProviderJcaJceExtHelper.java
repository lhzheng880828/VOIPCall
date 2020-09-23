package org.jitsi.bouncycastle.cms.jcajce;

import java.security.PrivateKey;
import java.security.Provider;
import javax.crypto.SecretKey;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.jcajce.ProviderJcaJceHelper;
import org.jitsi.bouncycastle.operator.SymmetricKeyUnwrapper;
import org.jitsi.bouncycastle.operator.jcajce.JceAsymmetricKeyUnwrapper;
import org.jitsi.bouncycastle.operator.jcajce.JceSymmetricKeyUnwrapper;

class ProviderJcaJceExtHelper extends ProviderJcaJceHelper implements JcaJceExtHelper {
    public ProviderJcaJceExtHelper(Provider provider) {
        super(provider);
    }

    public JceAsymmetricKeyUnwrapper createAsymmetricUnwrapper(AlgorithmIdentifier algorithmIdentifier, PrivateKey privateKey) {
        return new JceAsymmetricKeyUnwrapper(algorithmIdentifier, privateKey).setProvider(this.provider);
    }

    public SymmetricKeyUnwrapper createSymmetricUnwrapper(AlgorithmIdentifier algorithmIdentifier, SecretKey secretKey) {
        return new JceSymmetricKeyUnwrapper(algorithmIdentifier, secretKey).setProvider(this.provider);
    }
}
