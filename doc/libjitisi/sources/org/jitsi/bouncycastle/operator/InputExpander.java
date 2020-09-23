package org.jitsi.bouncycastle.operator;

import java.io.InputStream;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;

public interface InputExpander {
    AlgorithmIdentifier getAlgorithmIdentifier();

    InputStream getInputStream(InputStream inputStream);
}
