package org.jitsi.bouncycastle.cert.crmf;

import org.jitsi.bouncycastle.asn1.ASN1Encodable;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface Control {
    ASN1ObjectIdentifier getType();

    ASN1Encodable getValue();
}
