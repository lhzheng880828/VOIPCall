package org.jitsi.bouncycastle.pkcs.bc;

import java.io.IOException;
import org.jitsi.bouncycastle.asn1.pkcs.CertificationRequest;
import org.jitsi.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.jitsi.bouncycastle.crypto.util.PublicKeyFactory;
import org.jitsi.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.jitsi.bouncycastle.pkcs.PKCSException;

public class BcPKCS10CertificationRequest extends PKCS10CertificationRequest {
    public BcPKCS10CertificationRequest(CertificationRequest certificationRequest) {
        super(certificationRequest);
    }

    public BcPKCS10CertificationRequest(PKCS10CertificationRequest pKCS10CertificationRequest) {
        super(pKCS10CertificationRequest.toASN1Structure());
    }

    public BcPKCS10CertificationRequest(byte[] bArr) throws IOException {
        super(bArr);
    }

    public AsymmetricKeyParameter getPublicKey() throws PKCSException {
        try {
            return PublicKeyFactory.createKey(getSubjectPublicKeyInfo());
        } catch (IOException e) {
            throw new PKCSException("error extracting key encoding: " + e.getMessage(), e);
        }
    }
}
