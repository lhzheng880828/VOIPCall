package org.jitsi.bouncycastle.cert.ocsp.jcajce;

import java.security.PublicKey;
import org.jitsi.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.jitsi.bouncycastle.cert.ocsp.BasicOCSPRespBuilder;
import org.jitsi.bouncycastle.cert.ocsp.OCSPException;
import org.jitsi.bouncycastle.operator.DigestCalculator;

public class JcaBasicOCSPRespBuilder extends BasicOCSPRespBuilder {
    public JcaBasicOCSPRespBuilder(PublicKey publicKey, DigestCalculator digestCalculator) throws OCSPException {
        super(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()), digestCalculator);
    }
}
