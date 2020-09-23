package org.jitsi.bouncycastle.cert.jcajce;

import java.io.IOException;
import org.jitsi.bouncycastle.asn1.x509.AttributeCertificate;
import org.jitsi.bouncycastle.cert.X509AttributeCertificateHolder;
import org.jitsi.bouncycastle.x509.X509AttributeCertificate;

public class JcaX509AttributeCertificateHolder extends X509AttributeCertificateHolder {
    public JcaX509AttributeCertificateHolder(X509AttributeCertificate x509AttributeCertificate) throws IOException {
        super(AttributeCertificate.getInstance(x509AttributeCertificate.getEncoded()));
    }
}
