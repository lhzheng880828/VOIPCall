package org.jitsi.bouncycastle.operator;

import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.cert.X509CertificateHolder;

public interface ContentVerifierProvider {
    ContentVerifier get(AlgorithmIdentifier algorithmIdentifier) throws OperatorCreationException;

    X509CertificateHolder getAssociatedCertificate();

    boolean hasAssociatedCertificate();
}
