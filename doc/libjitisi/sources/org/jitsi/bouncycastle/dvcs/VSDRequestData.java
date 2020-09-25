package org.jitsi.bouncycastle.dvcs;

import org.jitsi.bouncycastle.asn1.dvcs.Data;
import org.jitsi.bouncycastle.cms.CMSException;
import org.jitsi.bouncycastle.cms.CMSSignedData;

public class VSDRequestData extends DVCSRequestData {
    private CMSSignedData doc;

    VSDRequestData(Data data) throws DVCSConstructionException {
        super(data);
        initDocument();
    }

    private void initDocument() throws DVCSConstructionException {
        if (this.doc != null) {
            return;
        }
        if (this.data.getMessage() == null) {
            throw new DVCSConstructionException("DVCSRequest.data.message should be specified for VSD service");
        }
        try {
            this.doc = new CMSSignedData(this.data.getMessage().getOctets());
        } catch (CMSException e) {
            throw new DVCSConstructionException("Can't read CMS SignedData from input", e);
        }
    }

    public byte[] getMessage() {
        return this.data.getMessage().getOctets();
    }

    public CMSSignedData getParsedMessage() {
        return this.doc;
    }
}