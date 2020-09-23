package org.jitsi.bouncycastle.eac.operator;

import java.io.OutputStream;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface EACSigner {
    OutputStream getOutputStream();

    byte[] getSignature();

    ASN1ObjectIdentifier getUsageIdentifier();
}
