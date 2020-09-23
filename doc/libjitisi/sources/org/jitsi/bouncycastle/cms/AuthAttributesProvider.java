package org.jitsi.bouncycastle.cms;

import org.jitsi.bouncycastle.asn1.ASN1Set;

interface AuthAttributesProvider {
    ASN1Set getAuthAttributes();
}
