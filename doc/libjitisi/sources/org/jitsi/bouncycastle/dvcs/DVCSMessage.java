package org.jitsi.bouncycastle.dvcs;

import org.jitsi.bouncycastle.asn1.ASN1Encodable;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.cms.ContentInfo;

public abstract class DVCSMessage {
    private final ContentInfo contentInfo;

    protected DVCSMessage(ContentInfo contentInfo) {
        this.contentInfo = contentInfo;
    }

    public abstract ASN1Encodable getContent();

    public ASN1ObjectIdentifier getContentType() {
        return this.contentInfo.getContentType();
    }
}
