package org.jitsi.bouncycastle.cms.jcajce;

import java.security.PrivateKey;
import javax.crypto.SecretKey;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.jcajce.DefaultJcaJceHelper;
import org.jitsi.bouncycastle.operator.SymmetricKeyUnwrapper;
import org.jitsi.bouncycastle.operator.jcajce.JceAsymmetricKeyUnwrapper;
import org.jitsi.bouncycastle.operator.jcajce.JceSymmetricKeyUnwrapper;

class DefaultJcaJceExtHelper extends DefaultJcaJceHelper implements JcaJceExtHelper {
    DefaultJcaJceExtHelper() {
    }

    public JceAsymmetricKeyUnwrapper createAsymmetricUnwrapper(AlgorithmIdentifier algorithmIdentifier, PrivateKey privateKey) {
        return new JceAsymmetricKeyUnwrapper(algorithmIdentifier, privateKey);
    }

    public SymmetricKeyUnwrapper createSymmetricUnwrapper(AlgorithmIdentifier algorithmIdentifier, SecretKey secretKey) {
        return new JceSymmetricKeyUnwrapper(algorithmIdentifier, secretKey);
    }
}
