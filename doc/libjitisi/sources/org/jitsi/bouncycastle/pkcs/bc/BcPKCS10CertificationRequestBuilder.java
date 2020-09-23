package org.jitsi.bouncycastle.pkcs.bc;

import java.io.IOException;
import org.jitsi.bouncycastle.asn1.x500.X500Name;
import org.jitsi.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.jitsi.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.jitsi.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;

public class BcPKCS10CertificationRequestBuilder extends PKCS10CertificationRequestBuilder {
    public BcPKCS10CertificationRequestBuilder(X500Name x500Name, AsymmetricKeyParameter asymmetricKeyParameter) throws IOException {
        super(x500Name, SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(asymmetricKeyParameter));
    }
}
