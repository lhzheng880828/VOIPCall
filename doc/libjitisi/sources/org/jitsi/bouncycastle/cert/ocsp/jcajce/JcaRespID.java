package org.jitsi.bouncycastle.cert.ocsp.jcajce;

import java.security.PublicKey;
import javax.security.auth.x500.X500Principal;
import org.jitsi.bouncycastle.asn1.x500.X500Name;
import org.jitsi.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.jitsi.bouncycastle.cert.ocsp.OCSPException;
import org.jitsi.bouncycastle.cert.ocsp.RespID;
import org.jitsi.bouncycastle.operator.DigestCalculator;

public class JcaRespID extends RespID {
    public JcaRespID(PublicKey publicKey, DigestCalculator digestCalculator) throws OCSPException {
        super(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()), digestCalculator);
    }

    public JcaRespID(X500Principal x500Principal) {
        super(X500Name.getInstance(x500Principal.getEncoded()));
    }
}
