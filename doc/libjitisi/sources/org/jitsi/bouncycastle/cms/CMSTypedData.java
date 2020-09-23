package org.jitsi.bouncycastle.cms;

import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface CMSTypedData extends CMSProcessable {
    ASN1ObjectIdentifier getContentType();
}
