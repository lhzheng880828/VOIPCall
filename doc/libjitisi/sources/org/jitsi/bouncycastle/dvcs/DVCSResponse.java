package org.jitsi.bouncycastle.dvcs;

import org.jitsi.bouncycastle.asn1.ASN1Encodable;
import org.jitsi.bouncycastle.asn1.ASN1OctetString;
import org.jitsi.bouncycastle.asn1.ASN1Sequence;
import org.jitsi.bouncycastle.asn1.cms.ContentInfo;
import org.jitsi.bouncycastle.asn1.cms.SignedData;
import org.jitsi.bouncycastle.asn1.dvcs.DVCSObjectIdentifiers;
import org.jitsi.bouncycastle.cms.CMSSignedData;

public class DVCSResponse extends DVCSMessage {
    private org.jitsi.bouncycastle.asn1.dvcs.DVCSResponse asn1;

    public DVCSResponse(ContentInfo contentInfo) throws DVCSConstructionException {
        super(contentInfo);
        if (DVCSObjectIdentifiers.id_ct_DVCSResponseData.equals(contentInfo.getContentType())) {
            try {
                if (contentInfo.getContent().toASN1Primitive() instanceof ASN1Sequence) {
                    this.asn1 = org.jitsi.bouncycastle.asn1.dvcs.DVCSResponse.getInstance(contentInfo.getContent());
                    return;
                } else {
                    this.asn1 = org.jitsi.bouncycastle.asn1.dvcs.DVCSResponse.getInstance(ASN1OctetString.getInstance(contentInfo.getContent()).getOctets());
                    return;
                }
            } catch (Exception e) {
                throw new DVCSConstructionException("Unable to parse content: " + e.getMessage(), e);
            }
        }
        throw new DVCSConstructionException("ContentInfo not a DVCS Request");
    }

    public DVCSResponse(CMSSignedData cMSSignedData) throws DVCSConstructionException {
        this(SignedData.getInstance(cMSSignedData.toASN1Structure().getContent()).getEncapContentInfo());
    }

    public ASN1Encodable getContent() {
        return this.asn1;
    }
}
