package org.jitsi.bouncycastle.cms.bc;

import org.jitsi.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.jitsi.bouncycastle.cert.X509CertificateHolder;
import org.jitsi.bouncycastle.cms.KeyTransRecipientInfoGenerator;
import org.jitsi.bouncycastle.operator.AsymmetricKeyWrapper;
import org.jitsi.bouncycastle.operator.bc.BcAsymmetricKeyWrapper;

public abstract class BcKeyTransRecipientInfoGenerator extends KeyTransRecipientInfoGenerator {
    public BcKeyTransRecipientInfoGenerator(X509CertificateHolder x509CertificateHolder, BcAsymmetricKeyWrapper bcAsymmetricKeyWrapper) {
        super(new IssuerAndSerialNumber(x509CertificateHolder.toASN1Structure()), (AsymmetricKeyWrapper) bcAsymmetricKeyWrapper);
    }

    public BcKeyTransRecipientInfoGenerator(byte[] bArr, BcAsymmetricKeyWrapper bcAsymmetricKeyWrapper) {
        super(bArr, (AsymmetricKeyWrapper) bcAsymmetricKeyWrapper);
    }
}
