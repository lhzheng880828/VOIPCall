package org.jitsi.bouncycastle.cert.crmf.jcajce;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import org.jitsi.bouncycastle.asn1.crmf.EncryptedValue;
import org.jitsi.bouncycastle.cert.X509CertificateHolder;
import org.jitsi.bouncycastle.cert.crmf.CRMFException;
import org.jitsi.bouncycastle.cert.crmf.EncryptedValueBuilder;
import org.jitsi.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.jitsi.bouncycastle.operator.KeyWrapper;
import org.jitsi.bouncycastle.operator.OutputEncryptor;

public class JcaEncryptedValueBuilder extends EncryptedValueBuilder {
    public JcaEncryptedValueBuilder(KeyWrapper keyWrapper, OutputEncryptor outputEncryptor) {
        super(keyWrapper, outputEncryptor);
    }

    public EncryptedValue build(X509Certificate x509Certificate) throws CertificateEncodingException, CRMFException {
        return build((X509CertificateHolder) new JcaX509CertificateHolder(x509Certificate));
    }
}
