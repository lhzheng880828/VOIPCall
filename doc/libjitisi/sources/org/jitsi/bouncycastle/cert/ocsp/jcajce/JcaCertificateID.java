package org.jitsi.bouncycastle.cert.ocsp.jcajce;

import java.math.BigInteger;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import org.jitsi.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.jitsi.bouncycastle.cert.ocsp.CertificateID;
import org.jitsi.bouncycastle.cert.ocsp.OCSPException;
import org.jitsi.bouncycastle.operator.DigestCalculator;

public class JcaCertificateID extends CertificateID {
    public JcaCertificateID(DigestCalculator digestCalculator, X509Certificate x509Certificate, BigInteger bigInteger) throws OCSPException, CertificateEncodingException {
        super(digestCalculator, new JcaX509CertificateHolder(x509Certificate), bigInteger);
    }
}
