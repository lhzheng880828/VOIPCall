package org.jitsi.bouncycastle.eac.operator;

import java.io.OutputStream;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface EACSignatureVerifier {
    OutputStream getOutputStream();

    ASN1ObjectIdentifier getUsageIdentifier();

    boolean verify(byte[] bArr);
}
