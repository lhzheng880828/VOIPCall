package org.jitsi.bouncycastle.operator;

import java.io.OutputStream;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;

public interface MacCalculator {
    AlgorithmIdentifier getAlgorithmIdentifier();

    GenericKey getKey();

    byte[] getMac();

    OutputStream getOutputStream();
}
