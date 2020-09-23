package org.jitsi.bouncycastle.dvcs;

import org.jitsi.bouncycastle.asn1.dvcs.Data;

public class CPDRequestData extends DVCSRequestData {
    CPDRequestData(Data data) throws DVCSConstructionException {
        super(data);
        initMessage();
    }

    private void initMessage() throws DVCSConstructionException {
        if (this.data.getMessage() == null) {
            throw new DVCSConstructionException("DVCSRequest.data.message should be specified for CPD service");
        }
    }

    public byte[] getMessage() {
        return this.data.getMessage().getOctets();
    }
}
