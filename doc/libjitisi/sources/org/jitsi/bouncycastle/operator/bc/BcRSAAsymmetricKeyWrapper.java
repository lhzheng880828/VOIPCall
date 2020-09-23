package org.jitsi.bouncycastle.operator.bc;

import java.io.IOException;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.jitsi.bouncycastle.crypto.AsymmetricBlockCipher;
import org.jitsi.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.jitsi.bouncycastle.crypto.engines.RSAEngine;
import org.jitsi.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.jitsi.bouncycastle.crypto.util.PublicKeyFactory;

public class BcRSAAsymmetricKeyWrapper extends BcAsymmetricKeyWrapper {
    public BcRSAAsymmetricKeyWrapper(AlgorithmIdentifier algorithmIdentifier, SubjectPublicKeyInfo subjectPublicKeyInfo) throws IOException {
        super(algorithmIdentifier, PublicKeyFactory.createKey(subjectPublicKeyInfo));
    }

    public BcRSAAsymmetricKeyWrapper(AlgorithmIdentifier algorithmIdentifier, AsymmetricKeyParameter asymmetricKeyParameter) {
        super(algorithmIdentifier, asymmetricKeyParameter);
    }

    /* access modifiers changed from: protected */
    public AsymmetricBlockCipher createAsymmetricWrapper(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return new PKCS1Encoding(new RSAEngine());
    }
}
