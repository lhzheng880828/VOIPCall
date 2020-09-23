package org.jitsi.bouncycastle.pkcs;

import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.operator.MacCalculator;
import org.jitsi.bouncycastle.operator.OperatorCreationException;

public interface PKCS12MacCalculatorBuilder {
    MacCalculator build(char[] cArr) throws OperatorCreationException;

    AlgorithmIdentifier getDigestAlgorithmIdentifier();
}
